package ch.cyberduck.core.preferences;

/*
 * Copyright (c) 2002-2023 iterate GmbH. All rights reserved.
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

import ch.cyberduck.core.Local;
import ch.cyberduck.core.exception.AccessDeniedException;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class MigratingSupportDirectoryFinder implements SupportDirectoryFinder {
    private static final Logger log = LogManager.getLogger(MigratingSupportDirectoryFinder.class);

    private final SupportDirectoryFinder deprecated;
    private final SupportDirectoryFinder proxy;

    /**
     * @param deprecated Deprecated implemenation providing previous application support folder
     */
    public MigratingSupportDirectoryFinder(final SupportDirectoryFinder deprecated, final SupportDirectoryFinder proxy) {
        this.deprecated = deprecated;
        this.proxy = proxy;
    }

    @Override
    public SupportDirectoryFinder setup() {
        final Local appdata = proxy.find();
        if(appdata.exists()) {
            log.debug("Application support folder {} already exists", appdata);
        }
        else {
            final Local previous = deprecated.find();
            if(previous.exists()) {
                log.warn("Migrate application support folder from {} to {}", previous, appdata);
                try {
                    // Rename folder recursively
                    previous.rename(appdata);
                }
                catch(AccessDeniedException e) {
                    log.warn("Failure {} migrating {} to application group directory {}", e, previous, appdata);
                }
            }
            else {
                log.debug("No previous application support folder found in {}", previous);
            }
        }
        return this;
    }

    @Override
    public Local find() {
        return proxy.find();
    }
}
