package ch.cyberduck.ui;

/*
 *  ch.cyberduck.ui.EditPanel.java
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
import java.awt.event.*;
import java.io.File;
import java.net.URL;
import java.util.Observable;
import java.util.Observer;

import com.enterprisedt.net.ftp.FTPTransferType;
import ch.cyberduck.Cyberduck;
import ch.cyberduck.Preferences;
import ch.cyberduck.connection.Bookmark;
import ch.cyberduck.connection.Session;
import ch.cyberduck.ui.common.*;
import ch.cyberduck.ui.layout.ParagraphLayout;

/**
* @version $Id$
 */
public class EditPanel extends JPanel implements Observer {
    
    private Bookmark selected;

    private JLabel urlLabel;
    private JButton saveButton, connectButton;
    private JTextField hostField, portField;
    private JTextField nameField;
    private JPasswordField passwdField;
    private JTextField pathField, saveToField;
    private JCheckBox anonymousCheckbox, sslCheckbox;
    private JRadioButton httpRadiobutton, ftpRadiobutton, sftpRadiobutton;
    private JRadioButton asciiRadio, binaryRadio;
    private JPanel parameterPanel;

    public EditPanel() {
        Cyberduck.DEBUG("[EditPanel]");
        this.init();
    }

    public void update(Observable o, Object arg) {
        if(o instanceof Bookmark) {
            Cyberduck.DEBUG("[EditPanel] update("+arg.toString()+")");
            if(arg.equals(Bookmark.SELECTION)) {
                this.selected = (Bookmark)o;

                sslCheckbox.setSelected(selected.getProtocol().equalsIgnoreCase(Session.HTTPS));

                hostField.setText(selected.getHost());
                pathField.setText(selected.getServerPathAsString());
                portField.setText(String.valueOf(selected.getPort()));
                
                nameField.setText(selected.getUsername());
                passwdField.setText(selected.getPassword());
                
                asciiRadio.setSelected(selected.getTransferType().equals(FTPTransferType.ASCII));
                binaryRadio.setSelected(selected.getTransferType().equals(FTPTransferType.BINARY));

                saveToField.setText(selected.getLocalPathAsString());

                sftpRadiobutton.setSelected(selected.getProtocol().equalsIgnoreCase(Session.SFTP));
                ftpRadiobutton.setSelected(selected.getProtocol().equalsIgnoreCase(Session.FTP));
                httpRadiobutton.setSelected(selected.getProtocol().equalsIgnoreCase(Session.HTTP) || selected.getProtocol().equalsIgnoreCase(Session.HTTPS));

		parameterPanel.setBorder(
			   BorderFactory.createTitledBorder(
				       BorderFactory.createEtchedBorder(javax.swing.border.EtchedBorder.LOWERED), selected.getHost()));
                urlLabel.setText(selected.getAddressAsString());
                new ConfigurationAction().actionPerformed(new ActionEvent(this, ActionEvent.ACTION_PERFORMED, null));
            }
        }
    }

