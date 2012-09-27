/*
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.nnapz.hbaseexplorer;

import java.io.IOException;
import java.util.*;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.hbase.HTableDescriptor;
import org.apache.hadoop.hbase.MasterNotRunningException;
import org.apache.hadoop.hbase.client.Get;
import org.apache.hadoop.hbase.client.HBaseAdmin;
import org.apache.hadoop.hbase.client.HTable;
import org.apache.hadoop.hbase.client.HTableInterface;
import org.apache.hadoop.hbase.client.HTablePool;
import org.apache.hadoop.hbase.client.NoServerForRegionException;
import org.apache.hadoop.hbase.client.RegionOfflineException;
import org.apache.hadoop.hbase.client.Result;
import org.apache.hadoop.hbase.client.ResultScanner;
import org.apache.hadoop.hbase.client.RetriesExhaustedException;
import org.apache.hadoop.hbase.client.Scan;
import org.apache.hadoop.hbase.mapreduce.TableInputFormat;
import org.apache.hadoop.hbase.mapreduce.TableSplit;
import org.apache.hadoop.hbase.util.Bytes;
import org.apache.hadoop.mapreduce.InputSplit;
import org.apache.hadoop.mapreduce.Job;
import org.apache.log4j.Logger;

import com.nnapz.hbaseexplorer.domain.HbaseSource;
import com.nnapz.hbaseexplorer.mr.TableStats;
import com.nnapz.hbaseexplorer.services.ConfigHolderService;

/**
 * Wrapper to access HBase. I decided to do this in Java, because (likely) changes in the API will rather lead to an
 * compile time error than with the magic of groovy.
 *
 * This class is a celebration of java collections.
 * 
 * @author Bob Schulze
 */
public class HBaseClient {

	public static final int TABLE_POOL_MAX_SIZE = 1000;

	private static final Logger log = Logger.getLogger(HBaseClient.class);
	
	private ConfigurationHolder configurationHolder;
	private HTablePool hTablePool;
	
    // for UI interaction to create tables
    public static final String FAMILY_VERSIONS = "FAMILY_VERSIONS";
    public static final String FAMILY_TTL = "FAMILY_TTL";
    public static final String FAMILY_BLOCKCACHE = "FAMILY_BLOCKCACHE";
    public static final String FAMILY_BLOCKSIZE = "FAMILY_BLOCKSIZE";
    public static final String FAMILY_BLOOMFILTER = "FAMILY_BLOOMFILTER";
    public static final String FAMILY_INMEMORY = "FAMILY_INMEMORY";
    public static final String FAMILY_COMPRESSION = "FAMILY_COMPRESSION";

    /**
     * Setup a client
     */
    public HBaseClient(HTablePool tablePool, ConfigurationHolder holder) {
       hTablePool = tablePool;
       configurationHolder = holder;
    }

    /**
     * List all tables.
     * @return HTableDescriptor's or null
     * @throws IOException
     */
    public HTableDescriptor[] listTables() throws IOException {
        return getHBaseAdmin().listTables();
    }

    public HTableDescriptor getTableDescriptor(String tableName) throws IOException {
        return getHBaseAdmin().getTableDescriptor(tableName.getBytes());
    }

    // forget this for now...
    public void executeRowCount(String tableName) throws IOException, ClassNotFoundException, InterruptedException {
        Job job = TableStats.createSubmittableJob(configurationHolder.getConf(), tableName);
        job.waitForCompletion(false);
    }

    /**
     * Hand out the Hbase Admin
     * @return the admin interface
     * @throws MasterNotRunningException if the master...
     */
    public HBaseAdmin getHBaseAdmin() throws IOException {
        return this.configurationHolder.getAdmin();
    }

    
    // TODO : extract closure, execute HBase API calls inside of checkout -> commit

