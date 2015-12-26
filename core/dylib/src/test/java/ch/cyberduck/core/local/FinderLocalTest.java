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

import ch.cyberduck.core.exception.LocalAccessDeniedException;
import ch.cyberduck.core.exception.NotfoundException;

import org.apache.commons.lang3.StringUtils;
import org.junit.Assert;
import org.junit.Ignore;
import org.junit.Test;

import java.util.UUID;

import static org.junit.Assert.*;

/**
 * @version $Id$
 */
public class FinderLocalTest {

    @Test
    public void testEqual() throws Exception {
        final String name = UUID.randomUUID().toString();
        FinderLocal l = new FinderLocal(System.getProperty("java.io.tmpdir"), name);
        Assert.assertEquals(new FinderLocal(System.getProperty("java.io.tmpdir"), name), l);
        LocalTouchFactory.get().touch(l);
        Assert.assertEquals(new FinderLocal(System.getProperty("java.io.tmpdir"), name), l);
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
        Assert.assertTrue(l.exists());
        Assert.assertTrue(new FinderLocal(System.getProperty("java.io.tmpdir"), StringUtils.upperCase(name)).exists());
        Assert.assertTrue(new FinderLocal(System.getProperty("java.io.tmpdir"), StringUtils.lowerCase(name)).exists());
    }

    @Test
    public void testList() throws Exception {
        Assert.assertFalse(new FinderLocal("../profiles").list().isEmpty());
    }

    @Test(expected = LocalAccessDeniedException.class)
    public void testListNotFound() throws Exception {
        final String name = UUID.randomUUID().toString();
        FinderLocal l = new FinderLocal(System.getProperty("java.io.tmpdir"), name);
        try {
            l.list();
        }
        catch(LocalAccessDeniedException e) {
            Assert.assertEquals("The folder “" + name + "” doesn’t exist. Please verify disk permissions.", e.getDetail());
            throw e;
        }
    }

    @Test
    public void testTilde() throws Exception {
        Assert.assertEquals(System.getProperty("user.home") + "/f", new FinderLocal("~/f").getAbsolute());
        Assert.assertEquals("~/f", new FinderLocal("~/f").getAbbreviatedPath());
    }

    @Test
    public void testDisplayName() throws Exception {
        Assert.assertEquals("f/a", new FinderLocal(System.getProperty("java.io.tmpdir"), "f:a").getDisplayName());
    }

    @Test
    public void testWriteUnixPermission() throws Exception {
//        this.repeat(new Callable<Local>() {
//            @Override
//            public Local call() throws Exception {
//                Local l = new FinderLocal(System.getProperty("java.io.tmpdir"), UUID.randomUUID().toString());
//                new DefaultLocalTouchFeature().touch(l);
//                final Permission permission = new Permission(644);
//                l.attributes().setPermission(permission);
//                assertEquals(permission, l.attributes().getPermission());
//                l.delete();
//                return l;
//            }
//        }, 10);
    }

    @Test
    public void testMkdir() throws Exception {
        FinderLocal l = new FinderLocal(System.getProperty("java.io.tmpdir"), UUID.randomUUID().toString());
        l.mkdir();
        Assert.assertTrue(l.exists());
        l.mkdir();
        Assert.assertTrue(l.exists());
        l.delete();
    }

    @Test
    public void testToUrl() throws Exception {
        Assert.assertEquals("file:/c/file", new FinderLocal("/c/file").toURL());
    }

    @Test
    @Ignore
    public void testBookmark() throws Exception {
        FinderLocal l = new FinderLocal(System.getProperty("java.io.tmpdir"), UUID.randomUUID().toString());
        Assert.assertNull(l.getBookmark());
        LocalTouchFactory.get().touch(l);
        Assert.assertNotNull(l.getBookmark());
        Assert.assertEquals(l.getBookmark(), l.getBookmark());
        Assert.assertSame(l.getBookmark(), l.getBookmark());
    }

    @Test
    public void testBookmarkSaved() throws Exception {
        FinderLocal l = new FinderLocal(System.getProperty("java.io.tmpdir"), UUID.randomUUID().toString());
        Assert.assertNull(l.getBookmark());
        l.setBookmark("a");
        Assert.assertEquals("a", l.getBookmark());
        Assert.assertNotNull(l.getOutputStream(false));
        Assert.assertNotNull(l.getInputStream());
    }

    @Test(expected = NotfoundException.class)
    public void testSymlinkTargetNotfound() throws Exception {
        FinderLocal l = new FinderLocal(System.getProperty("java.io.tmpdir"), UUID.randomUUID().toString());
        try {
            Assert.assertNull(l.getSymlinkTarget());
        }
        catch(NotfoundException e) {
            Assert.assertEquals("File not found", e.getMessage());
            throw e;
        }
    }

    @Test
    public void testSymlinkTarget() throws Exception {
        FinderLocal l = new FinderLocal("/var");
        Assert.assertNotNull(l.getSymlinkTarget());
    }

    @Test
    public void testSymbolicLink() throws Exception {
        Assert.assertTrue(new FinderLocal("/tmp").isSymbolicLink());
        Assert.assertFalse(new FinderLocal("/private/tmp").isSymbolicLink());
        Assert.assertFalse(new FinderLocal("/t").isSymbolicLink());
    }

    @Test
    public void testGetSymlinkTarget() throws Exception {
        Assert.assertEquals(new FinderLocal("/private/tmp"), new FinderLocal("/tmp").getSymlinkTarget());
    }

    @Test
    public void testGetSymlinkTargetAbsolute() throws Exception {
        Assert.assertEquals(new FinderLocal("/System/Library/Frameworks/JavaVM.framework/Versions/Current/Commands/java"),
                new FinderLocal("/usr/bin/java").getSymlinkTarget());
    }
}
