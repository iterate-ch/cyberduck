/*
 * Sshtools - Java SSH2 API
 *
 * Copyright (C) 2002 Lee David Painter.
 *
 * Written by: 2002 Lee David Painter <lee@sshtools.com>
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Library General Public License
 * as published by the Free Software Foundation; either version 2 of
 * the License, or (at your option) any later version.

 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Library General Public License for more details.
 *
 * You should have received a copy of the GNU Library General Public
 * License along with this program; if not, write to the Free Software
 * Foundation, Inc., 675 Mass Ave, Cambridge, MA 02139, USA.
 */
package com.sshtools.j2ssh.transport.kex;

import java.math.BigInteger;

import com.sshtools.j2ssh.transport.SshMessageStore;
import com.sshtools.j2ssh.transport.TransportProtocol;
import com.sshtools.j2ssh.transport.publickey.SshPrivateKey;

import java.io.IOException;


/**
 * This class provides a framework for implementing SSH protocol key exchange
 * methods for the API.
 *
 * @author <A HREF="mailto:lee@sshtools.com">Lee David Painter</A>
 * @version $Id$
 */
public abstract class SshKeyExchange { //implements Runnable {

    /** the secret value k */
    protected BigInteger secret;

    /** The methods message store */
    protected SshMessageStore messageStore = new SshMessageStore();

    /** the exchange hash output */
    protected byte exchangeHash[];

    /** the host key blob */
    protected byte hostKey[];

    /** the signature blob */
    protected byte signature[];

    /**
     * Constructor for the KeyExchange object
     */
    public SshKeyExchange() {
    }

    /**
     * Gets the exchange hash output of the key exchange
     *
     * @return
     */
    public byte[] getExchangeHash() {
        return exchangeHash;
    }

    /**
     * Gets the host key supplied by the server during key exchange
     *
     * @return
     */
    public byte[] getHostKey() {
        return hostKey;
    }

    /**
     * Gets the secret value produced during key exchange
     *
     * @return the k value
     */
    public BigInteger getSecret() {
        return secret;
    }

    /**
     * Gets the signature supplied during key exchange
     *
     * @return the signature blob
     */
    public byte[] getSignature() {
        return signature;
    }

    /**
     * Initiates the key exchange object before starting
     *
     * @param transport The transport protocol for sending/receiving
     */
    public abstract void init(TransportProtocol transport)
                       throws IOException;

    /**
     * Starts the client side of the key exchange
     *
     * @exception KeyExchangeException if key exchange fails
     */
    public abstract void performClientExchange(String clientId,
                                               String serverId,
                                               byte clientKexInit[],
                                               byte serverKexInit[])
                                        throws IOException;

    /**
     * Starts the server side of the key exchange
     *
     * @exception KeyExchangeException if key exchange fails
     */
    public abstract void performServerExchange(String clientId,
                                               String serverId,
                                               byte clientKexInit[],
                                               byte serverKexInit[],
                                               SshPrivateKey prvkey)
                                        throws IOException;

    /**
     * Resets the key exchange instance
     */
    public void reset() {
        exchangeHash = null;
        hostKey = null;
        signature = null;
        secret = null;
    }
}
