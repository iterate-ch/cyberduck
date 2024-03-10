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
import ch.cyberduck.core.cryptomator.features.CryptoBulkFeature;
import ch.cyberduck.core.cryptomator.features.CryptoReadFeature;
import ch.cyberduck.core.cryptomator.features.CryptoTouchFeature;
import ch.cyberduck.core.cryptomator.features.CryptoWriteFeature;
import ch.cyberduck.core.features.Directory;
import ch.cyberduck.core.features.Find;
import ch.cyberduck.core.io.StreamCopier;
import ch.cyberduck.core.pool.SessionPool;
import ch.cyberduck.core.s3.AbstractS3Test;
import ch.cyberduck.core.s3.S3AccessControlListFeature;
import ch.cyberduck.core.s3.S3DefaultDeleteFeature;
import ch.cyberduck.core.s3.S3DirectoryFeature;
import ch.cyberduck.core.s3.S3FindFeature;
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
import ch.cyberduck.core.worker.CopyWorker;
import ch.cyberduck.core.worker.DeleteWorker;
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

import static org.junit.Assert.*;
import static org.junit.Assume.assumeTrue;

@Category(IntegrationTest.class)
@RunWith(value = Parameterized.class)
public class CopyWorkerTest extends AbstractS3Test {

    @Test
    public void testCopyFile() throws Exception {
        final Path home = new Path("test-eu-central-1-cyberduck", EnumSet.of(Path.Type.directory, Path.Type.volume));
        final Path vault = new Path(home, new AlphanumericRandomStringService().random(), EnumSet.of(Path.Type.directory));
        final Path source = new Path(vault, new AlphanumericRandomStringService().random(), EnumSet.of(Path.Type.file));
        final Path target = new Path(vault, new AlphanumericRandomStringService().random(), EnumSet.of(Path.Type.file));
        final CryptoVault cryptomator = new CryptoVault(vault);
        cryptomator.create(session, new VaultCredentials("test"), vaultVersion);
        final DefaultVaultRegistry registry = new DefaultVaultRegistry(new DisabledPasswordStore(), new DisabledPasswordCallback(), cryptomator);
        session.withRegistry(registry);
        final byte[] content = RandomUtils.nextBytes(40500);
        final TransferStatus status = new TransferStatus();
        new CryptoBulkFeature<>(session, new DisabledBulkFeature(), new S3DefaultDeleteFeature(session), cryptomator).pre(Transfer.Type.upload, Collections.singletonMap(new TransferItem(source), status), new DisabledConnectionCallback());
        new StreamCopier(new TransferStatus(), new TransferStatus()).transfer(new ByteArrayInputStream(content), new CryptoWriteFeature<>(session, new S3MultipartWriteFeature(session, new S3AccessControlListFeature(session)), cryptomator).write(source, status.withLength(content.length), new DisabledConnectionCallback()));
        assertTrue(cryptomator.getFeature(session, Find.class, new DefaultFindFeature(session)).find(source));
        final CopyWorker worker = new CopyWorker(Collections.singletonMap(source, target), new SessionPool.SingleSessionPool(session, registry), PathCache.empty(), new DisabledProgressListener(), new DisabledConnectionCallback());
        worker.run(session);
        assertTrue(cryptomator.getFeature(session, Find.class, new S3FindFeature(session, new S3AccessControlListFeature(session))).find(source));
        assertTrue(cryptomator.getFeature(session, Find.class, new S3FindFeature(session, new S3AccessControlListFeature(session))).find(target));
        final ByteArrayOutputStream out = new ByteArrayOutputStream(content.length);
        assertEquals(content.length, IOUtils.copy(new CryptoReadFeature(session, new S3ReadFeature(session), cryptomator).read(target, new TransferStatus().withLength(content.length), new DisabledConnectionCallback()), out));
        assertArrayEquals(content, out.toByteArray());
        new DeleteWorker(new DisabledLoginCallback(), Collections.singletonList(vault), new DisabledProgressListener()).run(session);
    }

