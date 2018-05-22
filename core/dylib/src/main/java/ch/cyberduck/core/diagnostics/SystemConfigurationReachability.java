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

import ch.cyberduck.binding.Proxy;
import ch.cyberduck.binding.foundation.NSNotification;
import ch.cyberduck.binding.foundation.NSNotificationCenter;
import ch.cyberduck.core.Host;
import ch.cyberduck.core.HostUrlProvider;
import ch.cyberduck.core.idna.PunycodeConverter;
import ch.cyberduck.core.library.Native;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.rococoa.Foundation;

import java.util.HashSet;
import java.util.Set;

public final class SystemConfigurationReachability implements Reachability {
    private static final Logger log = Logger.getLogger(SystemConfigurationReachability.class);

    static {
        Native.load("core");
    }

    private final Set<UrlListener> listeners = new HashSet<>();
    private final NSNotificationCenter notificationCenter = NSNotificationCenter.defaultCenter();

    public SystemConfigurationReachability() {
        //
    }

    private final class UrlListener extends Proxy implements Callback {
        private final String url;
        private final Callback proxy;

        public UrlListener(final String url, final Callback proxy) {
            this.url = url;
            this.proxy = proxy;
        }

        public String getUrl() {
            return url;
        }

        @Override
        public void change() {
            proxy.change();
        }

        public void notify(final NSNotification notification) {
            if(log.isDebugEnabled()) {
                log.debug(String.format("Received notification %s", notification));
            }
            // Test if notification is matching hostname
            final String url = notification.object().toString();
            if(StringUtils.equals(this.url, url)) {
                this.change();
                notificationCenter.addObserver(this.id(), Foundation.selector("notify:"),
                    "kNetworkReachabilityChangedNotification", null);
                listeners.remove(this);
            }
        }
    }

    @Override
    public void monitor(final Host host, final Callback callback) {
        final String url = new HostUrlProvider().withUsername(false).get(host);
        final UrlListener listener = new UrlListener(url, callback);
        notificationCenter.addObserver(listener.id(), Foundation.selector("notify:"),
            "kNetworkReachabilityChangedNotification", null);
        listeners.add(listener);
        this.monitor(url);
    }

    private native boolean monitor(String url);

    @Override
    public boolean isReachable(final Host host) {
        return this.isReachable(this.toURL(host));
    }

    private String toURL(final Host host) {
        StringBuilder url = new StringBuilder(host.getProtocol().getScheme().toString());
        url.append("://");
        url.append(new PunycodeConverter().convert(host.getHostname()));
        url.append(":").append(host.getPort());
        return url.toString();
    }

    private native boolean isReachable(String url);

    /**
     * Opens the network configuration assistant for the URL denoting this host
     */
    @Override
    public void diagnose(final Host host) {
        this.diagnose(new HostUrlProvider().withUsername(false).get(host));
    }

    private native void diagnose(String url);
}
