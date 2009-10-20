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

    public void configure(final String hostname) {
        if(this.isSOCKSProxyEnabled() && !this.isHostExcluded(hostname)) {
            log.info("Using SOCKS Proxy");
            this.initSOCKS(this.getSOCKSProxyPort(), this.getSOCKSProxyHost());
        }
        else {
            this.clearSOCKS();
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
    private void initSOCKS(int port, String host) {
        if(-1 == port) {
            return;
        }
        Properties props = System.getProperties();
        // Indicates the name of the SOCKS proxy server and the port number
        // that will be used by the SOCKS protocol layer. If socksProxyHost
        // is specified then all TCP sockets will use the SOCKS proxy server
        // to establish a connection or accept one. The SOCKS proxy server
        // can either be a SOCKS v4 or v5 server and it has to allow for
        // unauthenticated connections.
        props.put(SOCKS_PORT, Integer.toString(port));
        props.put(SOCKS_HOST, host);
        System.setProperties(props);
    }

    /**
     * Clear SOCKS settings. Note that setting these properties affects
     * <b>all</b> TCP sockets in this JVM
     */
    private void clearSOCKS() {
        Properties prop = System.getProperties();
        prop.remove(SOCKS_HOST);
        prop.remove(SOCKS_PORT);
        System.setProperties(prop);
    }
}
