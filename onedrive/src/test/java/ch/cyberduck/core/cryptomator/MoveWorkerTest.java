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
import ch.cyberduck.core.DisabledLoginCallback;
import ch.cyberduck.core.DisabledPasswordCallback;
import ch.cyberduck.core.DisabledPasswordStore;
import ch.cyberduck.core.DisabledProgressListener;
import ch.cyberduck.core.Path;
import ch.cyberduck.core.PathCache;
import ch.cyberduck.core.cryptomator.features.CryptoAttributesFeature;
import ch.cyberduck.core.cryptomator.features.CryptoDeleteFeature;
import ch.cyberduck.core.cryptomator.features.CryptoDirectoryFeature;
import ch.cyberduck.core.cryptomator.features.CryptoFindFeature;
import ch.cyberduck.core.cryptomator.features.CryptoTouchFeature;
import ch.cyberduck.core.features.Delete;
import ch.cyberduck.core.onedrive.AbstractOneDriveTest;
import ch.cyberduck.core.onedrive.OneDriveAttributesFinderFeature;
import ch.cyberduck.core.onedrive.OneDriveDeleteFeature;
import ch.cyberduck.core.onedrive.OneDriveDirectoryFeature;
import ch.cyberduck.core.onedrive.OneDriveHomeFinderFeature;
import ch.cyberduck.core.onedrive.OneDriveWriteFeature;
import ch.cyberduck.core.shared.DefaultFindFeature;
import ch.cyberduck.core.shared.DefaultTouchFeature;
import ch.cyberduck.core.shared.DefaultUploadFeature;
import ch.cyberduck.core.transfer.TransferStatus;
import ch.cyberduck.core.vault.DefaultVaultRegistry;
import ch.cyberduck.core.vault.VaultCredentials;
import ch.cyberduck.core.worker.MoveWorker;
import ch.cyberduck.test.IntegrationTest;

import org.apache.commons.text.RandomStringGenerator;
import org.junit.Test;
import org.junit.experimental.categories.Category;

import java.util.Arrays;
import java.util.Collections;
import java.util.EnumSet;

import static org.junit.Assert.*;

@Category(IntegrationTest.class)
public class MoveWorkerTest extends AbstractOneDriveTest {

    @Test
    public void testMoveSameFolderCryptomator() throws Exception {
        final Path home = new OneDriveHomeFinderFeature(session).find();
        final Path vault = new Path(home, new AlphanumericRandomStringService().random(), EnumSet.of(Path.Type.directory));
        final Path source = new Path(vault, new AlphanumericRandomStringService().random(), EnumSet.of(Path.Type.file));
        final Path target = new Path(vault, new AlphanumericRandomStringService().random(), EnumSet.of(Path.Type.file));
        final CryptoVault cryptomator = new CryptoVault(vault, new DisabledPasswordStore());
        cryptomator.create(session, null, new VaultCredentials("test"));
        session.withRegistry(new DefaultVaultRegistry(new DisabledPasswordStore(), new DisabledPasswordCallback(), cryptomator));
        new CryptoTouchFeature<>(session, new DefaultTouchFeature<Void>(new DefaultUploadFeature<>(new OneDriveWriteFeature(session))), new OneDriveWriteFeature(session), cryptomator).touch(source, new TransferStatus());
        assertTrue(new CryptoFindFeature(session, new DefaultFindFeature(session), cryptomator).find(source));
        final MoveWorker worker = new MoveWorker(Collections.singletonMap(source, target), new DisabledProgressListener(), PathCache.empty(), new DisabledConnectionCallback());
        worker.run(session);
        assertFalse(new CryptoFindFeature(session, new DefaultFindFeature(session), cryptomator).find(source));
        assertTrue(new CryptoFindFeature(session, new DefaultFindFeature(session), cryptomator).find(target));
        new CryptoDeleteFeature(session, new OneDriveDeleteFeature(session), cryptomator).delete(Arrays.asList(target, vault), new DisabledLoginCallback(), new Delete.DisabledCallback());
    }

