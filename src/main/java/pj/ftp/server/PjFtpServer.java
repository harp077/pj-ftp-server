package pj.ftp.server;

//import com.jgoodies.looks.plastic.PlasticLookAndFeel;
//import com.jgoodies.looks.plastic.theme.SkyGreen;
import java.awt.Cursor;
import java.awt.event.ItemEvent;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
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
import org.apache.ftpserver.ConnectionConfigFactory;
import org.apache.ftpserver.FtpServer;
import org.apache.ftpserver.FtpServerConfigurationException;
import org.apache.ftpserver.FtpServerFactory;
import org.apache.ftpserver.ftplet.Authority;
import org.apache.ftpserver.ftplet.FtpException;
import org.apache.ftpserver.ftplet.UserManager;
import org.apache.ftpserver.listener.ListenerFactory;
import org.apache.ftpserver.message.MessageResource;
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
    public static MessageResource mrLog;
    public static java.util.logging.Logger jul;
    public static org.apache.log4j.Logger j4log;
    public static PjFtpServer frame;
    public static int FW = 700;
    public static int FH = 400;
    //public static String ftpFolder;
    public static List<String> lookAndFeelsDisplay = new ArrayList<>();
    public static List<String> lookAndFeelsRealNames = new ArrayList<>();
    public static Map<String, String> argsHM = new HashMap<String, String>();
    public static Thread Log_Thread;
    public static String currentLAF = "de.muntjak.tinylookandfeel.TinyLookAndFeel";
    public static String zagolovok = " Pure Java FTP Server, v1.0.2, build 26-09-2020";

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
        this.setTitle(zagolovok);
        //this.btnSkin.setVisible(false);
        this.taLog.setCursor(Cursor.getDefaultCursor());
    }

    private synchronized static void startServer(String args[], String tcpPort, String login, String password, String folder) throws FtpException, FtpServerConfigurationException {
        ////////////File propertiesFile = new File("cfg/log4j.properties");
        ////////////PropertyConfigurator.configure(propertiesFile.toString());
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
        authorities.add(new WritePermission());
        authorities.add(new ConcurrentLoginPermission(MAX_CONCURRENT_LOGINS, MAX_CONCURRENT_LOGINS_PER_IP));
        authorities.add(new TransferRatePermission(Integer.MAX_VALUE, Integer.MAX_VALUE));
        user.setAuthorities(authorities);
        userManager.save(user);

        ListenerFactory listenerFactory = new ListenerFactory();
        listenerFactory.setPort(Integer.parseInt(tcpPort));

        FtpServerFactory factory = new FtpServerFactory();
        factory.setUserManager(userManager);
        factory.addListener("default", listenerFactory.createListener());

        ConnectionConfigFactory configFactory = new ConnectionConfigFactory();
        //configFactory.setAnonymousLoginEnabled(true);
        configFactory.setMaxThreads(4);
        factory.setConnectionConfig(configFactory.createConnectionConfig());
        mrLog = factory.getMessageResource();
        Map<String, String> hmLog = mrLog.getMessages("INFO");

        server = factory.createServer();
        server.start();
        //jul.log(Level.SEVERE, "oppanki");
        j4log.log(Level.INFO, "pj-ftp-server running");
        running = true;
        if (args.length == 0) {
            Log_Thread = new Log_Thread("log/app.log");
            try {
                Log_Thread.start();
            } catch (IllegalThreadStateException itse) {}
            frame.setTitle(zagolovok + ", server running");
        }
    }

    public static void MyInstLF(String lf) {
        //UIManager.installLookAndFeel(lf,lf);  
        lookAndFeelsDisplay.add(lf);
        lookAndFeelsRealNames.add(lf);
    }

    public void changeLF() {
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
    }

    public void setLF(JFrame frame) {
        try {
            UIManager.setLookAndFeel(currentLAF);
        } catch (InstantiationException | IllegalAccessException | UnsupportedLookAndFeelException | ClassNotFoundException ex) {
            Logger.getLogger(this.getName()).log(Level.ERROR, null, ex);
        }
        SwingUtilities.updateComponentTreeUI(frame);
        //frame.pack();
    }

    public static void InstallLF() {
        MyInstLF("javax.swing.plaf.metal.MetalLookAndFeel");
        MyInstLF("de.muntjak.tinylookandfeel.TinyLookAndFeel");
    }

    private void setBooleanBtnTf(Boolean sset) {
        tfUser.setEditable(sset);
        tfPassw.setEditable(sset);
        tfPort.setEditable(sset);
        tfFolder.setEditable(sset);
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
        jSeparator5 = new javax.swing.JToolBar.Separator();
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
        jSeparator3 = new javax.swing.JToolBar.Separator();
        btnToggleRunStop = new javax.swing.JToggleButton();
        jSeparator6 = new javax.swing.JToolBar.Separator();
        jPanel2 = new javax.swing.JPanel();
        jScrollPane2 = new javax.swing.JScrollPane();
        taLog = new javax.swing.JTextArea();
        jPanel3 = new javax.swing.JPanel();
        jToolBar2 = new javax.swing.JToolBar();
        btnAbout = new javax.swing.JButton();
        jSeparator11 = new javax.swing.JToolBar.Separator();
        btnSelectFolder = new javax.swing.JButton();
        jSeparator9 = new javax.swing.JToolBar.Separator();
        tfFolder = new javax.swing.JTextField();
        jSeparator10 = new javax.swing.JToolBar.Separator();
        btnQuit = new javax.swing.JButton();
        jSeparator7 = new javax.swing.JToolBar.Separator();

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
        jToolBar1.setRollover(true);
        jToolBar1.add(jSeparator5);

        jLabel1.setText("Port: ");
        jToolBar1.add(jLabel1);

        tfPort.setColumns(1);
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
        jToolBar1.add(jSeparator3);

        btnToggleRunStop.setIcon(new javax.swing.ImageIcon(getClass().getResource("/img/run-16.png"))); // NOI18N
        btnToggleRunStop.setText("Run server ");
        btnToggleRunStop.setFocusable(false);
        btnToggleRunStop.setHorizontalTextPosition(javax.swing.SwingConstants.RIGHT);
        btnToggleRunStop.addItemListener(new java.awt.event.ItemListener() {
            public void itemStateChanged(java.awt.event.ItemEvent evt) {
                btnToggleRunStopItemStateChanged(evt);
            }
        });
        jToolBar1.add(btnToggleRunStop);
        jToolBar1.add(jSeparator6);

        jPanel1.add(jToolBar1, java.awt.BorderLayout.CENTER);

        getContentPane().add(jPanel1, java.awt.BorderLayout.PAGE_START);

        jPanel2.setBorder(javax.swing.BorderFactory.createTitledBorder("Output"));
        jPanel2.setLayout(new java.awt.BorderLayout());

        taLog.setColumns(20);
        taLog.setRows(5);
        jScrollPane2.setViewportView(taLog);

        jPanel2.add(jScrollPane2, java.awt.BorderLayout.CENTER);

        getContentPane().add(jPanel2, java.awt.BorderLayout.CENTER);

        jPanel3.setBorder(javax.swing.BorderFactory.createTitledBorder("parameters and actions"));
        jPanel3.setLayout(new java.awt.BorderLayout());

        jToolBar2.setFloatable(false);
        jToolBar2.setRollover(true);

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
        jToolBar2.add(jSeparator11);

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
        jToolBar2.add(jSeparator9);

        tfFolder.setText("/tmp");
        jToolBar2.add(tfFolder);
        jToolBar2.add(jSeparator10);

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
        jToolBar2.add(jSeparator7);

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
        if (!StringUtils.isNumeric(tfPort.getText()) || tfUser.getText().isEmpty() || tfPassw.getText().isEmpty() || tfFolder.getText().isEmpty()) {
            JOptionPane.showMessageDialog(frame, "Some wrong parameters !", "Error", JOptionPane.ERROR_MESSAGE);
            btnToggleRunStop.setSelected(false);
            return;
        }
        ImageIcon iconOn = new ImageIcon(getClass().getResource("/img/run-16.png"));
        ImageIcon iconOf = new ImageIcon(getClass().getResource("/img/stop-16.png"));
        if (evt.getStateChange() == ItemEvent.DESELECTED) {
            if (running == true) {
                server.stop();
                btnToggleRunStop.setIcon(iconOn);
                btnToggleRunStop.setText("Run server");
                setBooleanBtnTf(true);
                frame.setTitle(zagolovok + ", server stop");
                j4log.log(Level.INFO, "pj-ftp-server stop");
                return;
            }
        }
        if (evt.getStateChange() == ItemEvent.SELECTED) {
            try {
                startServer(new String[0], tfPort.getText().trim(), tfUser.getText().trim(), tfPassw.getText().trim(), tfFolder.getText().trim());
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
        String msg = " PJ-FTP-SERVER: "
                + "\n Pure Java FTP server. "
                + "\n Create by Roman Koldaev, "
                + "\n Saratov city, Russia. "
                + "\n mail: harp07@mail.ru "
                + "\n SourceForge: https://sf.net/u/harp07/profile/ "
                + "\n GitHub: https://github.com/harp077/ "
                + "\n Need JRE-1.8."; 
        ImageIcon icone = new ImageIcon(getClass().getResource("/img/logo/ftp-green-logo-128.png"));
        JOptionPane.showMessageDialog(frame, msg, "About", JOptionPane.INFORMATION_MESSAGE, icone);
    }//GEN-LAST:event_btnAboutActionPerformed

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
        java.awt.EventQueue.invokeLater(new Runnable() {
            public void run() {
                if (args.length == 0) {
                    frame = new PjFtpServer();
                    frame.InstallLF();
                    frame.setLF(frame);
                    frame.getRootPane().setWindowDecorationStyle(JRootPane.FRAME);
                    JFrame.setDefaultLookAndFeelDecorated(true);
                    JDialog.setDefaultLookAndFeelDecorated(true);
                    JOptionPane.setRootFrame(frame);
                    frame.setSize(FW, FH);
                    frame.setLocation(200, 200);
                    frame.setResizable(true);
                    frame.setVisible(true);
                }
                if (args.length > 0) {
                    Arrays.stream(args)
                        .forEach(x -> { argsHM.put(x.split("=")[0].toString(), x.split("=")[1].toString()); });
                    System.out.println(argsHM);
                    String pwd="";
                    if (argsHM.get("user").toLowerCase().trim().equals("anonymous")) {
                        pwd="jer@sey.com";
                        argsHM.put("passw", pwd);
                    }
                    System.out.println(argsHM);                    
                    try {
                        startServer(args, argsHM.get("port").trim(), argsHM.get("user").trim(), argsHM.get("passw").trim(), argsHM.get("folder").trim());
                    } catch (FtpException | FtpServerConfigurationException ex) {
                        java.util.logging.Logger.getLogger(PjFtpServer.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
                    }
                }
            }
        });
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton btnAbout;
    private javax.swing.JButton btnQuit;
    public static javax.swing.JButton btnSelectFolder;
    public static javax.swing.JToggleButton btnToggleRunStop;
    public static javax.swing.JCheckBox checkBoxAnonymous;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JPanel jPanel3;
    private javax.swing.JScrollPane jScrollPane2;
    private javax.swing.JToolBar.Separator jSeparator1;
    private javax.swing.JToolBar.Separator jSeparator10;
    private javax.swing.JToolBar.Separator jSeparator11;
    private javax.swing.JToolBar.Separator jSeparator2;
    private javax.swing.JToolBar.Separator jSeparator3;
    private javax.swing.JToolBar.Separator jSeparator4;
    private javax.swing.JToolBar.Separator jSeparator5;
    private javax.swing.JToolBar.Separator jSeparator6;
    private javax.swing.JToolBar.Separator jSeparator7;
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
