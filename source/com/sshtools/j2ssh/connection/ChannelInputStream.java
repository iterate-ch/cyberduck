/*
 * Sshtools - Java SSH2 API
 *
 * Copyright (C) 2002 Lee David Painter.
 *
 * Written by: 2002 Lee David Painter <lee@sshtools.com>
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Library General Public License
 * as published by the Free Software Foundation; either version 2 of
 * the License, or (at your option) any later version.

 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Library General Public License for more details.
 *
 * You should have received a copy of the GNU Library General Public
 * License along with this program; if not, write to the Free Software
 * Foundation, Inc., 675 Mass Ave, Cambridge, MA 02139, USA.
 */
package com.sshtools.j2ssh.connection;

import java.io.IOException;
import java.io.InputStream;

import com.sshtools.j2ssh.transport.SshMessageStore;
import com.sshtools.j2ssh.transport.MessageStoreEOFException;
import com.sshtools.j2ssh.transport.MessageNotAvailableException;


/**
 * This class implements a blocking <code>InputStream</code> for a connection
 * protocol channel.
 *
 * @author <A HREF="mailto:lee@sshtools.com">Lee David Painter</A>
 * @version $Id$
 */
public class ChannelInputStream
    extends InputStream {
    int filter[];
    byte msgdata[];
    int currentPos = 0;
    private SshMessageStore messageStore;
    private Integer type = null;

    /**
     * Creates a new ChannelInputStream object.
     *
     * @param messageStore the message store receiving data
     * @param type the extended channel data type
     */
    public ChannelInputStream(SshMessageStore messageStore, Integer type) {
        this.messageStore = messageStore;
        filter = new int[1];
        this.type = type;
        if(type!=null)
          filter[0] = SshMsgChannelExtendedData.SSH_MSG_CHANNEL_EXTENDED_DATA;
        else
          filter[0] = SshMsgChannelData.SSH_MSG_CHANNEL_DATA;
    }

    /**
     * Creates a new ChannelInputStream on the standard data channel
     * @param messageStore the message store receiving data
     */
    public ChannelInputStream(SshMessageStore messageStore) {
      this(messageStore, null);
    }

    /**
     * Returns the number of bytes currently available to read
     *
     * @return the number of available bytes that can be read without blocking
     */
    public int available() {

      int available = 0;
      if(msgdata!=null)
        available = msgdata.length - currentPos;

      if(available==0) {

        try {

          if(type!=null) {

            SshMsgChannelExtendedData msg =
                (SshMsgChannelExtendedData) messageStore.peekMessage(filter);
            available = msg.getChannelData().length;
          }
          else {
            SshMsgChannelData msg =
                (SshMsgChannelData) messageStore.peekMessage(filter);
            available = msg.getChannelData().length;
          }
        } catch(MessageStoreEOFException mse) {
          available = -1;
        } catch(MessageNotAvailableException mna) {
          available = 0;
        }

      }

      return available;



    }

    /**
     * Closes the InputStream
     *
     * @throws IOException if an error occurs during closing
     */
    public void close()
               throws IOException {
        messageStore.close();
    }

    /**
     * Reads a byte from the Inputstream. If there are no bytes available this
     * method blocks until new bytes arrive.
     *
     * @return the value of the byte read
     *
     * @throws java.io.IOException if an IO error occurs
     */
    public int read()
             throws java.io.IOException {

        try {

          block();

          return msgdata[currentPos++];

        } catch(MessageStoreEOFException mse) {
          return -1;
        }
    }

    /**
     * Reads a byte from the Inputstream. If there are no bytes available this
     * method blocks until new bytes arrive.
     *
     * @param b the byte array to receive the data
     * @param off the offset starting postion to read the data into
     * @param len the length to read
     *
     * @return the number of bytes read
     *
     * @throws IOException if an IO error occurs
     */
    public int read(byte b[], int off, int len)
             throws IOException {

      try {

        block();

        int actual;

        if (available()<len) {
            actual = available();
        } else {
            actual = len;
        }

        System.arraycopy(msgdata, currentPos, b, off, actual);
        currentPos += actual;

        return actual;

      } catch(MessageStoreEOFException mse) {
        return -1;
      }
    }

    /**
     * Attempts to collect the next message from the message store, if no
     * messages are available the method blocks until a new message is
     * received
     *
     * @throws IOException if an IO error occurs
     */
    private void block()
                throws MessageStoreEOFException {


          if (msgdata==null) {
              collectNextMessage();
          }

          if (currentPos>=msgdata.length) {
              collectNextMessage();
          }


    }

    /**
     * Collects the next message from the message store and makes it available
     * for reading
     *
     * @throws IOException if an IO error occurs
     */
    private void collectNextMessage() throws MessageStoreEOFException {

        // Collect the next message
        if(type!=null) {

          SshMsgChannelExtendedData msg =
              (SshMsgChannelExtendedData) messageStore.getMessage(filter);

          if(msg!=null) {
            msgdata = msg.getChannelData();
            currentPos = 0;
          }
        }
        else {
          SshMsgChannelData msg =
              (SshMsgChannelData) messageStore.getMessage(filter);

          if (msg!=null) {
              msgdata = msg.getChannelData();
              currentPos = 0;
          }
        }
    }
}
