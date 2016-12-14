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
import ch.cyberduck.binding.application.NSOutlineView;
import ch.cyberduck.binding.application.NSTableColumn;
import ch.cyberduck.binding.application.NSText;
import ch.cyberduck.core.pool.SessionPool;
import ch.cyberduck.core.transfer.SyncTransfer;
import ch.cyberduck.ui.cocoa.datasource.SyncPromptDataSource;

import org.apache.commons.lang3.StringUtils;

public class SyncPromptController extends TransferPromptController {

    private final TableColumnFactory tableColumnsFactory
            = new TableColumnFactory();

    public SyncPromptController(final WindowController parent, final SyncTransfer transfer,
                                final SessionPool source, final SessionPool destination) {
        super(parent, transfer);
        browserModel = new SyncPromptDataSource(this, source, destination, transfer, cache);
    }

    @Override
    public void setBrowserView(NSOutlineView view) {
        super.setBrowserView(view);
        {
            NSTableColumn c = tableColumnsFactory.create(SyncPromptDataSource.Column.sync.name());
            c.headerCell().setStringValue(StringUtils.EMPTY);
            c.setMinWidth(20f);
            c.setWidth(20f);
            c.setMaxWidth(20f);
            c.setResizingMask(NSTableColumn.NSTableColumnAutoresizingMask);
            c.setEditable(false);
            c.setDataCell(imageCellPrototype);
            c.dataCell().setAlignment(NSText.NSCenterTextAlignment);
            view.addTableColumn(c);
        }
        {
            NSTableColumn c = tableColumnsFactory.create(SyncPromptDataSource.Column.create.name());
            c.headerCell().setStringValue(StringUtils.EMPTY);
            c.setMinWidth(20f);
            c.setWidth(20f);
            c.setMaxWidth(20f);
            c.setResizingMask(NSTableColumn.NSTableColumnAutoresizingMask);
            c.setEditable(false);
            c.setDataCell(imageCellPrototype);
            c.dataCell().setAlignment(NSText.NSCenterTextAlignment);
            view.addTableColumn(c);
        }
        view.sizeToFit();
    }
}