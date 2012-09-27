package com.nnapz.hbaseexplorer.controllers
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

import grails.plugins.springsecurity.Secured;

import org.apache.hadoop.hbase.HTableDescriptor
import org.apache.hadoop.hbase.HColumnDescriptor
import org.apache.hadoop.hbase.MasterNotRunningException
import org.apache.hadoop.hbase.ZooKeeperConnectionException;

import com.nnapz.hbaseexplorer.Util
import com.nnapz.hbaseexplorer.domain.HbaseConfigFile;
import com.nnapz.hbaseexplorer.domain.HbaseSource
import com.nnapz.hbaseexplorer.domain.HbaseTableStats;
import com.nnapz.hbaseexplorer.services.HbaseService
import com.nnapz.hbaseexplorer.services.PatternService
import com.nnapz.hbaseexplorer.services.ThreadService
import com.nnapz.hbaseexplorer.OrmContext
import org.apache.hadoop.hbase.io.hfile.Compression
import com.nnapz.hbaseexplorer.OrmInterface
import org.apache.hadoop.hbase.client.Result

/**
 * Following the Grails model, this class handles all Hbase Page actions.
 */
class HbaseSourceController {

	HbaseService hbaseService
	ThreadService threadService
	PatternService patternService

	
	def index = { redirect(action: list, params: params) }

	// the delete, save and update actions only accept POST requests
	static allowedMethods = [delete: 'POST', save: 'POST', update: 'POST', dropTable: 'POST', disableTable: 'POST']


	def writeConfig = {
		//
	}

	/**
	 * Enable a Table. Returns fast and leaves a thread checking for completion.
	 */
	@Secured(['ROLE_ROOT','ROLE_HBASE_ADMIN'])
	def enableTable = {
		HbaseSource hbaseSourceInstance = HbaseSource.get(params.id)

		if (!hbaseSourceInstance) {
			flash.message = "CloudInstance not found with id ${params.id}"
			redirect(action: list)
			return
		}

		String tableName = params.tableName
		if (!tableName || tableName.length() == 0) {
			flash.message = "Table Name wrong"
			redirect(action: show, id: params.id)
			return
		}

		if (hbaseService.isTableEnabled(hbaseSourceInstance, tableName)) {
			flash.message = "Table ${tableName} is already enabled."
			redirect(action: show, id: params.id)
		}

		hbaseService.enableTable hbaseSourceInstance, tableName

		flash.message = "Enabling Table ${tableName} initiated. . PLS refresh this page as soon as the Process finished."
		redirect(action: show, id: params.id)

	}


	/**
	 * Disable a Table. Returns fast and leaves a thread checking for completion.
	 */
	@Secured(['ROLE_ROOT','ROLE_HBASE_ADMIN'])
	def disableTable = {
		HbaseSource hbaseSourceInstance = HbaseSource.get(params.id)

		if (!hbaseSourceInstance) {
			flash.message = "CloudInstance not found with id ${params.id}"
			redirect(action: show, id: params.id)
			return
		}

		String tableName = params.tableName
		if (!tableName || tableName.length() == 0) {
			flash.message = "Table Name wrong"
			redirect(action: show, id: params.id)
			return
		}

		if (!hbaseService.isTableEnabled(hbaseSourceInstance, tableName)) {
			flash.message = "Table ${tableName} is already disabled."
			redirect(action: show, id: params.id)
		}

		hbaseService.disableTable hbaseSourceInstance, tableName

		flash.message = "disabling table ${tableName} initiated. PLS refresh this page as soon as the Process finished."
		redirect(action: show, id: params.id)
	}

	/**
	 *  Drop a Table. Returns fast and leaves a thread checking for completion.
	 */
	@Secured(['ROLE_ROOT','ROLE_HBASE_ADMIN'])
	def dropTable = {
		HbaseSource hbaseSourceInstance = HbaseSource.get(params.id)

		if (!hbaseSourceInstance) {
			flash.message = "CloudInstance not found with id ${params.id}"
			redirect(action: list)
		}

		String tableName = params.tableName
		if (!tableName || tableName.length() == 0) {
			flash.message = "Table Name wrong"
			redirect(action: show, id: params.id)
		}

		HTableDescriptor table = (HTableDescriptor) hbaseService.tableList(hbaseSourceInstance).find {it.nameAsString.equals(tableName)}
		if (!table) {
			flash.message = "No such table: $tableName"
			redirect(action: show, id: params.id)
		}

		if (hbaseService.isTableEnabled(hbaseSourceInstance, tableName)) {
			flash.message = "Table ${tableName} is still enabled. Disable first."
			redirect(action: show, id: params.id)
		}

		hbaseService.dropTable(hbaseSourceInstance, tableName)
		flash.message = "Table ${tableName} dropped."
		redirect(action: show, id: params.id)
	}

