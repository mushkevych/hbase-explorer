
<html>
    <head>
        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
        <meta name="layout" content="main" />
        <g:set var="entityName" value="${message(code: 'hbasePattern.label', default: 'HbasePattern')}" />
        <title><g:message code="default.list.label" args="[entityName]" /></title>
    </head>
    <body>
        <g:render template="/navi"/>
		<div class="container">
            <h1>Pattern List</h1>
            <br/>
            <g:if test="${flash.message}">
            	<div class="alert alert-info">${flash.message}</div>
            </g:if>
            <table class="table table-striped table-bordered" style="width: 100%; font-size: 12px;" >
            	<thead>
                	<tr>
                      	<th>${message(code: 'hbasePattern.id.label', default: 'Id')}</th>
                       	<th>${message(code: 'hbasePattern.hbase.label', default: 'Hbase')}</th>
                       	<th>${message(code: 'hbasePattern.table.label', default: 'Table')}</th>
                       	<th>${message(code: 'hbasePattern.family.label', default: 'Family')}</th>
						<th>${message(code: 'hbasePattern.column.label', default: 'Column')}</th>
						<th>${message(code: 'hbasePattern.value.label', default: 'Value')}</th>
							
						<th>${message(code: 'hbasePattern.linkColumn.label', default: 'Link Column')}</th>
						<th>${message(code: 'hbasePattern.linkV.labalueel', default: 'Link Value')}</th>
						<th>${message(code: 'hbasePattern.type.label', default: 'Type')}</th>
					</tr>
                 </thead>
                 <tbody>
                    <g:each in="${hbasePatternInstanceList}" status="i" var="hbasePatternInstance">
                    	<tr>
                        
                            <td><g:link action="edit" id="${hbasePatternInstance.id}">${fieldValue(bean: hbasePatternInstance, field: "id")}</g:link></td>
                        

                            <td>${fieldValue(bean: hbasePatternInstance, field: "hbase")}</td>
                        
                            <td>${fieldValue(bean: hbasePatternInstance, field: "table")}</td>
                            <td>${fieldValue(bean: hbasePatternInstance, field: "family")}</td>
                            <td>${fieldValue(bean: hbasePatternInstance, field: "column")}</td>
                            <td>${fieldValue(bean: hbasePatternInstance, field: "value")}</td>

                            <td>${fieldValue(bean: hbasePatternInstance, field: "linkColumn")}</td>
                            <td>${fieldValue(bean: hbasePatternInstance, field: "linkValue")}</td>
                            <td>${fieldValue(bean: hbasePatternInstance, field: "type")}</td>
                        </tr>
                    </g:each>
                 </tbody>
             </table>
             <!-- 
             <div class="paginateButtons">
                <g:paginate total="${hbasePatternInstanceTotal}" />
            </div>
             -->
        </div>
    </body>
</html>
