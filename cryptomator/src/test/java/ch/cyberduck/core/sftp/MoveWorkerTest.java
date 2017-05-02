package ch.cyberduck.core.sftp;

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

import ch.cyberduck.core.AlphanumericRandomStringService;
import ch.cyberduck.core.Credentials;
import ch.cyberduck.core.DisabledCancelCallback;
import ch.cyberduck.core.DisabledHostKeyCallback;
import ch.cyberduck.core.DisabledLoginCallback;
import ch.cyberduck.core.DisabledPasswordCallback;
import ch.cyberduck.core.DisabledPasswordStore;
import ch.cyberduck.core.DisabledProgressListener;
import ch.cyberduck.core.Host;
import ch.cyberduck.core.LoginOptions;
import ch.cyberduck.core.Path;
import ch.cyberduck.core.PathCache;
import ch.cyberduck.core.cryptomator.CryptoDeleteFeature;
import ch.cyberduck.core.cryptomator.CryptoDirectoryFeature;
import ch.cyberduck.core.cryptomator.CryptoFindFeature;
import ch.cyberduck.core.cryptomator.CryptoTouchFeature;
import ch.cyberduck.core.cryptomator.CryptoVault;
import ch.cyberduck.core.exception.LoginCanceledException;
import ch.cyberduck.core.features.Delete;
import ch.cyberduck.core.shared.DefaultFindFeature;
import ch.cyberduck.core.shared.DefaultTouchFeature;
import ch.cyberduck.core.shared.DefaultUploadFeature;
import ch.cyberduck.core.transfer.TransferStatus;
import ch.cyberduck.core.vault.DefaultVaultRegistry;
import ch.cyberduck.core.worker.MoveWorker;
import ch.cyberduck.test.IntegrationTest;

import org.apache.commons.lang3.RandomStringUtils;
import org.junit.Test;
import org.junit.experimental.categories.Category;

import java.util.Arrays;
import java.util.Collections;
import java.util.EnumSet;
import java.util.UUID;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

@Category(IntegrationTest.class)
public class MoveWorkerTest {

    @Test
    public void testMoveSameFolderCryptomator() throws Exception {
        final Host host = new Host(new SFTPProtocol(), "test.cyberduck.ch", new Credentials(
                System.getProperties().getProperty("sftp.user"), System.getProperties().getProperty("sftp.password")
        ));
        final SFTPSession session = new SFTPSession(host);
        session.open(new DisabledHostKeyCallback());
        session.login(new DisabledPasswordStore(), new DisabledLoginCallback(), new DisabledCancelCallback(), PathCache.empty());
        final Path home = new SFTPHomeDirectoryService(session).find();
        final Path vault = new Path(home, UUID.randomUUID().toString(), EnumSet.of(Path.Type.directory));
        final Path source = new Path(vault, UUID.randomUUID().toString(), EnumSet.of(Path.Type.file));
        final Path target = new Path(vault, UUID.randomUUID().toString(), EnumSet.of(Path.Type.file));
        final CryptoVault cryptomator = new CryptoVault(vault, new DisabledPasswordStore()).create(session, null, new DisabledPasswordCallback() {
            @Override
            public void prompt(final Credentials credentials, final String title, final String reason, final LoginOptions options) throws LoginCanceledException {
                credentials.setPassword("vault");
            }
        });
        session.withRegistry(new DefaultVaultRegistry(new DisabledPasswordStore(), new DisabledPasswordCallback(), cryptomator));
        new CryptoTouchFeature<Void>(session, new DefaultTouchFeature<Void>(new DefaultUploadFeature<Void>(new SFTPWriteFeature(session))), new SFTPWriteFeature(session), cryptomator).touch(source, new TransferStatus());
        assertTrue(new CryptoFindFeature(session, new DefaultFindFeature(session), cryptomator).find(source));
        final MoveWorker worker = new MoveWorker(Collections.singletonMap(source, target), new DisabledProgressListener(), PathCache.empty());
        worker.run(session);
        assertFalse(new CryptoFindFeature(session, new DefaultFindFeature(session), cryptomator).find(source));
        assertTrue(new CryptoFindFeature(session, new DefaultFindFeature(session), cryptomator).find(target));
        new CryptoDeleteFeature(session, new SFTPDeleteFeature(session), cryptomator).delete(Collections.singletonList(target), new DisabledLoginCallback(), new Delete.DisabledCallback());
        session.close();
    }

