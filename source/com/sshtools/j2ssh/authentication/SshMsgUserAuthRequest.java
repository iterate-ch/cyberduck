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

import java.io.IOException;

import com.sshtools.j2ssh.transport.InvalidMessageException;
import com.sshtools.j2ssh.transport.SshMessage;
import com.sshtools.j2ssh.io.ByteArrayReader;
import com.sshtools.j2ssh.io.ByteArrayWriter;

/**
 *  Implements the SSH_MSG_USERAUTH_REQUEST message
 *
 *@author     <A HREF="mailto:lee@sshtools.com">Lee David Painter</A>
 *@created    20 December 2002
 *@version    $Id: SshMsgUserAuthRequest.java,v 1.8 2002/12/09 23:08:25 martianx
 *      Exp $
 */
public class SshMsgUserAuthRequest
         extends SshMessage {
    /**
     *  The message id
     */
    public final static int SSH_MSG_USERAUTH_REQUEST = 50;
    private String methodName;
    private String serviceName;
    private String username;
    private byte requestData[];


    /**
     *  Constructs the message
     */
    public SshMsgUserAuthRequest() {
        super(SSH_MSG_USERAUTH_REQUEST);
    }


    /**
     *  Constructs the message with the details supplied.
     *
     *@param  username     The username
     *@param  serviceName  The service to start after authentication
     *@param  methodName   The authentication method name
     *@param  requestData  the method specific request data
     */
    public SshMsgUserAuthRequest(String username, String serviceName,
            String methodName, byte requestData[]) {
        super(SSH_MSG_USERAUTH_REQUEST);

        this.username = username;
        this.serviceName = serviceName;
        this.methodName = methodName;
        this.requestData = requestData;
    }


    /**
     *  Gets the message name for debugging.
     *
     *@return
     */
    public String getMessageName() {
        return "SSH_MSG_USERAUTH_REQUEST";
    }


    /**
     *  Gets the method name for debuggin
     *
     *@return    The method name
     */
    public String getMethodName() {
        return methodName;
    }


    /**
     *  Gets the method specific request data
     *
     *@return    a byte array containing the request data
     */
    public byte[] getRequestData() {
        return requestData;
    }


    /**
     *  Gets the service name.
     *
     *@return    the service name to start
     */
    public String getServiceName() {
        return serviceName;
    }


    /**
     *  Gets the username.
     *
     *@return    the username
     */
    public String getUsername() {
        return username;
    }


    /**
     *  Constucts a byte array containing the message.
     *
     *@param  baw                          The byte array being written
     *@exception  InvalidMessageException  if the message is invalid or fails to
     *      write the data
     */
    protected void constructByteArray(ByteArrayWriter baw)
             throws InvalidMessageException {
        try {
            baw.writeString(username);
            baw.writeString(serviceName);
            baw.writeString(methodName);

            if (requestData != null) {
                baw.write(requestData);
            }
        } catch (IOException ioe) {
            throw new InvalidMessageException("Invalid message data");
        }
    }


    /**
     *  Constructs the message from a byte array.
     *
     *@param  bar                          The byte array being read.
     *@exception  InvalidMessageException  if the message is invalid or fails to
     *      read the data
     */
    protected void constructMessage(ByteArrayReader bar)
             throws InvalidMessageException {
        try {
            username = bar.readString();
            serviceName = bar.readString();
            methodName = bar.readString();

            if (bar.available() > 0) {
                requestData = new byte[bar.available()];
                bar.read(requestData);
            }
        } catch (IOException ioe) {
            throw new InvalidMessageException("Invalid message data");
        }
    }
}
