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
 *  An implementation MUST respond to all unrecognized messages with an
 *  SSH_MSG_UNIMPLEMENTED message in the order in which the messages were
 *  received. Such messages MUST be otherwise ignored. Later protocol versions
 *  may define other meanings for these message types. </p> <p>
 *
 *  byte SSH_MSG_UNIMPLEMENTED<br>
 *  uint32 packet sequence number of rejected message </p>
 *
 *@author     <A HREF="mailto:lee@sshtools.com">Lee David Painter</A>
 *@created    20 December 2002
 *@version    $Id: SshMsgUnimplemented.java,v 1.5 2002/12/10 00:07:32 martianx
 *      Exp $
 */
class SshMsgUnimplemented
         extends SshMessage {
    /**
     *  The message id of the message
     */
    protected final static int SSH_MSG_UNIMPLEMENTED = 3;

    // The sequence no of the message
    private long sequenceNo;


    /**
     *  Constructs the message ready for sending
     *
     *@param  sequenceNo  The sequence no of the unimplemented message.
     */
    public SshMsgUnimplemented(long sequenceNo) {
        super(SSH_MSG_UNIMPLEMENTED);
        this.sequenceNo = sequenceNo;
    }


    /**
     *  Constructs the message from data received.
     */
    public SshMsgUnimplemented() {
        super(SSH_MSG_UNIMPLEMENTED);
    }


    /**
     *  Gets the message name for debugging
     *
     *@return    "SSH_MSG_UNIMPLEMENTED"
     */
    public String getMessageName() {
        return "SSH_MSG_UNIMPLEMENTED";
    }


    /**
     *  Gets the sequence number of the unimplemented message
     *
     *@return    The sequence number
     */
    public long getSequenceNo() {
        return sequenceNo;
    }


    /**
     *  Abstract method implementation to construct a byte array containing the
     *  data
     *
     *@param  baw                          The byte array being written.
     *@exception  InvalidMessageException  if the message is invalid
     */
    protected void constructByteArray(ByteArrayWriter baw)
             throws InvalidMessageException {
        try {
            baw.writeInt(sequenceNo);
        } catch (IOException ioe) {
            throw new InvalidMessageException("Error extracting SSH_MSG_UNIMPLMENTED, expected int value");
        }
    }


    /**
     *  Abstract method implementation to construct the message from received
     *  data.
     *
     *@param  bar                          The data being read.
     *@exception  InvalidMessageException  if the message is invalid
     */
    protected void constructMessage(ByteArrayReader bar)
             throws InvalidMessageException {
        try {
            sequenceNo = bar.readInt();
        } catch (IOException ioe) {
            throw new InvalidMessageException("Error contructing SSH_MSG_UNIMPLEMENTED, expected int value");
        }
    }
}
