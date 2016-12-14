package ch.cyberduck.core.cryptomator.random;

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

import ch.cyberduck.core.random.DefaultSecureRandomProvider;
import ch.cyberduck.core.random.SecureRandomProvider;

import org.cryptomator.cryptolib.common.SecureRandomModule;

import java.security.SecureRandom;

public class FastSecureRandomProvider extends DefaultSecureRandomProvider implements SecureRandomProvider {

    private static final Object lock = new Object();

    private static FastSecureRandomProvider factory;

    public static FastSecureRandomProvider get() {
        synchronized(lock) {
            if(null == factory) {
                factory = new FastSecureRandomProvider();
            }
            return factory;
        }
    }

    private static SecureRandom seeder;

    @Override
    public SecureRandom provide() {
        synchronized(lock) {
            if(null == seeder) {
                final SecureRandom implementation = super.provide();
                seeder = new SecureRandomModule(implementation).provideFastSecureRandom(implementation);
            }
            return seeder;
        }
    }
}
