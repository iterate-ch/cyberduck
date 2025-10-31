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
import ch.cyberduck.core.DisabledConnectionCallback;
import ch.cyberduck.core.DisabledHostKeyCallback;
import ch.cyberduck.core.DisabledLoginCallback;
import ch.cyberduck.core.DisabledPasswordCallback;
import ch.cyberduck.core.DisabledProgressListener;
import ch.cyberduck.core.Host;
import ch.cyberduck.core.Path;
import ch.cyberduck.core.PathCache;
import ch.cyberduck.core.cryptomator.features.CryptoBulkFeature;
import ch.cyberduck.core.cryptomator.features.CryptoReadFeature;
import ch.cyberduck.core.cryptomator.features.CryptoTouchFeature;
import ch.cyberduck.core.cryptomator.features.CryptoWriteFeature;
import ch.cyberduck.core.features.Directory;
import ch.cyberduck.core.features.Find;
import ch.cyberduck.core.features.Write;
import ch.cyberduck.core.ftp.AbstractFTPTest;
import ch.cyberduck.core.ftp.FTPDirectoryFeature;
import ch.cyberduck.core.ftp.FTPReadFeature;
import ch.cyberduck.core.ftp.FTPSession;
import ch.cyberduck.core.ftp.FTPWriteFeature;
import ch.cyberduck.core.io.StreamCopier;
import ch.cyberduck.core.pool.SessionPool;
import ch.cyberduck.core.proxy.DisabledProxyFinder;
import ch.cyberduck.core.shared.DefaultFindFeature;
import ch.cyberduck.core.shared.DefaultHomeFinderService;
import ch.cyberduck.core.shared.DefaultTouchFeature;
import ch.cyberduck.core.shared.DisabledBulkFeature;
import ch.cyberduck.core.transfer.Transfer;
import ch.cyberduck.core.transfer.TransferItem;
import ch.cyberduck.core.transfer.TransferStatus;
import ch.cyberduck.core.vault.DefaultVaultRegistry;
import ch.cyberduck.core.vault.VaultCredentials;
import ch.cyberduck.core.vault.VaultMetadata;
import ch.cyberduck.core.worker.CopyWorker;
import ch.cyberduck.core.worker.DeleteWorker;
import ch.cyberduck.test.IntegrationTest;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.RandomUtils;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.util.Arrays;
import java.util.Collections;
import java.util.EnumSet;

import static org.junit.Assert.*;

@Category(IntegrationTest.class)
@RunWith(value = Parameterized.class)
public class CopyWorkerTest extends AbstractFTPTest {

    @Test
    public void testCopyFile() throws Exception {
        final Path home = new DefaultHomeFinderService(session).find();
        final Path vault = new Path(home, new AlphanumericRandomStringService().random(), EnumSet.of(Path.Type.directory));
        final Path source = new Path(vault, new AlphanumericRandomStringService().random(), EnumSet.of(Path.Type.file));
        final Path target = new Path(vault, new AlphanumericRandomStringService().random(), EnumSet.of(Path.Type.file));
        final AbstractVault cryptomator = new CryptoVaultProvider(session).create(session, null, new VaultCredentials("test"),
                new VaultMetadata(vault, vaultVersion));
        final DefaultVaultRegistry registry = new DefaultVaultRegistry(new DisabledPasswordCallback(), cryptomator);
        session.withRegistry(registry);
        final byte[] content = RandomUtils.nextBytes(40500);
        final TransferStatus status = new TransferStatus();
        new CryptoBulkFeature<>(session, new DisabledBulkFeature(), cryptomator).pre(Transfer.Type.upload, Collections.singletonMap(new TransferItem(source), status), new DisabledConnectionCallback());
        new StreamCopier(new TransferStatus(), new TransferStatus()).transfer(new ByteArrayInputStream(content), new CryptoWriteFeature<>(session, new FTPWriteFeature(session), cryptomator).write(source, status.setLength(content.length), new DisabledConnectionCallback()));
        assertTrue(cryptomator.getFeature(session, Find.class, new DefaultFindFeature(session)).find(source));
        final FTPSession copySession = new FTPSession(new Host(session.getHost()).withCredentials(new Credentials("test", "test")));
        copySession.open(new DisabledProxyFinder(), new DisabledHostKeyCallback(), new DisabledLoginCallback(), new DisabledCancelCallback());
        copySession.login(new DisabledLoginCallback(), new DisabledCancelCallback());
        final CopyWorker worker = new CopyWorker(Collections.singletonMap(source, target), new SessionPool.SingleSessionPool(copySession, registry), PathCache.empty(), new DisabledProgressListener(), new DisabledConnectionCallback());
        worker.run(session);
        assertTrue(cryptomator.getFeature(session, Find.class, new DefaultFindFeature(session)).find(source));
        assertTrue(cryptomator.getFeature(session, Find.class, new DefaultFindFeature(session)).find(target));
        final ByteArrayOutputStream out = new ByteArrayOutputStream(content.length);
        final InputStream in = new CryptoReadFeature(session, new FTPReadFeature(session), cryptomator).read(target, new TransferStatus().setLength(content.length), new DisabledConnectionCallback());
        assertEquals(content.length, IOUtils.copy(in, out));
        assertArrayEquals(content, out.toByteArray());
        in.close();
        new DeleteWorker(new DisabledLoginCallback(), Collections.singletonList(vault), new DisabledProgressListener()).run(session);
    }

