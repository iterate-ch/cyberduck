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

import com.enterprisedt.net.ftp.FTPTransferType;

import ch.cyberduck.core.*;
import ch.cyberduck.core.ftp.FTPSession;
import ch.cyberduck.core.io.BandwidthThrottle;
import ch.cyberduck.core.sftp.SFTPSession;
import ch.cyberduck.ui.cocoa.delegate.MenuDelegate;
import ch.cyberduck.ui.cocoa.growl.Growl;
import ch.cyberduck.ui.cocoa.threading.BackgroundActionImpl;

import com.apple.cocoa.application.*;
import com.apple.cocoa.foundation.*;

import org.apache.log4j.Logger;

import java.util.Iterator;

/**
 * @version $Id$
 */
public class CDTransferController extends CDWindowController implements NSToolbarItem.ItemValidation {
    private static Logger log = Logger.getLogger(CDTransferController.class);

    private static CDTransferController instance;

    private NSToolbar toolbar;

    public void awakeFromNib() {
        this.toolbar = new NSToolbar("Queue Toolbar");
        this.toolbar.setDelegate(this);
        this.toolbar.setAllowsUserCustomization(true);
        this.toolbar.setAutosavesConfiguration(true);
        this.window.setToolbar(toolbar);
    }

    public void setWindow(NSWindow window) {
        this.window = window;
        this.window.setReleasedWhenClosed(false);
        this.window.setDelegate(this);
        this.window.setTitle(NSBundle.localizedString("Transfers", ""));
    }

    /**
     * @param notification
     */
    public void windowDidBecomeKey(NSNotification notification) {
        updateHighlight();
    }

    /**
     * @param notification
     */
    public void windowDidResignKey(NSNotification notification) {
        updateHighlight();
    }

    /**
     * @param notification
     */
    public void windowWillClose(NSNotification notification) {
        // Do not call super as we are a singleton. super#windowWillClose would invalidate me
    }

    // ----------------------------------------------------------
    // Outlets
    // ----------------------------------------------------------

    private NSTextField urlField; // IBOutlet

    public void setUrlField(NSTextField urlField) {
        this.urlField = urlField;
    }

    private NSTextField localField; // IBOutlet

    public void setLocalField(NSTextField localField) {
        this.localField = localField;
    }

    private NSImageView iconView; //IBOutlet

    public void setIconView(final NSImageView iconView) {
        this.iconView = iconView;
    }

    private NSStepper queueSizeStepper; // IBOutlet

    public void setQueueSizeStepper(final NSStepper queueSizeStepper) {
        this.queueSizeStepper = queueSizeStepper;
        this.queueSizeStepper.setTarget(this);
        this.queueSizeStepper.setAction(new NSSelector("queueSizeStepperChanged", new Class[]{Object.class}));
    }

    public void queueSizeStepperChanged(final Object sender) {
        synchronized(Queue.instance()) {
            Queue.instance().notifyAll();
        }
    }
        
    private NSTextField filterField; // IBOutlet

    public void setFilterField(NSTextField filterField) {
        this.filterField = filterField;
        NSNotificationCenter.defaultCenter().addObserver(this,
                new NSSelector("filterFieldTextDidChange", new Class[]{NSNotification.class}),
                NSControl.ControlTextDidChangeNotification,
                this.filterField);
    }

    public void filterFieldTextDidChange(NSNotification notification) {
        NSDictionary userInfo = notification.userInfo();
        if(null != userInfo) {
            Object o = userInfo.allValues().lastObject();
            if(null != o) {
                final String searchString = ((NSText) o).string();
                transferModel.setFilter(searchString);
                this.reloadData();
            }
        }
    }

    private NSPopUpButton bandwidthPopup;

    private MenuDelegate bandwidthPopupDelegate;

    public void setBandwidthPopup(NSPopUpButton bandwidthPopup) {
        this.bandwidthPopup = bandwidthPopup;
        this.bandwidthPopup.setEnabled(false);
        this.bandwidthPopup.setAllowsMixedState(true);
        this.bandwidthPopup.setTarget(this);
        this.bandwidthPopup.setAction(new NSSelector("bandwidthPopupChanged", new Class[]{Object.class}));
        this.bandwidthPopup.itemAtIndex(0).setImage(NSImage.imageNamed("bandwidth16.tiff"));
        this.bandwidthPopup.menu().setDelegate(this.bandwidthPopupDelegate = new BandwidthDelegate());
    }

    private class BandwidthDelegate extends MenuDelegate {
        public int numberOfItemsInMenu(NSMenu menu) {
            return menu.numberOfItems();
        }

