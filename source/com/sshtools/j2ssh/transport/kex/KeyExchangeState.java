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


/**
 * <p>
 * The key exchange state object is synchronized so that a thread can wait for
 * the key exchange to complete (or fail)
 * </p>
 *
 * @author <A HREF="mailto:lee@sshtools.com">Lee David Painter</A>
 * @version $Id$
 *
 * @created 31 August 2002
 */
public class KeyExchangeState {
    /** The key exchange is currently in progress */
    public final static int IN_PROGRESS = 0;

    /** The key exchange completed */
    public final static int COMPLETE = 1;

    /**
     * The key exchange failed. Use <code>getFailedReason</code> to examine the
     * failure reason.
     */
    public final static int FAILED = 2;
    private BigInteger secret;
    private String reason;
    private byte exchangeHash[];
    private byte hostKey[];
    private byte signature[];
    private int state = IN_PROGRESS;

    /**
     * Constructs the KeyExchangeState
     */
    public KeyExchangeState() {
    }

    /**
     * Sets the key exchange to complete
     *
     * @param exchangeHash The hash output
     * @param hostKey The servers host key
     * @param signature The signature to verify the host key with
     * @param secret The secret key exchange value
     */
    public final synchronized void setComplete(byte exchangeHash[],
                                               byte hostKey[],
                                               byte signature[],
                                               BigInteger secret) {
        this.exchangeHash = exchangeHash;
        this.hostKey = hostKey;
        this.signature = signature;
        this.secret = secret;
        state = COMPLETE;

        notifyAll();
    }

    /**
     * Gets the exchange hash output of the key exchange
     *
     * @return the exchange hash
     */
    public byte[] getExchangeHash() {
        return exchangeHash;
    }

    /**
     * Sets the state to failed
     *
     * @param reason The reason for failure
     */
    public final synchronized void setFailed(String reason) {
        this.reason = reason;

        state = FAILED;

        notifyAll();
    }

    /**
     * Gets the host key supplied by the server during key exchange
     *
     * @return the host key blob
     */
    public byte[] getHostKey() {
        return hostKey;
    }

    /**
     * Gets the secret value produced during key exchange
     *
     * @return the secret value k
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
     * Gets the current state of the key exchange
     *
     * @return the state value
     */
    public synchronized int getState() {
        return state;
    }

    /**
     * A Thread can call this method to wait for the completetion of key
     * exchange
     */
    public final synchronized void waitForCompletion() {
        while (state==IN_PROGRESS) {
            try {
                wait();
            } catch (InterruptedException e) {
            }
        }
    }

    /**
     * Gets the failure reason
     *
     * @return the reason for failure
     */
    public synchronized String getFailureReason() {
        return reason;
    }
}
