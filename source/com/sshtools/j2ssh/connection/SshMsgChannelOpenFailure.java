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
 *  Implements the SSH_MSG_CHANNEL_OPEN_FAILURE message.
 *
 *@author     <A HREF="mailto:lee@sshtools.com">Lee David Painter</A>
 *@created    20 December 2002
 *@version    $Id: SshMsgChannelOpenFailure.java,v 1.7 2002/12/10 00:07:30
 *      martianx Exp $
 */
public class SshMsgChannelOpenFailure
         extends SshMessage {
    /**
     *  The message id
     */
    protected final static int SSH_MSG_CHANNEL_OPEN_FAILURE = 92;

    /**
     *  A Channel Failure Reason Code
     */
    protected final static long SSH_OPEN_ADMINISTRATIVELY_PROHIBITED = 1;

    /**
     *  A Channel Failure Reason Code
     */
    protected final static long SSH_OPEN_CONNECT_FAILED = 2;

    /**
     *  A Channel Failure Reason Code
     */
    protected final static long SSH_OPEN_UNKNOWN_CHANNEL_TYPE = 3;

    /**
     *  A Channel Failure Reason Code
     */
    protected final static long SSH_OPEN_RESOURCE_SHORTAGE = 4;
    private String additional;
    private String languageTag;
    private long reasonCode;
    private long recipientChannel;


    /**
     *  Constructor for the SshMsgChannelOpenFailure object
     *
     *@param  recipientChannel  The recipient channel id
     *@param  reasonCode        The reason code for the failure
     *@param  additional        Additional textual information
     *@param  languageTag       The language tag
     */
    public SshMsgChannelOpenFailure(long recipientChannel, long reasonCode,
            String additional, String languageTag) {
        super(SSH_MSG_CHANNEL_OPEN_FAILURE);
        this.recipientChannel = recipientChannel;
        this.reasonCode = reasonCode;
        this.additional = additional;
        this.languageTag = languageTag;
    }


    /**
     *  Constructor for the SshMsgChannelOpenFailure object
     */
    public SshMsgChannelOpenFailure() {
        super(SSH_MSG_CHANNEL_OPEN_FAILURE);
    }


    /**
     *  Gets the additional text
     *
     *@return    The additional text
     */
    public String getAdditionalText() {
        return additional;
    }


    /**
     *  Gets the language tag
     *
     *@return    The language tag
     */
    public String getLanguageTag() {
        return languageTag;
    }


    /**
     *  Gets the message name for debugging
     *
     *@return    "SSH_MSG_CHANNEL_OPEN_FAILURE"
     */
    public String getMessageName() {
        return "SSH_MSG_CHANNEL_OPEN_FAILURE";
    }


    /**
     *  Gets the reason code
     *
     *@return    The reason code
     */
    public long getReasonCode() {
        return reasonCode;
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
     *  message.
     *
     *@param  baw                       The byte array being written
     *@throws  InvalidMessageException  if the data cannot be written
     */
    protected void constructByteArray(ByteArrayWriter baw)
             throws InvalidMessageException {
        try {
            baw.writeInt(recipientChannel);
            baw.writeInt(reasonCode);
            baw.writeString(additional);
            baw.writeString(languageTag);
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
            reasonCode = bar.readInt();
            additional = bar.readString();
            languageTag = bar.readString();
        } catch (IOException ioe) {
            throw new InvalidMessageException("Invalid message data");
        }
    }
}
