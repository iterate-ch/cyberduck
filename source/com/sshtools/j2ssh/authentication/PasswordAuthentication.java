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

import java.awt.Component;
import java.awt.Dialog;
import java.awt.Frame;
import java.awt.Window;

import java.io.IOException;

import java.util.Map;
import java.util.Properties;

import javax.swing.SwingUtilities;

import com.sshtools.j2ssh.configuration.ConfigurationLoader;
import com.sshtools.j2ssh.configuration.PlatformConfiguration;
import com.sshtools.j2ssh.platform.NativeAuthenticationProvider;
import com.sshtools.j2ssh.transport.ServiceOperationException;
import com.sshtools.j2ssh.transport.TransportProtocolException;
import com.sshtools.j2ssh.io.ByteArrayReader;
import com.sshtools.j2ssh.io.ByteArrayWriter;

/**
 *  This class implements Password Authentication for the SSH Authenticaiton
 *  Protocol.
 *
 *@author     <A HREF="mailto:lee@sshtools.com">Lee David Painter</A>
 *@created    20 December 2002
 *@version    $Id: PasswordAuthentication.java,v 1.15 2002/12/09 22:51:23
 *      martianx Exp $
 */
public class PasswordAuthentication
         extends SshAuthentication {
    private static Logger log = Logger.getLogger(PasswordAuthentication.class);

    /**
     *  The password value
     */
    protected String password;


    /**
     *  Returns the SSH User Authentication method name.
     *
     *@return    "password"
     */
    public final String getMethodName() {
        return "password";
    }


    /**
     *  Sets the password for the authentication.
     *
     *@param  password  The user's password
     */
    public final void setPassword(String password) {
        this.password = password;
    }


    /**
     *  Displays a modal login prompt.
     *
     *@param  parent                            The parent component
     *@return                                   <tt>true</tt> if the instance is
     *      ready to authenticate otherwise <tt>false</tt>
     *@throws  AuthenticationProtocolException  if an error occurs
     */
    public final boolean showAuthenticationDialog(Component parent)
             throws AuthenticationProtocolException {

        if(password!=null)
          return true;
        // Create the password authentication dialog
        Window w =
                (Window) SwingUtilities.getAncestorOfClass(Window.class, parent);
        PasswordAuthenticationDialog dialog = null;

        if (w instanceof Frame) {
            dialog = new PasswordAuthenticationDialog((Frame) w);
        } else if (w instanceof Dialog) {
            dialog = new PasswordAuthenticationDialog((Dialog) w);
        } else {
            dialog = new PasswordAuthenticationDialog();
        }

        // Show the dialog
        if (dialog.showPromptForPassword(getUsername())) {
            setUsername(dialog.getUsername());
            setPassword(dialog.getPassword());

            return true;
        }

        return false;
    }


    /**
     *  Called by the framework to set any authenticated tokens that might be
     *  needed by authenticated services. This implementation does nothing
     *
     *@param  tokens
     */
    public void setAuthenticatedTokens(Map tokens) { }


    /**
     *  Called to authenticate a users password; this implementation simply
     *  fails
     *
     *@param  msg
     *@param  nativeSettings               A Map of native settings containing
     *      platform specific information that could be required for
     *      authentication
     *@throws  TransportProtocolException  if an error occurs in the Transport
     *      Protocol
     *@throws  ServiceOperationException   if a critical error occurs in the
     *      service operation
     */
    public void authenticate(SshMsgUserAuthRequest msg, Map nativeSettings)
             throws IOException {
        PlatformConfiguration platform =
                ConfigurationLoader.getPlatformConfiguration();

        if (platform == null) {
            authentication.authenticationFailed(this);

            return;
        }

        NativeAuthenticationProvider authImpl =
                NativeAuthenticationProvider.getInstance();

        if (authImpl == null) {
            log.error("Cannot perfrom authentication witout native authentication provider");
            authentication.authenticationFailed(this);

            return;
        }

        ByteArrayReader bar = new ByteArrayReader(msg.getRequestData());
        bar.read();

        String password = bar.readString();

        if (authImpl.logonUser(getUsername(), password, nativeSettings)) {
            log.info(getUsername() + " has passed password authentication");
            authentication.authenticationComplete(this);
        } else {
            log.info(getUsername() + " has failed password authentication");
            authentication.authenticationFailed(this);
        }
    }


    /**
     *  Sends the password authentication over the transport protocol.
     *
     *@param  serviceToStart                  The service to start after
     *      authentication succeeds
     *@exception  TransportProtocolException  if an error occurs in the
     *      Transport Protocol
     *@throws  ServiceOperationException      if a critical error occurs in the
     *      service operation
     */
    public final void authenticate(String serviceToStart)
             throws TransportProtocolException,
            ServiceOperationException {
        if ((getUsername() == null) || (password == null)) {
            throw new AuthenticationProtocolException("Username and password cannot be null!");
        }

        // Send a password authentication request
        ByteArrayWriter baw = new ByteArrayWriter();

        try {
            baw.write(0);
            baw.writeString(password);

            SshMsgUserAuthRequest msg =
                    new SshMsgUserAuthRequest(getUsername(), serviceToStart,
                    "password", baw.toByteArray());

            transport.sendMessage(msg, this);
        } catch (IOException ioe) {
            throw new AuthenticationProtocolException("Failed to write request data!");
        }
    }

    public Properties getPersistableProperties() {
      Properties properties = new Properties();
      if(getUsername()!=null)
        properties.setProperty("Username", getUsername());

      return properties;
    }

    public void setPersistableProperties(Properties properties) {
      setUsername(properties.getProperty("Username"));
      if(properties.getProperty("Password")!=null)
        setPassword(properties.getProperty("Password"));
    }

    public boolean canAuthenticate() {
      return (getUsername()!=null && password !=null);
    }
}