    @Test
    public void testMoveToDifferentFolderCryptomator() throws Exception {
        final Host host = new Host(new SFTPProtocol(), "test.cyberduck.ch", new Credentials(
                System.getProperties().getProperty("sftp.user"), System.getProperties().getProperty("sftp.password")
        ));
        final SFTPSession session = new SFTPSession(host);
        session.open(new DisabledHostKeyCallback());
        session.login(new DisabledPasswordStore(), new DisabledLoginCallback(), new DisabledCancelCallback(), PathCache.empty());
        final Path home = new SFTPHomeDirectoryService(session).find();
        final Path vault = new Path(home, UUID.randomUUID().toString(), EnumSet.of(Path.Type.directory));
        final Path source = new Path(vault, UUID.randomUUID().toString(), EnumSet.of(Path.Type.file));
        final Path targetFolder = new Path(vault, UUID.randomUUID().toString(), EnumSet.of(Path.Type.directory));
        final Path target = new Path(targetFolder, UUID.randomUUID().toString(), EnumSet.of(Path.Type.file));
        final CryptoVault cryptomator = new CryptoVault(vault, new DisabledPasswordStore()).create(session, null, new DisabledPasswordCallback() {
            @Override
            public void prompt(final Credentials credentials, final String title, final String reason, final LoginOptions options) throws LoginCanceledException {
                credentials.setPassword("vault");
            }
        });
        session.withRegistry(new DefaultVaultRegistry(new DisabledPasswordStore(), new DisabledPasswordCallback(), cryptomator));
        new CryptoTouchFeature<Void>(session, new DefaultTouchFeature<Void>(new DefaultUploadFeature<Void>(new SFTPWriteFeature(session))), new SFTPWriteFeature(session), cryptomator).touch(source, new TransferStatus());
        assertTrue(new CryptoFindFeature(session, new DefaultFindFeature(session), cryptomator).find(source));
        new CryptoDirectoryFeature<Void>(session, new SFTPDirectoryFeature(session), new SFTPWriteFeature(session), cryptomator).mkdir(targetFolder, null, new TransferStatus());
        assertTrue(new CryptoFindFeature(session, new DefaultFindFeature(session), cryptomator).find(targetFolder));
        final MoveWorker worker = new MoveWorker(Collections.singletonMap(source, target), new DisabledProgressListener(), PathCache.empty());
        worker.run(session);
        assertFalse(new CryptoFindFeature(session, new DefaultFindFeature(session), cryptomator).find(source));
        assertTrue(new CryptoFindFeature(session, new DefaultFindFeature(session), cryptomator).find(target));
        new CryptoDeleteFeature(session, new SFTPDeleteFeature(session), cryptomator).delete(Collections.singletonList(target), new DisabledLoginCallback(), new Delete.DisabledCallback());
        session.close();
    }

    @Test
    public void testMoveToDifferentFolderLongFilenameCryptomator() throws Exception {
        final Host host = new Host(new SFTPProtocol(), "test.cyberduck.ch", new Credentials(
                System.getProperties().getProperty("sftp.user"), System.getProperties().getProperty("sftp.password")
        ));
        final SFTPSession session = new SFTPSession(host);
        session.open(new DisabledHostKeyCallback());
        session.login(new DisabledPasswordStore(), new DisabledLoginCallback(), new DisabledCancelCallback(), PathCache.empty());
        final Path home = new SFTPHomeDirectoryService(session).find();
        final Path vault = new Path(home, UUID.randomUUID().toString(), EnumSet.of(Path.Type.directory));
        final Path source = new Path(vault, RandomStringUtils.random(130), EnumSet.of(Path.Type.file));
        final Path targetFolder = new Path(vault, RandomStringUtils.random(130), EnumSet.of(Path.Type.directory));
        final Path target = new Path(targetFolder, RandomStringUtils.random(130), EnumSet.of(Path.Type.file));
        final CryptoVault cryptomator = new CryptoVault(vault, new DisabledPasswordStore()).create(session, null, new DisabledPasswordCallback() {
            @Override
            public void prompt(final Credentials credentials, final String title, final String reason, final LoginOptions options) throws LoginCanceledException {
                credentials.setPassword("vault");
            }
        });
        session.withRegistry(new DefaultVaultRegistry(new DisabledPasswordStore(), new DisabledPasswordCallback(), cryptomator));
        new CryptoTouchFeature<Void>(session, new DefaultTouchFeature<Void>(new DefaultUploadFeature<Void>(new SFTPWriteFeature(session))), new SFTPWriteFeature(session), cryptomator).touch(source, new TransferStatus());
        assertTrue(new CryptoFindFeature(session, new DefaultFindFeature(session), cryptomator).find(source));
        new CryptoDirectoryFeature<Void>(session, new SFTPDirectoryFeature(session), new SFTPWriteFeature(session), cryptomator).mkdir(targetFolder, null, new TransferStatus());
        assertTrue(new CryptoFindFeature(session, new DefaultFindFeature(session), cryptomator).find(targetFolder));
        final MoveWorker worker = new MoveWorker(Collections.singletonMap(source, target), new DisabledProgressListener(), PathCache.empty());
        worker.run(session);
        assertFalse(new CryptoFindFeature(session, new DefaultFindFeature(session), cryptomator).find(source));
        assertTrue(new CryptoFindFeature(session, new DefaultFindFeature(session), cryptomator).find(target));
        new CryptoDeleteFeature(session, new SFTPDeleteFeature(session), cryptomator).delete(Collections.singletonList(target), new DisabledLoginCallback(), new Delete.DisabledCallback());
        session.close();
    }

