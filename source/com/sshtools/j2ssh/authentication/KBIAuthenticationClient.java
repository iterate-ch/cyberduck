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

import java.util.Properties;


/**
 *
 *
 * @author $author$
 * @version $Revision$
 */
public class KBIAuthenticationClient extends SshAuthenticationClient {
    KBIRequestHandler handler;

    /**
     *
     *
     * @return
     */
    public Properties getPersistableProperties() {
        return new Properties();
    }

    /**
     *
     *
     * @param handler
     */
    public void setKBIRequestHandler(KBIRequestHandler handler) {
        this.handler = handler;
    }

    /**
     *
     */
    public void reset() {
    }

    /**
     *
     *
     * @param authentication
     * @param serviceToStart
     *
     * @throws com.sshtools.j2ssh.authentication.TerminatedStateException
     *
     * @throws java.io.IOException
     * @throws AuthenticationProtocolException
     */
    public void authenticate(AuthenticationProtocolClient authentication,
        String serviceToStart)
        throws com.sshtools.j2ssh.authentication.TerminatedStateException, 
            java.io.IOException {
        if (handler == null) {
            throw new AuthenticationProtocolException(
                "A request handler must be set!");
        }

        authentication.registerMessage(SshMsgUserAuthInfoRequest.class,
            SshMsgUserAuthInfoRequest.SSH_MSG_USERAUTH_INFO_REQUEST);

        // Send the authentication request
        ByteArrayWriter baw = new ByteArrayWriter();
        baw.writeString("");
        baw.writeString("");

        SshMessage msg = new SshMsgUserAuthRequest(getUsername(),
                serviceToStart, getMethodName(), baw.toByteArray());
        authentication.sendMessage(msg);

        // Read a message
        while (true) {
            msg = authentication.readMessage(SshMsgUserAuthInfoRequest.SSH_MSG_USERAUTH_INFO_REQUEST);

            if (msg instanceof SshMsgUserAuthInfoRequest) {
                SshMsgUserAuthInfoRequest request = (SshMsgUserAuthInfoRequest) msg;
                KBIPrompt[] prompts = request.getPrompts();
                handler.showPrompts(request.getName(),
                    request.getInstruction(), prompts);

                // Now send the response message
                msg = new SshMsgUserAuthInfoResponse(prompts);
                authentication.sendMessage(msg);
            } else {
                throw new AuthenticationProtocolException(
                    "Unexpected authentication message " +
                    msg.getMessageName());
            }
        }
    }

    /**
     *
     *
     * @return
     */
    public boolean canAuthenticate() {
        return true;
    }

    /**
     *
     *
     * @return
     */
    public String getMethodName() {
        return "keyboard-interactive";
    }

    /**
     *
     *
     * @param properties
     */
    public void setPersistableProperties(Properties properties) {
    }

    /* public boolean showAuthenticationDialog(Component parent)
      throws java.io.IOException {
      final Component myparent = parent;
      this.handler = new KBIRequestHandlerDialog();
      //        this.handler = new KBIRequestHandler() {
      //                    public void showPrompts(String name, String instructions,
      //                        KBIPrompt[] prompts) {
      //                        if (prompts != null) {
      //                            for (int i = 0; i < prompts.length; i++) {
      //                                // We can echo the response back to the client
      //                                prompts[i].setResponse((JOptionPane
      //                                    .showInputDialog(myparent,
      //                                        prompts[i].getPrompt(), name,
      //                                        JOptionPane.QUESTION_MESSAGE)));
      //                            }
      //                        }
      //                    }
      //                };
      return true;
       }*/
}
