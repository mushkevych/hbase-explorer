
<html>
    <head>
        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8"/>
        <meta name="layout" content="main" />
        <title>Show HBase Instance</title>
        
    </head>
    <body>
		<content tag="customnavi">

		</content>
		<div class="container">
			<div class="span12">
				<h1>Details on HBase Cloud <i class="cloud-name" >${hbaseSourceInstance.name}</i></h1>
				<g:if test="${flash.message}">
					<div class="alert alert-info">
						${flash.message}
					</div>
				</g:if>
			</div>
			<div class="span9">	
				<table class="table">
					<tbody>
					<tr>
						<td class="left-td">Name:</td>
						<td>${fieldValue(bean:hbaseSourceInstance, field:'name')}, Id:${fieldValue(bean:hbaseSourceInstance, field:'id')}</td>
					</tr>
					<tr>
						<td class="left-td">Quorum Port:</td>
						<td>${fieldValue(bean:hbaseSourceInstance, field:'quorumPort')}</td>
					</tr>
					<tr>
						<td class="left-td">Quorum Servers:</td>
						<td>${fieldValue(bean:hbaseSourceInstance, field:'quorumServers')}</td>
					</tr>
					<tr>
						<td class="left-td">Master URL:</td>
						<td>${fieldValue(bean:hbaseSourceInstance, field:'masterUrl')}</td>
					</tr>
					</tbody>
				</table>
			</div>		
			<ul class="nav nav-list span2" style="font-size: large;" >
				<li>
					<g:link action="show" id="${hbaseSourceInstance.id}">
                         <i class="icon-eye-open"></i>
                         Show Details</g:link>
				</li>
				<li>
					<g:link action="scan" id="${hbaseSourceInstance.id}">
                   	<i class="icon-search"></i>
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
			<div class="span12">
			<hr/>
			</div>
			<div class="span12"  id="table_${hbaseSourceInstance.id}">
				<script type="text/javascript">
            		<g:remoteFunction action="tables" id="${hbaseSourceInstance.id}" update="table_${hbaseSourceInstance.id}" />
           		</script>
           		<div class="span4" style="font-size: large;">
           			fetching table information....
                    <img src="${createLinkTo(dir:'images',file:'spinner.gif')}" alt="Waiting.." />
                </div>
			</div>
			<div class="span12">
				<g:form class="form-inline">
					<fieldset>
						<input type="hidden" name="id" value="${hbaseSourceInstance?.id}" />
						<div class="form-actions">
  						<g:actionSubmit class="btn" value="Edit Source" action="edit"/>
  						<g:actionSubmit class="btn btn-danger" action="delete" onclick="return confirm('Are you sure?');" value="Delete Source" />
  						</div>
  					</fieldset>
  				</g:form>
			</div>
        </div>
        
    </body>
</html>