    @Test
    public void testMoveFolder() throws Exception {
        final Host host = new Host(new SFTPProtocol(), "test.cyberduck.ch", new Credentials(
                System.getProperties().getProperty("sftp.user"), System.getProperties().getProperty("sftp.password")
        ));
        final SFTPSession session = new SFTPSession(host);
        session.open(new DisabledHostKeyCallback());
        session.login(new DisabledPasswordStore(), new DisabledLoginCallback(), new DisabledCancelCallback(), PathCache.empty());
        final Path home = new SFTPHomeDirectoryService(session).find();
        final Path vault = new Path(home, new AlphanumericRandomStringService().random(), EnumSet.of(Path.Type.directory));
        final Path folder = new Path(vault, new AlphanumericRandomStringService().random(), EnumSet.of(Path.Type.directory));
        final Path file = new Path(folder, new AlphanumericRandomStringService().random(), EnumSet.of(Path.Type.file));
        final CryptoVault cryptomator = new CryptoVault(vault, new DisabledPasswordStore()).create(session, null, new DisabledPasswordCallback() {
            @Override
            public void prompt(final Credentials credentials, final String title, final String reason, final LoginOptions options) throws LoginCanceledException {
                credentials.setPassword("vault");
            }
        });
        session.withRegistry(new DefaultVaultRegistry(new DisabledPasswordStore(), new DisabledPasswordCallback(), cryptomator));
        new CryptoDirectoryFeature<Void>(session, new SFTPDirectoryFeature(session), new SFTPWriteFeature(session), cryptomator).mkdir(folder, null, new TransferStatus());
        assertTrue(new CryptoFindFeature(session, new DefaultFindFeature(session), cryptomator).find(folder));
        new CryptoTouchFeature<Void>(session, new DefaultTouchFeature<Void>(new DefaultUploadFeature<Void>(new SFTPWriteFeature(session))), new SFTPWriteFeature(session), cryptomator).touch(file, new TransferStatus());
        assertTrue(new CryptoFindFeature(session, new DefaultFindFeature(session), cryptomator).find(file));
        // rename file
        final Path fileRenamed = new Path(folder, new AlphanumericRandomStringService().random(), EnumSet.of(Path.Type.file));
        new MoveWorker(Collections.singletonMap(file, fileRenamed), new DisabledProgressListener(), PathCache.empty()).run(session);
        assertFalse(new CryptoFindFeature(session, new SFTPFindFeature(session), cryptomator).find(file));
        assertTrue(new CryptoFindFeature(session, new SFTPFindFeature(session), cryptomator).find(fileRenamed));
        // rename folder
        final Path folderRenamed = new Path(vault, new AlphanumericRandomStringService().random(), EnumSet.of(Path.Type.directory));
        new MoveWorker(Collections.singletonMap(folder, folderRenamed), new DisabledProgressListener(), PathCache.empty()).run(session);
        assertFalse(new CryptoFindFeature(session, new SFTPFindFeature(session), cryptomator).find(folder));
        assertTrue(new CryptoFindFeature(session, new SFTPFindFeature(session), cryptomator).find(folderRenamed));
        new CryptoDeleteFeature(session, new SFTPDeleteFeature(session), cryptomator).delete(Collections.singletonList(fileRenamed), new DisabledLoginCallback(), new Delete.DisabledCallback());
        new CryptoDeleteFeature(session, new SFTPDeleteFeature(session), cryptomator).delete(Collections.singletonList(folderRenamed), new DisabledLoginCallback(), new Delete.DisabledCallback());
        session.close();
    }