    @Test
    public void testCopyToDifferentFolderCryptomator() throws Exception {
        final Path home = new Path("test-eu-central-1-cyberduck", EnumSet.of(Path.Type.directory, Path.Type.volume));
        final Path vault = new Path(home, new AlphanumericRandomStringService().random(), EnumSet.of(Path.Type.directory));
        final Path source = new Path(vault, new AlphanumericRandomStringService().random(), EnumSet.of(Path.Type.file));
        final Path targetFolder = new Path(vault, new AlphanumericRandomStringService().random(), EnumSet.of(Path.Type.directory));
        final Path target = new Path(targetFolder, new AlphanumericRandomStringService().random(), EnumSet.of(Path.Type.file));
        final CryptoVault cryptomator = new CryptoVault(vault);
        cryptomator.create(session, new VaultCredentials("test"), vaultVersion);
        final DefaultVaultRegistry registry = new DefaultVaultRegistry(new DisabledPasswordStore(), new DisabledPasswordCallback(), cryptomator);
        session.withRegistry(registry);
        final S3AccessControlListFeature acl = new S3AccessControlListFeature(session);
        new CryptoTouchFeature<>(session, new S3TouchFeature(session, acl), new S3WriteFeature(session, acl), cryptomator).touch(source, new TransferStatus());
        assertTrue(cryptomator.getFeature(session, Find.class, new DefaultFindFeature(session)).find(source));

        cryptomator.getFeature(session, Directory.class, new S3DirectoryFeature(session, new S3WriteFeature(session, acl), acl)).mkdir(targetFolder, new TransferStatus());
        assertTrue(cryptomator.getFeature(session, Find.class, new DefaultFindFeature(session)).find(targetFolder));
        final CopyWorker worker = new CopyWorker(Collections.singletonMap(source, target), new SessionPool.SingleSessionPool(session, registry), PathCache.empty(), new DisabledProgressListener(), new DisabledConnectionCallback());
        worker.run(session);
        assertTrue(cryptomator.getFeature(session, Find.class, new DefaultFindFeature(session)).find(source));
        assertTrue(cryptomator.getFeature(session, Find.class, new DefaultFindFeature(session)).find(target));
        registry.clear();
        new DeleteWorker(new DisabledLoginCallback(), Collections.singletonList(vault), new DisabledProgressListener()).run(session);
    }

    @Test
    public void testCopyToDifferentFolderLongFilenameCryptomator() throws Exception {
        assumeTrue(vaultVersion == CryptoVault.VAULT_VERSION_DEPRECATED);
        final Path home = new Path("test-eu-central-1-cyberduck", EnumSet.of(Path.Type.directory, Path.Type.volume));
        final CryptoVault cryptomator = new CryptoVault(new Path(home, new AlphanumericRandomStringService().random(), EnumSet.of(Path.Type.directory)));
        final Path vault = cryptomator.create(session, new VaultCredentials("test"), vaultVersion);
        final Path source = new Path(vault, new AlphanumericRandomStringService(130).random(), EnumSet.of(Path.Type.file));
        final Path targetFolder = new Path(vault, new AlphanumericRandomStringService(130).random(), EnumSet.of(Path.Type.directory));
        final Path target = new Path(targetFolder, new AlphanumericRandomStringService(130).random(), EnumSet.of(Path.Type.file));
        final DefaultVaultRegistry registry = new DefaultVaultRegistry(new DisabledPasswordStore(), new DisabledPasswordCallback(), cryptomator);
        session.withRegistry(registry);
        final S3AccessControlListFeature acl = new S3AccessControlListFeature(session);
        new CryptoTouchFeature<>(session, new S3TouchFeature(session, acl), new S3WriteFeature(session, acl), cryptomator).touch(source, new TransferStatus());
        assertTrue(cryptomator.getFeature(session, Find.class, new DefaultFindFeature(session)).find(source));
        cryptomator.getFeature(session, Directory.class, new S3DirectoryFeature(session, new S3WriteFeature(session, acl), acl)).mkdir(targetFolder, new TransferStatus());
        assertTrue(cryptomator.getFeature(session, Find.class, new DefaultFindFeature(session)).find(targetFolder));
        final CopyWorker worker = new CopyWorker(Collections.singletonMap(source, target), new SessionPool.SingleSessionPool(session, registry), PathCache.empty(), new DisabledProgressListener(), new DisabledConnectionCallback());
        worker.run(session);
        assertTrue(cryptomator.getFeature(session, Find.class, new DefaultFindFeature(session)).find(source));
        assertTrue(cryptomator.getFeature(session, Find.class, new DefaultFindFeature(session)).find(target));
        registry.clear();
        new DeleteWorker(new DisabledLoginCallback(), Collections.singletonList(vault), new DisabledProgressListener()).run(session);
    }

