package ch.cyberduck.core.freenet;

/*
 * Copyright (c) 2002-2021 iterate GmbH. All rights reserved.
 * https://cyberduck.io/
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 */

import ch.cyberduck.core.DefaultHostPasswordStore;
import ch.cyberduck.core.DescriptiveUrl;
import ch.cyberduck.core.Scheme;
import ch.cyberduck.core.exception.LocalAccessDeniedException;
import ch.cyberduck.test.IntegrationTest;

import org.junit.Test;
import org.junit.experimental.categories.Category;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;

@Category(IntegrationTest.class)
public class FreenetAuthenticatedUrlProviderTest extends AbstractFreenetTest {

    @Test
    public void testToUrl() {
        final FreenetAuthenticatedUrlProvider provider = new FreenetAuthenticatedUrlProvider(new DefaultHostPasswordStore() {
            @Override
            public String getPassword(final String serviceName, final String accountName) throws LocalAccessDeniedException {
                return PROPERTIES.get("freenet.password");
            }

            @Override
            public void addPassword(final String serviceName, final String accountName, final String password) throws LocalAccessDeniedException {

            }

            @Override
            public String getPassword(final Scheme scheme, final int port, final String hostname, final String user) throws LocalAccessDeniedException {
                return System.getProperties().getProperty("freenet.password");
            }

            @Override
            public void addPassword(final Scheme scheme, final int port, final String hostname, final String user, final String password) throws LocalAccessDeniedException {

            }

            @Override
            public void deletePassword(final String serviceName, final String user) throws LocalAccessDeniedException {

            }

            @Override
            public void deletePassword(final Scheme scheme, final int port, final String hostname, final String user) throws LocalAccessDeniedException {

            }
        });
        final DescriptiveUrl url = provider.toUrl(session.getHost());
        assertNotEquals(DescriptiveUrl.EMPTY, url);
        assertEquals(DescriptiveUrl.Type.authenticated, url.getType());
    }
}
