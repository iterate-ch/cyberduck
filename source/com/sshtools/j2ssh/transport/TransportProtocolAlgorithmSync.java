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
package com.sshtools.j2ssh.transport;

import org.apache.log4j.Logger;

import com.sshtools.j2ssh.transport.cipher.SshCipher;
import com.sshtools.j2ssh.transport.compression.SshCompression;
import com.sshtools.j2ssh.transport.hmac.SshHmac;


/**
 * Provides a synchronized object store for all the transport protocol
 * algorithms so that whilst new keys are being created the input/output
 * streams block unitl the new objects are in place.
 *
 * @author <A HREF="mailto:lee@sshtools.com">Lee David Painter</A>
 * @version $Id$
 */
public class TransportProtocolAlgorithmSync {
    private static Logger log =
        Logger.getLogger(TransportProtocolAlgorithmSync.class);
    private SshCipher cipher = null;
    private SshCompression compression = null;
    private SshHmac hmac = null;
    private boolean isLocked = false;

    /**
     * Constructs the object
     */
    public TransportProtocolAlgorithmSync() {
    }

    /**
     * Sets the cipher object
     *
     * @param cipher
     */
    public synchronized void setCipher(SshCipher cipher) {
        this.cipher = cipher;
    }

    /**
     * Gets the current cipher object
     *
     * @return the current cipher
     */
    public synchronized SshCipher getCipher() {
        return cipher;
    }

    /**
     * Sets the compression object
     *
     * @param compression
     */
    public synchronized void setCompression(SshCompression compression) {
        this.compression = compression;
    }

    /**
     * Gets the current compression object
     *
     * @return the current compression
     */
    public synchronized SshCompression getCompression() {
        return compression;
    }

    /**
     * Sets the message authenticaiton object
     *
     * @param hmac
     */
    public synchronized void setHmac(SshHmac hmac) {
        this.hmac = hmac;
    }

    /**
     * Gets the message authenication object
     *
     * @return the current hmac
     */
    public synchronized SshHmac getHmac() {
        return hmac;
    }

    /**
     * Locks the object store
     */
    public synchronized void lock() {
        while (isLocked) {
            try {
                wait(50);
            } catch (InterruptedException e) {
            }
        }

        isLocked = true;
    }

    /**
     * Releases the object store
     */
    public synchronized void release() {
        isLocked = false;
        notifyAll();
    }
}
