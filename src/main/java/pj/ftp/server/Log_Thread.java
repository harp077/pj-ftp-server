package pj.ftp.server;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.SwingUtilities;
import static pj.ftp.server.PjFtpServer.running;
import static pj.ftp.server.PjFtpServer.taLog;

public class Log_Thread extends Thread {

    public static String logFile;

    public Log_Thread(String adres) {
        start();
        this.logFile = adres;
    }

    @Override
    public void run() {
        //java.awt.EventQueue.invokeLater(new Runnable() {
        //SwingUtilities.invokeLater(new Runnable() {
            //public void run() {
            synchronized (this) {
                while (running == true) {
                    try {
                        String textLine;
                        FileReader fr = new FileReader(logFile);
                        BufferedReader reader = new BufferedReader(fr);
                        while ((textLine = reader.readLine()) != null) {
                            //textLine = reader.readLine();
                            taLog.read(reader, "taLogArea");
                        }
                    } catch (IOException ioe) {
                        System.err.println(ioe);
                    }
                    try {
                        Thread.sleep(2000);
                    } catch (InterruptedException ex) {
                        Logger.getLogger(Log_Thread.class.getName()).log(Level.SEVERE, null, ex);
                    }
                }
            }
    }
        //});
    //}

}
