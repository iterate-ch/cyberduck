package ch.cyberduck.core;

/*
 * Copyright (c) 2002-2013 David Kocher. All rights reserved.
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
 * Bug fixes, suggestions and comments should be sent to feedback@cyberduck.ch
 */

import java.util.HashMap;
import java.util.Map;

/**
 * @version $Id$
 */
public abstract class CertificateStoreFactory extends Factory<CertificateStore> {

    /**
     * Registered factories
     */
    private static final Map<Platform, CertificateStoreFactory> factories
            = new HashMap<Platform, CertificateStoreFactory>();

    public static void addFactory(Platform platform, CertificateStoreFactory f) {
        factories.put(platform, f);
    }

    private static CertificateStore store;

    public static CertificateStore get() {
        if(null == store) {
            if(!factories.containsKey(NATIVE_PLATFORM)) {
                return new DisabledCertificateStore();
            }
            store = factories.get(NATIVE_PLATFORM).create();
        }
        return store;
    }
}
