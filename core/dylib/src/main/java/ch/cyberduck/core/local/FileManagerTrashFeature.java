package ch.cyberduck.core.local;

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

import ch.cyberduck.binding.foundation.NSFileManager;
import ch.cyberduck.binding.foundation.NSURL;
import ch.cyberduck.core.Local;
import ch.cyberduck.core.exception.LocalAccessDeniedException;
import ch.cyberduck.core.local.features.Trash;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.rococoa.ObjCObjectByReference;
import org.rococoa.cocoa.foundation.NSError;

public class FileManagerTrashFeature implements Trash {
    private static final Logger log = LogManager.getLogger(FileManagerTrashFeature.class);

    @Override
    public void trash(final Local file) throws LocalAccessDeniedException {
        log.debug("Move {} to Trash", file);
        final ObjCObjectByReference error = new ObjCObjectByReference();
        if(!NSFileManager.defaultManager().trashItemAtURL_resultingItemURL_error(
            NSURL.fileURLWithPath(file.getAbsolute()), null, error)) {
            final NSError f = error.getValueAs(NSError.class);
            if(null == f) {
                throw new LocalAccessDeniedException(file.getAbsolute());
            }
            throw new LocalAccessDeniedException(String.format("%s", f.localizedDescription()));
        }
    }
}
