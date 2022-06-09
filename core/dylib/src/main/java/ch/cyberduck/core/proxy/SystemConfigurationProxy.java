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

import ch.cyberduck.binding.foundation.NSAppleScript;
import ch.cyberduck.core.library.Native;

import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.net.URI;
import java.net.URISyntaxException;

public final class SystemConfigurationProxy extends AbstractProxyFinder implements ProxyFinder {
    private static final Logger log = LogManager.getLogger(SystemConfigurationProxy.class);

    static {
        Native.load("core");
    }

    @Override
    public void configure() {
        final String script = "tell application \"System Preferences\"\n" +
                "activate\n" +
                "reveal anchor \"Proxies\" of pane \"com.apple.preference.network\"\n" +
                "end tell";
        final NSAppleScript open = NSAppleScript.createWithSource(script);
        open.executeAndReturnError(null);
    }

    @Override
    public Proxy find(final String target) {
        final String route = this.findNative(target);
        if(null == route) {
            if(log.isInfoEnabled()) {
                log.info(String.format("No proxy configuration found for target %s", target));
            }
            // Direct
            return Proxy.DIRECT;
        }
        final URI proxy;
        try {
            proxy = new URI(route);
            try {
                // User info is never populated. Would have to lookup in keychain but we are unaware of the username
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
