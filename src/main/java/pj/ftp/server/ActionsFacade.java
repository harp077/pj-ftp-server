
package pj.ftp.server;

import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import static pj.ftp.server.PjFtpServer.frame;

public class ActionsFacade {
    
    public static List<String> lookAndFeelsDisplay = new ArrayList<>();
    public static List<String> lookAndFeelsRealNames = new ArrayList<>(); 
    public static Map<String, Integer> speedMap = new HashMap<String, Integer>();
    public static String[] loginsArray = {"1","10","100"};
    public static String[] loginsArrayPerIP = {"1","2","3","4","5","6","7","8"};
    public static String[] writableArray = {"true","false"};
    
    static {
        speedMap.put("1 Mbit/s", 1_000_000);
        //speedMap.put("5 Mbit/s", 5_000_000);
        speedMap.put("10 Mbit/s", 10_000_000);
        //speedMap.put("50 Mbit/s", 50_000_000);
        speedMap.put("100 Mbit/s", 100_000_000);
        speedMap.put("1000 Mbit/s", 1000_000_000);
    }
   
    public static boolean checkTcpPort(String tcpPort) {
        if (NumberUtils.isParsable(tcpPort)) {
            long port=Long.parseLong(tcpPort);
            if (port > 0 && port < 65536)
                return true;
        }
        return false;
    }
    
    public static void useExamples() {
        System.out.println("Examples of use:");
        System.out.println("java -jar pj-ftp-server.jar port=21 folder=/tmp listenip=127.0.0.1 user=root passw=root");
        System.out.println("java -jar pj-ftp-server.jar port=21 folder=/tmp listenip=127.0.0.1 user=anonymous"); 
        System.out.println("Anonymous mode not need passw parameter.");
    }

    public static void MyInstLF(String lf) {
        //UIManager.installLookAndFeel(lf,lf);  
        lookAndFeelsDisplay.add(lf);
        lookAndFeelsRealNames.add(lf);
    }

    public static void InstallLF() {
        MyInstLF("javax.swing.plaf.metal.MetalLookAndFeel");
        MyInstLF("de.muntjak.tinylookandfeel.TinyLookAndFeel");
    }  
    
    public static void setLF(JFrame frame) {
        try {
            UIManager.setLookAndFeel(ICFG.currentLAF);
        } catch (InstantiationException | IllegalAccessException | UnsupportedLookAndFeelException | ClassNotFoundException ex) {
            Logger.getLogger(ActionsFacade.class.getName()).log(Level.ERROR, null, ex);
        }
        SwingUtilities.updateComponentTreeUI(frame);
        //frame.pack();
    }    
    
    public static List<String> listLocalIpAddr () {
        List<String> listListenIP = new ArrayList<>();
        Enumeration<NetworkInterface> enumerationNI = null;
        try {
            enumerationNI = NetworkInterface.getNetworkInterfaces();
            int j=1;
            while (enumerationNI.hasMoreElements()) {
                NetworkInterface ni = enumerationNI.nextElement();
                Enumeration<InetAddress> niInetAddr = ni.getInetAddresses();
                while (niInetAddr.hasMoreElements()) {
                    InetAddress ia = niInetAddr.nextElement();
                    if (ia.getHostAddress().contains("%")) {
                        listListenIP.add(StringUtils.substringBefore(ia.getHostAddress(), "%"));
                        continue;
                    }
                    listListenIP.add(ia.getHostAddress());
                }
                j++;
            }
        } catch (SocketException | NullPointerException ex) {
            Logger.getLogger(ActionsFacade.class.getName()).log(Level.ERROR, null, ex);
        }  
        return listListenIP;
    }    
    
    public static void about(ImageIcon iii) {                                         
        String msg = " PJ-FTP-SERVER: Pure Java FTP server. Free portable cross-platform."
                + "\n Run as root (Linux) or admin (Windows) !"
                + "\n 1) Fully multi-threaded."
                + "\n 2) Multi platform support."
                + "\n 3) High upload/download speed - up to 800 Mbit/s (100 Mbyte/s) on 1Gbit/s channel when used SSD-drives on both upload/download sides. "               
                + "\n 4) Can run in GUI-mode - without CMD arguments:" 
                + "\n       # java -jar pj-ftp-server.jar"
                + "\n 5) Can run without GUI in CMD-mode with CMD arguments: "
                + "\n       # java -jar pj-ftp-server.jar port=21 folder=/tmp listenip=127.0.0.1 user=root passw=root "
                + "\n 6) For anonymous-mode in CMD-mode without GUI passw-parameter not need: "
                + "\n       # java -jar pj-ftp-server.jar port=21 folder=/tmp listenip=127.0.0.1 user=anonymous "
                + "\n 7) Need JRE-1.8."                
                + "\n Create by Roman Koldaev, "
                + "\n Saratov city, Russia. "
                + "\n mail: harp07@mail.ru "
                + "\n SourceForge: https://sf.net/u/harp07/profile/ "
                + "\n GitHub: https://github.com/harp077/ "; 
        JOptionPane.showMessageDialog(frame, msg, "About " + ICFG.zagolovok, JOptionPane.INFORMATION_MESSAGE, iii);
    }     
    
}
