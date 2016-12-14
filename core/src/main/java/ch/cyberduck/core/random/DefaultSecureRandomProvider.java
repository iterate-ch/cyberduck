package ch.cyberduck.core.random;

/*
 * Copyright (c) 2002-2016 iterate GmbH. All rights reserved.
 * https://cyberduck.io/
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
 */

import ch.cyberduck.core.preferences.PreferencesFactory;

import org.apache.log4j.Logger;

import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.SecureRandom;

public class DefaultSecureRandomProvider implements SecureRandomProvider {
    private static final Logger log = Logger.getLogger(DefaultSecureRandomProvider.class);

    private static final Object lock = new Object();

    private static DefaultSecureRandomProvider factory;

    public static DefaultSecureRandomProvider get() {
        synchronized(lock) {
            if(null == factory) {
                factory = new DefaultSecureRandomProvider();
            }
            return factory;
        }
    }

    private static SecureRandom seeder;

    @Override
    public SecureRandom provide() {
        synchronized(lock) {
            if(null == seeder) {
                try {
                    // Obtains random numbers from the underlying native OS, without blocking to prevent
                    // from excessive stalling. For example, /dev/urandom
                    seeder = SecureRandom.getInstance(PreferencesFactory.get().getProperty("connection.ssl.securerandom.algorithm"),
                            PreferencesFactory.get().getProperty("connection.ssl.securerandom.provider"));
                }
                catch(NoSuchAlgorithmException | NoSuchProviderException e) {
                    log.warn(String.format("Failure %s obtaining secure random", e.getMessage()));
                    // Keep null for default secure random
                    return null;
                }
            }
            return seeder;
        }
    }
}
