package ch.cyberduck.core.shared;

/*
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
import ch.cyberduck.core.Session;
import ch.cyberduck.core.exception.BackgroundException;
import ch.cyberduck.core.features.AttributesFinder;
import ch.cyberduck.core.features.Home;

public class DefaultHomeFinderService implements Home {

    private final Home chain;

    public DefaultHomeFinderService(final Session<?> session) {
        this.chain = new PathAttributesHomeFeature(session,
                // Chained implementation with precedence workdir > default path > remote default
                new DelegatingHomeFeature(
                        new WorkdirHomeFeature(session.getHost()),
                        new DefaultPathHomeFeature(session),
                        session.getFeature(Home.class)),
                session.getFeature(AttributesFinder.class), session.getFeature(PathContainerService.class));
    }

    @Override
    public Path find() throws BackgroundException {
        return chain.find();
    }
}
