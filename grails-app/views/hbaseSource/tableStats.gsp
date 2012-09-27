<html>
    <head>
        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8"/>
        <meta name="layout" content="main" />
        <title>Table Statistics for ${tableName} @ ${hbaseSource.name}</title>
    </head>
    <body>
    	<content tag="customnavi">
    		<li class="divider-vertical"></li>
    		<li><g:link controller="hbaseSource" action="show" id="${hbaseSource?.id}">HBase Source <strong>${hbaseSource.name}</strong></g:link>
    	</content>
		<div class="container">
			<h1>Stats from ${tableName} @ ${hbaseSource.name}</h1>
			 <g:if test="${flash.message}">
            	<div class="alert alert-info">${flash.message}</div>
             </g:if>
             <table class="table table-bordered table-striped">
             	<g:each in="${families}" var="family">    <%-- its a HbaseTableStats object --%>
              		<g:if test="${family.tableName}"><%-- otherwise its the table summary --%>
                		<tr>
                  			<td></td>
                  			<td>${family.creationDate}</td>
                  			<td>${family.familyName}</td>
                  			<td>${family.totalTimestamps}</td>
                		</tr>
              		</g:if>
              		<g:else> <%-- table summry line --%>
                		<tr>
                  			<td>${family.tableName}</td>
                		</tr>
             		 </g:else>
            	</g:each>
            </table>
		</div>
        <div class="body">
            <table>
            <g:each in="${families}" var="family">    <%-- its a HbaseTableStats object --%>
              <g:if test="${family.tableName}"><%-- otherwise its the table summary --%>
                <tr>
                  <td></td>
                  <td>${family.creationDate}</td>
                  <td>${family.familyName}</td>
                  <td>${family.totalTimestamps}</td>
                </tr>
              </g:if>
              <g:else> <%-- table summry line --%>
                <tr>
                  <td>
                    ${family.tableName}
                  </td>
                </tr>
              </g:else>
            </g:each>
            </table>

          <g:link action="pushTableStats" id="${hbaseSource.id}" params="${[tableName: tableName]}" title="Start a Stats Calculation">Push a Stats collection for ${tableName}</g:link>
        </div>
    </body>
</html>
