package ch.cyberduck.core.cryptomator;

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

import ch.cyberduck.core.AlphanumericRandomStringService;
import ch.cyberduck.core.DisabledConnectionCallback;
import ch.cyberduck.core.DisabledHostKeyCallback;
import ch.cyberduck.core.DisabledLoginCallback;
import ch.cyberduck.core.DisabledPasswordCallback;
import ch.cyberduck.core.DisabledPasswordStore;
import ch.cyberduck.core.DisabledProgressListener;
import ch.cyberduck.core.DisabledTranscriptListener;
import ch.cyberduck.core.Path;
import ch.cyberduck.core.PathCache;
import ch.cyberduck.core.cryptomator.features.CryptoBulkFeature;
import ch.cyberduck.core.cryptomator.features.CryptoDeleteFeature;
import ch.cyberduck.core.cryptomator.features.CryptoDirectoryFeature;
import ch.cyberduck.core.cryptomator.features.CryptoFindFeature;
import ch.cyberduck.core.cryptomator.features.CryptoReadFeature;
import ch.cyberduck.core.cryptomator.features.CryptoTouchFeature;
import ch.cyberduck.core.cryptomator.features.CryptoWriteFeature;
import ch.cyberduck.core.features.Delete;
import ch.cyberduck.core.googledrive.AbstractDriveTest;
import ch.cyberduck.core.googledrive.DriveDeleteFeature;
import ch.cyberduck.core.googledrive.DriveDirectoryFeature;
import ch.cyberduck.core.googledrive.DriveHomeFinderService;
import ch.cyberduck.core.googledrive.DriveReadFeature;
import ch.cyberduck.core.googledrive.DriveTouchFeature;
import ch.cyberduck.core.googledrive.DriveWriteFeature;
import ch.cyberduck.core.io.StreamCopier;
import ch.cyberduck.core.shared.DefaultFindFeature;
import ch.cyberduck.core.shared.DefaultTouchFeature;
import ch.cyberduck.core.shared.DefaultUploadFeature;
import ch.cyberduck.core.shared.DisabledBulkFeature;
import ch.cyberduck.core.transfer.Transfer;
import ch.cyberduck.core.transfer.TransferStatus;
import ch.cyberduck.core.vault.DefaultVaultRegistry;
import ch.cyberduck.core.vault.VaultCredentials;
import ch.cyberduck.core.worker.MoveWorker;
import ch.cyberduck.test.IntegrationTest;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.RandomUtils;
import org.apache.commons.text.RandomStringGenerator;
import org.junit.Assert;
import org.junit.Test;
import org.junit.experimental.categories.Category;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.util.Arrays;
import java.util.Collections;
import java.util.EnumSet;
import java.util.UUID;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;

@Category(IntegrationTest.class)
public class MoveWorkerTest extends AbstractDriveTest {

