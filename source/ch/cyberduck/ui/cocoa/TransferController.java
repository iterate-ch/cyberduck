package ch.cyberduck.ui.cocoa;

/*
 * Copyright (c) 2002-2010 David Kocher. All rights reserved.
 *
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
 * Bug fixes, suggestions and comments should be sent to:
 * dkocher@cyberduck.ch
 */

import ch.cyberduck.binding.application.*;
import ch.cyberduck.binding.foundation.NSArray;
import ch.cyberduck.binding.foundation.NSIndexSet;
import ch.cyberduck.binding.foundation.NSNotification;
import ch.cyberduck.binding.foundation.NSNotificationCenter;
import ch.cyberduck.binding.foundation.NSRange;
import ch.cyberduck.core.AbstractCollectionListener;
import ch.cyberduck.core.Collection;
import ch.cyberduck.core.Factory;
import ch.cyberduck.core.LocalFactory;
import ch.cyberduck.core.LocaleFactory;
import ch.cyberduck.core.Path;
import ch.cyberduck.core.Session;
import ch.cyberduck.core.SessionFactory;
import ch.cyberduck.core.TransferCollection;
import ch.cyberduck.core.exception.AccessDeniedException;
import ch.cyberduck.core.formatter.SizeFormatterFactory;
import ch.cyberduck.core.io.BandwidthThrottle;
import ch.cyberduck.core.local.ApplicationLauncherFactory;
import ch.cyberduck.core.local.LocalTrashFactory;
import ch.cyberduck.core.local.RevealService;
import ch.cyberduck.core.local.RevealServiceFactory;
import ch.cyberduck.core.pasteboard.PathPasteboard;
import ch.cyberduck.core.pasteboard.PathPasteboardFactory;
import ch.cyberduck.core.preferences.Preferences;
import ch.cyberduck.core.preferences.PreferencesFactory;
import ch.cyberduck.core.resources.IconCacheFactory;
import ch.cyberduck.core.ssl.DefaultTrustManagerHostnameCallback;
import ch.cyberduck.core.ssl.KeychainX509KeyManager;
import ch.cyberduck.core.ssl.KeychainX509TrustManager;
import ch.cyberduck.core.threading.BackgroundAction;
import ch.cyberduck.core.threading.BackgroundActionRegistry;
import ch.cyberduck.core.threading.ControllerMainAction;
import ch.cyberduck.core.threading.TransferBackgroundAction;
import ch.cyberduck.core.threading.TransferCollectionBackgroundAction;
import ch.cyberduck.core.transfer.DownloadTransfer;
import ch.cyberduck.core.transfer.QueueFactory;
import ch.cyberduck.core.transfer.SyncTransfer;
import ch.cyberduck.core.transfer.Transfer;
import ch.cyberduck.core.transfer.TransferCallback;
import ch.cyberduck.core.transfer.TransferItem;
import ch.cyberduck.core.transfer.TransferListener;
import ch.cyberduck.core.transfer.TransferOptions;
import ch.cyberduck.core.transfer.TransferProgress;
import ch.cyberduck.core.transfer.TransferSpeedometer;
import ch.cyberduck.ui.browser.DownloadDirectoryFinder;
import ch.cyberduck.ui.cocoa.delegate.AbstractMenuDelegate;
import ch.cyberduck.ui.cocoa.threading.WindowMainAction;
import ch.cyberduck.ui.cocoa.view.ControllerCell;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.rococoa.Foundation;
import org.rococoa.ID;
import org.rococoa.Rococoa;
import org.rococoa.Selector;
import org.rococoa.cocoa.CGFloat;
import org.rococoa.cocoa.foundation.NSInteger;
import org.rococoa.cocoa.foundation.NSSize;
import org.rococoa.cocoa.foundation.NSUInteger;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;

/**
 * @version $Id$
 */
public final class TransferController extends WindowController implements NSToolbar.Delegate {
    private static final Logger log = Logger.getLogger(TransferController.class);

    private NSToolbar toolbar;

    private RevealService reveal = RevealServiceFactory.get();

    private Preferences preferences
            = PreferencesFactory.get();

    private TransferCollection collection = TransferCollection.defaultCollection();

    public TransferController() {
        this.loadBundle();
        collection.addListener(new AbstractCollectionListener<Transfer>() {
            @Override
            public void collectionLoaded() {
                invoke(new ControllerMainAction(TransferController.this) {
                    @Override
                    public void run() {
                        reload();
                    }
                });
            }

            @Override
            public void collectionItemAdded(Transfer item) {
                invoke(new ControllerMainAction(TransferController.this) {
                    @Override
                    public void run() {
                        reload();
                    }
                });
            }

            @Override
            public void collectionItemRemoved(Transfer item) {
                invoke(new ControllerMainAction(TransferController.this) {
                    @Override
                    public void run() {
                        reload();
                    }
                });
            }
        });
    }

    @Override
    public void awakeFromNib() {
        this.toolbar = NSToolbar.toolbarWithIdentifier("Transfer Toolbar");
        this.toolbar.setDelegate(this.id());
        this.toolbar.setAllowsUserCustomization(true);
        this.toolbar.setAutosavesConfiguration(true);
        this.toolbar.setDisplayMode(NSToolbar.NSToolbarDisplayModeLabelOnly);
        this.window.setToolbar(toolbar);

        TransferCollection source = TransferCollection.defaultCollection();
        if(!source.isLoaded()) {
            transferSpinner.startAnimation(null);
        }
        source.addListener(new AbstractCollectionListener<Transfer>() {
            @Override
            public void collectionLoaded() {
                invoke(new WindowMainAction(TransferController.this) {
                    @Override
                    public void run() {
                        transferSpinner.stopAnimation(null);
                        transferTable.setGridStyleMask(NSTableView.NSTableViewSolidHorizontalGridLineMask);
                    }
                });
            }
        });
        if(source.isLoaded()) {
            transferSpinner.stopAnimation(null);
            transferTable.setGridStyleMask(NSTableView.NSTableViewSolidHorizontalGridLineMask);
        }
        super.awakeFromNib();
    }


