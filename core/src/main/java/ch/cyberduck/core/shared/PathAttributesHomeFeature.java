package ch.cyberduck.core.shared;/*
 * Copyright (c) 2002-2021 iterate GmbH. All rights reserved.
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
import ch.cyberduck.core.PathContainerService;
import ch.cyberduck.core.exception.BackgroundException;
import ch.cyberduck.core.features.AttributesFinder;
import ch.cyberduck.core.features.Home;

import java.util.EnumSet;

public class PathAttributesHomeFeature implements Home {

    private final Home proxy;
    private final AttributesFinder attributes;
    private final PathContainerService container;

    public PathAttributesHomeFeature(final Home proxy, final AttributesFinder attributes, final PathContainerService container) {
        this.proxy = proxy;
        this.attributes = attributes;
        this.container = container;
    }

    @Override
    public Path find() throws BackgroundException {
        final Path home = proxy.find();
        // Set correct type from protocol and current attributes from server
        return home.withAttributes(attributes.find(home)).withType(container.isContainer(home) ? EnumSet.of(Path.Type.volume, Path.Type.directory) : home.getType());
    }
}
