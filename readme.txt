Pure Java FTP server. Free portable cross-platform. Run as root/admin !
1) Can run in GUI-mode - without cmd arguments: 
        java -jar pj-ftp-server.jar .
2) Can run without GUI in cmd-mode with cmd arguments: 
        java -jar pj-ftp-server.jar port=21 folder=/tmp listenip=127.0.0.1 user=root passw=root .
3) For anonymous-mode in cmd-mode without GUI passw-parameter not need: 
        java -jar pj-ftp-server.jar port=21 folder=/tmp listenip=127.0.0.1 user=anonymous .
Need JRE-1.8:
https://www.oracle.com/java/technologies/javase/javase8-archive-downloads.html
