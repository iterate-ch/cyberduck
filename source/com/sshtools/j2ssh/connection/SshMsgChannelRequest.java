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
 *  Implements the SSH_MSG_CHANNEL_REQUEST message.
 *
 *@author     <A HREF="mailto:lee@sshtools.com">Lee David Painter</A>
 *@created    20 December 2002
 *@version    $Id: SshMsgChannelRequest.java,v 1.7 2002/12/10 00:07:30 martianx
 *      Exp $
 */
public class SshMsgChannelRequest
         extends SshMessage {
    /**
     *  The message id
     */
    protected final static int SSH_MSG_CHANNEL_REQUEST = 98;
    private String requestType;
    private byte channelData[];
    private boolean wantReply;
    private long recipientChannel;


    /**
     *  Constructor for the SshMsgChannelRequest object
     *
     *@param  recipientChannel  The recipient channel id
     *@param  requestType       The channel request type
     *@param  wantReply         True if you want the remote side to reply
     *@param  channelData       The channel request specific data
     */
    public SshMsgChannelRequest(long recipientChannel, String requestType,
            boolean wantReply, byte channelData[]) {
        super(SSH_MSG_CHANNEL_REQUEST);

        this.recipientChannel = recipientChannel;
        this.requestType = requestType;
        this.wantReply = wantReply;
        this.channelData = channelData;
    }


    /**
     *  Constructor for the SshMsgChannelRequest object
     */
    public SshMsgChannelRequest() {
        super(SSH_MSG_CHANNEL_REQUEST);
    }


    /**
     *  Gets the channel request specific data
     *
     *@return    The channel request data
     */
    public byte[] getChannelData() {
        return channelData;
    }


    /**
     *  Gets the message name for debugging
     *
     *@return    "SSH_MSG_CHANNEL_REQUEST"
     */
    public String getMessageName() {
        return "SSH_MSG_CHANNEL_REQUEST";
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
     *  Gets the request type
     *
     *@return    The request type
     */
    public String getRequestType() {
        return requestType;
    }


    /**
     *  Indicates whether the sender wants an explicit reply
     *
     *@return    The want reply value
     */
    public boolean getWantReply() {
        return wantReply;
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
            baw.writeInt(recipientChannel);
            baw.writeString(requestType);
            baw.write((wantReply ? 1 : 0));

            if (channelData != null) {
                baw.write(channelData);
            }
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
            requestType = bar.readString();
            wantReply = ((bar.read() == 0) ? false : true);

            if (bar.available() > 0) {
                channelData = new byte[bar.available()];
                bar.read(channelData);
            }
        } catch (IOException ioe) {
            throw new InvalidMessageException("Invalid message data");
        }
    }
}
