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
import ch.cyberduck.ui.cocoa.threading.BackgroundActionImpl;

import com.apple.cocoa.application.*;
import com.apple.cocoa.foundation.*;

import org.apache.log4j.Logger;

import java.util.Iterator;

/**
 * @version $Id$
 */
public class CDQueueController extends CDWindowController
        implements NSToolbarItem.ItemValidation {

    private static Logger log = Logger.getLogger(CDQueueController.class);

    private static CDQueueController instance;

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
        this.updateSelection();
    }

    /**
     * @param notification
     */
    public void windowDidResignKey(NSNotification notification) {
        this.updateSelection();
    }

    /**
     * @param notification
     */
    public void windowWillClose(NSNotification notification) {
        QueueCollection.instance().save();
    }

    // ----------------------------------------------------------
    // Outlets
    // ----------------------------------------------------------

    private NSTextField urlLabel;

    public void setUrlLabel(NSTextField urlLabel) {
        this.urlLabel = urlLabel;
        this.urlLabel.setTextColor(NSColor.darkGrayColor());
        this.urlLabel.setStringValue("URL:");
    }

    private NSTextField urlField;

    public void setUrlField(NSTextField urlField) {
        this.urlField = urlField;
    }

    private NSTextField localLabel;

    public void setLocalLabel(NSTextField localLabel) {
        this.localLabel = localLabel;
        this.localLabel.setTextColor(NSColor.darkGrayColor());
        this.localLabel.setStringValue(NSBundle.localizedString("Local File", "") + ":");
    }

    private NSTextField localField;

    public void setLocalField(NSTextField localField) {
        this.localField = localField;
    }

    private CDQueueController() {
        ;
    }

    private static final Object lock = new Object();

    public static CDQueueController instance() {
        synchronized(NSApplication.sharedApplication()) {
            if(null == instance) {
                instance = new CDQueueController();
                if(!NSApplication.loadNibNamed("Queue", instance)) {
                    log.fatal("Couldn't load Queue.nib");
                }
            }
            return instance;
        }
    }

    /**
     * @return true if any transfer is active
     */
    public boolean hasRunningTransfers() {
        synchronized(QueueCollection.instance()) {
            for(int i = 0; i < QueueCollection.instance().size(); i++) {
                Transfer q = (Transfer) QueueCollection.instance().get(i);
                if(q.isRunning()) {
                    return true;
                }
            }
            return false;
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
            if(instance.hasRunningTransfers()) {
                NSWindow sheet = NSAlertPanel.criticalAlertPanel(NSBundle.localizedString("Transfer in progress", ""), //title
                        NSBundle.localizedString("There are items in the queue currently being transferred. Quit anyway?", ""), // message
                        NSBundle.localizedString("Quit", ""), // defaultbutton
                        NSBundle.localizedString("Cancel", ""), //alternative button
                        null //other button
                );
                instance.alert(sheet, new CDSheetCallback() {
                    public void callback(int returncode) {
                        if(returncode == DEFAULT_OPTION) { //Quit
                            synchronized(QueueCollection.instance()) {
                                for(int i = 0; i < QueueCollection.instance().size(); i++) {
                                    Transfer transfer = (Transfer) QueueCollection.instance().get(i);
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

    private CDQueueTableDataSource queueModel;
    private NSTableView queueTable; // IBOutlet
    private CDTableDelegate delegate;

    public void setQueueTable(NSTableView view) {
        this.queueTable = view;
        this.queueTable.setDataSource(this.queueModel = new CDQueueTableDataSource());
        this.queueTable.setDelegate(this.delegate = new CDAbstractTableDelegate() {

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
                synchronized(QueueCollection.instance()) {
                    if(CDQueueController.this.queueTable.selectedRow() != -1) {
                        Transfer item = (Transfer) QueueCollection.instance().get(CDQueueController.this.queueTable.selectedRow());
                        if(!item.isRunning()) {
                            reloadButtonClicked(sender);
                        }
                    }
                }
            }

            public String tableViewToolTipForCell(NSTableView view, NSCell cell, NSMutableRect rect,
                                                  NSTableColumn tc, int row, NSPoint mouseLocation) {
                if(row < queueModel.numberOfRowsInTableView(view)) {
                    QueueCollection.instance().get(row).toString();
                }
                return null;
            }

            public void tableViewSelectionIsChanging(NSNotification aNotification) {
                updateSelection();
            }

            public void selectionDidChange(NSNotification notification) {
                updateSelection();
            }
        });
        // receive drag events from types
        // in fact we are not interested in file promises, but because the browser model can only initiate
        // a drag with tableView.dragPromisedFilesOfTypes(), we listens for those events
        // and then use the private pasteboard instead.
        this.queueTable.registerForDraggedTypes(new NSArray(new Object[]{
                "QueuePBoardType",
                NSPasteboard.StringPboardType,
                NSPasteboard.FilesPromisePboardType}));

        NSSelector setResizableMaskSelector
                = new NSSelector("setResizingMask", new Class[]{int.class});
        {
            NSTableColumn c = new NSTableColumn();
            c.setIdentifier(CDQueueTableDataSource.ICON_COLUMN);
            c.setMinWidth(32f);
            c.setWidth(32f);
            c.setMaxWidth(32f);
            if(setResizableMaskSelector.implementedByClass(NSTableColumn.class)) {
                c.setResizingMask(NSTableColumn.AutoresizingMask);
            }
            else {
                c.setResizable(true);
            }
            c.setDataCell(new CDIconCell());
            this.queueTable.addTableColumn(c);
        }
        {
            NSTableColumn c = new NSTableColumn();
            c.setIdentifier(CDQueueTableDataSource.PROGRESS_COLUMN);
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
            this.queueTable.addTableColumn(c);
        }
        this.queueTable.setGridStyleMask(NSTableView.SolidHorizontalGridLineMask);

        //selection properties
        this.queueTable.setAllowsMultipleSelection(true);
        this.queueTable.setAllowsEmptySelection(true);
        this.queueTable.setAllowsColumnReordering(false);
        this.queueTable.sizeToFit();
    }

    /**
     * Highlights the currently selected item and udpates the text fields
     */
    private void updateSelection() {
        synchronized(QueueCollection.instance()) {
            boolean key = window().isKeyWindow();
            for(int i = 0; i < QueueCollection.instance().size(); i++) {
                QueueCollection.instance().getController(i).setHighlighted(queueTable.isRowSelected(i) && key);
            }
            if(queueTable.selectedRow() != -1) {
                final Transfer q = (Transfer) QueueCollection.instance().get(queueTable.selectedRow());
                if(q.numberOfRoots() == 1) {
                    urlField.setAttributedStringValue(new NSAttributedString(q.getRoot().getHost().getURL()
                            + q.getRoot().getAbsolute(),
                            TRUNCATE_MIDDLE_ATTRIBUTES));
                    localField.setAttributedStringValue(new NSAttributedString(q.getRoot().getLocal().getAbsolute(),
                            TRUNCATE_MIDDLE_ATTRIBUTES));
                }
                else {
                    urlField.setAttributedStringValue(new NSAttributedString(q.getRoot().getHost().getURL()
                            + q.getRoot().getAbsolute(),
                            TRUNCATE_MIDDLE_ATTRIBUTES));
                    localField.setAttributedStringValue(new NSAttributedString(NSBundle.localizedString("Multiple files", ""),
                            TRUNCATE_MIDDLE_ATTRIBUTES));
                }
            }
            else {
                urlField.setStringValue("");
                localField.setStringValue("");
            }
            toolbar.validateVisibleItems();
        }
    }

    // ----------------------------------------------------------
    //
    // ----------------------------------------------------------

    private void reloadQueueTable() {
        synchronized(CDQueueController.instance()) {
            while(this.queueTable.subviews().count() > 0) {
                ((NSView) this.queueTable.subviews().lastObject()).removeFromSuperviewWithoutNeedingDisplay();
            }
            this.queueTable.reloadData();
            this.updateSelection();
        }
    }

    /**
     * Remove this item form the list
     *
     * @param transfer
     */
    public void removeItem(final Transfer transfer) {
        synchronized(QueueCollection.instance()) {
            QueueCollection.instance().remove(transfer);
            this.reloadQueueTable();
        }
    }

    /**
     * Add this item to the list; select it and scroll the view to make it visible
     *
     * @param transfer
     */
    public void addItem(final Transfer transfer) {
        synchronized(QueueCollection.instance()) {
            int row = QueueCollection.instance().size();
            QueueCollection.instance().add(row, transfer);
            this.reloadQueueTable();
            this.queueTable.selectRow(row, false);
            this.queueTable.scrollRowToVisible(row);
        }
    }

    /**
     * @param transfer
     */
    public void startItem(final Transfer transfer) {
        this.startItem(transfer, false, false);
    }

    /**
     * @param transfer
     * @param resumeRequested
     * @param reloadRequested
     */
    private void startItem(final Transfer transfer, final boolean resumeRequested, final boolean reloadRequested) {
        synchronized(QueueCollection.instance()) {
            if(!QueueCollection.instance().contains(transfer)) {
                this.addItem(transfer);
            }
        }
        if(Preferences.instance().getBoolean("queue.orderFrontOnStart")) {
            this.window.makeKeyAndOrderFront(null);
        }
        this.background(new BackgroundActionImpl(this) {
            boolean resume = resumeRequested;
            boolean reload = reloadRequested;

            public void run() {
                try {
                    transfer.getSession().addErrorListener(this);
                    transfer.getSession().addTranscriptListener(this);
                    transfer.addListener(new TransferListener() {

                        public void transferStarted(final Path path) {
                            if(path.attributes.isFile() && !path.getLocal().exists()) {
                                invoke(new Runnable() {
                                    public void run() {
                                        path.getLocal().createNewFile(); //hack to display actual icon #CDIconCell
                                    }
                                });
                            }
                            queueTable.setNeedsDisplay(true);
                        }

                        public void transferStopped(final Path path) {
                            queueTable.setNeedsDisplay(true);
                        }

                        public void queueStarted() {
                            invoke(new Runnable() {
                                public void run() {
                                    window.toolbar().validateVisibleItems();
                                }
                            });
                            if(transfer.getSession() instanceof ch.cyberduck.core.sftp.SFTPSession) {
                                ((ch.cyberduck.core.sftp.SFTPSession) transfer.getSession()).setHostKeyVerificationController(
                                        new CDHostKeyController(CDQueueController.instance()));
                            }
                            if(transfer.getSession() instanceof ch.cyberduck.core.ftps.FTPSSession) {
                                ((ch.cyberduck.core.ftps.FTPSSession) transfer.getSession()).setTrustManager(
                                        new CDX509TrustManagerController(CDQueueController.instance()));
                            }
                            transfer.getSession().setLoginController(new CDLoginController(CDQueueController.instance()));
                        }

                        public void queueStopped() {
                            transfer.removeListener(this);
                            if(transfer.isComplete() && !transfer.isCanceled()) {
                                if(transfer instanceof DownloadTransfer) {
                                    if(Preferences.instance().getBoolean("queue.postProcessItemWhenComplete")) {
                                        NSWorkspace.sharedWorkspace().openFile(transfer.getRoot().getLocal().toString());
                                    }
                                }
                                if(!hasFailed()) {
                                    if(Preferences.instance().getBoolean("queue.removeItemWhenComplete")) {
                                        invoke(new Runnable() {
                                            public void run() {
                                                removeItem(transfer);
                                            }
                                        });
                                    }
                                }
                            }
                            if(transfer.getSession() instanceof ch.cyberduck.core.sftp.SFTPSession) {
                                ((ch.cyberduck.core.sftp.SFTPSession) transfer.getSession()).setHostKeyVerificationController(null);
                            }
                            if(transfer.getSession() instanceof ch.cyberduck.core.ftps.FTPSSession) {
                                ((ch.cyberduck.core.ftps.FTPSSession) transfer.getSession()).setTrustManager(null);
                            }
                            transfer.getSession().setLoginController(null);
                        }
                    });
                    transfer.setResumeReqested(resume);
                    transfer.setReloadRequested(reload);
                    transfer.run(ValidatorFactory.create(transfer, CDQueueController.this));
                }
                finally {
                    transfer.getSession().close();
                    transfer.getSession().cache().clear();
                    transfer.getSession().removeErrorListener(this);
                    transfer.getSession().removeTranscriptListener(this);
                }
            }

            public void callback(final int returncode) {
                if(returncode == CDSheetCallback.DEFAULT_OPTION) { //Try Again
                    // Upon retry, use resume
                    reload = false;
                    resume = true;
                }
            }

            public void cleanup() {
                window.toolbar().validateVisibleItems();
                if(transfer.isComplete()) {
                    if(Preferences.instance().getBoolean("queue.orderBackOnStop")) {
                        if(!hasRunningTransfers()) {
                            window().close();
                        }
                    }
                }
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
        // itemIdent refered to a toolbar item that is not provide or supported by us or cocoa.
        // Returning null will inform the toolbar this kind of item is not supported.
        return null;
    }

    public void paste(final Object sender) {
        log.debug("paste");
        NSPasteboard pboard = NSPasteboard.pasteboardWithName("QueuePBoard");
        if(pboard.availableTypeFromArray(new NSArray("QueuePBoardType")) != null) {
            Object o = pboard.propertyListForType("QueuePBoardType");// get the data from paste board
            if(o != null) {
                NSArray elements = (NSArray) o;
                for(int i = 0; i < elements.count(); i++) {
                    NSDictionary dict = (NSDictionary) elements.objectAtIndex(i);
                    QueueCollection.instance().add(TransferFactory.create(dict));
                }
                pboard.setPropertyListForType(null, "QueuePBoardType");
                this.reloadQueueTable();
            }
        }
    }

    public void stopButtonClicked(final Object sender) {
        NSEnumerator iterator = queueTable.selectedRowEnumerator();
        while(iterator.hasMoreElements()) {
            int i = ((Number) iterator.nextElement()).intValue();
            Transfer transfer = (Transfer) QueueCollection.instance().get(i);
            if(transfer.isRunning()) {
                transfer.cancel();
            }
        }
    }

    public void stopAllButtonClicked(final Object sender) {
        for(int i = 0; i < QueueCollection.instance().size(); i++) {
            Transfer transfer = (Transfer) QueueCollection.instance().get(i);
            if(transfer.isRunning()) {
                transfer.cancel();
            }
        }
    }

    public void resumeButtonClicked(final Object sender) {
        NSEnumerator iterator = queueTable.selectedRowEnumerator();
        while(iterator.hasMoreElements()) {
            int i = ((Number) iterator.nextElement()).intValue();
            Transfer transfer = (Transfer) QueueCollection.instance().get(i);
            if(!transfer.isRunning()) {
                this.startItem(transfer, !transfer.isVirgin(), false);
            }
        }
    }

    public void reloadButtonClicked(final Object sender) {
        NSEnumerator iterator = queueTable.selectedRowEnumerator();
        while(iterator.hasMoreElements()) {
            int i = ((Number) iterator.nextElement()).intValue();
            Transfer transfer = (Transfer) QueueCollection.instance().get(i);
            if(!transfer.isRunning()) {
                this.startItem(transfer, false, true);
            }
        }
    }

    public void openButtonClicked(final Object sender) {
        if(this.queueTable.numberOfSelectedRows() == 1) {
            Transfer q = (Transfer) QueueCollection.instance().get(this.queueTable.selectedRow());
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
        if(this.queueTable.numberOfSelectedRows() == 1) {
            Transfer q = (Transfer) QueueCollection.instance().get(this.queueTable.selectedRow());
            for(Iterator iter = q.getRoots().iterator(); iter.hasNext();) {
                Local l = ((Path) iter.next()).getLocal();
                // If a second path argument is specified, a new file viewer is opened. If you specify an
                // empty string (@"") for this parameter, the file is selected in the main viewer.
                if(!NSWorkspace.sharedWorkspace().selectFile(l.getAbsolute(), l.getParentFile().getAbsolutePath())) {
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
        NSEnumerator iterator = queueTable.selectedRowEnumerator();
        int j = 0;
        while(iterator.hasMoreElements()) {
            int i = ((Number) iterator.nextElement()).intValue();
            Transfer q = (Transfer) QueueCollection.instance().get(i - j);
            if(!q.isRunning()) {
                QueueCollection.instance().remove(i - j);
                j++;
            }
        }
        this.reloadQueueTable();
    }

    public void clearButtonClicked(final Object sender) {
        for(int i = 0; i < QueueCollection.instance().size(); i++) {
            CDProgressController c = QueueCollection.instance().getController(i);
            if(!c.getQueue().isRunning() && c.getQueue().isComplete()) {
                QueueCollection.instance().remove(i);
                i--;
            }
        }
        this.reloadQueueTable();
    }

    public void trashButtonClicked(final Object sender) {
        NSEnumerator iterator = queueTable.selectedRowEnumerator();
        while(iterator.hasMoreElements()) {
            int i = ((Number) iterator.nextElement()).intValue();
            Transfer q = (Transfer) QueueCollection.instance().get(i);
            if(!q.isRunning()) {
                for(Iterator iter = q.getRoots().iterator(); iter.hasNext();) {
                    Local l = ((Path) iter.next()).getLocal();
                    if(l.exists()) {
                        if(0 > NSWorkspace.sharedWorkspace().performFileOperation(NSWorkspace.RecycleOperation,
                                l.getParent(), "", new NSArray(l.getName()))) {
                            log.warn("Failed to move "+l.getAbsolute()+" to Trash");
                        }
                    }
                }
            }
        }
        this.reloadQueueTable();
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
            NSPasteboard pboard = NSPasteboard.pasteboardWithName("QueuePBoard");
            if(pboard.availableTypeFromArray(new NSArray("QueuePBoardType")) != null) {
                Object o = pboard.propertyListForType("QueuePBoardType");
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
            NSPasteboard pboard = NSPasteboard.pasteboardWithName("QueuePBoard");
            if(pboard.availableTypeFromArray(new NSArray("QueuePBoardType")) != null) {
                return pboard.propertyListForType("QueuePBoardType") != null;
            }
            return false;
        }
        if(identifier.equals("stopButtonClicked:")) {
            return this.validate(new TransferToolbarValidator() {
                public boolean validate(Transfer transfer) {
                    return transfer.isRunning();
                }
            });
        }
        if(identifier.equals("reloadButtonClicked:")
                || identifier.equals("deleteButtonClicked:")) {
            return this.validate(new TransferToolbarValidator() {
                public boolean validate(Transfer transfer) {
                    return !transfer.isRunning();
                }
            });
        }
        if(identifier.equals("resumeButtonClicked:")) {
            return this.validate(new TransferToolbarValidator() {
                public boolean validate(Transfer transfer) {
                    return !transfer.isRunning() && !transfer.isComplete() && !transfer.isVirgin();
                }
            });
        }
        if(identifier.equals("openButtonClicked:")
                || identifier.equals("revealButtonClicked:")
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
        if(identifier.equals("clearButtonClicked:")) {
            return this.queueTable.numberOfRows() > 0;
        }
        return true;
    }

    private boolean validate(TransferToolbarValidator v) {
        NSEnumerator iterator = queueTable.selectedRowEnumerator();
        while(iterator.hasMoreElements()) {
            synchronized(QueueCollection.instance()) {
                int i = ((Number) iterator.nextElement()).intValue();
                Transfer transfer = (Transfer) QueueCollection.instance().get(i);
                if(null == transfer) {
                    return false;
                }
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