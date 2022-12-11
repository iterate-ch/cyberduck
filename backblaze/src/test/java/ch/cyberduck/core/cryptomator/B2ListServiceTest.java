package ch.cyberduck.core.cryptomator;

/*
 * Copyright (c) 2002-2018 iterate GmbH. All rights reserved.
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
import ch.cyberduck.core.DisabledLoginCallback;
import ch.cyberduck.core.DisabledPasswordCallback;
import ch.cyberduck.core.DisabledPasswordStore;
import ch.cyberduck.core.Path;
import ch.cyberduck.core.b2.AbstractB2Test;
import ch.cyberduck.core.b2.B2DeleteFeature;
import ch.cyberduck.core.b2.B2ListService;
import ch.cyberduck.core.b2.B2VersionIdProvider;
import ch.cyberduck.core.b2.B2WriteFeature;
import ch.cyberduck.core.cryptomator.features.CryptoListService;
import ch.cyberduck.core.cryptomator.features.CryptoTouchFeature;
import ch.cyberduck.core.cryptomator.features.CryptoVersionIdProvider;
import ch.cyberduck.core.features.Delete;
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

import synapticloop.b2.response.BaseB2Response;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

@Category(IntegrationTest.class)
@RunWith(value = Parameterized.class)
public class B2ListServiceTest extends AbstractB2Test {

    @Test
    public void testListCryptomator() throws Exception {
        final Path home = new Path("test-cyberduck", EnumSet.of(Path.Type.volume, Path.Type.directory));
        final CryptoVault cryptomator = new CryptoVault(
                new Path(home, new AlphanumericRandomStringService().random(), EnumSet.of(Path.Type.directory)));
        final Path vault = cryptomator.create(session, new VaultCredentials("test"), new DisabledPasswordStore(), vaultVersion);
        session.withRegistry(new DefaultVaultRegistry(new DisabledPasswordStore(), new DisabledPasswordCallback(), cryptomator));
        final B2VersionIdProvider fileid = new B2VersionIdProvider(session);
        assertTrue(new CryptoListService(session, new B2ListService(session, fileid), cryptomator).list(vault, new DisabledListProgressListener()).isEmpty());
        final Path test = new CryptoTouchFeature<BaseB2Response>(session, new DefaultTouchFeature<BaseB2Response>(new B2WriteFeature(session, fileid)
        ), new B2WriteFeature(session, fileid), cryptomator).touch(
                new Path(vault, new AlphanumericRandomStringService().random(), EnumSet.of(Path.Type.file)), new TransferStatus());
        test.attributes().setVersionId(new CryptoVersionIdProvider(session, fileid, cryptomator).getVersionId(test));
        assertEquals(test, new CryptoListService(session, new B2ListService(session, fileid), cryptomator).list(vault, new DisabledListProgressListener()).get(0));
        cryptomator.getFeature(session, Delete.class, new B2DeleteFeature(session, fileid)).delete(Arrays.asList(test, vault), new DisabledLoginCallback(), new Delete.DisabledCallback());
    }
}
