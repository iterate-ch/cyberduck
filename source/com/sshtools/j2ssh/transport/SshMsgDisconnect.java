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
package com.sshtools.j2ssh.transport;

import com.sshtools.j2ssh.io.ByteArrayReader;
import com.sshtools.j2ssh.io.ByteArrayWriter;

import java.io.IOException;


/**
 *
 *
 * @author $author$
 * @version $Revision$
 */
public class SshMsgDisconnect extends SshMessage {
    /**  */
    protected final static int SSH_MSG_DISCONNECT = 1;

    /**  */
    public final static int HOST_NOT_ALLOWED = 1;

    /**  */
    public final static int PROTOCOL_ERROR = 2;

    /**  */
    public final static int KEY_EXCHANGE_FAILED = 3;

    /**  */
    public final static int RESERVED = 4;

    /**  */
    public final static int MAC_ERROR = 5;

    /**  */
    public final static int COMPRESSION_ERROR = 6;

    /**  */
    public final static int SERVICE_NOT_AVAILABLE = 7;

    /**  */
    public final static int PROTOCOL_VERSION_NOT_SUPPORTED = 8;

    /**  */
    public final static int HOST_KEY_NOT_VERIFIABLE = 9;

    /**  */
    public final static int CONNECTION_LOST = 10;

    /**  */
    public final static int BY_APPLICATION = 11;

    /**  */
    public final static int TOO_MANY_CONNECTIONS = 12;

    /**  */
    public final static int AUTH_CANCELLED_BY_USER = 13;

    /**  */
    public final static int NO_MORE_AUTH_METHODS_AVAILABLE = 14;

    /**  */
    public final static int ILLEGAL_USER_NAME = 15;

    // The readble version of the disconneciton reason
    private String desc;

    // The language tag
    private String langTag;

    // Holds the reason for disconnection
    private int reasonCode;

    /**
     * Creates a new SshMsgDisconnect object.
     *
     * @param reasonCode
     * @param desc
     * @param langTag
     */
    public SshMsgDisconnect(int reasonCode, String desc, String langTag) {
        super(SSH_MSG_DISCONNECT);

        // Store the message values
        this.reasonCode = reasonCode;
        this.desc = desc;
        this.langTag = langTag;
    }

    /**
     * Creates a new SshMsgDisconnect object.
     */
    public SshMsgDisconnect() {
        super(SSH_MSG_DISCONNECT);
    }

    /**
     *
     *
     * @return
     */
    public String getDescription() {
        return desc;
    }

    /**
     *
     *
     * @return
     */
    public String getLanguageTag() {
        return langTag;
    }

    /**
     *
     *
     * @return
     */
    public String getMessageName() {
        return "SSH_MSG_DISCONNECT";
    }

    /**
     *
     *
     * @return
     */
    public int getReasonCode() {
        return reasonCode;
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
            baw.writeInt(reasonCode);
            baw.writeString(desc);
            baw.writeString(langTag);
        } catch (IOException ioe) {
            throw new InvalidMessageException("Error writing message data: " +
                ioe.getMessage());
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
            // Save the values
            reasonCode = (int) bar.readInt();
            desc = bar.readString();
            langTag = bar.readString();
        } catch (IOException ioe) {
            throw new InvalidMessageException("Error reading message data: " +
                ioe.getMessage());
        }
    }
}
