<html>
<head>
	<meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
	<meta http-equiv="X-UA-Compatible" content="IE=edge,chrome=1">
	<title>
		<g:layoutTitle default="HBase Explorer" />
	</title>
	<meta name="viewport" content="width=device-width, initial-scale=1.0">
	<link rel="shortcut icon" href="${resource(dir: 'images', file: 'favicon.ico')}" type="image/x-icon">
	<link rel="apple-touch-icon" href="${resource(dir: 'images', file: 'apple-touch-icon.png')}">
	<link rel="apple-touch-icon" sizes="114x114" href="${resource(dir: 'images', file: 'apple-touch-icon-retina.png')}">
	<link rel="stylesheet" href="${resource(dir: 'css', file: 'mobile.css')}" type="text/css">
	
	<r:require modules="jquery, bootstrap" />
	<style>
      body {
        padding-top: 60px; /* 60px to make the container go all the way to the bottom of the topbar */
      }
    </style>

<%-- periodically poll the status to display running jobs --%>
	<r:script disposition="head">	
   		function loadAgain() {
   		<sec:ifLoggedIn>
		<g:remoteFunction action="threads" controller="hbaseSource"
			update="threads" before="jQuery('#spinner').show();"
			after="jQuery('#spinner').hide();" />
                window.setTimeout(loadAgain, 5000);
        </sec:ifLoggedIn>
            };
	</r:script>
	<g:layoutHead />
	<r:layoutResources />
	<link rel="stylesheet" href="${resource(dir: 'css', file: 'main.css')}" type="text/css">
	
	<!-- WORKAROUND due to js libraries not linked by resources framework, remove after fixed -->
	<g:javascript plugin="twitter-bootstrap" src="bootstrap-alert.js"/>
	<g:javascript plugin="twitter-bootstrap" src="bootstrap-dropdown.js"/>
	<g:javascript plugin="twitter-bootstrap" src="bootstrap-button.js"/>
	<g:javascript plugin="twitter-bootstrap" src="bootstrap-tooltip.js"/>
	<g:javascript plugin="twitter-bootstrap" src="bootstrap-popover.js"/>
	<g:javascript plugin="twitter-bootstrap" src="bootstrap-modal.js"/>
	<g:javascript plugin="twitter-bootstrap" src="bootstrap-collapse.js"/>
	<g:javascript plugin="twitter-bootstrap" src="bootstrap-typeahead.js"/>
</head>

<body onload="${pageProperty(name:'body.onload')}; loadAgain();">
<!-- TOP NAVIGATION -->
	<div class="navbar navbar-fixed-top">
      <div class="navbar-inner">
        <div class="container">
          <a class="btn btn-navbar" data-toggle="collapse" data-target=".nav-collapse">
            <span class="icon-bar"></span>
            <span class="icon-bar"></span>
            <span class="icon-bar"></span>
          </a>
<!-- LOGO -->
          <g:link class="brand" controller="hbaseSource" action="List">
				<img width="170" src="${resource(dir: 'images', file: 'hbe_logo.png')}" alt="HBaseExplorer"/>
		  </g:link>
          <div class="nav-collapse">
<!-- TOP MENU RIGHT -->
          <ul class="nav">
			<li class="dropdown">
				<a href="#" class="dropdown-toggle" data-toggle="dropdown" >
				HBase Sources <b class="caret"></b>
				</a>
				<ul class="dropdown-menu">
					<li><g:link controller="hbaseSource" action="List">List instances</g:link></li>
					<li><g:link controller="hbaseSource" action="create">Add new instance</g:link></li>
				</ul>	
			</li>
			<li class="dropdown">
				<a href="#" class="dropdown-toggle" data-toggle="dropdown">
				HBase Patterns <b class="caret"></b>
				</a>
				<ul class="dropdown-menu">
					<li><g:link controller="hbasePattern" action="List">List patterns</g:link></li>
					<li><g:link controller="hbasePattern" action="create">Create pattern</g:link></li>
				</ul>	
			</li>
			<g:pageProperty default="" name="page.customnavi"/>
          </ul>
<!-- TOP MENU LEFT -->
          <ul class="nav pull-right">
          <sec:ifNotLoggedIn>
			<li><g:link controller="login">Please <strong>log in</strong></g:link></li>
		  </sec:ifNotLoggedIn>
		  <sec:ifLoggedIn>
          	<li class="dropdown">
              <a data-toggle="dropdown" class="dropdown-toggle" href="#">Hello <strong><sec:username />!</strong> <b class="caret"></b></a>
              <ul class="dropdown-menu">
                <li><g:link controller="logout">Logout</g:link></li>
                <sec:ifAnyGranted roles="ROLE_ROOT">
                 	<li class="divider"></li>
					<li><g:link controller="user">Manage Users</g:link></li>
					<li><g:link controller="user" action="create">Add User</g:link></li>
				</sec:ifAnyGranted>
              </ul>
            </li>
            </sec:ifLoggedIn>	
          </ul>
          </div><!--/.nav-collapse -->
        </div>
      </div>
	</div>
	
<!-- RUNNING TASKS DISPLAY -->
	<div id="threads" class="threads">
			<%-- implements a small running-jobs display --%>
	</div>
	
<!-- BODY  -->
	<g:layoutBody />
	<g:render template="/footer"/>
</body>
</html>