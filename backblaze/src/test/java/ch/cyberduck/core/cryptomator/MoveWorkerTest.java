package ch.cyberduck.core.cryptomator;

/*
 * Copyright (c) 2002-2018 iterate GmbH. All rights reserved.
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
import ch.cyberduck.core.DisabledLoginCallback;
import ch.cyberduck.core.DisabledPasswordCallback;
import ch.cyberduck.core.DisabledPasswordStore;
import ch.cyberduck.core.DisabledProgressListener;
import ch.cyberduck.core.Path;
import ch.cyberduck.core.PathCache;
import ch.cyberduck.core.b2.AbstractB2Test;
import ch.cyberduck.core.b2.B2DeleteFeature;
import ch.cyberduck.core.b2.B2DirectoryFeature;
import ch.cyberduck.core.b2.B2TouchFeature;
import ch.cyberduck.core.b2.B2VersionIdProvider;
import ch.cyberduck.core.b2.B2WriteFeature;
import ch.cyberduck.core.cryptomator.features.CryptoTouchFeature;
import ch.cyberduck.core.features.Delete;
import ch.cyberduck.core.features.Directory;
import ch.cyberduck.core.features.Find;
import ch.cyberduck.core.pool.SessionPool;
import ch.cyberduck.core.shared.DefaultFindFeature;
import ch.cyberduck.core.transfer.TransferStatus;
import ch.cyberduck.core.vault.DefaultVaultRegistry;
import ch.cyberduck.core.vault.VaultCredentials;
import ch.cyberduck.core.worker.MoveWorker;
import ch.cyberduck.test.IntegrationTest;

import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import java.util.Arrays;
import java.util.Collections;
import java.util.EnumSet;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

@Category(IntegrationTest.class)
@RunWith(value = Parameterized.class)
public class MoveWorkerTest extends AbstractB2Test {

    @Test
    public void testMoveFileIntoVault() throws Exception {
        final Path home = new Path("/test-cyberduck", EnumSet.of(Path.Type.directory, Path.Type.volume));
        final Path vault = new Path(home, new AlphanumericRandomStringService().random(), EnumSet.of(Path.Type.directory));
        final Path clearFile = new Path(home, new AlphanumericRandomStringService().random(), EnumSet.of(Path.Type.file));
        final B2VersionIdProvider fileid = new B2VersionIdProvider(session);
        new B2TouchFeature(session, fileid).touch(clearFile, new TransferStatus().withLength(0L));
        assertTrue(new DefaultFindFeature(session).find(clearFile));
        final Path encryptedFolder = new Path(vault, new AlphanumericRandomStringService().random(), EnumSet.of(Path.Type.directory));
        final Path encryptedFile = new Path(encryptedFolder, new AlphanumericRandomStringService().random(), EnumSet.of(Path.Type.file));
        final CryptoVault cryptomator = new CryptoVault(vault);
        cryptomator.create(session, new VaultCredentials("test"), new DisabledPasswordStore(), vaultVersion);
        final DefaultVaultRegistry registry = new DefaultVaultRegistry(new DisabledPasswordStore(), new DisabledPasswordCallback(), cryptomator);
        session.withRegistry(registry);
        cryptomator.getFeature(session, Directory.class, new B2DirectoryFeature(session, fileid)).mkdir(encryptedFolder, new TransferStatus().withLength(0L));
        assertTrue(cryptomator.getFeature(session, Find.class, new DefaultFindFeature(session)).find(encryptedFolder));
        // move file into vault
        final MoveWorker worker = new MoveWorker(Collections.singletonMap(clearFile, encryptedFile), new SessionPool.SingleSessionPool(session), PathCache.empty(), new DisabledProgressListener(), new DisabledLoginCallback());
        worker.run(session);
        assertFalse(new DefaultFindFeature(session).find(clearFile));
        assertTrue(cryptomator.getFeature(session, Find.class, new DefaultFindFeature(session)).find(encryptedFile));
        cryptomator.getFeature(session, Delete.class, new B2DeleteFeature(session, fileid)).delete(Arrays.asList(encryptedFile, encryptedFolder, vault), new DisabledLoginCallback(), new Delete.DisabledCallback());
        registry.clear();
    }

    @Test
    public void testMoveDirectoryIntoVault() throws Exception {
        final Path home = new Path("/test-cyberduck", EnumSet.of(Path.Type.directory, Path.Type.volume));
        final Path vault = new Path(home, new AlphanumericRandomStringService().random(), EnumSet.of(Path.Type.directory));
        final Path clearFolder = new Path(home, new AlphanumericRandomStringService().random(), EnumSet.of(Path.Type.directory));
        final Path clearFile = new Path(clearFolder, new AlphanumericRandomStringService().random(), EnumSet.of(Path.Type.file));
        final B2VersionIdProvider fileid = new B2VersionIdProvider(session);
        new B2DirectoryFeature(session, fileid).mkdir(clearFolder, new TransferStatus().withLength(0L));
        new B2TouchFeature(session, fileid).touch(clearFile, new TransferStatus().withLength(0L));
        assertTrue(new DefaultFindFeature(session).find(clearFolder));
        assertTrue(new DefaultFindFeature(session).find(clearFile));
        final CryptoVault cryptomator = new CryptoVault(vault);
        cryptomator.create(session, new VaultCredentials("test"), new DisabledPasswordStore(), vaultVersion);
        final DefaultVaultRegistry registry = new DefaultVaultRegistry(new DisabledPasswordStore(), new DisabledPasswordCallback(), cryptomator);
        session.withRegistry(registry);
        // move directory into vault
        final Path encryptedFolder = new Path(vault, clearFolder.getName(), EnumSet.of(Path.Type.directory));
        final Path encryptedFile = new Path(encryptedFolder, clearFile.getName(), EnumSet.of(Path.Type.file));
        final MoveWorker worker = new MoveWorker(Collections.singletonMap(clearFolder, encryptedFolder), new SessionPool.SingleSessionPool(session), PathCache.empty(), new DisabledProgressListener(), new DisabledLoginCallback());
        worker.run(session);
        assertTrue(cryptomator.getFeature(session, Find.class, new DefaultFindFeature(session)).find(encryptedFolder));
        assertTrue(cryptomator.getFeature(session, Find.class, new DefaultFindFeature(session)).find(encryptedFile));
        assertFalse(new DefaultFindFeature(session).find(clearFolder));
        assertFalse(new DefaultFindFeature(session).find(clearFile));
        cryptomator.getFeature(session, Delete.class, new B2DeleteFeature(session, fileid)).delete(Arrays.asList(encryptedFile, encryptedFolder, vault), new DisabledLoginCallback(), new Delete.DisabledCallback());
        registry.clear();
    }

    @Test
    public void testMoveFileOutsideVault() throws Exception {
        final Path home = new Path("/test-cyberduck", EnumSet.of(Path.Type.directory, Path.Type.volume));
        final Path vault = new Path(home, new AlphanumericRandomStringService().random(), EnumSet.of(Path.Type.directory));
        final Path clearFolder = new Path(home, new AlphanumericRandomStringService().random(), EnumSet.of(Path.Type.directory));
        final B2VersionIdProvider fileid = new B2VersionIdProvider(session);
        new B2DirectoryFeature(session, fileid).mkdir(clearFolder, new TransferStatus().withLength(0L));
        final Path encryptedFolder = new Path(vault, new AlphanumericRandomStringService().random(), EnumSet.of(Path.Type.directory));
        final Path encryptedFile = new Path(encryptedFolder, new AlphanumericRandomStringService().random(), EnumSet.of(Path.Type.file));
        final CryptoVault cryptomator = new CryptoVault(vault);
        cryptomator.create(session, new VaultCredentials("test"), new DisabledPasswordStore(), vaultVersion);
        final DefaultVaultRegistry registry = new DefaultVaultRegistry(new DisabledPasswordStore(), new DisabledPasswordCallback(), cryptomator);
        session.withRegistry(registry);
        cryptomator.getFeature(session, Directory.class, new B2DirectoryFeature(session, fileid)).mkdir(encryptedFolder, new TransferStatus().withLength(0L));
        assertTrue(cryptomator.getFeature(session, Find.class, new DefaultFindFeature(session)).find(encryptedFolder));
        new CryptoTouchFeature<>(session, new B2TouchFeature(session, fileid), new B2WriteFeature(session, fileid), cryptomator).touch(encryptedFile,
                new TransferStatus().withLength(0L));
        assertTrue(cryptomator.getFeature(session, Find.class, new DefaultFindFeature(session)).find(encryptedFile));
        // move file outside vault
        final Path fileRenamed = new Path(clearFolder, new AlphanumericRandomStringService().random(), EnumSet.of(Path.Type.file));
        final MoveWorker worker = new MoveWorker(Collections.singletonMap(encryptedFile, fileRenamed), new SessionPool.SingleSessionPool(session), PathCache.empty(), new DisabledProgressListener(), new DisabledLoginCallback());
        worker.run(session);
        assertFalse(cryptomator.getFeature(session, Find.class, new DefaultFindFeature(session)).find(encryptedFile));
        assertTrue(new DefaultFindFeature(session).find(fileRenamed));
        cryptomator.getFeature(session, Delete.class, new B2DeleteFeature(session, fileid)).delete(Arrays.asList(encryptedFolder, vault), new DisabledLoginCallback(), new Delete.DisabledCallback());
        new B2DeleteFeature(session, fileid).delete(Arrays.asList(fileRenamed, clearFolder), new DisabledLoginCallback(), new Delete.DisabledCallback());
        registry.clear();
    }

    @Test
    public void testMoveDirectoryOutsideVault() throws Exception {
        final Path home = new Path("/test-cyberduck", EnumSet.of(Path.Type.directory, Path.Type.volume));
        final Path vault = new Path(home, new AlphanumericRandomStringService().random(), EnumSet.of(Path.Type.directory));
        final Path encryptedFolder = new Path(vault, new AlphanumericRandomStringService().random(), EnumSet.of(Path.Type.directory));
        final Path encryptedFile = new Path(encryptedFolder, new AlphanumericRandomStringService().random(), EnumSet.of(Path.Type.file));
        final CryptoVault cryptomator = new CryptoVault(vault);
        cryptomator.create(session, new VaultCredentials("test"), new DisabledPasswordStore(), vaultVersion);
        final DefaultVaultRegistry registry = new DefaultVaultRegistry(new DisabledPasswordStore(), new DisabledPasswordCallback(), cryptomator);
        session.withRegistry(registry);
        final B2VersionIdProvider fileid = new B2VersionIdProvider(session);
        cryptomator.getFeature(session, Directory.class, new B2DirectoryFeature(session, fileid)).mkdir(encryptedFolder, new TransferStatus().withLength(0L));
        assertTrue(cryptomator.getFeature(session, Find.class, new DefaultFindFeature(session)).find(encryptedFolder));
        new CryptoTouchFeature<>(session, new B2TouchFeature(session, fileid), new B2WriteFeature(session, fileid), cryptomator).touch(encryptedFile,
                new TransferStatus().withLength(0L));
        assertTrue(cryptomator.getFeature(session, Find.class, new DefaultFindFeature(session)).find(encryptedFile));
        // move directory outside vault
        final Path directoryRenamed = new Path(home, new AlphanumericRandomStringService().random(), EnumSet.of(Path.Type.directory));
        final MoveWorker worker = new MoveWorker(Collections.singletonMap(encryptedFolder, directoryRenamed), new SessionPool.SingleSessionPool(session), PathCache.empty(), new DisabledProgressListener(), new DisabledLoginCallback());
        worker.run(session);
        assertFalse(cryptomator.getFeature(session, Find.class, new DefaultFindFeature(session)).find(encryptedFolder));
        assertFalse(cryptomator.getFeature(session, Find.class, new DefaultFindFeature(session)).find(encryptedFile));
        assertTrue(new DefaultFindFeature(session).find(directoryRenamed));
        final Path fileRenamed = new Path(directoryRenamed, encryptedFile.getName(), EnumSet.of(Path.Type.file));
        assertTrue(new DefaultFindFeature(session).find(fileRenamed));
        cryptomator.getFeature(session, Delete.class, new B2DeleteFeature(session, fileid)).delete(Collections.singletonList(vault), new DisabledLoginCallback(), new Delete.DisabledCallback());
        new B2DeleteFeature(session, fileid).delete(Arrays.asList(fileRenamed, directoryRenamed), new DisabledLoginCallback(), new Delete.DisabledCallback());
        registry.clear();
    }
}
