package ch.cyberduck.core.sds;

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

import ch.cyberduck.core.*;
import ch.cyberduck.core.exception.ConnectionCanceledException;
import ch.cyberduck.core.exception.LoginCanceledException;
import ch.cyberduck.core.exception.NotfoundException;
import ch.cyberduck.core.features.Copy;
import ch.cyberduck.core.features.Delete;
import ch.cyberduck.core.io.StatusOutputStream;
import ch.cyberduck.core.io.StreamCopier;
import ch.cyberduck.core.sds.io.swagger.client.api.NodesApi;
import ch.cyberduck.core.sds.io.swagger.client.model.EncryptRoomRequest;
import ch.cyberduck.core.sds.triplecrypt.CryptoReadFeature;
import ch.cyberduck.core.sds.triplecrypt.CryptoWriteFeature;
import ch.cyberduck.core.transfer.Transfer;
import ch.cyberduck.core.transfer.TransferStatus;
import ch.cyberduck.core.vault.VaultCredentials;
import ch.cyberduck.core.worker.MoveWorker;
import ch.cyberduck.test.IntegrationTest;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.RandomUtils;
import org.apache.commons.lang3.StringUtils;
import org.junit.Test;
import org.junit.experimental.categories.Category;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.Arrays;
import java.util.Collections;
import java.util.EnumSet;

import static org.junit.Assert.*;

@Category(IntegrationTest.class)
public class SDSDelegatingMoveFeatureTest extends AbstractSDSTest {

    @Test
    public void testMove() throws Exception {
        final SDSNodeIdProvider nodeid = new SDSNodeIdProvider(session).withCache(cache);
        final Path room = new SDSDirectoryFeature(session, nodeid).mkdir(new Path(
            new AlphanumericRandomStringService().random(), EnumSet.of(Path.Type.directory, Path.Type.volume)), null, new TransferStatus());
        final Path test = new SDSTouchFeature(session, nodeid).touch(new Path(room, new AlphanumericRandomStringService().random(), EnumSet.of(Path.Type.file)), new TransferStatus());
        final Path target = new Path(room, new AlphanumericRandomStringService().random(), EnumSet.of(Path.Type.file));
        assertEquals(new SDSAttributesFinderFeature(session, nodeid).find(test),
            new SDSDelegatingMoveFeature(session, nodeid, new SDSMoveFeature(session, nodeid)).move(test, target, new TransferStatus(), new Delete.DisabledCallback(), new DisabledConnectionCallback()).attributes());
        assertFalse(new SDSFindFeature(nodeid).find(test));
        assertTrue(new SDSFindFeature(nodeid).find(target));
        assertEquals(0, session.getMetrics().get(Copy.class));
        new SDSDeleteFeature(session, nodeid).delete(Collections.singletonList(room), new DisabledLoginCallback(), new Delete.DisabledCallback());
    }

    @Test
    public void testMoveDifferentDataRoom() throws Exception {
        final SDSNodeIdProvider nodeid = new SDSNodeIdProvider(session).withCache(cache);
        final Path room1 = new SDSDirectoryFeature(session, nodeid).mkdir(new Path(
            new AlphanumericRandomStringService().random(), EnumSet.of(Path.Type.directory, Path.Type.volume)), null, new TransferStatus());
        final Path room2 = new SDSDirectoryFeature(session, nodeid).mkdir(new Path(
            new AlphanumericRandomStringService().random(), EnumSet.of(Path.Type.directory, Path.Type.volume)), null, new TransferStatus());
        final Path test = new Path(room1, new AlphanumericRandomStringService().random(), EnumSet.of(Path.Type.file));
        new SDSTouchFeature(session, nodeid).touch(test, new TransferStatus());
        final Path target = new Path(room2, new AlphanumericRandomStringService().random(), EnumSet.of(Path.Type.file));
        new SDSDelegatingMoveFeature(session, nodeid, new SDSMoveFeature(session, nodeid)).move(test, target, new TransferStatus(), new Delete.DisabledCallback(), new DisabledConnectionCallback());
        assertFalse(new SDSFindFeature(nodeid).find(test));
        assertTrue(new SDSFindFeature(nodeid).find(target));
        assertEquals(0, session.getMetrics().get(Copy.class));
        new SDSDeleteFeature(session, nodeid).delete(Arrays.asList(room1, room2), new DisabledLoginCallback(), new Delete.DisabledCallback());
    }

