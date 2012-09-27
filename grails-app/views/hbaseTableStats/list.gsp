<html>
    <head>
        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8"/>
        <meta name="layout" content="main" />
        <title>Table Stats for ${tableName} on ${hbaseSourceInstance.name}</title>
    </head>
    <body>
    	<content tag="customnavi">
    		<li class="divider-vertical"></li>
    		<li><g:link controller="hbaseSource" action="show" id="${hbaseSourceInstance?.id}">Show <strong>${hbaseSourceInstance?.name}</strong></g:link>
    	</content>
    	<div class="container">
    		<div class="span12">
    			<h1>Table statistics for <i>${tableName}</i> on <i>${hbaseSourceInstance.name}</i></h1>
    			<br/>
    		 	<g:if test="${flash.message}">
            		<div class="alert alert-info">${flash.message}</div>
            		<br/>
            	</g:if>
            	<g:if test="${hbaseTableStatsInstanceList.size() == 0}">
              		<div class="alert">No data available yet. Please push a statistics collection below.</div>
              		<br/>
            	</g:if>
            </div>
            <div class="span12">
            <table id="getresult" class="table table-bordered" >
            	<thead>
                  <tr>
                  	<th colspan="2">creation</th>
                  	<th>timestamp count</th>
                   	<th>column count</th>
                   	<th>column size</th>
                   	<th>value count</th>
                   	<th>value size</th>
                   	<th>data size</th>
                   </tr>
                 </thead>
                 <tbody>
                    <g:each in="${hbaseTableStatsInstanceList}" status="i" var="hbaseTableStatsInstance">
                        <tr class="flatrow" >
                            <td colspan="2">
                              ${fieldValue(bean:hbaseTableStatsInstance, field:'creationDate')} <br>
                              ${fieldValue(bean:hbaseTableStatsInstance, field:'rowCount')}  Rows<br>
                              ${Math.round((double) hbaseTableStatsInstance.executionTime / 60000D)} min for ${hbaseTableStatsInstance.regionCount} Regions 
                            </td>
                            <td>${fieldValue(bean:hbaseTableStatsInstance, field:'totalTimestamps')}
                              <!-- to UI: this is the more-less unique number of distinct ts within the family. Remember, ts are bound to the column value and thus
                                   the same ones may appear many times within a family or in the set of families in that row.
                                   So this number represents the number of "records" that were written -->
                            </td>
                            <td>${fieldValue(bean:hbaseTableStatsInstance, field:'columnCount')}</td>
                            <td>${fieldValue(bean:hbaseTableStatsInstance, field:'columnSize')}</td>
                            <td>${fieldValue(bean:hbaseTableStatsInstance, field:'valueCount')}</td>
                            <td>${fieldValue(bean:hbaseTableStatsInstance, field:'valueSize')}</td>
                            <td>${fieldValue(bean:hbaseTableStatsInstance, field:'dataSize')}</td>       
                        </tr>
                      <g:each in="${hbaseTableStatsInstance.hbaseFamilyStats?.sort{it.familyName}}" var="family">
                          <tr>
                            <td>&nbsp;</td>
                            <td>Family <span class="columnname">${family.familyName.encodeAsHTML()}</span></td>
                            <td>${fieldValue(bean:family, field:'totalTimestamps')}
                              <!-- this number is usually higher than tha unique count on the row level. It represents the number of different timestamp-value pair in that
                                   family. -->
                            </td>
                            <td>${fieldValue(bean:family, field:'columnCount')}</td>
                            <td>${fieldValue(bean:family, field:'columnSize')}</td>
                            <td>${fieldValue(bean:family, field:'valueCount')}</td>
                            <td>${fieldValue(bean:family, field:'valueSize')}</td>
                            <td>${fieldValue(bean:family, field:'dataSize')}</td>
                          </tr>
                       </g:each>
                    </g:each>
                    <%-- todo: place a google piechart here---%>
                    </tbody>
            	</table>
            	<!-- 
            	<div class="paginateButtons">
                	<g:paginate total="${hbaseTableStatsInstanceTotal}" params="${[hbaseSource: hbaseSourceInstance.id]}"/>
            	</div>
            	 -->
            	<g:link class="btn btn-primary" controller="hbaseSource" action="pushTableStats" id="${hbaseSourceInstance.id}" params="[tableName: tableName]">Start a new Stats Collection</g:link>
            </div>
    	</div>
    </body>
</html>
