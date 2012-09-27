<%-- include to show one set of tables. Called by AJAX from list.gsp --%>

  <g:if test="${errorMsg != null}">
    <div class="alert alert-error">
    	${errorMsg}
    </div>
  </g:if>
  <g:else>
  	<g:each var="table" in="${tableList}">
  		<div class="row <% if (!online.contains(table.nameAsString)){ %>offline<% } %>">
  		<div class="span2">
  			<h3 class="<% if (!online.contains(table.nameAsString)){ %>offline<% } %>">${table.nameAsString}</h3>
  			<script type="text/javascript">
        		<g:remoteFunction action="regionCount" params="${[id:hbaseSourceInstance.id, tableName:table.nameAsString]}" update="r_${hbaseSourceInstance.id}_${table.nameAsString}"/>
        	</script>
        	<div style="margin-bottom: 5px;" id="r_${hbaseSourceInstance.id}_${table.nameAsString}">...</div>
   			
  			<g:form id="${hbaseSourceInstance.id}" method="POST">
  				<input type="hidden" name="tableName" value="${table.nameAsString}" >
  				<g:if  test="${!online.contains(table.nameAsString)}">
  					<g:actionSubmit  style="width:100px;" class="btn btn-small btn-success table-control" action="enableTable" value="Enable Table"/>
  					<g:actionSubmit  style="width:100px;" class="btn btn-small btn-danger table-control" action="dropTable" value="Drop Table"/>
				</g:if>
				<g:else>
					<g:link class="btn btn-mini table-control" controller="hbaseTableStats" action="list" params="${[hbaseSource: hbaseSourceInstance.id, tableName: table.nameAsString]}" 
        			title="Show Stats">Statistics</g:link>
					<g:actionSubmit style="width:100px;" class="btn btn-mini btn-inverse table-control" action="disableTable" value="Disable Table"/>
				</g:else>
				<g:link class="btn btn-small  table-control" action="cloneTable" id="${hbaseSourceInstance.id}" params="${[tableName: table.nameAsString]}" 
        			title="Clone Definiton w/o data">Clone</g:link>
  			</g:form>
  		</div>
  		<div class="span10">
  		<g:each var="family" in="${table.families}">
  			<div class="span2" style="margin-bottom: 8px;">
  				<h6 style="font-size: 1.1em;">${family.nameAsString}</h6>
  				<ul class="unstyled">
  					<li>versions: <b>${family.maxVersions}</b></li>
  					<li>TTL: <b>${Math.round(family.timeToLive / 60 / 60 / 24)}d </b></li>
  					<li>compression: <b>${family.compression} </b></li>
  					<li><a class="family-details" href="#" rel="popover" data-content="block-cache: ${family.isBlockCacheEnabled()}, 
  								block-size:${family.blocksize}, in-memory:${family.isInMemory()}, &#13;&#10;bloom-filter:${family.getBloomFilterType()}" 
  								data-original-title="${family.nameAsString}" onmouseover="$(this).popover('show');">more</a>
  					</li>
  				</ul>
  			</div>
  		</g:each>
  		</div>
  	</div>
  	<hr style="margin: 10px 0;"/>
  	</g:each>
 	
  </g:else>