    @Override
    public void setWindow(NSWindow window) {
        window.setContentMinSize(new NSSize(400d, 150d));
        window.setMovableByWindowBackground(true);
        window.setTitle(LocaleFactory.localizedString("Transfers"));
        super.setWindow(window);
    }

    @Override
    public boolean isSingleton() {
        return true;
    }

    @Override
    public void windowDidBecomeKey(NSNotification notification) {
        this.updateHighlight();
    }

    @Override
    public void windowDidResignKey(NSNotification notification) {
        this.updateHighlight();
    }

    @Override
    public void windowDidBecomeMain(NSNotification notification) {
        this.updateHighlight();
    }

    @Override
    public void windowDidResignMain(NSNotification notification) {
        this.updateHighlight();
    }

    @Outlet
    private NSTextField urlField;

    public void setUrlField(NSTextField urlField) {
        this.urlField = urlField;
        this.urlField.setAllowsEditingTextAttributes(true);
        this.urlField.setSelectable(true);
    }

    @Outlet
    private NSTextField localField;

    public void setLocalField(NSTextField localField) {
        this.localField = localField;
        this.localField.setAllowsEditingTextAttributes(true);
        this.localField.setSelectable(true);
    }

    @Outlet
    private NSTextField localLabel;

    public void setLocalLabel(NSTextField localLabel) {
        this.localLabel = localLabel;
        this.localLabel.setStringValue(LocaleFactory.localizedString("Local File:", "Transfer"));
    }

    @Outlet
    private NSImageView iconView;

    public void setIconView(final NSImageView iconView) {
        this.iconView = iconView;
    }

    @Outlet
    private NSStepper queueSizeStepper;

    public void setQueueSizeStepper(final NSStepper queueSizeStepper) {
        this.queueSizeStepper = queueSizeStepper;
        this.queueSizeStepper.setTarget(this.id());
        this.queueSizeStepper.setAction(Foundation.selector("queueSizeStepperChanged:"));
    }

    @Action
    public void queueSizeStepperChanged(final NSStepper sender) {
        // Queue size propery is changed using key value observer
        QueueFactory.get().resize(sender.intValue());
    }

    @Outlet
    private NSTextField filterField;

    public void setFilterField(NSTextField filterField) {
        this.filterField = filterField;
        NSNotificationCenter.defaultCenter().addObserver(this.id(),
                Foundation.selector("filterFieldTextDidChange:"),
                NSControl.NSControlTextDidChangeNotification,
                this.filterField);
    }

    public void filterFieldTextDidChange(NSNotification notification) {
        transferTableModel.setFilter(filterField.stringValue());
        this.reload();
    }

    @Outlet
    NSProgressIndicator transferSpinner;

    public void setTransferSpinner(NSProgressIndicator transferSpinner) {
        this.transferSpinner = transferSpinner;
    }

    /**
     * Change focus to filter field
     *
     * @param sender Search field
     */
    @Action
    public void searchButtonClicked(final ID sender) {
        this.window().makeFirstResponder(this.filterField);
    }

    private TranscriptController transcript;

    private NSDrawer logDrawer;

    public void drawerDidOpen(NSNotification notification) {
        preferences.setProperty("queue.transcript.open", true);
    }

    public void drawerDidClose(NSNotification notification) {
        preferences.setProperty("queue.transcript.open", false);
        transcript.clear();
    }

    public NSSize drawerWillResizeContents_toSize(final NSDrawer sender, final NSSize contentSize) {
        return contentSize;
    }

    public void setLogDrawer(NSDrawer drawer) {
        this.logDrawer = drawer;
        this.transcript = new TranscriptController() {
            @Override
            public boolean isOpen() {
                return logDrawer.state() == NSDrawer.OpenState;
            }
        };
        this.logDrawer.setContentView(this.transcript.getLogView());
        this.logDrawer.setDelegate(this.id());
    }

    public void toggleLogDrawer(final ID sender) {
        this.logDrawer.toggle(sender);
    }

    @Outlet
    private NSPopUpButton bandwidthPopup;

    private AbstractMenuDelegate bandwidthPopupDelegate;

    public void setBandwidthPopup(NSPopUpButton bandwidthPopup) {
        this.bandwidthPopup = bandwidthPopup;
        this.bandwidthPopup.setEnabled(false);
        this.bandwidthPopup.setAllowsMixedState(true);
        this.bandwidthPopup.setTarget(this.id());
        this.bandwidthPopup.setAction(Foundation.selector("bandwidthPopupChanged:"));
        this.bandwidthPopup.removeAllItems();
        this.bandwidthPopup.addItemWithTitle(StringUtils.EMPTY);
        this.bandwidthPopup.lastItem().setImage(IconCacheFactory.<NSImage>get().iconNamed("bandwidth.tiff", 16));
        this.bandwidthPopup.addItemWithTitle(LocaleFactory.localizedString("Unlimited Bandwidth", "Transfer"));
        this.bandwidthPopup.lastItem().setRepresentedObject(String.valueOf(BandwidthThrottle.UNLIMITED));
        this.bandwidthPopup.menu().addItem(NSMenuItem.separatorItem());
        final StringTokenizer options = new StringTokenizer(preferences.getProperty("queue.bandwidth.options"), ",");
        while(options.hasMoreTokens()) {
            final String bytes = options.nextToken();
            this.bandwidthPopup.addItemWithTitle(SizeFormatterFactory.get().format(Integer.parseInt(bytes)) + "/s");
            this.bandwidthPopup.lastItem().setRepresentedObject(bytes);
        }
        this.bandwidthPopup.menu().setDelegate((this.bandwidthPopupDelegate = new BandwidthMenuDelegate()).id());
    }

