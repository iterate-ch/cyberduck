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
import ch.cyberduck.core.idna.PunycodeConverter;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.rococoa.Foundation;

public class SystemConfigurationReachability implements Reachability {
    private static final Logger log = LogManager.getLogger(SystemConfigurationReachability.class);

    private final NSNotificationCenter notificationCenter = NSNotificationCenter.defaultCenter();

    public SystemConfigurationReachability() {
        //
    }

    private static final class NotificationFilterCallback extends Proxy {
        private final Callback proxy;

        public NotificationFilterCallback(final Callback proxy) {
            this.proxy = proxy;
        }

        public void notify(final NSNotification notification) {
            if(log.isDebugEnabled()) {
                log.debug(String.format("Received notification %s", notification));
            }
            proxy.change();
        }
    }

    @Override
    public Monitor monitor(final Host bookmark, final Callback callback) {
        final String url = toURL(bookmark);
        return new Reachability.Monitor() {
            private final CDReachabilityMonitor monitor = CDReachabilityMonitor.monitorForUrl(url);
            private final NotificationFilterCallback listener = new NotificationFilterCallback(callback);

            @Override
            public Monitor start() {
                notificationCenter.addObserver(listener.id(), Foundation.selector("notify:"),
                        "kNetworkReachabilityChangedNotification", monitor.id());
                monitor.startReachabilityMonitor();
                return this;
            }

            @Override
            public Monitor stop() {
                monitor.stopReachabilityMonitor();
                notificationCenter.removeObserver(listener.id());
                return this;
            }
        };
    }

    @Override
    public boolean isReachable(final Host bookmark) {
        final String url = toURL(bookmark);
        final CDReachabilityMonitor monitor = CDReachabilityMonitor.monitorForUrl(url);
        final boolean status = monitor.isReachable();
        if(log.isDebugEnabled()) {
            log.debug(String.format("Determined reachability %s for %s", status, url));
        }
        return status;
    }

    protected static String toURL(final Host host) {
        StringBuilder url = new StringBuilder(host.getProtocol().getScheme().toString());
        url.append("://");
        url.append(new PunycodeConverter().convert(host.getHostname()));
        url.append(":").append(host.getPort());
        return url.toString();
    }
}