    @Test
    public void testMoveSameFolderCryptomator() throws Exception {
        final Path home = new DriveHomeFinderService(session).find();
        final CryptoVault cryptomator = new CryptoVault(
                new Path(home, new AlphanumericRandomStringService().random(), EnumSet.of(Path.Type.directory)), new DisabledPasswordStore());
        final Path vault = cryptomator.create(session, null, new VaultCredentials("test"));
        final Path source = new Path(vault, new AlphanumericRandomStringService().random(), EnumSet.of(Path.Type.file));
        final Path target = new Path(vault, new AlphanumericRandomStringService().random(), EnumSet.of(Path.Type.file));
        session.withRegistry(new DefaultVaultRegistry(new DisabledPasswordStore(), new DisabledPasswordCallback(), cryptomator));
        final byte[] content = RandomUtils.nextBytes(40500);
        final TransferStatus status = new TransferStatus();
        new CryptoBulkFeature<>(session, new DisabledBulkFeature(), new DriveDeleteFeature(session), cryptomator).pre(Transfer.Type.upload, Collections.singletonMap(source, status), new DisabledConnectionCallback());
        new StreamCopier(new TransferStatus(), new TransferStatus()).transfer(new ByteArrayInputStream(content), new CryptoWriteFeature<>(session, new DriveWriteFeature(session), cryptomator).write(source, status.length(content.length), new DisabledConnectionCallback()));
        Assert.assertTrue(new CryptoFindFeature(session, new DefaultFindFeature(session), cryptomator).find(source));
        final MoveWorker worker = new MoveWorker(Collections.singletonMap(source, target), PathCache.empty(), new DisabledLoginCallback(), new DisabledHostKeyCallback(), new DisabledProgressListener(), new DisabledTranscriptListener());
        worker.run(session);
        Assert.assertFalse(new CryptoFindFeature(session, new DefaultFindFeature(session), cryptomator).find(source));
        Assert.assertTrue(new CryptoFindFeature(session, new DefaultFindFeature(session), cryptomator).find(target));
        final ByteArrayOutputStream out = new ByteArrayOutputStream(content.length);
        assertEquals(content.length, IOUtils.copy(new CryptoReadFeature(session, new DriveReadFeature(session), cryptomator).read(target, new TransferStatus().length(content.length), new DisabledConnectionCallback()), out));
        assertArrayEquals(content, out.toByteArray());
        new CryptoDeleteFeature(session, new DriveDeleteFeature(session), cryptomator).delete(Arrays.asList(target, vault), new DisabledLoginCallback(), new Delete.DisabledCallback());
    }

    @Test
    public void testMoveToDifferentFolderCryptomator() throws Exception {
        final Path home = new DriveHomeFinderService(session).find();
        final CryptoVault cryptomator = new CryptoVault(
                new Path(home, UUID.randomUUID().toString(), EnumSet.of(Path.Type.directory)), new DisabledPasswordStore());
        final Path vault = cryptomator.create(session, null, new VaultCredentials("test"));
        final Path source = new Path(vault, UUID.randomUUID().toString(), EnumSet.of(Path.Type.file));
        session.withRegistry(new DefaultVaultRegistry(new DisabledPasswordStore(), new DisabledPasswordCallback(), cryptomator));
        new CryptoTouchFeature<>(session, new DriveTouchFeature(session), new DriveWriteFeature(session), cryptomator).touch(source, new TransferStatus());
        Assert.assertTrue(new CryptoFindFeature(session, new DefaultFindFeature(session), cryptomator).find(source));
        final Path targetFolder = new CryptoDirectoryFeature<>(session, new DriveDirectoryFeature(session), new DriveWriteFeature(session), cryptomator).mkdir(
                new Path(vault, UUID.randomUUID().toString(), EnumSet.of(Path.Type.directory)), null, new TransferStatus());
        final Path target = new Path(targetFolder, UUID.randomUUID().toString(), EnumSet.of(Path.Type.file));
        Assert.assertTrue(new CryptoFindFeature(session, new DefaultFindFeature(session), cryptomator).find(targetFolder));
        final MoveWorker worker = new MoveWorker(Collections.singletonMap(source, target), PathCache.empty(), new DisabledLoginCallback(), new DisabledHostKeyCallback(), new DisabledProgressListener(), new DisabledTranscriptListener());
        worker.run(session);
        Assert.assertFalse(new CryptoFindFeature(session, new DefaultFindFeature(session), cryptomator).find(source));
        Assert.assertTrue(new CryptoFindFeature(session, new DefaultFindFeature(session), cryptomator).find(target));
        new CryptoDeleteFeature(session, new DriveDeleteFeature(session), cryptomator).delete(Arrays.asList(target, targetFolder, vault), new DisabledLoginCallback(), new Delete.DisabledCallback());
    }

