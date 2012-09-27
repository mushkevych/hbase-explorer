<html>
    <head>
        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8"/>
        <meta name="layout" content="main" />
        <title>HBase Source List</title>
    </head>
    <body>
  	<div class="container">
    	<h2>HBase Connections List</h2>
    	<br/>
            <g:if test="${flash.message}">
        	    <div class="alert alert-info">${flash.message}</div>
            </g:if>
            <g:if test="${hbaseSourceInstanceList.size() == 0}">
              <div class="alert alert-error">
                No HBase connection exists. Please <g:link controller="hbaseSource" action="create">create</g:link> one.
              </div>
            </g:if>
            <g:else>
            <g:if test="${flash.errmessage}">
            	<div class="alert alert-error">
					${flash.errmessage}
				</div>
			</g:if>
            <div class="container">
				<table class="table table-striped table-bordered">
                	<thead>
                		<tr>
                			<th>Name</th>
                			<th>Quorum Servers</th>
                			<th>Actions</th>
                		</tr>
                	</thead>
                    <tbody>
                    <g:each in="${hbaseSourceInstanceList}" status="i" var="hbaseSourceInstance">
                    	<tr>
                        	<td>
                              <g:link action="show" id="${hbaseSourceInstance.id}">${fieldValue(bean:hbaseSourceInstance, field:'name')}</g:link>
                              <br>
                              <g:link action="edit" id="${hbaseSourceInstance.id}">(Edit)</g:link>
                            </td>
							<td>
                                ${fieldValue(bean:hbaseSourceInstance, field:'quorumServers')?.replace(',',', ')}
								<br/>port: ${fieldValue(bean:hbaseSourceInstance, field:'quorumPort')}
                            </td>
							<td>
								<ul class="nav nav-list">
									<li>
										<g:link action="show" id="${hbaseSourceInstance.id}">
                              		  	<i class="icon-eye-open"></i>
                              		  	Show Details</g:link>
									</li>
									<li>
										<g:link action="scan" id="${hbaseSourceInstance.id}">
                              		 	<i class="icon-align-justify"></i>
                              		  	Scan</g:link>
									</li>
									<li>
										<g:link url="${hbaseSourceInstance.masterUrl}">
										<i class="icon-info-sign"></i>
										Master UI</g:link>
									</li>
									<li>
										<g:link action="createTable" id="${hbaseSourceInstance.id}">
										<i class="icon-plus-sign"></i>
										New Table</g:link>
									</li>
								</ul>
                                <%-- <g:link action="count" id="${hbaseSourceInstance.id}">Count</g:link> --%>
                              </td>
                          </tr>
                      </g:each>
                      </tbody>
                  </table>
              </div>
              <!-- 
              <div class="paginateButtons">
                  <g:paginate total="${hbaseSourceInstanceTotal}" />
              </div>
               -->
            </g:else>
        </div>
    </body>
</html>
