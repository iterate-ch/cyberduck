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

    public static native boolean isSOCKSProxyEnabled();

    public static native String getSOCKSProxyHost();

    public static native int getSOCKSProxyPort();

    public static native String getSOCKSProxyUser();

    /**
     * SOCKS port property name
     */
    final private static String SOCKS_PORT = "socksProxyPort";

    /**
     * SOCKS host property name
     */
    final private static String SOCKS_HOST = "socksProxyHost";

    /**
     * Set up SOCKS v4/v5 proxy settings. This can be used if there
     * is a SOCKS proxy server in place that must be connected thru.
     * Note that setting these properties directs <b>all</b> TCP
     * sockets in this JVM to the SOCKS proxy
     *
     * @param port SOCKS proxy port
     * @param host SOCKS proxy hostname
     */
    public static void initSOCKS(int port, String host) {
        Properties props = System.getProperties();
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
}