    private class BandwidthMenuDelegate extends AbstractMenuDelegate {
        @Override
        public NSInteger numberOfItemsInMenu(NSMenu menu) {
            return new NSInteger(new StringTokenizer(preferences.getProperty("queue.bandwidth.options"), ",").countTokens() + 3);
        }

        @Override
        public boolean menuUpdateItemAtIndex(NSMenu menu, NSMenuItem item, NSInteger i, boolean cancel) {
            if(item.representedObject() != null) {
                final int selected = transferTable.numberOfSelectedRows().intValue();
                final int bytes = Integer.valueOf(item.representedObject());
                final NSIndexSet iterator = transferTable.selectedRowIndexes();
                for(NSUInteger index = iterator.firstIndex(); !index.equals(NSIndexSet.NSNotFound); index = iterator.indexGreaterThanIndex(index)) {
                    final Transfer transfer = collection.get(index.intValue());
                    if(BandwidthThrottle.UNLIMITED == transfer.getBandwidth().getRate()) {
                        if(BandwidthThrottle.UNLIMITED == bytes) {
                            item.setState(selected > 1 ? NSCell.NSMixedState : NSCell.NSOnState);
                            break;
                        }
                        else {
                            item.setState(NSCell.NSOffState);
                        }
                    }
                    else {
                        final int bandwidth = (int) transfer.getBandwidth().getRate();
                        if(bytes == bandwidth) {
                            item.setState(selected > 1 ? NSCell.NSMixedState : NSCell.NSOnState);
                            break;
                        }
                        else {
                            item.setState(NSCell.NSOffState);
                        }
                    }
                }
            }
            return super.menuUpdateItemAtIndex(menu, item, i, cancel);
        }

        @Override
        protected Selector getDefaultAction() {
            return Foundation.selector("bandwidthPopupChanged:");
        }
    }

    @Action
    public void bandwidthPopupChanged(NSPopUpButton sender) {
        NSIndexSet selected = transferTable.selectedRowIndexes();
        float bandwidth = Float.valueOf(sender.selectedItem().representedObject());
        for(NSUInteger index = selected.firstIndex(); !index.equals(NSIndexSet.NSNotFound); index = selected.indexGreaterThanIndex(index)) {
            final Transfer transfer = collection.get(index.intValue());
            transfer.setBandwidth(bandwidth);
            if(transfer.isRunning()) {
                final BackgroundActionRegistry registry = this.getActions();
                // Find matching background task
                for(BackgroundAction action : registry.toArray(new BackgroundAction[registry.size()])) {
                    if(action instanceof TransferBackgroundAction) {
                        final TransferBackgroundAction t = (TransferBackgroundAction) action;
                        if(t.getTransfer().equals(transfer)) {
                            final TransferSpeedometer meter = t.getMeter();
                            meter.reset();
                        }
                    }
                }
            }
        }
        this.updateBandwidthPopup();
    }

    @Override
    protected String getBundleName() {
        return "Transfer";
    }

    @Override
    public void invalidate() {
        toolbar.setDelegate(null);
        toolbarItems.clear();
        transferTableModel.invalidate();
        bandwidthPopup.menu().setDelegate(null);
        super.invalidate();
    }

    private final TableColumnFactory tableColumnsFactory = new TableColumnFactory();

    @Outlet
    private NSTableView transferTable;
    private TransferTableDataSource transferTableModel;
    private AbstractTableDelegate<Transfer> transferTableDelegate;

