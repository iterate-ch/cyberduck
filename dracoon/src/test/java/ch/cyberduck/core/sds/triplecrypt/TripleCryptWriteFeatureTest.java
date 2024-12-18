package ch.cyberduck.core.sds.triplecrypt;

/*
 * Copyright (c) 2002-2017 iterate GmbH. All rights reserved.
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

import ch.cyberduck.core.AlphanumericRandomStringService;
import ch.cyberduck.core.Credentials;
import ch.cyberduck.core.DisabledConnectionCallback;
import ch.cyberduck.core.DisabledLoginCallback;
import ch.cyberduck.core.Host;
import ch.cyberduck.core.LoginOptions;
import ch.cyberduck.core.Path;
import ch.cyberduck.core.features.Delete;
import ch.cyberduck.core.io.MD5ChecksumCompute;
import ch.cyberduck.core.io.StatusOutputStream;
import ch.cyberduck.core.io.StreamCopier;
import ch.cyberduck.core.sds.AbstractSDSTest;
import ch.cyberduck.core.sds.SDSAttributesFinderFeature;
import ch.cyberduck.core.sds.SDSDeleteFeature;
import ch.cyberduck.core.sds.SDSDirectS3MultipartWriteFeature;
import ch.cyberduck.core.sds.SDSDirectoryFeature;
import ch.cyberduck.core.sds.SDSEncryptionBulkFeature;
import ch.cyberduck.core.sds.SDSNodeIdProvider;
import ch.cyberduck.core.sds.SDSReadFeature;
import ch.cyberduck.core.sds.io.swagger.client.api.NodesApi;
import ch.cyberduck.core.sds.io.swagger.client.model.EncryptRoomRequest;
import ch.cyberduck.core.sds.io.swagger.client.model.Node;
import ch.cyberduck.core.shared.DefaultFindFeature;
import ch.cyberduck.core.transfer.Transfer;
import ch.cyberduck.core.transfer.TransferItem;
import ch.cyberduck.core.transfer.TransferStatus;
import ch.cyberduck.core.vault.VaultCredentials;
import ch.cyberduck.test.IntegrationTest;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.RandomUtils;
import org.apache.commons.lang3.StringUtils;
import org.junit.Test;
import org.junit.experimental.categories.Category;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.Collections;
import java.util.EnumSet;
import java.util.UUID;

import static ch.cyberduck.core.sds.SDSAttributesFinderFeature.KEY_ENCRYPTED;
import static org.junit.Assert.*;

@Category(IntegrationTest.class)
public class TripleCryptWriteFeatureTest extends AbstractSDSTest {

    @Test
    public void testWrite() throws Exception {
        final SDSNodeIdProvider nodeid = new SDSNodeIdProvider(session);
        final Path room = new SDSDirectoryFeature(session, nodeid).mkdir(new Path(
                new AlphanumericRandomStringService().random(), EnumSet.of(Path.Type.directory, Path.Type.volume)), new TransferStatus());
        final EncryptRoomRequest encrypt = new EncryptRoomRequest().isEncrypted(true);
        new NodesApi(session.getClient()).encryptRoom(encrypt, Long.parseLong(new SDSNodeIdProvider(session).getVersionId(room)), StringUtils.EMPTY, null);
        room.attributes().withCustom(KEY_ENCRYPTED, String.valueOf(true));
        final TripleCryptWriteFeature writer = new TripleCryptWriteFeature(session, nodeid, new SDSDirectS3MultipartWriteFeature(session, nodeid));
        final byte[] content = RandomUtils.nextBytes(32769);
        final TransferStatus status = new TransferStatus();
        status.setLength(content.length);
        status.setChecksum(new MD5ChecksumCompute().compute(new ByteArrayInputStream(content), new TransferStatus()));
        final Path test = new Path(room, UUID.randomUUID().toString(), EnumSet.of(Path.Type.file));
        final SDSEncryptionBulkFeature bulk = new SDSEncryptionBulkFeature(session, nodeid);
        bulk.pre(Transfer.Type.upload, Collections.singletonMap(new TransferItem(test), status), new DisabledConnectionCallback());
        final StatusOutputStream<Node> out = writer.write(test, status, new DisabledConnectionCallback());
        assertNotNull(out);
        new StreamCopier(status, status).transfer(new ByteArrayInputStream(content), out);
        assertNotNull(test.attributes().getVersionId());
        assertTrue(new DefaultFindFeature(session).find(test));
        assertEquals(content.length, new SDSAttributesFinderFeature(session, nodeid).find(test).getSize());
        final byte[] compare = new byte[content.length];
        final InputStream stream = new TripleCryptReadFeature(session, nodeid, new SDSReadFeature(session, nodeid)).read(test, new TransferStatus(), new DisabledConnectionCallback() {
            @Override
            public void warn(final Host bookmark, final String title, final String message, final String defaultButton, final String cancelButton, final String preference) {
                //
            }

            @Override
            public Credentials prompt(final Host bookmark, final String title, final String reason, final LoginOptions options) {
                return new VaultCredentials(PROPERTIES.get("vault.passphrase"));
            }
        });
        IOUtils.readFully(stream, compare);
        stream.close();
        assertArrayEquals(content, compare);
        new SDSDeleteFeature(session, nodeid).delete(Collections.singletonList(test), new DisabledLoginCallback(), new Delete.DisabledCallback());
    }


    @Test
    public void testWriteMultipart() throws Exception {
        final SDSNodeIdProvider nodeid = new SDSNodeIdProvider(session);
        final Path room = new SDSDirectoryFeature(session, nodeid).mkdir(new Path(
                new AlphanumericRandomStringService().random(), EnumSet.of(Path.Type.directory, Path.Type.volume)), new TransferStatus());
        final EncryptRoomRequest encrypt = new EncryptRoomRequest().isEncrypted(true);
        new NodesApi(session.getClient()).encryptRoom(encrypt, Long.parseLong(new SDSNodeIdProvider(session).getVersionId(room)), StringUtils.EMPTY, null);
        room.attributes().withCustom(KEY_ENCRYPTED, String.valueOf(true));
        final byte[] content = RandomUtils.nextBytes(32769);
        final TransferStatus status = new TransferStatus();
        status.setLength(content.length);
        final Path test = new Path(room, UUID.randomUUID().toString(), EnumSet.of(Path.Type.file));
        final SDSEncryptionBulkFeature bulk = new SDSEncryptionBulkFeature(session, nodeid);
        bulk.pre(Transfer.Type.upload, Collections.singletonMap(new TransferItem(test), status), new DisabledConnectionCallback());
        final TripleCryptWriteFeature writer = new TripleCryptWriteFeature(session, nodeid, new SDSDirectS3MultipartWriteFeature(session, nodeid));
        final StatusOutputStream<Node> out = writer.write(test, status, new DisabledConnectionCallback());
        assertNotNull(out);
        new StreamCopier(status, status).transfer(new ByteArrayInputStream(content), out);
        assertNotNull(test.attributes().getVersionId());
        assertTrue(new DefaultFindFeature(session).find(test));
        assertEquals(content.length, new SDSAttributesFinderFeature(session, nodeid).find(test).getSize());
        final byte[] compare = new byte[content.length];
        final InputStream stream = new TripleCryptReadFeature(session, nodeid, new SDSReadFeature(session, nodeid)).read(test, new TransferStatus(), new DisabledConnectionCallback() {
            @Override
            public void warn(final Host bookmark, final String title, final String message, final String defaultButton, final String cancelButton, final String preference) {
                //
            }

            @Override
            public Credentials prompt(final Host bookmark, final String title, final String reason, final LoginOptions options) {
                return new VaultCredentials(PROPERTIES.get("vault.passphrase"));
            }
        });
        IOUtils.readFully(stream, compare);
        stream.close();
        assertArrayEquals(content, compare);
        new SDSDeleteFeature(session, nodeid).delete(Collections.singletonList(test), new DisabledLoginCallback(), new Delete.DisabledCallback());
    }
}
