package com.nnapz.hbaseexplorer;

import org.apache.hadoop.hbase.client.Result;

import java.util.Map;

/**
 * @author Bohdan Mushkevych
 *         Provides interface for HBaseExplorer interaction with any ORM
 */
public interface OrmInterface {
    /**
     * @param tableName the
     * @return True if table is covered by this ORM, false otherwise
     */
    boolean containsTable(String tableName);

    /**
     * @param tableName the
     * @param results   array of Results from scanner
     * @return Scanner Result, parsed into a Map, grouped by rowKey & timestamp, suitable for showing on UI
     *         for example:
     *         <rowKey1 :   <timestamp1:
     *         <family1: <col1: val1>, <col2: val2>>
     *         <family2 :...>
     *         >
     *         <timestamp2:
     *         <familyA: <col1: val1>, <col2: val2>>
     *         <familyB :...>
     *         >
     *         ...
     */
    Map<byte[], Map<Long, Map<String, Map<String, String>>>> parseResults(String tableName, Result[] results);

    /**
     * @param tableName the
     * @param pk        the
     * @return Human readable representation of the row key, suitable for showing on UI
     */
    String parseRowKey(String tableName, byte[] pk);

    /**
     * Return map, where:
     * - key is human readable name of the RowKey component (for example: "timeperiod", "domain_name", etc)
     * - value is the class of the component (for example: "timeperiod" : Integer.class)
     *
     * @return Map in format <Human Readable Name of the Component: Component Class.
     *         For example: <"timeperiod" : Integer.class> <"domain_name" : String.class>
     */
    Map<String, Class> getRowKeyComponents(String tableName);

    /**
     * Generate key based on transferred components
     *
     * @param tableName
     * @param components in format Map<String, Object>, where:
     *                   - String presents human readable name of the component, same as from @getComponents
     *                   - Object presents initialized object of the
     * @return Key in format
     */
    byte[] generateRowKey(String tableName, Map<String, Object> components);
}

