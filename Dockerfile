FROM glassfish:4.1

# Copy the service .war that will be autodeployed on start
COPY ./RESTServer/dist/RESTServer.war /usr/local/glassfish4/glassfish/domains/domain1/autodeploy/
