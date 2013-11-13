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

import ch.cyberduck.core.HostKeyController;
import ch.cyberduck.core.Local;
import ch.cyberduck.core.exception.AccessDeniedException;
import ch.cyberduck.core.exception.ConnectionCanceledException;

import org.apache.commons.io.IOUtils;
import org.apache.log4j.Logger;

import java.io.IOException;
import java.io.InputStream;

import ch.ethz.ssh2.KnownHosts;

/**
 * @version $Id$
 */
public abstract class MemoryHostKeyVerifier implements HostKeyController {
    private static Logger log = Logger.getLogger(MemoryHostKeyVerifier.class);

    /**
     * It is a thread safe implementation, therefore, you need only to instantiate one
     * <code>KnownHosts</code> for your whole application.
     */
    protected KnownHosts database;

    public MemoryHostKeyVerifier() {
        database = new KnownHosts();
    }

    public MemoryHostKeyVerifier(final Local file) {
        this.setDatabase(file);
    }

    protected void setDatabase(final Local file) {
        if(!file.exists()) {
            file.touch();
        }
        InputStream in = null;
        try {
            in = file.getInputStream();
            database = new KnownHosts(file.getAbsolute());
        }
        catch(IOException e) {
            log.error(String.format("Cannot read known hosts file %s", file));
        }
        catch(IllegalArgumentException e) {
            log.error(String.format("Cannot read known hosts file %s", file));
        }
        catch(AccessDeniedException e) {
            log.error(String.format("Cannot read known hosts file %s", file));
        }
        finally {
            IOUtils.closeQuietly(in);
        }
        if(null == database) {
            database = new KnownHosts();
        }
    }

    protected boolean isHostKeyDatabaseWritable() {
        return false;
    }

    @Override
    public boolean verify(final String hostname, final int port, final String serverHostKeyAlgorithm,
                          final byte[] serverHostKey) throws IOException, ConnectionCanceledException {
        final int result = database.verifyHostkey(hostname, serverHostKeyAlgorithm, serverHostKey);
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
     * @param hostname               Hostname
     * @param port                   Port number
     * @param serverHostKeyAlgorithm Algorithm
     * @param serverHostKey          Key blob
     * @return True if accepted.
     * @throws ch.cyberduck.core.exception.ConnectionCanceledException Canceled by user
     */
    protected abstract boolean isUnknownKeyAccepted(final String hostname, final int port, final String serverHostKeyAlgorithm,
                                                    final byte[] serverHostKey) throws ConnectionCanceledException;

    /**
     * @param hostname               Hostname
     * @param port                   Port number
     * @param serverHostKeyAlgorithm Algorithm
     * @param serverHostKey          Key blob
     * @return True if accepted.
     * @throws ch.cyberduck.core.exception.ConnectionCanceledException Canceled by user
     */
    protected abstract boolean isChangedKeyAccepted(final String hostname, final int port, final String serverHostKeyAlgorithm,
                                                    final byte[] serverHostKey) throws ConnectionCanceledException;

    protected void allow(final String hostname, final String serverHostKeyAlgorithm,
                         final byte[] serverHostKey, boolean always) {
        // The following call will ONLY put the key into the memory cache!
        // To save it in a known hosts file, also call "KnownHosts.addHostkeyToFile(...)"
        final String hashedHostname = KnownHosts.createHashedHostname(hostname);
        try {
            // Add the hostkey to the in-memory database
            database.addHostkey(new String[]{hashedHostname}, serverHostKeyAlgorithm, serverHostKey);
            if(always) {
                if(this.isHostKeyDatabaseWritable()) {
                    this.save(hostname, serverHostKeyAlgorithm, serverHostKey);
                }
            }
        }
        catch(IOException e) {
            log.error(String.format("Failure adding host key to database: %s", e.getMessage()));
        }
    }

    protected abstract void save(final String hostname, final String serverHostKeyAlgorithm,
                                 final byte[] serverHostKey) throws IOException;
}