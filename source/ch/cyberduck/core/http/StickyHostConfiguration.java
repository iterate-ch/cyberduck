package ch.cyberduck.core.http;

/*
 *  Copyright (c) 2008 David Kocher. All rights reserved.
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

import org.apache.commons.httpclient.HostConfiguration;
import org.apache.commons.httpclient.HttpHost;
import org.apache.log4j.Logger;

/**
 * @version $Id$
 */
public class StickyHostConfiguration extends HostConfiguration {
    private static Logger log = Logger.getLogger(StickyHostConfiguration.class);

    public StickyHostConfiguration() {
        super();
    }

    private StickyHostConfiguration(HostConfiguration config) {
        super(config);
    }

    /**
     * @return
     */
    @Override
    public Object clone() {
        return new StickyHostConfiguration(this);
    }

    @Override
    public synchronized void setHost(final String host, int port, final String scheme) {
        this.setHost(new HttpHost(host, port, this.getCachedProtocol(scheme)));
    }

    /**
     * Select a Protocol to be used for the given host, port and scheme. The
     * current Protocol may be selected, if appropriate. This method need not be
     * thread-safe; the caller must synchronize if necessary.
     * <p/>
     * This implementation returns the current Protocol if it has the given
     * scheme; otherwise it returns the Protocol registered for that scheme.
     *
     * @param scheme
     * @return
     */
    protected org.apache.commons.httpclient.protocol.Protocol getCachedProtocol(String scheme) {
        final org.apache.commons.httpclient.protocol.Protocol oldProtocol = getProtocol();
        if(oldProtocol != null) {
            if(oldProtocol.getScheme().equalsIgnoreCase(scheme)) {
                // The old {rotocol has the desired scheme.
                return oldProtocol; // Retain it.
            }
        }
        log.warn("No cached protocol for:" + scheme);
        return org.apache.commons.httpclient.protocol.Protocol.getProtocol(scheme);
    }
}