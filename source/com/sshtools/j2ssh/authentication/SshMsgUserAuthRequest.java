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

import com.sshtools.j2ssh.io.ByteArrayReader;
import com.sshtools.j2ssh.io.ByteArrayWriter;
import com.sshtools.j2ssh.transport.InvalidMessageException;
import com.sshtools.j2ssh.transport.SshMessage;

import java.io.IOException;


/**
 * @author $author$
 * @version $Revision$
 */
public class SshMsgUserAuthRequest extends SshMessage {
    /**  */
    public final static int SSH_MSG_USERAUTH_REQUEST = 50;
    private String methodName;
    private String serviceName;
    private String username;
    private byte[] requestData;

    /**
     * Creates a new SshMsgUserAuthRequest object.
     */
    public SshMsgUserAuthRequest() {
        super(SSH_MSG_USERAUTH_REQUEST);
    }

    /**
     * Creates a new SshMsgUserAuthRequest object.
     *
     * @param username
     * @param serviceName
     * @param methodName
     * @param requestData
     */
    public SshMsgUserAuthRequest(String username, String serviceName,
                                 String methodName, byte[] requestData) {
        super(SSH_MSG_USERAUTH_REQUEST);
        this.username = username;
        this.serviceName = serviceName;
        this.methodName = methodName;
        this.requestData = requestData;
    }

    /**
     * @return
     */
    public String getMessageName() {
        return "SSH_MSG_USERAUTH_REQUEST";
    }

    /**
     * @return
     */
    public String getMethodName() {
        return methodName;
    }

    /**
     * @return
     */
    public byte[] getRequestData() {
        return requestData;
    }

    /**
     * @return
     */
    public String getServiceName() {
        return serviceName;
    }

    /**
     * @return
     */
    public String getUsername() {
        return username;
    }

    /**
     * @param baw
     * @throws InvalidMessageException
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
        }
        catch (IOException ioe) {
            throw new InvalidMessageException("Invalid message data");
        }
    }

    /**
     * @param bar
     * @throws InvalidMessageException
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
        }
        catch (IOException ioe) {
            throw new InvalidMessageException("Invalid message data");
        }
    }
}