    @Test
    public void testCopyFolder() throws Exception {
        final Path home = new Path("test-eu-central-1-cyberduck", EnumSet.of(Path.Type.directory, Path.Type.volume));
        final CryptoVault cryptomator = new CryptoVault(new Path(home, new AlphanumericRandomStringService().random(), EnumSet.of(Path.Type.directory)));
        final Path vault = cryptomator.create(session, new VaultCredentials("test"), vaultVersion);
        final Path folder = new Path(vault, new AlphanumericRandomStringService().random(), EnumSet.of(Path.Type.directory));
        final Path file = new Path(folder, new AlphanumericRandomStringService().random(), EnumSet.of(Path.Type.file));
        final DefaultVaultRegistry registry = new DefaultVaultRegistry(new DisabledPasswordStore(), new DisabledPasswordCallback(), cryptomator);
        session.withRegistry(registry);
        final S3AccessControlListFeature acl = new S3AccessControlListFeature(session);
        cryptomator.getFeature(session, Directory.class, new S3DirectoryFeature(session, new S3WriteFeature(session, acl), acl)).mkdir(folder, new TransferStatus());
        assertTrue(cryptomator.getFeature(session, Find.class, new DefaultFindFeature(session)).find(folder));
        new CryptoTouchFeature<>(session, new S3TouchFeature(session, acl), new S3WriteFeature(session, acl), cryptomator).touch(file, new TransferStatus());
        assertTrue(cryptomator.getFeature(session, Find.class, new DefaultFindFeature(session)).find(file));
        // copy file
        final Path fileRenamed = new Path(folder, "f1", EnumSet.of(Path.Type.file));
        new CopyWorker(Collections.singletonMap(file, fileRenamed), new SessionPool.SingleSessionPool(session, registry), PathCache.empty(), new DisabledProgressListener(), new DisabledConnectionCallback()).run(session);
        assertTrue(cryptomator.getFeature(session, Find.class, new S3FindFeature(session, acl)).find(file));
        assertTrue(cryptomator.getFeature(session, Find.class, new S3FindFeature(session, acl)).find(fileRenamed));
        // copy folder
        final Path folderRenamed = new Path(vault, new AlphanumericRandomStringService().random(), EnumSet.of(Path.Type.directory));
        new CopyWorker(Collections.singletonMap(folder, folderRenamed), new SessionPool.SingleSessionPool(session, registry), PathCache.empty(), new DisabledProgressListener(), new DisabledConnectionCallback()).run(session);
        assertTrue(cryptomator.getFeature(session, Find.class, new S3FindFeature(session, acl)).find(folder));
        assertTrue(cryptomator.getFeature(session, Find.class, new S3FindFeature(session, acl)).find(folderRenamed));
        final Path fileRenamedInRenamedFolder = new Path(folderRenamed, "f1", EnumSet.of(Path.Type.file));
        assertTrue(cryptomator.getFeature(session, Find.class, new DefaultFindFeature(session)).find(fileRenamedInRenamedFolder));
        registry.clear();
        new DeleteWorker(new DisabledLoginCallback(), Collections.singletonList(vault), new DisabledProgressListener()).run(session);
    }

    @Test
    public void testCopyFileIntoVault() throws Exception {
        final Path home = new Path("test-eu-central-1-cyberduck", EnumSet.of(Path.Type.directory, Path.Type.volume));
        final Path vault = new Path(home, new AlphanumericRandomStringService().random(), EnumSet.of(Path.Type.directory));
        final Path cleartextFile = new Path(home, new AlphanumericRandomStringService().random(), EnumSet.of(Path.Type.file));
        final byte[] content = RandomUtils.nextBytes(40500);
        final S3AccessControlListFeature acl = new S3AccessControlListFeature(session);
        new StreamCopier(new TransferStatus(), new TransferStatus()).transfer(new ByteArrayInputStream(content), new S3MultipartWriteFeature(session, acl).write(cleartextFile, new TransferStatus().withLength(content.length), new DisabledConnectionCallback()));
        assertTrue(new S3FindFeature(session, acl).find(cleartextFile));
        final Path encryptedFolder = new Path(vault, new AlphanumericRandomStringService().random(), EnumSet.of(Path.Type.directory));
        final Path encryptedFile = new Path(encryptedFolder, new AlphanumericRandomStringService().random(), EnumSet.of(Path.Type.file));
        final CryptoVault cryptomator = new CryptoVault(vault);
        cryptomator.create(session, new VaultCredentials("test"), vaultVersion);
        final DefaultVaultRegistry registry = new DefaultVaultRegistry(new DisabledPasswordStore(), new DisabledPasswordCallback(), cryptomator);
        session.withRegistry(registry);
        cryptomator.getFeature(session, Directory.class, new S3DirectoryFeature(session, new S3WriteFeature(session, acl), acl)).mkdir(encryptedFolder, new TransferStatus());
        assertTrue(cryptomator.getFeature(session, Find.class, new DefaultFindFeature(session)).find(encryptedFolder));
        // copy file into vault
        final CopyWorker worker = new CopyWorker(Collections.singletonMap(cleartextFile, encryptedFile), new SessionPool.SingleSessionPool(session, registry), PathCache.empty(), new DisabledProgressListener(), new DisabledConnectionCallback());
        worker.run(session);
        assertTrue(new S3FindFeature(session, acl).find(cleartextFile));
        assertTrue(cryptomator.getFeature(session, Find.class, new S3FindFeature(session, acl)).find(encryptedFile));
        final ByteArrayOutputStream out = new ByteArrayOutputStream(content.length);
        assertEquals(content.length, IOUtils.copy(new CryptoReadFeature(session, new S3ReadFeature(session), cryptomator).read(encryptedFile, new TransferStatus().withLength(content.length), new DisabledConnectionCallback()), out));
        assertArrayEquals(content, out.toByteArray());
        registry.clear();
        new DeleteWorker(new DisabledLoginCallback(), Collections.singletonList(vault), new DisabledProgressListener()).run(session);
    }

