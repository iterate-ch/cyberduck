package ch.cyberduck.core;

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

import org.apache.log4j.Logger;

/**
 * @version $Id$
 */
public class SystemConfigurationProxy extends AbstractProxy implements Proxy {
    private static Logger log = Logger.getLogger(SystemConfigurationProxy.class);

    public static void register() {
        ProxyFactory.addFactory(Factory.NATIVE_PLATFORM, new Factory());
    }

    private static class Factory extends ProxyFactory {
        @Override
        protected Proxy create() {
            return new SystemConfigurationProxy();
        }
    }

    private SystemConfigurationProxy() {
        Native.load("Proxy");
    }

    /**
     * Use passive connect mode
     *
     * @return True if enabled in the system preferences
     */
    public native boolean usePassiveFTP();

    /**
     * Check to see if the hostname is excluded from proxy settings
     *
     * @param hostname
     * @return True if excluded
     */
    public native boolean isHostExcluded(String hostname);

    /**
     * SOCKS proxy setting enabled
     *
     * @return
     */
    public native boolean isSOCKSProxyEnabled();

    /**
     * SOCKS proxy setting hostname
     *
     * @return
     */
    public native String getSOCKSProxyHost();

    /**
     * HTTP proxy setting port
     *
     * @return
     */
    public native int getSOCKSProxyPort();

    /**
     * HTTP proxy setting enabled
     *
     * @return
     */
    public native boolean isHTTPProxyEnabled();

    /**
     * HTTP proxy setting hostname
     *
     * @return
     */
    public native String getHTTPProxyHost();

    /**
     * HTTP proxy setting port
     *
     * @return
     */
    public native int getHTTPProxyPort();

    /**
     * HTTPS proxy setting enabled
     *
     * @return
     */
    public native boolean isHTTPSProxyEnabled();

    /**
     * HTTPS proxy setting hostname
     *
     * @return
     */
    public native String getHTTPSProxyHost();

    /**
     * HTTPS proxy setting port
     *
     * @return
     */
    public native int getHTTPSProxyPort();
}