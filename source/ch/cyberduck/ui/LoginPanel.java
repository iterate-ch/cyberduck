package ch.cyberduck.ui;

/*
 *  ch.cyberduck.ui.LoginPanel.java
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
import java.util.Observable;
import java.util.Observer;

import ch.cyberduck.Cyberduck;
import ch.cyberduck.Preferences;
import ch.cyberduck.connection.Bookmark;
import ch.cyberduck.connection.Status;
import ch.cyberduck.ui.common.DummyVerifier;
import ch.cyberduck.ui.common.GUIFactory;
import ch.cyberduck.ui.layout.ParagraphLayout;

/**
* @version $Id$
 */
public class LoginPanel extends JPanel implements Observer {
    
    // The currently selected bookmark
    private Bookmark selected;
    
    private JTextField nameField;
    private JPasswordField passwdField;
    private JButton loginButton;
    private JButton cancelButton;
    private JCheckBox anonymousCheckbox;
    private JPanel parameterPanel;

    public LoginPanel() {
        Cyberduck.DEBUG("[LoginPanel]");
        this.init();
    }
    
    public void update(Observable o, Object arg) {
        if(o instanceof Bookmark) {
            if(arg.equals(Bookmark.SELECTION)) {
                this.selected = (Bookmark)o;
            }
        }
        if(arg.equals(Status.LOGINPANEL)) {
            this.setBorder(BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(javax.swing.border.EtchedBorder.LOWERED), "Login to " + selected.getHost()));
            nameField.setText(Preferences.instance().getProperty("ftp.login.name"));
            nameField.requestFocus();
            passwdField.setText("");
            anonymousCheckbox.setSelected(false);
        }

    }

    public JButton getDefaultButton() {
        return this.loginButton;
    }

    private class ParameterPanel extends JPanel {
        public ParameterPanel() {
            super();
            this.setLayout(new ParagraphLayout());

            // NameField
            this.add(GUIFactory.labelBuilder("User ID: ", GUIFactory.FONT_SMALL), ParagraphLayout.NEW_PARAGRAPH);
            this.add(nameField = GUIFactory.textFieldBuilder(GUIFactory.FONT_SMALL, new DummyVerifier()));
            nameField.setColumns(30);
            // PasswdField
            this.add(GUIFactory.labelBuilder("Password: ", GUIFactory.FONT_SMALL), ParagraphLayout.NEW_PARAGRAPH);
            this.add(passwdField = GUIFactory.passwordFieldBuilder(GUIFactory.FONT_SMALL, new DummyVerifier()));
            passwdField.setColumns(30);
            this.add(anonymousCheckbox = GUIFactory.checkboxBuilder("Anonymous Login", GUIFactory.FONT_SMALL, false), ParagraphLayout.NEW_LINE);
            anonymousCheckbox.addItemListener(new ItemListener() {
                public void itemStateChanged(ItemEvent e) {
                    if(e.getStateChange() == ItemEvent.SELECTED) {
                        nameField.setText(Preferences.instance().getProperty("ftp.login.anonymous.name"));
                        passwdField.setText(Preferences.instance().getProperty("ftp.login.anonymous.pass"));
                    }
                    else {
                        nameField.setText("");
                        passwdField.setText("");
                    }
                }
            });
        }
    }

    private class ButtonPanel extends JPanel {
        public ButtonPanel() {
            super();
            this.setLayout(new FlowLayout(FlowLayout.RIGHT));
            this.add(cancelButton = GUIFactory.buttonBuilder("Cancel", GUIFactory.FONT_SMALL));
            cancelButton.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    selected.status.setPanelProperty(selected.status.getLastPanelProperty());
                }
            });
            this.add(loginButton = GUIFactory.buttonBuilder("Login", GUIFactory.FONT_SMALL));
            loginButton.setDefaultCapable(true);
            loginButton.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent ae) {
                    selected.setUserInfo(nameField.getText()+":"+new String(passwdField.getPassword()));
                    selected.transfer();
                    //((Action)ActionMap.instance().get("Connect")).actionPerformed(new ActionEvent(selected, ae.getID(), ae.getActionCommand()));
                }
            });
        }
    }
            
    /**
     * Initialize the graphical user interface
     */
    private void init() {
        this.setLayout(new BorderLayout());
        this.add(new ParameterPanel(), BorderLayout.CENTER);
        this.add(new ButtonPanel(), BorderLayout.SOUTH);
    }
}