	/**
	 * List all HBase Tables. Default Scrolling.
	 */
	def list = {
		params.max = Math.min(params.max ? params.max.toInteger() : 10, 100)

		def clist = HbaseSource.list(params)
		[hbaseSourceInstanceList: clist, hbaseSourceInstanceTotal: HbaseSource.count()]
	}

	/**
	 * Hook to show one table.
	 */
	def show = {
		def hbaseSourceInstance = HbaseSource.get(params.id)
		PatternService patternService

		if (!hbaseSourceInstance) {
			flash.message = "CloudInstance not found with id ${params.id}"
			redirect(action: list)
		}
		else { return [hbaseSourceInstance: hbaseSourceInstance, hbs: hbaseService] }
	}

	/**
	 * Hook to clone a existing table. It creates a parameter set suitable for createTable and just forwards...
	 */
	@Secured(['ROLE_ROOT','ROLE_HBASE_ADMIN'])
	def cloneTable = {
		HbaseSource hbaseSourceInstance = HbaseSource.get(params.id)

		if (!hbaseSourceInstance) {
			flash.message = "CloudInstance not found with id ${params.id}"
			redirect(action: list)
		}

		String tableName = params.tableName
		if (!tableName || tableName.length() == 0) {
			flash.message = "Table Name wrong"
			redirect(action: show, id: params.id)
		}

		HTableDescriptor table = (HTableDescriptor) hbaseService.tableList(hbaseSourceInstance).find {it.nameAsString.equals(tableName)}
		if (!table) {
			flash.message = "No such table: $tableName"
			redirect(action: show, id: params.id)
		}

		HColumnDescriptor[] columns = table.columnFamilies
		Map newParams = [:]
		newParams.put("tableName", tableName + "_clone")
		int i = 1;
		columns.each{ HColumnDescriptor column ->
			newParams.put("familyName_" + i, column.nameAsString)
			newParams.put("versions_" + i, column.maxVersions)
			newParams.put("ttl_" + i, column.timeToLive)
			newParams.put("blockcache_" + i, column.blockCacheEnabled)
			newParams.put("blocksize_" + i, column.blocksize)
			newParams.put("compression_" + i, column.compression)
			i++;
		}
		newParams.put('fcount', i-1)
		redirect(action: createTable, id: hbaseSourceInstance.id, params: newParams)
	}

	/**
	 * Not to be used yet.
	 */
	def count = {
		def hbaseSourceInstance = HbaseSource.get(params.id)
		def tableName = 'xxx' // todo

		System.out.println("HbaseSourceController.count");
		if (!hbaseSourceInstance) {
			flash.message = "CloudInstance not found with id ${params.id}"
			redirect(action: list)
		}
		else {
			System.out.println("HbaseSourceController.rowcount");
			return hbaseService.rowCount(hbaseSourceInstance, tableName);
		}
	}

