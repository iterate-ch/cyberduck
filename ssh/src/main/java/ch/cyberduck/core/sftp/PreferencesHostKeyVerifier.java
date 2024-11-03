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

import ch.cyberduck.core.Host;
import ch.cyberduck.core.exception.BackgroundException;
import ch.cyberduck.core.preferences.Preferences;
import ch.cyberduck.core.preferences.PreferencesFactory;

import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bouncycastle.util.encoders.Base64;

import java.security.PublicKey;

import net.schmizz.sshj.common.KeyType;

/**
 * Saving accepted host keys in preferences as Base64 encoded strings.
 */
public abstract class PreferencesHostKeyVerifier extends AbstractHostKeyCallback {
    private static final Logger log = LogManager.getLogger(PreferencesHostKeyVerifier.class);

    private final Preferences preferences
        = PreferencesFactory.get();

    @Override
    public boolean verify(final Host host, final PublicKey key) throws BackgroundException {
        String lookup = preferences.getProperty(this.toFormat(host, key));
        if(StringUtils.isEmpty(lookup)) {
            // Backward compatiblity to find keys with no port number saved
            lookup = preferences.getProperty(this.toFormat(host, key, false));
        }
        if(StringUtils.equals(Base64.toBase64String(key.getEncoded()), lookup)) {
            if(log.isInfoEnabled()) {
                log.info("Accepted host key {} matching {}", key, lookup);
            }
            return true;
        }
        final boolean accept;
        if(null == lookup) {
            accept = this.isUnknownKeyAccepted(host, key);
        }
        else {
            accept = this.isChangedKeyAccepted(host, key);
        }
        return accept;
    }

    private String toFormat(final Host host, final PublicKey key) {
        return this.toFormat(host, key, true);
    }

    private String toFormat(final Host host, final PublicKey key, boolean port) {
        if(port) {
            return String.format("ssh.hostkey.%s.%s:%d", KeyType.fromKey(key), host.getHostname(), host.getPort());
        }
        return String.format("ssh.hostkey.%s.%s", KeyType.fromKey(key), host.getHostname());
    }

    @Override
    protected void allow(final Host host, final PublicKey key, final boolean persist) {
        if(persist) {
            preferences.setProperty(this.toFormat(host, key), Base64.toBase64String(key.getEncoded()));
        }
    }
}