    @Test
    public void testMoveFileIntoVault() throws Exception {
        final Host host = new Host(new SFTPProtocol(), "test.cyberduck.ch", new Credentials(
                System.getProperties().getProperty("sftp.user"), System.getProperties().getProperty("sftp.password")
        ));
        final SFTPSession session = new SFTPSession(host);
        session.open(new DisabledHostKeyCallback());
        session.login(new DisabledPasswordStore(), new DisabledLoginCallback(), new DisabledCancelCallback(), PathCache.empty());
        final Path home = new SFTPHomeDirectoryService(session).find();
        final Path vault = new Path(home, new AlphanumericRandomStringService().random(), EnumSet.of(Path.Type.directory));
        final Path clearFile = new Path(home, new AlphanumericRandomStringService().random(), EnumSet.of(Path.Type.file));
        new SFTPTouchFeature(session).touch(clearFile, new TransferStatus());
        assertTrue(new SFTPFindFeature(session).find(clearFile));
        final Path encryptedFolder = new Path(vault, new AlphanumericRandomStringService().random(), EnumSet.of(Path.Type.directory));
        final Path encryptedFile = new Path(encryptedFolder, new AlphanumericRandomStringService().random(), EnumSet.of(Path.Type.file));
        final CryptoVault cryptomator = new CryptoVault(vault, new DisabledPasswordStore()).create(session, null, new DisabledPasswordCallback() {
            @Override
            public void prompt(final Credentials credentials, final String title, final String reason, final LoginOptions options) throws LoginCanceledException {
                credentials.setPassword("vault");
            }
        });
        final DefaultVaultRegistry registry = new DefaultVaultRegistry(new DisabledPasswordStore(), new DisabledPasswordCallback(), cryptomator);
        session.withRegistry(registry);
        new CryptoDirectoryFeature<Void>(session, new SFTPDirectoryFeature(session), new SFTPWriteFeature(session), cryptomator).mkdir(encryptedFolder, null, new TransferStatus());
        assertTrue(new CryptoFindFeature(session, new DefaultFindFeature(session), cryptomator).find(encryptedFolder));
        // move file into vault
        final MoveWorker worker = new MoveWorker(Collections.singletonMap(clearFile, encryptedFile), new DisabledProgressListener(), PathCache.empty());
        worker.run(session);
        assertFalse(new SFTPFindFeature(session).find(clearFile));
        assertTrue(new CryptoFindFeature(session, new SFTPFindFeature(session), cryptomator).find(encryptedFile));
        new CryptoDeleteFeature(session, new SFTPDeleteFeature(session), cryptomator).delete(Arrays.asList(encryptedFolder, encryptedFile), new DisabledLoginCallback(), new Delete.DisabledCallback());
        session.close();
        registry.clear();
    }

