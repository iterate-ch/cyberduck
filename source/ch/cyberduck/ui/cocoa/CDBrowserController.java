package ch.cyberduck.ui.cocoa;

/*
 *  Copyright (c) 2003 David Kocher. All rights reserved.
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

import java.util.*;

import org.apache.log4j.Logger;

/**
 * @version $Id$
 */
public class CDBrowserController implements Observer {
	private static Logger log = Logger.getLogger(CDBrowserController.class);

	// ----------------------------------------------------------
	// Outlets
	// ----------------------------------------------------------

	private NSWindow window; // IBOutlet

	public void setMainWindow(NSWindow window) {
		this.window = window;
		this.window.setDelegate(this);
	}

	public NSWindow window() {
		return this.window;
	}

	private CDBrowserTableDataSource browserModel;
	private NSTableView browserTable; // IBOutlet

	public void setBrowserTable(NSTableView browserTable) {
		this.browserTable = browserTable;
		this.browserTable.setTarget(this);
		this.browserTable.setDoubleAction(new NSSelector("browserTableRowDoubleClicked", new Class[]{Object.class}));
		this.browserTable.setDataSource(this.browserModel = new CDBrowserTableDataSource());
		this.browserTable.setDelegate(this.browserModel);
		// receive drag events from types
		this.browserTable.registerForDraggedTypes(new NSArray(new Object[]{
			NSPasteboard.FilenamesPboardType,
			"BookmarkPboardType"}));
		this.browserTable.setRowHeight(17f);

		// setting appearance attributes
		this.browserTable.setAutoresizesAllColumnsToFit(true);
		NSSelector setUsesAlternatingRowBackgroundColorsSelector =
		    new NSSelector("setUsesAlternatingRowBackgroundColors", new Class[]{boolean.class});
		if (setUsesAlternatingRowBackgroundColorsSelector.implementedByClass(NSTableView.class)) {
			this.browserTable.setUsesAlternatingRowBackgroundColors(CDPreferencesImpl.instance().getProperty("browser.alternatingRows").equals("true"));
		}
		NSSelector setGridStyleMaskSelector =
		    new NSSelector("setGridStyleMask", new Class[]{int.class});
		if (setGridStyleMaskSelector.implementedByClass(NSTableView.class)) {
			if (Preferences.instance().getProperty("browser.horizontalLines").equals("true") && CDPreferencesImpl.instance().getProperty("browser.verticalLines").equals("true"))
				this.browserTable.setGridStyleMask(NSTableView.SolidHorizontalGridLineMask | NSTableView.SolidVerticalGridLineMask);
			else if (Preferences.instance().getProperty("browser.verticalLines").equals("true"))
				this.browserTable.setGridStyleMask(NSTableView.SolidVerticalGridLineMask);
			else if (Preferences.instance().getProperty("browser.horizontalLines").equals("true"))
				this.browserTable.setGridStyleMask(NSTableView.SolidHorizontalGridLineMask);
			else
				this.browserTable.setGridStyleMask(NSTableView.GridNone);
		}

		// ading table columns
		if (Preferences.instance().getProperty("browser.columnIcon").equals("true")) {
			NSTableColumn c = new NSTableColumn();
			c.setIdentifier("ICON");
			c.headerCell().setStringValue("");
			c.setMinWidth(20f);
			c.setWidth(20f);
			c.setMaxWidth(20f);
			c.setResizable(true);
			c.setEditable(false);
			c.setDataCell(new NSImageCell());
			c.dataCell().setAlignment(NSText.CenterTextAlignment);
			this.browserTable.addTableColumn(c);
		}
		if (Preferences.instance().getProperty("browser.columnFilename").equals("true")) {
			NSTableColumn c = new NSTableColumn();
			c.headerCell().setStringValue(NSBundle.localizedString("Filename", "A column in the browser"));
			c.setIdentifier("FILENAME");
			c.setMinWidth(100f);
			c.setWidth(250f);
			c.setMaxWidth(1000f);
			c.setResizable(true);
			c.setEditable(false);
			c.setDataCell(new NSTextFieldCell());
			c.dataCell().setAlignment(NSText.LeftTextAlignment);
			this.browserTable.addTableColumn(c);
		}
		if (Preferences.instance().getProperty("browser.columnSize").equals("true")) {
			NSTableColumn c = new NSTableColumn();
			c.headerCell().setStringValue(NSBundle.localizedString("Size", "A column in the browser"));
			c.setIdentifier("SIZE");
			c.setMinWidth(50f);
			c.setWidth(80f);
			c.setMaxWidth(200f);
			c.setResizable(true);
			c.setDataCell(new NSTextFieldCell());
			c.dataCell().setAlignment(NSText.RightTextAlignment);
			this.browserTable.addTableColumn(c);
		}
		if (Preferences.instance().getProperty("browser.columnModification").equals("true")) {
			NSTableColumn c = new NSTableColumn();
			c.headerCell().setStringValue(NSBundle.localizedString("Modified", "A column in the browser"));
			c.setIdentifier("MODIFIED");
			c.setMinWidth(100f);
			c.setWidth(180f);
			c.setMaxWidth(500f);
			c.setResizable(true);
			c.setDataCell(new NSTextFieldCell());
			c.dataCell().setAlignment(NSText.LeftTextAlignment);
			this.browserTable.addTableColumn(c);
		}
		if (Preferences.instance().getProperty("browser.columnOwner").equals("true")) {
			NSTableColumn c = new NSTableColumn();
			c.headerCell().setStringValue(NSBundle.localizedString("Owner", "A column in the browser"));
			c.setIdentifier("OWNER");
			c.setMinWidth(100f);
			c.setWidth(80f);
			c.setMaxWidth(500f);
			c.setResizable(true);
			c.setDataCell(new NSTextFieldCell());
			c.dataCell().setAlignment(NSText.LeftTextAlignment);
			this.browserTable.addTableColumn(c);
		}
		if (Preferences.instance().getProperty("browser.columnPermissions").equals("true")) {
			NSTableColumn c = new NSTableColumn();
			c.headerCell().setStringValue(NSBundle.localizedString("Permissions", "A column in the browser"));
			c.setIdentifier("PERMISSIONS");
			c.setMinWidth(100f);
			c.setWidth(100f);
			c.setMaxWidth(800f);
			c.setResizable(true);
			c.setDataCell(new NSTextFieldCell());
			c.dataCell().setAlignment(NSText.LeftTextAlignment);
			this.browserTable.addTableColumn(c);
		}

		this.browserTable.sizeToFit();
		// selection properties
		this.browserTable.setAllowsMultipleSelection(true);
		this.browserTable.setAllowsEmptySelection(true);
		this.browserTable.setAllowsColumnReordering(true);
	}

