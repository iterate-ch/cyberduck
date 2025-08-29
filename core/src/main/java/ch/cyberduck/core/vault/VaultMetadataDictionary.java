package ch.cyberduck.core.vault;

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

import ch.cyberduck.core.DeserializerFactory;
import ch.cyberduck.core.serializer.Deserializer;
import ch.cyberduck.core.serializer.PathDictionary;

public class VaultMetadataDictionary<T> {

    private final DeserializerFactory<T> factory;

    public VaultMetadataDictionary() {
        this.factory = new DeserializerFactory<>();
    }

    public VaultMetadataDictionary(final DeserializerFactory<T> factory) {
        this.factory = factory;
    }

    public VaultMetadata deserialize(final T serialized) {
        final Deserializer<T> dict = factory.create(serialized);
        final VaultMetadata vaultMetadata = new VaultMetadata();
        final T vaultObj = dict.objectForKey("Root");
        if(vaultObj != null) {
            vaultMetadata.root = new PathDictionary<>(factory).deserialize(vaultObj);
        }
        final String type = dict.stringForKey("Type");
        if(type != null) {
            vaultMetadata.type = VaultMetadata.Type.valueOf(type);
        }
        return vaultMetadata;
    }
}