    @Test
    public void testMoveWithRenameDifferentDataRoomSameFilename() throws Exception {
        final SDSNodeIdProvider nodeid = new SDSNodeIdProvider(session).withCache(cache);
        final Path room1 = new SDSDirectoryFeature(session, nodeid).mkdir(new Path(
            new AlphanumericRandomStringService().random(), EnumSet.of(Path.Type.directory, Path.Type.volume)), null, new TransferStatus());
        final Path room2 = new SDSDirectoryFeature(session, nodeid).mkdir(new Path(
            new AlphanumericRandomStringService().random(), EnumSet.of(Path.Type.directory, Path.Type.volume)), null, new TransferStatus());
        final Path test1 = new Path(room1, "A", EnumSet.of(Path.Type.file));
        new SDSTouchFeature(session, nodeid).touch(test1, new TransferStatus());
        final Path test2 = new Path(room2, "A", EnumSet.of(Path.Type.file));
        new SDSTouchFeature(session, nodeid).touch(test2, new TransferStatus());
        final Path target = new Path(room2, "A (2)", EnumSet.of(Path.Type.file));
        new SDSDelegatingMoveFeature(session, nodeid, new SDSMoveFeature(session, nodeid)).move(test1, target, new TransferStatus(), new Delete.DisabledCallback(), new DisabledConnectionCallback());
        assertFalse(new SDSFindFeature(nodeid).find(test1));
        assertTrue(new SDSFindFeature(nodeid).find(test2));
        assertTrue(new SDSFindFeature(nodeid).find(target));
        assertEquals(0, session.getMetrics().get(Copy.class));
        new SDSDeleteFeature(session, nodeid).delete(Arrays.asList(room1, room2), new DisabledLoginCallback(), new Delete.DisabledCallback());
    }

    @Test
    public void testMoveFromEncryptedDataRoom() throws Exception {
        final Path room1 = new Path("CD-TEST-ENCRYPTED", EnumSet.of(Path.Type.directory, Path.Type.volume, Path.Type.vault));
        room1.attributes().getAcl().addAll(new Acl.EmailUser(System.getProperties().getProperty("sds.user")), SDSPermissionsFeature.DELETE_ROLE);
        final SDSNodeIdProvider nodeid = new SDSNodeIdProvider(session).withCache(cache);
        final Path room2 = new SDSDirectoryFeature(session, nodeid).mkdir(new Path(
            new AlphanumericRandomStringService().random(), EnumSet.of(Path.Type.directory, Path.Type.volume)), null, new TransferStatus());
        final byte[] content = RandomUtils.nextBytes(32769);
        final TransferStatus status = new TransferStatus();
        status.setLength(content.length);
        final Path test = new Path(room1, new AlphanumericRandomStringService().random(), EnumSet.of(Path.Type.file, Path.Type.decrypted));
        final SDSEncryptionBulkFeature bulk = new SDSEncryptionBulkFeature(session, nodeid);
        bulk.pre(Transfer.Type.upload, Collections.singletonMap(test, status), new DisabledConnectionCallback());
        final CryptoWriteFeature writer = new CryptoWriteFeature(session, new SDSWriteFeature(session, nodeid));
        final StatusOutputStream<VersionId> out = writer.write(test, status, new DisabledConnectionCallback());
        assertNotNull(out);
        new StreamCopier(status, status).transfer(new ByteArrayInputStream(content), out);
        final Path target = new Path(room2, new AlphanumericRandomStringService().random(), EnumSet.of(Path.Type.file));
        new SDSDelegatingMoveFeature(session, nodeid, new SDSMoveFeature(session, nodeid)).move(test, target, new TransferStatus().length(content.length), new Delete.DisabledCallback(), new ConnectionCallback() {
            @Override
            public void warn(final Host bookmark, final String title, final String message, final String defaultButton, final String cancelButton, final String preference) throws ConnectionCanceledException {
                //
            }

            @Override
            public Credentials prompt(final Host bookmark, final String title, final String reason, final LoginOptions options) throws LoginCanceledException {
                return new VaultCredentials("ahbic3Ae");
            }
        });
        assertEquals(1, session.getMetrics().get(Copy.class));
        assertFalse(new SDSFindFeature(nodeid).find(test));
        assertTrue(new SDSFindFeature(nodeid).find(target));
        final byte[] compare = new byte[content.length];
        final InputStream stream = new SDSReadFeature(session, nodeid).read(target, new TransferStatus().length(content.length), new ConnectionCallback() {
            @Override
            public void warn(final Host bookmark, final String title, final String message, final String defaultButton, final String cancelButton, final String preference) throws ConnectionCanceledException {
                //
            }

            @Override
            public Credentials prompt(final Host bookmark, final String title, final String reason, final LoginOptions options) throws LoginCanceledException {
                return new VaultCredentials("ahbic3Ae");
            }
        });
        IOUtils.readFully(stream, compare);
        stream.close();
        assertArrayEquals(content, compare);
        new SDSDeleteFeature(session, nodeid).delete(Collections.singletonList(room2), new DisabledLoginCallback(), new Delete.DisabledCallback());
    }

