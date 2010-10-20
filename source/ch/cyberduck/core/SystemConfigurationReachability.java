package ch.cyberduck.core;

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

/**
 * @version $Id$
 */
public class SystemConfigurationReachability implements Reachability {

    public static void register() {
        ReachabilityFactory.addFactory(Factory.NATIVE_PLATFORM, new Factory());
    }

    private static class Factory extends ReachabilityFactory {
        @Override
        protected Reachability create() {
            return new SystemConfigurationReachability();
        }
    }

    private SystemConfigurationReachability() {
        ;
    }

    private static boolean JNI_LOADED = false;

    /**
     * Load native library extensions
     *
     * @return
     */
    private static boolean loadNative() {
        if(!JNI_LOADED) {
            JNI_LOADED = Native.load("Diagnostics");
        }
        return JNI_LOADED;
    }

    public boolean isReachable(Host host) {
        if(!Preferences.instance().getBoolean("connection.hostname.check")) {
            return true;
        }
        if(!loadNative()) {
            return false;
        }
        return this.isReachable(this.toURL(host));
    }

    private String toURL(Host host) {
        StringBuilder url = new StringBuilder(host.getProtocol().getScheme());
        url.append("://");
        url.append(host.getHostname(true));
        url.append(":").append(host.getPort());
        return url.toString();
    }

    private native boolean isReachable(String url);

    /**
     * Opens the network configuration assistant for the URL denoting this host
     *
     * @see Host#toURL
     */
    public void diagnose(Host host) {
        if(!loadNative()) {
            return;
        }
        this.diagnose(host.toURL());
    }

    private native void diagnose(String url);
}