    @Test
    public void testMoveToDifferentFolderLongFilenameCryptomator() throws Exception {
        final Path home = new DriveHomeFinderService(session).find();
        final CryptoVault cryptomator = new CryptoVault(
                new Path(home, new AlphanumericRandomStringService().random(), EnumSet.of(Path.Type.directory)), new DisabledPasswordStore());
        final Path vault = cryptomator.create(session, null, new VaultCredentials("test"));
        final Path source = new Path(vault, new RandomStringGenerator.Builder().build().generate(130), EnumSet.of(Path.Type.file));
        session.withRegistry(new DefaultVaultRegistry(new DisabledPasswordStore(), new DisabledPasswordCallback(), cryptomator));
        new CryptoTouchFeature<>(session, new DriveTouchFeature(session), new DriveWriteFeature(session), cryptomator).touch(source, new TransferStatus());
        Assert.assertTrue(new CryptoFindFeature(session, new DefaultFindFeature(session), cryptomator).find(source));
        final Path targetFolder = new CryptoDirectoryFeature<>(session, new DriveDirectoryFeature(session), new DriveWriteFeature(session), cryptomator).mkdir(
                new Path(vault, new RandomStringGenerator.Builder().build().generate(130), EnumSet.of(Path.Type.directory)), null, new TransferStatus());
        Assert.assertTrue(new CryptoFindFeature(session, new DefaultFindFeature(session), cryptomator).find(targetFolder));
        final Path target = new Path(targetFolder, new RandomStringGenerator.Builder().build().generate(130), EnumSet.of(Path.Type.file));
        final MoveWorker worker = new MoveWorker(Collections.singletonMap(source, target), PathCache.empty(), new DisabledLoginCallback(), new DisabledHostKeyCallback(), new DisabledProgressListener(), new DisabledTranscriptListener());
        worker.run(session);
        Assert.assertFalse(new CryptoFindFeature(session, new DefaultFindFeature(session), cryptomator).find(source));
        Assert.assertTrue(new CryptoFindFeature(session, new DefaultFindFeature(session), cryptomator).find(target));
        new CryptoDeleteFeature(session, new DriveDeleteFeature(session), cryptomator).delete(Arrays.asList(target, targetFolder, vault), new DisabledLoginCallback(), new Delete.DisabledCallback());
    }

    @Test
    public void testMoveFolder() throws Exception {
        final Path home = new DriveHomeFinderService(session).find();
        final CryptoVault cryptomator = new CryptoVault(
                new Path(home, new AlphanumericRandomStringService().random(), EnumSet.of(Path.Type.directory)), new DisabledPasswordStore());
        final Path vault = cryptomator.create(session, null, new VaultCredentials("test"));
        session.withRegistry(new DefaultVaultRegistry(new DisabledPasswordStore(), new DisabledPasswordCallback(), cryptomator));
        final Path folder = new CryptoDirectoryFeature<>(session, new DriveDirectoryFeature(session), new DriveWriteFeature(session), cryptomator).mkdir(
                new Path(vault, new AlphanumericRandomStringService().random(), EnumSet.of(Path.Type.directory)), null, new TransferStatus());
        Assert.assertTrue(new CryptoFindFeature(session, new DefaultFindFeature(session), cryptomator).find(folder));
        final Path file = new CryptoTouchFeature<>(session, new DriveTouchFeature(session), new DriveWriteFeature(session), cryptomator).touch(
                new Path(folder, new AlphanumericRandomStringService().random(), EnumSet.of(Path.Type.file)), new TransferStatus());
        Assert.assertTrue(new CryptoFindFeature(session, new DefaultFindFeature(session), cryptomator).find(file));
        // rename file
        final Path fileRenamed = new Path(folder, "f1", EnumSet.of(Path.Type.file));
        new MoveWorker(Collections.singletonMap(file, fileRenamed), PathCache.empty(), new DisabledLoginCallback(), new DisabledHostKeyCallback(), new DisabledProgressListener(), new DisabledTranscriptListener()).run(session);
        Assert.assertFalse(new CryptoFindFeature(session, new DefaultFindFeature(session), cryptomator).find(file));
        Assert.assertTrue(new CryptoFindFeature(session, new DefaultFindFeature(session), cryptomator).find(fileRenamed));
        // rename folder
        final Path folderRenamed = new Path(vault, new AlphanumericRandomStringService().random(), EnumSet.of(Path.Type.directory));
        new MoveWorker(Collections.singletonMap(folder, folderRenamed), PathCache.empty(), new DisabledLoginCallback(), new DisabledHostKeyCallback(), new DisabledProgressListener(), new DisabledTranscriptListener()).run(session);
        Assert.assertFalse(new CryptoFindFeature(session, new DefaultFindFeature(session), cryptomator).find(folder));
        Assert.assertTrue(new CryptoFindFeature(session, new DefaultFindFeature(session), cryptomator).find(folderRenamed));
        final Path fileRenamedInRenamedFolder = new Path(folderRenamed, "f1", EnumSet.of(Path.Type.file));
        Assert.assertTrue(new CryptoFindFeature(session, new DefaultFindFeature(session), cryptomator).find(fileRenamedInRenamedFolder));
        new CryptoDeleteFeature(session, new DriveDeleteFeature(session), cryptomator).delete(Arrays.asList(fileRenamedInRenamedFolder, folderRenamed, vault), new DisabledLoginCallback(), new Delete.DisabledCallback());
    }

