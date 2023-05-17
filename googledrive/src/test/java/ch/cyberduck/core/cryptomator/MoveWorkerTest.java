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
import ch.cyberduck.core.DisabledListProgressListener;
import ch.cyberduck.core.DisabledLoginCallback;
import ch.cyberduck.core.DisabledPasswordCallback;
import ch.cyberduck.core.DisabledPasswordStore;
import ch.cyberduck.core.DisabledProgressListener;
import ch.cyberduck.core.Path;
import ch.cyberduck.core.PathCache;
import ch.cyberduck.core.cryptomator.features.CryptoBulkFeature;
import ch.cyberduck.core.cryptomator.features.CryptoListService;
import ch.cyberduck.core.cryptomator.features.CryptoReadFeature;
import ch.cyberduck.core.cryptomator.features.CryptoTouchFeature;
import ch.cyberduck.core.cryptomator.features.CryptoWriteFeature;
import ch.cyberduck.core.features.Delete;
import ch.cyberduck.core.features.Directory;
import ch.cyberduck.core.features.Find;
import ch.cyberduck.core.googledrive.AbstractDriveTest;
import ch.cyberduck.core.googledrive.DriveDeleteFeature;
import ch.cyberduck.core.googledrive.DriveDirectoryFeature;
import ch.cyberduck.core.googledrive.DriveFileIdProvider;
import ch.cyberduck.core.googledrive.DriveHomeFinderService;
import ch.cyberduck.core.googledrive.DriveListService;
import ch.cyberduck.core.googledrive.DriveReadFeature;
import ch.cyberduck.core.googledrive.DriveTouchFeature;
import ch.cyberduck.core.googledrive.DriveWriteFeature;
import ch.cyberduck.core.io.StreamCopier;
import ch.cyberduck.core.pool.SessionPool;
import ch.cyberduck.core.shared.DefaultFindFeature;
import ch.cyberduck.core.shared.DefaultTouchFeature;
import ch.cyberduck.core.shared.DisabledBulkFeature;
import ch.cyberduck.core.transfer.Transfer;
import ch.cyberduck.core.transfer.TransferItem;
import ch.cyberduck.core.transfer.TransferStatus;
import ch.cyberduck.core.vault.DefaultVaultRegistry;
import ch.cyberduck.core.vault.VaultCredentials;
import ch.cyberduck.core.worker.CopyWorker;
import ch.cyberduck.core.worker.MoveWorker;
import ch.cyberduck.test.IntegrationTest;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.RandomUtils;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.util.Arrays;
import java.util.Collections;
import java.util.EnumSet;
import java.util.UUID;

import static org.junit.Assert.*;
import static org.junit.Assume.assumeTrue;

@Category(IntegrationTest.class)
@RunWith(value = Parameterized.class)
public class MoveWorkerTest extends AbstractDriveTest {

