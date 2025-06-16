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

import ch.cyberduck.binding.Action;
import ch.cyberduck.binding.Delegate;
import ch.cyberduck.binding.Outlet;
import ch.cyberduck.binding.SheetController;
import ch.cyberduck.binding.WindowController;
import ch.cyberduck.binding.application.*;
import ch.cyberduck.binding.foundation.NSAttributedString;
import ch.cyberduck.binding.foundation.NSIndexSet;
import ch.cyberduck.binding.foundation.NSNotification;
import ch.cyberduck.binding.foundation.NSObject;
import ch.cyberduck.core.Cache;
import ch.cyberduck.core.DescriptiveUrl;
import ch.cyberduck.core.LocaleFactory;
import ch.cyberduck.core.NSObjectTransferItemReference;
import ch.cyberduck.core.ProgressListener;
import ch.cyberduck.core.ReverseLookupCache;
import ch.cyberduck.core.TranscriptListener;
import ch.cyberduck.core.TransferItemCache;
import ch.cyberduck.core.UserDateFormatterFactory;
import ch.cyberduck.core.formatter.SizeFormatterFactory;
import ch.cyberduck.core.preferences.Preferences;
import ch.cyberduck.core.preferences.PreferencesFactory;
import ch.cyberduck.core.resources.IconCacheFactory;
import ch.cyberduck.core.shared.DefaultUrlProvider;
import ch.cyberduck.core.threading.BackgroundAction;
import ch.cyberduck.core.threading.DefaultMainAction;
import ch.cyberduck.core.transfer.Transfer;
import ch.cyberduck.core.transfer.TransferAction;
import ch.cyberduck.core.transfer.TransferItem;
import ch.cyberduck.core.transfer.TransferPrompt;
import ch.cyberduck.core.transfer.TransferStatus;
import ch.cyberduck.ui.cocoa.datasource.TransferPromptDataSource;
import ch.cyberduck.ui.cocoa.view.OutlineCell;

import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.rococoa.Foundation;
import org.rococoa.ID;
import org.rococoa.Rococoa;
import org.rococoa.cocoa.CGFloat;
import org.rococoa.cocoa.foundation.NSInteger;
import org.rococoa.cocoa.foundation.NSUInteger;

import java.text.MessageFormat;
import java.util.EnumSet;

public abstract class TransferPromptController extends SheetController implements TransferPrompt, ProgressListener, TranscriptListener {
    private static final Logger log = LogManager.getLogger(TransferPromptController.class);

    private static final NSAttributedString UNKNOWN_STRING = NSAttributedString.attributedStringWithAttributes(
            LocaleFactory.localizedString("Unknown"),
            TRUNCATE_MIDDLE_ATTRIBUTES);

    protected final Transfer transfer;

    protected final Cache<TransferItem> cache
            = new ReverseLookupCache<>(new TransferItemCache(Integer.MAX_VALUE), Integer.MAX_VALUE);

    protected final NSButtonCell buttonCellPrototype = NSButtonCell.buttonCell();
    protected final NSTextFieldCell outlineCellPrototype = OutlineCell.outlineCell();
    protected final NSImageCell imageCellPrototype = NSImageCell.imageCell();
    protected final NSTextFieldCell textCellPrototype = NSTextFieldCell.textFieldCell();

    // Setting appearance attributes
    final NSLayoutManager layoutManager = NSLayoutManager.layoutManager();

    private final TableColumnFactory tableColumnsFactory
            = new TableColumnFactory();

    private final Preferences preferences
            = PreferencesFactory.get();

    private final WindowController parent;

    private TransferAction action;

    @Delegate
    protected TransferPromptDataSource browserModel;
    @Delegate
    protected AbstractPathTableDelegate browserViewDelegate;

    /**
     * A browsable listing of duplicate files and folders
     */
    @Outlet
    private NSOutlineView browserView;
    @Outlet
    private NSButton toggleDetailsButton;
    @Outlet
    private NSTextField remoteURLField;
    @Outlet
    private NSTextField remoteSizeField;
    @Outlet
    private NSTextField remoteModificationField;
    @Outlet
    private NSTextField localURLField;
    @Outlet
    private NSTextField localSizeField;
    @Outlet
    private NSTextField localModificationField;
    @Outlet
    private NSProgressIndicator statusIndicator;
    @Outlet
    private NSTextField statusLabel;
    @Outlet
    private NSPopUpButton actionPopup;

    public TransferPromptController(final WindowController parent, final Transfer transfer) {
        this.parent = parent;
        this.transfer = transfer;
        this.action = TransferAction.forName(preferences.getProperty(
                String.format("queue.prompt.%s.action.default", transfer.getType().name())));
    }