    @Test
    public void testMoveToEncryptedDataRoom() throws Exception {
        final Path room1 = new Path("CD-TEST-ENCRYPTED", EnumSet.of(Path.Type.directory, Path.Type.volume, Path.Type.vault));
        room1.attributes().getAcl().addAll(new Acl.EmailUser(System.getProperties().getProperty("sds.user")), SDSPermissionsFeature.DELETE_ROLE);
        final SDSNodeIdProvider nodeid = new SDSNodeIdProvider(session).withCache(cache);
        final Path room2 = new SDSDirectoryFeature(session, nodeid).mkdir(new Path(
            new AlphanumericRandomStringService().random(), EnumSet.of(Path.Type.directory, Path.Type.volume)), null, new TransferStatus());
        final byte[] content = RandomUtils.nextBytes(32769);
        final TransferStatus status = new TransferStatus();
        status.setLength(content.length);
        final Path test = new Path(room2, new AlphanumericRandomStringService().random(), EnumSet.of(Path.Type.file, Path.Type.decrypted));
        final SDSWriteFeature writer = new SDSWriteFeature(session, nodeid);
        final StatusOutputStream<VersionId> out = writer.write(test, status, new DisabledConnectionCallback());
        assertNotNull(out);
        new StreamCopier(status, status).transfer(new ByteArrayInputStream(content), out);
        final Path target = new Path(room1, new AlphanumericRandomStringService().random(), EnumSet.of(Path.Type.file));
        new SDSDelegatingMoveFeature(session, nodeid, new SDSMoveFeature(session, nodeid)).move(test, target, new TransferStatus().length(content.length), new Delete.DisabledCallback(), new DisabledConnectionCallback());
        assertEquals(1, session.getMetrics().get(Copy.class));
        assertFalse(new SDSFindFeature(nodeid).find(test));
        assertTrue(new SDSFindFeature(nodeid).find(target));
        final byte[] compare = new byte[content.length];
        final InputStream stream = new CryptoReadFeature(session, nodeid, new SDSReadFeature(session, nodeid)).read(target, new TransferStatus().length(content.length), new ConnectionCallback() {
            @Override
            public void warn(final Host bookmark, final String title, final String message, final String defaultButton, final String cancelButton, final String preference) throws ConnectionCanceledException {
                //
            }

            @Override
            public Credentials prompt(final Host bookmark, final String title, final String reason, final LoginOptions options) throws LoginCanceledException {
                return new VaultCredentials("ahbic3Ae");
            }
        });
        IOUtils.readFully(stream, compare);
        stream.close();
        assertArrayEquals(content, compare);
        new SDSDeleteFeature(session, nodeid).delete(Collections.singletonList(room2), new DisabledLoginCallback(), new Delete.DisabledCallback());
    }

    @Test
    public void testMoveBetweenEncryptedDataRooms() throws Exception {
        final Path room1 = new Path("CD-TEST-ENCRYPTED", EnumSet.of(Path.Type.directory, Path.Type.volume, Path.Type.vault));
        room1.attributes().getAcl().addAll(new Acl.EmailUser(System.getProperties().getProperty("sds.user")), SDSPermissionsFeature.DELETE_ROLE);
        final Path room2 = new Path("CD-TEST-ENCRYPTED-TOO", EnumSet.of(Path.Type.directory, Path.Type.volume, Path.Type.vault));
        final byte[] content = RandomUtils.nextBytes(32769);
        final TransferStatus status = new TransferStatus();
        status.setLength(content.length);
        final Path test = new Path(room1, new AlphanumericRandomStringService().random(), EnumSet.of(Path.Type.file, Path.Type.decrypted));
        final SDSNodeIdProvider nodeid = new SDSNodeIdProvider(session).withCache(cache);
        final SDSEncryptionBulkFeature bulk = new SDSEncryptionBulkFeature(session, nodeid);
        bulk.pre(Transfer.Type.upload, Collections.singletonMap(test, status), new DisabledConnectionCallback());
        final CryptoWriteFeature writer = new CryptoWriteFeature(session, new SDSWriteFeature(session, nodeid));
        final StatusOutputStream<VersionId> out = writer.write(test, status, new DisabledConnectionCallback());
        assertNotNull(out);
        new StreamCopier(status, status).transfer(new ByteArrayInputStream(content), out);
        final Path target = new Path(room2, new AlphanumericRandomStringService().random(), EnumSet.of(Path.Type.file));
        new SDSDelegatingMoveFeature(session, nodeid, new SDSMoveFeature(session, nodeid)).move(test, target, new TransferStatus().length(content.length), new Delete.DisabledCallback(), new DisabledConnectionCallback());
        assertEquals(0, session.getMetrics().get(Copy.class));
        assertFalse(new SDSFindFeature(nodeid).find(test));
        assertTrue(new SDSFindFeature(nodeid).find(target));
        final byte[] compare = new byte[content.length];
        final InputStream stream = new CryptoReadFeature(session, nodeid, new SDSReadFeature(session, nodeid)).read(target, new TransferStatus().length(content.length), new ConnectionCallback() {
            @Override
            public void warn(final Host bookmark, final String title, final String message, final String defaultButton, final String cancelButton, final String preference) throws ConnectionCanceledException {
                //
            }

            @Override
            public Credentials prompt(final Host bookmark, final String title, final String reason, final LoginOptions options) throws LoginCanceledException {
                return new VaultCredentials("ahbic3Ae");
            }
        });
        IOUtils.readFully(stream, compare);
        stream.close();
        assertArrayEquals(content, compare);
        new SDSDeleteFeature(session, nodeid).delete(Collections.singletonList(target), new DisabledLoginCallback(), new Delete.DisabledCallback());
    }