    private class ConfigurationAction extends AbstractAction {
        public ConfigurationAction() {
            super();
        }
        public ConfigurationAction(String name) {
            super(name);
        }
        public void actionPerformed(ActionEvent ae) {
	    //            URL url = null;
            String url = null;
     //       try {
                // update url label
                int port;
                try {
                    port = Integer.parseInt(portField.getText());
                }
                catch(NumberFormatException e) {
                    port = httpRadiobutton.isSelected() ? Session.HTTP_PORT : Session.FTP_PORT;
                }
                if(httpRadiobutton.isSelected()) {
                    String protocol = sslCheckbox.isSelected() ? Session.HTTPS : Session.HTTP;
//                    url = new URL(protocol, hostField.getText(), port, pathField.getText());
		    url = protocol+"://"+hostField.getText()+port+pathField.getText();
                }
                if(ftpRadiobutton.isSelected()) {
                    String protocol = Session.FTP;
//                    url = new URL(protocol, nameField.getText() + "@" + hostField.getText(), port, pathField.getText());
		    url = protocol+"://"+nameField.getText()+"@"+hostField.getText()+port+pathField.getText();
                }
                if(sftpRadiobutton.isSelected()) {
                    String protocol = Session.SFTP;
		    url = protocol+"://"+nameField.getText()+"@"+hostField.getText()+port+pathField.getText();
//                    url = new URL(protocol, nameField.getText() + "@" + hostField.getText(), port, pathField.getText());
                }
                //Cyberduck.DEBUG("setting url label:\n"+url.toString());
                urlLabel.setText(url);
//                urlLabel.setText(url.toString());
                if(pathField.getText() != null) {
                    if(pathField.getText().equals("") || pathField.getText().endsWith("/")) {
                        saveToField.setText("");
                        saveToField.setEnabled(false);
                    }
                    else {
                        saveToField.setText(Preferences.instance().getProperty("download.path") + pathField.getText().substring(pathField.getText().lastIndexOf('/') + 1 ));
                        saveToField.setEnabled(true);
                    }
                }
  //          }
//            catch(java.net.MalformedURLException ex) {
//                ex.printStackTrace();
  //          }

            anonymousCheckbox.setSelected(nameField.getText()!=null && nameField.getText().equals(Preferences.instance().getProperty("ftp.login.anonymous.name")));
            sslCheckbox.setEnabled(httpRadiobutton.isSelected());
            anonymousCheckbox.setEnabled(ftpRadiobutton.isSelected() | sftpRadiobutton.isSelected());
            nameField.setEnabled(ftpRadiobutton.isSelected() | sftpRadiobutton.isSelected() );
            passwdField.setEnabled(ftpRadiobutton.isSelected() | sftpRadiobutton.isSelected() );
            asciiRadio.setEnabled(ftpRadiobutton.isSelected());
            binaryRadio.setEnabled(ftpRadiobutton.isSelected());

        }
    }
    
    public JButton getDefaultButton() {
        return this.saveButton;
    }

/*
    public java.awt.Dimension getMinimumSize() {
        java.awt.Dimension d = super.getMinimumSize();
        Cyberduck.DEBUG("[EditPanel] getMinimumSize():" + d.toString());
        return d;
    }
  */
    
    private class ButtonPanel extends JPanel {
    	public ButtonPanel() {
    		super();
    		this.init();
    	}
    	
    	private void init() {
			this.setLayout(new FlowLayout(FlowLayout.RIGHT));
	//        this.add(cancelButton = GUIFactory.buttonBuilder(GUIFactory.FONT_SMALL, new CancelAction()));
			this.add(saveButton = GUIFactory.buttonBuilder(GUIFactory.FONT_SMALL, new SaveAction()));
			saveButton.setDefaultCapable(true);
			this.add(connectButton = GUIFactory.buttonBuilder(GUIFactory.FONT_SMALL, new ConnectAction()));
		}
	}

    private class ParameterPanel extends JPanel {
    	public ParameterPanel() {
    		super();
    		this.init();
    	}
    	
