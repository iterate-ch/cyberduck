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
import ch.cyberduck.core.Host;
import ch.cyberduck.core.Local;
import ch.cyberduck.core.NullFilter;
import ch.cyberduck.core.Path;
import ch.cyberduck.core.TestProtocol;
import ch.cyberduck.core.b2.AbstractB2Test;
import ch.cyberduck.core.b2.B2AttributesFinderFeature;
import ch.cyberduck.core.b2.B2DeleteFeature;
import ch.cyberduck.core.b2.B2FindFeature;
import ch.cyberduck.core.b2.B2ReadFeature;
import ch.cyberduck.core.b2.B2VersionIdProvider;
import ch.cyberduck.core.cryptomator.features.CryptoReadFeature;
import ch.cyberduck.core.features.AttributesFinder;
import ch.cyberduck.core.features.Delete;
import ch.cyberduck.core.features.Find;
import ch.cyberduck.core.io.DisabledStreamListener;
import ch.cyberduck.core.io.StreamCopier;
import ch.cyberduck.core.local.DefaultLocalDirectoryFeature;
import ch.cyberduck.core.notification.DisabledNotificationService;
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
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Arrays;
import java.util.Collections;
import java.util.EnumSet;

import static org.junit.Assert.*;

@Category(IntegrationTest.class)
@RunWith(value = Parameterized.class)
public class CryptoB2SingleTransferWorkerTest extends AbstractB2Test {

    @Test
    public void testUpload() throws Exception {
        final Path home = new Path("/test-cyberduck", EnumSet.of(Path.Type.directory, Path.Type.volume));
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
        cryptomator.create(session, new VaultCredentials("test"), vaultVersion);
        session.withRegistry(new DefaultVaultRegistry(new DisabledPasswordStore(), new DisabledPasswordCallback(), cryptomator));
        final Transfer t = new UploadTransfer(new Host(new TestProtocol()), Collections.singletonList(new TransferItem(dir1, localDirectory1)), new NullFilter<>());
        assertTrue(new SingleTransferWorker(session, session, t, new TransferOptions(), new TransferSpeedometer(t), new DisabledTransferPrompt() {
            @Override
            public TransferAction prompt(final TransferItem file) {
                return TransferAction.overwrite;
            }
        }, new DisabledTransferErrorCallback(),
            new DisabledProgressListener(), new DisabledStreamListener(), new DisabledLoginCallback(), new DisabledNotificationService()) {

        }.run(session));
        final B2VersionIdProvider fileid = new B2VersionIdProvider(session);
        assertTrue(cryptomator.getFeature(session, Find.class, new B2FindFeature(session, fileid)).find(dir1));
        assertEquals(content.length, cryptomator.getFeature(session, AttributesFinder.class, new B2AttributesFinderFeature(session, fileid)).find(file1).getSize());
        {
            final ByteArrayOutputStream buffer = new ByteArrayOutputStream(content.length);
            final InputStream in = new CryptoReadFeature(session, new B2ReadFeature(session, fileid), cryptomator).read(file1, new TransferStatus().withLength(content.length), new DisabledConnectionCallback());
            new StreamCopier(new TransferStatus(), new TransferStatus()).transfer(in, buffer);
            assertArrayEquals(content, buffer.toByteArray());
        }
        assertEquals(content.length, cryptomator.getFeature(session, AttributesFinder.class, new B2AttributesFinderFeature(session, fileid)).find(file2).getSize());
        {
            final ByteArrayOutputStream buffer = new ByteArrayOutputStream(content.length);
            final InputStream in = new CryptoReadFeature(session, new B2ReadFeature(session, fileid), cryptomator).read(file1, new TransferStatus().withLength(content.length), new DisabledConnectionCallback());
            new StreamCopier(new TransferStatus(), new TransferStatus()).transfer(in, buffer);
            assertArrayEquals(content, buffer.toByteArray());
        }
        cryptomator.getFeature(session, Delete.class, new B2DeleteFeature(session, fileid)).delete(Arrays.asList(file1, file2, dir1, vault), new DisabledLoginCallback(), new Delete.DisabledCallback());
        localFile1.delete();
        localFile2.delete();
        localDirectory1.delete();
    }
}
