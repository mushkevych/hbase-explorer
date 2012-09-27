package com.nnapz.hbaseexplorer.domain
/**
 * Stats of individual tables of an Table Stats run
 */
public class HbaseFamilyStats {

    String familyName

    // see HbaseTableStats for understanding these values
    long totalTimestamps
    long valueCount
    long valueSize
    long columnCount
    long columnSize
    long dataSize

    HbaseTableStats hbaseTableStats
    static belongsTo = [hbaseTableStats: HbaseTableStats]

    static constraints = {
      familyName(nullable: false)
    }
}
