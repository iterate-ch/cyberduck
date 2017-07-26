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
import ch.cyberduck.core.DisabledLoginCallback;
import ch.cyberduck.core.DisabledPasswordCallback;
import ch.cyberduck.core.DisabledPasswordStore;
import ch.cyberduck.core.DisabledProgressListener;
import ch.cyberduck.core.Host;
import ch.cyberduck.core.Path;
import ch.cyberduck.core.PathCache;
import ch.cyberduck.core.Session;
import ch.cyberduck.core.cryptomator.features.CryptoDirectoryFeature;
import ch.cyberduck.core.cryptomator.features.CryptoFindFeature;
import ch.cyberduck.core.cryptomator.features.CryptoTouchFeature;
import ch.cyberduck.core.exception.BackgroundException;
import ch.cyberduck.core.pool.SessionPool;
import ch.cyberduck.core.s3.S3DirectoryFeature;
import ch.cyberduck.core.s3.S3FindFeature;
import ch.cyberduck.core.s3.S3Protocol;
import ch.cyberduck.core.s3.S3Session;
import ch.cyberduck.core.s3.S3TouchFeature;
import ch.cyberduck.core.s3.S3WriteFeature;
import ch.cyberduck.core.shared.DefaultFindFeature;
import ch.cyberduck.core.threading.BackgroundActionState;
import ch.cyberduck.core.transfer.TransferStatus;
import ch.cyberduck.core.vault.DefaultVaultRegistry;
import ch.cyberduck.core.vault.VaultCredentials;
import ch.cyberduck.core.vault.VaultRegistry;
import ch.cyberduck.core.worker.CopyWorker;
import ch.cyberduck.core.worker.DeleteWorker;
import ch.cyberduck.test.IntegrationTest;

import org.apache.commons.text.RandomStringGenerator;
import org.jets3t.service.model.StorageObject;
import org.junit.Test;
import org.junit.experimental.categories.Category;

import java.util.Arrays;
import java.util.Collections;
import java.util.EnumSet;

import static org.junit.Assert.assertTrue;

@Category(IntegrationTest.class)
public class CopyWorkerTest {

    @Test
    public void testCopyFile() throws Exception {
        final Host host = new Host(new S3Protocol(), new S3Protocol().getDefaultHostname(), new Credentials(
                System.getProperties().getProperty("s3.key"), System.getProperties().getProperty("s3.secret")
        ));
        final S3Session session = new S3Session(host);
        session.open(new DisabledHostKeyCallback());
        session.login(new DisabledPasswordStore(), new DisabledLoginCallback(), new DisabledCancelCallback(), PathCache.empty());
        final Path home = new Path("test-us-east-1-cyberduck", EnumSet.of(Path.Type.directory, Path.Type.volume));
        final Path vault = new Path(home, new AlphanumericRandomStringService().random(), EnumSet.of(Path.Type.directory));
        final Path source = new Path(vault, new AlphanumericRandomStringService().random(), EnumSet.of(Path.Type.file));
        final Path target = new Path(vault, new AlphanumericRandomStringService().random(), EnumSet.of(Path.Type.file));
        final CryptoVault cryptomator = new CryptoVault(vault, new DisabledPasswordStore());
        cryptomator.create(session, null, new VaultCredentials("test"));
        final DefaultVaultRegistry registry = new DefaultVaultRegistry(new DisabledPasswordStore(), new DisabledPasswordCallback(), cryptomator);
        session.withRegistry(registry);
        new CryptoTouchFeature<StorageObject>(session, new S3TouchFeature(session), new S3WriteFeature(session), cryptomator).touch(source, new TransferStatus());
        assertTrue(new CryptoFindFeature(session, new DefaultFindFeature(session), cryptomator).find(source));
        final CopyWorker worker = new CopyWorker(Collections.singletonMap(source, target), new TestSessionPool(session, registry), new DisabledProgressListener());
        worker.run(session);
        assertTrue(new CryptoFindFeature(session, new DefaultFindFeature(session), cryptomator).find(source));
        assertTrue(new CryptoFindFeature(session, new DefaultFindFeature(session), cryptomator).find(target));
        registry.clear();
        new DeleteWorker(new DisabledLoginCallback(), Collections.singletonList(vault), PathCache.empty(), new DisabledProgressListener()).run(session);
        session.close();
    }

