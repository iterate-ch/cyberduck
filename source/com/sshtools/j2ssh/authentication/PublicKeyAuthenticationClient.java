/*
 *  SSHTools - Java SSH2 API
 *
 *  Copyright (C) 2002-2003 Lee David Painter and Contributors.
 *
 *  Contributions made by:
 *
 *  Brett Smith
 *  Richard Pernavas
 *  Erwin Bolwidt
 *
 *  This program is free software; you can redistribute it and/or
 *  modify it under the terms of the GNU Library General Public License
 *  as published by the Free Software Foundation; either version 2 of
 *  the License, or (at your option) any later version.
 *
 *  You may also distribute it and/or modify it under the terms of the
 *  Apache style J2SSH Software License. A copy of which should have
 *  been provided with the distribution.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  License document supplied with your distribution for more details.
 *
 */
package com.sshtools.j2ssh.authentication;

import com.sshtools.j2ssh.io.ByteArrayWriter;
import com.sshtools.j2ssh.transport.SshMessage;
import com.sshtools.j2ssh.transport.publickey.SshPrivateKey;
import com.sshtools.j2ssh.transport.publickey.SshPublicKey;

import org.apache.log4j.Logger;

import java.io.IOException;
import java.util.Properties;


/**
 * @author $author$
 * @version $Revision$
 */
public class PublicKeyAuthenticationClient extends SshAuthenticationClient {
    private static Logger log = Logger.getLogger(PublicKeyAuthenticationClient.class);

    /**  */
    protected SshPrivateKey key;
    private String privateKeyFile = null;
    private String passphrase = null;

    /**
     * Creates a new PublicKeyAuthenticationClient object.
     */
    public PublicKeyAuthenticationClient() {
    }

    /**
     * @param key
     */
    public void setKey(SshPrivateKey key) {
        this.key = key;
    }

    public void setKeyfile(String privateKeyFile) {
        this.privateKeyFile = privateKeyFile;
    }

    public String getKeyfile() {
        return privateKeyFile;
    }

    /**
     *
     */
    public void reset() {
        privateKeyFile = null;
        passphrase = null;
    }

    /**
     * @return
     */
    public String getMethodName() {
        return "publickey";
    }

    /**
     * @param authentication
     * @param username
     * @param serviceToStart
     * @param key
     * @return
     * @throws IOException
     */
    public boolean acceptsKey(AuthenticationProtocolClient authentication,
                              String username, String serviceToStart, SshPublicKey key)
            throws IOException {
        authentication.registerMessage(SshMsgUserAuthPKOK.class,
                SshMsgUserAuthPKOK.SSH_MSG_USERAUTH_PK_OK);
        log.info("Determining if server can accept public key for authentication");

        ByteArrayWriter baw = new ByteArrayWriter();

        // Now prepare and send the message
        baw.write(0);
        baw.writeString(key.getAlgorithmName());
        baw.writeBinaryString(key.getEncoded());

        SshMessage msg = new SshMsgUserAuthRequest(username, serviceToStart,
                getMethodName(), baw.toByteArray());
        authentication.sendMessage(msg);

        try {
            msg = authentication.readMessage(SshMsgUserAuthPKOK.SSH_MSG_USERAUTH_PK_OK);

            if (msg instanceof SshMsgUserAuthPKOK) {
                return true;
            }
            else {
                throw new IOException("Unexpected message returned from readMessage");
            }
        }
        catch (TerminatedStateException ex) {
            return false;
        }
    }

