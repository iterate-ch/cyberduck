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
import ch.cyberduck.core.b2.AbstractB2Test;
import ch.cyberduck.core.b2.B2AttributesFinderFeature;
import ch.cyberduck.core.b2.B2DeleteFeature;
import ch.cyberduck.core.b2.B2DirectoryFeature;
import ch.cyberduck.core.b2.B2FileidProvider;
import ch.cyberduck.core.b2.B2FindFeature;
import ch.cyberduck.core.b2.B2ReadFeature;
import ch.cyberduck.core.b2.B2TouchFeature;
import ch.cyberduck.core.b2.B2WriteFeature;
import ch.cyberduck.core.cryptomator.features.CryptoBulkFeature;
import ch.cyberduck.core.cryptomator.features.CryptoDirectoryFeature;
import ch.cyberduck.core.cryptomator.features.CryptoFindFeature;
import ch.cyberduck.core.cryptomator.features.CryptoReadFeature;
import ch.cyberduck.core.cryptomator.features.CryptoTouchFeature;
import ch.cyberduck.core.cryptomator.features.CryptoWriteFeature;
import ch.cyberduck.core.io.StreamCopier;
import ch.cyberduck.core.pool.SessionPool;
import ch.cyberduck.core.shared.DefaultFindFeature;
import ch.cyberduck.core.shared.DefaultTouchFeature;
import ch.cyberduck.core.shared.DefaultUploadFeature;
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
import org.apache.commons.text.RandomStringGenerator;
import org.junit.Test;
import org.junit.experimental.categories.Category;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.util.Arrays;
import java.util.Collections;
import java.util.EnumSet;

import synapticloop.b2.response.BaseB2Response;

import static org.junit.Assert.*;

@Category(IntegrationTest.class)
public class CopyWorkerTest extends AbstractB2Test {

    @Test
    public void testCopyFile() throws Exception {
        final Path home = new Path("/test-cyberduck", EnumSet.of(Path.Type.directory, Path.Type.volume));
        final Path vault = new Path(home, new AlphanumericRandomStringService().random(), EnumSet.of(Path.Type.directory));
        final Path source = new Path(vault, new AlphanumericRandomStringService().random(), EnumSet.of(Path.Type.file));
        final Path target = new Path(vault, new AlphanumericRandomStringService().random(), EnumSet.of(Path.Type.file));
        final CryptoVault cryptomator = new CryptoVault(vault);
        cryptomator.create(session, null, new VaultCredentials("test"), new DisabledPasswordStore());
        final DefaultVaultRegistry registry = new DefaultVaultRegistry(new DisabledPasswordStore(), new DisabledPasswordCallback(), cryptomator);
        session.withRegistry(registry);
        final byte[] content = RandomUtils.nextBytes(40500);
        final TransferStatus status = new TransferStatus();
        final B2FileidProvider fileid = new B2FileidProvider(session).withCache(cache);
        new CryptoBulkFeature<>(session, new DisabledBulkFeature(), new B2DeleteFeature(session, fileid), cryptomator).pre(Transfer.Type.upload, Collections.singletonMap(new TransferItem(source), status), new DisabledConnectionCallback());
        new StreamCopier(new TransferStatus(), new TransferStatus()).transfer(new ByteArrayInputStream(content), new CryptoWriteFeature<>(session, new B2WriteFeature(session, fileid), cryptomator).write(source, status.length(content.length), new DisabledConnectionCallback()));
        assertTrue(new CryptoFindFeature(session, new DefaultFindFeature(session), cryptomator).find(source, new DisabledListProgressListener()));
        final CopyWorker worker = new CopyWorker(Collections.singletonMap(source, target), new SessionPool.SingleSessionPool(session, registry), PathCache.empty(), new DisabledProgressListener(), new DisabledConnectionCallback());
        worker.run(session);
        assertTrue(new CryptoFindFeature(session, new B2FindFeature(session, fileid), cryptomator).find(source, new DisabledListProgressListener()));
        assertTrue(new CryptoFindFeature(session, new B2FindFeature(session, fileid), cryptomator).find(target, new DisabledListProgressListener()));
        final ByteArrayOutputStream out = new ByteArrayOutputStream(content.length);
        assertEquals(content.length, IOUtils.copy(new CryptoReadFeature(session, new B2ReadFeature(session, fileid), cryptomator).read(target, new TransferStatus().length(content.length), new DisabledConnectionCallback()), out));
        assertArrayEquals(content, out.toByteArray());
        new DeleteWorker(new DisabledLoginCallback(), Collections.singletonList(vault), new DisabledProgressListener()).run(session);
    }