	public void browserTableRowDoubleClicked(Object sender) {
		log.debug("browserTableRowDoubleClicked");
		searchField.setStringValue("");
		if (browserTable.numberOfSelectedRows() > 0) {
			Path p = (Path) browserModel.getEntry(browserTable.selectedRow()); //last row selected
			if (p.isFile() || browserTable.numberOfSelectedRows() > 1) {
				NSEnumerator enum = browserTable.selectedRowEnumerator();
				if (this.isMounted()) {
					while (enum.hasMoreElements()) {
						Session session = browserModel.workdir().getSession().copy();
						CDQueueController.instance().addItem(new Queue(((Path) browserModel.getEntry(((Integer) enum.nextElement()).intValue())).copy(session), Queue.KIND_DOWNLOAD),
						    true);
					}
				}
			}
			if (p.isDirectory())
				p.list();
		}
	}

	private CDBookmarkTableDataSource bookmarkModel;
	private NSTableView bookmarkTable; // IBOutlet

	public void setBookmarkTable(NSTableView bookmarkTable) {
		this.bookmarkTable = bookmarkTable;
		this.bookmarkTable.setTarget(this);
		this.bookmarkTable.setDoubleAction(new NSSelector("bookmarkTableRowDoubleClicked", new Class[]{Object.class}));
		this.bookmarkTable.setDataSource(this.bookmarkModel = new CDBookmarkTableDataSource());
		this.bookmarkTable.setDelegate(this.bookmarkModel);
		// receive drag events from types
		this.bookmarkTable.registerForDraggedTypes(new NSArray(
		    new Object[]{"BookmarkPboardType",
		                 NSPasteboard.FilenamesPboardType, //accept bookmark files dragged from the Finder
		                 NSPasteboard.FilesPromisePboardType} //accept file promises made myself but then interpret them as BookmarkPboardType
		)
		);
		this.bookmarkTable.setRowHeight(45f);

		NSTableColumn iconColumn = new NSTableColumn();
		iconColumn.setIdentifier("ICON");
		iconColumn.setMinWidth(32f);
		iconColumn.setWidth(32f);
		iconColumn.setMaxWidth(32f);
		iconColumn.setEditable(false);
		iconColumn.setResizable(true);
		iconColumn.setDataCell(new NSImageCell());
		this.bookmarkTable.addTableColumn(iconColumn);

		NSTableColumn bookmarkColumn = new NSTableColumn();
		bookmarkColumn.setIdentifier("BOOKMARK");
		bookmarkColumn.setMinWidth(50f);
		bookmarkColumn.setWidth(200f);
		bookmarkColumn.setMaxWidth(500f);
		bookmarkColumn.setEditable(false);
		bookmarkColumn.setResizable(true);
		bookmarkColumn.setDataCell(new CDBookmarkCell());
		this.bookmarkTable.addTableColumn(bookmarkColumn);

		// setting appearance attributes
		this.bookmarkTable.setAutoresizesAllColumnsToFit(true);
		NSSelector setUsesAlternatingRowBackgroundColorsSelector =
		    new NSSelector("setUsesAlternatingRowBackgroundColors", new Class[]{boolean.class});
		if (setUsesAlternatingRowBackgroundColorsSelector.implementedByClass(NSTableView.class)) {
			this.bookmarkTable.setUsesAlternatingRowBackgroundColors(true);
		}
		NSSelector setGridStyleMaskSelector =
		    new NSSelector("setGridStyleMask", new Class[]{int.class});
		if (setGridStyleMaskSelector.implementedByClass(NSTableView.class)) {
			this.bookmarkTable.setGridStyleMask(NSTableView.SolidHorizontalGridLineMask);
		}
		this.bookmarkTable.setAutoresizesAllColumnsToFit(true);

		// selection properties
		this.bookmarkTable.setAllowsMultipleSelection(false);
		this.bookmarkTable.setAllowsEmptySelection(true);
		this.bookmarkTable.setAllowsColumnReordering(false);

		(NSNotificationCenter.defaultCenter()).addObserver(
		    this,
		    new NSSelector("bookmarkSelectionDidChange", new Class[]{NSNotification.class}),
		    NSTableView.TableViewSelectionDidChangeNotification,
		    bookmarkTable);

		this.bookmarkTable.sizeToFit();
	}

