package com.nnapz.hbaseexplorer.services
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

import com.nnapz.hbaseexplorer.ConfigurationHolder
import com.nnapz.hbaseexplorer.HBaseClient
import com.nnapz.hbaseexplorer.domain.HbaseFamilyStats
import com.nnapz.hbaseexplorer.domain.HbaseSource
import com.nnapz.hbaseexplorer.domain.HbaseTableStats
import com.nnapz.hbaseexplorer.mr.TableStats
import com.reinvent.surus.primarykey.AbstractPrimaryKey
import com.reinvent.surus.system.PoolManager
import com.reinvent.surus.system.TableContext
import org.apache.hadoop.hbase.HColumnDescriptor
import org.apache.hadoop.hbase.HTableDescriptor
import org.apache.hadoop.hbase.MasterNotRunningException
import org.apache.hadoop.hbase.client.HBaseAdmin
import org.apache.hadoop.hbase.client.HTablePool
import org.apache.hadoop.hbase.client.Result
import org.apache.hadoop.hbase.util.Bytes
import org.apache.hadoop.mapreduce.Job

import java.util.Map.Entry
import com.reinvent.surus.mapping.HFieldComponent
import com.reinvent.surus.mapping.JsonService

/**
 * Groovy Wrapper to access the HBaseService.
 *
 * Many methods return fast and push a thread that waits for the completion of this action, using the threadService.
 * That way we decouple somehow from the not-so well specified behaviour of the hbase admin methods as well.
 * @author Bob Schulze
 */
class HbaseService {

    ConfigHolderService configHolderService;

    // is a singleton
    private static final long TWO_MINUTES = 60 * 2 * 1000
    private long lastRefreshTs = 0
    private HTableDescriptor[] tableDescriptors

    protected ThreadService threadService
    protected HTablePool pool
    protected boolean transactional = false
    protected def quartzScheduler

    // not used currently
    Map<Long, Map<String, Map<byte[], byte[]>>> get(
            HbaseSource hbaseSourceInstance, String tableName, String rowKey, int versions)
    throws MasterNotRunningException {
        return getHBaseClient(hbaseSourceInstance).get(tableName, rowKey, versions)
    }

    Map<byte[], Map<Long, Map<String, Map<byte[], byte[]>>>> scan(
            HbaseSource hbaseSourceInstance, String tableName, byte[] rowKey, int versions, int rows)
    throws MasterNotRunningException {
        return getHBaseClient(hbaseSourceInstance).scan(tableName, rowKey, versions, rows)
    }

    Result[] scanAsResults(HbaseSource hbaseSourceInstance, String tableName, byte[] rowKey, int versions, int rows)
    throws MasterNotRunningException {
        return getHBaseClient(hbaseSourceInstance).scanAsResults(tableName, rowKey, versions, rows)
    }

    HTableDescriptor[] tableList(HbaseSource hbaseSourceInstance) throws MasterNotRunningException {
        return configHolderService.getConfigHolder(hbaseSourceInstance).tableDescriptors
    }

    boolean tableDescriptorsExpired() {
        if ((System.currentTimeMillis() - lastRefreshTs) > TWO_MINUTES) {
            return true;
        }
        return false;
    }

    HTableDescriptor getTable(HbaseSource hbaseSourceInstance, String tableName) throws MasterNotRunningException {
        return getHBaseClient(hbaseSourceInstance).getTableDescriptor(tableName)
    }

    // experimental
    long rowCount(HbaseSource hbaseSourceInstance, String tableName) {
        return getHBaseClient(hbaseSourceInstance).countRows(tableName);
    }

    void createTable(HbaseSource hbaseSourceInstance, HTableDescriptor spec) {
        threadService.execute('create-' + hbaseSourceInstance.name + '-' + spec.nameAsString, {
            HBaseAdmin admin = getHBaseClient(hbaseSourceInstance).getHBaseAdmin()
            try {
                admin.createTable(spec)
            } catch (Throwable twb) {
                log.error 'Error on table Creation', twb
                // org.apache.hadoop.hbase.client.NoServerForRegionException: No server address listed in .META. for region testbs2,,1261561733768 todo)
                // todo remove me its an hbase bug
            }
            // poll for completion

            threadService.waitFor(50, 500, {
                try {
                    admin.isTableEnabled(spec.nameAsString)
                } catch (Throwable twb) { /* may not exist yet */}
            })
        })
    }