    /**
     * Issues a Get to the table. Provides a map ordered by timestamps as key. Unlike scan(), returns nothing if the row
     * is now found.
     * @param tableName the table to get the data from
     * @param rowKey  the row key to look up
     * @param versions number of versions to be returned for each column value
     * @return a map ts->family-column->value or null, if there is no result
     * @throws IOException on any hbase IO problem
     */
    public TreeMap<Long, HashMap<String, HashMap<byte[], byte[]>>> get(String tableName,
                                                                       String rowKey, int versions) throws IOException {
        HTableInterface hTable = this.hTablePool.getTable(tableName);
        try {
	        Get get = new Get(rowKey.getBytes());
	        get.setMaxVersions(versions);
	        Result result = hTable.get(get);
	        // family->column->ts -> value
	        NavigableMap<byte[], NavigableMap<byte[], NavigableMap<Long, byte[]>>> map = result.getMap();
	        if (map == null || map.size() == 0) {
	            return null;
	        }
	        TreeMap<Long, HashMap<String, HashMap<byte[], byte[]>>> output = remapByTimestamp(map);
	        return output;
        } finally {
        	this.hTablePool.putTable(hTable);
        }

    }
    
    /**
     * Provide the results of a scan back as sorted arraylist with complete rows inside, grouped by timestamp
     * @param tableName table to scan
     * @param rowKey start row key ( a "startsWith" pattern)
     * @param versions number of versions for each qualifier
     * @param rows number of rows to be returned
     * @return a list of row results about as bif as the rows spec above
     * @throws IOException on any HBase IO problem
     */
    public TreeMap<byte[], TreeMap<Long, HashMap<String, HashMap<byte[], byte[]>>>> scan(
            String tableName, byte[] rowKey, int versions, int rows)
            throws IOException {
        HTableInterface hTable = this.hTablePool.getTable(tableName);
        ResultScanner scanner = null;
        try {
        	long startTimeScan = System.currentTimeMillis();
			Scan scan = new Scan();
			scan.setMaxVersions(versions);
			scan.setStartRow(rowKey);
			scan.setCaching(rows);
			scanner = hTable.getScanner(scan);
			TreeMap<byte[], TreeMap<Long, HashMap<String, HashMap<byte[], byte[]>>>> res =
			        new TreeMap<byte[], TreeMap<Long, HashMap<String, HashMap<byte[], byte[]>>>>();

			// fill a list with the re-mapped results
			Result[] results = scanner.next(rows);
			long stopTimeScan = System.currentTimeMillis();
			long timeForScan = stopTimeScan - startTimeScan;
            log.debug(String.format("Scan - tableName : %s, startRowKey : %s, versions : %d, numRows : %d took %d ms",
                    tableName, Bytes.toString(rowKey), versions, rows, timeForScan));

			long startTimeRemapping = System.currentTimeMillis();
			for (Result row: results) {
			    NavigableMap<byte[], NavigableMap<byte[], NavigableMap<Long, byte[]>>> map = row.getMap();
			    TreeMap<Long, HashMap<String, HashMap<byte[], byte[]>>> rowByTs = remapByTimestamp(map);
			    res.put(row.getRow(), rowByTs);
			}
			long stopTimeRemapping = System.currentTimeMillis();
			long timeForRemapping = stopTimeRemapping - startTimeRemapping;
            log.debug(String.format("Remapping of %d rows took %d ms", results.length, timeForRemapping));

			if (res.size() == 0) {
			    return null;
			}
			return res;
		} finally {
			if (scanner != null) {
				scanner.close();
			}
			this.hTablePool.putTable(hTable);
		}
    }

    /**
     * Fetches number of "rows" from HBase and returns them as Result[]
     * @param tableName table to scan
     * @param rowKey start row key ( a "startsWith" pattern)
     * @param versions number of versions for each qualifier
     * @param rows number of rows to be returned
     * @return a Result[]; possibly null
     * @throws IOException on any HBase IO problem
     */
    public Result[] scanAsResults(String tableName, byte[] rowKey, int versions, int rows) throws IOException {
        HTableInterface hTable = this.hTablePool.getTable(tableName);
        ResultScanner scanner = null;
        try {
			Scan scan = new Scan();
			scan.setMaxVersions(versions);
			scan.setStartRow(rowKey);
			scan.setCaching(rows);
			scanner = hTable.getScanner(scan);

			return scanner.next(rows);
		} finally {
			if (scanner != null) {
				scanner.close();
			}
			this.hTablePool.putTable(hTable);
		}
    }

