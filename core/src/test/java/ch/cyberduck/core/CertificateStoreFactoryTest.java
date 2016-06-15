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
 * Bug fixes, suggestions and comments should be sent to:
 * feedback@cyberduck.ch
 */

import ch.cyberduck.core.threading.MainAction;

import org.junit.Test;

import static org.junit.Assert.assertNotSame;
import static org.junit.Assert.assertSame;

public class CertificateStoreFactoryTest {

    public void testGet() throws Exception {
        assertSame(CertificateStoreFactory.get(), CertificateStoreFactory.get());
    }

    @Test
    public void testGetWithCustomController() throws Exception {
        assertNotSame(CertificateStoreFactory.get(new AbstractController() {
            @Override
            public void invoke(final MainAction runnable, final boolean wait) {
                //
            }
        }), CertificateStoreFactory.get(new AbstractController() {
            @Override
            public void invoke(final MainAction runnable, final boolean wait) {
                //
            }
        }));
    }
}