    @Test
    public void testMoveSameFolderCryptomator() throws Exception {
        final Path home = DriveHomeFinderService.MYDRIVE_FOLDER;
        final CryptoVault cryptomator = new CryptoVault(
            new Path(home, new AlphanumericRandomStringService().random(), EnumSet.of(Path.Type.directory)));
        final Path vault = cryptomator.create(session, new VaultCredentials("test"), vaultVersion);
        final Path source = new Path(vault, new AlphanumericRandomStringService().random(), EnumSet.of(Path.Type.file));
        final Path target = new Path(vault, new AlphanumericRandomStringService().random(), EnumSet.of(Path.Type.file));
        session.withRegistry(new DefaultVaultRegistry(new DisabledPasswordStore(), new DisabledPasswordCallback(), cryptomator));
        final byte[] content = RandomUtils.nextBytes(40500);
        final TransferStatus status = new TransferStatus();
        final DriveFileIdProvider fileid = new DriveFileIdProvider(session);
        new CryptoBulkFeature<>(session, new DisabledBulkFeature(), new DriveDeleteFeature(session, fileid), cryptomator).pre(Transfer.Type.upload, Collections.singletonMap(new TransferItem(source), status), new DisabledConnectionCallback());
        new StreamCopier(new TransferStatus(), new TransferStatus()).transfer(new ByteArrayInputStream(content), new CryptoWriteFeature<>(session, new DriveWriteFeature(session, fileid), cryptomator).write(source, status.withLength(content.length), new DisabledConnectionCallback()));
        assertTrue(cryptomator.getFeature(session, Find.class, new DefaultFindFeature(session)).find(source));
        final MoveWorker worker = new MoveWorker(Collections.singletonMap(source, target), new SessionPool.SingleSessionPool(session), PathCache.empty(), new DisabledProgressListener(), new DisabledLoginCallback());
        worker.run(session);
        assertFalse(cryptomator.getFeature(session, Find.class, new DefaultFindFeature(session)).find(source));
        assertTrue(cryptomator.getFeature(session, Find.class, new DefaultFindFeature(session)).find(target));
        final ByteArrayOutputStream out = new ByteArrayOutputStream(content.length);
        assertEquals(content.length, IOUtils.copy(new CryptoReadFeature(session, new DriveReadFeature(session, fileid), cryptomator).read(target, new TransferStatus().withLength(content.length), new DisabledConnectionCallback()), out));
        assertArrayEquals(content, out.toByteArray());
        cryptomator.getFeature(session, Delete.class, new DriveDeleteFeature(session, fileid)).delete(Arrays.asList(target, vault), new DisabledLoginCallback(), new Delete.DisabledCallback());
    }

    @Test
    public void testMoveToDifferentFolderCryptomator() throws Exception {
        final Path home = DriveHomeFinderService.MYDRIVE_FOLDER;
        final CryptoVault cryptomator = new CryptoVault(
            new Path(home, UUID.randomUUID().toString(), EnumSet.of(Path.Type.directory)));
        final Path vault = cryptomator.create(session, new VaultCredentials("test"), vaultVersion);
        final Path source = new Path(vault, UUID.randomUUID().toString(), EnumSet.of(Path.Type.file));
        session.withRegistry(new DefaultVaultRegistry(new DisabledPasswordStore(), new DisabledPasswordCallback(), cryptomator));
        final DriveFileIdProvider fileid = new DriveFileIdProvider(session);
        new CryptoTouchFeature<>(session, new DefaultTouchFeature<>(new DriveWriteFeature(session, fileid)), new DriveWriteFeature(session, fileid), cryptomator).touch(source, new TransferStatus());
        assertTrue(cryptomator.getFeature(session, Find.class, new DefaultFindFeature(session)).find(source));
        final Path targetFolder = cryptomator.getFeature(session, Directory.class, new DriveDirectoryFeature(session, fileid)).mkdir(
                new Path(vault, UUID.randomUUID().toString(), EnumSet.of(Path.Type.directory)), new TransferStatus());
        final Path target = new Path(targetFolder, UUID.randomUUID().toString(), EnumSet.of(Path.Type.file));
        assertTrue(cryptomator.getFeature(session, Find.class, new DefaultFindFeature(session)).find(targetFolder));
        final MoveWorker worker = new MoveWorker(Collections.singletonMap(source, target), new SessionPool.SingleSessionPool(session), PathCache.empty(), new DisabledProgressListener(), new DisabledLoginCallback());
        worker.run(session);
        assertFalse(cryptomator.getFeature(session, Find.class, new DefaultFindFeature(session)).find(source));
        assertTrue(cryptomator.getFeature(session, Find.class, new DefaultFindFeature(session)).find(target));
        cryptomator.getFeature(session, Delete.class, new DriveDeleteFeature(session, fileid)).delete(Arrays.asList(target, targetFolder, vault), new DisabledLoginCallback(), new Delete.DisabledCallback());
    }