    /**
     * Remaps a given result map from hbase to an order by timestamp
     * @param map a map as returned by Result.getMap()
     * @return a map  ts->family->column->value
     */
    public static TreeMap<Long, HashMap<String, HashMap<byte[], byte[]>>>
        remapByTimestamp(NavigableMap<byte[], NavigableMap<byte[], NavigableMap<Long, byte[]>>> map) {

        TreeMap<Long, HashMap<String, HashMap<byte[], byte[]>>> output =
                new TreeMap<Long, HashMap<String, HashMap<byte[], byte[]>>>();

        for (byte[] familyNameBytes : map.keySet()) {
            String familyName = new String(familyNameBytes);
            //System.out.println("familyNameBytes " + familyName);

            NavigableMap<byte[], NavigableMap<Long, byte[]>> columns = map.get(familyNameBytes);
            for (byte[] columnNameBytes : columns.keySet()) {
                //System.out.println("    column " + columnName);

                NavigableMap<Long, byte[]> values = columns.get(columnNameBytes);
                for (Long ts : values.keySet()) {
                    byte[] valueBytes = values.get(ts);
                    //System.out.println("      ts\t" + new Date(ts) + "\t" + value);

                    HashMap<String, HashMap<byte[], byte[]>> families = output.get(ts);
                    if (families == null) {
                        families = new HashMap<String, HashMap<byte[], byte[]>>();
                        output.put(ts, families);
                    }
                    HashMap<byte[], byte[]> family = families.get(familyName);
                    if (family == null) {
                        family = new HashMap<byte[], byte[]>();
                        families.put(familyName, family);
                    }

                    family.put(columnNameBytes, valueBytes);
                }

            }
        }
        return output;
    }

    private void print(TreeMap<Long, HashMap<String, HashMap<byte[], byte[]>>> output) {
        // collect all families
        ArrayList<String> allFamilies = new ArrayList<String>();
        Long[] timestamps = output.keySet().toArray(new Long[output.size()]);
        for (Long ts : timestamps) {
            HashMap<String, HashMap<byte[], byte[]>> families = output.get(ts);
            for (String familyName : families.keySet()) {
                if (!allFamilies.contains(familyName)) allFamilies.add(familyName);
            }
        }


        timestamps = output.keySet().toArray(new Long[output.size()]);
        Arrays.sort(timestamps);
        for (Long ts : timestamps) {
            System.out.println(new Date(ts) + " (" + ts + ")");
            HashMap<String, HashMap<byte[], byte[]>> families = output.get(ts);
            for (String familyName : allFamilies) {
                HashMap<byte[], byte[]> columns = families.get(familyName);
                if (columns != null) {
                    for (byte[] column : columns.keySet()) {
                        String value = new String(columns.get(column));
                        System.out.println("  " + familyName + ":" + new String(column) + "=" + value);
                    }
                }
            }
        }

    }

    // testing, code from hbase project
    class MyTableInputFormat extends TableInputFormat {

        MyTableInputFormat(HTable htable) {
            super();
            setHTable(htable);
        }
    }

    /**
     * Poor Men's version of htable.isTableEnabled(). Much faster as it just attempts a scan instead of asking all
     * regions for their opinion.
     * @param tableName the table name to check
     * @return  true if the scan seemed to work.
     * @throws IOException anything beside the expeczted exception if a table is offline.
     */
    public boolean checkOnline(String tableName) throws IOException {
       try {
           scan(tableName, Bytes.toBytes("a"), 1, 1);
       } catch (RegionOfflineException ex) {
           return false;
       } catch (RetriesExhaustedException ex) {
           return false;
       } catch (NoServerForRegionException ex) {
           return false;
       }


       return true;
    }

