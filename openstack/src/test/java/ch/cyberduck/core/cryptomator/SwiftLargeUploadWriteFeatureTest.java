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
import ch.cyberduck.core.DisabledLoginCallback;
import ch.cyberduck.core.DisabledPasswordCallback;
import ch.cyberduck.core.DisabledPasswordStore;
import ch.cyberduck.core.Path;
import ch.cyberduck.core.cryptomator.features.CryptoReadFeature;
import ch.cyberduck.core.cryptomator.features.CryptoWriteFeature;
import ch.cyberduck.core.cryptomator.random.RandomNonceGenerator;
import ch.cyberduck.core.features.Delete;
import ch.cyberduck.core.features.Find;
import ch.cyberduck.core.io.StreamCopier;
import ch.cyberduck.core.openstack.AbstractSwiftTest;
import ch.cyberduck.core.openstack.SwiftDeleteFeature;
import ch.cyberduck.core.openstack.SwiftFindFeature;
import ch.cyberduck.core.openstack.SwiftLargeUploadWriteFeature;
import ch.cyberduck.core.openstack.SwiftReadFeature;
import ch.cyberduck.core.openstack.SwiftRegionService;
import ch.cyberduck.core.openstack.SwiftSegmentService;
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
import java.io.OutputStream;
import java.util.Arrays;
import java.util.EnumSet;

import static org.junit.Assert.*;

@Category(IntegrationTest.class)
@RunWith(value = Parameterized.class)
public class SwiftLargeUploadWriteFeatureTest extends AbstractSwiftTest {

    @Test
    public void testWrite() throws Exception {
        final Path home = new Path("test.cyberduck.ch", EnumSet.of(Path.Type.volume, Path.Type.directory));
        home.attributes().setRegion("IAD");
        final Path vault = new Path(home, new AlphanumericRandomStringService().random(), EnumSet.of(Path.Type.directory));
        final Path test = new Path(vault, new AlphanumericRandomStringService().random(), EnumSet.of(Path.Type.file));
        final CryptoVault cryptomator = new CryptoVault(vault);
        cryptomator.create(session, new VaultCredentials("test"), vaultVersion);
        session.withRegistry(new DefaultVaultRegistry(new DisabledPasswordStore(), new DisabledPasswordCallback(), cryptomator));
        final SwiftRegionService regionService = new SwiftRegionService(session);
        final CryptoWriteFeature feature = new CryptoWriteFeature<>(session, new SwiftLargeUploadWriteFeature(session, regionService,
                new SwiftSegmentService(session, ".segments-test/")), cryptomator);
        final TransferStatus writeStatus = new TransferStatus();
        final FileHeader header = cryptomator.getFileHeaderCryptor().create();
        writeStatus.setHeader(cryptomator.getFileHeaderCryptor().encryptHeader(header));
        writeStatus.setNonces(new RandomNonceGenerator(cryptomator.getNonceSize()));
        writeStatus.setLength(-1L);
        final OutputStream out = feature.write(test, writeStatus, new DisabledConnectionCallback());
        final byte[] content = RandomUtils.nextBytes(6 * 1024 * 1024);
        final ByteArrayInputStream in = new ByteArrayInputStream(content);
        final TransferStatus progress = new TransferStatus();
        final BytecountStreamListener listener = new BytecountStreamListener();
        new StreamCopier(new TransferStatus(), progress).withListener(listener).transfer(in, out);
        assertEquals(content.length, listener.getSent());
        assertTrue(cryptomator.getFeature(session, Find.class, new SwiftFindFeature(session)).find(test));
        final byte[] compare = new byte[content.length];
        final InputStream stream = new CryptoReadFeature(session, new SwiftReadFeature(session, regionService), cryptomator).read(test, new TransferStatus().withLength(content.length), new DisabledConnectionCallback());
        IOUtils.readFully(stream, compare);
        stream.close();
        assertArrayEquals(content, compare);
        cryptomator.getFeature(session, Delete.class, new SwiftDeleteFeature(session)).delete(Arrays.asList(test, vault), new DisabledLoginCallback(), new Delete.DisabledCallback());
    }
}
