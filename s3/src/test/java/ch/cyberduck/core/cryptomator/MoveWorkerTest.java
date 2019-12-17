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
import ch.cyberduck.core.cryptomator.features.CryptoDeleteFeature;
import ch.cyberduck.core.cryptomator.features.CryptoDirectoryFeature;
import ch.cyberduck.core.cryptomator.features.CryptoFindFeature;
import ch.cyberduck.core.cryptomator.features.CryptoListService;
import ch.cyberduck.core.cryptomator.features.CryptoReadFeature;
import ch.cyberduck.core.cryptomator.features.CryptoTouchFeature;
import ch.cyberduck.core.cryptomator.features.CryptoWriteFeature;
import ch.cyberduck.core.exception.NotfoundException;
import ch.cyberduck.core.features.Delete;
import ch.cyberduck.core.io.StreamCopier;
import ch.cyberduck.core.pool.SessionPool;
import ch.cyberduck.core.s3.AbstractS3Test;
import ch.cyberduck.core.s3.S3DefaultDeleteFeature;
import ch.cyberduck.core.s3.S3DirectoryFeature;
import ch.cyberduck.core.s3.S3FindFeature;
import ch.cyberduck.core.s3.S3ListService;
import ch.cyberduck.core.s3.S3MultipartWriteFeature;
import ch.cyberduck.core.s3.S3ReadFeature;
import ch.cyberduck.core.s3.S3TouchFeature;
import ch.cyberduck.core.s3.S3WriteFeature;
import ch.cyberduck.core.shared.DefaultFindFeature;
import ch.cyberduck.core.shared.DisabledBulkFeature;
import ch.cyberduck.core.transfer.Transfer;
import ch.cyberduck.core.transfer.TransferItem;
import ch.cyberduck.core.transfer.TransferStatus;
import ch.cyberduck.core.vault.DefaultVaultRegistry;
import ch.cyberduck.core.vault.VaultCredentials;
import ch.cyberduck.core.worker.MoveWorker;
import ch.cyberduck.test.IntegrationTest;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.RandomUtils;
import org.apache.commons.text.RandomStringGenerator;
import org.junit.Test;
import org.junit.experimental.categories.Category;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.util.Arrays;
import java.util.Collections;
import java.util.EnumSet;
import java.util.UUID;

import static org.junit.Assert.*;

@Category(IntegrationTest.class)
public class MoveWorkerTest extends AbstractS3Test {

    @Test
    public void testMoveSameFolderCryptomator() throws Exception {
        final Path home = new Path("test-us-east-1-cyberduck", EnumSet.of(Path.Type.directory, Path.Type.volume));
        final Path vault = new Path(home, UUID.randomUUID().toString(), EnumSet.of(Path.Type.directory));
        final Path source = new Path(vault, UUID.randomUUID().toString(), EnumSet.of(Path.Type.file));
        final Path target = new Path(vault, UUID.randomUUID().toString(), EnumSet.of(Path.Type.file));
        final CryptoVault cryptomator = new CryptoVault(vault);
        cryptomator.create(session, null, new VaultCredentials("test"), new DisabledPasswordStore());
        session.withRegistry(new DefaultVaultRegistry(new DisabledPasswordStore(), new DisabledPasswordCallback(), cryptomator));
        final byte[] content = RandomUtils.nextBytes(40500);
        final TransferStatus status = new TransferStatus();
        new CryptoBulkFeature<>(session, new DisabledBulkFeature(), new S3DefaultDeleteFeature(session), cryptomator).pre(Transfer.Type.upload, Collections.singletonMap(new TransferItem(source), status), new DisabledConnectionCallback());
        new StreamCopier(new TransferStatus(), new TransferStatus()).transfer(new ByteArrayInputStream(content), new CryptoWriteFeature<>(session, new S3MultipartWriteFeature(session), cryptomator).write(source, status.length(content.length), new DisabledConnectionCallback()));
        assertTrue(new CryptoFindFeature(session, new DefaultFindFeature(session), cryptomator).find(source));
        final MoveWorker worker = new MoveWorker(Collections.singletonMap(source, target), new SessionPool.SingleSessionPool(session), PathCache.empty(), new DisabledProgressListener(), new DisabledLoginCallback());
        worker.run(session);
        assertFalse(new CryptoFindFeature(session, new DefaultFindFeature(session), cryptomator).find(source));
        assertTrue(new CryptoFindFeature(session, new DefaultFindFeature(session), cryptomator).find(target));
        final ByteArrayOutputStream out = new ByteArrayOutputStream(content.length);
        assertEquals(content.length, IOUtils.copy(new CryptoReadFeature(session, new S3ReadFeature(session), cryptomator).read(target, new TransferStatus().length(content.length), new DisabledConnectionCallback()), out));
        assertArrayEquals(content, out.toByteArray());
        new CryptoDeleteFeature(session, new S3DefaultDeleteFeature(session), cryptomator).delete(Arrays.asList(target, vault), new DisabledLoginCallback(), new Delete.DisabledCallback());
        session.close();
    }

