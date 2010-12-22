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

/**
 * @version $Id:$
 */
public class ProxyTest extends AbstractTestCase {

    public ProxyTest(String name) {
        super(name);
    }

    public void testWildcard() {
        AbstractProxy p = new AbstractProxy() {
            public boolean usePassiveFTP() {
                throw new UnsupportedOperationException();
            }

            public boolean isSOCKSProxyEnabled(Host host) {
                throw new UnsupportedOperationException();
            }

            public String getSOCKSProxyHost() {
                throw new UnsupportedOperationException();
            }

            public int getSOCKSProxyPort() {
                throw new UnsupportedOperationException();
            }

            public boolean isHTTPProxyEnabled(Host host) {
                throw new UnsupportedOperationException();
            }

            public String getHTTPProxyHost() {
                throw new UnsupportedOperationException();
            }

            public int getHTTPProxyPort() {
                throw new UnsupportedOperationException();
            }

            public boolean isHTTPSProxyEnabled(Host host) {
                throw new UnsupportedOperationException();
            }

            public String getHTTPSProxyHost() {
                throw new UnsupportedOperationException();
            }

            public int getHTTPSProxyPort() {
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
