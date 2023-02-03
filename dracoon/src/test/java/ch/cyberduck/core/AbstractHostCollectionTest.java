package ch.cyberduck.core;

/*
 * Copyright (c) 2002-2019 iterate GmbH. All rights reserved.
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

import ch.cyberduck.core.sds.SDSProtocol;
import ch.cyberduck.core.serializer.impl.dd.ProfilePlistReader;

import org.junit.Test;

import java.util.Collections;
import java.util.Optional;

import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;

public class AbstractHostCollectionTest {

    @Test
    public void testFindDefaultPath() throws Exception {
        final AbstractHostCollection c = new AbstractHostCollection() {
        };
        final Profile profileOAuth = new ProfilePlistReader(new ProtocolFactory(Collections.singleton(new SDSProtocol()))).read(
            this.getClass().getResourceAsStream("/DRACOON (OAuth).cyberduckprofile"));
        {
            final Host bookmarkOAuth = new Host(profileOAuth);
            bookmarkOAuth.setDefaultPath("/myotherroom");
            c.add(bookmarkOAuth);
            final Host parsed = new HostParser(new ProtocolFactory(Collections.singleton(profileOAuth))).get("dracoon://dracoon.team/myroom/more/path");
            final Optional<Host> matched = c.find(parsed);
            assertTrue(matched.isPresent());
            assertSame(bookmarkOAuth, matched.get());
        }
        {
            final Host bookmarkOAuth = new Host(profileOAuth);
            bookmarkOAuth.setDefaultPath("/myroom");
            c.add(bookmarkOAuth);
            final Host parsed = new HostParser(new ProtocolFactory(Collections.singleton(profileOAuth))).get("dracoon://dracoon.team/myroom/more/path");
            final Optional<Host> matched = c.find(parsed);
            assertTrue(matched.isPresent());
            assertSame(bookmarkOAuth, matched.get());
        }
    }
}
