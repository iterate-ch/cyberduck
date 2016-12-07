package ch.cyberduck.ui.cocoa.controller;

/*
 * Copyright (c) 2002-2016 iterate GmbH. All rights reserved.
 * https://cyberduck.io/
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
 */

import ch.cyberduck.binding.WindowController;
import ch.cyberduck.core.pool.SessionPool;
import ch.cyberduck.core.transfer.DownloadTransfer;
import ch.cyberduck.ui.cocoa.datasource.DownloadPromptDataSource;

public class DownloadPromptController extends TransferPromptController {

    public DownloadPromptController(final WindowController parent, final DownloadTransfer transfer,
                                    final SessionPool source, final SessionPool destination) {
        super(parent, transfer);
        browserModel = new DownloadPromptDataSource(this, source, destination, transfer, cache);
    }
}