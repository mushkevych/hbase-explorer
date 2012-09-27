<% String p = null %>
<%@ page import="org.apache.hadoop.hbase.HColumnDescriptor" %>
<html>
    <head>
        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8"/>
        <meta name="layout" content="main" />
        <title>Create New Table</title>
    </head>
    <body>
    	<content tag="customnavi">
    		<li class="divider-vertical"></li>
    		<li><g:link controller="hbaseSource" action="show" id="${hbaseSourceInstance?.id}">Show <strong>${hbaseSourceInstance?.name}</strong></g:link>
  		</content>
  		<div class="container">
  			<div class="span12">
  				<h1>Create new HBase Table on <i>${hbaseSourceInstance.name}</i></h1>
  				<g:if test="${flash.message}">
            		<div class="alert alert-info">${flash.message}</div>
            	</g:if>
            	<g:hasErrors bean="${hbaseSourceInstance}">
            		<div class="alert alert-error">
                		<g:renderErrors bean="${hbaseSourceInstance}" as="list" />
            		</div>
            	</g:hasErrors>
  			</div>
  			<div class="span12">
  				<g:form class="form-horizontal" action="saveTable" method="post" >
  					<input type="hidden" name="id" value="${hbaseSourceInstance.id}" />
                	<input type="hidden" name="fcount" value="${fcount}" />
  					<fieldset>
  						<div class="control-group">
    						<label class="control-label" for="tableName">Table Name</label>
							<div class="controls">
								<input class="input-xlarge" type="text" id="tableName" name="tableName" value="${params.tableName}"/>
							</div>
						</div>
						<div class="row">
							<div id="family-accordion" class="accordion">
								<g:each var="fcnt" in="${(1..fcount)}">
									<div class="accordion-group">
										<div class="row">
										<div  class="accordion-heading">
											<div class="span12" style="margin: 13px;">
									 		<div class="control-group left" style="margin-bottom: 0px;">
    											<label class="control-label" for="familyName_${fcnt}">Family ${fcnt} Name</label>
												<div class="controls">
													 <input class="input-large" type="text" id="familyName_${fcnt}" name="familyName_${fcnt}" value="${params["familyName_" + fcnt]}"/>
												</div>
											</div>
											<span class="left" style="margin-left: 20px;"><a  href="#family_${fcnt}" data-parent="#family-accordion" data-toggle="collapse">toggle details</a></span>
											</div>
											</div>
										</div>
										<div id="family_${fcnt}" class="accordion-body collapse">
										<div class="accordion-inner">
											<div class="row">
												<div class="span5">
													<div class="control-group">
														<label class="control-label" for="versions_${fcnt}">Versions</label>
														<div class="controls">
															<input class="input" type="text" name="versions_${fcnt}" value="${params["versions_" + fcnt] ?: HColumnDescriptor.DEFAULT_VERSIONS}"/>
														</div>
													</div>
													<div class="control-group">
														<label class="control-label" for="ttl_${fcnt}">TTL</label>
														<div class="controls">
															<input class="input" type="text" name="ttl_${fcnt}" value="${params["ttl_" + fcnt] ?: HColumnDescriptor.DEFAULT_TTL}"/>
															<p class="help-block">30 days = 108000</p>
														</div>
													</div>
												</div>
												<div class="span5">
													<div class="control-group">
														<label class="control-label" for="blocksize_${fcnt}">Block Size</label>
														<div class="controls">
															<input class="input" type="text" name="blocksize_${fcnt}" value="${params["blocksize_" + fcnt] ?: HColumnDescriptor.DEFAULT_BLOCKSIZE}"/>
														</div>
													</div>
													<div class="control-group">
														<label class="control-label" for="compression_${fcnt}">Compression</label>
														<div class="controls">
															<select name="compression_${fcnt}">
                                       							<g:each var="c" in="${['NONE', 'GZ', 'LZO']}">
                                            						<option value="${c}" <%= c == params["compression_" + fcnt] ? 'selected="selected"' : '' %> >${c}</option>
                                        						</g:each>
                                      						</select>
														</div>
													</div>		
												</div>
												<div class="span12">
													<div class="control-group left">
														<label class="control-label" for="inmemory_${fcnt}">In Memory</label>
														<div class="controls">
															<input type="checkbox" id="${fcnt}inmemory_1" name="inmemory_${fcnt}" ${params["inmemory_" + fcnt] && params["inmemory_" + fcnt] == 'true' ? 'checked=checked': '' }/>
														</div>
													</div>
													<div class="control-group left">
														<label class="control-label left" for="blockcache_${fcnt}">Block Cache</label>
														<div class="controls">
															<input type="checkbox" id="blockcache_${fcnt}" name="blockcache_${fcnt}" ${params["blockcache_" + fcnt] && params["blockcache_" + fcnt] == 'true' ? 'checked=checked': '' }/>
														</div>
													</div>
													<div class="control-group left">
														<label class="control-label" for="bloomfilter_${fcnt}">Bloomfilter</label>
														<div class="controls">
															<input type="checkbox" id="bloomfilter_${fcnt}" name="bloomfilter_${fcnt}" ${params["bloomfilter_" + fcnt] && params["bloomfilter_" + fcnt] == 'true' ? 'checked=checked': '' }/>
														</div>
													</div>	
												</div>
											</div>
										</div>
										</div>
									</div>
								</g:each>
							</div>
						</div>
						<div class="span12">
  							<div class="form-actions">
								<g:actionSubmit class="btn btn-primary" value="Save Table" />
								<g:submitButton class="btn" value="+ Add Family" name="increaseCreateTableFcount" />
                            	<g:submitButton class="btn" value="- Remove Last Family" name="rmCreateTableFamily" />
							</div>
						</div>
  					</fieldset>
  					
  				</g:form>
  			</div>
  		</div>
    </body>
</html>