    @Test
    public void testCopyToDifferentFolderCryptomator() throws Exception {
        final Path home = new DefaultHomeFinderService(session).find();
        final Path vault = new Path(home, new AlphanumericRandomStringService().random(), EnumSet.of(Path.Type.directory));
        final Path source = new Path(vault, new AlphanumericRandomStringService().random(), EnumSet.of(Path.Type.file));
        final Path targetFolder = new Path(vault, new AlphanumericRandomStringService().random(), EnumSet.of(Path.Type.directory));
        final Path target = new Path(targetFolder, new AlphanumericRandomStringService().random(), EnumSet.of(Path.Type.file));
        final AbstractVault cryptomator = new CryptoVaultProvider(session).create(session, null, new VaultCredentials("test"),
                new VaultMetadata(vault, vaultVersion));
        final DefaultVaultRegistry registry = new DefaultVaultRegistry(new DisabledPasswordCallback(), cryptomator);
        session.withRegistry(registry);
        new CryptoTouchFeature<>(session, new DefaultTouchFeature<Void>(
                session), cryptomator).touch(new CryptoWriteFeature<>(session, new FTPWriteFeature(session), cryptomator), source, new TransferStatus());
        assertTrue(cryptomator.getFeature(session, Find.class, new DefaultFindFeature(session)).find(source));
        cryptomator.getFeature(session, Directory.class, new FTPDirectoryFeature(session)).mkdir(
                new CryptoWriteFeature<>(session, new FTPWriteFeature(session), cryptomator), targetFolder, new TransferStatus());
        assertTrue(cryptomator.getFeature(session, Find.class, new DefaultFindFeature(session)).find(targetFolder));
        final FTPSession copySession = new FTPSession(new Host(session.getHost()).withCredentials(new Credentials("test", "test")));
        copySession.open(new DisabledProxyFinder(), new DisabledHostKeyCallback(), new DisabledLoginCallback(), new DisabledCancelCallback());
        copySession.login(new DisabledLoginCallback(), new DisabledCancelCallback());
        final CopyWorker worker = new CopyWorker(Collections.singletonMap(source, target), new SessionPool.SingleSessionPool(copySession, registry), PathCache.empty(), new DisabledProgressListener(), new DisabledConnectionCallback());
        worker.run(session);
        assertTrue(cryptomator.getFeature(session, Find.class, new DefaultFindFeature(session)).find(source));
        assertTrue(cryptomator.getFeature(session, Find.class, new DefaultFindFeature(session)).find(target));
        registry.clear();
        new DeleteWorker(new DisabledLoginCallback(), Collections.singletonList(vault), new DisabledProgressListener()).run(session);
    }

