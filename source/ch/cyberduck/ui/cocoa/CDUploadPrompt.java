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

import ch.cyberduck.core.Transfer;
import ch.cyberduck.core.TransferAction;

import org.apache.log4j.Logger;

/**
 * @version $Id$
 */
public class CDUploadPrompt extends CDTransferPrompt {
    private static Logger log = Logger.getLogger(CDUploadPrompt.class);

    public CDUploadPrompt(final CDWindowController parent, final Transfer transfer) {
        super(parent, transfer);
    }

    @Override
    public TransferAction prompt() {
        browserModel = new CDUploadPromptModel(this, transfer);
        return super.prompt();
    }
}