    @Test
    public void testCopyToDifferentFolderCryptomator() throws Exception {
        final Path home = new Path("/test-cyberduck", EnumSet.of(Path.Type.directory, Path.Type.volume));
        final Path vault = new Path(home, new AlphanumericRandomStringService().random(), EnumSet.of(Path.Type.directory));
        final Path source = new Path(vault, new AlphanumericRandomStringService().random(), EnumSet.of(Path.Type.file));
        final Path targetFolder = new Path(vault, new AlphanumericRandomStringService().random(), EnumSet.of(Path.Type.directory));
        final Path target = new Path(targetFolder, new AlphanumericRandomStringService().random(), EnumSet.of(Path.Type.file));
        final CryptoVault cryptomator = new CryptoVault(vault);
        cryptomator.create(session, null, new VaultCredentials("test"), new DisabledPasswordStore());
        final DefaultVaultRegistry registry = new DefaultVaultRegistry(new DisabledPasswordStore(), new DisabledPasswordCallback(), cryptomator);
        session.withRegistry(registry);
        final B2FileidProvider fileid = new B2FileidProvider(session).withCache(cache);
        new CryptoTouchFeature<BaseB2Response>(session, new DefaultTouchFeature<BaseB2Response>(new DefaultUploadFeature<BaseB2Response>(new B2WriteFeature(session, fileid)),
            new B2AttributesFinderFeature(session, fileid)), new B2WriteFeature(session, fileid), cryptomator).touch(source, new TransferStatus());
        assertTrue(new CryptoFindFeature(session, new DefaultFindFeature(session), cryptomator).find(source, new DisabledListProgressListener()));
        new CryptoDirectoryFeature<BaseB2Response>(session, new B2DirectoryFeature(session, fileid), new B2WriteFeature(session, fileid), cryptomator).mkdir(targetFolder, null, new TransferStatus());
        assertTrue(new CryptoFindFeature(session, new DefaultFindFeature(session), cryptomator).find(targetFolder, new DisabledListProgressListener()));
        final CopyWorker worker = new CopyWorker(Collections.singletonMap(source, target), new SessionPool.SingleSessionPool(session, registry), PathCache.empty(), new DisabledProgressListener(), new DisabledConnectionCallback());
        worker.run(session);
        assertTrue(new CryptoFindFeature(session, new DefaultFindFeature(session), cryptomator).find(source, new DisabledListProgressListener()));
        assertTrue(new CryptoFindFeature(session, new DefaultFindFeature(session), cryptomator).find(target, new DisabledListProgressListener()));
        registry.clear();
        new DeleteWorker(new DisabledLoginCallback(), Collections.singletonList(vault), new DisabledProgressListener()).run(session);
    }

