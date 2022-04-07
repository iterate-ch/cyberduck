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
import ch.cyberduck.core.PathAttributes;
import ch.cyberduck.core.cryptomator.features.CryptoAttributesFeature;
import ch.cyberduck.core.cryptomator.features.CryptoFindV6Feature;
import ch.cyberduck.core.features.Delete;
import ch.cyberduck.core.features.Directory;
import ch.cyberduck.core.features.Find;
import ch.cyberduck.core.onedrive.AbstractOneDriveTest;
import ch.cyberduck.core.onedrive.OneDriveHomeFinderService;
import ch.cyberduck.core.onedrive.features.GraphAttributesFinderFeature;
import ch.cyberduck.core.onedrive.features.GraphDeleteFeature;
import ch.cyberduck.core.onedrive.features.GraphDirectoryFeature;
import ch.cyberduck.core.onedrive.features.GraphFindFeature;
import ch.cyberduck.core.shared.DefaultAttributesFinderFeature;
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
import java.util.EnumSet;

import static org.junit.Assert.*;
import static org.junit.Assume.assumeTrue;

@Category(IntegrationTest.class)
@RunWith(value = Parameterized.class)
public class GraphDirectoryFeatureTest extends AbstractOneDriveTest {

    @Test
    public void testMakeDirectoryEncrypted() throws Exception {
        final Path home = new OneDriveHomeFinderService().find();
        final Path vault = new Path(home, new AlphanumericRandomStringService().random(), EnumSet.of(Path.Type.directory));
        final CryptoVault cryptomator = new CryptoVault(vault);
        cryptomator.create(session, new VaultCredentials("test"), new DisabledPasswordStore(), vaultVersion);
        session.withRegistry(new DefaultVaultRegistry(new DisabledPasswordStore(), new DisabledPasswordCallback(), cryptomator));
        final Path test = cryptomator.getFeature(session, Directory.class, new GraphDirectoryFeature(session, fileid)).mkdir(
                new Path(vault, new AlphanumericRandomStringService().random(), EnumSet.of(Path.Type.directory)), new TransferStatus());
        assertNotNull(test.attributes().getVault());
        final String id = test.attributes().getFileId();
        final long timestamp = test.attributes().getModificationDate();
        assertNotEquals(-1L, timestamp, 0L);
        // Assert both filename and file id matches
        assertTrue(new CryptoFindV6Feature(session, new GraphFindFeature(session, fileid), cryptomator).find(test));
        assertTrue(cryptomator.getFeature(session, Find.class, new DefaultFindFeature(session)).find(test));
        assertEquals(id, new CryptoAttributesFeature(session, new GraphAttributesFinderFeature(session, fileid), cryptomator).find(test).getFileId());
        assertEquals(id, new CryptoAttributesFeature(session, new DefaultAttributesFinderFeature(session), cryptomator).find(test).getFileId());
        cryptomator.getFeature(session, Delete.class, new GraphDeleteFeature(session, fileid)).delete(Arrays.asList(test, vault), new DisabledLoginCallback(), new Delete.DisabledCallback());
    }

    @Test
    public void testMakeDirectoryLongFilenameEncrypted() throws Exception {
        assumeTrue(vaultVersion == CryptoVault.VAULT_VERSION_DEPRECATED);
        final Path home = new OneDriveHomeFinderService().find();
        final Path vault = new Path(home, new AlphanumericRandomStringService().random(), EnumSet.of(Path.Type.directory));
        final CryptoVault cryptomator = new CryptoVault(vault);
        cryptomator.create(session, new VaultCredentials("test"), new DisabledPasswordStore(), vaultVersion);
        session.withRegistry(new DefaultVaultRegistry(new DisabledPasswordStore(), new DisabledPasswordCallback(), cryptomator));
        final Path test = cryptomator.getFeature(session, Directory.class, new GraphDirectoryFeature(session, fileid)).mkdir(
                new Path(vault, new AlphanumericRandomStringService(130).random(), EnumSet.of(Path.Type.directory)), new TransferStatus());
        assertNotNull(test.attributes().getVault());
        final String id = test.attributes().getFileId();
        assertTrue(cryptomator.getFeature(session, Find.class, new DefaultFindFeature(session)).find(test));
        final PathAttributes attributes = new CryptoAttributesFeature(session, new GraphAttributesFinderFeature(session, fileid), cryptomator).find(test);
        assertEquals(id, attributes.getFileId());
        cryptomator.getFeature(session, Delete.class, new GraphDeleteFeature(session, fileid)).delete(Arrays.asList(test, vault), new DisabledLoginCallback(), new Delete.DisabledCallback());
    }
}
