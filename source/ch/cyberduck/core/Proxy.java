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

import com.apple.cocoa.foundation.NSBundle;

import org.apache.log4j.Logger;

import java.util.Properties;

/**
 * @version $Id$
 */
public class Proxy {
    private static Logger log = Logger.getLogger(Proxy.class);

    static {
        try {
            NSBundle bundle = NSBundle.mainBundle();
            String lib = bundle.resourcePath() + "/Java/" + "libProxy.dylib";
            log.info("Locating libProxy.dylib at '" + lib + "'");
            System.load(lib);
            log.info("libProxy.dylib loaded");
        }
        catch (UnsatisfiedLinkError e) {
            log.error("Could not load the libProxy.dylib library:" + e.getMessage());
            throw e;
        }
    }

    /**
     * SOCKS port property name
     */
    private static final String SOCKS_PORT = "socksProxyPort";

    /**
     * SOCKS host property name
     */
    private static final String SOCKS_HOST = "socksProxyHost";

    /**
     * Set up SOCKS v4/v5 proxy settings. This can be used if there
     * is a SOCKS proxy server in place that must be connected through.
     * Note that setting these properties directs <b>all</b> TCP
     * sockets in this JVM to the SOCKS proxy
     *
     * @param port SOCKS proxy port
     * @param host SOCKS proxy hostname
     */
    public static void initSOCKS(int port, String host) {
        Properties props = System.getProperties();
        // Indicates the name of the SOCKS proxy server and the port number
        // that will be used by the SOCKS protocol layer. If socksProxyHost
        // is specified then all TCP sockets will use the SOCKS proxy server
        // to establish a connection or accept one. The SOCKS proxy server
        // can either be a SOCKS v4 or v5 server and it has to allow for
        // unauthenticated connections.
        props.put(SOCKS_PORT, ""+port);
        props.put(SOCKS_HOST, host);
        System.setProperties(props);
    }

    /**
     * Clear SOCKS settings. Note that setting these properties affects
     * <b>all</b> TCP sockets in this JVM
     */
    public static void clearSOCKS() {
        Properties prop = System.getProperties();
        prop.remove(SOCKS_HOST);
        prop.remove(SOCKS_PORT);
        System.setProperties(prop);
    }

    /**
     * Use passive connect mode
     * @return True if enabled in the system preferences
     */
    public static native boolean usePassiveFTP();

    /**
     * Check to see if the hostname is excluded from proxy settings
     * @param hostname
     * @return True if excluded
     */
    public static native boolean isHostExcluded(String hostname);

    /**
     * SOCKS proxy setting enabled
     * @return
     */
    public static native boolean isSOCKSProxyEnabled();

    /**
     * SOCKS proxy setting hostname
     * @return
     */
    public static native String getSOCKSProxyHost();

    /**
     * HTTP proxy setting port
     * @return
     */
    public static native int getSOCKSProxyPort();

    /**
     * HTTP proxy setting enabled
     * @return
     */
    public static native boolean isHTTPProxyEnabled();

    /**
     * HTTP proxy setting hostname
     * @return
     */
    public static native String getHTTPProxyHost();

    /**
     * HTTP proxy setting port
     * @return
     */
    public static native int getHTTPProxyPort();

    /**
     * HTTPS proxy setting enabled
     * @return
     */
    public static native boolean isHTTPSProxyEnabled();

    /**
     * HTTPS proxy setting hostname
     * @return
     */
    public static native String getHTTPSProxyHost();

    /**
     * HTTPS proxy setting port
     * @return
     */
    public static native int getHTTPSProxyPort();
}