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
package com.sshtools.j2ssh.connection;

import com.sshtools.j2ssh.io.ByteArrayReader;
import com.sshtools.j2ssh.io.ByteArrayWriter;
import com.sshtools.j2ssh.transport.InvalidMessageException;
import com.sshtools.j2ssh.transport.SshMessage;

import java.io.IOException;


/**
 *
 *
 * @author $author$
 * @version $Revision$
 */
public class SshMsgGlobalRequest extends SshMessage {
    /**  */
    protected final static int SSH_MSG_GLOBAL_REQUEST = 80;
    private String requestName;
    private byte[] requestData;
    private boolean wantReply;

    /**
     * Creates a new SshMsgGlobalRequest object.
     *
     * @param requestName
     * @param wantReply
     * @param requestData
     */
    public SshMsgGlobalRequest(String requestName, boolean wantReply,
        byte[] requestData) {
        super(SSH_MSG_GLOBAL_REQUEST);
        this.requestName = requestName;
        this.wantReply = wantReply;
        this.requestData = requestData;
    }

    /**
     * Creates a new SshMsgGlobalRequest object.
     */
    public SshMsgGlobalRequest() {
        super(SSH_MSG_GLOBAL_REQUEST);
    }

    /**
     *
     *
     * @return
     */
    public String getMessageName() {
        return "SSH_MSG_GLOBAL_REQUEST";
    }

    /**
     *
     *
     * @return
     */
    public byte[] getRequestData() {
        return requestData;
    }

    /**
     *
     *
     * @return
     */
    public String getRequestName() {
        return requestName;
    }

    /**
     *
     *
     * @return
     */
    public boolean getWantReply() {
        return wantReply;
    }

    /**
     *
     *
     * @param baw
     *
     * @throws InvalidMessageException
     */
    protected void constructByteArray(ByteArrayWriter baw)
        throws InvalidMessageException {
        try {
            baw.writeString(requestName);
            baw.write((wantReply ? 1 : 0));

            if (requestData != null) {
                baw.write(requestData);
            }
        } catch (IOException ioe) {
            throw new InvalidMessageException("Invalid message data");
        }
    }

    /**
     *
     *
     * @param bar
     *
     * @throws InvalidMessageException
     */
    protected void constructMessage(ByteArrayReader bar)
        throws InvalidMessageException {
        try {
            requestName = bar.readString();
            wantReply = ((bar.read() == 0) ? false : true);

            if (bar.available() > 0) {
                requestData = new byte[bar.available()];
                bar.read(requestData);
            }
        } catch (IOException ioe) {
            throw new InvalidMessageException("Invalid message data");
        }
    }
}
