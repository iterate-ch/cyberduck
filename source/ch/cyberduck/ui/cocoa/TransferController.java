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

import ch.cyberduck.core.*;
import ch.cyberduck.core.i18n.Locale;
import ch.cyberduck.core.io.BandwidthThrottle;
import ch.cyberduck.core.threading.AbstractBackgroundAction;
import ch.cyberduck.core.threading.ControllerMainAction;
import ch.cyberduck.ui.PathPasteboard;
import ch.cyberduck.ui.cocoa.application.*;
import ch.cyberduck.ui.cocoa.delegate.AbstractMenuDelegate;
import ch.cyberduck.ui.cocoa.foundation.*;
import ch.cyberduck.ui.cocoa.threading.AlertRepeatableBackgroundAction;
import ch.cyberduck.ui.cocoa.threading.WindowMainAction;
import ch.cyberduck.ui.cocoa.util.HyperlinkAttributedStringFactory;
import ch.cyberduck.ui.cocoa.view.ControllerCell;

import org.rococoa.Foundation;
import org.rococoa.ID;
import org.rococoa.Rococoa;
import org.rococoa.Selector;
import org.rococoa.cocoa.CGFloat;
import org.rococoa.cocoa.foundation.NSInteger;
import org.rococoa.cocoa.foundation.NSSize;
import org.rococoa.cocoa.foundation.NSUInteger;

import org.apache.log4j.Logger;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;

/**
 * @version $Id$
 */
public class TransferController extends WindowController implements NSToolbar.Delegate {
    private static Logger log = Logger.getLogger(TransferController.class);

    private static TransferController instance = null;

    private NSToolbar toolbar;

    @Override
    public void awakeFromNib() {
        this.toolbar = NSToolbar.toolbarWithIdentifier("Queue Toolbar");
        this.toolbar.setDelegate(this.id());
        this.toolbar.setAllowsUserCustomization(true);
        this.toolbar.setAutosavesConfiguration(true);
        this.window.setToolbar(toolbar);

        super.awakeFromNib();
    }

