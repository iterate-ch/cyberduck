package ch.cyberduck.ui;

/*
 *  ch.cyberduck.ui.PreferencesPanel.java
 *  Cyberduck
 *
 *  $Header$
 *  $Revision$
 *  $Date$
 *
 *  Copyright (c) 2003 David Kocher. All rights reserved.
 *  http://icu.unizh.ch/~dkocher/
 *
 *  This program is free software; you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation; either version 2 of the License, or
 *  (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  Bug fixes, suggestions and comments should be sent to:
 *  dkocher@cyberduck.ch
 */

import javax.swing.*;
import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;

import ch.cyberduck.Cyberduck;
import ch.cyberduck.Preferences;
import ch.cyberduck.ui.action.ActionMap;
import ch.cyberduck.ui.common.DummyVerifier;
import ch.cyberduck.ui.common.GUIFactory;
import ch.cyberduck.ui.common.IntegerVerifier;
import ch.cyberduck.ui.common.PathVerifier;
import ch.cyberduck.ui.layout.ParagraphLayout;

/**
* A panel with tabbed panes showing all user @link{Preferences}
 * @see ch.cyberduck.Preferences
 * @version $Id$
 */
public class PreferencesPanel extends JPanel {//implements ItemListener {

    public PreferencesPanel() {
        Cyberduck.DEBUG("[PreferencesPanel]");
        this.init();
    }

    /**
     * Initialize the graphical user interface
     */
    private void init() {
        this.setLayout(new BorderLayout());
        JTabbedPane mainPane = new JTabbedPane();
        // tabs
        JPanel tab1 = GUIFactory.panelBuilder(BoxLayout.Y_AXIS);
        tab1.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
        JPanel tab2 = GUIFactory.panelBuilder(BoxLayout.Y_AXIS);
        tab2.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
        JPanel tab3 = GUIFactory.panelBuilder(BoxLayout.Y_AXIS);
        tab3.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
        JPanel tab4 = GUIFactory.panelBuilder(BoxLayout.Y_AXIS);
        tab4.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
        JPanel tab5 = GUIFactory.panelBuilder(BoxLayout.Y_AXIS);
        tab5.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));

//        JPanel generalBorder = GUIFactory.panelBuilder(BorderFactory.createTitledBorder("General"));
        JPanel interfaceBorder = GUIFactory.panelBuilder(BorderFactory.createTitledBorder("Interface"));// (Needs relaunch)"));
        //JPanel lafBorder = GUIFactory.panelBuilder(BorderFactory.createTitledBorder("Look and Feel"));
        JPanel duplicateBorder = GUIFactory.panelBuilder(BorderFactory.createTitledBorder("Duplicate files"));
        JPanel pathBorder = GUIFactory.panelBuilder(BorderFactory.createTitledBorder("Download Path"));
        JPanel postProcessBorder = GUIFactory.panelBuilder(BorderFactory.createTitledBorder("Post Process (Mac only)"));
//        JPanel statusBorder = GUIFactory.panelBuilder(BorderFactory.createTitledBorder("Status Display"));
        JPanel loginBorder = GUIFactory.panelBuilder(BorderFactory.createTitledBorder("Login"));
        JPanel connectmodeBorder = GUIFactory.panelBuilder(BorderFactory.createTitledBorder("Connect Mode"));
//        JPanel transfermodeBorder = GUIFactory.panelBuilder(BorderFactory.createTitledBorder("Default Transfer Mode"));
        JPanel timeoutBorder = GUIFactory.panelBuilder(BorderFactory.createTitledBorder("Timeout"));
        //JPanel portBorder = GUIFactory.panelBuilder(BorderFactory.createTitledBorder("Default Port"));
        JPanel defaultProtocolBorder = GUIFactory.panelBuilder(BorderFactory.createTitledBorder("Default Protocol"));
        JPanel fontBorder = GUIFactory.panelBuilder(BorderFactory.createTitledBorder("Fonts"));
        JPanel bufferBorder = GUIFactory.panelBuilder(BorderFactory.createTitledBorder("Buffer Size"));
        //JPanel asciiTransfersBorder = GUIFactory.panelBuilder(BorderFactory.createTitledBorder("ASCII Transfers"));
        JPanel soundBorder = GUIFactory.panelBuilder(BorderFactory.createTitledBorder("Sound"));
        JPanel listingBorder = GUIFactory.panelBuilder(BorderFactory.createTitledBorder("Directory Listing (Needs relaunch)"));
        JPanel proxyBorder = GUIFactory.panelBuilder(BorderFactory.createTitledBorder("Proxy"));

        duplicateBorder.add(new DuplicatePanel());
        pathBorder.add(new PathPanel());
        postProcessBorder.add(new PostProcessPanel());
        //statusBorder.add(new StatusPanel());
        loginBorder.add(new LoginPanel());
        connectmodeBorder.add(new ConnectModePanel());