    @Test
    public void testMoveDirectory() throws Exception {
        final SDSNodeIdProvider nodeid = new SDSNodeIdProvider(session).withCache(cache);
        final Path room = new SDSDirectoryFeature(session, nodeid).mkdir(new Path(
            new AlphanumericRandomStringService().random(), EnumSet.of(Path.Type.directory, Path.Type.volume)), null, new TransferStatus());
        final Path test = new Path(room, new AlphanumericRandomStringService().random(), EnumSet.of(Path.Type.directory));
        new SDSDirectoryFeature(session, nodeid).mkdir(test, null, new TransferStatus());
        final Path target = new Path(room, new AlphanumericRandomStringService().random(), EnumSet.of(Path.Type.directory));
        new SDSMoveFeature(session, nodeid).move(test, target, new TransferStatus(), new Delete.DisabledCallback(), new DisabledConnectionCallback());
        assertEquals(0, session.getMetrics().get(Copy.class));
        assertFalse(new SDSFindFeature(nodeid).find(test));
        assertTrue(new SDSFindFeature(nodeid).find(target));
        new SDSDeleteFeature(session, nodeid).delete(Collections.singletonList(room), new DisabledLoginCallback(), new Delete.DisabledCallback());
    }

    @Test
    public void testMoveDataRoom() throws Exception {
        final Path test = new Path(new AlphanumericRandomStringService().random(), EnumSet.of(Path.Type.directory, Path.Type.volume));
        final SDSNodeIdProvider nodeid = new SDSNodeIdProvider(session).withCache(cache);
        new SDSDirectoryFeature(session, nodeid).mkdir(test, null, new TransferStatus());
        final Path target = new Path(new AlphanumericRandomStringService().random(), EnumSet.of(Path.Type.directory, Path.Type.volume));
        new SDSMoveFeature(session, nodeid).move(test, target, new TransferStatus(), new Delete.DisabledCallback(), new DisabledConnectionCallback());
        assertEquals(0, session.getMetrics().get(Copy.class));
        assertFalse(new SDSFindFeature(nodeid).find(test));
        assertTrue(new SDSFindFeature(nodeid).find(target));
        new SDSDeleteFeature(session, nodeid).delete(Collections.singletonList(target), new DisabledLoginCallback(), new Delete.DisabledCallback());
    }

