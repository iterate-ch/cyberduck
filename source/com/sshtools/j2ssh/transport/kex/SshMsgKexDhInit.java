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
package com.sshtools.j2ssh.transport.kex;

import java.io.IOException;

import java.math.BigInteger;

import com.sshtools.j2ssh.transport.InvalidMessageException;
import com.sshtools.j2ssh.transport.SshMessage;
import com.sshtools.j2ssh.io.ByteArrayReader;
import com.sshtools.j2ssh.io.ByteArrayWriter;

/**
 *  <p>
 *
 *  Implements the diffie-hellman-group1-sha1 key exchange init message. </p>
 *  <p>
 *
 *  byte SSH_MSG_KEXDH_INIT<br>
 *  mpint e </p> <p>
 *
 *  The Diffie-Hellman key exchange provides a shared secret that can not be
 *  determined by either party alone. The key exchange is combined with a
 *  signature with the host key to provide host authentication. </p>
 *
 *@author     <A HREF="mailto:lee@sshtools.com">Lee David Painter</A>
 *@created    31 August 2002
 *@version    $Id$
 */
public class SshMsgKexDhInit
         extends SshMessage {
    /**
     *  The message id for this message
     */
    protected final static int SSH_MSG_KEXDH_INIT = 30;

    // Stores the e value
    private BigInteger e;


    /**
     *  Constructs the message ready for sending
     *
     *@param  e  The diffie hellman e value
     */
    public SshMsgKexDhInit(BigInteger e) {
        super(SSH_MSG_KEXDH_INIT);
        this.e = e;
    }


    /**
     *  Constructs the message from data received
     */
    public SshMsgKexDhInit() {
        super(SSH_MSG_KEXDH_INIT);
    }


    /**
     *  Gets the diffie hellman e value
     *
     *@return    The e value
     */
    public BigInteger getE() {
        return e;
    }


    /**
     *  Gets the message name for debugging
     *
     *@return    "SSH_MSG_KEXDH_INIT"
     */
    public String getMessageName() {
        return "SSH_MSG_KEXDH_INIT";
    }


    /**
     *  Abstract method implementation to create a byte array containing the
     *  message.
     *
     *@param  baw                          The byte array to write to.
     *@exception  InvalidMessageException  if the data cannot be written.
     */
    protected void constructByteArray(ByteArrayWriter baw)
             throws InvalidMessageException {
        try {
            baw.writeBigInteger(e);
        } catch (IOException ioe) {
            throw new InvalidMessageException("Error writing message data: "
                    + ioe.getMessage());
        }
    }


    /**
     *  Abstract method implementation to construct the message from a byte
     *  array.
     *
     *@param  bar                          The byte array containing the message
     *      data.
     *@exception  InvalidMessageException  if the data cannot be read.
     */
    protected void constructMessage(ByteArrayReader bar)
             throws InvalidMessageException {
        try {
            e = bar.readBigInteger();
        } catch (IOException ioe) {
            throw new InvalidMessageException("Error reading message data: "
                    + ioe.getMessage());
        }
    }
}
