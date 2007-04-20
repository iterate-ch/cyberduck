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

import ch.cyberduck.core.SyncTransfer;
import ch.cyberduck.core.TransferAction;

import com.apple.cocoa.application.*;
import com.apple.cocoa.foundation.NSSelector;

import org.apache.log4j.Logger;

/**
 * @version $Id$
 */
public class CDSyncPrompt extends CDTransferPrompt {
    private static Logger log = Logger.getLogger(CDSyncPrompt.class);

    public CDSyncPrompt(final CDWindowController parent) {
        super(parent);
    }

    public void beginSheet(final boolean blocking) {
        synchronized(NSApplication.sharedApplication()) {
            if(!NSApplication.loadNibNamed("Sync", this)) {
                log.fatal("Couldn't load Sync.nib");
            }
        }
        super.beginSheet(blocking);
    }

    public void setBrowserView(NSOutlineView view) {
        view.setDataSource(this.browserModel = new CDSyncPromptModel(this, transfer));
        super.setBrowserView(view);
        NSSelector setResizableMaskSelector
                = new NSSelector("setResizingMask", new Class[]{int.class});
        {
            NSTableColumn c = new NSTableColumn();
            c.setIdentifier(CDSyncPromptModel.SYNC_COLUMN);
            c.headerCell().setStringValue("");
            c.setMinWidth(20f);
            c.setWidth(20f);
            c.setMaxWidth(20f);
            if(setResizableMaskSelector.implementedByClass(NSTableColumn.class)) {
                c.setResizingMask(NSTableColumn.AutoresizingMask);
            }
            else {
                c.setResizable(true);
            }
            c.setEditable(false);
            c.setDataCell(new NSImageCell());
            c.dataCell().setAlignment(NSText.CenterTextAlignment);
            view.addTableColumn(c);
        }
        {
            NSTableColumn c = new NSTableColumn();
            c.setIdentifier(CDSyncPromptModel.CREATE_COLUMN);
            c.headerCell().setStringValue("");
            c.setMinWidth(20f);
            c.setWidth(20f);
            c.setMaxWidth(20f);
            if(setResizableMaskSelector.implementedByClass(NSTableColumn.class)) {
                c.setResizingMask(NSTableColumn.AutoresizingMask);
            }
            else {
                c.setResizable(true);
            }
            c.setEditable(false);
            c.setDataCell(new NSImageCell());
            c.dataCell().setAlignment(NSText.CenterTextAlignment);
            view.addTableColumn(c);
        }
        view.sizeToFit();
    }

    public void callback(final int returncode) {
        if(returncode == DEFAULT_OPTION) { // Continue
            action = TransferAction.ACTION_OVERWRITE;
        }
        if(returncode == CANCEL_OPTION) { // Abort
            action = TransferAction.ACTION_CANCEL;
        }
        synchronized(promptLock) {
            promptLock.notifyAll();
        }
    }

    // ----------------------------------------------------------
    // Outlets
    // ----------------------------------------------------------

    protected NSButton downloadRadioCell;

    public void setDownloadRadioCell(NSButton downloadRadioCell) {
        this.downloadRadioCell = downloadRadioCell;
        this.downloadRadioCell.setState(NSCell.OnState);
        this.downloadRadioCell.setTarget(this);
        this.downloadRadioCell.setAction(new NSSelector("downloadCellClicked", new Class[]{Object.class}));
    }

    public void downloadCellClicked(final Object sender) {
        ((SyncTransfer)transfer).setCreateLocalFiles(downloadRadioCell.state() == NSCell.OnState);
        browserView.reloadData();
    }
    
    protected NSButton uploadRadioCell;

    public void setUploadRadioCell(NSButton uploadRadioCell) {
        this.uploadRadioCell = uploadRadioCell;
        this.uploadRadioCell.setState(NSCell.OnState);
        this.uploadRadioCell.setTarget(this);
        this.uploadRadioCell.setAction(new NSSelector("uploadCellClicked", new Class[]{Object.class}));
    }

    public void uploadCellClicked(final Object sender) {
        ((SyncTransfer)transfer).setCreateRemoteFiles(uploadRadioCell.state() == NSCell.OnState);
        browserView.reloadData();
    }
}