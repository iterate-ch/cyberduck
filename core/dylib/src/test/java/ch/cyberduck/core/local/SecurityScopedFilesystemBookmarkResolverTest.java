package ch.cyberduck.core.local;

/*
 * Copyright (c) 2002-2015 David Kocher. All rights reserved.
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

import ch.cyberduck.binding.foundation.NSURL;
import ch.cyberduck.core.Local;

import org.junit.Test;

import java.util.UUID;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

public class SecurityScopedFilesystemBookmarkResolverTest {

    @Test
    public void testCreateNotFound() throws Exception {
        final String name = UUID.randomUUID().toString();
        Local l = new FinderLocal(System.getProperty("user.dir"), name);
        assertNull(new SecurityScopedFilesystemBookmarkResolver().create(l));
    }

    @Test
    public void testCreateFileUserdir() throws Exception {
        final String name = UUID.randomUUID().toString();
        Local l = new FinderLocal(System.getProperty("user.dir"), name);
        new DefaultLocalTouchFeature().touch(l);
        try {
            final SecurityScopedFilesystemBookmarkResolver resolver = new SecurityScopedFilesystemBookmarkResolver();
            final String bookmark = resolver.create(l);
            assertNotNull(bookmark);
            final NSURL resolved = resolver.resolve(bookmark);
            assertNotNull(resolved);
        }
        finally {
            l.delete();
        }
    }
}