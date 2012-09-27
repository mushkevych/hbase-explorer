package com.nnapz.hbaseexplorer.domain
/**
 * Represents a statistics snapshot of a table (if familyName == null) or a table family
 */
public class HbaseTableStats {

    // identifiers
    String tableName

    Date creationDate = new Date()

    // Read the stats as follows
    // (based on this structure)  family
    //                              -- column 1
    //                                   -- ts1 -- value 1
    //                                   -- ts2
    //                                   -- ts3 -- value 2
    //                -->  Stats:  1 column, 3 ts, 2 values
    // the size represents the assumed byte size
    long rowCount          
    long totalTimestamps
    long valueCount
    long valueSize
    long columnCount
    long columnSize
    long dataSize           // 8 byte ts + columnSize + valueSize

    long executionTime      // gives an idea of the cluster performance; total map time / map count
    int regionCount         // mappers used (== regions)
  
    HbaseSource hbaseSource
    static belongsTo = [hbaseSource: HbaseSource]
    //HbaseFamilyStats[] hbaseFamilyStats
    static hasMany = [hbaseFamilyStats: HbaseFamilyStats]

    static constraints = {
      //tableName(nullable: false, blank: false)
      //creationDate(nullable: false, blank: false)
      rowCount(min:0L)
	  totalTimestamps(min:0L)
	  valueCount(min:0L)
	  columnCount(min:0L)
	  columnSize(min:0L)
	  dataSize(min:0L)
      hbaseFamilyStats(nullable: true)
      tableName(nullable: false)
    }

}