    @Test
    public void testCopyToDifferentFolderCryptomator() throws Exception {
        final Host host = new Host(new S3Protocol(), new S3Protocol().getDefaultHostname(), new Credentials(
                System.getProperties().getProperty("s3.key"), System.getProperties().getProperty("s3.secret")
        ));
        final S3Session session = new S3Session(host);
        session.open(new DisabledHostKeyCallback());
        session.login(new DisabledPasswordStore(), new DisabledLoginCallback(), new DisabledCancelCallback(), PathCache.empty());
        final Path home = new Path("test-us-east-1-cyberduck", EnumSet.of(Path.Type.directory, Path.Type.volume));
        final Path vault = new Path(home, new AlphanumericRandomStringService().random(), EnumSet.of(Path.Type.directory));
        final Path source = new Path(vault, new AlphanumericRandomStringService().random(), EnumSet.of(Path.Type.file));
        final Path targetFolder = new Path(vault, new AlphanumericRandomStringService().random(), EnumSet.of(Path.Type.directory));
        final Path target = new Path(targetFolder, new AlphanumericRandomStringService().random(), EnumSet.of(Path.Type.file));
        final CryptoVault cryptomator = new CryptoVault(vault, new DisabledPasswordStore());
        cryptomator.create(session, null, new VaultCredentials("test"));
        final DefaultVaultRegistry registry = new DefaultVaultRegistry(new DisabledPasswordStore(), new DisabledPasswordCallback(), cryptomator);
        session.withRegistry(registry);
        new CryptoTouchFeature<StorageObject>(session, new S3TouchFeature(session), new S3WriteFeature(session), cryptomator).touch(source, new TransferStatus());
        assertTrue(new CryptoFindFeature(session, new DefaultFindFeature(session), cryptomator).find(source));
        new CryptoDirectoryFeature<StorageObject>(session, new S3DirectoryFeature(session, new S3WriteFeature(session)), new S3WriteFeature(session), cryptomator).mkdir(targetFolder, null, new TransferStatus());
        assertTrue(new CryptoFindFeature(session, new DefaultFindFeature(session), cryptomator).find(targetFolder));
        final CopyWorker worker = new CopyWorker(Collections.singletonMap(source, target), new TestSessionPool(session, registry), new DisabledProgressListener());
        worker.run(session);
        assertTrue(new CryptoFindFeature(session, new DefaultFindFeature(session), cryptomator).find(source));
        assertTrue(new CryptoFindFeature(session, new DefaultFindFeature(session), cryptomator).find(target));
        registry.clear();
        new DeleteWorker(new DisabledLoginCallback(), Collections.singletonList(vault), PathCache.empty(), new DisabledProgressListener()).run(session);
        session.close();
    }

    @Test
    public void testCopyToDifferentFolderLongFilenameCryptomator() throws Exception {
        final Host host = new Host(new S3Protocol(), new S3Protocol().getDefaultHostname(), new Credentials(
                System.getProperties().getProperty("s3.key"), System.getProperties().getProperty("s3.secret")
        ));
        final S3Session session = new S3Session(host);
        session.open(new DisabledHostKeyCallback());
        session.login(new DisabledPasswordStore(), new DisabledLoginCallback(), new DisabledCancelCallback(), PathCache.empty());
        final Path home = new Path("test-us-east-1-cyberduck", EnumSet.of(Path.Type.directory, Path.Type.volume));
        final CryptoVault cryptomator = new CryptoVault(new Path(home, new AlphanumericRandomStringService().random(), EnumSet.of(Path.Type.directory)), new DisabledPasswordStore());
        final Path vault = cryptomator.create(session, null, new VaultCredentials("test"));
        final Path source = new Path(vault, new RandomStringGenerator.Builder().build().generate(130), EnumSet.of(Path.Type.file));
        final Path targetFolder = new Path(vault, new RandomStringGenerator.Builder().build().generate(130), EnumSet.of(Path.Type.directory));
        final Path target = new Path(targetFolder, new RandomStringGenerator.Builder().build().generate(130), EnumSet.of(Path.Type.file));
        final DefaultVaultRegistry registry = new DefaultVaultRegistry(new DisabledPasswordStore(), new DisabledPasswordCallback(), cryptomator);
        session.withRegistry(registry);
        new CryptoTouchFeature<StorageObject>(session, new S3TouchFeature(session), new S3WriteFeature(session), cryptomator).touch(source, new TransferStatus());
        assertTrue(new CryptoFindFeature(session, new DefaultFindFeature(session), cryptomator).find(source));
        new CryptoDirectoryFeature<StorageObject>(session, new S3DirectoryFeature(session, new S3WriteFeature(session)), new S3WriteFeature(session), cryptomator).mkdir(targetFolder, null, new TransferStatus());
        assertTrue(new CryptoFindFeature(session, new DefaultFindFeature(session), cryptomator).find(targetFolder));
        final CopyWorker worker = new CopyWorker(Collections.singletonMap(source, target), new TestSessionPool(session, registry), new DisabledProgressListener());
        worker.run(session);
        assertTrue(new CryptoFindFeature(session, new DefaultFindFeature(session), cryptomator).find(source));
        assertTrue(new CryptoFindFeature(session, new DefaultFindFeature(session), cryptomator).find(target));
        registry.clear();
        new DeleteWorker(new DisabledLoginCallback(), Collections.singletonList(vault), PathCache.empty(), new DisabledProgressListener()).run(session);
        session.close();
    }