	public void bookmarkSelectionDidChange(NSNotification notification) {
		editBookmarkButton.setEnabled(bookmarkTable.numberOfSelectedRows() == 1);
		removeBookmarkButton.setEnabled(bookmarkTable.numberOfSelectedRows() == 1);
	}

	public void bookmarkTableRowDoubleClicked(Object sender) {
		log.debug("bookmarkTableRowDoubleClicked");
		if (this.bookmarkTable.selectedRow() != -1) {
			Host host = (Host) CDBookmarksImpl.instance().getItem(bookmarkTable.selectedRow());
			this.mount(host);
		}
	}

	private NSComboBox quickConnectPopup; // IBOutlet
	private CDQuickConnectDataSource quickConnectDataSource;

	public void setQuickConnectPopup(NSComboBox quickConnectPopup) {
		this.quickConnectPopup = quickConnectPopup;
		this.quickConnectPopup.setTarget(this);
		this.quickConnectPopup.setAction(new NSSelector("quickConnectSelectionChanged", new Class[]{Object.class}));
		this.quickConnectPopup.setUsesDataSource(true);
		this.quickConnectPopup.setDataSource(this.quickConnectDataSource = new CDQuickConnectDataSource());
	}

	public void quickConnectSelectionChanged(Object sender) {
		log.debug("quickConnectSelectionChanged");
		String input = ((NSControl) sender).stringValue();
//		if(input.equals("Clear")) {
//			CDHistoryImpl.instance().clear();
//			this.quickConnectPopup.reloadData();
//		}
		Host host = CDHistoryImpl.instance().getItem(input);
		if (null == host) {
			int index;
			if ((index = input.indexOf('@')) != -1)
				host = new Host(input.substring(index + 1, input.length()), new Login(input.substring(index + 1, input.length()), input.substring(0, index)));
			else
				host = new Host(input, new Login(input));
		}
		this.mount(host);
	}

	private NSTextField searchField; // IBOutlet

	public void setSearchField(NSTextField searchField) {
		this.searchField = searchField;
		NSNotificationCenter.defaultCenter().addObserver(
		    this,
		    new NSSelector("searchFieldTextDidChange", new Class[]{Object.class}),
		    NSControl.ControlTextDidChangeNotification,
		    searchField);
	}

	public void searchFieldTextDidChange(NSNotification aNotification) {
//		log.debug("searchFieldTextDidChange:"+aNotification);
		String searchString = null;
		NSDictionary userInfo = aNotification.userInfo();
		if (null != userInfo) {
			Object o = userInfo.allValues().lastObject();
			if (null != o) {
				searchString = ((NSText) o).string();
				log.debug("searchFieldTextDidChange:" + searchString);
				Iterator i = browserModel.values().iterator();
				if (null == searchString || searchString.length() == 0) {
					this.browserModel.setActiveSet(this.browserModel.values());
					this.browserTable.reloadData();
				}
				else {
					List subset = new ArrayList();
					Path next;
					while (i.hasNext()) {
						next = (Path) i.next();
						//						if(next.getName().startsWith(searchString)) {
						if (Codec.decode(next.getName()).indexOf(searchString) != -1) {
							subset.add(next);
						}
					}
					this.browserModel.setActiveSet(subset);
					this.browserTable.reloadData();
				}
			}
		}
	}

	// ----------------------------------------------------------
	// Manage Bookmarks
	// ----------------------------------------------------------

	private NSButton showBookmarkButton; // IBOutlet

	public void setShowBookmarkButton(NSButton showBookmarkButton) {
		this.showBookmarkButton = showBookmarkButton;
		this.showBookmarkButton.setImage(NSImage.imageNamed("drawer.tiff"));
		this.showBookmarkButton.setAlternateImage(NSImage.imageNamed("drawerPressed.tiff"));
		this.showBookmarkButton.setTarget(this);
		this.showBookmarkButton.setAction(new NSSelector("toggleBookmarkDrawer", new Class[]{Object.class}));
	}

	private NSButton editBookmarkButton; // IBOutlet

	public void setEditBookmarkButton(NSButton editBookmarkButton) {
		this.editBookmarkButton = editBookmarkButton;
		this.editBookmarkButton.setImage(NSImage.imageNamed("edit.tiff"));
		this.editBookmarkButton.setAlternateImage(NSImage.imageNamed("editPressed.tiff"));
		this.editBookmarkButton.setTarget(this);
		this.editBookmarkButton.setEnabled(false);
		this.editBookmarkButton.setAction(new NSSelector("editBookmarkButtonClicked", new Class[]{Object.class}));
	}

	public void editBookmarkButtonClicked(Object sender) {
		this.bookmarkDrawer.open();
		CDBookmarkController controller = new CDBookmarkController(CDBookmarksImpl.instance().getItem(bookmarkTable.selectedRow()));
		controller.window().makeKeyAndOrderFront(null);
		this.bookmarkTable.reloadData();
	}

	private NSButton addBookmarkButton; // IBOutlet

	public void setAddBookmarkButton(NSButton addBookmarkButton) {
		this.addBookmarkButton = addBookmarkButton;
		this.addBookmarkButton.setImage(NSImage.imageNamed("add.tiff"));
		this.addBookmarkButton.setAlternateImage(NSImage.imageNamed("addPressed.tiff"));
		this.addBookmarkButton.setTarget(this);
		this.addBookmarkButton.setAction(new NSSelector("addBookmarkButtonClicked", new Class[]{Object.class}));
	}

