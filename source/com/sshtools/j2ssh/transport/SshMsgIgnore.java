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
package com.sshtools.j2ssh.transport;

import java.io.IOException;
import com.sshtools.j2ssh.io.ByteArrayReader;
import com.sshtools.j2ssh.io.ByteArrayWriter;

/**
 *  <p>
 *
 *  Implements the Ignored Data Message. </p> <p>
 *
 *  byte SSH_MSG_IGNORE<br>
 *  string data </p> <p>
 *
 *  All implementations MUST understand (and ignore) this message at any time
 *  (after receiving the protocol version). No implementation is required to
 *  send them. This message can be used as an additional protection measure
 *  against advanced traffic analysis techniques. </p>
 *
 *@author     <A HREF="mailto:lee@sshtools.com">Lee David Painter</A>
 *@created    20 December 2002
 *@version    $Id$
 */
class SshMsgIgnore
         extends SshMessage {
    /**
     *  The message id for the message
     */
    protected final static int SSH_MSG_IGNORE = 2;
    private String data;


    /**
     *  Constructs the message ready for sending.
     *
     *@param  data  A string of random data.
     */
    public SshMsgIgnore(String data) {
        super(SSH_MSG_IGNORE);
        this.data = data;
    }


    /**
     *  Default constructor
     */
    public SshMsgIgnore() {
        super(SSH_MSG_IGNORE);
    }


    /**
     *  Gets the data attribute of the ignore message
     *
     *@return    The data value.
     */
    public String getData() {
        return data;
    }


    /**
     *  Gets the message name for debugging
     *
     *@return    "SSH_MSG_IGNORE"
     */
    public String getMessageName() {
        return "SSH_MSG_IGNORE";
    }


    /**
     *  Abstract method implementation to construct a byte array containing the
     *  message data.
     *
     *@param  baw                          The byte array to store the message.
     *@exception  InvalidMessageException  if the message is invalid
     */
    protected void constructByteArray(ByteArrayWriter baw)
             throws InvalidMessageException {
        try {
            baw.writeString(data);
        } catch (IOException ioe) {
            throw new InvalidMessageException("Error occurred writing message data: "
                    + ioe.getMessage());
        }
    }


    /**
     *  Abstract method implementation to constrcut the message from a byte
     *  array.
     *
     *@param  bar                          The byte array containing the message
     *      data.
     *@exception  InvalidMessageException  if the message is invalid
     */
    protected void constructMessage(ByteArrayReader bar)
             throws InvalidMessageException {
        try {
            data = bar.readString();
        } catch (IOException ioe) {
            throw new InvalidMessageException("Error occurred reading message data: "
                    + ioe.getMessage());
        }
    }
}
