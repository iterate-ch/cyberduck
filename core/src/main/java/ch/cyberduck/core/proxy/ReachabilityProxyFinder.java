package ch.cyberduck.core.proxy;

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

import ch.cyberduck.core.Host;
import ch.cyberduck.core.diagnostics.Reachability;
import ch.cyberduck.core.diagnostics.ReachabilityFactory;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.Closeable;
import java.util.concurrent.atomic.AtomicReference;

public class ReachabilityProxyFinder implements ProxyFinder, Reachability.Callback, Closeable {
    private static final Logger log = LogManager.getLogger(ReachabilityProxyFinder.class);

    private final ProxyFinder delegate;
    private final Reachability.Monitor monitor;
    private final AtomicReference<Proxy> proxy = new AtomicReference<>();

    public ReachabilityProxyFinder(final Host host, final ProxyFinder delegate) {
        this(host, delegate, ReachabilityFactory.get());
    }

    public ReachabilityProxyFinder(final Host host, final ProxyFinder delegate, final Reachability reachability) {
        this.delegate = delegate;
        this.monitor = reachability.monitor(host, this);
    }

    @Override
    public Proxy find(final String target) {
        final Proxy cached = proxy.get();
        if(null != cached) {
            return cached;
        }
        final Proxy value = delegate.find(target);
        log.debug("Cache proxy {} for target {}", value, target);
        proxy.compareAndSet(null, value);
        return proxy.get();
    }

    @Override
    public void change() {
        log.debug("Reset cached proxy configuration");
        proxy.set(null);
    }

    public void close() {
        log.debug("Stop monitor {}", monitor);
        monitor.stop();
    }
}