    @Test
    public void testCopyToDifferentFolderLongFilenameCryptomator() throws Exception {
        final Path home = new Path("/test-cyberduck", EnumSet.of(Path.Type.directory, Path.Type.volume));
        final Path vault = new Path(home, new AlphanumericRandomStringService().random(), EnumSet.of(Path.Type.directory));
        final Path source = new Path(vault, new RandomStringGenerator.Builder().build().generate(130), EnumSet.of(Path.Type.file));
        final Path targetFolder = new Path(vault, new RandomStringGenerator.Builder().build().generate(130), EnumSet.of(Path.Type.directory));
        final Path target = new Path(targetFolder, new RandomStringGenerator.Builder().build().generate(130), EnumSet.of(Path.Type.file));
        final CryptoVault cryptomator = new CryptoVault(vault);
        cryptomator.create(session, null, new VaultCredentials("test"), new DisabledPasswordStore());
        final DefaultVaultRegistry registry = new DefaultVaultRegistry(new DisabledPasswordStore(), new DisabledPasswordCallback(), cryptomator);
        session.withRegistry(registry);
        final B2FileidProvider fileid = new B2FileidProvider(session).withCache(cache);
        new CryptoTouchFeature<BaseB2Response>(session, new DefaultTouchFeature<BaseB2Response>(new DefaultUploadFeature<BaseB2Response>(new B2WriteFeature(session, fileid)),
            new B2AttributesFinderFeature(session, fileid)), new B2WriteFeature(session, fileid), cryptomator).touch(source, new TransferStatus());
        assertTrue(new CryptoFindFeature(session, new DefaultFindFeature(session), cryptomator).find(source, new DisabledListProgressListener()));
        new CryptoDirectoryFeature<BaseB2Response>(session, new B2DirectoryFeature(session, fileid), new B2WriteFeature(session, fileid), cryptomator).mkdir(targetFolder, null, new TransferStatus());
        assertTrue(new CryptoFindFeature(session, new DefaultFindFeature(session), cryptomator).find(targetFolder, new DisabledListProgressListener()));
        final CopyWorker worker = new CopyWorker(Collections.singletonMap(source, target), new SessionPool.SingleSessionPool(session, registry), PathCache.empty(), new DisabledProgressListener(), new DisabledConnectionCallback());
        worker.run(session);
        assertTrue(new CryptoFindFeature(session, new DefaultFindFeature(session), cryptomator).find(source, new DisabledListProgressListener()));
        assertTrue(new CryptoFindFeature(session, new DefaultFindFeature(session), cryptomator).find(target, new DisabledListProgressListener()));
        registry.clear();
        new DeleteWorker(new DisabledLoginCallback(), Collections.singletonList(vault), new DisabledProgressListener()).run(session);
    }

    @Test
    public void testCopyFolder() throws Exception {
        final Path home = new Path("/test-cyberduck", EnumSet.of(Path.Type.directory, Path.Type.volume));
        final Path vault = new Path(home, new AlphanumericRandomStringService().random(), EnumSet.of(Path.Type.directory));
        final Path folder = new Path(vault, new AlphanumericRandomStringService().random(), EnumSet.of(Path.Type.directory));
        final Path file = new Path(folder, new AlphanumericRandomStringService().random(), EnumSet.of(Path.Type.file));
        final CryptoVault cryptomator = new CryptoVault(vault);
        cryptomator.create(session, null, new VaultCredentials("test"), new DisabledPasswordStore());
        final DefaultVaultRegistry registry = new DefaultVaultRegistry(new DisabledPasswordStore(), new DisabledPasswordCallback(), cryptomator);
        session.withRegistry(registry);
        final B2FileidProvider fileid = new B2FileidProvider(session).withCache(cache);
        new CryptoDirectoryFeature<BaseB2Response>(session, new B2DirectoryFeature(session, fileid), new B2WriteFeature(session, fileid), cryptomator).mkdir(folder, null, new TransferStatus());
        assertTrue(new CryptoFindFeature(session, new DefaultFindFeature(session), cryptomator).find(folder, new DisabledListProgressListener()));
        new CryptoTouchFeature<BaseB2Response>(session, new DefaultTouchFeature<BaseB2Response>(new DefaultUploadFeature<BaseB2Response>(new B2WriteFeature(session, fileid)),
            new B2AttributesFinderFeature(session, fileid)), new B2WriteFeature(session, fileid), cryptomator).touch(file, new TransferStatus());
        assertTrue(new CryptoFindFeature(session, new DefaultFindFeature(session), cryptomator).find(file, new DisabledListProgressListener()));
        // copy file
        final Path fileRenamed = new Path(folder, "f1", EnumSet.of(Path.Type.file));
        new CopyWorker(Collections.singletonMap(file, fileRenamed), new SessionPool.SingleSessionPool(session, registry), PathCache.empty(), new DisabledProgressListener(), new DisabledConnectionCallback()).run(session);
        assertTrue(new CryptoFindFeature(session, new B2FindFeature(session, fileid), cryptomator).find(file, new DisabledListProgressListener()));
        assertTrue(new CryptoFindFeature(session, new B2FindFeature(session, fileid), cryptomator).find(fileRenamed, new DisabledListProgressListener()));
        // copy folder
        final Path folderRenamed = new Path(vault, new AlphanumericRandomStringService().random(), EnumSet.of(Path.Type.directory));
        new CopyWorker(Collections.singletonMap(folder, folderRenamed), new SessionPool.SingleSessionPool(session, registry), PathCache.empty(), new DisabledProgressListener(), new DisabledConnectionCallback()).run(session);
        assertTrue(new CryptoFindFeature(session, new B2FindFeature(session, fileid), cryptomator).find(folder, new DisabledListProgressListener()));
        assertTrue(new CryptoFindFeature(session, new B2FindFeature(session, fileid), cryptomator).find(folderRenamed, new DisabledListProgressListener()));
        final Path fileRenamedInRenamedFolder = new Path(folderRenamed, "f1", EnumSet.of(Path.Type.file));
        assertTrue(new CryptoFindFeature(session, new DefaultFindFeature(session), cryptomator).find(fileRenamedInRenamedFolder, new DisabledListProgressListener()));
        registry.clear();
        new DeleteWorker(new DisabledLoginCallback(), Collections.singletonList(vault), new DisabledProgressListener()).run(session);
    }

