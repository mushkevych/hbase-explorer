package com.nnapz.hbaseexplorer.controllers

import com.nnapz.hbaseexplorer.domain.HbaseTableStats
import com.nnapz.hbaseexplorer.domain.HbaseSource

class HbaseTableStatsController {
    
    def index = { redirect(action:list,params:params) }

    // the delete, save and update actions only accept POST requests
    static allowedMethods = [delete:'POST', save:'POST', update:'POST']


   /**
    * unlike the normal grails list, we expect here also the hbaseSource and the tablename to filter for.
    */
    def list = {
       def hbaseSourceInstance = HbaseSource.get(params.hbaseSource)

    if (!hbaseSourceInstance) {
      flash.message = "CloudInstance not found with id ${params.hbaseSource}"
      redirect(controller: hbaseSource, action: list)
    }

    String tableName = params.tableName
    if (!tableName || tableName.length() == 0 || tableName.contains(' ')) {
      flash.message = "Table Name wrong"
      redirect(controller: hbaseSource, action: show, id: params.hbaseSource)
      return
    }

      params.max = Math.min( params.max ? params.max.toInteger() : 10,  100)
      def tableStats = HbaseTableStats.findAllByHbaseSourceAndTableName(hbaseSourceInstance, tableName, params)?.reverse()
      int count = HbaseTableStats.countByHbaseSourceAndTableName(hbaseSourceInstance, tableName, params)
      [ hbaseTableStatsInstanceList: tableStats, hbaseTableStatsInstanceTotal: count,
              tableName: tableName, hbaseSourceInstance: hbaseSourceInstance  ]
    }

    def show = {
        def hbaseTableStatsInstance = HbaseTableStats.get( params.id )

        if(!hbaseTableStatsInstance) {
            flash.message = "HbaseTableStats not found with id ${params.id}"
            redirect(action:list)
        }
        else { return [ hbaseTableStatsInstance : hbaseTableStatsInstance ] }
    }

    def delete = {
        def hbaseTableStatsInstance = HbaseTableStats.get( params.id )
        if(hbaseTableStatsInstance) {
            try {
                hbaseTableStatsInstance.delete(flush:true)
                flash.message = "HbaseTableStats ${params.id} deleted"
                redirect(action:list)
            }
            catch(org.springframework.dao.DataIntegrityViolationException e) {
                flash.message = "HbaseTableStats ${params.id} could not be deleted"
                redirect(action:show,id:params.id)
            }
        }
        else {
            flash.message = "HbaseTableStats not found with id ${params.id}"
            redirect(action:list)
        }
    }

    // perhaps to keep some comments ? --%>
    def update = {
        def hbaseTableStatsInstance = HbaseTableStats.get( params.id )
        if(hbaseTableStatsInstance) {
            if(params.version) {
                def version = params.version.toLong()
                if(hbaseTableStatsInstance.version > version) {
                    
                    hbaseTableStatsInstance.errors.rejectValue("version", "hbaseTableStats.optimistic.locking.failure", "Another user has updated this HbaseTableStats while you were editing.")
                    render(view:'edit',model:[hbaseTableStatsInstance:hbaseTableStatsInstance])
                    return
                }
            }
            hbaseTableStatsInstance.properties = params
            if(!hbaseTableStatsInstance.hasErrors() && hbaseTableStatsInstance.save()) {
                flash.message = "HbaseTableStats ${params.id} updated"
                redirect(action:show,id:hbaseTableStatsInstance.id)
            }
            else {
                render(view:'edit',model:[hbaseTableStatsInstance:hbaseTableStatsInstance])
            }
        }
        else {
            flash.message = "HbaseTableStats not found with id ${params.id}"
            redirect(action:list)
        }
    }

    def save = {
        def hbaseTableStatsInstance = new HbaseTableStats(params)
        if(!hbaseTableStatsInstance.hasErrors() && hbaseTableStatsInstance.save()) {
            flash.message = "HbaseTableStats ${hbaseTableStatsInstance.id} created"
            redirect(action:show,id:hbaseTableStatsInstance.id)
        }
        else {
            render(view:'create',model:[hbaseTableStatsInstance:hbaseTableStatsInstance])
        }
    }
}