    @Test
    public void testMoveToDifferentFolderLongFilenameCryptomator() throws Exception {
        assumeTrue(vaultVersion == CryptoVault.VAULT_VERSION_DEPRECATED);
        final Path home = DriveHomeFinderService.MYDRIVE_FOLDER;
        final CryptoVault cryptomator = new CryptoVault(
            new Path(home, new AlphanumericRandomStringService().random(), EnumSet.of(Path.Type.directory)));
        final Path vault = cryptomator.create(session, new VaultCredentials("test"), vaultVersion);
        final Path source = new Path(vault, new AlphanumericRandomStringService(130).random(), EnumSet.of(Path.Type.file));
        session.withRegistry(new DefaultVaultRegistry(new DisabledPasswordStore(), new DisabledPasswordCallback(), cryptomator));
        final DriveFileIdProvider fileid = new DriveFileIdProvider(session);
        new CryptoTouchFeature<>(session, new DefaultTouchFeature<>(new DriveWriteFeature(session, fileid)), new DriveWriteFeature(session, fileid), cryptomator).touch(source, new TransferStatus());
        assertTrue(cryptomator.getFeature(session, Find.class, new DefaultFindFeature(session)).find(source));
        final Path targetFolder = cryptomator.getFeature(session, Directory.class, new DriveDirectoryFeature(session, fileid)).mkdir(
                new Path(vault, new AlphanumericRandomStringService(130).random(), EnumSet.of(Path.Type.directory)), new TransferStatus());
        assertTrue(cryptomator.getFeature(session, Find.class, new DefaultFindFeature(session)).find(targetFolder));
        final Path target = new Path(targetFolder, new AlphanumericRandomStringService(130).random(), EnumSet.of(Path.Type.file));
        final MoveWorker worker = new MoveWorker(Collections.singletonMap(source, target), new SessionPool.SingleSessionPool(session), PathCache.empty(), new DisabledProgressListener(), new DisabledLoginCallback());
        worker.run(session);
        assertFalse(cryptomator.getFeature(session, Find.class, new DefaultFindFeature(session)).find(source));
        assertTrue(cryptomator.getFeature(session, Find.class, new DefaultFindFeature(session)).find(target));
        cryptomator.getFeature(session, Delete.class, new DriveDeleteFeature(session, fileid)).delete(Arrays.asList(target, targetFolder, vault), new DisabledLoginCallback(), new Delete.DisabledCallback());
    }

