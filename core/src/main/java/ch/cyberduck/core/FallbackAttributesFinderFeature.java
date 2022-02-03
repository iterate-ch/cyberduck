package ch.cyberduck.core;

/*
 * Copyright (c) 2002-2021 iterate GmbH. All rights reserved.
 * https://cyberduck.io/
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 */

import ch.cyberduck.core.exception.AccessDeniedException;
import ch.cyberduck.core.exception.BackgroundException;
import ch.cyberduck.core.exception.InteroperabilityException;
import ch.cyberduck.core.exception.NotfoundException;
import ch.cyberduck.core.features.AttributesFinder;
import ch.cyberduck.core.shared.DefaultAttributesFinderFeature;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class FallbackAttributesFinderFeature implements AttributesFinder {
    private static final Logger log = LogManager.getLogger(FallbackAttributesFinderFeature.class);

    private final AttributesFinder standard;
    private final AttributesFinder fallback;

    /**
     * Most commonly used to read attributes for files on server with no WebDAV support and thus 405 Method Not Allowed
     * reply for parent directory PROPFIND
     *
     * @param standard Default attributes finder using list implementation
     * @param fallback Fallback to native implementation
     */
    public FallbackAttributesFinderFeature(final AttributesFinder standard, final AttributesFinder fallback) {
        this.standard = standard;
        this.fallback = fallback;
    }

    @Override
    public PathAttributes find(final Path file, final ListProgressListener listener) throws BackgroundException {
        try {
            return standard.find(file, listener);
        }
        catch(InteroperabilityException | AccessDeniedException | NotfoundException f) {
            log.warn(String.format("Failure listing directory %s. %s", file.getParent(), f.getMessage()));
            if(fallback instanceof DefaultAttributesFinderFeature) {
                throw f;
            }
            // Try native implementation
            return fallback.find(file, listener);
        }
    }
}
