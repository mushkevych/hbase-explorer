%{--
  - Licensed under the Apache License, Version 2.0 (the "License");
  - you may not use this file except in compliance with the License.
  - You may obtain a copy of the License at
  -
  -   http://www.apache.org/licenses/LICENSE-2.0
  -
  - Unless required by applicable law or agreed to in writing, software
  - distributed under the License is distributed on an "AS IS" BASIS,
  - WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  - See the License for the specific language governing permissions and
  - limitations under the License.
  --}%
<h3><g:message code="Pattern Match"/></h3>
<br/>
<div class="span5">
	<div class="control-group <% if (hasErrors(bean:hbasePatternInstance,field:'hbase','errors')){%>error<% }%>">
		<label class="control-label" for="hbase">HBase Connection</label>
		<div class="controls">
			<input class="input-large" type="text" name="hbase" value="${fieldValue(bean:hbasePatternInstance,field:'hbase')}"/>
			<p class="help-block">HBase Connection name pattern.</p>
		</div>
	</div>	
	<div class="control-group <% if (hasErrors(bean:hbasePatternInstance,field:'table','errors')){%>error<% }%>">
		<label class="control-label" for="table">Table</label>
		<div class="controls">
			<input class="input-large" type="text" name="table" value="${fieldValue(bean:hbasePatternInstance,field:'table')}"/>
			<p class="help-block">Table to apply the config to.</p>
		</div>
	</div>
	<div class="control-group <% if (hasErrors(bean:hbasePatternInstance,field:'family','errors')){%>error<% }%>">
		<label class="control-label" for="family">Family</label>
		<div class="controls">
			<input class="input-large" type="text" name="family" value="${fieldValue(bean:hbasePatternInstance,field:'family')}"/>
			<p class="help-block">Name of the family to apply.</p>
		</div>
	</div>
</div>
<div class="span5">
	<div class="control-group <% if (hasErrors(bean:hbasePatternInstance,field:'column','errors')){%>error<% }%>">
		<label class="control-label" for="column">Column</label>
		<div class="controls">
			<input class="input-large" type="text" name="column" value="${fieldValue(bean:hbasePatternInstance,field:'column')}"/>
		</div>
	</div>
	<div class="control-group <% if (hasErrors(bean:hbasePatternInstance,field:'linkColumn','errors')){%>error<% }%>">
		<label class="control-label" for="linkColumn">Process Column</label>
		<div class="controls">
			<g:checkBox name="linkColumn" value="${hbasePatternInstance.linkColumn}"/>
			<p class="help-block">Use regular expressions to define a field match.</p>
		</div>
	</div>
	<br/>		
	<div class="control-group <% if (hasErrors(bean:hbasePatternInstance,field:'value','errors')){%>error<% }%>">
		<label class="control-label" for="column">Value</label>
		<div class="controls">
			<input class="input-large" type="text" name="value" value="${fieldValue(bean:hbasePatternInstance,field:'value')}"/>		
		</div>
	</div>
	<div class="control-group <% if (hasErrors(bean:hbasePatternInstance,field:'linkValue','errors')){%>error<% }%>">
		<label class="control-label" for="linkValue">Process Value</label>
		<div class="controls">
			<g:checkBox name="linkValue" value="${hbasePatternInstance.linkValue}"/>
			<p class="help-block">Check this if the following section should be applied to the displayed values.</p>
		</div>
	</div>	
</div>
<div class="span12">
	<hr>
	<h3><g:message code="Display Config" /></h3>
	<br/>
	<div class="control-group <% if (hasErrors(bean:hbasePatternInstance,field:'type','errors')){%>error<% }%>">
		<label class="control-label" for="type">Type</label>
		<div class="controls">
			<g:select name="type" from="${hbasePatternInstance.constraints.type.inList}" value="${hbasePatternInstance?.type}" valueMessagePrefix="hbasePattern.type"/>                 
			<p class="help-block">Type to use for displaying the data.</p>
		</div>
	</div>
	<div class="control-group <% if (hasErrors(bean:hbasePatternInstance,field:'targetLink','errors')){%>error<% }%>">
		<label class="control-label" for="targetLink">Target Link</label>
		<div class="controls">
			<input class="input-xlarge" type="text" name="targetLink" value="${fieldValue(bean:hbasePatternInstance,field:'targetLink')}"/>		
			<p class="help-block">scan?tableName=user&rowKey=@COLUMN@&id=1<br>
                          Use these placeholders: @HBASE@, @TABLE@, @FAMILY@, @COLUMN@, @VALUE@.</p>
		</div>
	</div>
</div>