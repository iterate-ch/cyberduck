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
package com.sshtools.j2ssh.transport.kex;

import java.io.IOException;
import java.math.BigInteger;

import com.sshtools.j2ssh.io.ByteArrayReader;
import com.sshtools.j2ssh.io.ByteArrayWriter;
import com.sshtools.j2ssh.transport.InvalidMessageException;
import com.sshtools.j2ssh.transport.SshMessage;


/**
 * @author $author$
 * @version $Revision$
 */
public class SshMsgKexDhInit extends SshMessage {
    /**  */
    protected final static int SSH_MSG_KEXDH_INIT = 30;

    // Stores the e value
    private BigInteger e;

    /**
     * Creates a new SshMsgKexDhInit object.
     *
     * @param e
     */
    public SshMsgKexDhInit(BigInteger e) {
        super(SSH_MSG_KEXDH_INIT);
        this.e = e;
    }

    /**
     * Creates a new SshMsgKexDhInit object.
     */
    public SshMsgKexDhInit() {
        super(SSH_MSG_KEXDH_INIT);
    }

    /**
     * @return
     */
    public BigInteger getE() {
        return e;
    }

    /**
     * @return
     */
    public String getMessageName() {
        return "SSH_MSG_KEXDH_INIT";
    }

    /**
     * @param baw
     * @throws InvalidMessageException
     */
    protected void constructByteArray(ByteArrayWriter baw)
            throws InvalidMessageException {
        try {
            baw.writeBigInteger(e);
        }
        catch (IOException ioe) {
            throw new InvalidMessageException("Error writing message data: " +
                    ioe.getMessage());
        }
    }

    /**
     * @param bar
     * @throws InvalidMessageException
     */
    protected void constructMessage(ByteArrayReader bar)
            throws InvalidMessageException {
        try {
            e = bar.readBigInteger();
        }
        catch (IOException ioe) {
            throw new InvalidMessageException("Error reading message data: " +
                    ioe.getMessage());
        }
    }
}