    @Test
    public void testMoveFolder() throws Exception {
        final Path home = DriveHomeFinderService.MYDRIVE_FOLDER;
        final CryptoVault cryptomator = new CryptoVault(
            new Path(home, new AlphanumericRandomStringService().random(), EnumSet.of(Path.Type.directory)));
        final Path vault = cryptomator.create(session, new VaultCredentials("test"), vaultVersion);
        session.withRegistry(new DefaultVaultRegistry(new DisabledPasswordStore(), new DisabledPasswordCallback(), cryptomator));
        final DriveFileIdProvider fileid = new DriveFileIdProvider(session);
        final Path folder = cryptomator.getFeature(session, Directory.class, new DriveDirectoryFeature(session, fileid)).mkdir(
                new Path(vault, new AlphanumericRandomStringService().random(), EnumSet.of(Path.Type.directory)), new TransferStatus());
        assertTrue(cryptomator.getFeature(session, Find.class, new DefaultFindFeature(session)).find(folder));
        final String filename = new AlphanumericRandomStringService().random();
        new CryptoTouchFeature<>(session, new DefaultTouchFeature<>(new DriveWriteFeature(session, fileid)), new DriveWriteFeature(session, fileid), cryptomator).touch(
                new Path(folder, filename, EnumSet.of(Path.Type.file)), new TransferStatus());
        assertTrue(cryptomator.getFeature(session, Find.class, new DefaultFindFeature(session)).find(
                new Path(folder, filename, EnumSet.of(Path.Type.file))));
        // rename file
        final String filenameRenamed = new AlphanumericRandomStringService().random();
        new MoveWorker(Collections.singletonMap(
                new Path(folder, filename, EnumSet.of(Path.Type.file)),
                new Path(folder, filenameRenamed, EnumSet.of(Path.Type.file))), new SessionPool.SingleSessionPool(session), PathCache.empty(), new DisabledProgressListener(), new DisabledLoginCallback()).run(session);
        assertFalse(cryptomator.getFeature(session, Find.class, new DefaultFindFeature(session)).find(
                new Path(folder, filename, EnumSet.of(Path.Type.file))));
        assertTrue(cryptomator.getFeature(session, Find.class, new DefaultFindFeature(session)).find(
                new Path(folder, filenameRenamed, EnumSet.of(Path.Type.file))));
        // rename folder
        final Path folderRenamed = new Path(vault, new AlphanumericRandomStringService().random(), EnumSet.of(Path.Type.directory));
        new MoveWorker(Collections.singletonMap(folder, folderRenamed), new SessionPool.SingleSessionPool(session), PathCache.empty(), new DisabledProgressListener(), new DisabledLoginCallback()).run(session);
        assertFalse(cryptomator.getFeature(session, Find.class, new DefaultFindFeature(session)).find(folder));
        assertTrue(cryptomator.getFeature(session, Find.class, new DefaultFindFeature(session)).find(folderRenamed));
        assertEquals(1, new CryptoListService(session, new DriveListService(session, fileid), cryptomator).list(folderRenamed, new DisabledListProgressListener()).size());
        final Path fileRenamedInRenamedFolder = new Path(folderRenamed, filenameRenamed, EnumSet.of(Path.Type.file));
        assertTrue(cryptomator.getFeature(session, Find.class, new DefaultFindFeature(session)).find(fileRenamedInRenamedFolder));
        cryptomator.getFeature(session, Delete.class, new DriveDeleteFeature(session, fileid)).delete(Arrays.asList(fileRenamedInRenamedFolder, folderRenamed, vault), new DisabledLoginCallback(), new Delete.DisabledCallback());
    }

    @Test
    public void testMoveFileIntoVault() throws Exception {
        final Path home = DriveHomeFinderService.MYDRIVE_FOLDER;
        final Path clearFile = new Path(home, new AlphanumericRandomStringService().random(), EnumSet.of(Path.Type.file));
        final DriveFileIdProvider fileid = new DriveFileIdProvider(session);
        new DriveTouchFeature(session, fileid).touch(clearFile, new TransferStatus());
        assertTrue(new DefaultFindFeature(session).find(clearFile));
        final CryptoVault cryptomator = new CryptoVault(
            new Path(home, new AlphanumericRandomStringService().random(), EnumSet.of(Path.Type.directory)));
        final Path vault = cryptomator.create(session, new VaultCredentials("test"), vaultVersion);
        final DefaultVaultRegistry registry = new DefaultVaultRegistry(new DisabledPasswordStore(), new DisabledPasswordCallback(), cryptomator);
        session.withRegistry(registry);
        final Path encryptedFolder = cryptomator.getFeature(session, Directory.class, new DriveDirectoryFeature(session, fileid)).mkdir(
                new Path(vault, new AlphanumericRandomStringService().random(), EnumSet.of(Path.Type.directory)), new TransferStatus());
        assertTrue(cryptomator.getFeature(session, Find.class, new DefaultFindFeature(session)).find(encryptedFolder));
        // move file into vault
        final Path encryptedFile = new Path(encryptedFolder, new AlphanumericRandomStringService().random(), EnumSet.of(Path.Type.file));
        final MoveWorker worker = new MoveWorker(Collections.singletonMap(clearFile, encryptedFile), new SessionPool.SingleSessionPool(session), PathCache.empty(), new DisabledProgressListener(), new DisabledLoginCallback());
        worker.run(session);
        assertTrue(cryptomator.getFeature(session, Find.class, new DefaultFindFeature(session)).find(encryptedFile));
        cryptomator.getFeature(session, Delete.class, new DriveDeleteFeature(session, fileid)).delete(Arrays.asList(encryptedFile, encryptedFolder, vault), new DisabledLoginCallback(), new Delete.DisabledCallback());
        registry.clear();
    }

