package ch.cyberduck.core.resources;

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

import ch.cyberduck.binding.application.NSImage;
import ch.cyberduck.core.AlphanumericRandomStringService;
import ch.cyberduck.core.Factory;
import ch.cyberduck.core.Local;
import ch.cyberduck.core.Path;
import ch.cyberduck.core.local.Application;
import ch.cyberduck.core.local.FinderLocal;
import ch.cyberduck.core.local.LocalTouchFactory;

import org.junit.Ignore;
import org.junit.Test;

import java.util.EnumSet;
import java.util.UUID;

import static org.junit.Assert.*;
import static org.junit.Assume.assumeFalse;

public class NSImageIconCacheTest {

    @Test
    public void testFolderIcon16() {
        final NSImage icon = new NSImageIconCache().folderIcon(16);
        assertNotNull(icon);
        assertTrue(icon.isValid());
        assertFalse(icon.isTemplate());
        assertEquals(16, icon.size().width.intValue());
        assertEquals(16, icon.size().height.intValue());
        assertTrue(icon.representations().count().intValue() >= 1);
    }

    @Test
    public void testFolderIcon32() {
        final NSImage icon = new NSImageIconCache().folderIcon(32);
        assertNotNull(icon);
        assertTrue(icon.isValid());
        assertFalse(icon.isTemplate());
        assertEquals(32, icon.size().width.intValue());
        assertEquals(32, icon.size().height.intValue());
        assertTrue(icon.representations().count().intValue() >= 1);
    }

    @Test
    public void testFolderIcon64() {
        final NSImage icon = new NSImageIconCache().folderIcon(64);
        assertNotNull(icon);
        assertTrue(icon.isValid());
        assertFalse(icon.isTemplate());
        assertEquals(64, icon.size().width.intValue());
        assertEquals(64, icon.size().height.intValue());
        assertTrue(icon.representations().count().intValue() >= 1);
    }

    @Test
    public void testFolderIcon128() {
        final NSImage icon = new NSImageIconCache().folderIcon(128);
        assertNotNull(icon);
        assertTrue(icon.isValid());
        assertFalse(icon.isTemplate());
        assertEquals(128, icon.size().width.intValue());
        assertEquals(128, icon.size().height.intValue());
        assertTrue(icon.representations().count().intValue() >= 1);
    }

    @Test
    public void testFolderIcon256() {
        final NSImage icon = new NSImageIconCache().folderIcon(256);
        assertNotNull(icon);
        assertTrue(icon.isValid());
        assertFalse(icon.isTemplate());
        assertEquals(256, icon.size().width.intValue());
        assertEquals(256, icon.size().height.intValue());
        assertTrue(icon.representations().count().intValue() >= 1);
    }

    @Test
    public void testFolderIcon512() {
        final NSImage icon = new NSImageIconCache().folderIcon(512);
        assertNotNull(icon);
        assertTrue(icon.isValid());
        assertFalse(icon.isTemplate());
        assertEquals(512, icon.size().width.intValue());
        assertEquals(512, icon.size().height.intValue());
        assertTrue(icon.representations().count().intValue() >= 1);
    }

    @Test
    public void testFolderIconAllSizes() {
        final NSImage icon = new NSImageIconCache().folderIcon(null);
        assertNotNull(icon);
        assertTrue(icon.isValid());
        assertFalse(icon.isTemplate());
        assertTrue(icon.representations().count().intValue() >= 1);
    }

    @Test
    public void testDocumentIcon() {
        final NSImage icon = new NSImageIconCache().documentIcon("txt", 64);
        assertNotNull(icon);
        assertTrue(icon.isValid());
        assertFalse(icon.isTemplate());
        assertEquals(64, icon.size().width.intValue());
        assertEquals(64, icon.size().height.intValue());
        assertNotNull(NSImage.imageNamed("txt (64px)"));
    }

    @Test
    public void testDocumentIconNoExtension() {
        final NSImage icon = new NSImageIconCache().documentIcon("", 64);
        assertNotNull(icon);
        assertTrue(icon.isValid());
        assertFalse(icon.isTemplate());
        assertEquals(64, icon.size().width.intValue());
        assertEquals(64, icon.size().height.intValue());
    }

    @Test
    public void testDocumentIconNullExtension() {
        final NSImage icon = new NSImageIconCache().documentIcon(null, 64);
        assertNotNull(icon);
        assertTrue(icon.isValid());
        assertFalse(icon.isTemplate());
        assertEquals(64, icon.size().width.intValue());
        assertEquals(64, icon.size().height.intValue());
    }

    @Test
    public void testIconForApplication() {
        final NSImageIconCache cache = new NSImageIconCache();
        assertNotNull(cache.applicationIcon(new Application("com.apple.TextEdit"), 32));
    }

