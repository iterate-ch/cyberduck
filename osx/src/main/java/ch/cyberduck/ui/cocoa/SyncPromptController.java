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
import ch.cyberduck.binding.application.NSOutlineView;
import ch.cyberduck.binding.application.NSTableColumn;
import ch.cyberduck.binding.application.NSText;
import ch.cyberduck.core.pool.SessionPool;
import ch.cyberduck.core.transfer.SyncTransfer;

import org.apache.commons.lang3.StringUtils;

public class SyncPromptController extends TransferPromptController {

    private final TableColumnFactory tableColumnsFactory
            = new TableColumnFactory();

    public SyncPromptController(final WindowController parent, final SyncTransfer transfer, final SessionPool session) {
        super(parent, transfer);
        browserModel = new SyncPromptModel(this, session, transfer, cache);
    }

    @Override
    public void setBrowserView(NSOutlineView view) {
        super.setBrowserView(view);
        {
            NSTableColumn c = tableColumnsFactory.create(SyncPromptModel.Column.sync.name());
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
            NSTableColumn c = tableColumnsFactory.create(SyncPromptModel.Column.create.name());
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