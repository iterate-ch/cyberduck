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
 *  Implements the SSH_MSG_CHANNEL_EXT_DATA message.
 *
 *@author     <A HREF="mailto:lee@sshtools.com">Lee David Painter</A>
 *@created    20 December 2002
 *@version    $Id: SshMsgChannelExtendedData.java,v 1.8 2002/12/18 19:27:29
 *      martianx Exp $
 */
public class SshMsgChannelExtendedData
         extends SshMessage {
    /**
     *  The message id
     */
    public final static int SSH_MSG_CHANNEL_EXTENDED_DATA = 95;

    /**
     *  The ext data type code (currently the only type available)
     */
    public final static int SSH_EXTENDED_DATA_STDERR = 1;
    private byte channelData[];
    private int dataTypeCode;
    private long recipientChannel;


    /**
     *  Constructor for the SshMsgChannelExtendedData object
     *
     *@param  recipientChannel  The data's channel
     *@param  dataTypeCode      The data type code
     *@param  channelData       The data
     */
    public SshMsgChannelExtendedData(long recipientChannel, int dataTypeCode,
            byte channelData[]) {
        super(SSH_MSG_CHANNEL_EXTENDED_DATA);

        this.recipientChannel = recipientChannel;
        this.dataTypeCode = dataTypeCode;
        this.channelData = channelData;
    }


    /**
     *  Constructor for the SshMsgChannelExtendedData object
     */
    public SshMsgChannelExtendedData() {
        super(SSH_MSG_CHANNEL_EXTENDED_DATA);
    }


    /**
     *  Gets the channel data
     *
     *@return    The channel data
     */
    public byte[] getChannelData() {
        return channelData;
    }


    /**
     *  Gets the dataTypeCode
     *
     *@return    The data type code
     */
    public int getDataTypeCode() {
        return dataTypeCode;
    }


    /**
     *  Gets the message name for degugging
     *
     *@return    "SSH_MSG_CHANNEL_EXTENDED_DATA"
     */
    public String getMessageName() {
        return "SSH_MSG_CHANNEL_EXTENDED_DATA";
    }


    /**
     *  Gets the recipient Channel id
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
     *@param  baw                          The byte array being written
     *@exception  InvalidMessageException  if the data cannot be written
     */
    protected void constructByteArray(ByteArrayWriter baw)
             throws InvalidMessageException {
        try {
            baw.writeInt(recipientChannel);
            baw.writeInt(dataTypeCode);

            if (channelData != null) {
                baw.writeBinaryString(channelData);
            } else {
                baw.writeString("");
            }
        } catch (IOException ioe) {
            throw new InvalidMessageException("Invalid message data");
        }
    }


    /**
     *  Abstract method implementation to extract the message from a byte array.
     *
     *@param  bar                          The data being read
     *@exception  InvalidMessageException  if the data cannot be read
     */
    protected void constructMessage(ByteArrayReader bar)
             throws InvalidMessageException {
        try {
            recipientChannel = bar.readInt();
            dataTypeCode = (int) bar.readInt();

            if (bar.available() > 0) {
                channelData = bar.readBinaryString();
            }
        } catch (IOException ioe) {
            throw new InvalidMessageException("Invalid message data");
        }
    }
}
