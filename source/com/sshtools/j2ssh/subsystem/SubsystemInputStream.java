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
import java.io.InputStream;

import com.sshtools.j2ssh.io.ByteArrayWriter;
import com.sshtools.j2ssh.transport.InvalidMessageException;


/**
 * @author $author$
 * @version $Revision$
 */
public class SubsystemInputStream extends InputStream {
    byte[] msgdata;
    int currentPos = 0;
    private SubsystemMessageStore messageStore;

    /**
     * Creates a new SubsystemInputStream object.
     *
     * @param messageStore
     */
    public SubsystemInputStream(SubsystemMessageStore messageStore) {
        this.messageStore = messageStore;
    }

    /**
     * @return
     */
    public int available() {
        if (msgdata == null) {
            return 0;
        }

        return msgdata.length - currentPos;
    }

    /**
     * @return
     * @throws IOException
     */
    public int read() throws IOException {
        if (msgdata == null) {
            collectNextMessage();
        }

        if (currentPos >= msgdata.length) {
            collectNextMessage();
        }

        return msgdata[currentPos++] & 0xFF;
    }

    private void collectNextMessage() throws IOException {
        SubsystemMessage msg = messageStore.nextMessage();

        try {
            ByteArrayWriter baw = new ByteArrayWriter();
            byte[] data = msg.toByteArray();
            baw.writeInt(data.length);
            baw.write(data);
            msgdata = baw.toByteArray();
        }
        catch (InvalidMessageException ime) {
            throw new IOException("An invalid message was encountered in the inputstream");
        }

        currentPos = 0;
    }
}