    @Test
    public void testCopyFileIntoVault() throws Exception {
        final Path home = new Path("/test-cyberduck", EnumSet.of(Path.Type.directory, Path.Type.volume));
        final Path vault = new Path(home, new AlphanumericRandomStringService().random(), EnumSet.of(Path.Type.directory));
        final Path cleartextFile = new Path(home, new AlphanumericRandomStringService().random(), EnumSet.of(Path.Type.file));
        final B2FileidProvider fileid = new B2FileidProvider(session).withCache(cache);
        new B2TouchFeature(session, fileid).touch(cleartextFile, new TransferStatus());
        assertTrue(new B2FindFeature(session, fileid).find(cleartextFile, new DisabledListProgressListener()));
        final Path encryptedFolder = new Path(vault, new AlphanumericRandomStringService().random(), EnumSet.of(Path.Type.directory));
        final Path encryptedFile = new Path(encryptedFolder, new AlphanumericRandomStringService().random(), EnumSet.of(Path.Type.file));
        final CryptoVault cryptomator = new CryptoVault(vault);
        cryptomator.create(session, null, new VaultCredentials("test"), new DisabledPasswordStore());
        final DefaultVaultRegistry registry = new DefaultVaultRegistry(new DisabledPasswordStore(), new DisabledPasswordCallback(), cryptomator);
        session.withRegistry(registry);
        new CryptoDirectoryFeature<BaseB2Response>(session, new B2DirectoryFeature(session, fileid), new B2WriteFeature(session, fileid), cryptomator).mkdir(encryptedFolder, null, new TransferStatus());
        assertTrue(new CryptoFindFeature(session, new DefaultFindFeature(session), cryptomator).find(encryptedFolder, new DisabledListProgressListener()));
        // copy file into vault
        final CopyWorker worker = new CopyWorker(Collections.singletonMap(cleartextFile, encryptedFile), new SessionPool.SingleSessionPool(session, registry), PathCache.empty(), new DisabledProgressListener(), new DisabledConnectionCallback());
        worker.run(session);
        assertTrue(new B2FindFeature(session, fileid).find(cleartextFile, new DisabledListProgressListener()));
        assertTrue(new CryptoFindFeature(session, new B2FindFeature(session, fileid), cryptomator).find(encryptedFile, new DisabledListProgressListener()));
        registry.clear();
        new DeleteWorker(new DisabledLoginCallback(), Collections.singletonList(vault), new DisabledProgressListener()).run(session);
    }