    public void setQueueTable(NSTableView view) {
        this.transferTable = view;
        this.transferTable.setRowHeight(new CGFloat(82));
        {
            NSTableColumn c = tableColumnsFactory.create(TransferTableDataSource.Column.progress.name());
            c.setMinWidth(80f);
            c.setWidth(300f);
            c.setResizingMask(NSTableColumn.NSTableColumnAutoresizingMask);
            c.setDataCell(prototype);
            this.transferTable.addTableColumn(c);
        }
        this.transferTable.setDataSource((transferTableModel = new TransferTableDataSource()).id());
        this.transferTable.setDelegate((transferTableDelegate = new AbstractTableDelegate<Transfer>(
                transferTable.tableColumnWithIdentifier(TransferTableDataSource.Column.progress.name())
        ) {
            @Override
            public String tooltip(final Transfer t) {
                return t.getName();
            }

            @Override
            public void enterKeyPressed(final ID sender) {
                this.tableRowDoubleClicked(sender);
            }

            @Override
            public void deleteKeyPressed(final ID sender) {
                deleteButtonClicked(sender);
            }

            @Override
            public void tableColumnClicked(NSTableView view, NSTableColumn tableColumn) {
                //
            }

            @Override
            public void tableRowDoubleClicked(final ID sender) {
                reloadButtonClicked(sender);
            }

            @Override
            public void selectionIsChanging(final NSNotification notification) {
                updateHighlight();
            }

            @Override
            public void selectionDidChange(final NSNotification notification) {
                updateHighlight();
                updateSelection();
                transferTable.noteHeightOfRowsWithIndexesChanged(
                        NSIndexSet.indexSetWithIndexesInRange(
                                NSRange.NSMakeRange(new NSUInteger(0), new NSUInteger(transferTable.numberOfRows()))));
            }

            public void tableView_willDisplayCell_forTableColumn_row(final NSTableView view, final NSCell cell,
                                                                     final NSTableColumn column, final NSInteger row) {
                if(Factory.Platform.osversion.matches("10\\.(5|6).*")) {
                    Rococoa.cast(cell, ControllerCell.class).setView(transferTableModel.getController(row.intValue()).view());
                }
            }

            public NSView tableView_viewForTableColumn_row(final NSTableView view, final NSTableColumn tableColumn,
                                                           final NSInteger row) {
                if(!Factory.Platform.osversion.matches("10\\.(5|6).*")) {
                    // 10.7 or later supports view View-Based Table Views
                    return transferTableModel.getController(row.intValue()).view();
                }
                return null;
            }

            @Override
            public boolean isTypeSelectSupported() {
                return true;
            }

            public String tableView_typeSelectStringForTableColumn_row(final NSTableView view, final NSTableColumn column, final NSInteger row) {
                return transferTableModel.getSource().get(row.intValue()).getName();
            }
        }).id());
        // receive drag events from types
        // in fact we are not interested in file promises, but because the browser model can only initiate
        // a drag with tableView.dragPromisedFilesOfTypes(), we listens for those events
        // and then use the private pasteboard instead.
        this.transferTable.registerForDraggedTypes(NSArray.arrayWithObjects(
                NSPasteboard.StringPboardType,
                // Accept file promises made myself
                NSPasteboard.FilesPromisePboardType));

        this.transferTable.setGridStyleMask(NSTableView.NSTableViewGridNone);
        //selection properties
        this.transferTable.setAllowsMultipleSelection(true);
        this.transferTable.setAllowsEmptySelection(true);
        this.transferTable.setAllowsColumnReordering(false);
        this.transferTable.sizeToFit();
    }

    private final NSCell prototype = ControllerCell.controllerCell();

    /**
     * Update highlighted rows
     */
    private void updateHighlight() {
        boolean main = window().isMainWindow();
        NSIndexSet set = transferTable.selectedRowIndexes();
        for(int i = 0; i < transferTableModel.numberOfRowsInTableView(transferTable).intValue(); i++) {
            boolean highlighted = set.containsIndex(new NSUInteger(i)) && main;
            if(transferTableModel.isHighlighted(i) == highlighted) {
                continue;
            }
            transferTableModel.setHighlighted(i, highlighted);
        }
    }

    /**
     * Update labels from selection
     */
    private void updateSelection() {
        this.updateLabels();
        this.updateIcon();
        this.updateBandwidthPopup();
        toolbar.validateVisibleItems();
    }

    private void updateLabels() {
        final int selected = transferTable.numberOfSelectedRows().intValue();
        if(1 == selected) {
            final Transfer transfer = transferTableModel.getSource().get(transferTable.selectedRow().intValue());
            // Draw text fields at the bottom
            final String remote = transfer.getRemote();
            urlField.setAttributedStringValue(HyperlinkAttributedStringFactory.create(remote));
            final String local = transfer.getLocal();
            if(local != null) {
                localField.setAttributedStringValue(
                        HyperlinkAttributedStringFactory.create(local, LocalFactory.get(local)));
            }
            else {
                localField.setStringValue(StringUtils.EMPTY);
            }
        }
        else {
            urlField.setStringValue(StringUtils.EMPTY);
            localField.setStringValue(StringUtils.EMPTY);
        }
    }

    private void updateIcon() {
        final int selected = transferTable.numberOfSelectedRows().intValue();
        if(1 == selected) {
            final Transfer transfer = transferTableModel.getSource().get(transferTable.selectedRow().intValue());
            // Draw file type icon
            if(transfer.getRoots().size() == 1) {
                if(transfer.getLocal() != null) {
                    iconView.setImage(IconCacheFactory.<NSImage>get().fileIcon(transfer.getRoot().local, 32));
                }
                else {
                    iconView.setImage(IconCacheFactory.<NSImage>get().fileIcon(transfer.getRoot().remote, 32));
                }
            }
            else {
                iconView.setImage(IconCacheFactory.<NSImage>get().iconNamed("NSMultipleDocuments", 32));
            }
        }
        else {
            iconView.setImage(null);
        }
    }

    private void updateBandwidthPopup() {
        final int selected = transferTable.numberOfSelectedRows().intValue();
        bandwidthPopup.setEnabled(selected > 0);
        NSIndexSet set = transferTable.selectedRowIndexes();
        for(NSUInteger index = set.firstIndex(); !index.equals(NSIndexSet.NSNotFound); index = set.indexGreaterThanIndex(index)) {
            final Transfer transfer = transferTableModel.getSource().get(index.intValue());
            if(transfer instanceof SyncTransfer) {
                // Currently we do not support bandwidth throtling for sync transfers due to
                // the problem of mapping both download and upload rate in the GUI
                bandwidthPopup.setEnabled(false);
                // Break through and set the standard icon below
                break;
            }
            if(transfer.getBandwidth().getRate() != BandwidthThrottle.UNLIMITED) {
                // Mark as throttled
                this.bandwidthPopup.itemAtIndex(new NSInteger(0)).setImage(IconCacheFactory.<NSImage>get().iconNamed("turtle.tiff"));
                return;
            }
        }
        // Set the standard icon
        this.bandwidthPopup.itemAtIndex(new NSInteger(0)).setImage(IconCacheFactory.<NSImage>get().iconNamed("bandwidth.tiff", 16));
    }

