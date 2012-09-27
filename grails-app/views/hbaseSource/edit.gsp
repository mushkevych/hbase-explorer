<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
<meta name="layout" content="main" />
<title>Edit HBaseSource</title>
</head>
<body>
	<g:render template="/navi"/>
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
		<g:form method="post" class="form-horizontal" enctype="multipart/form-data">
			<input type="hidden" name="id" value="${hbaseSourceInstance?.id}" />
			<input type="hidden" name="version"
				value="${hbaseSourceInstance?.version}" />
			<fieldset>
				<legend>Edit HBase Connection</legend>
				<g:render template="edit"/>
				<div class="form-actions">
					<g:actionSubmit class="btn btn-primary" value="Update" />
					<g:actionSubmit class="btn btn-danger"	onclick="return confirm('Are you sure?');" value="Delete" />
				</div>
			</fieldset>
		</g:form>
	</div>
</body>
</html>
