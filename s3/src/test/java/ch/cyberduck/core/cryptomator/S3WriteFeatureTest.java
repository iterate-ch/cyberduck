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
import ch.cyberduck.core.DisabledLoginCallback;
import ch.cyberduck.core.DisabledPasswordCallback;
import ch.cyberduck.core.DisabledPasswordStore;
import ch.cyberduck.core.Path;
import ch.cyberduck.core.PathAttributes;
import ch.cyberduck.core.cryptomator.features.CryptoReadFeature;
import ch.cyberduck.core.cryptomator.features.CryptoWriteFeature;
import ch.cyberduck.core.cryptomator.random.RotatingNonceGenerator;
import ch.cyberduck.core.features.AttributesFinder;
import ch.cyberduck.core.features.Delete;
import ch.cyberduck.core.features.Find;
import ch.cyberduck.core.io.StreamCopier;
import ch.cyberduck.core.s3.AbstractS3Test;
import ch.cyberduck.core.s3.S3AccessControlListFeature;
import ch.cyberduck.core.s3.S3AttributesFinderFeature;
import ch.cyberduck.core.s3.S3DefaultDeleteFeature;
import ch.cyberduck.core.s3.S3FindFeature;
import ch.cyberduck.core.s3.S3ReadFeature;
import ch.cyberduck.core.s3.S3WriteFeature;
import ch.cyberduck.core.transfer.TransferStatus;
import ch.cyberduck.core.vault.DefaultVaultRegistry;
import ch.cyberduck.core.vault.VaultCredentials;
import ch.cyberduck.test.IntegrationTest;

import org.apache.commons.lang3.RandomUtils;
import org.cryptomator.cryptolib.api.FileHeader;
import org.jets3t.service.model.StorageObject;
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

import static org.junit.Assert.*;

@Category(IntegrationTest.class)
@RunWith(value = Parameterized.class)
public class S3WriteFeatureTest extends AbstractS3Test {

    @Test
    public void testWrite() throws Exception {
        final TransferStatus status = new TransferStatus();
        final int length = 1048576;
        final byte[] content = RandomUtils.nextBytes(length);
        status.setLength(content.length);
        final Path home = new Path("test-eu-central-1-cyberduck", EnumSet.of(Path.Type.volume, Path.Type.directory));
        final Path vault = new Path(home, new AlphanumericRandomStringService().random(), EnumSet.of(Path.Type.directory));
        final Path test = new Path(vault, new AlphanumericRandomStringService().random(), EnumSet.of(Path.Type.file));
        final CryptoVault cryptomator = new CryptoVault(vault);
        cryptomator.create(session, new VaultCredentials("test"), vaultVersion);
        session.withRegistry(new DefaultVaultRegistry(new DisabledPasswordStore(), new DisabledPasswordCallback(), cryptomator));
        final S3AccessControlListFeature acl = new S3AccessControlListFeature(session);
        final CryptoWriteFeature<StorageObject> writer = new CryptoWriteFeature<>(session, new S3WriteFeature(session, acl), cryptomator);
        final FileHeader header = cryptomator.getFileHeaderCryptor().create();
        status.setHeader(cryptomator.getFileHeaderCryptor().encryptHeader(header));
        status.setNonces(new RotatingNonceGenerator(cryptomator.getNonceSize(), cryptomator.numberOfChunks(content.length)));
        status.setChecksum(writer.checksum(test, status).compute(new ByteArrayInputStream(content), status));
        final OutputStream out = writer.write(test, status, new DisabledConnectionCallback());
        assertNotNull(out);
        new StreamCopier(status, status).transfer(new ByteArrayInputStream(content), out);
        out.close();
        assertEquals(content.length, status.getResponse().getSize());
        assertTrue(cryptomator.getFeature(session, Find.class, new S3FindFeature(session, acl)).find(test));
        final PathAttributes attributes = cryptomator.getFeature(session, AttributesFinder.class, new S3AttributesFinderFeature(session, acl)).find(test);
        assertEquals(content.length, attributes.getSize());
        final ByteArrayOutputStream buffer = new ByteArrayOutputStream(content.length);
        final InputStream in = new CryptoReadFeature(session, new S3ReadFeature(session), cryptomator).read(test, new TransferStatus().withLength(content.length), new DisabledConnectionCallback());
        new StreamCopier(status, status).transfer(in, buffer);
        assertArrayEquals(content, buffer.toByteArray());
        cryptomator.getFeature(session, Delete.class, new S3DefaultDeleteFeature(session, acl)).delete(Arrays.asList(test, vault), new DisabledLoginCallback(), new Delete.DisabledCallback());
    }
}
