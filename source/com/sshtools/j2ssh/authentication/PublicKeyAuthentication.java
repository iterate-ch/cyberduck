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

import org.apache.log4j.Logger;

import java.awt.Color;
import java.awt.Component;
import java.awt.Dialog;
import java.awt.Frame;
import java.awt.Window;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

import java.util.Iterator;
import java.util.Map;
import java.util.Properties;

import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;

import java.io.IOException;
import java.util.List;
import java.util.ArrayList;

import com.sshtools.j2ssh.configuration.ConfigurationLoader;
import com.sshtools.j2ssh.configuration.ServerConfiguration;
import com.sshtools.j2ssh.platform.NativeAuthenticationProvider;
import com.sshtools.j2ssh.transport.ServiceOperationException;
import com.sshtools.j2ssh.transport.TransportProtocolException;
import com.sshtools.j2ssh.transport.publickey.InvalidSshKeyException;
import com.sshtools.j2ssh.transport.publickey.InvalidSshKeySignatureException;
import com.sshtools.j2ssh.transport.publickey.SECSHPublicKeyFormat;
import com.sshtools.j2ssh.transport.publickey.SshKeyPair;
import com.sshtools.j2ssh.transport.publickey.SshKeyPairFactory;
import com.sshtools.j2ssh.transport.publickey.SshPrivateKey;
import com.sshtools.j2ssh.transport.publickey.SshPrivateKeyFile;
import com.sshtools.j2ssh.transport.publickey.SshPublicKey;
import com.sshtools.j2ssh.transport.publickey.SshPublicKeyFile;
import com.sshtools.j2ssh.transport.publickey.SshtoolsPrivateKeyFormat;
import com.sshtools.j2ssh.io.ByteArrayReader;
import com.sshtools.j2ssh.io.ByteArrayWriter;

import com.sshtools.j2ssh.SshException;

/**
 *  This class implements the SSH Public Key authenticaiton method
 *
 *@author     <A HREF="mailto:lee@sshtools.com">Lee David Painter</A>
 *@created    20 December 2002
 *@version    $Id: PublicKeyAuthentication.java,v 1.12 2002/12/12 20:00:26
 *      martianx Exp $
 */
