package ch.cyberduck.core.dropbox;

/*
 * Copyright (c) 2002-2021 iterate GmbH. All rights reserved.
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
import ch.cyberduck.core.BytecountStreamListener;
import ch.cyberduck.core.DisabledLoginCallback;
import ch.cyberduck.core.Local;
import ch.cyberduck.core.Path;
import ch.cyberduck.core.PathAttributes;
import ch.cyberduck.core.features.Delete;
import ch.cyberduck.core.io.BandwidthThrottle;
import ch.cyberduck.core.transfer.TransferStatus;
import ch.cyberduck.test.IntegrationTest;

import org.apache.commons.io.IOUtils;
import org.apache.commons.io.input.NullInputStream;
import org.apache.commons.lang3.RandomUtils;
import org.junit.Test;
import org.junit.experimental.categories.Category;

import java.util.Collections;
import java.util.EnumSet;

import com.dropbox.core.v2.files.FileMetadata;
import com.dropbox.core.v2.files.Metadata;

import static org.junit.Assert.*;

@Category(IntegrationTest.class)
public class DropboxUploadFeatureTest extends AbstractDropboxTest {

    @Test
    public void testUploadSmall() throws Exception {
        final DropboxUploadFeature feature = new DropboxUploadFeature(session, new DropboxWriteFeature(session));
        final Path root = new Path("/", EnumSet.of(Path.Type.directory, Path.Type.volume));
        final String name = new AlphanumericRandomStringService().random();
        final Path test = new Path(root, name, EnumSet.of(Path.Type.file));
        final Local local = new Local(System.getProperty("java.io.tmpdir"), name);
        final int length = 56;
        final byte[] content = RandomUtils.nextBytes(length);
        IOUtils.write(content, local.getOutputStream(false));
        final TransferStatus status = new TransferStatus();
        status.setModified(1700638960509L);
        status.setLength(content.length);
        status.setMime("text/plain");
        final BytecountStreamListener count = new BytecountStreamListener();
        final Metadata metadata = feature.upload(test, local, new BandwidthThrottle(BandwidthThrottle.UNLIMITED),
                count, status, new DisabledLoginCallback());
        assertEquals(content.length, count.getSent());
        assertTrue(status.isComplete());
        assertTrue(new DropboxFindFeature(session).find(test));
        final PathAttributes attributes = new DropboxAttributesFinderFeature(session).find(test);
        assertEquals(1700638960000L, attributes.getModificationDate());
        assertEquals(content.length, attributes.getSize());
        assertEquals(((FileMetadata) metadata).getContentHash(), attributes.getChecksum().hash);
        new DropboxDeleteFeature(session).delete(Collections.singletonList(test), new DisabledLoginCallback(), new Delete.DisabledCallback());
        local.delete();
    }

    @Test
    public void testDecorate() throws Exception {
        final NullInputStream n = new NullInputStream(1L);
        assertSame(NullInputStream.class, new DropboxUploadFeature(session, new DropboxWriteFeature(session)).decorate(n, null).getClass());
    }
}