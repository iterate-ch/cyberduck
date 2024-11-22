/*
 * Copyright (c) 2002-2016 iterate GmbH. All rights reserved.
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

package ch.cyberduck.core.local;

import ch.cyberduck.binding.foundation.NSURL;
import ch.cyberduck.core.AlphanumericRandomStringService;
import ch.cyberduck.core.Local;
import ch.cyberduck.core.Permission;
import ch.cyberduck.core.SerializerFactory;
import ch.cyberduck.core.exception.AccessDeniedException;
import ch.cyberduck.core.exception.LocalAccessDeniedException;
import ch.cyberduck.core.exception.NotfoundException;
import ch.cyberduck.core.serializer.LocalDictionary;

import org.apache.commons.lang3.StringUtils;
import org.junit.Test;

import java.io.InputStream;
import java.io.OutputStream;
import java.util.concurrent.atomic.AtomicBoolean;

import com.dd.plist.NSDictionary;

import static org.junit.Assert.*;

public class FinderLocalTest {

    @Test
    public void testWriteNewFile() throws Exception {
        final FinderLocal file = new FinderLocal(System.getProperty("java.io.tmpdir"), new AlphanumericRandomStringService().random());
        final OutputStream out = file.getOutputStream(false);
        out.close();
        file.delete();
    }

    @Test
    public void testWriteExistingFile() throws Exception {
        final FinderLocal file = new FinderLocal(System.getProperty("java.io.tmpdir"), new AlphanumericRandomStringService().random());
        new DefaultLocalTouchFeature().touch(file);
        final OutputStream out = file.getOutputStream(false);
        out.close();
        file.delete();
    }

    @Test
    public void testEqual() throws Exception {
        final String name = new AlphanumericRandomStringService().random();
        FinderLocal l = new FinderLocal(System.getProperty("java.io.tmpdir"), name);
        assertEquals(new FinderLocal(System.getProperty("java.io.tmpdir"), name), l);
        new DefaultLocalTouchFeature().touch(l);
        assertEquals(new FinderLocal(System.getProperty("java.io.tmpdir"), name), l);
        final FinderLocal other = new FinderLocal(System.getProperty("java.io.tmpdir"), name + "-");
        assertNotSame(other, l);
        new DefaultLocalTouchFeature().touch(other);
        assertNotSame(other, l);
        l.delete();
        other.delete();
    }

    @Test(expected = LocalAccessDeniedException.class)
    public void testReadNoFile() throws Exception {
        final String name = new AlphanumericRandomStringService().random();
        FinderLocal l = new FinderLocal(System.getProperty("java.io.tmpdir"), name);
        l.getInputStream();
    }

    @Test
    public void testNoCaseSensitive() throws Exception {
        final String name = new AlphanumericRandomStringService().random();
        FinderLocal l = new FinderLocal(System.getProperty("java.io.tmpdir"), name);
        new DefaultLocalTouchFeature().touch(l);
        assertTrue(l.exists());
        assertTrue(new FinderLocal(System.getProperty("java.io.tmpdir"), StringUtils.upperCase(name)).exists());
        assertTrue(new FinderLocal(System.getProperty("java.io.tmpdir"), StringUtils.lowerCase(name)).exists());
        l.delete();
    }

    @Test
    public void testList() throws Exception {
        assertFalse(new FinderLocal("../../profiles").list().isEmpty());
    }

    @Test(expected = LocalAccessDeniedException.class)
    public void testListNotFound() throws Exception {
        final String name = new AlphanumericRandomStringService().random();
        FinderLocal l = new FinderLocal(System.getProperty("java.io.tmpdir"), name);
        l.list();
    }

    @Test
    public void testTilde() {
        assertEquals(System.getProperty("user.home") + "/f", new FinderLocal("~/f").getAbsolute());
        assertEquals("~/f", new FinderLocal("~/f").getAbbreviatedPath());
    }

    @Test
    public void testDisplayName() {
        assertEquals("f/a", new FinderLocal(System.getProperty("java.io.tmpdir"), "f:a").getDisplayName());
    }

    @Test
    public void testWriteUnixPermission() throws Exception {
        Local l = new FinderLocal(System.getProperty("java.io.tmpdir"), new AlphanumericRandomStringService().random());
        new DefaultLocalTouchFeature().touch(l);
        final Permission permission = new Permission(644);
        l.attributes().setPermission(permission);
        assertEquals(permission, l.attributes().getPermission());
        l.delete();
    }

    @Test
    public void testMkdir() throws Exception {
        FinderLocal l = new FinderLocal(System.getProperty("java.io.tmpdir"), new AlphanumericRandomStringService().random());
        new DefaultLocalDirectoryFeature().mkdir(l);
        assertTrue(l.exists());
        new DefaultLocalDirectoryFeature().mkdir(l);
        assertTrue(l.exists());
        l.delete();
    }

    @Test
    public void testToUrl() {
        assertEquals("file:/c/file", new FinderLocal("/c/file").toURL());
    }

    @Test
    public void testBookmarkSerialize() {
        FinderLocal l = new FinderLocal(System.getProperty("java.io.tmpdir"), new AlphanumericRandomStringService().random());
        assertNull(l.getBookmark());
        l.setBookmark("a");
        assertEquals("a", l.getBookmark());
        final NSDictionary dict = l.serialize(SerializerFactory.get());
        assertTrue(dict.containsKey("Bookmark"));
        assertEquals("a", dict.objectForKey("Bookmark").toString());
        final Local deserialized = new LocalDictionary<NSDictionary>().deserialize(dict);
        assertEquals("a", deserialized.getBookmark());
    }

    @Test(expected = NotfoundException.class)
    public void testSymlinkTargetNotfound() throws Exception {
        FinderLocal l = new FinderLocal(System.getProperty("java.io.tmpdir"), new AlphanumericRandomStringService().random());
        try {
            assertNull(l.getSymlinkTarget());
        }
        catch(NotfoundException e) {
            assertEquals("File not found", e.getMessage());
            throw e;
        }
    }

    @Test
    public void testSymlinkTarget() throws Exception {
        FinderLocal l = new FinderLocal("/var");
        assertNotNull(l.getSymlinkTarget());
    }

    @Test
    public void testSymbolicLink() {
        assertTrue(new FinderLocal("/tmp").isSymbolicLink());
        assertFalse(new FinderLocal("/private/tmp").isSymbolicLink());
        assertFalse(new FinderLocal("/t").isSymbolicLink());
    }

    @Test
    public void testGetSymlinkTarget() throws Exception {
        assertEquals(new Local("/private/tmp"), new FinderLocal("/tmp").getSymlinkTarget());
    }

    @Test
    public void testSkipSecurityScopeBookmarkInTemporary() throws Exception {
        final FinderLocal l = new FinderLocal(System.getProperty("java.io.tmpdir"), new AlphanumericRandomStringService().random()) {
            @Override
            public NSURL lock(final boolean interactive) throws AccessDeniedException {
                final NSURL lock = super.lock(interactive, new AliasFilesystemBookmarkResolver());
                assertNull(lock);
                return null;
            }
        };
        new DefaultLocalTouchFeature().touch(l);
        final InputStream in = l.getInputStream();
        in.close();
        l.delete();
    }

    @Test
    public void testReleaseSecurityScopeBookmarkInputStreamClose() throws Exception {
        final AtomicBoolean released = new AtomicBoolean(false);
        final FinderLocal l = new FinderLocal(System.getProperty("user.dir"), new AlphanumericRandomStringService().random()) {
            @Override
            public NSURL lock(final boolean interactive) throws AccessDeniedException {
                final NSURL lock = super.lock(interactive, new AliasFilesystemBookmarkResolver());
                assertNotNull(lock);
                return lock;
            }

            @Override
            public void release(final Object lock) {
                released.set(true);
                super.release(lock);
            }
        };
        new DefaultLocalTouchFeature().touch(l);
        final InputStream in = l.getInputStream();
        in.close();
        assertTrue(released.get());
        l.delete();
    }

    @Test
    public void testLockNoSuchFile() throws Exception {
        FinderLocal l = new FinderLocal(System.getProperty("java.io.tmpdir"), new AlphanumericRandomStringService().random());
        assertNull(l.lock(false));
    }

    @Test
    public void testLockTemporary() throws Exception {
        FinderLocal l = new FinderLocal(System.getProperty("java.io.tmpdir"), new AlphanumericRandomStringService().random());
        new DefaultLocalTouchFeature().touch(l);
        try {
            final NSURL lock = l.lock(false, new AliasFilesystemBookmarkResolver());
            assertNull(lock);
            l.release(lock);
        }
        finally {
            l.delete();
        }
    }

    @Test
    public void testLockUserdir() throws Exception {
        FinderLocal l = new FinderLocal(System.getProperty("user.dir"), new AlphanumericRandomStringService().random());
        new DefaultLocalTouchFeature().touch(l);
        try {
            final NSURL lock = l.lock(false, new AliasFilesystemBookmarkResolver());
            assertNotNull(lock);
            l.release(lock);
        }
        finally {
            l.delete();
        }
    }

    @Test
    public void testFollowLinks() {
        assertTrue(new Local("/tmp").exists());
    }

    @Test
    public void testIsSymbolicLink() {
        assertFalse(new FinderLocal(new AlphanumericRandomStringService().random()).isSymbolicLink());
        assertTrue(new FinderLocal("/tmp").isSymbolicLink());
    }

    @Test
    public void testMoveFolder() throws Exception {
        final String name = new AlphanumericRandomStringService().random();
        final String newname = new AlphanumericRandomStringService().random();
        new DefaultLocalDirectoryFeature().mkdir(new FinderLocal(name));
        new FinderLocal(name).rename(new FinderLocal(newname));
        assertFalse(new FinderLocal(name).exists());
        assertTrue(new FinderLocal(newname).exists());
    }
}