    private void reload() {
        while(transferTable.subviews().count().intValue() > 0) {
            (Rococoa.cast(transferTable.subviews().lastObject(), NSView.class)).removeFromSuperviewWithoutNeedingDisplay();
        }
        transferTable.reloadData();
        this.updateHighlight();
        this.updateSelection();
    }

    /**
     * Add this item to the list; select it and scroll the view to make it visible
     *
     * @param transfer Transfer
     */
    public void add(final Transfer transfer, final BackgroundAction action) {
        if(collection.size() > preferences.getInteger("queue.size.warn")) {
            final NSAlert alert = NSAlert.alert(
                    TransferToolbarItem.cleanup.label(), //title
                    LocaleFactory.localizedString("Remove completed transfers from list."), // message
                    TransferToolbarItem.cleanup.label(), // defaultbutton
                    LocaleFactory.localizedString("Cancel"), // alternate button
                    null //other button
            );
            alert.setShowsSuppressionButton(true);
            alert.suppressionButton().setTitle(LocaleFactory.localizedString("Don't ask again", "Configuration"));
            this.alert(alert, new SheetCallback() {
                @Override
                public void callback(int returncode) {
                    if(alert.suppressionButton().state() == NSCell.NSOnState) {
                        // Never show again.
                        preferences.setProperty("queue.size.warn", Integer.MAX_VALUE);
                    }
                    if(returncode == DEFAULT_OPTION) {
                        clearButtonClicked(null);
                    }
                    add(transfer);
                    background(action);
                }
            });
        }
        else {
            this.add(transfer);
            this.background(action);
        }
    }

    private void add(final Transfer transfer) {
        collection.add(transfer);
        final int row = collection.size() - 1;
        final NSInteger index = new NSInteger(row);
        transferTable.selectRowIndexes(NSIndexSet.indexSetWithIndex(index), false);
        transferTable.scrollRowToVisible(index);
    }

    /**
     * @param transfer Transfer
     */
    public void start(final Transfer transfer) {
        this.start(transfer, new TransferOptions());
    }

    /**
     * @param transfer Transfer
     */
    public void start(final Transfer transfer, final TransferOptions options) {
        this.start(transfer, options, new TransferCallback() {
            @Override
            public void complete(final Transfer transfer) {
                //
            }
        });
    }

    /**
     * @param transfer Transfer
     */
    public void start(final Transfer transfer, final TransferOptions options, final TransferCallback callback) {
        final ProgressController progress = transferTableModel.getController(transfer);
        final Session session = SessionFactory.create(transfer.getHost(),
                new KeychainX509TrustManager(new DefaultTrustManagerHostnameCallback(transfer.getHost())),
                new KeychainX509KeyManager());
        final BackgroundAction action = new TransferCollectionBackgroundAction(this,
                session,
                new TransferListener() {
                    @Override
                    public void start(final Transfer transfer) {
                        progress.start(transfer);
                        toolbar.validateVisibleItems();
                    }

                    @Override
                    public void stop(final Transfer transfer) {
                        progress.stop(transfer);
                        toolbar.validateVisibleItems();
                    }

                    @Override
                    public void progress(final TransferProgress status) {
                        progress.progress(status);
                    }
                }, progress, transcript, transfer, options) {
            @Override
            public void init() {
                super.init();
                if(preferences.getBoolean("queue.window.open.transfer.start")) {
                    window.makeKeyAndOrderFront(null);
                }
            }

            @Override
            public void finish() {
                super.finish();
                if(transfer.isComplete()) {
                    callback.complete(transfer);
                }
            }

            @Override
            public void cleanup() {
                super.cleanup();
                if(transfer.isReset() && transfer.isComplete()) {
                    if(preferences.getBoolean("queue.window.open.transfer.stop")) {
                        if(!(collection.numberOfRunningTransfers() > 0)) {
                            window.close();
                        }
                    }
                }
            }
        };
        if(!collection.contains(transfer)) {
            this.add(transfer, action);
        }
        else {
            this.background(action);
        }
    }

    @Override
    public void log(final boolean request, final String message) {
        transcript.log(request, message);
    }

    private enum TransferToolbarItem {
        resume {
            @Override
            public String label() {
                return LocaleFactory.localizedString("Resume", "Transfer");
            }
        },
        reload,
        stop,
        remove,
        cleanup {
            @Override
            public String label() {
                return LocaleFactory.localizedString("Clean Up");
            }
        },
        open,
        show,
        trash,
        log,
        search;

        public String label() {
            return LocaleFactory.localizedString(StringUtils.capitalize(this.name()));
        }
    }

    /**
     * Keep reference to weak toolbar items
     */
    private Map<String, NSToolbarItem> toolbarItems
            = new HashMap<String, NSToolbarItem>();

