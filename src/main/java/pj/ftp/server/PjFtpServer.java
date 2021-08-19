package pj.ftp.server;

import java.awt.Color;
import java.awt.event.ItemEvent;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.swing.DefaultComboBoxModel;
import javax.swing.ImageIcon;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JRootPane;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;
import org.apache.commons.lang3.StringUtils;
import org.apache.ftpserver.ConnectionConfig;
import org.apache.ftpserver.ConnectionConfigFactory;
import org.apache.ftpserver.FtpServer;
import org.apache.ftpserver.FtpServerConfigurationException;
import org.apache.ftpserver.FtpServerFactory;
import org.apache.ftpserver.ftplet.Authority;
import org.apache.ftpserver.ftplet.FtpException;
import org.apache.ftpserver.ftplet.UserManager;
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
    public static int MAX_CONCURRENT_LOGINS = 11;
    public static int MAX_CONCURRENT_LOGINS_PER_IP = 11;
    public static int MAX_IDLE_TIME = 9999;
    public static int MAX_THREADS_LOGINS = 128;
    public static int MAX_SPEED = Integer.MAX_VALUE;
    public static Boolean writeAccess = true;
    //public static MessageResource mrLog;
    //public static java.util.logging.Logger jul;
    public static org.apache.log4j.Logger j4log;
    public static PjFtpServer frame;
    public static Map<String, String> argsHM = new HashMap<String, String>();
    public static Thread Log_Thread;

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
        this.comboListenIP.setModel(new DefaultComboBoxModel<>(Actions.listLocalIpAddr().stream().toArray(String[]::new))); 
        this.comboListenIP.setEditable(false);
        this.taLog.setBackground(Color.BLACK);
        this.taLog.setForeground(Color.CYAN);
    }

    private synchronized static void startServer(String args[], String tcpPort, String login, String password, String folder, String listenIP) throws FtpException, FtpServerConfigurationException {
        //File propertiesFile = new File("cfg/log4j.properties");
        //PropertyConfigurator.configure(propertiesFile.toString());
        PropertyConfigurator.configure("cfg/log4j.properties");
        //DOMConfigurator.configure("cfg/log4j.xml"); 
        j4log = Logger.getLogger(PjFtpServer.class.getName());
        //jul.config(msg);

        PropertiesUserManagerFactory userManagerFactory = new PropertiesUserManagerFactory();
        UserManager userManager = userManagerFactory.createUserManager();
        BaseUser user = new BaseUser();
        user.setName(login);
        user.setPassword(password);
        user.setHomeDirectory(folder);
        List<Authority> authorities = new ArrayList<Authority>();
        if (writeAccess) authorities.add(new WritePermission());
        authorities.add(new ConcurrentLoginPermission(MAX_CONCURRENT_LOGINS, MAX_CONCURRENT_LOGINS_PER_IP));
        authorities.add(new TransferRatePermission(MAX_SPEED, MAX_SPEED));
        user.setAuthorities(authorities);
        user.setMaxIdleTime(MAX_IDLE_TIME);
        userManager.save(user);

        ListenerFactory listenerFactory = new ListenerFactory();
        listenerFactory.setPort(Integer.parseInt(tcpPort));
        listenerFactory.setServerAddress(listenIP);
        listenerFactory.setIdleTimeout(MAX_IDLE_TIME);

        FtpServerFactory ftpServerFactory = new FtpServerFactory();
        ftpServerFactory.setUserManager(userManager);
        ftpServerFactory.addListener("default", listenerFactory.createListener());

        ConnectionConfigFactory configFactory = new ConnectionConfigFactory();
        //configFactory.setAnonymousLoginEnabled(true);
        configFactory.setMaxThreads(MAX_THREADS_LOGINS);
        configFactory.setMaxAnonymousLogins(MAX_THREADS_LOGINS);
        configFactory.setMaxLogins(MAX_THREADS_LOGINS);
        ConnectionConfig connectionConfig = configFactory.createConnectionConfig();
        ftpServerFactory.setConnectionConfig(connectionConfig);
        //mrLog = factory.getMessageResource();
        //Map<String, String> hmLog = mrLog.getMessages("INFO");

        server = ftpServerFactory.createServer();
        server.start();
        //jul.log(Level.SEVERE, "oppanki");
        j4log.log(Level.INFO, "pj-ftp-server running");
        j4log.log(Level.INFO, "Max Threads = "+connectionConfig.getMaxThreads());
        j4log.log(Level.INFO, "Anonymous Login Enabled = "+connectionConfig.isAnonymousLoginEnabled());
        j4log.log(Level.INFO, "Max Anonymous Logins = "+connectionConfig.getMaxAnonymousLogins());
        j4log.log(Level.INFO, "Max Logins = "+connectionConfig.getMaxLogins());
        j4log.log(Level.INFO, "Server Address = "+listenerFactory.getServerAddress());
        j4log.log(Level.INFO, "Server Port = "+listenerFactory.getPort());
        j4log.log(Level.INFO, "Server Idle TimeOut = "+listenerFactory.getIdleTimeout());
        running = true;
        if (args.length == 0) {
            Log_Thread = new Log_Thread("log/app.log");
            try {
                Log_Thread.start();
            } catch (IllegalThreadStateException itse) {}
            frame.setTitle(ICFG.zagolovok + ", server running");
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

    public void setLF(JFrame frame) {
        try {
            UIManager.setLookAndFeel(ICFG.currentLAF);
        } catch (InstantiationException | IllegalAccessException | UnsupportedLookAndFeelException | ClassNotFoundException ex) {
            Logger.getLogger(this.getName()).log(Level.ERROR, null, ex);
        }
        SwingUtilities.updateComponentTreeUI(frame);
        //frame.pack();
    }

    private void setBooleanBtnTf(Boolean sset) {
        tfUser.setEditable(sset);
        tfPassw.setEditable(sset);
        tfPort.setEditable(sset);
        tfFolder.setEditable(sset);
        comboListenIP.setEnabled(sset);
        checkBoxAnonymous.setEnabled(sset);
        btnSelectFolder.setEnabled(sset);
        if (checkBoxAnonymous.isSelected()) {
            tfUser.setEditable(false);
            tfPassw.setEditable(false);
        }
    }

    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jPanel1 = new javax.swing.JPanel();
        jToolBar1 = new javax.swing.JToolBar();
        jSeparator9 = new javax.swing.JToolBar.Separator();
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
        jSeparator11 = new javax.swing.JToolBar.Separator();
        btnToggleRunStop = new javax.swing.JToggleButton();
        jSeparator7 = new javax.swing.JToolBar.Separator();
        jPanel2 = new javax.swing.JPanel();
        jScrollPane2 = new javax.swing.JScrollPane();
        taLog = new javax.swing.JTextArea();
        jPanel3 = new javax.swing.JPanel();
        jToolBar2 = new javax.swing.JToolBar();
        jSeparator3 = new javax.swing.JToolBar.Separator();
        btnSelectFolder = new javax.swing.JButton();
        jSeparator6 = new javax.swing.JToolBar.Separator();
        tfFolder = new javax.swing.JTextField();
        jSeparator10 = new javax.swing.JToolBar.Separator();
        btnClearLog = new javax.swing.JButton();
        jSeparator8 = new javax.swing.JToolBar.Separator();
        btnAbout = new javax.swing.JButton();
        jSeparator13 = new javax.swing.JToolBar.Separator();
        btnQuit = new javax.swing.JButton();
        jSeparator5 = new javax.swing.JToolBar.Separator();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
        setTitle("pj-ftp-server");
        setLocation(new java.awt.Point(99, 99));
        setMinimumSize(new java.awt.Dimension(800, 500));
        setUndecorated(true);
        setPreferredSize(new java.awt.Dimension(800, 500));
        setSize(new java.awt.Dimension(800, 500));

        jPanel1.setBorder(javax.swing.BorderFactory.createTitledBorder("parameters and actions"));
        jPanel1.setLayout(new java.awt.BorderLayout());

        jToolBar1.setFloatable(false);
        jToolBar1.add(jSeparator9);

        jLabel4.setText("Listen IP: ");
        jToolBar1.add(jLabel4);

        comboListenIP.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "127.0.0.1" }));
        jToolBar1.add(comboListenIP);
        jToolBar1.add(jSeparator12);

        jLabel1.setText("Port: ");
        jToolBar1.add(jLabel1);

        tfPort.setText("21");
        jToolBar1.add(tfPort);
        jToolBar1.add(jSeparator1);

        jLabel2.setText("User: ");
        jToolBar1.add(jLabel2);
        jToolBar1.add(tfUser);
        jToolBar1.add(jSeparator2);

        jLabel3.setText("Password: ");
        jToolBar1.add(jLabel3);
        jToolBar1.add(tfPassw);
        jToolBar1.add(jSeparator4);

        checkBoxAnonymous.setText("Anonymous mode");
        checkBoxAnonymous.setFocusable(false);
        checkBoxAnonymous.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        checkBoxAnonymous.setHorizontalTextPosition(javax.swing.SwingConstants.RIGHT);
        checkBoxAnonymous.addItemListener(new java.awt.event.ItemListener() {
            public void itemStateChanged(java.awt.event.ItemEvent evt) {
                checkBoxAnonymousItemStateChanged(evt);
            }
        });
        jToolBar1.add(checkBoxAnonymous);
        jToolBar1.add(jSeparator11);

        btnToggleRunStop.setIcon(new javax.swing.ImageIcon(getClass().getResource("/img/go-green-krug-16.png"))); // NOI18N
        btnToggleRunStop.setText("Run server ");
        btnToggleRunStop.setFocusable(false);
        btnToggleRunStop.setHorizontalTextPosition(javax.swing.SwingConstants.RIGHT);
        btnToggleRunStop.addItemListener(new java.awt.event.ItemListener() {
            public void itemStateChanged(java.awt.event.ItemEvent evt) {
                btnToggleRunStopItemStateChanged(evt);
            }
        });
        jToolBar1.add(btnToggleRunStop);
        jToolBar1.add(jSeparator7);

        jPanel1.add(jToolBar1, java.awt.BorderLayout.CENTER);

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
        jToolBar2.add(jSeparator3);

        btnSelectFolder.setIcon(new javax.swing.ImageIcon(getClass().getResource("/img/folder-green-16.png"))); // NOI18N
        btnSelectFolder.setText("Select Folder: ");
        btnSelectFolder.setFocusable(false);
        btnSelectFolder.setHorizontalTextPosition(javax.swing.SwingConstants.RIGHT);
        btnSelectFolder.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        btnSelectFolder.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnSelectFolderActionPerformed(evt);
            }
        });
        jToolBar2.add(btnSelectFolder);
        jToolBar2.add(jSeparator6);

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

        btnAbout.setIcon(new javax.swing.ImageIcon(getClass().getResource("/img/info-cyan-16.png"))); // NOI18N
        btnAbout.setText(" About");
        btnAbout.setFocusable(false);
        btnAbout.setHorizontalTextPosition(javax.swing.SwingConstants.RIGHT);
        btnAbout.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        btnAbout.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnAboutActionPerformed(evt);
            }
        });
        jToolBar2.add(btnAbout);
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
        jToolBar2.add(jSeparator5);

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
        } else {
            tfUser.setText("anonymous");
            tfPassw.setText("jer@sey.com");
            tfUser.setEditable(false);
            tfPassw.setEditable(false);
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
        if (!Actions.checkTcpPort(tfPort.getText().trim())) {
            JOptionPane.showMessageDialog(frame, "Port wrong !", "Error", JOptionPane.ERROR_MESSAGE); 
            btnToggleRunStop.setSelected(false);
            return;
        }
        if (tfUser.getText().isEmpty() || tfPassw.getText().isEmpty() || tfFolder.getText().isEmpty()) {
            JOptionPane.showMessageDialog(frame, "Some wrong parameters !", "Error", JOptionPane.ERROR_MESSAGE);
            btnToggleRunStop.setSelected(false);
            return;
        }
        if (!ICFG.ipv.isValid(comboListenIP.getSelectedItem().toString().trim()))  {
            JOptionPane.showMessageDialog(frame, "Wrong listen IP-address !", "Error", JOptionPane.ERROR_MESSAGE);
            btnToggleRunStop.setSelected(false);
            return;            
        }
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
                return;
            }
        }
        if (evt.getStateChange() == ItemEvent.SELECTED) {
            try {
                startServer(new String[0], tfPort.getText().trim(), tfUser.getText().trim(), tfPassw.getText().trim(), tfFolder.getText().trim(), comboListenIP.getSelectedItem().toString().trim());
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
                //putd = myd.getSelectedFile() + "";
                break;
            case JFileChooser.CANCEL_OPTION:
                break;
        }//switch
    }//GEN-LAST:event_btnSelectFolderActionPerformed

    private void btnAboutActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnAboutActionPerformed
        //changeLF();
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
        ImageIcon icone = new ImageIcon(getClass().getResource("/img/logo/ftp-green-logo-128.png"));
        JOptionPane.showMessageDialog(frame, msg, "About " + ICFG.zagolovok, JOptionPane.INFORMATION_MESSAGE, icone);
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
        if (args.length == 0) {
            java.awt.EventQueue.invokeLater(new Runnable() {
                @Override
                public void run() {                    
                    frame = new PjFtpServer();
                    Actions.InstallLF();
                    frame.setLF(frame);
                    frame.getRootPane().setWindowDecorationStyle(JRootPane.FRAME);
                    JFrame.setDefaultLookAndFeelDecorated(true);
                    JDialog.setDefaultLookAndFeelDecorated(true);
                    JOptionPane.setRootFrame(frame);
                    frame.setSize(ICFG.FW, ICFG.FH);
                    frame.setLocation(200, 200);
                    frame.setResizable(true);
                    frame.setVisible(true);
                }
            });
        }
        if (args.length > 0) {
            try {
                Arrays.stream(args)
                .forEach(x -> { argsHM.put(x.split("=")[0].toString(), x.split("=")[1].toString()); });
                System.out.println(argsHM);
                String pwd="";
                if (argsHM.get("user").toLowerCase().trim().equals("anonymous")) {
                    pwd="jer@sey.com";
                    argsHM.put("passw", pwd);
                }
                System.out.println(argsHM); 
                if (!ICFG.ipv.isValid(argsHM.get("listenip").trim()))  {
                    System.out.println("Wrong listen IP ! \nExit !"); 
                    Actions.useExamples();
                    return;
                }
                if (!Actions.checkTcpPort(argsHM.get("port").trim())) {
                    System.out.println("Port Wrong ! \nExit !"); 
                    Actions.useExamples();
                    return;
                }                
                try {
                    startServer(args, argsHM.get("port").trim(), argsHM.get("user").trim(), argsHM.get("passw").trim(), argsHM.get("folder").trim(), argsHM.get("listenip").trim());
                } catch (FtpException | FtpServerConfigurationException ex) {
                    java.util.logging.Logger.getLogger(PjFtpServer.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
                    System.out.println("\nNOT run !\nSome of parameters wrong !");
                    Actions.useExamples();                    
                }
            } catch (NullPointerException | ArrayIndexOutOfBoundsException ne) {
                System.out.println("NOT run !\nSome of parameters not given !");
                Actions.useExamples();
            }
        }
            //}
        //});
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    public static javax.swing.JButton btnAbout;
    public static javax.swing.JButton btnClearLog;
    private javax.swing.JButton btnQuit;
    public static javax.swing.JButton btnSelectFolder;
    public static javax.swing.JToggleButton btnToggleRunStop;
    public static javax.swing.JCheckBox checkBoxAnonymous;
    public static javax.swing.JComboBox<String> comboListenIP;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JPanel jPanel3;
    private javax.swing.JScrollPane jScrollPane2;
    private javax.swing.JToolBar.Separator jSeparator1;
    private javax.swing.JToolBar.Separator jSeparator10;
    private javax.swing.JToolBar.Separator jSeparator11;
    private javax.swing.JToolBar.Separator jSeparator12;
    private javax.swing.JToolBar.Separator jSeparator13;
    private javax.swing.JToolBar.Separator jSeparator2;
    private javax.swing.JToolBar.Separator jSeparator3;
    private javax.swing.JToolBar.Separator jSeparator4;
    private javax.swing.JToolBar.Separator jSeparator5;
    private javax.swing.JToolBar.Separator jSeparator6;
    private javax.swing.JToolBar.Separator jSeparator7;
    private javax.swing.JToolBar.Separator jSeparator8;
    private javax.swing.JToolBar.Separator jSeparator9;
    private javax.swing.JToolBar jToolBar1;
    private javax.swing.JToolBar jToolBar2;
    public static javax.swing.JTextArea taLog;
    public static javax.swing.JTextField tfFolder;
    public static javax.swing.JTextField tfPassw;
    public static javax.swing.JTextField tfPort;
    public static javax.swing.JTextField tfUser;
    // End of variables declaration//GEN-END:variables
}