    @Test
    public void testCopyDirectoryIntoVault() throws Exception {
        final Path home = new Path("test-eu-central-1-cyberduck", EnumSet.of(Path.Type.directory, Path.Type.volume));
        final Path vault = new Path(home, new AlphanumericRandomStringService().random(), EnumSet.of(Path.Type.directory));
        final Path cleartextFolder = new Path(home, new AlphanumericRandomStringService().random(), EnumSet.of(Path.Type.directory));
        final Path cleartextFile = new Path(cleartextFolder, new AlphanumericRandomStringService().random(), EnumSet.of(Path.Type.file));
        final S3AccessControlListFeature acl = new S3AccessControlListFeature(session);
        new S3DirectoryFeature(session, new S3WriteFeature(session, acl), acl).mkdir(cleartextFolder, new TransferStatus());
        new S3TouchFeature(session, acl).touch(cleartextFile, new TransferStatus());
        assertTrue(new S3FindFeature(session, acl).find(cleartextFolder));
        assertTrue(new S3FindFeature(session, acl).find(cleartextFile));
        final CryptoVault cryptomator = new CryptoVault(vault);
        cryptomator.create(session, new VaultCredentials("test"), vaultVersion);
        final DefaultVaultRegistry registry = new DefaultVaultRegistry(new DisabledPasswordStore(), new DisabledPasswordCallback(), cryptomator);
        session.withRegistry(registry);
        // move directory into vault
        final Path encryptedFolder = new Path(vault, cleartextFolder.getName(), EnumSet.of(Path.Type.directory));
        final Path encryptedFile = new Path(encryptedFolder, cleartextFile.getName(), EnumSet.of(Path.Type.file));
        final CopyWorker worker = new CopyWorker(Collections.singletonMap(cleartextFolder, encryptedFolder), new SessionPool.SingleSessionPool(session, registry), PathCache.empty(), new DisabledProgressListener(), new DisabledConnectionCallback());
        worker.run(session);
        assertTrue(cryptomator.getFeature(session, Find.class, new DefaultFindFeature(session)).find(encryptedFolder));
        assertTrue(cryptomator.getFeature(session, Find.class, new DefaultFindFeature(session)).find(encryptedFile));
        assertTrue(new S3FindFeature(session, acl).find(cleartextFolder));
        assertTrue(new S3FindFeature(session, acl).find(cleartextFile));
        registry.clear();
        new DeleteWorker(new DisabledLoginCallback(), Collections.singletonList(vault), new DisabledProgressListener()).run(session);
    }

