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
 * @version $Id:$
 */
public interface Proxy {

    /**
     *
     * @param hostname
     */
    public void configure(final String hostname);

    /**
     *
     * @return True if PASV should be used by default
     */
    boolean usePassiveFTP();

    boolean isHostExcluded(String hostname);

    boolean isSOCKSProxyEnabled();

    String getSOCKSProxyHost();

    int getSOCKSProxyPort();

    boolean isHTTPProxyEnabled();

    String getHTTPProxyHost();

    int getHTTPProxyPort();

    boolean isHTTPSProxyEnabled();

    String getHTTPSProxyHost();

    int getHTTPSProxyPort();
}
