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
import ch.cyberduck.core.cryptomator.features.CryptoTouchFeature;
import ch.cyberduck.core.features.AttributesFinder;
import ch.cyberduck.core.features.Delete;
import ch.cyberduck.core.features.Directory;
import ch.cyberduck.core.features.Find;
import ch.cyberduck.core.features.Move;
import ch.cyberduck.core.sftp.AbstractSFTPTest;
import ch.cyberduck.core.sftp.SFTPAttributesFinderFeature;
import ch.cyberduck.core.sftp.SFTPDeleteFeature;
import ch.cyberduck.core.sftp.SFTPDirectoryFeature;
import ch.cyberduck.core.sftp.SFTPFindFeature;
import ch.cyberduck.core.sftp.SFTPHomeDirectoryService;
import ch.cyberduck.core.sftp.SFTPMoveFeature;
import ch.cyberduck.core.sftp.SFTPWriteFeature;
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
import java.util.Collections;
import java.util.EnumSet;
import java.util.UUID;

import static org.junit.Assert.*;
import static org.junit.Assume.assumeTrue;

@Category(IntegrationTest.class)
@RunWith(value = Parameterized.class)
public class SFTPMoveFeatureTest extends AbstractSFTPTest {

    @Test
    public void testMoveSameFolderCryptomator() throws Exception {
        final Path home = new SFTPHomeDirectoryService(session).find();
        final Path vault = new Path(home, UUID.randomUUID().toString(), EnumSet.of(Path.Type.directory));
        final Path source = new Path(vault, UUID.randomUUID().toString(), EnumSet.of(Path.Type.file));
        final Path target = new Path(vault, UUID.randomUUID().toString(), EnumSet.of(Path.Type.file));
        final CryptoVault cryptomator = new CryptoVault(vault);
        cryptomator.create(session, new VaultCredentials("test"), vaultVersion);
        session.withRegistry(new DefaultVaultRegistry(new DisabledPasswordStore(), new DisabledPasswordCallback(), cryptomator));
        new CryptoTouchFeature<>(session, new CryptoTouchFeature<>(session, new DefaultTouchFeature<>(new SFTPWriteFeature(session)
        ), new SFTPWriteFeature(session), cryptomator), new SFTPWriteFeature(session), cryptomator).touch(source, new TransferStatus());
        assertTrue(cryptomator.getFeature(session, Find.class, new DefaultFindFeature(session)).find(source));
        cryptomator.getFeature(session, Move.class, new SFTPMoveFeature(session)).move(source, target, new TransferStatus(), new Delete.DisabledCallback(), new DisabledConnectionCallback());
        assertFalse(cryptomator.getFeature(session, Find.class, new DefaultFindFeature(session)).find(source));
        assertTrue(cryptomator.getFeature(session, Find.class, new DefaultFindFeature(session)).find(target));
        cryptomator.getFeature(session, Delete.class, new SFTPDeleteFeature(session)).delete(Arrays.asList(target, vault), new DisabledLoginCallback(), new Delete.DisabledCallback());

    }

    @Test
    public void testMoveToDifferentFolderCryptomator() throws Exception {
        final Path home = new SFTPHomeDirectoryService(session).find();
        final Path vault = new Path(home, UUID.randomUUID().toString(), EnumSet.of(Path.Type.directory));
        final Path source = new Path(vault, UUID.randomUUID().toString(), EnumSet.of(Path.Type.file));
        final Path targetFolder = new Path(vault, UUID.randomUUID().toString(), EnumSet.of(Path.Type.directory));
        final Path target = new Path(targetFolder, UUID.randomUUID().toString(), EnumSet.of(Path.Type.file));
        final CryptoVault cryptomator = new CryptoVault(vault);
        cryptomator.create(session, new VaultCredentials("test"), vaultVersion);
        session.withRegistry(new DefaultVaultRegistry(new DisabledPasswordStore(), new DisabledPasswordCallback(), cryptomator));
        new CryptoTouchFeature<>(session, new CryptoTouchFeature<>(session, new DefaultTouchFeature<>(new SFTPWriteFeature(session)
        ), new SFTPWriteFeature(session), cryptomator), new SFTPWriteFeature(session), cryptomator).touch(source, new TransferStatus());
        assertTrue(cryptomator.getFeature(session, Find.class, new DefaultFindFeature(session)).find(source));
        cryptomator.getFeature(session, Directory.class, new SFTPDirectoryFeature(session)).mkdir(targetFolder, new TransferStatus());
        assertTrue(cryptomator.getFeature(session, Find.class, new DefaultFindFeature(session)).find(targetFolder));
        cryptomator.getFeature(session, Move.class, new SFTPMoveFeature(session)).move(source, target, new TransferStatus(), new Delete.DisabledCallback(), new DisabledConnectionCallback());
        assertFalse(cryptomator.getFeature(session, Find.class, new DefaultFindFeature(session)).find(source));
        assertTrue(cryptomator.getFeature(session, Find.class, new DefaultFindFeature(session)).find(target));
        cryptomator.getFeature(session, Delete.class, new SFTPDeleteFeature(session)).delete(Arrays.asList(target, targetFolder, vault), new DisabledLoginCallback(), new Delete.DisabledCallback());

    }

