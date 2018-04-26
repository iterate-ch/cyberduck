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
import ch.cyberduck.core.ConnectionCallback;
import ch.cyberduck.core.Credentials;
import ch.cyberduck.core.DisabledConnectionCallback;
import ch.cyberduck.core.DisabledListProgressListener;
import ch.cyberduck.core.DisabledLoginCallback;
import ch.cyberduck.core.Host;
import ch.cyberduck.core.LoginOptions;
import ch.cyberduck.core.Path;
import ch.cyberduck.core.VersionId;
import ch.cyberduck.core.exception.ConnectionCanceledException;
import ch.cyberduck.core.exception.LoginCanceledException;
import ch.cyberduck.core.features.Delete;
import ch.cyberduck.core.features.Find;
import ch.cyberduck.core.io.StatusOutputStream;
import ch.cyberduck.core.io.StreamCopier;
import ch.cyberduck.core.sds.triplecrypt.CryptoReadFeature;
import ch.cyberduck.core.sds.triplecrypt.CryptoWriteFeature;
import ch.cyberduck.core.shared.DefaultFindFeature;
import ch.cyberduck.core.transfer.Transfer;
import ch.cyberduck.core.transfer.TransferStatus;
import ch.cyberduck.core.vault.VaultCredentials;
import ch.cyberduck.test.IntegrationTest;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.RandomUtils;
import org.junit.Test;
import org.junit.experimental.categories.Category;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.Arrays;
import java.util.Collections;
import java.util.EnumSet;

import static org.junit.Assert.*;

@Category(IntegrationTest.class)
public class SDSDelegatingCopyFeatureTest extends AbstractSDSTest {

    @Test
    public void testCopyFileServerSide() throws Exception {
        final SDSNodeIdProvider nodeid = new SDSNodeIdProvider(session).withCache(cache);
        final Path room = new SDSDirectoryFeature(session, nodeid).mkdir(
            new Path(new AlphanumericRandomStringService().random(), EnumSet.of(Path.Type.directory, Path.Type.volume)), null, new TransferStatus());
        final Path test = new SDSTouchFeature(session, nodeid).touch(new Path(room, new AlphanumericRandomStringService().random(), EnumSet.of(Path.Type.file)), new TransferStatus());
        final Path copy = new Path(new SDSDirectoryFeature(session, nodeid).mkdir(new Path(room, new AlphanumericRandomStringService().random(), EnumSet.of(Path.Type.directory)), null, new TransferStatus()), test.getName(), EnumSet.of(Path.Type.file));
        new SDSTouchFeature(session, nodeid).touch(copy, new TransferStatus());
        final SDSCopyFeature feature = new SDSCopyFeature(session, nodeid);
        assertTrue(feature.isSupported(test, copy));
        new SDSDelegatingCopyFeature(session, feature).copy(test, copy, new TransferStatus(), new DisabledConnectionCallback());
        assertTrue(new SDSFindFeature(nodeid).find(test));
        assertTrue(new SDSFindFeature(nodeid).find(copy));
        new SDSDeleteFeature(session, nodeid).delete(Collections.singletonList(room), new DisabledLoginCallback(), new Delete.DisabledCallback());
    }

    @Test
    public void testCopyFileWithRename() throws Exception {
        final SDSNodeIdProvider nodeid = new SDSNodeIdProvider(session).withCache(cache);
        final Path room = new SDSDirectoryFeature(session, nodeid).mkdir(
            new Path(new AlphanumericRandomStringService().random(), EnumSet.of(Path.Type.directory, Path.Type.volume)), null, new TransferStatus());
        final Path test = new SDSTouchFeature(session, nodeid).touch(new Path(room, new AlphanumericRandomStringService().random(), EnumSet.of(Path.Type.file)), new TransferStatus());
        final Path copy = new Path(new SDSDirectoryFeature(session, nodeid).mkdir(new Path(room, new AlphanumericRandomStringService().random(), EnumSet.of(Path.Type.directory)), null, new TransferStatus()), new AlphanumericRandomStringService().random(), EnumSet.of(Path.Type.file));
        final SDSCopyFeature feature = new SDSCopyFeature(session, nodeid);
        assertFalse(feature.isSupported(test, copy));
        new SDSDelegatingCopyFeature(session, feature).copy(test, copy, new TransferStatus(), new DisabledConnectionCallback());
        assertTrue(new SDSFindFeature(nodeid).find(test));
        assertTrue(new SDSFindFeature(nodeid).find(copy));
        new SDSDeleteFeature(session, nodeid).delete(Collections.singletonList(room), new DisabledLoginCallback(), new Delete.DisabledCallback());
    }