    @Test
    public void testCacheApplicationIcon() {
        final NSImageIconCache cache = new NSImageIconCache();
        final NSImage icon32 = cache.applicationIcon(new Application("com.apple.TextEdit"), 32);
        assertNotNull(icon32);
        assertEquals(32, icon32.size().width.intValue());
        assertEquals(32, icon32.size().height.intValue());
        final NSImage icon16 = cache.applicationIcon(new Application("com.apple.TextEdit"), 16);
        assertNotNull(icon16);
        assertEquals(16, icon16.size().width.intValue());
        assertEquals(16, icon16.size().height.intValue());
        final NSImage icon64 = cache.applicationIcon(new Application("com.apple.TextEdit"), 64);
        assertNotNull(icon64);
        assertEquals(64, icon64.size().width.intValue());
        assertEquals(64, icon64.size().height.intValue());
    }

    @Test
    public void testCacheSystemIcon() {
        final NSImageIconCache cache = new NSImageIconCache();
        final NSImage icon32 = cache.iconNamed("NSComputer", 32);
        assertNotNull(icon32);
        assertEquals(32, icon32.size().width.intValue());
        assertEquals(32, icon32.size().height.intValue());
        final NSImage icon16 = cache.iconNamed("NSComputer", 16);
        assertNotNull(icon16);
        assertEquals(16, icon16.size().width.intValue());
        assertEquals(16, icon16.size().height.intValue());
        final NSImage icon64 = cache.iconNamed("NSComputer", 64);
        assertNotNull(icon64);
        assertEquals(64, icon64.size().width.intValue());
        assertEquals(64, icon64.size().height.intValue());
        assertNotNull(NSImage.imageNamed("NSComputer (16px)"));
        assertNotNull(NSImage.imageNamed("NSComputer (32px)"));
        assertNotNull(NSImage.imageNamed("NSComputer (64px)"));
    }

    @Test
    public void testCacheTiff() {
        final NSImageIconCache cache = new NSImageIconCache();
        final NSImage icon32 = cache.fileIcon(new FinderLocal("../../img/ftp.tiff"), 32);
        assertNotNull(icon32);
        assertEquals(32, icon32.size().width.intValue());
        assertEquals(32, icon32.size().height.intValue());
        final NSImage icon16 = cache.fileIcon(new FinderLocal("../../img/ftp.tiff"), 16);
        assertNotNull(icon16);
        assertNotSame(icon16, icon32);
        assertEquals(16, icon16.size().width.intValue());
        assertEquals(16, icon16.size().height.intValue());
        final NSImage icon64 = cache.fileIcon(new FinderLocal("../../img/ftp.tiff"), 64);
        assertNotNull(icon64);
        assertNotSame(icon16, icon64);
        assertNotSame(icon32, icon64);
        assertEquals(64, icon64.size().width.intValue());
        assertEquals(64, icon64.size().height.intValue());
        assertNotNull(NSImage.imageNamed("ftp.tiff (16px)"));
        assertNotNull(NSImage.imageNamed("ftp.tiff (32px)"));
        assertNotNull(NSImage.imageNamed("ftp.tiff (64px)"));
    }

    @Test
    public void testIconForFileTxtType() throws Exception {
        final Local f = new FinderLocal(System.getProperty("java.io.tmpdir"), UUID.randomUUID().toString() + ".txt");
        final NSImageIconCache cache = new NSImageIconCache();
        NSImage icon = cache.fileIcon(f, 16);
        assertNull(icon);
        LocalTouchFactory.get().touch(f);
        icon = cache.fileIcon(f, 16);
        assertNotNull(icon);
        assertTrue(icon.isValid());
        assertFalse(icon.isTemplate());
        f.delete();
    }

    @Test
    @Ignore
    public void testIconForPathPdfType() throws Exception {
        final Path f = new Path("/f.pdf", EnumSet.of(Path.Type.file));
        final NSImageIconCache cache = new NSImageIconCache();
        NSImage icon = cache.fileIcon(f, 16);
        assertNotNull(icon);
        assertNotEquals(icon.TIFFRepresentation().base64Encoding(),
            cache.fileIcon(new FinderLocal("../../img/notfound.tiff"), 16).TIFFRepresentation().base64Encoding());
        assertTrue(icon.isValid());
        assertFalse(icon.isTemplate());
    }

    @Test
    public void testIconForPathFolder() throws Exception {
        final Path f = new Path(new AlphanumericRandomStringService().random(), EnumSet.of(Path.Type.directory));
        final NSImageIconCache cache = new NSImageIconCache();
        NSImage icon = cache.fileIcon(f, 16);
        assertNotNull(icon);
        assertTrue(icon.isValid());
        assertFalse(icon.isTemplate());
    }

    @Test
    public void testIconNotfound() {
        final NSImageIconCache cache = new NSImageIconCache();
        assertNull(cache.iconNamed(null, 64));
    }

    @Test
    public void testSymbol() {
        assumeFalse(Factory.Platform.osversion.matches("10\\.(12|13|14|15).*"));
        final NSImageIconCache cache = new NSImageIconCache();
        assertNotNull(cache.iconNamed("bell.badge", 64));
    }
}
