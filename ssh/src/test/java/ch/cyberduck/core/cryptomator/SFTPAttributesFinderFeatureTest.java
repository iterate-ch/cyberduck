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
import ch.cyberduck.core.AttributedList;
import ch.cyberduck.core.Cache;
import ch.cyberduck.core.CachingAttributesFinderFeature;
import ch.cyberduck.core.DisabledListProgressListener;
import ch.cyberduck.core.DisabledLoginCallback;
import ch.cyberduck.core.DisabledPasswordCallback;
import ch.cyberduck.core.DisabledPasswordStore;
import ch.cyberduck.core.Path;
import ch.cyberduck.core.PathAttributes;
import ch.cyberduck.core.PathCache;
import ch.cyberduck.core.cryptomator.features.CryptoListService;
import ch.cyberduck.core.cryptomator.features.CryptoTouchFeature;
import ch.cyberduck.core.features.AttributesFinder;
import ch.cyberduck.core.features.Delete;
import ch.cyberduck.core.sftp.AbstractSFTPTest;
import ch.cyberduck.core.sftp.SFTPDeleteFeature;
import ch.cyberduck.core.sftp.SFTPFindFeature;
import ch.cyberduck.core.sftp.SFTPHomeDirectoryService;
import ch.cyberduck.core.sftp.SFTPListService;
import ch.cyberduck.core.sftp.SFTPWriteFeature;
import ch.cyberduck.core.shared.DefaultAttributesFinderFeature;
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
public class SFTPAttributesFinderFeatureTest extends AbstractSFTPTest {

    @Test
    public void testFindCryptomator() throws Exception {
        final Path home = new SFTPHomeDirectoryService(session).find();
        final Path vault = new Path(home, new AlphanumericRandomStringService().random(), EnumSet.of(Path.Type.directory));
        final CryptoVault cryptomator = new CryptoVault(vault);
        cryptomator.create(session, new VaultCredentials("test"), vaultVersion);
        session.withRegistry(new DefaultVaultRegistry(new DisabledPasswordStore(), new DisabledPasswordCallback(), cryptomator));
        final Path test = new CryptoTouchFeature<>(session, new DefaultTouchFeature<>(new SFTPWriteFeature(session)
        ), new SFTPWriteFeature(session), cryptomator).touch(
                new Path(vault, new AlphanumericRandomStringService().random(), EnumSet.of(Path.Type.file)), new TransferStatus());
        test.attributes().setSize(0L);
        final PathAttributes attributes = cryptomator.getFeature(session, AttributesFinder.class, new DefaultAttributesFinderFeature(session)).find(test);
        assertNotNull(attributes);
        assertEquals(0L, attributes.getSize());
        cryptomator.getFeature(session, Delete.class, new SFTPDeleteFeature(session)).delete(Arrays.asList(test, vault), new DisabledLoginCallback(), new Delete.DisabledCallback());
        assertFalse(new SFTPFindFeature(session).find(vault));
        assertFalse(new SFTPFindFeature(session).find(cryptomator.getHome()));
        assertFalse(new SFTPFindFeature(session).find(cryptomator.getMasterkey()));
        assertFalse(new SFTPFindFeature(session).find(cryptomator.getConfig()));
    }

    @Test
    public void testFindDefaultAttributesFinderCryptomator() throws Exception {
        final Path home = new SFTPHomeDirectoryService(session).find();
        final Path vault = new Path(home, new AlphanumericRandomStringService().random(), EnumSet.of(Path.Type.directory));
        final CryptoVault cryptomator = new CryptoVault(vault);
        cryptomator.create(session, new VaultCredentials("test"), vaultVersion);
        session.withRegistry(new DefaultVaultRegistry(new DisabledPasswordStore(), new DisabledPasswordCallback(), cryptomator));
        final Path test = new CryptoTouchFeature<>(session, new DefaultTouchFeature<>(new SFTPWriteFeature(session)
        ), new SFTPWriteFeature(session), cryptomator).touch(
                new Path(vault, new AlphanumericRandomStringService().random(), EnumSet.of(Path.Type.file)), new TransferStatus());
        test.attributes().setSize(0L);
        final PathAttributes attributes = cryptomator.getFeature(session, AttributesFinder.class, new DefaultAttributesFinderFeature(session)).find(test);
        assertNotNull(attributes);
        assertEquals(0L, attributes.getSize());
        cryptomator.getFeature(session, Delete.class, new SFTPDeleteFeature(session)).delete(Arrays.asList(test, vault), new DisabledLoginCallback(), new Delete.DisabledCallback());
    }

    @Test
    public void testFindDefaultAttributesFinderWithCacheCryptomator() throws Exception {
        final Path home = new SFTPHomeDirectoryService(session).find();
        final Path vault = new Path(home, new AlphanumericRandomStringService().random(), EnumSet.of(Path.Type.directory));
        final CryptoVault cryptomator = new CryptoVault(vault);
        cryptomator.create(session, new VaultCredentials("test"), vaultVersion);
        session.withRegistry(new DefaultVaultRegistry(new DisabledPasswordStore(), new DisabledPasswordCallback(), cryptomator));
        final Path test = new CryptoTouchFeature<>(session, new DefaultTouchFeature<>(new SFTPWriteFeature(session)
        ), new SFTPWriteFeature(session), cryptomator).touch(
                new Path(vault, new AlphanumericRandomStringService().random(), EnumSet.of(Path.Type.file)), new TransferStatus());
        final Path found = new CryptoListService(session, new SFTPListService(session), cryptomator).list(test.getParent(), new DisabledListProgressListener()).get(test);
        assertEquals(0L, found.attributes().getSize());
        final Cache<Path> cache = new PathCache(1);
        final AttributedList<Path> list = new AttributedList<>();
        list.add(found);
        cache.put(vault, list);
        final PathAttributes attributes = new CachingAttributesFinderFeature(session, cache, cryptomator.getFeature(session, AttributesFinder.class, new DefaultAttributesFinderFeature(session))).find(test);
        assertNotNull(attributes);
        assertEquals(0L, attributes.getSize());
        assertEquals(0L, cache.get(vault).get(0).attributes().getSize());
        cryptomator.getFeature(session, Delete.class, new SFTPDeleteFeature(session)).delete(Arrays.asList(test, vault), new DisabledLoginCallback(), new Delete.DisabledCallback());
    }
}