        public boolean menuUpdateItemAtIndex(NSMenu menu, NSMenuItem item, int index, boolean shouldCancel) {
            final int selected = transferTable.numberOfSelectedRows();
            final int tag = item.tag();
            NSEnumerator iterator = transferTable.selectedRowEnumerator();
            while(iterator.hasMoreElements()) {
                int i = ((Number) iterator.nextElement()).intValue();
                Transfer transfer = (Transfer) TransferCollection.instance().get(i);
                if(BandwidthThrottle.UNLIMITED == transfer.getBandwidth()) {
                    if(BandwidthThrottle.UNLIMITED == tag) {
                        item.setState(selected > 1 ? NSCell.MixedState : NSCell.OnState);
                        break;
                    }
                    else {
                        item.setState(NSCell.OffState);
                    }
                }
                else {
                    int bandwidth = (int)transfer.getBandwidth()/1024;
                    if(tag == bandwidth) {
                        item.setState(selected > 1 ? NSCell.MixedState : NSCell.OnState);
                        break;
                    }
                    else {
                        item.setState(NSCell.OffState);
                    }
                }
            }
            return !shouldCancel;
        }
    }

    public void bandwidthPopupChanged(NSPopUpButton sender) {
        NSEnumerator iterator = transferTable.selectedRowEnumerator();
        synchronized(TransferCollection.instance()) {
            int bandwidth = BandwidthThrottle.UNLIMITED;
            if(sender.selectedItem().tag() > 0 ) {
                bandwidth = sender.selectedItem().tag()*1024; // from Kilobytes to Bytes
            }
            while(iterator.hasMoreElements()) {
                int i = ((Number) iterator.nextElement()).intValue();
                Transfer transfer = (Transfer) TransferCollection.instance().get(i);
                transfer.setBandwidth(bandwidth);
            }
        }
        this.updateBandwidthPopup();
    }

    private CDTransferController() {
        ;
    }

    public static CDTransferController instance() {
        synchronized(NSApplication.sharedApplication()) {
            if(null == instance) {
                instance = new CDTransferController();
                if(!NSApplication.loadNibNamed("Transfer", instance)) {
                    log.fatal("Couldn't load Transfer.nib");
                }
            }
            return instance;
        }
    }

    /*
      * @return NSApplication.TerminateLater or NSApplication.TerminateNow depending if there are
      * running transfers to be checked first
      */
    public static int applicationShouldTerminate(NSApplication app) {
        if(null != instance) {
            //Saving state of transfer window
            Preferences.instance().setProperty("queue.openByDefault", instance.window().isVisible());
            if(TransferCollection.instance().numberOfRunningTransfers() > 0) {
                NSWindow sheet = NSAlertPanel.criticalAlertPanel(NSBundle.localizedString("Transfer in progress", ""), //title
                        NSBundle.localizedString("There are files currently being transferred. Quit anyway?", ""), // message
                        NSBundle.localizedString("Quit", ""), // defaultbutton
                        NSBundle.localizedString("Cancel", ""), //alternative button
                        null //other button
                );
                instance.alert(sheet, new CDSheetCallback() {
                    public void callback(int returncode) {
                        if(returncode == DEFAULT_OPTION) { //Quit
                            synchronized(TransferCollection.instance()) {
                                for(int i = 0; i < TransferCollection.instance().size(); i++) {
                                    Transfer transfer = (Transfer) TransferCollection.instance().get(i);
                                    if(transfer.isRunning()) {
                                        transfer.interrupt();
                                    }
                                }
                            }
                            NSApplication.sharedApplication().replyToApplicationShouldTerminate(true);
                        }
                        if(returncode == ALTERNATE_OPTION) { //Cancel
                            NSApplication.sharedApplication().replyToApplicationShouldTerminate(false);
                        }
                    }
                });
                return NSApplication.TerminateLater; //break
            }
        }
        NSApplication.sharedApplication().replyToApplicationShouldTerminate(true);
        return NSApplication.TerminateNow;
    }

    private CDTransferTableDataSource transferModel;
    private NSTableView transferTable; // IBOutlet
    private CDTableDelegate delegate;

