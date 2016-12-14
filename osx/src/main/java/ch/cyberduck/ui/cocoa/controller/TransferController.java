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

import ch.cyberduck.binding.AbstractTableDelegate;
import ch.cyberduck.binding.Action;
import ch.cyberduck.binding.Delegate;
import ch.cyberduck.binding.DisabledSheetCallback;
import ch.cyberduck.binding.HyperlinkAttributedStringFactory;
import ch.cyberduck.binding.Outlet;
import ch.cyberduck.binding.WindowController;
import ch.cyberduck.binding.application.*;
import ch.cyberduck.binding.foundation.NSArray;
import ch.cyberduck.binding.foundation.NSIndexSet;
import ch.cyberduck.binding.foundation.NSNotification;
import ch.cyberduck.binding.foundation.NSNotificationCenter;
import ch.cyberduck.binding.foundation.NSRange;
import ch.cyberduck.core.AbstractCollectionListener;
import ch.cyberduck.core.Collection;
import ch.cyberduck.core.Host;
import ch.cyberduck.core.LocalFactory;
import ch.cyberduck.core.LocaleFactory;
import ch.cyberduck.core.Path;
import ch.cyberduck.core.PathCache;
import ch.cyberduck.core.SessionPoolFactory;
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
import ch.cyberduck.core.pool.SessionPool;
import ch.cyberduck.core.preferences.Preferences;
import ch.cyberduck.core.preferences.PreferencesFactory;
import ch.cyberduck.core.resources.IconCacheFactory;
import ch.cyberduck.core.threading.BackgroundAction;
import ch.cyberduck.core.threading.BackgroundActionRegistry;
import ch.cyberduck.core.threading.ControllerMainAction;
import ch.cyberduck.core.threading.DefaultMainAction;
import ch.cyberduck.core.threading.TransferBackgroundAction;
import ch.cyberduck.core.threading.TransferCollectionBackgroundAction;
import ch.cyberduck.core.threading.WindowMainAction;
import ch.cyberduck.core.transfer.DownloadTransfer;
import ch.cyberduck.core.transfer.Transfer;
import ch.cyberduck.core.transfer.TransferAdapter;
import ch.cyberduck.core.transfer.TransferCallback;
import ch.cyberduck.core.transfer.TransferItem;
import ch.cyberduck.core.transfer.TransferOptions;
import ch.cyberduck.core.transfer.TransferProgress;
import ch.cyberduck.core.transfer.TransferQueueFactory;
import ch.cyberduck.core.transfer.TransferSpeedometer;
import ch.cyberduck.ui.browser.DownloadDirectoryFinder;
import ch.cyberduck.ui.cocoa.datasource.TransferTableDataSource;
import ch.cyberduck.ui.cocoa.delegate.AbstractMenuDelegate;
import ch.cyberduck.ui.cocoa.toolbar.TransferToolbarFactory;
import ch.cyberduck.ui.cocoa.toolbar.TransferToolbarValidator;

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
import java.util.Iterator;
import java.util.List;
import java.util.StringTokenizer;

public final class TransferController extends WindowController implements NSToolbar.Delegate, NSMenu.Validation {
    private static final Logger log = Logger.getLogger(TransferController.class);

    private final TransferToolbarValidator toolbarValidator = new TransferToolbarValidator(this);

    private final TransferToolbarFactory toolbarFactory
            = new TransferToolbarFactory(this);

    private final NSNotificationCenter notificationCenter
            = NSNotificationCenter.defaultCenter();

    private final Preferences preferences
            = PreferencesFactory.get();

    private final TransferCollection collection = TransferCollection.defaultCollection();

    private final TableColumnFactory tableColumnsFactory = new TableColumnFactory();

    private TranscriptController transcript;

    @Outlet
    private NSProgressIndicator transferSpinner;
    @Outlet
    private NSToolbar toolbar;
    @Outlet
    private NSTextField urlField;
    @Outlet
    private NSTextField localField;
    @Outlet
    private NSTextField localLabel;
    @Outlet
    private NSImageView iconView;
    @Outlet
    private NSStepper queueSizeStepper;
    @Outlet
    private NSTextField filterField;
    @Outlet
    private NSDrawer logDrawer;
    @Outlet
    private NSPopUpButton bandwidthPopup;

    @Delegate
    private AbstractMenuDelegate bandwidthPopupDelegate;
    @Outlet
    private NSTableView transferTable;
    @Delegate
    private TransferTableDataSource transferTableModel;
    @Delegate
    private AbstractTableDelegate<Transfer> transferTableDelegate;