    @Test
    public void testMoveEncryptedDataRoom() throws Exception {
        final Path room = new Path(new AlphanumericRandomStringService().random(), EnumSet.of(Path.Type.directory, Path.Type.volume));
        final SDSNodeIdProvider nodeid = new SDSNodeIdProvider(session).withCache(cache);
        new SDSDirectoryFeature(session, nodeid).mkdir(room, null, new TransferStatus());
        final EncryptRoomRequest encrypt = new EncryptRoomRequest();
        encrypt.setIsEncrypted(true);
        new NodesApi(session.getClient()).encryptRoom(Long.parseLong(new SDSNodeIdProvider(session).withCache(cache).getFileid(room,
            new DisabledListProgressListener())), encrypt, StringUtils.EMPTY, null);
        final AttributedList<Path> list = new SDSListService(session, nodeid).list(room.getParent(), new DisabledListProgressListener());
        final Path encrypted = list.get(room);
        // create file inside encrypted room
        final Path folder = new Path(encrypted, new AlphanumericRandomStringService().random(), EnumSet.of(Path.Type.directory, Path.Type.directory));
        new SDSDirectoryFeature(session, nodeid).mkdir(folder, null, new TransferStatus());
        final Path renamed = new Path(new AlphanumericRandomStringService().random(), EnumSet.of(Path.Type.directory, Path.Type.volume, Path.Type.vault));
        final MoveWorker worker = new MoveWorker(Collections.singletonMap(encrypted, renamed), PathCache.empty(), PasswordStoreFactory.get(), new DisabledLoginCallback(), new DisabledHostKeyCallback(), new DisabledProgressListener(), new DisabledTranscriptListener());
        worker.run(session);
        assertEquals(0, session.getMetrics().get(Copy.class));
        assertFalse(new SDSFindFeature(nodeid).find(room));
        assertTrue(new SDSFindFeature(nodeid).find(renamed));
        assertTrue(new SDSFindFeature(nodeid).find(new Path(renamed, folder.getName(), EnumSet.of(Path.Type.directory, Path.Type.directory))));
        assertTrue(renamed.getType().contains(Path.Type.vault));
        new SDSDeleteFeature(session, nodeid).delete(Collections.singletonList(renamed), new DisabledLoginCallback(), new Delete.DisabledCallback());
    }

    @Test
    public void testMoveOverride() throws Exception {
        final SDSNodeIdProvider nodeid = new SDSNodeIdProvider(session).withCache(cache);
        final Path room = new SDSDirectoryFeature(session, nodeid).mkdir(new Path(
            new AlphanumericRandomStringService().random(), EnumSet.of(Path.Type.directory, Path.Type.volume)), null, new TransferStatus());
        final Path test = new Path(room, new AlphanumericRandomStringService().random(), EnumSet.of(Path.Type.file));
        new SDSTouchFeature(session, nodeid).touch(test, new TransferStatus());
        final Path target = new Path(room, new AlphanumericRandomStringService().random(), EnumSet.of(Path.Type.file));
        new SDSTouchFeature(session, nodeid).touch(target, new TransferStatus());
        new SDSMoveFeature(session, nodeid).move(test, target, new TransferStatus().exists(true), new Delete.DisabledCallback(), new DisabledConnectionCallback());
        assertFalse(new SDSFindFeature(nodeid).find(test));
        assertTrue(new SDSFindFeature(nodeid).find(target));
        new SDSDeleteFeature(session, nodeid).delete(Collections.singletonList(room), new DisabledLoginCallback(), new Delete.DisabledCallback());
    }

    @Test
    public void testMoveToDifferentParentAndRename() throws Exception {
        final SDSNodeIdProvider nodeid = new SDSNodeIdProvider(session).withCache(cache);
        final Path room = new SDSDirectoryFeature(session, nodeid).mkdir(new Path(
            new AlphanumericRandomStringService().random(), EnumSet.of(Path.Type.directory, Path.Type.volume)), null, new TransferStatus());
        final Path test = new Path(room, new AlphanumericRandomStringService().random(), EnumSet.of(Path.Type.file));
        new SDSTouchFeature(session, nodeid).touch(test, new TransferStatus());
        final Path target = new Path(room, new AlphanumericRandomStringService().random(), EnumSet.of(Path.Type.file));
        new SDSTouchFeature(session, nodeid).touch(target, new TransferStatus());
        new SDSMoveFeature(session, nodeid).move(test, target, new TransferStatus().exists(true), new Delete.DisabledCallback(), new DisabledConnectionCallback());
        assertFalse(new SDSFindFeature(nodeid).find(test));
        assertTrue(new SDSFindFeature(nodeid).find(target));
        new SDSDeleteFeature(session, nodeid).delete(Collections.singletonList(room), new DisabledLoginCallback(), new Delete.DisabledCallback());
    }

    @Test(expected = NotfoundException.class)
    public void testMoveNotFound() throws Exception {
        final Path room = new Path(
            new AlphanumericRandomStringService().random(), EnumSet.of(Path.Type.directory, Path.Type.volume));
        final Path test = new Path(room, new AlphanumericRandomStringService().random(), EnumSet.of(Path.Type.file));
        final SDSNodeIdProvider nodeid = new SDSNodeIdProvider(session).withCache(cache);
        new SDSMoveFeature(session, nodeid).move(test, new Path(room, new AlphanumericRandomStringService().random(), EnumSet.of(Path.Type.file)), new TransferStatus(), new Delete.DisabledCallback(), new DisabledConnectionCallback());
    }
}