    @Test
    public void testMoveDirectoryIntoVault() throws Exception {
        final Host host = new Host(new SFTPProtocol(), "test.cyberduck.ch", new Credentials(
                System.getProperties().getProperty("sftp.user"), System.getProperties().getProperty("sftp.password")
        ));
        final SFTPSession session = new SFTPSession(host);
        session.open(new DisabledHostKeyCallback());
        session.login(new DisabledPasswordStore(), new DisabledLoginCallback(), new DisabledCancelCallback(), PathCache.empty());
        final Path home = new SFTPHomeDirectoryService(session).find();
        final Path vault = new Path(home, new AlphanumericRandomStringService().random(), EnumSet.of(Path.Type.directory));
        final Path clearFolder = new Path(home, new AlphanumericRandomStringService().random(), EnumSet.of(Path.Type.directory));
        final Path clearFile = new Path(clearFolder, new AlphanumericRandomStringService().random(), EnumSet.of(Path.Type.file));
        new SFTPDirectoryFeature(session).mkdir(clearFolder, null, new TransferStatus());
        new SFTPTouchFeature(session).touch(clearFile, new TransferStatus());
        assertTrue(new SFTPFindFeature(session).find(clearFolder));
        assertTrue(new SFTPFindFeature(session).find(clearFile));
        final CryptoVault cryptomator = new CryptoVault(vault, new DisabledPasswordStore()).create(session, null, new DisabledPasswordCallback() {
            @Override
            public void prompt(final Credentials credentials, final String title, final String reason, final LoginOptions options) throws LoginCanceledException {
                credentials.setPassword("vault");
            }
        });
        final DefaultVaultRegistry registry = new DefaultVaultRegistry(new DisabledPasswordStore(), new DisabledPasswordCallback(), cryptomator);
        session.withRegistry(registry);
        // move directory into vault
        final Path encryptedFolder = new Path(vault, clearFolder.getName(), EnumSet.of(Path.Type.directory));
        final Path encryptedFile = new Path(encryptedFolder, clearFile.getName(), EnumSet.of(Path.Type.file));
        final MoveWorker worker = new MoveWorker(Collections.singletonMap(clearFolder, encryptedFolder), new DisabledProgressListener(), PathCache.empty());
        worker.run(session);
        assertTrue(new CryptoFindFeature(session, new DefaultFindFeature(session), cryptomator).find(encryptedFolder));
        assertTrue(new CryptoFindFeature(session, new DefaultFindFeature(session), cryptomator).find(encryptedFile));
        assertFalse(new SFTPFindFeature(session).find(clearFolder));
        assertFalse(new SFTPFindFeature(session).find(clearFile));
        new CryptoDeleteFeature(session, new SFTPDeleteFeature(session), cryptomator).delete(Arrays.asList(encryptedFolder, encryptedFile), new DisabledLoginCallback(), new Delete.DisabledCallback());
        session.close();
        registry.clear();
    }

    @Test
    public void testMoveFileOutsideVault() throws Exception {
        final Host host = new Host(new SFTPProtocol(), "test.cyberduck.ch", new Credentials(
                System.getProperties().getProperty("sftp.user"), System.getProperties().getProperty("sftp.password")
        ));
        final SFTPSession session = new SFTPSession(host);
        session.open(new DisabledHostKeyCallback());
        session.login(new DisabledPasswordStore(), new DisabledLoginCallback(), new DisabledCancelCallback(), PathCache.empty());
        final Path home = new SFTPHomeDirectoryService(session).find();
        final Path vault = new Path(home, new AlphanumericRandomStringService().random(), EnumSet.of(Path.Type.directory));
        final Path clearFolder = new Path(home, new AlphanumericRandomStringService().random(), EnumSet.of(Path.Type.directory));
        new SFTPDirectoryFeature(session).mkdir(clearFolder, null, new TransferStatus());
        final Path encryptedFolder = new Path(vault, new AlphanumericRandomStringService().random(), EnumSet.of(Path.Type.directory));
        final Path encryptedFile = new Path(encryptedFolder, new AlphanumericRandomStringService().random(), EnumSet.of(Path.Type.file));
        final CryptoVault cryptomator = new CryptoVault(vault, new DisabledPasswordStore()).create(session, null, new DisabledPasswordCallback() {
            @Override
            public void prompt(final Credentials credentials, final String title, final String reason, final LoginOptions options) throws LoginCanceledException {
                credentials.setPassword("vault");
            }
        });
        final DefaultVaultRegistry registry = new DefaultVaultRegistry(new DisabledPasswordStore(), new DisabledPasswordCallback(), cryptomator);
        session.withRegistry(registry);
        new CryptoDirectoryFeature<Void>(session, new SFTPDirectoryFeature(session), new SFTPWriteFeature(session), cryptomator).mkdir(encryptedFolder, null, new TransferStatus());
        assertTrue(new CryptoFindFeature(session, new DefaultFindFeature(session), cryptomator).find(encryptedFolder));
        new CryptoTouchFeature<Void>(session, new DefaultTouchFeature<Void>(new DefaultUploadFeature<Void>(new SFTPWriteFeature(session))), new SFTPWriteFeature(session), cryptomator).touch(encryptedFile, new TransferStatus());
        assertTrue(new CryptoFindFeature(session, new DefaultFindFeature(session), cryptomator).find(encryptedFile));
        // move file outside vault
        final Path fileRenamed = new Path(clearFolder, new AlphanumericRandomStringService().random(), EnumSet.of(Path.Type.file));
        final MoveWorker worker = new MoveWorker(Collections.singletonMap(encryptedFile, fileRenamed), new DisabledProgressListener(), PathCache.empty());
        worker.run(session);
        assertFalse(new CryptoFindFeature(session, new SFTPFindFeature(session), cryptomator).find(encryptedFile));
        assertTrue(new SFTPFindFeature(session).find(fileRenamed));
        new CryptoDeleteFeature(session, new SFTPDeleteFeature(session), cryptomator).delete(Collections.singletonList(encryptedFolder), new DisabledLoginCallback(), new Delete.DisabledCallback());
        new SFTPDeleteFeature(session).delete(Arrays.asList(fileRenamed, clearFolder), new DisabledLoginCallback(), new Delete.DisabledCallback());
        session.close();
        registry.clear();
    }

