package ch.cyberduck.core.cryptomator;

/*
 * Copyright (c) 2002-2013 David Kocher. All rights reserved.
 * http://cyberduck.ch/
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * Bug fixes, suggestions and comments should be sent to feedback@cyberduck.ch
 */

import ch.cyberduck.core.AlphanumericRandomStringService;
import ch.cyberduck.core.DisabledConnectionCallback;
import ch.cyberduck.core.DisabledLoginCallback;
import ch.cyberduck.core.DisabledPasswordCallback;
import ch.cyberduck.core.DisabledPasswordStore;
import ch.cyberduck.core.Path;
import ch.cyberduck.core.PathAttributes;
import ch.cyberduck.core.cryptomator.features.CryptoAttributesFeature;
import ch.cyberduck.core.cryptomator.features.CryptoFindFeature;
import ch.cyberduck.core.cryptomator.features.CryptoTouchFeature;
import ch.cyberduck.core.cryptomator.features.CryptoVersioningFeature;
import ch.cyberduck.core.cryptomator.features.CryptoWriteFeature;
import ch.cyberduck.core.cryptomator.random.RotatingNonceGenerator;
import ch.cyberduck.core.features.AttributesFinder;
import ch.cyberduck.core.features.Delete;
import ch.cyberduck.core.features.Write;
import ch.cyberduck.core.io.StatusOutputStream;
import ch.cyberduck.core.io.StreamCopier;
import ch.cyberduck.core.s3.AbstractS3Test;
import ch.cyberduck.core.s3.S3AccessControlListFeature;
import ch.cyberduck.core.s3.S3AttributesFinderFeature;
import ch.cyberduck.core.s3.S3DefaultDeleteFeature;
import ch.cyberduck.core.s3.S3DirectoryFeature;
import ch.cyberduck.core.s3.S3FindFeature;
import ch.cyberduck.core.s3.S3MultipartWriteFeature;
import ch.cyberduck.core.s3.S3TouchFeature;
import ch.cyberduck.core.s3.S3VersioningFeature;
import ch.cyberduck.core.s3.S3WriteFeature;
import ch.cyberduck.core.transfer.TransferStatus;
import ch.cyberduck.core.vault.DefaultVaultRegistry;
import ch.cyberduck.core.vault.VaultCredentials;
import ch.cyberduck.test.IntegrationTest;

import org.apache.commons.lang3.RandomUtils;
import org.cryptomator.cryptolib.api.FileHeader;
import org.jets3t.service.model.MultipartUpload;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import java.io.ByteArrayInputStream;
import java.util.Arrays;
import java.util.EnumSet;

import static org.junit.Assert.*;

@Category(IntegrationTest.class)
@RunWith(value = Parameterized.class)
public class S3VersioningFeatureTest extends AbstractS3Test {

    @Test
    public void testRevert() throws Exception {
        final Path bucket = new Path("versioning-test-us-east-1-cyberduck", EnumSet.of(Path.Type.directory, Path.Type.volume));
        final Path vault = new Path(bucket, new AlphanumericRandomStringService().random(), EnumSet.of(Path.Type.directory));
        final CryptoVault cryptomator = new CryptoVault(vault);
        cryptomator.create(session, null, new VaultCredentials("test"), new DisabledPasswordStore(), vaultVersion);
        session.withRegistry(new DefaultVaultRegistry(new DisabledPasswordStore(), new DisabledPasswordCallback(), cryptomator));
        final Path directory = new S3DirectoryFeature(session, new S3WriteFeature(session)).mkdir(new Path(
                bucket, new AlphanumericRandomStringService().random(), EnumSet.of(Path.Type.directory)), new TransferStatus());
        final AttributesFinder f = new CryptoAttributesFeature(session, new S3AttributesFinderFeature(session), cryptomator);
        final Path test = new CryptoTouchFeature<>(session, new S3TouchFeature(session), new S3WriteFeature(session), cryptomator).touch(
                new Path(vault, new AlphanumericRandomStringService().random(), EnumSet.of(Path.Type.file)), new TransferStatus());
        final PathAttributes initialAttributes = new PathAttributes(test.attributes());
        final String initialVersion = test.attributes().getVersionId();
        final byte[] content = RandomUtils.nextBytes(32769);
        final TransferStatus status = new TransferStatus();
        final Write<MultipartUpload> writer = new CryptoWriteFeature<>(session, new S3MultipartWriteFeature(session), cryptomator);
        final FileHeader header = cryptomator.getFileHeaderCryptor().create();
        status.setHeader(cryptomator.getFileHeaderCryptor().encryptHeader(header));
        status.setNonces(new RotatingNonceGenerator(cryptomator.numberOfChunks(content.length)));
        status.setChecksum(writer.checksum(test, status).compute(new ByteArrayInputStream(content), status));
        final StatusOutputStream<MultipartUpload> out = writer.write(test, status, new DisabledConnectionCallback());
        assertNotNull(out);
        new StreamCopier(status, status).transfer(new ByteArrayInputStream(content), out);
        final PathAttributes updated = new CryptoAttributesFeature(session, new S3AttributesFinderFeature(session, true), cryptomator).find(new Path(test).withAttributes(PathAttributes.EMPTY));
        assertNotEquals(initialVersion, updated.getVersionId());
        assertFalse(updated.getVersions().isEmpty());
        assertEquals(1, updated.getVersions().size());
        assertEquals(new Path(test).withAttributes(initialAttributes), updated.getVersions().get(0));
        assertTrue(new CryptoFindFeature(session, new S3FindFeature(session), cryptomator).find(updated.getVersions().get(0)));
        assertEquals(initialVersion, new CryptoAttributesFeature(session, new S3AttributesFinderFeature(session), cryptomator).find(updated.getVersions().get(0)).getVersionId());
        new CryptoVersioningFeature(session, new S3VersioningFeature(session, new S3AccessControlListFeature(session)), cryptomator).revert(new Path(test).withAttributes(initialAttributes));
        final PathAttributes reverted = new CryptoAttributesFeature(session, new S3AttributesFinderFeature(session, true), cryptomator).find(new Path(test).withAttributes(PathAttributes.EMPTY));
        assertNotEquals(initialVersion, reverted.getVersionId());
        assertEquals(2, reverted.getVersions().size());
        assertEquals(test.attributes().getSize(), reverted.getSize());
        assertEquals(content.length, reverted.getVersions().get(0).attributes().getSize());
        cryptomator.getFeature(session, Delete.class, new S3DefaultDeleteFeature(session)).delete(Arrays.asList(directory, test), new DisabledLoginCallback(), new Delete.DisabledCallback());
    }
}
