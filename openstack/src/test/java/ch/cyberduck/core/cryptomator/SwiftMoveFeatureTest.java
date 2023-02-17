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
import ch.cyberduck.core.cryptomator.features.CryptoFindV6Feature;
import ch.cyberduck.core.cryptomator.features.CryptoTouchFeature;
import ch.cyberduck.core.features.Delete;
import ch.cyberduck.core.features.Directory;
import ch.cyberduck.core.features.Find;
import ch.cyberduck.core.features.Move;
import ch.cyberduck.core.openstack.AbstractSwiftTest;
import ch.cyberduck.core.openstack.SwiftDeleteFeature;
import ch.cyberduck.core.openstack.SwiftDirectoryFeature;
import ch.cyberduck.core.openstack.SwiftFindFeature;
import ch.cyberduck.core.openstack.SwiftMoveFeature;
import ch.cyberduck.core.openstack.SwiftRegionService;
import ch.cyberduck.core.openstack.SwiftTouchFeature;
import ch.cyberduck.core.openstack.SwiftWriteFeature;
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
public class SwiftMoveFeatureTest extends AbstractSwiftTest {

    @Test
    public void testMove() throws Exception {
        final Path home = new Path("/test.cyberduck.ch", EnumSet.of(Path.Type.volume, Path.Type.directory));
        final Path vault = new Path(home, new AlphanumericRandomStringService().random(), EnumSet.of(Path.Type.directory));
        final Path folder = new Path(vault, new AlphanumericRandomStringService().random(), EnumSet.of(Path.Type.directory));
        final Path file = new Path(folder, new AlphanumericRandomStringService().random(), EnumSet.of(Path.Type.file));
        final CryptoVault cryptomator = new CryptoVault(vault);
        cryptomator.create(session, new VaultCredentials("test"), new DisabledPasswordStore(), vaultVersion);
        final SwiftRegionService regionService = new SwiftRegionService(session);
        session.withRegistry(new DefaultVaultRegistry(new DisabledPasswordStore(), new DisabledPasswordCallback(), cryptomator));
        cryptomator.getFeature(session, Directory.class, new SwiftDirectoryFeature(session, regionService)).mkdir(folder, new TransferStatus());
        new CryptoTouchFeature<>(session, new SwiftTouchFeature(session, regionService), new SwiftWriteFeature(session, regionService), cryptomator).touch(file,
                new TransferStatus().withLength(0L));
        assertTrue(cryptomator.getFeature(session, Find.class, new DefaultFindFeature(session)).find(file));
        final Move move = cryptomator.getFeature(session, Move.class, new SwiftMoveFeature(session, regionService));
        // rename file
        final Path fileRenamed = new Path(folder, "f1", EnumSet.of(Path.Type.file));
        move.move(file, fileRenamed, new TransferStatus(), new Delete.DisabledCallback(), new DisabledConnectionCallback());
        assertFalse(new CryptoFindV6Feature(session, new SwiftFindFeature(session), cryptomator).find(file));
        assertTrue(new CryptoFindV6Feature(session, new SwiftFindFeature(session), cryptomator).find(fileRenamed));
        // rename folder
        final Path folderRenamed = new Path(vault, new AlphanumericRandomStringService().random(), EnumSet.of(Path.Type.directory));
        move.move(folder, folderRenamed, new TransferStatus(), new Delete.DisabledCallback(), new DisabledConnectionCallback());
        assertFalse(new CryptoFindV6Feature(session, new SwiftFindFeature(session), cryptomator).find(folder));
        assertTrue(new CryptoFindV6Feature(session, new SwiftFindFeature(session), cryptomator).find(folderRenamed));
        final Path fileRenamedInRenamedFolder = new Path(folderRenamed, "f1", EnumSet.of(Path.Type.file));
        assertTrue(new CryptoFindV6Feature(session, new SwiftFindFeature(session), cryptomator).find(fileRenamedInRenamedFolder));
        cryptomator.getFeature(session, Delete.class, new SwiftDeleteFeature(session)).delete(Arrays.asList(fileRenamedInRenamedFolder, folderRenamed, vault), new DisabledLoginCallback(), new Delete.DisabledCallback());
    }
}
