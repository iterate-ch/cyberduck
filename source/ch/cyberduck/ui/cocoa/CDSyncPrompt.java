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

import com.apple.cocoa.application.*;
import com.apple.cocoa.foundation.NSSelector;

import ch.cyberduck.core.Preferences;
import ch.cyberduck.core.SyncTransfer;
import ch.cyberduck.core.Transfer;
import ch.cyberduck.core.TransferAction;

import org.apache.log4j.Logger;

/**
 * @version $Id$
 */
public class CDSyncPrompt extends CDTransferPrompt {
    private static Logger log = Logger.getLogger(CDSyncPrompt.class);

    public CDSyncPrompt(final CDWindowController parent, final Transfer transfer) {
        super(parent, transfer);
    }

    public void awakeFromNib() {
        this.browserView.setDataSource(this.browserModel = new CDSyncPromptModel(this, transfer));
        super.awakeFromNib();
    }

    public void setBrowserView(NSOutlineView view) {
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
        else if(returncode == CANCEL_OPTION) { // Abort
            action = TransferAction.ACTION_CANCEL;
        }
    }

    // ----------------------------------------------------------
    // Outlets
    // ----------------------------------------------------------

    public void setActionPopup(final NSPopUpButton actionPopup) {
        this.actionPopup = actionPopup;
        this.actionPopup.removeAllItems();

        final TransferAction defaultAction
                = ((SyncTransfer) transfer).getAction();

        final TransferAction[] actions = new TransferAction[]{
                SyncTransfer.ACTION_DOWNLOAD,
                SyncTransfer.ACTION_UPLOAD,
                SyncTransfer.ACTION_MIRROR
        };

        for(int i = 0; i < actions.length; i++) {
            if(null == actions[i]) {
                continue; //Not resumeable
            }
            this.actionPopup.addItem(actions[i].getLocalizableString());
            this.actionPopup.lastItem().setRepresentedObject(actions[i]);
            if(actions[i].equals(defaultAction)) {
                this.actionPopup.selectItem(actionPopup.lastItem());
            }
        }
        this.actionPopup.setTarget(this);
        this.actionPopup.setAction(new NSSelector("actionPopupClicked", new Class[]{NSPopUpButton.class}));
    }

    public void actionPopupClicked(NSPopUpButton sender) {
        final TransferAction current = ((SyncTransfer) transfer).getAction();
        final TransferAction selected = (TransferAction) sender.selectedItem().representedObject();

        if(current.equals(selected)) {
            return;
        }

        Preferences.instance().setProperty("queue.sync.action.default", selected.toString());
        ((SyncTransfer) transfer).setTransferAction(selected);

        this.browserView.reloadData();
    }
}