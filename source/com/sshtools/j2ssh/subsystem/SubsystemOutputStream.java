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
package com.sshtools.j2ssh.subsystem;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;

import com.sshtools.j2ssh.transport.InvalidMessageException;
import com.sshtools.j2ssh.io.ByteArrayReader;

/**
 *  Implements a Subsystem Outputstream. This is used by the SubsystemServer
 *  class to receive data from the remote computer. The SessionChannelServer
 *  class writes any received data to this Outputstream
 *
 *@author     <A HREF="mailto:lee@sshtools.com">Lee David Painter</A>
 *@created    20 December 2002
 *@version    $Id: SubsystemOutputStream.java,v 1.6 2002/12/10 00:07:30 martianx
 *      Exp $
 */
public class SubsystemOutputStream
         extends OutputStream {
    // Temporary storage buffer to build up a message
    ByteArrayOutputStream buffer = new ByteArrayOutputStream();
    SubsystemMessageStore messageStore;
    int messageStart = 0;


    /**
     *  Creates an outputstream with the supplied message store instance
     *  receiveing all incoming messages
     *
     *@param  messageStore  the destination for the OutputStream
     */
    public SubsystemOutputStream(SubsystemMessageStore messageStore) {
        super();
        this.messageStore = messageStore;
    }


    /**
     *  Overide the standard write method so that after the data has been
     *  written the Outputstream can process a message and add it to the message
     *  store
     *
     *@param  b             the message data array
     *@param  off           the offset of the message in the array
     *@param  len           the length of the message in the array
     *@throws  IOException  if the write operation fails
     */
    public void write(byte b[], int off, int len)
             throws IOException {
        // Write the data
        super.write(b, off, len);

        processMessage();
    }


    /**
     *  Writes a single byte to the outputstream
     *
     *@param  b             the byte value 0-255
     *@throws  IOException  if the write operation fails
     */
    public void write(int b)
             throws IOException {
        buffer.write(b);
    }


    /**
     *  Process a message and adds it to the message store
     *
     *@throws  IOException  if an invalid message is encountered
     */
    private void processMessage()
             throws IOException {
        // Now try to process a message
        if (buffer.size() > (messageStart + 4)) {
            int messageLength =
                    ByteArrayReader.readInt(buffer.toByteArray(), messageStart);

            if (messageLength <= (buffer.size() - 4)) {
                byte msgdata[] = new byte[messageLength];

                // Process a message
                System.arraycopy(buffer.toByteArray(), messageStart + 4,
                        msgdata, 0, messageLength);

                try {
                    messageStore.addMessage(msgdata);
                } catch (InvalidMessageException ime) {
                    throw new IOException("An invalid message was encountered in the outputstream");
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
