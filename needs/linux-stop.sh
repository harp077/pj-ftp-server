#!/bin/bash
##
out=`ps ax | grep pj-ftp-server.jar | grep -v 'grep'| cut -d' ' -f1`
kill -15 $out
out=`ps ax | grep pj-ftp-server.jar | grep -v 'grep'| cut -d' ' -f2`
kill -15 $out
out=`ps ax | grep pj-ftp-server.jar | grep -v 'grep'| cut -d' ' -f3`
kill -15 $out
date