    @Test
    public void testMoveToDifferentFolderCryptomator() throws Exception {
        final Path home = new Path("test-us-east-1-cyberduck", EnumSet.of(Path.Type.directory, Path.Type.volume));
        final Path vault = new Path(home, UUID.randomUUID().toString(), EnumSet.of(Path.Type.directory));
        final Path source = new Path(vault, UUID.randomUUID().toString(), EnumSet.of(Path.Type.file));
        final CryptoVault cryptomator = new CryptoVault(vault);
        cryptomator.create(session, null, new VaultCredentials("test"), new DisabledPasswordStore());
        session.withRegistry(new DefaultVaultRegistry(new DisabledPasswordStore(), new DisabledPasswordCallback(), cryptomator));
        new CryptoTouchFeature<>(session, new S3TouchFeature(session), new S3WriteFeature(session), cryptomator).touch(source, new TransferStatus());
        assertTrue(new CryptoFindFeature(session, new DefaultFindFeature(session), cryptomator).find(source));
        final Path targetFolder = new CryptoDirectoryFeature<>(session, new S3DirectoryFeature(session, new S3WriteFeature(session)), new S3WriteFeature(session), cryptomator).mkdir(
            new Path(vault, UUID.randomUUID().toString(), EnumSet.of(Path.Type.directory)), null, new TransferStatus());
        assertTrue(new CryptoFindFeature(session, new DefaultFindFeature(session), cryptomator).find(targetFolder));
        final Path target = new Path(targetFolder, UUID.randomUUID().toString(), EnumSet.of(Path.Type.file));
        final MoveWorker worker = new MoveWorker(Collections.singletonMap(source, target), new SessionPool.SingleSessionPool(session), PathCache.empty(), new DisabledProgressListener(), new DisabledLoginCallback());
        worker.run(session);
        assertFalse(new CryptoFindFeature(session, new DefaultFindFeature(session), cryptomator).find(source));
        assertTrue(new CryptoFindFeature(session, new DefaultFindFeature(session), cryptomator).find(target));
        new CryptoDeleteFeature(session, new S3DefaultDeleteFeature(session), cryptomator).delete(Arrays.asList(target, targetFolder, vault), new DisabledLoginCallback(), new Delete.DisabledCallback());
        session.close();
    }

