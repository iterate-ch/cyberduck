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

import java.util.*;

import org.apache.log4j.Logger;

import ch.cyberduck.core.*;
import ch.cyberduck.ui.cocoa.odb.Editor;

/**
 * @version $Id$
 */
public class CDBrowserController extends NSObject implements Controller, Observer {
    private static Logger log = Logger.getLogger(CDBrowserController.class);

    /**
     * Keep references of controller objects because otherweise they get garbage collected
     * if not referenced here.
     */
    private static NSMutableArray instances = new NSMutableArray();
	
    // ----------------------------------------------------------
    // Outlets
    // ----------------------------------------------------------

    private NSWindow window; // IBOutlet

    public void setWindow(NSWindow window) {
        this.window = window;
        this.window.setDelegate(this);
    }

    public NSWindow window() {
        return this.window;
    }

    public static CDBrowserController controllerForWindow(NSWindow window) {
        //2004-02-24 23:01:38.648 Cyberduck[1096] warning: can't find Java class for Objective C class (null).  Returning com/apple/cocoa/foundation/NSObject.
		if(window.isVisible()) {
			Object delegate = window.delegate();
			if (delegate != null && delegate instanceof CDBrowserController) {
				return (CDBrowserController)delegate;
			}
		}
		return null;
    }

    private NSTextView logView;

    public void setLogView(NSTextView logView) {
        this.logView = logView;
        this.logView.setFont(NSFont.userFixedPitchFontOfSize(9.0f));
    }

    private CDBrowserTableDataSource browserModel;
    private NSTableView browserTable; // IBOutlet