    @Test
    @Ignore("Filename shortening not implemented yet")
    public void testCopyToDifferentFolderLongFilenameCryptomator() throws Exception {
        final Path home = new DefaultHomeFinderService(session).find();
        final Path vault = new Path(home, new AlphanumericRandomStringService().random(), EnumSet.of(Path.Type.directory));
        final Path source = new Path(vault, new AlphanumericRandomStringService(130).random(), EnumSet.of(Path.Type.file));
        final Path targetFolder = new Path(vault, new AlphanumericRandomStringService(130).random(), EnumSet.of(Path.Type.directory));
        final Path target = new Path(targetFolder, new AlphanumericRandomStringService(130).random(), EnumSet.of(Path.Type.file));
        final AbstractVault cryptomator = new CryptoVaultProvider(session).create(session, null, new VaultCredentials("test"),
                new VaultMetadata(vault, vaultVersion));
        final DefaultVaultRegistry registry = new DefaultVaultRegistry(new DisabledPasswordCallback(), cryptomator);
        session.withRegistry(registry);
        new CryptoTouchFeature<>(session, new DefaultTouchFeature<Void>(
                session), cryptomator).touch(new CryptoWriteFeature<>(session, new FTPWriteFeature(session), cryptomator), source, new TransferStatus());
        assertTrue(cryptomator.getFeature(session, Find.class, new DefaultFindFeature(session)).find(source));
        cryptomator.getFeature(session, Directory.class, new FTPDirectoryFeature(session)).mkdir(
                new CryptoWriteFeature<>(session, new FTPWriteFeature(session), cryptomator), targetFolder, new TransferStatus());
        assertTrue(cryptomator.getFeature(session, Find.class, new DefaultFindFeature(session)).find(targetFolder));
        final FTPSession copySession = new FTPSession(new Host(session.getHost()).withCredentials(new Credentials("test", "test")));
        copySession.open(new DisabledProxyFinder(), new DisabledHostKeyCallback(), new DisabledLoginCallback(), new DisabledCancelCallback());
        copySession.login(new DisabledLoginCallback(), new DisabledCancelCallback());
        final CopyWorker worker = new CopyWorker(Collections.singletonMap(source, target), new SessionPool.SingleSessionPool(copySession, registry), PathCache.empty(), new DisabledProgressListener(), new DisabledConnectionCallback());
        worker.run(session);
        assertTrue(cryptomator.getFeature(session, Find.class, new DefaultFindFeature(session)).find(source));
        assertTrue(cryptomator.getFeature(session, Find.class, new DefaultFindFeature(session)).find(target));
        registry.clear();
        new DeleteWorker(new DisabledLoginCallback(), Collections.singletonList(vault), new DisabledProgressListener()).run(session);
    }

    @Test
    public void testCopyFolder() throws Exception {
        final Path home = new DefaultHomeFinderService(session).find();
        final Path vault = new Path(home, new AlphanumericRandomStringService().random(), EnumSet.of(Path.Type.directory));
        final Path folder = new Path(vault, new AlphanumericRandomStringService().random(), EnumSet.of(Path.Type.directory));
        final Path file = new Path(folder, new AlphanumericRandomStringService().random(), EnumSet.of(Path.Type.file));
        final AbstractVault cryptomator = new CryptoVaultProvider(session).create(session, null, new VaultCredentials("test"),
                new VaultMetadata(vault, vaultVersion));
        final DefaultVaultRegistry registry = new DefaultVaultRegistry(new DisabledPasswordCallback(), cryptomator);
        session.withRegistry(registry);
        cryptomator.getFeature(session, Directory.class, new FTPDirectoryFeature(session)).mkdir(
                new CryptoWriteFeature<>(session, new FTPWriteFeature(session), cryptomator), folder, new TransferStatus());
        assertTrue(cryptomator.getFeature(session, Find.class, new DefaultFindFeature(session)).find(folder));
        new CryptoTouchFeature<>(session, new DefaultTouchFeature<Void>(session), cryptomator).touch(
                new CryptoWriteFeature<>(session, new FTPWriteFeature(session), cryptomator), file, new TransferStatus());
        assertTrue(cryptomator.getFeature(session, Find.class, new DefaultFindFeature(session)).find(file));
        // copy file
        final Path fileRenamed = new Path(folder, "f1", EnumSet.of(Path.Type.file));
        final FTPSession copySession = new FTPSession(new Host(session.getHost()).withCredentials(new Credentials("test", "test")));
        copySession.open(new DisabledProxyFinder(), new DisabledHostKeyCallback(), new DisabledLoginCallback(), new DisabledCancelCallback());
        copySession.login(new DisabledLoginCallback(), new DisabledCancelCallback());
        new CopyWorker(Collections.singletonMap(file, fileRenamed), new SessionPool.SingleSessionPool(copySession, registry), PathCache.empty(), new DisabledProgressListener(), new DisabledConnectionCallback()).run(session);
        assertTrue(cryptomator.getFeature(session, Find.class, new DefaultFindFeature(session)).find(file));
        assertTrue(cryptomator.getFeature(session, Find.class, new DefaultFindFeature(session)).find(fileRenamed));
        // copy folder
        final Path folderRenamed = new Path(vault, new AlphanumericRandomStringService().random(), EnumSet.of(Path.Type.directory));
        new CopyWorker(Collections.singletonMap(folder, folderRenamed), new SessionPool.SingleSessionPool(copySession, registry), PathCache.empty(), new DisabledProgressListener(), new DisabledConnectionCallback()).run(session);
        assertTrue(cryptomator.getFeature(session, Find.class, new DefaultFindFeature(session)).find(folder));
        assertTrue(cryptomator.getFeature(session, Find.class, new DefaultFindFeature(session)).find(folderRenamed));
        final Path fileRenamedInRenamedFolder = new Path(folderRenamed, "f1", EnumSet.of(Path.Type.file));
        assertTrue(cryptomator.getFeature(session, Find.class, new DefaultFindFeature(session)).find(fileRenamedInRenamedFolder));
        registry.clear();
        new DeleteWorker(new DisabledLoginCallback(), Collections.singletonList(vault), new DisabledProgressListener()).run(session);
    }

