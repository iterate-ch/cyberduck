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
 *  Implements the SSH_MSG_GLOBAL_REQUEST message.
 *
 *@author     <A HREF="mailto:lee@sshtools.com">Lee David Painter</A>
 *@created    20 December 2002
 *@version    $Id: SshMsgGlobalRequest.java,v 1.7 2002/12/10 00:07:30 martianx
 *      Exp $
 */
public class SshMsgGlobalRequest
         extends SshMessage {
    /**
     *  The message id
     */
    protected final static int SSH_MSG_GLOBAL_REQUEST = 80;
    private String requestName;
    private byte requestData[];
    private boolean wantReply;


    /**
     *  Constructor for the SshMsgGlobalRequest object
     *
     *@param  requestName  The request name
     *@param  wantReply    True if a reply is required
     *@param  requestData  The request specific data
     */
    public SshMsgGlobalRequest(String requestName, boolean wantReply,
            byte requestData[]) {
        super(SSH_MSG_GLOBAL_REQUEST);

        this.requestName = requestName;
        this.wantReply = wantReply;
        this.requestData = requestData;
    }


    /**
     *  Constructor for the SshMsgGlobalRequest object
     */
    public SshMsgGlobalRequest() {
        super(SSH_MSG_GLOBAL_REQUEST);
    }


    /**
     *  Gets the message name for debugging
     *
     *@return    "SSH_MSG_GLOBAL_REQUEST"
     */
    public String getMessageName() {
        return "SSH_MSG_GLOBAL_REQUEST";
    }


    /**
     *  Gets the request data
     *
     *@return    The request data
     */
    public byte[] getRequestData() {
        return requestData;
    }


    /**
     *  Gets the request name
     *
     *@return    The request name
     */
    public String getRequestName() {
        return requestName;
    }


    /**
     *  Indicates whether the sender wants a reply
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
     *@param  baw                       The byte array being written to.
     *@throws  InvalidMessageException  if the data cannot be written
     */
    protected void constructByteArray(ByteArrayWriter baw)
             throws InvalidMessageException {
        try {
            baw.writeString(requestName);
            baw.write((wantReply ? 1 : 0));

            if (requestData != null) {
                baw.write(requestData);
            }
        } catch (IOException ioe) {
            throw new InvalidMessageException("Invalid message data");
        }
    }


    /**
     *  Abstract method implementation to construct the message from a byte
     *  array
     *
     *@param  bar                       The byte array being read
     *@throws  InvalidMessageException  if the data cannot be read
     */
    protected void constructMessage(ByteArrayReader bar)
             throws InvalidMessageException {
        try {
            requestName = bar.readString();
            wantReply = ((bar.read() == 0) ? false : true);

            if (bar.available() > 0) {
                requestData = new byte[bar.available()];
                bar.read(requestData);
            }
        } catch (IOException ioe) {
            throw new InvalidMessageException("Invalid message data");
        }
    }
}
