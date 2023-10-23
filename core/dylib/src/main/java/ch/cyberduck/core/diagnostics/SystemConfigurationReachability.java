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
import ch.cyberduck.binding.foundation.NSObject;
import ch.cyberduck.core.Host;
import ch.cyberduck.core.HostnameConfiguratorFactory;
import ch.cyberduck.core.idna.PunycodeConverter;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.rococoa.Foundation;
import org.rococoa.ObjCClass;
import org.rococoa.Rococoa;

public class SystemConfigurationReachability implements Reachability {
    private static final Logger log = LogManager.getLogger(SystemConfigurationReachability.class);

    private final NSNotificationCenter notificationCenter = NSNotificationCenter.defaultCenter();

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
            private final SystemConfigurationReachability.Native monitor = SystemConfigurationReachability.Native.monitorForUrl(url);
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
        final SystemConfigurationReachability.Native monitor = SystemConfigurationReachability.Native.monitorForUrl(url);
        final int flags = monitor.getFlags();
        if(log.isDebugEnabled()) {
            log.debug(String.format("Determined reachability flags %s for %s", flags, url));
        }
        final boolean reachable = (flags & Native.kSCNetworkReachabilityFlagsReachable) == Native.kSCNetworkReachabilityFlagsReachable;
        final boolean connectionRequired = (flags & Native.kSCNetworkReachabilityFlagsConnectionRequired) == Native.kSCNetworkReachabilityFlagsConnectionRequired;
        return reachable && !connectionRequired;
    }

    protected static String toURL(final Host bookmark) {
        StringBuilder url = new StringBuilder(bookmark.getProtocol().getScheme().toString());
        url.append("://");
        url.append(new PunycodeConverter().convert(HostnameConfiguratorFactory.get(bookmark.getProtocol()).getHostname(bookmark.getHostname())));
        url.append(":").append(bookmark.getPort());
        return url.toString();
    }

    public abstract static class Native extends NSObject {
        static {
            ch.cyberduck.core.library.Native.load("core");
        }

        /**
         * The specified node name or address can be reached via a transient connection, such as PPP.
         */
        public static final int kSCNetworkReachabilityFlagsTransientConnection = 1 << 0;
        /**
         * The specified node name or address can be reached using the current network configuration.
         */
        public static final int kSCNetworkReachabilityFlagsReachable = 1 << 1;
        /**
         * The specified node name or address can be reached using the current network configuration, but a connection
         * must first be established. If this flag is set, the kSCNetworkReachabilityFlagsConnectionOnTraffic flag,
         * kSCNetworkReachabilityFlagsConnectionOnDemand flag, or kSCNetworkReachabilityFlagsIsWWAN flag is also
         * typically set to indicate the type of connection required. If the user must manually make the connection,
         * the kSCNetworkReachabilityFlagsInterventionRequired flag is also set.
         */
        public static final int kSCNetworkReachabilityFlagsConnectionRequired = 1 << 2;
        /**
         * The specified node name or address can be reached using the current network configuration, but a connection
         * must first be established. Any traffic directed to the specified name or address will initiate the connection.
         */
        public static final int kSCNetworkReachabilityFlagsConnectionOnTraffic = 1 << 3;
        /**
         * The specified node name or address can be reached using the current network configuration, but a connection must first be established.
         */
        public static final int kSCNetworkReachabilityFlagsInterventionRequired = 1 << 4;
        /**
         * The specified node name or address can be reached using the current network configuration, but a connection must first be established.
         */
        public static final int kSCNetworkReachabilityFlagsConnectionOnDemand = 1 << 5;
        /**
         * The specified node name or address is one that is associated with a network interface on the current system.
         */
        public static final int kSCNetworkReachabilityFlagsIsLocalAddress = 1 << 16;
        /**
         * Network traffic to the specified node name or address will not go through a gateway, but is routed directly to one of the interfaces in the system.
         */
        public static final int kSCNetworkReachabilityFlagsIsDirect = 1 << 17;
        /**
         * The specified node name or address can be reached via a cellular connection, such as EDGE or GPRS.
         */
        public static final int kSCNetworkReachabilityFlagsIsWWAN = 1 << 18;

        private static final _Class CLASS = Rococoa.createClass("SystemConfigurationReachability", _Class.class);

        public interface _Class extends ObjCClass {
            SystemConfigurationReachability.Native alloc();
        }

        public static Native monitorForUrl(final String url) {
            return CLASS.alloc().initWithUrl(url);
        }

        public abstract Native initWithUrl(String url);

        public abstract void diagnoseInteractively();

        public abstract boolean startReachabilityMonitor();

        public abstract boolean stopReachabilityMonitor();

        public abstract int getFlags();
    }
}
