package ch.cyberduck.core.owncloud;

/*
 * Copyright (c) 2002-2024 iterate GmbH. All rights reserved.
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

import ch.cyberduck.core.AbstractPath;
import ch.cyberduck.core.AlphanumericRandomStringService;
import ch.cyberduck.core.BytecountStreamListener;
import ch.cyberduck.core.DisabledConnectionCallback;
import ch.cyberduck.core.DisabledLoginCallback;
import ch.cyberduck.core.Local;
import ch.cyberduck.core.Path;
import ch.cyberduck.core.PathAttributes;
import ch.cyberduck.core.dav.DAVAttributesFinderFeature;
import ch.cyberduck.core.dav.DAVDeleteFeature;
import ch.cyberduck.core.dav.DAVDirectoryFeature;
import ch.cyberduck.core.dav.DAVFindFeature;
import ch.cyberduck.core.dav.DAVReadFeature;
import ch.cyberduck.core.features.Delete;
import ch.cyberduck.core.io.BandwidthThrottle;
import ch.cyberduck.core.io.HashAlgorithm;
import ch.cyberduck.core.io.SHA1ChecksumCompute;
import ch.cyberduck.core.transfer.TransferStatus;
import ch.cyberduck.core.tus.TusCapabilities;
import ch.cyberduck.core.tus.TusWriteFeature;
import ch.cyberduck.test.IntegrationTest;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.RandomUtils;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.experimental.categories.Category;

import java.util.Collections;
import java.util.EnumSet;

import static org.junit.Assert.*;

@Category(IntegrationTest.class)
@Ignore
public class OcisUploadFeatureTest extends AbstractOcisTest {

    @Test
    public void testUploadLargeFileInChunks() throws Exception {
        final TusCapabilities capabilities = new TusCapabilities().withHashAlgorithm(HashAlgorithm.sha1)
                .withExtension(TusCapabilities.Extension.checksum)
                .withExtension(TusCapabilities.Extension.creation);
        final OcisUploadFeature feature = new OcisUploadFeature(session,
                new TusWriteFeature(capabilities, session.getClient().getClient()), capabilities);
        final Path directory = new DAVDirectoryFeature(session).mkdir(new Path(new OwncloudHomeFeature(session.getHost()).find(),
                new AlphanumericRandomStringService().random(), EnumSet.of(AbstractPath.Type.directory)), new TransferStatus());
        final String name = new AlphanumericRandomStringService().random();
        final Path file = new Path(directory, name, EnumSet.of(Path.Type.file));
        final Local local = new Local(System.getProperty("java.io.tmpdir"), name);
        {
            final byte[] content = RandomUtils.nextBytes(11 * 1024 * 1024);
            IOUtils.write(content, local.getOutputStream(false));
            final TransferStatus status = new TransferStatus();
            status.setModified(1712047338787L);
            status.setChecksum(new SHA1ChecksumCompute().compute(local.getInputStream(), new TransferStatus()));
            status.setLength(content.length);
            final BytecountStreamListener count = new BytecountStreamListener();
            assertFalse(feature.append(file, status).append);
            final Void response = feature.upload(file, local, new BandwidthThrottle(BandwidthThrottle.UNLIMITED), count, status, new DisabledConnectionCallback());
            assertTrue(status.isComplete());
            assertEquals(content.length, count.getSent());
            assertTrue(status.isComplete());
            assertEquals(PathAttributes.EMPTY, status.getResponse());
            assertTrue(new DAVFindFeature(session).find(file));
            assertEquals(content.length, new DAVAttributesFinderFeature(session).find(file).getSize());
            final byte[] compare = new byte[content.length];
            IOUtils.readFully(new DAVReadFeature(session).read(file, new TransferStatus().withLength(content.length), new DisabledConnectionCallback()), compare);
            assertArrayEquals(content, compare);
        }
        {
            // Replace
            final byte[] content = RandomUtils.nextBytes(10 * 1024 * 1024);
            IOUtils.write(content, local.getOutputStream(false));
            final TransferStatus status = new TransferStatus();
            status.setExists(true);
            status.setModified(1714114714290L);
            status.setChecksum(new SHA1ChecksumCompute().compute(local.getInputStream(), new TransferStatus()));
            status.setLength(content.length);
            final BytecountStreamListener count = new BytecountStreamListener();
            assertFalse(feature.append(file, status).append);
            final Void response = feature.upload(file, local, new BandwidthThrottle(BandwidthThrottle.UNLIMITED), count, status, new DisabledConnectionCallback());
            assertTrue(status.isComplete());
            assertEquals(content.length, count.getSent());
            assertTrue(status.isComplete());
            assertEquals(PathAttributes.EMPTY, status.getResponse());
            assertTrue(new DAVFindFeature(session).find(file));
            assertEquals(content.length, new DAVAttributesFinderFeature(session).find(file).getSize());
            final byte[] compare = new byte[content.length];
            IOUtils.readFully(new DAVReadFeature(session).read(file, new TransferStatus().withLength(content.length), new DisabledConnectionCallback()), compare);
            assertArrayEquals(content, compare);
        }
        new DAVDeleteFeature(session).delete(Collections.singletonList(directory), new DisabledLoginCallback(), new Delete.DisabledCallback());
        local.delete();
    }
}
