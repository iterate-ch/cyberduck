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
 *  Implements the service accept message. </p> <p>
 *
 *  If the server supports the service (and permits the client to use it), it
 *  MUST respond with the following: </p> <p>
 *
 *  <b>byte SSH_MSG_SERVICE_ACCEPT<br>
 *  string service name</b> </p>
 *
 *@author     <A HREF="mailto:lee@sshtools.com">Lee David Painter</A>
 *@created    20 December 2002
 *@version    $Id: SshMsgServiceAccept.java,v 1.5 2002/12/10 00:07:32 martianx
 *      Exp $
 */
class SshMsgServiceAccept
         extends SshMessage {
    /**
     *  The message id of the message.
     */
    protected final static int SSH_MSG_SERVICE_ACCEPT = 6;
    private String serviceName;


    /**
     *  Constructs the message ready for sending.
     *
     *@param  serviceName  The service name that has been accepted.
     */
    public SshMsgServiceAccept(String serviceName) {
        super(SSH_MSG_SERVICE_ACCEPT);
        this.serviceName = serviceName;
    }


    /**
     *  Constructs the message from data received.
     */
    public SshMsgServiceAccept() {
        super(SSH_MSG_SERVICE_ACCEPT);
    }


    /**
     *  Gets the message name for debugging
     *
     *@return    "SSH_MSG_SERVICE_ACCEPT"
     */
    public String getMessageName() {
        return "SSH_MSG_SERVICE_ACCEPT";
    }


    /**
     *  Gets the service name that has been accepted.
     *
     *@return    The service name
     */
    public String getServiceName() {
        return serviceName;
    }


    /**
     *  Abstract method implementation to construct a byte array.
     *
     *@param  baw                       The byte array being written.
     *@throws  InvalidMessageException  if the message is invalid
     */
    protected void constructByteArray(ByteArrayWriter baw)
             throws InvalidMessageException {
        try {
            baw.writeString(serviceName);
        } catch (IOException ioe) {
            throw new InvalidMessageException("Error writing message data");
        }
    }


    /**
     *  Abstract method implementation to construct the message from data
     *  received.
     *
     *@param  bar                       The data to be read.
     *@throws  InvalidMessageException  if the message is invalid
     */
    protected void constructMessage(ByteArrayReader bar)
             throws InvalidMessageException {
        try {
            serviceName = bar.readString();
        } catch (IOException ioe) {
            throw new InvalidMessageException("Error reading message data");
        }
    }
}
