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
 *  This class implements the service request message. </p> <p>
 *
 *  After the key exchange, the client requests a service. The service is
 *  identified by a name. The format of names and procedures for defining new
 *  names are defined in [SSH-ARCH]. </p> <p>
 *
 *  Currently, the following names have been reserved: </p> <p>
 *
 *  ssh-userauth<br>
 *  ssh-connection </p> <p>
 *
 *  byte SSH_MSG_SERVICE_REQUEST<br>
 *  string service name </p> <p>
 *
 *  If the server rejects the service request, it SHOULD send an appropriate
 *  SSH_MSG_DISCONNECT message and MUST disconnect. </p> <p>
 *
 *  When the service starts, it may have access to the session identifier
 *  generated during the key exchange. </p>
 *
 *@author     <A HREF="mailto:lee@sshtools.com">Lee David Painter</A>
 *@created    20 December 2002
 *@version    $Id: SshMsgServiceRequest.java,v 1.5 2002/12/10 00:07:32 martianx
 *      Exp $
 */
class SshMsgServiceRequest
         extends SshMessage {
    /**
     *  The message id of the message
     */
    protected final static int SSH_MSG_SERVICE_REQUEST = 5;
    private String serviceName;


    /**
     *  Constructs the message ready for sending
     *
     *@param  serviceName  The service requested
     */
    public SshMsgServiceRequest(String serviceName) {
        super(SSH_MSG_SERVICE_REQUEST);
        this.serviceName = serviceName;
    }


    /**
     *  Constructs the message from data received.
     */
    public SshMsgServiceRequest() {
        super(SSH_MSG_SERVICE_REQUEST);
    }


    /**
     *  Gets the message name for debugging
     *
     *@return    "SSH_MSG_SERVICE_REQUEST"
     */
    public String getMessageName() {
        return "SSH_MSG_SERVICE_REQUEST";
    }


    /**
     *  Gets the service name requested
     *
     *@return    The service name.
     */
    public String getServiceName() {
        return serviceName;
    }


    /**
     *  Abstract method implementation to create a byte array containing the
     *  message.
     *
     *@param  baw                          The byte array being written.
     *@exception  InvalidMessageException  if the message is invalid
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
     *  Abstract method implementation to construct the message from a byte
     *  array.
     *
     *@param  bar                          The data being read.
     *@exception  InvalidMessageException  if the message is invalid
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
