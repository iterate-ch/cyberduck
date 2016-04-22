package ch.cyberduck.core.local;

/*
 * Copyright (c) 2002-2014 David Kocher. All rights reserved.
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
 * Bug fixes, suggestions and comments should be sent to feedback@cyberduck.ch
 */

import ch.cyberduck.binding.application.NSWorkspace;
import ch.cyberduck.binding.foundation.NSArray;
import ch.cyberduck.core.Local;
import ch.cyberduck.core.exception.AccessDeniedException;
import ch.cyberduck.core.exception.LocalAccessDeniedException;
import ch.cyberduck.core.local.features.Trash;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;

/**
 * @version $Id$
 */
public class WorkspaceTrashFeature implements Trash {
    private static final Logger log = Logger.getLogger(WorkspaceTrashFeature.class);

    /**
     * Move file to trash on main interface thread using <code>NSWorkspace.RecycleOperation</code>.
     */
    @Override
    public void trash(final Local file) throws AccessDeniedException {
        if(file.exists()) {
            synchronized(NSWorkspace.class) {
                if(log.isDebugEnabled()) {
                    log.debug(String.format("Move %s to Trash", file));
                }
                final NSWorkspace workspace = NSWorkspace.sharedWorkspace();
                // Asynchronous operation. 0 if the operation is performed synchronously and succeeds, and a positive
                // integer if the operation is performed asynchronously and succeeds
                if(!workspace.performFileOperation(
                        NSWorkspace.RecycleOperation,
                        file.getParent().getAbsolute(), StringUtils.EMPTY,
                        NSArray.arrayWithObject(file.getName()))) {
                    throw new LocalAccessDeniedException(String.format("Failed to move %s to Trash", file.getName()));
                }
            }
        }
    }
}
