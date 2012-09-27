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
package com.nnapz.hbaseexplorer.mr;

import java.io.IOException;
import java.util.HashSet;
import java.util.NavigableMap;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.hbase.HBaseConfiguration;
import org.apache.hadoop.hbase.util.Bytes;
import org.apache.hadoop.hbase.client.Result;
import org.apache.hadoop.hbase.client.Scan;
import org.apache.hadoop.hbase.io.ImmutableBytesWritable;
import org.apache.hadoop.hbase.mapreduce.TableMapReduceUtil;
import org.apache.hadoop.hbase.mapreduce.TableMapper;
import org.apache.hadoop.io.NullWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Job;
import org.apache.hadoop.mapreduce.lib.output.NullOutputFormat;
import org.apache.hadoop.util.GenericOptionsParser;

/**
 * Get some stats about a table.  Currently to be executed using "hadoop jar ...", but its supposed to get under
 * the UI control.
 *
 * @author Bob Schulze
 */
public class TableStats {

    public static final String NAME = "hbe_rc";
    public static final String COUNTER_GROUP_PREFIX = "hbe_";                                  // all counters from hbe_rc
    public static final String COUNTER_GROUP_FAMILY_PREFIX = COUNTER_GROUP_PREFIX + "FAMILY_"; // family related
    public static final String COUNTER_GROUP = COUNTER_GROUP_PREFIX + "TOTAL";                 // table related

    public static final String RETRIEVED_ROWS = "RETRIEVED_ROWS";    // row count
    public static final String VALUES_COUNT = "VALUES_COUNT";   // sum of number of values (if they exists for a column)
                                                              //    for the whole table or a family
    public static final String VALUES_SIZE = "VALUES_SIZE";     // sum of size of values
    public static final String COLUMNS_SIZE = "COLUMNS_SIZE";   // size of columns for a family
    public static final String TOTAL_TIMESTAMPS = "TOTAL_TIMESTAMPS";    // for the whole table or a family
                                                              // for a record: unique per record
                                                              // for a family: total entries for a timestamp
    public static final String COLUMNS_COUNT = "COLUMNS_COUNT"; // sum of column qualifiers that exist
    public static final String DATA_SIZE = "DATA_SIZE";         // data size
    public static final String SUM_TIME_PER_MAP = "SUM_TIME_PER_MAP";    // sum of time needed per map
    public static final String EXCEPTIONS_MAPPER = "EXCEPTIONS_MAPPER";  // sum of exceptions

   /**
    * Follows the normal M/R patterns
    */
  static class RowCountMapper extends TableMapper<NullWritable, NullWritable> {


