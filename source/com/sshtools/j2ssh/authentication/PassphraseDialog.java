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
import java.awt.Color;
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
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import com.sshtools.j2ssh.ui.IconWrapperPanel;
import com.sshtools.j2ssh.ui.ResourceIcon;
import com.sshtools.j2ssh.ui.UIUtil;

/**
 *  Implements the password login screen for the PasswrodAuthentication object
 *
 *@author     Brett Smith
 *@created    17 November 2002
 *@version    $Id: PassphraseDialog.java,v 1.2 2002/12/09 22:51:23 martianx Exp
 *      $
 */
public class PassphraseDialog
         extends JDialog {
    //  Statics
    final static String PASSPHRASE_ICON =
            "/com/sshtools/j2ssh/authentication/largepassphrase.png";
    JButton jButtonCancel = new JButton();
    JButton jButtonOK = new JButton();
    JLabel message = new JLabel("Enter passphrase");

    /**
     *  The password mask field
     */
    JPasswordField jPasswordField = new JPasswordField(20);

    /**
     *  Flag to indicate whether the user cancelled the dialog or not.
     */
    boolean userCancelled = false;


    /**
     *  Constructs the dialog.
     */
    public PassphraseDialog() {
        super((Frame) null, "Passphrase", true);
        init(null);
    }


    /**
     *  Constructs the dialog.
     *
     *@param  parent  The parent frame
     */
    public PassphraseDialog(Frame parent) {
        super(parent, "Passphrase", true);
        init(parent);
    }


    /**
     *  Constructs the dialog.
     *
     *@param  parent  The parent dialog
     */
    public PassphraseDialog(Dialog parent) {
        super(parent, "Passphrase", true);
        init(parent);
    }


    /**
     *  Return if the cancel button was pressed
     *
     *@return    cancel pressed
     */
    public boolean isCancelled() {
        return userCancelled;
    }


    /**
     *  Set the message.
     *
     *@param  message  the message
     */
    public void setMessage(String message) {
        this.message.setText(message);
    }


    /**
     *  Set the message foreground color.
     *
     *@param  color  the message foreground color
     */
    public void setMessageForeground(Color color) {
        message.setForeground(color);
    }


    /**
     *  Gets the password entered by the user.
     *
     *@return    The users password
     */
    public char[] getPassphrase() {
        return jPasswordField.getPassword();
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
        userCancelled = false;
        hide();
    }


    /**
     *  Initializes the instance
     *
     *@exception  Exception  if an error occurs
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

        //  Passphrase panel
        JPanel passphrasePanel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.anchor = GridBagConstraints.WEST;
        gbc.insets = new Insets(0, 2, 2, 2);
        gbc.weightx = 1.0;
        UIUtil.jGridBagAdd(passphrasePanel, message, gbc,
                GridBagConstraints.REMAINDER);
        UIUtil.jGridBagAdd(passphrasePanel, jPasswordField, gbc,
                GridBagConstraints.REMAINDER);

        //  Create the center banner panel
        IconWrapperPanel centerPanel =
                new IconWrapperPanel(new ResourceIcon(PASSPHRASE_ICON),
                passphrasePanel);
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
