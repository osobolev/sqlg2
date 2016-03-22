Example of SQLG interaction with J2EE environment.

This example requires servlet container providing "jdbc/TestDS" datasource.

To build web application execute
    gradle build
command, then deploy it (build/libs/sqlg2.war) to servlet container ant test. Usual URL for Tomcat:
    http://localhost:8080/sqlg2/index.jsp
