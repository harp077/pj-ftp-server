
package pj.ftp.server;

import java.awt.Dimension;
import java.io.IOException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.Properties;
import org.apache.commons.validator.routines.InetAddressValidator;

public class ICFG {
    
    public static int FW = 970;
    public static int FH = 400;
    public static InetAddressValidator ipv = InetAddressValidator.getInstance();
    public static String aclNetDefaultAddress = "10.0.0.0";
    public static String aclNetDefaultPrefix = "/8";
    public static String aclTypeDefault = "allow";
    public static String DEFAULT_PORT = "21";
    public static Dimension tfAclNetSize = new Dimension(100,28);
    public static SimpleDateFormat stf  = new SimpleDateFormat("HH:mm:ss");
    public static SimpleDateFormat sdf  = new SimpleDateFormat("dd.MM.yy");
    public static SimpleDateFormat sdtf = new SimpleDateFormat("dd.MM.yy HH:mm:ss");       
    public static String currentLAF = "de.muntjak.tinylookandfeel.TinyLookAndFeel";
    //public static String zagolovok = " Pure Java FTP Server, v1.0.88, build 18-03-2022";    
    public static String zagolovok = " Pure Java FTP Server";    
    
	static {
		try ( InputStream input = ICFG.class.getClassLoader().getResourceAsStream("version.properties")) {
			if (input != null) {
				Properties prop = new Properties();
				prop.load(input);
				zagolovok = zagolovok + ", v"+prop.getProperty("version")+", build "+prop.getProperty("date");
			}
		}
		catch (IOException ex) {
			ex.printStackTrace();
		}
	}
	
}