    @Test
    public void testMoveToDifferentFolderLongFilenameCryptomator() throws Exception {
        assumeTrue(vaultVersion == CryptoVault.VAULT_VERSION_DEPRECATED);
        final Path home = new SFTPHomeDirectoryService(session).find();
        final Path vault = new Path(home, UUID.randomUUID().toString(), EnumSet.of(Path.Type.directory));
        final Path source = new Path(vault, new AlphanumericRandomStringService(130).random(), EnumSet.of(Path.Type.file));
        final Path targetFolder = new Path(vault, new AlphanumericRandomStringService(130).random(), EnumSet.of(Path.Type.directory));
        final Path target = new Path(targetFolder, new AlphanumericRandomStringService(130).random(), EnumSet.of(Path.Type.file));
        final CryptoVault cryptomator = new CryptoVault(vault);
        cryptomator.create(session, new VaultCredentials("test"), vaultVersion);
        session.withRegistry(new DefaultVaultRegistry(new DisabledPasswordStore(), new DisabledPasswordCallback(), cryptomator));
        new CryptoTouchFeature<>(session, new CryptoTouchFeature<>(session, new DefaultTouchFeature<>(new SFTPWriteFeature(session)
        ), new SFTPWriteFeature(session), cryptomator), new SFTPWriteFeature(session), cryptomator).touch(source, new TransferStatus());
        assertTrue(cryptomator.getFeature(session, Find.class, new DefaultFindFeature(session)).find(source));
        cryptomator.getFeature(session, Directory.class, new SFTPDirectoryFeature(session)).mkdir(targetFolder, new TransferStatus());
        assertTrue(cryptomator.getFeature(session, Find.class, new DefaultFindFeature(session)).find(targetFolder));
        cryptomator.getFeature(session, Move.class, new SFTPMoveFeature(session)).move(source, target, new TransferStatus(), new Delete.DisabledCallback(), new DisabledConnectionCallback());
        assertFalse(cryptomator.getFeature(session, Find.class, new DefaultFindFeature(session)).find(source));
        assertTrue(cryptomator.getFeature(session, Find.class, new DefaultFindFeature(session)).find(target));
        cryptomator.getFeature(session, Delete.class, new SFTPDeleteFeature(session)).delete(Arrays.asList(target, targetFolder, vault), new DisabledLoginCallback(), new Delete.DisabledCallback());
    }

    @Test
    public void testMoveFile() throws Exception {
        final Path home = new SFTPHomeDirectoryService(session).find();
        final Path vault = new Path(home, new AlphanumericRandomStringService().random(), EnumSet.of(Path.Type.directory));
        final Path folder = new Path(vault, new AlphanumericRandomStringService().random(), EnumSet.of(Path.Type.directory));
        final CryptoVault cryptomator = new CryptoVault(vault);
        cryptomator.create(session, new VaultCredentials("test"), vaultVersion);
        session.withRegistry(new DefaultVaultRegistry(new DisabledPasswordStore(), new DisabledPasswordCallback(), cryptomator));
        cryptomator.getFeature(session, Directory.class, new SFTPDirectoryFeature(session)).mkdir(folder, new TransferStatus());
        assertTrue(cryptomator.getFeature(session, Find.class, new DefaultFindFeature(session)).find(folder));
        final Path template = new Path(folder, new AlphanumericRandomStringService().random(), EnumSet.of(Path.Type.file));
        final Path file = new CryptoTouchFeature<>(session, new CryptoTouchFeature<>(session, new DefaultTouchFeature<>(new SFTPWriteFeature(session)
        ), new SFTPWriteFeature(session), cryptomator), new SFTPWriteFeature(session), cryptomator).touch(
                template, new TransferStatus()).withAttributes(cryptomator.getFeature(session, AttributesFinder.class, new SFTPAttributesFinderFeature(session)).find(template));
        assertTrue(cryptomator.getFeature(session, Find.class, new DefaultFindFeature(session)).find(file));
        final Move move = cryptomator.getFeature(session, Move.class, new SFTPMoveFeature(session));
        // rename file
        final Path fileRenamed = move.move(file, new Path(folder, new AlphanumericRandomStringService().random(), EnumSet.of(Path.Type.file)), new TransferStatus(), new Delete.DisabledCallback(), new DisabledConnectionCallback());
        assertEquals(fileRenamed.attributes(), cryptomator.getFeature(session, AttributesFinder.class, new SFTPAttributesFinderFeature(session)).find(fileRenamed));
        assertFalse(cryptomator.getFeature(session, Find.class, new SFTPFindFeature(session)).find(file));
        assertEquals(fileRenamed.attributes(), cryptomator.getFeature(session, AttributesFinder.class, new SFTPAttributesFinderFeature(session)).find(fileRenamed));
        assertTrue(cryptomator.getFeature(session, Find.class, new SFTPFindFeature(session)).find(fileRenamed));
        cryptomator.getFeature(session, Delete.class, new SFTPDeleteFeature(session)).delete(Arrays.asList(fileRenamed, folder), new DisabledLoginCallback(), new Delete.DisabledCallback());
    }

