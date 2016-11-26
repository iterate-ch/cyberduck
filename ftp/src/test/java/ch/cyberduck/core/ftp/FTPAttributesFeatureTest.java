package ch.cyberduck.core.ftp;

/*
 * Copyright (c) 2002-2014 David Kocher. All rights reserved.
 * http://cyberduck.io/
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
 * feedback@cyberduck.io
 */

import ch.cyberduck.core.AttributedList;
import ch.cyberduck.core.Attributes;
import ch.cyberduck.core.Credentials;
import ch.cyberduck.core.DisabledCancelCallback;
import ch.cyberduck.core.DisabledHostKeyCallback;
import ch.cyberduck.core.DisabledLoginCallback;
import ch.cyberduck.core.DisabledPasswordStore;
import ch.cyberduck.core.Host;
import ch.cyberduck.core.ListProgressListener;
import ch.cyberduck.core.Path;
import ch.cyberduck.core.PathCache;
import ch.cyberduck.core.Permission;
import ch.cyberduck.core.exception.AccessDeniedException;
import ch.cyberduck.core.exception.BackgroundException;
import ch.cyberduck.core.exception.InteroperabilityException;
import ch.cyberduck.core.exception.NotfoundException;
import ch.cyberduck.test.IntegrationTest;

import org.junit.Ignore;
import org.junit.Test;
import org.junit.experimental.categories.Category;

import java.util.EnumSet;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

@Category(IntegrationTest.class)
public class FTPAttributesFeatureTest {

    @Test(expected = InteroperabilityException.class)
    public void testAttributesUnknownCommand() throws Exception {
        final Host host = new Host(new FTPTLSProtocol(), "test.cyberduck.ch", new Credentials(
                System.getProperties().getProperty("ftp.user"), System.getProperties().getProperty("ftp.password")
        ));
        final FTPSession session = new FTPSession(host) {
            @Override
            public AttributedList<Path> list(final Path file, final ListProgressListener listener) throws BackgroundException {
                throw new AccessDeniedException("f");
            }
        };
        session.open(new DisabledHostKeyCallback());
        session.login(new DisabledPasswordStore(), new DisabledLoginCallback(), new DisabledCancelCallback(), PathCache.empty());
        final FTPAttributesFeature f = new FTPAttributesFeature(session);
        f.find(new Path(new FTPWorkdirService(session).find(), "test", EnumSet.of(Path.Type.file)));
        session.close();
    }

    @Test
    @Ignore
    public void testAttributesWrongFiletype() throws Exception {
        final Host host = new Host(new FTPTLSProtocol(), "test.cyberduck.ch", new Credentials(
                System.getProperties().getProperty("ftp.user"), System.getProperties().getProperty("ftp.password")
        ));
        final FTPSession session = new FTPSession(host) {
            @Override
            public AttributedList<Path> list(final Path file, final ListProgressListener listener) throws BackgroundException {
                throw new AccessDeniedException("f");
            }
        };
        session.open(new DisabledHostKeyCallback());
        session.login(new DisabledPasswordStore(), new DisabledLoginCallback(), new DisabledCancelCallback(), PathCache.empty());
        final FTPAttributesFeature f = new FTPAttributesFeature(session);
        final Path file = new Path(new FTPWorkdirService(session).find(), "test", EnumSet.of(Path.Type.file));
        final Attributes attributes = f.find(file);
        assertEquals(0L, attributes.getSize());
        assertEquals("1106", attributes.getOwner());
        assertEquals(new Permission("-rw-rw-rw-"), attributes.getPermission());
        // Test wrong type
        try {
            f.find(new Path(new FTPWorkdirService(session).find(), "test", EnumSet.of(Path.Type.directory)));
            fail();
        }
        catch(NotfoundException e) {
            // Expected
        }
        session.close();
    }
}
