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
import ch.cyberduck.core.b2.B2AttributesFinderFeature;
import ch.cyberduck.core.b2.B2DeleteFeature;
import ch.cyberduck.core.b2.B2FileidProvider;
import ch.cyberduck.core.b2.B2FindFeature;
import ch.cyberduck.core.b2.B2SingleUploadService;
import ch.cyberduck.core.b2.B2TouchFeature;
import ch.cyberduck.core.b2.B2WriteFeature;
import ch.cyberduck.core.cryptomator.features.CryptoAttributesFeature;
import ch.cyberduck.core.cryptomator.features.CryptoDeleteFeature;
import ch.cyberduck.core.cryptomator.features.CryptoFindFeature;
import ch.cyberduck.core.cryptomator.features.CryptoTouchFeature;
import ch.cyberduck.core.features.Delete;
import ch.cyberduck.core.shared.DefaultFindFeature;
import ch.cyberduck.core.shared.DefaultTouchFeature;
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

import static org.junit.Assert.*;

@Category(IntegrationTest.class)
public class B2TouchFeatureTest extends AbstractB2Test {

    @Test
    public void testTouchEncrypted() throws Exception {
        final Path home = new Path("/test-cyberduck", EnumSet.of(Path.Type.directory, Path.Type.volume));
        final CryptoVault cryptomator = new CryptoVault(
            new Path(home, new AlphanumericRandomStringService().random(), EnumSet.of(Path.Type.directory)));
        final Path vault = cryptomator.create(session, null, new VaultCredentials("test"), new DisabledPasswordStore());
        session.withRegistry(new DefaultVaultRegistry(new DisabledPasswordStore(), new DisabledPasswordCallback(), cryptomator));
        final B2FileidProvider fileid = new B2FileidProvider(session).withCache(cache);
        final Path test = new CryptoTouchFeature<BaseB2Response>(session, new B2TouchFeature(session, fileid), new B2WriteFeature(session, fileid), cryptomator).touch(
                new Path(vault, UUID.randomUUID().toString(), EnumSet.of(Path.Type.file)), new TransferStatus());
        assertNotNull(test.attributes().getVersionId());
        assertTrue(new CryptoFindFeature(session, new B2FindFeature(session, fileid), cryptomator).find(test, new DisabledListProgressListener()));
        assertEquals(test.attributes(), new CryptoAttributesFeature(session, new B2AttributesFinderFeature(session, fileid), cryptomator).find(test, new DisabledListProgressListener()));
        new CryptoDeleteFeature(session, new B2DeleteFeature(session, fileid), cryptomator).delete(Arrays.asList(test, vault), new DisabledLoginCallback(), new Delete.DisabledCallback());
    }

    @Test
    public void testTouchLongFilenameEncrypted() throws Exception {
        final Path home = new Path("/test-cyberduck", EnumSet.of(Path.Type.directory, Path.Type.volume));
        final Path vault = new Path(home, new AlphanumericRandomStringService().random(), EnumSet.of(Path.Type.directory));
        final Path test = new Path(vault, new RandomStringGenerator.Builder().build().generate(130), EnumSet.of(Path.Type.file));
        final CryptoVault cryptomator = new CryptoVault(vault);
        cryptomator.create(session, null, new VaultCredentials("test"), new DisabledPasswordStore());
        session.withRegistry(new DefaultVaultRegistry(new DisabledPasswordStore(), new DisabledPasswordCallback(), cryptomator));
        final B2FileidProvider fileid = new B2FileidProvider(session).withCache(cache);
        new CryptoTouchFeature<BaseB2Response>(session, new B2TouchFeature(session, fileid), new B2WriteFeature(session, fileid), cryptomator).touch(test, new TransferStatus());
        assertTrue(new CryptoFindFeature(session, new B2FindFeature(session, fileid), cryptomator).find(test, new DisabledListProgressListener()));
        new CryptoDeleteFeature(session, new B2DeleteFeature(session, fileid), cryptomator).delete(Arrays.asList(test, vault), new DisabledLoginCallback(), new Delete.DisabledCallback());
        session.close();
    }

    @Test
    public void testTouchEncryptedDefaultFeature() throws Exception {
        final Path home = new Path("/test-cyberduck", EnumSet.of(Path.Type.directory, Path.Type.volume));
        final CryptoVault cryptomator = new CryptoVault(
            new Path(home, new AlphanumericRandomStringService().random(), EnumSet.of(Path.Type.directory)));
        final Path vault = cryptomator.create(session, null, new VaultCredentials("test"), new DisabledPasswordStore());
        session.withRegistry(new DefaultVaultRegistry(new DisabledPasswordStore(), new DisabledPasswordCallback(), cryptomator));
        final B2FileidProvider fileid = new B2FileidProvider(session).withCache(cache);
        final Path test = new CryptoTouchFeature<BaseB2Response>(session, new DefaultTouchFeature<BaseB2Response>(new B2SingleUploadService(new B2WriteFeature(session, fileid)),
            new B2AttributesFinderFeature(session, fileid)), new B2WriteFeature(session, fileid), cryptomator).touch(
                new Path(vault, UUID.randomUUID().toString(), EnumSet.of(Path.Type.file)), new TransferStatus());
        assertNotNull(test.attributes().getVersionId());
        assertTrue(new CryptoFindFeature(session, new DefaultFindFeature(session), cryptomator).find(test, new DisabledListProgressListener()));
        new CryptoDeleteFeature(session, new B2DeleteFeature(session, fileid), cryptomator).delete(Arrays.asList(test, vault), new DisabledLoginCallback(), new Delete.DisabledCallback());
    }
}