    /**
     * Start a M/R job to gather stats about a table.
     */
    boolean pushTableStats(HbaseSource hbaseSourceInstance, String tableName) {
        String asyncName = 'tableStats-' + hbaseSourceInstance.name + '-' + tableName
        threadService.execute(asyncName, {
            // closure that executes asynchronously
            long startTime = System.currentTimeMillis()
            Job job
            try {
                job = getHBaseClient(hbaseSourceInstance).pushTableStats(tableName)
            } catch (Throwable twb) {
                log.error 'Error on row counting', twb
                // org.apache.hadoop.hbase.client.NoServerForRegionException: No server address listed in .META. for region testbs2,,1261561733768 todo)
                // todo remove me its an hbase bug
                return
            }
            // poll for completion
            threadService.waitFor(24 * 60, 60000, {       // 24 hours
                // give some progress
                threadService.setStatus asyncName, Math.round(job.mapProgress() * 100) + "%"  // we don't have reducers
                job.isComplete();
            })
            // do stats
            if (job.isSuccessful()) {
                // stats object for the table
                HbaseTableStats tableStats = new HbaseTableStats(hbaseSource: hbaseSourceInstance, tableName: tableName)
                tableStats.columnCount = job.counters.findCounter(TableStats.COUNTER_GROUP, TableStats.COLUMNS_COUNT).value
                tableStats.columnSize = job.counters.findCounter(TableStats.COUNTER_GROUP, TableStats.COLUMNS_SIZE).value
                tableStats.valueCount = job.counters.findCounter(TableStats.COUNTER_GROUP, TableStats.VALUES_COUNT).value
                tableStats.valueSize = job.counters.findCounter(TableStats.COUNTER_GROUP, TableStats.VALUES_SIZE).value
                tableStats.totalTimestamps = job.counters.findCounter(TableStats.COUNTER_GROUP, TableStats.TOTAL_TIMESTAMPS).value
                tableStats.dataSize = job.counters.findCounter(TableStats.COUNTER_GROUP, TableStats.DATA_SIZE).value
                tableStats.rowCount = job.counters.findCounter(TableStats.COUNTER_GROUP, TableStats.RETRIEVED_ROWS).value

                tableStats.executionTime = System.currentTimeMillis() - startTime
                tableStats.regionCount = regionCount(hbaseSourceInstance, tableName)
                HbaseTableStats.withTransaction {   // we don't have a hibernate session in this unbound thread otherwise
                    if (!tableStats.save()) { tableStats.errors.each { println it }}
                }
                // per family stats
                HTableDescriptor tableDescriptor = getTable(hbaseSourceInstance, tableName)
                tableDescriptor.families.each { HColumnDescriptor col ->
                    String familyName = new String(col.name)
                    String prefix = TableStats.COUNTER_GROUP_FAMILY_PREFIX + familyName;
                    HbaseFamilyStats familyStats = new HbaseFamilyStats(hbaseTableStats: tableStats)
                    familyStats.familyName = familyName
                    familyStats.columnCount = job.counters.findCounter(prefix, TableStats.COLUMNS_COUNT).value
                    familyStats.columnSize = job.counters.findCounter(prefix, TableStats.COLUMNS_SIZE).value
                    familyStats.valueCount = job.counters.findCounter(prefix, TableStats.VALUES_COUNT).value
                    familyStats.valueSize = job.counters.findCounter(prefix, TableStats.VALUES_SIZE).value
                    familyStats.totalTimestamps = job.counters.findCounter(prefix, TableStats.TOTAL_TIMESTAMPS).value
                    familyStats.dataSize = job.counters.findCounter(prefix, TableStats.DATA_SIZE).value
                    HbaseTableStats.withTransaction {   // we don't have a hibernate session in this unbound thread otherwise
                        if (!familyStats.save()) { familyStats.errors.each { println it }}
                    }
                    tableStats.addToHbaseFamilyStats(familyStats)
                }
            }
        })
    }

    /**
     * Get a HBase client and configure it a bit
     */
    private HBaseClient getHBaseClient(HbaseSource hbaseSourceInstance) {
        ConfigurationHolder configHolder = hbaseSourceInstance.configHolder()
        if (pool == null) {
            pool = new HTablePool(configHolder.getConf(), HBaseClient.TABLE_POOL_MAX_SIZE)
        }
        HBaseClient hbc = new HBaseClient(pool, configHolder);
        return hbc
    }

    /**
     * Provide a flat (noversion) record from a given versioned tree representation. The tree is expected to be ordered
     * by the oldest entry atop.
     * @returns family->column->value , records exist only for existing values
     */
    Map<String, Map<String, String>> getFlatMap(
            PatternService patternService,
            HbaseSource hbaseSourceInstance,
            String tableName,
            Map<Long, Map<String, Map<String, String>>> versionedMap) {

        HashMap<String, HashMap<String, String>> flatRecord = new HashMap<String, HashMap<String, String>>();
        if (versionedMap != null) {
            Long[] timestamps = versionedMap.keySet().toArray(new Long[versionedMap.size()])
            for (Long ts : timestamps) {
                Map<String, Map<String, String>> families = versionedMap.get(ts)

                for (Entry<String, Map<String, String>> familyEntry : families.entrySet()) {
                    // fill flat map with last ts
                    String familyName = familyEntry.getKey()
                    Map<String, String> family = familyEntry.getValue()
                    if (family == null) continue;
                    Map<String, String> flatFamily = flatRecord.get(familyName)
                    if (flatFamily == null) {
                        flatFamily = new HashMap<String, String>()
                        flatRecord.put(familyName, flatFamily)
                    }
                    for (Entry<String, String> entry : family.entrySet()) {
                        byte[] qualifier = Bytes.toBytes(entry.getKey())
                        byte[] value = Bytes.toBytes(entry.getValue())

                        String strQualifier = patternService.transformColumn(hbaseSourceInstance.name, tableName, familyName, qualifier, value);
                        String strValue = patternService.transformValue(hbaseSourceInstance.name, tableName, familyName, qualifier, value);
                        flatFamily.put(strQualifier, strValue);
                    }
                }
            }
        }

        return flatRecord
    }

