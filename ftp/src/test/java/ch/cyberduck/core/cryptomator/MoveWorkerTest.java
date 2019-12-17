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
import ch.cyberduck.core.Credentials;
import ch.cyberduck.core.DisabledCancelCallback;
import ch.cyberduck.core.DisabledHostKeyCallback;
import ch.cyberduck.core.DisabledListProgressListener;
import ch.cyberduck.core.DisabledLoginCallback;
import ch.cyberduck.core.DisabledPasswordCallback;
import ch.cyberduck.core.DisabledPasswordStore;
import ch.cyberduck.core.DisabledProgressListener;
import ch.cyberduck.core.Host;
import ch.cyberduck.core.Path;
import ch.cyberduck.core.PathCache;
import ch.cyberduck.core.cryptomator.features.CryptoDeleteFeature;
import ch.cyberduck.core.cryptomator.features.CryptoDirectoryFeature;
import ch.cyberduck.core.cryptomator.features.CryptoFindFeature;
import ch.cyberduck.core.cryptomator.features.CryptoListService;
import ch.cyberduck.core.cryptomator.features.CryptoTouchFeature;
import ch.cyberduck.core.exception.NotfoundException;
import ch.cyberduck.core.features.Delete;
import ch.cyberduck.core.ftp.AbstractFTPTest;
import ch.cyberduck.core.ftp.FTPDeleteFeature;
import ch.cyberduck.core.ftp.FTPDirectoryFeature;
import ch.cyberduck.core.ftp.FTPSession;
import ch.cyberduck.core.ftp.FTPWriteFeature;
import ch.cyberduck.core.ftp.list.FTPListService;
import ch.cyberduck.core.pool.SessionPool;
import ch.cyberduck.core.proxy.Proxy;
import ch.cyberduck.core.shared.DefaultAttributesFinderFeature;
import ch.cyberduck.core.shared.DefaultFindFeature;
import ch.cyberduck.core.shared.DefaultHomeFinderService;
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
import java.util.TimeZone;
import java.util.UUID;

import static org.junit.Assert.*;

@Category(IntegrationTest.class)
public class MoveWorkerTest extends AbstractFTPTest {

    @Test
    public void testMoveSameFolderCryptomator() throws Exception {
        final Path home = new DefaultHomeFinderService(session).find();
        final Path vault = new Path(home, UUID.randomUUID().toString(), EnumSet.of(Path.Type.directory));
        final Path source = new Path(vault, UUID.randomUUID().toString(), EnumSet.of(Path.Type.file));
        final Path target = new Path(vault, UUID.randomUUID().toString(), EnumSet.of(Path.Type.file));
        final CryptoVault cryptomator = new CryptoVault(vault);
        cryptomator.create(session, null, new VaultCredentials("test"), new DisabledPasswordStore());
        session.withRegistry(new DefaultVaultRegistry(new DisabledPasswordStore(), new DisabledPasswordCallback(), cryptomator));
        new CryptoTouchFeature<Integer>(session, new DefaultTouchFeature<Integer>(new DefaultUploadFeature<Integer>(new FTPWriteFeature(session)),
            new DefaultAttributesFinderFeature(session)), new FTPWriteFeature(session), cryptomator).touch(source, new TransferStatus());
        assertTrue(new CryptoFindFeature(session, new DefaultFindFeature(session), cryptomator).find(source));
        final MoveWorker worker = new MoveWorker(Collections.singletonMap(source, target), new SessionPool.SingleSessionPool(session), PathCache.empty(), new DisabledProgressListener(), new DisabledLoginCallback());
        worker.run(session);
        assertFalse(new CryptoFindFeature(session, new DefaultFindFeature(session), cryptomator).find(source));
        assertTrue(new CryptoFindFeature(session, new DefaultFindFeature(session), cryptomator).find(target));
        new CryptoDeleteFeature(session, new FTPDeleteFeature(session), cryptomator).delete(Arrays.asList(target, vault), new DisabledLoginCallback(), new Delete.DisabledCallback());
    }

