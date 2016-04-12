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
import ch.cyberduck.core.Local;
import ch.cyberduck.core.Permission;
import ch.cyberduck.core.exception.LocalAccessDeniedException;
import ch.cyberduck.core.exception.NotfoundException;

import org.apache.commons.lang3.StringUtils;
import org.junit.Assert;
import org.junit.Test;

import java.io.InputStream;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicBoolean;

import static org.junit.Assert.*;

public class FinderLocalTest {

    @Test
    public void testEqual() throws Exception {
        final String name = UUID.randomUUID().toString();
        FinderLocal l = new FinderLocal(System.getProperty("java.io.tmpdir"), name);
        assertEquals(new FinderLocal(System.getProperty("java.io.tmpdir"), name), l);
        LocalTouchFactory.get().touch(l);
        assertEquals(new FinderLocal(System.getProperty("java.io.tmpdir"), name), l);
        final FinderLocal other = new FinderLocal(System.getProperty("java.io.tmpdir"), name + "-");
        Assert.assertNotSame(other, l);
        LocalTouchFactory.get().touch(other);
        Assert.assertNotSame(other, l);
    }

    @Test(expected = LocalAccessDeniedException.class)
    public void testReadNoFile() throws Exception {
        final String name = UUID.randomUUID().toString();
        FinderLocal l = new FinderLocal(System.getProperty("java.io.tmpdir"), name);
        l.getInputStream();
    }

    @Test
    public void testNoCaseSensitive() throws Exception {
        final String name = UUID.randomUUID().toString();
        FinderLocal l = new FinderLocal(System.getProperty("java.io.tmpdir"), name);
        LocalTouchFactory.get().touch(l);
        assertTrue(l.exists());
        assertTrue(new FinderLocal(System.getProperty("java.io.tmpdir"), StringUtils.upperCase(name)).exists());
        assertTrue(new FinderLocal(System.getProperty("java.io.tmpdir"), StringUtils.lowerCase(name)).exists());
    }

    @Test
    public void testList() throws Exception {
        assertFalse(new FinderLocal("../../profiles").list().isEmpty());
    }

    @Test(expected = LocalAccessDeniedException.class)
    public void testListNotFound() throws Exception {
        final String name = UUID.randomUUID().toString();
        FinderLocal l = new FinderLocal(System.getProperty("java.io.tmpdir"), name);
        try {
            l.list();
        }
        catch(LocalAccessDeniedException e) {
            assertEquals("The folder “" + name + "” doesn’t exist. Please verify disk permissions.", e.getDetail());
            throw e;
        }
    }

    @Test
    public void testTilde() throws Exception {
        assertEquals(System.getProperty("user.home") + "/f", new FinderLocal("~/f").getAbsolute());
        assertEquals("~/f", new FinderLocal("~/f").getAbbreviatedPath());
    }

    @Test
    public void testDisplayName() throws Exception {
        assertEquals("f/a", new FinderLocal(System.getProperty("java.io.tmpdir"), "f:a").getDisplayName());
    }

    @Test
    public void testWriteUnixPermission() throws Exception {
        Local l = new FinderLocal(System.getProperty("java.io.tmpdir"), UUID.randomUUID().toString());
        new DefaultLocalTouchFeature().touch(l);
        final Permission permission = new Permission(644);
        l.attributes().setPermission(permission);
        assertEquals(permission, l.attributes().getPermission());
        l.delete();
    }

    @Test
    public void testMkdir() throws Exception {
        FinderLocal l = new FinderLocal(System.getProperty("java.io.tmpdir"), UUID.randomUUID().toString());
        l.mkdir();
        assertTrue(l.exists());
        l.mkdir();
        assertTrue(l.exists());
        l.delete();
    }

    @Test
    public void testToUrl() throws Exception {
        assertEquals("file:/c/file", new FinderLocal("/c/file").toURL());
    }

    @Test
    public void testBookmark() throws Exception {
        FinderLocal l = new FinderLocal(System.getProperty("java.io.tmpdir"), UUID.randomUUID().toString());
        assertNull(l.getBookmark());
        LocalTouchFactory.get().touch(l);
        assertNotNull(l.getBookmark());
        assertEquals(l.getBookmark(), l.getBookmark());
        assertSame(l.getBookmark(), l.getBookmark());
    }

    @Test
    public void testBookmarkSaved() throws Exception {
        FinderLocal l = new FinderLocal(System.getProperty("java.io.tmpdir"), UUID.randomUUID().toString());
        assertNull(l.getBookmark());
        l.setBookmark("a");
        assertEquals("a", l.getBookmark());
        assertNotNull(l.getOutputStream(false));
        assertNotNull(l.getInputStream());
    }

    @Test(expected = NotfoundException.class)
    public void testSymlinkTargetNotfound() throws Exception {
        FinderLocal l = new FinderLocal(System.getProperty("java.io.tmpdir"), UUID.randomUUID().toString());
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
    public void testSymbolicLink() throws Exception {
        assertTrue(new FinderLocal("/tmp").isSymbolicLink());
        assertFalse(new FinderLocal("/private/tmp").isSymbolicLink());
        assertFalse(new FinderLocal("/t").isSymbolicLink());
    }

    @Test
    public void testGetSymlinkTarget() throws Exception {
        assertEquals(new Local("/private/tmp"), new FinderLocal("/tmp").getSymlinkTarget());
    }

    @Test
    public void testGetSymlinkTargetAbsolute() throws Exception {
        assertEquals(new FinderLocal("/System/Library/Frameworks/JavaVM.framework/Versions/Current/Commands/java"),
                new FinderLocal("/usr/bin/java").getSymlinkTarget());
    }

    @Test
    public void testReleaseSecurityScopeBookmarkInputStreamClose() throws Exception {
        final AtomicBoolean released = new AtomicBoolean(false);
        FinderLocal l = new FinderLocal(System.getProperty("java.io.tmpdir"), UUID.randomUUID().toString()) {
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

    @Test(expected = LocalAccessDeniedException.class)
    public void testLockNoSuchFile() throws Exception {
        FinderLocal l = new FinderLocal(System.getProperty("java.io.tmpdir"), UUID.randomUUID().toString());
        l.lock();
    }

    @Test
    public void testLock() throws Exception {
        FinderLocal l = new FinderLocal(System.getProperty("java.io.tmpdir"), UUID.randomUUID().toString());
        new DefaultLocalTouchFeature().touch(l);
        try {
            final NSURL lock = l.lock();
            assertNotNull(lock);
            l.release(lock);
        }
        finally {
            l.delete();
        }
    }
}
