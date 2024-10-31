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

import org.apache.commons.lang3.StringUtils;

/**
 * Normalize paths for Deepbox
 */
public final class DeepboxPathNormalizer {

    private DeepboxPathNormalizer() {
    }

    /**
     * Replace forward slash "/" by dash "-" for Windows and ":" for macOS (shown as "/" in Finder)
     *
     * @return normalized path
     */
    public static String name(final String path) {
        return StringUtils.replaceChars(path, '/',
                Factory.Platform.getDefault() == Factory.Platform.Name.windows ? '-' : ':');
    }
}