	public void addBookmarkButtonClicked(Object sender) {
		this.bookmarkDrawer.open();
		Host item;
		if (this.isMounted()) {
			Host h = browserModel.workdir().getSession().getHost();
			item = new Host(h.getProtocol(), h.getHostname(), h.getPort(), h.getLogin(), h.getDefaultPath());
		}
		else {
			item = new Host("", new Login(""));
		}
		CDBookmarksImpl.instance().addItem(item);
		CDBookmarkController controller = new CDBookmarkController(item);
		controller.window().makeKeyAndOrderFront(null);
		this.bookmarkTable.reloadData();
	}

	private NSButton removeBookmarkButton; // IBOutlet

	public void setRemoveBookmarkButton(NSButton removeBookmarkButton) {
		this.removeBookmarkButton = removeBookmarkButton;
		this.removeBookmarkButton.setImage(NSImage.imageNamed("remove.tiff"));
		this.removeBookmarkButton.setAlternateImage(NSImage.imageNamed("removePressed.tiff"));
		this.removeBookmarkButton.setTarget(this);
		this.removeBookmarkButton.setEnabled(false);
		this.removeBookmarkButton.setAction(new NSSelector("removeBookmarkButtonClicked", new Class[]{Object.class}));
	}

	public void removeBookmarkButtonClicked(Object sender) {
		this.bookmarkDrawer.open();
		CDBookmarksImpl.instance().removeItem(bookmarkTable.selectedRow());
		this.bookmarkTable.reloadData();
	}

	// ----------------------------------------------------------
	// Browser navigation
	// ----------------------------------------------------------

	private NSButton upButton; // IBOutlet

	public void setUpButton(NSButton upButton) {
		this.upButton = upButton;
		this.upButton.setImage(NSImage.imageNamed("up.tiff"));
		this.upButton.setTarget(this);
		this.upButton.setAction(new NSSelector("upButtonClicked", new Class[]{Object.class}));
	}

	private NSButton backButton; // IBOutlet

	public void setBackButton(NSButton backButton) {
		this.backButton = backButton;
		this.backButton.setImage(NSImage.imageNamed("back.tiff"));
		this.backButton.setTarget(this);
		this.backButton.setAction(new NSSelector("backButtonClicked", new Class[]{Object.class}));
	}

	private NSPopUpButton pathPopup; // IBOutlet

	public void setPathPopup(NSPopUpButton pathPopup) {
		this.pathPopup = pathPopup;
	}

	// ----------------------------------------------------------
	// Drawers
	// ----------------------------------------------------------

	private NSDrawer logDrawer; // IBOutlet
	private static boolean logDrawerOpen;

	public void setLogDrawer(NSDrawer logDrawer) {
		this.logDrawer = logDrawer;
	}

	public void toggleLogDrawer(Object sender) {
		logDrawer.toggle(this);
		logDrawerOpen = (logDrawer.state() == NSDrawer.OpenState || logDrawer.state() == NSDrawer.OpeningState);
	}

	private NSDrawer bookmarkDrawer; // IBOutlet
	private static boolean bookmarkDrawerOpen;

	public void setBookmarkDrawer(NSDrawer bookmarkDrawer) {
		this.bookmarkDrawer = bookmarkDrawer;
	}

	public void toggleBookmarkDrawer(Object sender) {
		bookmarkDrawer.toggle(this);
		bookmarkDrawerOpen = (bookmarkDrawer.state() == NSDrawer.OpenState || bookmarkDrawer.state() == NSDrawer.OpeningState);
	}

	// ----------------------------------------------------------
	// Status
	// ----------------------------------------------------------

	private NSProgressIndicator progressIndicator; // IBOutlet

	public void setProgressIndicator(NSProgressIndicator progressIndicator) {
		this.progressIndicator = progressIndicator;
		this.progressIndicator.setIndeterminate(true);
		this.progressIndicator.setUsesThreadedAnimation(true);
	}

	private NSImageView statusIcon; // IBOutlet

	public void setStatusIcon(NSImageView statusIcon) {
		this.statusIcon = statusIcon;
//		this.statusIcon.setImage(NSImage.imageNamed("offline.tiff"));
	}

	private NSTextField statusLabel; // IBOutlet

	public void setStatusLabel(NSTextField statusLabel) {
		this.statusLabel = statusLabel;
		this.statusLabel.setObjectValue(NSBundle.localizedString("Idle", "No background thread is running"));
	}

	/**
	 * Keep references of controller objects because otherweise they get garbage collected
	 * if not referenced here.
	 */
	private static NSMutableArray instances = new NSMutableArray();

	private CDPathController pathController;

	private NSToolbar toolbar;

	// ----------------------------------------------------------
	// Constructor
	// ----------------------------------------------------------

	public CDBrowserController() {
		instances.addObject(this);
		log.debug("CDBrowserController");
		if (false == NSApplication.loadNibNamed("Browser", this)) {
			log.fatal("Couldn't load Browser.nib");
			return;
		}
	}

	public void awakeFromNib() {
		log.debug("awakeFromNib");
		NSPoint origin = this.window().frame().origin();
		this.window().setTitle("Cyberduck " + NSBundle.bundleForClass(this.getClass()).objectForInfoDictionaryKey("CFBundleVersion"));
		this.window().setFrameOrigin(new NSPoint(origin.x() + 16, origin.y() - 16));
		this.pathController = new CDPathController(pathPopup);
		// Drawer states
		if (logDrawerOpen)
			this.logDrawer.open();
		if (bookmarkDrawerOpen)
			this.bookmarkDrawer.open();
		// Toolbar
		this.toolbar = new NSToolbar("Cyberduck Toolbar");
		this.toolbar.setDelegate(this);
		this.toolbar.setAllowsUserCustomization(true);
		this.toolbar.setAutosavesConfiguration(true);
		this.window().setToolbar(toolbar);
		this.window().makeFirstResponder(quickConnectPopup);
	}