    /**
     * Execute a scan. Returns synchronously with or without data.
     */
    def scan = {
        HbaseSource hbaseSourceInstance = HbaseSource.get(params.id)

        if (!hbaseSourceInstance) {
            // try to figure out by name
            if (params.hbaseSource) {
                hbaseSourceInstance = HbaseSource.findByName(params.hbaseSource)
            }
        }
        if (!hbaseSourceInstance) {
            flash.message = "Hbase not found with id ${params.id}"
            redirect(action: list)
            return null;
        }

        String tableName = params.tableName                  // todo assert
        byte[] rowKey = params.rowKey
        int versions = params.versions ? Integer.parseInt(params.versions) : 1
        int rows = params.rows ? Integer.parseInt(params.rows) : 1

        try {
            def tableList = hbaseService.tableList(hbaseSourceInstance)

            if (tableName != null && OrmContext.containsOrmFor(tableName)) {
                rowKey = hbaseService.generateRowKeyFromParams(tableName, params)
            }
            if (tableName != null && rowKey != null) {
                long start = System.currentTimeMillis()
                long tableListStartTime = System.currentTimeMillis();
                OrmInterface ormInterface = OrmContext.getOrmFor(tableName);

                Result[] results = hbaseService.scanAsResults(hbaseSourceInstance, tableName, rowKey, versions, rows);
                Map<byte[], Map<Long, Map<String, Map<String, String>>>> scanResults =
                    ormInterface.parseResults(tableName, results);

                // get a (sorted) list of family names
                def allFamilies = tableList.find {it.nameAsString.equals(tableName)}.families*.nameAsString.sort()
                long tableListStopTime = System.currentTimeMillis();
                log.debug("Time for getting table-list : " + (tableListStopTime - tableListStartTime) + " ms");

                return [hbaseSourceInstance: hbaseSourceInstance,
                        scan: scanResults,
                        allFamilies: allFamilies,
                        tableNames: tableList,
                        took: System.currentTimeMillis() - start,
                        hbs: hbaseService,
                        rowKey: rowKey,
                        tableName: tableName,
                        patternService: patternService,
                        ormContext: OrmContext]
            } else {
                return [hbaseSourceInstance: hbaseSourceInstance,
                        tableNames: tableList,
                        hbs: hbaseService,
                        ormContext: OrmContext]
            }
        } catch (Exception e) {
            log.error "Exception occured during scan for ${hbaseSourceInstance.name}", e
            flash.errmessage = "Problems occured while executing SCAN operation: ${e.toString()}"
            redirect(action: list)
        }
    }

    /**
	 * Provide the current number of regions. Called from a ajax include.
	 */
	def regionCount = {
		def hbaseSourceInstance = HbaseSource.get(params?.id)
		String tableName = params?.tableName
		if (!hbaseSourceInstance || !tableName || tableName.length() == 0) {
			render 'Error: HBS or table missing'
		} else {
			render "${hbaseService.regionCount(hbaseSourceInstance, tableName)} Region(s)"
		}
	}

	/**
	 * Pushes a statistics mr job
	 */
	@Secured(['ROLE_ROOT','ROLE_HBASE_ADMIN'])
	def pushTableStats = {
		def hbaseSourceInstance = HbaseSource.get(params.id)
		String tableName = params.tableName
		if (!hbaseSourceInstance || !tableName) {
			flash.message = "CloudInstance not found with id ${params.id} or table name not found"
			redirect(action: list)
		}
		if (!tableName) {
			flash.message = "no table name"
			redirect(action: show, id: params.id)
		}
		if (hbaseService.pushTableStats(hbaseSourceInstance, tableName)) {
			flash.message = "Started Table Statistics Job. . PLS refresh this page as soon as the Process finished.  "
		} else {
			flash.message = "Table Statistics Job not started (Still running?) "
		}
		redirect(action: show, id: params.id)
	}

	/**
	 * Hook to table stats
	 */
	def tableStats = {
		def hbaseSourceInstance = HbaseSource.get(params.id)

		if (!hbaseSourceInstance) {
			flash.message = "CloudInstance not found with id ${params.id}"
			redirect(action: list)
		}

		String tableName = params.tableName
		if (!tableName || tableName.length() == 0 || tableName.contains(' ')) {
			flash.message = "Table Name wrong"
			redirect(action: show, id: params.id)
			return
		}

		[families: HbaseTableStats.findAllByHbaseSourceAndTableName(hbaseSourceInstance,tableName),
					tableName: tableName, hbaseSource: hbaseSourceInstance]
	}
	/**
	 * Hook to render the tables include.
	 */
	def tables = {

		def hbaseSourceInstance = HbaseSource.get(params.id)

		if (!hbaseSourceInstance) {
			flash.message = "CloudInstance not found with id ${params.id}"
			redirect(action: list)
		}

		def tlist = []         // contains a string or HTableDescriptor[]
		def onlineList = []    // if the table is online, than its name is in this list
		try {
			//   long start = System.currentTimeMillis()
			tlist = hbaseService.tableList(hbaseSourceInstance)
			//      System.out.println("tableList: " + (System.currentTimeMillis() - start));
			tlist.each {
				String tableName = it.nameAsString
				if (hbaseService.isTableEnabled(hbaseSourceInstance, tableName)) onlineList << tableName
			}
		} catch (MasterNotRunningException mnr) {
			tlist = 'Master not running'
		
			log.warn "Unable to show table details - Master Not Running"
		} catch (ZooKeeperConnectionException zkce) {
			tlist = "ZooKeeper connection problems"
			log.warn "Unable to show table details - ZooKeeper Connection Exception", zkce
		} catch (Exception e){
			tlist = "Exception occured (${e.toString()}). See logs for details."
			log.warn "Exception occured while showing table details", e
		}

		render(view: '_tables',
                model: [tableList: tlist,
                        hbaseSourceInstance: hbaseSourceInstance,
                        online: onlineList,
                        hbs: hbaseService])
	}

