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
import com.sshtools.j2ssh.io.ByteArrayReader;
import com.sshtools.j2ssh.io.ByteArrayWriter;

/**
 *  <p>
 *
 *  Implements the new keys message, sent after completioin of key exchange.
 *  </p> <p>
 *
 *  Key exchange ends by each side sending an SSH_MSG_NEWKEYS message. This
 *  message is sent with the old keys and algorithms. All messages sent after
 *  this message MUST use the new keys and algorithms. </p> <p>
 *
 *  When this message is received, the new keys and algorithms MUST be taken
 *  into use for receiving. </p> <p>
 *
 *  This message is the only valid message after key exchange, in addition to
 *  SSH_MSG_DEBUG, SSH_MSG_DISCONNECT and SSH_MSG_IGNORE messages. The purpose
 *  of this message is to ensure that a party is able to respond with a
 *  disconnect message that the other party can understand if something goes
 *  wrong with the key exchange. Implementations MUST NOT accept any other
 *  messages after key exchange before receiving SSH_MSG_NEWKEYS. </p> <b>byte
 *  SSH_MSG_NEWKEYS</b>
 *
 *@author     <A HREF="mailto:lee@sshtools.com">Lee David Painter</A>
 *@created    20 December 2002
 *@version    $Id$
 */
class SshMsgNewKeys
         extends SshMessage {
    /**
     *  The message id of the message
     */
    protected final static int SSH_MSG_NEWKEYS = 21;


    /**
     *  Constucts the message for sending
     */
    public SshMsgNewKeys() {
        super(SSH_MSG_NEWKEYS);
    }


    /**
     *  Gets the message name for debugging
     *
     *@return    "SSH_MSG_NEWKEYS"
     */
    public String getMessageName() {
        return "SSH_MSG_NEWKEYS";
    }


    /**
     *  Abstract method implementation to constrcut a byte array.
     *
     *@param  baw                          The byte array being written to.
     *@exception  InvalidMessageException  if the message is invalid
     */
    protected void constructByteArray(ByteArrayWriter baw)
             throws InvalidMessageException { }


    /**
     *  Abstract method implementation to construct the message from data
     *  received.
     *
     *@param  bar                          The data received.
     *@exception  InvalidMessageException  if the message is invalid
     */
    protected void constructMessage(ByteArrayReader bar)
             throws InvalidMessageException { }
}
