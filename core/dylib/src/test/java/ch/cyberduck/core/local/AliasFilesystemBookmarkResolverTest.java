package ch.cyberduck.core.local;

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

import ch.cyberduck.binding.foundation.NSURL;
import ch.cyberduck.core.AlphanumericRandomStringService;
import ch.cyberduck.core.Local;
import ch.cyberduck.core.exception.AccessDeniedException;

import org.junit.Test;

import java.util.UUID;

import static org.junit.Assert.*;

public class AliasFilesystemBookmarkResolverTest {

    @Test
    public void testCreateNotFound() throws Exception {
        final String name = UUID.randomUUID().toString();
        final FinderLocal l = new FinderLocal(System.getProperty("user.dir"), name);
        assertNull(new AliasFilesystemBookmarkResolver().create(l));
        assertEquals(-1L, l.attributes().getSize());
    }

    @Test
    public void testCreate() throws Exception {
        final String name = UUID.randomUUID().toString();
        final FinderLocal l = new FinderLocal(System.getProperty("user.dir"), name);
        new DefaultLocalTouchFeature().touch(l);
        try {
            final AliasFilesystemBookmarkResolver resolver = new AliasFilesystemBookmarkResolver();
            final String bookmark = resolver.create(l);
            assertNotNull(bookmark);
            final NSURL resolved = resolver.resolve(bookmark);
            assertNotNull(resolved);
        }
        finally {
            l.delete();
        }
    }

    @Test
    public void testRename() throws Exception {
        final FinderLocal source = new FinderLocal(System.getProperty("user.dir"), new AlphanumericRandomStringService().random()) {
            @Override
            public NSURL lock(final boolean interactive) throws AccessDeniedException {
                return this.lock(interactive, new AliasFilesystemBookmarkResolver());
            }
        };
        new DefaultLocalTouchFeature().touch(source);
        final FinderLocal target = new FinderLocal(System.getProperty("user.dir"), new AlphanumericRandomStringService().random()) {
            @Override
            public NSURL lock(final boolean interactive) throws AccessDeniedException {
                return this.lock(interactive, new AliasFilesystemBookmarkResolver());
            }
        };
        new DefaultLocalTouchFeature().touch(target);
        final AliasFilesystemBookmarkResolver resolver = new AliasFilesystemBookmarkResolver();

        final String bookmarkSource = resolver.create(source);
        assertNotNull(bookmarkSource);
        assertNotNull(resolver.resolve(bookmarkSource));
        assertEquals(source.getAbsolute(), resolver.resolve(bookmarkSource).path());

        final String bookmarkTarget = resolver.create(target);
        assertNotNull(bookmarkTarget);
        assertNotNull(resolver.resolve(bookmarkTarget));
        assertEquals(target.getAbsolute(), resolver.resolve(bookmarkTarget).path());

        new Local(source.getAbsolute()).rename(target);
        assertFalse(source.exists());
        assertTrue(target.exists());

        assertEquals(target.getAbsolute(), resolver.resolve(bookmarkSource).path());
        assertEquals(target.getAbsolute(), resolver.resolve(bookmarkTarget).path());

        source.setBookmark(bookmarkSource);
        assertTrue(source.exists());
        assertEquals(0, source.attributes().getSize());

        target.delete();
    }
}
