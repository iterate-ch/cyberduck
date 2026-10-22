package ch.cyberduck.core.cryptomator.features;

/*
 * Copyright (c) 2002-2026 iterate GmbH. All rights reserved.
 * https://cyberduck.io/
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 */

import ch.cyberduck.core.AttributedList;
import ch.cyberduck.core.CachingAttributesFinderFeature;
import ch.cyberduck.core.CachingFindFeature;
import ch.cyberduck.core.ConnectionCallback;
import ch.cyberduck.core.DisabledPasswordCallback;
import ch.cyberduck.core.Host;
import ch.cyberduck.core.ListProgressListener;
import ch.cyberduck.core.NullSession;
import ch.cyberduck.core.Path;
import ch.cyberduck.core.PathCache;
import ch.cyberduck.core.TestProtocol;
import ch.cyberduck.core.cryptomator.impl.v8.CryptomatorVault;
import ch.cyberduck.core.cryptomator.impl.v8.CryptomatorVaultTest;
import ch.cyberduck.core.cryptomator.impl.v8.MasterkeyVaultMetadataProvider;
import ch.cyberduck.core.exception.BackgroundException;
import ch.cyberduck.core.exception.NotfoundException;
import ch.cyberduck.core.features.Read;
import ch.cyberduck.core.features.Vault;
import ch.cyberduck.core.transfer.TransferStatus;
import ch.cyberduck.core.vault.DefaultVaultRegistry;
import ch.cyberduck.core.vault.VaultCredentials;

import org.apache.commons.io.IOUtils;
import org.cryptomator.cryptolib.api.CryptorProvider;
import org.junit.Test;

import java.io.InputStream;
import java.nio.charset.Charset;
import java.util.Arrays;
import java.util.EnumSet;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

/**
 * Lookup in an unlocked vault using the cache shared with the transfer. The directory contents are listed encrypted
 * by the proxy of the crypto features and must be cached decrypted for the cleartext path only.
 */
public class CryptoFindFeatureTest {

    private static final String MASTERKEY = "{\n" +
            "  \"version\": 8,\n" +
            "  \"scryptSalt\": \"RVAAirkArDU=\",\n" +
            "  \"scryptCostParam\": 32768,\n" +
            "  \"scryptBlockSize\": 8,\n" +
            "  \"primaryMasterKey\": \"+03NkJNWVsJ9Tb1CTpKhXyfINzjDirFFI+iJLOWIOySyxB+abpx34Q==\",\n" +
            "  \"hmacMasterKey\": \"aMoDtn7Y6kIXxyHo2zl47p5jCYTlRnfx3l3AMgULmIDSYAxVAraSgg==\",\n" +
            "  \"versionMac\": \"FzirA8UhwCmS5RsC4JvxbO+ZBxaCbIkzqD2Ocagd+A8=\"\n" +
            "}";

    private static final Path home = new Path("/", EnumSet.of(Path.Type.directory));
    private static final Path first = new Path(home, "first", EnumSet.of(Path.Type.file));
    private static final Path second = new Path(home, "second", EnumSet.of(Path.Type.file));

    /**
     * Server with the encrypted contents of the vault root listed in a single directory
     */
    private static final class VaultSession extends NullSession {
        final AtomicInteger lists = new AtomicInteger();
        private Path[] encrypted = new Path[0];

        VaultSession() {
            super(new Host(new TestProtocol()));
        }

        VaultSession withEncrypted(final Path... encrypted) {
            this.encrypted = encrypted;
            return this;
        }

        @Override
        public AttributedList<Path> list(final Path folder, final ListProgressListener listener) throws BackgroundException {
            lists.incrementAndGet();
            if(encrypted.length > 0 && folder.getAbsolute().equals(encrypted[0].getParent().getAbsolute())) {
                final AttributedList<Path> list = new AttributedList<>();
                for(Path f : encrypted) {
                    list.add(new Path(f.getParent(), f.getName(), EnumSet.of(Path.Type.file)));
                }
                listener.chunk(folder, list);
                return list;
            }
            throw new NotfoundException(folder.getAbsolute());
        }

        @Override
        @SuppressWarnings("unchecked")
        public <T> T _getFeature(final Class<T> type) {
            if(type == Read.class) {
                return (T) new Read() {
                    @Override
                    public InputStream read(final Path file, final TransferStatus status, final ConnectionCallback callback) throws BackgroundException {
                        if("masterkey.cryptomator".equals(file.getName())) {
                            return IOUtils.toInputStream(MASTERKEY, Charset.defaultCharset());
                        }
                        if("vault.cryptomator".equals(file.getName())) {
                            return IOUtils.toInputStream(CryptomatorVaultTest.createJWT(MASTERKEY, CryptomatorVault.VAULT_VERSION, CryptorProvider.Scheme.SIV_GCM, "vault123"), Charset.defaultCharset());
                        }
                        throw new NotfoundException(file.getName());
                    }

                    @Override
                    public boolean offset(final Path file) {
                        return false;
                    }
                };
            }
            return super._getFeature(type);
        }
    }

    private static VaultSession unlocked() throws BackgroundException {
        final VaultSession session = new VaultSession();
        final CryptomatorVault vault = new CryptomatorVault(home);
        vault.load(session, new MasterkeyVaultMetadataProvider(new VaultCredentials("vault123")));
        assertEquals(Vault.State.open, vault.getState());
        session.withEncrypted(vault.encrypt(session, first, true), vault.encrypt(session, second, true));
        session.withRegistry(new DefaultVaultRegistry(new DisabledPasswordCallback(), vault));
        return session;
    }

    @Test
    public void testFindInUnlockedVaultListsDirectoryOnce() throws Exception {
        final VaultSession session = unlocked();
        final PathCache cache = new PathCache(10);
        final CachingFindFeature find = new CachingFindFeature(session, cache);
        assertTrue(find.find(first));
        assertEquals(1, session.lists.get());
        // Existing file must be found from cached cleartext contents of the directory
        assertTrue(find.find(second));
        assertTrue(find.find(first));
        assertEquals(1, session.lists.get());
        // Cleartext contents are cached for the vault root
        assertTrue(cache.isCached(home));
        assertEquals(Arrays.asList(first.getName(), second.getName()),
                Arrays.asList(cache.get(home).get(0).getName(), cache.get(home).get(1).getName()));
    }

    @Test
    public void testAttributesInUnlockedVaultListsDirectoryOnce() throws Exception {
        final VaultSession session = unlocked();
        final PathCache cache = new PathCache(10);
        final CachingAttributesFinderFeature attributes = new CachingAttributesFinderFeature(session, cache);
        assertNotNull(attributes.find(first));
        assertEquals(1, session.lists.get());
        // Repeated lookups must be served from cached cleartext contents of the directory
        assertNotNull(attributes.find(second));
        assertNotNull(attributes.find(first));
        assertEquals(1, session.lists.get());
        assertTrue(cache.isCached(home));
        assertEquals(2, cache.get(home).size());
    }
}
