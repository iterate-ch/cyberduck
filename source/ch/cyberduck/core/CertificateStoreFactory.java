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

/**
 * @version $Id$
 */
public class CertificateStoreFactory extends Factory<CertificateStore> {

    protected CertificateStoreFactory() {
        super("factory.certificatestore.class");
    }

    private static CertificateStore store;

    public static synchronized CertificateStore get() {
        if(null == store) {
            store = new CertificateStoreFactory().create();
        }
        return store;
    }
}