	public void update(Observable o, Object arg) {
		log.debug("update:" + o + "," + arg);
//		if (o instanceof Session) {
		if (arg instanceof Path) {
			browserModel.setWorkdir((Path) arg);
			java.util.List cache = ((Path) arg).cache();
			java.util.Iterator i = cache.iterator();
			//		log.debug("List size:"+cache.size());
			browserModel.clear();
			while (i.hasNext()) {
				browserModel.addEntry((Path) i.next());
			}
			NSTableColumn selectedColumn = browserModel.selectedColumn() != null ? browserModel.selectedColumn() : browserTable.tableColumnWithIdentifier("FILENAME");
			browserTable.setIndicatorImage(browserModel.isSortedAscending() ? NSImage.imageNamed("NSAscendingSortIndicator") : NSImage.imageNamed("NSDescendingSortIndicator"), selectedColumn);
			browserModel.sort(selectedColumn, browserModel.isSortedAscending());
			browserTable.reloadData();
			this.toolbar.validateVisibleItems();
		}
		if (arg instanceof Message) {
			Message msg = (Message) arg;
			if (msg.getTitle().equals(Message.ERROR)) {
				NSAlertPanel.beginCriticalAlertSheet(
				    NSBundle.localizedString("Error", "Alert sheet title"), //title
				    NSBundle.localizedString("OK", "Alert default button"), // defaultbutton
				    null, //alternative button
				    null, //other button
				    this.window(), //docWindow
				    null, //modalDelegate
				    null, //didEndSelector
				    null, // dismiss selector
				    null, // context
				    (String) msg.getContent() // message
				);
				this.progressIndicator.stopAnimation(this);
				this.statusIcon.setImage(NSImage.imageNamed("alert.tiff"));
				this.statusLabel.setObjectValue(msg.getContent());
			}
			// update status label
			else if (msg.getTitle().equals(Message.PROGRESS)) {
				this.statusLabel.setObjectValue(msg.getContent());
				this.statusLabel.display();
			}
			else if (msg.getTitle().equals(Message.TRANSCRIPT)) {
				this.statusLabel.setObjectValue(msg.getContent());
			}
			else if (msg.getTitle().equals(Message.OPEN)) {
				this.statusIcon.setImage(null);
				this.statusIcon.setNeedsDisplay(true);
//					this.statusIcon.setImage(NSImage.imageNamed("online.tiff"));
				this.toolbar.validateVisibleItems();
			}
			else if (msg.getTitle().equals(Message.CLOSE)) {
//					this.statusIcon.setImage(NSImage.imageNamed("offline.tiff"));
				this.progressIndicator.stopAnimation(this);
				this.toolbar.validateVisibleItems();
			}
			else if (msg.getTitle().equals(Message.START)) {
				//this.statusIcon.setImage(NSImage.imageNamed("online.tiff"));
				this.progressIndicator.startAnimation(this);
				this.statusIcon.setImage(null);
				this.statusIcon.setNeedsDisplay(true);
				this.toolbar.validateVisibleItems();
			}
			else if (msg.getTitle().equals(Message.STOP)) {
				this.progressIndicator.stopAnimation(this);
				this.statusLabel.setObjectValue(NSBundle.localizedString("Idle", "No background thread is running"));
				this.toolbar.validateVisibleItems();
			}
		}
//		}
	}

	// ----------------------------------------------------------
	// Selector methods for the toolbar items
	// ----------------------------------------------------------

	public void gotoButtonClicked(Object sender) {
		log.debug("folderButtonClicked");
		CDGotoController controller = new CDGotoController(browserModel.workdir());
		NSApplication.sharedApplication().beginSheet(
		    controller.window(), //sheet
		    this.window(), //docwindow
		    controller, //modal delegate
		    new NSSelector(
		        "gotoSheetDidEnd",
		        new Class[]{NSPanel.class, int.class, Object.class}
		    ), // did end selector
		    browserModel.workdir()); //contextInfo
	}

	public void folderButtonClicked(Object sender) {
		log.debug("folderButtonClicked");
		CDFolderController controller = new CDFolderController();
		NSApplication.sharedApplication().beginSheet(
		    controller.window(), //sheet
		    this.window(), //docwindow
		    controller, //modal delegate
		    new NSSelector(
		        "newfolderSheetDidEnd",
		        new Class[]{NSPanel.class, int.class, Object.class}
		    ), // did end selector
		    browserModel.workdir()); //contextInfo
	}


	public void infoButtonClicked(Object sender) {
		log.debug("infoButtonClicked");
		Path path = (Path) browserModel.getEntry(browserTable.selectedRow());
		CDInfoController controller = new CDInfoController(path);
		controller.window().makeKeyAndOrderFront(null);
	}

	public void deleteButtonClicked(Object sender) {
		log.debug("deleteButtonClicked");
		NSEnumerator enum = browserTable.selectedRowEnumerator();
		Vector files = new Vector();
		StringBuffer alertText = new StringBuffer(NSBundle.localizedString("Really delete the following files? This cannot be undone.", "Confirm deleting files."));
		while (enum.hasMoreElements()) {
			int selected = ((Integer) enum.nextElement()).intValue();
			Path p = (Path) browserModel.getEntry(selected);
			files.add(p);
			alertText.append("\n- " + Codec.decode(p.getName()));
		}
		NSAlertPanel.beginCriticalAlertSheet(
		    NSBundle.localizedString("Delete", "Alert sheet title"), //title
		    NSBundle.localizedString("Delete", "Alert sheet default button"), // defaultbutton
		    NSBundle.localizedString("Cancel", "Alert sheet alternate button"), //alternative button
		    null, //other button
		    this.window(), //window
		    this, //delegate
		    new NSSelector
		        (
		            "deleteSheetDidEnd",
		            new Class[]
		            {
			            NSWindow.class, int.class, Object.class
		            }
		        ), // end selector
		    null, // dismiss selector
		    files, // contextInfo
		    alertText.toString()
		);
	}