    @Test
    public void testCopyServerSideToExistingFile() throws Exception {
        final SDSNodeIdProvider nodeid = new SDSNodeIdProvider(session).withCache(cache);
        final Path room = new SDSDirectoryFeature(session, nodeid).mkdir(
            new Path(new AlphanumericRandomStringService().random(), EnumSet.of(Path.Type.directory, Path.Type.volume)), null, new TransferStatus());
        final Path sourceFolder = new Path(room, new AlphanumericRandomStringService().random(), EnumSet.of(Path.Type.directory));
        final Path targetFolder = new Path(room, new AlphanumericRandomStringService().random(), EnumSet.of(Path.Type.directory));
        new SDSDirectoryFeature(session, nodeid).mkdir(sourceFolder, null, new TransferStatus());
        new SDSDirectoryFeature(session, nodeid).mkdir(targetFolder, null, new TransferStatus());
        final Path test = new Path(sourceFolder, new AlphanumericRandomStringService().random(), EnumSet.of(Path.Type.file));
        new SDSTouchFeature(session, nodeid).touch(test, new TransferStatus());
        final Path copy = new Path(targetFolder, test.getName(), EnumSet.of(Path.Type.file));
        new SDSTouchFeature(session, nodeid).touch(copy, new TransferStatus());
        final SDSCopyFeature feature = new SDSCopyFeature(session, nodeid);
        assertTrue(feature.isSupported(test, copy));
        new SDSDelegatingCopyFeature(session, feature).copy(test, copy, new TransferStatus().exists(true), new DisabledConnectionCallback());
        final Find find = new DefaultFindFeature(session);
        final AttributedList<Path> files = new SDSListService(session, nodeid).list(targetFolder, new DisabledListProgressListener());
        assertTrue(find.find(copy));
        new SDSDeleteFeature(session, nodeid).delete(Collections.singletonList(room), new DisabledLoginCallback(), new Delete.DisabledCallback());
    }

    @Test
    public void testCopyWithRenameToExistingFile() throws Exception {
        final SDSNodeIdProvider nodeid = new SDSNodeIdProvider(session).withCache(cache);
        final Path room = new SDSDirectoryFeature(session, nodeid).mkdir(
            new Path(new AlphanumericRandomStringService().random(), EnumSet.of(Path.Type.directory, Path.Type.volume)), null, new TransferStatus());
        final Path folder = new Path(room, new AlphanumericRandomStringService().random(), EnumSet.of(Path.Type.directory));
        new SDSDirectoryFeature(session, nodeid).mkdir(folder, null, new TransferStatus());
        final Path test = new Path(folder, new AlphanumericRandomStringService().random(), EnumSet.of(Path.Type.file));
        new SDSTouchFeature(session, nodeid).touch(test, new TransferStatus());
        final Path copy = new Path(folder, test.getName(), EnumSet.of(Path.Type.file));
        new SDSTouchFeature(session, nodeid).touch(copy, new TransferStatus());
        final SDSCopyFeature feature = new SDSCopyFeature(session, nodeid);
        assertFalse(feature.isSupported(test, copy));
        new SDSDelegatingCopyFeature(session, feature).copy(test, copy, new TransferStatus().exists(true), new DisabledConnectionCallback());
        final Find find = new DefaultFindFeature(session);
        final AttributedList<Path> files = new SDSListService(session, nodeid).list(folder, new DisabledListProgressListener());
        assertTrue(find.find(copy));
        new SDSDeleteFeature(session, nodeid).delete(Collections.singletonList(room), new DisabledLoginCallback(), new Delete.DisabledCallback());
    }

    @Test
    public void testCopyDirectoryServerSide() throws Exception {
        final SDSNodeIdProvider nodeid = new SDSNodeIdProvider(session).withCache(cache);
        final Path room = new SDSDirectoryFeature(session, nodeid).mkdir(
            new Path(new AlphanumericRandomStringService().random(), EnumSet.of(Path.Type.directory, Path.Type.volume)), null, new TransferStatus());
        final Path directory = new SDSDirectoryFeature(session, nodeid).mkdir(new Path(room, new AlphanumericRandomStringService().random(), EnumSet.of(Path.Type.directory)), null, new TransferStatus());
        final String name = new AlphanumericRandomStringService().random();
        final Path file = new SDSTouchFeature(session, nodeid).touch(new Path(directory, name, EnumSet.of(Path.Type.file)), new TransferStatus());
        final Path target_parent = new SDSDirectoryFeature(session, nodeid).mkdir(new Path(room, new AlphanumericRandomStringService().random(), EnumSet.of(Path.Type.directory)), null, new TransferStatus());
        final Path target = new Path(target_parent, directory.getName(), EnumSet.of(Path.Type.directory));
        final SDSCopyFeature feature = new SDSCopyFeature(session, nodeid);
        assertTrue(feature.isSupported(directory, target));
        final Path copy = new SDSDelegatingCopyFeature(session, feature).copy(directory, target, new TransferStatus(), new DisabledConnectionCallback());
        assertTrue(new SDSFindFeature(nodeid).find(file));
        assertTrue(new SDSFindFeature(nodeid).find(target));
        assertTrue(new SDSFindFeature(nodeid).find(copy));
        new SDSDeleteFeature(session, nodeid).delete(Collections.singletonList(room), new DisabledLoginCallback(), new Delete.DisabledCallback());
    }

