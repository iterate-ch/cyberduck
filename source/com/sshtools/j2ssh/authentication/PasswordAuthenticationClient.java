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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.io.IOException;

import java.util.Properties;


/**
 *
 *
 * @author $author$
 * @version $Revision$
 */
public class PasswordAuthenticationClient extends SshAuthenticationClient {
    private static Log log = LogFactory.getLog(PasswordAuthenticationClient.class);
    private PasswordChangePrompt changePrompt = null;

    /**  */
    protected String password = null;

    /**
     *
     *
     * @return
     */
    public final String getMethodName() {
        return "password";
    }

    /**
     *
     *
     * @param password
     */
    public final void setPassword(String password) {
        this.password = password;
    }

    /**
     *
     */
    public void reset() {
        password = null;
    }

    /**
     *
     *
     * @param changePrompt
     */
    public void setPasswordChangePrompt(PasswordChangePrompt changePrompt) {
        this.changePrompt = changePrompt;
    }

    /*public boolean showAuthenticationDialog(Component parent)
     throws AuthenticationProtocolException {
     if (password != null) {
         return true;
     }
     // Create the password authentication dialog
     Window w = (Window) SwingUtilities.getAncestorOfClass(Window.class,
        parent);
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
      }*/
    /*public void setAuthenticatedTokens(Map tokens) {
      }*/
    public void authenticate(AuthenticationProtocolClient authentication,
        String serviceToStart) throws IOException, TerminatedStateException {
        if ((getUsername() == null) || (password == null)) {
            throw new AuthenticationProtocolException(
                "Username and password cannot be null!");
        }

        authentication.registerMessage(SshMsgUserAuthPwdChangeReq.class,
            SshMsgUserAuthPwdChangeReq.SSH_MSG_USERAUTH_PWD_CHANGEREQ);

        // Send a password authentication request
        ByteArrayWriter baw = new ByteArrayWriter();
        baw.write(0);
        baw.writeString(password);

        SshMsgUserAuthRequest msg = new SshMsgUserAuthRequest(getUsername(),
                serviceToStart, "password", baw.toByteArray());
        authentication.sendMessage(msg);

        SshMsgUserAuthPwdChangeReq pwd = (SshMsgUserAuthPwdChangeReq) authentication.readMessage(SshMsgUserAuthPwdChangeReq.SSH_MSG_USERAUTH_PWD_CHANGEREQ);

        if (changePrompt != null) {
            String newpassword = changePrompt.changePassword(pwd.getPrompt());

            if (newpassword != null) {
                log.debug("Setting new password");
                baw = new ByteArrayWriter();
                baw.write(1);
                baw.writeString(password);
                baw.writeString(newpassword);
                msg = new SshMsgUserAuthRequest(getUsername(), serviceToStart,
                        "password", baw.toByteArray());
                authentication.sendMessage(msg);
            } else {
                throw new TerminatedStateException(AuthenticationProtocolState.FAILED);
            }
        } else {
            throw new TerminatedStateException(AuthenticationProtocolState.FAILED);
        }
    }

    /**
     *
     *
     * @return
     */
    public Properties getPersistableProperties() {
        Properties properties = new Properties();

        if (getUsername() != null) {
            properties.setProperty("Username", getUsername());
        }

        return properties;
    }

    /**
     *
     *
     * @param properties
     */
    public void setPersistableProperties(Properties properties) {
        setUsername(properties.getProperty("Username"));

        if (properties.getProperty("Password") != null) {
            setPassword(properties.getProperty("Password"));
        }
    }

    /**
     *
     *
     * @return
     */
    public boolean canAuthenticate() {
        return ((getUsername() != null) && (password != null));
    }
}