    @Override
    protected String getBundleName() {
        return "Prompt";
    }

    @Override
    public void setWindow(final NSWindow window) {
        window.setContentMinSize(window.frame().size);
        super.setWindow(window);
    }

    public void setToggleDetailsButton(NSButton toggleDetailsButton) {
        this.toggleDetailsButton = toggleDetailsButton;
    }

    @Override
    public void awakeFromNib() {
        this.setState(this.toggleDetailsButton, preferences.getBoolean("transfer.toggle.details"));
        super.awakeFromNib();
    }

    @Override
    public void invalidate() {
        browserView.setDataSource(null);
        browserView.setDelegate(null);
        super.invalidate();
    }

    @Override
    public void message(final String message) {
        if(null == statusLabel) {
            return;
        }
        // Update the status label at the bottom of the browser window
        statusLabel.setAttributedStringValue(NSAttributedString.attributedStringWithAttributes(message,
                TRUNCATE_MIDDLE_ATTRIBUTES));
    }

    /**
     * Reload the files in the prompt dialog
     */
    public void reload() {
        log.debug("Reload table view");
        browserView.reloadData();
        browserView.selectRowIndexes(NSIndexSet.indexSetWithIndex(new NSInteger(0L)), false);
        statusLabel.setAttributedStringValue(NSAttributedString.attributedStringWithAttributes(
                MessageFormat.format(LocaleFactory.localizedString("{0} Items"), String.valueOf(browserView.numberOfRows())),
                TRUNCATE_MIDDLE_ATTRIBUTES));
    }

    @Override
    public TransferAction prompt(final TransferItem file) {
        log.debug("Prompt for transfer action of {}", transfer);
        final int returncode = parent.alert(this);
        log.debug("Callback with return code {}", returncode);
        if(returncode == CANCEL_OPTION) { // Abort
            action = TransferAction.cancel;
        }
        preferences.setProperty("transfer.toggle.details", toggleDetailsButton.state());
        return action;
    }

    @Override
    public boolean isSelected(final TransferItem file) {
        return browserModel.isSelected(file);
    }

