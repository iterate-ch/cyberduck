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
package com.sshtools.j2ssh.subsystem;

import java.io.IOException;

import com.sshtools.j2ssh.io.ByteArrayReader;
import com.sshtools.j2ssh.io.ByteArrayWriter;
import com.sshtools.j2ssh.transport.InvalidMessageException;


/**
 * @author $author$
 * @version $Revision$
 */
public abstract class SubsystemMessage {
    private int type;

    /**
     * Creates a new SubsystemMessage object.
     *
     * @param type
     */
    public SubsystemMessage(int type) {
        this.type = type;
    }

    /**
     * @return
     */
    public abstract String getMessageName();

    /**
     * @return
     */
    public int getMessageType() {
        return type;
    }

    /**
     * @param baw
     * @throws InvalidMessageException
     * @throws IOException
     */
    public abstract void constructByteArray(ByteArrayWriter baw)
            throws InvalidMessageException, IOException;

    /**
     * @param bar
     * @throws InvalidMessageException
     * @throws IOException
     */
    public abstract void constructMessage(ByteArrayReader bar)
            throws InvalidMessageException, IOException;

    /**
     * @param data
     * @throws InvalidMessageException
     */
    public void fromByteArray(byte[] data) throws InvalidMessageException {
        try {
            ByteArrayReader bar = new ByteArrayReader(data);

            if (bar.available() > 0) {
                type = bar.read();
                constructMessage(bar);
            }
            else {
                throw new InvalidMessageException("Not enough message data to complete the message");
            }
        }
        catch (IOException ioe) {
            throw new InvalidMessageException("The message data cannot be read!");
        }
    }

    /**
     * @return
     * @throws InvalidMessageException
     */
    public byte[] toByteArray() throws InvalidMessageException {
        try {
            ByteArrayWriter baw = new ByteArrayWriter();
            baw.write(type);
            constructByteArray(baw);

            return baw.toByteArray();
        }
        catch (IOException ioe) {
            throw new InvalidMessageException("The message data cannot be written!");
        }
    }
}
