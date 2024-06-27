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
import ch.cyberduck.core.DisabledListProgressListener;
import ch.cyberduck.core.DisabledLoginCallback;
import ch.cyberduck.core.DisabledPasswordCallback;
import ch.cyberduck.core.DisabledPasswordStore;
import ch.cyberduck.core.Path;
import ch.cyberduck.core.PathAttributes;
import ch.cyberduck.core.cryptomator.features.CryptoListService;
import ch.cyberduck.core.features.AttributesFinder;
import ch.cyberduck.core.features.Delete;
import ch.cyberduck.core.features.Directory;
import ch.cyberduck.core.features.Find;
import ch.cyberduck.core.s3.AbstractS3Test;
import ch.cyberduck.core.s3.S3AccessControlListFeature;
import ch.cyberduck.core.s3.S3AttributesFinderFeature;
import ch.cyberduck.core.s3.S3DefaultDeleteFeature;
import ch.cyberduck.core.s3.S3DirectoryFeature;
import ch.cyberduck.core.s3.S3FindFeature;
import ch.cyberduck.core.s3.S3ListService;
import ch.cyberduck.core.s3.S3WriteFeature;
import ch.cyberduck.core.shared.DefaultFindFeature;
import ch.cyberduck.core.transfer.TransferStatus;
import ch.cyberduck.core.vault.DefaultVaultRegistry;
import ch.cyberduck.core.vault.VaultCredentials;
import ch.cyberduck.test.IntegrationTest;

import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import java.util.Arrays;
import java.util.Collections;
import java.util.EnumSet;
import java.util.stream.Collectors;

import static org.junit.Assert.*;
import static org.junit.Assume.assumeTrue;

@Category(IntegrationTest.class)
@RunWith(value = Parameterized.class)
public class S3DirectoryFeatureTest extends AbstractS3Test {

    @Test
    public void testMakeDirectoryEncrypted() throws Exception {
        final Path home = new Path("versioning-test-eu-central-1-cyberduck", EnumSet.of(Path.Type.directory, Path.Type.volume));
        final Path vault = new Path(home, new AlphanumericRandomStringService().random(), EnumSet.of(Path.Type.directory));
        final CryptoVault cryptomator = new CryptoVault(vault);
        cryptomator.create(session, new VaultCredentials("test"), vaultVersion);
        session.withRegistry(new DefaultVaultRegistry(new DisabledPasswordStore(), new DisabledPasswordCallback(), cryptomator));
        final S3AccessControlListFeature acl = new S3AccessControlListFeature(session);
        final Path test = cryptomator.getFeature(session, Directory.class, new S3DirectoryFeature(session, new S3WriteFeature(session, acl), acl)).mkdir(new Path(vault, new AlphanumericRandomStringService().random(), EnumSet.of(Path.Type.directory)), new TransferStatus());
        assertTrue(test.getType().contains(Path.Type.placeholder));
        assertTrue(cryptomator.getFeature(session, Find.class, new S3FindFeature(session, acl)).find(test));
        assertTrue(cryptomator.getFeature(session, Find.class, new DefaultFindFeature(session)).find(test));
        final PathAttributes attributes = cryptomator.getFeature(session, AttributesFinder.class, new S3AttributesFinderFeature(session, acl)).find(test);
        assertNotNull(attributes.getVersionId());
        cryptomator.getFeature(session, Delete.class, new S3DefaultDeleteFeature(session)).delete(Collections.singletonList(test), new DisabledLoginCallback(), new Delete.DisabledCallback());
        assertTrue(new CryptoListService(session, new S3ListService(session, acl), cryptomator).list(vault, new DisabledListProgressListener())
                .toStream().filter(f -> !f.attributes().isDuplicate()).collect(Collectors.toList()).isEmpty());
        cryptomator.getFeature(session, Delete.class, new S3DefaultDeleteFeature(session)).delete(Arrays.asList(test, vault), new DisabledLoginCallback(), new Delete.DisabledCallback());
    }

    @Test
    public void testMakeDirectoryLongFilenameEncrypted() throws Exception {
        assumeTrue(vaultVersion == CryptoVault.VAULT_VERSION_DEPRECATED);
        final Path home = new Path("test-eu-central-1-cyberduck", EnumSet.of(Path.Type.directory, Path.Type.volume));
        final Path vault = new Path(home, new AlphanumericRandomStringService().random(), EnumSet.of(Path.Type.directory));
        final CryptoVault cryptomator = new CryptoVault(vault);
        cryptomator.create(session, new VaultCredentials("test"), vaultVersion);
        session.withRegistry(new DefaultVaultRegistry(new DisabledPasswordStore(), new DisabledPasswordCallback(), cryptomator));
        final S3AccessControlListFeature acl = new S3AccessControlListFeature(session);
        final Path test = cryptomator.getFeature(session, Directory.class, new S3DirectoryFeature(session, new S3WriteFeature(session, acl), acl)).mkdir(new Path(vault, new AlphanumericRandomStringService(130).random(), EnumSet.of(Path.Type.directory)), new TransferStatus());
        final String versionId = test.attributes().getVersionId();
        assertTrue(cryptomator.getFeature(session, Find.class, new DefaultFindFeature(session)).find(test));
        final PathAttributes attributes = cryptomator.getFeature(session, AttributesFinder.class, new S3AttributesFinderFeature(session, acl)).find(test);
        assertEquals(versionId, attributes.getVersionId());
        cryptomator.getFeature(session, Delete.class, new S3DefaultDeleteFeature(session)).delete(Arrays.asList(test, vault), new DisabledLoginCallback(), new Delete.DisabledCallback());
    }
}