    @Test
    public void testMoveToDifferentFolderLongFilenameCryptomator() throws Exception {
        final Path home = new Path("test-us-east-1-cyberduck", EnumSet.of(Path.Type.directory, Path.Type.volume));
        final Path vault = new Path(home, UUID.randomUUID().toString(), EnumSet.of(Path.Type.directory));
        final Path source = new Path(vault, new RandomStringGenerator.Builder().build().generate(130), EnumSet.of(Path.Type.file));
        final CryptoVault cryptomator = new CryptoVault(vault);
        cryptomator.create(session, null, new VaultCredentials("test"), new DisabledPasswordStore());
        session.withRegistry(new DefaultVaultRegistry(new DisabledPasswordStore(), new DisabledPasswordCallback(), cryptomator));
        new CryptoTouchFeature<>(session, new S3TouchFeature(session), new S3WriteFeature(session), cryptomator).touch(source, new TransferStatus());
        assertTrue(new CryptoFindFeature(session, new DefaultFindFeature(session), cryptomator).find(source));
        final Path targetFolder = new CryptoDirectoryFeature<>(session, new S3DirectoryFeature(session, new S3WriteFeature(session)), new S3WriteFeature(session), cryptomator).mkdir(
            new Path(vault, new RandomStringGenerator.Builder().build().generate(130), EnumSet.of(Path.Type.directory)), null, new TransferStatus());
        assertTrue(new CryptoFindFeature(session, new DefaultFindFeature(session), cryptomator).find(targetFolder));
        final Path target = new Path(targetFolder, new RandomStringGenerator.Builder().build().generate(130), EnumSet.of(Path.Type.file));
        final MoveWorker worker = new MoveWorker(Collections.singletonMap(source, target), new SessionPool.SingleSessionPool(session), PathCache.empty(), new DisabledProgressListener(), new DisabledLoginCallback());
        worker.run(session);
        assertFalse(new CryptoFindFeature(session, new DefaultFindFeature(session), cryptomator).find(source));
        assertTrue(new CryptoFindFeature(session, new DefaultFindFeature(session), cryptomator).find(target));
        new CryptoDeleteFeature(session, new S3DefaultDeleteFeature(session), cryptomator).delete(Arrays.asList(target, vault), new DisabledLoginCallback(), new Delete.DisabledCallback());
        session.close();
    }

    @Test
    public void testMoveFolder() throws Exception {
        final Path home = new Path("test-us-east-1-cyberduck", EnumSet.of(Path.Type.directory, Path.Type.volume));
        final Path vault = new Path(home, new AlphanumericRandomStringService().random(), EnumSet.of(Path.Type.directory));
        final CryptoVault cryptomator = new CryptoVault(vault);
        cryptomator.create(session, null, new VaultCredentials("test"), new DisabledPasswordStore());
        session.withRegistry(new DefaultVaultRegistry(new DisabledPasswordStore(), new DisabledPasswordCallback(), cryptomator));
        final Path folder = new CryptoDirectoryFeature<>(session, new S3DirectoryFeature(session, new S3WriteFeature(session)), new S3WriteFeature(session), cryptomator).mkdir(
            new Path(vault, new AlphanumericRandomStringService().random(), EnumSet.of(Path.Type.directory)), null, new TransferStatus());
        assertTrue(new CryptoFindFeature(session, new DefaultFindFeature(session), cryptomator).find(folder));
        final Path file = new Path(folder, new AlphanumericRandomStringService().random(), EnumSet.of(Path.Type.file));
        new CryptoTouchFeature<>(session, new S3TouchFeature(session), new S3WriteFeature(session), cryptomator).touch(file, new TransferStatus());
        assertTrue(new CryptoFindFeature(session, new DefaultFindFeature(session), cryptomator).find(file));
        // rename file
        final Path fileRenamed = new Path(folder, "f1", EnumSet.of(Path.Type.file));
        new MoveWorker(Collections.singletonMap(file, fileRenamed), new SessionPool.SingleSessionPool(session), PathCache.empty(), new DisabledProgressListener(), new DisabledLoginCallback()).run(session);
        assertFalse(new CryptoFindFeature(session, new S3FindFeature(session), cryptomator).find(file));
        assertTrue(new CryptoFindFeature(session, new S3FindFeature(session), cryptomator).find(fileRenamed));
        // rename folder
        final Path folderRenamed = new Path(vault, new AlphanumericRandomStringService().random(), EnumSet.of(Path.Type.directory));
        new MoveWorker(Collections.singletonMap(folder, folderRenamed), new SessionPool.SingleSessionPool(session), PathCache.empty(), new DisabledProgressListener(), new DisabledLoginCallback()).run(session);
        assertFalse(new CryptoFindFeature(session, new S3FindFeature(session), cryptomator).find(folder));
        assertTrue(new CryptoFindFeature(session, new S3FindFeature(session), cryptomator).find(folderRenamed));
        try {
            new CryptoListService(session, new S3ListService(session), cryptomator).list(folder, new DisabledListProgressListener());
            fail();
        }
        catch(NotfoundException e) {
            //
        }
        assertEquals(1, new CryptoListService(session, new S3ListService(session), cryptomator).list(folderRenamed, new DisabledListProgressListener()).size());
        final Path fileRenamedInRenamedFolder = new Path(folderRenamed, "f1", EnumSet.of(Path.Type.file));
        assertTrue(new CryptoFindFeature(session, new DefaultFindFeature(session), cryptomator).find(fileRenamedInRenamedFolder));
        new CryptoDeleteFeature(session, new S3DefaultDeleteFeature(session), cryptomator).delete(Arrays.asList(fileRenamedInRenamedFolder, folderRenamed, vault), new DisabledLoginCallback(), new Delete.DisabledCallback());
        session.close();
    }

