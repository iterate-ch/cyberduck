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
import ch.cyberduck.core.BytecountStreamListener;
import ch.cyberduck.core.DisabledConnectionCallback;
import ch.cyberduck.core.DisabledListProgressListener;
import ch.cyberduck.core.DisabledLoginCallback;
import ch.cyberduck.core.DisabledPasswordCallback;
import ch.cyberduck.core.DisabledPasswordStore;
import ch.cyberduck.core.Local;
import ch.cyberduck.core.Path;
import ch.cyberduck.core.cryptomator.features.CryptoFindFeature;
import ch.cyberduck.core.cryptomator.features.CryptoListService;
import ch.cyberduck.core.cryptomator.features.CryptoReadFeature;
import ch.cyberduck.core.cryptomator.features.CryptoUploadFeature;
import ch.cyberduck.core.features.Delete;
import ch.cyberduck.core.io.BandwidthThrottle;
import ch.cyberduck.core.io.StreamCopier;
import ch.cyberduck.core.openstack.AbstractSwiftTest;
import ch.cyberduck.core.openstack.SwiftDeleteFeature;
import ch.cyberduck.core.openstack.SwiftFindFeature;
import ch.cyberduck.core.openstack.SwiftLargeObjectUploadFeature;
import ch.cyberduck.core.openstack.SwiftListService;
import ch.cyberduck.core.openstack.SwiftReadFeature;
import ch.cyberduck.core.openstack.SwiftRegionService;
import ch.cyberduck.core.openstack.SwiftWriteFeature;
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
import java.util.Arrays;
import java.util.EnumSet;
import java.util.UUID;

import static org.junit.Assert.*;

@Category(IntegrationTest.class)
@RunWith(value = Parameterized.class)
public class SwiftLargeObjectUploadFeatureTest extends AbstractSwiftTest {

    @Test
    public void testLargeObjectUpload() throws Exception {
        // 5L * 1024L * 1024L
        final Path home = new Path("test.cyberduck.ch", EnumSet.of(Path.Type.volume, Path.Type.directory));
        home.attributes().setRegion("IAD");
        final Path vault = new Path(home, new AlphanumericRandomStringService().random(), EnumSet.of(Path.Type.directory));
        final Path test = new Path(vault, new AlphanumericRandomStringService().random(), EnumSet.of(Path.Type.file));
        final CryptoVault cryptomator = new CryptoVault(vault);
        cryptomator.create(session, null, new VaultCredentials("test"), new DisabledPasswordStore(), vaultVersion);
        session.withRegistry(new DefaultVaultRegistry(new DisabledPasswordStore(), new DisabledPasswordCallback(), cryptomator));
        final SwiftRegionService regionService = new SwiftRegionService(session);
        final CryptoUploadFeature m = new CryptoUploadFeature<>(session,
            new SwiftLargeObjectUploadFeature(session, regionService, new SwiftWriteFeature(session,
                regionService), 5242880L, 5), new SwiftWriteFeature(session,
            regionService), cryptomator);
        final Local local = new Local(System.getProperty("java.io.tmpdir"), UUID.randomUUID().toString());
        final int length = 5242885;
        final byte[] content = RandomUtils.nextBytes(length);
        IOUtils.write(content, local.getOutputStream(false));
        final TransferStatus writeStatus = new TransferStatus();
        final FileHeader header = cryptomator.getFileHeaderCryptor().create();
        writeStatus.setHeader(cryptomator.getFileHeaderCryptor().encryptHeader(header));
        writeStatus.setLength(content.length);
        final BytecountStreamListener count = new BytecountStreamListener();
        m.upload(test, local, new BandwidthThrottle(BandwidthThrottle.UNLIMITED), count, writeStatus, null);
        assertEquals(content.length, count.getSent());
        assertTrue(writeStatus.isComplete());
        assertTrue(new CryptoFindFeature(session, new SwiftFindFeature(session), cryptomator).find(test));
        assertEquals(content.length, new CryptoListService(session, new SwiftListService(session, regionService), new SwiftFindFeature(session), cryptomator).list(test.getParent(), new DisabledListProgressListener()).get(test).attributes().getSize());
        final ByteArrayOutputStream buffer = new ByteArrayOutputStream(content.length);
        final TransferStatus readStatus = new TransferStatus().withLength(content.length);
        final InputStream in = new CryptoReadFeature(session, new SwiftReadFeature(session, regionService), cryptomator).read(test, readStatus, new DisabledConnectionCallback());
        new StreamCopier(readStatus, readStatus).transfer(in, buffer);
        assertArrayEquals(content, buffer.toByteArray());
        cryptomator.getFeature(session, Delete.class, new SwiftDeleteFeature(session)).delete(Arrays.asList(test, vault), new DisabledLoginCallback(), new Delete.DisabledCallback());
        local.delete();
        session.close();
    }
}
