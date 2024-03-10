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
import ch.cyberduck.core.cryptomator.features.CryptoReadFeature;
import ch.cyberduck.core.features.AttributesFinder;
import ch.cyberduck.core.features.Delete;
import ch.cyberduck.core.features.Find;
import ch.cyberduck.core.io.DisabledStreamListener;
import ch.cyberduck.core.io.StreamCopier;
import ch.cyberduck.core.local.DefaultLocalDirectoryFeature;
import ch.cyberduck.core.nio.LocalAttributesFinderFeature;
import ch.cyberduck.core.nio.LocalDeleteFeature;
import ch.cyberduck.core.nio.LocalFindFeature;
import ch.cyberduck.core.nio.LocalHomeFinderFeature;
import ch.cyberduck.core.nio.LocalProtocol;
import ch.cyberduck.core.nio.LocalReadFeature;
import ch.cyberduck.core.nio.LocalSession;
import ch.cyberduck.core.notification.DisabledNotificationService;
import ch.cyberduck.core.preferences.PreferencesFactory;
import ch.cyberduck.core.proxy.Proxy;
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
public class CryptoLocalSingleTransferWorkerTest {

    @Parameterized.Parameters(name = "vaultVersion = {0}")
    public static Object[] data() {
        return new Object[]{CryptoVault.VAULT_VERSION_DEPRECATED, CryptoVault.VAULT_VERSION};
    }

    @Parameterized.Parameter
    public int vaultVersion;

    @Test
    public void testUpload() throws Exception {
        final LocalSession session = new LocalSession(new Host(new LocalProtocol(), new LocalProtocol().getDefaultHostname()));
        session.open(Proxy.DIRECT, new DisabledHostKeyCallback(), new DisabledLoginCallback(), new DisabledCancelCallback());
        session.login(Proxy.DIRECT, new DisabledLoginCallback(), new DisabledCancelCallback());
        final Path home = new LocalHomeFinderFeature().find();
        final Path vault = new Path(home, new AlphanumericRandomStringService().random(), EnumSet.of(Path.Type.directory));
        StringBuilder directoryname = new StringBuilder();
        for(int i = 0; i < 10; i++) {
            directoryname.append(new AlphanumericRandomStringService().random());
        }
        final Path dir1 = new Path(vault, directoryname.toString(), EnumSet.of(Path.Type.directory));
        final Local localDirectory1 = new Local(System.getProperty("java.io.tmpdir"), directoryname.toString());
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
        session.withRegistry(new DefaultVaultRegistry(new DisabledPasswordStore(), new DisabledPasswordCallback() {
            @Override
            public Credentials prompt(final Host bookmark, final String title, final String reason, final LoginOptions options) {
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
                new DisabledProgressListener(), new DisabledStreamListener(), new DisabledLoginCallback(), new DisabledNotificationService()) {

        }.run(session));
        assertTrue(cryptomator.getFeature(session, Find.class, new LocalFindFeature(session)).find(dir1));
        assertEquals(content.length, cryptomator.getFeature(session, AttributesFinder.class, new LocalAttributesFinderFeature(session)).find(file1).getSize());
        {
            final ByteArrayOutputStream buffer = new ByteArrayOutputStream(content.length);
            final InputStream in = new CryptoReadFeature(session, new LocalReadFeature(session), cryptomator).read(file1, new TransferStatus().withLength(content.length), new DisabledConnectionCallback());
            new StreamCopier(new TransferStatus(), new TransferStatus()).transfer(in, buffer);
            assertArrayEquals(content, buffer.toByteArray());
        }
        assertEquals(content.length, cryptomator.getFeature(session, AttributesFinder.class, new LocalAttributesFinderFeature(session)).find(file2).getSize());
        {
            final ByteArrayOutputStream buffer = new ByteArrayOutputStream(content.length);
            final InputStream in = new CryptoReadFeature(session, new LocalReadFeature(session), cryptomator).read(file1, new TransferStatus().withLength(content.length), new DisabledConnectionCallback());
            new StreamCopier(new TransferStatus(), new TransferStatus()).transfer(in, buffer);
            assertArrayEquals(content, buffer.toByteArray());
        }
        cryptomator.getFeature(session, Delete.class, new LocalDeleteFeature(session)).delete(Arrays.asList(file1, file2, dir1, vault), new DisabledLoginCallback(), new Delete.DisabledCallback());
        localFile1.delete();
        localFile2.delete();
        localDirectory1.delete();
    }
}