//        transfermodeBorder.add(new TransferModePanel());
        timeoutBorder.add(new TimeoutPanel());
        //portBorder.add(new PortPanel());
        defaultProtocolBorder.add(new ProtocolPanel());
        fontBorder.add(new FontPanel());
        bufferBorder.add(new BufferPanel());
        interfaceBorder.add(new InterfacePanel());
        //lafBorder.add(new LafPanel());
        soundBorder.add(new SoundPanel());
        listingBorder.add(new ListingPanel());
        proxyBorder.add(new ProxyPanel());

        // tab 1 // general
        tab1.add(pathBorder);
        tab1.add(postProcessBorder);
        tab1.add(Box.createVerticalGlue());

        tab2.add(interfaceBorder);
        //tab2.add(statusBorder);
        tab2.add(soundBorder);
        tab2.add(fontBorder);
        //tab2.add(lafBorder);
        tab2.add(Box.createVerticalGlue());

        tab3.add(duplicateBorder);
        tab3.add(defaultProtocolBorder);
//        tab3.add(transfermodeBorder);
        tab3.add(Box.createVerticalGlue());

        tab5.add(bufferBorder);
        tab5.add(timeoutBorder);
        tab5.add(proxyBorder);
        tab5.add(Box.createVerticalGlue());

        tab4.add(loginBorder);
        tab4.add(connectmodeBorder);
        tab4.add(listingBorder);
        tab4.add(Box.createVerticalGlue());

        mainPane.addTab("General", tab1);
        mainPane.addTab("Look & Feel", tab2);
        mainPane.addTab("Connection", tab3);
        mainPane.addTab("Ftp", tab4);
        mainPane.addTab("Advanced", tab5);

        this.add(mainPane, BorderLayout.CENTER);
        JPanel bottomPanel = new JPanel();
        bottomPanel.setLayout(new FlowLayout(FlowLayout.CENTER));
        bottomPanel.add(GUIFactory.labelBuilder("Changes will take effect immediatly.", GUIFactory.FONT_SMALL));
        this.add(bottomPanel, BorderLayout.SOUTH);
}
        /*
        private class GeneralPanel extends JPanel {
            public GeneralPanel() {
                super();
                this.setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
                this.add(saveTableEntriesCheckbox = GUIFactory.checkboxBuilder("Save bookmarks", GUIFactory.FONT_SMALL, Preferences.instance().getProperty("table.save"), this));
                this.add(logConnectionCheckbox = GUIFactory.checkboxBuilder("Log connections", GUIFactory.FONT_SMALL, Preferences.instance().getProperty("connection.log"), this));
                this.add(encodeFilesCheckbox = GUIFactory.checkboxBuilder("Encode/Decode filenames", GUIFactory.FONT_SMALL, Preferences.instance().getProperty("files.encode"), this));
                this.add(speechCheckbox = GUIFactory.checkboxBuilder("Enable speech synthesis", GUIFactory.FONT_SMALL, Preferences.instance().getProperty("connection.log.speech"), this));
                speechCheckbox.setEnabled(false);
            }
        }
         */

    private class SoundPanel extends JPanel {
        public SoundPanel() {
            super();
            this.setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
            this.add(GUIFactory.checkboxBuilder("Play sound when opening connection", GUIFactory.FONT_SMALL, Preferences.instance().getProperty("status.sound.start"), new ItemListener() {
                public void itemStateChanged(ItemEvent e) {
                    Preferences.instance().setProperty("status.sound.start", e.getStateChange() == ItemEvent.SELECTED);
                }
            })
                     );

            this.add(GUIFactory.checkboxBuilder("Play sound when connection is closed", GUIFactory.FONT_SMALL, Preferences.instance().getProperty("status.sound.stop"), new ItemListener() {
                public void itemStateChanged(ItemEvent e) {
                    Preferences.instance().setProperty("status.sound.stop", e.getStateChange() == ItemEvent.SELECTED);
                }
            })
                     );

            this.add(GUIFactory.checkboxBuilder("Play sound when transfer is complete", GUIFactory.FONT_SMALL, Preferences.instance().getProperty("status.sound.complete"), new ItemListener() {
                public void itemStateChanged(ItemEvent e) {
                    Preferences.instance().setProperty("status.sound.complete", e.getStateChange() == ItemEvent.SELECTED);
                }
            })
                     );

        }
    }

    /*
     private class LafPanel extends JPanel {
         public LafPanel() {
             super();
             this.setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
             JComboBox lafComboBox = new JComboBox(UIManager.getInstalledLookAndFeels());
             lafComboBox.setSelected(Preferences.instance().getProperty("laf.default"));
             lafComboBox.addItemListener(new ItemListener() {
                 public void itemStateChanged(ItemEvent ie) {
                     Cyberduck.DEBUG(ie.paramString());
                     if(ie.getStateChange() == ItemEvent.SELECTED) {
                         try {
                             String lafClassName = ((UIManager.LookAndFeelInfo)ie.getItem()).getClassName();
                             Preferences.instance().setProperty("laf.default", lafClassName);
                             //UIManager.setLookAndFeel(lafClassName);
                             //SwingUtilities.updateComponentTreeUI(frame);
                         }
                         catch(Exception ex) {
                             ex.printStackTrace();
                         }
                     }
                 }
             }
                                         );
             this.add(lafComboBox);
         }
     }
     */

    private class InterfacePanel extends JPanel {
        public InterfacePanel() {
            super();
            this.setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
            ButtonGroup windowModeGroup = new ButtonGroup();
            JPanel windowModePanel = GUIFactory.panelBuilder(new java.awt.GridLayout(2, 2));
            boolean multiplewindow = (Preferences.instance().getProperty("interface.multiplewindow")).equals("true");
            //        windowModePanel.add(GUIFactory.labelBuilder(Cyberduck.getIcon(Cyberduck.getResource(this.getClass(), "singlewindow.jpg"))));
            //      windowModePanel.add(GUIFactory.labelBuilder(Cyberduck.getIcon(Cyberduck.getResource(this.getClass(), "multiplewindow.jpg"))));
            this.add(GUIFactory.radiobuttonBuilder(windowModeGroup, "Single Window Mode", GUIFactory.FONT_SMALL, !multiplewindow, new ItemListener() {
                public void itemStateChanged(ItemEvent e) {
                    Preferences.instance().setProperty("interface.multiplewindow", !(e.getStateChange() == ItemEvent.SELECTED));
                    if(e.getStateChange() == ItemEvent.SELECTED)
                        ((Action)ActionMap.instance().get("Reconfigure Interface")).actionPerformed(new ActionEvent(this, ActionEvent.ACTION_PERFORMED, e.paramString()));

                }
            })
                     );
            this.add(GUIFactory.radiobuttonBuilder(windowModeGroup, "Multiple Window Mode", GUIFactory.FONT_SMALL, multiplewindow, new ItemListener() {
                public void itemStateChanged(ItemEvent e) {
                    Preferences.instance().setProperty("interface.multiplewindow", e.getStateChange() == ItemEvent.SELECTED);
                    if(e.getStateChange() == ItemEvent.SELECTED)
                        ((Action)ActionMap.instance().get("Reconfigure Interface")).actionPerformed(new ActionEvent(this, ActionEvent.ACTION_PERFORMED, e.paramString()));
                }
            })
                     );
            this.add(windowModePanel);
            this.add(Box.createVerticalStrut(10));
            this.add(GUIFactory.checkboxBuilder("Show error dialogs", GUIFactory.FONT_SMALL, Preferences.instance().getProperty("interface.error-dialog"), new ItemListener() {
                public void itemStateChanged(ItemEvent e) {
                    Preferences.instance().setProperty("interface.error-dialog", e.getStateChange() == ItemEvent.SELECTED);
                }
            })
                     );
        }
    }

    private class FontPanel extends JPanel {
        public FontPanel() {
            super();
            this.setLayout(new FlowLayout(FlowLayout.LEFT));
            this.add(GUIFactory.labelBuilder("Small font size: ", GUIFactory.FONT_SMALL));
            String[] fontsizes = {"8", "9", "10", "11", "12", "13", "14"};
            final JComboBox smallfontCombo;
            this.add(smallfontCombo = GUIFactory.comboBuilder(fontsizes, Preferences.instance().getProperty("font.small"), GUIFactory.FONT_SMALL));
            smallfontCombo.addItemListener(new ItemListener() {
                public void itemStateChanged(ItemEvent e) {
                    Preferences.instance().setProperty("font.small", (String)smallfontCombo.getSelectedItem());
                }
            });

            this.add(GUIFactory.labelBuilder("Normal font size: ", GUIFactory.FONT_SMALL));
            final JComboBox normalfontCombo;
            this.add(normalfontCombo = GUIFactory.comboBuilder(fontsizes, Preferences.instance().getProperty("font.normal"), GUIFactory.FONT_SMALL));
            normalfontCombo.addItemListener(new ItemListener() {
                public void itemStateChanged(ItemEvent e) {
                    if(e.getStateChange() == ItemEvent.SELECTED)
                        Preferences.instance().setProperty("font.normal", (String)normalfontCombo.getSelectedItem());
                }
            });
        }
    }

    private class DuplicatePanel extends JPanel {
        public DuplicatePanel() {
            super();
            this.setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
            ButtonGroup filenameRadioGroup = new ButtonGroup();
            this.add(GUIFactory.radiobuttonBuilder(filenameRadioGroup, "Ask me what to do", GUIFactory.FONT_SMALL, Preferences.instance().getProperty("duplicate.ask"), new ItemListener() {
                public void itemStateChanged(ItemEvent e) {
                    Preferences.instance().setProperty("duplicate.ask", e.getStateChange() == ItemEvent.SELECTED);
                }
            })
                     );

            this.add(GUIFactory.radiobuttonBuilder(filenameRadioGroup, "Overwrite existing file", GUIFactory.FONT_SMALL,  Preferences.instance().getProperty("duplicate.overwrite"), new ItemListener() {
                public void itemStateChanged(ItemEvent e) {
                    Preferences.instance().setProperty("duplicate.overwrite", e.getStateChange() == ItemEvent.SELECTED);
                }
            })
                     );
            this.add(GUIFactory.radiobuttonBuilder(filenameRadioGroup, "Use new but similar name", GUIFactory.FONT_SMALL, Preferences.instance().getProperty("duplicate.similar"), new ItemListener() {
                public void itemStateChanged(ItemEvent e) {
                    Preferences.instance().setProperty("duplicate.similar", e.getStateChange() == ItemEvent.SELECTED);
                }
            })
                     );
            this.add(GUIFactory.radiobuttonBuilder(filenameRadioGroup, "Resume transfer if possible", GUIFactory.FONT_SMALL, Preferences.instance().getProperty("duplicate.resume"), new ItemListener() {
                public void itemStateChanged(ItemEvent e) {
                    Preferences.instance().setProperty("duplicate.resume", e.getStateChange() == ItemEvent.SELECTED);
                }
            })
                     );
        }
    }

    /*
     private class StatusPanel extends JPanel {
         public StatusPanel() {
             super();
             this.setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
             this.add(showLocalPathCheckbox = GUIFactory.checkboxBuilder("Show local path", GUIFactory.FONT_SMALL, Preferences.instance().getProperty("statuspanel.localpath"), this));
             this.add(showTranscriptCheckbox = GUIFactory.checkboxBuilder("Show transcript messages", GUIFactory.FONT_SMALL, Preferences.instance().getProperty("statuspanel.transcriptmessage"), this));
             this.add(showProgressCheckbox = GUIFactory.checkboxBuilder("Show progress messages", GUIFactory.FONT_SMALL, Preferences.instance().getProperty("statuspanel.progressmessage"), this));
             this.add(showStatusCheckbox = GUIFactory.checkboxBuilder("Show status messages", GUIFactory.FONT_SMALL, Preferences.instance().getProperty("statuspanel.statusmessage"), this));
             this.add(showTranscriptCheckbox = GUIFactory.checkboxBuilder("Show transcript messages", GUIFactory.FONT_SMALL, Preferences.instance().getProperty("statuspanel.transcriptmessage"), this));
         }
     }
     */

    private class ProtocolPanel extends JPanel {
        public ProtocolPanel() {
            super();
            this.setLayout(new FlowLayout(FlowLayout.LEFT));
            ButtonGroup protocolRadioGroup = new ButtonGroup();
            this.add(GUIFactory.radiobuttonBuilder(protocolRadioGroup, "SFTP", GUIFactory.FONT_SMALL, Preferences.instance().getProperty("connection.protocol.default").equals("sftp"), new ItemListener() {
                public void itemStateChanged(ItemEvent e) {
                    if(e.getStateChange() == ItemEvent.SELECTED)
                        Preferences.instance().setProperty("connection.protocol.default", "sftp");
                }
            })
                     );
            this.add(GUIFactory.radiobuttonBuilder(protocolRadioGroup, "HTTP", GUIFactory.FONT_SMALL, Preferences.instance().getProperty("connection.protocol.default").equals("http"), new ItemListener() {
                public void itemStateChanged(ItemEvent e) {
                    if(e.getStateChange() == ItemEvent.SELECTED)
                        Preferences.instance().setProperty("connection.protocol.default", "http");
                }
            })
                     );
            this.add(GUIFactory.radiobuttonBuilder(protocolRadioGroup, "FTP", GUIFactory.FONT_SMALL, Preferences.instance().getProperty("connection.protocol.default").equals("ftp"), new ItemListener() {
                public void itemStateChanged(ItemEvent e) {
                    if(e.getStateChange() == ItemEvent.SELECTED)
                        Preferences.instance().setProperty("connection.protocol.default", "ftp");
                }
            })
                     );
        }
    }

    private class ProxyPanel extends JPanel {
        public ProxyPanel() {
            super();
            this.setLayout(new ParagraphLayout());
            this.add(GUIFactory.checkboxBuilder("Use proxy", GUIFactory.FONT_SMALL, Preferences.instance().getProperty("connection.proxy"), new ItemListener() {
                public void itemStateChanged(ItemEvent e) {
                    Preferences.instance().setProperty("connection.proxy", e.getStateChange() == ItemEvent.SELECTED);
                }
            }), ParagraphLayout.NEW_PARAGRAPH);
            this.add(GUIFactory.textFieldBuilder(Preferences.instance().getProperty("connection.proxy.host"), GUIFactory.FONT_SMALL, 20, new DummyVerifier("connection.proxy.host")));
            this.add(GUIFactory.labelBuilder("Host", GUIFactory.FONT_SMALL));
            this.add(GUIFactory.textFieldBuilder(Preferences.instance().getProperty("connection.proxy.port"), GUIFactory.FONT_SMALL, 20, new IntegerVerifier("connection.proxy.port")), ParagraphLayout.NEW_LINE);
            this.add(GUIFactory.labelBuilder("Port", GUIFactory.FONT_SMALL));
            this.add(GUIFactory.checkboxBuilder("Authentication", GUIFactory.FONT_SMALL, Preferences.instance().getProperty("connection.proxy.authenticate"), new ItemListener() {
                public void itemStateChanged(ItemEvent e) {
                    Preferences.instance().setProperty("connection.proxy.authenticate", e.getStateChange() == ItemEvent.SELECTED);
                }
            }), ParagraphLayout.NEW_PARAGRAPH);
            this.add(GUIFactory.textFieldBuilder(Preferences.instance().getProperty("connection.proxy.username"), GUIFactory.FONT_SMALL, 20, new DummyVerifier("connection.proxy.username")));
            this.add(GUIFactory.labelBuilder("User", GUIFactory.FONT_SMALL));
            this.add(GUIFactory.textFieldBuilder(Preferences.instance().getProperty("connection.proxy.password"), GUIFactory.FONT_SMALL, 20, new DummyVerifier("connection.proxy.password")), ParagraphLayout.NEW_LINE);
            this.add(GUIFactory.labelBuilder("Password", GUIFactory.FONT_SMALL));
        }
    }

    private class TimeoutPanel extends JPanel {
        public TimeoutPanel() {
            super();
            this.setLayout(new FlowLayout(FlowLayout.LEFT));
            final JTextField timeoutField = GUIFactory.textFieldBuilder(Preferences.instance().getProperty("connection.timeout"), GUIFactory.FONT_SMALL, new IntegerVerifier("connection.timeout"));
            this.add(GUIFactory.labelBuilder("Timeout after ", GUIFactory.FONT_SMALL));
            this.add(timeoutField);
            this.add(GUIFactory.labelBuilder(" minutes.", GUIFactory.FONT_SMALL));
            JButton defaultTimeoutButton = GUIFactory.buttonBuilder("Default", GUIFactory.FONT_SMALL);
            defaultTimeoutButton.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    timeoutField.setText(Preferences.instance().getProperty("connection.timeout.default"));
                }
            });
            this.add(defaultTimeoutButton);
        }
    }
    
    private class BufferPanel extends JPanel {
        public BufferPanel() {
            super();
            this.setLayout(new FlowLayout(FlowLayout.LEFT));
            String[] buffersizes = {"1024", "2048", "4096", "8192"};
            final JComboBox bufferSizeCombo;
            this.add(bufferSizeCombo = GUIFactory.comboBuilder(buffersizes, Preferences.instance().getProperty("connection.buffer"), GUIFactory.FONT_SMALL));
            bufferSizeCombo.setEditable(true);
            bufferSizeCombo.setInputVerifier(new IntegerVerifier("connection.buffer"));
            bufferSizeCombo.addItemListener(new ItemListener() {
                public void itemStateChanged(ItemEvent e) {
                    Preferences.instance().setProperty("connection.buffer", (String)bufferSizeCombo.getSelectedItem());
                }
            }
                                            );
        }
    }

    private class TransferModePanel extends JPanel {
        public TransferModePanel() {
            super();
            this.setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
            ButtonGroup downloadModeGroup = new ButtonGroup();
            //transfermodePanel.add(autoRadio = GUIFactory.radiobuttonBuilder(downloadModeGroup, "Auto", GUIFactory.FONT_SMALL, Preferences.instance().getProperty("connection.transfertype.default").equals("auto"), this));
            this.add(GUIFactory.radiobuttonBuilder(downloadModeGroup, "Binary", GUIFactory.FONT_SMALL, Preferences.instance().getProperty("connection.transfertype.default").equals("binary"), new ItemListener() {
                public void itemStateChanged(ItemEvent e) {
                    if(e.getStateChange() == ItemEvent.SELECTED)
                        Preferences.instance().setProperty("connection.transfertype.default", "binary");
                }
            })
                     );
            this.add(GUIFactory.radiobuttonBuilder(downloadModeGroup, "ASCII", GUIFactory.FONT_SMALL, Preferences.instance().getProperty("connection.transfertype.default").equals("ascii"), new ItemListener() {
                public void itemStateChanged(ItemEvent e) {
                    if(e.getStateChange() == ItemEvent.SELECTED)
                        Preferences.instance().setProperty("connection.transfertype.default", "ascii");
                }
            })
                     );

        }
    }

    private class PathPanel extends JPanel {
        public PathPanel() {
            super();
            this.setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
            JLabel errorLabel = GUIFactory.labelBuilder("The path does not exist.", GUIFactory.FONT_SMALL);
            errorLabel.setVisible(false);
            final JTextField pathField = GUIFactory.textFieldBuilder(Preferences.instance().getProperty("download.path"), GUIFactory.FONT_SMALL, new PathVerifier(errorLabel, "download.path"));
            //        pathField.selectAll();
            //        pathField.setColumns(15);
            JButton defaultDownloadLocation = GUIFactory.buttonBuilder("Default", GUIFactory.FONT_SMALL, new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    pathField.setText(System.getProperty("user.dir") + "/");
                    Preferences.instance().setProperty("download.path", pathField.getText() + "/");
                }
            }
                                                                       );
            JButton selectDownloadLocation = GUIFactory.buttonBuilder("Select...", GUIFactory.FONT_SMALL, new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    java.awt.FileDialog chooser = new java.awt.FileDialog(new java.awt.Frame(), "Select Download Location", java.awt.FileDialog.LOAD);
                    chooser.setTitle("Select Download Location:");
                    chooser.setDirectory(System.getProperty("user.home"));
                    chooser.setVisible(true);
                    String resultPath = chooser.getDirectory();
                    if(resultPath != null) {
                        java.io.File file = new java.io.File(resultPath);
                        if(file != null) {
                            pathField.setText(file.getPath());
                            Preferences.instance().setProperty("download.path", pathField.getText());
                        }
                    }
                }
            }
                                                                      );
            JPanel path1 = new JPanel();
            path1.setLayout(new FlowLayout(FlowLayout.LEFT));
            path1.add(pathField);
            path1.add(defaultDownloadLocation);
            path1.add(selectDownloadLocation);
            JPanel path2 = new JPanel();
            path2.setLayout(new FlowLayout(FlowLayout.LEFT));
            path2.add(errorLabel);
            this.add(path1);
            this.add(path2);
        }
    }

    private class PostProcessPanel extends JPanel {
        public PostProcessPanel() {
            super();
            this.setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
            this.add(GUIFactory.checkboxBuilder("Post-process files after downloading", GUIFactory.FONT_SMALL, Preferences.instance().getProperty("files.postprocess"), new ItemListener() {
                public void itemStateChanged(ItemEvent e) {
                    Preferences.instance().setProperty("files.postprocess", e.getStateChange() == ItemEvent.SELECTED);
                }
            })
                     );
        }
    }

    private class LoginPanel extends JPanel {
        public LoginPanel() {
            super();
            this.setLayout(new FlowLayout(FlowLayout.LEFT));
            this.add(GUIFactory.labelBuilder("Email for anonymous login: ", GUIFactory.FONT_SMALL));
            //        this.add(Box.createVerticalStrut(5));
            this.add(GUIFactory.textFieldBuilder(Preferences.instance().getProperty("ftp.login.anonymous.pass"), GUIFactory.FONT_SMALL, new DummyVerifier("ftp.login.anonymous.pass")));
        }
    }

    private class ConnectModePanel extends JPanel {
        public ConnectModePanel() {
            super();
            this.setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
            ButtonGroup connectModeGroup = new ButtonGroup();
            this.add(GUIFactory.radiobuttonBuilder(connectModeGroup, "Passive (PASV)", GUIFactory.FONT_SMALL, Preferences.instance().getProperty("ftp.passive"),new ItemListener() {
                public void itemStateChanged(ItemEvent e) {
                    Preferences.instance().setProperty("ftp.passive", e.getStateChange() == ItemEvent.SELECTED);
                }
            })
                     );
            this.add(GUIFactory.radiobuttonBuilder(connectModeGroup, "Active", GUIFactory.FONT_SMALL, Preferences.instance().getProperty("ftp.active"), new ItemListener() {
                public void itemStateChanged(ItemEvent e) {
                    Preferences.instance().setProperty("ftp.active", e.getStateChange() == ItemEvent.SELECTED);
                }
            })
                     );
        }
    }

    private class ListingPanel extends JPanel {
        public ListingPanel() {
            super();
            this.setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
            this.add(GUIFactory.checkboxBuilder("Show files beginning with '.'", GUIFactory.FONT_SMALL, Preferences.instance().getProperty("ftp.showHidden"), new ItemListener() {
                public void itemStateChanged(ItemEvent e) {
                    Preferences.instance().setProperty("ftp.showHidden", e.getStateChange() == ItemEvent.SELECTED);
                }
            })
                     );

            this.add(Box.createVerticalStrut(10));
            this.add(GUIFactory.labelBuilder("Show the following columns in listings:", GUIFactory.FONT_SMALL));
            this.add(Box.createVerticalStrut(5));
            this.add(GUIFactory.checkboxBuilder("Icon (Folder or File)", GUIFactory.FONT_SMALL, Preferences.instance().getProperty("ftp.listing.showType"), new ItemListener() {
                public void itemStateChanged(ItemEvent e) {
                    Preferences.instance().setProperty("ftp.listing.showType", e.getStateChange() == ItemEvent.SELECTED);
                }
            })
                     );

            this.add(GUIFactory.checkboxBuilder("Filenames", GUIFactory.FONT_SMALL, Preferences.instance().getProperty("ftp.listing.showFilenames"), new ItemListener() {
                public void itemStateChanged(ItemEvent e) {
                    Preferences.instance().setProperty("ftp.listing.showFilenames", e.getStateChange() == ItemEvent.SELECTED);
                }
            })
                     );

            this.add(GUIFactory.checkboxBuilder("Size", GUIFactory.FONT_SMALL, Preferences.instance().getProperty("ftp.listing.showSize"), new ItemListener() {
                public void itemStateChanged(ItemEvent e) {
                    Preferences.instance().setProperty("ftp.listing.showSize", e.getStateChange() == ItemEvent.SELECTED);
                }
            })
                     );

            this.add(GUIFactory.checkboxBuilder("Modification Date", GUIFactory.FONT_SMALL, Preferences.instance().getProperty("ftp.listing.showDate"), new ItemListener() {
                public void itemStateChanged(ItemEvent e) {
                    Preferences.instance().setProperty("ftp.listing.showDate", e.getStateChange() == ItemEvent.SELECTED);
                }
            })
                     );

            this.add(GUIFactory.checkboxBuilder("Owner", GUIFactory.FONT_SMALL, Preferences.instance().getProperty("ftp.listing.showOwner"), new ItemListener() {
                public void itemStateChanged(ItemEvent e) {
                    Preferences.instance().setProperty("ftp.listing.showOwner", e.getStateChange() == ItemEvent.SELECTED);
                }
            })
                     );            
            this.add(GUIFactory.checkboxBuilder("Access rights", GUIFactory.FONT_SMALL, Preferences.instance().getProperty("ftp.listing.showAccess"), new ItemListener() {
                public void itemStateChanged(ItemEvent e) {
                    Preferences.instance().setProperty("ftp.listing.showAccess", e.getStateChange() == ItemEvent.SELECTED);
                }
            })
                     );
        }
    }

    /*
     private class PortPanel extends JPanel {
         public PortPanel() {
             super();
             this.setLayout(new FlowLayout(FlowLayout.LEFT));
             this.add(GUIFactory.labelBuilder("Connect to port number: ", GUIFactory.FONT_SMALL));
             this.add(GUIFactory.textFieldBuilder(Preferences.instance().getProperty("ftp.port.default"), GUIFactory.FONT_SMALL, new IntegerVerifier("ftp.port.default")));
         }
     }
     */
}
    
    // ************************************************************************************************************
