package ch.cyberduck.core.cryptomator;

/*
 * Copyright (c) 2002-2024 iterate GmbH. All rights reserved.
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

import ch.cyberduck.core.AbstractPath;
import ch.cyberduck.core.AlphanumericRandomStringService;
import ch.cyberduck.core.BytecountStreamListener;
import ch.cyberduck.core.DisabledConnectionCallback;
import ch.cyberduck.core.DisabledLoginCallback;
import ch.cyberduck.core.DisabledPasswordCallback;
import ch.cyberduck.core.DisabledPasswordStore;
import ch.cyberduck.core.Local;
import ch.cyberduck.core.Path;
import ch.cyberduck.core.cryptomator.features.CryptoReadFeature;
import ch.cyberduck.core.cryptomator.features.CryptoUploadFeature;
import ch.cyberduck.core.dav.DAVDeleteFeature;
import ch.cyberduck.core.dav.DAVDirectoryFeature;
import ch.cyberduck.core.dav.DAVFindFeature;
import ch.cyberduck.core.dav.DAVReadFeature;
import ch.cyberduck.core.features.AttributesFinder;
import ch.cyberduck.core.features.Delete;
import ch.cyberduck.core.features.Find;
import ch.cyberduck.core.io.BandwidthThrottle;
import ch.cyberduck.core.io.HashAlgorithm;
import ch.cyberduck.core.io.StreamCopier;
import ch.cyberduck.core.owncloud.AbstractOcisTest;
import ch.cyberduck.core.owncloud.OcisUploadFeature;
import ch.cyberduck.core.owncloud.OwncloudAttributesFinderFeature;
import ch.cyberduck.core.owncloud.OwncloudHomeFeature;
import ch.cyberduck.core.transfer.TransferStatus;
import ch.cyberduck.core.tus.TusCapabilities;
import ch.cyberduck.core.tus.TusWriteFeature;
import ch.cyberduck.core.vault.DefaultVaultRegistry;
import ch.cyberduck.core.vault.VaultCredentials;
import ch.cyberduck.test.IntegrationTest;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.RandomUtils;
import org.cryptomator.cryptolib.api.FileHeader;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.util.Arrays;
import java.util.EnumSet;
import java.util.UUID;

import static org.junit.Assert.*;

@Category(IntegrationTest.class)
@RunWith(value = Parameterized.class)
@Ignore
public class OcisUploadFeatureTest extends AbstractOcisTest {

    @Test
    public void testUploadVault() throws Exception {
        // 5L * 1024L * 1024L
        final Path directory = new DAVDirectoryFeature(session).mkdir(new Path(new OwncloudHomeFeature(session.getHost()).find(),
                new AlphanumericRandomStringService().random(), EnumSet.of(AbstractPath.Type.directory)), new TransferStatus());
        final CryptoVault cryptomator = new CryptoVault(
                new Path(directory, new AlphanumericRandomStringService().random(), EnumSet.of(Path.Type.directory)));
        final Path vault = cryptomator.create(session, new VaultCredentials("test"), vaultVersion);
        session.withRegistry(new DefaultVaultRegistry(new DisabledPasswordStore(), new DisabledPasswordCallback(), cryptomator));
        final TusCapabilities capabilities = new TusCapabilities().withHashAlgorithm(HashAlgorithm.sha1);
        final CryptoUploadFeature service = new CryptoUploadFeature<>(session,
                new OcisUploadFeature(session,
                        new TusWriteFeature(capabilities, session.getClient().getClient()), capabilities),
                new TusWriteFeature(capabilities, session.getClient().getClient()), cryptomator);
        final Local local = new Local(System.getProperty("java.io.tmpdir"), UUID.randomUUID().toString());
        final byte[] content = RandomUtils.nextBytes(5242885);
        IOUtils.write(content, local.getOutputStream(false));
        final TransferStatus writeStatus = new TransferStatus();
        final FileHeader header = cryptomator.getFileHeaderCryptor().create();
        writeStatus.setHeader(cryptomator.getFileHeaderCryptor().encryptHeader(header));
        writeStatus.setLength(content.length);
        final Path test = new Path(vault, new AlphanumericRandomStringService().random(), EnumSet.of(Path.Type.file));
        final BytecountStreamListener counter = new BytecountStreamListener();
        service.upload(test, local, new BandwidthThrottle(BandwidthThrottle.UNLIMITED), counter, writeStatus, new DisabledConnectionCallback());
        assertEquals(content.length, counter.getSent());
        assertTrue(writeStatus.isComplete());
        assertTrue(cryptomator.getFeature(session, Find.class, new DAVFindFeature(session)).find(test));
        assertEquals(content.length, cryptomator.getFeature(session, AttributesFinder.class, new OwncloudAttributesFinderFeature(session)).find(test).getSize());
        final ByteArrayOutputStream buffer = new ByteArrayOutputStream(content.length);
        final TransferStatus readStatus = new TransferStatus().withLength(content.length);
        final InputStream in = new CryptoReadFeature(session, new DAVReadFeature(session), cryptomator).read(test, readStatus, new DisabledConnectionCallback());
        new StreamCopier(readStatus, readStatus).transfer(in, buffer);
        assertArrayEquals(content, buffer.toByteArray());
        cryptomator.getFeature(session, Delete.class, new DAVDeleteFeature(session)).delete(Arrays.asList(test, vault), new DisabledLoginCallback(), new Delete.DisabledCallback());
        local.delete();
    }
}