    @Test
    public void testMoveFileIntoVault() throws Exception {
        final Path home = new DriveHomeFinderService(session).find();
        final Path clearFile = new Path(home, new AlphanumericRandomStringService().random(), EnumSet.of(Path.Type.file));
        new DriveTouchFeature(session).touch(clearFile, new TransferStatus());
        Assert.assertTrue(new DefaultFindFeature(session).find(clearFile));
        final CryptoVault cryptomator = new CryptoVault(
                new Path(home, new AlphanumericRandomStringService().random(), EnumSet.of(Path.Type.directory)), new DisabledPasswordStore());
        final Path vault = cryptomator.create(session, null, new VaultCredentials("test"));
        final DefaultVaultRegistry registry = new DefaultVaultRegistry(new DisabledPasswordStore(), new DisabledPasswordCallback(), cryptomator);
        session.withRegistry(registry);
        final Path encryptedFolder = new CryptoDirectoryFeature<>(session, new DriveDirectoryFeature(session), new DriveWriteFeature(session), cryptomator).mkdir(
                new Path(vault, new AlphanumericRandomStringService().random(), EnumSet.of(Path.Type.directory)), null, new TransferStatus());
        Assert.assertTrue(new CryptoFindFeature(session, new DefaultFindFeature(session), cryptomator).find(encryptedFolder));
        // move file into vault
        final Path encryptedFile = new Path(encryptedFolder, new AlphanumericRandomStringService().random(), EnumSet.of(Path.Type.file));
        final MoveWorker worker = new MoveWorker(Collections.singletonMap(clearFile, encryptedFile), PathCache.empty(), new DisabledLoginCallback(), new DisabledHostKeyCallback(), new DisabledProgressListener(), new DisabledTranscriptListener());
        worker.run(session);
        Assert.assertFalse(new DefaultFindFeature(session).find(clearFile));
        Assert.assertTrue(new CryptoFindFeature(session, new DefaultFindFeature(session), cryptomator).find(encryptedFile));
        new CryptoDeleteFeature(session, new DriveDeleteFeature(session), cryptomator).delete(Arrays.asList(encryptedFile, encryptedFolder, vault), new DisabledLoginCallback(), new Delete.DisabledCallback());
        registry.clear();
    }