    @Test
    public void testMoveToDifferentFolderCryptomator() throws Exception {
        final Path home = new OneDriveHomeFinderFeature(session).find();
        final Path vault = new Path(home, new AlphanumericRandomStringService().random(), EnumSet.of(Path.Type.directory));
        final Path source = new Path(vault, new AlphanumericRandomStringService().random(), EnumSet.of(Path.Type.file));
        final Path targetFolder = new Path(vault, new AlphanumericRandomStringService().random(), EnumSet.of(Path.Type.directory));
        final Path target = new Path(targetFolder, new AlphanumericRandomStringService().random(), EnumSet.of(Path.Type.file));
        final CryptoVault cryptomator = new CryptoVault(vault, new DisabledPasswordStore());
        cryptomator.create(session, null, new VaultCredentials("test"));
        session.withRegistry(new DefaultVaultRegistry(new DisabledPasswordStore(), new DisabledPasswordCallback(), cryptomator));
        new CryptoTouchFeature<>(session, new DefaultTouchFeature<Void>(new DefaultUploadFeature<>(new OneDriveWriteFeature(session))), new OneDriveWriteFeature(session), cryptomator).touch(source, new TransferStatus());
        assertTrue(new CryptoFindFeature(session, new DefaultFindFeature(session), cryptomator).find(source));
        new CryptoDirectoryFeature<>(session, new OneDriveDirectoryFeature(session), new OneDriveWriteFeature(session), cryptomator).mkdir(targetFolder, null, new TransferStatus());
        assertTrue(new CryptoFindFeature(session, new DefaultFindFeature(session), cryptomator).find(targetFolder));
        final MoveWorker worker = new MoveWorker(Collections.singletonMap(source, target), new DisabledProgressListener(), PathCache.empty(), new DisabledConnectionCallback());
        worker.run(session);
        assertFalse(new CryptoFindFeature(session, new DefaultFindFeature(session), cryptomator).find(source));
        assertTrue(new CryptoFindFeature(session, new DefaultFindFeature(session), cryptomator).find(target));
        new CryptoDeleteFeature(session, new OneDriveDeleteFeature(session), cryptomator).delete(Arrays.asList(target, targetFolder, vault), new DisabledLoginCallback(), new Delete.DisabledCallback());
    }

    @Test
    public void testMoveToDifferentFolderLongFilenameCryptomator() throws Exception {
        final Path home = new OneDriveHomeFinderFeature(session).find();
        final Path vault = new Path(home, new AlphanumericRandomStringService().random(), EnumSet.of(Path.Type.directory));
        final Path source = new Path(vault, new RandomStringGenerator.Builder().build().generate(130), EnumSet.of(Path.Type.file));
        final Path targetFolder = new Path(vault, new RandomStringGenerator.Builder().build().generate(130), EnumSet.of(Path.Type.directory));
        final Path target = new Path(targetFolder, new RandomStringGenerator.Builder().build().generate(130), EnumSet.of(Path.Type.file));
        final CryptoVault cryptomator = new CryptoVault(vault, new DisabledPasswordStore());
        cryptomator.create(session, null, new VaultCredentials("test"));
        session.withRegistry(new DefaultVaultRegistry(new DisabledPasswordStore(), new DisabledPasswordCallback(), cryptomator));
        new CryptoTouchFeature<>(session, new DefaultTouchFeature<Void>(new DefaultUploadFeature<>(new OneDriveWriteFeature(session))), new OneDriveWriteFeature(session), cryptomator).touch(source, new TransferStatus());
        assertTrue(new CryptoFindFeature(session, new DefaultFindFeature(session), cryptomator).find(source));
        new CryptoDirectoryFeature<>(session, new OneDriveDirectoryFeature(session), new OneDriveWriteFeature(session), cryptomator).mkdir(targetFolder, null, new TransferStatus());
        assertTrue(new CryptoFindFeature(session, new DefaultFindFeature(session), cryptomator).find(targetFolder));
        final MoveWorker worker = new MoveWorker(Collections.singletonMap(source, target), new DisabledProgressListener(), PathCache.empty(), new DisabledConnectionCallback());
        worker.run(session);
        assertFalse(new CryptoFindFeature(session, new DefaultFindFeature(session), cryptomator).find(source));
        assertTrue(new CryptoFindFeature(session, new DefaultFindFeature(session), cryptomator).find(target));
        new CryptoDeleteFeature(session, new OneDriveDeleteFeature(session), cryptomator).delete(Arrays.asList(target, targetFolder, vault), new DisabledLoginCallback(), new Delete.DisabledCallback());
    }