    @Test
    public void testMoveToDifferentFolderCryptomator() throws Exception {
        final Path home = new DefaultHomeFinderService(session).find();
        final Path vault = new Path(home, UUID.randomUUID().toString(), EnumSet.of(Path.Type.directory));
        final Path source = new Path(vault, UUID.randomUUID().toString(), EnumSet.of(Path.Type.file));
        final Path targetFolder = new Path(vault, UUID.randomUUID().toString(), EnumSet.of(Path.Type.directory));
        final Path target = new Path(targetFolder, UUID.randomUUID().toString(), EnumSet.of(Path.Type.file));
        final CryptoVault cryptomator = new CryptoVault(vault);
        cryptomator.create(session, null, new VaultCredentials("test"), new DisabledPasswordStore());
        session.withRegistry(new DefaultVaultRegistry(new DisabledPasswordStore(), new DisabledPasswordCallback(), cryptomator));
        new CryptoTouchFeature<Integer>(session, new DefaultTouchFeature<Integer>(new DefaultUploadFeature<Integer>(new FTPWriteFeature(session)),
            new DefaultAttributesFinderFeature(session)), new FTPWriteFeature(session), cryptomator).touch(source, new TransferStatus());
        assertTrue(new CryptoFindFeature(session, new DefaultFindFeature(session), cryptomator).find(source));
        new CryptoDirectoryFeature<Integer>(session, new FTPDirectoryFeature(session), new FTPWriteFeature(session), cryptomator).mkdir(targetFolder, null, new TransferStatus());
        assertTrue(new CryptoFindFeature(session, new DefaultFindFeature(session), cryptomator).find(targetFolder));
        final MoveWorker worker = new MoveWorker(Collections.singletonMap(source, target), new SessionPool.SingleSessionPool(session)
            , PathCache.empty(), new DisabledProgressListener(), new DisabledLoginCallback());
        worker.run(session);
        assertFalse(new CryptoFindFeature(session, new DefaultFindFeature(session), cryptomator).find(source));
        assertTrue(new CryptoFindFeature(session, new DefaultFindFeature(session), cryptomator).find(target));
        new CryptoDeleteFeature(session, new FTPDeleteFeature(session), cryptomator).delete(Arrays.asList(target, targetFolder, vault), new DisabledLoginCallback(), new Delete.DisabledCallback());
    }

    @Test
    public void testMoveToDifferentFolderLongFilenameCryptomator() throws Exception {
        final Path home = new DefaultHomeFinderService(session).find();
        final Path vault = new Path(home, UUID.randomUUID().toString(), EnumSet.of(Path.Type.directory));
        final Path sourceFolder = new Path(vault, new RandomStringGenerator.Builder().build().generate(130), EnumSet.of(Path.Type.directory));
        final Path source = new Path(sourceFolder, new RandomStringGenerator.Builder().build().generate(130), EnumSet.of(Path.Type.file));
        final Path targetFolder = new Path(vault, new RandomStringGenerator.Builder().build().generate(130), EnumSet.of(Path.Type.directory));
        final Path target = new Path(targetFolder, new RandomStringGenerator.Builder().build().generate(130), EnumSet.of(Path.Type.file));
        final CryptoVault cryptomator = new CryptoVault(vault);
        cryptomator.create(session, null, new VaultCredentials("test"), new DisabledPasswordStore());
        session.withRegistry(new DefaultVaultRegistry(new DisabledPasswordStore(), new DisabledPasswordCallback(), cryptomator));
        new CryptoDirectoryFeature<Integer>(session, new FTPDirectoryFeature(session), new FTPWriteFeature(session), cryptomator).mkdir(sourceFolder, null, new TransferStatus());
        new CryptoTouchFeature<Integer>(session, new DefaultTouchFeature<Integer>(new DefaultUploadFeature<Integer>(new FTPWriteFeature(session)),
            new DefaultAttributesFinderFeature(session)), new FTPWriteFeature(session), cryptomator).touch(source, new TransferStatus());
        assertTrue(new CryptoFindFeature(session, new DefaultFindFeature(session), cryptomator).find(source));
        new CryptoDirectoryFeature<Integer>(session, new FTPDirectoryFeature(session), new FTPWriteFeature(session), cryptomator).mkdir(targetFolder, null, new TransferStatus());
        assertTrue(new CryptoFindFeature(session, new DefaultFindFeature(session), cryptomator).find(targetFolder));
        final MoveWorker worker = new MoveWorker(Collections.singletonMap(source, target), new SessionPool.SingleSessionPool(session), PathCache.empty(), new DisabledProgressListener(), new DisabledLoginCallback());
        worker.run(session);
        assertFalse(new CryptoFindFeature(session, new DefaultFindFeature(session), cryptomator).find(source));
        assertTrue(new CryptoFindFeature(session, new DefaultFindFeature(session), cryptomator).find(target));
        new CryptoDeleteFeature(session, new FTPDeleteFeature(session), cryptomator).delete(Arrays.asList(sourceFolder, target, targetFolder, vault), new DisabledLoginCallback(), new Delete.DisabledCallback());
    }

