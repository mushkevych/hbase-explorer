<h2>Please Log-in</h2>
<br/>
<g:if test="${flash.message}">
	<div class="alert alert-info">
		${flash.message}
	</div>
</g:if>
<form action='${postUrl}' method='POST' id='loginForm' autocomplete='off' class="form-horizontal" >
<fieldset>
	<div class="control-group">
    	<label class="control-label" for="username">User Name</label>
		<div class="controls">
			<input class="input-xlarge" class='text_'  type="text" id="username" name="j_username"/>
		</div>
	</div>
	<div class="control-group">
    	<label class="control-label" class='text_' for="password">Password</label>
		<div class="controls">
			<input class="input-xlarge" type="password" id="password" name="j_password"/>
		</div>
	</div>
	<div class="control-group">
    	<label class="control-label" for="remember_me">Remember me</label>
		<div class="controls">
			<input type='checkbox' class='chk' name='${rememberMeParameter}' id='remember_me' <g:if test='${hasCookie}'>checked='checked'</g:if>/>
		</div>
	</div>				
	<div class="form-actions">
		<input type="submit" id="submit" class="btn btn-primary" value="Log In" />
	</div>
</fieldset>
</form>

<script type='text/javascript'>
	<!--
	(function() {
		document.forms['loginForm'].elements['j_username'].focus();
	})();
	// -->
</script>