    public void setBrowserView(final NSOutlineView view) {
        this.browserView = view;
        this.browserView.setHeaderView(null);
        this.browserView.setRowHeight(new CGFloat(layoutManager.defaultLineHeightForFont(
                NSFont.systemFontOfSize(preferences.getFloat("browser.font.size"))).intValue() + 2));
        {
            NSTableColumn c = tableColumnsFactory.create(TransferPromptDataSource.Column.filename.name());
            c.headerCell().setStringValue(LocaleFactory.localizedString("Filename"));
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
            NSTableColumn c = tableColumnsFactory.create(TransferPromptDataSource.Column.size.name());
            c.headerCell().setStringValue(StringUtils.EMPTY);
            c.setMinWidth(50f);
            c.setWidth(80f);
            c.setMaxWidth(100f);
            c.setResizingMask(NSTableColumn.NSTableColumnAutoresizingMask | NSTableColumn.NSTableColumnUserResizingMask);
            c.setEditable(false);
            c.setDataCell(textCellPrototype);
            this.browserView.addTableColumn(c);
        }
        {
            NSTableColumn c = tableColumnsFactory.create(TransferPromptDataSource.Column.warning.name());
            c.headerCell().setStringValue(StringUtils.EMPTY);
            c.setMinWidth(20f);
            c.setWidth(20f);
            c.setMaxWidth(20f);
            c.setResizingMask(NSTableColumn.NSTableColumnAutoresizingMask);
            c.setEditable(false);
            c.setDataCell(imageCellPrototype);
            c.dataCell().setAlignment(TEXT_ALIGNMENT_CENTER);
            this.browserView.addTableColumn(c);
        }
        {
            NSTableColumn c = tableColumnsFactory.create(TransferPromptDataSource.Column.include.name());
            c.headerCell().setStringValue(StringUtils.EMPTY);
            c.setMinWidth(20f);
            c.setWidth(20f);
            c.setMaxWidth(20f);
            c.setResizingMask(NSTableColumn.NSTableColumnAutoresizingMask);
            c.setEditable(false);
            final NSButtonCell cell = buttonCellPrototype;
            cell.setTitle(StringUtils.EMPTY);
            cell.setControlSize(NSCell.NSSmallControlSize);
            cell.setButtonType(NSButtonCell.NSSwitchButton);
            cell.setAllowsMixedState(false);
            cell.setTarget(this.id());
            cell.setAlignment(TEXT_ALIGNMENT_CENTER);
            c.setDataCell(cell);
            this.browserView.addTableColumn(c);
        }
        this.browserView.setDataSource(this.browserModel.id());
        this.browserView.setDelegate((this.browserViewDelegate = new AbstractPathTableDelegate(
                browserView.tableColumnWithIdentifier(TransferPromptDataSource.Column.filename.name())
        ) {

            @Override
            public void enterKeyPressed(final ID sender) {
                //
            }

            @Override
            public void deleteKeyPressed(final ID sender) {
                //
            }

            @Override
            public void tableColumnClicked(NSTableView view, NSTableColumn tableColumn) {
                //
            }

            @Override
            public void tableRowDoubleClicked(final ID sender) {
                //
            }

            @Override
            public void selectionDidChange(final NSNotification notification) {
                if(browserView.selectedRow().intValue() == -1) {
                    remoteURLField.setStringValue(StringUtils.EMPTY);
                    remoteSizeField.setStringValue(StringUtils.EMPTY);
                    remoteModificationField.setStringValue(StringUtils.EMPTY);
                    localURLField.setStringValue(StringUtils.EMPTY);
                    localSizeField.setStringValue(StringUtils.EMPTY);
                    localModificationField.setStringValue(StringUtils.EMPTY);
                }
                else {
                    final TransferItem item = cache.lookup(new NSObjectTransferItemReference(
                            browserView.itemAtRow(browserView.selectedRow())));
                    if(item.local != null) {
                        localURLField.setAttributedStringValue(NSAttributedString.attributedStringWithAttributes(
                                item.local.getAbsolute(),
                                TRUNCATE_MIDDLE_ATTRIBUTES));
                        if(item.local.attributes().getSize() == -1) {
                            localSizeField.setAttributedStringValue(UNKNOWN_STRING);
                        }
                        else {
                            localSizeField.setAttributedStringValue(NSAttributedString.attributedStringWithAttributes(
                                    SizeFormatterFactory.get().format(item.local.attributes().getSize()),
                                    TRUNCATE_MIDDLE_ATTRIBUTES));
                        }
                        if(item.local.attributes().getModificationDate() == -1) {
                            localModificationField.setAttributedStringValue(UNKNOWN_STRING);
                        }
                        else {
                            localModificationField.setAttributedStringValue(NSAttributedString.attributedStringWithAttributes(
                                    UserDateFormatterFactory.get().getLongFormat(item.local.attributes().getModificationDate()),
                                    TRUNCATE_MIDDLE_ATTRIBUTES));
                        }
                    }
                    remoteURLField.setAttributedStringValue(NSAttributedString.attributedStringWithAttributes(
                            new DefaultUrlProvider(transfer.getSource()).toUrl(item.remote, EnumSet.of(DescriptiveUrl.Type.provider)).find(DescriptiveUrl.Type.provider).getUrl(),
                            TRUNCATE_MIDDLE_ATTRIBUTES));
                    final TransferStatus status = browserModel.getStatus(item);
                    if(status.getRemote().getSize() == -1) {
                        remoteSizeField.setAttributedStringValue(UNKNOWN_STRING);
                    }
                    else {
                        remoteSizeField.setAttributedStringValue(NSAttributedString.attributedStringWithAttributes(
                                SizeFormatterFactory.get().format(status.getRemote().getSize()),
                                TRUNCATE_MIDDLE_ATTRIBUTES));
                    }
                    if(status.getRemote().getModificationDate() == -1) {
                        remoteModificationField.setAttributedStringValue(UNKNOWN_STRING);
                    }
                    else {
                        remoteModificationField.setAttributedStringValue(NSAttributedString.attributedStringWithAttributes(
                                UserDateFormatterFactory.get().getLongFormat(status.getRemote().getModificationDate()),
                                TRUNCATE_MIDDLE_ATTRIBUTES));
                    }
                }
            }

            @Override
            protected boolean isTypeSelectSupported() {
                return true;
            }

            public String tableView_typeSelectStringForTableColumn_row(final NSTableView view, final NSTableColumn column, final NSInteger row) {
                return cache.lookup(new NSObjectTransferItemReference(browserView.itemAtRow(row))).remote.getName();
            }

            public void outlineView_willDisplayCell_forTableColumn_item(final NSOutlineView view, final NSCell cell,
                                                                        final NSTableColumn column, final NSObject item) {
                final String identifier = column.identifier();
                final TransferItem file = cache.lookup(new NSObjectTransferItemReference(item));
                final TransferStatus status = browserModel.getStatus(file);
                if(identifier.equals(TransferPromptDataSource.Column.include.name())) {
                    cell.setEnabled(!status.isRejected());
                }
                if(identifier.equals(TransferPromptDataSource.Column.filename.name())) {
                    (Rococoa.cast(cell, OutlineCell.class)).setIcon(IconCacheFactory.<NSImage>get().fileIcon(file.remote, 16));
                }
                if(cell.isKindOfClass(Foundation.getClass(NSTextFieldCell.class.getSimpleName()))) {
                    if(status.isRejected()) {
                        Rococoa.cast(cell, NSTextFieldCell.class).setTextColor(NSColor.disabledControlTextColor());
                    }
                    else {
                        Rococoa.cast(cell, NSTextFieldCell.class).setTextColor(NSColor.controlTextColor());
                    }
                }
            }
        }).id());
        // selection properties
        this.browserView.setAllowsMultipleSelection(true);
        this.browserView.setAllowsEmptySelection(true);
        this.browserView.setAllowsColumnResizing(true);
        this.browserView.setAllowsColumnSelection(false);
        this.browserView.setAllowsColumnReordering(true);
        this.browserView.setUsesAlternatingRowBackgroundColors(preferences.getBoolean("browser.alternatingRows"));
        if(preferences.getBoolean("browser.horizontalLines") && preferences.getBoolean("browser.verticalLines")) {
            this.browserView.setGridStyleMask(new NSUInteger(NSTableView.NSTableViewSolidHorizontalGridLineMask.intValue() | NSTableView.NSTableViewSolidVerticalGridLineMask.intValue()));
        }
        else if(preferences.getBoolean("browser.verticalLines")) {
            this.browserView.setGridStyleMask(NSTableView.NSTableViewSolidVerticalGridLineMask);
        }
        else if(preferences.getBoolean("browser.horizontalLines")) {
            this.browserView.setGridStyleMask(NSTableView.NSTableViewSolidHorizontalGridLineMask);
        }
        else {
            this.browserView.setGridStyleMask(NSTableView.NSTableViewGridNone);
        }
        this.browserView.sizeToFit();
    }

