Pure Java FTP server. 
Can run in GUI-mode - without cmd arguments: 
    java -jar pj-ftp-server.jar .
Can run without GUI in cmd-mode with cmd arguments: 
    java -jar pj-ftp-server.jar port=21 folder=/tmp user=root passw=root .
For anonymous-mode in cmd-mode without GUI passw-parameter not need: 
    java -jar pj-ftp-server.jar port=21 folder=/tmp user=anonymous .
 Need JRE-1.8. 