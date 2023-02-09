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
import ch.cyberduck.core.SimplePathPredicate;
import ch.cyberduck.core.cryptomator.features.CryptoFindV6Feature;
import ch.cyberduck.core.cryptomator.features.CryptoListService;
import ch.cyberduck.core.cryptomator.features.CryptoReadFeature;
import ch.cyberduck.core.cryptomator.features.CryptoWriteFeature;
import ch.cyberduck.core.cryptomator.random.RotatingNonceGenerator;
import ch.cyberduck.core.features.Delete;
import ch.cyberduck.core.io.StreamCopier;
import ch.cyberduck.core.onedrive.AbstractOneDriveTest;
import ch.cyberduck.core.onedrive.GraphItemListService;
import ch.cyberduck.core.onedrive.OneDriveHomeFinderService;
import ch.cyberduck.core.onedrive.features.GraphDeleteFeature;
import ch.cyberduck.core.onedrive.features.GraphFindFeature;
import ch.cyberduck.core.onedrive.features.GraphReadFeature;
import ch.cyberduck.core.onedrive.features.GraphWriteFeature;
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
import org.nuxeo.onedrive.client.types.DriveItem;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Arrays;
import java.util.EnumSet;

import static org.junit.Assert.*;

@Category(IntegrationTest.class)
@RunWith(value = Parameterized.class)
public class GraphWriteFeatureTest extends AbstractOneDriveTest {

    @Test
    public void testWrite() throws Exception {
        final TransferStatus status = new TransferStatus();
        final int length = 1048576;
        final byte[] content = RandomUtils.nextBytes(length);
        status.setLength(content.length);
        final Path home = new OneDriveHomeFinderService().find();
        final Path vault = new Path(home, new AlphanumericRandomStringService().random(), EnumSet.of(Path.Type.directory));
        final Path test = new Path(vault, new AlphanumericRandomStringService().random(), EnumSet.of(Path.Type.file));
        final CryptoVault cryptomator = new CryptoVault(vault);
        cryptomator.create(session, new VaultCredentials("test"), new DisabledPasswordStore(), vaultVersion);
        session.withRegistry(new DefaultVaultRegistry(new DisabledPasswordStore(), new DisabledPasswordCallback(), cryptomator));
        final CryptoWriteFeature<DriveItem.Metadata> writer = new CryptoWriteFeature<>(session, new GraphWriteFeature(session, fileid), cryptomator);
        final FileHeader header = cryptomator.getFileHeaderCryptor().create();
        status.setHeader(cryptomator.getFileHeaderCryptor().encryptHeader(header));
        status.setNonces(new RotatingNonceGenerator(cryptomator.getNonceSize(), cryptomator.numberOfChunks(content.length)));
        status.setChecksum(writer.checksum(test, status).compute(new ByteArrayInputStream(content), status));
        final OutputStream out = writer.write(test, status, new DisabledConnectionCallback());
        assertNotNull(out);
        new StreamCopier(status, status).transfer(new ByteArrayInputStream(content), out);
        out.close();
        assertTrue(new CryptoFindV6Feature(session, new GraphFindFeature(session, fileid), cryptomator).find(test));
        final PathAttributes attributes = new CryptoListService(session, new GraphItemListService(session, fileid), cryptomator).list(test.getParent(), new DisabledListProgressListener()).find(new SimplePathPredicate(test)).attributes();
        assertEquals(content.length, attributes.getSize());
        assertEquals(content.length, new CryptoWriteFeature<>(session, new GraphWriteFeature(session, fileid), cryptomator).append(test, status.withRemote(attributes)).size, 0L);
        final ByteArrayOutputStream buffer = new ByteArrayOutputStream(content.length);
        final InputStream in = new CryptoReadFeature(session, new GraphReadFeature(session, fileid), cryptomator).read(test, new TransferStatus().withLength(content.length), new DisabledConnectionCallback());
        new StreamCopier(status, status).transfer(in, buffer);
        assertArrayEquals(content, buffer.toByteArray());
        cryptomator.getFeature(session, Delete.class, new GraphDeleteFeature(session, fileid)).delete(Arrays.asList(test, vault), new DisabledLoginCallback(), new Delete.DisabledCallback());
    }
}
