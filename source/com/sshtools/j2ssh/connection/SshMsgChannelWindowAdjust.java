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
package com.sshtools.j2ssh.connection;

import java.io.IOException;

import com.sshtools.j2ssh.transport.InvalidMessageException;
import com.sshtools.j2ssh.transport.SshMessage;
import com.sshtools.j2ssh.io.ByteArrayReader;
import com.sshtools.j2ssh.io.ByteArrayWriter;

/**
 *  Implements the SSH_MSG_CHANNEL_WINDOW_ADJUST message.
 *
 *@author     <A HREF="mailto:lee@sshtools.com">Lee David Painter</A>
 *@created    20 December 2002
 *@version    $Id: SshMsgChannelWindowAdjust.java,v 1.7 2002/12/10 00:07:30
 *      martianx Exp $
 */
public class SshMsgChannelWindowAdjust
         extends SshMessage {
    /**
     *  The message id
     */
    protected final static int SSH_MSG_CHANNEL_WINDOW_ADJUST = 93;
    private long bytesToAdd;
    private long recipientChannel;


    /**
     *  Constructor for the SshMsgChannelWindowAdjust object
     *
     *@param  recipientChannel  The recipient channel id
     *@param  bytesToAdd        The number of bytes to add to the window space
     */
    public SshMsgChannelWindowAdjust(long recipientChannel, long bytesToAdd) {
        super(SSH_MSG_CHANNEL_WINDOW_ADJUST);

        this.recipientChannel = recipientChannel;
        this.bytesToAdd = bytesToAdd;
    }


    /**
     *  Constructor for the SshMsgChannelWindowAdjust object
     */
    public SshMsgChannelWindowAdjust() {
        super(SSH_MSG_CHANNEL_WINDOW_ADJUST);
    }


    /**
     *  Gets the number of bytes to add to the window space
     *
     *@return    The number of bytes to add
     */
    public long getBytesToAdd() {
        return bytesToAdd;
    }


    /**
     *  Gets the message name for debugging
     *
     *@return
     */
    public String getMessageName() {
        return "SSH_MSG_CHANNEL_WINDOW_ADJUST";
    }


    /**
     *  Gets the recipient channel id
     *
     *@return    The recipient channel id
     */
    public long getRecipientChannel() {
        return recipientChannel;
    }


    /**
     *  Abstract method implementation to construct a byte array containing the
     *  message data.
     *
     *@param  baw                       The byte array being written to
     *@throws  InvalidMessageException  if the data cannot be written
     */
    protected void constructByteArray(ByteArrayWriter baw)
             throws InvalidMessageException {
        try {
            baw.writeInt(recipientChannel);
            baw.writeInt(bytesToAdd);
        } catch (IOException ioe) {
            throw new InvalidMessageException("Invalid message data");
        }
    }


    /**
     *  Abstract method implementation to construct the message from a byte
     *  array.
     *
     *@param  bar                       The byte array being read
     *@throws  InvalidMessageException  if the data cannot be read
     */
    protected void constructMessage(ByteArrayReader bar)
             throws InvalidMessageException {
        try {
            recipientChannel = bar.readInt();
            bytesToAdd = bar.readInt();
        } catch (IOException ioe) {
            throw new InvalidMessageException("Invalid message data");
        }
    }
}