	/**
	 * Execute a GET, currently not used. Still needed? We have scan....
	 */
	def get = {
		HbaseSource hbaseSourceInstance = HbaseSource.get(params.id)
		String tableName = params.tableName
		String rowKey = params.rowKey
		int versions = params.versions ? Integer.parseInt(params.versions) : 0
		if (!hbaseSourceInstance) {
			flash.message = "CloudInstance not found with id ${params.id}"
			redirect(action: list)
		}
		else {

			def tableList = hbaseService.tableList(hbaseSourceInstance)
			if (tableName != null && rowKey != null) {

				long start = System.currentTimeMillis()
				TreeMap<Long, HashMap<String, HashMap<byte[], byte[]>>> out =
						hbaseService.get(hbaseSourceInstance, tableName, rowKey, versions)

				HashMap<String, HashMap<String, String>> flatRecord =
                    hbaseService.getFlatMap(patternService, hbaseSourceInstance, tableName, out)

				// get a (sorted) list of family names
				def allFamilies = hbaseService.tableList(hbaseSourceInstance).find {it.nameAsString.equals(tableName)}.families*.nameAsString.sort()

				return [hbaseSourceInstance: hbaseSourceInstance, res: out, allFamilies: allFamilies,
					tableNames: tableList, took: System.currentTimeMillis() - start,
					flatList: flatRecord, rowKey: rowKey]
			} else {
				return [hbaseSourceInstance: hbaseSourceInstance, tableNames: tableList]
			}
		}
	}

	/**
	 * Delete a HbaseSource definition. Does not delete the HBase tables in any way.
	 */
	def delete = {
		def hbaseSourceInstance = HbaseSource.get(params.id)
		if (hbaseSourceInstance) {
			try {
				hbaseSourceInstance.delete(flush: true)
				flash.message = "CloudInstance ${params.id} deleted"
				redirect(action: list)
			}
			catch (org.springframework.dao.DataIntegrityViolationException e) {
				flash.message = "CloudInstance ${params.id} could not be deleted"
				redirect(action: show, id: params.id)
			}
		}
		else {
			flash.message = "CloudInstance not found with id ${params.id}"
			redirect(action: list)
		}
	}

	/**
	 * Hook to edit the habsesource instance
	 */
	def edit = {
		def hbaseSourceInstance = HbaseSource.get(params.id)

		if (!hbaseSourceInstance) {
			flash.message = "CloudInstance not found with id ${params.id}"
			redirect(action: list)
		}
		else {
			return [hbaseSourceInstance: hbaseSourceInstance]
		}
	}

	/**
	 * Hook to update the hbasesource instance
	 */
	def update = {
		def hbaseSourceInstance = HbaseSource.get(params.id)
		if (hbaseSourceInstance) {
			if (params.version) {
				def version = params.version.toLong()
				if (hbaseSourceInstance.version > version) {

					hbaseSourceInstance.errors.rejectValue("version", "cloudInstance.optimistic.locking.failure", "Another user has updated this HbaseSource while you were editing.")
					render(view: 'edit', model: [hbaseSourceInstance: hbaseSourceInstance])
					return
				}
			}
			hbaseSourceInstance.properties = params
			if (!hbaseSourceInstance.hasErrors() && hbaseSourceInstance.save()) {
				flash.message = "CloudInstance ${params.id} updated"
				redirect(action: show, id: hbaseSourceInstance.id)
			}
			else {
				render(view: 'edit', model: [hbaseSourceInstance: hbaseSourceInstance])
			}
		}
		else {
			flash.message = "CloudInstance not found with id ${params.id}"
			redirect(action: list)
		}
	}

