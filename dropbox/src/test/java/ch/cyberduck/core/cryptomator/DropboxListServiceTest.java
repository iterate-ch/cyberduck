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

import ch.cyberduck.core.AbstractDropboxTest;
import ch.cyberduck.core.AlphanumericRandomStringService;
import ch.cyberduck.core.DisabledListProgressListener;
import ch.cyberduck.core.DisabledLoginCallback;
import ch.cyberduck.core.DisabledPasswordCallback;
import ch.cyberduck.core.DisabledPasswordStore;
import ch.cyberduck.core.Path;
import ch.cyberduck.core.cryptomator.features.CryptoListService;
import ch.cyberduck.core.cryptomator.features.CryptoTouchFeature;
import ch.cyberduck.core.dropbox.DropboxAttributesFinderFeature;
import ch.cyberduck.core.dropbox.DropboxDeleteFeature;
import ch.cyberduck.core.dropbox.DropboxFindFeature;
import ch.cyberduck.core.dropbox.DropboxListService;
import ch.cyberduck.core.dropbox.DropboxUploadFeature;
import ch.cyberduck.core.dropbox.DropboxWriteFeature;
import ch.cyberduck.core.features.Delete;
import ch.cyberduck.core.shared.DefaultHomeFinderService;
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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

@Category(IntegrationTest.class)
@RunWith(value = Parameterized.class)
public class DropboxListServiceTest extends AbstractDropboxTest {

    @Test
    public void testListCryptomator() throws Exception {
        final Path home = new DefaultHomeFinderService(session).find();
        final Path vault = new Path(home, new AlphanumericRandomStringService().random(), EnumSet.of(Path.Type.directory));
        final Path test = new Path(vault, new AlphanumericRandomStringService().random(), EnumSet.of(Path.Type.file));
        final CryptoVault cryptomator = new CryptoVault(vault);
        cryptomator.create(session, null, new VaultCredentials("test"), new DisabledPasswordStore(), vaultVersion);
        session.withRegistry(new DefaultVaultRegistry(new DisabledPasswordStore(), new DisabledPasswordCallback(), cryptomator));
        assertTrue(new CryptoListService(session, new DropboxListService(session), new DropboxFindFeature(session), cryptomator).list(vault, new DisabledListProgressListener()).isEmpty());
        new CryptoTouchFeature<String>(session, new DefaultTouchFeature<String>(new DropboxUploadFeature(new DropboxWriteFeature(session)),
            new DropboxAttributesFinderFeature(session)), new DropboxWriteFeature(session), cryptomator).touch(test, new TransferStatus());
        assertEquals(test, new CryptoListService(session, new DropboxListService(session), new DropboxFindFeature(session), cryptomator).list(vault, new DisabledListProgressListener()).get(0));
        cryptomator.getFeature(session, Delete.class, new DropboxDeleteFeature(session)).delete(Arrays.asList(test, vault), new DisabledLoginCallback(), new Delete.DisabledCallback());
    }
}
