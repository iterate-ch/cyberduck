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

import com.sshtools.j2ssh.io.ByteArrayReader;
import com.sshtools.j2ssh.transport.InvalidMessageException;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;


/**
 *
 *
 * @author $author$
 * @version $Revision$
 */
public class SubsystemOutputStream extends OutputStream {
    // Temporary storage buffer to build up a message
    ByteArrayOutputStream buffer = new ByteArrayOutputStream();
    SubsystemMessageStore messageStore;
    int messageStart = 0;

    /**
     * Creates a new SubsystemOutputStream object.
     *
     * @param messageStore
     */
    public SubsystemOutputStream(SubsystemMessageStore messageStore) {
        super();
        this.messageStore = messageStore;
    }

    /**
     *
     *
     * @param b
     * @param off
     * @param len
     *
     * @throws IOException
     */
    public void write(byte[] b, int off, int len) throws IOException {
        // Write the data
        super.write(b, off, len);
        processMessage();
    }

    /**
     *
     *
     * @param b
     *
     * @throws IOException
     */
    public void write(int b) throws IOException {
        buffer.write(b);
    }

    private void processMessage() throws IOException {
        // Now try to process a message
        if (buffer.size() > (messageStart + 4)) {
            int messageLength = (int) ByteArrayReader.readInt(buffer.toByteArray(),
                    messageStart);

            if (messageLength <= (buffer.size() - 4)) {
                byte[] msgdata = new byte[messageLength];

                // Process a message
                System.arraycopy(buffer.toByteArray(), messageStart + 4,
                    msgdata, 0, messageLength);

                try {
                    messageStore.addMessage(msgdata);
                } catch (InvalidMessageException ime) {
                    throw new IOException(
                        "An invalid message was encountered in the outputstream: " +
                        ime.getMessage());
                }

                if (messageLength == (buffer.size() - 4)) {
                    buffer.reset();
                    messageStart = 0;
                } else {
                    messageStart = messageLength + 4;
                }
            }
        }
    }
}
