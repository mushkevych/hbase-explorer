<ul class="file-list">
<g:each in="${hbaseSourceInstance?.configFiles}" var="configFile">
	<li>
       <span>${configFile.name}</span><span class="uploaded-timestamp">${new Date(configFile.uploadTimestamp).toGMTString()}</span>
       <input type="button" value="Remove" onclick="${remoteFunction(action:'removeConfigFile',update:'config-file-list',params:['configFileId':configFile?.id, 'id':hbaseSourceInstance?.id])}"/>
    </li>
</g:each>
</ul>
