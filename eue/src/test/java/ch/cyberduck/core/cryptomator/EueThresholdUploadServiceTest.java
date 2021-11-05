package ch.cyberduck.core.cryptomator;/*
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
import ch.cyberduck.core.DisabledConnectionCallback;
import ch.cyberduck.core.DisabledLoginCallback;
import ch.cyberduck.core.DisabledPasswordCallback;
import ch.cyberduck.core.DisabledPasswordStore;
import ch.cyberduck.core.Local;
import ch.cyberduck.core.Path;
import ch.cyberduck.core.cryptomator.features.CryptoAttributesFeature;
import ch.cyberduck.core.cryptomator.features.CryptoFindFeature;
import ch.cyberduck.core.cryptomator.features.CryptoReadFeature;
import ch.cyberduck.core.cryptomator.features.CryptoUploadFeature;
import ch.cyberduck.core.eue.AbstractEueSessionTest;
import ch.cyberduck.core.eue.EueAttributesFinderFeature;
import ch.cyberduck.core.eue.EueDeleteFeature;
import ch.cyberduck.core.eue.EueFindFeature;
import ch.cyberduck.core.eue.EueMultipartWriteFeature;
import ch.cyberduck.core.eue.EueReadFeature;
import ch.cyberduck.core.eue.EueResourceIdProvider;
import ch.cyberduck.core.eue.EueThresholdUploadService;
import ch.cyberduck.core.eue.EueThresholdWriteFeature;
import ch.cyberduck.core.eue.EueUploadService;
import ch.cyberduck.core.eue.EueWriteFeature;
import ch.cyberduck.core.features.Delete;
import ch.cyberduck.core.io.BandwidthThrottle;
import ch.cyberduck.core.io.DisabledStreamListener;
import ch.cyberduck.core.io.StreamCopier;
import ch.cyberduck.core.shared.DefaultHomeFinderService;
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

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.EnumSet;
import java.util.UUID;

import static org.junit.Assert.*;

@Category(IntegrationTest.class)
@RunWith(value = Parameterized.class)
public class EueThresholdUploadServiceTest extends AbstractEueSessionTest {

    @Test
    public void testUpload() throws Exception {
        final Path home = new DefaultHomeFinderService(session).find();
        final Path vault = new Path(home, new AlphanumericRandomStringService().random(), EnumSet.of(Path.Type.directory));
        final Path test = new Path(vault, new AlphanumericRandomStringService().random(), EnumSet.of(Path.Type.file));
        final CryptoVault cryptomator = new CryptoVault(vault, profile.getProperties().get("cryptomator.vault.masterkey.filename"), profile.getProperties().get("cryptomator.vault.pepper").getBytes(StandardCharsets.UTF_8));
        cryptomator.create(session, null, new VaultCredentials("test"), new DisabledPasswordStore(), vaultVersion);
        final DefaultVaultRegistry registry = new DefaultVaultRegistry(new DisabledPasswordStore(), new DisabledPasswordCallback(), cryptomator);
        session.withRegistry(registry);
        final EueResourceIdProvider fileid = new EueResourceIdProvider(session);
        final CryptoUploadFeature<EueWriteFeature.Chunk> m = new CryptoUploadFeature<>(session,
            new EueThresholdUploadService(session, fileid, registry), new EueThresholdWriteFeature(session, fileid), cryptomator);
        final Local local = new Local(System.getProperty("java.io.tmpdir"), UUID.randomUUID().toString());
        final byte[] content = RandomUtils.nextBytes(8840780);
        IOUtils.write(content, local.getOutputStream(false));
        final TransferStatus writeStatus = new TransferStatus();
        final FileHeader header = cryptomator.getFileHeaderCryptor().create();
        writeStatus.setHeader(cryptomator.getFileHeaderCryptor().encryptHeader(header));
        writeStatus.setLength(content.length);
        m.upload(test, local, new BandwidthThrottle(BandwidthThrottle.UNLIMITED), new DisabledStreamListener(), writeStatus, null);
        assertTrue(writeStatus.isComplete());
        assertTrue(new CryptoFindFeature(session, new EueFindFeature(session, fileid), cryptomator).find(test));
        assertEquals(content.length, new CryptoAttributesFeature(session, new EueAttributesFinderFeature(session, fileid), cryptomator).find(test).getSize());
        final ByteArrayOutputStream buffer = new ByteArrayOutputStream(content.length);
        final TransferStatus readStatus = new TransferStatus().withLength(content.length);
        final InputStream in = new CryptoReadFeature(session, new EueReadFeature(session, fileid), cryptomator).read(test, readStatus, new DisabledConnectionCallback());
        new StreamCopier(readStatus, readStatus).transfer(in, buffer);
        assertArrayEquals(content, buffer.toByteArray());
        cryptomator.getFeature(session, Delete.class, new EueDeleteFeature(session, fileid)).delete(Arrays.asList(test, vault), new DisabledLoginCallback(), new Delete.DisabledCallback());
        local.delete();
    }
}