       /**
     * M/R map call
     */
    @Override
    public void map(ImmutableBytesWritable row, Result result, Context context)
        throws IOException {
            long start = System.currentTimeMillis();
            try {
                context.getCounter(COUNTER_GROUP, RETRIEVED_ROWS).increment(1L);

                int columnCount = 0;
                int valueCount = 0;
                int columnSize = 0;
                int valueSize = 0;
                NavigableMap<byte[], NavigableMap<byte[], NavigableMap<Long, byte[]>>> rowData = result.getMap();
                HashSet<Long> timestamps = new HashSet<Long>();
                for (byte[] family: rowData.keySet()) {
                    String familyName = Bytes.toString(family);
                    NavigableMap<byte[], NavigableMap<Long, byte[]>> columns = rowData.get(family);
                    if (columns != null) {
                        context.getCounter(COUNTER_GROUP_FAMILY_PREFIX + familyName, COLUMNS_COUNT).increment(columns.size());
                        columnCount += columns.size();
                        int valueCountForFamily = 0;
                        long valueSizeForFamily = 0;
                        long columnSizeForFamily = 0;
                        for (byte[] column: columns.keySet()) {
                            columnSize += column.length;
                            columnSizeForFamily += column.length;
                            NavigableMap<Long, byte[]> values = columns.get(column);
                            //String columnName = Bytes.toString(column);
                            //context.getCounter(familyName, columnName + "_TIMESTAMPS").increment(values.size());
                            //int columnValues = 0;
                            context.getCounter(COUNTER_GROUP_FAMILY_PREFIX + familyName, TOTAL_TIMESTAMPS).increment(values.size());
                            for (Long ts: values.keySet()) {
                                timestamps.add(ts);
                                byte[] value = values.get(ts);
                                if (value != null && value.length > 0) {   // ther is no null String in hbase
                                    valueCount++;
                                    valueCountForFamily++;
                                    valueSize += value.length;
                                    valueSizeForFamily += value.length;
                                    //columnValues++;
                                }
                            }
                            //context.getCounter(familyName, columnName + "_VALUE_COUNT").increment(columnValues);
                        }
                        context.getCounter(COUNTER_GROUP_FAMILY_PREFIX + familyName, VALUES_COUNT).increment(valueCountForFamily);
                        context.getCounter(COUNTER_GROUP_FAMILY_PREFIX + familyName, VALUES_SIZE).increment(valueSizeForFamily);
                        context.getCounter(COUNTER_GROUP_FAMILY_PREFIX + familyName, COLUMNS_SIZE).increment(columnSizeForFamily);
                        context.getCounter(COUNTER_GROUP_FAMILY_PREFIX + familyName, DATA_SIZE).increment(columns.size() * 8 + valueSizeForFamily + columnSizeForFamily);
                    }
                }
                context.getCounter(COUNTER_GROUP, TOTAL_TIMESTAMPS).increment(timestamps.size());
                context.getCounter(COUNTER_GROUP, COLUMNS_COUNT).increment(columnCount);
                context.getCounter(COUNTER_GROUP, COLUMNS_SIZE).increment(columnSize);
                context.getCounter(COUNTER_GROUP, VALUES_COUNT).increment(valueCount);
                context.getCounter(COUNTER_GROUP, VALUES_SIZE).increment(valueSize);
                context.getCounter(COUNTER_GROUP, DATA_SIZE).increment(columnCount * 8 /* long */  + valueSize + columnSize);

            } catch (Exception e) {
                context.getCounter(COUNTER_GROUP, EXCEPTIONS_MAPPER).increment(1L);
                e.printStackTrace();
                if (e instanceof IOException) {
                    throw (IOException)e;
                }
            }
            context.getCounter(COUNTER_GROUP, SUM_TIME_PER_MAP).increment(System.currentTimeMillis() - start);
        }

    }

  /**
   * M/R Job setup. No reduce.
   * @param conf a suitable hadoop+hbase configuration
   * @param tableName the table we want to get stats from
   * @return the Job object, to be started
   * @throws java.io.IOException any hadoop IO problem
   */
  public static Job createSubmittableJob(Configuration conf, String tableName)
      throws IOException {

      Job job = new Job(conf, NAME + "_" + tableName);
      if (job.getJar() == null) {
          job.setJarByClass(TableStats.class);  // otherwise set in conf already
      }
      Scan scan = new Scan();
      scan.setMaxVersions(10000);  // todo fixme
      TableMapReduceUtil.initTableMapperJob(tableName, scan, RowCountMapper.class,
              Text.class, Result.class, job);
      job.setOutputFormatClass(NullOutputFormat.class);
      job.setNumReduceTasks(0);
      
      return job;
  }

  /**
   * Setup for jar submit. Assumes a working hadoop environment.
   * @param args main args
   * @throws Exception just any ex
   */
  public static void main(String[] args) throws Exception {
    Configuration conf = HBaseConfiguration.create();
    String[] otherArgs = new GenericOptionsParser(conf, args).getRemainingArgs();
    if (otherArgs.length < 1) {
      System.err.println("ERROR: Wrong number of parameters: " + args.length);
      System.err.println("Usage: TableStats <tablename>");
      System.exit(-1);
    }
    Job job = createSubmittableJob(conf, args[0]);
    System.exit(job.waitForCompletion(true) ? 0 : 1);
  }
}