    @Override
    public NSToolbarItem toolbar_itemForItemIdentifier_willBeInsertedIntoToolbar(final NSToolbar toolbar, final String identifier, final boolean flag) {
        if(!toolbarItems.containsKey(identifier)) {
            toolbarItems.put(identifier, NSToolbarItem.itemWithIdentifier(identifier));
        }
        final NSToolbarItem item = toolbarItems.get(identifier);
        switch(TransferToolbarItem.valueOf(identifier)) {
            case resume:
                item.setLabel(TransferToolbarItem.resume.label());
                item.setPaletteLabel(TransferToolbarItem.resume.label());
                item.setToolTip(TransferToolbarItem.resume.label());
                item.setImage(IconCacheFactory.<NSImage>get().iconNamed("resume.tiff"));
                item.setTarget(this.id());
                item.setAction(Foundation.selector("resumeButtonClicked:"));
                return item;
            case reload:
                item.setLabel(TransferToolbarItem.reload.label());
                item.setPaletteLabel(TransferToolbarItem.reload.label());
                item.setToolTip(TransferToolbarItem.reload.label());
                item.setImage(IconCacheFactory.<NSImage>get().iconNamed("reload.tiff"));
                item.setTarget(this.id());
                item.setAction(Foundation.selector("reloadButtonClicked:"));
                return item;
            case stop:
                item.setLabel(TransferToolbarItem.stop.label());
                item.setPaletteLabel(TransferToolbarItem.stop.label());
                item.setToolTip(TransferToolbarItem.stop.label());
                item.setImage(IconCacheFactory.<NSImage>get().iconNamed("stop.tiff", 32));
                item.setTarget(this.id());
                item.setAction(Foundation.selector("stopButtonClicked:"));
                return item;
            case remove:
                item.setLabel(TransferToolbarItem.remove.label());
                item.setPaletteLabel(TransferToolbarItem.remove.label());
                item.setToolTip(TransferToolbarItem.remove.label());
                item.setImage(IconCacheFactory.<NSImage>get().iconNamed("clean.tiff"));
                item.setTarget(this.id());
                item.setAction(Foundation.selector("deleteButtonClicked:"));
                return item;
            case cleanup:
                item.setLabel(TransferToolbarItem.cleanup.label());
                item.setPaletteLabel(TransferToolbarItem.cleanup.label());
                item.setToolTip(TransferToolbarItem.cleanup.label());
                item.setImage(IconCacheFactory.<NSImage>get().iconNamed("cleanall.tiff"));
                item.setTarget(this.id());
                item.setAction(Foundation.selector("clearButtonClicked:"));
                return item;
            case open:
                item.setLabel(TransferToolbarItem.open.label());
                item.setPaletteLabel(TransferToolbarItem.open.label());
                item.setToolTip(TransferToolbarItem.open.label());
                item.setImage(IconCacheFactory.<NSImage>get().iconNamed("open.tiff"));
                item.setTarget(this.id());
                item.setAction(Foundation.selector("openButtonClicked:"));
                return item;
            case show:
                item.setLabel(TransferToolbarItem.show.label());
                item.setPaletteLabel(LocaleFactory.localizedString("Show in Finder"));
                item.setToolTip(LocaleFactory.localizedString("Show in Finder"));
                item.setImage(IconCacheFactory.<NSImage>get().iconNamed("reveal.tiff"));
                item.setTarget(this.id());
                item.setAction(Foundation.selector("revealButtonClicked:"));
                return item;
            case trash:
                item.setLabel(TransferToolbarItem.trash.label());
                item.setPaletteLabel(TransferToolbarItem.trash.label());
                item.setToolTip(LocaleFactory.localizedString("Move to Trash"));
                item.setImage(IconCacheFactory.<NSImage>get().iconNamed("trash.tiff"));
                item.setTarget(this.id());
                item.setAction(Foundation.selector("trashButtonClicked:"));
                return item;
            case log:
                item.setLabel(TransferToolbarItem.log.label());
                item.setPaletteLabel(TransferToolbarItem.log.label());
                item.setToolTip(LocaleFactory.localizedString("Toggle Log Drawer"));
                item.setImage(IconCacheFactory.<NSImage>get().iconNamed("log.tiff"));
                item.setTarget(this.id());
                item.setAction(Foundation.selector("toggleLogDrawer:"));
                break;
            case search:
                item.setLabel(TransferToolbarItem.search.label());
                item.setPaletteLabel(TransferToolbarItem.search.label());
                item.setView(filterField);
                return item;
        }
        // Identifier refered to a toolbar item that is not provide or supported.
        // Returning null will inform the toolbar this kind of item is not supported.
        return null;
    }

    @Action
    public void paste(final ID sender) {
        for(PathPasteboard pasteboard : PathPasteboardFactory.allPasteboards()) {
            if(pasteboard.isEmpty()) {
                continue;
            }
            if(log.isDebugEnabled()) {
                log.debug("Paste download transfer from pasteboard");
            }
            final List<TransferItem> downloads = new ArrayList<TransferItem>();
            for(Path download : pasteboard) {
                downloads.add(new TransferItem(download, LocalFactory.get(
                        new DownloadDirectoryFinder().find(pasteboard.getSession().getHost()),
                        download.getName())));
            }
            this.add(new DownloadTransfer(pasteboard.getSession().getHost(), downloads));
            pasteboard.clear();
        }
    }

    @Action
    public void stopButtonClicked(final ID sender) {
        NSIndexSet selected = transferTable.selectedRowIndexes();
        final BackgroundActionRegistry registry = this.getActions();
        for(NSUInteger index = selected.firstIndex(); !index.equals(NSIndexSet.NSNotFound); index = selected.indexGreaterThanIndex(index)) {
            final Transfer transfer = transferTableModel.getSource().get(index.intValue());
            if(transfer.isRunning()) {
                // Find matching background task
                for(BackgroundAction action : registry.toArray(new BackgroundAction[registry.size()])) {
                    if(action instanceof TransferBackgroundAction) {
                        final TransferBackgroundAction t = (TransferBackgroundAction) action;
                        if(t.getTransfer().equals(transfer)) {
                            t.cancel();
                        }
                    }
                }
            }
        }
    }

