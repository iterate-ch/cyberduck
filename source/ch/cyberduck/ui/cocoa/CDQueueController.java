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

import com.apple.cocoa.application.*;
import com.apple.cocoa.foundation.*;

import org.apache.log4j.Logger;

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
        this.updateTableViewSelection();
        //Saving state of transfer window
        Preferences.instance().setProperty("queue.openByDefault", true);
    }

    /**
     * @param notification
     */
    public void windowDidResignKey(NSNotification notification) {
        this.updateTableViewSelection();
    }

    /**
     * @param notification
     */
    public void windowWillClose(NSNotification notification) {
        this.queueModel.save();
        //Saving state of transfer window
        Preferences.instance().setProperty("queue.openByDefault", false);
    }

    // ----------------------------------------------------------
    // Outlets
    // ----------------------------------------------------------

    private NSDrawer logDrawer; // IBOutlet

    public void setLogDrawer(NSDrawer logDrawer) {
        this.logDrawer = logDrawer;
    }

    private NSTextView logView;

    public void setLogView(NSTextView logView) {
        this.logView = logView;
    }

    public void toggleLogDrawer(final Object sender) {
        this.logDrawer.toggle(this);
    }

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
        synchronized(lock) {
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
        for(int i = 0; i < this.queueModel.size(); i++) {
            Queue q = (Queue) this.queueModel.get(i);
            if(q.isRunning()) {
                return true;
            }
        }
        return false;
    }

    /*
      * @return NSApplication.TerminateLater or NSApplication.TerminateNow depending if there are
      * running transfers to be checked first
      */
    public static int applicationShouldTerminate(NSApplication app) {
        if(null != instance) {
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
                            for(int i = 0; i < instance.queueModel.size(); i++) {
                                Queue queue = (Queue) instance.queueModel.get(i);
                                if(queue.isRunning()) {
                                    queue.interrupt();
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

        this.queueTable.setDataSource(this.queueModel = CDQueueTableDataSource.instance());
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
                if(CDQueueController.this.queueTable.selectedRow() != -1) {
                    Queue item = (Queue) queueModel.get(CDQueueController.this.queueTable.selectedRow());
                    if(!item.isRunning()) {
                        reloadButtonClicked(sender);
                    }
                }
            }

            public String tableViewToolTipForCell(NSTableView view, NSCell cell, NSMutableRect rect,
                                                  NSTableColumn tc, int row, NSPoint mouseLocation) {
                if(row < queueModel.numberOfRowsInTableView(view)) {
                    queueModel.get(row).toString();
                }
                return null;
            }

            /**
             *
             * @param view
             * @param i
             * @return The height of the particular view in this row
             */
            public float tableViewHeightOfRow(NSTableView view, int i) {
                final CDProgressController c = queueModel.getController(i);
                if(null == c) {
                    log.warn("No progress controller at index "+i);
                    return 0;
                }
                return c.view().frame().size().height();
            }

            public void tableViewSelectionIsChanging(NSNotification aNotification) {
                updateTableViewSelection();
            }

            public void selectionDidChange(NSNotification notification) {
                updateTableViewSelection();
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
                c.setResizingMask(NSTableColumn.AutoresizingMask | NSTableColumn.UserResizingMask);
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
                c.setResizingMask(NSTableColumn.AutoresizingMask | NSTableColumn.UserResizingMask);
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
    private void updateTableViewSelection() {
        boolean key = window().isKeyWindow();
        for(int i = 0; i < queueModel.size(); i++) {
            queueModel.getController(i).setHighlighted(queueTable.isRowSelected(i) && key);
        }
        if(queueTable.selectedRow() != -1) {
            Queue q = (Queue) queueModel.get(queueTable.selectedRow());
            if(q.numberOfRoots() == 1) {
                urlField.setAttributedStringValue(new NSAttributedString(q.getRoot().getHost().getURL()
                        + q.getRoot().getAbsolute(),
                        TRUNCATE_MIDDLE_PARAGRAPH_DICTIONARY));
                localField.setAttributedStringValue(new NSAttributedString(q.getRoot().getLocal().getAbsolute(),
                        TRUNCATE_MIDDLE_PARAGRAPH_DICTIONARY));
            }
            else {
                urlField.setAttributedStringValue(new NSAttributedString(q.getRoot().getHost().getURL(),
                        TRUNCATE_MIDDLE_PARAGRAPH_DICTIONARY));
                localField.setAttributedStringValue(new NSAttributedString(NSBundle.localizedString("Multiple files", ""),
                        TRUNCATE_MIDDLE_PARAGRAPH_DICTIONARY));
            }
        }
        else {
            urlField.setStringValue("");
            localField.setStringValue("");
        }
        toolbar.validateVisibleItems();
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
            this.updateTableViewSelection();
        }
    }

    /**
     * Remove this item form the list
     *
     * @param queue
     */
    public void removeItem(Queue queue) {
        synchronized(CDQueueController.instance()) {
            this.queueModel.remove(queue);
            this.reloadQueueTable();
        }
    }

    /**
     * Add this item to the list; select it and scroll the view to make it visible
     *
     * @param queue
     */
    public void addItem(Queue queue) {
        synchronized(CDQueueController.instance()) {
            int row = this.queueModel.size();
            this.queueModel.add(row, queue);
            this.reloadQueueTable();
            this.queueTable.selectRow(row, false);
            this.queueTable.scrollRowToVisible(row);
        }
    }

    /**
     * @param queue
     */
    public void startItem(Queue queue) {
        this.addItem(queue);
        this.startItem(queue, false);
    }

    /**
     * @param queue
     * @param resumeRequested
     */
    private void startItem(final Queue queue, final boolean resumeRequested) {
        queue.addListener(new QueueListener() {
            private TranscriptListener transcript;

            public void transferStarted(final Path path) {
                queueTable.setNeedsDisplay();
            }

            public void transferStopped(final Path path) {
                queueTable.setNeedsDisplay();
            }

            public void queueStarted() {
                toolbar.validateVisibleItems();
                queue.getSession().addTranscriptListener(transcript = new TranscriptListener() {
                    public void log(String message) {
                        synchronized(lock) {
                            logView.textStorage().beginEditing();
                            logView.textStorage().appendAttributedString(
                                    new NSAttributedString(message + "\n", FIXED_WITH_FONT_ATTRIBUTES));
                            logView.textStorage().endEditing();
                        }
                    }
                });
                if(queue.getSession() instanceof ch.cyberduck.core.sftp.SFTPSession) {
                    ((ch.cyberduck.core.sftp.SFTPSession) queue.getSession()).setHostKeyVerificationController(
                            new CDHostKeyController(CDQueueController.instance()));
                }
                if(queue.getSession() instanceof ch.cyberduck.core.ftps.FTPSSession) {
                    ((ch.cyberduck.core.ftps.FTPSSession) queue.getSession()).setTrustManager(
                            new CDX509TrustManagerController(CDQueueController.instance()));
                }
                queue.getSession().setLoginController(new CDLoginController(CDQueueController.instance()));
            }

            public void queueStopped() {
                toolbar.validateVisibleItems();
                if(queue.isComplete()) {
                    if(Preferences.instance().getBoolean("queue.orderBackOnStop")) {
                        if(!hasRunningTransfers()) {
                            CDQueueController.this.invoke(new Runnable() {
                                public void run() {
                                    window().close();
                                }
                            });
                        }
                    }
                }
                queue.getSession().removeTranscriptListener(transcript);
                queue.removeListener(this);
                if(queue.isComplete() && !queue.isCanceled()) {
                    if(queue instanceof DownloadQueue) {
                        if(Preferences.instance().getBoolean("queue.postProcessItemWhenComplete")) {
                            boolean success = NSWorkspace.sharedWorkspace().openFile(queue.getRoot().getLocal().toString());
                            log.info("Success opening file:" + success);
                        }
                    }
                    if(Preferences.instance().getBoolean("queue.removeItemWhenComplete")) {
                        removeItem(queue);
                    }
                }
                if(queue.getSession() instanceof ch.cyberduck.core.sftp.SFTPSession) {
                    ((ch.cyberduck.core.sftp.SFTPSession) queue.getSession()).setHostKeyVerificationController(null);
                }
                if(queue.getSession() instanceof ch.cyberduck.core.ftps.FTPSSession) {
                    ((ch.cyberduck.core.ftps.FTPSSession) queue.getSession()).setTrustManager(null);
                }
                queue.getSession().setLoginController(null);
            }
        });
        if(Preferences.instance().getBoolean("queue.orderFrontOnStart")) {
            this.window.makeKeyAndOrderFront(null);
        }
        queue.run(resumeRequested);
    }

    private static final String TOOLBAR_RESUME = "Resume";
    private static final String TOOLBAR_RELOAD = "Reload";
    private static final String TOOLBAR_STOP = "Stop";
    private static final String TOOLBAR_REMOVE = "Remove";
    private static final String TOOLBAR_CLEAN_UP = "Clean Up";
    private static final String TOOLBAR_OPEN = "Open";
    private static final String TOOLBAR_SHOW = "Show";

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
                    this.queueModel.add(QueueFactory.createQueue(dict));
                }
                pboard.setPropertyListForType(null, "QueuePBoardType");
                this.reloadQueueTable();
            }
        }
    }

    public void stopButtonClicked(final Object sender) {
        synchronized(sender) {
            NSEnumerator iterator = queueTable.selectedRowEnumerator();
            while(iterator.hasMoreElements()) {
                Queue queue = (Queue) this.queueModel.get(((Integer) iterator.nextElement()).intValue());
                if(queue.isRunning()) {
                    queue.cancel();
                }
            }
        }
    }

    public void stopAllButtonClicked(final Object sender) {
        synchronized(sender) {
            for(int i = 0; i < this.queueModel.size(); i++) {
                Queue queue = (Queue) this.queueModel.get(i);
                if(queue.isRunning()) {
                    queue.cancel();
                }
            }
        }
    }

    public void resumeButtonClicked(final Object sender) {
        synchronized(sender) {
            NSEnumerator iterator = queueTable.selectedRowEnumerator();
            while(iterator.hasMoreElements()) {
                int i = ((Integer) iterator.nextElement()).intValue();
                Queue queue = (Queue) this.queueModel.get(i);
                if(!queue.isRunning()) {
                    this.startItem(queue, true);
                }
            }
        }
    }

    public void reloadButtonClicked(final Object sender) {
        synchronized(sender) {
            NSEnumerator iterator = queueTable.selectedRowEnumerator();
            while(iterator.hasMoreElements()) {
                int i = ((Integer) iterator.nextElement()).intValue();
                Queue queue = (Queue) this.queueModel.get(i);
                if(!queue.isRunning()) {
                    this.startItem(queue, false);
                }
            }
        }
    }

    public void openButtonClicked(final Object sender) {
        if(this.queueTable.selectedRow() != -1) {
            Queue q = (Queue) this.queueModel.get(this.queueTable.selectedRow());
            String file = q.getRoot().getLocal().toString();
            if(!NSWorkspace.sharedWorkspace().openFile(file)) {
                if(q.isComplete()) {
                    this.alert(NSAlertPanel.criticalAlertPanel(NSBundle.localizedString("Could not open the file", ""), //title
                            NSBundle.localizedString("Could not open the file", "") + " \""
                                    + file
                                    + "\". " + NSBundle.localizedString("It moved since you downloaded it.", ""), // message
                            NSBundle.localizedString("OK", ""), // defaultbutton
                            null, //alternative button
                            null //other button
                    ));
                }
                else {
                    this.alert(NSAlertPanel.criticalAlertPanel(NSBundle.localizedString("Could not open the file", ""), //title
                            NSBundle.localizedString("Could not open the file", "") + " \""
                                    + file
                                    + "\". " + NSBundle.localizedString("The file has not yet been downloaded.", ""), // message
                            NSBundle.localizedString("OK", ""), // defaultbutton
                            null, //alternative button
                            null //other button
                    ));
                }
            }
        }
    }

    public void revealButtonClicked(final Object sender) {
        if(this.queueTable.selectedRow() != -1) {
            Queue q = (Queue) this.queueModel.get(this.queueTable.selectedRow());
            String file = q.getRoot().getLocal().toString();
            if(!NSWorkspace.sharedWorkspace().selectFile(file, "")) {
                if(q.isComplete()) {
                    this.alert(NSAlertPanel.criticalAlertPanel(NSBundle.localizedString("Could not show the file in the Finder", ""), //title
                            NSBundle.localizedString("Could not show the file", "") + " \""
                                    + file
                                    + "\". " + NSBundle.localizedString("It moved since you downloaded it.", ""), // message
                            NSBundle.localizedString("OK", ""), // defaultbutton
                            null, //alternative button
                            null //other button
                    ));
                }
                else {
                    this.alert(NSAlertPanel.criticalAlertPanel(NSBundle.localizedString("Could not show the file in the Finder", ""), //title
                            NSBundle.localizedString("Could not show the file", "") + " \""
                                    + file
                                    + "\". " + NSBundle.localizedString("The file has not yet been downloaded.", ""), // message
                            NSBundle.localizedString("OK", ""), // defaultbutton
                            null, //alternative button
                            null //other button
                    ));
                }
            }
        }
    }

    public void deleteButtonClicked(final Object sender) {
        NSEnumerator iterator = queueTable.selectedRowEnumerator();
        int j = 0;
        while(iterator.hasMoreElements()) {
            int i = ((Integer) iterator.nextElement()).intValue();
            Queue q = (Queue) this.queueModel.get(i - j);
            if(!q.isRunning()) {
                this.queueModel.remove(i - j);
                j++;
            }
        }
        this.reloadQueueTable();
    }

    public void clearButtonClicked(final Object sender) {
        for(int i = 0; i < this.queueModel.size(); i++) {
            CDProgressController c = this.queueModel.getController(i);
            c.clear();
            if(!c.getQueue().isRunning() && c.getQueue().isComplete()) {
                this.queueModel.remove(i);
                i--;
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
                        Queue q = QueueFactory.createQueue(dict);
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
            if(this.queueTable.numberOfSelectedRows() < 1) {
                return false;
            }
            NSEnumerator iterator = queueTable.selectedRowEnumerator();
            while(iterator.hasMoreElements()) {
                Queue queue = (Queue) this.queueModel.get(((Integer) iterator.nextElement()).intValue());
                if(null == queue) {
                    return false;
                }
                if(!queue.isRunning()) {
                    return false;
                }
            }
            return true;
        }
        if(identifier.equals("resumeButtonClicked:")) {
            if(this.queueTable.numberOfSelectedRows() > 0) {
                Queue queue = (Queue) this.queueModel.get(this.queueTable.selectedRow());
                if(null == queue) {
                    return false;
                }
                return !queue.isRunning() && !queue.isComplete();
            }
            return false;
        }
        if(identifier.equals("reloadButtonClicked:")) {
            if(this.queueTable.numberOfSelectedRows() > 0) {
                Queue queue = (Queue) this.queueModel.get(this.queueTable.selectedRow());
                if(null == queue) {
                    return false;
                }
                return !queue.isRunning();
            }
            return false;
        }
        if(identifier.equals("openButtonClicked:")
                || identifier.equals(TOOLBAR_SHOW) || identifier.equals("revealButtonClicked:")) {
            if(this.queueTable.numberOfSelectedRows() == 1) {
                Queue queue = (Queue) this.queueModel.get(this.queueTable.selectedRow());
                if(null == queue) {
                    return false;
                }
                return queue.getRoot().getLocal().exists();
            }
            return false;
        }
        if(identifier.equals("clearButtonClicked:")) {
            return this.queueTable.numberOfRows() > 0;
        }
        if(identifier.equals("deleteButtonClicked:")) {
            if(this.queueTable.numberOfSelectedRows() < 1) {
                return false;
            }
            NSEnumerator iterator = queueTable.selectedRowEnumerator();
            while(iterator.hasMoreElements()) {
                Queue queue = (Queue) this.queueModel.get(((Integer) iterator.nextElement()).intValue());
                if(null == queue) {
                    return false;
                }
                if(queue.isRunning()) {
                    return false;
                }
            }
            return true;
        }
        return true;
    }
}