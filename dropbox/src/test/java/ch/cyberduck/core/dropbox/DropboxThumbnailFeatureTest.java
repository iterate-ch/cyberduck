package ch.cyberduck.core.dropbox;

/*
 * Copyright (c) 2002-2026 iterate GmbH. All rights reserved.
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

import ch.cyberduck.core.AbstractDropboxTest;
import ch.cyberduck.core.AlphanumericRandomStringService;
import ch.cyberduck.core.ConnectionCallback;
import ch.cyberduck.core.LoginCallback;
import ch.cyberduck.core.Path;
import ch.cyberduck.core.exception.NotfoundException;
import ch.cyberduck.core.features.Delete;
import ch.cyberduck.core.features.Thumbnail;
import ch.cyberduck.core.io.StreamCopier;
import ch.cyberduck.core.shared.DefaultHomeFinderService;
import ch.cyberduck.core.transfer.TransferStatus;
import ch.cyberduck.test.IntegrationTest;

import org.apache.commons.io.IOUtils;
import org.junit.Test;
import org.junit.experimental.categories.Category;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Collections;
import java.util.EnumSet;

import com.dropbox.core.v2.files.ThumbnailSize;

import static org.junit.Assert.*;

@Category(IntegrationTest.class)
public class DropboxThumbnailFeatureTest extends AbstractDropboxTest {

    @Test
    public void testToThumbnailSize() {
        assertEquals(ThumbnailSize.W32H32, DropboxThumbnailFeature.toThumbnailSize(16));
        assertEquals(ThumbnailSize.W32H32, DropboxThumbnailFeature.toThumbnailSize(32));
        assertEquals(ThumbnailSize.W256H256, DropboxThumbnailFeature.toThumbnailSize(150));
        assertEquals(ThumbnailSize.W1024H768, DropboxThumbnailFeature.toThumbnailSize(1024));
        assertEquals(ThumbnailSize.W3200H2400, DropboxThumbnailFeature.toThumbnailSize(4096));
    }

    @Test
    public void testIsSupported() {
        final Thumbnail feature = new DropboxThumbnailFeature(session);
        assertTrue(feature.isSupported(new Path("/f.png", EnumSet.of(Path.Type.file))));
        assertTrue(feature.isSupported(new Path("/f.JPG", EnumSet.of(Path.Type.file))));
        assertFalse(feature.isSupported(new Path("/f.pdf", EnumSet.of(Path.Type.file))));
        assertFalse(feature.isSupported(new Path("/f.png", EnumSet.of(Path.Type.directory))));
        assertFalse(feature.isSupported(new Path("/f", EnumSet.of(Path.Type.file))));
    }

    @Test(expected = NotfoundException.class)
    public void testThumbnailNotFound() throws Exception {
        final Path drive = new DefaultHomeFinderService(session).find();
        new DropboxThumbnailFeature(session).thumbnail(
                new Path(drive, String.format("%s.png", new AlphanumericRandomStringService().random()), EnumSet.of(Path.Type.file)), 150);
    }

    @Test
    public void testThumbnail() throws Exception {
        final Path drive = new DefaultHomeFinderService(session).find();
        final Path test = new Path(drive, String.format("%s.png", new AlphanumericRandomStringService().random()), EnumSet.of(Path.Type.file));
        final BufferedImage image = new BufferedImage(320, 240, BufferedImage.TYPE_INT_RGB);
        final ByteArrayOutputStream container = new ByteArrayOutputStream();
        ImageIO.write(image, "png", container);
        final byte[] content = container.toByteArray();
        final TransferStatus status = new TransferStatus().setLength(content.length);
        final OutputStream out = new DropboxWriteFeature(session).write(test, status, ConnectionCallback.noop);
        new StreamCopier(new TransferStatus(), new TransferStatus()).transfer(new ByteArrayInputStream(content), out);
        final InputStream in = new DropboxThumbnailFeature(session).thumbnail(test, 150);
        assertNotNull(in);
        final byte[] thumbnail = IOUtils.toByteArray(in);
        in.close();
        assertNotEquals(0, thumbnail.length);
        assertNotNull(ImageIO.read(new ByteArrayInputStream(thumbnail)));
        new DropboxDeleteFeature(session).delete(Collections.singletonList(test), LoginCallback.noop, new Delete.DisabledCallback());
    }
}