    @Test
    public void testCopyFolder() throws Exception {
        final Host host = new Host(new S3Protocol(), new S3Protocol().getDefaultHostname(), new Credentials(
                System.getProperties().getProperty("s3.key"), System.getProperties().getProperty("s3.secret")
        ));
        final S3Session session = new S3Session(host);
        session.open(new DisabledHostKeyCallback());
        session.login(new DisabledPasswordStore(), new DisabledLoginCallback(), new DisabledCancelCallback(), PathCache.empty());
        final Path home = new Path("test-us-east-1-cyberduck", EnumSet.of(Path.Type.directory, Path.Type.volume));
        final CryptoVault cryptomator = new CryptoVault(new Path(home, new AlphanumericRandomStringService().random(), EnumSet.of(Path.Type.directory)), new DisabledPasswordStore());
        final Path vault = cryptomator.create(session, null, new VaultCredentials("test"));
        final Path folder = new Path(vault, new AlphanumericRandomStringService().random(), EnumSet.of(Path.Type.directory));
        final Path file = new Path(folder, new AlphanumericRandomStringService().random(), EnumSet.of(Path.Type.file));
        final DefaultVaultRegistry registry = new DefaultVaultRegistry(new DisabledPasswordStore(), new DisabledPasswordCallback(), cryptomator);
        session.withRegistry(registry);
        new CryptoDirectoryFeature<StorageObject>(session, new S3DirectoryFeature(session, new S3WriteFeature(session)), new S3WriteFeature(session), cryptomator).mkdir(folder, null, new TransferStatus());
        assertTrue(new CryptoFindFeature(session, new DefaultFindFeature(session), cryptomator).find(folder));
        new CryptoTouchFeature<StorageObject>(session, new S3TouchFeature(session), new S3WriteFeature(session), cryptomator).touch(file, new TransferStatus());
        assertTrue(new CryptoFindFeature(session, new DefaultFindFeature(session), cryptomator).find(file));
        // copy file
        final Path fileRenamed = new Path(folder, "f1", EnumSet.of(Path.Type.file));
        new CopyWorker(Collections.singletonMap(file, fileRenamed), new TestSessionPool(session, registry), new DisabledProgressListener()).run(session);
        assertTrue(new CryptoFindFeature(session, new S3FindFeature(session), cryptomator).find(file));
        assertTrue(new CryptoFindFeature(session, new S3FindFeature(session), cryptomator).find(fileRenamed));
        // copy folder
        final Path folderRenamed = new Path(vault, new AlphanumericRandomStringService().random(), EnumSet.of(Path.Type.directory));
        new CopyWorker(Collections.singletonMap(folder, folderRenamed), new TestSessionPool(session, registry), new DisabledProgressListener()).run(session);
        assertTrue(new CryptoFindFeature(session, new S3FindFeature(session), cryptomator).find(folder));
        assertTrue(new CryptoFindFeature(session, new S3FindFeature(session), cryptomator).find(folderRenamed));
        final Path fileRenamedInRenamedFolder = new Path(folderRenamed, "f1", EnumSet.of(Path.Type.file));
        assertTrue(new CryptoFindFeature(session, new DefaultFindFeature(session), cryptomator).find(fileRenamedInRenamedFolder));
        registry.clear();
        new DeleteWorker(new DisabledLoginCallback(), Collections.singletonList(vault), PathCache.empty(), new DisabledProgressListener()).run(session);
        session.close();
    }