    @Test
    public void testCopyFileToDifferentDataRoom() throws Exception {
        final SDSNodeIdProvider nodeid = new SDSNodeIdProvider(session).withCache(cache);
        final Path room1 = new SDSDirectoryFeature(session, nodeid).mkdir(new Path(
            new AlphanumericRandomStringService().random(), EnumSet.of(Path.Type.directory, Path.Type.volume)), null, new TransferStatus());
        final Path room2 = new SDSDirectoryFeature(session, nodeid).mkdir(new Path(
            new AlphanumericRandomStringService().random(), EnumSet.of(Path.Type.directory, Path.Type.volume)), null, new TransferStatus());
        final Path source = new SDSTouchFeature(session, nodeid).touch(new Path(room1, new AlphanumericRandomStringService().random(), EnumSet.of(Path.Type.file)), new TransferStatus());
        final Path target = new SDSTouchFeature(session, nodeid).touch(new Path(room2, source.getName(), EnumSet.of(Path.Type.file)), new TransferStatus());
        final SDSCopyFeature feature = new SDSCopyFeature(session, nodeid);
        assertTrue(feature.isSupported(source, target));
        new SDSDelegatingCopyFeature(session, feature).copy(source, target, new TransferStatus(), new DisabledConnectionCallback());
        assertTrue(new SDSFindFeature(nodeid).find(source));
        assertTrue(new SDSFindFeature(nodeid).find(target));
        new SDSDeleteFeature(session, nodeid).delete(Arrays.asList(room1, room2), new DisabledLoginCallback(), new Delete.DisabledCallback());
    }

    @Test
    public void testCopyFromEncryptedDataRoom() throws Exception {
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
        final SDSCopyFeature feature = new SDSCopyFeature(session, nodeid);
        final Path copy = new SDSDelegatingCopyFeature(session, feature).copy(test, target, new TransferStatus().length(content.length), new ConnectionCallback() {
            @Override
            public void warn(final Host bookmark, final String title, final String message, final String defaultButton, final String cancelButton, final String preference) throws ConnectionCanceledException {
                //
            }

            @Override
            public Credentials prompt(final Host bookmark, final String title, final String reason, final LoginOptions options) throws LoginCanceledException {
                return new VaultCredentials("ahbic3Ae");
            }
        });
        assertFalse(feature.isSupported(test, target));
        assertTrue(new SDSFindFeature(nodeid).find(test));
        assertTrue(new SDSFindFeature(nodeid).find(copy));
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
    public void testCopyToEncryptedDataRoom() throws Exception {
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
        final SDSCopyFeature feature = new SDSCopyFeature(session, nodeid);
        new SDSDelegatingCopyFeature(session, feature).copy(test, target, new TransferStatus().length(content.length), new DisabledConnectionCallback());
        assertFalse(feature.isSupported(test, target));
        assertTrue(new SDSFindFeature(nodeid).find(test));
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
    public void testCopyFileWithRenameBetweenEncryptedDataRooms() throws Exception {
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
        final Path target = new Path(room2, new AlphanumericRandomStringService().random(), EnumSet.of(Path.Type.file, Path.Type.decrypted));
        final SDSCopyFeature feature = new SDSCopyFeature(session, nodeid);
        new SDSDelegatingCopyFeature(session, feature).copy(test, target, new TransferStatus().length(content.length), new ConnectionCallback() {
            @Override
            public void warn(final Host bookmark, final String title, final String message, final String defaultButton, final String cancelButton, final String preference) throws ConnectionCanceledException {
                //
            }

            @Override
            public Credentials prompt(final Host bookmark, final String title, final String reason, final LoginOptions options) throws LoginCanceledException {
                return new VaultCredentials("ahbic3Ae");
            }
        });
        assertFalse(feature.isSupported(test, target));
        assertTrue(new SDSFindFeature(nodeid).find(test));
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
    public void testCopyFileSameNameBetweenEncryptedDataRooms() throws Exception {
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
        final Path target = new Path(room2, test.getName(), EnumSet.of(Path.Type.file, Path.Type.decrypted));
        final SDSCopyFeature feature = new SDSCopyFeature(session, nodeid);
        assertTrue(feature.isSupported(test, target));
        new SDSDelegatingCopyFeature(session, feature).copy(test, target, new TransferStatus().length(content.length), new DisabledConnectionCallback());
        assertTrue(new SDSFindFeature(nodeid).find(test));
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
}