	/**
	 * Hook to create a new HbaseSource instance
	 */
	def create = {
		def hbaseSourceInstance = new HbaseSource()
		hbaseSourceInstance.properties = params
		return ['hbaseSourceInstance': hbaseSourceInstance]
	}

	/**
	 * Hook for the table creation page.
	 */
	@Secured(['ROLE_ROOT','ROLE_HBASE_ADMIN'])
	def createTable = {
		HbaseSource hbaseSourceInstance = HbaseSource.get(params.id)

		if (!hbaseSourceInstance) {
			flash.message = "CloudInstance not found with id ${params.id}"
			redirect(action: list)
		}
		int fcount = params.fcount ? Integer.parseInt(params.fcount) : 1
		return ['hbaseSourceInstance': hbaseSourceInstance, fcount: fcount]
	}

	/**
	 * Processes the table creation. Returns fast and leaves a thread for the completion. This method accepts also
	 * parameters that control the family form fragements that are displayed in the entry screen.
	 *
	 * Upon successful initiation of the create, this method returns to the Hbasesource instance list.
	 */
	@Secured(['ROLE_ROOT','ROLE_HBASE_ADMIN'])
	def saveTable = {
		HbaseSource hbaseSourceInstance = HbaseSource.get(params.id)

		if (!hbaseSourceInstance) {
			flash.message = "CloudInstance not found with id ${params.id}"
			redirect(action: createTable, id: params.id)
			return
		}

		int fcount = params.fcount ? Integer.parseInt(params.fcount) : 1

		// handle increasing the family form fragments
		if (params.increaseCreateTableFcount) {
			fcount += 1
			params.put('fcount', fcount)
			redirect(action: createTable, id: params.id, params: params)
			return
		}

		// handle decreasing the family form fragments
		if (params.rmCreateTableFamily) {
			fcount -= 1
			params.put('fcount', fcount)
			redirect(action: createTable, id: params.id, params: params)
			return
		}

		String tableName = params.tableName
		if (!tableName || tableName.length() == 0 || tableName.contains(' ')) {
			flash.message = "Table Name wrong"
			params.put('fcount', fcount)
			redirect(action: createTable, id: params.id, params: params)
			return
		}

		// Collect tha flat form parameters and push them into HColumnDescriptor's. Expect the following parameters:
		// family-1, valueX-1, valueY-1 etc

		// first collect a mapping between the number and the family name
		HashMap<String, String> familyNames = new HashMap<String, String>()
		HashMap<String, HColumnDescriptor> familySpecs = new HashMap<String, HColumnDescriptor>()
		HTableDescriptor tableSpec = new HTableDescriptor(tableName)
		params.each {String k, String[] v ->
			int inx = k.lastIndexOf('_')
			if (inx > -1) {        // no parameter for us
				String number = k.substring(inx + 1)
				String name = k.substring(0, inx)
				if (name.equals('familyName')) {
					if (!v || v[0].length() == 0 || !v[0] || v[0].length() == 0) {
						flash.message = "Missing family name for Family ${number}"
						params.put('fcount', fcount)
						redirect(action: createTable, id: params.id, params: params)
						return
					}
					String familyName = v[0]
					//System.out.println("v = " + v);
					familyNames.put(number, familyName)
					HColumnDescriptor familySpec = new HColumnDescriptor(familyName)
					familySpecs.put(familyName, familySpec)
					tableSpec.addFamily familySpec
				}
			}
		}
		if (familyNames.size() == 0) {
			flash.message = "Missing family specs"
			params.put('fcount', fcount)
			redirect(action: createTable, id: params.id, params: params)
			return
		}

		// now set all params
		// todo: proper try-catch
		params.each {String k, String[] v ->
			int inx = k.lastIndexOf('_')
			if (inx > -1) {
				String number = k.substring(inx + 1)
				String name = k.substring(0, inx)
				String familyName = familyNames.get(number)
				HColumnDescriptor familySpec = familySpecs.get(familyName)
				//System.out.println("familySpec = " + familySpec);
				if (name.equals('versions')) familySpec.setMaxVersions(Util.getInt(v[0], HColumnDescriptor.DEFAULT_VERSIONS));
				else if (name.equals('ttl')) familySpec.setTimeToLive(Util.getInt(v[0], HColumnDescriptor.DEFAULT_TTL));
				else if (name.equals('blockcache')) familySpec.setBlockCacheEnabled(Util.getBoolean(v[0], false /* NOT HColumnDescriptor.DEFAULT_BLOCKCACHE !! */));
				else if (name.equals('blocksize')) familySpec.setBlocksize(Util.getInt(v[0], HColumnDescriptor.DEFAULT_BLOCKSIZE));
//				else if (name.equals('bloomfilter')) familySpec.setBloomfilter(Util.getBoolean(v[0], false /* HColumnDescriptor.DEFAULT_BLOOMFILTER */));
				else if (name.equals('inmemory')) familySpec.setInMemory(Util.getBoolean(v[0], false /* HColumnDescriptor.DEFAULT_IN_MEMORY */));
				else if (name.equals('compression')) {
					String compression = v[0];
					if (compression != null && compression.length() > 0) {
						familySpec.setCompressionType(Compression.Algorithm.valueOf(compression));
					}
				} else {
					log.warn "Unknown family setting: ${k}"
				}
			} // if relevant
		} // each

		// todo: move up again, here just for testing form input quickly without server interaction
		if (hbaseService.tableList(hbaseSourceInstance).find {it.nameAsString.equals(tableName)}) {
			flash.message = "Table with name ${tableName} exists already."
			params.put('fcount', fcount)
			redirect(action: createTable, id: params.id)
			return
		}

		// push async creation.
		hbaseService.createTable hbaseSourceInstance, tableSpec
		flash.message = "New Table ${tableName} creation initiated"
		redirect(action: show, id: params.id)
	}

