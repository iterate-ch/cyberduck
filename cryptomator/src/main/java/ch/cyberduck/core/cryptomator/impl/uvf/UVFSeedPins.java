package ch.cyberduck.core.cryptomator.impl.uvf;

/*
 * Copyright (c) 2002-2025 iterate GmbH. All rights reserved.
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

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Seed digests observed for vaults to reject outdated or forged vault metadata.
 * TODO Spike only. Must be persisted integrity protected outside of the vault storage
 */
public final class UVFSeedPins {

    private static final Map<String, List<byte[]>> pins = new ConcurrentHashMap<>();

    private UVFSeedPins() {
    }

    /**
     * @param vault Identifier of vault
     * @return Seed digests that must be present in vault metadata
     */
    public static Collection<byte[]> get(final String vault) {
        return Collections.unmodifiableList(pins.getOrDefault(vault, Collections.emptyList()));
    }

    /**
     * @param vault   Identifier of vault
     * @param digests All seed digests of successfully validated vault metadata
     */
    public static void put(final String vault, final Collection<byte[]> digests) {
        pins.put(vault, new ArrayList<>(digests));
    }
}