    @Test
    public void testMoveFileIntoVault() throws Exception {
        final Path home = new Path("test-us-east-1-cyberduck", EnumSet.of(Path.Type.directory, Path.Type.volume));
        final Path vault = new Path(home, new AlphanumericRandomStringService().random(), EnumSet.of(Path.Type.directory));
        final Path clearFile = new Path(home, new AlphanumericRandomStringService().random(), EnumSet.of(Path.Type.file));
        new S3TouchFeature(session).touch(clearFile, new TransferStatus());
        assertTrue(new S3FindFeature(session).find(clearFile));
        final CryptoVault cryptomator = new CryptoVault(vault);
        cryptomator.create(session, null, new VaultCredentials("test"), new DisabledPasswordStore());
        final DefaultVaultRegistry registry = new DefaultVaultRegistry(new DisabledPasswordStore(), new DisabledPasswordCallback(), cryptomator);
        session.withRegistry(registry);
        final Path encryptedFolder = new CryptoDirectoryFeature<>(session, new S3DirectoryFeature(session, new S3WriteFeature(session)), new S3WriteFeature(session), cryptomator).mkdir(
            new Path(vault, new AlphanumericRandomStringService().random(), EnumSet.of(Path.Type.directory)), null, new TransferStatus());
        assertTrue(new CryptoFindFeature(session, new DefaultFindFeature(session), cryptomator).find(encryptedFolder));
        final Path encryptedFile = new Path(encryptedFolder, new AlphanumericRandomStringService().random(), EnumSet.of(Path.Type.file));
        // move file into vault
        final MoveWorker worker = new MoveWorker(Collections.singletonMap(clearFile, encryptedFile), new SessionPool.SingleSessionPool(session), PathCache.empty(), new DisabledProgressListener(), new DisabledLoginCallback());
        worker.run(session);
        assertFalse(new S3FindFeature(session).find(clearFile));
        assertTrue(new CryptoFindFeature(session, new S3FindFeature(session), cryptomator).find(encryptedFile));
        new CryptoDeleteFeature(session, new S3DefaultDeleteFeature(session), cryptomator).delete(Arrays.asList(encryptedFile, encryptedFolder, vault), new DisabledLoginCallback(), new Delete.DisabledCallback());
        session.close();
        registry.clear();
    }