    @Test
    public void testMoveFolder() throws Exception {
        final Path home = new SFTPHomeDirectoryService(session).find();
        final Path vault = new Path(home, new AlphanumericRandomStringService().random(), EnumSet.of(Path.Type.directory));
        final Path folder = new Path(vault, new AlphanumericRandomStringService().random(), EnumSet.of(Path.Type.directory));
        final CryptoVault cryptomator = new CryptoVault(vault);
        cryptomator.create(session, new VaultCredentials("test"), vaultVersion);
        session.withRegistry(new DefaultVaultRegistry(new DisabledPasswordStore(), new DisabledPasswordCallback(), cryptomator));
        cryptomator.getFeature(session, Directory.class, new SFTPDirectoryFeature(session)).mkdir(folder, new TransferStatus());
        assertTrue(cryptomator.getFeature(session, Find.class, new DefaultFindFeature(session)).find(folder));
        final Move move = cryptomator.getFeature(session, Move.class, new SFTPMoveFeature(session));
        // rename folder
        final Path folderRenamed = new Path(vault, new AlphanumericRandomStringService().random(), EnumSet.of(Path.Type.directory));
        move.move(folder, folderRenamed, new TransferStatus(), new Delete.DisabledCallback(), new DisabledConnectionCallback());
        assertFalse(cryptomator.getFeature(session, Find.class, new SFTPFindFeature(session)).find(folder));
        assertTrue(cryptomator.getFeature(session, Find.class, new SFTPFindFeature(session)).find(folderRenamed));
        cryptomator.getFeature(session, Delete.class, new SFTPDeleteFeature(session)).delete(Collections.singletonList(folderRenamed), new DisabledLoginCallback(), new Delete.DisabledCallback());
    }

    @Test
    public void testMoveFolderWithFile() throws Exception {
        final Path home = new SFTPHomeDirectoryService(session).find();
        final Path vault = new Path(home, new AlphanumericRandomStringService().random(), EnumSet.of(Path.Type.directory));
        final Path folder = new Path(vault, "folder-1", EnumSet.of(Path.Type.directory));
        final Path file = new Path(folder, "file-1", EnumSet.of(Path.Type.file));
        final CryptoVault cryptomator = new CryptoVault(vault);
        cryptomator.create(session, new VaultCredentials("test"), vaultVersion);
        session.withRegistry(new DefaultVaultRegistry(new DisabledPasswordStore(), new DisabledPasswordCallback(), cryptomator));
        cryptomator.getFeature(session, Directory.class, new SFTPDirectoryFeature(session)).mkdir(folder, new TransferStatus());
        assertTrue(cryptomator.getFeature(session, Find.class, new DefaultFindFeature(session)).find(folder));
        new CryptoTouchFeature<>(session, new CryptoTouchFeature<>(session, new DefaultTouchFeature<>(new SFTPWriteFeature(session)
        ), new SFTPWriteFeature(session), cryptomator), new SFTPWriteFeature(session), cryptomator).touch(file, new TransferStatus());
        assertTrue(cryptomator.getFeature(session, Find.class, new DefaultFindFeature(session)).find(file));
        final Move move = cryptomator.getFeature(session, Move.class, new SFTPMoveFeature(session));
        // rename file
        final Path fileRenamed = new Path(folder, "f1", EnumSet.of(Path.Type.file));
        move.move(file, fileRenamed, new TransferStatus(), new Delete.DisabledCallback(), new DisabledConnectionCallback());
        assertFalse(cryptomator.getFeature(session, Find.class, new SFTPFindFeature(session)).find(file));
        assertTrue(cryptomator.getFeature(session, Find.class, new SFTPFindFeature(session)).find(fileRenamed));
        // rename folder
        final Path folderRenamed = new Path(vault, new AlphanumericRandomStringService().random(), EnumSet.of(Path.Type.directory));
        move.move(folder, folderRenamed, new TransferStatus(), new Delete.DisabledCallback(), new DisabledConnectionCallback());
        assertFalse(cryptomator.getFeature(session, Find.class, new SFTPFindFeature(session)).find(folder));
        assertTrue(cryptomator.getFeature(session, Find.class, new SFTPFindFeature(session)).find(folderRenamed));
        final Path fileRenamedInRenamedFolder = new Path(folderRenamed, "f1", EnumSet.of(Path.Type.file));
        assertTrue(cryptomator.getFeature(session, Find.class, new SFTPFindFeature(session)).find(fileRenamedInRenamedFolder));
        cryptomator.getFeature(session, Delete.class, new SFTPDeleteFeature(session)).delete(Arrays.asList(fileRenamedInRenamedFolder, folderRenamed, vault), new DisabledLoginCallback(), new Delete.DisabledCallback());
    }
}
