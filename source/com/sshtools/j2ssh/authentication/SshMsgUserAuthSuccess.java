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
package com.sshtools.j2ssh.authentication;

import com.sshtools.j2ssh.transport.InvalidMessageException;
import com.sshtools.j2ssh.transport.SshMessage;
import com.sshtools.j2ssh.io.ByteArrayReader;
import com.sshtools.j2ssh.io.ByteArrayWriter;

/**
 *  Implements the SSH_MSG_USERAUTH_SUCCESS message
 *
 *@author     <A HREF="mailto:lee@sshtools.com">Lee David Painter</A>
 *@created    20 December 2002
 *@version    $Id: SshMsgUserAuthSuccess.java,v 1.7 2002/12/09 23:08:26 martianx
 *      Exp $
 */
public class SshMsgUserAuthSuccess
         extends SshMessage {
    /**
     *  The message id
     */
    protected final static int SSH_MSG_USERAUTH_SUCCESS = 52;


    /**
     *  Constructs the message
     */
    public SshMsgUserAuthSuccess() {
        super(SSH_MSG_USERAUTH_SUCCESS);
    }


    /**
     *  Gets the message name for debugging.
     *
     *@return    "SSH_MSG_USERAUTH_SUCCESS"
     */
    public String getMessageName() {
        return "SSH_MSG_USERAUTH_SUCCESS";
    }


    /**
     *  Constructs a byte array containing the message data.
     *
     *@param  baw                          The byte array being written
     *@exception  InvalidMessageException  if the message is invalid or fails to
     *      write the data
     */
    protected void constructByteArray(ByteArrayWriter baw)
             throws InvalidMessageException { }


    /**
     *  Constructs the message a byte array.
     *
     *@param  bar                          The byte array being read.
     *@exception  InvalidMessageException  if the message is invalid or fails to
     *      read the data
     */
    protected void constructMessage(ByteArrayReader bar)
             throws InvalidMessageException { }
}