/*
    public void itemStateChanged(ItemEvent event) {
//        Cyberduck.DEBUG("[PreferencesPanel] itemStateChanged()");
        Object source = event.getSource();
        boolean selected = (event.getStateChange() == ItemEvent.SELECTED);
        if(source instanceof JRadioButton) {
            if (source == passivemodeRadio)
                Preferences.instance().setProperty("ftp.passive", selected);
            else if (source == activemodeRadio)
                Preferences.instance().setProperty("ftp.active", selected);
        */
            /*
            else if (source == autoRadio)
                Preferences.instance().setProperty("connection.transfertype.default", "auto");
             */
            /*
            else if (source == asciiRadio)
                Preferences.instance().setProperty("connection.transfertype.default", "ascii");
            else if (source == binaryRadio)
                Preferences.instance().setProperty("connection.transfertype.default", "binary");
             */
            /*
            else if (source == askMeRadio)
                Preferences.instance().setProperty("duplicate.ask", selected);
            else if (source == overwriteRadio)
                Preferences.instance().setProperty("duplicate.overwrite", selected);
            else if (source == similarNameRadio)
                Preferences.instance().setProperty("duplicate.similar", selected);
            else if (source == resumeRadio)
                Preferences.instance().setProperty("duplicate.resume", selected);
             */
            /*
            else if (source == httpRadio) {
                if(selected)
                    Preferences.instance().setProperty("connection.protocol.default", "http");
            }
            else if (source == ftpRadio) {
                if(selected)
                    Preferences.instance().setProperty("connection.protocol.default", "ftp");
            }
             */
        //}
        /*
        if(source instanceof JComboBox) {
            JComboBox combo = (JComboBox)source;*/
