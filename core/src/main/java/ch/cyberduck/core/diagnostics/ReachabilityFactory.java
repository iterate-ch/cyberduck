package ch.cyberduck.core.diagnostics;

/*
 * Copyright (c) 2002-2009 David Kocher. All rights reserved.
 *
 * http://cyberduck.ch/
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * Bug fixes, suggestions and comments should be sent to:
 * dkocher@cyberduck.ch
 */

import ch.cyberduck.core.Factory;
import ch.cyberduck.core.Host;

public class ReachabilityFactory extends Factory<Reachability> {

    private ReachabilityFactory() {
        super("factory.reachability.class");
    }

    public static Reachability get() {
        return new ProtocolAwareReachability(new ReachabilityFactory().create());
    }

    private static final class ProtocolAwareReachability implements Reachability {
        private final Reachability monitor;

        public ProtocolAwareReachability(final Reachability monitor) {
            this.monitor = monitor;
        }

        @Override
        public boolean isReachable(final Host bookmark) {
            switch(bookmark.getProtocol().getScheme()) {
                case file:
                    return new DiskReachability().isReachable(bookmark);
                case https:
                case http:
                    return new ChainedReachability(monitor, new ResolverReachability(), new HttpReachability()).isReachable(bookmark);
            }
            return new ChainedReachability(monitor, new ResolverReachability(), new TcpReachability()).isReachable(bookmark);
        }

        @Override
        public Monitor monitor(final Host bookmark, final Callback callback) {
            switch(bookmark.getProtocol().getScheme()) {
                case file:
                    return Monitor.disabled;
            }
            return monitor.monitor(bookmark, callback);
        }
    }
}