	public void deleteSheetDidEnd(NSWindow sheet, int returnCode, Object contextInfo) {
		log.debug("deleteSheetDidEnd");
		sheet.orderOut(null);
		switch (returnCode) {
			case (NSAlertPanel.DefaultReturn):
				Vector files = (Vector) contextInfo;
				if (files.size() > 0) {
					Iterator i = files.iterator();
					Path p = null;
					while (i.hasNext()) {
						p = (Path) i.next();
						p.delete();
					}
					p.getParent().list();
				}
				break;
			case (NSAlertPanel.AlternateReturn):
				break;
		}
	}

	public void refreshButtonClicked(Object sender) {
		log.debug("refreshButtonClicked");
		browserModel.workdir().list();
	}

	public void downloadButtonClicked(Object sender) {
		log.debug("downloadButtonClicked");
		NSEnumerator enum = browserTable.selectedRowEnumerator();
		while (enum.hasMoreElements()) {
			Session session = browserModel.workdir().getSession().copy();
			CDQueueController.instance().addItem(new Queue(((Path) browserModel.getEntry(((Integer) enum.nextElement()).intValue())).copy(session),
			    Queue.KIND_DOWNLOAD), true);
		}
	}

	public void uploadButtonClicked(Object sender) {
		log.debug("uploadButtonClicked");
		NSOpenPanel panel = new NSOpenPanel();
		panel.setCanChooseDirectories(true);
		panel.setCanChooseFiles(true);
		panel.setAllowsMultipleSelection(true);
		panel.beginSheetForDirectory(System.getProperty("user.home"), null, null, this.window(), this, new NSSelector("uploadPanelDidEnd", new Class[]{NSOpenPanel.class, int.class, Object.class}), null);
	}

	public void uploadPanelDidEnd(NSOpenPanel sheet, int returnCode, Object contextInfo) {
		sheet.orderOut(null);
		switch (returnCode) {
			case NSPanel.OKButton:
				Path parent = browserModel.workdir();
				// selected files on the local filesystem
				NSArray selected = sheet.filenames();
				java.util.Enumeration enumerator = selected.objectEnumerator();
				while (enumerator.hasMoreElements()) {
					Session session = parent.getSession().copy();
					Path item = parent.copy(session);
					item.setPath(parent.getAbsolute(), new Local((String) enumerator.nextElement()));
					CDQueueController.instance().addItem(new Queue(item,
					    Queue.KIND_UPLOAD), true, (Observer) this);
				}
				break;
			case NSPanel.CancelButton:
				break;
		}
	}

	public void insideButtonClicked(Object sender) {
		log.debug("insideButtonClicked");
		this.browserTableRowDoubleClicked(sender);
	}

	public void backButtonClicked(Object sender) {
		log.debug("backButtonClicked");
		browserModel.workdir().getSession().getPreviousPath().list();
	}

	public void upButtonClicked(Object sender) {
		log.debug("upButtonClicked");
		browserModel.workdir().getParent().list();
	}

	public void connectButtonClicked(Object sender) {
		log.debug("connectButtonClicked");
		CDConnectionController controller = new CDConnectionController(this);
		NSApplication.sharedApplication().beginSheet(
		    controller.window(), //sheet
		    this.window(), //docwindow
		    controller, //modal delegate
		    new NSSelector(
		        "connectionSheetDidEnd",
		        new Class[]{NSWindow.class, int.class, Object.class}
		    ), // did end selector
		    null); //contextInfo
	}

	public void disconnectButtonClicked(Object sender) {
		this.unmount();
	}

	public void mount(Host host) {
		log.debug("mount:" + host);
		this.unmount();

		Session session = host.createSession();
		session.addObserver((Observer) this);
		session.addObserver((Observer) pathController);

		progressIndicator.startAnimation(this);
		pathController.removeAllItems();
		browserModel.clear();
		browserTable.reloadData();
		this.window().setTitle(host.getProtocol() + ":" + host.getHostname());

		if (host.getProtocol().equals(Session.SFTP)) {
			try {
				host.setHostKeyVerificationController(new CDHostKeyController(this.window()));
			}
			catch (com.sshtools.j2ssh.transport.InvalidHostFileException e) {
				//This exception is thrown whenever an exception occurs open or reading from the host file.
				NSAlertPanel.beginCriticalAlertSheet(
				    NSBundle.localizedString("Error", "Alert sheet title"), //title
				    NSBundle.localizedString("OK", "Alert default button"), // defaultbutton
				    null, //alternative button
				    null, //other button
				    this.window(), //docWindow
				    null, //modalDelegate
				    null, //didEndSelector
				    null, // dismiss selector
				    null, // context
				    NSBundle.localizedString("Could not open or read the host file", "Alert sheet text") + ": " + e.getMessage() // message
				);
			}
		}
		host.getLogin().setController(new CDLoginController(this.window()));
		session.mount();
		CDHistoryImpl.instance().addItem(host);
	}

	public boolean isMounted() {
		return browserModel.workdir() != null;
	}

