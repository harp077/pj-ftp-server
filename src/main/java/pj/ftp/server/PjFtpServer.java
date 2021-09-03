package pj.ftp.server;

import java.awt.Color;
import java.awt.event.ItemEvent;
import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import javax.swing.DefaultComboBoxModel;
import javax.swing.ImageIcon;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JRootPane;
import org.apache.commons.net.util.SubnetUtils;
import org.apache.ftpserver.ConnectionConfig;
import org.apache.ftpserver.ConnectionConfigFactory;
import org.apache.ftpserver.FtpServer;
import org.apache.ftpserver.FtpServerConfigurationException;
import org.apache.ftpserver.FtpServerFactory;
import org.apache.ftpserver.ftplet.Authority;
import org.apache.ftpserver.ftplet.FtpException;
import org.apache.ftpserver.ftplet.UserManager;
import org.apache.ftpserver.ipfilter.RemoteIpFilter;
import org.apache.ftpserver.ipfilter.SessionFilter;
import org.apache.ftpserver.listener.ListenerFactory;
import org.apache.ftpserver.usermanager.PropertiesUserManagerFactory;
import org.apache.ftpserver.usermanager.impl.BaseUser;
import org.apache.ftpserver.usermanager.impl.ConcurrentLoginPermission;
import org.apache.ftpserver.usermanager.impl.TransferRatePermission;
import org.apache.ftpserver.usermanager.impl.WritePermission;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;

public class PjFtpServer extends javax.swing.JFrame {

    public static Boolean running = false;
    public static FtpServer server;
    //public static MessageResource mrLog;
    //public static java.util.logging.Logger jul;
    public static org.apache.log4j.Logger j4log;
    public static PjFtpServer frame;
    //public static Map<String, String> argsHM = new HashMap<String, String>();
    //public static Thread Log_Thread;
    public static SessionFilter sessionFilter;

    //public static List<String> listListenIP = new ArrayList<>();

    /*static {
        try (FileInputStream ins = new FileInputStream("cfg/jul.properties")) {
            LogManager.getLogManager().readConfiguration(ins);
            jul = java.util.logging.Logger.getLogger(FTPTestServer.class.getName());
        } catch (Exception ignore) { ignore.printStackTrace(); }
    } */
    
    public PjFtpServer() {
        initComponents();
        ImageIcon icone = new ImageIcon(getClass().getResource("/img/top-frame-triangle-16.png"));
        this.setIconImage(icone.getImage());
        this.setTitle(ICFG.zagolovok);
        //
        this.comboSpeed.setModel(new DefaultComboBoxModel<>(ActionsFacade.speedMap.keySet().stream().sorted().toArray(String[]::new)));
        this.comboSpeed.setEditable(false);
        this.comboSpeed.setSelectedItem("125 Mbyte/s=1000 Mbit/s");
        ConfigFTP.MAX_SPEED=ActionsFacade.speedMap.get(comboSpeed.getSelectedItem().toString());
        System.out.println(maxSpeedString()); 
        //
        this.comboMaxLogins.setModel(new DefaultComboBoxModel<>(ActionsFacade.loginsArray));
        this.comboMaxLogins.setEditable(false);
        this.comboMaxLogins.setSelectedItem("100");
        ConfigFTP.MAX_THREADS_LOGINS=Integer.parseInt(comboMaxLogins.getSelectedItem().toString());        
        System.out.println("Max concurrent Logins = "+ConfigFTP.MAX_THREADS_LOGINS);
        //
        this.comboMaxLoginsPerIP.setModel(new DefaultComboBoxModel<>(ActionsFacade.loginsArrayPerIP));
        this.comboMaxLoginsPerIP.setEditable(false);
        this.comboMaxLoginsPerIP.setSelectedItem("3");
        ConfigFTP.MAX_CONCURRENT_LOGINS_PER_IP=Integer.parseInt(comboMaxLoginsPerIP.getSelectedItem().toString());        
        System.out.println("Max concurrent Logins Per IP = "+ConfigFTP.MAX_CONCURRENT_LOGINS_PER_IP);
        // 
        this.comboWritable.setModel(new DefaultComboBoxModel<>(ActionsFacade.writableArray));
        this.comboWritable.setEditable(false);
        ConfigFTP.writable=Boolean.parseBoolean(comboWritable.getSelectedItem().toString());
        System.out.println("Writable = "+ConfigFTP.writable); 
        // 
        this.comboPrefixMask.setModel(new DefaultComboBoxModel<>(ActionsFacade.aclNetPrefixMaskArray));
        this.comboPrefixMask.setEditable(false);
        ConfigFTP.aclNetPrefix=comboPrefixMask.getSelectedItem().toString().trim();
        //System.out.println("IP-Filter Network = "+ConfigFTP.aclNetAddress+ConfigFTP.aclNetPrefix); 
        //    
        this.comboTypeACL.setModel(new DefaultComboBoxModel<>(ActionsFacade.aclTypeArray));
        this.comboTypeACL.setEditable(false);
        ConfigFTP.aclType=comboTypeACL.getSelectedItem().toString().trim();
        //System.out.println("IP-Filter Network Type = "+ConfigFTP.aclType); 
        //
        this.comboListenIP.setModel(new DefaultComboBoxModel<>(ActionsFacade.listLocalIpAddr().stream().toArray(String[]::new))); 
        this.comboListenIP.setEditable(false);
        this.taLog.setBackground(Color.BLACK);
        this.taLog.setForeground(Color.CYAN);
        this.tfFolder.setEditable(false);
        this.tfAclNetAdres.setText(ICFG.aclNetDefaultAddress);
        //this.tfAllowNet.setSize(77, 24);
        this.tfAclNetAdres.setMaximumSize(ICFG.tfAclNetSize);
        this.tfAclNetAdres.setMinimumSize(ICFG.tfAclNetSize);
        this.tfAclNetAdres.setPreferredSize(ICFG.tfAclNetSize);
        tfAclNetAdres.setEnabled(false);
        comboPrefixMask.setEnabled(false); 
        comboTypeACL.setEnabled(false);
        btnAclNetIpData.setEnabled(false);
        tfFolder.setText(ConfigFTP.folder);
        tfPort.setText(ConfigFTP.port);
    }
    
