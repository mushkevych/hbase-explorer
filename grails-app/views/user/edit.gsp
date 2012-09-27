<%@ page import="com.nnapz.hbaseexplorer.domain.security.User" %>
<!doctype html>
<html>
	<head>
		<meta name="layout" content="main">
		<g:set var="entityName" value="${message(code: 'user.label', default: 'User')}" />
		<title>Add User</title>
	</head>
	<body>
		<div class="container">
			<div class="span12">
			<g:if test="${flash.message}">
				<div class="alert alert-info">
					${flash.message}
				</div>
			</g:if>
			<g:hasErrors bean="${userInstance}">
				<div class="alert alert-error">
					<ul>
						<g:eachError bean="${userInstance}" var="error">
							<li <g:if test="${error in org.springframework.validation.FieldError}">data-field-id="${error.field}"</g:if>><g:message error="${error}"/></li>
						</g:eachError>
					</ul>
					<g:renderErrors bean="${userInstance}" as="list" />
				</div>
			</g:hasErrors>
			<g:form method="post" class="form-horizontal" action="save" >
					<fieldset>
						<legend>Edit User</legend>
						<g:render template="form"/>
						<div class="form-actions">
							<g:actionSubmit action="update" class="btn btn-primary" value="Update" />
							<g:actionSubmit action="delete" class="btn btn-danger" value="Delete"/>
						</div>
					</fieldset>
			</g:form>
			</div>
		</div>
	</body>
</html>