    @Test
    public void testCopyFileIntoVault() throws Exception {
        final Host host = new Host(new S3Protocol(), new S3Protocol().getDefaultHostname(), new Credentials(
                System.getProperties().getProperty("s3.key"), System.getProperties().getProperty("s3.secret")
        ));
        final S3Session session = new S3Session(host);
        session.open(new DisabledHostKeyCallback());
        session.login(new DisabledPasswordStore(), new DisabledLoginCallback(), new DisabledCancelCallback(), PathCache.empty());
        final Path home = new Path("test-us-east-1-cyberduck", EnumSet.of(Path.Type.directory, Path.Type.volume));
        final Path vault = new Path(home, new AlphanumericRandomStringService().random(), EnumSet.of(Path.Type.directory));
        final Path cleartextFile = new Path(home, new AlphanumericRandomStringService().random(), EnumSet.of(Path.Type.file));
        new S3TouchFeature(session).touch(cleartextFile, new TransferStatus());
        assertTrue(new S3FindFeature(session).find(cleartextFile));
        final Path encryptedFolder = new Path(vault, new AlphanumericRandomStringService().random(), EnumSet.of(Path.Type.directory));
        final Path encryptedFile = new Path(encryptedFolder, new AlphanumericRandomStringService().random(), EnumSet.of(Path.Type.file));
        final CryptoVault cryptomator = new CryptoVault(vault, new DisabledPasswordStore());
        cryptomator.create(session, null, new VaultCredentials("test"));
        final DefaultVaultRegistry registry = new DefaultVaultRegistry(new DisabledPasswordStore(), new DisabledPasswordCallback(), cryptomator);
        session.withRegistry(registry);
        new CryptoDirectoryFeature<StorageObject>(session, new S3DirectoryFeature(session, new S3WriteFeature(session)), new S3WriteFeature(session), cryptomator).mkdir(encryptedFolder, null, new TransferStatus());
        assertTrue(new CryptoFindFeature(session, new DefaultFindFeature(session), cryptomator).find(encryptedFolder));
        // copy file into vault
        final CopyWorker worker = new CopyWorker(Collections.singletonMap(cleartextFile, encryptedFile), new TestSessionPool(session, registry), new DisabledProgressListener());
        worker.run(session);
        assertTrue(new S3FindFeature(session).find(cleartextFile));
        assertTrue(new CryptoFindFeature(session, new S3FindFeature(session), cryptomator).find(encryptedFile));
        registry.clear();
        new DeleteWorker(new DisabledLoginCallback(), Collections.singletonList(vault), PathCache.empty(), new DisabledProgressListener()).run(session);
        session.close();
    }

