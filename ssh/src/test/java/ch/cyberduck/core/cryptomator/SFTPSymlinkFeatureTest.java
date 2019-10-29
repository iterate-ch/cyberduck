package ch.cyberduck.core.cryptomator;

/*
 * Copyright (c) 2002-2019 iterate GmbH. All rights reserved.
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

import ch.cyberduck.core.AbstractPath;
import ch.cyberduck.core.DisabledListProgressListener;
import ch.cyberduck.core.DisabledLoginCallback;
import ch.cyberduck.core.DisabledPasswordCallback;
import ch.cyberduck.core.DisabledPasswordStore;
import ch.cyberduck.core.Path;
import ch.cyberduck.core.cryptomator.features.CryptoDeleteFeature;
import ch.cyberduck.core.cryptomator.features.CryptoFindFeature;
import ch.cyberduck.core.cryptomator.features.CryptoListService;
import ch.cyberduck.core.cryptomator.features.CryptoSymlinkFeature;
import ch.cyberduck.core.cryptomator.features.CryptoTouchFeature;
import ch.cyberduck.core.features.Delete;
import ch.cyberduck.core.sftp.AbstractSFTPTest;
import ch.cyberduck.core.sftp.SFTPAttributesFinderFeature;
import ch.cyberduck.core.sftp.SFTPDeleteFeature;
import ch.cyberduck.core.sftp.SFTPFindFeature;
import ch.cyberduck.core.sftp.SFTPHomeDirectoryService;
import ch.cyberduck.core.sftp.SFTPListService;
import ch.cyberduck.core.sftp.SFTPSymlinkFeature;
import ch.cyberduck.core.sftp.SFTPWriteFeature;
import ch.cyberduck.core.shared.DefaultTouchFeature;
import ch.cyberduck.core.shared.DefaultUploadFeature;
import ch.cyberduck.core.transfer.TransferStatus;
import ch.cyberduck.core.vault.DefaultVaultRegistry;
import ch.cyberduck.core.vault.VaultCredentials;
import ch.cyberduck.test.IntegrationTest;

import org.junit.Test;
import org.junit.experimental.categories.Category;

import java.util.Collections;
import java.util.EnumSet;
import java.util.UUID;

import static org.junit.Assert.*;

@Category(IntegrationTest.class)
public class SFTPSymlinkFeatureTest extends AbstractSFTPTest {

    @Test
    public void testSymlink() throws Exception {
        final Path home = new SFTPHomeDirectoryService(session).find();
        final Path vault = new Path(home, UUID.randomUUID().toString(), EnumSet.of(Path.Type.directory));
        final CryptoVault cryptomator = new CryptoVault(vault);
        cryptomator.create(session, null, new VaultCredentials("test"), new DisabledPasswordStore());
        session.withRegistry(new DefaultVaultRegistry(new DisabledPasswordStore(), new DisabledPasswordCallback(), cryptomator));
        final Path target = new Path(vault, UUID.randomUUID().toString(), EnumSet.of(Path.Type.file));
        new CryptoTouchFeature<Void>(session, new DefaultTouchFeature<Void>(new DefaultUploadFeature<Void>(new SFTPWriteFeature(session)),
            new SFTPAttributesFinderFeature(session)), new SFTPWriteFeature(session), cryptomator).touch(target, new TransferStatus());
        final Path link = new Path(vault, UUID.randomUUID().toString(), EnumSet.of(Path.Type.file, AbstractPath.Type.symboliclink));
        new CryptoSymlinkFeature(session, new SFTPSymlinkFeature(session), cryptomator).symlink(link, target.getName());
        assertTrue(new CryptoFindFeature(session, new SFTPFindFeature(session), cryptomator).find(link));
        assertEquals(EnumSet.of(Path.Type.file, Path.Type.symboliclink, Path.Type.decrypted),
            new CryptoListService(session, new SFTPListService(session), cryptomator).list(vault, new DisabledListProgressListener()).get(link).getType());
        new CryptoDeleteFeature(session, new SFTPDeleteFeature(session), cryptomator).delete(Collections.singletonList(link), new DisabledLoginCallback(), new Delete.DisabledCallback());
        assertFalse(new CryptoFindFeature(session, new SFTPFindFeature(session), cryptomator).find(link));
        assertTrue(new CryptoFindFeature(session, new SFTPFindFeature(session), cryptomator).find(target));
        new CryptoDeleteFeature(session, new SFTPDeleteFeature(session), cryptomator).delete(Collections.singletonList(target), new DisabledLoginCallback(), new Delete.DisabledCallback());
    }
}
