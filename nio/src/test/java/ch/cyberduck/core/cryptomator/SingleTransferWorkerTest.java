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

import ch.cyberduck.core.*;
import ch.cyberduck.core.cryptomator.features.CryptoAttributesFeature;
import ch.cyberduck.core.cryptomator.features.CryptoDeleteFeature;
import ch.cyberduck.core.cryptomator.features.CryptoFindFeature;
import ch.cyberduck.core.cryptomator.features.CryptoReadFeature;
import ch.cyberduck.core.exception.LoginCanceledException;
import ch.cyberduck.core.features.Delete;
import ch.cyberduck.core.io.DisabledStreamListener;
import ch.cyberduck.core.io.StreamCopier;
import ch.cyberduck.core.nio.LocalAttributesFinderFeature;
import ch.cyberduck.core.nio.LocalDeleteFeature;
import ch.cyberduck.core.nio.LocalFindFeature;
import ch.cyberduck.core.nio.LocalHomeFinderFeature;
import ch.cyberduck.core.nio.LocalProtocol;
import ch.cyberduck.core.nio.LocalReadFeature;
import ch.cyberduck.core.nio.LocalSession;
import ch.cyberduck.core.preferences.PreferencesFactory;
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

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertTrue;

@Category(IntegrationTest.class)
public class SingleTransferWorkerTest {

    @Test
    public void testUpload() throws Exception {
        final LocalSession session = new LocalSession(new Host(new LocalProtocol(), new LocalProtocol().getDefaultHostname()));
        session.open(new DisabledHostKeyCallback(), new DisabledLoginCallback());
        session.login(new DisabledPasswordStore(), new DisabledLoginCallback(), new DisabledCancelCallback());
        final Path home = new LocalHomeFinderFeature(session).find();
        final Path vault = new Path(home, new AlphanumericRandomStringService().random(), EnumSet.of(Path.Type.directory));
        StringBuilder directoryname = new StringBuilder();
        for(int i = 0; i < 10; i++) {
            directoryname.append(new AlphanumericRandomStringService().random());
        }
        final Path dir1 = new Path(vault, directoryname.toString(), EnumSet.of(Path.Type.directory));
        final Local localDirectory1 = new Local(System.getProperty("java.io.tmpdir"), directoryname.toString());
        localDirectory1.mkdir();
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
        session.withRegistry(new DefaultVaultRegistry(new DisabledPasswordStore(), new PasswordCallback() {
            @Override
            public Credentials prompt(final Host bookmark, final String title, final String reason, final LoginOptions options) throws LoginCanceledException {
                return new VaultCredentials("test");
            }
        }));
        PreferencesFactory.get().setProperty("factory.vault.class", CryptoVault.class.getName());
        final Transfer t = new UploadTransfer(new Host(new TestProtocol()), Collections.singletonList(new TransferItem(dir1, localDirectory1)), new NullFilter<>());
        assertTrue(new SingleTransferWorker(session, session, t, new TransferOptions(), new TransferSpeedometer(t), new DisabledTransferPrompt() {
            @Override
            public TransferAction prompt(final TransferItem file) {
                return TransferAction.overwrite;
            }
        }, new DisabledTransferErrorCallback(),
            new DisabledProgressListener(), new DisabledStreamListener(), new DisabledLoginCallback(), new DisabledPasswordCallback()) {

        }.run(session, session));
        assertTrue(new CryptoFindFeature(session, new LocalFindFeature(session), cryptomator).find(dir1));
        Assert.assertEquals(content.length, new CryptoAttributesFeature(session, new LocalAttributesFinderFeature(session), cryptomator).find(file1).getSize());
        {
            final ByteArrayOutputStream buffer = new ByteArrayOutputStream(content.length);
            final InputStream in = new CryptoReadFeature(session, new LocalReadFeature(session), cryptomator).read(file1, new TransferStatus().length(content.length), new DisabledConnectionCallback());
            new StreamCopier(new TransferStatus(), new TransferStatus()).transfer(in, buffer);
            assertArrayEquals(content, buffer.toByteArray());
        }
        Assert.assertEquals(content.length, new CryptoAttributesFeature(session, new LocalAttributesFinderFeature(session), cryptomator).find(file2).getSize());
        {
            final ByteArrayOutputStream buffer = new ByteArrayOutputStream(content.length);
            final InputStream in = new CryptoReadFeature(session, new LocalReadFeature(session), cryptomator).read(file1, new TransferStatus().length(content.length), new DisabledConnectionCallback());
            new StreamCopier(new TransferStatus(), new TransferStatus()).transfer(in, buffer);
            assertArrayEquals(content, buffer.toByteArray());
        }
        new CryptoDeleteFeature(session, new LocalDeleteFeature(session), cryptomator).delete(Arrays.asList(file1, file2, dir1, vault), new DisabledLoginCallback(), new Delete.DisabledCallback());
        localFile1.delete();
        localFile2.delete();
        localDirectory1.delete();
    }
}
