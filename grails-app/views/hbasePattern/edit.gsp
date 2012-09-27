<html>
    <head>
        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
        <meta name="layout" content="main" />
        <g:set var="entityName" value="${message(code: 'hbasePattern.label', default: 'HbasePattern')}" />
        <title><g:message code="default.edit.label" args="[entityName]" /></title>
    </head>
    <body>
        <g:render template="/navi"/>
        <div class="container">
        	<g:if test="${flash.message}">
            	<div class="alert alert-info">${flash.message}</div>
            </g:if>
           	<g:hasErrors bean="${hbasePatternInstance}">
				<div class="alert alert-error">
					<g:renderErrors bean="${hbasePatternInstance}" as="list" />
				</div>
			</g:hasErrors> 
        	<g:form method="post" class="form-horizontal" >
                <g:hiddenField name="id" value="${hbasePatternInstance?.id}" />
                <g:hiddenField name="version" value="${hbasePatternInstance?.version}" />
                <fieldset>
                	<legend>Edit HBase Pattern</legend>
                	<br/>
					<g:render template="edit" />
					<div class="span12">
						<div class="form-actions">
                    		<g:actionSubmit class="btn btn-primary" action="update" value="${message(code: 'default.button.update.label', default: 'Update')}" />
                    		<g:actionSubmit class="btn btn-danger" action="delete" value="${message(code: 'default.button.delete.label', default: 'Delete')}" onclick="return confirm('${message(code: 'default.button.delete.confirm.message', default: 'Are you sure?')}');" />
                    	</div>
                	</div>
                </fieldset>
        	</g:form>
        </div>
    </body>
</html>