    @Test
    public void testMoveFolder() throws Exception {
        final Path home = new OneDriveHomeFinderFeature(session).find();
        final Path vault = new Path(home, new AlphanumericRandomStringService().random(), EnumSet.of(Path.Type.directory));
        final Path folder = new Path(vault, new AlphanumericRandomStringService().random(), EnumSet.of(Path.Type.directory));
        final Path file = new Path(folder, new AlphanumericRandomStringService().random(), EnumSet.of(Path.Type.file));
        final CryptoVault cryptomator = new CryptoVault(vault, new DisabledPasswordStore());
        cryptomator.create(session, null, new VaultCredentials("test"));
        session.withRegistry(new DefaultVaultRegistry(new DisabledPasswordStore(), new DisabledPasswordCallback(), cryptomator));
        new CryptoDirectoryFeature<>(session, new OneDriveDirectoryFeature(session), new OneDriveWriteFeature(session), cryptomator).mkdir(folder, null, new TransferStatus());
        assertTrue(new CryptoFindFeature(session, new DefaultFindFeature(session), cryptomator).find(folder));
        new CryptoTouchFeature<>(session, new DefaultTouchFeature<Void>(new DefaultUploadFeature<>(new OneDriveWriteFeature(session))), new OneDriveWriteFeature(session), cryptomator).touch(file, new TransferStatus());
        assertTrue(new CryptoFindFeature(session, new DefaultFindFeature(session), cryptomator).find(file));
        // rename file
        final Path fileRenamed = new Path(folder, "f1", EnumSet.of(Path.Type.file));
        new MoveWorker(Collections.singletonMap(file, fileRenamed), new DisabledProgressListener(), PathCache.empty(), new DisabledConnectionCallback()).run(session);
        assertFalse(new CryptoFindFeature(session, new DefaultFindFeature(session), cryptomator).find(file));
        assertTrue(new CryptoFindFeature(session, new DefaultFindFeature(session), cryptomator).find(fileRenamed));
        // rename folder
        final Path folderRenamed = new Path(vault, new AlphanumericRandomStringService().random(), EnumSet.of(Path.Type.directory));
        new MoveWorker(Collections.singletonMap(folder, folderRenamed), new DisabledProgressListener(), PathCache.empty(), new DisabledConnectionCallback()).run(session);
        assertFalse(new CryptoFindFeature(session, new DefaultFindFeature(session), cryptomator).find(folder));
        assertTrue(new CryptoFindFeature(session, new DefaultFindFeature(session), cryptomator).find(folderRenamed));
        final Path fileRenamedInRenamedFolder = new Path(folderRenamed, "f1", EnumSet.of(Path.Type.file));
        assertTrue(new CryptoFindFeature(session, new DefaultFindFeature(session), cryptomator).find(fileRenamedInRenamedFolder));
        new CryptoDeleteFeature(session, new OneDriveDeleteFeature(session), cryptomator).delete(Arrays.asList(fileRenamedInRenamedFolder, folderRenamed, vault), new DisabledLoginCallback(), new Delete.DisabledCallback());
    }

    @Test
    public void testMoveFileIntoVault() throws Exception {
        final Path home = new OneDriveHomeFinderFeature(session).find();
        final Path vault = new Path(home, new AlphanumericRandomStringService().random(), EnumSet.of(Path.Type.directory));
        final Path clearFile = new Path(home, new AlphanumericRandomStringService().random(), EnumSet.of(Path.Type.file));
        new DefaultTouchFeature<Void>(new DefaultUploadFeature<>(new OneDriveWriteFeature(session))).touch(clearFile, new TransferStatus());
        assertTrue(new DefaultFindFeature(session).find(clearFile));
        final Path encryptedFolder = new Path(vault, new AlphanumericRandomStringService().random(), EnumSet.of(Path.Type.directory));
        final Path encryptedFile = new Path(encryptedFolder, new AlphanumericRandomStringService().random(), EnumSet.of(Path.Type.file));
        final CryptoVault cryptomator = new CryptoVault(vault, new DisabledPasswordStore());
        cryptomator.create(session, null, new VaultCredentials("test"));
        final DefaultVaultRegistry registry = new DefaultVaultRegistry(new DisabledPasswordStore(), new DisabledPasswordCallback(), cryptomator);
        session.withRegistry(registry);
        new CryptoDirectoryFeature<>(session, new OneDriveDirectoryFeature(session), new OneDriveWriteFeature(session), cryptomator).mkdir(encryptedFolder, null, new TransferStatus());
        assertTrue(new CryptoFindFeature(session, new DefaultFindFeature(session), cryptomator).find(encryptedFolder));
        // move file into vault
        final MoveWorker worker = new MoveWorker(Collections.singletonMap(clearFile, encryptedFile), new DisabledProgressListener(), PathCache.empty(), new DisabledConnectionCallback());
        worker.run(session);
        assertFalse(new DefaultFindFeature(session).find(clearFile));
        assertTrue(new CryptoFindFeature(session, new DefaultFindFeature(session), cryptomator).find(encryptedFile));
        new CryptoDeleteFeature(session, new OneDriveDeleteFeature(session), cryptomator).delete(Arrays.asList(encryptedFile, encryptedFolder, vault), new DisabledLoginCallback(), new Delete.DisabledCallback());
        registry.clear();
    }

