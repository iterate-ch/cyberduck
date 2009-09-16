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

import ch.cyberduck.core.*;
import ch.cyberduck.core.i18n.Locale;
import ch.cyberduck.ui.cocoa.application.*;
import ch.cyberduck.ui.cocoa.foundation.NSAttributedString;
import ch.cyberduck.ui.cocoa.foundation.NSIndexSet;
import ch.cyberduck.ui.cocoa.foundation.NSNotification;
import ch.cyberduck.ui.cocoa.foundation.NSObject;
import ch.cyberduck.ui.cocoa.threading.WindowMainAction;

import org.apache.log4j.Logger;
import org.rococoa.Foundation;
import org.rococoa.Rococoa;
import org.rococoa.ID;
import org.rococoa.cocoa.CGFloat;
import org.rococoa.cocoa.foundation.NSUInteger;
import org.rococoa.cocoa.foundation.NSInteger;

import java.util.HashMap;

/**
 * @version $Id$
 */
public abstract class CDTransferPrompt extends CDSheetController implements TransferPrompt {
    private static Logger log = Logger.getLogger(CDTransferPrompt.class);

    public static TransferPrompt create(CDWindowController parent, final Transfer transfer) {
        if(transfer instanceof DownloadTransfer) {
            return new CDDownloadPrompt(parent, transfer);
        }
        if(transfer instanceof UploadTransfer) {
            return new CDUploadPrompt(parent, transfer);
        }
        if(transfer instanceof SyncTransfer) {
            return new CDSyncPrompt(parent, transfer);
        }
        throw new IllegalArgumentException(transfer.toString());
    }

    public CDTransferPrompt(final CDWindowController parent, final Transfer transfer) {
        super(parent);
        this.transfer = transfer;
    }

    @Override
    protected String getBundleName() {
        return "Prompt";
    }

    @Outlet
    private NSButton toggleDetailsButton;

    public void setToggleDetailsButton(NSButton toggleDetailsButton) {
        this.toggleDetailsButton = toggleDetailsButton;
    }

    @Override
    public void awakeFromNib() {
        this.transfer.getSession().addProgressListener(l);
        this.reloadData();
        if(browserView.numberOfRows().intValue() > 0) {
            browserView.selectRowIndexes(NSIndexSet.indexSetWithIndex(new NSInteger(0)), false);
        }
        this.setState(this.toggleDetailsButton, Preferences.instance().getBoolean("transfer.toggle.details"));

        super.awakeFromNib();
    }

    @Override
    public void invalidate() {
        transfer.getSession().removeProgressListener(l);
        browserView.setDataSource(null);
        browserView.setDelegate(null);
        browserModel.invalidate();
        super.invalidate();
    }

    /**
     *
     */
    private ProgressListener l = new ProgressListener() {
        public void message(final String msg) {
            invoke(new WindowMainAction(CDTransferPrompt.this) {
                public void run() {
                    // Update the status label at the bottom of the browser window
                    statusLabel.setAttributedStringValue(NSAttributedString.attributedStringWithAttributes(msg,
                            TRUNCATE_MIDDLE_ATTRIBUTES));
                }
            });
        }
    };

    /**
     *
     */
    protected TransferAction action
            = TransferAction.forName(Preferences.instance().getProperty("queue.prompt.action.default"));

    /**
     * @return
     */
    public TransferAction getAction() {
        return action;
    }

    /**
     *
     */
    protected Transfer transfer;

    public void callback(final int returncode) {
        log.debug("callback:" + returncode);
        if(returncode == ALTERNATE_OPTION) { // Abort
            action = TransferAction.ACTION_CANCEL;
        }
        Preferences.instance().setProperty("transfer.toggle.details", this.toggleDetailsButton.state());
    }

    /**
     * Reload the files in the prompt dialog
     */
    public void reloadData() {
        log.debug("reloadData");
        statusIndicator.startAnimation(null);
        browserView.reloadData();
        statusIndicator.stopAnimation(null);
        statusLabel.setAttributedStringValue(NSAttributedString.attributedStringWithAttributes(
                browserView.numberOfRows() + " " + Locale.localizedString("files"),
                TRUNCATE_MIDDLE_ATTRIBUTES));
    }

    public TransferAction prompt() {
        log.debug("prompt:" + transfer);
        for(Path next : transfer.getRoots()) {
            if(browserModel.filter().accept(next)) {
                browserModel.add(next);
            }
        }
        this.beginSheet();
        return action;
    }

