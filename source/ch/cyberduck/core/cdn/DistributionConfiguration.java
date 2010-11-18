package ch.cyberduck.core.cdn;

/*
 * Copyright (c) 2002-2010 David Kocher. All rights reserved.
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

import ch.cyberduck.core.Path;

import java.util.List;

/**
 * CDN configuration actions.
 *
 * @version $Id:$
 */
public interface DistributionConfiguration {

    /**
     * @return True if configuration is known.
     */
    boolean isConfigured();

    /**
     * Write distribution configuration for origin.
     *
     * @param enabled           True if distribution should be activated if not yet.
     * @param origin            Source server
     * @param method            Protocol
     * @param cnames            CNAME entires in the DNS pointing to the same origin.
     * @param logging           True if logging should be enabled for access to CDN.
     * @param defaultRootObject Index file for root of container
     */
    void write(boolean enabled, String origin, Distribution.Method method,
               String[] cnames, boolean logging, String defaultRootObject);

    /**
     * Read distribution status of origin
     *
     * @param origin Source server
     * @param method Protocol
     * @return
     */
    Distribution read(String origin, Distribution.Method method);

    /**
     * Invalidate distribution objects.
     *
     * @param origin Source server
     * @param method Protocol
     * @param files
     */
    void invalidate(String origin, Distribution.Method method, List<Path> files);

    /**
     * @param method
     * @return True if objects in the edge location can be deleted from the CDN
     */
    boolean isInvalidationSupported(Distribution.Method method);

    /**
     * Index file for root of container
     *
     * @param method
     * @return
     */
    boolean isDefaultRootSupported(Distribution.Method method);

    /**
     * @return
     */
    boolean isLoggingSupported(Distribution.Method method);

    /**
     * @param method
     * @return
     */
    boolean isCnameSupported(Distribution.Method method);

    /**
     * List available distribution methods for this CDN.
     *
     * @return The supported protocols
     */
    List<Distribution.Method> getMethods();

    /**
     * @param method Protocol
     * @return DNS name for container
     */
    String getOrigin(Distribution.Method method, String container);

    /**
     * Marketing name for the distribution service
     *
     * @return Localized description
     */
    String toString();

    /**
     * Clear any cached distribution information.
     *
     * @see #isConfigured()
     */
    void clear();
}
