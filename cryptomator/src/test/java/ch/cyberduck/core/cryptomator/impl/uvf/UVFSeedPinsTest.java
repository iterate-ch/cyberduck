package ch.cyberduck.core.cryptomator.impl.uvf;

/*
 * Copyright (c) 2002-2026 iterate GmbH. All rights reserved.
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

import ch.cyberduck.core.Credentials;
import ch.cyberduck.core.DisabledPasswordStore;
import ch.cyberduck.core.Host;
import ch.cyberduck.core.Path;
import ch.cyberduck.core.TestProtocol;

import org.junit.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.EnumSet;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import static org.junit.Assert.*;

public class UVFSeedPinsTest {

    private static final Host BOOKMARK = new Host(new TestProtocol(), "h", new Credentials("u"));
    private static final Path HOME = new Path("/vault", EnumSet.of(Path.Type.directory));

    @Test
    public void testNoPins() throws Exception {
        assertTrue(new UVFSeedPins(new InMemoryPasswordStore()).get(BOOKMARK, HOME).isEmpty());
    }

    @Test
    public void testAddKeepsNewerPins() throws Exception {
        final UVFSeedPins pins = new UVFSeedPins(new InMemoryPasswordStore());
        // Unlock of current metadata finishes first
        pins.add(BOOKMARK, HOME, Arrays.asList(digest(1), digest(2)));
        // Concurrent unlock of outdated metadata finishes later
        pins.add(BOOKMARK, HOME, Collections.singletonList(digest(1)));
        assertDigests(pins.get(BOOKMARK, HOME), digest(1), digest(2));
    }

    @Test
    public void testPersisted() throws Exception {
        final InMemoryPasswordStore keychain = new InMemoryPasswordStore();
        new UVFSeedPins(keychain).add(BOOKMARK, HOME, Collections.singletonList(digest(1)));
        // After restart
        assertDigests(new UVFSeedPins(keychain).get(BOOKMARK, HOME), digest(1));
        assertEquals(1, keychain.passwords.size());
        assertTrue(keychain.passwords.keySet().iterator().next().startsWith("Cryptomator Seed Pins (u)"));
    }

    @Test
    public void testSeparateVaults() throws Exception {
        final UVFSeedPins pins = new UVFSeedPins(new InMemoryPasswordStore());
        pins.add(BOOKMARK, HOME, Collections.singletonList(digest(1)));
        assertTrue(pins.get(BOOKMARK, new Path("/other", EnumSet.of(Path.Type.directory))).isEmpty());
        assertTrue(pins.get(new Host(new TestProtocol(), "h", new Credentials("other")), HOME).isEmpty());
        assertTrue(pins.get(new Host(new TestProtocol(), "other", new Credentials("u")), HOME).isEmpty());
    }

    @Test
    public void testIgnoreInvalidPins() throws Exception {
        final InMemoryPasswordStore keychain = new InMemoryPasswordStore();
        final UVFSeedPins pins = new UVFSeedPins(keychain);
        pins.add(BOOKMARK, HOME, Collections.singletonList(digest(1)));
        final String key = keychain.passwords.keySet().iterator().next();
        keychain.passwords.put(key, keychain.passwords.get(key) + ",invalid,AAAA");
        assertDigests(pins.get(BOOKMARK, HOME), digest(1));
    }

    @Test
    public void testConcurrentAdd() throws Exception {
        final UVFSeedPins pins = new UVFSeedPins(new InMemoryPasswordStore());
        final ExecutorService executor = Executors.newFixedThreadPool(8);
        try {
            final List<Future<?>> futures = new ArrayList<>();
            for(int i = 0; i < 50; i++) {
                final byte[] digest = digest(i);
                futures.add(executor.submit(() -> {
                    pins.add(BOOKMARK, HOME, Collections.singletonList(digest));
                    return null;
                }));
            }
            for(Future<?> future : futures) {
                future.get();
            }
        }
        finally {
            executor.shutdownNow();
        }
        assertEquals(50, pins.get(BOOKMARK, HOME).size());
    }

    private static byte[] digest(final int value) {
        final byte[] digest = new byte[32];
        digest[0] = (byte) value;
        return digest;
    }

    private static void assertDigests(final Collection<byte[]> actual, final byte[]... expected) {
        assertEquals(expected.length, actual.size());
        for(byte[] digest : expected) {
            assertTrue(actual.stream().anyMatch(pin -> Arrays.equals(digest, pin)));
        }
    }

    /**
     * Keychain in memory. Reading and saving pins are separate calls as with the system keychain.
     */
    static final class InMemoryPasswordStore extends DisabledPasswordStore {
        final Map<String, String> passwords = new ConcurrentHashMap<>();

        @Override
        public String getPassword(final String serviceName, final String accountName) {
            return passwords.get(String.format("%s/%s", serviceName, accountName));
        }

        @Override
        public void addPassword(final String serviceName, final String accountName, final String password) {
            passwords.put(String.format("%s/%s", serviceName, accountName), password);
        }
    }
}