    @Override
    public void setWindow(NSWindow window) {
        window.setMovableByWindowBackground(true);
        window.setTitle(Locale.localizedString("Transfers"));
        window.setDelegate(this.id());
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
        this.localLabel.setStringValue(Locale.localizedString("Local File:", "Transfer"));
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
    public void queueSizeStepperChanged(final ID sender) {
        synchronized(Queue.instance()) {
            Queue.instance().notify();
        }
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

    /**
     * Change focus to filter field
     *
     * @param sender
     */
    @Action
    public void searchButtonClicked(final ID sender) {
        this.window().makeFirstResponder(this.filterField);
    }

    private TranscriptController transcript;

    private NSDrawer logDrawer;

    public void drawerWillOpen(NSNotification notification) {
        logDrawer.setContentSize(new NSSize(logDrawer.contentSize().width.doubleValue(),
                Preferences.instance().getDouble("queue.logDrawer.size.height")
        ));
    }

    public void drawerDidOpen(NSNotification notification) {
        Preferences.instance().setProperty("queue.logDrawer.isOpen", true);
    }

    public void drawerWillClose(NSNotification notification) {
        Preferences.instance().setProperty("queue.logDrawer.size.height",
                logDrawer.contentSize().height.doubleValue());
    }

    public void drawerDidClose(NSNotification notification) {
        Preferences.instance().setProperty("queue.logDrawer.isOpen", false);
    }

    public void setLogDrawer(NSDrawer logDrawer) {
        this.logDrawer = logDrawer;
        this.transcript = new TranscriptController();
        this.logDrawer.setContentView(this.transcript.getLogView());
        NSNotificationCenter.defaultCenter().addObserver(this.id(),
                Foundation.selector("drawerWillOpen:"),
                NSDrawer.DrawerWillOpenNotification,
                this.logDrawer);
        NSNotificationCenter.defaultCenter().addObserver(this.id(),
                Foundation.selector("drawerDidOpen:"),
                NSDrawer.DrawerDidOpenNotification,
                this.logDrawer);
        NSNotificationCenter.defaultCenter().addObserver(this.id(),
                Foundation.selector("drawerWillClose:"),
                NSDrawer.DrawerWillCloseNotification,
                this.logDrawer);
        NSNotificationCenter.defaultCenter().addObserver(this.id(),
                Foundation.selector("drawerDidClose:"),
                NSDrawer.DrawerDidCloseNotification,
                this.logDrawer);
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
        this.bandwidthPopup.addItemWithTitle("");
        this.bandwidthPopup.lastItem().setImage(IconCache.iconNamed("bandwidth.tiff", 16));
        this.bandwidthPopup.addItemWithTitle(Locale.localizedString("Unlimited Bandwidth", "Transfer"));
        this.bandwidthPopup.lastItem().setRepresentedObject(String.valueOf(BandwidthThrottle.UNLIMITED));
        this.bandwidthPopup.menu().addItem(NSMenuItem.separatorItem());
        final StringTokenizer options = new StringTokenizer(Preferences.instance().getProperty("queue.bandwidth.options"), ",");
        while(options.hasMoreTokens()) {
            final String bytes = options.nextToken();
            this.bandwidthPopup.addItemWithTitle(Status.getSizeAsString(Integer.parseInt(bytes)) + "/s");
            this.bandwidthPopup.lastItem().setRepresentedObject(bytes);
        }
        this.bandwidthPopup.menu().setDelegate((this.bandwidthPopupDelegate = new BandwidthMenuDelegate()).id());
    }

    private class BandwidthMenuDelegate extends AbstractMenuDelegate {
        public NSInteger numberOfItemsInMenu(NSMenu menu) {
            return new NSInteger(new StringTokenizer(Preferences.instance().getProperty("queue.bandwidth.options"), ",").countTokens() + 3);
        }

        @Override
        public boolean menuUpdateItemAtIndex(NSMenu menu, NSMenuItem item, NSInteger i, boolean cancel) {
            log.debug("menuUpdateItemAtIndex:" + item);
            if(item.representedObject() != null) {
                final int selected = transferTable.numberOfSelectedRows().intValue();
                int bytes = Integer.valueOf(item.representedObject());
                NSIndexSet iterator = transferTable.selectedRowIndexes();
                for(NSUInteger index = iterator.firstIndex(); !index.equals(NSIndexSet.NSNotFound); index = iterator.indexGreaterThanIndex(index)) {
                    Transfer transfer = TransferCollection.defaultCollection().get(index.intValue());
                    if(BandwidthThrottle.UNLIMITED == transfer.getBandwidth()) {
                        if(BandwidthThrottle.UNLIMITED == bytes) {
                            item.setState(selected > 1 ? NSCell.NSMixedState : NSCell.NSOnState);
                            break;
                        }
                        else {
                            item.setState(NSCell.NSOffState);
                        }
                    }
                    else {
                        int bandwidth = (int) transfer.getBandwidth();
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
        NSIndexSet iterator = transferTable.selectedRowIndexes();
        int bandwidth = Integer.valueOf(sender.selectedItem().representedObject());
        for(NSUInteger index = iterator.firstIndex(); !index.equals(NSIndexSet.NSNotFound); index = iterator.indexGreaterThanIndex(index)) {
            Transfer transfer = TransferCollection.defaultCollection().get(index.intValue());
            transfer.setBandwidth(bandwidth);
        }
        this.updateBandwidthPopup();
    }

    /**
     * Loading bundle
     */
    private TransferController() {
        this.loadBundle();
        TransferCollection.defaultCollection().addListener(new AbstractCollectionListener<Transfer>() {
            @Override
            public void collectionLoaded() {
                invoke(new ControllerMainAction(TransferController.this) {
                    public void run() {
                        reload();
                    }
                });
            }

            @Override
            public void collectionItemAdded(Transfer item) {
                invoke(new ControllerMainAction(TransferController.this) {
                    public void run() {
                        reload();
                    }
                });
            }

            @Override
            public void collectionItemRemoved(Transfer item) {
                invoke(new ControllerMainAction(TransferController.this) {
                    public void run() {
                        reload();
                    }
                });
            }
        });
    }

    public static TransferController instance() {
        synchronized(NSApplication.sharedApplication()) {
            if(null == instance) {
                instance = new TransferController();
            }
            return instance;
        }
    }

    @Override
    protected String getBundleName() {
        return "Transfer";
    }

    @Override
    protected void invalidate() {
        toolbar.setDelegate(null);
        toolbarItems.clear();
        transferTableModel.invalidate();
        bandwidthPopup.menu().setDelegate(null);
        super.invalidate();
    }

    /**
     * @return NSApplication.TerminateLater or NSApplication.TerminateNow depending if there are
     *         running transfers to be checked first
     */
    public static NSUInteger applicationShouldTerminate(final NSApplication app) {
        if(null != instance) {
            //Saving state of transfer window
            Preferences.instance().setProperty("queue.openByDefault", instance.window().isVisible());
            if(TransferCollection.defaultCollection().numberOfRunningTransfers() > 0) {
                final NSAlert alert = NSAlert.alert(Locale.localizedString("Transfer in progress"), //title
                        Locale.localizedString("There are files currently being transferred. Quit anyway?"), // message
                        Locale.localizedString("Quit"), // defaultbutton
                        Locale.localizedString("Cancel"), //alternative button
                        null //other button
                );
                instance.alert(alert, new SheetCallback() {
                    public void callback(int returncode) {
                        if(returncode == DEFAULT_OPTION) { //Quit
                            for(Transfer transfer : TransferCollection.defaultCollection()) {
                                if(transfer.isRunning()) {
                                    transfer.interrupt();
                                }
                            }
                            app.replyToApplicationShouldTerminate(true);
                        }
                        if(returncode == ALTERNATE_OPTION) { //Cancel
                            app.replyToApplicationShouldTerminate(false);
                        }
                    }
                });
                return NSApplication.NSTerminateLater; //break
            }
        }
        return NSApplication.NSTerminateNow;
    }

    private final TableColumnFactory tableColumnsFactory = new TableColumnFactory();

    private static class TableColumnFactory extends HashMap<String, NSTableColumn> {
        private NSTableColumn create(String identifier) {
            if(!this.containsKey(identifier)) {
                this.put(identifier, NSTableColumn.tableColumnWithIdentifier(identifier));
            }
            return this.get(identifier);
        }
    }

    @Outlet
    private NSTableView transferTable;
    private TransferTableDataSource transferTableModel;
    private AbstractTableDelegate<Transfer> transferTableDelegate;

    public void setQueueTable(NSTableView view) {
        this.transferTable = view;
        this.transferTable.setRowHeight(new CGFloat(82));
        this.transferTable.setDataSource((transferTableModel = new TransferTableDataSource()).id());
        this.transferTable.setDelegate((transferTableDelegate = new AbstractTableDelegate<Transfer>() {
            public String tooltip(Transfer t) {
                return t.getName();
            }

            public void enterKeyPressed(final ID sender) {
                this.tableRowDoubleClicked(sender);
            }

            public void deleteKeyPressed(final ID sender) {
                deleteButtonClicked(sender);
            }

            @Override
            public void tableColumnClicked(NSTableView view, NSTableColumn tableColumn) {
                ;
            }

            @Override
            public void tableRowDoubleClicked(final ID sender) {
                reloadButtonClicked(sender);
            }

            @Override
            public void selectionIsChanging(NSNotification notification) {
                updateHighlight();
            }

            @Override
            public void selectionDidChange(NSNotification notification) {
                updateHighlight();
                updateSelection();
                transferTable.noteHeightOfRowsWithIndexesChanged(
                        NSIndexSet.indexSetWithIndexesInRange(
                                NSRange.NSMakeRange(new NSUInteger(0), new NSUInteger(transferTable.numberOfRows()))));
            }

            public void tableView_willDisplayCell_forTableColumn_row(NSTableView view, NSCell cell, NSTableColumn tableColumn, NSInteger row) {
                Rococoa.cast(cell, ControllerCell.class).setView(transferTableModel.getController(row.intValue()).view());
            }

            @Override
            public boolean isTypeSelectSupported() {
                return true;
            }

            public String tableView_typeSelectStringForTableColumn_row(NSTableView tableView,
                                                                       NSTableColumn tableColumn,
                                                                       NSInteger row) {
                return transferTableModel.getSource().get(row.intValue()).getName();
            }
        }).id());
        // receive drag events from types
        // in fact we are not interested in file promises, but because the browser model can only initiate
        // a drag with tableView.dragPromisedFilesOfTypes(), we listens for those events
        // and then use the private pasteboard instead.
        this.transferTable.registerForDraggedTypes(NSArray.arrayWithObjects(
                NSPasteboard.StringPboardType,
                NSPasteboard.FilesPromisePboardType));

        {
            NSTableColumn c = tableColumnsFactory.create(TransferTableDataSource.PROGRESS_COLUMN);
            c.setMinWidth(80f);
            c.setWidth(300f);
            c.setResizingMask(NSTableColumn.NSTableColumnAutoresizingMask);
            c.setDataCell(prototype);
            this.transferTable.addTableColumn(c);
        }
        this.transferTable.setGridStyleMask(NSTableView.NSTableViewSolidHorizontalGridLineMask);
        //selection properties
        this.transferTable.setAllowsMultipleSelection(true);
        this.transferTable.setAllowsEmptySelection(true);
        this.transferTable.setAllowsColumnReordering(false);
        this.transferTable.sizeToFit();
    }

    private final NSCell prototype = ControllerCell.controllerCell();

    /**
     *
     */
    private void updateHighlight() {
        boolean isKeyWindow = window().isKeyWindow();
        for(int i = 0; i < transferTableModel.getSource().size(); i++) {
            transferTableModel.setHighlighted(i, transferTable.isRowSelected(new NSInteger(i)) && isKeyWindow);
        }
    }

    /**
     *
     */
    private void updateSelection() {
        log.debug("updateSelection");
        this.updateLabels();
        this.updateIcon();
        this.updateBandwidthPopup();
        toolbar.validateVisibleItems();
    }

    /**
     *
     */
    private void updateLabels() {
        log.debug("updateLabels");
        final int selected = transferTable.numberOfSelectedRows().intValue();
        if(1 == selected) {
            final Transfer transfer = transferTableModel.getSource().get(transferTable.selectedRow().intValue());
            // Draw text fields at the bottom
            String url = transfer.getRoot().toURL();
            urlField.setAttributedStringValue(
                    HyperlinkAttributedStringFactory.create(NSMutableAttributedString.create(url,
                            TRUNCATE_MIDDLE_ATTRIBUTES), url));
            if(transfer.numberOfRoots() == 1) {
                localField.setAttributedStringValue(
                        HyperlinkAttributedStringFactory.create(NSMutableAttributedString.create(transfer.getRoot().getLocal().getAbsolute(),
                                TRUNCATE_MIDDLE_ATTRIBUTES), transfer.getRoot().getLocal().toURL()));
            }
            else {
                localField.setAttributedStringValue(NSAttributedString.attributedStringWithAttributes(Locale.localizedString("Multiple files"),
                        TRUNCATE_MIDDLE_ATTRIBUTES));
            }
        }
        else {
            urlField.setStringValue("");
            localField.setStringValue("");
        }
    }

    /**
     *
     */
    private void updateIcon() {
        log.debug("updateIcon");
        final int selected = transferTable.numberOfSelectedRows().intValue();
        if(1 != selected) {
            iconView.setImage(null);
            return;
        }
        final Transfer transfer = transferTableModel.getSource().get(transferTable.selectedRow().intValue());
        // Draw file type icon
        if(transfer.numberOfRoots() == 1) {
            iconView.setImage(IconCache.instance().iconForPath(transfer.getRoot().getLocal(), 32));
        }
        else {
            iconView.setImage(IconCache.iconNamed("NSMultipleDocuments", 32));
        }
    }

    /**
     *
     */
    private void updateBandwidthPopup() {
        log.debug("updateBandwidthPopup");
        final int selected = transferTable.numberOfSelectedRows().intValue();
        bandwidthPopup.setEnabled(selected > 0);
        NSIndexSet iterator = transferTable.selectedRowIndexes();
        for(NSUInteger index = iterator.firstIndex(); !index.equals(NSIndexSet.NSNotFound); index = iterator.indexGreaterThanIndex(index)) {
            final Transfer transfer = transferTableModel.getSource().get(index.intValue());
            if(transfer instanceof SyncTransfer) {
                // Currently we do not support bandwidth throtling for sync transfers due to
                // the problem of mapping both download and upload rate in the GUI
                bandwidthPopup.setEnabled(false);
                // Break through and set the standard icon below
                break;
            }
            if(transfer.getBandwidth() != BandwidthThrottle.UNLIMITED) {
                // Mark as throttled
                this.bandwidthPopup.itemAtIndex(new NSInteger(0)).setImage(IconCache.iconNamed("turtle.tiff"));
                return;
            }
        }
        // Set the standard icon
        this.bandwidthPopup.itemAtIndex(new NSInteger(0)).setImage(IconCache.iconNamed("bandwidth.tiff", 16));
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
     * Remove this item form the list
     *
     * @param transfer
     */
    public void removeTransfer(final Transfer transfer) {
        TransferCollection.defaultCollection().remove(transfer);
    }

    /**
     * Add this item to the list; select it and scroll the view to make it visible
     *
     * @param transfer
     */
    public void addTransfer(final Transfer transfer) {
        TransferCollection.defaultCollection().add(transfer);
        final int row = TransferCollection.defaultCollection().size() - 1;
        final NSInteger index = new NSInteger(row);
        transferTable.selectRowIndexes(NSIndexSet.indexSetWithIndex(index), false);
        transferTable.scrollRowToVisible(index);
    }

    /**
     * @param transfer
     */
    public void startTransfer(final Transfer transfer) {
        this.startTransfer(transfer, false, false);
    }

    /**
     * @param transfer
     * @param resumeRequested
     * @param reloadRequested
     */
    private void startTransfer(final Transfer transfer, final boolean resumeRequested, final boolean reloadRequested) {
        if(!TransferCollection.defaultCollection().contains(transfer)) {
            this.addTransfer(transfer);
        }
        if(Preferences.instance().getBoolean("queue.orderFrontOnStart")) {
            this.window().makeKeyAndOrderFront(null);
        }
        this.background(new AlertRepeatableBackgroundAction(this) {
            private boolean resume = resumeRequested;
            private boolean reload = reloadRequested;

            private TransferListener tl;

            @Override
            public boolean prepare() {
                transfer.addListener(tl = new TransferAdapter() {
                    @Override
                    public void transferQueued() {
                        validateToolbar();
                    }

                    @Override
                    public void transferResumed() {
                        validateToolbar();
                    }

                    @Override
                    public void transferWillStart() {
                        validateToolbar();
                        badge();
                    }

                    @Override
                    public void transferDidEnd() {
                        validateToolbar();
                        badge();
                    }

                    private void badge() {
                        if(Preferences.instance().getBoolean("queue.dock.badge")) {
                            int count = TransferCollection.defaultCollection().numberOfRunningTransfers();
                            if(0 == count) {
                                NSApplication.sharedApplication().dockTile().setBadgeLabel("");
                            }
                            else {
                                NSApplication.sharedApplication().dockTile().setBadgeLabel(
                                        String.valueOf(count));
                            }
                        }
                    }
                });
                // Attach listeners
                super.prepare();
                // Always continue. Current status might be canceled if interrupted before.
                return true;
            }

            public void run() {
                final TransferOptions options = new TransferOptions();
                options.reloadRequested = reload;
                options.resumeRequested = resume;
                transfer.start(TransferPromptController.create(TransferController.this, transfer), options);
            }

            @Override
            public void finish() {
                super.finish();
                transfer.removeListener(tl);
                // Upon retry, use resume
                reload = false;
                resume = true;
            }

            @Override
            public void cleanup() {
                if(transfer.isComplete() && !transfer.isCanceled()) {
                    if(transfer.isReset()) {
                        if(Preferences.instance().getBoolean("queue.removeItemWhenComplete")) {
                            removeTransfer(transfer);
                        }
                        if(Preferences.instance().getBoolean("queue.orderBackOnStop")) {
                            if(!(TransferCollection.defaultCollection().numberOfRunningTransfers() > 0)) {
                                window().close();
                            }
                        }
                    }
                }
                TransferCollection.defaultCollection().save();
            }

            @Override
            public Session getSession() {
                return transfer.getSession();
            }

            @Override
            public void pause() {
                transfer.fireTransferQueued();
                // Upon retry do not suggest to overwrite already completed items from the transfer
                resume = true;
                super.pause();
                transfer.fireTransferResumed();
            }

            @Override
            public boolean isCanceled() {
                return transfer.isCanceled();
            }

            @Override
            public void log(final boolean request, final String message) {
                if(logDrawer.state() == NSDrawer.OpenState) {
                    invoke(new WindowMainAction(TransferController.this) {
                        public void run() {
                            TransferController.this.transcript.log(request, message);
                        }
                    });
                }
                super.log(request, message);
            }

            private final Object lock = new Object();

            @Override
            public Object lock() {
                // No synchronization with other tasks
                return lock;
            }
        });
    }

    private void validateToolbar() {
        invoke(new WindowMainAction(TransferController.this) {
            public void run() {
                window().toolbar().validateVisibleItems();
                updateIcon();
            }
        });
    }

    private static final String TOOLBAR_RESUME = "Resume";
    private static final String TOOLBAR_RELOAD = "Reload";
    private static final String TOOLBAR_STOP = "Stop";
    private static final String TOOLBAR_REMOVE = "Remove";
    private static final String TOOLBAR_CLEAN_UP = "Clean Up";
    private static final String TOOLBAR_OPEN = "Open";
    private static final String TOOLBAR_SHOW = "Show";
    private static final String TOOLBAR_TRASH = "Trash";
    private static final String TOOLBAR_FILTER = "Search";

    /**
     * Keep reference to weak toolbar items
     */
    private Map<String, NSToolbarItem> toolbarItems
            = new HashMap<String, NSToolbarItem>();

    public NSToolbarItem toolbar_itemForItemIdentifier_willBeInsertedIntoToolbar(NSToolbar toolbar, final String itemIdentifier, boolean flag) {
        if(!toolbarItems.containsKey(itemIdentifier)) {
            toolbarItems.put(itemIdentifier, NSToolbarItem.itemWithIdentifier(itemIdentifier));
        }
        final NSToolbarItem item = toolbarItems.get(itemIdentifier);
        if(itemIdentifier.equals(TOOLBAR_STOP)) {
            item.setLabel(Locale.localizedString(TOOLBAR_STOP));
            item.setPaletteLabel(Locale.localizedString(TOOLBAR_STOP));
            item.setToolTip(Locale.localizedString(TOOLBAR_STOP));
            item.setImage(IconCache.iconNamed("stop.tiff", 32));
            item.setTarget(this.id());
            item.setAction(Foundation.selector("stopButtonClicked:"));
            return item;
        }
        if(itemIdentifier.equals(TOOLBAR_RESUME)) {
            item.setLabel(Locale.localizedString(TOOLBAR_RESUME));
            item.setPaletteLabel(Locale.localizedString(TOOLBAR_RESUME));
            item.setToolTip(Locale.localizedString(TOOLBAR_RESUME));
            item.setImage(IconCache.iconNamed("resume.tiff"));
            item.setTarget(this.id());
            item.setAction(Foundation.selector("resumeButtonClicked:"));
            return item;
        }
        if(itemIdentifier.equals(TOOLBAR_RELOAD)) {
            item.setLabel(Locale.localizedString(TOOLBAR_RELOAD));
            item.setPaletteLabel(Locale.localizedString(TOOLBAR_RELOAD));
            item.setToolTip(Locale.localizedString(TOOLBAR_RELOAD));
            item.setImage(IconCache.iconNamed("reload.tiff"));
            item.setTarget(this.id());
            item.setAction(Foundation.selector("reloadButtonClicked:"));
            return item;
        }
        if(itemIdentifier.equals(TOOLBAR_SHOW)) {
            item.setLabel(Locale.localizedString(TOOLBAR_SHOW));
            item.setPaletteLabel(Locale.localizedString("Show in Finder"));
            item.setToolTip(Locale.localizedString("Show in Finder"));
            item.setImage(IconCache.iconNamed("reveal.tiff"));
            item.setTarget(this.id());
            item.setAction(Foundation.selector("revealButtonClicked:"));
            return item;
        }
        if(itemIdentifier.equals(TOOLBAR_OPEN)) {
            item.setLabel(Locale.localizedString(TOOLBAR_OPEN));
            item.setPaletteLabel(Locale.localizedString(TOOLBAR_OPEN));
            item.setToolTip(Locale.localizedString(TOOLBAR_OPEN));
            item.setImage(IconCache.iconNamed("open.tiff"));
            item.setTarget(this.id());
            item.setAction(Foundation.selector("openButtonClicked:"));
            return item;
        }
        if(itemIdentifier.equals(TOOLBAR_REMOVE)) {
            item.setLabel(Locale.localizedString(TOOLBAR_REMOVE));
            item.setPaletteLabel(Locale.localizedString(TOOLBAR_REMOVE));
            item.setToolTip(Locale.localizedString(TOOLBAR_REMOVE));
            item.setImage(IconCache.iconNamed("clean.tiff"));
            item.setTarget(this.id());
            item.setAction(Foundation.selector("deleteButtonClicked:"));
            return item;
        }
        if(itemIdentifier.equals(TOOLBAR_CLEAN_UP)) {
            item.setLabel(Locale.localizedString(TOOLBAR_CLEAN_UP));
            item.setPaletteLabel(Locale.localizedString(TOOLBAR_CLEAN_UP));
            item.setToolTip(Locale.localizedString(TOOLBAR_CLEAN_UP));
            item.setImage(IconCache.iconNamed("cleanAll.tiff"));
            item.setTarget(this.id());
            item.setAction(Foundation.selector("clearButtonClicked:"));
            return item;
        }
        if(itemIdentifier.equals(TOOLBAR_TRASH)) {
            item.setLabel(Locale.localizedString(TOOLBAR_TRASH));
            item.setPaletteLabel(Locale.localizedString(TOOLBAR_TRASH));
            item.setToolTip(Locale.localizedString("Move to Trash"));
            item.setImage(IconCache.iconNamed("trash.tiff"));
            item.setTarget(this.id());
            item.setAction(Foundation.selector("trashButtonClicked:"));
            return item;
        }
        if(itemIdentifier.equals(TOOLBAR_FILTER)) {
            item.setLabel(Locale.localizedString(TOOLBAR_FILTER));
            item.setPaletteLabel(Locale.localizedString(TOOLBAR_FILTER));
            item.setView(this.filterField);
            item.setMinSize(this.filterField.frame().size);
            item.setMaxSize(this.filterField.frame().size);
            return item;
        }
        // itemIdent refered to a toolbar item that is not provide or supported by us or cocoa.
        // Returning null will inform the toolbar this kind of item is not supported.
        return null;
    }

    @Action
    public void paste(final ID sender) {
        log.debug("paste");
        for(PathPasteboard pasteboard : PathPasteboard.allPasteboards()) {
            if(pasteboard.isEmpty()) {
                continue;
            }
            TransferCollection.defaultCollection().add(new DownloadTransfer(pasteboard.copy()));
            pasteboard.clear();
        }
    }

    @Action
    public void stopButtonClicked(final ID sender) {
        NSIndexSet iterator = transferTable.selectedRowIndexes();
        for(NSUInteger index = iterator.firstIndex(); !index.equals(NSIndexSet.NSNotFound); index = iterator.indexGreaterThanIndex(index)) {
            final Transfer transfer = transferTableModel.getSource().get(index.intValue());
            if(transfer.isRunning()) {
                this.background(new AbstractBackgroundAction() {
                    public void run() {
                        transfer.cancel();
                    }
                });
            }
        }
    }

    @Action
    public void stopAllButtonClicked(final ID sender) {
        final Collection<Transfer> transfers = transferTableModel.getSource();
        for(final Transfer transfer : transfers) {
            if(transfer.isRunning()) {
                this.background(new AbstractBackgroundAction() {
                    public void run() {
                        transfer.cancel();
                    }
                });
            }
        }
    }

    @Action
    public void resumeButtonClicked(final ID sender) {
        NSIndexSet iterator = transferTable.selectedRowIndexes();
        for(NSUInteger index = iterator.firstIndex(); !index.equals(NSIndexSet.NSNotFound); index = iterator.indexGreaterThanIndex(index)) {
            final Collection<Transfer> transfers = transferTableModel.getSource();
            final Transfer transfer = transfers.get(index.intValue());
            if(!transfer.isRunning()) {
                this.startTransfer(transfer, true, false);
            }
        }
    }

    @Action
    public void reloadButtonClicked(final ID sender) {
        NSIndexSet iterator = transferTable.selectedRowIndexes();
        for(NSUInteger index = iterator.firstIndex(); !index.equals(NSIndexSet.NSNotFound); index = iterator.indexGreaterThanIndex(index)) {
            final Collection<Transfer> transfers = transferTableModel.getSource();
            final Transfer transfer = transfers.get(index.intValue());
            if(!transfer.isRunning()) {
                this.startTransfer(transfer, false, true);
            }
        }
    }

    @Action
    public void openButtonClicked(final ID sender) {
        if(transferTable.numberOfSelectedRows().intValue() == 1) {
            final Transfer transfer = transferTableModel.getSource().get(transferTable.selectedRow().intValue());
            for(Path i : transfer.getRoots()) {
                Local l = i.getLocal();
                if(!l.open()) {
                    log.warn("Error opening file:" + l.getAbsolute());
                }
            }
        }
    }

    @Action
    public void revealButtonClicked(final ID sender) {
        NSIndexSet iterator = transferTable.selectedRowIndexes();
        final Collection<Transfer> transfers = transferTableModel.getSource();
        for(NSUInteger index = iterator.firstIndex(); !index.equals(NSIndexSet.NSNotFound); index = iterator.indexGreaterThanIndex(index)) {
            final Transfer transfer = transfers.get(index.intValue());
            for(Path i : transfer.getRoots()) {
                Local l = i.getLocal();
                if(l.reveal()) {
                    break;
                }
            }
        }
    }

    @Action
    public void deleteButtonClicked(final ID sender) {
        NSIndexSet iterator = transferTable.selectedRowIndexes();
        final Collection<Transfer> transfers = transferTableModel.getSource();
        for(NSUInteger index = iterator.firstIndex(); !index.equals(NSIndexSet.NSNotFound); index = iterator.indexGreaterThanIndex(index)) {
            final Transfer transfer = transfers.get(index.intValue());
            if(!transfer.isRunning()) {
                TransferCollection.defaultCollection().remove(transfer);
            }
        }
        TransferCollection.defaultCollection().save();
    }

    @Action
    public void clearButtonClicked(final ID sender) {
        final Collection<Transfer> transfers = transferTableModel.getSource();
        for(Transfer transfer : transfers) {
            if(!transfer.isRunning() && transfer.isComplete()) {
                TransferCollection.defaultCollection().remove(transfer);
            }
        }
        TransferCollection.defaultCollection().save();
    }

    @Action
    public void trashButtonClicked(final ID sender) {
        NSIndexSet iterator = transferTable.selectedRowIndexes();
        final Collection<Transfer> transfers = transferTableModel.getSource();
        for(NSUInteger index = iterator.firstIndex(); !index.equals(NSIndexSet.NSNotFound); index = iterator.indexGreaterThanIndex(index)) {
            final Transfer transfer = transfers.get(index.intValue());
            if(!transfer.isRunning()) {
                for(Path path : transfer.getRoots()) {
                    path.getLocal().delete();
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
     * @param toolbar
     */
    public NSArray toolbarDefaultItemIdentifiers(NSToolbar toolbar) {
        return NSArray.arrayWithObjects(
                TOOLBAR_RESUME,
                TOOLBAR_STOP,
                TOOLBAR_RELOAD,
                TOOLBAR_REMOVE,
                TOOLBAR_SHOW,
                NSToolbarItem.NSToolbarFlexibleItemIdentifier,
                TOOLBAR_FILTER
        );
    }

    /**
     * NSToolbar.Delegate
     *
     * @param toolbar
     */
    public NSArray toolbarAllowedItemIdentifiers(NSToolbar toolbar) {
        return NSArray.arrayWithObjects(
                TOOLBAR_RESUME,
                TOOLBAR_RELOAD,
                TOOLBAR_STOP,
                TOOLBAR_REMOVE,
                TOOLBAR_CLEAN_UP,
                TOOLBAR_SHOW,
                TOOLBAR_OPEN,
                TOOLBAR_TRASH,
                TOOLBAR_FILTER,
                NSToolbarItem.NSToolbarCustomizeToolbarItemIdentifier,
                NSToolbarItem.NSToolbarSpaceItemIdentifier,
                NSToolbarItem.NSToolbarSeparatorItemIdentifier,
                NSToolbarItem.NSToolbarFlexibleSpaceItemIdentifier
        );
    }

    public NSArray toolbarSelectableItemIdentifiers(NSToolbar toolbar) {
        return NSArray.array();
    }

    /**
     * @param item
     */
    public boolean validateMenuItem(NSMenuItem item) {
        final Selector action = item.action();
        if(action.equals(Foundation.selector("paste:"))) {
            final List<PathPasteboard> pasteboards = PathPasteboard.allPasteboards();
            if(pasteboards.size() == 1) {
                for(PathPasteboard pasteboard : pasteboards) {
                    if(pasteboard.size() == 1) {
                        item.setTitle(Locale.localizedString("Paste") + " \"" + pasteboard.get(0).getName() + "\"");
                    }
                    else {
                        item.setTitle(Locale.localizedString("Paste") + " (" + pasteboard.size() + " " +
                                Locale.localizedString("files") + ")");
                    }
                }
            }
            else {
                item.setTitle(Locale.localizedString("Paste"));
            }
        }
        return this.validateItem(action);
    }

    /**
     * @param item
     */
    public boolean validateToolbarItem(final NSToolbarItem item) {
        return this.validateItem(item.action());
    }

    /**
     * Validates menu and toolbar items
     *
     * @param action
     * @return true if the item with the identifier should be selectable
     */
    private boolean validateItem(final Selector action) {
        if(action.equals(Foundation.selector("paste:"))) {
            return !PathPasteboard.allPasteboards().isEmpty();
        }
        if(action.equals(Foundation.selector("stopButtonClicked:"))) {
            return this.validate(new TransferToolbarValidator() {
                public boolean validate(Transfer transfer) {
                    return transfer.isRunning();
                }
            });
        }
        if(action.equals(Foundation.selector("reloadButtonClicked:"))
                || action.equals(Foundation.selector("deleteButtonClicked:"))) {
            return this.validate(new TransferToolbarValidator() {
                public boolean validate(Transfer transfer) {
                    return !transfer.isRunning();
                }
            });
        }
        if(action.equals(Foundation.selector("resumeButtonClicked:"))) {
            return this.validate(new TransferToolbarValidator() {
                public boolean validate(Transfer transfer) {
                    if(transfer.isRunning()) {
                        return false;
                    }
                    return transfer.isResumable() && !transfer.isComplete();
                }
            });
        }
        if(action.equals(Foundation.selector("openButtonClicked:"))
                || action.equals(Foundation.selector("trashButtonClicked:"))) {
            return this.validate(new TransferToolbarValidator() {
                public boolean validate(Transfer transfer) {
                    if(!transfer.isComplete()) {
                        return false;
                    }
                    if(!transfer.isRunning()) {
                        for(Path i : transfer.getRoots()) {
                            if(i.getLocal().exists()) {
                                return true;
                            }
                        }
                    }
                    return false;
                }
            });
        }
        if(action.equals(Foundation.selector("revealButtonClicked:"))) {
            return this.validate(new TransferToolbarValidator() {
                public boolean validate(Transfer transfer) {
                    for(Path i : transfer.getRoots()) {
                        if(i.getLocal().exists()) {
                            return true;
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
     * @param v The validator to use
     * @return True if one or more of the selected items passes the validation test
     */
    private boolean validate(TransferToolbarValidator v) {
        final NSIndexSet iterator = transferTable.selectedRowIndexes();
        final Collection<Transfer> transfers = transferTableModel.getSource();
        for(NSUInteger index = iterator.firstIndex(); !index.equals(NSIndexSet.NSNotFound); index = iterator.indexGreaterThanIndex(index)) {
            final Transfer transfer = transfers.get(index.intValue());
            if(v.validate(transfer)) {
                return true;
            }
        }
        return false;
    }

    private interface TransferToolbarValidator {
        public boolean validate(Transfer transfer);
    }
}