    @Test
    public void testMoveDirectoryIntoVault() throws Exception {
        final Path home = new DriveHomeFinderService(session).find();
        final Path clearFolder = new Path(home, new AlphanumericRandomStringService().random(), EnumSet.of(Path.Type.directory));
        final Path clearFile = new Path(clearFolder, new AlphanumericRandomStringService().random(), EnumSet.of(Path.Type.file));
        new DriveDirectoryFeature(session).mkdir(clearFolder, null, new TransferStatus());
        new DriveTouchFeature(session).touch(clearFile, new TransferStatus());
        Assert.assertTrue(new DefaultFindFeature(session).find(clearFolder));
        Assert.assertTrue(new DefaultFindFeature(session).find(clearFile));
        final CryptoVault cryptomator = new CryptoVault(
                new Path(home, new AlphanumericRandomStringService().random(), EnumSet.of(Path.Type.directory)), new DisabledPasswordStore());
        final Path vault = cryptomator.create(session, null, new VaultCredentials("test"));
        final DefaultVaultRegistry registry = new DefaultVaultRegistry(new DisabledPasswordStore(), new DisabledPasswordCallback(), cryptomator);
        session.withRegistry(registry);
        // move directory into vault
        final Path encryptedFolder = new Path(vault, clearFolder.getName(), EnumSet.of(Path.Type.directory));
        final Path encryptedFile = new Path(encryptedFolder, clearFile.getName(), EnumSet.of(Path.Type.file));
        final MoveWorker worker = new MoveWorker(Collections.singletonMap(clearFolder, encryptedFolder), PathCache.empty(), new DisabledLoginCallback(), new DisabledHostKeyCallback(), new DisabledProgressListener(), new DisabledTranscriptListener());
        worker.run(session);
        Assert.assertTrue(new CryptoFindFeature(session, new DefaultFindFeature(session), cryptomator).find(encryptedFolder));
        Assert.assertTrue(new CryptoFindFeature(session, new DefaultFindFeature(session), cryptomator).find(encryptedFile));
        Assert.assertFalse(new DefaultFindFeature(session).find(clearFolder));
        Assert.assertFalse(new DefaultFindFeature(session).find(clearFile));
        new CryptoDeleteFeature(session, new DriveDeleteFeature(session), cryptomator).delete(Arrays.asList(encryptedFile, encryptedFolder, vault), new DisabledLoginCallback(), new Delete.DisabledCallback());
        registry.clear();
    }

    @Test
    public void testMoveFileOutsideVault() throws Exception {
        final Path home = new DriveHomeFinderService(session).find();
        final Path clearFolder = new Path(home, new AlphanumericRandomStringService().random(), EnumSet.of(Path.Type.directory));
        new DriveDirectoryFeature(session).mkdir(clearFolder, null, new TransferStatus());
        final CryptoVault cryptomator = new CryptoVault(
                new Path(home, new AlphanumericRandomStringService().random(), EnumSet.of(Path.Type.directory)), new DisabledPasswordStore());
        final Path vault = cryptomator.create(session, null, new VaultCredentials("test"));
        final DefaultVaultRegistry registry = new DefaultVaultRegistry(new DisabledPasswordStore(), new DisabledPasswordCallback(), cryptomator);
        session.withRegistry(registry);
        final Path encryptedFolder = new CryptoDirectoryFeature<>(session, new DriveDirectoryFeature(session), new DriveWriteFeature(session), cryptomator).mkdir(
                new Path(vault, new AlphanumericRandomStringService().random(), EnumSet.of(Path.Type.directory)), null, new TransferStatus());
        Assert.assertTrue(new CryptoFindFeature(session, new DefaultFindFeature(session), cryptomator).find(encryptedFolder));
        final Path encryptedFile = new CryptoTouchFeature<Void>(session, new DefaultTouchFeature<Void>(new DefaultUploadFeature<Void>(new DriveWriteFeature(session))), new DriveWriteFeature(session), cryptomator).touch(
                new Path(encryptedFolder, new AlphanumericRandomStringService().random(), EnumSet.of(Path.Type.file)), new TransferStatus());
        Assert.assertTrue(new CryptoFindFeature(session, new DefaultFindFeature(session), cryptomator).find(encryptedFile));
        // move file outside vault
        final Path fileRenamed = new Path(clearFolder, new AlphanumericRandomStringService().random(), EnumSet.of(Path.Type.file));
        final MoveWorker worker = new MoveWorker(Collections.singletonMap(encryptedFile, fileRenamed), PathCache.empty(), new DisabledLoginCallback(), new DisabledHostKeyCallback(), new DisabledProgressListener(), new DisabledTranscriptListener());
        worker.run(session);
        Assert.assertFalse(new CryptoFindFeature(session, new DefaultFindFeature(session), cryptomator).find(encryptedFile));
        Assert.assertTrue(new DefaultFindFeature(session).find(fileRenamed));
        new CryptoDeleteFeature(session, new DriveDeleteFeature(session), cryptomator).delete(Collections.singletonList(encryptedFolder), new DisabledLoginCallback(), new Delete.DisabledCallback());
        new DriveDeleteFeature(session).delete(Arrays.asList(fileRenamed, clearFolder), new DisabledLoginCallback(), new Delete.DisabledCallback());
        registry.clear();
    }