    private static final NSAttributedString UNKNOWN_STRING = NSAttributedString.attributedStringWithAttributes(
            Locale.localizedString("Unknown"),
            TRUNCATE_MIDDLE_ATTRIBUTES);

    protected final TableColumnFactory tableColumnsFactory = new TableColumnFactory();

    protected static class TableColumnFactory extends HashMap<String,NSTableColumn> {
        protected NSTableColumn create(String identifier) {
            if(!this.containsKey(identifier)) {
                this.put(identifier, NSTableColumn.tableColumnWithIdentifier(identifier));
            }
            return this.get(identifier);
        }
    }

    /**
     * A browsable listing of duplicate files and folders
     */
    @Outlet
    protected NSOutlineView browserView;
    protected CDTransferPromptModel browserModel;
    protected CDAbstractPathTableDelegate browserViewDelegate;

    public void setBrowserView(final NSOutlineView view) {
        this.browserView = view;
        this.browserView.setHeaderView(null);
        this.browserView.setDataSource(this.browserModel.id());
        this.browserView.setDelegate((this.browserViewDelegate = new CDAbstractPathTableDelegate() {

            public void enterKeyPressed(final ID sender) {
                ;
            }

            public void deleteKeyPressed(final ID sender) {
                ;
            }

            public void tableColumnClicked(NSTableView view, NSTableColumn tableColumn) {
                ;
            }

            public void tableRowDoubleClicked(final ID sender) {
                ;
            }

            public void selectionDidChange(NSNotification notification) {
                if(browserView.selectedRow().intValue() == -1) {
                    hideLocalDetails(true);
                    hideRemoteDetails(true);
                }
                else {
                    final Path p = browserModel.lookup(browserView.itemAtRow(browserView.selectedRow()));
                    localURLField.setAttributedStringValue(NSAttributedString.attributedStringWithAttributes(
                            p.getLocal().getAbsolute(),
                            TRUNCATE_MIDDLE_ATTRIBUTES));
                    localURLField.setHidden(false);

                    if(transfer.exists(p.getLocal())) {
                        if(p.getLocal().attributes.getSize() == -1) {
                            localSizeField.setAttributedStringValue(UNKNOWN_STRING);
                        }
                        else {
                            localSizeField.setAttributedStringValue(NSAttributedString.attributedStringWithAttributes(
                                    Status.getSizeAsString(p.getLocal().attributes.getSize()),
                                    TRUNCATE_MIDDLE_ATTRIBUTES));
                        }
                        localSizeField.setHidden(false);
                        if(p.getLocal().attributes.getModificationDate() == -1) {
                            localModificationField.setAttributedStringValue(UNKNOWN_STRING);
                        }
                        else {
                            localModificationField.setAttributedStringValue(NSAttributedString.attributedStringWithAttributes(
                                    CDDateFormatter.getLongFormat(p.getLocal().attributes.getModificationDate()),
                                    TRUNCATE_MIDDLE_ATTRIBUTES));
                        }
                        localModificationField.setHidden(false);
                    }
                    else {
                        localSizeField.setHidden(true);
                        localModificationField.setHidden(true);
                    }

                    remoteURLField.setAttributedStringValue(NSAttributedString.attributedStringWithAttributes(
                            p.getHost().toURL() + p.getAbsolute(),
                            TRUNCATE_MIDDLE_ATTRIBUTES));
                    remoteURLField.setHidden(false);

                    if(transfer.exists(p)) {
                        if(p.attributes.getSize() == -1) {
                            remoteSizeField.setAttributedStringValue(UNKNOWN_STRING);
                        }
                        else {
                            remoteSizeField.setAttributedStringValue(NSAttributedString.attributedStringWithAttributes(
                                    Status.getSizeAsString(p.attributes.getSize()),
                                    TRUNCATE_MIDDLE_ATTRIBUTES));
                        }
                        remoteSizeField.setHidden(false);
                        if(p.attributes.getModificationDate() == -1) {
                            remoteModificationField.setAttributedStringValue(UNKNOWN_STRING);
                        }
                        else {
                            remoteModificationField.setAttributedStringValue(NSAttributedString.attributedStringWithAttributes(
                                    CDDateFormatter.getLongFormat(p.attributes.getModificationDate()),
                                    TRUNCATE_MIDDLE_ATTRIBUTES));
                        }
                        remoteModificationField.setHidden(false);
                    }
                    else {
                        remoteSizeField.setHidden(true);
                        remoteModificationField.setHidden(true);
                    }
                }
            }

            private void hideRemoteDetails(boolean hidden) {
                remoteURLField.setHidden(hidden);
                remoteSizeField.setHidden(hidden);
                remoteModificationField.setHidden(hidden);
            }

            private void hideLocalDetails(boolean hidden) {
                localURLField.setHidden(hidden);
                localSizeField.setHidden(hidden);
                localModificationField.setHidden(hidden);
            }

            /**
             * @see NSOutlineView.Delegate
             */
            public void outlineView_willDisplayCell_forTableColumn_item(NSOutlineView view, NSCell cell,
                                                                        NSTableColumn tableColumn, NSObject item) {
                final String identifier = tableColumn.identifier();
                final Path path = browserModel.lookup(item);
                if(identifier.equals(CDTransferPromptModel.INCLUDE_COLUMN)) {
                    cell.setEnabled(transfer.isSelectable(path));
                }
                if(identifier.equals(CDTransferPromptModel.FILENAME_COLUMN)) {
                    (Rococoa.cast(cell, CDOutlineCell.class)).setIcon(CDIconCache.instance().iconForPath(path, 16));
                }
                if(cell.isKindOfClass(Foundation.getClass(NSTextFieldCell.class.getSimpleName()))) {
                    if(!transfer.isIncluded(path)) {
                        Rococoa.cast(cell, NSTextFieldCell.class).setTextColor(NSColor.disabledControlTextColor());
                    }
                    else {
                        Rococoa.cast(cell, NSTextFieldCell.class).setTextColor(NSColor.controlTextColor());
                    }
                }
            }
        }).id());
        this.browserView.setRowHeight(new CGFloat(NSLayoutManager.layoutManager().defaultLineHeightForFont(
                NSFont.systemFontOfSize(Preferences.instance().getFloat("browser.font.size"))).intValue() + 2));
        // selection properties
        this.browserView.setAllowsMultipleSelection(true);
        this.browserView.setAllowsEmptySelection(true);
        this.browserView.setAllowsColumnResizing(true);
        this.browserView.setAllowsColumnSelection(false);
        this.browserView.setAllowsColumnReordering(true);
        this.browserView.setUsesAlternatingRowBackgroundColors(Preferences.instance().getBoolean("browser.alternatingRows"));
        if(Preferences.instance().getBoolean("browser.horizontalLines") && Preferences.instance().getBoolean("browser.verticalLines")) {
            this.browserView.setGridStyleMask(new NSUInteger(NSTableView.NSTableViewSolidHorizontalGridLineMask.intValue() | NSTableView.NSTableViewSolidVerticalGridLineMask.intValue()));
        }
        else if(Preferences.instance().getBoolean("browser.verticalLines")) {
            this.browserView.setGridStyleMask(NSTableView.NSTableViewSolidVerticalGridLineMask);
        }
        else if(Preferences.instance().getBoolean("browser.horizontalLines")) {
            this.browserView.setGridStyleMask(NSTableView.NSTableViewSolidHorizontalGridLineMask);
        }
        else {
            this.browserView.setGridStyleMask(NSTableView.NSTableViewGridNone);
        }
        {
            NSTableColumn c = tableColumnsFactory.create(CDTransferPromptModel.FILENAME_COLUMN);
            c.headerCell().setStringValue(Locale.localizedString("Filename"));
            c.setMinWidth(100f);
            c.setWidth(220f);
            c.setMaxWidth(800f);
            c.setResizingMask(NSTableColumn.NSTableColumnAutoresizingMask | NSTableColumn.NSTableColumnUserResizingMask);
            c.setEditable(false);
            c.setDataCell(outlineCellPrototype);
            this.browserView.addTableColumn(c);
            this.browserView.setOutlineTableColumn(c);
        }
        {
            NSTableColumn c = tableColumnsFactory.create(CDTransferPromptModel.SIZE_COLUMN);
            c.headerCell().setStringValue("");
            c.setMinWidth(50f);
            c.setWidth(80f);
            c.setMaxWidth(100f);
            c.setResizingMask(NSTableColumn.NSTableColumnAutoresizingMask | NSTableColumn.NSTableColumnUserResizingMask);
            c.setEditable(false);
            c.setDataCell(textCellPrototype);
            this.browserView.addTableColumn(c);
        }
        {
            NSTableColumn c = tableColumnsFactory.create(CDTransferPromptModel.WARNING_COLUMN);
            c.headerCell().setStringValue("");
            c.setMinWidth(20f);
            c.setWidth(20f);
            c.setMaxWidth(20f);
            c.setResizingMask(NSTableColumn.NSTableColumnAutoresizingMask);
            c.setEditable(false);
            c.setDataCell(imageCellPrototype);
            c.dataCell().setAlignment(NSText.NSCenterTextAlignment);
            this.browserView.addTableColumn(c);
        }
        {
            NSTableColumn c = tableColumnsFactory.create(CDTransferPromptModel.INCLUDE_COLUMN);
            c.headerCell().setStringValue("");
            c.setMinWidth(20f);
            c.setWidth(20f);
            c.setMaxWidth(20f);
            c.setResizingMask(NSTableColumn.NSTableColumnAutoresizingMask);
            c.setEditable(false);
            final NSButtonCell cell = buttonCellPrototype;
            cell.setTitle("");
            cell.setControlSize(NSCell.NSSmallControlSize);
            cell.setButtonType(NSButtonCell.NSSwitchButton);
            cell.setAllowsMixedState(false);
            cell.setTarget(this.id());
            cell.setAlignment(NSText.NSCenterTextAlignment);
            c.setDataCell(cell);
            this.browserView.addTableColumn(c);
        }
        this.browserView.sizeToFit();
    }

