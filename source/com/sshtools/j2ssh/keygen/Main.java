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
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintWriter;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.ProgressMonitor;
import javax.swing.SwingConstants;
import javax.swing.UIManager;

import org.apache.log4j.*;

import com.sshtools.j2ssh.transport.publickey.SshKeyGenerator;
import com.sshtools.j2ssh.transport.publickey.OpenSSHPublicKeyFormat;
import com.sshtools.j2ssh.transport.publickey.SECSHPublicKeyFormat;
import com.sshtools.j2ssh.ui.IconWrapperPanel;
import com.sshtools.j2ssh.ui.ResourceIcon;
import com.sshtools.j2ssh.ui.UIUtil;

/**
 *  A Utility to create a j2ssh public / private key pair
 *
 *@author     Brett Smith
 *@created    20 December 2002
 *@version    $Id$
 */
public class Main
         extends JFrame
         implements ActionListener {
    //  Statics
    final static String ICON =
            "/com/sshtools/j2ssh/authentication/largepassphrase.png";
    JButton close;
    JButton generate;
    KeygenPanel keygen;


    /**
     *  Creates a new Main object.
     */
    public Main() {
        super("ssh-keygen");

        //  Set the frame icon
        setIconImage(new ResourceIcon(ICON).getImage());

        //
        keygen = new KeygenPanel();

        //  Create the center banner panel
        IconWrapperPanel centerPanel =
                new IconWrapperPanel(new ResourceIcon(ICON), keygen);
        centerPanel.setBorder(BorderFactory.createEmptyBorder(4, 4, 4, 4));

        //  Button panel
        JPanel buttonPanel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.anchor = GridBagConstraints.CENTER;
        gbc.insets = new Insets(6, 6, 0, 0);
        gbc.weighty = 1.0;
        generate = new JButton("Generate");
        generate.addActionListener(this);
        generate.setMnemonic('g');
        this.getRootPane().setDefaultButton(generate);
        UIUtil.jGridBagAdd(buttonPanel, generate, gbc,
                GridBagConstraints.RELATIVE);
        close = new JButton("Close");
        close.addActionListener(this);
        close.setMnemonic('c');
        UIUtil.jGridBagAdd(buttonPanel, close, gbc, GridBagConstraints.REMAINDER);

        JPanel southPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 0, 0));
        southPanel.add(buttonPanel);

        //  Wrap the whole thing in an empty border
        JPanel mainPanel = new JPanel(new BorderLayout());
        mainPanel.setBorder(BorderFactory.createEmptyBorder(4, 4, 4, 4));
        mainPanel.add(centerPanel, BorderLayout.CENTER);
        mainPanel.add(southPanel, BorderLayout.SOUTH);

        //  Build the main panel
        getContentPane().setLayout(new GridLayout(1, 1));
        getContentPane().add(mainPanel);
    }


    /**
     *  Handle action events
     *
     *@param  evt
     */
    public void actionPerformed(ActionEvent evt) {
        //  Close
        if (evt.getSource() == close) {
            dispose();

            return;
        }

        final String newPassphrase =
                new String(keygen.getNewPassphrase()).trim();
        final String oldPassphrase =
                new String(keygen.getOldPassphrase()).trim();

        if ((keygen.getAction() == KeygenPanel.GENERATE_KEY_PAIR)
                || (keygen.getAction() == KeygenPanel.CHANGE_PASSPHRASE)) {
            if (newPassphrase.length() == 0) {
                if (JOptionPane.showConfirmDialog(this,
                        "Passphrase is empty. Are you sure?",
                        "Empty Passphrase",
                        JOptionPane.YES_NO_OPTION) == JOptionPane.NO_OPTION) {
                    return;
                }
            }
        }

        final File inputFile = new File(keygen.getInputFilename());
        final File outputFile = new File(keygen.getOutputFilename());
        final File publicFile = new File(keygen.getOutputFilename() + ".pub");

        //  Check if the output file was supplied
        if ((keygen.getAction() == KeygenPanel.CONVERT_IETF_SECSH_TO_OPENSSH)
                || (keygen.getAction() == KeygenPanel.CONVERT_OPENSSH_TO_IETF_SECSH)
                || (keygen.getAction() == KeygenPanel.GENERATE_KEY_PAIR)) {
            if (keygen.getOutputFilename().length() == 0) {
                JOptionPane.showMessageDialog(this, "No Output file supplied.",
                        "Error", JOptionPane.ERROR_MESSAGE);

                return;
            }

            //  Check if the output file exists, and confirm overwrit if it does
            if (outputFile.exists()) {
                if (JOptionPane.showConfirmDialog(this,
                        "Output file "
                        + outputFile.getName()
                        + " exists. Are you sure?",
                        "File exists",
                        JOptionPane.YES_NO_OPTION) == JOptionPane.NO_OPTION) {
                    return;
                }
            }

            //  Make sure the output file is writeable
            if (outputFile.exists() && !outputFile.canWrite()) {
                JOptionPane.showMessageDialog(this,
                        "Output file "
                        + outputFile.getName()
                        + " can not be written.",
                        "Unwriteable file",
                        JOptionPane.ERROR_MESSAGE);

                return;
            }
        }

        //  If this is a conversion, check the input file is provided
        if ((keygen.getAction() == KeygenPanel.CONVERT_IETF_SECSH_TO_OPENSSH)
                || (keygen.getAction() == KeygenPanel.CONVERT_OPENSSH_TO_IETF_SECSH)) {
            if (keygen.getInputFilename().length() == 0) {
                JOptionPane.showMessageDialog(this, "No Input file supplied.",
                        "Error", JOptionPane.ERROR_MESSAGE);

                return;
            }
        } else if (keygen.getAction() == KeygenPanel.GENERATE_KEY_PAIR) {
            //  Check if the public key file is writeable. We should test if it exists
            //  as thats just too many questions for the user
            if (publicFile.exists() && !publicFile.canWrite()) {
                JOptionPane.showMessageDialog(this,
                        "Public key file "
                        + publicFile.getName()
                        + " can not be written.",
                        "Unwriteable file",
                        JOptionPane.ERROR_MESSAGE);

                return;
            }
        }

        //  Now generate the key
        final ProgressMonitor monitor =
                new ProgressMonitor(this, "Generating keys", "Generating", 0, 100);
        monitor.setMillisToDecideToPopup(0);
        monitor.setMillisToPopup(0);

        Runnable r =
            new Runnable() {
                public void run() {
                    try {
                        if (keygen.getAction() == KeygenPanel.CHANGE_PASSPHRASE) {
                            monitor.setNote("Changing passphrase");
                            SshKeyGenerator.changePassphrase(inputFile,
                                    oldPassphrase,
                                    newPassphrase);
                            monitor.setNote("Complete");
                            JOptionPane.showMessageDialog(Main.this,
                                    "Passphrase changed",
                                    "Passphrase changed",
                                    JOptionPane.INFORMATION_MESSAGE);
                        } else if (keygen.getAction() == KeygenPanel.CONVERT_IETF_SECSH_TO_OPENSSH) {
                            monitor.setNote("Converting key file");
                            writeString(outputFile,
                                    SshKeyGenerator.convertPublicKeyFile(inputFile,
                                    new SECSHPublicKeyFormat(),
                                    new OpenSSHPublicKeyFormat()));
                            monitor.setNote("Complete");
                            JOptionPane.showMessageDialog(Main.this,
                                    "Key converted",
                                    "Key converted",
                                    JOptionPane.INFORMATION_MESSAGE);
                        } else if (keygen.getAction() == KeygenPanel.CONVERT_OPENSSH_TO_IETF_SECSH) {
                            monitor.setNote("Converting key file");
                            writeString(outputFile,
                                    SshKeyGenerator.convertPublicKeyFile(inputFile,
                                    new OpenSSHPublicKeyFormat(),
                                    new SECSHPublicKeyFormat()));
                            monitor.setNote("Complete");
                        } else {
                            monitor.setNote("Creating generator");

                            SshKeyGenerator generator = new SshKeyGenerator();
                            monitor.setNote("Generating");

                            String username = System.getProperty("user.name");
                            generator.generateKeyPair(keygen.getType(),
                                    keygen.getBits(),
                                    outputFile.getAbsolutePath(),
                                    username, newPassphrase);
                            monitor.setNote("Complete");
                            JOptionPane.showMessageDialog(Main.this,
                                    "Key generated to "
                                    + outputFile.getName(),
                                    "Complete",
                                    JOptionPane.INFORMATION_MESSAGE);
                        }
                    } catch (Exception e) {
                        JOptionPane.showMessageDialog(Main.this,
                                e.getMessage(), "Error",
                                JOptionPane.ERROR_MESSAGE);
                    } finally {
                        monitor.close();
                    }
                }
            };

        Thread t = new Thread(r);
        t.start();
    }


    /**
     *  Allow execution from command line
     *
     *@param  args  the command line arguments
     */
    public static void main(String args[]) {
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception e) {
        }

        Main main = new Main();
        main.addWindowListener(
            new WindowAdapter() {
                public void windowClosing(WindowEvent evt) {
                    System.exit(0);
                }
            });
        main.pack();
        UIUtil.positionComponent(SwingConstants.CENTER, main);
        main.setVisible(true);
    }


    /**
     *  Description of the Method
     *
     *@param  file             Description of the Parameter
     *@param  string           Description of the Parameter
     *@exception  IOException  Description of the Exception
     */
    private void writeString(File file, String string)
             throws IOException {
        FileOutputStream out = null;

        try {
            out = new FileOutputStream(file);

            PrintWriter w = new PrintWriter(out, true);
            w.println(string);
        } finally {
            if (out != null) {
                out.close();
            }
        }
    }

    static
    {
        BasicConfigurator.configure();
    }
}
