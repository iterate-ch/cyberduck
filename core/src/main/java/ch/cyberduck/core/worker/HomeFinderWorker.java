package ch.cyberduck.core.worker;

/*
 * Copyright (c) 2002-2019 iterate GmbH. All rights reserved.
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
import ch.cyberduck.core.Session;
import ch.cyberduck.core.exception.BackgroundException;
import ch.cyberduck.core.features.Home;

public class HomeFinderWorker extends Worker<Path> {

    @Override
    public Path run(final Session<?> session) throws BackgroundException {
        return session.getFeature(Home.class).find();
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("HomeFinderWorker{");
        sb.append('}');
        return sb.toString();
    }
}
