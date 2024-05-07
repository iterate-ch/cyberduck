package ch.cyberduck.core.diagnostics;

/*
 * Copyright (c) 2002-2023 iterate GmbH. All rights reserved.
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

import ch.cyberduck.core.Host;
import ch.cyberduck.core.LocalFactory;
import ch.cyberduck.core.Path;
import ch.cyberduck.core.exception.BackgroundException;
import ch.cyberduck.core.exception.NotfoundException;
import ch.cyberduck.core.shared.DefaultPathHomeFeature;
import ch.cyberduck.core.shared.DelegatingHomeFeature;
import ch.cyberduck.core.shared.WorkdirHomeFeature;

public class DiskReachability implements Reachability {

    @Override
    public void test(final Host bookmark) throws BackgroundException {
        final Path home = new DelegatingHomeFeature(new WorkdirHomeFeature(bookmark), new DefaultPathHomeFeature(bookmark)).find();
        if(!LocalFactory.get(home.getAbsolute()).exists()) {
            throw new NotfoundException(home.getName());
        }
    }

    @Override
    public Monitor monitor(final Host bookmark, final Callback callback) {
        return Monitor.disabled;
    }
}
