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

import java.io.IOException;

import com.sshtools.j2ssh.transport.InvalidMessageException;
import com.sshtools.j2ssh.transport.SshMessage;
import com.sshtools.j2ssh.io.ByteArrayReader;
import com.sshtools.j2ssh.io.ByteArrayWriter;

/**
 *  This class implements the SSH_MSG_USERAUTH_PK_OK message
 *
 *@author     <A HREF="mailto:lee@sshtools.com">Lee David Painter</A>
 *@created    20 December 2002
 *@version    $Id: SshMsgUserAuthPKOK.java,v 1.4 2002/12/10 11:20:35 martianx
 *      Exp $
 */
public class SshMsgUserAuthPKOK
         extends SshMessage {
    /**
     *  The message id
     */
    public final static int SSH_MSG_USERAUTH_PK_OK = 60;
    private String algorithm;
    private byte key[];
    private boolean ok;


    /**
     *  Creates a new SshMsgUserAuthPKOK object.
     */
    public SshMsgUserAuthPKOK() {
        super(SSH_MSG_USERAUTH_PK_OK);
    }


    /**
     *  Creates a new SshMsgUserAuthPKOK object.
     *
     *@param  ok         is the key acceptable for public key authentication
     *@param  algorithm  the public key algorithm name
     *@param  key        the public key blob
     */
    public SshMsgUserAuthPKOK(boolean ok, String algorithm, byte key[]) {
        super(SSH_MSG_USERAUTH_PK_OK);
        this.ok = ok;
        this.algorithm = algorithm;
        this.key = key;
    }


    /**
     *  Returns the message name for debugging
     *
     *@return    "SSH_MSG_USERAUTH_PK_OK"
     */
    public String getMessageName() {
        return "SSH_MSG_USERAUTH_PK_OK";
    }


    /**
     *  Constructs a byte array containing the message data
     *
     *@param  baw                       the byte array being written to
     *@throws  InvalidMessageException  if the message is invalid/or fails to
     *      write data
     */
    protected void constructByteArray(ByteArrayWriter baw)
             throws InvalidMessageException {
        try {
            baw.write(ok ? 1 : 0);
            baw.writeString(algorithm);
            baw.writeBinaryString(key);
        } catch (IOException ioe) {
            throw new InvalidMessageException("Failed to write message data!");
        }
    }


    /**
     *  Contructs the message from a byte array
     *
     *@param  bar                       the byte array being read
     *@throws  InvalidMessageException  if the message is invalid/or fails to
     *      read data
     */
    protected void constructMessage(ByteArrayReader bar)
             throws InvalidMessageException {
        try {
            ok = ((bar.read() == 1) ? true : false);
            algorithm = bar.readString();
            key = bar.readBinaryString();
        } catch (IOException ioe) {
            throw new InvalidMessageException("Failed to read message data!");
        }
    }
}