    /**
     * @param authentication
     * @param serviceToStart
     * @throws IOException
     * @throws TerminatedStateException
     * @throws AuthenticationProtocolException
     *
     */
    public void authenticate(AuthenticationProtocolClient authentication,
                             String serviceToStart) throws IOException, TerminatedStateException {
        if ((getUsername() == null) || (key == null)) {
            throw new AuthenticationProtocolException("You must supply a username and a key");
        }

        ByteArrayWriter baw = new ByteArrayWriter();
        log.info("Generating data to sign");

        SshPublicKey pub = key.getPublicKey();
        log.info("Preparing public key authentication request");

        // Now prepare and send the message
        baw.write(1);
        baw.writeString(pub.getAlgorithmName());
        baw.writeBinaryString(pub.getEncoded());

        // Create the signature data
        ByteArrayWriter data = new ByteArrayWriter();
        data.writeBinaryString(authentication.getSessionIdentifier());
        data.write(SshMsgUserAuthRequest.SSH_MSG_USERAUTH_REQUEST);
        data.writeString(getUsername());
        data.writeString(serviceToStart);
        data.writeString(getMethodName());
        data.write(1);
        data.writeString(pub.getAlgorithmName());
        data.writeBinaryString(pub.getEncoded());

        // Generate the signature
        baw.writeBinaryString(key.generateSignature(data.toByteArray()));

        SshMsgUserAuthRequest msg = new SshMsgUserAuthRequest(getUsername(),
                serviceToStart, getMethodName(), baw.toByteArray());
        authentication.sendMessage(msg);
    }

    /*public boolean showAuthenticationDialog(Component parent) {
         SshPrivateKeyFile pkf = null;
         if (privateKeyFile == null) {
        JFileChooser chooser = new JFileChooser();
        chooser.setFileHidingEnabled(false);
        chooser.setDialogTitle("Select Private Key File For Authentication");
        if (chooser.showOpenDialog(parent) == JFileChooser.APPROVE_OPTION) {
            privateKeyFile = chooser.getSelectedFile().getAbsolutePath();
        } else {
            return false;
        }
         }
         FileInputStream in = null;
         try {
        pkf = SshPrivateKeyFile.parse(new File(privateKeyFile));
         } catch (InvalidSshKeyException iske) {
        JOptionPane.showMessageDialog(parent, iske.getMessage());
        return false;
         } catch (IOException ioe) {
        JOptionPane.showMessageDialog(parent, ioe.getMessage());
         }
         // Now see if its passphrase protected
         if (pkf.isPassphraseProtected()) {
        if (passphrase == null) {
            // Show the passphrase dialog
     Window w = (Window) SwingUtilities.getAncestorOfClass(Window.class,
                    parent);
            PassphraseDialog dialog = null;
            if (w instanceof Frame) {
                dialog = new PassphraseDialog((Frame) w);
            } else if (w instanceof Dialog) {
                dialog = new PassphraseDialog((Dialog) w);
            } else {
                dialog = new PassphraseDialog();
            }
            do {
                dialog.setVisible(true);
                if (dialog.isCancelled()) {
                    return false;
                }
                passphrase = new String(dialog.getPassphrase());
                try {
                    key = pkf.toPrivateKey(passphrase);
                    break;
                } catch (InvalidSshKeyException ihke) {
                    dialog.setMessage("Passphrase Invalid! Try again");
                    dialog.setMessageForeground(Color.red);
                }
            } while (true);
        } else {
            try {
                key = pkf.toPrivateKey(passphrase);
            } catch (InvalidSshKeyException ihke) {
                return false;
            }
        }
         } else {
        try {
            key = pkf.toPrivateKey(null);
        } catch (InvalidSshKeyException ihke) {
            JOptionPane.showMessageDialog(parent, ihke.getMessage());
            return false;
        }
         }
         return true;
     }*/
    public Properties getPersistableProperties() {
        Properties properties = new Properties();

        if (getUsername() != null) {
            properties.setProperty("Username", getUsername());
        }

        if (privateKeyFile != null) {
            properties.setProperty("PrivateKey", privateKeyFile);
        }

        return properties;
    }

    /**
     * @param properties
     */
    public void setPersistableProperties(Properties properties) {
        setUsername(properties.getProperty("Username"));

        if (properties.getProperty("PrivateKey") != null) {
            privateKeyFile = properties.getProperty("PrivateKey");
        }

        if (properties.getProperty("Passphrase") != null) {
            passphrase = properties.getProperty("Passphrase");
        }
    }

    /**
     * @return
     */
    public boolean canAuthenticate() {
        return ((getUsername() != null) && (key != null));
    }
}