    public void setRemoteURLField(final NSTextField f) {
        this.remoteURLField = f;
    }

    public void setRemoteSizeField(final NSTextField f) {
        this.remoteSizeField = f;
    }

    public void setRemoteModificationField(final NSTextField f) {
        this.remoteModificationField = f;
    }

    public void setLocalURLField(final NSTextField f) {
        this.localURLField = f;
    }

    public void setLocalSizeField(final NSTextField f) {
        this.localSizeField = f;
    }

    public void setLocalModificationField(final NSTextField f) {
        this.localModificationField = f;
    }

    public void setStatusIndicator(final NSProgressIndicator f) {
        this.statusIndicator = f;
        this.statusIndicator.setDisplayedWhenStopped(false);
    }

    public void setStatusLabel(final NSTextField f) {
        this.statusLabel = f;
    }

    public void setActionPopup(final NSPopUpButton actionPopup) {
        this.actionPopup = actionPopup;
        this.actionPopup.removeAllItems();
        this.actionPopup.setAutoenablesItems(false);

        final TransferAction defaultAction
                = TransferAction.forName(preferences.getProperty(String.format("queue.prompt.%s.action.default", transfer.getType().name())));

        for(TransferAction action : TransferAction.forTransfer(transfer.getType())) {
            this.actionPopup.addItemWithTitle(action.getTitle());
            this.actionPopup.lastItem().setRepresentedObject(action.name());
            if(action.equals(defaultAction)) {
                this.actionPopup.selectItem(actionPopup.lastItem());
            }
            this.actionPopup.addItemWithTitle(action.getDescription());
            this.actionPopup.lastItem().setAttributedTitle(NSAttributedString.attributedStringWithAttributes(action.getDescription(), MENU_HELP_FONT_ATTRIBUTES));
            this.actionPopup.lastItem().setEnabled(false);
        }
        this.actionPopup.setTarget(this.id());
        this.actionPopup.setAction(Foundation.selector("actionPopupClicked:"));
    }

    @Action
    public void actionPopupClicked(NSPopUpButton sender) {
        final TransferAction selected = TransferAction.forName(sender.selectedItem().representedObject());
        if(action.equals(selected)) {
            return;
        }
        preferences.setProperty(String.format("queue.prompt.%s.action.default", transfer.getType().name()), selected.name());
        action = selected;
        browserModel.setAction(selected);
        this.reload();
    }

    @Override
    public void log(final Type request, final String message) {
        //
    }

    @Override
    public void stop(final BackgroundAction action) {
        this.invoke(new DefaultMainAction() {
            @Override
            public void run() {
                statusIndicator.stopAnimation(null);
            }
        });
    }

    @Override
    public void start(final BackgroundAction action) {
        this.invoke(new DefaultMainAction() {
            @Override
            public void run() {
                statusIndicator.startAnimation(null);
            }
        });
    }
}
