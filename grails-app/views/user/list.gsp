
<%@ page import="com.nnapz.hbaseexplorer.domain.security.User" %>
<!doctype html>
<html>
	<head>
		<meta name="layout" content="main">
		<g:set var="entityName" value="${message(code: 'user.label', default: 'User')}" />
		<title><g:message code="default.list.label" args="[entityName]" /></title>
	</head>
	<body>
	<div class="container">
    	<h2>user List</h2>
    	<g:if test="${flash.message}">
    		<div class="alert alert-info">${flash.message}</div>
    	</g:if>
    	<table class="table table-bordered table-striped">
    		<thead>
				<tr>
					<g:sortableColumn property="username" title="${message(code: 'user.username.label', default: 'Username')}" />
				</tr>
			</thead>
			<tbody>
				<g:each in="${userInstanceList}" status="i" var="userInstance">
					<tr>
						<td><g:link action="edit" id="${userInstance.id}">${fieldValue(bean: userInstance, field: "username")}</g:link></td>
					</tr>
				</g:each>
			</tbody>
    	</table>
    </div>
	</body>
</html>
