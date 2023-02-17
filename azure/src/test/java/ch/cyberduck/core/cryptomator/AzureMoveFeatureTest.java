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
import ch.cyberduck.core.azure.AbstractAzureTest;
import ch.cyberduck.core.azure.AzureDeleteFeature;
import ch.cyberduck.core.azure.AzureDirectoryFeature;
import ch.cyberduck.core.azure.AzureFindFeature;
import ch.cyberduck.core.azure.AzureMoveFeature;
import ch.cyberduck.core.azure.AzureTouchFeature;
import ch.cyberduck.core.azure.AzureWriteFeature;
import ch.cyberduck.core.cryptomator.features.CryptoFindV6Feature;
import ch.cyberduck.core.cryptomator.features.CryptoTouchFeature;
import ch.cyberduck.core.features.Delete;
import ch.cyberduck.core.features.Directory;
import ch.cyberduck.core.features.Find;
import ch.cyberduck.core.features.Move;
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

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

@Category(IntegrationTest.class)
@RunWith(value = Parameterized.class)
public class AzureMoveFeatureTest extends AbstractAzureTest {

    @Test
    public void testMove() throws Exception {
        final Path home = new Path("cyberduck", EnumSet.of(Path.Type.directory, Path.Type.volume));
        final CryptoVault cryptomator = new CryptoVault(
            new Path(home, new AlphanumericRandomStringService().random(), EnumSet.of(Path.Type.directory)));
        final Path vault = cryptomator.create(session, new VaultCredentials("test"), new DisabledPasswordStore(), vaultVersion);
        session.withRegistry(new DefaultVaultRegistry(new DisabledPasswordStore(), new DisabledPasswordCallback(), cryptomator));
        final Path folder = cryptomator.getFeature(session, Directory.class, new AzureDirectoryFeature(session, null)).mkdir(
                new Path(vault, new AlphanumericRandomStringService().random(), EnumSet.of(Path.Type.directory)), new TransferStatus());
        final Path file = new CryptoTouchFeature<Void>(session, new AzureTouchFeature(session, null), new AzureWriteFeature(session, null), cryptomator).touch(
                new Path(folder, new AlphanumericRandomStringService().random(), EnumSet.of(Path.Type.file)), new TransferStatus().withLength(0L));
        assertTrue(cryptomator.getFeature(session, Find.class, new DefaultFindFeature(session)).find(file));
        final Move move = cryptomator.getFeature(session, Move.class, new AzureMoveFeature(session, null));
        // rename file
        final Path fileRenamed = new Path(folder, "f1", EnumSet.of(Path.Type.file));
        move.move(file, fileRenamed, new TransferStatus(), new Delete.DisabledCallback(), new DisabledConnectionCallback());
        assertFalse(new CryptoFindV6Feature(session, new AzureFindFeature(session, null), cryptomator).find(file));
        assertTrue(new CryptoFindV6Feature(session, new AzureFindFeature(session, null), cryptomator).find(fileRenamed));
        // rename folder
        final Path folderRenamed = new Path(vault, new AlphanumericRandomStringService().random(), EnumSet.of(Path.Type.directory, Path.Type.placeholder));
        move.move(folder, folderRenamed, new TransferStatus(), new Delete.DisabledCallback(), new DisabledConnectionCallback());
        assertFalse(new CryptoFindV6Feature(session, new AzureFindFeature(session, null), cryptomator).find(folder));
        assertTrue(new CryptoFindV6Feature(session, new AzureFindFeature(session, null), cryptomator).find(folderRenamed));
        final Path fileRenamedInRenamedFolder = new Path(folderRenamed, "f1", EnumSet.of(Path.Type.file));
        assertTrue(new CryptoFindV6Feature(session, new AzureFindFeature(session, null), cryptomator).find(fileRenamedInRenamedFolder));
        cryptomator.getFeature(session, Delete.class, new AzureDeleteFeature(session, null)).delete(Arrays.asList(
                fileRenamedInRenamedFolder, folderRenamed, vault), new DisabledLoginCallback(), new Delete.DisabledCallback());
    }
}
