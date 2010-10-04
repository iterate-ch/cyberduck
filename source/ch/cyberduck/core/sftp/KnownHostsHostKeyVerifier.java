package ch.cyberduck.core.sftp;

/*
 * Copyright (c) 2002-2010 David Kocher. All rights reserved.
 *
 * http://cyberduck.ch/
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * Bug fixes, suggestions and comments should be sent to:
 * dkocher@cyberduck.ch
 */

import ch.ethz.ssh2.KnownHosts;

import org.apache.log4j.Logger;

import java.io.IOException;

/**
 * @version $Id:$
 */
public abstract class KnownHostsHostKeyVerifier extends HostKeyController {
    protected static Logger log = Logger.getLogger(KnownHostsHostKeyVerifier.class);

    /**
     * It is a thread safe implementation, therefore, you need only to instantiate one
     * <code>KnownHosts</code> for your whole application.
     */
    protected KnownHosts database;

    protected KnownHosts getDatabase() {
        if(null == database) {
            database = new KnownHosts();
        }
        return database;
    }

    protected boolean isHostKeyDatabaseWritable() {
        return false;
    }

    public boolean verifyServerHostKey(final String hostname, final int port, final String serverHostKeyAlgorithm,
                                       final byte[] serverHostKey) throws Exception {
        int result = this.getDatabase().verifyHostkey(hostname, serverHostKeyAlgorithm, serverHostKey);
        if(KnownHosts.HOSTKEY_IS_OK == result) {
            return true; // We are happy
        }
        if(KnownHosts.HOSTKEY_IS_NEW == result) {
            return this.isUnknownKeyAccepted(hostname, port, serverHostKeyAlgorithm, serverHostKey);
        }
        if(KnownHosts.HOSTKEY_HAS_CHANGED == result) {
            return this.isChangedKeyAccepted(hostname, port, serverHostKeyAlgorithm, serverHostKey);
        }
        return false;
    }

    /**
     * Remember host key.
     *
     * @param hostname
     * @param serverHostKeyAlgorithm
     * @param serverHostKey
     * @param always
     */
    protected void allow(final String hostname, final String serverHostKeyAlgorithm,
                         final byte[] serverHostKey, boolean always) {
        // The following call will ONLY put the key into the memory cache!
        // To save it in a known hosts file, also call "KnownHosts.addHostkeyToFile(...)"
        String hashedHostname = KnownHosts.createHashedHostname(hostname);
        try {
            // Add the hostkey to the in-memory database
            this.getDatabase().addHostkey(new String[]{hashedHostname}, serverHostKeyAlgorithm, serverHostKey);
        }
        catch(IOException e) {
            log.error(e.getMessage());
        }
        if(always) {
            if(this.isHostKeyDatabaseWritable()) {
                this.save(hostname, serverHostKeyAlgorithm, serverHostKey);
            }
        }
    }

    /**
     * Serialize host key to lookup
     *
     * @param hostname
     * @param serverHostKeyAlgorithm
     * @param serverHostKey
     */
    protected void save(final String hostname, final String serverHostKeyAlgorithm,
                        final byte[] serverHostKey) {
        throw new UnsupportedOperationException();
    }
}