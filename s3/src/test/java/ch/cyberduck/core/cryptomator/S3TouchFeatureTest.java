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
import ch.cyberduck.core.DisabledLoginCallback;
import ch.cyberduck.core.DisabledPasswordCallback;
import ch.cyberduck.core.DisabledPasswordStore;
import ch.cyberduck.core.Path;
import ch.cyberduck.core.cryptomator.features.CryptoTouchFeature;
import ch.cyberduck.core.features.AttributesFinder;
import ch.cyberduck.core.features.Delete;
import ch.cyberduck.core.features.Find;
import ch.cyberduck.core.s3.AbstractS3Test;
import ch.cyberduck.core.s3.S3AccessControlListFeature;
import ch.cyberduck.core.s3.S3AttributesFinderFeature;
import ch.cyberduck.core.s3.S3DefaultDeleteFeature;
import ch.cyberduck.core.s3.S3FindFeature;
import ch.cyberduck.core.s3.S3TouchFeature;
import ch.cyberduck.core.s3.S3WriteFeature;
import ch.cyberduck.core.shared.DefaultFindFeature;
import ch.cyberduck.core.shared.DefaultTouchFeature;
import ch.cyberduck.core.transfer.TransferStatus;
import ch.cyberduck.core.vault.DefaultVaultRegistry;
import ch.cyberduck.core.vault.VaultCredentials;
import ch.cyberduck.test.IntegrationTest;

import org.junit.Ignore;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import java.util.Arrays;
import java.util.EnumSet;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assume.assumeTrue;

@Category(IntegrationTest.class)
@RunWith(value = Parameterized.class)
public class S3TouchFeatureTest extends AbstractS3Test {

    @Test
    public void testTouchEncrypted() throws Exception {
        final Path home = new Path("test-eu-central-1-cyberduck", EnumSet.of(Path.Type.directory, Path.Type.volume));
        final Path vault = new Path(home, new AlphanumericRandomStringService().random(), EnumSet.of(Path.Type.directory));
        final CryptoVault cryptomator = new CryptoVault(vault);
        cryptomator.create(session, new VaultCredentials("test"), vaultVersion);
        session.withRegistry(new DefaultVaultRegistry(new DisabledPasswordStore(), new DisabledPasswordCallback(), cryptomator));
        final TransferStatus status = new TransferStatus();
        final S3AccessControlListFeature acl = new S3AccessControlListFeature(session);
        final Path test = new CryptoTouchFeature<>(session, new S3TouchFeature(session, acl), new S3WriteFeature(session, acl), cryptomator).touch(
                new Path(vault, new AlphanumericRandomStringService().random(), EnumSet.of(Path.Type.file)), status);
        assertEquals(0L, test.attributes().getSize());
        assertEquals(0L, status.getResponse().getSize());
        assertTrue(cryptomator.getFeature(session, Find.class, new DefaultFindFeature(session)).find(test));
        assertEquals(test.attributes(), cryptomator.getFeature(session, AttributesFinder.class, new S3AttributesFinderFeature(session, acl)).find(test));
        cryptomator.getFeature(session, Delete.class, new S3DefaultDeleteFeature(session, acl)).delete(Arrays.asList(test, vault), new DisabledLoginCallback(), new Delete.DisabledCallback());
    }

    @Test
    public void testTouchLongFilenameEncrypted() throws Exception {
        assumeTrue(vaultVersion == CryptoVault.VAULT_VERSION_DEPRECATED);
        final Path home = new Path("test-eu-central-1-cyberduck", EnumSet.of(Path.Type.directory, Path.Type.volume));
        final Path vault = new Path(home, new AlphanumericRandomStringService().random(), EnumSet.of(Path.Type.directory));
        final CryptoVault cryptomator = new CryptoVault(vault);
        cryptomator.create(session, new VaultCredentials("test"), vaultVersion);
        session.withRegistry(new DefaultVaultRegistry(new DisabledPasswordStore(), new DisabledPasswordCallback(), cryptomator));
        final TransferStatus status = new TransferStatus();
        final S3AccessControlListFeature acl = new S3AccessControlListFeature(session);
        final Path test = new CryptoTouchFeature<>(session, new S3TouchFeature(session, acl), new S3WriteFeature(session, acl), cryptomator).touch(
                new Path(vault, new AlphanumericRandomStringService(130).random(), EnumSet.of(Path.Type.file)), status);
        assertEquals(0L, test.attributes().getSize());
        assertEquals(0L, status.getResponse().getSize());
        assertTrue(cryptomator.getFeature(session, Find.class, new S3FindFeature(session, acl)).find(test));
        cryptomator.getFeature(session, Delete.class, new S3DefaultDeleteFeature(session, acl)).delete(Arrays.asList(test, vault), new DisabledLoginCallback(), new Delete.DisabledCallback());
    }

    @Test
    @Ignore
    public void testTouchEncryptedDefaultFeature() throws Exception {
        final Path home = new Path("test-eu-central-1-cyberduck", EnumSet.of(Path.Type.directory, Path.Type.volume));
        final Path vault = new Path(home, new AlphanumericRandomStringService().random(), EnumSet.of(Path.Type.directory));
        final CryptoVault cryptomator = new CryptoVault(vault);
        cryptomator.create(session, new VaultCredentials("test"), vaultVersion);
        session.withRegistry(new DefaultVaultRegistry(new DisabledPasswordStore(), new DisabledPasswordCallback(), cryptomator));
        final TransferStatus status = new TransferStatus();
        final S3AccessControlListFeature acl = new S3AccessControlListFeature(session);
        final Path test = new CryptoTouchFeature<>(session, new DefaultTouchFeature<>(new S3WriteFeature(session, acl)), new S3WriteFeature(session, acl), cryptomator).touch(
                new Path(vault, new AlphanumericRandomStringService().random(), EnumSet.of(Path.Type.file)), status);
        assertEquals(0L, test.attributes().getSize());
        assertEquals(0L, status.getResponse().getSize());
        assertTrue(cryptomator.getFeature(session, Find.class, new DefaultFindFeature(session)).find(test));
        cryptomator.getFeature(session, Delete.class, new S3DefaultDeleteFeature(session, acl)).delete(Arrays.asList(test, vault), new DisabledLoginCallback(), new Delete.DisabledCallback());
    }
}
