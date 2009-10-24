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

import org.apache.log4j.Logger;

import java.util.Properties;

/**
 * @version $Id$
 */
public abstract class AbstractProxy implements Proxy {
    private static Logger log = Logger.getLogger(AbstractProxy.class);

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
     */
    public void configure(final Host host) {
        Properties properties = System.getProperties();
        if(this.isSOCKSProxyEnabled() && !this.isHostExcluded(host.getHostname())) {
            log.info("Using SOCKS Proxy");
            if(-1 == host.getPort()) {
                return;
            }
            // Indicates the name of the SOCKS proxy server and the port number
            // that will be used by the SOCKS protocol layer. If socksProxyHost
            // is specified then all TCP sockets will use the SOCKS proxy server
            // to establish a connection or accept one. The SOCKS proxy server
            // can either be a SOCKS v4 or v5 server and it has to allow for
            // unauthenticated connections.
            properties.put(SOCKS_PORT, Integer.toString(host.getPort()));
            properties.put(SOCKS_HOST, host);
        }
        else {
            properties.remove(SOCKS_HOST);
            properties.remove(SOCKS_PORT);
        }
        System.setProperties(properties);
    }
}
