
package pj.ftp.server;

import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import javax.swing.JFrame;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;

public class Actions {
    
    public static List<String> lookAndFeelsDisplay = new ArrayList<>();
    public static List<String> lookAndFeelsRealNames = new ArrayList<>();    
    
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
            Logger.getLogger(Actions.class.getName()).log(Level.ERROR, null, ex);
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
            Logger.getLogger(Actions.class.getName()).log(Level.ERROR, null, ex);
        }  
        return listListenIP;
    }    
    
}
