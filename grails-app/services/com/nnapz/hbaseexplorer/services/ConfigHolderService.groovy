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
package com.nnapz.hbaseexplorer.services

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap

import javax.servlet.ServletContext;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.Path
import org.apache.hadoop.hbase.HBaseConfiguration;
import org.apache.hadoop.hbase.HConstants;
import org.apache.hadoop.hbase.MasterNotRunningException;
import org.apache.hadoop.hbase.ZooKeeperConnectionException;
import org.apache.hadoop.hbase.client.HConnectionManager;
import org.apache.log4j.Logger;


import com.nnapz.hbaseexplorer.ConfigurationHolder;
import com.nnapz.hbaseexplorer.domain.HbaseConfigFile;
import com.nnapz.hbaseexplorer.domain.HbaseSource;

class ConfigHolderService {

	ServletContext servletContext
	
    public static final String PATH_TO_MR_JAR = "mapred.jar.path"

	static final String PARAM_HBASE_CLIENT_PAUSE = "hbase.client.pause"
	static final String PARAM_HBASE_CLIENT_RETRIES_NUMBER = "hbase.client.retries.number"
	static final String PARAM_HBASE_ZOOKEEPER_PROPERTY_INIT_LIMIT = "hbase.zookeeper.property.initLimit"
	static final String PARAM_HBASE_ZOOKEEPER_PROPERTY_CLIENT_PORT = "hbase.zookeeper.property.clientPort"
	static final String PARAM_HADOOP_JOB_UGI = "hadoop.job.ugi"
	static final String PARAM_MAPRED_JOB_TRACKER = "mapred.job.tracker"
	static final String PARAM_FS_DEFAULT_NAME = "fs.default.name";

	private Map<HbaseSource, ConfigurationHolder> configHolderMap = new ConcurrentHashMap<HbaseSource, ConfigurationHolder>();
	
	/**
	 * Provides {@link Configuration} associated with {@link HbaseSource}.
	 * If the desired object was created before then it's reused, if not
	 * then new Configuration object is created.
	 * 
	 * @param source
	 * @return
	 * @throws ZooKeeperConnectionException 
	 * @throws MasterNotRunningException 
	 */
	public ConfigurationHolder getConfigHolder(HbaseSource source) {
		if (configHolderMap.containsKey(source)){
            log.debug("Retrieving existing Configuration object ( sourceName=$source.name, configPoolSize=${configHolderMap.size()})");
			return configHolderMap.get(source);
		} else {
			Configuration config = createConfig(source);
            log.debug("Creating new Configuration object ( sourceName=$source.name, configPoolSize=${configHolderMap.size()})");
            ConfigurationHolder configurationHolder = new ConfigurationHolder(config);
			configHolderMap.put(source, configurationHolder);
			return configurationHolder;
		}
	}

	/** Creates new {@link Configuration} based on given {@link HbaseSource} instance */
	private Configuration createConfig(HbaseSource source){
		try {
			Configuration config = HBaseConfiguration.create();
			config.set(PARAM_HBASE_CLIENT_PAUSE,"300");
			config.set(PARAM_HBASE_CLIENT_RETRIES_NUMBER,"3");
			config.set(PARAM_HBASE_ZOOKEEPER_PROPERTY_INIT_LIMIT,"3");

			config.set(HConstants.ZOOKEEPER_QUORUM, source.quorumServers);
			config.setInt(PARAM_HBASE_ZOOKEEPER_PROPERTY_CLIENT_PORT, source.quorumPort);

			if (source.hdfsUname && source.hdfsPwd){
				config.set(PARAM_HADOOP_JOB_UGI, "${source.hdfsUname},${source.hdfsPwd}");
			}

			if (source.jobTracker){
				config.set(PARAM_MAPRED_JOB_TRACKER, source.jobTracker);
			}

			if (source.filesystem){
				config.set(PARAM_FS_DEFAULT_NAME,source.filesystem);
			}


			return config
		} catch (Exception e){
			log.warn "Exception occured when creating HBase Configuration for source ${source.name} (${e.getMessage()})"
		}
	}
	
	def deleteConnection(HbaseSource source){
		Configuration conf = configHolderMap.get(source)?.conf
		configHolderMap.remove(source)
		if (conf){
			log.debug "Closing connections associated with source $source.name"
			HConnectionManager.deleteConnection(conf,true)
		}
	}
	
}