    @Test
    public void testCopyFileIntoVault() throws Exception {
        final Path home = new DefaultHomeFinderService(session).find();
        final Path vault = new Path(home, new AlphanumericRandomStringService().random(), EnumSet.of(Path.Type.directory));
        final Path cleartextFile = new Path(home, new AlphanumericRandomStringService().random(), EnumSet.of(Path.Type.file));
        new DefaultTouchFeature<Void>(
                session).touch(new FTPWriteFeature(session), cleartextFile, new TransferStatus());
        assertTrue(new DefaultFindFeature(session).find(cleartextFile));
        final Path encryptedFolder = new Path(vault, new AlphanumericRandomStringService().random(), EnumSet.of(Path.Type.directory));
        final Path encryptedFile = new Path(encryptedFolder, new AlphanumericRandomStringService().random(), EnumSet.of(Path.Type.file));
        final AbstractVault cryptomator = new CryptoVaultProvider(session).create(session, null, new VaultCredentials("test"),
                new VaultMetadata(vault, vaultVersion));
        final DefaultVaultRegistry registry = new DefaultVaultRegistry(new DisabledPasswordCallback(), cryptomator);
        session.withRegistry(registry);
        cryptomator.getFeature(session, Directory.class, new FTPDirectoryFeature(session)).mkdir(
                cryptomator.getFeature(session, Write.class, new FTPWriteFeature(session)), encryptedFolder, new TransferStatus());
        assertTrue(cryptomator.getFeature(session, Find.class, new DefaultFindFeature(session)).find(encryptedFolder));
        // copy file into vault
        final FTPSession copySession = new FTPSession(new Host(session.getHost()).withCredentials(new Credentials("test", "test")));
        copySession.open(new DisabledProxyFinder(), new DisabledHostKeyCallback(), new DisabledLoginCallback(), new DisabledCancelCallback());
        copySession.login(new DisabledLoginCallback(), new DisabledCancelCallback());
        final CopyWorker worker = new CopyWorker(Collections.singletonMap(cleartextFile, encryptedFile), new SessionPool.SingleSessionPool(copySession, registry), PathCache.empty(), new DisabledProgressListener(), new DisabledConnectionCallback());
        worker.run(session);
        assertTrue(new DefaultFindFeature(session).find(cleartextFile));
        assertTrue(cryptomator.getFeature(session, Find.class, new DefaultFindFeature(session)).find(encryptedFile));
        registry.clear();
        new DeleteWorker(new DisabledLoginCallback(), Collections.singletonList(vault), new DisabledProgressListener()).run(session);
    }

