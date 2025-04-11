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
import ch.cyberduck.core.HostnameConfigurator;
import ch.cyberduck.core.Resolver;
import ch.cyberduck.core.exception.BackgroundException;
import ch.cyberduck.core.proxy.Proxy;
import ch.cyberduck.core.proxy.ProxyFactory;
import ch.cyberduck.core.proxy.ProxyFinder;
import ch.cyberduck.core.proxy.ProxyHostUrlProvider;
import ch.cyberduck.core.threading.CancelCallback;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class ResolverReachability extends DisabledReachability {
    private static final Logger log = LogManager.getLogger(ResolverReachability.class);

    private final ProxyFinder proxy;
    private final Resolver resolver = new Resolver();

    public ResolverReachability() {
        this(ProxyFactory.get());
    }

    public ResolverReachability(final ProxyFinder proxy) {
        this.proxy = proxy;
    }

    @Override
    public void test(final Host bookmark) throws BackgroundException {
        final HostnameConfigurator configurator = bookmark.getProtocol().getFeature(HostnameConfigurator.class);
        final String hostname = configurator.getHostname(bookmark.getHostname());
        if(proxy.find(new ProxyHostUrlProvider().get(bookmark)) == Proxy.DIRECT) {
            // Only try to resolve target hostname if direct connection
            resolver.resolve(hostname, CancelCallback.noop);
        }
        else {
            log.warn("Skip attempt to resolve hostname {} for {} with proxy configuration found", hostname, bookmark);
        }
    }
}