public class PublicKeyAuthentication
         extends SshAuthentication {
    private Logger log = Logger.getLogger(PublicKeyAuthentication.class);
    protected SshPrivateKey key;
    private String privateKeyFile = null;
    private String passphrase = null;


    /**
     *  Creates a new PublicKeyAuthentication object.
     */
    public PublicKeyAuthentication() { }


    /**
     *  Sets the SshPrivateKey instance for the authentication
     *
     *@param  key  An initialized SshPrivateKey instance
     */
    public void setKey(SshPrivateKey key) {
        this.key = key;
    }


    /**
     *  Returns the SSH Authentication method name
     *
     *@return    "publickey"
     */
    public String getMethodName() {
        return "publickey";
    }


    /**
     *  Performs client side public key authentication
     *
     *@param  serviceToStart                    The service to start upon
     *      successfull authentication
     *@throws  TransportProtocolException       if an error occurs in the
     *      Transport Protocol
     *@throws  ServiceOperationException        if a critical error occurs in
     *      the service operation
     *@throws  AuthenticationProtocolException  if an authentication error
     *      occurs
     */
    public void authenticate(String serviceToStart)
             throws TransportProtocolException,
            ServiceOperationException {
        if ((getUsername() == null) || (key == null)) {
            throw new AuthenticationProtocolException("You must supply a username and a key");
        }

        try {
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
            data.writeBinaryString(authentication.getExchangeHash());
            data.write(SshMsgUserAuthRequest.SSH_MSG_USERAUTH_REQUEST);
            data.writeString(getUsername());
            data.writeString(serviceToStart);
            data.writeString(getMethodName());
            data.write(1);
            data.writeString(pub.getAlgorithmName());
            data.writeBinaryString(pub.getEncoded());

            // Generate the signature
            baw.writeBinaryString(key.generateSignature(data.toByteArray()));

            SshMsgUserAuthRequest msg =
                    new SshMsgUserAuthRequest(getUsername(), serviceToStart,
                    getMethodName(), baw.toByteArray());

            transport.sendMessage(msg, this);
        } catch (IOException ioe) {
            throw new AuthenticationProtocolException("Failed to write request data!");
        }
    }


    /**
     *  Performs server side public key authentication
     *
     *@param  msg                               The authentication request
     *      message
     *@param  nativeSettings                    A Map of native name/value pairs
     *      which may contain required platform specific information
     *@throws  TransportProtocolException       if an error occurs in the
     *      Transport Protocol
     *@throws  ServiceOperationException        if a critical error occurs in
     *      the service operation
     *@throws  AuthenticationProtocolException  if an authentication operation
     *      critically fails
     */
    public void authenticate(SshMsgUserAuthRequest msg, Map nativeSettings)
             throws IOException {

            ByteArrayReader bar = new ByteArrayReader(msg.getRequestData());

            // If check == 0 then authenticate, otherwise just inform that
            // the authentication can continue with the key supplied
            int check = bar.read();
            String algorithm = bar.readString();
            byte encoded[] = bar.readBinaryString();
            byte signature[] = null;

            if (check == 0) {
                // Verify that the public key can be used for authenticaiton
                boolean ok = SshKeyPairFactory.supportsKey(algorithm);

                // Send the reply
                SshMsgUserAuthPKOK reply =
                        new SshMsgUserAuthPKOK(ok, algorithm, encoded);
                transport.sendMessage(reply, this);
            } else {
                NativeAuthenticationProvider authProv =
                        NativeAuthenticationProvider.getInstance();

                if (authProv == null) {
                    log.error("Authentication failed because no native authentication provider is available");
                    authentication.authenticationFailed(this);
                }

                String userHome = authProv.getHomeDirectory(getUsername(), nativeSettings);

                if (userHome == null) {
                    log.error("Authentication failed because no home directory for "
                            + getUsername() + " is available");
                    authentication.authenticationFailed(this);
                } else {

                    // Replace '\' with '/' because when we use it in String.replaceAll
                    // for some reason it removes them?
                    userHome = userHome.replace('\\', '/');

                    ServerConfiguration config =
                            ConfigurationLoader.getServerConfiguration();
                    String authorizationFile;
                    String userConfigDir = config.getUserConfigDirectory();

                    // First replace any '\' with '/' (Becasue replaceAll removes them!)
                    userConfigDir = userConfigDir.replace('\\', '/');

                    // Replace any home directory tokens
                    userConfigDir = userConfigDir.replaceAll("%D", userHome);

                    // Replace any username tokens
                    userConfigDir =
                            userConfigDir.replaceAll("%U", getUsername());

                    // Replace the '/' with File.seperator and trim
                    userConfigDir =
                            userConfigDir.replace('/', File.separatorChar).trim();

                    if (!userConfigDir.endsWith(File.separator)) {
                        userConfigDir += File.separator;
                    }

                    authorizationFile =
                            userConfigDir + config.getAuthorizationFile();

                    // Load the authorization file
                    File file = new File(authorizationFile);

                    if (!file.exists()) {
						log.info("authorizationFile: " + authorizationFile + " does not exist.");
                        log.info("Authentication failed because no authorization file is available");
                        authentication.authenticationFailed(this);

                        return;
                    }

                    FileInputStream in = new FileInputStream(file);

                    /**
                     * TODO: authorization file loading and key retrieval
                     */
                    in.close();

                    Iterator it = new ArrayList().iterator(); //auth.getKey().iterator();
                    SshKeyPair pair = SshKeyPairFactory.newInstance(algorithm);
                    SshPublicKey authorizedKey;
                    SshPublicKey key = pair.decodePublicKey(encoded);
                    boolean valid = false;
                    String keyfile;

                    while (it.hasNext()) {
                        keyfile = (String) it.next();

                        // Look for the file in the user config dir first
                        file = new File(userConfigDir + keyfile);

                        // If it does not exist then look absolute
                        if (!file.exists()) {
                            file = new File(keyfile);
                        }

                        if (file.exists()) {
                            SshPublicKeyFile pkf =
                                    SshPublicKeyFile.parse(file,
                                    new SECSHPublicKeyFormat());
                            authorizedKey = pkf.toPublicKey();

                            if (authorizedKey.getFingerprint().equals(key.getFingerprint())) {
					            if (authProv.logonUser(getUsername(), nativeSettings)) {
					                log.info(getUsername() + " has passed password authentication");
					                authentication.authenticationComplete(this);
					            } else {
					                log.info(getUsername() + " has failed password authentication");
					                authentication.authenticationFailed(this);
					            }
                                valid = true;
                            }
                        } else {
                            log.info("Failed attempt to load key file "
                                    + keyfile);
                        }
                    }

                    if (!valid) {
                        authentication.authenticationFailed(this);

                        return;
                    }

                    signature = bar.readBinaryString();

                    ByteArrayWriter data = new ByteArrayWriter();
                    data.writeBinaryString(authentication.getExchangeHash());
                    data.write(SshMsgUserAuthRequest.SSH_MSG_USERAUTH_REQUEST);
                    data.writeString(msg.getUsername());
                    data.writeString(msg.getServiceName());
                    data.writeString(getMethodName());
                    data.write(1);
                    data.writeString(key.getAlgorithmName());
                    data.writeBinaryString(key.getEncoded());

                    if (key.verifySignature(signature, data.toByteArray())) {
                        authentication.authenticationComplete(this);
                    } else {
                        authentication.authenticationFailed(this);
                    }
                }
            }

    }


    /**
     *  Displays a file chooser for the user to select a private key followed by
     *  a passphrase chooser if the selected private key is protected
     *
     *@param  parent  The parent component
     *@return         <tt>true</tt> if the authenication is ready to be
     *      performed otherwise <tt>false</tt>
     */
    public boolean showAuthenticationDialog(Component parent) {

        SshPrivateKeyFile pkf = null;

        if(privateKeyFile==null) {
          JFileChooser chooser = new JFileChooser();
          chooser.setFileHidingEnabled(false);
          chooser.setDialogTitle("Select Private Key File For Authentication");

          if (chooser.showOpenDialog(parent) == JFileChooser.APPROVE_OPTION) {
              privateKeyFile = chooser.getSelectedFile().getAbsolutePath();
          }
        }
        FileInputStream in = null;

        try {
            pkf = SshPrivateKeyFile.parse(new File(privateKeyFile),
                    new SshtoolsPrivateKeyFormat());
        } catch (InvalidSshKeyException iske) {
            JOptionPane.showMessageDialog(parent, iske.getMessage());
            return false;
        } catch (IOException ioe) {
            JOptionPane.showMessageDialog(parent, ioe.getMessage());
        }


        // Now see if its passphrase protected
        if (pkf.isPassphraseProtected()) {

            if(passphrase==null) {
              // Show the passphrase dialog
              Window w =
                      (Window) SwingUtilities.getAncestorOfClass(Window.class,
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
              }
              else {
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
    }

     public Properties getPersistableProperties() {
      Properties properties = new Properties();
      if(getUsername()!=null)
        properties.setProperty("Username", getUsername());
      if(privateKeyFile!=null)
        properties.setProperty("PrivateKey", privateKeyFile);

      return properties;
    }

    public void setPersistableProperties(Properties properties) {
      setUsername(properties.getProperty("Username"));
      if(properties.getProperty("PrivateKey")!=null)
        privateKeyFile = properties.getProperty("PrivateKey");
      if(properties.getProperty("Passphrase")!=null)
        passphrase = properties.getProperty("Passphrase");

    }

    public boolean canAuthenticate() {
      return (getUsername()!=null && key!=null);
    }
}
