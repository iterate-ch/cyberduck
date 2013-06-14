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
import ch.cyberduck.core.Protocol;
import ch.cyberduck.core.threading.BackgroundException;

import java.util.List;

/**
 * CDN configuration actions.
 *
 * @version $Id$
 */
public interface DistributionConfiguration {

    /**
     * Write distribution configuration for origin.
     *
     * @param container         Container
     * @param enabled           True if distribution should be activated if not yet.
     * @param method            Distribution method
     * @param cnames            CNAME entires in the DNS pointing to the same origin.
     * @param logging           True if logging should be enabled for access to CDN.
     * @param loggingBucket     The logging target
     * @param defaultRootObject Index file for root of container
     */
    void write(final Path container, boolean enabled, Distribution.Method method,
               String[] cnames, boolean logging, String loggingBucket, String defaultRootObject) throws BackgroundException;

    /**
     * Read distribution configuration of origin
     *
     * @param container Container
     * @param method    Distribution protocol
     * @return Distribution Configuration
     */
    Distribution read(final Path container, Distribution.Method method) throws BackgroundException;

    /**
     * Purge objects from edge locations
     *
     * @param container Container
     * @param method    Distribution method
     * @param files     Selected files or containers to purge
     * @param recursive Apply recursively to selected container or placeholder
     */
    void invalidate(final Path container, Distribution.Method method, List<Path> files, boolean recursive) throws BackgroundException;

    /**
     * @param method Distribution method
     * @return True if objects in the edge location can be deleted from the CDN
     */
    boolean isInvalidationSupported(Distribution.Method method);

    /**
     * Index file for root of container
     *
     * @param method Distribution method
     * @return True if index file can be specified
     */
    boolean isDefaultRootSupported(Distribution.Method method);

    /**
     * @param method Distribution method
     * @return True if CDN is is configured logging requests to storage
     */
    boolean isLoggingSupported(Distribution.Method method);

    /**
     * @param method Distribution method
     * @return If there is an analytics provider
     */
    boolean isAnalyticsSupported(Distribution.Method method);

    /**
     * @param method Distribution method
     * @return True if CNAME for for the CDN URI can be configured
     */
    boolean isCnameSupported(Distribution.Method method);

    /**
     * List available distribution methods for this CDN.
     *
     * @param container Container
     * @return The supported protocols
     */
    List<Distribution.Method> getMethods(final Path container);

    /**
     * @return Hostname and port
     */
    Protocol getProtocol();

    /**
     * Marketing name for the distribution service
     *
     * @return Localized description
     */
    String getName();

    /**
     * @param method Distribution method
     * @return CDN name
     */
    String getName(Distribution.Method method);
}
