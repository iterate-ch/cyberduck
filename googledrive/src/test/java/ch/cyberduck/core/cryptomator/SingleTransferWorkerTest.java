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
import ch.cyberduck.core.Host;
import ch.cyberduck.core.Local;
import ch.cyberduck.core.NullFilter;
import ch.cyberduck.core.Path;
import ch.cyberduck.core.TestProtocol;
import ch.cyberduck.core.cryptomator.features.CryptoAttributesFeature;
import ch.cyberduck.core.cryptomator.features.CryptoDeleteFeature;
import ch.cyberduck.core.cryptomator.features.CryptoFindFeature;
import ch.cyberduck.core.cryptomator.features.CryptoReadFeature;
import ch.cyberduck.core.features.Delete;
import ch.cyberduck.core.googledrive.AbstractDriveTest;
import ch.cyberduck.core.googledrive.DriveDeleteFeature;
import ch.cyberduck.core.googledrive.DriveFileidProvider;
import ch.cyberduck.core.googledrive.DriveHomeFinderService;
import ch.cyberduck.core.googledrive.DriveReadFeature;
import ch.cyberduck.core.io.DisabledStreamListener;
import ch.cyberduck.core.io.StreamCopier;
import ch.cyberduck.core.local.DefaultLocalDirectoryFeature;
import ch.cyberduck.core.notification.DisabledNotificationService;
import ch.cyberduck.core.shared.DefaultAttributesFinderFeature;
import ch.cyberduck.core.shared.DefaultFindFeature;
import ch.cyberduck.core.transfer.DisabledTransferErrorCallback;
import ch.cyberduck.core.transfer.DisabledTransferPrompt;
import ch.cyberduck.core.transfer.Transfer;
import ch.cyberduck.core.transfer.TransferAction;
import ch.cyberduck.core.transfer.TransferItem;
import ch.cyberduck.core.transfer.TransferOptions;
import ch.cyberduck.core.transfer.TransferSpeedometer;
import ch.cyberduck.core.transfer.TransferStatus;
import ch.cyberduck.core.transfer.UploadTransfer;
import ch.cyberduck.core.vault.DefaultVaultRegistry;
import ch.cyberduck.core.vault.VaultCredentials;
import ch.cyberduck.core.worker.SingleTransferWorker;
import ch.cyberduck.test.IntegrationTest;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.RandomUtils;
import org.junit.Assert;
import org.junit.Test;
import org.junit.experimental.categories.Category;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Arrays;
import java.util.Collections;
import java.util.EnumSet;

@Category(IntegrationTest.class)
public class SingleTransferWorkerTest extends AbstractDriveTest {

    @Test
    public void testUpload() throws Exception {
        final Path home = DriveHomeFinderService.MYDRIVE_FOLDER;
        final Path vault = new Path(home, new AlphanumericRandomStringService().random(), EnumSet.of(Path.Type.directory));
        final Path dir1 = new Path(vault, new AlphanumericRandomStringService().random(), EnumSet.of(Path.Type.directory));
        final Local localDirectory1 = new Local(System.getProperty("java.io.tmpdir"), new AlphanumericRandomStringService().random());
        new DefaultLocalDirectoryFeature().mkdir(localDirectory1);
        final byte[] content = RandomUtils.nextBytes(62768);
        final Path file1 = new Path(dir1, new AlphanumericRandomStringService().random(), EnumSet.of(Path.Type.file));
        final Local localFile1 = new Local(localDirectory1, file1.getName());
        final OutputStream out1 = localFile1.getOutputStream(false);
        IOUtils.write(content, out1);
        out1.close();
        final Path file2 = new Path(dir1, new AlphanumericRandomStringService().random(), EnumSet.of(Path.Type.file));
        final Local localFile2 = new Local(localDirectory1, file2.getName());
        final OutputStream out2 = localFile2.getOutputStream(false);
        IOUtils.write(content, out2);
        out2.close();
        final CryptoVault cryptomator = new CryptoVault(vault);
        cryptomator.create(session, null, new VaultCredentials("test"), new DisabledPasswordStore());
        session.withRegistry(new DefaultVaultRegistry(new DisabledPasswordStore(), new DisabledPasswordCallback(), cryptomator));
        final Transfer t = new UploadTransfer(new Host(new TestProtocol()), Collections.singletonList(new TransferItem(dir1, localDirectory1)), new NullFilter<>());
        Assert.assertTrue(new SingleTransferWorker(session, session, t, new TransferOptions(), new TransferSpeedometer(t), new DisabledTransferPrompt() {
            @Override
            public TransferAction prompt(final TransferItem file) {
                return TransferAction.overwrite;
            }
        }, new DisabledTransferErrorCallback(),
            new DisabledProgressListener(), new DisabledStreamListener(), new DisabledLoginCallback(), new DisabledPasswordCallback(), new DisabledNotificationService()) {

        }.run(session, session));
        Assert.assertTrue(new CryptoFindFeature(session, new DefaultFindFeature(session), cryptomator).find(dir1, new DisabledListProgressListener()));
        Assert.assertEquals(content.length, new CryptoAttributesFeature(session, new DefaultAttributesFinderFeature(session), cryptomator).find(file1, new DisabledListProgressListener()).getSize());
        {
            final ByteArrayOutputStream buffer = new ByteArrayOutputStream(content.length);
            final InputStream in = new CryptoReadFeature(session, new DriveReadFeature(session, new DriveFileidProvider(session).withCache(cache)), cryptomator).read(file1, new TransferStatus().length(content.length), new DisabledConnectionCallback());
            new StreamCopier(new TransferStatus(), new TransferStatus()).transfer(in, buffer);
            Assert.assertArrayEquals(content, buffer.toByteArray());
        }
        Assert.assertEquals(content.length, new CryptoAttributesFeature(session, new DefaultAttributesFinderFeature(session), cryptomator).find(file2, new DisabledListProgressListener()).getSize());
        {
            final ByteArrayOutputStream buffer = new ByteArrayOutputStream(content.length);
            final InputStream in = new CryptoReadFeature(session, new DriveReadFeature(session, new DriveFileidProvider(session).withCache(cache)), cryptomator).read(file1, new TransferStatus().length(content.length), new DisabledConnectionCallback());
            new StreamCopier(new TransferStatus(), new TransferStatus()).transfer(in, buffer);
            Assert.assertArrayEquals(content, buffer.toByteArray());
        }
        new CryptoDeleteFeature(session, new DriveDeleteFeature(session, new DriveFileidProvider(session).withCache(cache)), cryptomator).delete(Arrays.asList(file1, file2, dir1, vault), new DisabledLoginCallback(), new Delete.DisabledCallback());
        localFile1.delete();
        localFile2.delete();
        localDirectory1.delete();
    }
}
