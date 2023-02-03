package ch.cyberduck.core.diagnostics;

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

import ch.cyberduck.core.Host;

public class DisabledReachability implements Reachability {

    @Override
    public boolean isReachable(final Host bookmark) {
        return true;
    }

    @Override
    public Monitor monitor(final Host host, final Callback callback) {
        return new DisabledMonitor();
    }

    private static class DisabledMonitor implements Monitor {
        @Override
        public Monitor start() {
            return this;
        }

        @Override
        public Monitor stop() {
            return this;

        }
    }
}