    @Test
    public void testCopyDirectoryIntoVault() throws Exception {
        final Path home = new Path("/test-cyberduck", EnumSet.of(Path.Type.directory, Path.Type.volume));
        final Path vault = new Path(home, new AlphanumericRandomStringService().random(), EnumSet.of(Path.Type.directory));
        final Path cleartextFolder = new Path(home, new AlphanumericRandomStringService().random(), EnumSet.of(Path.Type.directory));
        final Path cleartextFile = new Path(cleartextFolder, new AlphanumericRandomStringService().random(), EnumSet.of(Path.Type.file));
        final B2FileidProvider fileid = new B2FileidProvider(session).withCache(cache);
        new B2DirectoryFeature(session, fileid).mkdir(cleartextFolder, null, new TransferStatus());
        new B2TouchFeature(session, fileid).touch(cleartextFile, new TransferStatus());
        assertTrue(new B2FindFeature(session, fileid).find(cleartextFolder, new DisabledListProgressListener()));
        assertTrue(new B2FindFeature(session, fileid).find(cleartextFile, new DisabledListProgressListener()));
        final CryptoVault cryptomator = new CryptoVault(vault);
        cryptomator.create(session, null, new VaultCredentials("test"), new DisabledPasswordStore());
        final DefaultVaultRegistry registry = new DefaultVaultRegistry(new DisabledPasswordStore(), new DisabledPasswordCallback(), cryptomator);
        session.withRegistry(registry);
        // move directory into vault
        final Path encryptedFolder = new Path(vault, cleartextFolder.getName(), EnumSet.of(Path.Type.directory));
        final Path encryptedFile = new Path(encryptedFolder, cleartextFile.getName(), EnumSet.of(Path.Type.file));
        final CopyWorker worker = new CopyWorker(Collections.singletonMap(cleartextFolder, encryptedFolder), new SessionPool.SingleSessionPool(session, registry), PathCache.empty(), new DisabledProgressListener(), new DisabledConnectionCallback());
        worker.run(session);
        assertTrue(new CryptoFindFeature(session, new DefaultFindFeature(session), cryptomator).find(encryptedFolder, new DisabledListProgressListener()));
        assertTrue(new CryptoFindFeature(session, new DefaultFindFeature(session), cryptomator).find(encryptedFile, new DisabledListProgressListener()));
        assertTrue(new B2FindFeature(session, fileid).find(cleartextFolder, new DisabledListProgressListener()));
        assertTrue(new B2FindFeature(session, fileid).find(cleartextFile, new DisabledListProgressListener()));
        registry.clear();
        new DeleteWorker(new DisabledLoginCallback(), Collections.singletonList(vault), new DisabledProgressListener()).run(session);
    }

    @Test
    public void testCopyFileOutsideVault() throws Exception {
        final Path home = new Path("/test-cyberduck", EnumSet.of(Path.Type.directory, Path.Type.volume));
        final Path vault = new Path(home, new AlphanumericRandomStringService().random(), EnumSet.of(Path.Type.directory));
        final Path clearFolder = new Path(home, new AlphanumericRandomStringService().random(), EnumSet.of(Path.Type.directory));
        final B2FileidProvider fileid = new B2FileidProvider(session).withCache(cache);
        new B2DirectoryFeature(session, fileid).mkdir(clearFolder, null, new TransferStatus());
        final Path encryptedFolder = new Path(vault, new AlphanumericRandomStringService().random(), EnumSet.of(Path.Type.directory));
        final Path encryptedFile = new Path(encryptedFolder, new AlphanumericRandomStringService().random(), EnumSet.of(Path.Type.file));
        final CryptoVault cryptomator = new CryptoVault(vault);
        cryptomator.create(session, null, new VaultCredentials("test"), new DisabledPasswordStore());
        final DefaultVaultRegistry registry = new DefaultVaultRegistry(new DisabledPasswordStore(), new DisabledPasswordCallback(), cryptomator);
        session.withRegistry(registry);
        new CryptoDirectoryFeature<BaseB2Response>(session, new B2DirectoryFeature(session, fileid), new B2WriteFeature(session, fileid), cryptomator).mkdir(encryptedFolder, null, new TransferStatus());
        assertTrue(new CryptoFindFeature(session, new DefaultFindFeature(session), cryptomator).find(encryptedFolder, new DisabledListProgressListener()));
        new CryptoTouchFeature<BaseB2Response>(session, new DefaultTouchFeature<BaseB2Response>(new DefaultUploadFeature<BaseB2Response>(new B2WriteFeature(session, fileid)),
            new B2AttributesFinderFeature(session, fileid)), new B2WriteFeature(session, fileid), cryptomator).touch(encryptedFile, new TransferStatus());
        assertTrue(new CryptoFindFeature(session, new DefaultFindFeature(session), cryptomator).find(encryptedFile, new DisabledListProgressListener()));
        // move file outside vault
        final Path cleartextFile = new Path(clearFolder, new AlphanumericRandomStringService().random(), EnumSet.of(Path.Type.file));
        final CopyWorker worker = new CopyWorker(Collections.singletonMap(encryptedFile, cleartextFile), new SessionPool.SingleSessionPool(session, registry), PathCache.empty(), new DisabledProgressListener(), new DisabledConnectionCallback());
        worker.run(session);
        assertTrue(new CryptoFindFeature(session, new B2FindFeature(session, fileid), cryptomator).find(encryptedFile, new DisabledListProgressListener()));
        assertTrue(new B2FindFeature(session, fileid).find(cleartextFile, new DisabledListProgressListener()));
        registry.clear();
        new DeleteWorker(new DisabledLoginCallback(), Arrays.asList(vault, clearFolder), new DisabledProgressListener()).run(session);
    }