    @Test
    public void testMoveFolder() throws Exception {
        final Path home = new DefaultHomeFinderService(session).find();
        final Path vault = new Path(home, new AlphanumericRandomStringService().random(), EnumSet.of(Path.Type.directory));
        final Path folder = new Path(vault, new AlphanumericRandomStringService().random(), EnumSet.of(Path.Type.directory));
        final Path file = new Path(folder, new AlphanumericRandomStringService().random(), EnumSet.of(Path.Type.file));
        final CryptoVault cryptomator = new CryptoVault(vault);
        cryptomator.create(session, null, new VaultCredentials("test"), new DisabledPasswordStore());
        session.withRegistry(new DefaultVaultRegistry(new DisabledPasswordStore(), new DisabledPasswordCallback(), cryptomator));
        new CryptoDirectoryFeature<Integer>(session, new FTPDirectoryFeature(session), new FTPWriteFeature(session), cryptomator).mkdir(folder, null, new TransferStatus());
        assertTrue(new CryptoFindFeature(session, new DefaultFindFeature(session), cryptomator).find(folder));
        new CryptoTouchFeature<Integer>(session, new DefaultTouchFeature<Integer>(new DefaultUploadFeature<Integer>(new FTPWriteFeature(session)),
            new DefaultAttributesFinderFeature(session)), new FTPWriteFeature(session), cryptomator).touch(file, new TransferStatus());
        assertTrue(new CryptoFindFeature(session, new DefaultFindFeature(session), cryptomator).find(file));
        // rename file
        final Path fileRenamed = new Path(folder, "f1", EnumSet.of(Path.Type.file));
        new MoveWorker(Collections.singletonMap(file, fileRenamed), new SessionPool.SingleSessionPool(session), PathCache.empty(), new DisabledProgressListener(), new DisabledLoginCallback()).run(session);
        assertFalse(new CryptoFindFeature(session, new DefaultFindFeature(session), cryptomator).find(file));
        assertTrue(new CryptoFindFeature(session, new DefaultFindFeature(session), cryptomator).find(fileRenamed));
        // rename folder
        final Path folderRenamed = new Path(vault, new AlphanumericRandomStringService().random(), EnumSet.of(Path.Type.directory));
        new MoveWorker(Collections.singletonMap(folder, folderRenamed), new SessionPool.SingleSessionPool(session), PathCache.empty(), new DisabledProgressListener(), new DisabledLoginCallback()).run(session);
        assertFalse(new CryptoFindFeature(session, new DefaultFindFeature(session), cryptomator).find(folder));
        assertTrue(new CryptoFindFeature(session, new DefaultFindFeature(session), cryptomator).find(folderRenamed));
        try {
            new CryptoListService(session, new FTPListService(session, null, TimeZone.getDefault()), cryptomator).list(folder, new DisabledListProgressListener());
            fail();
        }
        catch(NotfoundException e) {
            //
        }
        assertEquals(1, new CryptoListService(session, new FTPListService(session, null, TimeZone.getDefault()), cryptomator).list(folderRenamed, new DisabledListProgressListener()).size());
        new CryptoDeleteFeature(session, new FTPDeleteFeature(session), cryptomator).delete(Arrays.asList(
            new Path(folderRenamed, "f1", EnumSet.of(Path.Type.file)), folderRenamed), new DisabledLoginCallback(), new Delete.DisabledCallback());
    }

