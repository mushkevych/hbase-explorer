/**
 * 
 */
package com.nnapz.hbaseexplorer;

import java.io.IOException;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.hbase.HTableDescriptor;
import org.apache.hadoop.hbase.client.HBaseAdmin;
import org.apache.hadoop.hbase.client.HTablePool;

/**
 * Aggregates admin, conf and table pool for object reusage.
 * @author praeuber
 */
public class ConfigurationHolder {

	// how many simultaneous users ?
	private static final int TABLE_POOL_MAX_SIZE = 10;
	private static final long DESCRIPTORS_CACHE_TIME_INTERVAL_MS = 30000L; 
	
	HTablePool pool;
	HBaseAdmin admin;
	Configuration conf;
	
	
	private long descriptorsCachedTime;
	private HTableDescriptor[] descriptorsCache;
	
	public ConfigurationHolder(Configuration conf) {
		this.conf = conf;
		this.pool = new HTablePool(conf, TABLE_POOL_MAX_SIZE);
	}

	public HTablePool getPool() {
		return pool;
	}

	public HBaseAdmin getAdmin() throws IOException {
		if (admin == null){
			admin = new HBaseAdmin(this.conf);
		}
		return admin;
	}

	public HTableDescriptor[] getTableDescriptors() throws IOException{
		if (descriptorsCache == null 
				|| ( System.currentTimeMillis()-descriptorsCachedTime) > DESCRIPTORS_CACHE_TIME_INTERVAL_MS){
			descriptorsCache = getAdmin().listTables();
			descriptorsCachedTime = System.currentTimeMillis();
		}
		
		return descriptorsCache;
	}
	
	public Configuration getConf() {
		return conf;
	}
}
