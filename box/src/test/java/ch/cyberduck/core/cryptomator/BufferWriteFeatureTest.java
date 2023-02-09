package ch.cyberduck.core.cryptomator;

/*
 * Copyright (c) 2002-2021 iterate GmbH. All rights reserved.
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
import ch.cyberduck.core.BytecountStreamListener;
import ch.cyberduck.core.DisabledConnectionCallback;
import ch.cyberduck.core.DisabledLoginCallback;
import ch.cyberduck.core.DisabledPasswordCallback;
import ch.cyberduck.core.DisabledPasswordStore;
import ch.cyberduck.core.Path;
import ch.cyberduck.core.box.AbstractBoxTest;
import ch.cyberduck.core.box.BoxDeleteFeature;
import ch.cyberduck.core.box.BoxDirectoryFeature;
import ch.cyberduck.core.box.BoxFileidProvider;
import ch.cyberduck.core.box.BoxFindFeature;
import ch.cyberduck.core.box.BoxReadFeature;
import ch.cyberduck.core.cryptomator.features.CryptoFindV6Feature;
import ch.cyberduck.core.cryptomator.features.CryptoReadFeature;
import ch.cyberduck.core.cryptomator.features.CryptoWriteFeature;
import ch.cyberduck.core.cryptomator.random.RandomNonceGenerator;
import ch.cyberduck.core.features.Delete;
import ch.cyberduck.core.io.StatusOutputStream;
import ch.cyberduck.core.io.StreamCopier;
import ch.cyberduck.core.shared.BufferWriteFeature;
import ch.cyberduck.core.transfer.TransferStatus;
import ch.cyberduck.core.vault.DefaultVaultRegistry;
import ch.cyberduck.core.vault.VaultCredentials;
import ch.cyberduck.test.IntegrationTest;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.RandomUtils;
import org.cryptomator.cryptolib.api.FileHeader;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.Arrays;
import java.util.EnumSet;

import static org.junit.Assert.*;

@Category(IntegrationTest.class)
@RunWith(value = Parameterized.class)
public class BufferWriteFeatureTest extends AbstractBoxTest {

    @Test
    public void testWriteVault() throws Exception {
        final BoxFileidProvider fileid = new BoxFileidProvider(session);
        final Path container = new BoxDirectoryFeature(session, fileid).mkdir(new Path(new AlphanumericRandomStringService().random(), EnumSet.of(Path.Type.directory)), new TransferStatus());
        final Path vault = new Path(container, new AlphanumericRandomStringService().random(), EnumSet.of(Path.Type.directory));
        final Path test = new Path(vault, new AlphanumericRandomStringService().random(), EnumSet.of(Path.Type.file));
        final CryptoVault cryptomator = new CryptoVault(vault);
        cryptomator.create(session, new VaultCredentials("test"), new DisabledPasswordStore(), vaultVersion);
        session.withRegistry(new DefaultVaultRegistry(new DisabledPasswordStore(), new DisabledPasswordCallback(), cryptomator));
        final CryptoWriteFeature feature = new CryptoWriteFeature<>(session, new BufferWriteFeature(session), cryptomator);
        final byte[] content = RandomUtils.nextBytes(1024 * 1024);
        final TransferStatus writeStatus = new TransferStatus();
        final FileHeader header = cryptomator.getFileHeaderCryptor().create();
        writeStatus.setHeader(cryptomator.getFileHeaderCryptor().encryptHeader(header));
        writeStatus.setNonces(new RandomNonceGenerator(cryptomator.getNonceSize()));
        writeStatus.setChecksum(feature.checksum(test, new TransferStatus()).compute(new ByteArrayInputStream(content), new TransferStatus()));
        writeStatus.setLength(content.length);
        final StatusOutputStream out = feature.write(test, writeStatus, new DisabledConnectionCallback());
        final ByteArrayInputStream in = new ByteArrayInputStream(content);
        final TransferStatus progress = new TransferStatus();
        final BytecountStreamListener count = new BytecountStreamListener();
        new StreamCopier(new TransferStatus(), progress).withListener(count).transfer(in, out);
        assertEquals(content.length, count.getSent());
        assertEquals(content.length, count.getRecv());
        assertTrue(new CryptoFindV6Feature(session, new BoxFindFeature(session, fileid), cryptomator).find(test));
        final byte[] compare = new byte[content.length];
        final InputStream stream = new CryptoReadFeature(session, new BoxReadFeature(session, fileid), cryptomator).read(test, new TransferStatus().withLength(content.length), new DisabledConnectionCallback());
        IOUtils.readFully(stream, compare);
        stream.close();
        assertArrayEquals(content, compare);
        cryptomator.getFeature(session, Delete.class, new BoxDeleteFeature(session, fileid)).delete(Arrays.asList(test, vault), new DisabledLoginCallback(), new Delete.DisabledCallback());
    }
}