    @Test
    public void testCopyDirectoryIntoVault() throws Exception {
        final Host host = new Host(new S3Protocol(), new S3Protocol().getDefaultHostname(), new Credentials(
                System.getProperties().getProperty("s3.key"), System.getProperties().getProperty("s3.secret")
        ));
        final S3Session session = new S3Session(host);
        session.open(new DisabledHostKeyCallback());
        session.login(new DisabledPasswordStore(), new DisabledLoginCallback(), new DisabledCancelCallback(), PathCache.empty());
        final Path home = new Path("test-us-east-1-cyberduck", EnumSet.of(Path.Type.directory, Path.Type.volume));
        final Path vault = new Path(home, new AlphanumericRandomStringService().random(), EnumSet.of(Path.Type.directory));
        final Path cleartextFolder = new Path(home, new AlphanumericRandomStringService().random(), EnumSet.of(Path.Type.directory));
        final Path cleartextFile = new Path(cleartextFolder, new AlphanumericRandomStringService().random(), EnumSet.of(Path.Type.file));
        new S3DirectoryFeature(session, new S3WriteFeature(session)).mkdir(cleartextFolder, null, new TransferStatus());
        new S3TouchFeature(session).touch(cleartextFile, new TransferStatus());
        assertTrue(new S3FindFeature(session).find(cleartextFolder));
        assertTrue(new S3FindFeature(session).find(cleartextFile));
        final CryptoVault cryptomator = new CryptoVault(vault, new DisabledPasswordStore());
        cryptomator.create(session, null, new VaultCredentials("test"));
        final DefaultVaultRegistry registry = new DefaultVaultRegistry(new DisabledPasswordStore(), new DisabledPasswordCallback(), cryptomator);
        session.withRegistry(registry);
        // move directory into vault
        final Path encryptedFolder = new Path(vault, cleartextFolder.getName(), EnumSet.of(Path.Type.directory));
        final Path encryptedFile = new Path(encryptedFolder, cleartextFile.getName(), EnumSet.of(Path.Type.file));
        final CopyWorker worker = new CopyWorker(Collections.singletonMap(cleartextFolder, encryptedFolder), new TestSessionPool(session, registry), new DisabledProgressListener());
        worker.run(session);
        assertTrue(new CryptoFindFeature(session, new DefaultFindFeature(session), cryptomator).find(encryptedFolder));
        assertTrue(new CryptoFindFeature(session, new DefaultFindFeature(session), cryptomator).find(encryptedFile));
        assertTrue(new S3FindFeature(session).find(cleartextFolder));
        assertTrue(new S3FindFeature(session).find(cleartextFile));
        registry.clear();
        new DeleteWorker(new DisabledLoginCallback(), Collections.singletonList(vault), PathCache.empty(), new DisabledProgressListener()).run(session);
        session.close();
    }

    @Test
    public void testCopyFileOutsideVault() throws Exception {
        final Host host = new Host(new S3Protocol(), new S3Protocol().getDefaultHostname(), new Credentials(
                System.getProperties().getProperty("s3.key"), System.getProperties().getProperty("s3.secret")
        ));
        final S3Session session = new S3Session(host);
        session.open(new DisabledHostKeyCallback());
        session.login(new DisabledPasswordStore(), new DisabledLoginCallback(), new DisabledCancelCallback(), PathCache.empty());
        final Path home = new Path("test-us-east-1-cyberduck", EnumSet.of(Path.Type.directory, Path.Type.volume));
        final Path vault = new Path(home, new AlphanumericRandomStringService().random(), EnumSet.of(Path.Type.directory));
        final Path clearFolder = new Path(home, new AlphanumericRandomStringService().random(), EnumSet.of(Path.Type.directory));
        new S3DirectoryFeature(session, new S3WriteFeature(session)).mkdir(clearFolder, null, new TransferStatus());
        final Path encryptedFolder = new Path(vault, new AlphanumericRandomStringService().random(), EnumSet.of(Path.Type.directory));
        final Path encryptedFile = new Path(encryptedFolder, new AlphanumericRandomStringService().random(), EnumSet.of(Path.Type.file));
        final CryptoVault cryptomator = new CryptoVault(vault, new DisabledPasswordStore());
        cryptomator.create(session, null, new VaultCredentials("test"));
        final DefaultVaultRegistry registry = new DefaultVaultRegistry(new DisabledPasswordStore(), new DisabledPasswordCallback(), cryptomator);
        session.withRegistry(registry);
        new CryptoDirectoryFeature<StorageObject>(session, new S3DirectoryFeature(session, new S3WriteFeature(session)), new S3WriteFeature(session), cryptomator).mkdir(encryptedFolder, null, new TransferStatus());
        assertTrue(new CryptoFindFeature(session, new DefaultFindFeature(session), cryptomator).find(encryptedFolder));
        new CryptoTouchFeature<StorageObject>(session, new S3TouchFeature(session), new S3WriteFeature(session), cryptomator).touch(encryptedFile, new TransferStatus());
        assertTrue(new CryptoFindFeature(session, new DefaultFindFeature(session), cryptomator).find(encryptedFile));
        // move file outside vault
        final Path cleartextFile = new Path(clearFolder, new AlphanumericRandomStringService().random(), EnumSet.of(Path.Type.file));
        final CopyWorker worker = new CopyWorker(Collections.singletonMap(encryptedFile, cleartextFile), new TestSessionPool(session, registry), new DisabledProgressListener());
        worker.run(session);
        assertTrue(new CryptoFindFeature(session, new S3FindFeature(session), cryptomator).find(encryptedFile));
        assertTrue(new S3FindFeature(session).find(cleartextFile));
        registry.clear();
        new DeleteWorker(new DisabledLoginCallback(), Arrays.asList(vault, clearFolder), PathCache.empty(), new DisabledProgressListener()).run(session);
        session.close();
    }

