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

import java.io.IOException;
import java.io.InputStream;

import com.sshtools.j2ssh.transport.InvalidMessageException;
import com.sshtools.j2ssh.io.ByteArrayWriter;

/**
 *  <p>
 *
 *  Implements a Subsystem inputstream. This is used by the SubsystemServer
 *  class to output data to the remote computer. The SessionChannelServer class
 *  listens on this Inputstream and sends any data through the SSH connection to
 *  the remote computer<br>
 *  This class collects the next available message from the outgoing message
 *  store and makes it available for reading one byte at a time. When the whole
 *  message has been read the Inputstream collects the next available message
 *  and the process starts again. This ensures that each message gets sent
 *  individually </p>
 *
 *@author     <A HREF="mailto:lee@sshtools.com">Lee David Painter</A>
 *@created    20 December 2002
 *@version    $Id: SubsystemInputStream.java,v 1.5 2002/12/10 00:07:30 martianx
 *      Exp $
 */
public class SubsystemInputStream
         extends InputStream {
    byte msgdata[];
    int currentPos = 0;
    private SubsystemMessageStore messageStore;


    /**
     *  Creates the Inputstream
     *
     *@param  messageStore  The outgoing message store to listen on
     */
    public SubsystemInputStream(SubsystemMessageStore messageStore) {
        this.messageStore = messageStore;
    }


    /**
     *  Returns the number of bytes currently available to read
     *
     *@return    the number of available bytes
     */
    public int available() {
        if (msgdata == null) {
            return 0;
        }

        return msgdata.length - currentPos;
    }


    /**
     *  Reads a byte from the Inputstream, blocks untill a byte becomes
     *  available.
     *
     *@return               0-255 or -1 if EOF
     *@throws  IOException  if the stream has failed
     */
    public int read()
             throws IOException {
        if (msgdata == null) {
            collectNextMessage();
        }

        if (currentPos >= msgdata.length) {
            collectNextMessage();
        }

        return msgdata[currentPos++];
    }


    /**
     *  Collects the next message from the message store and makes it available
     *  for reading
     *
     *@throws  IOException  if the stream fails for example becuase of an
     *      invalid message
     */
    private void collectNextMessage()
             throws IOException {
        SubsystemMessage msg = messageStore.nextMessage();

        try {
            ByteArrayWriter baw = new ByteArrayWriter();
            byte data[] = msg.toByteArray();
            baw.writeInt(data.length);
            baw.write(data);
            msgdata = baw.toByteArray();
        } catch (InvalidMessageException ime) {
            throw new IOException("An invalid message was encountered in the inputstream");
        }

        currentPos = 0;
    }
}
