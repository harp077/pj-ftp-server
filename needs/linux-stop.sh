#!/bin/bash
##
##out=$(ps ax | grep webapp-runner | grep -v 'grep'| cut -d' ' -f1)
out=`ps ax | grep pj-ftp-server.jar | grep -v 'grep'| cut -d' ' -f1`
kill -15 $out
out=`ps ax | grep pj-ftp-server.jar | grep -v 'grep'| cut -d' ' -f2`
kill -15 $out
out=`ps ax | grep pj-ftp-server.jar | grep -v 'grep'| cut -d' ' -f3`
kill -15 $out
###killall webapp-runner-8.5.45.0.jar
date