    public void setQueueTable(NSTableView view) {
        this.transferTable = view;
        this.transferTable.setDataSource(this.transferModel = new CDTransferTableDataSource());
        this.transferTable.setDelegate(this.delegate = new CDAbstractTableDelegate() {

            public void enterKeyPressed(final Object sender) {
                this.tableRowDoubleClicked(sender);
            }

            public void deleteKeyPressed(final Object sender) {
                deleteButtonClicked(sender);
            }

            public void tableColumnClicked(NSTableView view, NSTableColumn tableColumn) {
                ;
            }

            public void tableRowDoubleClicked(final Object sender) {
                synchronized(TransferCollection.instance()) {
                    if(CDTransferController.this.transferTable.selectedRow() != -1) {
                        Transfer item = (Transfer) TransferCollection.instance().get(CDTransferController.this.transferTable.selectedRow());
                        if(!item.isRunning()) {
                            reloadButtonClicked(sender);
                        }
                    }
                }
            }

            public String tableViewToolTipForCell(NSTableView view, NSCell cell, NSMutableRect rect,
                                                  NSTableColumn tc, int row, NSPoint mouseLocation) {
                if(row < TransferCollection.instance().size()) {
                    TransferCollection.instance().get(row).toString();
                }
                return null;
            }

            public void tableViewSelectionIsChanging(NSNotification aNotification) {
                updateHighlight();
            }

            public void selectionDidChange(NSNotification notification) {
                updateHighlight();
                updateSelection();
            }
        });
        // receive drag events from types
        // in fact we are not interested in file promises, but because the browser model can only initiate
        // a drag with tableView.dragPromisedFilesOfTypes(), we listens for those events
        // and then use the private pasteboard instead.
        this.transferTable.registerForDraggedTypes(new NSArray(new Object[]{
                CDPasteboards.TransferPasteboardType,
                NSPasteboard.StringPboardType,
                NSPasteboard.FilesPromisePboardType}));

        NSSelector setResizableMaskSelector
                = new NSSelector("setResizingMask", new Class[]{int.class});
        {
            NSTableColumn c = new NSTableColumn();
            c.setIdentifier(CDTransferTableDataSource.ICON_COLUMN);
            c.setMinWidth(32f);
            c.setWidth(32f);
            c.setMaxWidth(32f);
            if(setResizableMaskSelector.implementedByClass(NSTableColumn.class)) {
                c.setResizingMask(NSTableColumn.AutoresizingMask);
            }
            else {
                c.setResizable(true);
            }
            c.setDataCell(new CDTransferIconCell());
            this.transferTable.addTableColumn(c);
        }
        {
            NSTableColumn c = new NSTableColumn();
            c.setIdentifier(CDTransferTableDataSource.PROGRESS_COLUMN);
            c.setMinWidth(80f);
            c.setWidth(300f);
            c.setMaxWidth(1000f);
            if(setResizableMaskSelector.implementedByClass(NSTableColumn.class)) {
                c.setResizingMask(NSTableColumn.AutoresizingMask);
            }
            else {
                c.setResizable(true);
            }
            c.setDataCell(new CDProgressCell());
            this.transferTable.addTableColumn(c);
        }
        this.transferTable.setGridStyleMask(NSTableView.SolidHorizontalGridLineMask);

        //selection properties
        this.transferTable.setAllowsMultipleSelection(true);
        this.transferTable.setAllowsEmptySelection(true);
        this.transferTable.setAllowsColumnReordering(false);
        this.transferTable.sizeToFit();
    }

