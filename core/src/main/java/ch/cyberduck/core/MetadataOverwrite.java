package ch.cyberduck.core;

/*
 * Copyright (c) 2002-2016 iterate GmbH. All rights reserved.
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

import java.util.Map;
import java.util.Objects;

public class MetadataOverwrite {
    /**
     * Stores original key/values per path. Used in WriteMetadataWorker.
     * DO NOT CHANGE
     */
    public final Map<Path, Map<String, String>> original;
    /**
     * Used as replacement for previous Map&lt;String, String&gt; used in Read-/WriteMetadataWorker.
     */
    public final Map<String, String> metadata;

    public MetadataOverwrite(Map<Path, Map<String, String>> original, Map<String, String> updated) {
        this.original = original;
        this.metadata = updated;
    }

    @Override
    public boolean equals(final Object o) {
        if(this == o) {
            return true;
        }
        if(o == null || getClass() != o.getClass()) {
            return false;
        }
        final MetadataOverwrite that = (MetadataOverwrite) o;
        return Objects.equals(metadata, that.metadata);
    }

    @Override
    public int hashCode() {
        return Objects.hash(metadata);
    }
}
