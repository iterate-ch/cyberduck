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
import ch.ethz.ssh2.KnownHosts;
import ch.ethz.ssh2.crypto.Base64;

import org.apache.log4j.Logger;

/**
 * Saving accepted host keys in preferences as Base64 encoded strings.
 *
 * @version $Id:$
 */
public abstract class PreferencesHostsHostKeyVerifier extends HostKeyController {
    protected static Logger log = Logger.getLogger(PreferencesHostsHostKeyVerifier.class);

    protected boolean isHostKeyDatabaseWritable() {
        return true;
    }

    @Override
    protected boolean isUnknownKeyAccepted(String hostname, int port, String serverHostKeyAlgorithm, byte[] serverHostKey)
            throws ConnectionCanceledException {

        if(String.valueOf(Base64.encode(serverHostKey)).equals(
                Preferences.instance().getProperty("ssh.hostkey." + serverHostKeyAlgorithm + "." + KnownHosts.createHashedHostname(hostname)))) {
            return true;
        }
        return false;
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
        Preferences.instance().setProperty("ssh.hostkey." + serverHostKeyAlgorithm + "." + KnownHosts.createHashedHostname(hostname),
                String.valueOf(Base64.encode(serverHostKey)));
    }
}
