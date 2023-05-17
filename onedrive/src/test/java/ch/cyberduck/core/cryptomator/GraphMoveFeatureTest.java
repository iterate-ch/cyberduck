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
import ch.cyberduck.core.cryptomator.features.CryptoAttributesFeature;
import ch.cyberduck.core.cryptomator.features.CryptoTouchFeature;
import ch.cyberduck.core.features.Delete;
import ch.cyberduck.core.features.Directory;
import ch.cyberduck.core.features.Find;
import ch.cyberduck.core.features.Move;
import ch.cyberduck.core.onedrive.AbstractOneDriveTest;
import ch.cyberduck.core.onedrive.OneDriveHomeFinderService;
import ch.cyberduck.core.onedrive.features.GraphAttributesFinderFeature;
import ch.cyberduck.core.onedrive.features.GraphDeleteFeature;
import ch.cyberduck.core.onedrive.features.GraphDirectoryFeature;
import ch.cyberduck.core.onedrive.features.GraphFindFeature;
import ch.cyberduck.core.onedrive.features.GraphMoveFeature;
import ch.cyberduck.core.onedrive.features.GraphWriteFeature;
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

import static org.junit.Assert.*;

@Category(IntegrationTest.class)
@RunWith(value = Parameterized.class)
public class GraphMoveFeatureTest extends AbstractOneDriveTest {

    @Test
    public void testMove() throws Exception {
        final Path home = new OneDriveHomeFinderService().find();
        final Path vault = new Path(home, new AlphanumericRandomStringService().random(), EnumSet.of(Path.Type.directory));
        final Path folder = new Path(vault, new AlphanumericRandomStringService().random(), EnumSet.of(Path.Type.directory));
        final CryptoVault cryptomator = new CryptoVault(vault);
        cryptomator.create(session, new VaultCredentials("test"), vaultVersion);
        session.withRegistry(new DefaultVaultRegistry(new DisabledPasswordStore(), new DisabledPasswordCallback(), cryptomator));
        cryptomator.getFeature(session, Directory.class, new GraphDirectoryFeature(session, fileid)).mkdir(folder, new TransferStatus());
        final String filename = new AlphanumericRandomStringService().random();
        final Path file = new CryptoTouchFeature<>(session, new DefaultTouchFeature<>(new GraphWriteFeature(session, fileid)
        ), new GraphWriteFeature(session, fileid), cryptomator).touch(
                new Path(folder, filename, EnumSet.of(Path.Type.file)), new TransferStatus());
        assertTrue(cryptomator.getFeature(session, Find.class, new DefaultFindFeature(session)).find(file));
        final Move move = cryptomator.getFeature(session, Move.class, new GraphMoveFeature(session, fileid));
        // rename file
        final Path fileRenamed = move.move(file, new Path(folder, "f1", EnumSet.of(Path.Type.file)), new TransferStatus(), new Delete.DisabledCallback(), new DisabledConnectionCallback());
        assertEquals(file.attributes().getFileId(), fileRenamed.attributes().getFileId());
        assertFalse(cryptomator.getFeature(session, Find.class, new GraphFindFeature(session, fileid)).find(new Path(folder, filename, EnumSet.of(Path.Type.file))));
        assertTrue(cryptomator.getFeature(session, Find.class, new GraphFindFeature(session, fileid)).find(fileRenamed));
        assertEquals(fileRenamed.attributes().getModificationDate(), new CryptoAttributesFeature(session, new GraphAttributesFinderFeature(session, fileid), cryptomator).find(fileRenamed).getModificationDate());
        // rename folder
        final Path folderRenamed = new Path(vault, new AlphanumericRandomStringService().random(), EnumSet.of(Path.Type.directory));
        move.move(folder, folderRenamed, new TransferStatus(), new Delete.DisabledCallback(), new DisabledConnectionCallback());
        assertFalse(cryptomator.getFeature(session, Find.class, new GraphFindFeature(session, fileid)).find(folder));
        assertTrue(cryptomator.getFeature(session, Find.class, new GraphFindFeature(session, fileid)).find(folderRenamed));
        final Path fileRenamedInRenamedFolder = new Path(folderRenamed, "f1", EnumSet.of(Path.Type.file));
        assertTrue(cryptomator.getFeature(session, Find.class, new GraphFindFeature(session, fileid)).find(fileRenamedInRenamedFolder));
        cryptomator.getFeature(session, Delete.class, new GraphDeleteFeature(session, fileid)).delete(Arrays.asList(
                fileRenamedInRenamedFolder, folderRenamed, vault), new DisabledLoginCallback(), new Delete.DisabledCallback());
    }
}
