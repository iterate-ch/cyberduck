package ch.cyberduck.core.openstack;

/*
 * Copyright (c) 2002-2020 iterate GmbH. All rights reserved.
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
import ch.cyberduck.core.features.Copy;

public abstract class SwiftCopy implements Copy {
    private final SwiftSession session;

    private final PathContainerService containerService
        = new PathContainerService();

    private final SwiftRegionService regionService;

    public SwiftCopy(final SwiftSession session) {
        this(session, new SwiftRegionService(session));
    }

    public SwiftCopy(final SwiftSession session, final SwiftRegionService regionService) {
        this.session = session;
        this.regionService = regionService;
    }

    protected PathContainerService containerService() {
        return containerService;
    }

    protected SwiftRegionService regionService() {
        return regionService;
    }

    protected SwiftSession session() {
        return session;
    }

    @Override
    public boolean isRecursive(final Path source, final Path target) {
        return false;
    }

    @Override
    public boolean isSupported(final Path source, final Path target) {
        return !containerService.isContainer(source) && !containerService.isContainer(target);
    }
}
