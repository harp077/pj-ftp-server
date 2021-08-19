
package pj.ftp.server;

import org.apache.commons.validator.routines.InetAddressValidator;

public interface ICFG {
    
    public static int FW = 900;
    public static int FH = 400;
    public static InetAddressValidator ipv = InetAddressValidator.getInstance();
    public static String currentLAF = "de.muntjak.tinylookandfeel.TinyLookAndFeel";
    public static String zagolovok = " Pure Java FTP Server, v1.0.32, build 19-08-21";    
    
}
