package ch.cyberduck.core.cryptomator;

/*
 * Copyright (c) 2002-2023 iterate GmbH. All rights reserved.
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
import ch.cyberduck.core.Path;
import ch.cyberduck.core.brick.AbstractBrickTest;
import ch.cyberduck.core.brick.BrickDeleteFeature;
import ch.cyberduck.core.brick.BrickFindFeature;
import ch.cyberduck.core.brick.BrickWriteFeature;
import ch.cyberduck.core.brick.io.swagger.client.model.FileEntity;
import ch.cyberduck.core.cryptomator.features.CryptoTouchFeature;
import ch.cyberduck.core.cryptomator.features.CryptoWriteFeature;
import ch.cyberduck.core.features.Delete;
import ch.cyberduck.core.features.Find;
import ch.cyberduck.core.shared.DefaultFindFeature;
import ch.cyberduck.core.shared.DefaultTouchFeature;
import ch.cyberduck.core.transfer.TransferStatus;
import ch.cyberduck.core.vault.DefaultVaultRegistry;
import ch.cyberduck.core.vault.VaultCredentials;
import ch.cyberduck.core.vault.VaultMetadata;
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

@Category(IntegrationTest.class)
@RunWith(value = Parameterized.class)
public class DefaultTouchFeatureTest extends AbstractBrickTest {

    //TODO

    @Test
    @Ignore(value = "Filename shortening not yet implemented")
    public void testTouchLongFilenameEncrypted() throws Exception {
        final Path home = new Path("/", EnumSet.of(Path.Type.directory, Path.Type.volume));
        final Path vault = new Path(home, new AlphanumericRandomStringService().random(), EnumSet.of(Path.Type.directory));
        final AbstractVault cryptomator = new CryptoVaultProvider(session).create(session, null, new VaultCredentials("test"),
                new VaultMetadata(vault, vaultVersion));
        session.withRegistry(new DefaultVaultRegistry(new DisabledPasswordCallback(), cryptomator));
        final TransferStatus status = new TransferStatus();
        final Path test = new CryptoTouchFeature<>(session, new DefaultTouchFeature<FileEntity>(session), cryptomator).touch(
                new CryptoWriteFeature<>(session, new BrickWriteFeature(session), cryptomator), new Path(vault, new AlphanumericRandomStringService(130).random(), EnumSet.of(Path.Type.file)), status);
        assertEquals(TransferStatus.UNKNOWN_LENGTH, status.getResponse().getSize());
        assertTrue(cryptomator.getFeature(session, Find.class, new BrickFindFeature(session)).find(test));
        cryptomator.getFeature(session, Delete.class, new BrickDeleteFeature(session)).delete(Arrays.asList(test, vault), new DisabledLoginCallback(), new Delete.DisabledCallback());
    }

    @Test
    @Ignore(value = "Filename shortening not yet implemented")
    public void testTouchLongFilenameEncryptedDefaultFeature() throws Exception {
        final Path home = new Path("/", EnumSet.of(Path.Type.directory, Path.Type.volume));
        final Path vault = new Path(home, new AlphanumericRandomStringService().random(), EnumSet.of(Path.Type.directory));
        final AbstractVault cryptomator = new CryptoVaultProvider(session).create(session, null, new VaultCredentials("test"),
                new VaultMetadata(vault, vaultVersion));
        session.withRegistry(new DefaultVaultRegistry(new DisabledPasswordCallback(), cryptomator));
        final TransferStatus status = new TransferStatus();
        final Path test = new CryptoTouchFeature<>(session, new DefaultTouchFeature<FileEntity>(session), cryptomator).touch(
                new CryptoWriteFeature<>(session, new BrickWriteFeature(session), cryptomator), new Path(vault, new AlphanumericRandomStringService(130).random(), EnumSet.of(Path.Type.file)), status);
        assertEquals(TransferStatus.UNKNOWN_LENGTH, status.getResponse().getSize());
        assertTrue(cryptomator.getFeature(session, Find.class, new DefaultFindFeature(session)).find(test));
        cryptomator.getFeature(session, Delete.class, new BrickDeleteFeature(session)).delete(Arrays.asList(test, vault), new DisabledLoginCallback(), new Delete.DisabledCallback());
    }
}
