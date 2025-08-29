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

import ch.cyberduck.core.Path;
import ch.cyberduck.core.Serializable;
import ch.cyberduck.core.serializer.Serializer;

import java.util.Objects;

public class VaultMetadata implements Serializable {

    public Path root;
    public Type type;

    public enum Type {
        V8, UVF
    }

    public VaultMetadata() {
    }

    public VaultMetadata(final Path path, final Type type) {
        this.root = path;
        this.type = type;
    }

    @Override
    public <T> T serialize(final Serializer<T> dict) {
        if(root != null) {
            dict.setObjectForKey(root, "Root");
        }
        if(type != null) {
            dict.setStringForKey(type.name(), "Type");
        }
        return dict.getSerialized();
    }

    @Override
    public final boolean equals(final Object o) {
        if(o == this) {
            return true;
        }
        if(!(o instanceof VaultMetadata)) {
            return false;
        }

        VaultMetadata that = (VaultMetadata) o;
        if(!Objects.equals(root, that.root)) {
            return false;
        }
        if(type != that.type) {
            return false;
        }
        return true;
    }

    @Override
    public int hashCode() {
        int result = Objects.hashCode(root);
        result = 31 * result + Objects.hashCode(type);
        return result;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("VaultMetadata{");
        sb.append("root=").append(root);
        sb.append(", type=").append(type);
        sb.append('}');
        return sb.toString();
    }
}

