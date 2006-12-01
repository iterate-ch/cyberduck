package ch.cyberduck.core;

/*
 *  Copyright (c) 2006 David Kocher. All rights reserved.
 *  http://cyberduck.ch/
 *
 *  This program is free software; you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation; either version 2 of the License, or
 *  (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  Bug fixes, suggestions and comments should be sent to:
 *  dkocher@cyberduck.ch
 */

import ch.cyberduck.ui.cocoa.CDSyncQueueValidatorController;
import ch.cyberduck.ui.cocoa.CDUploadQueueValidatorController;
import ch.cyberduck.ui.cocoa.CDDownloadQueueValidatorController;
import ch.cyberduck.ui.cocoa.CDWindowController;

/**
 * @version $Id$
 */
public class ValidatorFactory {

    public static Validator create(final Queue q, final CDWindowController parent) {
        if(q instanceof DownloadQueue) {
            return new CDDownloadQueueValidatorController(parent);
        }
        if(q instanceof UploadQueue) {
            return new CDUploadQueueValidatorController(parent);
        }
        if(q instanceof SyncQueue) {
            return new CDSyncQueueValidatorController(parent);
        }
        return null;
    }
}