    public TransferController() {
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

        if(!collection.isLoaded()) {
            transferSpinner.startAnimation(null);
        }
        collection.addListener(new AbstractCollectionListener<Transfer>() {
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
        if(collection.isLoaded()) {
            transferSpinner.stopAnimation(null);
            transferTable.setGridStyleMask(NSTableView.NSTableViewSolidHorizontalGridLineMask);
        }
        super.awakeFromNib();
    }

    @Override
    public void setWindow(NSWindow window) {
        window.setFrameAutosaveName("Transfers");
        window.setContentMinSize(new NSSize(400d, 150d));
        window.setMovableByWindowBackground(true);
        window.setTitle(LocaleFactory.localizedString("Transfers"));
        if(window.respondsToSelector(Foundation.selector("setTabbingIdentifier:"))) {
            window.setTabbingIdentifier(preferences.getProperty("queue.window.tabbing.identifier"));
        }
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

    public void setUrlField(NSTextField urlField) {
        this.urlField = urlField;
        this.urlField.setAllowsEditingTextAttributes(true);
        this.urlField.setSelectable(true);
    }

    public void setLocalField(NSTextField localField) {
        this.localField = localField;
        this.localField.setAllowsEditingTextAttributes(true);
        this.localField.setSelectable(true);
    }

    public void setLocalLabel(NSTextField localLabel) {
        this.localLabel = localLabel;
        this.localLabel.setStringValue(LocaleFactory.localizedString("Local File:", "Transfer"));
    }

    public void setIconView(final NSImageView iconView) {
        this.iconView = iconView;
    }

    public void setQueueSizeStepper(final NSStepper queueSizeStepper) {
        this.queueSizeStepper = queueSizeStepper;
        this.queueSizeStepper.setTarget(this.id());
        this.queueSizeStepper.setAction(Foundation.selector("queueSizeStepperChanged:"));
    }

    @Action
    public void queueSizeStepperChanged(final NSStepper sender) {
        // Queue size propery is changed using key value observer
        TransferQueueFactory.get().resize(sender.intValue());
    }

    public NSTextField getFilterField() {
        return filterField;
    }

    public void setFilterField(NSTextField filterField) {
        this.filterField = filterField;
        notificationCenter.addObserver(this.id(),
                Foundation.selector("filterFieldTextDidChange:"),
                NSControl.NSControlTextDidChangeNotification,
                this.filterField);
    }

    public void filterFieldTextDidChange(NSNotification notification) {
        transferTableModel.setFilter(filterField.stringValue());
        this.reload();
    }

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
        window.makeFirstResponder(this.filterField);
    }

    @Action
    public void drawerDidOpen(final NSNotification notification) {
        preferences.setProperty("queue.transcript.open", true);
    }

    @Action
    public void drawerDidClose(final NSNotification notification) {
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

    @Action
    public void bandwidthPopupChanged(NSPopUpButton sender) {
        final NSIndexSet selected = transferTable.selectedRowIndexes();
        final float bandwidth = Float.valueOf(sender.selectedItem().representedObject());
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
        transferTableModel.invalidate();
        bandwidthPopup.menu().setDelegate(null);
        super.invalidate();
    }

    public void setQueueTable(NSTableView view) {
        this.transferTable = view;
        this.transferTable.setRowHeight(new CGFloat(82));
        {
            NSTableColumn c = tableColumnsFactory.create(TransferTableDataSource.Column.progress.name());
            c.setMinWidth(80f);
            c.setWidth(300f);
            c.setResizingMask(NSTableColumn.NSTableColumnAutoresizingMask);
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

            public NSView tableView_viewForTableColumn_row(final NSTableView view, final NSTableColumn column, final NSInteger row) {
                final ProgressController controller = transferTableModel.getController(row.intValue());
                return controller.view();
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
        // No grid lines until list is loaded
        this.transferTable.setGridStyleMask(NSTableView.NSTableViewGridNone);
        // Set sselection properties
        this.transferTable.setAllowsMultipleSelection(true);
        this.transferTable.setAllowsEmptySelection(true);
        this.transferTable.setAllowsColumnReordering(false);
        this.transferTable.sizeToFit();
    }

    public NSTableView getTransferTable() {
        return transferTable;
    }

    public TransferTableDataSource getTransferTableModel() {
        return transferTableModel;
    }

    /**
     * Update highlighted rows
     */
    private void updateHighlight() {
        final boolean main = window().isMainWindow();
        final NSIndexSet set = transferTable.selectedRowIndexes();
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
            final String remote = transfer.getRemote().getUrl();
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
        final NSIndexSet set = transferTable.selectedRowIndexes();
        for(NSUInteger index = set.firstIndex(); !index.equals(NSIndexSet.NSNotFound); index = set.indexGreaterThanIndex(index)) {
            final Transfer transfer = transferTableModel.getSource().get(index.intValue());
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
                    TransferToolbarFactory.TransferToolbarItem.cleanup.label(), //title
                    LocaleFactory.localizedString("Remove completed transfers from list."), // message
                    TransferToolbarFactory.TransferToolbarItem.cleanup.label(), // defaultbutton
                    LocaleFactory.localizedString("Cancel"), // alternate button
                    null //other button
            );
            alert.setShowsSuppressionButton(true);
            alert.suppressionButton().setTitle(LocaleFactory.localizedString("Don't ask again", "Configuration"));
            this.alert(alert, new DisabledSheetCallback() {
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
        final PathCache cache = new PathCache(preferences.getInteger("transfer.cache.size"));
        final Host source = transfer.getSource();
        final Host destination = transfer.getDestination();
        final BackgroundAction action = new TransferCollectionBackgroundAction(this,
                null == source ? SessionPool.DISCONNECTED : SessionPoolFactory.create(this, cache, source),
                null == destination ? SessionPool.DISCONNECTED : SessionPoolFactory.create(this, cache, destination),
                new TransferAdapter() {
                    @Override
                    public void start(final Transfer transfer) {
                        super.start(transfer);
                        progress.start(transfer);
                        invoke(new DefaultMainAction() {
                            @Override
                            public void run() {
                                toolbar.validateVisibleItems();
                            }
                        });
                    }

                    @Override
                    public void stop(final Transfer transfer) {
                        super.stop(transfer);
                        progress.stop(transfer);
                        invoke(new DefaultMainAction() {
                            @Override
                            public void run() {
                                toolbar.validateVisibleItems();
                            }
                        });
                    }

                    @Override
                    public void progress(final TransferProgress status) {
                        super.progress(status);
                        progress.progress(status);
                    }
                }, progress, transfer.withCache(cache), options) {
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
    public void log(final Type request, final String message) {
        transcript.log(request, message);
    }

    @Override
    public NSToolbarItem toolbar_itemForItemIdentifier_willBeInsertedIntoToolbar(final NSToolbar toolbar, final String identifier, final boolean flag) {
        return toolbarFactory.create(identifier);
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
                        new DownloadDirectoryFinder().find(pasteboard.getBookmark()),
                        download.getName())));
            }
            this.add(new DownloadTransfer(pasteboard.getBookmark(), downloads));
            pasteboard.clear();
        }
    }

    @Action
    public void stopButtonClicked(final ID sender) {
        final NSIndexSet selected = transferTable.selectedRowIndexes();
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
        final NSIndexSet selected = transferTable.selectedRowIndexes();
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
        final NSIndexSet selected = transferTable.selectedRowIndexes();
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
        final NSIndexSet selected = transferTable.selectedRowIndexes();
        final Collection<Transfer> transfers = transferTableModel.getSource();
        final RevealService reveal = RevealServiceFactory.get();
        for(NSUInteger index = selected.firstIndex(); !index.equals(NSIndexSet.NSNotFound); index = selected.indexGreaterThanIndex(index)) {
            final Transfer transfer = transfers.get(index.intValue());
            for(TransferItem l : transfer.getRoots()) {
                reveal.reveal(l.local);
            }
        }
    }

    @Action
    public void deleteButtonClicked(final ID sender) {
        final NSIndexSet selected = transferTable.selectedRowIndexes();
        final Collection<Transfer> transfers = transferTableModel.getSource();
        int i = 0;
        final List<Transfer> remove = new ArrayList<Transfer>();
        for(NSUInteger index = selected.firstIndex(); !index.equals(NSIndexSet.NSNotFound); index = selected.indexGreaterThanIndex(index)) {
            final Transfer t = transfers.get(index.intValue() - i);
            if(!t.isRunning()) {
                remove.add(t);
            }
        }
        collection.removeAll(remove);
        collection.save();
    }

    @Action
    public void clearButtonClicked(final ID sender) {
        for(Iterator<Transfer> iter = collection.iterator(); iter.hasNext(); ) {
            final Transfer t = iter.next();
            if(t.isComplete()) {
                iter.remove();
            }
        }
        collection.save();
    }

    @Action
    public void trashButtonClicked(final ID sender) {
        final NSIndexSet selected = transferTable.selectedRowIndexes();
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
        return toolbarFactory.getDefault();
    }

    /**
     * NSToolbar.Delegate
     *
     * @param toolbar Window toolbar
     */
    @Override
    public NSArray toolbarAllowedItemIdentifiers(final NSToolbar toolbar) {
        return toolbarFactory.getAllowed();
    }

    @Override
    public NSArray toolbarSelectableItemIdentifiers(final NSToolbar toolbar) {
        return NSArray.array();
    }

    /**
     * @param item Menu item
     * @return True if enabled
     */
    @Override
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
        return toolbarValidator.validate(action);
    }

    /**
     * @param item Toolbar item
     */
    @Override
    @Action
    public boolean validateToolbarItem(final NSToolbarItem item) {
        return toolbarValidator.validate(item);
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
        public Selector getDefaultAction() {
            return Foundation.selector("bandwidthPopupChanged:");
        }
    }
}
