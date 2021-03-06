#!/bin/bash

#sudo systemctl stop tomcat.service

sudo rm -rf /opt/tomcat/webapps/docs  /opt/tomcat/webapps/examples /opt/tomcat/webapps/host-manager  /opt/tomcat/webapps/manager /opt/tomcat/webapps/ROOT* /opt/tomcat/webapps/noteapp-1

#sudo chown tomcat:tomcat /opt/tomcat/webapps/ROOT.war

#sudo systemctl start tomcat.service

# cleanup log files
sudo rm -rf /opt/tomcat/logs/catalina*
sudo rm -rf /opt/tomcat/logs/*.log
sudo rm -rf /opt/tomcat/logs/*.txt

sudo systemctl restart tomcat.service

sudo /opt/aws/amazon-cloudwatch-agent/bin/amazon-cloudwatch-agent-ctl -a fetch-config -m ec2 -c file:/opt/tomcat/cloudwatch-config.json -s
