package ch.cyberduck.ui.resources;

/*
 * Copyright (c) 2012 David Kocher. All rights reserved.
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

import ch.cyberduck.core.AbstractTestCase;
import ch.cyberduck.core.NullLocal;
import ch.cyberduck.core.Protocol;
import ch.cyberduck.core.ProtocolFactory;
import ch.cyberduck.core.local.Application;
import ch.cyberduck.ui.cocoa.application.NSImage;

import org.junit.Ignore;
import org.junit.Test;

import java.util.UUID;

import static org.junit.Assert.*;

/**
 * @version $Id$
 */
public class NSImageIconCacheTest extends AbstractTestCase {

    @Test
    public void testFolderIcon16() throws Exception {
        final NSImage icon = new NSImageIconCache().folderIcon(16);
        assertNotNull(icon);
        assertTrue(icon.isValid());
        assertFalse(icon.isTemplate());
        assertEquals(16, icon.size().width.intValue());
        assertEquals(16, icon.size().height.intValue());
        assertEquals(1, icon.representations().count().intValue());
    }

    @Test
    public void testFolderIcon32() throws Exception {
        final NSImage icon = new NSImageIconCache().folderIcon(32);
        assertNotNull(icon);
        assertTrue(icon.isValid());
        assertFalse(icon.isTemplate());
        assertEquals(32, icon.size().width.intValue());
        assertEquals(32, icon.size().height.intValue());
        assertEquals(1, icon.representations().count().intValue());
    }

    @Test
    public void testFolderIcon64() throws Exception {
        final NSImage icon = new NSImageIconCache().folderIcon(64);
        assertNotNull(icon);
        assertTrue(icon.isValid());
        assertFalse(icon.isTemplate());
        assertEquals(64, icon.size().width.intValue());
        assertEquals(64, icon.size().height.intValue());
        assertEquals(1, icon.representations().count().intValue());
    }

    @Test
    public void testFolderIcon128() throws Exception {
        final NSImage icon = new NSImageIconCache().folderIcon(128);
        assertNotNull(icon);
        assertTrue(icon.isValid());
        assertFalse(icon.isTemplate());
        assertEquals(128, icon.size().width.intValue());
        assertEquals(128, icon.size().height.intValue());
        assertEquals(1, icon.representations().count().intValue());
    }

    @Test
    public void testFolderIcon256() throws Exception {
        final NSImage icon = new NSImageIconCache().folderIcon(256);
        assertNotNull(icon);
        assertTrue(icon.isValid());
        assertFalse(icon.isTemplate());
        assertEquals(256, icon.size().width.intValue());
        assertEquals(256, icon.size().height.intValue());
        assertEquals(1, icon.representations().count().intValue());
    }

    @Test
    public void testFolderIcon512() throws Exception {
        final NSImage icon = new NSImageIconCache().folderIcon(512);
        assertNotNull(icon);
        assertTrue(icon.isValid());
        assertFalse(icon.isTemplate());
        assertEquals(512, icon.size().width.intValue());
        assertEquals(512, icon.size().height.intValue());
        assertEquals(1, icon.representations().count().intValue());
    }

    @Test
    public void testFolderIconAllSizes() throws Exception {
        final NSImage icon = new NSImageIconCache().folderIcon(null);
        assertNotNull(icon);
        assertTrue(icon.isValid());
        assertFalse(icon.isTemplate());
        assertEquals(1, icon.representations().count().intValue());
    }

    @Test
    public void testDocumentIcon() throws Exception {
        final NSImage icon = new NSImageIconCache().documentIcon("txt", 64);
        assertNotNull(icon);
        assertTrue(icon.isValid());
        assertFalse(icon.isTemplate());
        assertEquals(64, icon.size().width.intValue());
        assertEquals(64, icon.size().height.intValue());
//        assertEquals(4, icon.representations().count().intValue());
    }

    @Test
    @Ignore
    public void testVolume() throws Exception {
        final NSImageIconCache cache = new NSImageIconCache();
        for(Protocol p : ProtocolFactory.getKnownProtocols()) {
            assertNotNull(cache.volumeIcon(p, 32));
        }
    }

    @Test
    public void testIconForApplication() throws Exception {
        final NSImageIconCache cache = new NSImageIconCache();
        assertNotNull(cache.applicationIcon(new Application("com.apple.TextEdit"), 32));
    }

    @Test
    public void testIconForPath() throws Exception {
        final NullLocal f = new NullLocal(System.getProperty("java.io.tmpdir"), UUID.randomUUID().toString() + ".txt");
        final NSImageIconCache cache = new NSImageIconCache();
        NSImage icon = cache.fileIcon(f, 16);
        assertNull(icon);
        assertTrue(f.touch());
        icon = cache.fileIcon(f, 16);
        assertNotNull(icon);
        assertTrue(icon.isValid());
        assertFalse(icon.isTemplate());
//        assertEquals(4, icon.representations().count().intValue());
        f.delete();
        assertNull(cache.fileIcon(f, 16));
    }
}