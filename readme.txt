Pure Java FTP server. Free portable cross-platform. Run as root/admin !

1) Fully multi-threaded.

2) Multi platform support.

3) Very high upload/download speed - up to 900 Mbps on 1Gbps channel when used SSD-drives on both upload/download sides.

4) Can run in GUI-mode - without CMD arguments: 
        # java -jar pj-ftp-server.jar .

5) Can run without GUI in CMD-mode with CMD arguments: 
        # java -jar pj-ftp-server.jar port=21 folder=/tmp listenip=127.0.0.1 user=root passw=root 

6) For anonymous-mode in CMD-mode without GUI passw-parameter not need: 
        # java -jar pj-ftp-server.jar port=21 folder=/tmp listenip=127.0.0.1 user=anonymous 

7) Need JRE-1.8:  
https://www.oracle.com/java/technologies/javase/javase8-archive-downloads.html
