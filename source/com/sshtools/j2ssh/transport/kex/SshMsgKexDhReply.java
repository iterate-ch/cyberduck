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
 *  The server responds to a SSH_MSG_KEXDH_INIT with the following: </p> <p>
 *
 *  byte SSH_MSG_KEXDH_REPLY<br>
 *  string server public host key and certificates (K_S)<br>
 *  mpint f<br>
 *  string signature of H </p> <p>
 *
 *  The hash H is computed as the HASH hash of the concatenation of the
 *  following: </p> <p>
 *
 *  string V_C, the client's version string (CR and NL excluded)<br>
 *  string V_S, the server's version string (CR and NL excluded)<br>
 *  string I_C, the payload of the client's SSH_MSG_KEXINIT<br>
 *  string I_S, the payload of the server's SSH_MSG_KEXINIT<br>
 *  string K_S, the host key<br>
 *  mpint e, exchange value sent by the client<br>
 *  mpint f, exchange value sent by the server<br>
 *  mpint K, the shared secret </p> <p>
 *
 *  This value is called the exchange hash, and it is used to authenticate the
 *  key exchange. The exchange hash SHOULD be kept secret. </p> <p>
 *
 *  The signature algorithm MUST be applied over H, not the original data. Most
 *  signature algorithms include hashing and additional padding. For example,
 *  "ssh-dss" specifies SHA-1 hashing; in that case, the data is first hashed
 *  with HASH to compute H, and H is then hashed with SHA-1 as part of the
 *  signing operation. </p>
 *
 *@author     <A HREF="mailto:lee@sshtools.com">Lee David Painter</A>
 *@created    31 August 2002
 *@version    $Id: SshMsgKexDhReply.java,v 1.4 2002/12/09 22:51:31 martianx Exp
 *      $
 */
public class SshMsgKexDhReply
         extends SshMessage {
    /**
     *  The message id of the message
     */
    protected final static int SSH_MSG_KEXDH_REPLY = 31;

    // The diffie hellman f value
    private BigInteger f;

    // The host key data
    private byte hostKey[];

    // The signature
    private byte signature[];


    /**
     *  Constructs the message ready for sending.
     *
     *@param  hostKey    The servers host key data
     *@param  f          The diffie hellman f value
     *@param  signature  The signature to verify ownership of a private key
     */
    public SshMsgKexDhReply(byte hostKey[], BigInteger f, byte signature[]) {
        super(SSH_MSG_KEXDH_REPLY);
        this.hostKey = hostKey;
        this.f = f;
        this.signature = signature;
    }


    /**
     *  Constructs the message from data received.
     */
    public SshMsgKexDhReply() {
        super(SSH_MSG_KEXDH_REPLY);
    }


    /**
     *  Gets the f attribute of the message
     *
     *@return    The f value
     */
    public BigInteger getF() {
        return f;
    }


    /**
     *  Gets the hostKey attribute of the message
     *
     *@return    The hostKey value
     */
    public byte[] getHostKey() {
        return hostKey;
    }


    /**
     *  Gets the message name for debugging
     *
     *@return    "SSH_MSG_KEXDH_REPLY"
     */
    public String getMessageName() {
        return "SSH_MSG_KEXDH_REPLY";
    }


    /**
     *  Gets the signature attribute of the message
     *
     *@return    The signature value
     */
    public byte[] getSignature() {
        return signature;
    }


    /**
     *  Abstract method implementation to construct a byte array containing the
     *  message.
     *
     *@param  baw                          The byte array being written to.
     *@exception  InvalidMessageException  if the data cannot be written.
     */
    protected void constructByteArray(ByteArrayWriter baw)
             throws InvalidMessageException {
        try {
            baw.writeBinaryString(hostKey);
            baw.writeBigInteger(f);
            baw.writeBinaryString(signature);
        } catch (IOException ioe) {
            throw new InvalidMessageException("Error writing message data: "
                    + ioe.getMessage());
        }
    }


    /**
     *  Abstract method implementation to construct the message from a byte
     *  array.
     *
     *@param  bar                          The byte array containing the data.
     *@exception  InvalidMessageException  if the data cannot be read.
     */
    protected void constructMessage(ByteArrayReader bar)
             throws InvalidMessageException {
        try {
            hostKey = bar.readBinaryString();
            f = bar.readBigInteger();
            signature = bar.readBinaryString();
        } catch (IOException ioe) {
            throw new InvalidMessageException("Error reading message data: "
                    + ioe.getMessage());
        }
    }
}