    @Test
    public void testCopyDirectoryIntoVault() throws Exception {
        final Path home = new DefaultHomeFinderService(session).find();
        final Path vault = new Path(home, new AlphanumericRandomStringService().random(), EnumSet.of(Path.Type.directory));
        final Path cleartextFolder = new Path(home, new AlphanumericRandomStringService().random(), EnumSet.of(Path.Type.directory));
        final Path cleartextFile = new Path(cleartextFolder, new AlphanumericRandomStringService().random(), EnumSet.of(Path.Type.file));
        new FTPDirectoryFeature(session).mkdir(new FTPWriteFeature(session), cleartextFolder, new TransferStatus());
        new DefaultTouchFeature<Void>(
                session).touch(new FTPWriteFeature(session), cleartextFile, new TransferStatus());
        assertTrue(new DefaultFindFeature(session).find(cleartextFolder));
        assertTrue(new DefaultFindFeature(session).find(cleartextFile));
        final AbstractVault cryptomator = new CryptoVaultProvider(session).create(session, null, new VaultCredentials("test"),
                new VaultMetadata(vault, vaultVersion));
        final DefaultVaultRegistry registry = new DefaultVaultRegistry(new DisabledPasswordCallback(), cryptomator);
        session.withRegistry(registry);
        // move directory into vault
        final Path encryptedFolder = new Path(vault, cleartextFolder.getName(), EnumSet.of(Path.Type.directory));
        final Path encryptedFile = new Path(encryptedFolder, cleartextFile.getName(), EnumSet.of(Path.Type.file));
        final FTPSession copySession = new FTPSession(new Host(session.getHost()).withCredentials(new Credentials("test", "test")));
        copySession.open(new DisabledProxyFinder(), new DisabledHostKeyCallback(), new DisabledLoginCallback(), new DisabledCancelCallback());
        copySession.login(new DisabledLoginCallback(), new DisabledCancelCallback());
        final CopyWorker worker = new CopyWorker(Collections.singletonMap(cleartextFolder, encryptedFolder), new SessionPool.SingleSessionPool(copySession, registry), PathCache.empty(), new DisabledProgressListener(), new DisabledConnectionCallback());
        worker.run(session);
        assertTrue(cryptomator.getFeature(session, Find.class, new DefaultFindFeature(session)).find(encryptedFolder));
        assertTrue(cryptomator.getFeature(session, Find.class, new DefaultFindFeature(session)).find(encryptedFile));
        assertTrue(new DefaultFindFeature(session).find(cleartextFolder));
        assertTrue(new DefaultFindFeature(session).find(cleartextFile));
        registry.clear();
        new DeleteWorker(new DisabledLoginCallback(), Collections.singletonList(vault), new DisabledProgressListener()).run(session);
    }

    @Test
    public void testCopyFileOutsideVault() throws Exception {
        final Path home = new DefaultHomeFinderService(session).find();
        final Path vault = new Path(home, new AlphanumericRandomStringService().random(), EnumSet.of(Path.Type.directory));
        final Path clearFolder = new Path(home, new AlphanumericRandomStringService().random(), EnumSet.of(Path.Type.directory));
        new FTPDirectoryFeature(session).mkdir(new FTPWriteFeature(session), clearFolder, new TransferStatus());
        final Path encryptedFolder = new Path(vault, new AlphanumericRandomStringService().random(), EnumSet.of(Path.Type.directory));
        final Path encryptedFile = new Path(encryptedFolder, new AlphanumericRandomStringService().random(), EnumSet.of(Path.Type.file));
        final AbstractVault cryptomator = new CryptoVaultProvider(session).create(session, null, new VaultCredentials("test"),
                new VaultMetadata(vault, vaultVersion));
        final DefaultVaultRegistry registry = new DefaultVaultRegistry(new DisabledPasswordCallback(), cryptomator);
        session.withRegistry(registry);
        cryptomator.getFeature(session, Directory.class, new FTPDirectoryFeature(session)).mkdir(
                new CryptoWriteFeature<>(session, new FTPWriteFeature(session), cryptomator), encryptedFolder, new TransferStatus());
        assertTrue(cryptomator.getFeature(session, Find.class, new DefaultFindFeature(session)).find(encryptedFolder));
        new CryptoTouchFeature<>(session, new DefaultTouchFeature<Void>(
                session), cryptomator).touch(new CryptoWriteFeature<>(session, new FTPWriteFeature(session), cryptomator), encryptedFile, new TransferStatus());
        assertTrue(cryptomator.getFeature(session, Find.class, new DefaultFindFeature(session)).find(encryptedFile));
        // move file outside vault
        final Path cleartextFile = new Path(clearFolder, new AlphanumericRandomStringService().random(), EnumSet.of(Path.Type.file));
        final FTPSession copySession = new FTPSession(new Host(session.getHost()).withCredentials(new Credentials("test", "test")));
        copySession.open(new DisabledProxyFinder(), new DisabledHostKeyCallback(), new DisabledLoginCallback(), new DisabledCancelCallback());
        copySession.login(new DisabledLoginCallback(), new DisabledCancelCallback());
        final CopyWorker worker = new CopyWorker(Collections.singletonMap(encryptedFile, cleartextFile), new SessionPool.SingleSessionPool(copySession, registry), PathCache.empty(), new DisabledProgressListener(), new DisabledConnectionCallback());
        worker.run(session);
        assertTrue(cryptomator.getFeature(session, Find.class, new DefaultFindFeature(session)).find(encryptedFile));
        assertTrue(new DefaultFindFeature(session).find(cleartextFile));
        registry.clear();
        new DeleteWorker(new DisabledLoginCallback(), Arrays.asList(vault, clearFolder), new DisabledProgressListener()).run(session);
    }