    /**
     *
     */
    private void updateHighlight() {
        log.debug("updateHighlight");
        synchronized(TransferCollection.instance()) {
            boolean key = window().isKeyWindow();
            for(int i = 0; i < TransferCollection.instance().size(); i++) {
                transferModel.setHighlighted((Transfer)TransferCollection.instance().get(i),
                        transferTable.isRowSelected(i) && key);
            }
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
        final int selected = transferTable.numberOfSelectedRows();
        if(1 == selected) {
            final Transfer transfer = (Transfer) TransferCollection.instance().get(transferTable.selectedRow());
            // Draw text fields at the bottom
            urlField.setAttributedStringValue(new NSAttributedString(transfer.getRoot().getHost().getURL(),
                    TRUNCATE_MIDDLE_ATTRIBUTES));
            if(transfer.numberOfRoots() == 1) {
                localField.setAttributedStringValue(new NSAttributedString(transfer.getRoot().getLocal().getAbsolute(),
                        TRUNCATE_MIDDLE_ATTRIBUTES));
            }
            else {
                localField.setAttributedStringValue(new NSAttributedString(NSBundle.localizedString("Multiple files", ""),
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
        log.debug("updateIcon:");
        final int selected = transferTable.numberOfSelectedRows();
        if(1 != selected) {
            iconView.setImage(null);
            return;
        }
        final Transfer transfer = (Transfer) TransferCollection.instance().get(transferTable.selectedRow());
        // Draw file type icon
        if(transfer.getRoot().getLocal().exists()) {
            if(transfer.numberOfRoots() == 1) {
                iconView.setImage(CDIconCache.instance().iconForPath(transfer.getRoot().getLocal(), 32));
            }
            else {
                iconView.setImage(NSImage.imageNamed("multipleDocuments32.tiff"));
            }
        }
    }

    /**
     *
     */
    private void updateBandwidthPopup() {
        log.debug("updateBandwidthPopup");
        final int selected = transferTable.numberOfSelectedRows();
        bandwidthPopup.setEnabled(selected > 0);
        NSEnumerator iterator = transferTable.selectedRowEnumerator();
        synchronized(TransferCollection.instance()) {
            while(iterator.hasMoreElements()) {
                int i = ((Number) iterator.nextElement()).intValue();
                Transfer transfer = (Transfer) TransferCollection.instance().get(i);
                if(transfer instanceof SyncTransfer) {
                    // Currently we do not support bandwidth throtling for sync transfers due to
                    // the problem of mapping both download and upload rate in the GUI
                    bandwidthPopup.setEnabled(false);
                    // Break through and set the standard icon below
                    break;
                }
                if(transfer.getBandwidth() != BandwidthThrottle.UNLIMITED) {
                    // Mark as throttled
                    this.bandwidthPopup.itemAtIndex(0).setImage(NSImage.imageNamed("turtle.tiff"));
                    return;
                }
            }
        }
        // Set the standard icon
        this.bandwidthPopup.itemAtIndex(0).setImage(NSImage.imageNamed("bandwidth16.tiff"));
    }

    private void reloadData() {
        synchronized(instance()) {
            while(transferTable.subviews().count() > 0) {
                ((NSView) transferTable.subviews().lastObject()).removeFromSuperviewWithoutNeedingDisplay();
            }
            transferTable.reloadData();
        }
        this.updateHighlight();
    }

    /**
     * Remove this item form the list
     *
     * @param transfer
     */
    public void removeTransfer(final Transfer transfer) {
        synchronized(TransferCollection.instance()) {
            TransferCollection.instance().remove(transfer);
            this.reloadData();
        }
    }

    /**
     * Add this item to the list; select it and scroll the view to make it visible
     *
     * @param transfer
     */
    public void addTransfer(final Transfer transfer) {
        synchronized(TransferCollection.instance()) {
            TransferCollection.instance().add(transfer);
            final int row = TransferCollection.instance().size()-1;
            this.reloadData();
            transferTable.selectRow(row, false);
            transferTable.scrollRowToVisible(row);
        }
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
        synchronized(TransferCollection.instance()) {
            if(!TransferCollection.instance().contains(transfer)) {
                this.addTransfer(transfer);
            }
        }
        if(Preferences.instance().getBoolean("queue.orderFrontOnStart")) {
            this.window.makeKeyAndOrderFront(null);
        }
        this.background(new BackgroundActionImpl(this) {
            private boolean resume = resumeRequested;
            private boolean reload = reloadRequested;

            private TransferListener tl;

            public void prepare() {
                transfer.getSession().addErrorListener(this);
                transfer.getSession().addTranscriptListener(this);
                transfer.addListener(tl = new TransferAdapter() {
                    public void transferQueued() {
                        invoke(new Runnable() {
                            public void run() {
                                window.toolbar().validateVisibleItems();
                            }
                        });
                    }

                    public void transferPaused() {
                        invoke(new Runnable() {
                            public void run() {
                                window.toolbar().validateVisibleItems();
                            }
                        });
                    }

                    public void transferResumed() {
                        invoke(new Runnable() {
                            public void run() {
                                window.toolbar().validateVisibleItems();
                            }
                        });
                    }

                    public void transferWillStart() {
                        invoke(new Runnable() {
                            public void run() {
                                window.toolbar().validateVisibleItems();
                                updateIcon();
                            }
                        });
                    }

                    public void transferDidEnd() {
                        invoke(new Runnable() {
                            public void run() {
                                window.toolbar().validateVisibleItems();
                                updateIcon();
                            }
                        });
                    }

                    public void didTransferPath(final Path path) {
                        if(!hasFailed()) {
                            clearTranscript();
                        }
                    }
                });
                if(transfer.getSession() instanceof ch.cyberduck.core.sftp.SFTPSession) {
                    ((ch.cyberduck.core.sftp.SFTPSession) transfer.getSession()).setHostKeyVerificationController(
                            new CDHostKeyController(CDTransferController.this));
                }
                if(transfer.getSession() instanceof ch.cyberduck.core.ftps.FTPSSession) {
                    ((ch.cyberduck.core.ftps.FTPSSession) transfer.getSession()).setTrustManager(
                            new CDX509TrustManagerController(CDTransferController.this));
                }
                transfer.getSession().setLoginController(new CDLoginController(CDTransferController.this));
                super.prepare();
            }

            public void run() {
                TransferOptions options = new TransferOptions();
                options.reloadRequested = reload;
                options.resumeRequested = resume;
                transfer.start(CDTransferPrompt.create(CDTransferController.this, transfer), options ,true);
            }

            public void finish() {
                if(transfer.getSession() instanceof ch.cyberduck.core.sftp.SFTPSession) {
                    ((ch.cyberduck.core.sftp.SFTPSession) transfer.getSession()).setHostKeyVerificationController(null);
                }
                if(transfer.getSession() instanceof ch.cyberduck.core.ftps.FTPSSession) {
                    ((ch.cyberduck.core.ftps.FTPSSession) transfer.getSession()).setTrustManager(null);
                }
                transfer.getSession().setLoginController(null);

                transfer.removeListener(tl);
                transfer.getSession().removeErrorListener(this);
                transfer.getSession().removeTranscriptListener(this);
                
                super.finish();
            }

            public void cleanup() {
                if(transfer.isComplete() && !transfer.isCanceled() && !this.hasFailed()) {
                    if(transfer instanceof DownloadTransfer) {
                        Growl.instance().notify("Download complete", transfer.getName());
                        if(Preferences.instance().getBoolean("queue.postProcessItemWhenComplete")) {
                            NSWorkspace.sharedWorkspace().openFile(transfer.getRoot().getLocal().toString());
                        }
                    }
                    if(transfer instanceof UploadTransfer) {
                        Growl.instance().notify("Upload complete", transfer.getName());
                    }
                    if(transfer instanceof SyncTransfer) {
                        Growl.instance().notify("Synchronization complete", transfer.getName());
                    }
                    if(!hasFailed()) {
                        if(Preferences.instance().getBoolean("queue.removeItemWhenComplete")) {
                            removeTransfer(transfer);
                        }
                    }
                    if(Preferences.instance().getBoolean("queue.orderBackOnStop")) {
                        if(!(TransferCollection.instance().numberOfRunningTransfers() > 0)) {
                            window().close();
                        }
                    }
                }
                // Upon retry, use resume
                reload = false;
                resume = true;
                TransferCollection.instance().save();
            }

            public boolean isCanceled() {
                if(transfer.isCanceled()) {
                    return true;
                }
                return super.isCanceled();
            }

            public Session session() {
                return transfer.getSession();
            }

            public void pause(final Object lock) {
                transfer.fireTransferPaused();
                super.pause(lock);
                transfer.fireTransferResumed();
            }
        }, new Object());
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
     * NSToolbar.Delegate
     *
     * @param toolbar
     * @param itemIdentifier
     * @param flag
     */
    public NSToolbarItem toolbarItemForItemIdentifier(NSToolbar toolbar, String itemIdentifier, boolean flag) {
        NSToolbarItem item = new NSToolbarItem(itemIdentifier);
        if(itemIdentifier.equals(TOOLBAR_STOP)) {
            item.setLabel(NSBundle.localizedString(TOOLBAR_STOP, ""));
            item.setPaletteLabel(NSBundle.localizedString(TOOLBAR_STOP, ""));
            item.setToolTip(NSBundle.localizedString(TOOLBAR_STOP, ""));
            item.setImage(NSImage.imageNamed("stop.tiff"));
            item.setTarget(this);
            item.setAction(new NSSelector("stopButtonClicked", new Class[]{Object.class}));
            return item;
        }
        if(itemIdentifier.equals(TOOLBAR_RESUME)) {
            item.setLabel(NSBundle.localizedString(TOOLBAR_RESUME, ""));
            item.setPaletteLabel(NSBundle.localizedString(TOOLBAR_RESUME, ""));
            item.setToolTip(NSBundle.localizedString(TOOLBAR_RESUME, ""));
            item.setImage(NSImage.imageNamed("resume.tiff"));
            item.setTarget(this);
            item.setAction(new NSSelector("resumeButtonClicked", new Class[]{Object.class}));
            return item;
        }
        if(itemIdentifier.equals(TOOLBAR_RELOAD)) {
            item.setLabel(NSBundle.localizedString(TOOLBAR_RELOAD, ""));
            item.setPaletteLabel(NSBundle.localizedString(TOOLBAR_RELOAD, ""));
            item.setToolTip(NSBundle.localizedString(TOOLBAR_RELOAD, ""));
            item.setImage(NSImage.imageNamed("reload.tiff"));
            item.setTarget(this);
            item.setAction(new NSSelector("reloadButtonClicked", new Class[]{Object.class}));
            return item;
        }
        if(itemIdentifier.equals(TOOLBAR_SHOW)) {
            item.setLabel(NSBundle.localizedString(TOOLBAR_SHOW, ""));
            item.setPaletteLabel(NSBundle.localizedString("Show in Finder", ""));
            item.setToolTip(NSBundle.localizedString("Show in Finder", ""));
            item.setImage(NSImage.imageNamed("reveal.tiff"));
            item.setTarget(this);
            item.setAction(new NSSelector("revealButtonClicked", new Class[]{Object.class}));
            return item;
        }
        if(itemIdentifier.equals(TOOLBAR_OPEN)) {
            item.setLabel(NSBundle.localizedString(TOOLBAR_OPEN, ""));
            item.setPaletteLabel(NSBundle.localizedString(TOOLBAR_OPEN, ""));
            item.setToolTip(NSBundle.localizedString(TOOLBAR_OPEN, ""));
            item.setImage(NSImage.imageNamed("open.tiff"));
            item.setTarget(this);
            item.setAction(new NSSelector("openButtonClicked", new Class[]{Object.class}));
            return item;
        }
        if(itemIdentifier.equals(TOOLBAR_REMOVE)) {
            item.setLabel(NSBundle.localizedString(TOOLBAR_REMOVE, ""));
            item.setPaletteLabel(NSBundle.localizedString(TOOLBAR_REMOVE, ""));
            item.setToolTip(NSBundle.localizedString(TOOLBAR_REMOVE, ""));
            item.setImage(NSImage.imageNamed("clean.tiff"));
            item.setTarget(this);
            item.setAction(new NSSelector("deleteButtonClicked", new Class[]{Object.class}));
            return item;
        }
        if(itemIdentifier.equals(TOOLBAR_CLEAN_UP)) {
            item.setLabel(NSBundle.localizedString(TOOLBAR_CLEAN_UP, ""));
            item.setPaletteLabel(NSBundle.localizedString(TOOLBAR_CLEAN_UP, ""));
            item.setToolTip(NSBundle.localizedString(TOOLBAR_CLEAN_UP, ""));
            item.setImage(NSImage.imageNamed("cleanAll.tiff"));
            item.setTarget(this);
            item.setAction(new NSSelector("clearButtonClicked", new Class[]{Object.class}));
            return item;
        }
        if(itemIdentifier.equals(TOOLBAR_TRASH)) {
            item.setLabel(NSBundle.localizedString(TOOLBAR_TRASH, ""));
            item.setPaletteLabel(NSBundle.localizedString(TOOLBAR_TRASH, ""));
            item.setToolTip(NSBundle.localizedString("Move to Trash", ""));
            item.setImage(NSImage.imageNamed("trash.tiff"));
            item.setTarget(this);
            item.setAction(new NSSelector("trashButtonClicked", new Class[]{Object.class}));
            return item;
        }
        if(itemIdentifier.equals(TOOLBAR_FILTER)) {
            item.setLabel(NSBundle.localizedString(TOOLBAR_FILTER, ""));
            item.setPaletteLabel(NSBundle.localizedString(TOOLBAR_FILTER, ""));
            item.setView(this.filterField);
            item.setMinSize(this.filterField.frame().size());
            item.setMaxSize(this.filterField.frame().size());
            return item;
        }
        // itemIdent refered to a toolbar item that is not provide or supported by us or cocoa.
        // Returning null will inform the toolbar this kind of item is not supported.
        return null;
    }

    public void paste(final Object sender) {
        log.debug("paste");
        NSPasteboard pboard = NSPasteboard.pasteboardWithName(CDPasteboards.TransferPasteboard);
        if(pboard.availableTypeFromArray(new NSArray(CDPasteboards.TransferPasteboardType)) != null) {
            Object o = pboard.propertyListForType(CDPasteboards.TransferPasteboardType);// get the data from paste board
            if(o != null) {
                NSArray elements = (NSArray) o;
                for(int i = 0; i < elements.count(); i++) {
                    NSDictionary dict = (NSDictionary) elements.objectAtIndex(i);
                    TransferCollection.instance().add(TransferFactory.create(dict));
                }
                pboard.setPropertyListForType(null, CDPasteboards.TransferPasteboardType);
                this.reloadData();
            }
        }
    }

    public void stopButtonClicked(final Object sender) {
        NSEnumerator iterator = transferTable.selectedRowEnumerator();
        while(iterator.hasMoreElements()) {
            int i = ((Number) iterator.nextElement()).intValue();
            final Transfer transfer = (Transfer) TransferCollection.instance().get(i);
            if(transfer.isRunning() || transfer.isQueued()) {
                this.background(new BackgroundActionImpl(this) {
                    public void run() {
                        transfer.cancel();
                    }

                    public void cleanup() {
                        ;
                    }
                });
            }
        }
    }

    public void stopAllButtonClicked(final Object sender) {
        for(int i = 0; i < TransferCollection.instance().size(); i++) {
            final Transfer transfer = (Transfer) TransferCollection.instance().get(i);
            if(transfer.isRunning() || transfer.isQueued()) {
                this.background(new BackgroundActionImpl(this) {
                    public void run() {
                        transfer.cancel();
                    }

                    public void cleanup() {
                        ;
                    }
                });
            }
        }
    }

    public void resumeButtonClicked(final Object sender) {
        NSEnumerator iterator = transferTable.selectedRowEnumerator();
        while(iterator.hasMoreElements()) {
            int i = ((Number) iterator.nextElement()).intValue();
            Transfer transfer = (Transfer) TransferCollection.instance().get(i);
            if(!transfer.isRunning()) {
                this.startTransfer(transfer, !transfer.isVirgin(), false);
            }
        }
    }

    public void reloadButtonClicked(final Object sender) {
        NSEnumerator iterator = transferTable.selectedRowEnumerator();
        while(iterator.hasMoreElements()) {
            int i = ((Number) iterator.nextElement()).intValue();
            Transfer transfer = (Transfer) TransferCollection.instance().get(i);
            if(!transfer.isRunning() && !transfer.isQueued()) {
                this.startTransfer(transfer, false, true);
            }
        }
    }

    public void openButtonClicked(final Object sender) {
        if(transferTable.numberOfSelectedRows() == 1) {
            Transfer q = (Transfer) TransferCollection.instance().get(transferTable.selectedRow());
            for(Iterator iter = q.getRoots().iterator(); iter.hasNext();) {
                Local l = ((Path) iter.next()).getLocal();
                if(!NSWorkspace.sharedWorkspace().openFile(l.getAbsolute())) {
                    if(q.isComplete()) {
                        this.alert(NSAlertPanel.criticalAlertPanel(NSBundle.localizedString("Could not open the file", ""), //title
                                NSBundle.localizedString("Could not open the file", "") + " \""
                                        + l.getName()
                                        + "\". " + NSBundle.localizedString("It moved since you downloaded it.", ""), // message
                                NSBundle.localizedString("OK", ""), // defaultbutton
                                null, //alternative button
                                null //other button
                        ));
                    }
                    else {
                        this.alert(NSAlertPanel.criticalAlertPanel(NSBundle.localizedString("Could not open the file", ""), //title
                                NSBundle.localizedString("Could not open the file", "") + " \""
                                        + l.getName()
                                        + "\". " + NSBundle.localizedString("The file has not yet been downloaded.", ""), // message
                                NSBundle.localizedString("OK", ""), // defaultbutton
                                null, //alternative button
                                null //other button
                        ));
                    }
                }
            }
        }
    }

    public void revealButtonClicked(final Object sender) {
        if(transferTable.numberOfSelectedRows() == 1) {
            Transfer q = (Transfer) TransferCollection.instance().get(transferTable.selectedRow());
            for(Iterator iter = q.getRoots().iterator(); iter.hasNext();) {
                Local l = ((Path) iter.next()).getLocal();
                // If a second path argument is specified, a new file viewer is opened. If you specify an
                // empty string (@"") for this parameter, the file is selected in the main viewer.
                if(!NSWorkspace.sharedWorkspace().selectFile(l.getAbsolute(), l.getParent().getAbsolute())) {
                    if(q.isComplete()) {
                        this.alert(NSAlertPanel.criticalAlertPanel(NSBundle.localizedString("Could not show the file in the Finder", ""), //title
                                NSBundle.localizedString("Could not show the file", "") + " \""
                                        + l.getName()
                                        + "\". " + NSBundle.localizedString("It moved since you downloaded it.", ""), // message
                                NSBundle.localizedString("OK", ""), // defaultbutton
                                null, //alternative button
                                null //other button
                        ));
                    }
                    else {
                        this.alert(NSAlertPanel.criticalAlertPanel(NSBundle.localizedString("Could not show the file in the Finder", ""), //title
                                NSBundle.localizedString("Could not show the file", "") + " \""
                                        + l.getName()
                                        + "\". " + NSBundle.localizedString("The file has not yet been downloaded.", ""), // message
                                NSBundle.localizedString("OK", ""), // defaultbutton
                                null, //alternative button
                                null //other button
                        ));
                    }
                }
                else break;
            }
        }
    }

    public void deleteButtonClicked(final Object sender) {
        NSEnumerator iterator = transferTable.selectedRowEnumerator();
        int j = 0;
        while(iterator.hasMoreElements()) {
            int i = ((Number) iterator.nextElement()).intValue();
            Transfer q = (Transfer) TransferCollection.instance().get(i - j);
            if(!q.isRunning()) {
                TransferCollection.instance().remove(i - j);
                j++;
            }
        }
        TransferCollection.instance().save();
        this.reloadData();
    }

    public void clearButtonClicked(final Object sender) {
        for(int i = 0; i < TransferCollection.instance().size(); i++) {
            Transfer t = (Transfer)TransferCollection.instance().get(i);
            if(!t.isRunning() && t.isComplete()) {
                TransferCollection.instance().remove(i);
                i--;
            }
        }
        TransferCollection.instance().save();
        this.reloadData();
    }

    public void trashButtonClicked(final Object sender) {
        NSEnumerator iterator = transferTable.selectedRowEnumerator();
        while(iterator.hasMoreElements()) {
            int i = ((Number) iterator.nextElement()).intValue();
            Transfer q = (Transfer) TransferCollection.instance().get(i);
            if(!q.isRunning()) {
                for(Iterator iter = q.getRoots().iterator(); iter.hasNext();) {
                    Local l = ((Path) iter.next()).getLocal();
                    if(l.exists()) {
                        if(0 > NSWorkspace.sharedWorkspace().performFileOperation(NSWorkspace.RecycleOperation,
                                l.getParent().getAbsolute(), "", new NSArray(l.getName()))) {
                            log.warn("Failed to move "+l.getAbsolute()+" to Trash");
                        }
                    }
                }
            }
        }
        this.updateIcon();
    }

    /**
     * NSToolbar.Delegate
     *
     * @param toolbar
     */
    public NSArray toolbarDefaultItemIdentifiers(NSToolbar toolbar) {
        return new NSArray(new Object[]{
                TOOLBAR_RESUME,
                TOOLBAR_RELOAD,
                TOOLBAR_STOP,
                TOOLBAR_REMOVE,
                TOOLBAR_CLEAN_UP,
                NSToolbarItem.FlexibleSpaceItemIdentifier,
                TOOLBAR_OPEN,
                TOOLBAR_SHOW
        });
    }

    /**
     * NSToolbar.Delegate
     *
     * @param toolbar
     */
    public NSArray toolbarAllowedItemIdentifiers(NSToolbar toolbar) {
        return new NSArray(new Object[]{
                TOOLBAR_RESUME,
                TOOLBAR_RELOAD,
                TOOLBAR_STOP,
                TOOLBAR_REMOVE,
                TOOLBAR_CLEAN_UP,
                TOOLBAR_SHOW,
                TOOLBAR_OPEN,
                TOOLBAR_TRASH,
                TOOLBAR_FILTER,
                NSToolbarItem.CustomizeToolbarItemIdentifier,
                NSToolbarItem.SpaceItemIdentifier,
                NSToolbarItem.SeparatorItemIdentifier,
                NSToolbarItem.FlexibleSpaceItemIdentifier
        });
    }

    /**
     * @param item
     */
    public boolean validateMenuItem(NSMenuItem item) {
        String identifier = item.action().name();
        if(item.action().name().equals("paste:")) {
            boolean valid = false;
            NSPasteboard pboard = NSPasteboard.pasteboardWithName(CDPasteboards.TransferPasteboard);
            if(pboard.availableTypeFromArray(new NSArray(CDPasteboards.TransferPasteboardType)) != null) {
                Object o = pboard.propertyListForType(CDPasteboards.TransferPasteboardType);
                if(o != null) {
                    NSArray elements = (NSArray) o;
                    for(int i = 0; i < elements.count(); i++) {
                        NSDictionary dict = (NSDictionary) elements.objectAtIndex(i);
                        Transfer q = TransferFactory.create(dict);
                        if(q.numberOfRoots() == 1)
                            item.setTitle(NSBundle.localizedString("Paste", "Menu item") + " \""
                                    + q.getRoot().getName() + "\"");
                        else {
                            item.setTitle(NSBundle.localizedString("Paste", "Menu item")
                                    + " (" + q.numberOfRoots() + " " +
                                    NSBundle.localizedString("files", "") + ")");
                        }
                        valid = true;
                    }
                }
            }
            if(!valid) {
                item.setTitle(NSBundle.localizedString("Paste", "Menu item"));
            }
        }
        return this.validateItem(identifier);
    }

    /**
     * @param item
     */
    public boolean validateToolbarItem(NSToolbarItem item) {
        return this.validateItem(item.action().name());
    }

    /**
     * Validates menu and toolbar items
     *
     * @param identifier
     * @return true if the item with the identifier should be selectable
     */
    private boolean validateItem(String identifier) {
        if(identifier.equals("paste:")) {
            NSPasteboard pboard = NSPasteboard.pasteboardWithName(CDPasteboards.TransferPasteboard);
            if(pboard.availableTypeFromArray(new NSArray(CDPasteboards.TransferPasteboardType)) != null) {
                return pboard.propertyListForType(CDPasteboards.TransferPasteboardType) != null;
            }
            return false;
        }
        if(identifier.equals("stopButtonClicked:")) {
            return this.validate(new TransferToolbarValidator() {
                public boolean validate(Transfer transfer) {
                    return transfer.isRunning() || transfer.isQueued();
                }
            });
        }
        if(identifier.equals("reloadButtonClicked:")
                || identifier.equals("deleteButtonClicked:")) {
            return this.validate(new TransferToolbarValidator() {
                public boolean validate(Transfer transfer) {
                    return !transfer.isRunning() && !transfer.isQueued();
                }
            });
        }
        if(identifier.equals("resumeButtonClicked:")) {
            return this.validate(new TransferToolbarValidator() {
                public boolean validate(Transfer transfer) {
                    return transfer.isResumable();
                }
            });
        }
        if(identifier.equals("openButtonClicked:")
                || identifier.equals("trashButtonClicked:")) {
            return this.validate(new TransferToolbarValidator() {
                public boolean validate(Transfer transfer) {
                    if(!transfer.isRunning()) {
                        for(Iterator iter = transfer.getRoots().iterator(); iter.hasNext(); ) {
                            if(((Path)iter.next()).getLocal().exists()) {
                                return true;
                            }
                        }
                    }
                    return false;
                }
            });
        }
        if(identifier.equals("revealButtonClicked:")) {
            return this.validate(new TransferToolbarValidator() {
                public boolean validate(Transfer transfer) {
                    for(Iterator iter = transfer.getRoots().iterator(); iter.hasNext(); ) {
                        if(((Path)iter.next()).getLocal().exists()) {
                            return true;
                        }
                    }
                    return false;
                }
            });
        }
        if(identifier.equals("clearButtonClicked:")) {
            return transferTable.numberOfRows() > 0;
        }
        return true;
    }

    /**
     * Validates the selected items in the transfer window against the toolbar validator
     * @param v The validator to use
     * @return True if one or more of the selected items passes the validation test
     */
    private boolean validate(TransferToolbarValidator v) {
        NSEnumerator iterator = transferTable.selectedRowEnumerator();
        synchronized(TransferCollection.instance()) {
            while(iterator.hasMoreElements()) {
                int i = ((Number) iterator.nextElement()).intValue();
                Transfer transfer = (Transfer) TransferCollection.instance().get(i);
                if(v.validate(transfer)) {
                    return true;
                }
            }
        }
        return false;
    }

    private interface TransferToolbarValidator {
        public boolean validate(Transfer transfer);
    }
}