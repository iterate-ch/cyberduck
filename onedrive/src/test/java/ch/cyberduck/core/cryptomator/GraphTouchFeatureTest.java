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
import ch.cyberduck.core.cryptomator.features.CryptoDeleteFeature;
import ch.cyberduck.core.cryptomator.features.CryptoFindFeature;
import ch.cyberduck.core.cryptomator.features.CryptoTouchFeature;
import ch.cyberduck.core.features.Delete;
import ch.cyberduck.core.onedrive.AbstractOneDriveTest;
import ch.cyberduck.core.onedrive.features.GraphAttributesFinderFeature;
import ch.cyberduck.core.onedrive.features.OneDriveDeleteFeature;
import ch.cyberduck.core.onedrive.features.OneDriveFindFeature;
import ch.cyberduck.core.onedrive.features.OneDriveHomeFinderFeature;
import ch.cyberduck.core.onedrive.features.GraphWriteFeature;
import ch.cyberduck.core.shared.DefaultFindFeature;
import ch.cyberduck.core.shared.DefaultTouchFeature;
import ch.cyberduck.core.shared.DefaultUploadFeature;
import ch.cyberduck.core.transfer.TransferStatus;
import ch.cyberduck.core.vault.DefaultVaultRegistry;
import ch.cyberduck.core.vault.VaultCredentials;
import ch.cyberduck.test.IntegrationTest;

import org.apache.commons.text.RandomStringGenerator;
import org.junit.Test;
import org.junit.experimental.categories.Category;

import java.util.Arrays;
import java.util.EnumSet;

import static org.junit.Assert.*;

@Category(IntegrationTest.class)
public class GraphTouchFeatureTest extends AbstractOneDriveTest {

    @Test
    public void testTouchLongFilenameEncrypted() throws Exception {
        final Path home = new OneDriveHomeFinderFeature(session).find();
        final Path vault = new Path(home, new AlphanumericRandomStringService().random(), EnumSet.of(Path.Type.directory));
        final CryptoVault cryptomator = new CryptoVault(vault);
        cryptomator.create(session, null, new VaultCredentials("test"), new DisabledPasswordStore());
        session.withRegistry(new DefaultVaultRegistry(new DisabledPasswordStore(), new DisabledPasswordCallback(), cryptomator));
        final Path test = new CryptoTouchFeature<Void>(session, new DefaultTouchFeature<Void>(new DefaultUploadFeature<>(new GraphWriteFeature(session)),
            new GraphAttributesFinderFeature(session)), new GraphWriteFeature(session), cryptomator).touch(
            new Path(vault, new RandomStringGenerator.Builder().build().generate(130), EnumSet.of(Path.Type.file)), new TransferStatus());
        assertNotNull(test.attributes().getVault());
        assertTrue(new CryptoFindFeature(session, new OneDriveFindFeature(session), cryptomator).find(test));
        final PathAttributes attributes = new CryptoAttributesFeature(session, new GraphAttributesFinderFeature(session), cryptomator).find(test);
        assertNotNull(attributes.getVersionId());
        assertEquals(test.attributes(), attributes);
        new CryptoDeleteFeature(session, new OneDriveDeleteFeature(session), cryptomator).delete(Arrays.asList(test, vault), new DisabledLoginCallback(), new Delete.DisabledCallback());
    }

    @Test
    public void testTouchLongFilenameEncryptedDefaultFeature() throws Exception {
        final Path home = new OneDriveHomeFinderFeature(session).find();
        final Path vault = new Path(home, new AlphanumericRandomStringService().random(), EnumSet.of(Path.Type.directory));
        final CryptoVault cryptomator = new CryptoVault(vault);
        cryptomator.create(session, null, new VaultCredentials("test"), new DisabledPasswordStore());
        session.withRegistry(new DefaultVaultRegistry(new DisabledPasswordStore(), new DisabledPasswordCallback(), cryptomator));
        final Path test = new CryptoTouchFeature<>(session, new DefaultTouchFeature<Void>(new DefaultUploadFeature<Void>(new GraphWriteFeature(session)),
            new GraphAttributesFinderFeature(session)), new GraphWriteFeature(session), cryptomator).touch(
            new Path(vault, new RandomStringGenerator.Builder().build().generate(130), EnumSet.of(Path.Type.file)), new TransferStatus());
        assertNotNull(test.attributes().getVault());
        assertTrue(new CryptoFindFeature(session, new DefaultFindFeature(session), cryptomator).find(test));
        final PathAttributes attributes = new CryptoAttributesFeature(session, new GraphAttributesFinderFeature(session), cryptomator).find(test);
        assertNotNull(attributes.getVersionId());
        assertEquals(test.attributes(), attributes);
        new CryptoDeleteFeature(session, new OneDriveDeleteFeature(session), cryptomator).delete(Arrays.asList(test, vault), new DisabledLoginCallback(), new Delete.DisabledCallback());
    }
}
