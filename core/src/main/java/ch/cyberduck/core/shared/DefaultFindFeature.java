package ch.cyberduck.core.shared;

/*
 * Copyright (c) 2013 David Kocher. All rights reserved.
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
 * feedback@cyberduck.ch
 */

import ch.cyberduck.core.ListProgressListener;
import ch.cyberduck.core.Path;
import ch.cyberduck.core.Session;
import ch.cyberduck.core.exception.BackgroundException;
import ch.cyberduck.core.exception.NotfoundException;
import ch.cyberduck.core.features.Find;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * The default implementation does a directory listing of the parent folder.
 */
public class DefaultFindFeature extends ListFilteringFeature implements Find {
    private static final Logger log = LogManager.getLogger(DefaultFindFeature.class);

    public DefaultFindFeature(final Session<?> session) {
        super(session);
    }

    @Override
    public boolean find(final Path file, final ListProgressListener listener) throws BackgroundException {
        if(file.isRoot()) {
            return true;
        }
        try {
            final Path found = this.search(file, listener);
            return found != null;
        }
        catch(NotfoundException e) {
            if(log.isDebugEnabled()) {
                log.debug("Parent directory for file {} not found", file);
            }
            return false;
        }
    }
}
