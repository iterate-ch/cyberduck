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
import ch.cyberduck.core.b2.AbstractB2Test;
import ch.cyberduck.core.b2.B2AttributesFinderFeature;
import ch.cyberduck.core.b2.B2DeleteFeature;
import ch.cyberduck.core.b2.B2FindFeature;
import ch.cyberduck.core.b2.B2TouchFeature;
import ch.cyberduck.core.b2.B2VersionIdProvider;
import ch.cyberduck.core.b2.B2WriteFeature;
import ch.cyberduck.core.cryptomator.features.CryptoAttributesFeature;
import ch.cyberduck.core.cryptomator.features.CryptoFindV6Feature;
import ch.cyberduck.core.cryptomator.features.CryptoTouchFeature;
import ch.cyberduck.core.features.Delete;
import ch.cyberduck.core.features.Find;
import ch.cyberduck.core.shared.DefaultFindFeature;
import ch.cyberduck.core.shared.DefaultTouchFeature;
import ch.cyberduck.core.transfer.TransferStatus;
import ch.cyberduck.core.vault.DefaultVaultRegistry;
import ch.cyberduck.core.vault.VaultCredentials;
import ch.cyberduck.test.IntegrationTest;

import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import java.util.Arrays;
import java.util.EnumSet;
import java.util.UUID;

import static org.junit.Assert.*;
import static org.junit.Assume.assumeTrue;

@Category(IntegrationTest.class)
@RunWith(value = Parameterized.class)
public class B2TouchFeatureTest extends AbstractB2Test {

    @Test
    public void testTouchEncrypted() throws Exception {
        final Path home = new Path("/test-cyberduck", EnumSet.of(Path.Type.directory, Path.Type.volume));
        final CryptoVault cryptomator = new CryptoVault(
                new Path(home, new AlphanumericRandomStringService().random(), EnumSet.of(Path.Type.directory)));
        final Path vault = cryptomator.create(session, new VaultCredentials("test"), new DisabledPasswordStore(), vaultVersion);
        session.withRegistry(new DefaultVaultRegistry(new DisabledPasswordStore(), new DisabledPasswordCallback(), cryptomator));
        final B2VersionIdProvider fileid = new B2VersionIdProvider(session);
        final TransferStatus status = new TransferStatus();
        final Path test = new CryptoTouchFeature<>(session, new B2TouchFeature(session, fileid), new B2WriteFeature(session, fileid), cryptomator).touch(
                new Path(vault, UUID.randomUUID().toString(), EnumSet.of(Path.Type.file)), status);
        assertEquals(0L, test.attributes().getSize());
        assertEquals(0L, status.getResponse().getSize());
        assertNotNull(test.attributes().getVersionId());
        assertTrue(new CryptoFindV6Feature(session, new B2FindFeature(session, fileid), cryptomator).find(test));
        assertEquals(test.attributes(), new CryptoAttributesFeature(session, new B2AttributesFinderFeature(session, fileid), cryptomator).find(test));
        cryptomator.getFeature(session, Delete.class, new B2DeleteFeature(session, fileid)).delete(Arrays.asList(test, vault), new DisabledLoginCallback(), new Delete.DisabledCallback());
    }

    @Test
    public void testTouchLongFilenameEncrypted() throws Exception {
        assumeTrue(vaultVersion == CryptoVault.VAULT_VERSION_DEPRECATED);
        final Path home = new Path("/test-cyberduck", EnumSet.of(Path.Type.directory, Path.Type.volume));
        final Path vault = new Path(home, new AlphanumericRandomStringService().random(), EnumSet.of(Path.Type.directory));
        final CryptoVault cryptomator = new CryptoVault(vault);
        cryptomator.create(session, new VaultCredentials("test"), new DisabledPasswordStore(), vaultVersion);
        session.withRegistry(new DefaultVaultRegistry(new DisabledPasswordStore(), new DisabledPasswordCallback(), cryptomator));
        final B2VersionIdProvider fileid = new B2VersionIdProvider(session);
        final TransferStatus status = new TransferStatus();
        final Path test = new CryptoTouchFeature<>(session, new B2TouchFeature(session, fileid), new B2WriteFeature(session, fileid), cryptomator).touch(
                new Path(vault, new AlphanumericRandomStringService(130).random(), EnumSet.of(Path.Type.file)), status);
        assertEquals(0L, test.attributes().getSize());
        assertEquals(0L, status.getResponse().getSize());
        assertTrue(new CryptoFindV6Feature(session, new B2FindFeature(session, fileid), cryptomator).find(test));
        cryptomator.getFeature(session, Delete.class, new B2DeleteFeature(session, fileid)).delete(Arrays.asList(test, vault), new DisabledLoginCallback(), new Delete.DisabledCallback());
    }

    @Test
    public void testTouchEncryptedDefaultFeature() throws Exception {
        final Path home = new Path("/test-cyberduck", EnumSet.of(Path.Type.directory, Path.Type.volume));
        final CryptoVault cryptomator = new CryptoVault(
                new Path(home, new AlphanumericRandomStringService().random(), EnumSet.of(Path.Type.directory)));
        final Path vault = cryptomator.create(session, new VaultCredentials("test"), new DisabledPasswordStore(), vaultVersion);
        session.withRegistry(new DefaultVaultRegistry(new DisabledPasswordStore(), new DisabledPasswordCallback(), cryptomator));
        final B2VersionIdProvider fileid = new B2VersionIdProvider(session);
        final TransferStatus status = new TransferStatus();
        final Path test = new CryptoTouchFeature<>(session, new DefaultTouchFeature<>(new B2WriteFeature(session, fileid)), new B2WriteFeature(session, fileid), cryptomator).touch(
                new Path(vault, UUID.randomUUID().toString(), EnumSet.of(Path.Type.file)), status);
        assertEquals(0L, test.attributes().getSize());
        assertEquals(0L, status.getResponse().getSize());
        assertNotNull(test.attributes().getVersionId());
        assertTrue(cryptomator.getFeature(session, Find.class, new DefaultFindFeature(session)).find(test));
        cryptomator.getFeature(session, Delete.class, new B2DeleteFeature(session, fileid)).delete(Arrays.asList(test, vault), new DisabledLoginCallback(), new Delete.DisabledCallback());
    }
}
