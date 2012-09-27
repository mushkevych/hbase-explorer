<%@page import="com.nnapz.hbaseexplorer.domain.security.Role"%>
<%@ page import="com.nnapz.hbaseexplorer.domain.security.User" %>

<div class="control-group <% if (hasErrors(bean:userInstance,field:'username','errors')){%>error<% }%>">
    <label class="control-label" for="username">Username</label>
	<div class="controls">
		<input class="input-xlarge" type="text" id="username" name="username" value="${fieldValue(bean:userInstance,field:'username')}"/>
	</div>
</div>

<div class="control-group <% if (hasErrors(bean:userInstance,field:'password','errors')){%>error<% }%>">
    <label class="control-label" for="password">Password</label>
	<div class="controls">
		<input class="input-xlarge" type="password" id="password" name="password" value="${fieldValue(bean:userInstance,field:'password')}"/>
	</div>
</div>

<div class="control-group <% if (hasErrors(bean:userInstance,field:'authority','errors')){%>error<% }%>">
    <label class="control-label" for="authority">Authority</label>
	<div class="controls">
		<g:if test="${userInstance != null}">
    			<g:select class="input-xlarge" name="authority" from="${Role.ROLES}" value="${userAuthority ?: 'USER_ROLE'}"/>
    	</g:if>
	</div>
</div>

<g:hiddenField name="accountExpired" value="${false}"/>
<g:hiddenField name="accountLocked" value="${false}"/>
<g:hiddenField name="passwordExpired" value="${false}"/>
<g:hiddenField name="enabled" value="${true}"/>


