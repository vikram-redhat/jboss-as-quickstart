jboss-as-tasks
==============

What is it?
-----------

This is your project! It's a sample, Maven 3 project to help you
get your foot in the door developing with Java EE 6 on JBoss AS 7 or JBoss EAP 6. 
This project is setup to allow you to use JPA 2.0. 
It includes a persistence unit and some sample persistence code to help 
you get your feet wet with database access in enterprise Java. 

It does not contain an user interface layer. The main purpose of the project is 
to show you how to test JPA with Arquillian.

System requirements
-------------------

All you need to build this project is Java 6.0 (Java SDK 1.6) or better, Maven
3.0 or better.

You will use a real server to test internals of your application with Arquillian.

Running the Arquillian tests
============================

By default, tests are configured to be skipped. The reason is that the sample
test is an Arquillian test, which requires the use of a container. You can
activate this test by selecting one of the container configuration provided 
for JBoss AS 7 / JBoss EAP 6 (remote).

Testing on Remote Server
-------------------------
 
First you need to start JBoss AS 7 or JBoss EAP6. To do this, run
  
    $JBOSS_HOME/bin/standalone.sh
  
or if you are using windows
 
    $JBOSS_HOME/bin/standalone.bat

To run the test in JBoss AS 7, first start a JBoss AS 7 or JBoss EAP 6 instance. Then, run the
test goal with the following profile activated:

    mvn clean test -Parq-jbossas-remote

Testing on Managed Server
-------------------------
 
Arquillian will start the container for you. All you have to do is setup a path to your
extracted . To do this, run
  
    export JBOSS_HOME=/path/to/jboss-as
  
or if you are using windows
 
    set JBOSS_HOME=X:\path\to\jboss-as

Or hardcode the path in pom.xml file

To run the test in JBoss AS 7 or JBoss EAP 6, run the test goal with the following profile activated:

    mvn clean test -Parq-jbossas-managed

Running tests from JBDS
-----------------------

To be able to run the tests from JBDS, first set the active Maven profile in
project properties to be either 'arq-jbossas-managed' for running on
managed server or 'arq-jbossas-remote' for running on remote server.

To run the tests, right click on the project or individual classes and select
Run As > JUnit Test in the context menu.

Reading output of the tests in a console
----------------------------------------

### Maven

Maven prints summary of performed tests into the console:

    -------------------------------------------------------
     T E S T S
    -------------------------------------------------------
    Running org.jboss.as.quickstarts.tasks.TaskDaoTest
    log4j:WARN No appenders could be found for logger (org.jboss.logging).
    log4j:WARN Please initialize the log4j system properly.
    Tests run: 5, Failures: 0, Errors: 0, Skipped: 0, Time elapsed: 2.31 sec
    Running org.jboss.as.quickstarts.tasks.UserDaoTest
    Tests run: 3, Failures: 0, Errors: 0, Skipped: 0, Time elapsed: 1.084 sec

    Results :

    Tests run: 8, Failures: 0, Errors: 0, Skipped: 0

### Server log

SQL statements generated by Hibernate are written into the server log.

#### Examples

Creating the database schema:

    10:16:58,770 INFO  [stdout] (MSC service thread 1-2) Hibernate: create table Tasks_task (id bigint not null, title varchar(255), owner_id bigint, primary key (id))
    10:16:58,771 INFO  [stdout] (MSC service thread 1-2) Hibernate: create table Tasks_user (id bigint not null, username varchar(255), primary key (id))
    10:16:58,772 INFO  [stdout] (MSC service thread 1-2) Hibernate: alter table Tasks_task add constraint FKE61757B62CC79EF1 foreign key (owner_id) references Tasks_user

Generating ID for a new entity and inserting the entity into the database:

    10:16:58,956 INFO  [stdout] (http--127.0.0.1-8080-1) Hibernate: select tbl.next_val from hibernate_sequences tbl where tbl.sequence_name=? for update
    10:16:58,957 INFO  [stdout] (http--127.0.0.1-8080-1) Hibernate: insert into hibernate_sequences (sequence_name, next_val)  values (?,?)
    10:16:58,958 INFO  [stdout] (http--127.0.0.1-8080-1) Hibernate: update hibernate_sequences set next_val=?  where next_val=? and sequence_name=?
    10:16:58,960 INFO  [stdout] (http--127.0.0.1-8080-1) Hibernate: insert into Tasks_user (username, id) values (?, ?)


Importing the project into an IDE
=================================

If you created the project using the Maven archetype wizard in your IDE
(Eclipse, NetBeans or IntelliJ IDEA), then there is nothing to do. You should
already have an IDE project.

If you created the project from the commandline using archetype:generate, then
you need to import the project into your IDE. If you are using NetBeans 6.8 or
IntelliJ IDEA 9, then all you have to do is open the project as an existing
project. Both of these IDEs recognize Maven projects natively.
 
Detailed instructions for using Eclipse with JBoss AS 7 are provided in the 
JBoss AS 7 Getting Started Guide for Developers.

Downloading the sources and Javadocs
====================================

If you want to be able to debug into the source code or look at the Javadocs
of any library in the project, you can run either of the following two
commands to pull them into your local repository. The IDE should then detect
them.

    mvn dependency:sources
    mvn dependency:resolve -Dclassifier=javadoc