    	private void init() {
			this.setLayout(new ParagraphLayout());
			
			// protocol selection
			ButtonGroup protocolGroup = new ButtonGroup();
			this.add(GUIFactory.labelBuilder("URL:", GUIFactory.FONT_SMALL), ParagraphLayout.NEW_PARAGRAPH);
			this.add(urlLabel = new DraggableLabel("", GUIFactory.FONT_SMALL));

			//@todo replace with combobox
			this.add(GUIFactory.labelBuilder("Protocol:", GUIFactory.FONT_SMALL), ParagraphLayout.NEW_PARAGRAPH);
			this.add(sftpRadiobutton = GUIFactory.radiobuttonBuilder(protocolGroup, GUIFactory.FONT_SMALL, new ConfigurationAction("SFTP")));
			this.add(httpRadiobutton = GUIFactory.radiobuttonBuilder(protocolGroup, GUIFactory.FONT_SMALL, new ConfigurationAction("HTTP")));
			this.add(ftpRadiobutton = GUIFactory.radiobuttonBuilder(protocolGroup, GUIFactory.FONT_SMALL, new ConfigurationAction("FTP")));
			sslCheckbox = GUIFactory.checkboxBuilder("Use SSL", GUIFactory.FONT_SMALL, false, new ItemListener() {
				public void itemStateChanged(ItemEvent e) {
				    if(sftpRadiobutton.isSelected())
					portField.setText(""+Session.SFTP_PORT);
				    if(ftpRadiobutton.isSelected())
					portField.setText(""+Session.FTP_PORT);
				    if(httpRadiobutton.isSelected())
					portField.setText(sslCheckbox.isSelected() ? ""+Session.HTTPS_PORT : ""+Session.HTTP_PORT);
				};
			});
			sslCheckbox = GUIFactory.checkboxBuilder("Use SSL", GUIFactory.FONT_SMALL, false, new ItemListener() {
				public void itemStateChanged(ItemEvent e) {
					if(httpRadiobutton.isSelected())
						portField.setText(sslCheckbox.isSelected() ? ""+Session.HTTPS_PORT : ""+Session.HTTP_PORT);
				};
			});
			this.add(sslCheckbox);
			
	
			this.add(GUIFactory.labelBuilder("Host:", GUIFactory.FONT_SMALL), ParagraphLayout.NEW_PARAGRAPH);
			this.add(hostField = GUIFactory.textFieldBuilder(GUIFactory.FONT_SMALL, 30, new DummyVerifier()));
			hostField.setAction(new ConfigurationAction("Host"));
			hostField.addKeyListener(new KeyAdapter() {
				public void keyReleased(KeyEvent ev) {
					hostField.getAction().actionPerformed(new ActionEvent(ev.getSource(), ev.getID(), "KEY"));
				}
			}
									 );
			this.add(GUIFactory.labelBuilder("Port:", GUIFactory.FONT_SMALL));
			this.add(portField = GUIFactory.textFieldBuilder(GUIFactory.FONT_SMALL, 4, new IntegerVerifier()));
			portField.setAction( new ConfigurationAction("Port"));
			portField.addKeyListener(new KeyAdapter() {
				public void keyReleased(KeyEvent ev) {
					portField.getAction().actionPerformed(new ActionEvent(ev.getSource(), ev.getID(), "KEY"));
				}
			}
									 );
			this.add(GUIFactory.labelBuilder("Remote Path:", GUIFactory.FONT_SMALL), ParagraphLayout.NEW_PARAGRAPH);
			this.add(pathField = GUIFactory.textFieldBuilder(GUIFactory.FONT_SMALL, 30));
			pathField.setAction(new ConfigurationAction("Remote Path"));
			pathField.addKeyListener(new KeyAdapter() {
				public void keyReleased(KeyEvent ev) {
					pathField.getAction().actionPerformed(new ActionEvent(ev.getSource(), ev.getID(), "KEY"));
				}
			}
									 );
	
			this.add(GUIFactory.labelBuilder("Save As:", GUIFactory.FONT_SMALL), ParagraphLayout.NEW_PARAGRAPH);
			JLabel pathErrorLabel = GUIFactory.labelBuilder("The directory does not exist.", GUIFactory.FONT_SMALL);
			pathErrorLabel.setVisible(false);
			this.add(saveToField = GUIFactory.textFieldBuilder("", GUIFactory.FONT_SMALL, 30,  new ParentPathVerifier(pathErrorLabel)));
			this.add(pathErrorLabel, ParagraphLayout.NEW_LINE);
			this.add(GUIFactory.labelBuilder("Login:", GUIFactory.FONT_SMALL), ParagraphLayout.NEW_PARAGRAPH);
			// NameField
			this.add(nameField = GUIFactory.textFieldBuilder(GUIFactory.FONT_SMALL, 20, new DummyVerifier()));
			this.add(GUIFactory.labelBuilder("User ID", GUIFactory.FONT_SMALL));
			nameField.setAction(new ConfigurationAction("User"));
			nameField.addKeyListener(new KeyAdapter() {
				public void keyReleased(KeyEvent ev) {
					nameField.getAction().actionPerformed(new ActionEvent(ev.getSource(), ev.getID(), "KEY"));
				}
			}
									 );
			// PasswdField
			this.add(passwdField = GUIFactory.passwordFieldBuilder(GUIFactory.FONT_SMALL, 20, new DummyVerifier()), ParagraphLayout.NEW_LINE);
			this.add(GUIFactory.labelBuilder("Password", GUIFactory.FONT_SMALL));
			passwdField.setAction(new ConfigurationAction("Password"));
			passwdField.addKeyListener(new KeyAdapter() {
				public void keyReleased(KeyEvent ev) {
					passwdField.getAction().actionPerformed(new ActionEvent(ev.getSource(), ev.getID(), "KEY"));
				}
			}
									 );
			this.add(anonymousCheckbox = GUIFactory.checkboxBuilder("Anonymous Login", GUIFactory.FONT_SMALL), ParagraphLayout.NEW_LINE);
			anonymousCheckbox.addItemListener(new ItemListener() {
				public void itemStateChanged(ItemEvent e) {
					if(e.getStateChange() == ItemEvent.SELECTED) {
						nameField.setText(Preferences.instance().getProperty("ftp.login.anonymous.name"));
						passwdField.setText(Preferences.instance().getProperty("ftp.login.anonymous.pass"));
					}
					/*
					else {
						nameField.setText("");
						passwdField.setText("");
					}
					 */
				}
			}
											  );
			
			
			ButtonGroup transferModeGroup = new ButtonGroup();
			this.add(GUIFactory.labelBuilder("Transfer Mode:", GUIFactory.FONT_SMALL), ParagraphLayout.NEW_PARAGRAPH);
	//        this.add(autoRadio = GUIFactory.radiobuttonBuilder(transferModeGroup, "Auto", GUIFactory.FONT_SMALL, Preferences.instance().getProperty("connection.transfertype.default").equals("auto"), null));
			this.add(asciiRadio = GUIFactory.radiobuttonBuilder(transferModeGroup, "ASCII", GUIFactory.FONT_SMALL));
			this.add(binaryRadio = GUIFactory.radiobuttonBuilder(transferModeGroup, "Binary", GUIFactory.FONT_SMALL));
    	}
    }
    