/*
            if (source == normalfontCombo)
                Preferences.instance().setProperty("font.normal", (String)combo.getSelectedItem());
            else if (source == smallfontCombo)
                Preferences.instance().setProperty("font.small", (String)combo.getSelectedItem());
 */
/*            else if (source == bufferSizeCombo)
                Preferences.instance().setProperty("connection.buffer", (String)combo.getSelectedItem());*/
        /*

            else if (source == startSoundCombo)
                Preferences.instance().setProperty("sound.sound.start", (String)startSoundCombo.getSelectedItem());
	    else if (source == stopSoundCombo)
                Preferences.instance().setProperty("sound.sound.stop", (String)stopSoundCombo.getSelectedItem());
	    else if (source == completeSoundCombo)
                Preferences.instance().setProperty("sound.sound.complete", (String)completeSoundCombo.getSelectedItem());
        */
        /*
        }
	else if(source instanceof JCheckBox) {*/
            /*
            if(source == errorDialogCheckbox)
                Preferences.instance().setProperty("interface.error-dialog", selected);
             */
            /*
	    if (source == startSound)
                Preferences.instance().setProperty("status.sound.start", selected);
	    else if (source == stopSound)
                Preferences.instance().setProperty("status.sound.stop", selected);
	    else if (source == completeSound)
                Preferences.instance().setProperty("status.sound.complete", selected);
                 */

            /*
	    else if (source == saveTableEntriesCheckbox)
                Preferences.instance().setProperty("table.save", selected);
            */
            /*
	    else if (source == encodeFilesCheckbox)
                Preferences.instance().setProperty("files.encode", selected);
             */
            /*
	    else if (source == showHiddenCheckbox)
                Preferences.instance().setProperty("ftp.showHidden", selected);
	    else if (source == postProcessCheckbox)
                Preferences.instance().setProperty("files.postprocess", selected);
*/
            /*
            else if (source == logConnectionCheckbox)
                Preferences.instance().setProperty("connection.log", selected);
             */

            /*
            else if (source == showTranscriptCheckbox)
                Preferences.instance().setProperty("statuspanel.transcriptmessage", selected);
            else if (source == showProgressCheckbox) 
                Preferences.instance().setProperty("statuspanel.progressmessage", selected);
            else if (source == showLocalPathCheckbox) 
                Preferences.instance().setProperty("statuspanel.localpath", selected);
             */
            /*
            else if (source == proxyCheckbox)
                Preferences.instance().setProperty("connection.proxy", selected);
            else if (source == proxyAuthCheckbox)
                Preferences.instance().setProperty("connection.proxy.authenticate", selected);
             */
/*
            else if (source == listing_Type)
                Preferences.instance().setProperty("ftp.listing.showType", selected);
            else if (source == listing_Filenames)
                Preferences.instance().setProperty("ftp.listing.showFilenames", selected);
            else if (source == listing_Size)
                Preferences.instance().setProperty("ftp.listing.showSize", selected);
            else if (source == listing_Date)
                Preferences.instance().setProperty("ftp.listing.showDate", selected);
            else if (source == listing_Owner)
                Preferences.instance().setProperty("ftp.listing.showOwner", selected);
            else if (source == listing_Access)
                Preferences.instance().setProperty("ftp.listing.showAccess", selected);
 */
