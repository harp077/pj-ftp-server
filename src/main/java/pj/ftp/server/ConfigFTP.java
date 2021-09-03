
package pj.ftp.server;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.commons.io.FileUtils;
import static pj.ftp.server.PjFtpServer.frame;

public class ConfigFTP {
    
    public static String listenIP="127.0.0.1";
    public static String username;
    public static String password;
    public static String folder=FileUtils.getTempDirectoryPath();//System.getProperty("java.io.tmpdir");
    public static String port=ICFG.DEFAULT_PORT;
    public static int MAX_CONCURRENT_LOGINS = 10;
    public static int MAX_CONCURRENT_LOGINS_PER_IP = 3;
    public static int MAX_IDLE_TIME = 900;
    public static int MAX_THREADS_LOGINS = 100;
    public static int MAX_SPEED = 125_000_000;// = Integer.MAX_VALUE;99_999;//Integer.MAX_VALUE; = in Kbit/sek !!
    public static Boolean writable = true;
    public static String aclNetAddress = ICFG.aclNetDefaultAddress;
    public static String aclNetPrefix = ICFG.aclNetDefaultPrefix;
    public static Boolean ipFilterEnabled = false; 
    public static String aclType = ICFG.aclTypeDefault;
    //
    public static Properties prop;
    
    public static void loadCFGfromFile() {
        try (InputStream input = new FileInputStream("cmd-mode-config.properties")) {
            Properties prop = new Properties();
            // load a properties file
            prop.load(input);
            System.out.println(prop);
            //
            listenIP=prop.getProperty("listen.ip", "127.0.0.1").trim();
            username=prop.getProperty("username", "anonymous").trim();
            password=prop.getProperty("password", "jer@sey.com").trim();
            port=prop.getProperty("port", ICFG.DEFAULT_PORT).trim();
            folder=prop.getProperty("folder", FileUtils.getTempDirectoryPath()).trim();
            writable=Boolean.parseBoolean(prop.getProperty("writable", "true").trim());
            MAX_CONCURRENT_LOGINS=Integer.parseInt(prop.getProperty("max.concurrent.logins", "10").trim());
            MAX_CONCURRENT_LOGINS_PER_IP=Integer.parseInt(prop.getProperty("max.concurrent.logins.per.ip", "3").trim());
            MAX_THREADS_LOGINS=MAX_CONCURRENT_LOGINS;
            ipFilterEnabled=Boolean.parseBoolean(prop.getProperty("ip.filter.enabled", "true").trim());
            aclNetAddress=prop.getProperty("ip.filter.network.address", "10.0.0.0").trim();
            aclNetPrefix=prop.getProperty("ip.filter.network.prefix", "/8").trim();
            aclType=prop.getProperty("ip.filter.type", ICFG.aclTypeDefault).trim();
            
        } catch (IOException ex) {
            Logger.getLogger(ConfigFTP.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    public static void loadCFGfromGUI() {
        listenIP=frame.comboListenIP.getSelectedItem().toString().trim();
        username=frame.tfUser.getText().trim();
        password=frame.tfPassw.getText().trim();
        port=frame.tfPort.getText().trim();
        folder=frame.tfFolder.getText().trim();
        writable=Boolean.parseBoolean(frame.comboWritable.getSelectedItem().toString().trim());
        ipFilterEnabled=frame.checkBoxIpFilter.isSelected();
        aclNetAddress=frame.tfAclNetAdres.getText().trim();
        aclNetPrefix=frame.comboPrefixMask.getSelectedItem().toString().split("=")[0].trim();
        MAX_CONCURRENT_LOGINS=Integer.parseInt(frame.comboMaxLogins.getSelectedItem().toString().trim());
        MAX_CONCURRENT_LOGINS_PER_IP=Integer.parseInt(frame.comboMaxLoginsPerIP.getSelectedItem().toString().trim());
        MAX_THREADS_LOGINS=MAX_CONCURRENT_LOGINS;
        aclType=frame.comboTypeACL.getSelectedItem().toString().trim();
    }    
    
    public static void saveCFGfromGUI() {  
        try(OutputStream output = new FileOutputStream("cmd-mode-config.properties")) {
            Properties prop = new Properties();
            prop.setProperty("listen.ip", frame.comboListenIP.getSelectedItem().toString().trim());
            prop.setProperty("username", frame.tfUser.getText().trim());
            prop.setProperty("password", frame.tfPassw.getText().trim());
            prop.setProperty("port", frame.tfPort.getText().trim());
            prop.setProperty("folder", frame.tfFolder.getText().trim());
            prop.setProperty("writable", frame.comboWritable.getSelectedItem().toString().trim());
            prop.setProperty("ip.filter.enabled", ""+frame.checkBoxIpFilter.isSelected());
            prop.setProperty("ip.filter.type", frame.comboTypeACL.getSelectedItem().toString().trim());
            prop.setProperty("ip.filter.network.address", frame.tfAclNetAdres.getText().trim());
            prop.setProperty("ip.filter.network.prefix", frame.comboPrefixMask.getSelectedItem().toString().split("=")[0].trim());
            prop.setProperty("max.concurrent.logins", frame.comboMaxLogins.getSelectedItem().toString().trim());
            prop.setProperty("max.concurrent.logins.per.ip",frame.comboMaxLoginsPerIP.getSelectedItem().toString().trim()); 
            
            prop.store(output, " terminal cmd-mode-config for run in CMD-mode:");
            
        } catch (IOException ex) {
            Logger.getLogger(ConfigFTP.class.getName()).log(Level.SEVERE, null, ex);
        }
        
    }  
    
}
