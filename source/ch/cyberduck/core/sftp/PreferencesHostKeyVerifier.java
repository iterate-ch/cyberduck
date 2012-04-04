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

import ch.cyberduck.core.ConnectionCanceledException;
import ch.cyberduck.core.Preferences;

import ch.ethz.ssh2.crypto.Base64;

/**
 * Saving accepted host keys in preferences as Base64 encoded strings.
 *
 * @version $Id$
 */
public abstract class PreferencesHostKeyVerifier extends MemoryHostKeyVerifier {

    @Override
    protected boolean isHostKeyDatabaseWritable() {
        return true;
    }

    @Override
    protected boolean isUnknownKeyAccepted(String hostname, int port, String serverHostKeyAlgorithm, byte[] serverHostKey)
            throws ConnectionCanceledException {
        return String.valueOf(Base64.encode(serverHostKey)).equals(
                Preferences.instance().getProperty(String.format("ssh.hostkey.%s.%s", serverHostKeyAlgorithm, hostname)));
    }

    @Override
    protected void save(final String hostname, final String serverHostKeyAlgorithm,
                        final byte[] serverHostKey) {
        Preferences.instance().setProperty(String.format("ssh.hostkey.%s.%s", serverHostKeyAlgorithm, hostname),
                String.valueOf(Base64.encode(serverHostKey)));
    }
}
