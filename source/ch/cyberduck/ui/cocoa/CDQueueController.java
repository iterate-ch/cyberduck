package ch.cyberduck.ui.cocoa;

/*
 *  Copyright (c) 2004 David Kocher. All rights reserved.
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
import com.apple.cocoa.foundation.*;

import org.apache.log4j.Logger;

import ch.cyberduck.core.*;
import ch.cyberduck.ui.cocoa.growl.Growl;

/**
 * @version $Id$
 */
public class CDQueueController extends CDController {
	private static Logger log = Logger.getLogger(CDQueueController.class);

	private static CDQueueController instance;

	private static NSMutableArray instances = new NSMutableArray();

	private NSToolbar toolbar;

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
		if(null == instance) {
			instance = new CDQueueController();
			if(false == NSApplication.loadNibNamed("Queue", instance)) {
				log.fatal("Couldn't load Queue.nib");
			}
		}
		return instance;
	}

	public boolean hasRunningTransfers() {
		for(int i = 0; i < this.queueModel.size(); i++) {
			Queue q = this.queueModel.getItem(i);
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
		sheet.orderOut(null);
		if(returncode == NSAlertPanel.DefaultReturn) { //Quit
			this.stopAllButtonClicked(null);
			NSApplication.sharedApplication().replyToApplicationShouldTerminate(true);
		}
		if(returncode == NSAlertPanel.AlternateReturn) { //Cancel
			NSApplication.sharedApplication().replyToApplicationShouldTerminate(false);
		}
	}

	public void windowDidBecomeKey(NSNotification notification) {
		this.tableViewSelectionChange();
	}

	public void windowDidResignKey(NSNotification notification) {
		this.tableViewSelectionChange();
	}

	public void windowWillClose(NSNotification notification) {
		this.queueModel.save();
	}

	private CDQueueTableDataSource queueModel;
	private NSTableView queueTable; // IBOutlet

	private static NSMutableParagraphStyle lineBreakByTruncatingMiddleParagraph = new NSMutableParagraphStyle();

	static {
		lineBreakByTruncatingMiddleParagraph.setLineBreakMode(NSParagraphStyle.LineBreakByTruncatingMiddle);
	}

	private static final NSDictionary TRUNCATE_MIDDLE_PARAGRAPH_DICTIONARY = new NSDictionary(new Object[]{lineBreakByTruncatingMiddleParagraph},
	    new Object[]{NSAttributedString.ParagraphStyleAttributeName});

	public void setQueueTable(NSTableView queueTable) {
		this.queueTable = queueTable;
		this.queueTable.setTarget(this);
		this.queueTable.setDoubleAction(new NSSelector("queueTableRowDoubleClicked", new Class[]{Object.class}));
		this.queueTable.setDataSource(this.queueModel = CDQueueTableDataSource.instance());
		this.queueTable.setDelegate(this);
		// receive drag events from types
		// in fact we are not interested in file promises, but because the browser model can only initiate
		// a drag with tableView.dragPromisedFilesOfTypes(), we listens for those events
		// and then use the private pasteboard instead.
		this.queueTable.registerForDraggedTypes(new NSArray(new Object[]{"QueuePBoardType",
		                                                                 NSPasteboard.StringPboardType,
		                                                                 NSPasteboard.FilesPromisePboardType}));
		this.queueTable.setRowHeight(50f);
		{
			NSTableColumn c = new NSTableColumn();
			c.setIdentifier("ICON");
			c.setMinWidth(36f);
			c.setWidth(36f);
			c.setMaxWidth(36f);
			c.setEditable(false);
			c.setResizable(true);
			c.setDataCell(new CDIconCell());
			this.queueTable.addTableColumn(c);
		}
		
		{
			NSTableColumn c = new NSTableColumn();
			c.setIdentifier("PROGRESS");
			c.setMinWidth(80f);
			c.setWidth(300f);
			c.setMaxWidth(1000f);
			c.setEditable(false);
			c.setResizable(true);
			c.setDataCell(new CDProgressCell());
			this.queueTable.addTableColumn(c);
		}
		NSSelector setUsesAlternatingRowBackgroundColorsSelector =
		    new NSSelector("setUsesAlternatingRowBackgroundColors", new Class[]{boolean.class});
		if(setUsesAlternatingRowBackgroundColorsSelector.implementedByClass(NSTableView.class)) {
			this.queueTable.setUsesAlternatingRowBackgroundColors(true);
		}
		NSSelector setGridStyleMaskSelector =
		    new NSSelector("setGridStyleMask", new Class[]{int.class});
		if(setGridStyleMaskSelector.implementedByClass(NSTableView.class)) {
			this.queueTable.setGridStyleMask(NSTableView.SolidHorizontalGridLineMask);
		}

		//selection properties
		this.queueTable.setAllowsMultipleSelection(true);
		this.queueTable.setAllowsEmptySelection(true);
		this.queueTable.setAllowsColumnReordering(false);
		this.queueTable.sizeToFit();
	}

	private void tableViewSelectionChange() {
		boolean key = this.window().isKeyWindow();
		for(int i = 0; i < this.queueModel.size(); i++) {
			this.queueModel.getController(i).setHighlighted(this.queueTable.isRowSelected(i) && key);
		}
		this.toolbar.validateVisibleItems();
		if(this.queueTable.selectedRow() != -1) {
			Queue q = this.queueModel.getItem(this.queueTable.selectedRow());
			if(q.numberOfRoots() == 1) {
				this.urlField.setAttributedStringValue(new NSAttributedString(q.getRoot().getHost().getURL()+"/"+q.getRoot().getAbsolute(),
																			  TRUNCATE_MIDDLE_PARAGRAPH_DICTIONARY));
				this.localField.setAttributedStringValue(new NSAttributedString(q.getRoot().getLocal().getAbsolute(),
																				TRUNCATE_MIDDLE_PARAGRAPH_DICTIONARY));
			}
			else {
				this.urlField.setAttributedStringValue(new NSAttributedString(q.getRoot().getHost().getURL(),
																			  TRUNCATE_MIDDLE_PARAGRAPH_DICTIONARY));
				this.localField.setAttributedStringValue(new NSAttributedString(NSBundle.localizedString("Multiple files", ""),
																				//				    +" ("+q.numberOfJobs()+" "+NSBundle.localizedString("files", "")+")",
																				TRUNCATE_MIDDLE_PARAGRAPH_DICTIONARY));
			}
		}
		else {
			this.urlField.setStringValue("");
			this.localField.setStringValue("");
		}
	}

	public void tableViewSelectionIsChanging(NSNotification notification) {
		this.tableViewSelectionChange();
	}

	public void tableViewSelectionDidChange(NSNotification notification) {
		this.tableViewSelectionChange();
	}

	private void reloadQueueTable() {
		this.queueTable.deselectAll(null);
		while(this.queueTable.subviews().count() > 0) {
			((NSView)this.queueTable.subviews().lastObject()).removeFromSuperviewWithoutNeedingDisplay();
		}
		this.queueTable.reloadData();
		this.tableViewSelectionChange();
	}
	
	public void removeItem(Queue queue) {
		this.queueModel.removeItem(queue);
		this.reloadQueueTable();
	}

	public void addItem(Queue queue) {
		this.queueModel.addItem(queue);
		this.reloadQueueTable();
		this.queueTable.selectRow(this.queueModel.size()-1, false);
		this.queueTable.scrollRowToVisible(this.queueModel.size()-1);
	}

	public void startItem(Queue queue) {
		this.addItem(queue);
		this.startItem(queue, false);
	}
	
	private void startItem(Queue queue, boolean resumeRequested) {
		queue.addObserver(new java.util.Observer() {
			public void update(final java.util.Observable o, final Object arg) {
				Message msg = (Message)arg;
				if(msg.getTitle().equals(Message.QUEUE_START)) {
					toolbar.validateVisibleItems();
				}
				if(msg.getTitle().equals(Message.QUEUE_STOP)) {
					toolbar.validateVisibleItems();
					o.deleteObserver(this);
				}
			}
		});
		if(Preferences.instance().getProperty("queue.orderFrontOnTransfer").equals("true")) {
			this.window().makeKeyAndOrderFront(null);
		}
		queue.getRoot().getHost().getCredentials().setController(new CDLoginController(this));
		if(queue.getRoot().getHost().getProtocol().equals(Session.SFTP)) {
			try {
				queue.getRoot().getHost().setHostKeyVerificationController(new CDHostKeyController(this));
			}
			catch(com.sshtools.j2ssh.transport.InvalidHostFileException e) {
				//This exception is thrown whenever an exception occurs open or reading from the host file.
				this.beginSheet(NSAlertPanel.criticalAlertPanel(NSBundle.localizedString("Error", ""), //title
				    NSBundle.localizedString("Could not open or read the host file", "")+": "+e.getMessage(), // message
				    NSBundle.localizedString("OK", ""), // defaultbutton
				    null, //alternative button
				    null //other button
				));
				queue.getRoot().getSession().close();
				return;
			}
		}
		queue.start(resumeRequested);
	}

	public boolean isVisible() {
		return this.window() != null && this.window().isVisible();
	}

	public void awakeFromNib() {
		this.toolbar = new NSToolbar("Queue Toolbar");
		this.toolbar.setDelegate(this);
		this.toolbar.setAllowsUserCustomization(true);
		this.toolbar.setAutosavesConfiguration(true);
		this.window().setReleasedWhenClosed(false);
		this.window().setToolbar(toolbar);
	}

	// ----------------------------------------------------------
	// Toolbar Delegate
	// ----------------------------------------------------------

	public NSToolbarItem toolbarItemForItemIdentifier(NSToolbar toolbar, String itemIdentifier, boolean flag) {
		NSToolbarItem item = new NSToolbarItem(itemIdentifier);
		if(itemIdentifier.equals("Stop")) {
			item.setLabel(NSBundle.localizedString("Stop", ""));
			item.setPaletteLabel(NSBundle.localizedString("Stop", ""));
			item.setToolTip(NSBundle.localizedString("Stop", ""));
			item.setImage(NSImage.imageNamed("stop.tiff"));
			item.setTarget(this);
			item.setAction(new NSSelector("stopButtonClicked", new Class[]{Object.class}));
			return item;
		}
		if(itemIdentifier.equals("Resume")) {
			item.setLabel(NSBundle.localizedString("Resume", ""));
			item.setPaletteLabel(NSBundle.localizedString("Resume", ""));
			item.setToolTip(NSBundle.localizedString("Resume", ""));
			item.setImage(NSImage.imageNamed("resume.tiff"));
			item.setTarget(this);
			item.setAction(new NSSelector("resumeButtonClicked", new Class[]{Object.class}));
			return item;
		}
		if(itemIdentifier.equals("Reload")) {
			item.setLabel(NSBundle.localizedString("Reload", ""));
			item.setPaletteLabel(NSBundle.localizedString("Reload", ""));
			item.setToolTip(NSBundle.localizedString("Reload", ""));
			item.setImage(NSImage.imageNamed("reload.tiff"));
			item.setTarget(this);
			item.setAction(new NSSelector("reloadButtonClicked", new Class[]{Object.class}));
			return item;
		}
		if(itemIdentifier.equals("Show")) {
			item.setLabel(NSBundle.localizedString("Show", ""));
			item.setPaletteLabel(NSBundle.localizedString("Show in Finder", ""));
			item.setToolTip(NSBundle.localizedString("Show in Finder", ""));
			item.setImage(NSImage.imageNamed("reveal.tiff"));
			item.setTarget(this);
			item.setAction(new NSSelector("revealButtonClicked", new Class[]{Object.class}));
			return item;
		}
		if(itemIdentifier.equals("Open")) {
			item.setLabel(NSBundle.localizedString("Open", ""));
			item.setPaletteLabel(NSBundle.localizedString("Open", ""));
			item.setToolTip(NSBundle.localizedString("Open", ""));
			item.setImage(NSImage.imageNamed("open.tiff"));
			item.setTarget(this);
			item.setAction(new NSSelector("openButtonClicked", new Class[]{Object.class}));
			return item;
		}
		if(itemIdentifier.equals("Remove")) {
			item.setLabel(NSBundle.localizedString("Remove", ""));
			item.setPaletteLabel(NSBundle.localizedString("Remove", ""));
			item.setToolTip(NSBundle.localizedString("Remove", ""));
			item.setImage(NSImage.imageNamed("clean.tiff"));
			item.setTarget(this);
			item.setAction(new NSSelector("deleteButtonClicked", new Class[]{Object.class}));
			return item;
		}
		if(itemIdentifier.equals("Clear")) {
			item.setLabel(NSBundle.localizedString("Clear", ""));
			item.setPaletteLabel(NSBundle.localizedString("Clear", ""));
			item.setToolTip(NSBundle.localizedString("Clear", ""));
			item.setImage(NSImage.imageNamed("cleanAll.tiff"));
			item.setTarget(this);
			item.setAction(new NSSelector("clearButtonClicked", new Class[]{Object.class}));
			return item;
		}
		// itemIdent refered to a toolbar item that is not provide or supported by us or cocoa.
		// Returning null will inform the toolbar this kind of item is not supported.
		return null;
	}

	public void queueTableRowDoubleClicked(Object sender) {
		if(this.queueTable.selectedRow() != -1) {
			Queue item = this.queueModel.getItem(this.queueTable.selectedRow());
			this.reloadButtonClicked(sender);
		}
	}

	public void paste(Object sender) {
		log.debug("paste");
		NSPasteboard pboard = NSPasteboard.pasteboardWithName("QueuePBoard");
		if(pboard.availableTypeFromArray(new NSArray("QueuePBoardType")) != null) {
			Object o = pboard.propertyListForType("QueuePBoardType");// get the data from paste board
			if(o != null) {
				NSArray elements = (NSArray)o;
				for(int i = 0; i < elements.count(); i++) {
					NSDictionary dict = (NSDictionary)elements.objectAtIndex(i);
					this.queueModel.addItem(Queue.createQueue(dict));
				}
				pboard.setPropertyListForType(null, "QueuePBoardType");
				this.reloadQueueTable();
			}
		}
	}

	public void stopButtonClicked(Object sender) {
		NSEnumerator enum = queueTable.selectedRowEnumerator();
		while(enum.hasMoreElements()) {
			Queue queue = this.queueModel.getItem(((Integer)enum.nextElement()).intValue());
			if(queue.isRunning()) {
				queue.cancel();
			}
		}
	}

	public void stopAllButtonClicked(Object sender) {
		for(int i = 0; i < this.queueModel.size(); i++) {
			Queue queue = this.queueModel.getItem(i);
			if(queue.isRunning()) {
				queue.cancel();
			}
		}
	}

	public void resumeButtonClicked(Object sender) {
		NSEnumerator enum = queueTable.selectedRowEnumerator();
		while(enum.hasMoreElements()) {
			Queue queue = this.queueModel.getItem(((Integer)enum.nextElement()).intValue());
			if(!queue.isRunning()) {
				this.startItem(queue, true);
			}
		}
	}

	public void reloadButtonClicked(Object sender) {
		NSEnumerator enum = queueTable.selectedRowEnumerator();
		while(enum.hasMoreElements()) {
			Queue queue = this.queueModel.getItem(((Integer)enum.nextElement()).intValue());
			if(!queue.isRunning()) {
				this.startItem(queue, false);
			}
		}
	}

	public synchronized void openButtonClicked(Object sender) {
		if(this.queueTable.selectedRow() != -1) {
			Queue item = this.queueModel.getItem(this.queueTable.selectedRow());
			Path f = item.getRoot();
			String file = item.getRoot().getLocal().toString();
			if(!NSWorkspace.sharedWorkspace().openFile(file)) {
				if(item.isComplete()) {
					this.beginSheet(NSAlertPanel.criticalAlertPanel(NSBundle.localizedString("Could not open the file", ""), //title
					    NSBundle.localizedString("Could not open the file", "")+" \""
					    +file
					    +"\". "+NSBundle.localizedString("It moved since you downloaded it.", ""), // message
					    NSBundle.localizedString("OK", ""), // defaultbutton
					    null, //alternative button
					    null //other button
					));
				}
				else {
					this.beginSheet(NSAlertPanel.criticalAlertPanel(NSBundle.localizedString("Could not open the file", ""), //title
					    NSBundle.localizedString("Could not open the file", "")+" \""
					    +file
					    +"\". "+NSBundle.localizedString("The file has not yet been downloaded.", ""), // message
					    NSBundle.localizedString("OK", ""), // defaultbutton
					    null, //alternative button
					    null //other button
					));
				}
			}
		}
	}

	public synchronized void revealButtonClicked(Object sender) {
		if(this.queueTable.selectedRow() != -1) {
			Queue item = this.queueModel.getItem(this.queueTable.selectedRow());
			Path f = item.getRoot();
			String file = item.getRoot().getLocal().toString();
			if(!NSWorkspace.sharedWorkspace().selectFile(file, "")) {
				if(item.isComplete()) {
					this.beginSheet(NSAlertPanel.criticalAlertPanel(NSBundle.localizedString("Could not show the file in the Finder", ""), //title
					    NSBundle.localizedString("Could not show the file", "")+" \""
					    +file
					    +"\". "+NSBundle.localizedString("It moved since you downloaded it.", ""), // message
					    NSBundle.localizedString("OK", ""), // defaultbutton
					    null, //alternative button
					    null //other button
					));
				}
				else {
					this.beginSheet(NSAlertPanel.criticalAlertPanel(NSBundle.localizedString("Could not show the file in the Finder", ""), //title
					    NSBundle.localizedString("Could not show the file", "")+" \""
					    +file
					    +"\". "+NSBundle.localizedString("The file has not yet been downloaded.", ""), // message
					    NSBundle.localizedString("OK", ""), // defaultbutton
					    null, //alternative button
					    null //other button
					));
				}
			}
		}
	}

	public void deleteButtonClicked(Object sender) {
		NSEnumerator enum = queueTable.selectedRowEnumerator();
		int j = 0;
		while(enum.hasMoreElements()) {
			int i = ((Integer)enum.nextElement()).intValue();
			Queue queue = this.queueModel.getItem(i-j);
			if(!queue.isRunning()) {
				this.queueModel.removeItem(i-j);
				j++;
			}
		}
		this.reloadQueueTable();
	}

	public void clearButtonClicked(Object sender) {
		for(int i = 0; i < this.queueModel.size(); i++) {
			Queue q = this.queueModel.getItem(i);
			if(q.getSize() == q.getCurrent() && q.getSize() > 0) {
				this.queueModel.removeItem(i);
				i--;
			}
		}
		this.reloadQueueTable();
	}

	public NSArray toolbarDefaultItemIdentifiers(NSToolbar toolbar) {
		return new NSArray(new Object[]{
			"Resume",
			"Reload",
			"Stop",
			"Remove",
			"Clear",
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
			"Clear",
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
		String identifier = item.itemIdentifier();
		return this.validateItem(identifier);
	}

	private boolean validateItem(String identifier) {
		if(identifier.equals("paste:")) {
			NSPasteboard pboard = NSPasteboard.pasteboardWithName("QueuePBoard");
			return pboard.availableTypeFromArray(new NSArray("QueuePBoardType")) != null
			    && pboard.propertyListForType("QueuePBoardType") != null;
		}
		if(identifier.equals("Stop") || identifier.equals("stopButtonClicked:")) {
			if(this.queueTable.numberOfSelectedRows() < 1) {
				return false;
			}
			NSEnumerator enum = queueTable.selectedRowEnumerator();
			while(enum.hasMoreElements()) {
				Queue queue = this.queueModel.getItem(((Integer)enum.nextElement()).intValue());
				if(!queue.isRunning()) {
					return false;
				}
			}
			return true;
		}
		if(identifier.equals("Resume") || identifier.equals("resumeButtonClicked:")) {
			if(this.queueTable.numberOfSelectedRows() > 0) {
				Queue queue = this.queueModel.getItem(this.queueTable.selectedRow());
				return !queue.isRunning() && !queue.isComplete();
			}
			return false;
		}
		if(identifier.equals("Reload") || identifier.equals("reloadButtonClicked:")) {
			if(this.queueTable.numberOfSelectedRows() > 0) {
				Queue queue = this.queueModel.getItem(this.queueTable.selectedRow());
				return !queue.isRunning();
			}
			return false;
		}
		if(identifier.equals("Show") || identifier.equals("revealButtonClicked:")) {
			return this.queueTable.numberOfSelectedRows() == 1;
		}
		if(identifier.equals("Open") || identifier.equals("openButtonClicked:")) {
			return this.queueTable.numberOfSelectedRows() == 1;
		}
		if(identifier.equals("Clear")) {
			return this.queueTable.numberOfRows() > 0;
		}
		if(identifier.equals("Remove") || identifier.equals("deleteButtonClicked:")) {
			if(this.queueTable.numberOfSelectedRows() < 1) {
				return false;
			}
			NSEnumerator enum = queueTable.selectedRowEnumerator();
			while(enum.hasMoreElements()) {
				Queue queue = this.queueModel.getItem(((Integer)enum.nextElement()).intValue());
				if(queue.isRunning()) {
					return false;
				}
			}
			return true;
		}
		return true;
	}
}