    public void setBrowserTable(NSTableView browserTable) {
        log.debug("setBrowserTable");
        this.browserTable = browserTable;
        this.browserTable.setTarget(this);
        this.browserTable.setDoubleAction(new NSSelector("browserTableRowDoubleClicked", new Class[]{Object.class}));
        this.browserTable.setDataSource(this.browserModel = new CDBrowserTableDataSource());
        this.browserTable.setDelegate(this.browserModel);
//        (NSNotificationCenter.defaultCenter()).addObserver(this,
//                new NSSelector("browserTableRowEdited", new Class[]{NSNotification.class}),
//                NSText.TextDidEndEditingNotification,
//                this.browserTable);

        // receive drag events from types
        this.browserTable.registerForDraggedTypes(new NSArray(new Object[]{
            "QueuePboardType",
            NSPasteboard.FilenamesPboardType, //accept files dragged from the Finder for uploading
            NSPasteboard.FilesPromisePboardType} //accept file promises made myself but then interpret them as QueuePboardType
        ));
		
        // setting appearance attributes
        this.browserTable.setRowHeight(17f);
        this.browserTable.setAutoresizesAllColumnsToFit(true);
        NSSelector setUsesAlternatingRowBackgroundColorsSelector =
                new NSSelector("setUsesAlternatingRowBackgroundColors", new Class[]{boolean.class});
        if (setUsesAlternatingRowBackgroundColorsSelector.implementedByClass(NSTableView.class)) {
            this.browserTable.setUsesAlternatingRowBackgroundColors(Preferences.instance().getProperty("browser.alternatingRows").equals("true"));
        }
        NSSelector setGridStyleMaskSelector =
                new NSSelector("setGridStyleMask", new Class[]{int.class});
        if (setGridStyleMaskSelector.implementedByClass(NSTableView.class)) {
            if (Preferences.instance().getProperty("browser.horizontalLines").equals("true") && Preferences.instance().getProperty("browser.verticalLines").equals("true")) {
                this.browserTable.setGridStyleMask(NSTableView.SolidHorizontalGridLineMask | NSTableView.SolidVerticalGridLineMask);
            }
            else if (Preferences.instance().getProperty("browser.verticalLines").equals("true")) {
                this.browserTable.setGridStyleMask(NSTableView.SolidVerticalGridLineMask);
            }
            else if (Preferences.instance().getProperty("browser.horizontalLines").equals("true")) {
                this.browserTable.setGridStyleMask(NSTableView.SolidHorizontalGridLineMask);
            }
            else {
                this.browserTable.setGridStyleMask(NSTableView.GridNone);
            }
        }
		
        // ading table columns
        if (Preferences.instance().getProperty("browser.columnIcon").equals("true")) {
            NSTableColumn c = new NSTableColumn();
            c.setIdentifier("TYPE");
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
            c.setEditable(false); //@todo allow filename editing
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
            c.setMaxWidth(100f);
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
			//log.info("Using date formatter with scheme "+NSUserDefaults.standardUserDefaults().objectForKey(NSUserDefaults.ShortTimeDateFormatString));
			c.dataCell().setFormatter(new NSGregorianDateFormatter((String)NSUserDefaults.standardUserDefaults().objectForKey(NSUserDefaults.ShortTimeDateFormatString), 
																   true));
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
        if (browserModel.numberOfRowsInTableView(browserTable) > 0 && browserTable.numberOfSelectedRows() > 0) {
            Path p = (Path)browserModel.getEntry(browserTable.selectedRow()); //last row selected
            if (p.attributes.isFile() || browserTable.numberOfSelectedRows() > 1) {
				if(Preferences.instance().getProperty("browser.doubleClickOnFile").equals("edit")) {
					this.editButtonClicked(sender);
				}
				else {
					this.downloadButtonClicked(sender);
				}
            }
            if (p.attributes.isDirectory()) {
                p.list();
            }
        }
    }

//    public void browserTableRowEdited(Object sender) {
//        log.debug("browserTableRowEdited");
//    }

    private CDBookmarkTableDataSource bookmarkModel;
    private NSTableView bookmarkTable; // IBOutlet

    public void setBookmarkTable(NSTableView bookmarkTable) {
        log.debug("setBookmarkTable");
        this.bookmarkTable = bookmarkTable;
        this.bookmarkTable.setTarget(this);
        this.bookmarkTable.setDoubleAction(new NSSelector("bookmarkTableRowDoubleClicked", new Class[]{Object.class}));
        this.bookmarkTable.setDataSource(this.bookmarkModel = new CDBookmarkTableDataSource());
        this.bookmarkTable.setDelegate(this.bookmarkModel);

        // receive drag events from types
        this.bookmarkTable.registerForDraggedTypes(new NSArray(new Object[]
															   {NSPasteboard.FilenamesPboardType}
															   )); //accept bookmark files dragged from the Finder
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
            this.bookmarkTable.setUsesAlternatingRowBackgroundColors(Preferences.instance().getProperty("browser.alternatingRows").equals("true"));
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

        this.bookmarkTable.sizeToFit();

        (NSNotificationCenter.defaultCenter()).addObserver(this,
                new NSSelector("bookmarkSelectionDidChange", new Class[]{NSNotification.class}),
                NSTableView.TableViewSelectionDidChangeNotification,
                this.bookmarkTable);
    }

    public void bookmarkSelectionDidChange(NSNotification notification) {
        log.debug("bookmarkSelectionDidChange");
        editBookmarkButton.setEnabled(bookmarkTable.numberOfSelectedRows() == 1);
        removeBookmarkButton.setEnabled(bookmarkTable.numberOfSelectedRows() == 1);
    }

    public void bookmarkTableRowDoubleClicked(Object sender) {
        log.debug("bookmarkTableRowDoubleClicked");
        if (this.bookmarkTable.selectedRow() != -1) {
            Host host = (Host)BookmarkList.instance().getItem(bookmarkTable.selectedRow());
            this.window().setTitle(host.getProtocol() + ":" + host.getHostname());
            this.mount(host);
        }
    }

    private NSComboBox quickConnectPopup; // IBOutlet
    private Object quickConnectDataSource;

    public void setQuickConnectPopup(NSComboBox quickConnectPopup) {
        this.quickConnectPopup = quickConnectPopup;
        this.quickConnectPopup.setTarget(this);
        this.quickConnectPopup.setAction(new NSSelector("quickConnectSelectionChanged", new Class[]{Object.class}));
        this.quickConnectPopup.setUsesDataSource(true);
        this.quickConnectPopup.setDataSource(this.quickConnectDataSource = new Object() {
            public int numberOfItemsInComboBox(NSComboBox combo) {
                return BookmarkList.instance().size();
            }

            public Object comboBoxObjectValueForItemAtIndex(NSComboBox combo, int row) {
                return BookmarkList.instance().getItem(row).getHostname();
            }
        });
    }

    public void quickConnectSelectionChanged(Object sender) {
        log.debug("quickConnectSelectionChanged");
        String input = ((NSControl)sender).stringValue();
        for (Iterator iter = BookmarkList.instance().iterator(); iter.hasNext();) {
            Host h = (Host)iter.next();
            if (h.getHostname().equals(input)) {
                this.mount(h);
                return;
            }
        }
        //        Host host = CDHistoryImpl.instance().getItem(input);
        int index;
        Host host = null;
        if ((index = input.indexOf('@')) != -1) {
            host = new Host(input.substring(index + 1, input.length()),
                    new Login(input.substring(index + 1, input.length()),
                            input.substring(0, index), null));
        }
        else {
            host = new Host(input, new Login(input, null, null));
            if (host.getProtocol().equals(Session.FTP)) {
                host.getLogin().setUsername(Preferences.instance().getProperty("ftp.anonymous.name"));
            }
            else {
                host.getLogin().setUsername(Preferences.instance().getProperty("connection.login.name"));
            }
        }
        this.mount(host);
    }

    private NSTextField searchField; // IBOutlet

    public void setSearchField(NSTextField searchField) {
        this.searchField = searchField;
        NSNotificationCenter.defaultCenter().addObserver(this,
                new NSSelector("searchFieldTextDidChange", new Class[]{Object.class}),
                NSControl.ControlTextDidChangeNotification,
                searchField);
    }

    public void searchFieldTextDidChange(NSNotification notification) {
        String searchString = null;
        NSDictionary userInfo = notification.userInfo();
        if (null != userInfo) {
            Object o = userInfo.allValues().lastObject();
            if (null != o) {
                searchString = ((NSText)o).string();
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
                        next = (Path)i.next();
                        if (next.getName().toLowerCase().startsWith(searchString.toLowerCase())) {
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
        CDBookmarkController controller = new CDBookmarkController(bookmarkTable,
                BookmarkList.instance().getItem(bookmarkTable.selectedRow()));
        controller.window().makeKeyAndOrderFront(null);
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
            Host h = pathController.workdir().getSession().getHost();
            item = new Host(h.getProtocol(),
                    h.getHostname(),
                    h.getPort(),
                    new Login(h.getHostname(), h.getLogin().getUsername(), h.getLogin().getPassword()),
                    pathController.workdir().getAbsolute());
        }
        else {
            item = new Host("", new Login("", null, null));
        }
        BookmarkList.instance().addItem(item);
        this.bookmarkTable.reloadData();
        this.bookmarkTable.selectRow(BookmarkList.instance().indexOf(item), false);
        this.bookmarkTable.scrollRowToVisible(BookmarkList.instance().indexOf(item));
        CDBookmarkController controller = new CDBookmarkController(bookmarkTable, item);
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
        switch (NSAlertPanel.runCriticalAlert(NSBundle.localizedString("Delete Bookmark", ""),
                NSBundle.localizedString("Do you want to delete the selected bookmark?", ""),
                NSBundle.localizedString("Delete", ""),
                NSBundle.localizedString("Cancel", ""),
                null)) {
            case NSAlertPanel.DefaultReturn:
                BookmarkList.instance().removeItem(bookmarkTable.selectedRow());
                this.bookmarkTable.reloadData();
                break;
            case NSAlertPanel.AlternateReturn:
                break;
        }
    }
	
	public void copyURLButtonClicked(Object sender) {
        log.debug("copyURLButtonClicked");
		Host h = pathController.workdir().getSession().getHost();
		NSPasteboard pboard = NSPasteboard.pasteboardWithName(NSPasteboard.GeneralPboard);
		pboard.declareTypes(new NSArray(NSPasteboard.StringPboardType), null);
		if(!pboard.setStringForType(h.getURL(), NSPasteboard.StringPboardType)) {
			log.error("Error writing URL to NSPasteboard.StringPboardType.");
		}
//		pboard.declareTypes(new NSArray(NSPasteboard.URLPboardType), null);
//		if(!pboard.setStringForType(h.getURL(), NSPasteboard.URLPboardType)) {
//			log.error("Error writing URL to NSPasteboard.URLPboardType.");
//		}
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

    public void setLogDrawer(NSDrawer logDrawer) {
        this.logDrawer = logDrawer;
    }

    public void toggleLogDrawer(Object sender) {
        logDrawer.toggle(this);
        Preferences.instance().setProperty("logDrawer.isOpen", logDrawer.state() == NSDrawer.OpenState || logDrawer.state() == NSDrawer.OpeningState);
    }

    private NSDrawer bookmarkDrawer; // IBOutlet

    public void setBookmarkDrawer(NSDrawer bookmarkDrawer) {
        this.bookmarkDrawer = bookmarkDrawer;
        this.bookmarkDrawer.setDelegate(this);
    }

    public void toggleBookmarkDrawer(Object sender) {
        bookmarkDrawer.toggle(this);
        Preferences.instance().setProperty("bookmarkDrawer.isOpen", bookmarkDrawer.state() == NSDrawer.OpenState || bookmarkDrawer.state() == NSDrawer.OpeningState);
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

    private CDPathController pathController;

    private NSToolbar toolbar;

    // ----------------------------------------------------------
    // Constructor
    // ----------------------------------------------------------

    public CDBrowserController() {
        instances.addObject(this);
        if (false == NSApplication.loadNibNamed("Browser", this)) {
            log.fatal("Couldn't load Browser.nib");
        }
    }

    public void awakeFromNib() {
        this.window.setTitle("Cyberduck " + NSBundle.bundleForClass(this.getClass()).objectForInfoDictionaryKey("CFBundleVersion"));
//        NSPoint origin = this.window.frame().origin();
//        this.window.setFrameOrigin(this.window.cascadeTopLeftFromPoint(new NSPoint(origin.x(), origin.y())));
        this.pathController = new CDPathController(pathPopup);
        // Drawer states
        if (Preferences.instance().getProperty("logDrawer.isOpen").equals("true")) {
            this.logDrawer.open();
        }
        if (Preferences.instance().getProperty("bookmarkDrawer.isOpen").equals("true")) {
            this.showBookmarkButton.setState(NSCell.OnState);
            this.bookmarkDrawer.open();
        }
        // Toolbar
        this.toolbar = new NSToolbar("Cyberduck Toolbar");
        this.toolbar.setDelegate(this);
        this.toolbar.setAllowsUserCustomization(true);
        this.toolbar.setAutosavesConfiguration(true);
        this.window.setToolbar(toolbar);
        this.window.makeFirstResponder(quickConnectPopup);
    }


    public void update(final Observable o, final Object arg) {
        log.debug("update:" + o + "," + arg);
        if (arg instanceof Path) {
            browserModel.setData(((Path)arg).cache());
            NSTableColumn selectedColumn = browserModel.selectedColumn() != null ? browserModel.selectedColumn() : browserTable.tableColumnWithIdentifier("FILENAME");
            browserTable.setIndicatorImage(browserModel.isSortedAscending() ? NSImage.imageNamed("NSAscendingSortIndicator") : NSImage.imageNamed("NSDescendingSortIndicator"), selectedColumn);
            browserModel.sort(selectedColumn, browserModel.isSortedAscending());
            browserTable.reloadData();
//            browserTable.setNeedsDisplay(true);
            toolbar.validateVisibleItems();
            window.makeFirstResponder(browserTable);
        }
        else if (arg instanceof Message) {
            Message msg = (Message)arg;
            if (msg.getTitle().equals(Message.ERROR)) {
                if (window().isVisible()) {
					NSAlertPanel.beginCriticalAlertSheet(NSBundle.localizedString("Error", "Alert sheet title"), //title
														 NSBundle.localizedString("OK", "Alert default button"), // defaultbutton
														 null, //alternative button
														 null, //other button
														 window(), //docWindow
														 null, //modalDelegate
														 null, //didEndSelector
														 null, // dismiss selector
														 null, // context
														 (String)msg.getContent() // message
														 );
                }
                progressIndicator.stopAnimation(this);
                statusIcon.setImage(NSImage.imageNamed("alert.tiff"));
                statusIcon.setNeedsDisplay(true);
                statusLabel.setObjectValue(msg.getContent());
                statusLabel.display();
                //window().setDocumentEdited(false);
            }
            else if (msg.getTitle().equals(Message.REFRESH)) {
                refreshButtonClicked(null);
            }
            // update status label
            else if (msg.getTitle().equals(Message.PROGRESS)) {
                statusLabel.setObjectValue(msg.getContent());
                statusLabel.display();
                //statusIcon.setImage(isConnected() ? NSImage.imageNamed("online.tiff") : NSImage.imageNamed("offline.tiff"));
                //statusIcon.setNeedsDisplay(true);
            }
            else if (msg.getTitle().equals(Message.OPEN)) {
                statusIcon.setImage(null);
                statusIcon.setNeedsDisplay(true);
                //                CDHistoryImpl.instance().addItem(((Session) o).host);
                toolbar.validateVisibleItems();
                window().setDocumentEdited(true);
            }
            else if (msg.getTitle().equals(Message.CLOSE)) {
                window().setDocumentEdited(false);
//				browserModel.clear();
//				browserTable.reloadData();
            }
            else if (msg.getTitle().equals(Message.START)) {
                statusIcon.setImage(null);
                statusIcon.setNeedsDisplay(true);
                progressIndicator.startAnimation(this);
                toolbar.validateVisibleItems();
            }
            else if (msg.getTitle().equals(Message.STOP)) {
                progressIndicator.stopAnimation(this);
                statusLabel.setObjectValue(NSBundle.localizedString("Idle", "No background thread is running"));
                statusLabel.display();
                //statusIcon.setImage(isConnected() ? NSImage.imageNamed("online.tiff") : NSImage.imageNamed("offline.tiff"));
                //statusIcon.setNeedsDisplay(true);
                toolbar.validateVisibleItems();
            }
        }
    }
	
    // ----------------------------------------------------------
    // Selector methods for the toolbar items
    // ----------------------------------------------------------

    public void editButtonClicked(Object sender) {
        NSEnumerator enum = browserTable.selectedRowEnumerator();
        while (enum.hasMoreElements()) {
            int selected = ((Integer)enum.nextElement()).intValue();
            Path path = browserModel.getEntry(selected);
            if (path.attributes.isFile()) {
                Editor editor = new Editor();
                editor.open(path);
            }
        }
    }

    public void gotoButtonClicked(Object sender) {
        log.debug("folderButtonClicked");
        CDGotoController controller = new CDGotoController(pathController.workdir());
        NSApplication.sharedApplication().beginSheet(controller.window(), //sheet
                this.window(), //docwindow
                controller, //modal delegate
                new NSSelector("gotoSheetDidEnd",
                        new Class[]{NSPanel.class, int.class, Object.class}), // did end selector
                pathController.workdir()); //contextInfo
    }

    public void folderButtonClicked(Object sender) {
        log.debug("folderButtonClicked");
        CDFolderController controller = new CDFolderController();
        NSApplication.sharedApplication().beginSheet(controller.window(), //sheet
                this.window(), //docwindow
                controller, //modal delegate
                new NSSelector("newFolderSheetDidEnd",
                        new Class[]{NSPanel.class, int.class, Object.class}), // did end selector
                pathController.workdir()); //contextInfo
    }


    public void infoButtonClicked(Object sender) {
        log.debug("infoButtonClicked");
        NSEnumerator enum = browserTable.selectedRowEnumerator();
		List files = new ArrayList();
        while (enum.hasMoreElements()) {
            int selected = ((Integer)enum.nextElement()).intValue();
			files.add(browserModel.getEntry(selected));
//            Path path = browserModel.getEntry(selected);
//            CDInfoController controller = new CDInfoController(path);
//            controller.window().makeKeyAndOrderFront(null);
        }
		CDInfoController controller = new CDInfoController(files);
		controller.window().makeKeyAndOrderFront(null);
    }

    public void deleteButtonClicked(Object sender) {
        log.debug("deleteButtonClicked");
        NSEnumerator enum = browserTable.selectedRowEnumerator();
        Vector files = new Vector();
        StringBuffer alertText = new StringBuffer(NSBundle.localizedString("Really delete the following files? This cannot be undone.", "Confirm deleting files."));
        while (enum.hasMoreElements()) {
            int selected = ((Integer)enum.nextElement()).intValue();
            Path p = (Path)browserModel.getEntry(selected);
            files.add(p);
            alertText.append("\n- " + p.getName());
        }
		NSAlertPanel.beginCriticalAlertSheet(NSBundle.localizedString("Delete", "Alert sheet title"), //title
											 NSBundle.localizedString("Delete", "Alert sheet default button"), // defaultbutton
											 NSBundle.localizedString("Cancel", "Alert sheet alternate button"), //alternative button
											 null, //other button
											 this.window(), //window
											 this, //delegate
											 new NSSelector
											 ("deleteSheetDidEnd",
											  new Class[]
											  {
												  NSWindow.class, int.class, Object.class
											  }), // end selector
											 null, // dismiss selector
											 files, // contextInfo
											 alertText.toString());
    }

    public void deleteSheetDidEnd(NSWindow sheet, int returnCode, Object contextInfo) {
        log.debug("deleteSheetDidEnd");
        sheet.orderOut(null);
        switch (returnCode) {
            case (NSAlertPanel.DefaultReturn):
                Vector files = (Vector)contextInfo;
                if (files.size() > 0) {
                    Iterator i = files.iterator();
                    Path p = null;
                    while (i.hasNext()) {
                        p = (Path)i.next();
                        p.delete();
                    }
                    p.getParent().list(true);
                }
                break;
            case (NSAlertPanel.AlternateReturn):
                break;
        }
    }

    public void refreshButtonClicked(Object sender) {
        log.debug("refreshButtonClicked");
        this.browserTable.deselectAll(sender);
        this.pathController.workdir().list(true);
    }

    public void downloadAsButtonClicked(Object sender) {
        if (browserModel.numberOfRowsInTableView(browserTable) > 0 && browserTable.numberOfSelectedRows() > 0) {
            if (this.isMounted()) {
                NSEnumerator enum = browserTable.selectedRowEnumerator();
                while (enum.hasMoreElements()) {
                    Session session = pathController.workdir().getSession().copy();
                    Path path = ((Path)browserModel.getEntry(((Integer)enum.nextElement()).intValue())).copy(session);
                    NSSavePanel panel = NSSavePanel.savePanel();
                    panel.setMessage(NSBundle.localizedString("Download the selected file to...", ""));
                    panel.setNameFieldLabel(NSBundle.localizedString("Download As:", ""));
                    panel.setPrompt(NSBundle.localizedString("Download", ""));
                    panel.setTitle("Download");
                    panel.setCanCreateDirectories(true);
                    panel.beginSheetForDirectory(null,
                            path.getLocal().getName(),
                            this.window(),
                            this,
                            new NSSelector("saveAsPanelDidEnd", new Class[]{NSOpenPanel.class, int.class, Object.class}),
                            path);
                }
            }
        }
    }

    public void saveAsPanelDidEnd(NSSavePanel sheet, int returnCode, Object contextInfo) {
        switch (returnCode) {
            case (NSAlertPanel.DefaultReturn):
                {
                    String filename = null;
                    if ((filename = sheet.filename()) != null) {
                        Path path = (Path)contextInfo;
                        path.setLocal(new Local(filename));
                        Queue queue = new Queue(Queue.KIND_DOWNLOAD);
                        queue.addRoot(path);
                        QueueList.instance().addItem(queue);
                        CDQueueController.instance().startItem(queue);
                    }
                    break;
                }
            case (NSAlertPanel.AlternateReturn):
                {
                    break;
                }
        }
    }

    public void downloadButtonClicked(Object sender) {
        if (browserModel.numberOfRowsInTableView(browserTable) > 0 && browserTable.numberOfSelectedRows() > 0) {
            if (this.isMounted()) {
                NSEnumerator enum = browserTable.selectedRowEnumerator();
                Queue q = new Queue(Queue.KIND_DOWNLOAD);
                Session session = pathController.workdir().getSession().copy();
                while (enum.hasMoreElements()) {
                    Path path = ((Path)browserModel.getEntry(((Integer)enum.nextElement()).intValue())).copy(session);
                    q.addRoot(path);
                }
                QueueList.instance().addItem(q);
                CDQueueController.instance().startItem(q);
            }
        }
    }

    public void uploadButtonClicked(Object sender) {
        log.debug("uploadButtonClicked");
        NSOpenPanel panel = NSOpenPanel.openPanel();
        panel.setCanChooseDirectories(true);
        panel.setCanChooseFiles(true);
        panel.setAllowsMultipleSelection(true);
        panel.beginSheetForDirectory(null, null, null, this.window(), this, new NSSelector("uploadPanelDidEnd", new Class[]{NSOpenPanel.class, int.class, Object.class}), null);
    }

    public void uploadPanelDidEnd(NSOpenPanel sheet, int returnCode, Object contextInfo) {
        sheet.orderOut(null);
        switch (returnCode) {
            case (NSAlertPanel.DefaultReturn):
                Path parent = pathController.workdir();
                // selected files on the local filesystem
                NSArray selected = sheet.filenames();
                java.util.Enumeration enumerator = selected.objectEnumerator();
                Queue q = new Queue(Queue.KIND_UPLOAD);
                Session session = parent.getSession().copy();
                while (enumerator.hasMoreElements()) {
                    Path item = parent.copy(session);
                    item.setPath(parent.getAbsolute(), new Local((String)enumerator.nextElement()));
                    q.addRoot(item);
                }
                QueueList.instance().addItem(q);
                CDQueueController.instance().startItem(q, (Observer)this);
                break;
            case (NSAlertPanel.AlternateReturn):
                break;
        }
    }

    public void insideButtonClicked(Object sender) {
        log.debug("insideButtonClicked");
        this.browserTableRowDoubleClicked(sender);
    }

    public void backButtonClicked(Object sender) {
        log.debug("backButtonClicked");
        pathController.workdir().getSession().getPreviousPath().list();
    }

    public void upButtonClicked(Object sender) {
        log.debug("upButtonClicked");
        pathController.workdir().getParent().list();
    }

    public void connectButtonClicked(Object sender) {
        log.debug("connectButtonClicked");
        CDConnectionController controller = new CDConnectionController(this);
        NSApplication.sharedApplication().beginSheet(controller.window(), //sheet
                this.window(), //docwindow
                controller, //modal delegate
                new NSSelector("connectionSheetDidEnd",
                        new Class[]{NSWindow.class, int.class, Object.class}), // did end selector
                null); //contextInfo
    }

    public void disconnectButtonClicked(Object sender) {
        this.unmount(new NSSelector("unmountSheetDidEnd",
                new Class[]{NSWindow.class, int.class, Object.class}), null // end selector
        );
    }

    public boolean isMounted() {
        boolean mounted = pathController.workdir() != null;
        return mounted;
    }

    public boolean isConnected() {
        boolean connected = false;
        if (this.isMounted()) {
            connected = pathController.workdir().getSession().isConnected();
        }
        log.info("Connected:" + connected);
        return connected;
    }

    public void mount(final Host host) {
        log.debug("mount:" + host);
        if (this.unmount(new NSSelector("mountSheetDidEnd",
                new Class[]{NSWindow.class, int.class, Object.class}), host// end selector
        )) {
            this.window().setTitle(host.getProtocol() + ":" + host.getHostname());
            pathController.removeAllItems();
            browserModel.clear();
            browserTable.reloadData();

            TranscriptFactory.addImpl(host.getHostname(), new CDTranscriptImpl(logView));

            Session session = SessionFactory.createSession(host);
            session.addObserver((Observer)this);
            session.addObserver((Observer)pathController);

            progressIndicator.startAnimation(this);

            if (session instanceof ch.cyberduck.core.sftp.SFTPSession) {
                try {
                    host.setHostKeyVerificationController(new CDHostKeyController(this));
                }
                catch (com.sshtools.j2ssh.transport.InvalidHostFileException e) {
                    //This exception is thrown whenever an exception occurs open or reading from the host file.
					NSAlertPanel.beginCriticalAlertSheet(NSBundle.localizedString("Error", "Alert sheet title"), //title
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
            host.getLogin().setController(new CDLoginController(this));
            session.mount();
        }
    }

    public void mountSheetDidEnd(NSWindow sheet, int returncode, Object contextInfo) {
        this.unmountSheetDidEnd(sheet, returncode, contextInfo);
        if (returncode == NSAlertPanel.DefaultReturn) {
            this.mount((Host)contextInfo);
        }
    }

    /**
     * @return True if the unmount process has finished, false if the user has to agree first to close the connection
     */
    public boolean unmount(NSSelector selector, Object context) {
        log.debug("unmount");
		//this.window().makeKeyAndOrderFront(null);
        if (this.isConnected()) {
			NSAlertPanel.beginCriticalAlertSheet(NSBundle.localizedString("Disconnect from", "Alert sheet title") + " " + pathController.workdir().getSession().getHost().getHostname(), //title
												 NSBundle.localizedString("Disconnect", "Alert sheet default button"), // defaultbutton
												 NSBundle.localizedString("Cancel", "Alert sheet alternate button"), // alternate button
												 null, //other button
												 this.window(), //window
												 this, //delegate
												 selector,
												 null, // dismiss selector
												 context, // context
												 NSBundle.localizedString("The connection will be closed.", "Alert sheet text") // message
												 );
            return false;
        }
        return true;
    }

	public static void reviewMountedBrowsers(boolean proceed) {
		// Determine if there are any open connections
		if(proceed) {
			NSArray windows = NSApplication.sharedApplication().windows();
			int count = windows.count();
			log.debug("Number of open windows:"+count);
			int terminateReturnValue = NSApplication.TerminateNow;
			while (0 != count--) {
				NSWindow window = (NSWindow)windows.objectAtIndex(count);
				CDBrowserController controller = CDBrowserController.controllerForWindow(window);
				if (null != controller) {
					log.debug("Window with index number "+count+" has a controller attached");
					if(!controller.unmount(new NSSelector("terminateReviewSheetDidEnd",
												   new Class[]{NSWindow.class, int.class, Object.class}),
									null
									)) {
						return; 
					}
				}
			}
		}
		// also check if the transfer queue has items running
		/*
		if(CDQueueController.instance().checkForRunningTransfers() == NSApplication.TerminateLater) {
			// the transfer queue must be cancled first and wil then send the application terminate answer event itself
			NSApplication.sharedApplication().replyToApplicationShouldTerminate(false);
			return;
		}
		 */
		// no running transfer, we quit if the user didn't choose to !procceed
		NSApplication.sharedApplication().replyToApplicationShouldTerminate(proceed);
	}
	
    // ----------------------------------------------------------
    // Window delegate methods
    // ----------------------------------------------------------

    public boolean windowShouldClose(NSWindow sender) {
        return this.unmount(new NSSelector("closeSheetDidEnd",
                new Class[]{NSWindow.class, int.class, Object.class}), null // end selector
        );
    }

	public void unmountSheetDidEnd(NSWindow sheet, int returncode, Object contextInfo) {
		sheet.orderOut(null);
		if (returncode == NSAlertPanel.DefaultReturn) {
			pathController.workdir().getSession().close();
		}
        if (returncode == NSAlertPanel.AlternateReturn) {
			//
        }
	}

    public void closeSheetDidEnd(NSWindow sheet, int returncode, Object contextInfo) {
        this.unmountSheetDidEnd(sheet, returncode, contextInfo);
        if (returncode == NSAlertPanel.DefaultReturn) {
            this.window().close();
        }
        if (returncode == NSAlertPanel.AlternateReturn) {
			//
        }
    }
	
	public void terminateReviewSheetDidEnd(NSWindow sheet, int returncode, Object contextInfo) {
        this.closeSheetDidEnd(sheet, returncode, contextInfo);
        if (returncode == NSAlertPanel.DefaultReturn) {
			CDBrowserController.reviewMountedBrowsers(true);
        }
        if (returncode == NSAlertPanel.AlternateReturn) {
			CDBrowserController.reviewMountedBrowsers(false);
        }
	}

    public void windowWillClose(NSNotification notification) {
        log.debug("windowWillClose");
        if (this.isMounted()) {
            pathController.workdir().getSession().deleteObserver((Observer)this);
            pathController.workdir().getSession().deleteObserver((Observer)pathController);
        }
        NSNotificationCenter.defaultCenter().removeObserver(this);
        this.bookmarkDrawer.close();
        this.logDrawer.close();
        instances.removeObject(this);
    }

    public boolean validateMenuItem(_NSObsoleteMenuItemProtocol cell) {
        boolean v = this.validateItem(cell.action().name());
        log.debug("validateMenuItem:" + cell.action().name() + "->" + v);
        return v;
    }

    private boolean validateItem(String identifier) {
        if (identifier.equals("addBookmarkButtonClicked:")) {
            return true;
        }
        if (identifier.equals("removeBookmarkButtonClicked:")) {
            return bookmarkTable.numberOfSelectedRows() == 1;
        }
        if (identifier.equals("editBookmarkButtonClicked:")) {
            return bookmarkTable.numberOfSelectedRows() == 1;
        }
        if (identifier.equals("Edit") || identifier.equals("editButtonClicked:")) {
            if (this.isMounted() && browserModel.numberOfRowsInTableView(browserTable) > 0 && browserTable.selectedRow() != -1) {
                Path p = (Path)browserModel.getEntry(browserTable.selectedRow());
                String editorPath = null;
                NSSelector absolutePathForAppBundleWithIdentifierSelector =
                        new NSSelector("absolutePathForAppBundleWithIdentifier", new Class[]{String.class});
                if (absolutePathForAppBundleWithIdentifierSelector.implementedByClass(NSWorkspace.class)) {
                    editorPath = NSWorkspace.sharedWorkspace().absolutePathForAppBundleWithIdentifier(Preferences.instance().getProperty("editor.bundleIdentifier"));
                }
                return p.attributes.isFile() && editorPath != null;
            }
            return false;
        }
        if (identifier.equals("gotoButtonClicked:")) {
            return this.isMounted();
        }
        if (identifier.equals("Get Info") || identifier.equals("infoButtonClicked:")) {
            return this.isMounted() && browserTable.selectedRow() != -1;
        }
        if (identifier.equals("New Folder") || identifier.equals("folderButtonClicked:")) {
            return this.isMounted();
        }
        if (identifier.equals("Delete") || identifier.equals("deleteButtonClicked:")) {
            return this.isMounted() && browserTable.selectedRow() != -1;
        }
        if (identifier.equals("Refresh") || identifier.equals("refreshButtonClicked:")) {
            return this.isMounted();
        }
        if (identifier.equals("Download") || identifier.equals("downloadButtonClicked:")) {
            return this.isMounted() && browserTable.selectedRow() != -1;
        }
        if (identifier.equals("Upload") || identifier.equals("downloadButtonClicked:")) {
            return this.isMounted();
        }
        if (identifier.equals("downloadAsButtonClicked:")) {
            return this.isMounted() && browserTable.selectedRow() != -1;
        }
        if (identifier.equals("insideButtonClicked:")) {
            return this.isMounted() && browserTable.selectedRow() != -1;
        }
        if (identifier.equals("upButtonClicked:")) {
            return this.isMounted();
        }
        if (identifier.equals("backButtonClicked:")) {
            return this.isMounted();
        }
        if (identifier.equals("copyURLButtonClicked:")) {
            return this.isMounted();
        }
        if (identifier.equals("Disconnect")) {
            return this.isMounted() && pathController.workdir().getSession().isConnected();
        }
        return true; // by default everything is enabled
    }
    // ----------------------------------------------------------
    // Toolbar Delegate
    // ----------------------------------------------------------
	
    public boolean validateToolbarItem(NSToolbarItem item) {
        //	log.debug("validateToolbarItem:"+item.label());
        this.backButton.setEnabled(pathController.numberOfItems() > 0);
        this.upButton.setEnabled(pathController.numberOfItems() > 0);
        this.pathPopup.setEnabled(pathController.numberOfItems() > 0);
        return this.validateItem(item.itemIdentifier());
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
            return item;
        }
        if (itemIdentifier.equals("Bookmarks")) {
//            item.setLabel(NSBundle.localizedString("Bookmarks", "Toolbar item"));
//            item.setPaletteLabel(NSBundle.localizedString("Bookmarks", "Toolbar item"));
//            item.setToolTip(NSBundle.localizedString("Toggle Bookmarks", "Toolbar item tooltip"));
            item.setView(showBookmarkButton);
            item.setMinSize(showBookmarkButton.frame().size());
            item.setMaxSize(showBookmarkButton.frame().size());
            return item;
        }
        if (itemIdentifier.equals("Quick Connect")) {
            item.setLabel(NSBundle.localizedString("Quick Connect", "Toolbar item"));
            item.setPaletteLabel(NSBundle.localizedString("Quick Connect", "Toolbar item"));
            item.setToolTip(NSBundle.localizedString("Connect to server", "Toolbar item tooltip"));
            item.setView(quickConnectPopup);
            item.setMinSize(quickConnectPopup.frame().size());
            item.setMaxSize(quickConnectPopup.frame().size());
            return item;
        }
        if (itemIdentifier.equals("Refresh")) {
            item.setLabel(NSBundle.localizedString("Refresh", "Toolbar item"));
            item.setPaletteLabel(NSBundle.localizedString("Refresh", "Toolbar item"));
            item.setToolTip(NSBundle.localizedString("Refresh directory listing", "Toolbar item tooltip"));
            item.setImage(NSImage.imageNamed("refresh.tiff"));
            item.setTarget(this);
            item.setAction(new NSSelector("refreshButtonClicked", new Class[]{Object.class}));
            return item;
        }
        if (itemIdentifier.equals("Download")) {
            item.setLabel(NSBundle.localizedString("Download", "Toolbar item"));
            item.setPaletteLabel(NSBundle.localizedString("Download", "Toolbar item"));
            item.setToolTip(NSBundle.localizedString("Download file", "Toolbar item tooltip"));
            item.setImage(NSImage.imageNamed("downloadFile.tiff"));
            item.setTarget(this);
            item.setAction(new NSSelector("downloadButtonClicked", new Class[]{Object.class}));
            return item;
        }
        if (itemIdentifier.equals("Upload")) {
            item.setLabel(NSBundle.localizedString("Upload", "Toolbar item"));
            item.setPaletteLabel(NSBundle.localizedString("Upload", "Toolbar item"));
            item.setToolTip(NSBundle.localizedString("Upload local file to the remote host", "Toolbar item tooltip"));
            item.setImage(NSImage.imageNamed("uploadFile.tiff"));
            item.setTarget(this);
            item.setAction(new NSSelector("uploadButtonClicked", new Class[]{Object.class}));
            return item;
        }
        if (itemIdentifier.equals("Get Info")) {
            item.setLabel(NSBundle.localizedString("Get Info", "Toolbar item"));
            item.setPaletteLabel(NSBundle.localizedString("Get Info", "Toolbar item"));
            item.setToolTip(NSBundle.localizedString("Show file attributes", "Toolbar item tooltip"));
            item.setImage(NSImage.imageNamed("info.tiff"));
            item.setTarget(this);
            item.setAction(new NSSelector("infoButtonClicked", new Class[]{Object.class}));
            return item;
        }
        if (itemIdentifier.equals("Edit")) {
            item.setLabel(NSBundle.localizedString("Edit", "Toolbar item"));
            item.setPaletteLabel(NSBundle.localizedString("Edit", "Toolbar item"));
            item.setToolTip(NSBundle.localizedString("Edit file in external editor", "Toolbar item tooltip"));
            item.setImage(NSImage.imageNamed("pencil.tiff"));
            NSSelector absolutePathForAppBundleWithIdentifierSelector =
                    new NSSelector("absolutePathForAppBundleWithIdentifier", new Class[]{String.class});
            if (absolutePathForAppBundleWithIdentifierSelector.implementedByClass(NSWorkspace.class)) {
                String editorPath = NSWorkspace.sharedWorkspace().absolutePathForAppBundleWithIdentifier(Preferences.instance().getProperty("editor.bundleIdentifier"));
                if (editorPath != null) {
                    item.setImage(NSWorkspace.sharedWorkspace().iconForFile(editorPath));
                }
            }
            item.setTarget(this);
            item.setAction(new NSSelector("editButtonClicked", new Class[]{Object.class}));
            return item;
        }
        if (itemIdentifier.equals("Delete")) {
            item.setLabel(NSBundle.localizedString("Delete", "Toolbar item"));
            item.setPaletteLabel(NSBundle.localizedString("Delete", "Toolbar item"));
            item.setToolTip(NSBundle.localizedString("Delete file", "Toolbar item tooltip"));
            item.setImage(NSImage.imageNamed("deleteFile.tiff"));
            item.setTarget(this);
            item.setAction(new NSSelector("deleteButtonClicked", new Class[]{Object.class}));
            return item;
        }
        if (itemIdentifier.equals("New Folder")) {
            item.setLabel(NSBundle.localizedString("New Folder", "Toolbar item"));
            item.setPaletteLabel(NSBundle.localizedString("New Folder", "Toolbar item"));
            item.setToolTip(NSBundle.localizedString("Create New Folder", "Toolbar item tooltip"));
            item.setImage(NSImage.imageNamed("newfolder.tiff"));
            item.setTarget(this);
            item.setAction(new NSSelector("folderButtonClicked", new Class[]{Object.class}));
            return item;
        }
        if (itemIdentifier.equals("Disconnect")) {
            item.setLabel(NSBundle.localizedString("Disconnect", "Toolbar item"));
            item.setPaletteLabel(NSBundle.localizedString("Disconnect", "Toolbar item"));
            item.setToolTip(NSBundle.localizedString("Disconnect from server", "Toolbar item tooltip"));
            item.setImage(NSImage.imageNamed("eject.tiff"));
            item.setTarget(this);
            item.setAction(new NSSelector("disconnectButtonClicked", new Class[]{Object.class}));
            return item;
        }
        // itemIdent refered to a toolbar item that is not provide or supported by us or cocoa.
        // Returning null will inform the toolbar this kind of item is not supported.
        return null;
    }


    public NSArray toolbarDefaultItemIdentifiers(NSToolbar toolbar) {
        return new NSArray(new Object[]{
            "New Connection",
            NSToolbarItem.SeparatorItemIdentifier,
            "Bookmarks",
            "Quick Connect",
            "Refresh",
            "Get Info",
            "Edit",
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
            "Edit",
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

    // ----------------------------------------------------------
    // Browser Model
    // ----------------------------------------------------------

    private static final NSImage symlinkIcon = NSImage.imageNamed("symlink.tiff");
    private static final NSImage folderIcon = NSImage.imageNamed("folder16.tiff");
    private static final NSImage notFoundIcon = NSImage.imageNamed("notfound.tiff");

    private static NSMutableParagraphStyle lineBreakByTruncatingMiddleParagraph = new NSMutableParagraphStyle();

    static {
        lineBreakByTruncatingMiddleParagraph.setLineBreakMode(NSParagraphStyle.LineBreakByTruncatingMiddle);
    }

    private static final NSDictionary TABLE_CELL_PARAGRAPH_DICTIONARY = new NSDictionary(new Object[]
    {
        lineBreakByTruncatingMiddleParagraph
    },
            new Object[]{
                NSAttributedString.ParagraphStyleAttributeName
            } //keys
    );

    private class CDBrowserTableDataSource extends CDTableDataSource {
        private List fullData;
        private List currentData;

        public CDBrowserTableDataSource() {
            this.fullData = new ArrayList();
            this.currentData = new ArrayList();
        }

        public int numberOfRowsInTableView(NSTableView tableView) {
            return currentData.size();
        }

        public Object tableViewObjectValueForLocation(NSTableView tableView, NSTableColumn tableColumn, int row) {
            if (row < this.numberOfRowsInTableView(tableView)) {
                String identifier = (String)tableColumn.identifier();
                Path p = (Path)this.currentData.get(row);
                if (identifier.equals("TYPE")) {
                    NSImage icon;
                    if (p.attributes.isSymbolicLink()) {
                        icon = symlinkIcon;
                    }
                    else if (p.attributes.isDirectory()) {
                        icon = folderIcon;
                    }
                    else if (p.attributes.isFile()) {
                        icon = CDIconCache.instance().get(p.getExtension());
                    }
                    else {
                        icon = notFoundIcon;
                    }
                    icon.setSize(new NSSize(16f, 16f));
                    return icon;
                }
                else if (identifier.equals("FILENAME")) {
                    return new NSAttributedString(p.getName(), TABLE_CELL_PARAGRAPH_DICTIONARY);
                }
                else if (identifier.equals("SIZE")) {
                    return new NSAttributedString(Status.getSizeAsString(p.status.getSize()), TABLE_CELL_PARAGRAPH_DICTIONARY);
                }
                else if (identifier.equals("MODIFIED")) {
//					return new NSGregorianDate();
                	return new NSGregorianDate((double)p.attributes.getTimestamp().getTime()/1000,
                								NSDate.DateFor1970);
                }
                else if (identifier.equals("OWNER")) {
                    return new NSAttributedString(p.attributes.getOwner(), TABLE_CELL_PARAGRAPH_DICTIONARY);
                }
                else if (identifier.equals("PERMISSIONS")) {
                    return new NSAttributedString(p.attributes.getPermission().toString(), TABLE_CELL_PARAGRAPH_DICTIONARY);
                }
                throw new IllegalArgumentException("Unknown identifier: " + identifier);
            }
            return null;
        }

        /**
         * The files dragged from the browser to the Finder
         */
        private Path[] promisedDragPaths;

		// ----------------------------------------------------------
		// Drop methods
		// ----------------------------------------------------------
		
        public int tableViewValidateDrop(NSTableView tableView, NSDraggingInfo info, int row, int operation) {
            log.info("tableViewValidateDrop:row:" + row + ",operation:" + operation);
            if (isMounted()) {
                if (info.draggingPasteboard().availableTypeFromArray(new NSArray(NSPasteboard.FilenamesPboardType)) != null) {
                    if (row != -1 && row < tableView.numberOfRows()) {
                        Path selected = this.getEntry(row);
                        if (selected.attributes.isDirectory()) {
                            tableView.setDropRowAndDropOperation(row, NSTableView.DropOn);
                            return NSDraggingInfo.DragOperationCopy;
                        }
                    }
                    tableView.setDropRowAndDropOperation(-1, NSTableView.DropOn);
                    return NSDraggingInfo.DragOperationCopy;
                }
                NSPasteboard pboard = NSPasteboard.pasteboardWithName("QueuePBoard");
                if (pboard.availableTypeFromArray(new NSArray("QueuePBoardType")) != null) {
                    if (row != -1 && row < tableView.numberOfRows()) {
                        Path selected = this.getEntry(row);
                        if (selected.attributes.isDirectory()) {
                            tableView.setDropRowAndDropOperation(row, NSTableView.DropOn);
                            return NSDraggingInfo.DragOperationMove;
                        }
                    }
                }
            }
            return NSDraggingInfo.DragOperationNone;
        }

        public boolean tableViewAcceptDrop(NSTableView tableView, NSDraggingInfo info, int row, int operation) {
            log.debug("tableViewAcceptDrop:row:" + row + ",operation:" + operation);
            NSPasteboard infoPboard = info.draggingPasteboard();
            if (infoPboard.availableTypeFromArray(new NSArray(NSPasteboard.FilenamesPboardType)) != null) {
                NSArray filesList = (NSArray)infoPboard.propertyListForType(NSPasteboard.FilenamesPboardType);
				Queue q = new Queue(Queue.KIND_UPLOAD);
				Session session = pathController.workdir().getSession().copy();
				for (int i = 0; i < filesList.count(); i++) {
					log.debug(filesList.objectAtIndex(i));
					Path p = null;
					if (row != -1) {
						p = PathFactory.createPath(session,
												   this.getEntry(row).getAbsolute(),
												   new Local((String)filesList.objectAtIndex(i)));
					}
					else {
						p = PathFactory.createPath(session,
												   pathController.workdir().getAbsolute(),
												   new Local((String)filesList.objectAtIndex(i)));
					}
					q.addRoot(p);
				}
				if (q.numberOfRoots() > 0) {
					QueueList.instance().addItem(q);
					CDQueueController.instance().startItem(q, (Observer)CDBrowserController.this);
				}
				return true;
            }
            else if (row != -1 && row < tableView.numberOfRows()) {
                NSPasteboard pboard = NSPasteboard.pasteboardWithName("QueuePBoard");
                log.debug("availableTypeFromArray:QueuePBoardType: " + pboard.availableTypeFromArray(new NSArray("QueuePBoardType")));
                if (pboard.availableTypeFromArray(new NSArray("QueuePBoardType")) != null) {
                    NSArray elements = (NSArray)pboard.propertyListForType("QueuePBoardType");// get the data from pasteboard
					for (int i = 0; i < elements.count(); i++) {
						NSDictionary dict = (NSDictionary)elements.objectAtIndex(i);
						Path parent = this.getEntry(row);
						if (parent.attributes.isDirectory()) {
							Queue q = new Queue(dict);
							List files = q.getRoots();
							for (Iterator iter = files.iterator(); iter.hasNext();) {
								Path p = (Path)iter.next();
								PathFactory.createPath(parent.getSession(), p.getAbsolute()).rename(parent.getAbsolute() + "/" + p.getName());
							}
							tableView.deselectAll(null);
							pathController.workdir().list(true);
							return true;
						}
					}
				}
            }
            return false;
        }


        // ----------------------------------------------------------
        // Drag methods
        // ----------------------------------------------------------
		
        /**
         * Invoked by tableView after it has been determined that a drag should begin, but before the drag has been started.
         * The drag image and other drag-related information will be set up and provided by the table view once this call
         * returns with true.
         *
         * @param rows is the list of row numbers that will be participating in the drag.
         * @return To refuse the drag, return false. To start a drag, return true and place the drag data onto pboard
         *         (data, owner, and so on).
         */
        public boolean tableViewWriteRowsToPasteboard(NSTableView tableView, NSArray rows, NSPasteboard pboard) {
            log.debug("tableViewWriteRowsToPasteboard:" + rows);
            if (rows.count() > 0) {
                this.promisedDragPaths = new Path[rows.count()];
				// The fileTypes argument is the list of fileTypes being promised. The array elements can consist of file extensions and HFS types encoded with the NSHFSFileTypes method fileTypeForHFSTypeCode. If promising a directory of files, only include the top directory in the array.
                NSMutableArray fileTypes = new NSMutableArray();
                NSMutableArray queueDictionaries = new NSMutableArray();
				// declare our dragged type in the paste board
                pboard.declareTypes(new NSArray(NSPasteboard.FilesPromisePboardType), null);
                Queue q = new Queue(Queue.KIND_DOWNLOAD);
                Session session = pathController.workdir().getSession().copy();
                for (int i = 0; i < rows.count(); i++) {
                    promisedDragPaths[i] = (Path)this.getEntry(((Integer)rows.objectAtIndex(i)).intValue()).copy(session);
                    if (promisedDragPaths[i].attributes.isFile()) {
						// fileTypes.addObject(NSPathUtilities.FileTypeRegular);
                        if (promisedDragPaths[i].getExtension() != null) {
                            fileTypes.addObject(promisedDragPaths[i].getExtension());
                        }
                        else {
                            fileTypes.addObject(NSPathUtilities.FileTypeUnknown);
                        }
                    }
                    else if (promisedDragPaths[i].attributes.isDirectory()) {
						// fileTypes.addObject(NSPathUtilities.FileTypeDirectory);
                        fileTypes.addObject("'fldr'");
                    }
                    else {
                        fileTypes.addObject(NSPathUtilities.FileTypeUnknown);
                    }
                    q.addRoot(promisedDragPaths[i]);
                }
                queueDictionaries.addObject(q.getAsDictionary());
				// Writing data for private use when the item gets dragged to the transfer queue.
                NSPasteboard queuePboard = NSPasteboard.pasteboardWithName("QueuePBoard");
                queuePboard.declareTypes(new NSArray("QueuePBoardType"), null);
                if (queuePboard.setPropertyListForType(queueDictionaries, "QueuePBoardType")) {
                    log.debug("QueuePBoardType data sucessfully written to pasteboard");
                }
				
                NSEvent event = NSApplication.sharedApplication().currentEvent();
                NSPoint dragPosition = tableView.convertPointFromView(event.locationInWindow(), null);
                NSRect imageRect = new NSRect(new NSPoint(dragPosition.x() - 16, dragPosition.y() - 16), new NSSize(32, 32));
				
                tableView.dragPromisedFilesOfTypes(fileTypes, imageRect, this, true, event);
            }
			// we return false because we don't want the table to draw the drag image
            return false;
        }
		
        /**
			* @return the names (not full paths) of the files that the receiver promises to create at dropDestination.
         *         This method is invoked when the drop has been accepted by the destination and the destination, in the case of another
         *         Cocoa application, invokes the NSDraggingInfo method namesOfPromisedFilesDroppedAtDestination. For long operations,
         *         you can cache dropDestination and defer the creation of the files until the finishedDraggingImage method to avoid
         *         blocking the destination application.
         */
        public NSArray namesOfPromisedFilesDroppedAtDestination(java.net.URL dropDestination) {
            log.debug("namesOfPromisedFilesDroppedAtDestination:" + dropDestination);
			NSMutableArray promisedDragNames = new NSMutableArray();
			if(null != dropDestination) {
                Queue q = new Queue(Queue.KIND_DOWNLOAD);
                for (int i = 0; i < promisedDragPaths.length; i++) {
                    try {
                        //@todo check if the returned path is the trash
                        this.promisedDragPaths[i].setLocal(new Local(java.net.URLDecoder.decode(dropDestination.getPath(), "UTF-8"),
																	 this.promisedDragPaths[i].getName()));
                        q.addRoot(this.promisedDragPaths[i]);
                        promisedDragNames.addObject(this.promisedDragPaths[i].getName());
                    }
                    catch (java.io.UnsupportedEncodingException e) {
                        log.error(e.getMessage());
                    }
                }
                if (q.numberOfRoots() > 0) {
                    QueueList.instance().addItem(q);
                    CDQueueController.instance().startItem(q);
                }
//                this.promisedDragPaths = null;
            }
			return promisedDragNames;
        }

// ----------------------------------------------------------
// Delegate methods
// ----------------------------------------------------------

        public boolean isSortedAscending() {
            return this.sortAscending;
        }

        public NSTableColumn selectedColumn() {
            return this.selectedColumn;
        }

        private boolean sortAscending = true;
        private NSTableColumn selectedColumn = null;

        public void sort(NSTableColumn tableColumn, final boolean ascending) {
            final int higher = ascending ? 1 : -1;
            final int lower = ascending ? -1 : 1;
            if (tableColumn.identifier().equals("TYPE")) {
                Collections.sort(this.values(),
                        new Comparator() {
                            public int compare(Object o1, Object o2) {
                                Path p1 = (Path)o1;
                                Path p2 = (Path)o2;
                                if (p1.attributes.isDirectory() && p2.attributes.isDirectory()) {
                                    return 0;
                                }
                                if (p1.attributes.isFile() && p2.attributes.isFile()) {
                                    return 0;
                                }
                                if (p1.attributes.isFile()) {
                                    return higher;
                                }
                                return lower;
                            }
                        });
            }
            else if (tableColumn.identifier().equals("FILENAME")) {
                Collections.sort(this.values(),
                        new Comparator() {
                            public int compare(Object o1, Object o2) {
                                Path p1 = (Path)o1;
                                Path p2 = (Path)o2;
                                if (ascending) {
                                    return p1.getName().compareToIgnoreCase(p2.getName());
                                }
                                else {
                                    return -p1.getName().compareToIgnoreCase(p2.getName());
                                }
                            }
                        });
            }
            else if (tableColumn.identifier().equals("SIZE")) {
                Collections.sort(this.values(),
                        new Comparator() {
                            public int compare(Object o1, Object o2) {
                                long p1 = ((Path)o1).status.getSize();
                                long p2 = ((Path)o2).status.getSize();
                                if (p1 > p2) {
                                    return higher;
                                }
                                else if (p1 < p2) {
                                    return lower;
                                }
                                else {
                                    return 0;
                                }
                            }
                        });
            }
            else if (tableColumn.identifier().equals("MODIFIED")) {
                Collections.sort(this.values(),
                        new Comparator() {
                            public int compare(Object o1, Object o2) {
                                Path p1 = (Path)o1;
                                Path p2 = (Path)o2;
                                if (ascending) {
                                    return p1.attributes.getTimestamp().compareTo(p2.attributes.getTimestamp());
                                }
                                else {
                                    return -p1.attributes.getTimestamp().compareTo(p2.attributes.getTimestamp());
                                }
                            }
                        });
            }
            else if (tableColumn.identifier().equals("OWNER")) {
                Collections.sort(this.values(),
                        new Comparator() {
                            public int compare(Object o1, Object o2) {
                                Path p1 = (Path)o1;
                                Path p2 = (Path)o2;
                                if (ascending) {
                                    return p1.attributes.getOwner().compareToIgnoreCase(p2.attributes.getOwner());
                                }
                                else {
                                    return -p1.attributes.getOwner().compareToIgnoreCase(p2.attributes.getOwner());
                                }
                            }
                        });
            }
        }

        public void tableViewDidClickTableColumn(NSTableView tableView, NSTableColumn tableColumn) {
            log.debug("tableViewDidClickTableColumn");
            if (this.selectedColumn == tableColumn) {
                this.sortAscending = !this.sortAscending;
            }
            else {
                if (selectedColumn != null) {
                    tableView.setIndicatorImage(null, selectedColumn);
                }
                this.selectedColumn = tableColumn;
            }
            tableView.setIndicatorImage(this.sortAscending ? NSImage.imageNamed("NSAscendingSortIndicator") : NSImage.imageNamed("NSDescendingSortIndicator"), tableColumn);
            this.sort(tableColumn, sortAscending);
            tableView.reloadData();
        }

        public boolean tableViewShouldEditLocation(NSTableView view, NSTableColumn tableColumn, int row) {
            //          log.debug("tableViewShouldEditLocation:" + row);
            //            if (tableColumn.identifier().equals("FILENAME")) {
            //                return true;
            //            }
            return false;
        }

        // ----------------------------------------------------------
        // Data access
        // ----------------------------------------------------------

        public void clear() {
            this.fullData.clear();
            this.currentData.clear();
        }

        public void setData(List data) {
            this.fullData = data;
            this.currentData = data;
        }

        public Path getEntry(int row) {
            if (row < currentData.size()) {
                return (Path)this.currentData.get(row);
            }
            return null;
        }

        public void removeEntry(Path o) {
            int frow = fullData.indexOf(o);
            if (frow < fullData.size()) {
                fullData.remove(frow);
            }
            int crow = currentData.indexOf(o);
            if (crow < currentData.size()) {
                currentData.remove(crow);
            }
        }

        public int indexOf(Path o) {
            return currentData.indexOf(o);
        }

        public void setActiveSet(List currentData) {
            this.currentData = currentData;
        }

        public List values() {
            return this.fullData;
        }
    }
}