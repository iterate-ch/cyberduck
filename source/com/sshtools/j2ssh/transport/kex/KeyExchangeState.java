/*
 *  SSHTools - Java SSH2 API
 *
 *  Copyright (C) 2002-2003 Lee David Painter and Contributors.
 *
 *  Contributions made by:
 *
 *  Brett Smith
 *  Richard Pernavas
 *  Erwin Bolwidt
 *
 *  This program is free software; you can redistribute it and/or
 *  modify it under the terms of the GNU Library General Public License
 *  as published by the Free Software Foundation; either version 2 of
 *  the License, or (at your option) any later version.
 *
 *  You may also distribute it and/or modify it under the terms of the
 *  Apache style J2SSH Software License. A copy of which should have
 *  been provided with the distribution.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  License document supplied with your distribution for more details.
 *
 */
package com.sshtools.j2ssh.transport.kex;

import java.math.BigInteger;


/**
 * @author $author$
 * @version $Revision$
 */
public class KeyExchangeState {
    /**  */
    public final static int IN_PROGRESS = 0;

    /**  */
    public final static int COMPLETE = 1;

    /**  */
    public final static int FAILED = 2;
    private BigInteger secret;
    private String reason;
    private byte[] exchangeHash;
    private byte[] hostKey;
    private byte[] signature;
    private int state = IN_PROGRESS;

    /**
     * Creates a new KeyExchangeState object.
     */
    public KeyExchangeState() {
    }

    /**
     * @param exchangeHash
     * @param hostKey
     * @param signature
     * @param secret
     */
    public final synchronized void setComplete(byte[] exchangeHash,
                                               byte[] hostKey, byte[] signature, BigInteger secret) {
        this.exchangeHash = exchangeHash;
        this.hostKey = hostKey;
        this.signature = signature;
        this.secret = secret;
        state = COMPLETE;
        notifyAll();
    }

    /**
     * @return
     */
    public byte[] getExchangeHash() {
        return exchangeHash;
    }

    /**
     * @param reason
     */
    public final synchronized void setFailed(String reason) {
        this.reason = reason;
        state = FAILED;
        notifyAll();
    }

    /**
     * @return
     */
    public byte[] getHostKey() {
        return hostKey;
    }

    /**
     * @return
     */
    public BigInteger getSecret() {
        return secret;
    }

    /**
     * @return
     */
    public byte[] getSignature() {
        return signature;
    }

    /**
     * @return
     */
    public synchronized int getState() {
        return state;
    }

    /**
     *
     */
    public final synchronized void waitForCompletion() {
        while (state == IN_PROGRESS) {
            try {
                wait();
            }
            catch (InterruptedException e) {
            }
        }
    }

    /**
     * @return
     */
    public synchronized String getFailureReason() {
        return reason;
    }
}