    /**
     * Provide a region count as a rough size estimation
     * @param tableName the table to gather info from
     * @return the number of (online) regions
     * @throws IOException  on any hbase IO  problem
     */
    public int getRegionCount(String tableName) throws IOException {
        HTableInterface htable = this.hTablePool.getTable(tableName);
        if (htable instanceof HTable) {
        	return ((HTable)htable).getRegionsInfo().size();
		}
        throw new RuntimeException("No HTable instance ? (" + htable + ")");
    }

    /**
     * Create and start a M/R statistics Job
     * @param tableName the table we want stats for
     * @return the newly created job for status polling etc
     * @throws Exception on any Hbase problem
     */
    public Job pushTableStats(String tableName) throws Exception {
        if (!checkOnline(tableName)) return null;       // todo ex if no jobtracker was set up
        Job job = TableStats.createSubmittableJob(configurationHolder.getConf(), tableName);
        job.submit(); 
        return job;
    }

    /**
     * Attempt to scan the whole table w/o M/R. Expect this to run a while. With this version, we would need to copy all
     * data across the network, which is of course stupid. This code is probably removed soon.
     *
     * NOT USED / EXPERIMENTAL, waiting for 0.21.
     *
     * @param tableName  the name of the table to count
     * @return
     * see TableInputFormatBase#getSplits()
     * @throws InterruptedException on region access interruption
     * @throws java.io.IOException on Hbase IO
     */
    public long countRows(String tableName) throws IOException, InterruptedException {
    	HTableInterface htable = this.hTablePool.getTable(tableName);
    	if (!(htable instanceof HTable)) {
    		throw new RuntimeException("No HTable instance ? (" + htable + ")");
		}
    	
        MyTableInputFormat tif = new MyTableInputFormat((HTable)htable);

        long count = 0;

        Scan fts = new Scan();    // full table scan
        tif.setScan(fts);

        List<InputSplit> splits = tif.getSplits(/* JobConf */ null);   // at least 0.20.2 does not make use of JobConf

        log.debug("splits = " + splits);
        
        // we have now as many splits as we have regions. Using our own little thread pooling, we
        // go now to all regions for counting.
        int splitcnt = 0;
        for (InputSplit split: splits) {
            TableSplit tis = (TableSplit) split;
            // todo threads
            Scan scan = new Scan(); // setup scan to scan exactly from-to
            scan.setMaxVersions(1);
            scan.setStartRow(tis.getStartRow());
            scan.setStopRow(tis.getEndRow());
            // todo min content ?

            // note: no locking, so we may experience a region split here. Possibly we'll loose newly added rows at the end

            long start = System.currentTimeMillis();
            ResultScanner regionResult = htable.getScanner(scan);
            System.out.println("regionResult = " + (System.currentTimeMillis() - start));

            int CHUNK = 15000;
            while (regionResult.next() != null)  {
                count++;       // todo: faster if in chunks?
                if (count % CHUNK == 0) {
                	 log.debug("count after " + tableName + " Split " + splitcnt + "/" + splits.size() + " :" + count+
                        " (" + (System.currentTimeMillis() - start) + "ms)");
                }
            }
            regionResult.close();
            splitcnt++;
        }

        return count;
    }


    public static void main(String[] args) {
        try {
        	HbaseSource src = new HbaseSource();
        	src.setName("temp");
        	src.setQuorumServers(args[0]);
        	src.setQuorumPort(Integer.parseInt(args[1]));
        	src.setMasterUrl("none");
            ConfigurationHolder configHolder = new ConfigHolderService().getConfigHolder(src);
            Configuration conf = configHolder.getConf();
			HBaseClient hbc = new HBaseClient(new HTablePool(conf, TABLE_POOL_MAX_SIZE), configHolder);
            String rowKey = args[3];
            TreeMap<Long, HashMap<String, HashMap<byte[], byte[]>>> o = hbc.get(args[2], rowKey, 100);
            if (o == null || o.size() == 0) {
                System.out.println("No Result");
            } else {
                hbc.print(o);
                System.out.println("Rows for " + rowKey + ": " + o.size());
            }
            // todo timiing
        } catch (Throwable twb) {
            twb.printStackTrace();
        }
    }
}
