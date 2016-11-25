package ch.cyberduck.ui.cocoa;

/*
 *  Copyright (c) 2005 David Kocher. All rights reserved.
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

import ch.cyberduck.binding.WindowController;
import ch.cyberduck.core.pool.SessionPool;
import ch.cyberduck.core.transfer.UploadTransfer;

public class UploadPromptController extends TransferPromptController {

    public UploadPromptController(final WindowController parent, final UploadTransfer transfer, final SessionPool session) {
        super(parent, transfer);
        browserModel = new UploadPromptDataSource(this, session, transfer, cache);
    }
}