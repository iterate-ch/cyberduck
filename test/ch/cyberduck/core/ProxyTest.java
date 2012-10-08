package ch.cyberduck.core;

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

import org.junit.Test;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

/**
 * @version $Id$
 */
public class ProxyTest extends AbstractTestCase {

    @Test
    public void testWildcard() {
        AbstractProxy p = new AbstractProxy() {
            @Override
            public boolean usePassiveFTP() {
                throw new UnsupportedOperationException();
            }

            @Override
            public boolean isSOCKSProxyEnabled(Host host) {
                throw new UnsupportedOperationException();
            }

            @Override
            public String getSOCKSProxyHost(Host host) {
                throw new UnsupportedOperationException();
            }

            @Override
            public int getSOCKSProxyPort(Host host) {
                throw new UnsupportedOperationException();
            }

            @Override
            public boolean isHTTPProxyEnabled(Host host) {
                throw new UnsupportedOperationException();
            }

            @Override
            public String getHTTPProxyHost(Host host) {
                throw new UnsupportedOperationException();
            }

            @Override
            public int getHTTPProxyPort(Host host) {
                throw new UnsupportedOperationException();
            }

            @Override
            public boolean isHTTPSProxyEnabled(Host host) {
                throw new UnsupportedOperationException();
            }

            @Override
            public String getHTTPSProxyHost(Host host) {
                throw new UnsupportedOperationException();
            }

            @Override
            public int getHTTPSProxyPort(Host host) {
                throw new UnsupportedOperationException();
            }

            @Override
            public boolean matches(String wildcard, String hostname) {
                return super.matches(wildcard, hostname);
            }
        };
        assertTrue(p.matches("*.cyberduck.ch", "a.cyberduck.ch"));
        assertTrue(p.matches("*.cyberduck.ch", "a.b.cyberduck.ch"));
        assertFalse(p.matches("*.cyberduck.ch", "cyberduck.ch"));
    }
}
