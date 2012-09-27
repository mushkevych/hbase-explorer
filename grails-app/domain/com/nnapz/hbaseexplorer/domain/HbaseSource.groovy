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

package com.nnapz.hbaseexplorer.domain

import com.nnapz.hbaseexplorer.ConfigurationHolder;
import com.nnapz.hbaseexplorer.services.ConfigHolderService
import org.apache.commons.lang.builder.HashCodeBuilder
import org.apache.hadoop.conf.Configuration
import org.apache.hadoop.fs.Path
import org.apache.hadoop.hbase.client.HConnectionManager
import org.apache.hadoop.hbase.client.HBaseAdmin
import org.apache.hadoop.hbase.client.HTablePool;

/**
 * Collects all information to access a HBase Database.
 * @author Bob Schulze(allias), Mateusz Pytel(e-m-p)
 */
public class HbaseSource {

	static transients = [ "configHolderService" ]
		
	/** Keeps associations between {@link HbaseSource} and {@link Configuration}.*/
	ConfigHolderService configHolderService

	/** Instance name */
	String name

	/** ZooKeeper Quorum server list. Hadoop property: <code>hbase.zookeeper.quorum</code>.*/
	String quorumServers

	/** ZooKeeper Client port. Hadoop property: <code>hbase.zookeeper.property.clientPort</code>.*/
	int    quorumPort

	/** HBase Master web UI URL.*/
	String masterUrl

	/** JobTracker URL. Hadoop property: <code>mapred.job.tracker</code>. */
	String jobTracker

	/** 
	 * (after core-default.xml) The name of the default file system.  A URI whose
	 * scheme and authority determine the FileSystem implementation. Since 0.90.*
	 * required for successful mrjob submission
	 */
	String filesystem

	/** HDFS User name - required for authentication */
	String hdfsUname;
	
	/** HDFS Password - required for authentication */
	String hdfsPwd;
	
	static constraints = {
		quorumPort(min: 30)
		quorumServers(nullable: false, blank: false)
		name(maxLength: 100, minLength: 2)
		jobTracker(blank: true, nullable: true)
		filesystem(blank:true, nullable : true)
		masterUrl(blank: true, nullable: true)
		hdfsUname(blank: true, nullable: true)
		hdfsPwd(blank: true, nullable: true)
	}

	static hasMany = [hbaseTableStats: HbaseTableStats, configFiles : HbaseConfigFile]

	int hashCode(){
		HashCodeBuilder hcb = new HashCodeBuilder();
		hcb.append(quorumPort).append(quorumServers).append(masterUrl)
		   .append(jobTracker).append(filesystem).append(name)
		   .append(hdfsUname).append(hdfsPwd)
		return hcb.toHashCode();
	}

	boolean equals(o) {
		if (this.is(o)) return true;
		if (getClass() != o.class) return false;

		HbaseSource that = (HbaseSource) o;

		if (quorumPort != that.quorumPort) return false;
		if (jobTracker != that.jobTracker) return false;
		if (masterUrl != that.masterUrl) return false;
		if (name != that.name) return false;
		if (filesystem != that.filesystem) return false;
		if (quorumServers != that.quorumServers) return false;

		return true;
	}
	
	public ConfigurationHolder configHolder() {
		configHolderService.getConfigHolder(this);
	}

	public HBaseAdmin hbaseAdmin() {
		configHolderService.getConfigHolder(this).getAdmin();
	}
	
	public HTablePool hTablePool() {
		configHolderService.getConfigHolder(this).getPool();	
	}
		
	public Configuration hbaseConfig(){
		configHolderService.getConfigHolder(this).getConf();
	}

	/** Delete old {@link Configuration} from connection pool */
	def beforeUpdate = {
		
	}

	/** Delete {@link Configuration} and remove files associated with this configuration */
	def beforeDelete = {
		configFiles.each { configFile ->
			if (configFile?.location){
				File file = new File(configFile.location);
				if (file.exists()) {
					file.delete();
				}
			}
		}
	}

}
