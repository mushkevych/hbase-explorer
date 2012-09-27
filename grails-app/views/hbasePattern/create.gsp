<html>
    <head>
        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
        <meta name="layout" content="main" />
        <g:set var="entityName" value="${message(code: 'hbasePattern.label', default: 'HbasePattern')}" />
        <title><g:message code="default.create.label" args="[entityName]" /></title>
    </head>
    <body>
        <div class="container">
        	<g:if test="${flash.message}">
				<div class="alert alert-info">
					${flash.message}
				</div>
			</g:if>
			<g:hasErrors bean="${hbaseSourceInstance}">
				<div class="alert alert-error">
					<g:renderErrors bean="${hbaseSourceInstance}" as="list" />
				</div>
			</g:hasErrors>
			<g:form action="save" class="form-horizontal" method="post" >
				<fieldset>
					<legend>Create New Pattern</legend>
					<br/>
					<g:render template="edit" />
					<div class="span12">
						<div class="form-actions">
							<button type="submit" class="btn btn-primary">Create</button>
						</div>
					</div>
				</fieldset>
			</g:form>
        </div>
    </body>
</html>
