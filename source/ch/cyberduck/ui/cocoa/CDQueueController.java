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

import ch.cyberduck.core.Message;
import ch.cyberduck.core.Path;
import ch.cyberduck.core.Preferences;
import ch.cyberduck.core.Queue;

import com.apple.cocoa.application.NSAlertPanel;
import com.apple.cocoa.application.NSApplication;
import com.apple.cocoa.application.NSCell;
import com.apple.cocoa.application.NSImage;
import com.apple.cocoa.application.NSMenuItem;
import com.apple.cocoa.application.NSPasteboard;
import com.apple.cocoa.application.NSTableColumn;
import com.apple.cocoa.application.NSTableView;
import com.apple.cocoa.application.NSTextField;
import com.apple.cocoa.application.NSToolbar;
import com.apple.cocoa.application.NSToolbarItem;
import com.apple.cocoa.application.NSView;
import com.apple.cocoa.application.NSWindow;
import com.apple.cocoa.application.NSWorkspace;
import com.apple.cocoa.foundation.NSArray;
import com.apple.cocoa.foundation.NSAttributedString;
import com.apple.cocoa.foundation.NSBundle;
import com.apple.cocoa.foundation.NSDictionary;
import com.apple.cocoa.foundation.NSEnumerator;
import com.apple.cocoa.foundation.NSMutableArray;
import com.apple.cocoa.foundation.NSMutableRect;
import com.apple.cocoa.foundation.NSNotification;
import com.apple.cocoa.foundation.NSPoint;
import com.apple.cocoa.foundation.NSSelector;

import org.apache.log4j.Logger;

import java.util.Observable;
import java.util.Observer;

/**
 * @version $Id$
 */
public class CDQueueController extends CDWindowController {
    private static Logger log = Logger.getLogger(CDQueueController.class);

    private static CDQueueController instance;

    private static NSMutableArray instances = new NSMutableArray();

    private NSToolbar toolbar;

    public void awakeFromNib() {
        super.awakeFromNib();

        this.toolbar = new NSToolbar("Queue Toolbar");
        this.toolbar.setDelegate(this);
        this.toolbar.setAllowsUserCustomization(true);
        this.toolbar.setAutosavesConfiguration(true);
        this.window().setDelegate(this);
        this.window().setReleasedWhenClosed(false);
        this.window().setToolbar(toolbar);
    }

    // ----------------------------------------------------------
    // Outlets
    // ----------------------------------------------------------

    private NSTextField urlField;

    public void setUrlField(NSTextField urlField) {
        this.urlField = urlField;
    }

    private NSTextField localField;

    public void setLocalField(NSTextField localField) {
        this.localField = localField;
    }

    private CDQueueController() {
        instances.addObject(this);
    }

    public static CDQueueController instance() {
        if (null == instance) {
            instance = new CDQueueController();
            if (!NSApplication.loadNibNamed("Queue", instance)) {
                log.fatal("Couldn't load Queue.nib");
            }
        }
        return instance;
    }

