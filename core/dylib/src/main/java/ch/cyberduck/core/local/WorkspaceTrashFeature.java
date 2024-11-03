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
import ch.cyberduck.core.exception.LocalAccessDeniedException;
import ch.cyberduck.core.local.features.Trash;
import ch.cyberduck.core.unicode.NFDNormalizer;

import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class WorkspaceTrashFeature implements Trash {
    private static final Logger log = LogManager.getLogger(WorkspaceTrashFeature.class);

    private final NSWorkspace workspace = NSWorkspace.sharedWorkspace();

    /**
     * Move file to trash on main interface thread using <code>NSWorkspace.RecycleOperation</code>.
     */
    @Override
    public void trash(final Local file) throws LocalAccessDeniedException {
        synchronized(NSWorkspace.class) {
            if(log.isDebugEnabled()) {
                log.debug("Move {} to Trash", file);
            }
            // Asynchronous operation. 0 if the operation is performed synchronously and succeeds, and a positive
            // integer if the operation is performed asynchronously and succeeds
            if(!workspace.performFileOperation(
                NSWorkspace.RecycleOperation,
                new NFDNormalizer().normalize(file.getParent().getAbsolute()).toString(), StringUtils.EMPTY,
                NSArray.arrayWithObject(new NFDNormalizer().normalize(file.getName()).toString()))) {
                throw new LocalAccessDeniedException(String.format("Failed to move %s to Trash", file.getName()));
            }
        }
    }
}