	public boolean isConnected() {
		if (this.isMounted())
			return browserModel.workdir().getSession().isConnected();
		return false;
	}

	public void unmount() {
		log.debug("unmount");
		if (this.isConnected()) {
			browserModel.workdir().getSession().close();
		}
	}


	// ----------------------------------------------------------
	// Window delegate methods
	// ----------------------------------------------------------

	public boolean windowShouldClose(NSWindow sender) {
		if (this.isConnected()) {
			NSAlertPanel.beginCriticalAlertSheet(
			    NSBundle.localizedString("Disconnect from", "Alert sheet title") + " " + browserModel.workdir().getSession().getHost().getHostname(), //title
			    NSBundle.localizedString("Disconnect", "Alert sheet default button"), // defaultbutton
			    NSBundle.localizedString("Cancel", "Alert sheet alternate button"), //alternative button
			    null, //other button
			    sender, //window
			    this, //delegate
			    new NSSelector
			        (
			            "closeSheetDidEnd",
			            new Class[]
			            {
				            NSWindow.class, int.class, Object.class
			            }
			        ), // end selector
			    null, // dismiss selector
			    null, // context
			    NSBundle.localizedString("The connection will be closed.", "Alert sheet text") // message
			);
			return false;
		}
		return true;
	}

	public void windowWillClose(NSNotification notification) {
		if (this.isMounted()) {
			browserModel.workdir().getSession().deleteObserver((Observer) this);
			browserModel.workdir().getSession().deleteObserver((Observer) pathController);
		}
		this.window().setDelegate(null);
		NSNotificationCenter.defaultCenter().removeObserver(this);
		instances.removeObject(this);
	}


	// ----------------------------------------------------------
	// IB action methods
	// ----------------------------------------------------------

	public void closeSheetDidEnd(NSWindow sheet, int returncode, Object contextInfo) {
		// if multi window app only close the one window with main.close()
		sheet.orderOut(null);
		if (returncode == NSAlertPanel.DefaultReturn) {
			this.unmount();
			this.window().close();
		}
	}

	public boolean validateMenuItem(_NSObsoleteMenuItemProtocol cell) {
		//	log.debug("validateMenuItem:"+aCell);
		String sel = cell.action().name();
//		log.debug("validateMenuItem:"+sel);
		if (sel.equals("gotoButtonClicked:")) {
			return this.isMounted();
		}
		if (sel.equals("infoButtonClicked:")) {
			return this.isMounted() && browserTable.selectedRow() != -1;
		}
		if (sel.equals("folderButtonClicked:")) {
			return this.isMounted();
		}
		if (sel.equals("deleteButtonClicked:")) {
			return this.isMounted() && browserTable.selectedRow() != -1;
		}
		if (sel.equals("refreshButtonClicked:")) {
			return this.isMounted();
		}
		if (sel.equals("insideButtonClicked:")) {
			return this.isMounted() && browserTable.selectedRow() != -1;
		}
		if (sel.equals("upButtonClicked:")) {
			return this.isMounted();
		}
		if (sel.equals("addBookmarkButtonClicked:")) {
			return true;
		}
		if (sel.equals("removeBookmarkButtonClicked:")) {
			return bookmarkTable.numberOfSelectedRows() == 1;
		}
		if (sel.equals("editBookmarkButtonClicked:")) {
			return bookmarkTable.numberOfSelectedRows() == 1;
		}
		if (sel.equals("backButtonClicked:")) {
			return this.isMounted();
		}
		return true;
	}

	// ----------------------------------------------------------
	// Toolbar Delegate
	// ----------------------------------------------------------

	public boolean validateToolbarItem(NSToolbarItem item) {
		//	log.debug("validateToolbarItem:"+item.label());
		backButton.setEnabled(pathController.numberOfItems() > 0);
		upButton.setEnabled(pathController.numberOfItems() > 0);
		pathPopup.setEnabled(pathController.numberOfItems() > 0);

		String identifier = item.itemIdentifier();
		if (identifier.equals("Refresh")) {
			return this.isMounted();
		}
		else if (identifier.equals("Download")) {
			return this.isMounted() && browserTable.selectedRow() != -1;
		}
		else if (identifier.equals("Upload")) {
			return this.isMounted();
		}
		else if (identifier.equals("Delete")) {
			return this.isMounted() && browserTable.selectedRow() != -1;
		}
		else if (identifier.equals("New Folder")) {
			return this.isMounted();
		}
		else if (identifier.equals("Get Info")) {
			return this.isMounted() && browserTable.selectedRow() != -1;
		}
		else if (identifier.equals("Disconnect")) {
			return this.isMounted() && browserModel.workdir().getSession().isConnected();
		}
		return true;
	}

