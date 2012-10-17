DESCRIPTION
========================
HBaseExplorer is a tool allows to manipulate and explore HBase instances. See  http://hbase.apache.org/
to learn more about HBase and its Data Storage Model. The Goal is to have a Administrators Utility for
maintaining the potentially huge data sets in various HBase instances. While there is a HBase Shell UI
(that can also be used to script administrative tasks), this tool shall allow to get a bit more visual
insight into the data.

RELEASE NOTES
========================

VERSION 0.6.0 Surus ORM Build
+ Support for multiple ORM 
+ Build-in support for Surus ORM
+ Support for complex (multi-component) rowKeys

VERSION 0.6.0 
+ UI redesign

VERSION 0.5.1
- Temporary disabled upload config file option
+ Significant improvement of SCAN performance (Issue 3489917)
+ Invalidate table descriptors if tables are added / removed (Issue 3489920) 

VERSION 0.5.0
+ Grails upgrade to version 2.0 ( temporarly disabled maven support )
+ Security model

VERSION 0.4.3
+ Possibility to specify HDFS credentials 

VERSION 0.4.2
+ Code refactoring
+ Advanced configuration option by uploading hadoop .xml config files

VERSION 0.4.1
+ HBase libraries switched to : hbase 0.90.1, zookeeper 3.3.3, hadoop 0.20.1 (Issue 3072491)
+ Re-use of HBase connections 
+ Handling connection exceptions (Issue 3303376)
+ Connection recovery fixed (Issue 3303377)

SECURITY MODEL
===========================================
For first log in use username: admin, password: admin
There are three roles defined, for now two of them are used
 * ROLE_ROOT - Full control of the application including credentials management.
 * ROLE_USER - Can use all HBase related functions but not to credentials management.

You can easily disable access control by placing file hbaseexplorer-config.groovy
in 'shared' folder of tomcat directory, containing entry: hbaseexplorer.security.active=false

IMPLEMENTED FEATURES
=========================
* Data Source Management
 # Create, Edit, Modify, Drop a Hbase Cluster definition
 # Table Detail Display
* Table Management
 # Create Tables, Drop Tables
 # Clone a Table within the same Hbase instance
 # Table Statistics
* Querying Data
 # HBase Scan
* Data Display
 # Top-Version Display
 # Timestamp-oriented display
 # automatic Linking between views based on configured patterns. Shall allow a simple navigation in the data.
 
HOW-TO
========================
Its implemented as a web application based on the Grails (http://www.grails.org) framework.
You may deploy a J2EE WAR package to your J2EE containers, or download a preconfigured Tomcat package 
(STANDALONE) that just needs to be started. Follow INSTALL.txt for details. 
All data is stored using a embedded database instance.

Al Lias, E-M-P