	/**
	 * Save a Hbasesource instance setting.
	 */
	def save = {
		def hbaseSourceInstance = new HbaseSource(params)
		
		if (!hbaseSourceInstance.hasErrors() && hbaseSourceInstance.save()) {
			flash.message = "CloudInstance ${hbaseSourceInstance.id} created"
			redirect(action: show, id: hbaseSourceInstance.id)
		}
		else {
			render(view: 'create', model: [hbaseSourceInstance: hbaseSourceInstance])
		}
	}

	/**
	 * Lists the threads that are currently executing. Polled by ajax includes
	 */
	def threads = {
		render(view: '_threads', model: [threadService: threadService])
	}

	/** 
	 * @param file - File to temporarly store and append to hbaseSourceInstance config
	 * @param hbaseSourceInstance - hbase source associated with this config file
	 */
	private def handleConfigFile = { file, hbaseSourceInstance ->

		File tmpDir = (File) servletContext.getAttribute('javax.servlet.context.tempdir')
		
		
		
		if(!file.empty) {
			
			def path = "config_${hbaseSourceInstance.name}_${file.getOriginalFilename()}_${System.currentTimeMillis()}.xml"
			def name = file.getOriginalFilename();
			def uploadedTimestamp = System.currentTimeMillis();
			
			File destinationFile = new File(tmpDir,path)
			

			file.transferTo( destinationFile )
			log.debug "Configuration file uploaded to ${destinationFile.getAbsolutePath()}"
			
			def confFile = new HbaseConfigFile(	name:name,
												location:destinationFile.getAbsolutePath(),
												uploadTimestamp:uploadedTimestamp,
												hbaseSource: hbaseSourceInstance );
			confFile.save();
			hbaseSourceInstance.configFiles.add(confFile);
			hbaseSourceInstance.save()
		}
		
	}

	def uploadFile = {
		HbaseSource hbaseSourceInstance = HbaseSource.get(params.id);
		
		if (hbaseSourceInstance){
			def f = request.getFile("configFile")
			handleConfigFile(f, hbaseSourceInstance)
		}
		
		render(view:'edit', model: [hbaseSourceInstance: hbaseSourceInstance]);
		
	}
	
	def removeConfigFile = { 
		
		HbaseSource hbaseSourceInstance = HbaseSource.get(params.id)
		HbaseConfigFile configFile = HbaseConfigFile.get(params.configFileId)
		
		if (configFile){
			def file = new File(configFile.location)
			
			if (file.exists()){
				file.delete()
			}
			
			hbaseSourceInstance.configFiles.remove(configFile)
			configFile.delete();
		}
		
		render(template:"filelist", model:[hbaseSourceInstance:hbaseSourceInstance])
	}
}
