package ch.cyberduck.core.preferences;

/*
 * Copyright (c) 2002-2018 iterate GmbH. All rights reserved.
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

import java.util.HashMap;
import java.util.Map;

public abstract class DefaultPreferences extends Preferences {

    private final Map<String, String> defaults = new HashMap<>();

    /**
     * Default value for a given property.
     *
     * @param property The property to query.
     * @return A default value if any or null if not found.
     */
    @Override
    public String getDefault(final String property) {
        return defaults.get(property);
    }

    @Override
    public void setDefault(final String property, final String value) {
        defaults.put(property, value);
    }
}