    @Test
    public void testCopyDirectoryOutsideVault() throws Exception {
        final Path home = new DefaultHomeFinderService(session).find();
        final Path vault = new Path(home, new AlphanumericRandomStringService().random(), EnumSet.of(Path.Type.directory));
        final Path encryptedFolder = new Path(vault, new AlphanumericRandomStringService().random(), EnumSet.of(Path.Type.directory));
        final Path encryptedFile = new Path(encryptedFolder, new AlphanumericRandomStringService().random(), EnumSet.of(Path.Type.file));
        final AbstractVault cryptomator = new CryptoVaultProvider(session).create(session, null, new VaultCredentials("test"),
                new VaultMetadata(vault, vaultVersion));
        final DefaultVaultRegistry registry = new DefaultVaultRegistry(new DisabledPasswordCallback(), cryptomator);
        session.withRegistry(registry);
        cryptomator.getFeature(session, Directory.class, new FTPDirectoryFeature(session)).mkdir(
                new CryptoWriteFeature<>(session, new FTPWriteFeature(session), cryptomator), encryptedFolder, new TransferStatus());
        assertTrue(cryptomator.getFeature(session, Find.class, new DefaultFindFeature(session)).find(encryptedFolder));
        new CryptoTouchFeature<>(session, new DefaultTouchFeature<Void>(
                session), cryptomator).touch(new CryptoWriteFeature<>(session, new FTPWriteFeature(session), cryptomator), encryptedFile, new TransferStatus());
        assertTrue(cryptomator.getFeature(session, Find.class, new DefaultFindFeature(session)).find(encryptedFile));
        // copy directory outside vault
        final Path cleartextFolder = new Path(home, new AlphanumericRandomStringService().random(), EnumSet.of(Path.Type.directory));
        final FTPSession copySession = new FTPSession(new Host(session.getHost()).withCredentials(new Credentials("test", "test")));
        copySession.open(new DisabledProxyFinder(), new DisabledHostKeyCallback(), new DisabledLoginCallback(), new DisabledCancelCallback());
        copySession.login(new DisabledLoginCallback(), new DisabledCancelCallback());
        final CopyWorker worker = new CopyWorker(Collections.singletonMap(encryptedFolder, cleartextFolder), new SessionPool.SingleSessionPool(copySession, registry), PathCache.empty(), new DisabledProgressListener(), new DisabledConnectionCallback());
        worker.run(session);
        assertTrue(cryptomator.getFeature(session, Find.class, new DefaultFindFeature(session)).find(encryptedFolder));
        assertTrue(cryptomator.getFeature(session, Find.class, new DefaultFindFeature(session)).find(encryptedFile));
        assertTrue(new DefaultFindFeature(session).find(cleartextFolder));
        final Path fileRenamed = new Path(cleartextFolder, encryptedFile.getName(), EnumSet.of(Path.Type.file));
        assertTrue(new DefaultFindFeature(session).find(fileRenamed));
        registry.clear();
        new DeleteWorker(new DisabledLoginCallback(), Arrays.asList(cleartextFolder, vault), new DisabledProgressListener()).run(session);
    }
}