    @Test
    public void testCopyDirectoryOutsideVault() throws Exception {
        final Path home = new Path("/test-cyberduck", EnumSet.of(Path.Type.directory, Path.Type.volume));
        final Path vault = new Path(home, new AlphanumericRandomStringService().random(), EnumSet.of(Path.Type.directory));
        final Path encryptedFolder = new Path(vault, new AlphanumericRandomStringService().random(), EnumSet.of(Path.Type.directory));
        final Path encryptedFile = new Path(encryptedFolder, new AlphanumericRandomStringService().random(), EnumSet.of(Path.Type.file));
        final CryptoVault cryptomator = new CryptoVault(vault);
        cryptomator.create(session, null, new VaultCredentials("test"), new DisabledPasswordStore());
        final DefaultVaultRegistry registry = new DefaultVaultRegistry(new DisabledPasswordStore(), new DisabledPasswordCallback(), cryptomator);
        session.withRegistry(registry);
        final B2FileidProvider fileid = new B2FileidProvider(session).withCache(cache);
        new CryptoDirectoryFeature<BaseB2Response>(session, new B2DirectoryFeature(session, fileid), new B2WriteFeature(session, fileid), cryptomator).mkdir(encryptedFolder, null, new TransferStatus());
        assertTrue(new CryptoFindFeature(session, new DefaultFindFeature(session), cryptomator).find(encryptedFolder, new DisabledListProgressListener()));
        new CryptoTouchFeature<BaseB2Response>(session, new DefaultTouchFeature<BaseB2Response>(new DefaultUploadFeature<BaseB2Response>(new B2WriteFeature(session, fileid)),
            new B2AttributesFinderFeature(session, fileid)), new B2WriteFeature(session, fileid), cryptomator).touch(encryptedFile, new TransferStatus());
        assertTrue(new CryptoFindFeature(session, new DefaultFindFeature(session), cryptomator).find(encryptedFile, new DisabledListProgressListener()));
        // copy directory outside vault
        final Path cleartextFolder = new Path(home, new AlphanumericRandomStringService().random(), EnumSet.of(Path.Type.directory));
        final CopyWorker worker = new CopyWorker(Collections.singletonMap(encryptedFolder, cleartextFolder), new SessionPool.SingleSessionPool(session, registry), PathCache.empty(), new DisabledProgressListener(), new DisabledConnectionCallback());
        worker.run(session);
        assertTrue(new CryptoFindFeature(session, new B2FindFeature(session, fileid), cryptomator).find(encryptedFolder, new DisabledListProgressListener()));
        assertTrue(new CryptoFindFeature(session, new B2FindFeature(session, fileid), cryptomator).find(encryptedFile, new DisabledListProgressListener()));
        assertTrue(new B2FindFeature(session, fileid).find(cleartextFolder, new DisabledListProgressListener()));
        final Path fileRenamed = new Path(cleartextFolder, encryptedFile.getName(), EnumSet.of(Path.Type.file));
        assertTrue(new B2FindFeature(session, fileid).find(fileRenamed, new DisabledListProgressListener()));
        registry.clear();
        new DeleteWorker(new DisabledLoginCallback(), Arrays.asList(cleartextFolder, vault), new DisabledProgressListener()).run(session);
    }
}