    @Test
    public void testCopyFileOutsideVault() throws Exception {
        final Path home = new Path("test-eu-central-1-cyberduck", EnumSet.of(Path.Type.directory, Path.Type.volume));
        final Path vault = new Path(home, new AlphanumericRandomStringService().random(), EnumSet.of(Path.Type.directory));
        final Path clearFolder = new Path(home, new AlphanumericRandomStringService().random(), EnumSet.of(Path.Type.directory));
        final S3AccessControlListFeature acl = new S3AccessControlListFeature(session);
        new S3DirectoryFeature(session, new S3WriteFeature(session, acl),
                acl).mkdir(clearFolder, new TransferStatus());
        final Path encryptedFolder = new Path(vault, new AlphanumericRandomStringService().random(), EnumSet.of(Path.Type.directory));
        final Path encryptedFile = new Path(encryptedFolder, new AlphanumericRandomStringService().random(), EnumSet.of(Path.Type.file));
        final CryptoVault cryptomator = new CryptoVault(vault);
        cryptomator.create(session, new VaultCredentials("test"), vaultVersion);
        final DefaultVaultRegistry registry = new DefaultVaultRegistry(new DisabledPasswordStore(), new DisabledPasswordCallback(), cryptomator);
        session.withRegistry(registry);
        cryptomator.getFeature(session, Directory.class, new S3DirectoryFeature(session, new S3WriteFeature(session, acl), acl)).mkdir(encryptedFolder, new TransferStatus());
        assertTrue(cryptomator.getFeature(session, Find.class, new DefaultFindFeature(session)).find(encryptedFolder));
        new CryptoTouchFeature<>(session, new S3TouchFeature(session, acl), new S3WriteFeature(session, acl), cryptomator).touch(encryptedFile, new TransferStatus());
        assertTrue(cryptomator.getFeature(session, Find.class, new DefaultFindFeature(session)).find(encryptedFile));
        // move file outside vault
        final Path cleartextFile = new Path(clearFolder, new AlphanumericRandomStringService().random(), EnumSet.of(Path.Type.file));
        final CopyWorker worker = new CopyWorker(Collections.singletonMap(encryptedFile, cleartextFile), new SessionPool.SingleSessionPool(session, registry), PathCache.empty(), new DisabledProgressListener(), new DisabledConnectionCallback());
        worker.run(session);
        assertTrue(cryptomator.getFeature(session, Find.class, new S3FindFeature(session, acl)).find(encryptedFile));
        assertTrue(new S3FindFeature(session, acl).find(cleartextFile));
        registry.clear();
        new DeleteWorker(new DisabledLoginCallback(), Arrays.asList(vault, clearFolder), new DisabledProgressListener()).run(session);
    }

    @Test
    public void testCopyDirectoryOutsideVault() throws Exception {
        final Path home = new Path("test-eu-central-1-cyberduck", EnumSet.of(Path.Type.directory, Path.Type.volume));
        final Path vault = new Path(home, new AlphanumericRandomStringService().random(), EnumSet.of(Path.Type.directory));
        final Path encryptedFolder = new Path(vault, new AlphanumericRandomStringService().random(), EnumSet.of(Path.Type.directory));
        final Path encryptedFile = new Path(encryptedFolder, new AlphanumericRandomStringService().random(), EnumSet.of(Path.Type.file));
        final CryptoVault cryptomator = new CryptoVault(vault);
        cryptomator.create(session, new VaultCredentials("test"), vaultVersion);
        final DefaultVaultRegistry registry = new DefaultVaultRegistry(new DisabledPasswordStore(), new DisabledPasswordCallback(), cryptomator);
        session.withRegistry(registry);
        final S3AccessControlListFeature acl = new S3AccessControlListFeature(session);
        cryptomator.getFeature(session, Directory.class, new S3DirectoryFeature(session, new S3WriteFeature(session, acl),
                acl)).mkdir(encryptedFolder, new TransferStatus());
        assertTrue(cryptomator.getFeature(session, Find.class, new DefaultFindFeature(session)).find(encryptedFolder));
        new CryptoTouchFeature<>(session, new S3TouchFeature(session, acl), new S3WriteFeature(session, acl), cryptomator).touch(encryptedFile, new TransferStatus());
        assertTrue(cryptomator.getFeature(session, Find.class, new DefaultFindFeature(session)).find(encryptedFile));
        // copy directory outside vault
        final Path cleartextFolder = new Path(home, new AlphanumericRandomStringService().random(), EnumSet.of(Path.Type.directory));
        final CopyWorker worker = new CopyWorker(Collections.singletonMap(encryptedFolder, cleartextFolder), new SessionPool.SingleSessionPool(session, registry), PathCache.empty(), new DisabledProgressListener(), new DisabledConnectionCallback());
        worker.run(session);
        assertTrue(cryptomator.getFeature(session, Find.class, new S3FindFeature(session, acl)).find(encryptedFolder));
        assertTrue(cryptomator.getFeature(session, Find.class, new S3FindFeature(session, acl)).find(encryptedFile));
        assertTrue(new S3FindFeature(session, acl).find(cleartextFolder));
        final Path fileRenamed = new Path(cleartextFolder, encryptedFile.getName(), EnumSet.of(Path.Type.file));
        assertTrue(new S3FindFeature(session, acl).find(fileRenamed));
        registry.clear();
        new DeleteWorker(new DisabledLoginCallback(), Arrays.asList(cleartextFolder, vault), new DisabledProgressListener()).run(session);
    }
}