    @Test
    public void testMoveDirectoryOutsideVault() throws Exception {
        final Host host = new Host(new SFTPProtocol(), "test.cyberduck.ch", new Credentials(
                System.getProperties().getProperty("sftp.user"), System.getProperties().getProperty("sftp.password")
        ));
        final SFTPSession session = new SFTPSession(host);
        session.open(new DisabledHostKeyCallback());
        session.login(new DisabledPasswordStore(), new DisabledLoginCallback(), new DisabledCancelCallback(), PathCache.empty());
        final Path home = new SFTPHomeDirectoryService(session).find();
        final Path vault = new Path(home, new AlphanumericRandomStringService().random(), EnumSet.of(Path.Type.directory));
        final Path encryptedFolder = new Path(vault, new AlphanumericRandomStringService().random(), EnumSet.of(Path.Type.directory));
        final Path encryptedFile = new Path(encryptedFolder, new AlphanumericRandomStringService().random(), EnumSet.of(Path.Type.file));
        final CryptoVault cryptomator = new CryptoVault(vault, new DisabledPasswordStore()).create(session, null, new DisabledPasswordCallback() {
            @Override
            public void prompt(final Credentials credentials, final String title, final String reason, final LoginOptions options) throws LoginCanceledException {
                credentials.setPassword("vault");
            }
        });
        final DefaultVaultRegistry registry = new DefaultVaultRegistry(new DisabledPasswordStore(), new DisabledPasswordCallback(), cryptomator);
        session.withRegistry(registry);
        new CryptoDirectoryFeature<Void>(session, new SFTPDirectoryFeature(session), new SFTPWriteFeature(session), cryptomator).mkdir(encryptedFolder, null, new TransferStatus());
        assertTrue(new CryptoFindFeature(session, new DefaultFindFeature(session), cryptomator).find(encryptedFolder));
        new CryptoTouchFeature<Void>(session, new DefaultTouchFeature<Void>(new DefaultUploadFeature<Void>(new SFTPWriteFeature(session))), new SFTPWriteFeature(session), cryptomator).touch(encryptedFile, new TransferStatus());
        assertTrue(new CryptoFindFeature(session, new DefaultFindFeature(session), cryptomator).find(encryptedFile));
        // move directory outside vault
        final Path directoryRenamed = new Path(home, new AlphanumericRandomStringService().random(), EnumSet.of(Path.Type.directory));
        final MoveWorker worker = new MoveWorker(Collections.singletonMap(encryptedFolder, directoryRenamed), new DisabledProgressListener(), PathCache.empty());
        worker.run(session);
        assertFalse(new CryptoFindFeature(session, new SFTPFindFeature(session), cryptomator).find(encryptedFolder));
        assertFalse(new CryptoFindFeature(session, new SFTPFindFeature(session), cryptomator).find(encryptedFile));
        assertTrue(new SFTPFindFeature(session).find(directoryRenamed));
        final Path fileRenamed = new Path(directoryRenamed, encryptedFile.getName(), EnumSet.of(Path.Type.file));
        assertTrue(new SFTPFindFeature(session).find(fileRenamed));
        new CryptoDeleteFeature(session, new SFTPDeleteFeature(session), cryptomator).delete(Arrays.asList(directoryRenamed, fileRenamed), new DisabledLoginCallback(), new Delete.DisabledCallback());
        session.close();
        registry.clear();
    }
}
