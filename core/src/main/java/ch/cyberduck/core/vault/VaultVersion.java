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

import ch.cyberduck.core.Serializable;
import ch.cyberduck.core.serializer.Serializer;

import java.util.Objects;

public class VaultVersion implements Serializable {

    public Type type;

    public enum Type {
        V8, UVF
    }

    public VaultVersion() {
    }

    public VaultVersion(final Type type) {
        this.type = type;
    }

    @Override
    public <T> T serialize(final Serializer<T> dict) {
        if(type != null) {
            dict.setStringForKey(type.name(), "Type");
        }
        return dict.getSerialized();
    }

    @Override
    public final boolean equals(final Object o) {
        if(!(o instanceof VaultVersion)) {
            return false;
        }

        VaultVersion that = (VaultVersion) o;
        return type == that.type;
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(type);
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("VaultVersion{");
        sb.append("type=").append(type);
        sb.append('}');
        return sb.toString();
    }
}

