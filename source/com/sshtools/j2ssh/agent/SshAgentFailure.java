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
package com.sshtools.j2ssh.agent;

import com.sshtools.j2ssh.io.ByteArrayReader;
import com.sshtools.j2ssh.io.ByteArrayWriter;
import com.sshtools.j2ssh.subsystem.SubsystemMessage;
import com.sshtools.j2ssh.transport.InvalidMessageException;

import java.io.IOException;


class SshAgentFailure extends SubsystemMessage {
    /**  */
    public static final int SSH_AGENT_FAILURE = 102;

    /**  */
    public static final int SSH_AGENT_ERROR_TIMEOUT = 1;

    /**  */
    public static final int SSH_AGENT_ERROR_KEY_NOT_FOUND = 2;

    /**  */
    public static final int SSH_AGENT_ERROR_DECRYPT_FAILED = 3;

    /**  */
    public static final int SSH_AGENT_ERROR_SIZE_ERROR = 4;

    /**  */
    public static final int SSH_AGENT_ERROR_KEY_NOT_SUITABLE = 5;

    /**  */
    public static final int SSH_AGENT_ERROR_DENIED = 6;

    /**  */
    public static final int SSH_AGENT_ERROR_FAILURE = 7;

    /**  */
    public static final int SSH_AGENT_ERROR_UNSUPPORTED_OP = 8;
    private int errorcode;

    /**
     * Creates a new SshAgentFailure object.
     */
    public SshAgentFailure() {
        super(SSH_AGENT_FAILURE);
    }

    /**
     * Creates a new SshAgentFailure object.
     *
     * @param errorcode
     */
    public SshAgentFailure(int errorcode) {
        super(SSH_AGENT_FAILURE);
        this.errorcode = errorcode;
    }

    /**
     *
     *
     * @return
     */
    public String getMessageName() {
        return "SSH_AGENT_FAILURE";
    }

    /**
     *
     *
     * @return
     */
    public int getErrorCode() {
        return errorcode;
    }

    /**
     *
     *
     * @param baw
     *
     * @throws java.io.IOException
     * @throws com.sshtools.j2ssh.transport.InvalidMessageException DOCUMENT
     *         ME!
     * @throws InvalidMessageException
     */
    public void constructByteArray(ByteArrayWriter baw)
        throws java.io.IOException, 
            com.sshtools.j2ssh.transport.InvalidMessageException {
        try {
            baw.writeInt(errorcode);
        } catch (IOException ioe) {
            throw new InvalidMessageException(ioe.getMessage());
        }
    }

    /**
     *
     *
     * @param bar
     *
     * @throws java.io.IOException
     * @throws com.sshtools.j2ssh.transport.InvalidMessageException DOCUMENT
     *         ME!
     * @throws InvalidMessageException
     */
    public void constructMessage(ByteArrayReader bar)
        throws java.io.IOException, 
            com.sshtools.j2ssh.transport.InvalidMessageException {
        try {
            errorcode = (int) bar.readInt();
        } catch (IOException ioe) {
            throw new InvalidMessageException(ioe.getMessage());
        }
    }
}
