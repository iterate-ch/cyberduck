package ch.cyberduck.core.cloud;

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

import ch.cyberduck.core.Host;
import ch.cyberduck.core.http.HTTPSession;

import java.util.List;

/**
 * @version $Id$
 */
public abstract class CloudSession extends HTTPSession {

    protected CloudSession(Host h) {
        super(h);
    }

    /**
     * @param enabled
     * @param method
     * @param cnames
     * @param logging
     */
    public abstract void writeDistribution(final boolean enabled, String container, Distribution.Method method, final String[] cnames, boolean logging);

    /**
     * @return
     */
    public abstract Distribution readDistribution(String container, Distribution.Method method);

    /**
     * @return The supported protocols
     */
    public abstract List<Distribution.Method> getSupportedDistributionMethods();

    /**
     * Marketing name for the distribution service
     *
     * @return Localized description
     */
    public abstract String getDistributionServiceName();

    /**
     * @return List of redundancy level options. Empty list
     *         no storage options are available.
     */
    public abstract List<String> getSupportedStorageClasses();

    /**
     * Use ACL support.
     *
     * @return Always returning false because permissions should be set using ACLs
     */
    @Override
    public boolean isUnixPermissionsSupported() {
        return false;
    }

    @Override
    public boolean isTimestampSupported() {
        return false;
    }
}