    public boolean hasRunningTransfers() {
        for (int i = 0; i < this.queueModel.size(); i++) {
            Queue q = (Queue) this.queueModel.get(i);
            if (q.isRunning()) {
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
        if (null != instance) {
            if (instance.hasRunningTransfers()) {
                NSAlertPanel.beginCriticalAlertSheet(NSBundle.localizedString("Transfer in progress", ""), //title
                        NSBundle.localizedString("Quit", ""), // defaultbutton
                        NSBundle.localizedString("Cancel", ""), //alternative button
                        null, //other button
                        instance.window(), //window
                        instance, //delegate
                        new NSSelector("checkForRunningTransfersSheetDidEnd",
                                new Class[]{NSWindow.class, int.class, Object.class}),
                        null, // dismiss selector
                        null, // context
                        NSBundle.localizedString("There are items in the queue currently being transferred. Quit anyway?", "") // message
                );
                return NSApplication.TerminateLater; //break
            }
        }
        NSApplication.sharedApplication().replyToApplicationShouldTerminate(true);
        return NSApplication.TerminateNow;
    }

    public void checkForRunningTransfersSheetDidEnd(NSWindow sheet, int returncode, Object contextInfo) {
        if (returncode == NSAlertPanel.DefaultReturn) { //Quit
            this.stopAllButtonClicked(null);
            NSApplication.sharedApplication().replyToApplicationShouldTerminate(true);
        }
        if (returncode == NSAlertPanel.AlternateReturn) { //Cancel
            NSApplication.sharedApplication().replyToApplicationShouldTerminate(false);
        }
    }

    public void windowDidBecomeKey(NSNotification notification) {
        this.updateTableViewSelection();
    }

    public void windowDidResignKey(NSNotification notification) {
        this.updateTableViewSelection();
    }

    public void windowWillClose(NSNotification notification) {
        this.queueModel.save();
    }

    private CDQueueTableDataSource queueModel;
    private NSTableView queueTable; // IBOutlet
    private CDTableDelegate delegate;

    public void setQueueTable(NSTableView queueTable) {
        this.queueTable = queueTable;

        this.queueTable.setDataSource(this.queueModel = CDQueueTableDataSource.instance());
        this.queueTable.setDelegate(this.delegate = new CDAbstractTableDelegate() {
            public void enterKeyPressed(Object sender) {
                if (CDQueueController.this.queueTable.selectedRow() != -1) {
                    Queue item = (Queue) queueModel.get(CDQueueController.this.queueTable.selectedRow());
                    if (!item.isRunning()) {
                        reloadButtonClicked(sender);
                    }
                }
            }

            public void deleteKeyPressed(Object sender) {
                deleteButtonClicked(sender);
            }

            public void tableColumnClicked(NSTableView view, NSTableColumn tableColumn) {

            }

            public String tableViewToolTipForCell(NSTableView tableView, NSCell cell, NSMutableRect rect,
                                                  NSTableColumn tc, int row, NSPoint mouseLocation) {
                if (row < queueModel.numberOfRowsInTableView(tableView)) {
                    queueModel.get(row).toString();
                }
                return null;
            }

            public void tableViewSelectionIsChanging(NSNotification aNotification) {
                updateTableViewSelection();
            }

            public void tableViewSelectionDidChange(NSNotification aNotification) {
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

        this.queueTable.setRowHeight(50f);
        NSSelector setResizableMaskSelector
                = new NSSelector("setResizingMask", new Class[]{int.class});
        {
            NSTableColumn c = new NSTableColumn();
            c.setIdentifier("ICON");
            c.setMinWidth(36f);
            c.setWidth(36f);
            c.setMaxWidth(36f);
            if (setResizableMaskSelector.implementedByClass(NSTableColumn.class)) {
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
            c.setIdentifier("PROGRESS");
            c.setMinWidth(80f);
            c.setWidth(300f);
            c.setMaxWidth(1000f);
            if (setResizableMaskSelector.implementedByClass(NSTableColumn.class)) {
                c.setResizingMask(NSTableColumn.AutoresizingMask);
            }
            else {
                c.setResizable(true);
            }
            c.setDataCell(new CDProgressCell());
            this.queueTable.addTableColumn(c);
        }
        this.queueTable.setUsesAlternatingRowBackgroundColors(true);
        this.queueTable.setGridStyleMask(NSTableView.SolidHorizontalGridLineMask);

        //selection properties
        this.queueTable.setAllowsMultipleSelection(true);
        this.queueTable.setAllowsEmptySelection(true);
        this.queueTable.setAllowsColumnReordering(false);
        this.queueTable.sizeToFit();
    }

    private void updateTableViewSelection() {
        boolean key = window().isKeyWindow();
        for (int i = 0; i < queueModel.size(); i++) {
            queueModel.getController(i).setHighlighted(queueTable.isRowSelected(i) && key);
        }
        toolbar.validateVisibleItems();
        if (queueTable.selectedRow() != -1) {
            Queue q = (Queue) queueModel.get(queueTable.selectedRow());
            if (q.numberOfRoots() == 1) {
                urlField.setAttributedStringValue(new NSAttributedString(q.getRoot().getHost().getURL()
                        + Path.DELIMITER + q.getRoot().getAbsolute(),
                        TRUNCATE_MIDDLE_PARAGRAPH_DICTIONARY));
                localField.setAttributedStringValue(new NSAttributedString(q.getRoot().getLocal().getAbsolute(),
                        TRUNCATE_MIDDLE_PARAGRAPH_DICTIONARY));
            }
            else {
                urlField.setAttributedStringValue(new NSAttributedString(q.getRoot().getHost().getURL(),
                        TRUNCATE_MIDDLE_PARAGRAPH_DICTIONARY));
                localField.setAttributedStringValue(new NSAttributedString(NSBundle.localizedString("Multiple files", ""),
                        //				    +" ("+q.numberOfJobs()+" "+NSBundle.localizedString("files", "")+")",
                        TRUNCATE_MIDDLE_PARAGRAPH_DICTIONARY));
            }
        }
        else {
            urlField.setStringValue("");
            localField.setStringValue("");
        }
    }

    // ----------------------------------------------------------
    //
    // ----------------------------------------------------------

    private void reloadQueueTable() {
        this.queueTable.deselectAll(null);
        while (this.queueTable.subviews().count() > 0) {
            ((NSView) this.queueTable.subviews().lastObject()).removeFromSuperviewWithoutNeedingDisplay();
        }
        this.queueTable.reloadData();
        this.updateTableViewSelection();
    }

    public void removeItem(Queue queue) {
        this.queueModel.remove(queue);
        this.reloadQueueTable();
    }

    public void addItem(Queue queue) {
        int row = this.queueModel.size();
        this.queueModel.add(row, queue);
        this.queueModel.getController(row).init();
        this.reloadQueueTable();
        this.queueTable.selectRow(row, false);
        this.queueTable.scrollRowToVisible(row);
    }

    public void startItem(Queue queue) {
        this.addItem(queue);
        this.startItem(queue, false);
    }

    private void startItem(final Queue queue, final boolean resumeRequested) {
        queue.addObserver(new Observer() {
            public void update(final Observable o, final Object arg) {
                Message msg = (Message) arg;
                if (msg.getTitle().equals(Message.QUEUE_START)) {
                    CDQueueController.this.invoke(new Runnable() {
                        public void run() {
                            toolbar.validateVisibleItems();
                        }
                    });
                }
                if (msg.getTitle().equals(Message.QUEUE_STOP)) {
                    CDQueueController.this.invoke(new Runnable() {
                        public void run() {
                            toolbar.validateVisibleItems();
                            int row = queueTable.selectedRow();
                            reloadQueueTable();
                            queueTable.selectRow(row, false);
                            if (Preferences.instance().getBoolean("queue.orderBackOnTransfer")) {
                                window().close();
                            }
                        }
                    });
                    o.deleteObserver(this);
                }
            }
        });
        if (Preferences.instance().getBoolean("queue.orderFrontOnTransfer")) {
            this.window().makeKeyAndOrderFront(null);
        }
        if (queue.getSession() instanceof ch.cyberduck.core.sftp.SFTPSession) {
            ((ch.cyberduck.core.sftp.SFTPSession) queue.getSession()).setHostKeyVerificationController(new CDHostKeyController(this));
        }
        if (queue.getSession() instanceof ch.cyberduck.core.ftps.FTPSSession) {
            ((ch.cyberduck.core.ftps.FTPSSession) queue.getSession()).setTrustManager(
                    new CDX509TrustManagerController(this));
        }
        queue.getHost().setLoginController(new CDLoginController(this));
        new Thread("Session") {
            public void run() {
                queue.process(resumeRequested, false);
            }
        }.start();
    }

    public boolean isVisible() {
        return this.window() != null && this.window().isVisible();
    }

    // ----------------------------------------------------------
    // Toolbar Delegate
    // ----------------------------------------------------------

    public NSToolbarItem toolbarItemForItemIdentifier(NSToolbar toolbar, String itemIdentifier, boolean flag) {
        NSToolbarItem item = new NSToolbarItem(itemIdentifier);
        if (itemIdentifier.equals("Stop")) {
            item.setLabel(NSBundle.localizedString("Stop", ""));
            item.setPaletteLabel(NSBundle.localizedString("Stop", ""));
            item.setToolTip(NSBundle.localizedString("Stop", ""));
            item.setImage(NSImage.imageNamed("stop.tiff"));
            item.setTarget(this);
            item.setAction(new NSSelector("stopButtonClicked", new Class[]{Object.class}));
            return item;
        }
        if (itemIdentifier.equals("Resume")) {
            item.setLabel(NSBundle.localizedString("Resume", ""));
            item.setPaletteLabel(NSBundle.localizedString("Resume", ""));
            item.setToolTip(NSBundle.localizedString("Resume", ""));
            item.setImage(NSImage.imageNamed("resume.tiff"));
            item.setTarget(this);
            item.setAction(new NSSelector("resumeButtonClicked", new Class[]{Object.class}));
            return item;
        }
        if (itemIdentifier.equals("Reload")) {
            item.setLabel(NSBundle.localizedString("Reload", ""));
            item.setPaletteLabel(NSBundle.localizedString("Reload", ""));
            item.setToolTip(NSBundle.localizedString("Reload", ""));
            item.setImage(NSImage.imageNamed("reload.tiff"));
            item.setTarget(this);
            item.setAction(new NSSelector("reloadButtonClicked", new Class[]{Object.class}));
            return item;
        }
        if (itemIdentifier.equals("Show")) {
            item.setLabel(NSBundle.localizedString("Show", ""));
            item.setPaletteLabel(NSBundle.localizedString("Show in Finder", ""));
            item.setToolTip(NSBundle.localizedString("Show in Finder", ""));
            item.setImage(NSImage.imageNamed("reveal.tiff"));
            item.setTarget(this);
            item.setAction(new NSSelector("revealButtonClicked", new Class[]{Object.class}));
            return item;
        }
        if (itemIdentifier.equals("Open")) {
            item.setLabel(NSBundle.localizedString("Open", ""));
            item.setPaletteLabel(NSBundle.localizedString("Open", ""));
            item.setToolTip(NSBundle.localizedString("Open", ""));
            item.setImage(NSImage.imageNamed("open.tiff"));
            item.setTarget(this);
            item.setAction(new NSSelector("openButtonClicked", new Class[]{Object.class}));
            return item;
        }
        if (itemIdentifier.equals("Remove")) {
            item.setLabel(NSBundle.localizedString("Remove", ""));
            item.setPaletteLabel(NSBundle.localizedString("Remove", ""));
            item.setToolTip(NSBundle.localizedString("Remove", ""));
            item.setImage(NSImage.imageNamed("clean.tiff"));
            item.setTarget(this);
            item.setAction(new NSSelector("deleteButtonClicked", new Class[]{Object.class}));
            return item;
        }
        if (itemIdentifier.equals("Clean Up")) {
            item.setLabel(NSBundle.localizedString("Clean Up", ""));
            item.setPaletteLabel(NSBundle.localizedString("Clean Up", ""));
            item.setToolTip(NSBundle.localizedString("Clean Up", ""));
            item.setImage(NSImage.imageNamed("cleanAll.tiff"));
            item.setTarget(this);
            item.setAction(new NSSelector("clearButtonClicked", new Class[]{Object.class}));
            return item;
        }
        // itemIdent refered to a toolbar item that is not provide or supported by us or cocoa.
        // Returning null will inform the toolbar this kind of item is not supported.
        return null;
    }

    public void paste(Object sender) {
        log.debug("paste");
        NSPasteboard pboard = NSPasteboard.pasteboardWithName("QueuePBoard");
        if (pboard.availableTypeFromArray(new NSArray("QueuePBoardType")) != null) {
            Object o = pboard.propertyListForType("QueuePBoardType");// get the data from paste board
            if (o != null) {
                NSArray elements = (NSArray) o;
                for (int i = 0; i < elements.count(); i++) {
                    NSDictionary dict = (NSDictionary) elements.objectAtIndex(i);
                    this.queueModel.add(Queue.createQueue(dict));
                }
                pboard.setPropertyListForType(null, "QueuePBoardType");
                this.reloadQueueTable();
            }
        }
    }

    public void stopButtonClicked(Object sender) {
        NSEnumerator iterator = queueTable.selectedRowEnumerator();
        while (iterator.hasMoreElements()) {
            Queue queue = (Queue) this.queueModel.get(((Integer) iterator.nextElement()).intValue());
            if (queue.isRunning()) {
                queue.cancel();
            }
        }
    }

    public void stopAllButtonClicked(Object sender) {
        for (int i = 0; i < this.queueModel.size(); i++) {
            Queue queue = (Queue) this.queueModel.get(i);
            if (queue.isRunning()) {
                queue.cancel();
            }
        }
    }

    public void resumeButtonClicked(Object sender) {
        NSEnumerator iterator = queueTable.selectedRowEnumerator();
        while (iterator.hasMoreElements()) {
            int i = ((Integer) iterator.nextElement()).intValue();
            this.queueModel.getController(i).init();
            Queue queue = (Queue) this.queueModel.get(i);
            if (!queue.isRunning()) {
                this.startItem(queue, true);
            }
        }
    }

    public void reloadButtonClicked(Object sender) {
        NSEnumerator iterator = queueTable.selectedRowEnumerator();
        while (iterator.hasMoreElements()) {
            int i = ((Integer) iterator.nextElement()).intValue();
            this.queueModel.getController(i).init();
            Queue queue = (Queue) this.queueModel.get(i);
            if (!queue.isRunning()) {
                this.startItem(queue, false);
            }
        }
    }

    public void openButtonClicked(Object sender) {
        if (this.queueTable.selectedRow() != -1) {
            Queue q = (Queue) this.queueModel.get(this.queueTable.selectedRow());
            Path f = q.getRoot();
            String file = q.getRoot().getLocal().toString();
            if (!NSWorkspace.sharedWorkspace().openFile(file)) {
                if (q.isComplete()) {
                    this.beginSheet(NSAlertPanel.criticalAlertPanel(NSBundle.localizedString("Could not open the file", ""), //title
                            NSBundle.localizedString("Could not open the file", "") + " \""
                                    + file
                                    + "\". " + NSBundle.localizedString("It moved since you downloaded it.", ""), // message
                            NSBundle.localizedString("OK", ""), // defaultbutton
                            null, //alternative button
                            null //other button
                    ));
                }
                else {
                    this.beginSheet(NSAlertPanel.criticalAlertPanel(NSBundle.localizedString("Could not open the file", ""), //title
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

    public void revealButtonClicked(Object sender) {
        if (this.queueTable.selectedRow() != -1) {
            Queue q = (Queue) this.queueModel.get(this.queueTable.selectedRow());
            Path f = q.getRoot();
            String file = q.getRoot().getLocal().toString();
            if (!NSWorkspace.sharedWorkspace().selectFile(file, "")) {
                if (q.isComplete()) {
                    this.beginSheet(NSAlertPanel.criticalAlertPanel(NSBundle.localizedString("Could not show the file in the Finder", ""), //title
                            NSBundle.localizedString("Could not show the file", "") + " \""
                                    + file
                                    + "\". " + NSBundle.localizedString("It moved since you downloaded it.", ""), // message
                            NSBundle.localizedString("OK", ""), // defaultbutton
                            null, //alternative button
                            null //other button
                    ));
                }
                else {
                    this.beginSheet(NSAlertPanel.criticalAlertPanel(NSBundle.localizedString("Could not show the file in the Finder", ""), //title
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

    public void deleteButtonClicked(Object sender) {
        NSEnumerator iterator = queueTable.selectedRowEnumerator();
        int j = 0;
        while (iterator.hasMoreElements()) {
            int i = ((Integer) iterator.nextElement()).intValue();
            Queue q = (Queue) this.queueModel.get(i - j);
            if (!q.isRunning()) {
                this.queueModel.remove(i - j);
                j++;
            }
        }
        this.reloadQueueTable();
    }

    public void clearButtonClicked(Object sender) {
        for (int i = 0; i < this.queueModel.size(); i++) {
            Queue q = (Queue) this.queueModel.get(i);
            if (q.isComplete()) {
                this.queueModel.remove(i);
                i--;
            }
        }
        this.reloadQueueTable();
    }

    // ----------------------------------------------------------
    // Toolbar Validation
    // ----------------------------------------------------------

    public NSArray toolbarDefaultItemIdentifiers(NSToolbar toolbar) {
        return new NSArray(new Object[]{
                "Resume",
                "Reload",
                "Stop",
                "Remove",
                "Clean Up",
                NSToolbarItem.FlexibleSpaceItemIdentifier,
                "Open",
                "Show"
        });
    }

    public NSArray toolbarAllowedItemIdentifiers(NSToolbar toolbar) {
        return new NSArray(new Object[]{
                "Resume",
                "Reload",
                "Stop",
                "Remove",
                "Clean Up",
                "Show",
                "Open",
                NSToolbarItem.CustomizeToolbarItemIdentifier,
                NSToolbarItem.SpaceItemIdentifier,
                NSToolbarItem.SeparatorItemIdentifier,
                NSToolbarItem.FlexibleSpaceItemIdentifier
        });
    }

    public boolean validateMenuItem(NSMenuItem item) {
        return this.validateItem(item.action().name());
    }

    public boolean validateToolbarItem(NSToolbarItem item) {
        return this.validateItem(item.itemIdentifier());
    }

    private boolean validateItem(String identifier) {
        if (identifier.equals("paste:")) {
            NSPasteboard pboard = NSPasteboard.pasteboardWithName("QueuePBoard");
            return pboard.availableTypeFromArray(new NSArray("QueuePBoardType")) != null
                    && pboard.propertyListForType("QueuePBoardType") != null;
        }
        if (identifier.equals("Stop") || identifier.equals("stopButtonClicked:")) {
            if (this.queueTable.numberOfSelectedRows() < 1) {
                return false;
            }
            NSEnumerator iterator = queueTable.selectedRowEnumerator();
            while (iterator.hasMoreElements()) {
                Queue queue = (Queue) this.queueModel.get(((Integer) iterator.nextElement()).intValue());
                if (!queue.isRunning()) {
                    return false;
                }
            }
            return true;
        }
        if (identifier.equals("Resume") || identifier.equals("resumeButtonClicked:")) {
            if (this.queueTable.numberOfSelectedRows() > 0) {
                Queue queue = (Queue) this.queueModel.get(this.queueTable.selectedRow());
                return !queue.isRunning() && !queue.isComplete();
            }
            return false;
        }
        if (identifier.equals("Reload") || identifier.equals("reloadButtonClicked:")) {
            if (this.queueTable.numberOfSelectedRows() > 0) {
                Queue queue = (Queue) this.queueModel.get(this.queueTable.selectedRow());
                return !queue.isRunning();
            }
            return false;
        }
        if (identifier.equals("Show") || identifier.equals("revealButtonClicked:")) {
            return this.queueTable.numberOfSelectedRows() == 1;
        }
        if (identifier.equals("Open") || identifier.equals("openButtonClicked:")) {
            return this.queueTable.numberOfSelectedRows() == 1;
        }
        if (identifier.equals("Clean Up")) {
            return this.queueTable.numberOfRows() > 0;
        }
        if (identifier.equals("Remove") || identifier.equals("deleteButtonClicked:")) {
            if (this.queueTable.numberOfSelectedRows() < 1) {
                return false;
            }
            NSEnumerator iterator = queueTable.selectedRowEnumerator();
            while (iterator.hasMoreElements()) {
                Queue queue = (Queue) this.queueModel.get(((Integer) iterator.nextElement()).intValue());
                if (queue.isRunning()) {
                    return false;
                }
            }
            return true;
        }
        return true;
    }
}