    @Test
    public void testMoveDirectoryIntoVault() throws Exception {
        final Path home = new Path("test-us-east-1-cyberduck", EnumSet.of(Path.Type.directory, Path.Type.volume));
        final Path vault = new Path(home, new AlphanumericRandomStringService().random(), EnumSet.of(Path.Type.directory));
        final Path clearFolder = new S3DirectoryFeature(session, new S3WriteFeature(session)).mkdir(new Path(home, new AlphanumericRandomStringService().random(), EnumSet.of(Path.Type.directory)), null, new TransferStatus());
        final Path clearFile = new Path(clearFolder, new AlphanumericRandomStringService().random(), EnumSet.of(Path.Type.file));
        new S3TouchFeature(session).touch(clearFile, new TransferStatus());
        assertTrue(new S3FindFeature(session).find(clearFolder));
        assertTrue(new S3FindFeature(session).find(clearFile));
        final CryptoVault cryptomator = new CryptoVault(vault);
        cryptomator.create(session, null, new VaultCredentials("test"), new DisabledPasswordStore());
        final DefaultVaultRegistry registry = new DefaultVaultRegistry(new DisabledPasswordStore(), new DisabledPasswordCallback(), cryptomator);
        session.withRegistry(registry);
        // move directory into vault
        final Path encryptedFolder = new Path(vault, clearFolder.getName(), EnumSet.of(Path.Type.directory));
        final Path encryptedFile = new Path(encryptedFolder, clearFile.getName(), EnumSet.of(Path.Type.file));
        final MoveWorker worker = new MoveWorker(Collections.singletonMap(clearFolder, encryptedFolder), new SessionPool.SingleSessionPool(session), PathCache.empty(), new DisabledProgressListener(), new DisabledLoginCallback());
        worker.run(session);
        assertTrue(new CryptoFindFeature(session, new S3FindFeature(session), cryptomator).find(encryptedFolder));
        assertTrue(new CryptoFindFeature(session, new S3FindFeature(session), cryptomator).find(encryptedFile));
        assertFalse(new S3FindFeature(session).find(clearFolder));
        assertFalse(new S3FindFeature(session).find(clearFile));
        new CryptoDeleteFeature(session, new S3DefaultDeleteFeature(session), cryptomator).delete(Arrays.asList(encryptedFile, encryptedFolder, vault), new DisabledLoginCallback(), new Delete.DisabledCallback());
        session.close();
        registry.clear();
    }

    @Test
    public void testMoveFileOutsideVault() throws Exception {
        final Path home = new Path("test-us-east-1-cyberduck", EnumSet.of(Path.Type.directory, Path.Type.volume));
        final Path vault = new Path(home, new AlphanumericRandomStringService().random(), EnumSet.of(Path.Type.directory));
        final Path clearFolder = new S3DirectoryFeature(session, new S3WriteFeature(session)).mkdir(
            new Path(home, new AlphanumericRandomStringService().random(), EnumSet.of(Path.Type.directory)), null, new TransferStatus());
        final CryptoVault cryptomator = new CryptoVault(vault);
        cryptomator.create(session, null, new VaultCredentials("test"), new DisabledPasswordStore());
        final DefaultVaultRegistry registry = new DefaultVaultRegistry(new DisabledPasswordStore(), new DisabledPasswordCallback(), cryptomator);
        session.withRegistry(registry);
        final Path encryptedFolder = new CryptoDirectoryFeature<>(session, new S3DirectoryFeature(session, new S3WriteFeature(session)), new S3WriteFeature(session), cryptomator).mkdir(
            new Path(vault, new AlphanumericRandomStringService().random(), EnumSet.of(Path.Type.directory)), null, new TransferStatus());
        final Path encryptedFile = new Path(encryptedFolder, new AlphanumericRandomStringService().random(), EnumSet.of(Path.Type.file));
        assertTrue(new CryptoFindFeature(session, new DefaultFindFeature(session), cryptomator).find(encryptedFolder));
        new CryptoTouchFeature<>(session, new S3TouchFeature(session), new S3WriteFeature(session), cryptomator).touch(encryptedFile, new TransferStatus());
        assertTrue(new CryptoFindFeature(session, new DefaultFindFeature(session), cryptomator).find(encryptedFile));
        // move file outside vault
        final Path fileRenamed = new Path(clearFolder, new AlphanumericRandomStringService().random(), EnumSet.of(Path.Type.file));
        final MoveWorker worker = new MoveWorker(Collections.singletonMap(encryptedFile, fileRenamed), new SessionPool.SingleSessionPool(session), PathCache.empty(), new DisabledProgressListener(), new DisabledLoginCallback());
        worker.run(session);
        assertFalse(new CryptoFindFeature(session, new S3FindFeature(session), cryptomator).find(encryptedFile));
        assertTrue(new S3FindFeature(session).find(fileRenamed));
        new CryptoDeleteFeature(session, new S3DefaultDeleteFeature(session), cryptomator).delete(Arrays.asList(encryptedFolder, vault), new DisabledLoginCallback(), new Delete.DisabledCallback());
        new S3DefaultDeleteFeature(session).delete(Arrays.asList(fileRenamed, clearFolder), new DisabledLoginCallback(), new Delete.DisabledCallback());
        session.close();
        registry.clear();
    }

