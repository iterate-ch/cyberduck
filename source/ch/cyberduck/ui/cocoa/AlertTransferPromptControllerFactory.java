package ch.cyberduck.ui.cocoa;

/*
 * Copyright (c) 2002-2013 David Kocher. All rights reserved.
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

import ch.cyberduck.core.Factory;
import ch.cyberduck.core.Session;
import ch.cyberduck.core.transfer.DisabledTransferPrompt;
import ch.cyberduck.core.transfer.DownloadTransfer;
import ch.cyberduck.core.transfer.SyncTransfer;
import ch.cyberduck.core.transfer.Transfer;
import ch.cyberduck.core.transfer.TransferPrompt;
import ch.cyberduck.core.transfer.UploadTransfer;
import ch.cyberduck.ui.Controller;
import ch.cyberduck.ui.TransferPromptControllerFactory;

/**
 * @version $Id$
 */
public final class AlertTransferPromptControllerFactory extends TransferPromptControllerFactory {

    public static void register() {
        TransferPromptControllerFactory.addFactory(Factory.NATIVE_PLATFORM, new AlertTransferPromptControllerFactory());
    }

    @Override
    protected TransferPrompt create() {
        return null;
    }

    @Override
    public TransferPrompt create(final Controller c, final Transfer transfer, final Session session) {
        switch(transfer.getType()) {
            case download:
                return new DownloadPromptController((WindowController) c, (DownloadTransfer) transfer, session);
            case upload:
                return new UploadPromptController((WindowController) c, (UploadTransfer) transfer, session);
            case sync:
                return new SyncPromptController((WindowController) c, (SyncTransfer) transfer, session);
        }
        return new DisabledTransferPrompt();
    }

    private AlertTransferPromptControllerFactory() {
        //
    }
}
