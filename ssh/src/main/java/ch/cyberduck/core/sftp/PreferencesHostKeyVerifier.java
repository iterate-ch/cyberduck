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

import ch.cyberduck.core.exception.ChecksumException;
import ch.cyberduck.core.exception.ConnectionCanceledException;
import ch.cyberduck.core.preferences.Preferences;
import ch.cyberduck.core.preferences.PreferencesFactory;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.bouncycastle.util.encoders.Base64;

import java.security.PublicKey;

import net.schmizz.sshj.common.KeyType;

/**
 * Saving accepted host keys in preferences as Base64 encoded strings.
 */
public abstract class PreferencesHostKeyVerifier extends AbstractHostKeyCallback {
    private static final Logger log = Logger.getLogger(PreferencesHostKeyVerifier.class);

    private final Preferences preferences
            = PreferencesFactory.get();

    @Override
    public boolean verify(final String hostname, final int port, final PublicKey key)
            throws ConnectionCanceledException, ChecksumException {
        final String lookup = preferences.getProperty(this.getFormat(hostname, key));
        if(StringUtils.equals(Base64.toBase64String(key.getEncoded()), lookup)) {
            if(log.isInfoEnabled()) {
                log.info(String.format("Accepted host key %s matching %s", key, lookup));
            }
            return true;
        }
        final boolean accept;
        if(null == lookup) {
            accept = this.isUnknownKeyAccepted(hostname, key);
        }
        else {
            accept = this.isChangedKeyAccepted(hostname, key);
        }
        return accept;
    }

    private String getFormat(final String hostname, final PublicKey key) {
        return String.format("ssh.hostkey.%s.%s", KeyType.fromKey(key), hostname);
    }

    @Override
    protected void allow(final String hostname, final PublicKey key, final boolean persist) {
        if(persist) {
            preferences.setProperty(this.getFormat(hostname, key), Base64.toBase64String(key.getEncoded()));
        }
    }
}
