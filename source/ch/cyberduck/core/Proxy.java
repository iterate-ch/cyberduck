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
public interface Proxy {

    /**
     * Configure default proxy settings to connect to host
     *
     * @param host
     */
    public void configure(Host host);

    /**
     * @return True if PASV should be used by default
     */
    boolean usePassiveFTP();

    boolean isSOCKSProxyEnabled(Host host);

    String getSOCKSProxyHost(Host host);

    int getSOCKSProxyPort(Host host);

    boolean isHTTPProxyEnabled(Host host);

    String getHTTPProxyHost(Host host);

    int getHTTPProxyPort(Host host);

    boolean isHTTPSProxyEnabled(Host host);

    String getHTTPSProxyHost(Host host);

    int getHTTPSProxyPort(Host host);
}
