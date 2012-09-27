import grails.plugins.springsecurity.SecurityConfigType;

// locations to search for config files that get merged into the main config
// config files can either be Java properties files or ConfigSlurper scripts

grails.config.locations = [ "classpath:${appName}-config.properties",
                             "classpath:${appName}-config.groovy",
                             "file:${userHome}/.grails/${appName}-config.properties",
                             "file:${userHome}/.grails/${appName}-config.groovy"]

if(System.properties["${appName}.config.location"]) {
    grails.config.locations << "file:" + System.properties["${appName}.config.location"]
}

grails.doc.license="Licensed under the Apache License, Version 2.0 (the 'License')"
grails.doc.title="HBaseExplorer"
grails.doc.authors="Bob Schulze, Mateusz Pytel, Patrick Raeuber, Bohdan Mushkevych"

//Required for appropriate resources handling in documentation
grails.resources.adhoc.excludes = [	"/docs/*" ]
grails.resources.adhoc.patterns = [	"/docs/**" ]

grails.resources.CSSPreprocessorResourceMapper.excludes= ["/docs/**.css"]
grails.resources.CSSRewriterResourceMapper.excludes=["/docs/**.css"]

grails.mime.file.extensions = true // enables the parsing of file extensions from URLs into the request format
grails.mime.use.accept.header = false
grails.mime.types = [ html: [
		'text/html',
		'application/xhtml+xml'
	],
	xml: [
		'text/xml',
		'application/xml'
	],
	text: 'text/plain',
	js: 'text/javascript',
	rss: 'application/rss+xml',
	atom: 'application/atom+xml',
	css: 'text/css',
	csv: 'text/csv',
	all: '*/*',
	json: [
		'application/json',
		'text/json'
	],
	form: 'application/x-www-form-urlencoded',
	multipartForm: 'multipart/form-data'
]
// The default codec used to encode data with ${}
grails.views.default.codec="html" // none, html, base64
grails.views.gsp.encoding="UTF-8"
grails.converters.encoding="UTF-8"

// enabled native2ascii conversion of i18n properties files
grails.enable.native2ascii = true

// set per-environment serverURL stem for creating absolute links
environments {
}

// Log4J configuration
def catalinaBase = System.properties.getProperty('catalina.base')
if (!catalinaBase) catalinaBase = '.'   // just in case
def logDirectory = "${catalinaBase}/logs"

log4j = {
    appenders {
        console name: 'stdout', layout: pattern(conversionPattern: '%c{2} %m%n')
        rollingFile name: 'logfile', file: "${logDirectory}/hbaseexplorer.log".toString(), layout: pattern(conversionPattern: '[%5p] %d{HH:mm:ss} (%F:%M:%L)%n%m%n%n'), maxFileSize:'2MB'
        rollingFile name: 'stacktrace', file: "${logDirectory}/hbaseexplorer_stacktrace.log".toString(), layout: pattern(conversionPattern: '%c{2} %m%n'), maxFileSize:'2MB'
    }

	error stacktrace: "StackTrace"
	
	debug 'org.grails.plugin.resource'

    info    'com.nnapz.hbaseexplorer',
            'com.reinvent'

	
	warn    'org.codehaus.groovy.grails.web.servlet',  //  controllers
			'org.codehaus.groovy.grails.web.pages', //  GSP
			'org.codehaus.groovy.grails.web.sitemesh', //  layouts
			'org.codehaus.groovy.grails.web.mapping.filter', // URL mapping
			'org.codehaus.groovy.grails.web.mapping', // URL mapping
			'org.codehaus.groovy.grails.commons', // core / classloading
			'org.codehaus.groovy.grails.plugins', // plugins
			'org.codehaus.groovy.grails.orm.hibernate', // hibernate integration
			'org.springframework',
			'org.hibernate',
			'net.sf.ehcache.hibernate'

	warn    'org.mortbay.log'
			'org.apache.hadoop'
			'org.apache.hbase'
			'grails.app.domains'
			'grails.app.controllers'
			'grails.app.services'


}

grails.plugins.twitterbootstrap.defaultBundle = false
grails.plugins.twitterbootstrap.fixtaglib = true

grails.plugins.springsecurity.securityConfigType = SecurityConfigType.Requestmap

grails.plugins.springsecurity.controllerAnnotations.staticRules = [
	'/hbaseSource/threads*':['IS_AUTHENTICATED_ANONYMOUSLY'],
	'/hbaseSource/*'	 : 	['IS_AUTHENTICATED_REMEMBERED'],
	'/hbasePattern/*'	 :	['IS_AUTHENTICATED_REMEMBERED'],
	'/hbaseTableStats/*' :	['IS_AUTHENTICATED_REMEMBERED'],
	'/user/*'			 : 	['ROLE_ROOT'],
	'/'					 :  ['IS_AUTHENTICATED_REMEMBERED'],
	'/**'				 :  ['IS_AUTHENTICATED_ANONYMOUSLY']
	
]


// Added by the Spring Security Core plugin:
grails.plugins.springsecurity.userLookup.userDomainClassName = 'com.nnapz.hbaseexplorer.domain.security.User'
grails.plugins.springsecurity.userLookup.authorityJoinClassName = 'com.nnapz.hbaseexplorer.domain.security.UserRole'
grails.plugins.springsecurity.authority.className = 'com.nnapz.hbaseexplorer.domain.security.Role'
grails.plugins.springsecurity.requestMap.className = 'com.nnapz.hbaseexplorer.domain.security.Requestmap'
grails.plugins.springsecurity.roleHierarchy = "ROLE_ROOT > ROLE_ADMIN > ROLE_USER"
