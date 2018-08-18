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
import ch.cyberduck.core.ListService;
import ch.cyberduck.core.Path;
import ch.cyberduck.core.PathCache;
import ch.cyberduck.core.SimplePathPredicate;
import ch.cyberduck.core.b2.AbstractB2Test;
import ch.cyberduck.core.b2.B2AttributesFinderFeature;
import ch.cyberduck.core.b2.B2DeleteFeature;
import ch.cyberduck.core.b2.B2FileidProvider;
import ch.cyberduck.core.b2.B2ListService;
import ch.cyberduck.core.b2.B2WriteFeature;
import ch.cyberduck.core.cryptomator.features.CryptoDeleteFeature;
import ch.cyberduck.core.cryptomator.features.CryptoIdProvider;
import ch.cyberduck.core.cryptomator.features.CryptoListService;
import ch.cyberduck.core.cryptomator.features.CryptoTouchFeature;
import ch.cyberduck.core.features.Delete;
import ch.cyberduck.core.shared.DefaultTouchFeature;
import ch.cyberduck.core.shared.DefaultUploadFeature;
import ch.cyberduck.core.transfer.TransferStatus;
import ch.cyberduck.core.vault.DefaultVaultRegistry;
import ch.cyberduck.core.vault.VaultCredentials;
import ch.cyberduck.test.IntegrationTest;

import org.junit.Test;
import org.junit.experimental.categories.Category;

import java.util.Arrays;
import java.util.EnumSet;

import synapticloop.b2.response.BaseB2Response;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

@Category(IntegrationTest.class)
public class B2ListServiceTest extends AbstractB2Test {

    @Test
    public void testListCryptomator() throws Exception {
        final Path home = new Path("test-cyberduck", EnumSet.of(Path.Type.volume, Path.Type.directory));
        final CryptoVault cryptomator = new CryptoVault(
            new Path(home, new AlphanumericRandomStringService().random(), EnumSet.of(Path.Type.directory)));
        final Path vault = cryptomator.create(session, null, new VaultCredentials("test"), new DisabledPasswordStore());
        session.withRegistry(new DefaultVaultRegistry(new DisabledPasswordStore(), new DisabledPasswordCallback(), cryptomator));
        final B2FileidProvider fileid = new B2FileidProvider(session).withCache(cache);
        assertTrue(new CryptoListService(session, new B2ListService(session, fileid), cryptomator).list(vault, new DisabledListProgressListener()).isEmpty());
        final Path test = new CryptoTouchFeature<BaseB2Response>(session, new DefaultTouchFeature<BaseB2Response>(new DefaultUploadFeature<BaseB2Response>(new B2WriteFeature(session, fileid)),
            new B2AttributesFinderFeature(session, fileid)), new B2WriteFeature(session, fileid), cryptomator).touch(
            new Path(vault, new AlphanumericRandomStringService().random(), EnumSet.of(Path.Type.file)), new TransferStatus());
        test.attributes().setVersionId(new CryptoIdProvider(session, fileid, cryptomator).getFileid(test, new DisabledListProgressListener()));
        assertEquals(test, new CryptoListService(session, new B2ListService(session, fileid), cryptomator).list(vault, new DisabledListProgressListener()).get(0));
        new CryptoDeleteFeature(session, new B2DeleteFeature(session, fileid), cryptomator).delete(Arrays.asList(test, vault), new DisabledLoginCallback(), new Delete.DisabledCallback());
    }

    @Test
    public void testListCryptomatorCached() throws Exception {
        final PathCache cache = new PathCache(Integer.MAX_VALUE);
        final B2FileidProvider fileid = new B2FileidProvider(session).withCache(cache);
        final ListService listService = session._getFeature(ListService.class);
        final Path home = new Path("test-cyberduck", EnumSet.of(Path.Type.volume, Path.Type.directory));
        cache.put(home, listService.withCache(cache).list(home, new DisabledListProgressListener()));
        final Path vault = new Path(home, new AlphanumericRandomStringService().random(), EnumSet.of(Path.Type.directory));
        final CryptoVault cryptomator = new CryptoVault(vault);
        cryptomator.create(session, null, new VaultCredentials("test"), new DisabledPasswordStore());
        session.withRegistry(new DefaultVaultRegistry(new DisabledPasswordStore(), new DisabledPasswordCallback(), cryptomator));
        final Path test = new Path(vault, new AlphanumericRandomStringService().random(), EnumSet.of(Path.Type.file));
        assertTrue(new CryptoListService(session, listService, cryptomator).list(vault, new DisabledListProgressListener()).isEmpty());
        new CryptoTouchFeature<BaseB2Response>(session, new DefaultTouchFeature<BaseB2Response>(new DefaultUploadFeature<>(new B2WriteFeature(session, fileid)),
            new B2AttributesFinderFeature(session, fileid)), new B2WriteFeature(session, fileid), cryptomator).touch(test, new TransferStatus());
        assertEquals(new SimplePathPredicate(test), new SimplePathPredicate(new CryptoListService(session, listService, cryptomator).list(vault, new DisabledListProgressListener()).get(0)));
        new CryptoDeleteFeature(session, new B2DeleteFeature(session, fileid), cryptomator).delete(Arrays.asList(test, vault), new DisabledLoginCallback(), new Delete.DisabledCallback());
    }
}
