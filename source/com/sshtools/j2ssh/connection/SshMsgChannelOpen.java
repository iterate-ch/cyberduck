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
 *  Implements the SSH_MSG_CHANNEL_OPEN message.
 *
 *@author     <A HREF="mailto:lee@sshtools.com">Lee David Painter</A>
 *@created    20 December 2002
 *@version    $Id: SshMsgChannelOpen.java,v 1.7 2002/12/10 00:07:30 martianx Exp
 *      $
 */
public class SshMsgChannelOpen
         extends SshMessage {
    /**
     *  The message id
     */
    protected final static int SSH_MSG_CHANNEL_OPEN = 90;
    private String channelType;
    private byte channelData[];
    private long initialWindowSize;
    private long maximumPacketSize;
    private long senderChannelId;


    /**
     *  Constructor for the SshMsgChannelOpen object
     *
     *@param  channelType        The channel type (i.e. 'session')
     *@param  senderChannelId    The senders channel id
     *@param  initialWindowSize  The initial data window space
     *@param  maximumPacketSize  The maximum packet size
     *@param  channelData        The channel specific data
     */
    public SshMsgChannelOpen(String channelType, long senderChannelId,
            long initialWindowSize, long maximumPacketSize,
            byte channelData[]) {
        super(SSH_MSG_CHANNEL_OPEN);

        this.channelType = channelType;
        this.senderChannelId = senderChannelId;
        this.initialWindowSize = initialWindowSize;
        this.maximumPacketSize = maximumPacketSize;
        this.channelData = channelData;
    }


    /**
     *  Constructor for the SshMsgChannelOpen object
     */
    public SshMsgChannelOpen() {
        super(SSH_MSG_CHANNEL_OPEN);
    }


    /**
     *  Gets the channel specific data
     *
     *@return    The channel data value
     */
    public byte[] getChannelData() {
        return channelData;
    }


    /**
     *  Gets the channel type
     *
     *@return    The channel type
     */
    public String getChannelType() {
        return channelType;
    }


    /**
     *  Gets the initial window size
     *
     *@return    The initial window size
     */
    public long getInitialWindowSize() {
        return initialWindowSize;
    }


    /**
     *  Gets the maximum packet size
     *
     *@return    The maximum packet size
     */
    public long getMaximumPacketSize() {
        return maximumPacketSize;
    }


    /**
     *  Gets the message name for debugging
     *
     *@return    "SSH_MSG_CHANNEL_OPEN"
     */
    public String getMessageName() {
        return "SSH_MSG_CHANNEL_OPEN";
    }


    /**
     *  Gets the sender channel id
     *
     *@return    The sender channel id
     */
    public long getSenderChannelId() {
        return senderChannelId;
    }


    /**
     *  Abstract method implementation to construct a byte array containing the
     *  message data.
     *
     *@param  baw                       The byte array being written
     *@throws  InvalidMessageException  if the data cannot be written
     */
    protected void constructByteArray(ByteArrayWriter baw)
             throws InvalidMessageException {
        try {
            baw.writeString(channelType);
            baw.writeInt(senderChannelId);
            baw.writeInt(initialWindowSize);
            baw.writeInt(maximumPacketSize);

            if (channelData != null) {
                baw.write(channelData);
            }
        } catch (IOException ioe) {
            throw new InvalidMessageException("Could not write message data");
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
            channelType = bar.readString();
            senderChannelId = bar.readInt();
            initialWindowSize = bar.readInt();
            maximumPacketSize = bar.readInt();

            if (bar.available() > 0) {
                channelData = new byte[bar.available()];
                bar.read(channelData);
            }
        } catch (IOException ioe) {
            throw new InvalidMessageException("Invalid message data");
        }
    }
}
