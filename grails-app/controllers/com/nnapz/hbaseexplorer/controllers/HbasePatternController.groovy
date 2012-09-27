package com.nnapz.hbaseexplorer.controllers

import com.nnapz.hbaseexplorer.domain.HbasePattern
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

class HbasePatternController {

    static allowedMethods = [save: "POST", update: "POST", delete: "POST"]
  
    def index = {
        redirect(action: "list", params: params)
    }

    def list = {
        params.max = Math.min(params.max ? params.int('max') : 10, 100)
        [hbasePatternInstanceList: HbasePattern.list(params), hbasePatternInstanceTotal: HbasePattern.count()]
    }

    def create = {
        def hbasePatternInstance = new HbasePattern()
        hbasePatternInstance.properties = params
        return [hbasePatternInstance: hbasePatternInstance]
    }

    def save = {
        def hbasePatternInstance = new HbasePattern(params)
        if (hbasePatternInstance.save(flush: true)) {
            flash.message = "${message(code: 'default.created.message', args: [message(code: 'hbasePattern.label', default: 'HbasePattern'), hbasePatternInstance.id])}"
            redirect(action: "list")
        }
        else {
            render(view: "create", model: [hbasePatternInstance: hbasePatternInstance])
        }
    }

    def show = {
        def hbasePatternInstance = HbasePattern.get(params.id)
        if (!hbasePatternInstance) {
            flash.message = "${message(code: 'default.not.found.message', args: [message(code: 'hbasePattern.label', default: 'HbasePattern'), params.id])}"
            redirect(action: "list")
        }
        else {
            [hbasePatternInstance: hbasePatternInstance]
        }
    }

    def edit = {
        def hbasePatternInstance = HbasePattern.get(params.id)
        if (!hbasePatternInstance) {
            flash.message = "${message(code: 'default.not.found.message', args: [message(code: 'hbasePattern.label', default: 'HbasePattern'), params.id])}"
            redirect(action: "list")
        }
        else {
            return [hbasePatternInstance: hbasePatternInstance]
        }
    }

    def update = {
        def hbasePatternInstance = HbasePattern.get(params.id)
        if (hbasePatternInstance) {
            if (params.version) {
                def version = params.version.toLong()
                if (hbasePatternInstance.version > version) {
                    
                    hbasePatternInstance.errors.rejectValue("version", "default.optimistic.locking.failure", [message(code: 'hbasePattern.label', default: 'HbasePattern')] as Object[], "Another user has updated this HbasePattern while you were editing")
                    render(view: "edit", model: [hbasePatternInstance: hbasePatternInstance])
                    return
                }
            }
            hbasePatternInstance.properties = params
            if (!hbasePatternInstance.hasErrors() && hbasePatternInstance.save(flush: true)) {
                flash.message = "${message(code: 'default.updated.message', args: [message(code: 'hbasePattern.label', default: 'HbasePattern'), hbasePatternInstance.id])}"
                redirect(action: "list", id: hbasePatternInstance.id)
            }
            else {
                render(view: "edit", model: [hbasePatternInstance: hbasePatternInstance])
            }
        }
        else {
            flash.message = "${message(code: 'default.not.found.message', args: [message(code: 'hbasePattern.label', default: 'HbasePattern'), params.id])}"
            redirect(action: "list")
        }
    }

    def delete = {
        def hbasePatternInstance = HbasePattern.get(params.id)
        if (hbasePatternInstance) {
            try {
                hbasePatternInstance.delete(flush: true)
                flash.message = "${message(code: 'default.deleted.message', args: [message(code: 'hbasePattern.label', default: 'HbasePattern'), params.id])}"
                redirect(action: "list")
            }
            catch (org.springframework.dao.DataIntegrityViolationException e) {
                flash.message = "${message(code: 'default.not.deleted.message', args: [message(code: 'hbasePattern.label', default: 'HbasePattern'), params.id])}"
                redirect(action: "show", id: params.id)
            }
        }
        else {
            flash.message = "${message(code: 'default.not.found.message', args: [message(code: 'hbasePattern.label', default: 'HbasePattern'), params.id])}"
            redirect(action: "list")
        }
    }
}
