
package pj.ftp.server;

import java.awt.Dimension;
import java.text.SimpleDateFormat;
import org.apache.commons.validator.routines.InetAddressValidator;

public interface ICFG {
    
    public static int FW = 940;
    public static int FH = 400;
    public static InetAddressValidator ipv = InetAddressValidator.getInstance();
    public static String allowNetDefaultAddress = "10.0.0.0";
    public static String allowNetDefaultPrefixMask = "/8=255.0.0.0";
    public static Dimension tfAllowNetSize = new Dimension(100,24);
    SimpleDateFormat stf  = new SimpleDateFormat("HH:mm:ss");
    SimpleDateFormat sdf  = new SimpleDateFormat("dd.MM.yy");
    SimpleDateFormat sdtf = new SimpleDateFormat("dd.MM.yy HH:mm:ss");       
    public static String currentLAF = "de.muntjak.tinylookandfeel.TinyLookAndFeel";
    public static String zagolovok = " Pure Java FTP Server, v1.0.48, build 21-08-21";    
    
}
