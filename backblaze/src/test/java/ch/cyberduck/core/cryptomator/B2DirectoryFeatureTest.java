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
import ch.cyberduck.core.DisabledLoginCallback;
import ch.cyberduck.core.DisabledPasswordCallback;
import ch.cyberduck.core.DisabledPasswordStore;
import ch.cyberduck.core.Path;
import ch.cyberduck.core.b2.AbstractB2Test;
import ch.cyberduck.core.b2.B2DeleteFeature;
import ch.cyberduck.core.b2.B2DirectoryFeature;
import ch.cyberduck.core.b2.B2FileidProvider;
import ch.cyberduck.core.b2.B2WriteFeature;
import ch.cyberduck.core.cryptomator.features.CryptoDeleteFeature;
import ch.cyberduck.core.cryptomator.features.CryptoDirectoryFeature;
import ch.cyberduck.core.cryptomator.features.CryptoFindFeature;
import ch.cyberduck.core.features.Delete;
import ch.cyberduck.core.shared.DefaultFindFeature;
import ch.cyberduck.core.transfer.TransferStatus;
import ch.cyberduck.core.vault.DefaultVaultRegistry;
import ch.cyberduck.core.vault.VaultCredentials;
import ch.cyberduck.test.IntegrationTest;

import org.apache.commons.text.RandomStringGenerator;
import org.junit.Test;
import org.junit.experimental.categories.Category;

import java.util.Arrays;
import java.util.EnumSet;
import java.util.UUID;

import synapticloop.b2.response.BaseB2Response;

import static org.junit.Assert.assertTrue;

@Category(IntegrationTest.class)
public class B2DirectoryFeatureTest extends AbstractB2Test {

    @Test
    public void testMakeDirectoryEncrypted() throws Exception {
        final Path home = new Path("/test-cyberduck", EnumSet.of(Path.Type.directory, Path.Type.volume));
        final CryptoVault cryptomator = new CryptoVault(
            new Path(home, new AlphanumericRandomStringService().random(), EnumSet.of(Path.Type.directory)));
        final Path vault = cryptomator.create(session, null, new VaultCredentials("test"), new DisabledPasswordStore());
        session.withRegistry(new DefaultVaultRegistry(new DisabledPasswordStore(), new DisabledPasswordCallback(), cryptomator));
        final B2FileidProvider fileid = new B2FileidProvider(session).withCache(cache);
        final Path test = new CryptoDirectoryFeature<BaseB2Response>(session, new B2DirectoryFeature(session, fileid), new B2WriteFeature(session, fileid), cryptomator).mkdir(
                new Path(vault, UUID.randomUUID().toString(), EnumSet.of(Path.Type.directory)), null, new TransferStatus());
        assertTrue(test.getType().contains(Path.Type.placeholder));
        assertTrue(new CryptoFindFeature(session, new DefaultFindFeature(session), cryptomator).find(test, new DisabledListProgressListener()));
        new CryptoDeleteFeature(session, new B2DeleteFeature(session, fileid), cryptomator).delete(Arrays.asList(test, vault), new DisabledLoginCallback(), new Delete.DisabledCallback());
    }

    @Test
    public void testMakeDirectoryLongFilenameEncrypted() throws Exception {
        final Path home = new Path("/test-cyberduck", EnumSet.of(Path.Type.directory, Path.Type.volume));
        final CryptoVault cryptomator = new CryptoVault(
            new Path(home, new AlphanumericRandomStringService().random(), EnumSet.of(Path.Type.directory)));
        final Path vault = cryptomator.create(session, null, new VaultCredentials("test"), new DisabledPasswordStore());
        session.withRegistry(new DefaultVaultRegistry(new DisabledPasswordStore(), new DisabledPasswordCallback(), cryptomator));
        final B2FileidProvider fileid = new B2FileidProvider(session).withCache(cache);
        final Path test = new CryptoDirectoryFeature<BaseB2Response>(session, new B2DirectoryFeature(session, fileid), new B2WriteFeature(session, fileid), cryptomator).mkdir(new Path(vault, new RandomStringGenerator.Builder().build().generate(130), EnumSet.of(Path.Type.directory)), null, new TransferStatus());
        assertTrue(new CryptoFindFeature(session, new DefaultFindFeature(session), cryptomator).find(test, new DisabledListProgressListener()));
        new CryptoDeleteFeature(session, new B2DeleteFeature(session, fileid), cryptomator).delete(Arrays.asList(test, vault), new DisabledLoginCallback(), new Delete.DisabledCallback());
        session.close();
    }
}