    @Test
    public void testMoveDirectoryIntoVault() throws Exception {
        final Path home = DriveHomeFinderService.MYDRIVE_FOLDER;
        final Path clearFolder = new Path(home, new AlphanumericRandomStringService().random(), EnumSet.of(Path.Type.directory));
        final Path clearFile = new Path(clearFolder, new AlphanumericRandomStringService().random(), EnumSet.of(Path.Type.file));
        final DriveFileIdProvider fileid = new DriveFileIdProvider(session);
        new DriveDirectoryFeature(session, fileid).mkdir(clearFolder, new TransferStatus());
        new DriveTouchFeature(session, fileid).touch(clearFile, new TransferStatus());
        assertTrue(new DefaultFindFeature(session).find(clearFolder));
        assertTrue(new DefaultFindFeature(session).find(clearFile));
        final CryptoVault cryptomator = new CryptoVault(
                new Path(home, new AlphanumericRandomStringService().random(), EnumSet.of(Path.Type.directory)));
        final Path vault = cryptomator.create(session, new VaultCredentials("test"), vaultVersion);
        final DefaultVaultRegistry registry = new DefaultVaultRegistry(new DisabledPasswordStore(), new DisabledPasswordCallback(), cryptomator);
        session.withRegistry(registry);
        // move directory into vault
        final Path encryptedFolder = new Path(vault, clearFolder.getName(), EnumSet.of(Path.Type.directory));
        final Path encryptedFile = new Path(encryptedFolder, clearFile.getName(), EnumSet.of(Path.Type.file));
        final CopyWorker worker = new CopyWorker(Collections.singletonMap(clearFolder, encryptedFolder), new SessionPool.SingleSessionPool(session, registry), PathCache.empty(), new DisabledProgressListener(), new DisabledLoginCallback());
        worker.run(session);
        assertTrue(cryptomator.getFeature(session, Find.class, new DefaultFindFeature(session)).find(encryptedFolder));
        assertTrue(cryptomator.getFeature(session, Find.class, new DefaultFindFeature(session)).find(encryptedFile));
        cryptomator.getFeature(session, Delete.class, new DriveDeleteFeature(session, fileid)).delete(Arrays.asList(encryptedFile, encryptedFolder, vault), new DisabledLoginCallback(), new Delete.DisabledCallback());
        registry.clear();
    }

    @Test
    public void testMoveFileOutsideVault() throws Exception {
        final Path home = DriveHomeFinderService.MYDRIVE_FOLDER;
        final Path clearFolder = new Path(home, new AlphanumericRandomStringService().random(), EnumSet.of(Path.Type.directory));
        final DriveFileIdProvider fileid = new DriveFileIdProvider(session);
        new DriveDirectoryFeature(session, fileid).mkdir(clearFolder, new TransferStatus());
        final CryptoVault cryptomator = new CryptoVault(
            new Path(home, new AlphanumericRandomStringService().random(), EnumSet.of(Path.Type.directory)));
        final Path vault = cryptomator.create(session, new VaultCredentials("test"), vaultVersion);
        final DefaultVaultRegistry registry = new DefaultVaultRegistry(new DisabledPasswordStore(), new DisabledPasswordCallback(), cryptomator);
        session.withRegistry(registry);
        final Path encryptedFolder = cryptomator.getFeature(session, Directory.class, new DriveDirectoryFeature(session, fileid)).mkdir(
                new Path(vault, new AlphanumericRandomStringService().random(), EnumSet.of(Path.Type.directory)), new TransferStatus());
        assertTrue(cryptomator.getFeature(session, Find.class, new DefaultFindFeature(session)).find(encryptedFolder));
        final Path encryptedFile = new CryptoTouchFeature<>(session, new DefaultTouchFeature<>(new DriveWriteFeature(session, fileid)), new DriveWriteFeature(session, fileid), cryptomator).touch(
                new Path(encryptedFolder, new AlphanumericRandomStringService().random(), EnumSet.of(Path.Type.file)), new TransferStatus());
        assertTrue(cryptomator.getFeature(session, Find.class, new DefaultFindFeature(session)).find(encryptedFile));
        // move file outside vault
        final Path fileRenamed = new Path(clearFolder, new AlphanumericRandomStringService().random(), EnumSet.of(Path.Type.file));
        final MoveWorker worker = new MoveWorker(Collections.singletonMap(encryptedFile, fileRenamed), new SessionPool.SingleSessionPool(session, registry), PathCache.empty(), new DisabledProgressListener(), new DisabledLoginCallback());
        worker.run(session);
        assertTrue(new DefaultFindFeature(session).find(fileRenamed));
        cryptomator.getFeature(session, Delete.class, new DriveDeleteFeature(session, fileid)).delete(Arrays.asList(encryptedFolder, vault), new DisabledLoginCallback(), new Delete.DisabledCallback());
        new DriveDeleteFeature(session, fileid).delete(Arrays.asList(fileRenamed, clearFolder), new DisabledLoginCallback(), new Delete.DisabledCallback());
        registry.clear();
    }