    @Test
    public void testMoveDirectoryIntoVault() throws Exception {
        final Path home = new OneDriveHomeFinderFeature(session).find();
        final Path vault = new Path(home, new AlphanumericRandomStringService().random(), EnumSet.of(Path.Type.directory));
        final Path clearFolder = new Path(home, new AlphanumericRandomStringService().random(), EnumSet.of(Path.Type.directory));
        final Path clearFile = new Path(clearFolder, new AlphanumericRandomStringService().random(), EnumSet.of(Path.Type.file));
        new OneDriveDirectoryFeature(session).mkdir(clearFolder, null, new TransferStatus());
        new DefaultTouchFeature<Void>(new DefaultUploadFeature<>(new OneDriveWriteFeature(session))).touch(clearFile, new TransferStatus());
        assertTrue(new DefaultFindFeature(session).find(clearFolder));
        assertTrue(new DefaultFindFeature(session).find(clearFile));
        final CryptoVault cryptomator = new CryptoVault(vault, new DisabledPasswordStore());
        cryptomator.create(session, null, new VaultCredentials("test"));
        final DefaultVaultRegistry registry = new DefaultVaultRegistry(new DisabledPasswordStore(), new DisabledPasswordCallback(), cryptomator);
        session.withRegistry(registry);
        // move directory into vault
        final Path encryptedFolder = new Path(vault, clearFolder.getName(), EnumSet.of(Path.Type.directory));
        final Path encryptedFile = new Path(encryptedFolder, clearFile.getName(), EnumSet.of(Path.Type.file));
        final MoveWorker worker = new MoveWorker(Collections.singletonMap(clearFolder, encryptedFolder), new DisabledProgressListener(), PathCache.empty(), new DisabledConnectionCallback());
        worker.run(session);
        assertTrue(new CryptoFindFeature(session, new DefaultFindFeature(session), cryptomator).find(encryptedFolder));
        assertTrue(new CryptoFindFeature(session, new DefaultFindFeature(session), cryptomator).find(encryptedFile));
        assertFalse(new DefaultFindFeature(session).find(clearFolder));
        assertFalse(new DefaultFindFeature(session).find(clearFile));
        new CryptoDeleteFeature(session, new OneDriveDeleteFeature(session), cryptomator).delete(Arrays.asList(encryptedFile, encryptedFolder, vault), new DisabledLoginCallback(), new Delete.DisabledCallback());
        registry.clear();
    }

