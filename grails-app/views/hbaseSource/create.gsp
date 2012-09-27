<html>
<head>
	<meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
	<meta name="layout" content="main" />
	<title>Create HbaseSource</title>
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
		<g:form action="save" class="form-horizontal" method="post" enctype="multipart/form-data">
			<fieldset>
				<legend>Create HBase Connection</legend>
				<g:render template="edit" />
				<div class="form-actions">
					<button type="submit" class="btn btn-primary">Create</button>
				</div>
			</fieldset>
		</g:form>
	</div>
</body>
</html>