    @Test
    public void testMoveDirectoryOutsideVault() throws Exception {
        final Path home = DriveHomeFinderService.MYDRIVE_FOLDER;
        final CryptoVault cryptomator = new CryptoVault(
            new Path(home, new AlphanumericRandomStringService().random(), EnumSet.of(Path.Type.directory)));
        final Path vault = cryptomator.create(session, new VaultCredentials("test"), vaultVersion);
        final DefaultVaultRegistry registry = new DefaultVaultRegistry(new DisabledPasswordStore(), new DisabledPasswordCallback(), cryptomator);
        session.withRegistry(registry);
        final DriveFileIdProvider fileid = new DriveFileIdProvider(session);
        final Path encryptedFolder = cryptomator.getFeature(session, Directory.class, new DriveDirectoryFeature(session, fileid)).mkdir(
                new Path(vault, new AlphanumericRandomStringService().random(), EnumSet.of(Path.Type.directory)), new TransferStatus());
        assertTrue(cryptomator.getFeature(session, Find.class, new DefaultFindFeature(session)).find(encryptedFolder));
        final String filename = new AlphanumericRandomStringService().random();
        final Path encryptedFile = new CryptoTouchFeature<>(session, new DefaultTouchFeature<>(new DriveWriteFeature(session, fileid)), new DriveWriteFeature(session, fileid), cryptomator).touch(
                new Path(encryptedFolder, filename, EnumSet.of(Path.Type.file)), new TransferStatus());
        assertTrue(cryptomator.getFeature(session, Find.class, new DefaultFindFeature(session)).find(encryptedFile));
        // move directory outside vault
        final Path directoryRenamed = new Path(home, new AlphanumericRandomStringService().random(), EnumSet.of(Path.Type.directory));
        final MoveWorker worker = new MoveWorker(Collections.singletonMap(encryptedFolder, directoryRenamed), new SessionPool.SingleSessionPool(session, registry), PathCache.empty(), new DisabledProgressListener(), new DisabledLoginCallback());
        worker.run(session);
        assertTrue(new DefaultFindFeature(session).find(directoryRenamed));
        final Path fileRenamed = new Path(directoryRenamed, filename, EnumSet.of(Path.Type.file));
        assertTrue(new DefaultFindFeature(session).find(fileRenamed));
        cryptomator.getFeature(session, Delete.class, new DriveDeleteFeature(session, fileid)).delete(Collections.singletonList(vault), new DisabledLoginCallback(), new Delete.DisabledCallback());
        new DriveDeleteFeature(session, fileid).delete(Arrays.asList(fileRenamed, directoryRenamed), new DisabledLoginCallback(), new Delete.DisabledCallback());
        registry.clear();
    }
}