    // experimental code
    void executeRowCount(HbaseSource hbaseSourceInstance, String tableName) {
        HBaseClient hbc = new HBaseClient(pool, configHolderService.getConfigHolder(hbaseSourceInstance))
        hbc.executeRowCount tableName
    }

    /** Wrappers around the hbase API methods  */

    /**
     * This is a rather slow operation.....
     */
    boolean XisTableEnabled(HbaseSource hbaseSourceInstance, String tableName) {
        getHBaseClient(hbaseSourceInstance).getHBaseAdmin().isTableEnabled(tableName)
    }

    /**
     * Check if the table is online
     * @return true if it appears to be online
     */
    boolean isTableEnabled(HbaseSource hbaseSourceInstance, String tableName) {
        getHBaseClient(hbaseSourceInstance).checkOnline(tableName)
    }

    /**
     * Trigger the enabling of a table. Nothing happens if such an operation is executing already.
     * @return true if the action was executed
     */
    boolean enableTable(HbaseSource hbaseSourceInstance, String tableName) {
        return threadService.execute('enable-' + hbaseSourceInstance.name + '-' + tableName, {
            HBaseAdmin admin = getHBaseClient(hbaseSourceInstance).getHBaseAdmin()
            admin.enableTable(tableName)
            // poll for completion
            threadService.waitFor(50, 500, {isTableEnabled(hbaseSourceInstance, tableName) })
        }
        )
    }

    /**
     * Trigger disabling of a table. Nothing happens if such an operation is executing already.
     * @return true if the action was executed
     */
    boolean disableTable(HbaseSource hbaseSourceInstance, String tableName) {
        return threadService.execute('disable-' + hbaseSourceInstance.name + '-' + tableName, {
            HBaseAdmin admin = getHBaseClient(hbaseSourceInstance).getHBaseAdmin()
            admin.disableTable(tableName)
            // poll for completion
            threadService.waitFor(50, 500, {!isTableEnabled(hbaseSourceInstance, tableName) })
        }
        )
    }

    /**
     * throws TableNotDisabledException
     */
    void dropTable(HbaseSource hbaseSourceInstance, String tableName) {
        threadService.execute('delete-' + hbaseSourceInstance.name + '-' + tableName, {
            HBaseAdmin admin = getHBaseClient(hbaseSourceInstance).getHBaseAdmin()
            // todo: this may still fail: we need a mechanism to reply this to the screen
            admin.deleteTable(tableName)            //  may throw TableNotDisabledException
            // poll for completion
            threadService.waitFor(50, 100, {!admin.tableExists(tableName) })
        }
        )
    }

    int regionCount(HbaseSource hbaseSourceInstance, String tableName) {
        return getHBaseClient(hbaseSourceInstance).getRegionCount(tableName)
    }

    /* ORM SUPPORT SECTION*/

    /**
     * method converts raw string into Java Primitive accordingly to <type>
     * In case of conversion error default value is returned:
     * 0 for numeric, "" for string, false for boolean
     * @param raw string presenting the object
     * @param type of the target object
     * @return null if raw is null or valid object otherwise
     */
    public Object convertFromStringSafe(JsonService jsonService, String raw, Class type) {
        try {
            return jsonService.convertFromString(raw, type);
        } catch (NumberFormatException e) {
            if (type == Integer.class || type == Integer.TYPE) {
                return new Integer(0);
            } else if (type == Float.class || type == Float.TYPE) {
                return new Float(0.0);
            } else if (type == Double.class || type == Double.TYPE) {
                return new Double(0);
            } else if (type == Long.class || type == Long.TYPE) {
                return new Long(0);
            } else {
                throw new IllegalArgumentException("Unknown type " + type.getName());
            }
        }
    }

    public byte[] generateRowKeyFromParams(String tableName, Map params) {
        byte[] pk = null;
        JsonService jsonService = null;
        PoolManager poolManager = TableContext.getPoolManager(tableName);
        try {
            jsonService = poolManager.getJsonService();
            AbstractPrimaryKey primaryKey = poolManager.getPrimaryKey();
            Map<String, Object> keyComponents = new HashMap<String, Object>();

            HFieldComponent[] fieldComponents = primaryKey.getComponents();
            for (HFieldComponent component : fieldComponents) {
                String value = (String) params.get(component.name());
                log.info(String.format("Component name: %s, param value %s", component, value));

                Object o = convertFromStringSafe(jsonService, value.trim(), component.type());
                keyComponents.put(component.name(), o);
            }

            pk = primaryKey.generateRowKey(keyComponents).get();
        } catch (Exception e) {
            log.error("Unable to form row key from HTTP params", e);
            pk = Bytes.toBytes("");
        } finally {
            if (jsonService != null) {
                poolManager.putJsonService(jsonService);
            }
        }

        return pk;
    }
}



