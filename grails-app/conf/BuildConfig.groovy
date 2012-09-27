grails.project.class.dir = "target/classes"
grails.project.test.class.dir = "target/test-classes"
grails.project.test.reports.dir	= "target/test-reports"
//grails.project.war.file = "target/${appName}-${appVersion}.war"
grails.project.dependency.resolution = {
    // inherit Grails' default dependencies
    inherits( "global" ) {
        // uncomment to disable ehcache
        // excludes 'ehcache'
    }
	
    log "warn" // log level of Ivy resolver, either 'error', 'warn', 'info', 'debug' or 'verbose'
    
	repositories {
		inherits true // Whether to inherit repository definitions from plugins
	
        mavenLocal()
		grailsPlugins()
        grailsHome()
		grailsCentral()
		mavenCentral()
		mavenRepo 'http://repository.apache.org'
		grailsRepo("http://svn.codehaus.org/grails-plugins", "test")
		//for Cloudera Hadoop Distribution artifacts
		mavenRepo 'https://repository.cloudera.com/content/repositories/releases/'
		
        // uncomment the below to enable remote dependency resolution from public Maven repositories
        mavenRepo "http://repo2.maven.org/maven2"
        mavenRepo "http://snapshots.repository.codehaus.org"
        mavenRepo "http://repository.codehaus.org"
        mavenRepo "http://download.java.net/maven/2/"
        mavenRepo "http://repository.jboss.com/maven2/"

    }
	
    dependencies {
		compile "commons-cli:commons-cli:1.2"
		compile "com.reinvent.data:synergy-surus:01"
		compile "com.google.code.gson:gson:1.7.1"
		compile "com.google.guava:guava:11.0.2"
        compile("org.apache.hadoop:hadoop-mapreduce-client-common:2.0.0-cdh4.0.1") {
            excludes "jetty", "jetty-util", "slf4j-log4j12","hadoop-ant", "zookeeper-ant"
        }
        compile("org.apache.hadoop:hadoop-mapreduce-client-core:2.0.0-cdh4.0.1") {
            excludes "jetty", "jetty-util", "slf4j-log4j12","hadoop-ant", "zookeeper-ant"
        }
        compile("org.apache.hadoop:hadoop-common:2.0.0-cdh4.0.1") {
            excludes "jetty", "jetty-util", "slf4j-log4j12","hadoop-ant", "zookeeper-ant"
        }
		compile("org.apache.hbase:hbase:0.92.1-cdh4.0.1") {
			excludes "thrift","hadoop-ant", "zookeeper-ant", "slf4j-log4j12"
		}
    }
	
	plugins {
		runtime ":hibernate:$grailsVersion"
		runtime ":jquery:1.7.1"
		runtime ":resources:1.1.5"
		runtime ":spring-security-core:1.2.7.1"
		compile ":twitter-bootstrap:2.0.1.22"

		build ":tomcat:$grailsVersion"
	}
	
}

grails.war.resources = { stagingDir, args ->
	try {
		copy(todir: "${stagingDir}/docs"){
			fileset(dir:"${basedir}/target/docs",includes:"**")
		}
	} catch (Exception e){
		println "! IMPORTANT ! Please execute grails 'doc' target before 'war' "
		throw e;
	}
}