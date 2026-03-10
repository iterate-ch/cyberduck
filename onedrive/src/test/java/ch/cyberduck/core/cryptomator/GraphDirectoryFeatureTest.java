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
import ch.cyberduck.core.DisabledPasswordCallback;
import ch.cyberduck.core.LoginCallback;
import ch.cyberduck.core.Path;
import ch.cyberduck.core.PathAttributes;
import ch.cyberduck.core.cryptomator.features.CryptoListService;
import ch.cyberduck.core.features.AttributesFinder;
import ch.cyberduck.core.features.Delete;
import ch.cyberduck.core.features.Directory;
import ch.cyberduck.core.features.Find;
import ch.cyberduck.core.features.Timestamp;
import ch.cyberduck.core.features.Write;
import ch.cyberduck.core.onedrive.AbstractOneDriveTest;
import ch.cyberduck.core.onedrive.GraphItemListService;
import ch.cyberduck.core.onedrive.OneDriveHomeFinderService;
import ch.cyberduck.core.onedrive.features.GraphAttributesFinderFeature;
import ch.cyberduck.core.onedrive.features.GraphDeleteFeature;
import ch.cyberduck.core.onedrive.features.GraphDirectoryFeature;
import ch.cyberduck.core.onedrive.features.GraphFindFeature;
import ch.cyberduck.core.onedrive.features.GraphTimestampFeature;
import ch.cyberduck.core.onedrive.features.GraphWriteFeature;
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
import java.util.Collections;
import java.util.EnumSet;
import java.util.stream.Collectors;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assume.assumeTrue;

@Category(IntegrationTest.class)
@RunWith(value = Parameterized.class)
public class GraphDirectoryFeatureTest extends AbstractOneDriveTest {

    @Test
    public void testMakeDirectoryEncrypted() throws Exception {
        final Path home = new OneDriveHomeFinderService().find();
        final Path vault = new Path(home, new AlphanumericRandomStringService().random(), EnumSet.of(Path.Type.directory));
        final CryptoVault cryptomator = new CryptoVault(vault);
        cryptomator.create(session, new VaultCredentials("test"), vaultVersion);
        session.withRegistry(new DefaultVaultRegistry(new DisabledPasswordCallback(), cryptomator));
        final Path test = cryptomator.getFeature(session, Directory.class, new GraphDirectoryFeature(session, fileid)).mkdir(
                cryptomator.getFeature(session, Write.class, new GraphWriteFeature(session, fileid)), new Path(vault, new AlphanumericRandomStringService().random(), EnumSet.of(Path.Type.directory)), new TransferStatus());
        final String id = test.attributes().getFileId();
        cryptomator.getFeature(session, Timestamp.class, new GraphTimestampFeature(session, fileid)).setTimestamp(test, 1772875587000L);
        // Assert both filename and file id matches
        assertTrue(cryptomator.getFeature(session, Find.class, new GraphFindFeature(session, fileid)).find(test));
        assertTrue(cryptomator.getFeature(session, Find.class, new DefaultFindFeature(session)).find(test));
        final PathAttributes attributes = cryptomator.getFeature(session, AttributesFinder.class, new GraphAttributesFinderFeature(session, fileid)).find(test);
        assertEquals(id, attributes.getFileId());
        assertEquals(Timestamp.toSeconds(1772875587000L), Timestamp.toSeconds(attributes.getModificationDate()));
        assertEquals(id, cryptomator.getFeature(session, AttributesFinder.class, new DefaultAttributesFinderFeature(session)).find(test).getFileId());
        cryptomator.getFeature(session, Delete.class, new GraphDeleteFeature(session, fileid)).delete(Collections.singletonList(test), LoginCallback.noop, new Delete.DisabledCallback());
        assertTrue(new CryptoListService(session, new GraphItemListService(session, fileid), cryptomator).list(vault, new DisabledListProgressListener())
                .toStream().filter(f -> !f.attributes().isDuplicate()).collect(Collectors.toList()).isEmpty());
        cryptomator.getFeature(session, Delete.class, new GraphDeleteFeature(session, fileid)).delete(Collections.singletonList(vault), LoginCallback.noop, new Delete.DisabledCallback());
    }

    @Test
    public void testMakeDirectoryLongFilenameEncrypted() throws Exception {
        assumeTrue(vaultVersion == CryptoVault.VAULT_VERSION_DEPRECATED);
        final Path home = new OneDriveHomeFinderService().find();
        final Path vault = new Path(home, new AlphanumericRandomStringService().random(), EnumSet.of(Path.Type.directory));
        final CryptoVault cryptomator = new CryptoVault(vault);
        cryptomator.create(session, new VaultCredentials("test"), vaultVersion);
        session.withRegistry(new DefaultVaultRegistry(new DisabledPasswordCallback(), cryptomator));
        final Path test = cryptomator.getFeature(session, Directory.class, new GraphDirectoryFeature(session, fileid)).mkdir(
                cryptomator.getFeature(session, Write.class, new GraphWriteFeature(session, fileid)), new Path(vault, new AlphanumericRandomStringService(130).random(), EnumSet.of(Path.Type.directory)), new TransferStatus());
        final String id = test.attributes().getFileId();
        assertTrue(cryptomator.getFeature(session, Find.class, new GraphFindFeature(session, fileid)).find(test));
        assertTrue(cryptomator.getFeature(session, Find.class, new DefaultFindFeature(session)).find(test));
        final PathAttributes attributes = cryptomator.getFeature(session, AttributesFinder.class, new GraphAttributesFinderFeature(session, fileid)).find(test);
        assertEquals(id, attributes.getFileId());
        cryptomator.getFeature(session, Delete.class, new GraphDeleteFeature(session, fileid)).delete(Arrays.asList(test, vault), LoginCallback.noop, new Delete.DisabledCallback());
    }
}