    @Test
    public void testMoveFileIntoVault() throws Exception {
        final Path home = new DefaultHomeFinderService(session).find();
        final Path vault = new Path(home, new AlphanumericRandomStringService().random(), EnumSet.of(Path.Type.directory));
        final Path clearFile = new Path(home, new AlphanumericRandomStringService().random(), EnumSet.of(Path.Type.file));
        new DefaultTouchFeature<Integer>(new DefaultUploadFeature<Integer>(new FTPWriteFeature(session)),
            new DefaultAttributesFinderFeature(session)).touch(clearFile, new TransferStatus());
        assertTrue(new DefaultFindFeature(session).find(clearFile));
        final Path encryptedFolder = new Path(vault, new AlphanumericRandomStringService().random(), EnumSet.of(Path.Type.directory));
        final Path encryptedFile = new Path(encryptedFolder, new AlphanumericRandomStringService().random(), EnumSet.of(Path.Type.file));
        final CryptoVault cryptomator = new CryptoVault(vault);
        cryptomator.create(session, null, new VaultCredentials("test"), new DisabledPasswordStore());
        final DefaultVaultRegistry registry = new DefaultVaultRegistry(new DisabledPasswordStore(), new DisabledPasswordCallback(), cryptomator);
        session.withRegistry(registry);
        new CryptoDirectoryFeature<Integer>(session, new FTPDirectoryFeature(session), new FTPWriteFeature(session), cryptomator).mkdir(encryptedFolder, null, new TransferStatus());
        assertTrue(new CryptoFindFeature(session, new DefaultFindFeature(session), cryptomator).find(encryptedFolder));
        // move file into vault
        final FTPSession copySession = new FTPSession(new Host(session.getHost()).withCredentials(new Credentials("test", "test")));
        copySession.open(Proxy.DIRECT, new DisabledHostKeyCallback(), new DisabledLoginCallback());
        copySession.login(Proxy.DIRECT, new DisabledLoginCallback(), new DisabledCancelCallback());
        final MoveWorker worker = new MoveWorker(Collections.singletonMap(clearFile, encryptedFile), new SessionPool.SingleSessionPool(copySession), PathCache.empty(), new DisabledProgressListener(), new DisabledLoginCallback());
        worker.run(session);
        assertFalse(new DefaultFindFeature(session).find(clearFile));
        assertTrue(new CryptoFindFeature(session, new DefaultFindFeature(session), cryptomator).find(encryptedFile));
        new CryptoDeleteFeature(session, new FTPDeleteFeature(session), cryptomator).delete(Arrays.asList(encryptedFile, encryptedFolder, vault), new DisabledLoginCallback(), new Delete.DisabledCallback());
        registry.clear();
    }