	public NSToolbarItem toolbarItemForItemIdentifier(NSToolbar toolbar, String itemIdentifier, boolean flag) {

		NSToolbarItem item = new NSToolbarItem(itemIdentifier);

		if (itemIdentifier.equals("New Connection")) {
			item.setLabel(NSBundle.localizedString("New Connection", "Toolbar item"));
			item.setPaletteLabel(NSBundle.localizedString("New Connection", "Toolbar item"));
			item.setToolTip(NSBundle.localizedString("Connect to remote host", "Toolbar item tooltip"));
			item.setImage(NSImage.imageNamed("connect.tiff"));
			item.setTarget(this);
			item.setAction(new NSSelector("connectButtonClicked", new Class[]{Object.class}));
		}
		else if (itemIdentifier.equals("Bookmarks")) {
			item.setView(showBookmarkButton);
			item.setMinSize(showBookmarkButton.frame().size());
			item.setMaxSize(showBookmarkButton.frame().size());
		}
		else if (itemIdentifier.equals("Quick Connect")) {
			item.setLabel(NSBundle.localizedString("Quick Connect", "Toolbar item"));
			item.setPaletteLabel(NSBundle.localizedString("Quick Connect", "Toolbar item"));
			item.setToolTip(NSBundle.localizedString("Connect to server", "Toolbar item tooltip"));
			item.setView(quickConnectPopup);
			item.setMinSize(quickConnectPopup.frame().size());
			item.setMaxSize(quickConnectPopup.frame().size());
		}
		else if (itemIdentifier.equals("Refresh")) {
			item.setLabel(NSBundle.localizedString("Refresh", "Toolbar item"));
			item.setPaletteLabel(NSBundle.localizedString("Refresh", "Toolbar item"));
			item.setToolTip(NSBundle.localizedString("Refresh directory listing", "Toolbar item tooltip"));
			item.setImage(NSImage.imageNamed("refresh.tiff"));
			item.setTarget(this);
			item.setAction(new NSSelector("refreshButtonClicked", new Class[]{Object.class}));
		}
		else if (itemIdentifier.equals("Download")) {
			item.setLabel(NSBundle.localizedString("Download", "Toolbar item"));
			item.setPaletteLabel(NSBundle.localizedString("Download", "Toolbar item"));
			item.setToolTip(NSBundle.localizedString("Download file", "Toolbar item tooltip"));
			item.setImage(NSImage.imageNamed("downloadFile.tiff"));
			item.setTarget(this);
			item.setAction(new NSSelector("downloadButtonClicked", new Class[]{Object.class}));
		}
		else if (itemIdentifier.equals("Upload")) {
			item.setLabel(NSBundle.localizedString("Upload", "Toolbar item"));
			item.setPaletteLabel(NSBundle.localizedString("Upload", "Toolbar item"));
			item.setToolTip(NSBundle.localizedString("Upload local file to the remote host", "Toolbar item tooltip"));
			item.setImage(NSImage.imageNamed("uploadFile.tiff"));
			item.setTarget(this);
			item.setAction(new NSSelector("uploadButtonClicked", new Class[]{Object.class}));
		}
		else if (itemIdentifier.equals("Get Info")) {
			item.setLabel(NSBundle.localizedString("Get Info", "Toolbar item"));
			item.setPaletteLabel(NSBundle.localizedString("Get Info", "Toolbar item"));
			item.setToolTip(NSBundle.localizedString("Show file attributes", "Toolbar item tooltip"));
			item.setImage(NSImage.imageNamed("info.tiff"));
			item.setTarget(this);
			item.setAction(new NSSelector("infoButtonClicked", new Class[]{Object.class}));
		}
		else if (itemIdentifier.equals("Delete")) {
			item.setLabel(NSBundle.localizedString("Delete", "Toolbar item"));
			item.setPaletteLabel(NSBundle.localizedString("Delete", "Toolbar item"));
			item.setToolTip(NSBundle.localizedString("Delete file", "Toolbar item tooltip"));
			item.setImage(NSImage.imageNamed("deleteFile.tiff"));
			item.setTarget(this);
			item.setAction(new NSSelector("deleteButtonClicked", new Class[]{Object.class}));
		}
		else if (itemIdentifier.equals("New Folder")) {
			item.setLabel(NSBundle.localizedString("New Folder", "Toolbar item"));
			item.setPaletteLabel(NSBundle.localizedString("New Folder", "Toolbar item"));
			item.setToolTip(NSBundle.localizedString("Create New Folder", "Toolbar item tooltip"));
			item.setImage(NSImage.imageNamed("newfolder.icns"));
			item.setTarget(this);
			item.setAction(new NSSelector("folderButtonClicked", new Class[]{Object.class}));
		}
		else if (itemIdentifier.equals("Disconnect")) {
			item.setLabel(NSBundle.localizedString("Disconnect", "Toolbar item"));
			item.setPaletteLabel(NSBundle.localizedString("Disconnect", "Toolbar item"));
			item.setToolTip(NSBundle.localizedString("Disconnect from server", "Toolbar item tooltip"));
			item.setImage(NSImage.imageNamed("eject.tiff"));
			item.setTarget(this);
			item.setAction(new NSSelector("disconnectButtonClicked", new Class[]{Object.class}));
		}
		else {
			// itemIdent refered to a toolbar item that is not provide or supported by us or cocoa.
			// Returning null will inform the toolbar this kind of item is not supported.
			item = null;
		}
		return item;
	}


	public NSArray toolbarDefaultItemIdentifiers(NSToolbar toolbar) {
		return new NSArray(new Object[]{
			"New Connection",
			NSToolbarItem.SeparatorItemIdentifier,
			"Bookmarks",
			"Quick Connect",
			"Refresh",
			"Get Info",
			"Download",
			"Upload",
			NSToolbarItem.FlexibleSpaceItemIdentifier,
			"Disconnect"
		});
	}

	public NSArray toolbarAllowedItemIdentifiers(NSToolbar toolbar) {
		return new NSArray(new Object[]{
			"New Connection",
			"Bookmarks",
			"Quick Connect",
			"Refresh",
			"Download",
			"Upload",
			"Delete",
			"New Folder",
			"Get Info",
			"Disconnect",
			NSToolbarItem.CustomizeToolbarItemIdentifier,
			NSToolbarItem.SpaceItemIdentifier,
			NSToolbarItem.SeparatorItemIdentifier,
			NSToolbarItem.FlexibleSpaceItemIdentifier
		});
	}
}