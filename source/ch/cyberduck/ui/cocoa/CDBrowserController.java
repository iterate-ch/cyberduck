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
public class CDBrowserController extends NSDocument implements Observer { //@todo
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
        Object delegate = window.delegate();
        if (delegate != null && delegate instanceof CDBrowserController) {
            return (CDBrowserController) delegate;
        }
        else {
            return null;
        }
    }
	
//	public boolean isDocumentEdited() {
//		log.debug("isDocumentEdited");
//		return this.isConnected();
//	}

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
        (NSNotificationCenter.defaultCenter()).addObserver(this,
                new NSSelector("browserTableRowEdited", new Class[]{NSNotification.class}),
                NSText.TextDidEndEditingNotification,
                this.browserTable);
    }

    public void browserTableRowDoubleClicked(Object sender) {
        log.debug("browserTableRowDoubleClicked");
        searchField.setStringValue("");
        if (browserModel.size() > 0 && browserTable.numberOfSelectedRows() > 0) {
            Path p = (Path) browserModel.getEntry(browserTable.selectedRow()); //last row selected
            if (p.isFile() || browserTable.numberOfSelectedRows() > 1) {
                this.downloadButtonClicked(sender);
            }
            if (p.isDirectory()) {
                p.list();
            }
        }
    }

    public void browserTableRowEdited(Object sender) {
        log.debug("browserTableRowEdited");
    }

    private CDBookmarkTableDataSource bookmarkModel;
    private NSTableView bookmarkTable; // IBOutlet

    public void setBookmarkTable(NSTableView bookmarkTable) {
        log.debug("setBookmarkTable");
        this.bookmarkTable = bookmarkTable;
        this.bookmarkTable.setTarget(this);
        this.bookmarkTable.setDoubleAction(new NSSelector("bookmarkTableRowDoubleClicked", new Class[]{Object.class}));
        this.bookmarkTable.setDataSource(this.bookmarkModel = new CDBookmarkTableDataSource());
        this.bookmarkTable.setDelegate(this.bookmarkModel);
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
            Host host = (Host) BookmarkList.instance().getItem(bookmarkTable.selectedRow());
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
        String input = ((NSControl) sender).stringValue();
        for (Iterator iter = BookmarkList.instance().iterator(); iter.hasNext();) {
            Host h = (Host) iter.next();
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
                        /*
                        try {
                            Perl5Matcher matcher = new Perl5Matcher();
                            Pattern pattern = new Perl5Compiler().compile(searchString);
                            if (matcher.matches(next.getName().toLowerCase(), pattern)) {
                                subset.add(next);
                            }
                        }
                        catch (MalformedPatternException e) {
                            log.error("Unparseable filter string supplied:" + searchString);
                        }
                         */
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

    private static int OFFSET = 0;

    // ----------------------------------------------------------
    // Constructor
    // ----------------------------------------------------------

    public CDBrowserController() {
        instances.addObject(this);
        OFFSET = +16;
        if (false == NSApplication.loadNibNamed("Browser", this)) {
            log.fatal("Couldn't load Browser.nib");
        }
        log.debug("offset:" + OFFSET);
    }

    public void awakeFromNib() {
        NSPoint origin = this.window.frame().origin();
        this.window.setTitle("Cyberduck " + NSBundle.bundleForClass(this.getClass()).objectForInfoDictionaryKey("CFBundleVersion"));
        this.window.setFrameOrigin(new NSPoint(origin.x() + OFFSET, origin.y() - OFFSET));
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


    public void update(Observable o, Object arg) {
        log.debug("update:" + o + "," + arg);
        if (arg instanceof Path) {
            browserModel.setData(((Path) arg).cache());
            NSTableColumn selectedColumn = browserModel.selectedColumn() != null ? browserModel.selectedColumn() : browserTable.tableColumnWithIdentifier("FILENAME");
            browserTable.setIndicatorImage(browserModel.isSortedAscending() ? NSImage.imageNamed("NSAscendingSortIndicator") : NSImage.imageNamed("NSDescendingSortIndicator"), selectedColumn);
            browserModel.sort(selectedColumn, browserModel.isSortedAscending());
            browserTable.reloadData();
            this.toolbar.validateVisibleItems();
			this.window.makeFirstResponder(browserTable);
        }
        else if (arg instanceof Message) {
            Message msg = (Message) arg;
            if (msg.getTitle().equals(Message.ERROR)) {
                if (this.window().isVisible()) {
                    NSAlertPanel.beginCriticalAlertSheet(NSBundle.localizedString("Error", "Alert sheet title"), //title
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
                }
                this.progressIndicator.stopAnimation(this);
                this.statusIcon.setImage(NSImage.imageNamed("alert.tiff"));
                this.statusLabel.setObjectValue(msg.getContent());
            }
            else if (msg.getTitle().equals(Message.REFRESH)) {
                this.refreshButtonClicked(null);
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
//                CDHistoryImpl.instance().addItem(((Session) o).host);
//					this.statusIcon.setImage(NSImage.imageNamed("online.tiff"));
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
    }

    // ----------------------------------------------------------
    // Selector methods for the toolbar items
    // ----------------------------------------------------------

    public void editButtonClicked(Object sender) {
        NSEnumerator enum = browserTable.selectedRowEnumerator();
        while (enum.hasMoreElements()) {
            int selected = ((Integer) enum.nextElement()).intValue();
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
                new NSSelector("newfolderSheetDidEnd",
                        new Class[]{NSPanel.class, int.class, Object.class}), // did end selector
                pathController.workdir()); //contextInfo
    }


    public void infoButtonClicked(Object sender) {
        log.debug("infoButtonClicked");
        NSEnumerator enum = browserTable.selectedRowEnumerator();
        while (enum.hasMoreElements()) {
            int selected = ((Integer) enum.nextElement()).intValue();
            Path path = browserModel.getEntry(selected);
            CDInfoController controller = new CDInfoController(path);
            controller.window().makeKeyAndOrderFront(null);
        }
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
                Vector files = (Vector) contextInfo;
                if (files.size() > 0) {
                    Iterator i = files.iterator();
                    Path p = null;
                    while (i.hasNext()) {
                        p = (Path) i.next();
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
        pathController.workdir().list(true);
    }

    public void downloadAsButtonClicked(Object sender) {
        if (browserModel.size() > 0 && browserTable.numberOfSelectedRows() > 0) {
            if (this.isMounted()) {
                NSEnumerator enum = browserTable.selectedRowEnumerator();
                while (enum.hasMoreElements()) {
                    Session session = pathController.workdir().getSession().copy();
                    Path path = ((Path) browserModel.getEntry(((Integer) enum.nextElement()).intValue())).copy(session);
                    NSSavePanel panel = NSSavePanel.savePanel();
                    panel.setMessage(NSBundle.localizedString("Download the selected file to...", ""));
                    panel.setNameFieldLabel(NSBundle.localizedString("Download As:", ""));
                    panel.setPrompt(NSBundle.localizedString("Download", ""));
                    panel.setTitle("Download");
                    panel.setCanCreateDirectories(true);
                    panel.beginSheetForDirectory(System.getProperty("user.home"),
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
                        Path path = (Path) contextInfo;
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
        if (browserModel.size() > 0 && browserTable.numberOfSelectedRows() > 0) {
            if (this.isMounted()) {
                NSEnumerator enum = browserTable.selectedRowEnumerator();
                Queue q = new Queue(Queue.KIND_DOWNLOAD);
                Session session = pathController.workdir().getSession().copy();
                while (enum.hasMoreElements()) {
                    Path path = ((Path) browserModel.getEntry(((Integer) enum.nextElement()).intValue())).copy(session);
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
        panel.beginSheetForDirectory(System.getProperty("user.home"), null, null, this.window(), this, new NSSelector("uploadPanelDidEnd", new Class[]{NSOpenPanel.class, int.class, Object.class}), null);
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
                    item.setPath(parent.getAbsolute(), new Local((String) enumerator.nextElement()));
                    //                    Queue queue = new Queue(item, Queue.KIND_UPLOAD);
                    q.addRoot(item);
                }
                QueueList.instance().addItem(q);
                CDQueueController.instance().startItem(q, (Observer) this);
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
        return pathController.workdir() != null;
    }

    public boolean isConnected() {
        if (this.isMounted()) {
            return pathController.workdir().getSession().isConnected();
        }
        return false;
    }

    public void mount(Host host) {
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
            session.addObserver((Observer) this);
            session.addObserver((Observer) pathController);

            progressIndicator.startAnimation(this);

            if (session instanceof ch.cyberduck.core.sftp.SFTPSession) {
                try {
                    host.setHostKeyVerificationController(new CDHostKeyController(this.window()));
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
            host.getLogin().setController(new CDLoginController(this.window()));
            session.mount();
        }
    }

    public void mountSheetDidEnd(NSWindow sheet, int returncode, Object contextInfo) {
        this.unmountSheetDidEnd(sheet, returncode, contextInfo);
        if (returncode == NSAlertPanel.DefaultReturn) {
            this.mount((Host) contextInfo);
        }
    }

    /**
     * @return True if the unmount process has finished, false if the user has to agree first
     */
    public boolean unmount(NSSelector selector, Host context) {
        log.debug("unmount");
        if (this.isConnected()) {
            NSAlertPanel.beginCriticalAlertSheet(NSBundle.localizedString("Disconnect from", "Alert sheet title") + " " + pathController.workdir().getSession().getHost().getHostname(), //title
                    NSBundle.localizedString("Disconnect", "Alert sheet default button"), // defaultbutton
                    NSBundle.localizedString("Cancel", "Alert sheet alternate button"), //alternative button
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

    public void unmountSheetDidEnd(NSWindow sheet, int returncode, Object contextInfo) {
        sheet.orderOut(null);
        if (returncode == NSAlertPanel.DefaultReturn) {
            pathController.workdir().getSession().close();
        }
    }

    // ----------------------------------------------------------
    // Window delegate methods
    // ----------------------------------------------------------

    public boolean windowShouldClose(NSWindow sender) {
        return this.unmount(new NSSelector("closeSheetDidEnd",
                new Class[]{NSWindow.class, int.class, Object.class}), null // end selector
        );
    }

    public void closeSheetDidEnd(NSWindow sheet, int returncode, Object contextInfo) {
        this.unmountSheetDidEnd(sheet, returncode, contextInfo);
        if (returncode == NSAlertPanel.DefaultReturn) {
            this.window().close();
        }
    }

    public void windowWillClose(NSNotification notification) {
        log.debug("windowWillClose");
        OFFSET = -16;
        if (this.isMounted()) {
            pathController.workdir().getSession().deleteObserver((Observer) this);
            pathController.workdir().getSession().deleteObserver((Observer) pathController);
        }
        NSNotificationCenter.defaultCenter().removeObserver(this);
        instances.removeObject(this);
        this.bookmarkDrawer.close();
        this.logDrawer.close();
    }

    public boolean validateMenuItem(_NSObsoleteMenuItemProtocol cell) {
        boolean v = this.validateItem(cell.action().name());
		log.debug("validateMenuItem:"+cell.action().name()+"->"+v);
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
            if (this.isMounted() && browserModel.size() > 0 && browserTable.selectedRow() != -1) {
                Path p = (Path) browserModel.getEntry(browserTable.selectedRow());
                return p.attributes.isFile();
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
        backButton.setEnabled(pathController.numberOfItems() > 0);
        upButton.setEnabled(pathController.numberOfItems() > 0);
        pathPopup.setEnabled(pathController.numberOfItems() > 0);

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
        else if (itemIdentifier.equals("Edit")) {
            item.setLabel(NSBundle.localizedString("Edit", "Toolbar item"));
            item.setPaletteLabel(NSBundle.localizedString("Edit", "Toolbar item"));
            item.setToolTip(NSBundle.localizedString("Edit file in external editor", "Toolbar item tooltip"));
            NSSelector absolutePathForAppBundleWithIdentifierSelector =
                    new NSSelector("absolutePathForAppBundleWithIdentifier", new Class[]{String.class});
            if (absolutePathForAppBundleWithIdentifierSelector.implementedByClass(NSWorkspace.class)) {
                item.setImage(NSWorkspace.sharedWorkspace().iconForFile(NSWorkspace.sharedWorkspace().absolutePathForAppBundleWithIdentifier(Preferences.instance().getProperty("editor.bundleIdentifier"))));
            }
            else {
                item.setImage(NSImage.imageNamed("pencil.tiff"));
            }
            item.setTarget(this);
            item.setAction(new NSSelector("editButtonClicked", new Class[]{Object.class}));
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
            item.setImage(NSImage.imageNamed("newfolder.tiff"));
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
                String identifier = (String) tableColumn.identifier();
                Path p = (Path) this.currentData.get(row);
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
                    return p.getName();
                }
                else if (identifier.equals("SIZE")) {
                    return Status.getSizeAsString(p.status.getSize());
                }
                else if (identifier.equals("MODIFIED")) {
                    return p.attributes.getTimestampAsString();
                }
                else if (identifier.equals("OWNER")) {
                    return p.attributes.getOwner();
                }
                else if (identifier.equals("PERMISSIONS")) {
                    return p.attributes.getPermission().toString();
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
                        if (selected.isDirectory()) {
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
                        if (selected.isDirectory()) {
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
                Object o = infoPboard.propertyListForType(NSPasteboard.FilenamesPboardType);// get the data from paste board
                log.debug("tableViewAcceptDrop:" + o);
                if (o != null) {
                    if (o instanceof NSArray) {
                        NSArray filesList = (NSArray) o;
                        Queue q = new Queue(Queue.KIND_UPLOAD);
                        Session session = pathController.workdir().getSession().copy();
                        for (int i = 0; i < filesList.count(); i++) {
                            log.debug(filesList.objectAtIndex(i));
                            Path p = null;
                            if (row != -1) {
                                p = PathFactory.createPath(session,
                                        this.getEntry(row).getAbsolute(),
                                        new Local((String) filesList.objectAtIndex(i)));
                            }
                            else {
                                p = PathFactory.createPath(session,
                                        pathController.workdir().getAbsolute(),
                                        new Local((String) filesList.objectAtIndex(i)));
                            }
                            q.addRoot(p);
                        }
                        if (q.numberOfRoots() > 0) {
                            QueueList.instance().addItem(q);
                            CDQueueController.instance().startItem(q, (Observer) CDBrowserController.this);
                        }
                        return true;
                    }
                    return false;
                }
                return false;
            }
            if (row != -1 && row < tableView.numberOfRows()) {
                NSPasteboard pboard = NSPasteboard.pasteboardWithName("QueuePBoard");
                log.debug("availableTypeFromArray:QueuePBoardType: " + pboard.availableTypeFromArray(new NSArray("QueuePBoardType")));
                if (pboard.availableTypeFromArray(new NSArray("QueuePBoardType")) != null) {
                    Object o = pboard.propertyListForType("QueuePBoardType");// get the data from paste board
                    log.debug("tableViewAcceptDrop:" + o);
                    if (o != null) {
                        NSArray elements = (NSArray) o;
                        for (int i = 0; i < elements.count(); i++) {
                            NSDictionary dict = (NSDictionary) elements.objectAtIndex(i);
                            Path parent = this.getEntry(row);
                            if (parent.isDirectory()) {
                                Queue q = new Queue(dict);
                                List files = q.getRoots();
                                for (Iterator iter = files.iterator(); iter.hasNext();) {
                                    Path p = (Path) iter.next();
                                    p.rename(parent.getAbsolute(), p.getName());
                                }
                            }
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
                    promisedDragPaths[i] = (Path) this.getEntry(((Integer) rows.objectAtIndex(i)).intValue()).copy(session);
                    if (promisedDragPaths[i].isFile()) {
//					fileTypes.addObject(NSPathUtilities.FileTypeRegular);
                        if (promisedDragPaths[i].getExtension() != null) {
                            fileTypes.addObject(promisedDragPaths[i].getExtension());
                        }
                        else {
                            fileTypes.addObject(NSPathUtilities.FileTypeUnknown);
                        }
                    }
                    else if (promisedDragPaths[i].isDirectory()) {
//					fileTypes.addObject(NSPathUtilities.FileTypeDirectory);
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
                else {
                    log.error("Could not write QueuePBoardType data to pasteboard");
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
            if (null == dropDestination) {
                return null; //return paths for interapplication communication
            }
            else {
                NSMutableArray promisedDragNames = new NSMutableArray();
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
                this.promisedDragPaths = null;
                return promisedDragNames;
            }
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
                                Path p1 = (Path) o1;
                                Path p2 = (Path) o2;
                                if (p1.isDirectory() && p2.isDirectory()) {
                                    return 0;
                                }
                                if (p1.isFile() && p2.isFile()) {
                                    return 0;
                                }
                                if (p1.isFile()) {
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
                                Path p1 = (Path) o1;
                                Path p2 = (Path) o2;
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
                                long p1 = ((Path) o1).status.getSize();
                                long p2 = ((Path) o2).status.getSize();
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
                                Path p1 = (Path) o1;
                                Path p2 = (Path) o2;
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
                                Path p1 = (Path) o1;
                                Path p2 = (Path) o2;
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
            log.debug("tableViewShouldEditLocation:" + row);
            if (tableColumn.identifier().equals("FILENAME")) {
                return true;
            }
            return false;
        }

// ----------------------------------------------------------
// Data access
// ----------------------------------------------------------

        public int size() {
            return this.currentData.size();
        }

        public void clear() {
            this.fullData.clear();
            this.currentData.clear();
        }

        public void setData(List data) {
            this.fullData = data;
            this.currentData = data;
        }

        public Path getEntry(int row) {
            return (Path) this.currentData.get(row);
        }

        public void removeEntry(Path o) {
            fullData.remove(fullData.indexOf(o));
            currentData.remove(currentData.indexOf(o));
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