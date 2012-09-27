package com.nnapz.hbaseexplorer.domain

class HbaseConfigFile {

	String location;
	String name;
	long uploadTimestamp; 
	
	static belongsTo= [hbaseSource : HbaseSource]
	
    static constraints = {
    }
}