    @Test
    public void testMoveDirectoryOutsideVault() throws Exception {
        final Path home = new DriveHomeFinderService(session).find();
        final CryptoVault cryptomator = new CryptoVault(
                new Path(home, new AlphanumericRandomStringService().random(), EnumSet.of(Path.Type.directory)), new DisabledPasswordStore());
        final Path vault = cryptomator.create(session, null, new VaultCredentials("test"));
        final DefaultVaultRegistry registry = new DefaultVaultRegistry(new DisabledPasswordStore(), new DisabledPasswordCallback(), cryptomator);
        session.withRegistry(registry);
        final Path encryptedFolder = new CryptoDirectoryFeature<>(session, new DriveDirectoryFeature(session), new DriveWriteFeature(session), cryptomator).mkdir(
                new Path(vault, new AlphanumericRandomStringService().random(), EnumSet.of(Path.Type.directory)), null, new TransferStatus());
        Assert.assertTrue(new CryptoFindFeature(session, new DefaultFindFeature(session), cryptomator).find(encryptedFolder));
        final String filename = new AlphanumericRandomStringService().random();
        final Path encryptedFile = new CryptoTouchFeature<Void>(session, new DefaultTouchFeature<Void>(new DefaultUploadFeature<Void>(new DriveWriteFeature(session))), new DriveWriteFeature(session), cryptomator).touch(
                new Path(encryptedFolder, filename, EnumSet.of(Path.Type.file)), new TransferStatus());
        Assert.assertTrue(new CryptoFindFeature(session, new DefaultFindFeature(session), cryptomator).find(encryptedFile));
        // move directory outside vault
        final Path directoryRenamed = new Path(home, new AlphanumericRandomStringService().random(), EnumSet.of(Path.Type.directory));
        final MoveWorker worker = new MoveWorker(Collections.singletonMap(encryptedFolder, directoryRenamed), PathCache.empty(), new DisabledLoginCallback(), new DisabledHostKeyCallback(), new DisabledProgressListener(), new DisabledTranscriptListener());
        worker.run(session);
        Assert.assertFalse(new CryptoFindFeature(session, new DefaultFindFeature(session), cryptomator).find(encryptedFolder));
        Assert.assertFalse(new CryptoFindFeature(session, new DefaultFindFeature(session), cryptomator).find(encryptedFile));
        Assert.assertTrue(new DefaultFindFeature(session).find(directoryRenamed));
        final Path fileRenamed = new Path(directoryRenamed, filename, EnumSet.of(Path.Type.file));
        Assert.assertTrue(new DefaultFindFeature(session).find(fileRenamed));
        new DriveDeleteFeature(session).delete(Arrays.asList(fileRenamed, directoryRenamed), new DisabledLoginCallback(), new Delete.DisabledCallback());
        registry.clear();
    }
}