    @Test
    public void testMoveFileOutsideVault() throws Exception {
        final Path home = new OneDriveHomeFinderFeature(session).find();
        final Path vault = new Path(home, new AlphanumericRandomStringService().random(), EnumSet.of(Path.Type.directory));
        final Path clearFolder = new Path(home, new AlphanumericRandomStringService().random(), EnumSet.of(Path.Type.directory));
        new OneDriveDirectoryFeature(session).mkdir(clearFolder, null, new TransferStatus());
        final Path encryptedFolder = new Path(vault, new AlphanumericRandomStringService().random(), EnumSet.of(Path.Type.directory));
        final Path encryptedFile = new Path(encryptedFolder, new AlphanumericRandomStringService().random(), EnumSet.of(Path.Type.file));
        final CryptoVault cryptomator = new CryptoVault(vault, new DisabledPasswordStore());
        cryptomator.create(session, null, new VaultCredentials("test"));
        final DefaultVaultRegistry registry = new DefaultVaultRegistry(new DisabledPasswordStore(), new DisabledPasswordCallback(), cryptomator);
        session.withRegistry(registry);
        new CryptoDirectoryFeature<>(session, new OneDriveDirectoryFeature(session), new OneDriveWriteFeature(session), cryptomator).mkdir(encryptedFolder, null, new TransferStatus());
        assertTrue(new CryptoFindFeature(session, new DefaultFindFeature(session), cryptomator).find(encryptedFolder));
        new CryptoTouchFeature<>(session, new DefaultTouchFeature<Void>(new DefaultUploadFeature<>(new OneDriveWriteFeature(session))), new OneDriveWriteFeature(session), cryptomator).touch(encryptedFile, new TransferStatus());
        assertTrue(new CryptoFindFeature(session, new DefaultFindFeature(session), cryptomator).find(encryptedFile));
        assertEquals(0L, new CryptoAttributesFeature(session, new OneDriveAttributesFinderFeature(session), cryptomator).find(encryptedFile).getSize());
        // move file outside vault
        final Path fileRenamed = new Path(clearFolder, new AlphanumericRandomStringService().random(), EnumSet.of(Path.Type.file));
        final MoveWorker worker = new MoveWorker(Collections.singletonMap(encryptedFile, fileRenamed), new DisabledProgressListener(), PathCache.empty(), new DisabledConnectionCallback());
        worker.run(session);
        assertFalse(new CryptoFindFeature(session, new DefaultFindFeature(session), cryptomator).find(encryptedFile));
        assertTrue(new DefaultFindFeature(session).find(fileRenamed));
        new CryptoDeleteFeature(session, new OneDriveDeleteFeature(session), cryptomator).delete(Collections.singletonList(encryptedFolder), new DisabledLoginCallback(), new Delete.DisabledCallback());
        new OneDriveDeleteFeature(session).delete(Arrays.asList(fileRenamed, clearFolder), new DisabledLoginCallback(), new Delete.DisabledCallback());
        registry.clear();
    }

    @Test
    public void testMoveDirectoryOutsideVault() throws Exception {
        final Path home = new OneDriveHomeFinderFeature(session).find();
        final Path vault = new Path(home, new AlphanumericRandomStringService().random(), EnumSet.of(Path.Type.directory));
        final Path encryptedFolder = new Path(vault, new AlphanumericRandomStringService().random(), EnumSet.of(Path.Type.directory));
        final Path encryptedFile = new Path(encryptedFolder, new AlphanumericRandomStringService().random(), EnumSet.of(Path.Type.file));
        final CryptoVault cryptomator = new CryptoVault(vault, new DisabledPasswordStore());
        cryptomator.create(session, null, new VaultCredentials("test"));
        final DefaultVaultRegistry registry = new DefaultVaultRegistry(new DisabledPasswordStore(), new DisabledPasswordCallback(), cryptomator);
        session.withRegistry(registry);
        new CryptoDirectoryFeature<>(session, new OneDriveDirectoryFeature(session), new OneDriveWriteFeature(session), cryptomator).mkdir(encryptedFolder, null, new TransferStatus());
        assertTrue(new CryptoFindFeature(session, new DefaultFindFeature(session), cryptomator).find(encryptedFolder));
        new CryptoTouchFeature<>(session, new DefaultTouchFeature<Void>(new DefaultUploadFeature<>(new OneDriveWriteFeature(session))), new OneDriveWriteFeature(session), cryptomator).touch(encryptedFile, new TransferStatus());
        assertTrue(new CryptoFindFeature(session, new DefaultFindFeature(session), cryptomator).find(encryptedFile));
        // move directory outside vault
        final Path directoryRenamed = new Path(home, new AlphanumericRandomStringService().random(), EnumSet.of(Path.Type.directory));
        final MoveWorker worker = new MoveWorker(Collections.singletonMap(encryptedFolder, directoryRenamed), new DisabledProgressListener(), PathCache.empty(), new DisabledConnectionCallback());
        worker.run(session);
        assertFalse(new CryptoFindFeature(session, new DefaultFindFeature(session), cryptomator).find(encryptedFolder));
        assertFalse(new CryptoFindFeature(session, new DefaultFindFeature(session), cryptomator).find(encryptedFile));
        assertTrue(new DefaultFindFeature(session).find(directoryRenamed));
        final Path fileRenamed = new Path(directoryRenamed, encryptedFile.getName(), EnumSet.of(Path.Type.file));
        assertTrue(new DefaultFindFeature(session).find(fileRenamed));
        new OneDriveDeleteFeature(session).delete(Arrays.asList(fileRenamed, directoryRenamed), new DisabledLoginCallback(), new Delete.DisabledCallback());
        registry.clear();
    }
}