    @Action
    public void stopAllButtonClicked(final ID sender) {
        final Collection<Transfer> transfers = transferTableModel.getSource();
        final BackgroundActionRegistry registry = this.getActions();
        for(final Transfer transfer : transfers) {
            if(transfer.isRunning()) {
                // Find matching background task
                for(BackgroundAction action : registry.toArray(new BackgroundAction[registry.size()])) {
                    if(action instanceof TransferBackgroundAction) {
                        final TransferBackgroundAction t = (TransferBackgroundAction) action;
                        if(t.getTransfer().equals(transfer)) {
                            t.cancel();
                        }
                    }
                }
            }
        }
    }

    @Action
    public void resumeButtonClicked(final ID sender) {
        NSIndexSet selected = transferTable.selectedRowIndexes();
        for(NSUInteger index = selected.firstIndex(); !index.equals(NSIndexSet.NSNotFound); index = selected.indexGreaterThanIndex(index)) {
            final Collection<Transfer> transfers = transferTableModel.getSource();
            final Transfer transfer = transfers.get(index.intValue());
            if(!transfer.isRunning()) {
                final TransferOptions options = new TransferOptions();
                options.resumeRequested = true;
                options.reloadRequested = false;
                this.start(transfer, options);
            }
        }
    }

    @Action
    public void reloadButtonClicked(final ID sender) {
        NSIndexSet selected = transferTable.selectedRowIndexes();
        for(NSUInteger index = selected.firstIndex(); !index.equals(NSIndexSet.NSNotFound); index = selected.indexGreaterThanIndex(index)) {
            final Collection<Transfer> transfers = transferTableModel.getSource();
            final Transfer transfer = transfers.get(index.intValue());
            if(!transfer.isRunning()) {
                final TransferOptions options = new TransferOptions();
                options.resumeRequested = false;
                options.reloadRequested = true;
                this.start(transfer, options);
            }
        }
    }

    @Action
    public void openButtonClicked(final ID sender) {
        if(transferTable.numberOfSelectedRows().intValue() == 1) {
            final Transfer transfer = transferTableModel.getSource().get(transferTable.selectedRow().intValue());
            for(TransferItem l : transfer.getRoots()) {
                ApplicationLauncherFactory.get().open(l.local);
            }
        }
    }

    @Action
    public void revealButtonClicked(final ID sender) {
        NSIndexSet selected = transferTable.selectedRowIndexes();
        final Collection<Transfer> transfers = transferTableModel.getSource();
        for(NSUInteger index = selected.firstIndex(); !index.equals(NSIndexSet.NSNotFound); index = selected.indexGreaterThanIndex(index)) {
            final Transfer transfer = transfers.get(index.intValue());
            for(TransferItem l : transfer.getRoots()) {
                reveal.reveal(l.local);
            }
        }
    }

    @Action
    public void deleteButtonClicked(final ID sender) {
        NSIndexSet selected = transferTable.selectedRowIndexes();
        final Collection<Transfer> transfers = transferTableModel.getSource();
        int i = 0;
        final List<Transfer> remove = new ArrayList<Transfer>();
        for(NSUInteger index = selected.firstIndex(); !index.equals(NSIndexSet.NSNotFound); index = selected.indexGreaterThanIndex(index)) {
            final Transfer transfer = transfers.get(index.intValue() - i);
            if(!transfer.isRunning()) {
                remove.add(transfer);
            }
        }
        for(Transfer t : remove) {
            collection.remove(t);
        }
        collection.save();
    }

    @Action
    public void clearButtonClicked(final ID sender) {
        for(Iterator<Transfer> iter = collection.iterator(); iter.hasNext(); ) {
            Transfer transfer = iter.next();
            if(!transfer.isRunning() && transfer.isComplete()) {
                iter.remove();
            }
        }
        collection.save();
    }

    @Action
    public void trashButtonClicked(final ID sender) {
        NSIndexSet selected = transferTable.selectedRowIndexes();
        final Collection<Transfer> transfers = transferTableModel.getSource();
        for(NSUInteger index = selected.firstIndex(); !index.equals(NSIndexSet.NSNotFound); index = selected.indexGreaterThanIndex(index)) {
            final Transfer transfer = transfers.get(index.intValue());
            if(!transfer.isRunning()) {
                for(TransferItem l : transfer.getRoots()) {
                    try {
                        LocalTrashFactory.get().trash(l.local);
                    }
                    catch(AccessDeniedException e) {
                        log.warn(String.format("Failure trashing file %s %s", l.local, e.getMessage()));
                    }
                }
            }
        }
        this.updateIcon();
    }

    @Action
    public void printDocument(final ID sender) {
        this.print(transferTable);
    }

    /**
     * NSToolbar.Delegate
     *
     * @param toolbar Window toolbar
     */
    @Override
    public NSArray toolbarDefaultItemIdentifiers(final NSToolbar toolbar) {
        return NSArray.arrayWithObjects(
                TransferToolbarItem.resume.name(),
                TransferToolbarItem.stop.name(),
                TransferToolbarItem.reload.name(),
                TransferToolbarItem.remove.name(),
                TransferToolbarItem.show.name(),
                NSToolbarItem.NSToolbarFlexibleItemIdentifier,
                TransferToolbarItem.search.name()
        );
    }

