package com.nnapz.hbaseexplorer

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;

import groovy.mock.interceptor.MockFor;

import org.apache.hadoop.conf.Configuration;

import org.apache.hadoop.hbase.HConstants;
import org.apache.hadoop.hbase.client.HBaseAdmin;
import org.apache.hadoop.hbase.client.HConnection
import org.apache.hadoop.hbase.client.HConnectionManager;


import com.nnapz.hbaseexplorer.ConfigurationHolder;
import com.nnapz.hbaseexplorer.domain.HbaseSource;
import com.nnapz.hbaseexplorer.services.ConfigHolderService;
import com.sun.org.apache.xalan.internal.lib.ObjectFactory.ConfigurationError;

/**
 * See the API for {@link grails.test.mixin.services.ServiceUnitTestMixin} for usage instructions
 */
@TestFor(ConfigHolderService)
class ConfigHolderServiceTests {

	public static final String name = "test-name";
	public static final String quorumServers = "test-quorum-servers"
	public static final int quorumPort = 1234;
	public static final String masterUrl = "test-master-url"
	public static final String jobTracker = "test-job-tracker"
	public static final String filesystem = "test-filesystem"
	public static final String hdfsUname = "test-uname"
	public static final String hdfsPwd = "test-pwd"

	
	private static getHbaseSource(){
		HbaseSource testSource = new HbaseSource(name: name, 
												 quorumServers: quorumServers, 
												 quorumPort: quorumPort,
												 masterUrl: masterUrl, 
												 jobTracker: jobTracker, 
												 filesystem: filesystem, 
												 hdfsUname: hdfsUname, 
												 hdfsPwd: hdfsPwd)
		return testSource 
	}
	
    void testGetConfigHolder() {
		ConfigHolderService configHolderService = new ConfigHolderService()		
        ConfigurationHolder configHolder = configHolderService.getConfigHolder(getHbaseSource());
		
		//check if objects were instantiated successfully
		assertNotNull("Config Holder shouldn't be null", configHolder)
		assertNotNull("Configuration should be already initialized",configHolder.conf)
		Configuration conf = configHolder.getConf()
		
		//Verify configuration values
		assertEquals("Quorum Servers",quorumServers,conf.get(HConstants.ZOOKEEPER_QUORUM))
		assertEquals("Quorum Port",quorumPort,conf.getInt(ConfigHolderService.PARAM_HBASE_ZOOKEEPER_PROPERTY_CLIENT_PORT,0))
		assertEquals("Hadoop Job UGI", "$hdfsUname,$hdfsPwd".toString(), conf.get(ConfigHolderService.PARAM_HADOOP_JOB_UGI))
		assertEquals("JobTracker",jobTracker, conf.get(ConfigHolderService.PARAM_MAPRED_JOB_TRACKER))
		assertEquals("FileSystem", filesystem, conf.getAt(ConfigHolderService.PARAM_FS_DEFAULT_NAME))
		
		//verify that the same config is returned for equal source
		Configuration conf2 = configHolderService.getConfigHolder(getHbaseSource()).conf
		assertTrue("Config for equal source should be the same object", conf.is(conf2))	
	}
	
	void testDeleteConnection() {

		ConfigHolderService configHolderService = new ConfigHolderService()
		ConfigurationHolder configHolder = configHolderService.getConfigHolder(getHbaseSource())
		HConnectionManager.metaClass.static.deleteConnection = {Configuration conf, boolean proxy -> 
			//verify that the proper connection is deleted
			assertEquals(configHolder.conf, conf)
			assertEquals(proxy,true)
		}
		configHolderService.deleteConnection(getHbaseSource())
		ConfigurationHolder configHolder2 = configHolderService.getConfigHolder(getHbaseSource())
		assertFalse("New config holder should be created", configHolder.is(configHolder2))
		
		//remove metaClass override
		GroovySystem.metaClassRegistry.removeMetaClass(HConnectionManager.class);
	}
}
