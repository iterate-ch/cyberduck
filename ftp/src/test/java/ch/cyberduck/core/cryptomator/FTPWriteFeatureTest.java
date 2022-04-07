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
import ch.cyberduck.core.Path;
import ch.cyberduck.core.PathAttributes;
import ch.cyberduck.core.cryptomator.features.CryptoAttributesFeature;
import ch.cyberduck.core.cryptomator.features.CryptoListService;
import ch.cyberduck.core.cryptomator.features.CryptoReadFeature;
import ch.cyberduck.core.cryptomator.features.CryptoWriteFeature;
import ch.cyberduck.core.cryptomator.random.RotatingNonceGenerator;
import ch.cyberduck.core.features.Delete;
import ch.cyberduck.core.features.Find;
import ch.cyberduck.core.ftp.AbstractFTPTest;
import ch.cyberduck.core.ftp.FTPAttributesFinderFeature;
import ch.cyberduck.core.ftp.FTPDeleteFeature;
import ch.cyberduck.core.ftp.FTPReadFeature;
import ch.cyberduck.core.ftp.FTPWriteFeature;
import ch.cyberduck.core.ftp.list.FTPListService;
import ch.cyberduck.core.io.StreamCopier;
import ch.cyberduck.core.shared.DefaultFindFeature;
import ch.cyberduck.core.shared.DefaultHomeFinderService;
import ch.cyberduck.core.transfer.TransferStatus;
import ch.cyberduck.core.vault.DefaultVaultRegistry;
import ch.cyberduck.core.vault.VaultCredentials;
import ch.cyberduck.test.IntegrationTest;

import org.apache.commons.lang3.RandomUtils;
import org.cryptomator.cryptolib.api.FileHeader;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Arrays;
import java.util.EnumSet;
import java.util.TimeZone;

import static org.junit.Assert.*;

@Category(IntegrationTest.class)
@RunWith(value = Parameterized.class)
public class FTPWriteFeatureTest extends AbstractFTPTest {

    @Test
    public void testWrite() throws Exception {
        final TransferStatus status = new TransferStatus();
        final int length = 1048576;
        final byte[] content = RandomUtils.nextBytes(length);
        status.setLength(content.length);
        final Path home = new DefaultHomeFinderService(session).find();
        final Path vault = new Path(home, new AlphanumericRandomStringService().random(), EnumSet.of(Path.Type.directory));
        final Path test = new Path(vault, new AlphanumericRandomStringService().random(), EnumSet.of(Path.Type.file));
        final CryptoVault cryptomator = new CryptoVault(vault);
        cryptomator.create(session, new VaultCredentials("test"), new DisabledPasswordStore(), vaultVersion);
        session.withRegistry(new DefaultVaultRegistry(new DisabledPasswordStore(), new DisabledPasswordCallback(), cryptomator));
        final CryptoWriteFeature<Void> writer = new CryptoWriteFeature<>(session, new FTPWriteFeature(session), cryptomator);
        final FileHeader header = cryptomator.getFileHeaderCryptor().create();
        status.setHeader(cryptomator.getFileHeaderCryptor().encryptHeader(header));
        status.setNonces(new RotatingNonceGenerator(cryptomator.numberOfChunks(content.length)));
        status.setChecksum(writer.checksum(test, status).compute(new ByteArrayInputStream(content), status));
        status.setLength(content.length);
        final OutputStream out = writer.write(test, status, new DisabledConnectionCallback());
        assertNotNull(out);
        new StreamCopier(status, status).transfer(new ByteArrayInputStream(content), out);
        out.close();
        assertEquals(TransferStatus.UNKNOWN_LENGTH, status.getResponse().getSize());
        assertTrue(cryptomator.getFeature(session, Find.class, new DefaultFindFeature(session)).find(test));
        final PathAttributes attr = new CryptoAttributesFeature(session, new FTPAttributesFinderFeature(session), cryptomator).find(test);
        assertEquals(content.length, new CryptoListService(session, new FTPListService(session, null, TimeZone.getDefault()), cryptomator).list(test.getParent(), new DisabledListProgressListener()).get(test).attributes().getSize());
        assertEquals(content.length, new CryptoWriteFeature<>(session, new FTPWriteFeature(session), cryptomator).append(test, status.withRemote(attr)).size, 0L);
        final ByteArrayOutputStream buffer = new ByteArrayOutputStream(content.length);
        final InputStream in = new CryptoReadFeature(session, new FTPReadFeature(session), cryptomator).read(test, new TransferStatus(), new DisabledConnectionCallback());
        new StreamCopier(status, status).transfer(in, buffer);
        assertArrayEquals(content, buffer.toByteArray());
        cryptomator.getFeature(session, Delete.class, new FTPDeleteFeature(session)).delete(Arrays.asList(test, vault), new DisabledLoginCallback(), new Delete.DisabledCallback());
    }
}