    /**
     * NSToolbar.Delegate
     *
     * @param toolbar Window toolbar
     */
    @Override
    public NSArray toolbarAllowedItemIdentifiers(final NSToolbar toolbar) {
        return NSArray.arrayWithObjects(
                TransferToolbarItem.resume.name(),
                TransferToolbarItem.reload.name(),
                TransferToolbarItem.stop.name(),
                TransferToolbarItem.remove.name(),
                TransferToolbarItem.cleanup.name(),
                TransferToolbarItem.show.name(),
                TransferToolbarItem.open.name(),
                TransferToolbarItem.trash.name(),
                TransferToolbarItem.search.name(),
                TransferToolbarItem.log.name(),
                NSToolbarItem.NSToolbarCustomizeToolbarItemIdentifier,
                NSToolbarItem.NSToolbarSpaceItemIdentifier,
                NSToolbarItem.NSToolbarSeparatorItemIdentifier,
                NSToolbarItem.NSToolbarFlexibleSpaceItemIdentifier
        );
    }

    @Override
    public NSArray toolbarSelectableItemIdentifiers(final NSToolbar toolbar) {
        return NSArray.array();
    }

    /**
     * @param item Menu item
     * @return True if enabled
     */
    public boolean validateMenuItem(final NSMenuItem item) {
        final Selector action = item.action();
        if(action.equals(Foundation.selector("paste:"))) {
            final List<PathPasteboard> pasteboards = PathPasteboardFactory.allPasteboards();
            if(pasteboards.size() == 1) {
                for(PathPasteboard pasteboard : pasteboards) {
                    if(pasteboard.size() == 1) {
                        item.setTitle(MessageFormat.format(LocaleFactory.localizedString("Paste {0}"), pasteboard.get(0).getName()));
                    }
                    else {
                        item.setTitle(MessageFormat.format(LocaleFactory.localizedString("Paste {0}"),
                                MessageFormat.format(LocaleFactory.localizedString("{0} Files"), String.valueOf(pasteboard.size())) + ")"));
                    }
                }
            }
            else {
                item.setTitle(LocaleFactory.localizedString("Paste"));
            }
        }
        return this.validateItem(action);
    }

    /**
     * @param item Toolbar item
     */
    @Override
    public boolean validateToolbarItem(final NSToolbarItem item) {
        return this.validateItem(item.action());
    }

    /**
     * Validates menu and toolbar items
     *
     * @param action Method target
     * @return true if the item with the identifier should be selectable
     */
    private boolean validateItem(final Selector action) {
        if(action.equals(Foundation.selector("paste:"))) {
            return !PathPasteboardFactory.allPasteboards().isEmpty();
        }
        if(action.equals(Foundation.selector("stopButtonClicked:"))) {
            return this.validate(new TransferToolbarValidator() {
                @Override
                public boolean validate(final Transfer transfer) {
                    return transfer.isRunning();
                }
            });
        }
        if(action.equals(Foundation.selector("reloadButtonClicked:"))) {
            return this.validate(new TransferToolbarValidator() {
                @Override
                public boolean validate(final Transfer transfer) {
                    return transfer.getType().isReloadable() && !transfer.isRunning();
                }
            });
        }
        if(action.equals(Foundation.selector("deleteButtonClicked:"))) {
            return this.validate(new TransferToolbarValidator() {
                @Override
                public boolean validate(final Transfer transfer) {
                    return !transfer.isRunning();
                }
            });
        }
        if(action.equals(Foundation.selector("resumeButtonClicked:"))) {
            return this.validate(new TransferToolbarValidator() {
                @Override
                public boolean validate(final Transfer transfer) {
                    if(transfer.isRunning()) {
                        return false;
                    }
                    return !transfer.isComplete();
                }
            });
        }
        if(action.equals(Foundation.selector("openButtonClicked:"))
                || action.equals(Foundation.selector("trashButtonClicked:"))) {
            return this.validate(new TransferToolbarValidator() {
                @Override
                public boolean validate(final Transfer transfer) {
                    if(transfer.getLocal() != null) {
                        if(!transfer.isComplete()) {
                            return false;
                        }
                        if(!transfer.isRunning()) {
                            for(TransferItem l : transfer.getRoots()) {
                                if(l.local.exists()) {
                                    return true;
                                }
                            }
                        }
                    }
                    return false;
                }
            });
        }
        if(action.equals(Foundation.selector("revealButtonClicked:"))) {
            return this.validate(new TransferToolbarValidator() {
                @Override
                public boolean validate(final Transfer transfer) {
                    if(transfer.getLocal() != null) {
                        for(TransferItem l : transfer.getRoots()) {
                            if(l.local.exists()) {
                                return true;
                            }
                        }
                    }
                    return false;
                }
            });
        }
        if(action.equals(Foundation.selector("clearButtonClicked:"))) {
            return transferTable.numberOfRows().intValue() > 0;
        }
        return true;
    }

    /**
     * Validates the selected items in the transfer window against the toolbar validator
     *
     * @param validator The validator to use
     * @return True if one or more of the selected items passes the validation test
     */
    private boolean validate(final TransferToolbarValidator validator) {
        final NSIndexSet iterator = transferTable.selectedRowIndexes();
        final Collection<Transfer> transfers = transferTableModel.getSource();
        for(NSUInteger index = iterator.firstIndex(); !index.equals(NSIndexSet.NSNotFound); index = iterator.indexGreaterThanIndex(index)) {
            final Transfer transfer = transfers.get(index.intValue());
            if(validator.validate(transfer)) {
                return true;
            }
        }
        return false;
    }

    private interface TransferToolbarValidator {
        boolean validate(Transfer transfer);
    }
}
