package ch.cyberduck.core.vault.registry;

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
import ch.cyberduck.core.CachingFindFeature;
import ch.cyberduck.core.DisabledPasswordCallback;
import ch.cyberduck.core.Host;
import ch.cyberduck.core.ListProgressListener;
import ch.cyberduck.core.NullSession;
import ch.cyberduck.core.Path;
import ch.cyberduck.core.PathCache;
import ch.cyberduck.core.TestProtocol;
import ch.cyberduck.core.exception.BackgroundException;
import ch.cyberduck.core.exception.NotfoundException;
import ch.cyberduck.core.features.Find;
import ch.cyberduck.core.features.Vault;
import ch.cyberduck.core.shared.DefaultFindFeature;
import ch.cyberduck.core.vault.DefaultVaultRegistry;
import ch.cyberduck.core.vault.DisabledVaultProvider;
import ch.cyberduck.core.vault.VaultProvider;
import ch.cyberduck.core.vault.VaultRegistry;
import ch.cyberduck.core.vault.VaultVersion;

import org.junit.Test;

import java.lang.reflect.Proxy;
import java.util.Collections;
import java.util.EnumSet;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class VaultRegistryFindFeatureTest {

    @Test
    public void testListParentDirectoryOnceWithAutodetect() throws Exception {
        final AtomicInteger lists = new AtomicInteger();
        final Host host = new Host(new TestProtocol());
        final NullSession session = new NullSession(host) {
            @Override
            public AttributedList<Path> list(final Path folder, final ListProgressListener listener) {
                lists.incrementAndGet();
                return new AttributedList<>(Collections.singletonList(new Path(folder, "other", EnumSet.of(Path.Type.file))));
            }

            @Override
            @SuppressWarnings("unchecked")
            public <T> T _getFeature(final Class<T> type) {
                if(type == VaultProvider.class) {
                    return (T) new DisabledVaultProvider() {
                        @Override
                        public VaultVersion find(final Path directory, final Find find, final ListProgressListener listener) throws BackgroundException {
                            // Look up multiple vault markers
                            find.find(new Path(directory, "masterkey.cryptomator", EnumSet.of(Path.Type.file)), listener);
                            find.find(new Path(directory, "vault.uvf", EnumSet.of(Path.Type.file)), listener);
                            return null;
                        }
                    };
                }
                return super._getFeature(type);
            }
        };
        session.withRegistry(new DefaultVaultRegistry(new DisabledPasswordCallback()));
        final Find find = new CachingFindFeature(session, new PathCache(10));
        assertFalse(find.find(new Path("/d/f", EnumSet.of(Path.Type.file))));
        assertEquals(1, lists.get());
        // Subsequent lookup in same directory served from cache
        assertFalse(find.find(new Path("/d/g", EnumSet.of(Path.Type.file))));
        assertEquals(1, lists.get());
    }

    @Test
    public void testVaultFoundDoesNotCacheEncryptedContents() throws Exception {
        final AtomicInteger lists = new AtomicInteger();
        final NullSession session = new NullSession(new Host(new TestProtocol())) {
            @Override
            public AttributedList<Path> list(final Path folder, final ListProgressListener listener) {
                lists.incrementAndGet();
                return new AttributedList<>(Collections.singletonList(new Path(folder, "masterkey.cryptomator", EnumSet.of(Path.Type.file))));
            }

            @Override
            @SuppressWarnings("unchecked")
            public <T> T _getFeature(final Class<T> type) {
                if(type == VaultProvider.class) {
                    return (T) new DisabledVaultProvider() {
                        @Override
                        public VaultVersion find(final Path directory, final Find find, final ListProgressListener listener) throws BackgroundException {
                            return find.find(new Path(directory, "masterkey.cryptomator", EnumSet.of(Path.Type.file)), listener) ?
                                    new VaultVersion(VaultVersion.Type.V8) : null;
                        }
                    };
                }
                return super._getFeature(type);
            }
        };
        // Lookup in vault does not notify listener with decrypted contents
        final Vault vault = (Vault) Proxy.newProxyInstance(Vault.class.getClassLoader(), new Class<?>[]{Vault.class}, (proxy, method, args) -> {
            if("getFeature".equals(method.getName())) {
                return (Find) (file, listener) -> true;
            }
            if("equals".equals(method.getName())) {
                return proxy == args[0];
            }
            if("hashCode".equals(method.getName())) {
                return System.identityHashCode(proxy);
            }
            throw new UnsupportedOperationException(method.getName());
        });
        final PathCache cache = new PathCache(10);
        final Path directory = new Path("/d", EnumSet.of(Path.Type.directory));
        final Find find = new CachingFindFeature(session.getCaseSensitivity(), cache,
                new VaultRegistryFindFeature(session, new DefaultFindFeature(session), VaultRegistry.DISABLED, (s, d, v) -> vault));
        assertTrue(find.find(new Path(directory, "f", EnumSet.of(Path.Type.file))));
        assertEquals(1, lists.get());
        // Contents of directory with vault are encrypted and not found by cleartext name
        assertFalse(cache.isCached(directory));
    }

    @Test
    public void testListMissingParentDirectoryOnceWithAutodetect() throws Exception {
        final AtomicInteger lists = new AtomicInteger();
        final NullSession session = new NullSession(new Host(new TestProtocol())) {
            @Override
            public AttributedList<Path> list(final Path folder, final ListProgressListener listener) throws BackgroundException {
                lists.incrementAndGet();
                throw new NotfoundException(folder.getAbsolute());
            }

            @Override
            @SuppressWarnings("unchecked")
            public <T> T _getFeature(final Class<T> type) {
                if(type == VaultProvider.class) {
                    return (T) new DisabledVaultProvider() {
                        @Override
                        public VaultVersion find(final Path directory, final Find find, final ListProgressListener listener) throws BackgroundException {
                            // Look up multiple vault markers
                            find.find(new Path(directory, "masterkey.cryptomator", EnumSet.of(Path.Type.file)), listener);
                            find.find(new Path(directory, "vault.uvf", EnumSet.of(Path.Type.file)), listener);
                            return null;
                        }
                    };
                }
                return super._getFeature(type);
            }
        };
        session.withRegistry(new DefaultVaultRegistry(new DisabledPasswordCallback()));
        final PathCache cache = new PathCache(10);
        final Find find = new CachingFindFeature(session, cache);
        final Path directory = new Path("/new", EnumSet.of(Path.Type.directory));
        assertFalse(find.find(new Path(directory, "f", EnumSet.of(Path.Type.file))));
        assertEquals(1, lists.get());
        assertTrue(cache.isCached(directory));
        // Subsequent lookup in same missing directory served from cache
        assertFalse(find.find(new Path(directory, "g", EnumSet.of(Path.Type.file))));
        assertEquals(1, lists.get());
    }
}
