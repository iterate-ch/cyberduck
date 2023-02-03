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
import ch.cyberduck.core.AttributedList;
import ch.cyberduck.core.DisabledConnectionCallback;
import ch.cyberduck.core.DisabledListProgressListener;
import ch.cyberduck.core.DisabledLoginCallback;
import ch.cyberduck.core.DisabledPasswordCallback;
import ch.cyberduck.core.DisabledPasswordStore;
import ch.cyberduck.core.Path;
import ch.cyberduck.core.PathAttributes;
import ch.cyberduck.core.cryptomator.features.CryptoAttributesFeature;
import ch.cyberduck.core.cryptomator.features.CryptoFindV6Feature;
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
import org.jets3t.service.model.StorageObject;
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
        final Path bucket = new Path("versioning-test-eu-central-1-cyberduck", EnumSet.of(Path.Type.directory, Path.Type.volume));
        final Path vault = new Path(bucket, new AlphanumericRandomStringService().random(), EnumSet.of(Path.Type.directory));
        final CryptoVault cryptomator = new CryptoVault(vault);
        cryptomator.create(session, new VaultCredentials("test"), new DisabledPasswordStore(), vaultVersion);
        session.withRegistry(new DefaultVaultRegistry(new DisabledPasswordStore(), new DisabledPasswordCallback(), cryptomator));
        final AttributesFinder f = new CryptoAttributesFeature(session, new S3AttributesFinderFeature(session, new S3AccessControlListFeature(session)), cryptomator);
        final Path test = new CryptoTouchFeature<>(session, new S3TouchFeature(session, new S3AccessControlListFeature(session)), new S3WriteFeature(session, new S3AccessControlListFeature(session)), cryptomator).touch(
                new Path(vault, new AlphanumericRandomStringService().random(), EnumSet.of(Path.Type.file)), new TransferStatus());
        final PathAttributes initialAttributes = new PathAttributes(test.attributes());
        final String initialVersion = test.attributes().getVersionId();
        final byte[] content = RandomUtils.nextBytes(32769);
        final TransferStatus status = new TransferStatus();
        final Write<StorageObject> writer = new CryptoWriteFeature<>(session, new S3MultipartWriteFeature(session, new S3AccessControlListFeature(session)), cryptomator);
        final FileHeader header = cryptomator.getFileHeaderCryptor().create();
        status.setHeader(cryptomator.getFileHeaderCryptor().encryptHeader(header));
        status.setNonces(new RotatingNonceGenerator(cryptomator.numberOfChunks(content.length)));
        status.setChecksum(writer.checksum(test, status).compute(new ByteArrayInputStream(content), status));
        final StatusOutputStream<StorageObject> out = writer.write(test, status, new DisabledConnectionCallback());
        assertNotNull(out);
        new StreamCopier(status, status).transfer(new ByteArrayInputStream(content), out);
        final PathAttributes updated = new CryptoAttributesFeature(session, new S3AttributesFinderFeature(session, new S3AccessControlListFeature(session)), cryptomator).find(new Path(test).withAttributes(PathAttributes.EMPTY));
        assertNotEquals(initialVersion, updated.getVersionId());
        {
            final AttributedList<Path> versions = new CryptoVersioningFeature(session, new S3VersioningFeature(session, new S3AccessControlListFeature(session)), cryptomator)
                    .list(new Path(test).withAttributes(updated), new DisabledListProgressListener());
            assertFalse(versions.isEmpty());
            assertEquals(1, versions.size());
            assertEquals(new Path(test).withAttributes(initialAttributes), versions.get(0));
            assertTrue(new CryptoFindV6Feature(session, new S3FindFeature(session, new S3AccessControlListFeature(session)), cryptomator).find(versions.get(0)));
            assertEquals(initialVersion, new CryptoAttributesFeature(session, new S3AttributesFinderFeature(session, new S3AccessControlListFeature(session)), cryptomator).find(versions.get(0)).getVersionId());
        }
        new CryptoVersioningFeature(session, new S3VersioningFeature(session, new S3AccessControlListFeature(session)), cryptomator).revert(new Path(test).withAttributes(initialAttributes));
        final PathAttributes reverted = new CryptoAttributesFeature(session, new S3AttributesFinderFeature(session, new S3AccessControlListFeature(session)), cryptomator).find(new Path(test).withAttributes(PathAttributes.EMPTY));
        assertNotEquals(initialVersion, reverted.getVersionId());
        {
            final AttributedList<Path> versions = new CryptoVersioningFeature(session, new S3VersioningFeature(session, new S3AccessControlListFeature(session)), cryptomator)
                    .list(new Path(test).withAttributes(reverted), new DisabledListProgressListener());
            assertEquals(2, versions.size());
            assertEquals(test.attributes().getSize(), reverted.getSize());
            assertEquals(content.length, versions.get(0).attributes().getSize());
        }
        cryptomator.getFeature(session, Delete.class, new S3DefaultDeleteFeature(session)).delete(Arrays.asList(test, vault), new DisabledLoginCallback(), new Delete.DisabledCallback());
    }
}
