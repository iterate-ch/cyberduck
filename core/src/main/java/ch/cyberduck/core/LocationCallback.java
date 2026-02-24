package ch.cyberduck.core;

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

import ch.cyberduck.core.exception.ConnectionCanceledException;
import ch.cyberduck.core.features.Location;

import java.util.Set;

public interface LocationCallback {

    /**
     * Prompts the user to select a location name from the given list of available regions.
     *
     * @param bookmark      The host for which the selection is being made.
     * @param title         The title of the prompt.
     * @param message       The message to display in the prompt.
     * @param regions       The set of available regions to choose from.
     * @param defaultRegion The default region to preselect in the prompt.
     * @return The location name selected by the user from the provided regions.
     * @throws ConnectionCanceledException If the connection is canceled during the process.
     */
    Location.Name select(Host bookmark, String title, String message, Set<Location.Name> regions, Location.Name defaultRegion) throws ConnectionCanceledException;
}
