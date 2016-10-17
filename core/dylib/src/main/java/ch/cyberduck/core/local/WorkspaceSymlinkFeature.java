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

import ch.cyberduck.binding.foundation.NSFileManager;
import ch.cyberduck.core.Local;
import ch.cyberduck.core.LocaleFactory;
import ch.cyberduck.core.exception.AccessDeniedException;
import ch.cyberduck.core.exception.LocalAccessDeniedException;
import ch.cyberduck.core.local.features.Symlink;

import org.apache.log4j.Logger;
import org.rococoa.ObjCObjectByReference;
import org.rococoa.cocoa.foundation.NSError;

public class WorkspaceSymlinkFeature implements Symlink {
    private static final Logger log = Logger.getLogger(WorkspaceSymlinkFeature.class);

    @Override
    public void symlink(final Local file, final String target) throws AccessDeniedException {
        final ObjCObjectByReference error = new ObjCObjectByReference();
        final boolean success = NSFileManager.defaultManager().createSymbolicLinkAtPath_withDestinationPath_error(
                file.getAbsolute(), target, error);
        if(!success) {
            final NSError f = error.getValueAs(NSError.class);
            if(null == f) {
                throw new LocalAccessDeniedException(String.format("%s %s",
                        LocaleFactory.localizedString("Cannot create file", "Error"), file.getAbsolute()));
            }
            throw new LocalAccessDeniedException(String.format("%s", f.localizedDescription()));
        }
        if(log.isDebugEnabled()) {
            log.debug(String.format("Created symbolic link %s with target %s", file, target));
        }
    }
}
