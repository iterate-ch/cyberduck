/*
 *  Sshtools - Java SSH2 API
 *
 *  Copyright (C) 2002 Lee David Painter.
 *
 *  Written by: 2002 Lee David Painter <lee@sshtools.com>
 *
 *  This program is free software; you can redistribute it and/or
 *  modify it under the terms of the GNU Library General Public License
 *  as published by the Free Software Foundation; either version 2 of
 *  the License, or (at your option) any later version.
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU Library General Public License for more details.
 *
 *  You should have received a copy of the GNU Library General Public
 *  License along with this program; if not, write to the Free Software
 *  Foundation, Inc., 675 Mass Ave, Cambridge, MA 02139, USA.
 */
package com.sshtools.j2ssh.authentication;

import java.awt.BorderLayout;
import java.awt.Dialog;
import java.awt.FlowLayout;
import java.awt.Frame;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Insets;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import com.sshtools.j2ssh.ui.IconWrapperPanel;
import com.sshtools.j2ssh.ui.ResourceIcon;
import com.sshtools.j2ssh.ui.*;

/**
 *  This class implements the password login screen for the
 *  PasswrodAuthentication objects showAuthenticationDialog method.
 *
 *@author     <A HREF="mailto:lee@sshtools.com">Lee David Painter</A>
 *@created    20 December 2002
 *@version    $Id: PasswordAuthenticationDialog.java,v 1.8 2002/12/09 22:51:23
 *      martianx Exp $
 */
public class PasswordAuthenticationDialog
         extends JDialog {
    //  Statics
    final static String KEY_ICON =
            "/com/sshtools/j2ssh/authentication/largecard.png";
    JButton jButtonCancel = new JButton();
    JButton jButtonOK = new JButton();

    /**
     *  Dialog components
     */
    JPasswordField jPasswordField = new JPasswordField(20);
    XTextField jTextUsername = new XTextField(20);

    /**
     *  Flag to indicate whether the user cancelled the dialog or not.
     */
    boolean userCancelled = false;


    /**
     *  Constructs the dialog.
     */
    public PasswordAuthenticationDialog() {
        super((Frame) null, "Password Authentication", true);
        init(null);
    }


    /**
     *  Constructs the dialog.
     *
     *@param  parent  The parent frame
     */
    public PasswordAuthenticationDialog(Frame parent) {
        super(parent, "Password Authentication", true);
        init(parent);
    }


    /**
     *  Constructs the dialog.
     *
     *@param  parent  The parent dialog
     */
    public PasswordAuthenticationDialog(Dialog parent) {
        super(parent, "Password Authentication", true);
        init(parent);
    }


    /**
     *  Gets the password entered by the user.
     *
     *@return    The users password
     */
    public String getPassword() {
        return String.valueOf(jPasswordField.getPassword());
    }


    /**
     *  Gets the username supplied by the user.
     *
     *@return    The username
     */
    public String getUsername() {
        return jTextUsername.getText();
    }


    /**
     *  Display the dialog and return indicating whether the authentication is
     *  ready to be performed.
     *
     *@param  username  The username
     *@return           <tt>true</tt> if the authentication is ready otherwise
     *      <tt>false</tt>
     */
    public boolean showPromptForPassword(String username) {
        jTextUsername.setText(username);

        if (!username.equals("")) {
            jPasswordField.grabFocus();
        }

        UIUtil.positionComponent(SwingConstants.CENTER, this);
        setVisible(true);

        return !userCancelled;
    }


    /**
     *  Initialise
     *
     *@param  parent  parent window
     */
    void init(Window parent) {
        getContentPane().setLayout(new GridLayout(1, 1));

        if (parent != null) {
            this.setLocationRelativeTo(parent);
        }

        try {
            jbInit();
            pack();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }


    /**
     *  The cancel action
     *
     *@param  e
     */
    void jButtonCancel_actionPerformed(ActionEvent e) {
        userCancelled = true;
        hide();
    }


    /**
     *  Handles the OK button event.
     *
     *@param  e  The action
     */
    void jButtonOK_actionPerformed(ActionEvent e) {
        if (jTextUsername.getText().trim().equals("")) {
            JOptionPane.showMessageDialog(this, "You must enter a username!",
                    "Password Authentication",
                    JOptionPane.OK_OPTION);

            return;
        }

        hide();
    }


    /**
     *  Description of the Method
     *
     *@exception  Exception  Description of the Exception
     */
    void jbInit()
             throws Exception {
        // Add a window listener to see when the window closes without
        // selecting OK
        addWindowListener(
            new WindowAdapter() {
                public void windowClosing(WindowEvent evt) {
                    userCancelled = true;
                }
            });

        //  Ok button
        jButtonOK.addActionListener(
            new java.awt.event.ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    jButtonOK_actionPerformed(e);
                }
            });
        jButtonOK.setText("OK");
        jButtonOK.setMnemonic('o');
        getRootPane().setDefaultButton(jButtonOK);

        //  Cancel button
        jButtonCancel.setText("Cancel");
        jButtonCancel.setMnemonic('c');
        jButtonCancel.addActionListener(
            new java.awt.event.ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    jButtonCancel_actionPerformed(e);
                }
            });

        //  User / password panel
        JPanel userPasswordPanel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.anchor = GridBagConstraints.WEST;
        gbc.insets = new Insets(0, 2, 2, 2);
        gbc.weightx = 1.0;

        //  Username
        UIUtil.jGridBagAdd(userPasswordPanel, new JLabel("User"), gbc,
                GridBagConstraints.REMAINDER);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        UIUtil.jGridBagAdd(userPasswordPanel, jTextUsername, gbc,
                GridBagConstraints.REMAINDER);
        gbc.fill = GridBagConstraints.NONE;

        //  Username
        UIUtil.jGridBagAdd(userPasswordPanel, new JLabel("Password"), gbc,
                GridBagConstraints.REMAINDER);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        UIUtil.jGridBagAdd(userPasswordPanel, jPasswordField, gbc,
                GridBagConstraints.REMAINDER);
        gbc.fill = GridBagConstraints.NONE;

        //  Create the center banner panel
        IconWrapperPanel centerPanel =
                new IconWrapperPanel(new ResourceIcon(KEY_ICON), userPasswordPanel);
        centerPanel.setBorder(BorderFactory.createEmptyBorder(4, 4, 4, 4));

        //
        JPanel buttonPanel = new JPanel(new GridBagLayout());
        gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.anchor = GridBagConstraints.CENTER;
        gbc.insets = new Insets(6, 6, 0, 0);
        gbc.weighty = 1.0;
        UIUtil.jGridBagAdd(buttonPanel, jButtonOK, gbc,
                GridBagConstraints.RELATIVE);
        UIUtil.jGridBagAdd(buttonPanel, jButtonCancel, gbc,
                GridBagConstraints.REMAINDER);

        JPanel southPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 0, 0));
        southPanel.add(buttonPanel);

        //  Wrap the whole thing in an empty border
        JPanel mainPanel = new JPanel(new BorderLayout());
        mainPanel.setBorder(BorderFactory.createEmptyBorder(4, 4, 4, 4));
        mainPanel.add(centerPanel, BorderLayout.CENTER);
        mainPanel.add(southPanel, BorderLayout.SOUTH);

        //  Build the main panel
        getContentPane().add(mainPanel);

        //
        jPasswordField.grabFocus();
    }
}