    protected final NSButtonCell buttonCellPrototype = NSButtonCell.buttonCell();
    protected final NSTextFieldCell outlineCellPrototype = CDOutlineCell.outlineCell();
    protected final NSImageCell imageCellPrototype = NSImageCell.imageCell();
    protected final NSTextFieldCell textCellPrototype = NSTextFieldCell.textFieldCell();

    @Outlet
    private NSTextField remoteURLField;

    public void setRemoteURLField(final NSTextField f) {
        this.remoteURLField = f;
        this.remoteURLField.setHidden(true);
    }

    @Outlet
    private NSTextField remoteSizeField;

    public void setRemoteSizeField(final NSTextField f) {
        this.remoteSizeField = f;
        this.remoteSizeField.setHidden(true);
    }

    @Outlet
    private NSTextField remoteModificationField;

    public void setRemoteModificationField(final NSTextField f) {
        this.remoteModificationField = f;
        this.remoteModificationField.setHidden(true);
    }

    @Outlet
    private NSTextField localURLField;

    public void setLocalURLField(final NSTextField f) {
        this.localURLField = f;
        this.localURLField.setHidden(true);
    }

    @Outlet
    private NSTextField localSizeField;

    public void setLocalSizeField(final NSTextField f) {
        this.localSizeField = f;
        this.localSizeField.setHidden(true);
    }

