package ch.cyberduck.core.sds;

/*
 * Copyright (c) 2002-2022 iterate GmbH. All rights reserved.
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

import ch.cyberduck.core.AlphanumericRandomStringService;
import ch.cyberduck.core.Credentials;
import ch.cyberduck.core.DisabledConnectionCallback;
import ch.cyberduck.core.DisabledLoginCallback;
import ch.cyberduck.core.Host;
import ch.cyberduck.core.LoginOptions;
import ch.cyberduck.core.Path;
import ch.cyberduck.core.PathAttributes;
import ch.cyberduck.core.exception.NotfoundException;
import ch.cyberduck.core.features.Delete;
import ch.cyberduck.core.io.MD5ChecksumCompute;
import ch.cyberduck.core.io.StatusOutputStream;
import ch.cyberduck.core.io.StreamCopier;
import ch.cyberduck.core.preferences.HostPreferences;
import ch.cyberduck.core.sds.io.swagger.client.model.Node;
import ch.cyberduck.core.sds.triplecrypt.TripleCryptReadFeature;
import ch.cyberduck.core.sds.triplecrypt.TripleCryptWriteFeature;
import ch.cyberduck.core.shared.DefaultAttributesFinderFeature;
import ch.cyberduck.core.shared.DefaultFindFeature;
import ch.cyberduck.core.transfer.TransferStatus;
import ch.cyberduck.core.unicode.NFDNormalizer;
import ch.cyberduck.core.vault.VaultCredentials;
import ch.cyberduck.test.IntegrationTest;

import org.apache.commons.io.IOUtils;
import org.apache.commons.io.input.NullInputStream;
import org.apache.commons.lang3.RandomUtils;
import org.junit.Test;
import org.junit.experimental.categories.Category;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.Collections;
import java.util.EnumSet;

import static org.junit.Assert.*;

@Category(IntegrationTest.class)
public class SDSDirectS3MultipartWriteFeatureTest extends AbstractSDSTest {

    @Test
    public void testWriteZeroLength() throws Exception {
        final SDSNodeIdProvider nodeid = new SDSNodeIdProvider(session);
        final Path room = new SDSDirectoryFeature(session, nodeid).mkdir(
                new Path(new AlphanumericRandomStringService().random(), EnumSet.of(Path.Type.directory, Path.Type.volume)), new TransferStatus());
        final TransferStatus status = new TransferStatus();
        final Path test = new Path(room, new AlphanumericRandomStringService().random(), EnumSet.of(Path.Type.file));
        final SDSDirectS3MultipartWriteFeature writer = new SDSDirectS3MultipartWriteFeature(session, nodeid);
        final StatusOutputStream<Node> out = writer.write(test, status, new DisabledConnectionCallback());
        assertNotNull(out);
        new StreamCopier(status, status).transfer(new NullInputStream(0L), out);
        assertEquals(0L, out.getStatus().getSize(), 0L);
        assertNotNull(test.attributes().getVersionId());
        assertTrue(new DefaultFindFeature(session).find(test));
        new SDSDeleteFeature(session, nodeid).delete(Collections.singletonList(room), new DisabledLoginCallback(), new Delete.DisabledCallback());
    }

    @Test
    public void testWrite() throws Exception {
        final SDSNodeIdProvider nodeid = new SDSNodeIdProvider(session);
        final Path room = new SDSDirectoryFeature(session, nodeid).createRoom(
                new Path(new AlphanumericRandomStringService().random(), EnumSet.of(Path.Type.directory, Path.Type.volume)), false);
        final byte[] content = RandomUtils.nextBytes(32769);
        final Path test = new Path(room, new NFDNormalizer().normalize(String.format("ä%s", new AlphanumericRandomStringService().random())).toString(), EnumSet.of(Path.Type.file));
        {
            final SDSDirectS3MultipartWriteFeature writer = new SDSDirectS3MultipartWriteFeature(session, nodeid);
            final TransferStatus status = new TransferStatus();
            status.setLength(content.length);
            status.setChecksum(new MD5ChecksumCompute().compute(new ByteArrayInputStream(content), new TransferStatus()));
            status.setTimestamp(1632127025217L);
            final StatusOutputStream<Node> out = writer.write(test, status, new DisabledConnectionCallback());
            assertNotNull(out);
            new StreamCopier(status, status).transfer(new ByteArrayInputStream(content), out);
            assertEquals(content.length, out.getStatus().getSize(), 0L);
        }
        assertNotNull(test.attributes().getVersionId());
        assertTrue(new DefaultFindFeature(session).find(test));
        assertTrue(new SDSFindFeature(session, nodeid).find(test));
        final PathAttributes attr = new SDSAttributesFinderFeature(session, nodeid).find(test);
        assertEquals(test.attributes().getVersionId(), attr.getVersionId());
        assertEquals(1632127025217L, attr.getModificationDate());
        assertEquals(1632127025217L, new DefaultAttributesFinderFeature(session).find(test).getModificationDate());
        final byte[] compare = new byte[content.length];
        final InputStream stream = new SDSReadFeature(session, nodeid).read(test, new TransferStatus().withLength(content.length), new DisabledConnectionCallback());
        IOUtils.readFully(stream, compare);
        stream.close();
        assertArrayEquals(content, compare);
        String previousVersion = attr.getVersionId();
        // Overwrite
        {
            final byte[] change = RandomUtils.nextBytes(256);
            final TransferStatus status = new TransferStatus();
            status.setLength(change.length);
            final SDSDirectS3MultipartWriteFeature writer = new SDSDirectS3MultipartWriteFeature(session, nodeid);
            final StatusOutputStream<Node> out = writer.write(test, status.exists(true), new DisabledConnectionCallback());
            assertNotNull(out);
            new StreamCopier(status, status).transfer(new ByteArrayInputStream(change), out);
            assertNotEquals(previousVersion, new SDSAttributesAdapter(session).toAttributes(out.getStatus()).getVersionId());
        }
        // Read with previous version must fail
        try {
            test.attributes().withVersionId(previousVersion);
            new SDSReadFeature(session, nodeid).read(test, new TransferStatus(), new DisabledConnectionCallback());
            fail();
        }
        catch(NotfoundException e) {
            // Expected
        }
        new SDSDeleteFeature(session, nodeid).delete(Collections.singletonList(room), new DisabledLoginCallback(), new Delete.DisabledCallback());
    }

    @Test
    public void testWriteEncrypted() throws Exception {
        final SDSNodeIdProvider nodeid = new SDSNodeIdProvider(session);
        final Path room = new SDSDirectoryFeature(session, nodeid).createRoom(
            new Path(new AlphanumericRandomStringService().random(), EnumSet.of(Path.Type.directory, Path.Type.volume)), true);
        final byte[] content = RandomUtils.nextBytes(new HostPreferences(session.getHost()).getInteger("sds.upload.multipart.chunksize") + 1);
        final Path test = new Path(room, new NFDNormalizer().normalize(String.format("ä%s", new AlphanumericRandomStringService().random())).toString(), EnumSet.of(Path.Type.file));
        {
            final TripleCryptWriteFeature writer = new TripleCryptWriteFeature(session, nodeid, new SDSDirectS3MultipartWriteFeature(session, nodeid));
            final TransferStatus status = new TransferStatus();
            status.setLength(content.length);
            status.setChecksum(new MD5ChecksumCompute().compute(new ByteArrayInputStream(content), new TransferStatus()));
            status.setTimestamp(1632127025217L);
            final StatusOutputStream<Node> out = writer.write(test, status, new DisabledConnectionCallback());
            assertNotNull(out);
            new StreamCopier(status, status).transfer(new ByteArrayInputStream(content), out);
            assertEquals(content.length, out.getStatus().getSize(), 0L);
        }
        assertNotNull(test.attributes().getVersionId());
        assertTrue(new DefaultFindFeature(session).find(test));
        assertTrue(new SDSFindFeature(session, nodeid).find(test));
        final PathAttributes attr = new SDSAttributesFinderFeature(session, nodeid).find(test);
        assertEquals(test.attributes().getVersionId(), attr.getVersionId());
        assertEquals(1632127025217L, attr.getModificationDate());
        assertEquals(1632127025217L, new DefaultAttributesFinderFeature(session).find(test).getModificationDate());
        final byte[] compare = new byte[content.length];
        final InputStream stream = new TripleCryptReadFeature(session, nodeid, new SDSReadFeature(session, nodeid)).read(test, new TransferStatus().withLength(content.length), new DisabledConnectionCallback() {
            @Override
            public Credentials prompt(final Host bookmark, final String title, final String reason, final LoginOptions options) {
                return new VaultCredentials("eth[oh8uv4Eesij");
            }
        });
        IOUtils.readFully(stream, compare);
        stream.close();
        assertArrayEquals(content, compare);
        String previousVersion = attr.getVersionId();
        // Overwrite
        {
            final byte[] change = RandomUtils.nextBytes(256);
            final TransferStatus status = new TransferStatus();
            status.setLength(change.length);
            final TripleCryptWriteFeature writer = new TripleCryptWriteFeature(session, nodeid, new SDSDirectS3MultipartWriteFeature(session, nodeid));
            final StatusOutputStream<Node> out = writer.write(test, status.exists(true), new DisabledConnectionCallback());
            assertNotNull(out);
            new StreamCopier(status, status).transfer(new ByteArrayInputStream(change), out);
            assertNotEquals(previousVersion, new SDSAttributesAdapter(session).toAttributes(out.getStatus()).getVersionId());
        }
        // Read with previous version must fail
        try {
            test.attributes().withVersionId(previousVersion);
            new TripleCryptReadFeature(session, nodeid, new SDSReadFeature(session, nodeid)).read(test, new TransferStatus(), new DisabledConnectionCallback() {
                @Override
                public Credentials prompt(final Host bookmark, final String title, final String reason, final LoginOptions options) {
                    return new VaultCredentials("eth[oh8uv4Eesij");
                }
            });
            fail();
        }
        catch(NotfoundException e) {
            // Expected
        }
        new SDSDeleteFeature(session, nodeid).delete(Collections.singletonList(room), new DisabledLoginCallback(), new Delete.DisabledCallback());
    }

}
