package ch.cyberduck.core.deepbox;

/*
 * Copyright (c) 2002-2024 iterate GmbH. All rights reserved.
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

import ch.cyberduck.core.Factory;
import ch.cyberduck.core.PathNormalizer;

import org.apache.commons.lang3.StringUtils;

/**
 * Normalize paths for Deepbox:
 * - replace forward slash "/" by dash "-" for Windows and ":" for macOS (shown as "/" in Finder)
 * Furthermore, duplicate file names are filtered out (after normalization) in the listing.
 * Finally, apart from forward slashes, the standard filtering is applied in Mountain Duck as described in <a href="https://docs.mountainduck.io/mountainduck/issues/#files-and-folders-not-synced-from-server">...</a>.
 */
public final class DeepboxPathNormalizer {

    private DeepboxPathNormalizer() {
    }

    public static String name(final String path) {
        return StringUtils.replace(PathNormalizer.normalize(path), "/",
                Factory.Platform.getDefault() == Factory.Platform.Name.windows ? "-" : ":");
    }
}