    public static String maxSpeedString () {
        return "Max speed = " + String.format("%3.1f", (0.0+ConfigFTP.MAX_SPEED)/1000000) + " Mbyte/s = " + String.format("%3.1f", 8*(0.0+ConfigFTP.MAX_SPEED)/1000000) + " Mbit/s";
    }
    
    public static void showLogTA() {
        if (running == true) {
            try {
                //String textLine;
                FileReader freader = new FileReader("log/app.log");
                BufferedReader breader = new BufferedReader(freader);
                //while ((textLine = reader.readLine()) != null) {
                while ( breader.readLine() != null) {    
                    //textLine = reader.readLine();
                    taLog.read(breader, "taLogArea");
                }
            } catch (IOException ioe) {
                System.err.println(ioe);
            }
        }
    }    

    private synchronized static void startServer(String args[]) throws FtpException, FtpServerConfigurationException {
        //File propertiesFile = new File("cfg/log4j.properties");
        //PropertyConfigurator.configure(propertiesFile.toString());
        PropertyConfigurator.configure("cfg/log4j.properties");
        //DOMConfigurator.configure("cfg/log4j.xml"); 
        j4log = Logger.getLogger(PjFtpServer.class.getName());
        //jul.config(msg);

        PropertiesUserManagerFactory userManagerFactory = new PropertiesUserManagerFactory();
        UserManager userManager = userManagerFactory.createUserManager();
        BaseUser user = new BaseUser();
        user.setName(ConfigFTP.username);
        user.setPassword(ConfigFTP.password);
        user.setHomeDirectory(ConfigFTP.folder);
        List<Authority> authorities = new ArrayList<Authority>();
        if (ConfigFTP.writable) authorities.add(new WritePermission());
        authorities.add(new ConcurrentLoginPermission(ConfigFTP.MAX_CONCURRENT_LOGINS, ConfigFTP.MAX_CONCURRENT_LOGINS_PER_IP));
        authorities.add(new TransferRatePermission(ConfigFTP.MAX_SPEED, ConfigFTP.MAX_SPEED));
        user.setAuthorities(authorities);
        user.setMaxIdleTime(ConfigFTP.MAX_IDLE_TIME);
        userManager.save(user);
        
        ListenerFactory listenerFactory = new ListenerFactory();
        listenerFactory.setPort(Integer.parseInt(ConfigFTP.port));
        listenerFactory.setServerAddress(ConfigFTP.listenIP);
        listenerFactory.setIdleTimeout(ConfigFTP.MAX_IDLE_TIME);
        j4log.log(Level.INFO, "pj-ftp-server try to start");
        j4log.log(Level.INFO, "try to start at = " + ICFG.sdtf.format(new Date()));
        if (ConfigFTP.ipFilterEnabled) {
        try {
            //ConfigFTP.aclNetAddress=tfAllowNet.getText().trim();
            //ConfigFTP.allowNetPrefixMask=comboPrefixMask.getSelectedItem().toString().trim();
            //System.out.println("Allow Network = "+aclNetAddress+allowNetPrefixMask.split("=")[0]);             
            //sessionFilter = new RemoteIpFilter(IpFilterType.ALLOW, aclNetAddress + allowNetPrefixMask.split("=")[0]);
            RemoteIpFilter rif = new RemoteIpFilter(ActionsFacade.aclTypeMap.get(ConfigFTP.aclType.trim()));
            boolean bnet=rif.add(ConfigFTP.aclNetAddress + ConfigFTP.aclNetPrefix);
            boolean bloop=rif.add("127.0.0.1"); //- BLOCK LOOP-BACK IF NOT LISTEN ON THIS !!!!!!!!!!!!!!!!!!!!!
            if (bnet && bloop) {
                System.out.println("IP-Filter Type = " +rif.getType().name());
                j4log.log(Level.INFO, "IP-Filter Type = " +rif.getType().name());                
                System.out.println("IP-Filter Network = " +ConfigFTP.aclNetAddress + ConfigFTP.aclNetPrefix +" , IP-Filter make success !");
                j4log.log(Level.INFO, "IP-Filter Network = " +ConfigFTP.aclNetAddress + ConfigFTP.aclNetPrefix +" , IP-Filter make success !");
                if (rif.getType().name().equals("DENY")) { 
                    System.out.println("IP-Filter:  All other networks are allowed");
                    j4log.log(Level.INFO, "IP-Filter:  All other networks are allowed");
                } else {
                    System.out.println("IP-Filter:  All other networks are denied"); 
                    j4log.log(Level.INFO, "IP-Filter:  All other networks are denied");
                }
            }
            sessionFilter=rif;
            listenerFactory.setSessionFilter(sessionFilter);
        } catch (NumberFormatException | UnknownHostException ex) {
            java.util.logging.Logger.getLogger(PjFtpServer.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        }            
        }

        FtpServerFactory ftpServerFactory = new FtpServerFactory();
        ftpServerFactory.setUserManager(userManager);
        ftpServerFactory.addListener("default", listenerFactory.createListener());

        ConnectionConfigFactory configFactory = new ConnectionConfigFactory();
        //configFactory.setAnonymousLoginEnabled(true);
        configFactory.setMaxThreads(ConfigFTP.MAX_THREADS_LOGINS + 8);
        configFactory.setMaxAnonymousLogins(ConfigFTP.MAX_THREADS_LOGINS);
        configFactory.setMaxLogins(ConfigFTP.MAX_THREADS_LOGINS);
        ConnectionConfig connectionConfig = configFactory.createConnectionConfig();
        ftpServerFactory.setConnectionConfig(connectionConfig);
        //mrLog = factory.getMessageResource();
        //Map<String, String> hmLog = mrLog.getMessages("INFO");

        server = ftpServerFactory.createServer();
        server.start();
        //jul.log(Level.SEVERE, "oppanki");
        j4log.log(Level.INFO, "pj-ftp-server running");
        j4log.log(Level.INFO, "Max Threads = "+connectionConfig.getMaxThreads());
        if (ConfigFTP.username.trim().equals("anonymous")) {
            j4log.log(Level.INFO, "Anonymous Login Enabled by default = "+connectionConfig.isAnonymousLoginEnabled());
            j4log.log(Level.INFO, "Max concurrent Anonymous Logins = "+connectionConfig.getMaxAnonymousLogins());
        }
        j4log.log(Level.INFO, "Max concurrent Logins = "+connectionConfig.getMaxLogins());
        j4log.log(Level.INFO, "Max concurrent Logins per IP = "+ConfigFTP.MAX_CONCURRENT_LOGINS_PER_IP);
        j4log.log(Level.INFO, "Server Listen Address = "+listenerFactory.getServerAddress());
        j4log.log(Level.INFO, "Server Listen Port = "+listenerFactory.getPort());
        j4log.log(Level.INFO, "Server Idle TimeOut = "+listenerFactory.getIdleTimeout() + " sec");
        j4log.log(Level.INFO, "Writable = "+ConfigFTP.writable);
        j4log.log(Level.INFO, "Folder = "+ConfigFTP.folder);
        j4log.log(Level.INFO, maxSpeedString());
        if (ConfigFTP.ipFilterEnabled) {
            j4log.log(Level.INFO, "IP-Filter Network = " + ConfigFTP.aclNetAddress+ConfigFTP.aclNetPrefix);
            j4log.log(Level.INFO, "IP-Filter Type = " + ConfigFTP.aclType.toUpperCase());
            if (ConfigFTP.aclType.trim().equals("deny")) { 
                System.out.println("IP-Filter:  All other networks are allowed");
                j4log.log(Level.INFO, "IP-Filter:  All other networks are allowed");
            } else {
                System.out.println("IP-Filter:  All other networks are denied"); 
                j4log.log(Level.INFO, "IP-Filter:  All other networks are denied");
            }            
        }
        running = true;
        if (args.length == 0) {
            /*Log_Thread = new Log_Thread("log/app.log");
            try {
                Log_Thread.start();
            } catch (IllegalThreadStateException itse) {}*/
            frame.setTitle(ICFG.zagolovok + ", server running");
            showLogTA();
        }
    }

    /*public void changeLF() {
        String changeLook = (String) JOptionPane.showInputDialog(frame, "Choose Look and Feel Here:", "Select Look and Feel", JOptionPane.QUESTION_MESSAGE, new ImageIcon(getClass().getResource("/img/color_swatch.png")), lookAndFeelsDisplay.toArray(), null);
        if (changeLook != null) {
            for (int a = 0; a < lookAndFeelsDisplay.size(); a++) {
                if (changeLook.equals(lookAndFeelsDisplay.get(a))) {
                    currentLAF = lookAndFeelsRealNames.get(a);
                    setLF(frame);
                    break;
                }
            }
        }
    }*/

    private void setBooleanBtnTf(Boolean sset) {
        tfUser.setEditable(sset);
        tfPassw.setEditable(sset);
        tfPort.setEditable(sset);
        tfAclNetAdres.setEnabled(sset);
        //
        comboTypeACL.setEnabled(sset);
        comboPrefixMask.setEnabled(sset);
        comboListenIP.setEnabled(sset);
        comboSpeed.setEnabled(sset);
        comboMaxLogins.setEnabled(sset);
        comboMaxLoginsPerIP.setEnabled(sset);
        comboWritable.setEnabled(sset);
        //
        checkBoxAnonymous.setEnabled(sset);
        checkBoxIpFilter.setEnabled(sset);
        btnSelectFolder.setEnabled(sset);
        if (checkBoxAnonymous.isSelected()) {
            tfUser.setEditable(false);
            tfPassw.setEditable(false);
        }
        if (!checkBoxIpFilter.isSelected()) {
            tfAclNetAdres.setEnabled(false);
            comboPrefixMask.setEnabled(false);
            comboTypeACL.setEnabled(false);
        }        
    }
    
    private Boolean checkAclNetwork() {
        try { 
            new SubnetUtils(ConfigFTP.aclNetAddress + ConfigFTP.aclNetPrefix);
            return true;
        } catch (IllegalArgumentException iae) {
            JOptionPane.showMessageDialog(frame, "Wrong Network IP-address ! = "+tfAclNetAdres.getText().trim()+comboPrefixMask.getSelectedItem().toString().trim().split("=")[0], "Error", JOptionPane.ERROR_MESSAGE);
            //btnToggleRunStop.setSelected(false);
            ConfigFTP.aclNetAddress = ICFG.aclNetDefaultAddress;
            System.out.println("ConfigFTP.allowNetAddress = "+ConfigFTP.aclNetAddress);
            ConfigFTP.aclNetPrefix = ICFG.aclNetDefaultPrefix;
            System.out.println("ConfigFTP.allowNetPrefix = "+ConfigFTP.aclNetPrefix);
            ConfigFTP.aclType = ICFG.aclTypeDefault;
            System.out.println("ConfigFTP.aclType = " + ConfigFTP.aclType); 
            comboTypeACL.setSelectedItem(ConfigFTP.aclType);
            System.out.println("comboPrefixMask.setSelectedItem = "+Arrays.asList(ActionsFacade.aclNetPrefixMaskArray).stream().filter(x->x.contains(ConfigFTP.aclNetPrefix)).findFirst());
            // BEZ .orElse("/32=255.255.255.255") NOT WORK !!!!!!
            comboPrefixMask.setSelectedItem(Arrays.asList(ActionsFacade.aclNetPrefixMaskArray).stream().filter(x->x.contains(ConfigFTP.aclNetPrefix)).findFirst().orElse("/32=255.255.255.255"));
            //System.out.println("tfAllowNet.isEditable() = "+tfAllowNet.isEditable());
            //System.out.println("tfAllowNet.isEnabled() = "+tfAllowNet.isEnabled());
            tfAclNetAdres.setText(ConfigFTP.aclNetAddress);
            return false;
        }  
    }

    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jPanel1 = new javax.swing.JPanel();
        jToolBar1 = new javax.swing.JToolBar();
        jSeparator7 = new javax.swing.JToolBar.Separator();
        jLabel8 = new javax.swing.JLabel();
        comboWritable = new javax.swing.JComboBox<>();
        jSeparator19 = new javax.swing.JToolBar.Separator();
        jLabel4 = new javax.swing.JLabel();
        comboListenIP = new javax.swing.JComboBox<>();
        jSeparator12 = new javax.swing.JToolBar.Separator();
        jLabel1 = new javax.swing.JLabel();
        tfPort = new javax.swing.JTextField();
        jSeparator1 = new javax.swing.JToolBar.Separator();
        jLabel2 = new javax.swing.JLabel();
        tfUser = new javax.swing.JTextField();
        jSeparator2 = new javax.swing.JToolBar.Separator();
        jLabel3 = new javax.swing.JLabel();
        tfPassw = new javax.swing.JTextField();
        jSeparator4 = new javax.swing.JToolBar.Separator();
        checkBoxAnonymous = new javax.swing.JCheckBox();
        btnAbout = new javax.swing.JButton();
        jToolBar3 = new javax.swing.JToolBar();
        jSeparator6 = new javax.swing.JToolBar.Separator();
        jLabel5 = new javax.swing.JLabel();
        comboSpeed = new javax.swing.JComboBox<>();
        jSeparator15 = new javax.swing.JToolBar.Separator();
        jLabel6 = new javax.swing.JLabel();
        comboMaxLogins = new javax.swing.JComboBox<>();
        jSeparator16 = new javax.swing.JToolBar.Separator();
        jLabel7 = new javax.swing.JLabel();
        comboMaxLoginsPerIP = new javax.swing.JComboBox<>();
        jSeparator18 = new javax.swing.JToolBar.Separator();
        jLabel9 = new javax.swing.JLabel();
        comboTypeACL = new javax.swing.JComboBox<>();
        jLabel10 = new javax.swing.JLabel();
        tfAclNetAdres = new javax.swing.JTextField();
        comboPrefixMask = new javax.swing.JComboBox<>();
        jSeparator11 = new javax.swing.JToolBar.Separator();
        checkBoxIpFilter = new javax.swing.JCheckBox();
        btnAclNetIpData = new javax.swing.JButton();
        jPanel2 = new javax.swing.JPanel();
        jScrollPane2 = new javax.swing.JScrollPane();
        taLog = new javax.swing.JTextArea();
        jPanel3 = new javax.swing.JPanel();
        jToolBar2 = new javax.swing.JToolBar();
        btnToggleRunStop = new javax.swing.JToggleButton();
        jSeparator3 = new javax.swing.JToolBar.Separator();
        btnSaveToCmdCfg = new javax.swing.JButton();
        jSeparator5 = new javax.swing.JToolBar.Separator();
        btnSelectFolder = new javax.swing.JButton();
        tfFolder = new javax.swing.JTextField();
        jSeparator10 = new javax.swing.JToolBar.Separator();
        btnClearLog = new javax.swing.JButton();
        jSeparator8 = new javax.swing.JToolBar.Separator();
        btnShowLog = new javax.swing.JButton();
        jSeparator13 = new javax.swing.JToolBar.Separator();
        btnQuit = new javax.swing.JButton();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
        setTitle("pj-ftp-server");
        setLocation(new java.awt.Point(99, 99));
        setMinimumSize(new java.awt.Dimension(800, 500));
        setUndecorated(true);
        setPreferredSize(new java.awt.Dimension(800, 500));
        setSize(new java.awt.Dimension(800, 500));

        jPanel1.setBorder(javax.swing.BorderFactory.createTitledBorder("parameters and actions"));
        jPanel1.setLayout(new java.awt.BorderLayout());

        jToolBar1.setBorder(javax.swing.BorderFactory.createTitledBorder("Configuration:"));
        jToolBar1.setFloatable(false);
        jToolBar1.add(jSeparator7);

        jLabel8.setText("Writable:");
        jToolBar1.add(jLabel8);

        comboWritable.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "Item 111" }));
        comboWritable.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                comboWritableActionPerformed(evt);
            }
        });
        jToolBar1.add(comboWritable);
        jToolBar1.add(jSeparator19);

        jLabel4.setText("Listen IP:");
        jToolBar1.add(jLabel4);

        comboListenIP.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "127.0.0.1" }));
        comboListenIP.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                comboListenIPActionPerformed(evt);
            }
        });
        jToolBar1.add(comboListenIP);
        jToolBar1.add(jSeparator12);

        jLabel1.setText("Port:");
        jToolBar1.add(jLabel1);

        tfPort.setText("21");
        jToolBar1.add(tfPort);
        jToolBar1.add(jSeparator1);

        jLabel2.setText("User:");
        jToolBar1.add(jLabel2);
        jToolBar1.add(tfUser);
        jToolBar1.add(jSeparator2);

        jLabel3.setText("Password:");
        jToolBar1.add(jLabel3);
        jToolBar1.add(tfPassw);
        jToolBar1.add(jSeparator4);

        checkBoxAnonymous.setText("Anonymous");
        checkBoxAnonymous.setToolTipText("Anonymous mode");
        checkBoxAnonymous.setFocusable(false);
        checkBoxAnonymous.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        checkBoxAnonymous.setHorizontalTextPosition(javax.swing.SwingConstants.RIGHT);
        checkBoxAnonymous.addItemListener(new java.awt.event.ItemListener() {
            public void itemStateChanged(java.awt.event.ItemEvent evt) {
                checkBoxAnonymousItemStateChanged(evt);
            }
        });
        jToolBar1.add(checkBoxAnonymous);

        btnAbout.setIcon(new javax.swing.ImageIcon(getClass().getResource("/img/help-green-16.png"))); // NOI18N
        btnAbout.setText("About");
        btnAbout.setFocusable(false);
        btnAbout.setHorizontalTextPosition(javax.swing.SwingConstants.RIGHT);
        btnAbout.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnAboutActionPerformed(evt);
            }
        });
        jToolBar1.add(btnAbout);

        jPanel1.add(jToolBar1, java.awt.BorderLayout.CENTER);

        jToolBar3.setBorder(javax.swing.BorderFactory.createTitledBorder("Configuration:"));
        jToolBar3.setFloatable(false);
        jToolBar3.add(jSeparator6);

        jLabel5.setText("MAX speed:");
        jToolBar3.add(jLabel5);

        comboSpeed.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "Item 111" }));
        comboSpeed.setToolTipText("");
        comboSpeed.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                comboSpeedActionPerformed(evt);
            }
        });
        jToolBar3.add(comboSpeed);
        jToolBar3.add(jSeparator15);

        jLabel6.setText("MAX logins:");
        jLabel6.setToolTipText("Max concurrent Logins");
        jToolBar3.add(jLabel6);

        comboMaxLogins.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "Item 111" }));
        comboMaxLogins.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                comboMaxLoginsActionPerformed(evt);
            }
        });
        jToolBar3.add(comboMaxLogins);
        jToolBar3.add(jSeparator16);

        jLabel7.setText("Max login per IP:");
        jLabel7.setToolTipText("Max concurrent Logins per IP");
        jToolBar3.add(jLabel7);

        comboMaxLoginsPerIP.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "Item 111" }));
        comboMaxLoginsPerIP.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                comboMaxLoginsPerIPActionPerformed(evt);
            }
        });
        jToolBar3.add(comboMaxLoginsPerIP);
        jToolBar3.add(jSeparator18);

        jLabel9.setText("Network-");
        jToolBar3.add(jLabel9);

        comboTypeACL.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "Item 1" }));
        comboTypeACL.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                comboTypeACLActionPerformed(evt);
            }
        });
        jToolBar3.add(comboTypeACL);

        jLabel10.setText("=");
        jToolBar3.add(jLabel10);

        tfAclNetAdres.setHorizontalAlignment(javax.swing.JTextField.LEFT);
        tfAclNetAdres.setText("10.0.0.0");
        jToolBar3.add(tfAclNetAdres);

        comboPrefixMask.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "Item 111" }));
        comboPrefixMask.setToolTipText("");
        comboPrefixMask.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                comboPrefixMaskActionPerformed(evt);
            }
        });
        jToolBar3.add(comboPrefixMask);
        jToolBar3.add(jSeparator11);

        checkBoxIpFilter.setText("IP-FILTER");
        checkBoxIpFilter.setToolTipText("Enable/Disable IP-filter");
        checkBoxIpFilter.setFocusable(false);
        checkBoxIpFilter.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        checkBoxIpFilter.setHorizontalTextPosition(javax.swing.SwingConstants.RIGHT);
        checkBoxIpFilter.addItemListener(new java.awt.event.ItemListener() {
            public void itemStateChanged(java.awt.event.ItemEvent evt) {
                checkBoxIpFilterItemStateChanged(evt);
            }
        });
        jToolBar3.add(checkBoxIpFilter);

        btnAclNetIpData.setIcon(new javax.swing.ImageIcon(getClass().getResource("/img/ip-blue-16.png"))); // NOI18N
        btnAclNetIpData.setText("Data");
        btnAclNetIpData.setFocusable(false);
        btnAclNetIpData.setHorizontalTextPosition(javax.swing.SwingConstants.RIGHT);
        btnAclNetIpData.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnAclNetIpDataActionPerformed(evt);
            }
        });
        jToolBar3.add(btnAclNetIpData);

        jPanel1.add(jToolBar3, java.awt.BorderLayout.PAGE_START);

        getContentPane().add(jPanel1, java.awt.BorderLayout.PAGE_START);

        jPanel2.setBorder(javax.swing.BorderFactory.createTitledBorder("Log-File content - /log/app.log"));
        jPanel2.setLayout(new java.awt.BorderLayout());

        taLog.setColumns(20);
        taLog.setRows(5);
        jScrollPane2.setViewportView(taLog);

        jPanel2.add(jScrollPane2, java.awt.BorderLayout.CENTER);

        getContentPane().add(jPanel2, java.awt.BorderLayout.CENTER);

        jPanel3.setBorder(javax.swing.BorderFactory.createTitledBorder("parameters and actions"));
        jPanel3.setLayout(new java.awt.BorderLayout());

        jToolBar2.setFloatable(false);

        btnToggleRunStop.setIcon(new javax.swing.ImageIcon(getClass().getResource("/img/go-green-krug-16.png"))); // NOI18N
        btnToggleRunStop.setText("Run server ");
        btnToggleRunStop.setFocusable(false);
        btnToggleRunStop.setHorizontalTextPosition(javax.swing.SwingConstants.RIGHT);
        btnToggleRunStop.addItemListener(new java.awt.event.ItemListener() {
            public void itemStateChanged(java.awt.event.ItemEvent evt) {
                btnToggleRunStopItemStateChanged(evt);
            }
        });
        jToolBar2.add(btnToggleRunStop);
        jToolBar2.add(jSeparator3);

        btnSaveToCmdCfg.setIcon(new javax.swing.ImageIcon(getClass().getResource("/img/save-16.png"))); // NOI18N
        btnSaveToCmdCfg.setText("Make CMD-config");
        btnSaveToCmdCfg.setToolTipText("Save this config to cmd-mode-config.properties");
        btnSaveToCmdCfg.setFocusable(false);
        btnSaveToCmdCfg.setHorizontalTextPosition(javax.swing.SwingConstants.RIGHT);
        btnSaveToCmdCfg.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnSaveToCmdCfgActionPerformed(evt);
            }
        });
        jToolBar2.add(btnSaveToCmdCfg);
        jToolBar2.add(jSeparator5);

        btnSelectFolder.setIcon(new javax.swing.ImageIcon(getClass().getResource("/img/folder-green-16.png"))); // NOI18N
        btnSelectFolder.setText("Select Folder: ");
        btnSelectFolder.setFocusable(false);
        btnSelectFolder.setHorizontalTextPosition(javax.swing.SwingConstants.RIGHT);
        btnSelectFolder.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnSelectFolderActionPerformed(evt);
            }
        });
        jToolBar2.add(btnSelectFolder);

        tfFolder.setEditable(false);
        tfFolder.setText("/tmp");
        jToolBar2.add(tfFolder);
        jToolBar2.add(jSeparator10);

        btnClearLog.setIcon(new javax.swing.ImageIcon(getClass().getResource("/img/clear-yellow-16.png"))); // NOI18N
        btnClearLog.setText("Clear Log");
        btnClearLog.setFocusable(false);
        btnClearLog.setHorizontalTextPosition(javax.swing.SwingConstants.RIGHT);
        btnClearLog.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnClearLogActionPerformed(evt);
            }
        });
        jToolBar2.add(btnClearLog);
        jToolBar2.add(jSeparator8);

        btnShowLog.setIcon(new javax.swing.ImageIcon(getClass().getResource("/img/clipboard-pencil-16.png"))); // NOI18N
        btnShowLog.setText("Show Log");
        btnShowLog.setFocusable(false);
        btnShowLog.setHorizontalTextPosition(javax.swing.SwingConstants.RIGHT);
        btnShowLog.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnShowLogActionPerformed(evt);
            }
        });
        jToolBar2.add(btnShowLog);
        jToolBar2.add(jSeparator13);

        btnQuit.setIcon(new javax.swing.ImageIcon(getClass().getResource("/img/quit-16.png"))); // NOI18N
        btnQuit.setText("Quit");
        btnQuit.setFocusable(false);
        btnQuit.setHorizontalTextPosition(javax.swing.SwingConstants.RIGHT);
        btnQuit.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        btnQuit.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnQuitActionPerformed(evt);
            }
        });
        jToolBar2.add(btnQuit);

        jPanel3.add(jToolBar2, java.awt.BorderLayout.CENTER);

        getContentPane().add(jPanel3, java.awt.BorderLayout.PAGE_END);

        getAccessibleContext().setAccessibleDescription("");

        pack();
        setLocationRelativeTo(null);
    }// </editor-fold>//GEN-END:initComponents

    private void checkBoxAnonymousItemStateChanged(java.awt.event.ItemEvent evt) {//GEN-FIRST:event_checkBoxAnonymousItemStateChanged
        if (tfUser.getText().trim().equals("anonymous")) {
            tfUser.setText("");
            tfPassw.setText("");
            tfUser.setEditable(true);
            tfPassw.setEditable(true);
            ConfigFTP.username="";
            ConfigFTP.password="";
        } else {
            tfUser.setText("anonymous");
            tfPassw.setText("jer@sey.com");
            tfUser.setEditable(false);
            tfPassw.setEditable(false);
            ConfigFTP.username="anonymous";
            ConfigFTP.password="jer@sey.com";            
        }
    }//GEN-LAST:event_checkBoxAnonymousItemStateChanged

    private void btnQuitActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnQuitActionPerformed
        int r = JOptionPane.showConfirmDialog(frame, "Really Quit ?", "Quit ?", JOptionPane.YES_NO_OPTION);
        if (r == JOptionPane.YES_OPTION) {
            try {
                server.stop();
            } catch (NullPointerException ne) {        }
            System.exit(0);           
        }
    }//GEN-LAST:event_btnQuitActionPerformed

    private void btnToggleRunStopItemStateChanged(java.awt.event.ItemEvent evt) {//GEN-FIRST:event_btnToggleRunStopItemStateChanged
        //
        ConfigFTP.loadCFGfromGUI();
        //
        if (!ActionsFacade.checkTcpPort(ConfigFTP.port)) {
            JOptionPane.showMessageDialog(frame, "Port wrong !", "Error", JOptionPane.ERROR_MESSAGE); 
            ConfigFTP.port=ICFG.DEFAULT_PORT;
            tfPort.setText(ConfigFTP.port);
            btnToggleRunStop.setSelected(false);
            return;
        }
        if (tfUser.getText().isEmpty() || tfPassw.getText().isEmpty() || tfFolder.getText().isEmpty()) {
            JOptionPane.showMessageDialog(frame, "Some parameters is empty !", "Error", JOptionPane.ERROR_MESSAGE);
            btnToggleRunStop.setSelected(false);
            return;
        }
        if (!ICFG.ipv.isValid(comboListenIP.getSelectedItem().toString().trim()))  {
            JOptionPane.showMessageDialog(frame, "Wrong listen IP-address !", "Error", JOptionPane.ERROR_MESSAGE);
            btnToggleRunStop.setSelected(false);
            return;            
        }
        if (!checkAclNetwork()) return;
        ImageIcon iconOn = new ImageIcon(getClass().getResource("/img/go-green-krug-16.png"));
        ImageIcon iconOf = new ImageIcon(getClass().getResource("/img/stop-16.png"));
        if (evt.getStateChange() == ItemEvent.DESELECTED) {
            if (running == true) {
                server.stop();
                btnToggleRunStop.setIcon(iconOn);
                btnToggleRunStop.setText("Run server");
                setBooleanBtnTf(true);
                taLog.grabFocus();//.setFocusable(true);
                frame.setTitle(ICFG.zagolovok + ", server stop");
                j4log.log(Level.INFO, "pj-ftp-server stop");
                showLogTA();
                return;
            }
        }
        if (evt.getStateChange() == ItemEvent.SELECTED) {
            try {
                startServer(new String[0]);
                btnToggleRunStop.setIcon(iconOf);
                btnToggleRunStop.setText("Stop server");
                setBooleanBtnTf(false);
            } catch (FtpException | FtpServerConfigurationException fe) {
                JOptionPane.showMessageDialog(frame, "Some wrong !", "Error", JOptionPane.ERROR_MESSAGE);
                btnToggleRunStop.setSelected(false);
            }
        }

    }//GEN-LAST:event_btnToggleRunStopItemStateChanged

    private void btnSelectFolderActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnSelectFolderActionPerformed
        JFileChooser myd = new JFileChooser();
        //myd.addChoosableFileFilter(new AudioFileFilter());
        //myd.setAcceptAllFileFilterUsed(false);
        myd.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
        switch (myd.showDialog(frame, "Select Folder")) {
            case JFileChooser.APPROVE_OPTION:
                //ftpFolder = myd.getSelectedFile().getPath();
                tfFolder.setText(myd.getSelectedFile().getPath());
                ConfigFTP.folder=myd.getSelectedFile().getPath();
                //putd = myd.getSelectedFile() + "";
                break;
            case JFileChooser.CANCEL_OPTION:
                break;
        }//switch
    }//GEN-LAST:event_btnSelectFolderActionPerformed

    private void btnAboutActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnAboutActionPerformed
        ActionsFacade.about(new ImageIcon(getClass().getResource("/img/logo/ftp-green-logo-128.png")));
    }//GEN-LAST:event_btnAboutActionPerformed

    private void btnClearLogActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnClearLogActionPerformed
        try {
            new PrintWriter("log/app.log").close();
            taLog.setText("");
        } catch (FileNotFoundException ex) {
            java.util.logging.Logger.getLogger(PjFtpServer.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        }        
        /*try (PrintWriter writer = new PrintWriter("log/app.log")) {
            writer.print("");
            writer.close();
            taLog.setText("");
            //taLog.repaint();
            //taLog.updateUI();
        } catch (FileNotFoundException ex) {
            java.util.logging.Logger.getLogger(PjFtpServer.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        }*/
    }//GEN-LAST:event_btnClearLogActionPerformed

    private void comboSpeedActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_comboSpeedActionPerformed
        ConfigFTP.MAX_SPEED=ActionsFacade.speedMap.get(comboSpeed.getSelectedItem().toString());
        System.out.println(maxSpeedString()); 
    }//GEN-LAST:event_comboSpeedActionPerformed

    private void comboMaxLoginsActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_comboMaxLoginsActionPerformed
        ConfigFTP.MAX_THREADS_LOGINS=Integer.parseInt(comboMaxLogins.getSelectedItem().toString());        
        System.out.println("Max concurrent Logins = "+ConfigFTP.MAX_THREADS_LOGINS);
    }//GEN-LAST:event_comboMaxLoginsActionPerformed

    private void comboMaxLoginsPerIPActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_comboMaxLoginsPerIPActionPerformed
        ConfigFTP.MAX_CONCURRENT_LOGINS_PER_IP=Integer.parseInt(comboMaxLoginsPerIP.getSelectedItem().toString());        
        System.out.println("Max concurrent Logins Per IP = "+ConfigFTP.MAX_CONCURRENT_LOGINS_PER_IP);
    }//GEN-LAST:event_comboMaxLoginsPerIPActionPerformed

    private void comboWritableActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_comboWritableActionPerformed
        ConfigFTP.writable=Boolean.parseBoolean(comboWritable.getSelectedItem().toString());
        System.out.println("Writable = "+ConfigFTP.writable);
    }//GEN-LAST:event_comboWritableActionPerformed

    private void comboPrefixMaskActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_comboPrefixMaskActionPerformed
        ConfigFTP.aclNetAddress=tfAclNetAdres.getText().trim();
        ConfigFTP.aclNetPrefix=comboPrefixMask.getSelectedItem().toString().split("=")[0].trim();
        ConfigFTP.aclType=comboTypeACL.getSelectedItem().toString().trim();
        checkAclNetwork();
        System.out.println("IP-Filter Network = " + ConfigFTP.aclNetAddress+ConfigFTP.aclNetPrefix); 
        System.out.println("IP-Filter Type = " + ConfigFTP.aclType.toUpperCase());
        if (ConfigFTP.aclType.trim().equals("deny"))  
            System.out.println("IP-Filter:  All other networks are allowed");
        else 
            System.out.println("IP-Filter:  All other networks are denied"); 
    }//GEN-LAST:event_comboPrefixMaskActionPerformed

    private void checkBoxIpFilterItemStateChanged(java.awt.event.ItemEvent evt) {//GEN-FIRST:event_checkBoxIpFilterItemStateChanged
        if (ConfigFTP.ipFilterEnabled) {
            ConfigFTP.ipFilterEnabled=false;
            tfAclNetAdres.setEnabled(false);
            comboPrefixMask.setEnabled(false); 
            comboTypeACL.setEnabled(false);
            btnAclNetIpData.setEnabled(false);
        } else {
            ConfigFTP.ipFilterEnabled=true;
            tfAclNetAdres.setEnabled(true);
            comboPrefixMask.setEnabled(true); 
            comboTypeACL.setEnabled(true); 
            btnAclNetIpData.setEnabled(true);
        }
    }//GEN-LAST:event_checkBoxIpFilterItemStateChanged

    private void comboListenIPActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_comboListenIPActionPerformed
        ConfigFTP.listenIP=comboListenIP.getSelectedItem().toString().trim();
    }//GEN-LAST:event_comboListenIPActionPerformed

    private void btnSaveToCmdCfgActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnSaveToCmdCfgActionPerformed
        ConfigFTP.saveCFGfromGUI();
    }//GEN-LAST:event_btnSaveToCmdCfgActionPerformed

    private void btnAclNetIpDataActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnAclNetIpDataActionPerformed
        ConfigFTP.aclNetAddress=tfAclNetAdres.getText().trim();
        ConfigFTP.aclNetPrefix=comboPrefixMask.getSelectedItem().toString().split("=")[0].trim();
        ConfigFTP.aclType=comboTypeACL.getSelectedItem().toString().trim();
        if (checkAclNetwork()) {
            System.out.println(ConfigFTP.aclType.toUpperCase()+" Network = "+ConfigFTP.aclNetAddress+ConfigFTP.aclNetPrefix); 
            JOptionPane.showMessageDialog(this, ActionsFacade.ipCalculator(ConfigFTP.aclNetAddress + ConfigFTP.aclNetPrefix), "IP-data for "+ConfigFTP.aclType.toUpperCase()+" Network", JOptionPane.INFORMATION_MESSAGE);
        }
    }//GEN-LAST:event_btnAclNetIpDataActionPerformed

    private void btnShowLogActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnShowLogActionPerformed
        showLogTA();
    }//GEN-LAST:event_btnShowLogActionPerformed

    private void comboTypeACLActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_comboTypeACLActionPerformed
        ConfigFTP.aclType=comboTypeACL.getSelectedItem().toString().trim();
        ConfigFTP.aclNetAddress=tfAclNetAdres.getText().trim();
        ConfigFTP.aclNetPrefix=comboPrefixMask.getSelectedItem().toString().split("=")[0].trim();
        checkAclNetwork();
        System.out.println("IP-Filter Network = " + ConfigFTP.aclNetAddress+ConfigFTP.aclNetPrefix); 
        System.out.println("IP-Filter Type = " + ConfigFTP.aclType.toUpperCase());
        if (ConfigFTP.aclType.trim().equals("deny"))  
            System.out.println("IP-Filter:  All other networks are allowed");
        else 
            System.out.println("IP-Filter:  All other networks are denied");        
    }//GEN-LAST:event_comboTypeACLActionPerformed

    public static void main(String args[]) {
        /*try {
            for (javax.swing.UIManager.LookAndFeelInfo info : javax.swing.UIManager.getInstalledLookAndFeels()) {
                if ("Metal".equals(info.getName())) {
                    javax.swing.UIManager.setLookAndFeel(info.getClassName());
                    break;
                }
            }
        } catch (ClassNotFoundException | InstantiationException | IllegalAccessException | javax.swing.UnsupportedLookAndFeelException ex) {
            java.util.logging.Logger.getLogger(PjFtpServer.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } */
        //ConfigFTP.loadCFG();
        if (args.length == 0) {
            java.awt.EventQueue.invokeLater(new Runnable() {
                @Override
                public void run() {                    
                    frame = new PjFtpServer();
                    ActionsFacade.InstallLF();
                    ActionsFacade.setLF(frame);
                    frame.getRootPane().setWindowDecorationStyle(JRootPane.FRAME);
                    JFrame.setDefaultLookAndFeelDecorated(true);
                    JDialog.setDefaultLookAndFeelDecorated(true);
                    JOptionPane.setRootFrame(frame);
                    frame.setSize(ICFG.FW, ICFG.FH);
                    frame.setLocation(50, 50);
                    frame.setResizable(true);
                    frame.setVisible(true);
                }
            });
        }
        if (args.length > 0) {
            try {
                /*Arrays.stream(args)
                .forEach(x -> { argsHM.put(x.split("=")[0].toString(), x.split("=")[1].toString()); });
                System.out.println(argsHM);
                String pwd="";
                if (argsHM.get("user").toLowerCase().trim().equals("anonymous")) {
                    pwd="jer@sey.com";
                    argsHM.put("passw", pwd);
                }
                System.out.println(argsHM);*/ 
                //
                ConfigFTP.loadCFGfromFile();
                //
                if (!ICFG.ipv.isValid(ConfigFTP.listenIP))  {
                    System.out.println("Wrong listen IP ! \nExit !"); 
                    //ActionsFacade.useExamples();
                    return;
                }
                if (!ActionsFacade.checkTcpPort(ConfigFTP.port)) {
                    System.out.println("Port Wrong ! \nExit !"); 
                    //ActionsFacade.useExamples();
                    return;
                }                
                try {
                    startServer(args);
                } catch (FtpException | FtpServerConfigurationException ex) {
                    java.util.logging.Logger.getLogger(PjFtpServer.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
                    System.out.println("\nNOT run !\nSome of parameters wrong !");
                    //ActionsFacade.useExamples();                    
                }
            } catch (NullPointerException | ArrayIndexOutOfBoundsException ne) {
                System.out.println("NOT run !\nSome of parameters not given !");
                System.out.println("Exception = " + ne.toString());
                //ActionsFacade.useExamples();
            }
        }
            //}
        //});
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    public static javax.swing.JButton btnAbout;
    public static javax.swing.JButton btnAclNetIpData;
    public static javax.swing.JButton btnClearLog;
    private javax.swing.JButton btnQuit;
    public static javax.swing.JButton btnSaveToCmdCfg;
    public static javax.swing.JButton btnSelectFolder;
    public static javax.swing.JButton btnShowLog;
    public static javax.swing.JToggleButton btnToggleRunStop;
    public static javax.swing.JCheckBox checkBoxAnonymous;
    public static javax.swing.JCheckBox checkBoxIpFilter;
    public static javax.swing.JComboBox<String> comboListenIP;
    public static javax.swing.JComboBox<String> comboMaxLogins;
    public static javax.swing.JComboBox<String> comboMaxLoginsPerIP;
    public static javax.swing.JComboBox<String> comboPrefixMask;
    public static javax.swing.JComboBox<String> comboSpeed;
    public static javax.swing.JComboBox<String> comboTypeACL;
    public static javax.swing.JComboBox<String> comboWritable;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel10;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JLabel jLabel5;
    private javax.swing.JLabel jLabel6;
    private javax.swing.JLabel jLabel7;
    private javax.swing.JLabel jLabel8;
    private javax.swing.JLabel jLabel9;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JPanel jPanel3;
    private javax.swing.JScrollPane jScrollPane2;
    private javax.swing.JToolBar.Separator jSeparator1;
    private javax.swing.JToolBar.Separator jSeparator10;
    private javax.swing.JToolBar.Separator jSeparator11;
    private javax.swing.JToolBar.Separator jSeparator12;
    private javax.swing.JToolBar.Separator jSeparator13;
    private javax.swing.JToolBar.Separator jSeparator15;
    private javax.swing.JToolBar.Separator jSeparator16;
    private javax.swing.JToolBar.Separator jSeparator18;
    private javax.swing.JToolBar.Separator jSeparator19;
    private javax.swing.JToolBar.Separator jSeparator2;
    private javax.swing.JToolBar.Separator jSeparator3;
    private javax.swing.JToolBar.Separator jSeparator4;
    private javax.swing.JToolBar.Separator jSeparator5;
    private javax.swing.JToolBar.Separator jSeparator6;
    private javax.swing.JToolBar.Separator jSeparator7;
    private javax.swing.JToolBar.Separator jSeparator8;
    private javax.swing.JToolBar jToolBar1;
    private javax.swing.JToolBar jToolBar2;
    private javax.swing.JToolBar jToolBar3;
    public static javax.swing.JTextArea taLog;
    public static javax.swing.JTextField tfAclNetAdres;
    public static javax.swing.JTextField tfFolder;
    public static javax.swing.JTextField tfPassw;
    public static javax.swing.JTextField tfPort;
    public static javax.swing.JTextField tfUser;
    // End of variables declaration//GEN-END:variables
}