    @Test
    public void testCopyDirectoryOutsideVault() throws Exception {
        final Host host = new Host(new S3Protocol(), new S3Protocol().getDefaultHostname(), new Credentials(
                System.getProperties().getProperty("s3.key"), System.getProperties().getProperty("s3.secret")
        ));
        final S3Session session = new S3Session(host);
        session.open(new DisabledHostKeyCallback());
        session.login(new DisabledPasswordStore(), new DisabledLoginCallback(), new DisabledCancelCallback(), PathCache.empty());
        final Path home = new Path("test-us-east-1-cyberduck", EnumSet.of(Path.Type.directory, Path.Type.volume));
        final Path vault = new Path(home, new AlphanumericRandomStringService().random(), EnumSet.of(Path.Type.directory));
        final Path encryptedFolder = new Path(vault, new AlphanumericRandomStringService().random(), EnumSet.of(Path.Type.directory));
        final Path encryptedFile = new Path(encryptedFolder, new AlphanumericRandomStringService().random(), EnumSet.of(Path.Type.file));
        final CryptoVault cryptomator = new CryptoVault(vault, new DisabledPasswordStore());
        cryptomator.create(session, null, new VaultCredentials("test"));
        final DefaultVaultRegistry registry = new DefaultVaultRegistry(new DisabledPasswordStore(), new DisabledPasswordCallback(), cryptomator);
        session.withRegistry(registry);
        new CryptoDirectoryFeature<StorageObject>(session, new S3DirectoryFeature(session, new S3WriteFeature(session)), new S3WriteFeature(session), cryptomator).mkdir(encryptedFolder, null, new TransferStatus());
        assertTrue(new CryptoFindFeature(session, new DefaultFindFeature(session), cryptomator).find(encryptedFolder));
        new CryptoTouchFeature<StorageObject>(session, new S3TouchFeature(session), new S3WriteFeature(session), cryptomator).touch(encryptedFile, new TransferStatus());
        assertTrue(new CryptoFindFeature(session, new DefaultFindFeature(session), cryptomator).find(encryptedFile));
        // copy directory outside vault
        final Path cleartextFolder = new Path(home, new AlphanumericRandomStringService().random(), EnumSet.of(Path.Type.directory));
        final CopyWorker worker = new CopyWorker(Collections.singletonMap(encryptedFolder, cleartextFolder), new TestSessionPool(session, registry), new DisabledProgressListener());
        worker.run(session);
        assertTrue(new CryptoFindFeature(session, new S3FindFeature(session), cryptomator).find(encryptedFolder));
        assertTrue(new CryptoFindFeature(session, new S3FindFeature(session), cryptomator).find(encryptedFile));
        assertTrue(new S3FindFeature(session).find(cleartextFolder));
        final Path fileRenamed = new Path(cleartextFolder, encryptedFile.getName(), EnumSet.of(Path.Type.file));
        assertTrue(new S3FindFeature(session).find(fileRenamed));
        registry.clear();
        new DeleteWorker(new DisabledLoginCallback(), Arrays.asList(cleartextFolder, vault), PathCache.empty(), new DisabledProgressListener()).run(session);
        session.close();
    }

    private static class TestSessionPool implements SessionPool {
        private final Session<?> session;
        private final VaultRegistry registry;

        public TestSessionPool(final Session<?> session, final VaultRegistry registry) {
            this.session = session;
            this.registry = registry;
        }

        @Override
        public Session<?> borrow(final BackgroundActionState callback) throws BackgroundException {
            return session;
        }

        @Override
        public void release(final Session<?> session, final BackgroundException failure) {
        }

        @Override
        public void evict() {
        }

        @Override
        public Host getHost() {
            return session.getHost();
        }

        @Override
        public PathCache getCache() {
            return PathCache.empty();
        }

        @Override
        public VaultRegistry getVault() {
            return registry;
        }

        @Override
        public Session.State getState() {
            return Session.State.open;
        }

        @Override
        public <T> T getFeature(final Class<T> type) {
            return session.getFeature(type);
        }

        @Override
        public void shutdown() {
        }
    }
}