    @Test
    public void testMoveDirectoryIntoVault() throws Exception {
        final Path home = new DefaultHomeFinderService(session).find();
        final Path vault = new Path(home, new AlphanumericRandomStringService().random(), EnumSet.of(Path.Type.directory));
        final Path clearFolder = new Path(home, new AlphanumericRandomStringService().random(), EnumSet.of(Path.Type.directory));
        final Path clearFile = new Path(clearFolder, new AlphanumericRandomStringService().random(), EnumSet.of(Path.Type.file));
        new FTPDirectoryFeature(session).mkdir(clearFolder, null, new TransferStatus());
        new DefaultTouchFeature<Integer>(new DefaultUploadFeature<Integer>(new FTPWriteFeature(session)), new DefaultAttributesFinderFeature(session)).touch(clearFile, new TransferStatus());
        assertTrue(new DefaultFindFeature(session).find(clearFolder));
        assertTrue(new DefaultFindFeature(session).find(clearFile));
        final CryptoVault cryptomator = new CryptoVault(vault);
        cryptomator.create(session, null, new VaultCredentials("test"), new DisabledPasswordStore());
        final DefaultVaultRegistry registry = new DefaultVaultRegistry(new DisabledPasswordStore(), new DisabledPasswordCallback(), cryptomator);
        session.withRegistry(registry);
        // move directory into vault
        final Path encryptedFolder = new Path(vault, clearFolder.getName(), EnumSet.of(Path.Type.directory));
        final Path encryptedFile = new Path(encryptedFolder, clearFile.getName(), EnumSet.of(Path.Type.file));
        final FTPSession copySession = new FTPSession(new Host(session.getHost()).withCredentials(new Credentials("test", "test")));
        copySession.open(Proxy.DIRECT, new DisabledHostKeyCallback(), new DisabledLoginCallback());
        copySession.login(Proxy.DIRECT, new DisabledLoginCallback(), new DisabledCancelCallback());
        final MoveWorker worker = new MoveWorker(Collections.singletonMap(clearFolder, encryptedFolder), new SessionPool.SingleSessionPool(copySession), PathCache.empty(), new DisabledProgressListener(), new DisabledLoginCallback());
        worker.run(session);
        assertTrue(new CryptoFindFeature(session, new DefaultFindFeature(session), cryptomator).find(encryptedFolder));
        assertTrue(new CryptoFindFeature(session, new DefaultFindFeature(session), cryptomator).find(encryptedFile));
        assertFalse(new DefaultFindFeature(session).find(clearFolder));
        assertFalse(new DefaultFindFeature(session).find(clearFile));
        new CryptoDeleteFeature(session, new FTPDeleteFeature(session), cryptomator).delete(Arrays.asList(encryptedFile, encryptedFolder, vault), new DisabledLoginCallback(), new Delete.DisabledCallback());
        registry.clear();
    }

    @Test
    public void testMoveFileOutsideVault() throws Exception {
        final Path home = new DefaultHomeFinderService(session).find();
        final Path vault = new Path(home, new AlphanumericRandomStringService().random(), EnumSet.of(Path.Type.directory));
        final Path clearFolder = new Path(home, new AlphanumericRandomStringService().random(), EnumSet.of(Path.Type.directory));
        new FTPDirectoryFeature(session).mkdir(clearFolder, null, new TransferStatus());
        final Path encryptedFolder = new Path(vault, new AlphanumericRandomStringService().random(), EnumSet.of(Path.Type.directory));
        final Path encryptedFile = new Path(encryptedFolder, new AlphanumericRandomStringService().random(), EnumSet.of(Path.Type.file));
        final CryptoVault cryptomator = new CryptoVault(vault);
        cryptomator.create(session, null, new VaultCredentials("test"), new DisabledPasswordStore());
        final DefaultVaultRegistry registry = new DefaultVaultRegistry(new DisabledPasswordStore(), new DisabledPasswordCallback(), cryptomator);
        session.withRegistry(registry);
        new CryptoDirectoryFeature<Integer>(session, new FTPDirectoryFeature(session), new FTPWriteFeature(session), cryptomator).mkdir(encryptedFolder, null, new TransferStatus());
        assertTrue(new CryptoFindFeature(session, new DefaultFindFeature(session), cryptomator).find(encryptedFolder));
        new CryptoTouchFeature<Integer>(session, new DefaultTouchFeature<Integer>(new DefaultUploadFeature<Integer>(new FTPWriteFeature(session)),
            new DefaultAttributesFinderFeature(session)), new FTPWriteFeature(session), cryptomator).touch(encryptedFile, new TransferStatus());
        assertTrue(new CryptoFindFeature(session, new DefaultFindFeature(session), cryptomator).find(encryptedFile));
        // move file outside vault
        final Path fileRenamed = new Path(clearFolder, new AlphanumericRandomStringService().random(), EnumSet.of(Path.Type.file));
        final FTPSession copySession = new FTPSession(new Host(session.getHost()).withCredentials(new Credentials("test", "test")));
        copySession.open(Proxy.DIRECT, new DisabledHostKeyCallback(), new DisabledLoginCallback());
        copySession.login(Proxy.DIRECT, new DisabledLoginCallback(), new DisabledCancelCallback());
        final MoveWorker worker = new MoveWorker(Collections.singletonMap(encryptedFile, fileRenamed), new SessionPool.SingleSessionPool(copySession), PathCache.empty(), new DisabledProgressListener(), new DisabledLoginCallback());
        worker.run(session);
        assertFalse(new CryptoFindFeature(session, new DefaultFindFeature(session), cryptomator).find(encryptedFile));
        assertTrue(new DefaultFindFeature(session).find(fileRenamed));
        new CryptoDeleteFeature(session, new FTPDeleteFeature(session), cryptomator).delete(Arrays.asList(encryptedFolder, vault), new DisabledLoginCallback(), new Delete.DisabledCallback());
        new FTPDeleteFeature(session).delete(Arrays.asList(fileRenamed, clearFolder), new DisabledLoginCallback(), new Delete.DisabledCallback());
        registry.clear();
    }