    @Outlet
    private NSTextField localModificationField;

    public void setLocalModificationField(final NSTextField f) {
        this.localModificationField = f;
        this.localModificationField.setHidden(true);
    }

    @Outlet
    private NSProgressIndicator statusIndicator;

    public void setStatusIndicator(final NSProgressIndicator f) {
        this.statusIndicator = f;
        this.statusIndicator.setDisplayedWhenStopped(false);
    }

    @Outlet
    private NSTextField statusLabel;

    public void setStatusLabel(final NSTextField f) {
        this.statusLabel = f;
    }

    @Outlet
    protected NSPopUpButton actionPopup;

    public void setActionPopup(final NSPopUpButton actionPopup) {
        this.actionPopup = actionPopup;
        this.actionPopup.removeAllItems();

        final TransferAction defaultAction
                = TransferAction.forName(Preferences.instance().getProperty("queue.prompt.action.default"));

        final TransferAction[] actions = new TransferAction[]{
                transfer.isResumable() ? TransferAction.ACTION_RESUME : null,
                TransferAction.ACTION_OVERWRITE,
                TransferAction.ACTION_RENAME,
                TransferAction.ACTION_SKIP};

        for(TransferAction action : actions) {
            if(null == action) {
                continue; //Not resumeable
            }
            this.actionPopup.addItemWithTitle(action.getLocalizableString());
            this.actionPopup.lastItem().setRepresentedObject(action.toString());
            if(action.equals(defaultAction)) {
                this.actionPopup.selectItem(actionPopup.lastItem());
            }
        }
        this.action = TransferAction.forName(this.actionPopup.selectedItem().representedObject());
        this.actionPopup.setTarget(this.id());
        this.actionPopup.setAction(Foundation.selector("actionPopupClicked:"));
    }

    public void actionPopupClicked(NSPopUpButton sender) {
        final TransferAction selected = TransferAction.forName(sender.selectedItem().representedObject());

        if(this.action.equals(selected)) {
            return;
        }

        Preferences.instance().setProperty("queue.prompt.action.default", selected.toString());

        this.action = selected;
        this.browserView.reloadData();
    }
}
