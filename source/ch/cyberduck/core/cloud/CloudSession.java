package ch.cyberduck.core.cloud;

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

import ch.cyberduck.core.TranscriptListener;
import ch.cyberduck.core.ssl.SSLSession;

import java.util.List;

/**
 * @version $Id:$
 */
public interface CloudSession {
    /**
     * @param container
     * @param method
     * @return Cached distribution if available.
     */
    Distribution getDistribution(String container, Distribution.Method method);

    /**
     * @param enabled
     * @param method
     * @param cnames
     * @param logging
     */
    void writeDistribution(boolean enabled, String container, Distribution.Method method,
                                           String[] cnames, boolean logging, String defaultRootObject);

    /**
     * @return
     */
    Distribution readDistribution(String container, Distribution.Method method);

    /**
     * @return The supported protocols
     */
    List<Distribution.Method> getSupportedDistributionMethods();

    /**
     * Marketing name for the distribution service
     *
     * @return Localized description
     */
    String getDistributionServiceName();

    /**
     * @return List of redundancy level options. Empty list
     *         no storage options are available.
     */
    List<String> getSupportedStorageClasses();
}
