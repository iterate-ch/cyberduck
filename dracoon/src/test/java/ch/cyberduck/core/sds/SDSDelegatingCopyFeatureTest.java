package ch.cyberduck.core.sds;

/*
 * Copyright (c) 2002-2017 iterate GmbH. All rights reserved.
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

import ch.cyberduck.core.Acl;
import ch.cyberduck.core.AlphanumericRandomStringService;
import ch.cyberduck.core.AttributedList;
import ch.cyberduck.core.Credentials;
import ch.cyberduck.core.DisabledConnectionCallback;
import ch.cyberduck.core.DisabledListProgressListener;
import ch.cyberduck.core.DisabledLoginCallback;
import ch.cyberduck.core.Host;
import ch.cyberduck.core.LoginOptions;
import ch.cyberduck.core.Path;
import ch.cyberduck.core.exception.UnsupportedException;
import ch.cyberduck.core.features.Delete;
import ch.cyberduck.core.features.Find;
import ch.cyberduck.core.io.DisabledStreamListener;
import ch.cyberduck.core.io.StatusOutputStream;
import ch.cyberduck.core.io.StreamCopier;
import ch.cyberduck.core.sds.io.swagger.client.api.NodesApi;
import ch.cyberduck.core.sds.io.swagger.client.model.EncryptRoomRequest;
import ch.cyberduck.core.sds.io.swagger.client.model.Node;
import ch.cyberduck.core.sds.triplecrypt.TripleCryptReadFeature;
import ch.cyberduck.core.sds.triplecrypt.TripleCryptWriteFeature;
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
import java.util.Arrays;
import java.util.Collections;
import java.util.EnumSet;

import static ch.cyberduck.core.sds.SDSAttributesFinderFeature.KEY_ENCRYPTED;
import static org.junit.Assert.*;

@Category(IntegrationTest.class)
public class SDSDelegatingCopyFeatureTest extends AbstractSDSTest {

    @Test
    public void testCopyFileServerSide() throws Exception {
        final SDSNodeIdProvider nodeid = new SDSNodeIdProvider(session);
        final Path room = new SDSDirectoryFeature(session, nodeid).mkdir(
            new Path(new AlphanumericRandomStringService().random(), EnumSet.of(Path.Type.directory, Path.Type.volume)), new TransferStatus());
        final Path test = new SDSTouchFeature(session, nodeid).touch(new Path(room, new AlphanumericRandomStringService().random(), EnumSet.of(Path.Type.file)), new TransferStatus());
        final Path copy = new Path(new SDSDirectoryFeature(session, nodeid).mkdir(new Path(room, new AlphanumericRandomStringService().random(), EnumSet.of(Path.Type.directory)), new TransferStatus()), test.getName(), EnumSet.of(Path.Type.file));
        new SDSTouchFeature(session, nodeid).touch(copy, new TransferStatus());
        final SDSCopyFeature proxy = new SDSCopyFeature(session, nodeid);
        assertThrows(UnsupportedException.class, () -> proxy.preflight(room, test));
        try {
            proxy.preflight(room, test);
        }
        catch(UnsupportedException e) {
            assertEquals("Unsupported", e.getMessage());
            assertEquals(String.format("Cannot copy %s.", room.getName()), e.getDetail(false));
        }
        final SDSDelegatingCopyFeature feature = new SDSDelegatingCopyFeature(session, nodeid, proxy);
        assertTrue(feature.isSupported(test, copy));
        final Path target = feature.copy(test, copy, new TransferStatus(), new DisabledConnectionCallback(), new DisabledStreamListener());
        assertNotEquals(test.attributes().getVersionId(), target.attributes().getVersionId());
        assertEquals(target.attributes().getVersionId(), new SDSAttributesFinderFeature(session, nodeid).find(target).getVersionId());
        assertTrue(new SDSFindFeature(session, nodeid).find(test));
        assertTrue(new SDSFindFeature(session, nodeid).find(copy));
        new SDSDeleteFeature(session, nodeid).delete(Collections.singletonList(room), new DisabledLoginCallback(), new Delete.DisabledCallback());
    }

    @Test
    public void testCopyFileWithRename() throws Exception {
        final SDSNodeIdProvider nodeid = new SDSNodeIdProvider(session);
        final Path room = new SDSDirectoryFeature(session, nodeid).mkdir(
            new Path(new AlphanumericRandomStringService().random(), EnumSet.of(Path.Type.directory, Path.Type.volume)), new TransferStatus());
        final Path test = new SDSTouchFeature(session, nodeid).touch(new Path(room, new AlphanumericRandomStringService().random(), EnumSet.of(Path.Type.file)), new TransferStatus());
        final Path copy = new Path(new SDSDirectoryFeature(session, nodeid).mkdir(new Path(room, new AlphanumericRandomStringService().random(), EnumSet.of(Path.Type.directory)), new TransferStatus()), new AlphanumericRandomStringService().random(), EnumSet.of(Path.Type.file));
        final SDSCopyFeature proxy = new SDSCopyFeature(session, nodeid);
        final SDSDelegatingCopyFeature feature = new SDSDelegatingCopyFeature(session, nodeid, proxy);
        assertFalse(proxy.isSupported(test, copy));
        assertTrue(feature.isSupported(test, copy));
        final Path target = feature.copy(test, copy, new TransferStatus(), new DisabledConnectionCallback(), new DisabledStreamListener());
        assertNotEquals(test.attributes().getVersionId(), target.attributes().getVersionId());
        assertEquals(target.attributes().getVersionId(), new SDSAttributesFinderFeature(session, nodeid).find(target).getVersionId());
        assertTrue(new SDSFindFeature(session, nodeid).find(test));
        assertTrue(new SDSFindFeature(session, nodeid).find(copy));
        new SDSDeleteFeature(session, nodeid).delete(Collections.singletonList(room), new DisabledLoginCallback(), new Delete.DisabledCallback());
    }

    @Test
    public void testCopyServerSideToExistingFile() throws Exception {
        final SDSNodeIdProvider nodeid = new SDSNodeIdProvider(session);
        final Path room = new SDSDirectoryFeature(session, nodeid).mkdir(
            new Path(new AlphanumericRandomStringService().random(), EnumSet.of(Path.Type.directory, Path.Type.volume)), new TransferStatus());
        final Path sourceFolder = new Path(room, new AlphanumericRandomStringService().random(), EnumSet.of(Path.Type.directory));
        final Path targetFolder = new Path(room, new AlphanumericRandomStringService().random(), EnumSet.of(Path.Type.directory));
        new SDSDirectoryFeature(session, nodeid).mkdir(sourceFolder, new TransferStatus());
        new SDSDirectoryFeature(session, nodeid).mkdir(targetFolder, new TransferStatus());
        final Path test = new Path(sourceFolder, new AlphanumericRandomStringService().random(), EnumSet.of(Path.Type.file));
        new SDSTouchFeature(session, nodeid).touch(test, new TransferStatus());
        final Path copy = new Path(targetFolder, test.getName(), EnumSet.of(Path.Type.file));
        new SDSTouchFeature(session, nodeid).touch(copy, new TransferStatus());
        final SDSDelegatingCopyFeature feature = new SDSDelegatingCopyFeature(session, nodeid, new SDSCopyFeature(session, nodeid));
        assertTrue(feature.isSupported(test, copy));
        final Path target = feature.copy(test, copy, new TransferStatus().exists(true), new DisabledConnectionCallback(), new DisabledStreamListener());
        assertNotEquals(test.attributes().getVersionId(), target.attributes().getVersionId());
        assertEquals(target.attributes().getVersionId(), new SDSAttributesFinderFeature(session, nodeid).find(target).getVersionId());
        final Find find = new DefaultFindFeature(session);
        final AttributedList<Path> files = new SDSListService(session, nodeid).list(targetFolder, new DisabledListProgressListener());
        assertTrue(find.find(copy));
        new SDSDeleteFeature(session, nodeid).delete(Collections.singletonList(room), new DisabledLoginCallback(), new Delete.DisabledCallback());
    }

    @Test
    public void testCopyWithRenameToExistingFile() throws Exception {
        final SDSNodeIdProvider nodeid = new SDSNodeIdProvider(session);
        final Path room = new SDSDirectoryFeature(session, nodeid).mkdir(
            new Path(new AlphanumericRandomStringService().random(), EnumSet.of(Path.Type.directory, Path.Type.volume)), new TransferStatus());
        final Path folder = new Path(room, new AlphanumericRandomStringService().random(), EnumSet.of(Path.Type.directory));
        new SDSDirectoryFeature(session, nodeid).mkdir(folder, new TransferStatus());
        final Path test = new Path(folder, new AlphanumericRandomStringService().random(), EnumSet.of(Path.Type.file));
        new SDSTouchFeature(session, nodeid).touch(test, new TransferStatus());
        final Path copy = new Path(folder, test.getName(), EnumSet.of(Path.Type.file));
        final SDSCopyFeature proxy = new SDSCopyFeature(session, nodeid);
        final SDSDelegatingCopyFeature feature = new SDSDelegatingCopyFeature(session, nodeid, proxy);
        assertFalse(proxy.isSupported(test, copy));
        assertTrue(feature.isSupported(test, copy));
        assertNotNull(feature.copy(test, copy, new TransferStatus().exists(true), new DisabledConnectionCallback(), new DisabledStreamListener()).attributes().getVersionId());
        final Find find = new DefaultFindFeature(session);
        final AttributedList<Path> files = new SDSListService(session, nodeid).list(folder, new DisabledListProgressListener());
        assertTrue(find.find(copy));
        new SDSDeleteFeature(session, nodeid).delete(Collections.singletonList(room), new DisabledLoginCallback(), new Delete.DisabledCallback());
    }

    @Test
    public void testCopyDirectoryServerSide() throws Exception {
        final SDSNodeIdProvider nodeid = new SDSNodeIdProvider(session);
        final Path room = new SDSDirectoryFeature(session, nodeid).mkdir(
            new Path(new AlphanumericRandomStringService().random(), EnumSet.of(Path.Type.directory, Path.Type.volume)), new TransferStatus());
        final Path directory = new SDSDirectoryFeature(session, nodeid).mkdir(new Path(room, new AlphanumericRandomStringService().random(), EnumSet.of(Path.Type.directory)), new TransferStatus());
        final String name = new AlphanumericRandomStringService().random();
        final Path file = new SDSTouchFeature(session, nodeid).touch(new Path(directory, name, EnumSet.of(Path.Type.file)), new TransferStatus());
        final Path target_parent = new SDSDirectoryFeature(session, nodeid).mkdir(new Path(room, new AlphanumericRandomStringService().random(), EnumSet.of(Path.Type.directory)), new TransferStatus());
        final Path target = new Path(target_parent, directory.getName(), EnumSet.of(Path.Type.directory));
        final SDSDelegatingCopyFeature feature = new SDSDelegatingCopyFeature(session, nodeid, new SDSCopyFeature(session, nodeid));
        assertTrue(feature.isSupported(directory, target));
        final Path copy = feature.copy(directory, target, new TransferStatus(), new DisabledConnectionCallback(), new DisabledStreamListener());
        assertNotNull(copy.attributes().getVersionId());
        assertTrue(new SDSFindFeature(session, nodeid).find(file));
        assertTrue(new SDSFindFeature(session, nodeid).find(target));
        assertTrue(new SDSFindFeature(session, nodeid).find(copy));
        new SDSDeleteFeature(session, nodeid).delete(Collections.singletonList(room), new DisabledLoginCallback(), new Delete.DisabledCallback());
    }

    @Test
    public void testCopyFileToDifferentDataRoom() throws Exception {
        final SDSNodeIdProvider nodeid = new SDSNodeIdProvider(session);
        final Path room1 = new SDSDirectoryFeature(session, nodeid).mkdir(new Path(
            new AlphanumericRandomStringService().random(), EnumSet.of(Path.Type.directory, Path.Type.volume)), new TransferStatus());
        final Path room2 = new SDSDirectoryFeature(session, nodeid).mkdir(new Path(
            new AlphanumericRandomStringService().random(), EnumSet.of(Path.Type.directory, Path.Type.volume)), new TransferStatus());
        final Path source = new SDSTouchFeature(session, nodeid).touch(new Path(room1, new AlphanumericRandomStringService().random(), EnumSet.of(Path.Type.file)), new TransferStatus());
        final Path target = new SDSTouchFeature(session, nodeid).touch(new Path(room2, source.getName(), EnumSet.of(Path.Type.file)), new TransferStatus());
        final SDSDelegatingCopyFeature feature = new SDSDelegatingCopyFeature(session, nodeid, new SDSCopyFeature(session, nodeid));
        assertTrue(feature.isSupported(source, target));
        assertNotNull(feature.copy(source, target, new TransferStatus(), new DisabledConnectionCallback(), new DisabledStreamListener()).attributes().getVersionId());
        assertTrue(new SDSFindFeature(session, nodeid).find(source));
        assertTrue(new SDSFindFeature(session, nodeid).find(target));
        new SDSDeleteFeature(session, nodeid).delete(Arrays.asList(room1, room2), new DisabledLoginCallback(), new Delete.DisabledCallback());
    }

    @Test
    public void testCopyFromEncryptedDataRoom() throws Exception {
        final SDSNodeIdProvider nodeid = new SDSNodeIdProvider(session);
        final Path room1 = new SDSDirectoryFeature(session, nodeid).createRoom(
                new Path(new AlphanumericRandomStringService().random(), EnumSet.of(Path.Type.directory, Path.Type.volume)), true);
        final Path room2 = new SDSDirectoryFeature(session, nodeid).mkdir(new Path(
                new AlphanumericRandomStringService().random(), EnumSet.of(Path.Type.directory, Path.Type.volume)), new TransferStatus());
        final byte[] content = RandomUtils.nextBytes(32769);
        final TransferStatus status = new TransferStatus();
        status.setLength(content.length);
        final Path test = new Path(room1, new AlphanumericRandomStringService().random(), EnumSet.of(Path.Type.file));
        final SDSEncryptionBulkFeature bulk = new SDSEncryptionBulkFeature(session, nodeid);
        bulk.pre(Transfer.Type.upload, Collections.singletonMap(new TransferItem(test), status), new DisabledConnectionCallback());
        final TripleCryptWriteFeature writer = new TripleCryptWriteFeature(session, nodeid, new SDSDirectS3MultipartWriteFeature(session, nodeid));
        final StatusOutputStream<Node> out = writer.write(test, status, new DisabledConnectionCallback());
        assertNotNull(out);
        new StreamCopier(status, status).transfer(new ByteArrayInputStream(content), out);
        final Path target = new Path(room2, new AlphanumericRandomStringService().random(), EnumSet.of(Path.Type.file));
        final SDSCopyFeature proxy = new SDSCopyFeature(session, nodeid);
        final SDSDelegatingCopyFeature feature = new SDSDelegatingCopyFeature(session, nodeid, proxy);
        final Path copy = feature.copy(test, target, new TransferStatus().withLength(content.length), new DisabledConnectionCallback() {
            @Override
            public void warn(final Host bookmark, final String title, final String message, final String defaultButton, final String cancelButton, final String preference) {
                //
            }

            @Override
            public Credentials prompt(final Host bookmark, final String title, final String reason, final LoginOptions options) {
                return new VaultCredentials(PROPERTIES.get("vault.passphrase"));
            }
        }, new DisabledStreamListener());
        assertNotNull(copy.attributes().getVersionId());
        assertEquals(copy.attributes().getVersionId(), new SDSAttributesFinderFeature(session, nodeid).find(copy).getVersionId());
        assertFalse(proxy.isSupported(test, target));
        assertTrue(feature.isSupported(test, target));
        assertTrue(new SDSFindFeature(session, nodeid).find(test));
        assertTrue(new SDSFindFeature(session, nodeid).find(copy));
        final byte[] compare = new byte[content.length];
        final InputStream stream = new SDSReadFeature(session, nodeid).read(target, new TransferStatus().withLength(content.length), new DisabledConnectionCallback() {
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
        new SDSDeleteFeature(session, nodeid).delete(Arrays.asList(room1, room2), new DisabledLoginCallback(), new Delete.DisabledCallback());
    }

    @Test
    public void testCopyToEncryptedDataRoom() throws Exception {
        final SDSNodeIdProvider nodeid = new SDSNodeIdProvider(session);
        final Path room1 = new SDSDirectoryFeature(session, nodeid).createRoom(
                new Path(new AlphanumericRandomStringService().random(), EnumSet.of(Path.Type.directory, Path.Type.volume)), true);
        final Path room2 = new SDSDirectoryFeature(session, nodeid).mkdir(new Path(
                new AlphanumericRandomStringService().random(), EnumSet.of(Path.Type.directory, Path.Type.volume)), new TransferStatus());
        final byte[] content = RandomUtils.nextBytes(32769);
        final TransferStatus status = new TransferStatus();
        status.setLength(content.length);
        final Path test = new Path(room2, new AlphanumericRandomStringService().random(), EnumSet.of(Path.Type.file));
        final SDSDirectS3MultipartWriteFeature writer = new SDSDirectS3MultipartWriteFeature(session, nodeid);
        final StatusOutputStream<Node> out = writer.write(test, status, new DisabledConnectionCallback());
        assertNotNull(out);
        new StreamCopier(status, status).transfer(new ByteArrayInputStream(content), out);
        final Path target = new Path(room1, new AlphanumericRandomStringService().random(), EnumSet.of(Path.Type.file));
        final SDSCopyFeature proxy = new SDSCopyFeature(session, nodeid);
        final SDSDelegatingCopyFeature feature = new SDSDelegatingCopyFeature(session, nodeid, proxy);
        assertNotNull(feature.copy(test, target, new TransferStatus().withLength(content.length), new DisabledConnectionCallback(), new DisabledStreamListener()).attributes().getVersionId());
        assertFalse(proxy.isSupported(test, target));
        assertTrue(feature.isSupported(test, target));
        assertTrue(new SDSFindFeature(session, nodeid).find(test));
        assertTrue(new SDSFindFeature(session, nodeid).find(target));
        final byte[] compare = new byte[content.length];
        final InputStream stream = new TripleCryptReadFeature(session, nodeid, new SDSReadFeature(session, nodeid)).read(target, new TransferStatus().withLength(content.length), new DisabledConnectionCallback() {
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
        new SDSDeleteFeature(session, nodeid).delete(Arrays.asList(room1, room2), new DisabledLoginCallback(), new Delete.DisabledCallback());
    }

    @Test
    public void testCopyFileWithRenameBetweenEncryptedDataRooms() throws Exception {
        final SDSNodeIdProvider nodeid = new SDSNodeIdProvider(session);
        final Path room1 = new SDSDirectoryFeature(session, nodeid).createRoom(
                new Path(new AlphanumericRandomStringService().random(), EnumSet.of(Path.Type.directory, Path.Type.volume)), true);
        room1.attributes().getAcl().addAll(new Acl.EmailUser(System.getProperties().getProperty("dracoon.user")), SDSPermissionsFeature.DELETE_ROLE);
        final Path room2 = new SDSDirectoryFeature(session, nodeid).mkdir(new Path(
                new AlphanumericRandomStringService().random(), EnumSet.of(Path.Type.directory, Path.Type.volume)), new TransferStatus());
        final EncryptRoomRequest encrypt = new EncryptRoomRequest().isEncrypted(true);
        new NodesApi(session.getClient()).encryptRoom(encrypt, Long.parseLong(new SDSNodeIdProvider(session).getVersionId(room2)), StringUtils.EMPTY, null);
        room2.attributes().withCustom(KEY_ENCRYPTED, String.valueOf(true));
        final byte[] content = RandomUtils.nextBytes(32769);
        final TransferStatus status = new TransferStatus();
        status.setLength(content.length);
        final Path test = new Path(room1, new AlphanumericRandomStringService().random(), EnumSet.of(Path.Type.file));
        final SDSEncryptionBulkFeature bulk = new SDSEncryptionBulkFeature(session, nodeid);
        bulk.pre(Transfer.Type.upload, Collections.singletonMap(new TransferItem(test), status), new DisabledConnectionCallback());
        final TripleCryptWriteFeature writer = new TripleCryptWriteFeature(session, nodeid, new SDSDirectS3MultipartWriteFeature(session, nodeid));
        final StatusOutputStream<Node> out = writer.write(test, status, new DisabledConnectionCallback());
        assertNotNull(out);
        new StreamCopier(status, status).transfer(new ByteArrayInputStream(content), out);
        final Path target = new Path(room2, new AlphanumericRandomStringService().random(), EnumSet.of(Path.Type.file));
        final SDSCopyFeature proxy = new SDSCopyFeature(session, nodeid);
        final SDSDelegatingCopyFeature feature = new SDSDelegatingCopyFeature(session, nodeid, proxy);
        assertNotNull(feature.copy(test, target, new TransferStatus().withLength(content.length), new DisabledConnectionCallback() {
            @Override
            public void warn(final Host bookmark, final String title, final String message, final String defaultButton, final String cancelButton, final String preference) {
                //
            }

            @Override
            public Credentials prompt(final Host bookmark, final String title, final String reason, final LoginOptions options) {
                return new VaultCredentials(PROPERTIES.get("vault.passphrase"));
            }
        }, new DisabledStreamListener()).attributes().getVersionId());
        assertFalse(proxy.isSupported(test, target));
        assertTrue(feature.isSupported(test, target));
        assertTrue(new SDSFindFeature(session, nodeid).find(test));
        assertTrue(new SDSFindFeature(session, nodeid).find(target));
        final byte[] compare = new byte[content.length];
        final InputStream stream = new TripleCryptReadFeature(session, nodeid, new SDSReadFeature(session, nodeid)).read(target, new TransferStatus().withLength(content.length), new DisabledConnectionCallback() {
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
        new SDSDeleteFeature(session, nodeid).delete(Arrays.asList(room1, room2), new DisabledLoginCallback(), new Delete.DisabledCallback());
    }

    @Test
    public void testCopyFileSameNameBetweenEncryptedDataRooms() throws Exception {
        final SDSNodeIdProvider nodeid = new SDSNodeIdProvider(session);
        final Path room1 = new SDSDirectoryFeature(session, nodeid).createRoom(
                new Path(new AlphanumericRandomStringService().random(), EnumSet.of(Path.Type.directory, Path.Type.volume)), true);
        room1.attributes().getAcl().addAll(new Acl.EmailUser(System.getProperties().getProperty("dracoon.user")), SDSPermissionsFeature.DELETE_ROLE);
        final Path room2 = new SDSDirectoryFeature(session, nodeid).mkdir(new Path(
                new AlphanumericRandomStringService().random(), EnumSet.of(Path.Type.directory, Path.Type.volume)), new TransferStatus());
        final EncryptRoomRequest encrypt = new EncryptRoomRequest().isEncrypted(true);
        new NodesApi(session.getClient()).encryptRoom(encrypt, Long.parseLong(new SDSNodeIdProvider(session).getVersionId(room2)), StringUtils.EMPTY, null);
        room2.attributes().withCustom(KEY_ENCRYPTED, String.valueOf(true));
        final byte[] content = RandomUtils.nextBytes(32769);
        final TransferStatus status = new TransferStatus();
        status.setLength(content.length);
        final Path test = new Path(room1, new AlphanumericRandomStringService().random(), EnumSet.of(Path.Type.file));
        final SDSEncryptionBulkFeature bulk = new SDSEncryptionBulkFeature(session, nodeid);
        bulk.pre(Transfer.Type.upload, Collections.singletonMap(new TransferItem(test), status), new DisabledConnectionCallback());
        final TripleCryptWriteFeature writer = new TripleCryptWriteFeature(session, nodeid, new SDSDirectS3MultipartWriteFeature(session, nodeid));
        final StatusOutputStream<Node> out = writer.write(test, status, new DisabledConnectionCallback());
        assertNotNull(out);
        new StreamCopier(status, status).transfer(new ByteArrayInputStream(content), out);
        test.withAttributes(status.getResponse());
        final Path target = new Path(room2, test.getName(), EnumSet.of(Path.Type.file));
        final SDSDelegatingCopyFeature feature = new SDSDelegatingCopyFeature(session, nodeid, new SDSCopyFeature(session, nodeid));
        assertTrue(feature.isSupported(test, target));
        final Path copy = feature.copy(test, target, new TransferStatus().withLength(content.length), new DisabledConnectionCallback() {
            @Override
            public void warn(final Host bookmark, final String title, final String message, final String defaultButton, final String cancelButton, final String preference) {
                //
            }

            @Override
            public Credentials prompt(final Host bookmark, final String title, final String reason, final LoginOptions options) {
                return new VaultCredentials(PROPERTIES.get("vault.passphrase"));
            }
        }, new DisabledStreamListener());
        assertNotNull(copy.attributes().getVersionId());
        assertTrue(new SDSFindFeature(session, nodeid).find(test));
        assertTrue(new SDSFindFeature(session, nodeid).find(target));
        final byte[] compare = new byte[content.length];
        final InputStream stream = new TripleCryptReadFeature(session, nodeid, new SDSReadFeature(session, nodeid)).read(copy, new TransferStatus().withLength(content.length), new DisabledConnectionCallback() {
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
        new SDSDeleteFeature(session, nodeid).delete(Arrays.asList(room1, room2), new DisabledLoginCallback(), new Delete.DisabledCallback());
    }
}
