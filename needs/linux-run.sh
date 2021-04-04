#!/bin/bash
#
#cd /opt/jennom/
java -jar pj-ftp-server.jar port=21 folder=/var/ftp/pub listenip=127.0.0.1 user=anonymous >> pj-ftp.log & date;

