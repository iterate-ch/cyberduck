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

import com.sshtools.j2ssh.transport.InvalidMessageException;
import com.sshtools.j2ssh.transport.SshMessage;
import com.sshtools.j2ssh.io.ByteArrayReader;
import com.sshtools.j2ssh.io.ByteArrayWriter;

/**
 *  Implements the SSH_MSG_REQUEST_FAILURE message.
 *
 *@author     <A HREF="mailto:lee@sshtools.com">Lee David Painter</A>
 *@created    20 December 2002
 *@version    $Id: SshMsgRequestFailure.java,v 1.7 2002/12/10 00:07:30 martianx
 *      Exp $
 */
public class SshMsgRequestFailure
         extends SshMessage {
    /**
     *  The message id
     */
    public final static int SSH_MSG_REQUEST_FAILURE = 82;


    /**
     *  Constructor for the SshMsgRequestFailure object
     */
    public SshMsgRequestFailure() {
        super(SSH_MSG_REQUEST_FAILURE);
    }


    /**
     *  Gets the message name for debugging
     *
     *@return    "SSH_MSG_REQUEST_FAILURE"
     */
    public String getMessageName() {
        return "SSH_MSG_REQUEST_FAILURE";
    }


    /**
     *  Abstract method implementaiton to construct a byte array contianing the
     *  message data.
     *
     *@param  baw                       The byte array being written to
     *@throws  InvalidMessageException  if the data cannot be written
     */
    protected void constructByteArray(ByteArrayWriter baw)
             throws InvalidMessageException { }


    /**
     *  Abstract method implementation to construct the message from a byte
     *  array.
     *
     *@param  bar                       The byte array being read
     *@throws  InvalidMessageException  if the data cannot be read
     */
    protected void constructMessage(ByteArrayReader bar)
             throws InvalidMessageException { }
}
