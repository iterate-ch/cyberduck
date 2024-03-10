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

import ch.cyberduck.core.AbstractPath;
import ch.cyberduck.core.AlphanumericRandomStringService;
import ch.cyberduck.core.BytecountStreamListener;
import ch.cyberduck.core.DisabledConnectionCallback;
import ch.cyberduck.core.DisabledLoginCallback;
import ch.cyberduck.core.DisabledPasswordCallback;
import ch.cyberduck.core.DisabledPasswordStore;
import ch.cyberduck.core.Local;
import ch.cyberduck.core.Path;
import ch.cyberduck.core.box.AbstractBoxTest;
import ch.cyberduck.core.box.BoxAttributesFinderFeature;
import ch.cyberduck.core.box.BoxDeleteFeature;
import ch.cyberduck.core.box.BoxDirectoryFeature;
import ch.cyberduck.core.box.BoxFileidProvider;
import ch.cyberduck.core.box.BoxFindFeature;
import ch.cyberduck.core.box.BoxReadFeature;
import ch.cyberduck.core.box.BoxThresholdUploadService;
import ch.cyberduck.core.box.BoxWriteFeature;
import ch.cyberduck.core.cryptomator.features.CryptoBulkFeature;
import ch.cyberduck.core.cryptomator.features.CryptoReadFeature;
import ch.cyberduck.core.cryptomator.features.CryptoUploadFeature;
import ch.cyberduck.core.features.AttributesFinder;
import ch.cyberduck.core.features.Delete;
import ch.cyberduck.core.features.Find;
import ch.cyberduck.core.io.BandwidthThrottle;
import ch.cyberduck.core.io.StreamCopier;
import ch.cyberduck.core.shared.DisabledBulkFeature;
import ch.cyberduck.core.transfer.Transfer;
import ch.cyberduck.core.transfer.TransferItem;
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
import java.util.Collections;
import java.util.EnumSet;
import java.util.Map;
import java.util.UUID;

import static org.junit.Assert.*;

@Category(IntegrationTest.class)
@RunWith(value = Parameterized.class)
public class BoxThresholdUploadServiceTest extends AbstractBoxTest {

    @Test
    public void testUploadVaultWithBulkFeature() throws Exception {
        final BoxFileidProvider fileid = new BoxFileidProvider(session);
        final Path container = new BoxDirectoryFeature(session, fileid).mkdir(new Path(new AlphanumericRandomStringService().random(), EnumSet.of(AbstractPath.Type.directory)), new TransferStatus().withLength(0L));
        final Path vault = new Path(container, new AlphanumericRandomStringService().random(), EnumSet.of(Path.Type.directory));
        final Path test = new Path(vault, new AlphanumericRandomStringService().random(), EnumSet.of(Path.Type.file));
        final CryptoVault cryptomator = new CryptoVault(vault);
        cryptomator.create(session, new VaultCredentials("test"), vaultVersion);
        final DefaultVaultRegistry registry = new DefaultVaultRegistry(new DisabledPasswordStore(), new DisabledPasswordCallback(), cryptomator);
        session.withRegistry(registry);
        final Local local = new Local(System.getProperty("java.io.tmpdir"), UUID.randomUUID().toString());
        final byte[] content = RandomUtils.nextBytes(50240000);
        IOUtils.write(content, local.getOutputStream(false));
        final TransferStatus writeStatus = new TransferStatus();
        final FileHeader header = cryptomator.getFileHeaderCryptor().create();
        writeStatus.setHeader(cryptomator.getFileHeaderCryptor().encryptHeader(header));
        writeStatus.setLength(content.length);
        final CryptoBulkFeature<Map<TransferItem, TransferStatus>> bulk = new CryptoBulkFeature<>(session, new DisabledBulkFeature(), new BoxDeleteFeature(session, fileid), cryptomator);
        bulk.pre(Transfer.Type.upload, Collections.singletonMap(new TransferItem(test), writeStatus), new DisabledConnectionCallback());
        final BytecountStreamListener count = new BytecountStreamListener();
        final CryptoUploadFeature feature = new CryptoUploadFeature<>(session,
                new BoxThresholdUploadService(session, fileid, registry),
                new BoxWriteFeature(session, fileid), cryptomator);
        feature.upload(test, local, new BandwidthThrottle(BandwidthThrottle.UNLIMITED), count, writeStatus, new DisabledConnectionCallback());
        assertEquals(content.length, count.getSent());
        assertTrue(writeStatus.isComplete());
        assertTrue(cryptomator.getFeature(session, Find.class, new BoxFindFeature(session, fileid)).find(test));
        assertEquals(content.length, cryptomator.getFeature(session, AttributesFinder.class, new BoxAttributesFinderFeature(session, fileid)).find(test).getSize());
        final ByteArrayOutputStream buffer = new ByteArrayOutputStream(content.length);
        final TransferStatus readStatus = new TransferStatus().withLength(content.length);
        final InputStream in = new CryptoReadFeature(session, new BoxReadFeature(session, fileid), cryptomator).read(test, readStatus, new DisabledConnectionCallback());
        new StreamCopier(readStatus, readStatus).transfer(in, buffer);
        assertArrayEquals(content, buffer.toByteArray());
        cryptomator.getFeature(session, Delete.class, new BoxDeleteFeature(session, fileid)).delete(Arrays.asList(test, vault), new DisabledLoginCallback(), new Delete.DisabledCallback());
        local.delete();
    }
}
