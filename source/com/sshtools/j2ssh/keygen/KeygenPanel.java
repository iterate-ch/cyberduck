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
package com.sshtools.j2ssh.keygen;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import java.io.File;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JProgressBar;
import javax.swing.JTextField;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import com.sshtools.j2ssh.ui.*;

/**
 *  Provides a UI component for generating a j2ssh public / private key pair
 *
 *@author     Brett Smith
 *@created    20 December 2002
 *@version    $Id$
 */
public class KeygenPanel
         extends JPanel
         implements DocumentListener, ActionListener {
    /**
     *  Description of the Field
     */
    public final static int GENERATE_KEY_PAIR = 0;

    /**
     *  Description of the Field
     */
    public final static int CONVERT_IETF_SECSH_TO_OPENSSH = 1;

    /**
     *  Description of the Field
     */
    public final static int CONVERT_OPENSSH_TO_IETF_SECSH = 2;

    /**
     *  Description of the Field
     */
    public final static int CHANGE_PASSPHRASE = 3;

    //  Private instance variables
    private JButton browseInput;

    //  Private instance variables
    private JButton browseOutput;
    private JComboBox action;
    private JComboBox type;
    private JLabel bitsLabel;
    private JLabel inputFileLabel;
    private JLabel newPassphraseLabel;
    private JLabel oldPassphraseLabel;
    private JLabel outputFileLabel;
    private JLabel typeLabel;
    private JPasswordField newPassphrase;
    private JPasswordField oldPassphrase;
    private JProgressBar strength;
    private XTextField inputFile;
    private XTextField outputFile;
    private NumericTextField bits;


    /**
     *  Constructor for the KeygenPanel object
     */
    public KeygenPanel() {
        super();

        JPanel keyPanel = new JPanel(new GridBagLayout());
        keyPanel.setBorder(BorderFactory.createTitledBorder("Key"));

        //  Create the main panel
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.NONE;
        gbc.anchor = GridBagConstraints.WEST;

        Insets normalInsets = new Insets(0, 2, 4, 2);
        Insets indentedInsets = new Insets(0, 26, 4, 2);
        gbc.insets = normalInsets;
        gbc.weightx = 0.0;

        //  Action
        UIUtil.jGridBagAdd(keyPanel, new JLabel("Action"), gbc, 1);
        gbc.weightx = 1.0;
        action =
                new JComboBox(new String[]{
                "Generate key pair",
                "Convert IETF SECSH to OpenSSH",
                "Convert OpenSSH to IETF SECSH",
                "Change passphrase"
                });
        action.addActionListener(this);
        gbc.weightx = 2.0;
        UIUtil.jGridBagAdd(keyPanel, action, gbc, GridBagConstraints.RELATIVE);
        gbc.weightx = 0.0;
        UIUtil.jGridBagAdd(keyPanel, new JLabel(), gbc,
                GridBagConstraints.REMAINDER);

        gbc.insets = indentedInsets;

        //  File
        inputFileLabel = new JLabel("Input File");
        UIUtil.jGridBagAdd(keyPanel, inputFileLabel, gbc, 1);
        gbc.insets = normalInsets;
        gbc.weightx = 1.0;
        inputFile = new XTextField(20);
        UIUtil.jGridBagAdd(keyPanel, inputFile, gbc, GridBagConstraints.RELATIVE);
        inputFileLabel.setLabelFor(inputFile);
        gbc.weightx = 0.0;
        browseInput = new JButton("Browse");
        browseInput.setMnemonic('b');
        browseInput.addActionListener(this);
        UIUtil.jGridBagAdd(keyPanel, browseInput, gbc,
                GridBagConstraints.REMAINDER);

        //  File
        gbc.insets = indentedInsets;
        outputFileLabel = new JLabel("Output File");
        UIUtil.jGridBagAdd(keyPanel, outputFileLabel, gbc, 1);
        gbc.insets = normalInsets;
        gbc.weightx = 1.0;
        outputFile = new XTextField(20);
        UIUtil.jGridBagAdd(keyPanel, outputFile, gbc,
                GridBagConstraints.RELATIVE);
        gbc.weightx = 0.0;
        outputFileLabel.setLabelFor(outputFile);
        browseOutput = new JButton("Browse");
        browseOutput.setMnemonic('r');
        browseOutput.addActionListener(this);
        UIUtil.jGridBagAdd(keyPanel, browseOutput, gbc,
                GridBagConstraints.REMAINDER);

        //  Old Passphrase
        gbc.insets = indentedInsets;
        oldPassphraseLabel = new JLabel("Old Passphrase");
        UIUtil.jGridBagAdd(keyPanel, oldPassphraseLabel, gbc, 1);
        gbc.insets = normalInsets;
        gbc.weightx = 2.0;
        oldPassphrase = new JPasswordField(20);
        oldPassphrase.setBackground(Color.white);
        oldPassphrase.getDocument().addDocumentListener(this);
        oldPassphraseLabel.setLabelFor(oldPassphrase);
        UIUtil.jGridBagAdd(keyPanel, oldPassphrase, gbc,
                GridBagConstraints.RELATIVE);
        UIUtil.jGridBagAdd(keyPanel, new JLabel(), gbc,
                GridBagConstraints.REMAINDER);

        //  Passphrase
        gbc.insets = indentedInsets;

        newPassphraseLabel = new JLabel("New Passphrase");
        UIUtil.jGridBagAdd(keyPanel, newPassphraseLabel, gbc, 1);
        gbc.insets = normalInsets;
        gbc.weightx = 2.0;
        newPassphrase = new JPasswordField(20);
        newPassphrase.setBackground(Color.white);
        newPassphrase.getDocument().addDocumentListener(this);
        newPassphraseLabel.setLabelFor(newPassphrase);
        UIUtil.jGridBagAdd(keyPanel, newPassphrase, gbc,
                GridBagConstraints.RELATIVE);
        UIUtil.jGridBagAdd(keyPanel, new JLabel(), gbc,
                GridBagConstraints.REMAINDER);

        //  Bits
        gbc.insets = indentedInsets;
        bitsLabel = new JLabel("Bits");
        UIUtil.jGridBagAdd(keyPanel, bitsLabel, gbc, 1);
        gbc.weightx = 2.0;
        gbc.insets = normalInsets;
        bits =
                new NumericTextField(new Integer(512), new Integer(1024),
                new Integer(1024));
        bitsLabel.setLabelFor(bits);
        UIUtil.jGridBagAdd(keyPanel, bits, gbc, GridBagConstraints.RELATIVE);
        UIUtil.jGridBagAdd(keyPanel, new JLabel(), gbc,
                GridBagConstraints.REMAINDER);

        //  Type
        gbc.insets = indentedInsets;
        typeLabel = new JLabel("Type");
        UIUtil.jGridBagAdd(keyPanel, typeLabel, gbc, 1);
        gbc.insets = normalInsets;
        gbc.weightx = 2.0;
        type = new JComboBox(new String[]{"DSA", "RSA"});
        type.setFont(inputFile.getFont());

        //  Combo boxes look crap in metal
        UIUtil.jGridBagAdd(keyPanel, type, gbc, GridBagConstraints.RELATIVE);
        UIUtil.jGridBagAdd(keyPanel, new JLabel(), gbc,
                GridBagConstraints.REMAINDER);
        strength = new JProgressBar(0, 40);
        strength.setStringPainted(true);

        JPanel strengthPanel = new JPanel(new GridLayout(1, 1));
        strengthPanel.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createTitledBorder("Passphrase strength"),
                BorderFactory.createEmptyBorder(4,
                4,
                4,
                4)));
        strengthPanel.add(strength);

        //  Build this panel
        setLayout(new BorderLayout());
        add(keyPanel, BorderLayout.CENTER);
        add(strengthPanel, BorderLayout.SOUTH);

        calculateStrength();
        setAvailableActions();
    }


    /**
     *  Return the action
     *
     *@return    action
     */
    public int getAction() {
        return action.getSelectedIndex();
    }


    /**
     *  Return the number of bits
     *
     *@return    bits
     */
    public int getBits() {
        return ((Integer) bits.getValue()).intValue();
    }


    /**
     *  Return the input filename
     *
     *@return    file
     */
    public String getInputFilename() {
        return inputFile.getText();
    }


    /**
     *  Return the new passphrase
     *
     *@return    passphrase
     */
    public char[] getNewPassphrase() {
        return newPassphrase.getPassword();
    }


    /**
     *  Return the old passphrase
     *
     *@return    passphrase
     */
    public char[] getOldPassphrase() {
        return oldPassphrase.getPassword();
    }


    /**
     *  Return the output filename
     *
     *@return    file
     */
    public String getOutputFilename() {
        return outputFile.getText();
    }


    /**
     *  Return the current key type
     *
     *@return    type
     */
    public String getType() {
        return (String) type.getSelectedItem();
    }


    /**
     *  DOCUMENT ME!
     *
     *@param  evt  DOCUMENT ME!
     */
    public void actionPerformed(ActionEvent evt) {
        if (evt.getSource() == browseOutput) {
            File f = new File(outputFile.getText());
            JFileChooser chooser = new JFileChooser(f);
            chooser.setSelectedFile(f);
            chooser.setDialogTitle("Choose output file ..");
            chooser.setFileSelectionMode(JFileChooser.FILES_ONLY);

            if (chooser.showSaveDialog(this) == JFileChooser.APPROVE_OPTION) {
                outputFile.setText(chooser.getSelectedFile().getPath());
            }
        } else if (evt.getSource() == browseInput) {
            File f = new File(inputFile.getText());
            JFileChooser chooser = new JFileChooser(f);
            chooser.setSelectedFile(f);
            chooser.setDialogTitle("Choose input file ..");
            chooser.setFileSelectionMode(JFileChooser.FILES_ONLY);

            if (chooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
                inputFile.setText(chooser.getSelectedFile().getPath());
            }
        } else {
            setAvailableActions();
        }
    }


    /**
     *  DOCUMENT ME!
     *
     *@param  e  DOCUMENT ME!
     */
    public void changedUpdate(DocumentEvent e) {
        calculateStrength();
    }


    /**
     *  DOCUMENT ME!
     *
     *@param  e  DOCUMENT ME!
     */
    public void insertUpdate(DocumentEvent e) {
        calculateStrength();
    }


    /**
     *  DOCUMENT ME!
     *
     *@param  e  DOCUMENT ME!
     */
    public void removeUpdate(DocumentEvent e) {
        calculateStrength();
    }


    /**
     *  Sets the availableActions attribute of the KeygenPanel object
     */
    private void setAvailableActions() {
        inputFile.setEnabled((getAction() == CONVERT_IETF_SECSH_TO_OPENSSH)
                || (getAction() == CONVERT_OPENSSH_TO_IETF_SECSH)
                || (getAction() == CHANGE_PASSPHRASE));
        inputFileLabel.setEnabled(inputFile.isEnabled());
        browseInput.setEnabled(inputFile.isEnabled());
        bits.setEnabled(getAction() == GENERATE_KEY_PAIR);
        bitsLabel.setEnabled(bits.isEnabled());
        outputFile.setEnabled((getAction() == CONVERT_IETF_SECSH_TO_OPENSSH)
                || (getAction() == CONVERT_OPENSSH_TO_IETF_SECSH)
                || (getAction() == GENERATE_KEY_PAIR));
        outputFileLabel.setEnabled(outputFile.isEnabled());
        browseOutput.setEnabled(outputFile.isEnabled());
        newPassphrase.setEnabled((getAction() == GENERATE_KEY_PAIR)
                || (getAction() == CHANGE_PASSPHRASE));
        newPassphraseLabel.setEnabled(newPassphrase.isEnabled());
        oldPassphrase.setEnabled(getAction() == CHANGE_PASSPHRASE);
        oldPassphraseLabel.setEnabled(oldPassphrase.isEnabled());
        type.setEnabled(getAction() == GENERATE_KEY_PAIR);
        typeLabel.setEnabled(type.isEnabled());

        if (inputFile.isEnabled()) {
            inputFile.requestFocus();
        } else {
            outputFile.requestFocus();
        }
    }


    /**
     *  DOCUMENT ME!
     */
    private void calculateStrength() {
        char pw[] = newPassphrase.getPassword();
        strength.setValue((pw.length < 40) ? pw.length : 40);

        Color f;
        String t;

        if (pw.length == 0) {
            f = Color.red;
            t = "Empty!!";
        } else {
            StringBuffer buf = new StringBuffer();
            buf.append(pw.length);
            buf.append(" characters - ");

            if (pw.length < 10) {
                f = Color.red;
                buf.append("Weak!");
            } else if (pw.length < 20) {
                f = Color.orange;
                buf.append("Ok");
            } else if (pw.length < 30) {
                f = Color.green.darker();
                buf.append("Strong");
            } else {
                f = Color.green;
                buf.append("Very strong!");
            }

            t = buf.toString();
        }

        strength.setString(t);
        strength.setForeground(f);
    }
}