    @Test
    public void testMoveDirectoryOutsideVault() throws Exception {
        final Path home = new DefaultHomeFinderService(session).find();
        final Path vault = new Path(home, new AlphanumericRandomStringService().random(), EnumSet.of(Path.Type.directory));
        final Path encryptedFolder = new Path(vault, new AlphanumericRandomStringService().random(), EnumSet.of(Path.Type.directory));
        final Path encryptedFile = new Path(encryptedFolder, new AlphanumericRandomStringService().random(), EnumSet.of(Path.Type.file));
        final CryptoVault cryptomator = new CryptoVault(vault);
        cryptomator.create(session, null, new VaultCredentials("test"), new DisabledPasswordStore());
        final DefaultVaultRegistry registry = new DefaultVaultRegistry(new DisabledPasswordStore(), new DisabledPasswordCallback(), cryptomator);
        session.withRegistry(registry);
        new CryptoDirectoryFeature<Integer>(session, new FTPDirectoryFeature(session), new FTPWriteFeature(session), cryptomator).mkdir(encryptedFolder, null, new TransferStatus());
        assertTrue(new CryptoFindFeature(session, new DefaultFindFeature(session), cryptomator).find(encryptedFolder));
        new CryptoTouchFeature<Integer>(session, new DefaultTouchFeature<Integer>(new DefaultUploadFeature<Integer>(new FTPWriteFeature(session)),
            new DefaultAttributesFinderFeature(session)), new FTPWriteFeature(session), cryptomator).touch(encryptedFile, new TransferStatus());
        assertTrue(new CryptoFindFeature(session, new DefaultFindFeature(session), cryptomator).find(encryptedFile));
        // move directory outside vault
        final Path directoryRenamed = new Path(home, new AlphanumericRandomStringService().random(), EnumSet.of(Path.Type.directory));
        final FTPSession copySession = new FTPSession(new Host(session.getHost()).withCredentials(new Credentials("test", "test")));
        copySession.open(Proxy.DIRECT, new DisabledHostKeyCallback(), new DisabledLoginCallback());
        copySession.login(Proxy.DIRECT, new DisabledLoginCallback(), new DisabledCancelCallback());
        final MoveWorker worker = new MoveWorker(Collections.singletonMap(encryptedFolder, directoryRenamed), new SessionPool.SingleSessionPool(copySession), PathCache.empty(), new DisabledProgressListener(), new DisabledLoginCallback());
        worker.run(session);
        assertFalse(new CryptoFindFeature(session, new DefaultFindFeature(session), cryptomator).find(encryptedFolder));
        assertFalse(new CryptoFindFeature(session, new DefaultFindFeature(session), cryptomator).find(encryptedFile));
        assertTrue(new DefaultFindFeature(session).find(directoryRenamed));
        final Path fileRenamed = new Path(directoryRenamed, encryptedFile.getName(), EnumSet.of(Path.Type.file));
        assertTrue(new DefaultFindFeature(session).find(fileRenamed));
        new CryptoDeleteFeature(session, new FTPDeleteFeature(session), cryptomator).delete(Collections.singletonList(vault), new DisabledLoginCallback(), new Delete.DisabledCallback());
        new FTPDeleteFeature(session).delete(Arrays.asList(fileRenamed, directoryRenamed), new DisabledLoginCallback(), new Delete.DisabledCallback());
        registry.clear();
    }
}
