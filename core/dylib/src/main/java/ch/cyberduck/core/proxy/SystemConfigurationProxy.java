package ch.cyberduck.core.proxy;

/*
 *  Copyright (c) 2005 David Kocher. All rights reserved.
 *  http://cyberduck.ch/
 *
 *  This program is free software; you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation; either version 2 of the License, or
 *  (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  Bug fixes, suggestions and comments should be sent to:
 *  dkocher@cyberduck.ch
 */

import ch.cyberduck.core.Host;
import ch.cyberduck.core.HostUrlProvider;
import ch.cyberduck.core.library.Native;
import ch.cyberduck.core.preferences.Preferences;
import ch.cyberduck.core.preferences.PreferencesFactory;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;

import java.net.URI;
import java.net.URISyntaxException;

public final class SystemConfigurationProxy extends AbstractProxyFinder implements ProxyFinder {
    private static final Logger log = Logger.getLogger(SystemConfigurationProxy.class);

    static {
        Native.load("core");
    }

    private final HostUrlProvider provider
            = new ProxyHostUrlProvider();

    private final Preferences preferences
            = PreferencesFactory.get();

    @Override
    public Proxy find(final Host target) {
        if(!preferences.getBoolean("connection.proxy.enable")) {
            return Proxy.DIRECT;
        }
        final String route = this.findNative(provider.get(target));
        if(null == route) {
            if(log.isInfoEnabled()) {
                log.info(String.format("No poxy configuration found for target %s", target));
            }
            // Direct
            return Proxy.DIRECT;
        }
        final URI proxy;
        try {
            proxy = new URI(route);
            try {
                return new Proxy(Proxy.Type.valueOf(StringUtils.upperCase(proxy.getScheme())),
                        proxy.getHost(), proxy.getPort());
            }
            catch(IllegalArgumentException e) {
                log.warn(String.format("Unsupported scheme for proxy %s", proxy));
            }
        }
        catch(URISyntaxException e) {
            log.warn(String.format("Invalid proxy configuration %s", route));
        }
        return Proxy.DIRECT;
    }

    /**
     * Find SOCKS and HTTP proxy settings
     *
     * @param target The URL the application intends to access
     * @return Proxy URL or null if direct connection
     */
    public native String findNative(String target);
}