    @Test
    public void testMoveDirectoryOutsideVault() throws Exception {
        final Path home = new Path("test-us-east-1-cyberduck", EnumSet.of(Path.Type.directory, Path.Type.volume));
        final Path vault = new Path(home, new AlphanumericRandomStringService().random(), EnumSet.of(Path.Type.directory));
        final CryptoVault cryptomator = new CryptoVault(vault);
        cryptomator.create(session, null, new VaultCredentials("test"), new DisabledPasswordStore());
        final DefaultVaultRegistry registry = new DefaultVaultRegistry(new DisabledPasswordStore(), new DisabledPasswordCallback(), cryptomator);
        session.withRegistry(registry);
        final Path encryptedFolder = new CryptoDirectoryFeature<>(session, new S3DirectoryFeature(session, new S3WriteFeature(session)), new S3WriteFeature(session), cryptomator).mkdir(
            new Path(vault, new AlphanumericRandomStringService().random(), EnumSet.of(Path.Type.directory)), null, new TransferStatus());
        final Path encryptedFile = new Path(encryptedFolder, new AlphanumericRandomStringService().random(), EnumSet.of(Path.Type.file));
        assertTrue(new CryptoFindFeature(session, new DefaultFindFeature(session), cryptomator).find(encryptedFolder));
        new CryptoTouchFeature<>(session, new S3TouchFeature(session), new S3WriteFeature(session), cryptomator).touch(encryptedFile, new TransferStatus());
        assertTrue(new CryptoFindFeature(session, new DefaultFindFeature(session), cryptomator).find(encryptedFile));
        // move directory outside vault
        final Path directoryRenamed = new Path(home, new AlphanumericRandomStringService().random(), EnumSet.of(Path.Type.directory, Path.Type.placeholder));
        final MoveWorker worker = new MoveWorker(Collections.singletonMap(encryptedFolder, directoryRenamed), new SessionPool.SingleSessionPool(session), PathCache.empty(), new DisabledProgressListener(), new DisabledLoginCallback());
        worker.run(session);
        assertFalse(new CryptoFindFeature(session, new S3FindFeature(session), cryptomator).find(encryptedFolder));
        assertFalse(new CryptoFindFeature(session, new S3FindFeature(session), cryptomator).find(encryptedFile));
        assertTrue(new S3FindFeature(session).find(directoryRenamed));
        final Path fileRenamed = new Path(directoryRenamed, encryptedFile.getName(), EnumSet.of(Path.Type.file));
        assertTrue(new S3FindFeature(session).find(fileRenamed));
        new CryptoDeleteFeature(session, new S3DefaultDeleteFeature(session), cryptomator).delete(Collections.singletonList(vault), new DisabledLoginCallback(), new Delete.DisabledCallback());
        new S3DefaultDeleteFeature(session).delete(Arrays.asList(fileRenamed, directoryRenamed), new DisabledLoginCallback(), new Delete.DisabledCallback());
        session.close();
        registry.clear();
    }
}
