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
<div class="control-group <% if (hasErrors(bean:hbaseSourceInstance,field:'name','errors')){%>error<% }%>">
    <label class="control-label" for="name">Name</label>
	<div class="controls">
		<input class="input-xlarge" type="text" id="name" name="name" value="${fieldValue(bean:hbaseSourceInstance,field:'name')}"/>
		<p class="help-block">The name of this instance within this tool.</p>
	</div>
</div>

<div class="control-group <% if (hasErrors(bean:hbaseSourceInstance,field:'quorumPort','errors')){%>error<% }%>">
    <label class="control-label" for="quorumPort">Quorum Client Port</label>
	<div class="controls">
		<input class="input-xlarge" type="text" id="quorumPort" name="quorumPort" value="${fieldValue(bean:hbaseSourceInstance,field:'quorumPort')}"/>
 		<p class="help-block"> The ZooKeeper Port that clients should contact.</p>
	</div>
</div>

<div class="control-group <% if (hasErrors(bean:hbaseSourceInstance,field:'quorumServers','errors')){%>error<% }%>">
    <label class="control-label" for="quorumPort">Quorum Servers</label>
	<div class="controls">
		<input class="input-xlarge" type="text" id="quorumServers" name="quorumServers" value="${fieldValue(bean:hbaseSourceInstance,field:'quorumServers')}"/>
 		<p class="help-block">Comma-separated list of ZooKeper server addresses.</p>
	</div>
</div>

<div class="control-group <% if (hasErrors(bean:hbaseSourceInstance,field:'masterUrl','errors')){%>error<% }%>">
    <label class="control-label" for="quorumPort">HBase Master URL</label>
	<div class="controls">
		<input class="input-xlarge" type="text" id="masterUrl" name="masterUrl" value="${fieldValue(bean:hbaseSourceInstance,field:'masterUrl')}"/>
 		<p class="help-block">The URL to HBase Master server UI (for reference only).</p>
	</div>
</div>

<div class="control-group <% if (hasErrors(bean:hbaseSourceInstance,field:'jobTracker','errors')){%>error<% }%>">
    <label class="control-label" for="jobTracker">JobTracker URL</label>
	<div class="controls">
		<input class="input-xlarge span3" type="text" id="jobTracker" name="jobTracker" value="${fieldValue(bean:hbaseSourceInstance,field:'jobTracker')}"/>
 		<p class="help-block">URL to the JobTracker for MapReduce jobs submission.</p>
	</div>
</div>

<div class="control-group <% if (hasErrors(bean:hbaseSourceInstance,field:'filesystem','errors')){%>error<% }%>">
    <label class="control-label" for="filesystem">HDFS file system URI</label>
	<div class="controls">
		<input class="input-xlarge" type="text" id="filesystem" name="filesystem" value="${fieldValue(bean:hbaseSourceInstance,field:'filesystem')}"/>
 		<p class="help-block">A URI whose scheme and authority determine the FileSystem implementation.</p>
	</div>
</div>

<div class="control-group <% if (hasErrors(bean:hbaseSourceInstance,field:'hdfsUname','errors')){%>error<% }%>">
    <label class="control-label" for="hdfsUname">HDFS User name</label>
	<div class="controls">
		<input class="input-xlarge span3" type="text" id="hdfsUname" name="hdfsUname" value="${fieldValue(bean:hbaseSourceInstance,field:'hdfsUname')}"/>
 		<p class="help-block">Name of the user in filesystem.</p>
	</div>
</div>

<div class="control-group <% if (hasErrors(bean:hbaseSourceInstance,field:'hdfsPwd','errors')){%>error<% }%>">
    <label class="control-label" for="hdfsPwd">HDFS User password</label>
	<div class="controls">
		<input class="input-xlarge span3" type="password" id="hdfsPwd" name="hdfsPwd" value="${fieldValue(bean:hbaseSourceInstance,field:'hdfsPwd')}"/>
 		<p class="help-block">Password for specified user.</p>
	</div>
</div>

