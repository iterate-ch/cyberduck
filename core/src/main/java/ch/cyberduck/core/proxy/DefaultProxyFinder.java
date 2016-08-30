package ch.cyberduck.core.proxy;

/*
 * Copyright (c) 2002-2015 David Kocher. All rights reserved.
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
 * Bug fixes, suggestions and comments should be sent to feedback@cyberduck.ch
 */

import ch.cyberduck.core.Host;
import ch.cyberduck.core.HostUrlProvider;
import ch.cyberduck.core.preferences.Preferences;
import ch.cyberduck.core.preferences.PreferencesFactory;

import java.net.InetSocketAddress;
import java.net.ProxySelector;
import java.net.URI;

public class DefaultProxyFinder implements ProxyFinder {

    private final ProxySelector selector
            = ProxySelector.getDefault();

    private Preferences preferences
            = PreferencesFactory.get();

    private HostUrlProvider provider
            = new ProxyHostUrlProvider();

    @Override
    public Proxy find(final Host target) {
        if(!preferences.getBoolean("connection.proxy.enable")) {
            return Proxy.DIRECT;
        }
        for(java.net.Proxy proxy : selector.select(URI.create(provider.get(target)))) {
            switch(proxy.type()) {
                case DIRECT: {
                    return Proxy.DIRECT;
                }
                case HTTP: {
                    if(proxy.address() instanceof InetSocketAddress) {
                        final InetSocketAddress address = (InetSocketAddress) proxy.address();
                        return new Proxy(Proxy.Type.HTTP, address.getHostName(), address.getPort());
                    }
                }
                case SOCKS: {
                    if(proxy.address() instanceof InetSocketAddress) {
                        final InetSocketAddress address = (InetSocketAddress) proxy.address();
                        return new Proxy(Proxy.Type.SOCKS, address.getHostName(), address.getPort());
                    }
                }
            }
        }
        return Proxy.DIRECT;
    }
}