    /**
     * Initialize the graphical user interface
     */
    private void init() {
        Cyberduck.DEBUG("[EditPanel] init()");
        this.setLayout(new BorderLayout());
        this.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
        this.add(parameterPanel = new ParameterPanel(), BorderLayout.CENTER);
        this.add(new ButtonPanel(), BorderLayout.SOUTH);
    }


    // ************************************************************************************************************

    private class ConnectAction extends SaveAction {
        public ConnectAction () {
            this.putValue(NAME, "Connect");
            this.putValue(SHORT_DESCRIPTION, "Save changes and connect");
        }
        public void actionPerformed(ActionEvent ae) {
            Cyberduck.DEBUG(ae.paramString());
            super.actionPerformed(ae);
            selected.transfer();
        }
    }


    private class SaveAction extends AbstractAction {//extends CancelAction {
        public SaveAction() {
            this.putValue(NAME, "Save");
            this.putValue(SHORT_DESCRIPTION, "Save changes");
        }
        public void actionPerformed(ActionEvent ae) {
            Cyberduck.DEBUG(ae.paramString());
            if(httpRadiobutton.isSelected())
                selected.setProtocol(sslCheckbox.isSelected() ? Session.HTTPS : Session.HTTP);
            if(ftpRadiobutton.isSelected()) {
                selected.setProtocol(Session.FTP);
                selected.setUserInfo(nameField.getText()+":"+new String(passwdField.getPassword()));
            }
            if(sftpRadiobutton.isSelected()) {
                selected.setProtocol(Session.SFTP);
                selected.setUserInfo(nameField.getText()+":"+new String(passwdField.getPassword()));
            }
            selected.setTransferType(asciiRadio.isSelected() ? FTPTransferType.ASCII : FTPTransferType.BINARY);
            try {
                selected.setPort(Integer.parseInt(portField.getText()));
            }
            catch(NumberFormatException e) {
                selected.setPort(Session.FTP_PORT);
            }
            selected.setHost(hostField.getText());
            selected.setServerPath(pathField.getText());
            if(selected.isDownload())
                selected.setLocalPath(new File(saveToField.getText()));
        }
    }
}
