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

import com.enterprisedt.net.ftp.FTPConnectMode;

import ch.cyberduck.core.*;
import ch.cyberduck.core.ftps.FTPSSession;
import ch.cyberduck.ui.cocoa.delegate.EditMenuDelegate;
import ch.cyberduck.ui.cocoa.delegate.HistoryMenuDelegate;
import ch.cyberduck.ui.cocoa.growl.Growl;
import ch.cyberduck.ui.cocoa.odb.Editor;
import ch.cyberduck.ui.cocoa.threading.BackgroundAction;
import ch.cyberduck.ui.cocoa.threading.BackgroundActionImpl;

import com.apple.cocoa.application.*;
import com.apple.cocoa.foundation.*;

import org.apache.log4j.Logger;

import java.io.File;
import java.io.IOException;
import java.security.cert.CertificateExpiredException;
import java.security.cert.CertificateNotYetValidException;
import java.security.cert.X509Certificate;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * @version $Id$
 */
public class CDBrowserController extends CDWindowController
        implements NSToolbarItem.ItemValidation {
    private static Logger log = Logger.getLogger(CDBrowserController.class);

    /**
     * Applescriptability
     *
     * @return The NSIndexSpecifier for all browsers or null if there is none
     */
    public NSScriptObjectSpecifier objectSpecifier() {
        log.debug("objectSpecifier");
        NSArray orderedDocs = (NSArray) NSKeyValue.valueForKey(NSApplication.sharedApplication(), "orderedBrowsers");
        int index = orderedDocs.indexOfObject(this);
        if(index >= 0 && index < orderedDocs.count()) {
            NSScriptClassDescription desc
                    = (NSScriptClassDescription) NSScriptClassDescription.classDescriptionForClass(NSApplication.class);
            return new NSIndexSpecifier(desc, null, "orderedBrowsers", index);
        }
        return null;
    }

    public String getWorkingDirectory() {
        if(this.isMounted()) {
            return this.workdir().getAbsolute();
        }
        return null;
    }

    public Object handleMountScriptCommand(NSScriptCommand command) {
        log.debug("handleMountScriptCommand:" + command);
        NSDictionary args = command.evaluatedArguments();
        Object portObj = args.objectForKey("Port");
        Host host;
        Object bookmarkObj = args.objectForKey("Bookmark");
        if(bookmarkObj != null) {
            HostCollection bookmarks = HostCollection.instance();
            int index = bookmarks.indexOf(bookmarkObj);
            if(index < 0) {
                return null;
            }
            host = (Host) bookmarks.get(index);
        }
        else {
            if(portObj != null) {
                Object protocolObj = args.objectForKey("Protocol");
                if(protocolObj != null) {
                    host = new Host((String) args.objectForKey("Protocol"),
                            (String) args.objectForKey("Host"),
                            Integer.parseInt((String) args.objectForKey("Port")));
                }
                else {
                    host = new Host((String) args.objectForKey("Host"),
                            Integer.parseInt((String) args.objectForKey("Port")));
                }
            }
            else {
                Object protocolObj = args.objectForKey("Protocol");
                if(protocolObj != null) {
                    host = new Host((String) args.objectForKey("Protocol"),
                            (String) args.objectForKey("Host"));
                }
                else {
                    host = new Host((String) args.objectForKey("Host"));
                }
            }
            Object pathObj = args.objectForKey("InitialPath");
            if(pathObj != null) {
                host.setDefaultPath((String) args.objectForKey("InitialPath"));
            }
            Object userObj = args.objectForKey("Username");
            if(userObj != null) {
                host.setCredentials((String) args.objectForKey("Username"), (String) args.objectForKey("Password"));
            }
            Object modeObj = args.objectForKey("Mode");
            if(modeObj != null) {
                if(modeObj.equals(FTPConnectMode.ACTIVE.toString()))
                    host.setFTPConnectMode(FTPConnectMode.ACTIVE);
                if(modeObj.equals(FTPConnectMode.PASV.toString()))
                    host.setFTPConnectMode(FTPConnectMode.PASV);
            }
        }
        Session session = this.init(host);
        this.setWorkdir(session.mount());
        return null;
    }

    public Object handleCloseScriptCommand(NSScriptCommand command) {
        log.debug("handleCloseScriptCommand:" + command);
        this.handleDisconnectScriptCommand(command);
        this.window.close();
        return null;
    }

    public Object handleDisconnectScriptCommand(NSScriptCommand command) {
        log.debug("handleDisconnectScriptCommand:" + command);
        this.disconnectButtonClicked(null);
        return null;
    }

    public NSArray handleListScriptCommand(NSScriptCommand command) {
        log.debug("handleListScriptCommand:" + command);
        NSMutableArray result = new NSMutableArray();
        if(this.isMounted()) {
            NSDictionary args = command.evaluatedArguments();
            Object pathObj = args.objectForKey("Path");
            Path path = this.workdir();
            if(pathObj != null) {
                String folder = (String) args.objectForKey("Path");
                if(folder.charAt(0) == '/') {
                    path = PathFactory.createPath(this.session,
                            folder);
                }
                else {
                    path = PathFactory.createPath(this.session,
                            this.workdir().getAbsolute(),
                            folder);
                }
            }
            path.attributes.setType(Path.DIRECTORY_TYPE);
            for(Iterator i = path.childs().iterator(); i.hasNext();) {
                result.addObject(((Path) i.next()).getName());
            }
        }
        return result;
    }

    public Object handleGotoScriptCommand(NSScriptCommand command) {
        log.debug("handleGotoScriptCommand:" + command);
        if(this.isMounted()) {
            NSDictionary args = command.evaluatedArguments();
            Path path = PathFactory.createPath(this.session,
                    this.workdir().getAbsolute(),
                    (String) args.objectForKey("Path"));
            this.setWorkdir(path);
        }
        return null;
    }

    public Object handleMoveScriptCommand(NSScriptCommand command) {
        log.debug("handleMoveScriptCommand:" + command);
        if(this.isMounted()) {
            NSDictionary args = command.evaluatedArguments();
            String from = (String) args.objectForKey("From");
            if(!from.startsWith(Path.DELIMITER)) {
                from = this.workdir().getAbsolute() + Path.DELIMITER + from;
            }
            String to = (String) args.objectForKey("To");
            if(!to.startsWith(Path.DELIMITER)) {
                to = this.workdir().getAbsolute() + Path.DELIMITER + to;
            }
            this.renamePath(PathFactory.createPath(session, from), PathFactory.createPath(session, to));
        }
        return null;
    }

    public Object handleCreateFolderScriptCommand(NSScriptCommand command) {
        log.debug("handleCreateFolderScriptCommand:" + command);
        if(this.isMounted()) {
            NSDictionary args = command.evaluatedArguments();
            CDFolderController c = new CDFolderController(this);
            c.createFolder(this.workdir(), (String) args.objectForKey("Path"));
        }
        return null;
    }

    public Object handleExistsScriptCommand(NSScriptCommand command) {
        log.debug("handleExistsScriptCommand:" + command);
        if(this.isMounted()) {
            NSDictionary args = command.evaluatedArguments();
            Path path = PathFactory.createPath(this.session,
                    this.workdir().getAbsolute(),
                    (String) args.objectForKey("Path"));
            return new Integer(path.exists() ? 1 : 0);
        }
        return new Integer(0);
    }

    public Object handleCreateFileScriptCommand(NSScriptCommand command) {
        log.debug("handleCreateFileScriptCommand:" + command);
        if(this.isMounted()) {
            NSDictionary args = command.evaluatedArguments();
            CDCreateFileController c = new CDCreateFileController(this);
            c.createFile(this.workdir(), (String) args.objectForKey("Path"), false);
        }
        return null;
    }

    public Object handleEditScriptCommand(NSScriptCommand command) {
        log.debug("handleEditScriptCommand:" + command);
        if(this.isMounted()) {
            NSDictionary args = command.evaluatedArguments();
            Path path = PathFactory.createPath(this.session,
                    this.workdir().getAbsolute(),
                    (String) args.objectForKey("Path"));
            Editor editor = new Editor(this);
            editor.open(path);
        }
        return null;
    }

    public Object handleDeleteScriptCommand(NSScriptCommand command) {
        log.debug("handleDeleteScriptCommand:" + command);
        if(this.isMounted()) {
            NSDictionary args = command.evaluatedArguments();
            Path path = PathFactory.createPath(this.session,
                    this.workdir().getAbsolute(),
                    (String) args.objectForKey("Path"));
            try {
                path.cwdir();
                path.attributes.setType(Path.DIRECTORY_TYPE);
            }
            catch(IOException e) {
                path.attributes.setType(Path.FILE_TYPE);
            }
            path.delete();
            this.reloadData(true);
        }
        return null;
    }

    public Object handleRefreshScriptCommand(NSScriptCommand command) {
        log.debug("handleRefreshScriptCommand:" + command);
        if(this.isMounted()) {
            this.reloadButtonClicked(null);
        }
        return null;
    }

    public Object handleSyncScriptCommand(NSScriptCommand command) {
        log.debug("handleSyncScriptCommand:" + command);
        if(this.isMounted()) {
            NSDictionary args = command.evaluatedArguments();
            final Path path = PathFactory.createPath(this.session,
                    (String) args.objectForKey("Path"));
            path.attributes.setType(Path.DIRECTORY_TYPE);
            Object localObj = args.objectForKey("Local");
            if(localObj != null) {
                path.setLocal(new Local((String) localObj));
            }
            final Transfer q = new SyncTransfer(path);
            TransferOptions options = new TransferOptions();
            options.closeSession = false;
            q.start(CDTransferPrompt.create(this, q), options);
        }
        return null;
    }

    public Object handleDownloadScriptCommand(NSScriptCommand command) {
        log.debug("handleDownloadScriptCommand:" + command);
        if(this.isMounted()) {
            NSDictionary args = command.evaluatedArguments();
            final Path path = PathFactory.createPath(this.session,
                    this.workdir().getAbsolute(),
                    (String) args.objectForKey("Path"));
            try {
                path.cwdir();
                path.attributes.setType(Path.DIRECTORY_TYPE);
            }
            catch(IOException e) {
                path.attributes.setType(Path.FILE_TYPE);
            }
            Object localObj = args.objectForKey("Local");
            if(localObj != null) {
                path.setLocal(new Local((String) localObj, path.getName()));
            }
            Object nameObj = args.objectForKey("Name");
            if(nameObj != null) {
                path.setLocal(new Local(path.getLocal().getParent().getAbsolute(), (String) nameObj));
            }
            final Transfer q = new DownloadTransfer(path);
            TransferOptions options = new TransferOptions();
            options.closeSession = false;
            q.start(CDTransferPrompt.create(this, q), options);
        }
        return null;
    }

    public Object handleUploadScriptCommand(NSScriptCommand command) {
        log.debug("handleUploadScriptCommand:" + command);
        if(this.isMounted()) {
            NSDictionary args = command.evaluatedArguments();
            final Path path = PathFactory.createPath(this.session,
                    this.workdir().getAbsolute(),
                    new Local((String) args.objectForKey("Path")));
            if(path.getLocal().attributes.isFile()) {
                path.attributes.setType(Path.FILE_TYPE);
            }
            if(path.getLocal().attributes.isDirectory()) {
                path.attributes.setType(Path.DIRECTORY_TYPE);
            }
            Object remoteObj = args.objectForKey("Remote");
            if(remoteObj != null) {
                path.setPath((String) remoteObj, path.getName());
            }
            Object nameObj = args.objectForKey("Name");
            if(nameObj != null) {
                path.setPath(this.workdir().getAbsolute(), (String) nameObj);
            }
            final Transfer q = new UploadTransfer(path);
            TransferOptions options = new TransferOptions();
            options.closeSession = false;
            q.start(CDTransferPrompt.create(this, q), options);
        }
        return null;
    }

    // ----------------------------------------------------------
    // Constructor
    // ----------------------------------------------------------

    public CDBrowserController() {
        synchronized(NSApplication.sharedApplication()) {
            if(!NSApplication.loadNibNamed("Browser", this)) {
                log.fatal("Couldn't load Browser.nib");
            }
        }
    }

    public static CDBrowserController controllerForWindow(NSWindow window) {
        if(window.isVisible()) {
            Object delegate = window.delegate();
            if(delegate != null && delegate instanceof CDBrowserController) {
                return (CDBrowserController) delegate;
            }
        }
        return null;
    }

    public static void validateToolbarItems() {
        NSArray windows = NSApplication.sharedApplication().windows();
        int count = windows.count();
        while(0 != count--) {
            NSWindow window = (NSWindow) windows.objectAtIndex(count);
            CDBrowserController controller = CDBrowserController.controllerForWindow(window);
            if(null != controller) {
                window.toolbar().validateVisibleItems();
            }
        }
    }

    public static void updateBookmarkTableRowHeight() {
        NSArray windows = NSApplication.sharedApplication().windows();
        int count = windows.count();
        while(0 != count--) {
            NSWindow window = (NSWindow) windows.objectAtIndex(count);
            CDBrowserController controller = CDBrowserController.controllerForWindow(window);
            if(null != controller) {
                controller._updateBookmarkCellHeight();
            }
        }
    }

    public static void updateBrowserTableAttributes() {
        NSArray windows = NSApplication.sharedApplication().windows();
        int count = windows.count();
        while(0 != count--) {
            NSWindow window = (NSWindow) windows.objectAtIndex(count);
            CDBrowserController controller = CDBrowserController.controllerForWindow(window);
            if(null != controller) {
                controller._updateBrowserAttributes(controller.browserListView);
                controller._updateBrowserAttributes(controller.browserOutlineView);
            }
        }
    }

    public static void updateBrowserTableColumns() {
        NSArray windows = NSApplication.sharedApplication().windows();
        int count = windows.count();
        while(0 != count--) {
            NSWindow window = (NSWindow) windows.objectAtIndex(count);
            CDBrowserController controller = CDBrowserController.controllerForWindow(window);
            if(null != controller) {
                controller._updateBrowserColumns(controller.browserListView);
                controller._updateBrowserColumns(controller.browserOutlineView);
            }
        }
    }

    private NSToolbar toolbar;

    public void awakeFromNib() {
        log.debug("awakeFromNib");
        this._updateBrowserColumns(this.browserListView);
        this._updateBrowserColumns(this.browserOutlineView);

        // Configure window
        this.window.setTitle(
                NSBundle.mainBundle().infoDictionary().objectForKey("CFBundleName").toString());
        if(Preferences.instance().getBoolean("browser.bookmarkDrawer.isOpen")) {
            this.bookmarkDrawer.open();
        }
        // Configure Toolbar
        this.toolbar = new NSToolbar("Cyberduck Toolbar");
        this.toolbar.setDelegate(this);
        this.toolbar.setAllowsUserCustomization(true);
        this.toolbar.setAutosavesConfiguration(true);
        this.window.setToolbar(toolbar);

        this.browserSwitchClicked(Preferences.instance().getInteger("browser.view"));

        this.window.setInitialFirstResponder(this.quickConnectPopup);

        this.browserListView.setNextKeyView(this.searchField);
        this.browserOutlineView.setNextKeyView(this.searchField);
    }

    protected Comparator getComparator() {
        return ((CDTableDelegate) this.getSelectedBrowserView().delegate()).getSortingComparator();
    }

    private boolean showHiddenFiles;
    private PathFilter filenameFilter;

    {
        if(Preferences.instance().getBoolean("browser.showHidden")) {
            this.filenameFilter = new NullPathFilter();
            this.showHiddenFiles = true;
        }
        else {
            this.filenameFilter = new HiddenFilesPathFilter();
            this.showHiddenFiles = false;
        }
    }

    protected PathFilter getFileFilter() {
        return this.filenameFilter;
    }

    protected void setFileFilter(final String searchString) {
        log.debug("setFileFilter:" + searchString);
        if(null == searchString || searchString.length() == 0) {
            this.searchField.setStringValue("");
            // Revert to the last used default filter
            if(this.getShowHiddenFiles()) {
                this.filenameFilter = new NullPathFilter();
            }
            else {
                this.filenameFilter = new HiddenFilesPathFilter();
            }
        }
        else {
            // Setting up a custom filter for the directory listing
            this.filenameFilter = new PathFilter() {
                public boolean accept(AbstractPath file) {
                    return file.getName().toLowerCase().indexOf(searchString.toLowerCase()) != -1;
                }
            };
        }
    }

    public void setShowHiddenFiles(boolean showHidden) {
        if(showHidden) {
            this.filenameFilter = new NullPathFilter();
            this.showHiddenFiles = true;
        }
        else {
            this.filenameFilter = new HiddenFilesPathFilter();
            this.showHiddenFiles = false;
        }
    }

    public boolean getShowHiddenFiles() {
        return this.showHiddenFiles;
    }

    /**
     * Marks the current browser as the first responder
     */
    private void getFocus() {
        this.window.makeFirstResponder(this.getSelectedBrowserView());
    }

    /**
     * @param preserveSelection All selected files should be reselected after reloading the view
     * @pre Must always be invoked from the main interface thread
     */
    public void reloadData(final boolean preserveSelection) {
        if(preserveSelection) {
            //Remember the previously selected paths
            this.reloadData(this.getSelectedPaths());
        }
        else {
            this.reloadData(Collections.EMPTY_LIST);
        }
    }

    /**
     * @param selected The items to be selected
     * @see #setSelectedPaths(java.util.Collection)
     */
    protected void reloadData(final java.util.Collection selected) {
        log.debug("reloadData");
        if(this.isMounted()) {
            if(!this.workdir().isCached() || this.workdir().childs().attributes().isDirty()) {
                this.background(new BackgroundAction() {
                    public void run() {
                        workdir().childs();
                    }

                    public void cleanup() {
                        reloadData(selected);
                    }
                });
                return;
            }
        }
        this.deselectAll();
        // Tell the browser view to reload the data. This will request all paths from the browser model
        // which will refetch paths from the server marked as invalid.
        final NSTableView browser = this.getSelectedBrowserView();
        browser.reloadData();
        if(this.isMounted()) {
            // Delay for later invocation to make sure this is displayed as the last status message
            this.invoke(new Runnable() {
                public void run() {
                    statusLabel.setAttributedStringValue(new NSAttributedString(
                            browser.numberOfRows() + " " + NSBundle.localizedString("files", ""),
                            TRUNCATE_MIDDLE_ATTRIBUTES));
                    statusLabel.display();
                }
            });
        }
        this.setSelectedPaths(selected);
    }

    /**
     * @param path
     * @param expand Expand the existing selection
     */
    private void selectRow(Path path, boolean expand) {
        log.debug("selectRow:" + path);
        final NSTableView browser = this.getSelectedBrowserView();
        if(this.getSelectedBrowserModel().contains(browser, path)) {
            this.selectRow(this.getSelectedBrowserModel().indexOf(browser, path), expand);
        }
    }

    /**
     * @param row
     * @param expand Expand the existing selection
     */
    private void selectRow(int row, boolean expand) {
        log.debug("selectRow:" + row);
        final NSTableView browser = this.getSelectedBrowserView();
        browser.selectRow(row, expand);
        browser.scrollRowToVisible(row);
    }

    protected void setSelectedPath(Path selected) {
        List list = new Collection();
        list.add(selected);
        this.setSelectedPaths(list);
    }

    protected void setSelectedPaths(java.util.Collection selected) {
        log.debug("setSelectedPaths");
        this.deselectAll();
        if(!selected.isEmpty()) {
            switch(this.browserSwitchView.selectedSegment()) {
                case LIST_VIEW: {
                    //selection handling
                    for(Iterator iter = selected.iterator(); iter.hasNext();) {
                        this.selectRow((Path) iter.next(), true);
                    }
                    break;
                }
                case OUTLINE_VIEW: {
                    for(int i = 0; i < this.browserOutlineView.numberOfRows(); i++) {
                        Path p = (Path) this.browserOutlineView.itemAtRow(i);
                        if(null == p) {
                            break;
                        }
                        if(selected.contains(p)) {
                            this.selectRow(p, true);
                        }
                    }
                    break;
                }
            }
        }
    }

    /**
     * @return The first selected path found or null if there is no selection
     */
    protected Path getSelectedPath() {
        List selected = this.getSelectedPaths();
        if(selected.size() > 0) {
            return (Path) selected.get(0);
        }
        return null;
    }

    /**
     * @return All selected paths or an empty list if there is no selection
     */
    protected Collection getSelectedPaths() {
        Collection selectedFiles = new Collection();
        if(this.isMounted()) {
            NSIndexSet iterator = this.getSelectedBrowserView().selectedRowIndexes();
            for(int index = iterator.firstIndex(); index != NSIndexSet.NotFound; index = iterator.indexGreaterThanIndex(index))
            {
                Path selected = this.pathAtRow(index);
                if(null == selected) {
                    break;
                }
                selectedFiles.add(selected);
            }
        }
        return selectedFiles;
    }

    protected int getSelectionCount() {
        return this.getSelectedBrowserView().numberOfSelectedRows();
    }

    private void deselectAll() {
        log.debug("deselectAll");
        this.getSelectedBrowserView().deselectAll(null);
    }

    private Path pathAtRow(int row) {
        Path item = null;
        switch(this.browserSwitchView.selectedSegment()) {
            case LIST_VIEW: {
                item = (Path) this.browserListModel.childs(this.workdir()).get(row);
                break;
            }
            case OUTLINE_VIEW: {
                item = (Path) this.browserOutlineView.itemAtRow(row);
                break;
            }
        }
        return item;
    }

    // ----------------------------------------------------------
    // Outlets
    // ----------------------------------------------------------

    public void setWindow(NSWindow window) {
        window.setDelegate(this);
        window.setTitle((String) NSBundle.mainBundle().infoDictionary().objectForKey("CFBundleName"));
        window.setMiniwindowImage(NSImage.imageNamed("cyberduck-document.icns"));
        super.setWindow(window);
    }

    private NSDrawer bookmarkDrawer;

    private NSDrawer.Notifications bookmarkDrawerNotifications = new NSDrawer.Notifications() {
        public void drawerWillOpen(NSNotification notification) {
        }

        public void drawerDidOpen(NSNotification notification) {
            Preferences.instance().setProperty("browser.bookmarkDrawer.isOpen", true);
        }

        public void drawerWillClose(NSNotification notification) {
        }

        public void drawerDidClose(NSNotification notification) {
            Preferences.instance().setProperty("browser.bookmarkDrawer.isOpen", false);
        }
    };

    public void setBookmarkDrawer(NSDrawer bookmarkDrawer) {
        this.bookmarkDrawer = bookmarkDrawer;
        NSNotificationCenter.defaultCenter().addObserver(bookmarkDrawerNotifications,
                new NSSelector("drawerDidOpen", new Class[]{Object.class}),
                NSDrawer.DrawerDidOpenNotification,
                this.bookmarkDrawer);
        NSNotificationCenter.defaultCenter().addObserver(bookmarkDrawerNotifications,
                new NSSelector("drawerDidClose", new Class[]{Object.class}),
                NSDrawer.DrawerDidCloseNotification,
                this.bookmarkDrawer);
    }

//    private NSTextField bookmarkSearchField;
//
//    public void setBookmarkSearchField(NSTextField bookmarkSearchField) {
//        this.bookmarkSearchField = bookmarkSearchField;
//        NSNotificationCenter.defaultCenter().addObserver(this,
//                new NSSelector("bookmarkSearchFieldDidChange", new Class[]{Object.class}),
//                NSControl.ControlTextDidChangeNotification,
//                this.bookmarkSearchField);
//    }
//
//    public void bookmarkSearchFieldDidChange(NSNotification notification) {
//        NSDictionary userInfo = notification.userInfo();
//        if(null != userInfo) {
//            Object o = userInfo.allValues().lastObject();
//            if(null != o) {
//                final String searchString = ((NSText)o).string();
//                this.bookmarkModel.setFilter(new HostFilter() {
//                    public boolean accept(Host host) {
//                        return host.getNickname().indexOf(searchString) != -1
//                                || host.getHostname().indexOf(searchString) != -1;
//                    }
//                });
//                this.bookmarkTable.reloadData();
//            }
//        }
//    }

    private NSTabView browserTabView;

    public void setBrowserTabView(NSTabView browserTabView) {
        this.browserTabView = browserTabView;
    }

    /**
     * @return The currently selected browser view (which is either an outlineview or a plain tableview)
     */
    public NSTableView getSelectedBrowserView() {
        switch(this.browserSwitchView.selectedSegment()) {
            case LIST_VIEW: {
                return this.browserListView;
            }
            case OUTLINE_VIEW: {
                return this.browserOutlineView;
            }
        }
        return null;
    }

    /**
     * @return The datasource of the currently selected browser view
     */
    public CDBrowserTableDataSource getSelectedBrowserModel() {
        return (CDBrowserTableDataSource) this.getSelectedBrowserView().dataSource();
    }

    private NSSegmentedControl browserSwitchView;

    private static final int LIST_VIEW = 0;
    private static final int OUTLINE_VIEW = 1;

    public void setBrowserSwitchView(NSSegmentedControl browserSwitchView) {
        this.browserSwitchView = browserSwitchView;
        this.browserSwitchView.setSegmentCount(2); // list, outline
        this.browserSwitchView.setImage(NSImage.imageNamed("list.tiff"), LIST_VIEW);
        this.browserSwitchView.setImage(NSImage.imageNamed("outline.tiff"), OUTLINE_VIEW);
        this.browserSwitchView.setTarget(this);
        this.browserSwitchView.setAction(new NSSelector("browserSwitchButtonClicked", new Class[]{Object.class}));
        ((NSSegmentedCell) this.browserSwitchView.cell()).setTrackingMode(NSSegmentedCell.NSSegmentSwitchTrackingSelectOne);
        this.browserSwitchView.cell().setControlSize(NSCell.RegularControlSize);
        this.browserSwitchView.setSelected(Preferences.instance().getInteger("browser.view"));
    }

    public void browserSwitchButtonClicked(final NSSegmentedControl sender) {
        this.browserSwitchClicked(sender.selectedSegment());
    }

    public void browserSwitchMenuClicked(final NSMenuItem sender) {
        this.browserSwitchClicked(sender.tag());
    }

    private void browserSwitchClicked(final int tag) {
        Preferences.instance().setProperty("browser.view", tag);
        this.browserSwitchView.setSelected(tag);
        this.browserTabView.selectTabViewItemAtIndex(tag);
        this.reloadData(false);
        this.quickConnectPopup.setNextKeyView(this.getSelectedBrowserView());
        this.searchField.setNextKeyView(this.getSelectedBrowserView());
    }

    private class AbstractBrowserTableDelegate extends CDAbstractTableDelegate {
        public boolean isColumnEditable(NSTableColumn column) {
            if(Preferences.instance().getBoolean("browser.editable")) {
                return column.identifier().equals(CDBrowserTableDataSource.FILENAME_COLUMN);
            }
            return false;
        }

        public void tableRowDoubleClicked(final Object sender) {
            CDBrowserController.this.insideButtonClicked(sender);
        }

        public void enterKeyPressed(final Object sender) {
            ;
        }

        public void deleteKeyPressed(final Object sender) {
            CDBrowserController.this.deleteFileButtonClicked(sender);
        }

        public void tableColumnClicked(NSTableView view, NSTableColumn tableColumn) {
            List selected = CDBrowserController.this.getSelectedPaths();
            if(this.selectedColumnIdentifier().equals(tableColumn.identifier())) {
                this.setSortedAscending(!this.isSortedAscending());
            }
            else {
                this.setBrowserColumnSortingIndicator(null, this.selectedColumnIdentifier());
                this.setSelectedColumn(tableColumn);
            }
            this.setBrowserColumnSortingIndicator(
                    this.isSortedAscending() ?
                            NSImage.imageNamed("NSAscendingSortIndicator") :
                            NSImage.imageNamed("NSDescendingSortIndicator"),
                    tableColumn.identifier().toString());
            view.deselectAll(null);
            view.reloadData();
            for(Iterator i = selected.iterator(); i.hasNext();) {
                view.selectRowIndexes(new NSIndexSet(
                        CDBrowserController.this.getSelectedBrowserModel().indexOf(view, (Path) i.next())),
                        true);
            }
        }

        public void selectionDidChange(NSNotification notification) {
            if(Preferences.instance().getBoolean("browser.info.isInspector")) {
                if(inspector != null && inspector.window() != null && inspector.window().isVisible()) {
                    List files = new Collection();
                    for(Iterator i = getSelectedPaths().iterator(); i.hasNext();) {
                        files.add(i.next());
                    }
                    if(files.size() > 0) {
                        inspector.setFiles(files);
                    }
                }
            }
        }

        private void setBrowserColumnSortingIndicator(NSImage image, String columnIdentifier) {
            if(browserListView.tableColumnWithIdentifier(columnIdentifier) != null)
                browserListView.setIndicatorImage(image, browserListView.tableColumnWithIdentifier(columnIdentifier));
            if(browserOutlineView.tableColumnWithIdentifier(columnIdentifier) != null)
                browserOutlineView.setIndicatorImage(image, browserOutlineView.tableColumnWithIdentifier(columnIdentifier));
        }
    }

    private CDBrowserOutlineViewModel browserOutlineModel;
    private NSOutlineView browserOutlineView; // IBOutlet
    private CDTableDelegate browserOutlineViewDelegate;

    public void setBrowserOutlineView(NSOutlineView browserOutlineView) {
        this.browserOutlineView = browserOutlineView;
        // receive drag events from types
        this.browserOutlineView.registerForDraggedTypes(new NSArray(new Object[]{
                CDPasteboards.TransferPasteboardType,
                NSPasteboard.FilenamesPboardType, //accept files dragged from the Finder for uploading
                NSPasteboard.FilesPromisePboardType} //accept file promises made myself but then interpret them as TransferPasteboardType
        ));

        // setting appearance attributes
        this.browserOutlineView.setRowHeight(17f);
        this._updateBrowserAttributes(this.browserOutlineView);
        // selection properties
        this.browserOutlineView.setAllowsMultipleSelection(true);
        this.browserOutlineView.setAllowsEmptySelection(true);
        this.browserOutlineView.setAllowsColumnResizing(true);
        this.browserOutlineView.setAllowsColumnSelection(false);
        this.browserOutlineView.setAllowsColumnReordering(true);

        this.browserOutlineView.setDataSource(this.browserOutlineModel = new CDBrowserOutlineViewModel(this));
        this.browserOutlineView.setDelegate(this.browserOutlineViewDelegate = new AbstractBrowserTableDelegate() {

            public void enterKeyPressed(final Object sender) {
                if(Preferences.instance().getBoolean("browser.enterkey.rename")) {
                    if(CDBrowserController.this.browserOutlineView.numberOfSelectedRows() == 1) {
                        CDBrowserController.this.browserOutlineView.editLocation(
                                CDBrowserController.this.browserOutlineView.columnWithIdentifier(CDBrowserTableDataSource.FILENAME_COLUMN),
                                CDBrowserController.this.browserOutlineView.selectedRow(),
                                null, true);
                    }
                }
                else {
                    this.tableRowDoubleClicked(sender);
                }
            }

            /**
             * @see NSOutlineView.Delegate
             */
            public void outlineViewWillDisplayCell(NSOutlineView outlineView, Object cell,
                                                   NSTableColumn tableColumn, Path item) {
                String identifier = (String) tableColumn.identifier();
                if(item != null) {
                    if(identifier.equals(CDBrowserTableDataSource.FILENAME_COLUMN)) {
                        ((CDOutlineCell) cell).setIcon(browserOutlineModel.iconForPath(item));
                    }
                    if(cell instanceof NSTextFieldCell) {
                        if(!CDBrowserController.this.isConnected()) {// || CDBrowserController.this.activityRunning) {
                            ((NSTextFieldCell) cell).setTextColor(NSColor.disabledControlTextColor());
                        }
                        else {
                            ((NSTextFieldCell) cell).setTextColor(NSColor.controlTextColor());
                        }
                    }
                }
            }

            /**
             * @see NSOutlineView.Delegate
             */
            public boolean outlineViewShouldExpandItem(final NSOutlineView view, final Path item) {
                return true;
            }

            /**
             * @see NSOutlineView.Notifications
             */
            public void outlineViewItemDidExpand(NSNotification notification) {
                statusLabel.setAttributedStringValue(new NSAttributedString(
                        CDBrowserController.this.browserOutlineView.numberOfRows() + " " +
                                NSBundle.localizedString("files", ""),
                        TRUNCATE_MIDDLE_ATTRIBUTES));
            }

            /**
             * @see NSOutlineView.Notifications
             */
            public void outlineViewItemDidCollapse(NSNotification notification) {
                statusLabel.setAttributedStringValue(new NSAttributedString(
                        CDBrowserController.this.browserOutlineView.numberOfRows() + " " +
                                NSBundle.localizedString("files", ""),
                        TRUNCATE_MIDDLE_ATTRIBUTES));
            }

            /**
             * @see NSOutlineView.Delegate
             */
            public String outlineViewToolTipForCell(NSOutlineView view, NSCell cell, NSMutableRect rect, NSTableColumn tableColumn,
                                                    final Path item, NSPoint mouseLocation) {
                return super.tooltipForPath(item);
            }
        });
        NSSelector setResizableMaskSelector = new NSSelector("setResizingMask", new Class[]{int.class});
        {
            NSTableColumn c = new NSTableColumn();
            c.headerCell().setStringValue(NSBundle.localizedString("Filename", "A column in the browser"));
            c.setIdentifier(CDBrowserTableDataSource.FILENAME_COLUMN);
            c.setMinWidth(100f);
            c.setWidth(250f);
            c.setMaxWidth(1000f);
            if(setResizableMaskSelector.implementedByClass(NSTableColumn.class)) {
                c.setResizingMask(NSTableColumn.AutoresizingMask | NSTableColumn.UserResizingMask);
            }
            else {
                c.setResizable(true);
            }
            NSTextFieldCell cell = new CDOutlineCell() {
                public void selectAndEditWithFrameInView(NSRect rect, NSView view, NSText text, Object object, int selStart, int selLength) {
                    super.selectAndEditWithFrameInView(new NSRect(rect.x() + 20, rect.y(), rect.width() - 20, rect.height()),
                            view, text, object, selStart, selLength);
                }

                public void editWithFrameInView(NSRect rect, NSView view, NSText text, Object object, NSEvent event) {
                    super.editWithFrameInView(new NSRect(rect.x() + 20, rect.y(), rect.width() - 20, rect.height()),
                            view, text, object, event);
                }
            };
            {
                cell.setEditable(true);
                cell.setTarget(browserListView.target());
                cell.setAction(browserListView.action());
            }
            c.setDataCell(cell);
            this.browserOutlineView.addTableColumn(c);
            this.browserOutlineView.setOutlineTableColumn(c);
        }
    }

    private CDBrowserListViewModel browserListModel;
    private NSTableView browserListView; // IBOutlet
    private CDTableDelegate browserListViewDelegate;

    public void setBrowserListView(NSTableView view) {
        this.browserListView = view;
        // receive drag events from types
        this.browserListView.registerForDraggedTypes(new NSArray(new Object[]{
                CDPasteboards.TransferPasteboardType,
                NSPasteboard.FilenamesPboardType, //accept files dragged from the Finder for uploading
                NSPasteboard.FilesPromisePboardType} //accept file promises made myself but then interpret them as TransferPasteboardType
        ));

        // setting appearance attributes
        this.browserListView.setRowHeight(17f);
        this._updateBrowserAttributes(this.browserListView);
        // selection properties
        this.browserListView.setAllowsMultipleSelection(true);
        this.browserListView.setAllowsEmptySelection(true);
        this.browserListView.setAllowsColumnResizing(true);
        this.browserListView.setAllowsColumnSelection(false);
        this.browserListView.setAllowsColumnReordering(true);

        this.browserListView.setDataSource(this.browserListModel = new CDBrowserListViewModel(this));
        this.browserListView.setDelegate(this.browserListViewDelegate = new AbstractBrowserTableDelegate() {
            public void enterKeyPressed(final Object sender) {
                if(Preferences.instance().getBoolean("browser.enterkey.rename")) {
                    if(CDBrowserController.this.browserListView.numberOfSelectedRows() == 1) {
                        CDBrowserController.this.browserListView.editLocation(
                                CDBrowserController.this.browserListView.columnWithIdentifier(CDBrowserTableDataSource.FILENAME_COLUMN),
                                CDBrowserController.this.browserListView.selectedRow(),
                                null, true);
                    }
                }
                else {
                    this.tableRowDoubleClicked(sender);
                }
            }

            public void tableViewWillDisplayCell(NSTableView view, Object cell, NSTableColumn tableColumn, int row) {
                if(cell instanceof NSTextFieldCell) {
                    if(!CDBrowserController.this.isConnected()) {// || CDBrowserController.this.activityRunning) {
                        ((NSTextFieldCell) cell).setTextColor(NSColor.disabledControlTextColor());
                    }
                    else {
                        ((NSTextFieldCell) cell).setTextColor(NSColor.controlTextColor());
                    }
                }
            }

            public String tableViewToolTipForCell(NSTableView view, NSCell cell, NSMutableRect rect,
                                                  NSTableColumn tc, int row, NSPoint mouseLocation) {
                if(row < getSelectedBrowserModel().childs(CDBrowserController.this.workdir()).size()) {
                    Path p = (Path) getSelectedBrowserModel().childs(CDBrowserController.this.workdir()).get(row);
                    return super.tooltipForPath(p);
                }
                return null;
            }
        });
        NSSelector setResizableMaskSelector
                = new NSSelector("setResizingMask", new Class[]{int.class});
        {
            NSTableColumn c = new NSTableColumn();
            c.setIdentifier(CDBrowserTableDataSource.ICON_COLUMN);
            c.headerCell().setStringValue("");
            c.setMinWidth(20f);
            c.setWidth(20f);
            c.setMaxWidth(20f);
            if(setResizableMaskSelector.implementedByClass(NSTableColumn.class)) {
                c.setResizingMask(NSTableColumn.AutoresizingMask);
            }
            else {
                c.setResizable(true);
            }
            c.setDataCell(new NSImageCell());
            c.dataCell().setAlignment(NSText.CenterTextAlignment);
            this.browserListView.addTableColumn(c);
        }
        {
            NSTableColumn c = new NSTableColumn();
            c.headerCell().setStringValue(NSBundle.localizedString("Filename", "A column in the browser"));
            c.setIdentifier(CDBrowserTableDataSource.FILENAME_COLUMN);
            c.setMinWidth(100f);
            c.setWidth(250f);
            c.setMaxWidth(1000f);
            if(setResizableMaskSelector.implementedByClass(NSTableColumn.class)) {
                c.setResizingMask(NSTableColumn.AutoresizingMask | NSTableColumn.UserResizingMask);
            }
            else {
                c.setResizable(true);
            }
            NSTextFieldCell cell = new NSTextFieldCell();
            {
                cell.setEditable(true); //to enable inline renaming of files
                cell.setTarget(view.target());
                cell.setAction(view.action());
            }
            c.setDataCell(cell);
            this.browserListView.addTableColumn(c);
        }
    }

    protected void _updateBrowserAttributes(NSTableView tableView) {
        tableView.setUsesAlternatingRowBackgroundColors(Preferences.instance().getBoolean("browser.alternatingRows"));
        if(Preferences.instance().getBoolean("browser.horizontalLines") && Preferences.instance().getBoolean("browser.verticalLines")) {
            tableView.setGridStyleMask(NSTableView.SolidHorizontalGridLineMask | NSTableView.SolidVerticalGridLineMask);
        }
        else if(Preferences.instance().getBoolean("browser.verticalLines")) {
            tableView.setGridStyleMask(NSTableView.SolidVerticalGridLineMask);
        }
        else if(Preferences.instance().getBoolean("browser.horizontalLines")) {
            tableView.setGridStyleMask(NSTableView.SolidHorizontalGridLineMask);
        }
        else {
            tableView.setGridStyleMask(NSTableView.GridNone);
        }
    }

    protected void _updateBookmarkCellHeight() {
        this.bookmarkTable.setRowHeight(
                Preferences.instance().getBoolean("browser.bookmarkDrawer.smallItems") ? 18f : 45f);
        this.bookmarkTable.reloadData();
    }

    protected void _updateBrowserColumns(NSTableView table) {
        NSSelector setResizableMaskSelector
                = new NSSelector("setResizingMask", new Class[]{int.class});
        table.removeTableColumn(table.tableColumnWithIdentifier(CDBrowserTableDataSource.SIZE_COLUMN));
        if(Preferences.instance().getBoolean("browser.columnSize")) {
            NSTableColumn c = new NSTableColumn();
            c.headerCell().setStringValue(NSBundle.localizedString("Size", "A column in the browser"));
            c.setIdentifier(CDBrowserTableDataSource.SIZE_COLUMN);
            c.setMinWidth(50f);
            c.setWidth(80f);
            c.setMaxWidth(100f);
            if(setResizableMaskSelector.implementedByClass(NSTableColumn.class)) {
                c.setResizingMask(NSTableColumn.AutoresizingMask);
            }
            else {
                c.setResizable(true);
            }
            c.setDataCell(new NSTextFieldCell());
            table.addTableColumn(c);
        }
        table.removeTableColumn(table.tableColumnWithIdentifier(CDBrowserTableDataSource.MODIFIED_COLUMN));
        if(Preferences.instance().getBoolean("browser.columnModification")) {
            NSTableColumn c = new NSTableColumn();
            c.headerCell().setStringValue(NSBundle.localizedString("Modified", "A column in the browser"));
            c.setIdentifier(CDBrowserTableDataSource.MODIFIED_COLUMN);
            c.setMinWidth(100f);
            c.setWidth(150f);
            c.setMaxWidth(500f);
            if(setResizableMaskSelector.implementedByClass(NSTableColumn.class)) {
                c.setResizingMask(NSTableColumn.AutoresizingMask | NSTableColumn.UserResizingMask);
            }
            else {
                c.setResizable(true);
            }
            c.setDataCell(new NSTextFieldCell());
            table.addTableColumn(c);
        }
        table.removeTableColumn(table.tableColumnWithIdentifier(CDBrowserTableDataSource.OWNER_COLUMN));
        if(Preferences.instance().getBoolean("browser.columnOwner")) {
            NSTableColumn c = new NSTableColumn();
            c.headerCell().setStringValue(NSBundle.localizedString("Owner", "A column in the browser"));
            c.setIdentifier(CDBrowserTableDataSource.OWNER_COLUMN);
            c.setMinWidth(50f);
            c.setWidth(80f);
            c.setMaxWidth(500f);
            if(setResizableMaskSelector.implementedByClass(NSTableColumn.class)) {
                c.setResizingMask(NSTableColumn.AutoresizingMask | NSTableColumn.UserResizingMask);
            }
            else {
                c.setResizable(true);
            }
            c.setDataCell(new NSTextFieldCell());
            table.addTableColumn(c);
        }
        table.removeTableColumn(table.tableColumnWithIdentifier(CDBrowserTableDataSource.PERMISSIONS_COLUMN));
        if(Preferences.instance().getBoolean("browser.columnPermissions")) {
            NSTableColumn c = new NSTableColumn();
            c.headerCell().setStringValue(NSBundle.localizedString("Permissions", "A column in the browser"));
            c.setIdentifier(CDBrowserTableDataSource.PERMISSIONS_COLUMN);
            c.setMinWidth(100f);
            c.setWidth(100f);
            c.setMaxWidth(800f);
            if(setResizableMaskSelector.implementedByClass(NSTableColumn.class)) {
                c.setResizingMask(NSTableColumn.AutoresizingMask | NSTableColumn.UserResizingMask);
            }
            else {
                c.setResizable(true);
            }
            c.setDataCell(new NSTextFieldCell());
            table.addTableColumn(c);
        }
        table.removeTableColumn(table.tableColumnWithIdentifier(CDBrowserTableDataSource.KIND_COLUMN));
        if(Preferences.instance().getBoolean("browser.columnKind")) {
            NSTableColumn c = new NSTableColumn();
            c.headerCell().setStringValue(NSBundle.localizedString("Kind", "A column in the browser"));
            c.setIdentifier(CDBrowserTableDataSource.KIND_COLUMN);
            c.setMinWidth(50f);
            c.setWidth(80f);
            c.setMaxWidth(500f);
            if(setResizableMaskSelector.implementedByClass(NSTableColumn.class)) {
                c.setResizingMask(NSTableColumn.AutoresizingMask | NSTableColumn.UserResizingMask);
            }
            else {
                c.setResizable(true);
            }
            c.setDataCell(new NSTextFieldCell());
            table.addTableColumn(c);
        }
        table.setIndicatorImage(((CDTableDelegate) table.delegate()).isSortedAscending() ?
                NSImage.imageNamed("NSAscendingSortIndicator") :
                NSImage.imageNamed("NSDescendingSortIndicator"),
                table.tableColumnWithIdentifier(Preferences.instance().getProperty("browser.sort.column")));
//        table.setAutosaveTableColumns(true);
        table.sizeToFit();
        this.reloadData(false);
    }

    private CDBookmarkTableDataSource bookmarkModel;

    private NSTableView bookmarkTable; // IBOutlet
    private CDTableDelegate bookmarkTableDelegate;
    private CollectionListener bookmarkCollectionListener;

    public void setBookmarkTable(NSTableView view) {
        this.bookmarkTable = view;
        this.bookmarkTable.setDataSource(this.bookmarkModel = new CDBookmarkTableDataSource());
        HostCollection.instance().addListener(this.bookmarkCollectionListener = new CollectionListener() {
            public void collectionItemAdded(Object item) {
                bookmarkTable.reloadData();
            }

            public void collectionItemRemoved(Object item) {
                bookmarkTable.reloadData();
            }

            public void collectionItemChanged(Object item) {
                bookmarkTable.reloadData();
            }
        });
        this.bookmarkTable.setDelegate(this.bookmarkTableDelegate = new CDAbstractTableDelegate() {
            public void tableRowDoubleClicked(final Object sender) {
                CDBrowserController.this.connectBookmarkButtonClicked(sender);
            }

            public void enterKeyPressed(final Object sender) {
                this.tableRowDoubleClicked(sender);
            }

            public void deleteKeyPressed(final Object sender) {
                CDBrowserController.this.deleteBookmarkButtonClicked(sender);
            }

            public void tableColumnClicked(NSTableView view, NSTableColumn tableColumn) {

            }

            public String toolTipForItem(Object item) {
                return null;
            }

            public void selectionDidChange(NSNotification notification) {
                editBookmarkButton.setEnabled(bookmarkTable.numberOfSelectedRows() == 1);
                deleteBookmarkButton.setEnabled(bookmarkTable.selectedRow() != -1);
            }
        });
        // receive drag events from types
        this.bookmarkTable.registerForDraggedTypes(new NSArray(new Object[]
                {
                        NSPasteboard.FilenamesPboardType, //accept bookmark files dragged from the Finder
                        NSPasteboard.FilesPromisePboardType,
                        "HostPBoardType" //moving bookmarks
                }));

        this._updateBookmarkCellHeight();

        NSSelector setResizableMaskSelector
                = new NSSelector("setResizingMask", new Class[]{int.class});
        {
            NSTableColumn c = new NSTableColumn();
            c.setIdentifier(CDBookmarkTableDataSource.ICON_COLUMN);
            c.headerCell().setStringValue("");
            c.setMinWidth(32f);
            c.setWidth(32f);
            c.setMaxWidth(32f);
            if(setResizableMaskSelector.implementedByClass(NSTableColumn.class)) {
                c.setResizingMask(NSTableColumn.AutoresizingMask);
            }
            else {
                c.setResizable(true);
            }
            c.setDataCell(new NSImageCell());
            this.bookmarkTable.addTableColumn(c);
        }
        {
            NSTableColumn c = new NSTableColumn();
            c.setIdentifier(CDBookmarkTableDataSource.BOOKMARK_COLUMN);
            c.headerCell().setStringValue(NSBundle.localizedString("Bookmarks", "A column in the browser"));
            c.setMinWidth(50f);
            c.setWidth(200f);
            c.setMaxWidth(500f);
            if(setResizableMaskSelector.implementedByClass(NSTableColumn.class)) {
                c.setResizingMask(NSTableColumn.AutoresizingMask);
            }
            else {
                c.setResizable(true);
            }
            c.setDataCell(new CDBookmarkCell());
            this.bookmarkTable.addTableColumn(c);
        }

        // setting appearance attributes
        this.bookmarkTable.setUsesAlternatingRowBackgroundColors(Preferences.instance().getBoolean("browser.alternatingRows"));
        this.bookmarkTable.setGridStyleMask(NSTableView.SolidHorizontalGridLineMask);

        // selection properties
        this.bookmarkTable.setAllowsMultipleSelection(true);
        this.bookmarkTable.setAllowsEmptySelection(true);
        this.bookmarkTable.setAllowsColumnResizing(false);
        this.bookmarkTable.setAllowsColumnSelection(false);
        this.bookmarkTable.setAllowsColumnReordering(false);
        this.bookmarkTable.sizeToFit();
    }

    private NSPopUpButton actionPopupButton;

    public void setActionPopupButton(NSPopUpButton actionPopupButton) {
        this.actionPopupButton = actionPopupButton;
        this.actionPopupButton.setPullsDown(true);
        this.actionPopupButton.setAutoenablesItems(true);
        this.actionPopupButton.itemAtIndex(0).setImage(NSImage.imageNamed("gear.tiff"));
    }

//    private NSPopUpButton historyPopupButton;
//
//    private NSMenu historyPopupButtonMenu;
//
//    private Object historyPopupButtonMenuDelegate;
//
//    public void setHistoryPopupButton(NSPopUpButton historyPopupButton) {
//        this.historyPopupButton = historyPopupButton;
//        this.historyPopupButton.setPullsDown(true);
//        this.historyPopupButton.setAutoenablesItems(true);
//        this.historyPopupButton.setEnabled(false);
//        this.historyPopupButton.setImage(NSImage.imageNamed("history.tiff"));
//        this.historyPopupButton.setMenu(historyPopupButtonMenu = new NSMenu());
//        this.historyPopupButton.menu().setDelegate(historyPopupButtonMenuDelegate = new PathHistoryMenuDelegate(this));
//    }

    private NSComboBox quickConnectPopup; // IBOutlet

    private NSObject quickConnectPopupModel;

    public void setQuickConnectPopup(NSComboBox quickConnectPopup) {
        this.quickConnectPopup = quickConnectPopup;
        this.quickConnectPopup.setTarget(this);
        this.quickConnectPopup.setCompletes(true);
        this.quickConnectPopup.setAction(new NSSelector("quickConnectSelectionChanged", new Class[]{Object.class}));
        this.quickConnectPopup.setUsesDataSource(true);
        this.quickConnectPopup.setDataSource(this.quickConnectPopupModel = new NSObject/*NSComboBox.DataSource*/() {
            public int numberOfItemsInComboBox(final NSComboBox combo) {
                return HostCollection.instance().size();
            }

            public Object comboBoxObjectValueForItemAtIndex(final NSComboBox sender, final int row) {
                if(row < numberOfItemsInComboBox(sender)) {
                    return ((Host) HostCollection.instance().get(row)).getNickname();
                }
                return null;
            }
        });
        NSNotificationCenter.defaultCenter().addObserver(this,
                new NSSelector("quickConnectWillPopUp", new Class[]{Object.class}),
                NSComboBox.ComboBoxWillPopUpNotification,
                this.quickConnectPopup);
        this.quickConnectWillPopUp(null);
    }

    public void quickConnectWillPopUp(NSNotification notification) {
        int size = HostCollection.instance().size();
        this.quickConnectPopup.setNumberOfVisibleItems(size > 10 ? 10 : size);
    }

    public void quickConnectSelectionChanged(final Object sender) {
        if(null == sender) {
            return;
        }
        String input = ((NSControl) sender).stringValue();
        if(null == input || input.length() == 0) {
            return;
        }
        try {
            // First look for equivalent bookmarks
            for(Iterator iter = HostCollection.instance().iterator(); iter.hasNext();) {
                Host h = (Host) iter.next();
                if(h.getNickname().equals(input)) {
                    this.mount(h);
                    return;
                }
            }
            // Try to parse the input as a URL and extract protocol, hostname, username and password if any.
            this.mount(Host.parse(input));
        }
        catch(java.net.MalformedURLException e) {
            // No URL; assume a hostname has been entered
            this.mount(new Host(input));
        }
    }

//    private NSComboBox navigationPopup; // IBOutlet
//
//    private NSObject navigationPopupModel;
//
//    public void setNavigationPopup(NSComboBox navigationPopup) {
//        this.navigationPopup = navigationPopup;
//        this.navigationPopup.setCompletes(true);
//        this.navigationPopup.setUsesDataSource(true);
//        this.navigationPopup.setDataSource(this.navigationPopupModel = new NSObject()/*NSComboBox.DataSource*/ {
//            private final Comparator comparator = new NullComparator();
//            private final PathFilter filter = new PathFilter() {
//                public boolean accept(AbstractPath p) {
//                    return p.attributes.isDirectory();
//                }
//            };
//
//            /**
//             * @see NSComboBox.DataSource
//             */
//            public int numberOfItemsInComboBox(NSComboBox combo) {
//                if(!isMounted()) {
//                    return 0;
//                }
//                return workdir().childs(comparator, filter).size();
//            }
//
//            /**
//             * @see NSComboBox.DataSource
//             */
//            public Object comboBoxObjectValueForItemAtIndex(final NSComboBox sender, final int row) {
//                final List childs = workdir().childs(comparator, filter);
//                final int size = childs.size();
//                if(row < size) {
//                    return ((Path)childs.get(row)).getAbsolute();
//                }
//                return null;
//            }
//        });
//        this.navigationPopup.setTarget(this);
//        this.navigationPopup.setAction(new NSSelector("navigationPopupSelectionChanged", new Class[]{Object.class}));
//    }

//    public void navigationPopupSelectionChanged(NSComboBox sender) {
//        if(this.navigationPopup.stringValue().length() != 0) {
//            this.background(new BackgroundAction() {
//                final Path dir = (Path)workdir.clone();
//                final String filename = navigationPopup.stringValue();
//
//                public void run() {
//                    if (filename.charAt(0) != '/') {
//                        dir.setPath(workdir.getAbsolute(), filename);
//                    }
//                    else {
//                        dir.setPath(filename);
//                    }
//                    setWorkdir(dir);
//                }
//
//                public void cleanup() {
//                    if(workdir.getParent().equals(dir)) {
//                        setSelectedPath(workdir);
//                    }
//                }
//            });
//        }
//    }

    private NSTextField searchField; // IBOutlet

    public void setSearchField(NSTextField searchField) {
        this.searchField = searchField;
        NSNotificationCenter.defaultCenter().addObserver(this,
                new NSSelector("searchFieldTextDidChange", new Class[]{Object.class}),
                NSControl.ControlTextDidChangeNotification,
                this.searchField);
    }

    public void searchFieldTextDidChange(NSNotification notification) {
        NSDictionary userInfo = notification.userInfo();
        if(null != userInfo) {
            Object o = userInfo.allValues().lastObject();
            if(null != o) {
                final String searchString = ((NSText) o).string();
                this.setFileFilter(searchString);
                this.reloadData(true);
            }
        }
    }

    // ----------------------------------------------------------
    // Manage Bookmarks
    // ----------------------------------------------------------

    public void connectBookmarkButtonClicked(final Object sender) {
        if(bookmarkTable.numberOfSelectedRows() == 1) {
            final Host selected = (Host) HostCollection.instance().get(bookmarkTable.selectedRow());
            CDBrowserController.this.mount(selected);
            if(Preferences.instance().getBoolean("browser.closeDrawer")) {
                bookmarkDrawer.close();
            }
        }
    }

    private NSButton editBookmarkButton; // IBOutlet

    public void setEditBookmarkButton(NSButton editBookmarkButton) {
        this.editBookmarkButton = editBookmarkButton;
        this.editBookmarkButton.setEnabled(false);
        this.editBookmarkButton.setTarget(this);
        this.editBookmarkButton.setAction(new NSSelector("editBookmarkButtonClicked", new Class[]{Object.class}));
    }

    public void editBookmarkButtonClicked(final Object sender) {
        this.bookmarkDrawer.open();
        CDBookmarkController c = CDBookmarkController.Factory.create(
                (Host) HostCollection.instance().get(bookmarkTable.selectedRow())
        );
        c.window().makeKeyAndOrderFront(null);
    }

    private NSButton addBookmarkButton; // IBOutlet

    public void setAddBookmarkButton(NSButton addBookmarkButton) {
        this.addBookmarkButton = addBookmarkButton;
        this.addBookmarkButton.setTarget(this);
        this.addBookmarkButton.setAction(new NSSelector("addBookmarkButtonClicked", new Class[]{Object.class}));
    }

    public void addBookmarkButtonClicked(final Object sender) {
        this.bookmarkDrawer.open();
        Host item;
        if(this.isMounted()) {
            item = (Host) this.session.getHost().clone();
            item.setDefaultPath(this.workdir().getAbsolute());
        }
        else {
            item = new Host(Preferences.instance().getProperty("connection.protocol.default"),
                    "localhost",
                    Preferences.instance().getInteger("connection.port.default"));
        }
        HostCollection.instance().add(item);
        final int index = HostCollection.instance().lastIndexOf(item);
        this.bookmarkTable.selectRow(index, false);
        this.bookmarkTable.scrollRowToVisible(index);
        CDBookmarkController c = CDBookmarkController.Factory.create(item);
        c.window().makeKeyAndOrderFront(null);
    }

    private NSButton deleteBookmarkButton; // IBOutlet

    public void setDeleteBookmarkButton(NSButton deleteBookmarkButton) {
        this.deleteBookmarkButton = deleteBookmarkButton;
        this.deleteBookmarkButton.setEnabled(false);
        this.deleteBookmarkButton.setTarget(this);
        this.deleteBookmarkButton.setAction(new NSSelector("deleteBookmarkButtonClicked",
                new Class[]{Object.class}));
    }

    public void deleteBookmarkButtonClicked(final Object sender) {
        this.bookmarkDrawer.open();
        NSEnumerator iterator = this.bookmarkTable.selectedRowEnumerator();
        int[] indexes = new int[this.bookmarkTable.numberOfSelectedRows()];
        int i = 0;
        while(iterator.hasMoreElements()) {
            indexes[i] = ((Number) iterator.nextElement()).intValue();
            i++;
        }
        this.bookmarkTable.deselectAll(null);
        int j = 0;
        for(i = 0; i < indexes.length; i++) {
            int row = indexes[i] - j;
            this.bookmarkTable.selectRow(row, false);
            this.bookmarkTable.scrollRowToVisible(row);
            Host host = (Host) HostCollection.instance().get(row);
            switch(NSAlertPanel.runCriticalAlert(NSBundle.localizedString("Delete Bookmark", ""),
                    NSBundle.localizedString("Do you want to delete the selected bookmark?", "")
                            + " (" + host.getNickname() + ")",
                    NSBundle.localizedString("Delete", ""),
                    NSBundle.localizedString("Cancel", ""),
                    null)) {
                case CDSheetCallback.DEFAULT_OPTION:
                    HostCollection.instance().remove(row);
                    j++;
            }
        }
        this.bookmarkTable.deselectAll(null);
    }

    // ----------------------------------------------------------
    // Browser navigation
    // ----------------------------------------------------------

    private static final int NAVIGATION_LEFT_SEGMENT_BUTTON = 0;
    private static final int NAVIGATION_RIGHT_SEGMENT_BUTTON = 1;

    private static final int NAVIGATION_UP_SEGMENT_BUTTON = 0;

    private NSSegmentedControl navigationButton; // IBOutlet

    public void setNavigationButton(NSSegmentedControl navigationButton) {
        this.navigationButton = navigationButton;
        this.navigationButton.setTarget(this);
        this.navigationButton.setAction(new NSSelector("navigationButtonClicked", new Class[]{Object.class}));
    }

    public void navigationButtonClicked(NSSegmentedControl sender) {
        switch(sender.selectedSegment()) {
            case NAVIGATION_LEFT_SEGMENT_BUTTON: {
                this.backButtonClicked(sender);
                break;
            }
            case NAVIGATION_RIGHT_SEGMENT_BUTTON: {
                this.forwardButtonClicked(sender);
                break;
            }
        }
    }

    public void backButtonClicked(final Object sender) {
        final Path selected = this.getPreviousPath();
        if(selected != null) {
            final Path previous = this.workdir();
            this.background(new BackgroundAction() {
                public void run() {
                    setWorkdir(selected);
                }

                public void cleanup() {
                    if(previous.getParent().equals(selected)) {
                        setSelectedPath(previous);
                    }
                }
            });
        }
    }

    public void forwardButtonClicked(final Object sender) {
        final Path selected = this.getForwardPath();
        if(selected != null) {
            this.background(new BackgroundAction() {
                public void run() {
                    setWorkdir(selected);
                }

                public void cleanup() {
                    ;
                }
            });
        }
    }

    private NSSegmentedControl upButton; // IBOutlet

    public void setUpButton(NSSegmentedControl upButton) {
        this.upButton = upButton;
        this.upButton.setTarget(this);
        this.upButton.setAction(new NSSelector("upButtonClicked", new Class[]{Object.class}));
    }

    public void upButtonClicked(final Object sender) {
        final Path previous = this.workdir();
        this.background(new BackgroundAction() {
            public void run() {
                setWorkdir((Path) previous.getParent());
            }

            public void cleanup() {
                setSelectedPath(previous);
            }
        });
    }

    private Path workdir;

    private NSPopUpButton pathPopupButton; // IBOutlet

    public void setPathPopup(NSPopUpButton pathPopupButton) {
        this.pathPopupButton = pathPopupButton;
        this.pathPopupButton.setTarget(this);
        this.pathPopupButton.setAction(new NSSelector("pathPopupSelectionChanged", new Class[]{Object.class}));
    }

    public void pathPopupSelectionChanged(final Object sender) {
        final Path selected = (Path) pathPopupButton.itemAtIndex(
                pathPopupButton.indexOfSelectedItem()).representedObject();
        final Path previous = this.workdir();
        if(selected != null) {
            this.background(new BackgroundAction() {
                public void run() {
                    setWorkdir(selected);
                }

                public void cleanup() {
                    if(previous.getParent().equals(selected)) {
                        setSelectedPath(previous);
                    }
                }
            });
        }
    }

    private NSPopUpButton encodingPopup;

    public void setEncodingPopup(NSPopUpButton encodingPopup) {
        this.encodingPopup = encodingPopup;
        this.encodingPopup.setTarget(this);
        this.encodingPopup.setAction(new NSSelector("encodingButtonClicked", new Class[]{Object.class}));
        this.encodingPopup.removeAllItems();
        this.encodingPopup.addItemsWithTitles(new NSArray(
                ((CDMainController) NSApplication.sharedApplication().delegate()).availableCharsets()));
        this.encodingPopup.selectItemWithTitle(Preferences.instance().getProperty("browser.charset.encoding"));
    }

    public void encodingButtonClicked(final NSPopUpButton sender) {
        this.encodingChanged(sender.titleOfSelectedItem());
    }

    public void encodingMenuClicked(final NSMenuItem sender) {
        this.encodingChanged(sender.title());
    }

    public void encodingChanged(final String encoding) {
        if(null == encoding) {
            return;
        }
        this.setEncoding(encoding);
        if(this.isMounted()) {
            if(this.session.getEncoding().equals(encoding)) {
                return;
            }
            if(this.isBusy()) {
                this.interrupt();
            }
            this.background(new BackgroundAction() {
                public void run() {
                    unmount(false);
                }

                public void cleanup() {
                    session.getHost().setEncoding(encoding);
                    reloadButtonClicked(null);
                }
            });
        }
    }

    /**
     * @param encoding
     */
    private void setEncoding(final String encoding) {
        this.encodingPopup.selectItemWithTitle(encoding);
    }

    // ----------------------------------------------------------
    // Drawers
    // ----------------------------------------------------------

    public void toggleBookmarkDrawer(final Object sender) {
        this.bookmarkDrawer.toggle(this);
        if(this.bookmarkDrawer.state() == NSDrawer.OpenState || this.bookmarkDrawer.state() == NSDrawer.OpeningState) {
            this.window.makeFirstResponder(this.bookmarkTable);
            if(this.isMounted()) {
                int row = HostCollection.instance().indexOf(this.getSession().getHost());
                if(row != -1) {
                    this.bookmarkTable.selectRow(row, false);
                    this.bookmarkTable.scrollRowToVisible(row);
                }
            }
        }
        else {
            if(this.isMounted()) {
                this.getFocus();
            }
            else {
                if(this.window.toolbar().isVisible()) {
                    this.window.makeFirstResponder(this.quickConnectPopup);
                }
            }
        }
    }

    // ----------------------------------------------------------
    // Status
    // ----------------------------------------------------------

    private NSProgressIndicator spinner; // IBOutlet

    public void setSpinner(NSProgressIndicator spinner) {
        this.spinner = spinner;
        this.spinner.setDisplayedWhenStopped(false);
        this.spinner.setIndeterminate(true);
        this.spinner.setUsesThreadedAnimation(true);
    }

    private NSTextField statusLabel; // IBOutlet

    public void setStatusLabel(NSTextField statusLabel) {
        this.statusLabel = statusLabel;
    }

    private NSButton securityLabel; // IBOutlet

    public void setSecurityLabel(NSButton securityLabel) {
        this.securityLabel = securityLabel;
        this.securityLabel.setImage(NSImage.imageNamed("unlocked.tiff"));
        this.securityLabel.setEnabled(false);
        this.securityLabel.setTarget(this);
        this.securityLabel.setAction(new NSSelector("securityLabelClicked", new Class[]{Object.class}));
    }

    public void securityLabelClicked(final Object sender) {
        CDWindowController c = new CDWindowController() {
            public void awakeFromNib() {
                this.window().setTitle(CDBrowserController.this.window().title());
                this.window().center();
                this.window().makeKeyAndOrderFront(null);
            }

            private NSTextView textView; // IBOutlet

            public void setTextView(NSTextView textView) {
                this.textView = textView;
                this.textView.textStorage().appendAttributedString(
                        new NSAttributedString(session.getSecurityInformation(), FIXED_WITH_FONT_ATTRIBUTES));
            }

            private NSButton alertIcon; // IBOutlet

            public void setAlertIcon(NSButton alertIcon) {
                this.alertIcon = alertIcon;
                this.alertIcon.setHidden(true);
            }

            private NSTextField alertLabel; // IBOutlet

            public void setAlertLabel(NSTextField alertLabel) {
                this.alertLabel = alertLabel;
                if(session instanceof FTPSSession) {
                    X509Certificate[] certificates = ((FTPSSession) session).getTrustManager().getAcceptedIssuers();
                    for(int i = 0; i < certificates.length; i++) {
                        try {
                            certificates[i].checkValidity();
                        }
                        catch(CertificateNotYetValidException e) {
                            log.warn(e.getMessage());
                            this.alertIcon.setHidden(false);
                            this.alertLabel.setStringValue(NSBundle.localizedString("Certificate not yet valid", "")
                                    + ": " + e.getMessage());
                        }
                        catch(CertificateExpiredException e) {
                            log.warn(e.getMessage());
                            this.alertIcon.setHidden(false);
                            this.alertLabel.setStringValue(NSBundle.localizedString("Certificate expired", "")
                                    + ": " + e.getMessage());
                        }
                    }
                }
            }
        };
        synchronized(NSApplication.sharedApplication()) {
            if(!NSApplication.loadNibNamed("Security", c)) {
                log.fatal("Couldn't load Security.nib");
            }
        }
    }

    // ----------------------------------------------------------
    // Selector methods for the toolbar items
    // ----------------------------------------------------------

    public void showTransferQueueClicked(final Object sender) {
        CDTransferController controller = CDTransferController.instance();
        controller.window().makeKeyAndOrderFront(null);
    }

    /**
     * Marks all expanded directories as invalid and tells the
     * browser table to reload its data
     *
     * @param sender
     */
    public void reloadButtonClicked(final Object sender) {
        if(this.isMounted()) {
            switch(this.browserSwitchView.selectedSegment()) {
                case LIST_VIEW: {
                    this.workdir().invalidate();
                    break;
                }
                case OUTLINE_VIEW: {
                    this.workdir().invalidate();
                    for(int i = 0; i < this.browserOutlineView.numberOfRows(); i++) {
                        Path p = (Path) this.browserOutlineView.itemAtRow(i);
                        if(null == p) {
                            break;
                        }
                        if(p.attributes.isDirectory()) {
                            p.invalidate();
                        }
                    }
                    break;
                }
            }
            final Path workdir = this.workdir();
            this.background(new BackgroundAction() {
                public void run() {
                    setWorkdir(workdir);
                }

                public void cleanup() {
                    ;
                }
            });
        }
    }

    /**
     * @param source      The original file to duplicate
     * @param destination The destination of the duplicated file
     * @param edit        Open the duplicated file in the external editor
     */
    protected void duplicatePath(final Path source, final Path destination, boolean edit) {
        this.duplicatePaths(Collections.singletonMap(source, destination), edit);
    }

    /**
     * @param selected A map with the original files as the key and the destination
     *                 files as the value
     * @param edit     Open the duplicated files in the external editor
     */
    protected void duplicatePaths(final Map selected, final boolean edit) {
        final Map normalized = this.checkHierarchy(selected);
        this.checkOverwrite(normalized.values(), new BackgroundAction() {
            public void run() {
                Iterator sourcesIter = normalized.keySet().iterator();
                Iterator destinationsIter = normalized.values().iterator();
                for(; sourcesIter.hasNext();) {
                    final Path source = (Path) sourcesIter.next();
                    final Path destination = (Path) destinationsIter.next();
                    final Local local = new Local(NSPathUtilities.temporaryDirectory(),
                            destination.getName());
                    TransferOptions options = new TransferOptions();
                    options.closeSession = false;
                    try {
                        source.setLocal(local);
                        DownloadTransfer download = new DownloadTransfer(source);
                        download.start(new TransferPrompt() {
                            public TransferAction prompt(Transfer transfer) {
                                return TransferAction.ACTION_OVERWRITE;
                            }
                        }, options);
                        if(!isConnected()) {
                            break;
                        }
                        source.setLocal(null);
                        destination.setLocal(local);
                        UploadTransfer upload = new UploadTransfer(destination);
                        upload.start(new TransferPrompt() {
                            public TransferAction prompt(Transfer transfer) {
                                return TransferAction.ACTION_OVERWRITE;
                            }
                        }, options);
                        if(!isConnected()) {
                            break;
                        }
                    }
                    finally {
                        local.delete(true);
                    }
                }
            }

            public void cleanup() {
                for(Iterator iter = normalized.values().iterator(); iter.hasNext();) {
                    Path duplicate = (Path) iter.next();
                    if(edit) {
                        Editor editor = new Editor(CDBrowserController.this);
                        editor.open(duplicate);
                    }
                    if(duplicate.getName().charAt(0) == '.') {
                        setShowHiddenFiles(true);
                    }
                }
                reloadData(normalized.values());
            }
        });
    }

    /**
     * @param path    The existing file
     * @param renamed The renamed file
     */
    protected void renamePath(final Path path, final Path renamed) {
        this.renamePaths(Collections.singletonMap(path, renamed));
    }

    /**
     * @param selected A map with the original files as the key and the destination
     *                 files as the value
     */
    protected void renamePaths(final Map selected) {
        final Map normalized = this.checkHierarchy(selected);
        this.checkMove(normalized.values(), new BackgroundAction() {
            public void run() {
                Iterator originalIterator = normalized.keySet().iterator();
                Iterator renamedIterator = normalized.values().iterator();
                while(originalIterator.hasNext()) {
                    ((Path) originalIterator.next()).rename(((AbstractPath) renamedIterator.next()).getAbsolute());
                    if(!isConnected()) {
                        break;
                    }
                }
            }

            public void cleanup() {
                reloadData(normalized.values());
            }
        });
    }

    /**
     * Displays a warning dialog about already existing files
     *
     * @param selected The files to check for existance
     */
    private void checkOverwrite(final java.util.Collection selected, final BackgroundAction action) {
        if(selected.size() > 0) {
            StringBuffer alertText = new StringBuffer(
                    NSBundle.localizedString("A file with the same name already exists. Do you want to replace the existing file?", ""));
            int i = 0;
            Iterator iter = null;
            boolean alert = false;
            for(iter = selected.iterator(); i < 10 && iter.hasNext();) {
                Path item = (Path) iter.next();
                if(item.exists()) {
                    alertText.append("\n" + Character.toString('\u2022') + " " + item.getName());
                    alert = true;
                }
                i++;
            }
            if(iter.hasNext()) {
                alertText.append("\n" + Character.toString('\u2022') + " ...)");
            }
            if(alert) {
                NSWindow sheet = NSAlertPanel.criticalAlertPanel(
                        NSBundle.localizedString("Overwrite", "Alert sheet title"), //title
                        alertText.toString(),
                        NSBundle.localizedString("Overwrite", "Alert sheet default button"), // defaultbutton
                        NSBundle.localizedString("Cancel", "Alert sheet alternate button"), //alternative button
                        null //other button
                );
                CDSheetController c = new CDSheetController(this, sheet) {
                    public void callback(final int returncode) {
                        if(returncode == DEFAULT_OPTION) {
                            CDBrowserController.this.background(action);
                        }
                    }
                };
                c.beginSheet(true);
            }
            else {
                this.background(action);
            }
        }
    }

    /**
     * Displays a warning dialog about files to be moved
     *
     * @param selected The files to check for existance
     */
    private void checkMove(final java.util.Collection selected, final BackgroundAction action) {
        if(selected.size() > 0) {
            if(Preferences.instance().getBoolean("browser.confirmMove")) {
                StringBuffer alertText = new StringBuffer(
                        NSBundle.localizedString("Do you want to move the selected files?", ""));
                int i = 0;
                Iterator iter = null;
                for(iter = selected.iterator(); i < 10 && iter.hasNext();) {
                    Path item = (Path) iter.next();
                    alertText.append("\n" + Character.toString('\u2022') + " " + item.getName());
                    i++;
                }
                if(iter.hasNext()) {
                    alertText.append("\n" + Character.toString('\u2022') + " ...)");
                }
                NSWindow sheet = NSAlertPanel.criticalAlertPanel(
                        NSBundle.localizedString("Move", "Alert sheet title"), //title
                        alertText.toString(),
                        NSBundle.localizedString("Move", "Alert sheet default button"), // defaultbutton
                        NSBundle.localizedString("Cancel", "Alert sheet alternate button"), //alternative button
                        null //other button
                );
                CDSheetController c = new CDSheetController(this, sheet) {
                    public void callback(final int returncode) {
                        if(returncode == DEFAULT_OPTION) {
                            checkOverwrite(selected, action);
                        }
                    }
                };
                c.beginSheet(true);
            }
            else {
                this.checkOverwrite(selected, action);
            }
        }
    }

    /**
     * Prunes the map of selected files. Files which are a child of an already included directory
     * are removed from the returned map.
     */
    private Map checkHierarchy(final Map selected) {
        final Map normalized = new HashMap();
        Iterator sourcesIter = selected.keySet().iterator();
        Iterator destinationsIter = selected.values().iterator();
        while(sourcesIter.hasNext()) {
            Path f = (Path) sourcesIter.next();
            Path r = (Path) destinationsIter.next();
            boolean duplicate = false;
            for(Iterator normalizedIter = normalized.keySet().iterator(); normalizedIter.hasNext();) {
                Path n = (Path) normalizedIter.next();
                if(f.isChild(n)) {
                    // The selected file is a child of a directory
                    // already included for deletion
                    duplicate = true;
                    break;
                }
                if(n.isChild(f)) {
                    // Remove the previously added file as it is a child 
                    // of the currently evaluated file
                    normalizedIter.remove();
                }
            }
            if(!duplicate) {
                normalized.put(f, r);
            }
        }
        return normalized;
    }

    /**
     * Prunes the list of selected files. Files which are a child of an already included directory
     * are removed from the returned list.
     */
    private List checkHierarchy(final List selected) {
        final List normalized = new Collection();
        for(Iterator iter = selected.iterator(); iter.hasNext();) {
            Path f = (Path) iter.next();
            boolean duplicate = false;
            for(Iterator normalizedIter = normalized.iterator(); normalizedIter.hasNext();) {
                Path n = (Path) normalizedIter.next();
                if(f.isChild(n)) {
                    // The selected file is a child of a directory
                    // already included for deletion
                    duplicate = true;
                    break;
                }
            }
            if(!duplicate) {
                normalized.add(f);
            }
        }
        return normalized;
    }

    /**
     * Recursively deletes the file
     *
     * @param file
     */
    public void deletePath(final Path file) {
        this.deletePaths(Collections.singletonList(file));
    }

    /**
     * Recursively deletes the files
     *
     * @param selected The files selected in the browser to delete
     */
    public void deletePaths(final List selected) {
        final List normalized = this.checkHierarchy(selected);
        if(normalized.size() > 0) {
            StringBuffer alertText =
                    new StringBuffer(NSBundle.localizedString("Really delete the following files? This cannot be undone.", "Confirm deleting files."));
            int i = 0;
            Iterator iter = null;
            for(iter = normalized.iterator(); i < 10 && iter.hasNext();) {
                alertText.append("\n" + Character.toString('\u2022') + " " + ((Path) iter.next()).getName());
                i++;
            }
            if(iter.hasNext()) {
                alertText.append("\n" + Character.toString('\u2022') + " " + "(...)");
            }
            NSWindow sheet = NSAlertPanel.criticalAlertPanel(NSBundle.localizedString("Delete", "Alert sheet title"), //title
                    alertText.toString(),
                    NSBundle.localizedString("Delete", "Alert sheet default button"), // defaultbutton
                    NSBundle.localizedString("Cancel", "Alert sheet alternate button"), //alternative button
                    null //other button
            );
            CDSheetController c = new CDSheetController(this, sheet) {
                public void callback(final int returncode) {
                    if(returncode == DEFAULT_OPTION) {
                        CDBrowserController.this.deletePathsImpl(normalized);
                    }
                }
            };
            c.beginSheet(false);
        }
    }

    private void deletePathsImpl(final List files) {
        this.background(new BackgroundAction() {
            public void run() {
                for(Iterator iter = files.iterator(); iter.hasNext();) {
                    Path f = (Path) iter.next();
                    f.delete();
                    if(!isConnected()) {
                        break;
                    }
                }
            }

            public void cleanup() {
                reloadData(false);
            }
        });
    }

    /**
     * @param selected
     * @return True if the selected path is editable (not a directory and no known binary file)
     */
    private boolean isEditable(final Path selected) {
        if(selected.attributes.isFile()) {
            return !selected.getBinaryFiletypePattern().matcher(selected.getName()).matches();
        }
        return false;
    }

    public void gotoButtonClicked(final Object sender) {
        CDSheetController controller = new CDGotoController(this);
        controller.beginSheet(false);
    }

    public void createFileButtonClicked(final Object sender) {
        CDSheetController controller = new CDCreateFileController(this);
        controller.beginSheet(false);
    }

    public void duplicateFileButtonClicked(final Object sender) {
        if(this.getSelectionCount() > 0) {
            CDSheetController controller = new CDDuplicateFileController(this);
            controller.beginSheet(false);
        }
    }

    public void createFolderButtonClicked(final Object sender) {
        CDSheetController controller = new CDFolderController(this);
        controller.beginSheet(false);
    }

    public void renameFileButtonClicked(final Object sender) {
        if(this.getSelectionCount() == 1) {
            final NSTableView browser = this.getSelectedBrowserView();
            browser.editLocation(
                    browser.columnWithIdentifier(CDBrowserTableDataSource.FILENAME_COLUMN),
                    browser.selectedRow(), null, true);
            //editColumn:mBrowserEditingColumn row:mBrowserEditingRow withEvent:nil select:YES
        }
    }

    public void sendCustomCommandClicked(final Object sender) {
        CDSheetController controller = new CDCommandController(this, this.session);
        controller.beginSheet(false);
    }

    public void editMenuClicked(final NSMenuItem sender) {
        for(Iterator i = this.getSelectedPaths().iterator(); i.hasNext();) {
            final Path selected = (Path) i.next();
            if(this.isEditable(selected)) {
                Object identifier = Editor.SUPPORTED_EDITORS.get(sender.title());
                if(identifier != null) {
                    Editor editor = new Editor(this);
                    editor.open(selected, (String) identifier);
                }
            }
        }
    }

    public void editButtonClicked(final Object sender) {
        for(Iterator i = this.getSelectedPaths().iterator(); i.hasNext();) {
            final Path selected = (Path) i.next();
            if(this.isEditable(selected)) {
                Editor editor = new Editor(this);
                editor.open(selected);
            }
        }
    }

    private CDInfoController inspector = null;

    public void infoButtonClicked(final Object sender) {
        if(this.getSelectionCount() > 0) {
            List files = this.getSelectedPaths();
            if(Preferences.instance().getBoolean("browser.info.isInspector")) {
                if(null == this.inspector || null == this.inspector.window()) {
                    this.inspector = CDInfoController.Factory.create(this, files);
                }
                else {
                    this.inspector.setFiles(files);
                }
                this.inspector.window().makeKeyAndOrderFront(null);
            }
            else {
                CDInfoController c = CDInfoController.Factory.create(this, files);
                c.window().makeKeyAndOrderFront(null);
            }
        }
    }

    public void deleteFileButtonClicked(final Object sender) {
        this.deletePaths(this.getSelectedPaths());
    }

    private static String lastSelectedDownloadDirectory = null;

    private NSOpenPanel downloadToPanel;

    public void downloadToButtonClicked(final Object sender) {
        downloadToPanel = NSOpenPanel.openPanel();
        downloadToPanel.setCanChooseDirectories(true);
        downloadToPanel.setCanCreateDirectories(true);
        downloadToPanel.setCanChooseFiles(false);
        downloadToPanel.setAllowsMultipleSelection(false);
        downloadToPanel.setPrompt(NSBundle.localizedString("Download To", ""));
        downloadToPanel.setTitle(NSBundle.localizedString("Download To", ""));
        downloadToPanel.beginSheetForDirectory(
                lastSelectedDownloadDirectory, //trying to be smart
                null,
                null,
                this.window,
                this,
                new NSSelector("downloadToPanelDidEnd", new Class[]{NSOpenPanel.class, int.class, Object.class}),
                null);
    }


    public void downloadToPanelDidEnd(NSOpenPanel sheet, int returncode, Object contextInfo) {
        sheet.close();
        if(returncode == CDSheetCallback.DEFAULT_OPTION) {
            final Session session = this.getTransferSession();
            final List roots = new Collection();
            for(Iterator i = this.getSelectedPaths().iterator(); i.hasNext();) {
                Path path = (Path) ((Path) i.next()).clone(session);
                path.setLocal(new Local(sheet.filename(), path.getLocal().getName()));
                roots.add(path);
            }
            final Transfer q = new DownloadTransfer(roots);
            this.transfer(q);
        }
        lastSelectedDownloadDirectory = sheet.filename();
        downloadToPanel = null;
    }


    public void downloadAsButtonClicked(final Object sender) {
        final Session session = this.getTransferSession();
        for(Iterator i = this.getSelectedPaths().iterator(); i.hasNext();) {
            Path path = (Path) ((Path) i.next()).clone(session);
            NSSavePanel panel = NSSavePanel.savePanel();
            panel.setMessage(NSBundle.localizedString("Download the selected file to...", ""));
            panel.setNameFieldLabel(NSBundle.localizedString("Download As:", ""));
            panel.setPrompt(NSBundle.localizedString("Download", ""));
            panel.setTitle(NSBundle.localizedString("Download", ""));
            panel.setCanCreateDirectories(true);
            panel.beginSheetForDirectory(null,
                    path.getLocal().getName(),
                    this.window,
                    this,
                    new NSSelector("downloadAsPanelDidEnd", new Class[]{NSOpenPanel.class, int.class, Object.class}),
                    path);
        }
    }

    public void downloadAsPanelDidEnd(NSSavePanel sheet, int returncode, Object contextInfo) {
        sheet.close();
        if(returncode == CDSheetCallback.DEFAULT_OPTION) {
            String filename;
            if((filename = sheet.filename()) != null) {
                Path path = (Path) contextInfo;
                path.setLocal(new Local(filename));
                final Transfer q = new DownloadTransfer(path);
                this.transfer(q);
            }
        }
    }

    private NSOpenPanel syncPanel;

    public void syncButtonClicked(final Object sender) {
        final Path selection;
        if(this.getSelectionCount() == 1 &&
                this.getSelectedPath().attributes.isDirectory()) {
            selection = this.getSelectedPath();
        }
        else {
            selection = this.workdir();
        }
        syncPanel = NSOpenPanel.openPanel();
        syncPanel.setCanChooseDirectories(selection.attributes.isDirectory());
        syncPanel.setCanChooseFiles(selection.attributes.isFile());
        syncPanel.setCanCreateDirectories(true);
        syncPanel.setAllowsMultipleSelection(false);
        syncPanel.setMessage(NSBundle.localizedString("Synchronize", "")
                + " " + selection.getName() + " "
                + NSBundle.localizedString("with", "Synchronize <file> with <file>") + "...");
        syncPanel.setPrompt(NSBundle.localizedString("Choose", ""));
        syncPanel.setTitle(NSBundle.localizedString("Synchronize", ""));
        syncPanel.beginSheetForDirectory(null,
                null,
                null,
                this.window, //parent window
                this,
                new NSSelector("syncPanelDidEnd", new Class[]{NSOpenPanel.class, int.class, Object.class}),
                selection //context info
        );
    }

    public void syncPanelDidEnd(NSOpenPanel sheet, int returncode, Object contextInfo) {
        sheet.close();
        if(returncode == CDSheetCallback.DEFAULT_OPTION) {
            final Path selection = (Path) contextInfo;
            if(sheet.filenames().count() > 0) {
                Path root = (Path) selection.clone(this.getTransferSession());
                root.setLocal(new Local((String) sheet.filenames().lastObject()));
                final Transfer q = new SyncTransfer(root);
                this.transfer(q, selection);
            }
        }
        syncPanel = null;
    }

    public void downloadButtonClicked(final Object sender) {
        final Session session = this.getTransferSession();
        final List roots = new Collection();
        for(Iterator i = this.getSelectedPaths().iterator(); i.hasNext();) {
            Path path = (Path) ((Path) i.next()).clone(session);
            roots.add(path);
        }
        final Transfer q = new DownloadTransfer(roots);
        this.transfer(q);
    }

    private static String lastSelectedUploadDirectory = null;

    private NSOpenPanel uploadPanel;

    public void uploadButtonClicked(final Object sender) {
        uploadPanel = NSOpenPanel.openPanel();
        uploadPanel.setCanChooseDirectories(true);
        uploadPanel.setCanCreateDirectories(false);
        uploadPanel.setCanChooseFiles(true);
        uploadPanel.setAllowsMultipleSelection(true);
        uploadPanel.setPrompt(NSBundle.localizedString("Upload", ""));
        uploadPanel.setTitle(NSBundle.localizedString("Upload", ""));
        uploadPanel.beginSheetForDirectory(
                lastSelectedUploadDirectory, //trying to be smart
                null,
                null,
                this.window,
                this,
                new NSSelector("uploadPanelDidEnd", new Class[]{NSOpenPanel.class, int.class, Object.class}),
                null);
    }

    public void uploadPanelDidEnd(NSOpenPanel sheet, int returncode, Object contextInfo) {
        sheet.close();
        if(returncode == CDSheetCallback.DEFAULT_OPTION) {
            Path workdir = this.getSelectedPath();
            if(null == workdir || !workdir.attributes.isDirectory()) {
                workdir = this.workdir();
            }
            // selected files on the local filesystem
            NSArray selected = sheet.filenames();
            java.util.Enumeration iterator = selected.objectEnumerator();
            final Session session = this.getTransferSession();
            final List roots = new Collection();
            while(iterator.hasMoreElements()) {
                roots.add(PathFactory.createPath(session,
                        workdir.getAbsolute(),
                        new Local((String) iterator.nextElement())));
            }
            final Transfer q = new UploadTransfer(roots);
            this.transfer(q, workdir);
        }
        lastSelectedUploadDirectory = new File(sheet.filename()).getParent();
        uploadPanel = null;
    }

    /**
     * @return The session to be used for file transfers
     */
    protected Session getTransferSession() {
        if(this.session.getMaxConnections() == 1) {
            return this.session;
        }
        return (Session) this.getSession().clone();
    }

    /**
     * @param transfer
     * @param workdir  Will reload the data for this directory in the browser after the
     *                 transfer completes
     * @see #transfer(Transfer)
     */
    protected void transfer(final Transfer transfer, final Path workdir) {
        final TransferListener l;
        transfer.addListener(l = new TransferAdapter() {
            public void transferDidEnd() {
                if(isMounted()) {
                    workdir.invalidate();
                    if(!transfer.isCanceled()) {
                        invoke(new Runnable() {
                            public void run() {
                                reloadData(true);
                            }
                        });
                    }
                }
            }
        });
        this.addListener(new CDWindowListener() {
            public void windowWillClose() {
                transfer.removeListener(l);
            }
        });
        this.transfer(transfer);
    }

    /**
     * Trasnfers the files either using the queue or using
     * the browser session if #connection.pool.max is 1
     *
     * @param transfer
     * @see CDTransferController
     */
    protected void transfer(final Transfer transfer) {
        if(transfer.getSession().getMaxConnections() == 1) {
            final TransferListener l;
            transfer.addListener(l = new TransferAdapter() {
                public void transferDidEnd() {
                    if(transfer.isComplete() && !transfer.isCanceled()) {
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
                    }
                }
            });
            this.addListener(new CDWindowListener() {
                public void windowWillClose() {
                    transfer.removeListener(l);
                }
            });
            this.background(new BackgroundAction() {
                public void run() {
                    transfer.start(CDTransferPrompt.create(CDBrowserController.this, transfer));
                }

                public void cleanup() {
                    ;
                }
            });
        }
        else {
            CDTransferController.instance().startTransfer(transfer);
        }
    }

    public void insideButtonClicked(final Object sender) {
        if(this.getSelectionCount() > 0) {
            final Path selected = this.getSelectedPath(); //last row selected
            if(selected.attributes.isDirectory()) {
                this.background(new BackgroundAction() {
                    public void run() {
                        setWorkdir(selected);
                    }

                    public void cleanup() {
                        ;
                    }
                });
            }
            else if(selected.attributes.isFile() || this.getSelectionCount() > 1) {
                if(Preferences.instance().getBoolean("browser.doubleclick.edit")) {
                    this.editButtonClicked(null);
                }
                else {
                    this.downloadButtonClicked(null);
                }
            }
        }
    }

    public void connectButtonClicked(final Object sender) {
        final CDSheetController controller = CDConnectionController.instance(this);
        this.addListener(new CDWindowListener() {
            public void windowWillClose() {
                controller.invalidate();
            }
        });
        controller.beginSheet(false);
    }

    public void interruptButtonClicked(final Object sender) {
        this.interrupt();
    }

    public void disconnectButtonClicked(final Object sender) {
        if(this.isBusy()) {
            // Interrupt any pending operation by forcefully closing the socket
            this.interrupt();
        }
        else {
            this.background(new BackgroundAction() {
                public void run() {
                    unmount(false);
                }

                public void cleanup() {
                    ;
                }
            });
        }
    }

    public void showHiddenFilesClicked(final NSMenuItem sender) {
        if(sender.state() == NSCell.OnState) {
            this.setShowHiddenFiles(false);
            sender.setState(NSCell.OffState);
        }
        else if(sender.state() == NSCell.OffState) {
            this.setShowHiddenFiles(true);
            sender.setState(NSCell.OnState);
        }
        if(this.isMounted()) {
            this.reloadData(true);
        }
    }

    /**
     * @return true if a connection is being opened or is already initialized
     */
    public boolean hasSession() {
        return this.session != null;
    }

    /**
     * @return This browser's session or null if not mounted
     */
    public Session getSession() {
        return this.session;
    }

    /**
     * @return true if the remote file system has been mounted
     */
    public boolean isMounted() {
        return this.hasSession() && this.workdir() != null;
    }

    private boolean interrupted;

    public boolean isInterrupted() {
        return this.interrupted;
    }

    /**
     * @return true if mounted and the connection to the server is alive
     */
    public boolean isConnected() {
        if(this.isMounted()) {
            return this.session.isConnected();
        }
        return false;
    }

    public void cut(final Object sender) {
        if(this.getSelectionCount() > 0) {
            final List roots = new Collection();
            for(Iterator i = this.getSelectedPaths().iterator(); i.hasNext();) {
                roots.add((Path) i.next());
            }
            final Transfer q = new DownloadTransfer(roots);
            // Writing data for private use when the item gets dragged to the transfer queue.
            NSPasteboard queuePboard = NSPasteboard.pasteboardWithName(CDPasteboards.TransferPasteboard);
            queuePboard.declareTypes(new NSArray(CDPasteboards.TransferPasteboardType), null);
            if(queuePboard.setPropertyListForType(new NSArray(q.getAsDictionary()), CDPasteboards.TransferPasteboardType)) {
                log.debug("TransferPasteboardType data sucessfully written to pasteboard");
            }
            Path p = this.getSelectedPath();
            NSPasteboard pboard = NSPasteboard.generalPasteboard();
            pboard.declareTypes(new NSArray(NSPasteboard.StringPboardType), null);
            if(!pboard.setStringForType(p.getAbsolute(), NSPasteboard.StringPboardType)) {
                log.error("Error writing absolute path of selected item to NSPasteboard.StringPboardType.");
            }
        }
    }

    public void paste(final Object sender) {
        final NSPasteboard pboard = NSPasteboard.pasteboardWithName(CDPasteboards.TransferPasteboard);
        if(pboard.availableTypeFromArray(new NSArray(CDPasteboards.TransferPasteboardType)) != null) {
            Object o = pboard.propertyListForType(CDPasteboards.TransferPasteboardType);// get the data from paste board
            if(o != null) {
                final Map files = new HashMap();
                Path parent = this.workdir();
                if(this.getSelectionCount() == 1) {
                    Path selected = this.getSelectedPath();
                    if(selected.attributes.isDirectory()) {
                        parent = selected;
                    }
                    else {
                        parent = (Path) selected.getParent();
                    }
                }
                final NSArray elements = (NSArray) o;
                for(int i = 0; i < elements.count(); i++) {
                    NSDictionary dict = (NSDictionary) elements.objectAtIndex(i);
                    Transfer q = TransferFactory.create(dict);
                    for(Iterator iter = q.getRoots().iterator(); iter.hasNext();) {
                        Path current = PathFactory.createPath(getSession(),
                                ((Path) iter.next()).getAbsolute());
                        Path renamed = PathFactory.createPath(getSession(),
                                parent.getAbsolute(), current.getName());
                        files.put(current, renamed);
                    }
                }
                this.renamePaths(files);
                pboard.declareTypes(null, null);
            }
        }
    }

    public void pasteFromFinder(final Object sender) {
        NSPasteboard pboard = NSPasteboard.generalPasteboard();
        if(pboard.availableTypeFromArray(new NSArray(NSPasteboard.FilenamesPboardType)) != null) {
            Object o = pboard.propertyListForType(NSPasteboard.FilenamesPboardType);
            if(o != null) {
                NSArray elements = (NSArray) o;
                final Path workdir = this.workdir();
                final Session session = this.getTransferSession();
                final List roots = new Collection();
                for(int i = 0; i < elements.count(); i++) {
                    Path p = PathFactory.createPath(session,
                            workdir.getAbsolute(),
                            new Local((String) elements.objectAtIndex(i)));
                    roots.add(p);
                }
                final Transfer q = new UploadTransfer(roots);
                if(q.numberOfRoots() > 0) {
                    this.transfer(q, workdir);
                }
            }
        }
    }

    public void copyURLButtonClicked(final Object sender) {
        String url = null;
        if(this.getSelectionCount() > 0) {
            url = this.getSelectedPath().toURL();
        }
        else {
            url = this.workdir().toURL();
        }
        if(null == url) {
            return;
        }
        NSPasteboard pboard = NSPasteboard.generalPasteboard();
        pboard.declareTypes(new NSArray(NSPasteboard.StringPboardType), null);
        if(!pboard.setStringForType(url, NSPasteboard.StringPboardType)) {
            log.error("Error writing URL to NSPasteboard.StringPboardType.");
        }
    }

    /**
     * A task is in progress; e.g. a file listing is expected from the server
     */
    private boolean activityRunning;

    /**
     * @return true if there is any network activity running in the background
     */
    public boolean isBusy() {
        return this.activityRunning;
    }

    /**
     * A lock to make sure that actions are not run in parallel
     */
    protected final Object backgroundLock = new Object();

    private BackgroundActionImpl backgroundAction = null;

    /**
     * Will queue up the <code>BackgroundAction</code> to be run in a background thread. Will be executed
     * as soon as no other previous <code>BackgroundAction</code> is pending.
     * Before the <code>BackgroundAction</code> is run, the progress indicator of this browser
     * is animated. While the <code>BackgroundAction</code> is executed, #isBusy will return true
     *
     * @param runnable The action to execute
     * @pre must always be invoked form the main interface thread
     * @see ch.cyberduck.ui.cocoa.CDWindowController#background(ch.cyberduck.ui.cocoa.threading.BackgroundActionImpl,Object)
     * @see #isBusy()
     */
    public void background(final BackgroundAction runnable) {
        super.background(backgroundAction = new BackgroundActionImpl(this) {
            public void prepare() {
                activityRunning = true;
                interrupted = false;
                spinner.startAnimation(this);
                session.addErrorListener(this);
                session.addTranscriptListener(this);
                super.prepare();
            }

            public void run() {
                runnable.run();
            }

            public void finish() {
                activityRunning = false;
                spinner.stopAnimation(this);
                if(hasSession()) {
                    // It is important _not_ to do this in #cleanup as otherwise
                    // the listeners are still registered when the next BackgroundAction
                    // is already running
                    session.removeTranscriptListener(this);
                    session.removeErrorListener(this);
                }
                super.finish();
            }

            public void cleanup() {
                runnable.cleanup();
            }

            public Session session() {
                return session;
            }
        }, backgroundLock);
    }

    private static final NSImage FOLDER_ICON = NSImage.imageNamed("folder16.tiff");
    private static final NSImage DISK_ICON = NSImage.imageNamed("disk.tiff");

    /**
     * Accessor to the working directory
     *
     * @return The current working directory or null if no file system is mounted
     */
    protected Path workdir() {
        return this.workdir;
    }

    /**
     * Sets the current working directory. This will udpate the path selection dropdown button
     * and also add this path to the browsing history. If the path cannot be a working directory (e.g. permission
     * issues trying to enter the directory), reloading the browser view is canceled and the working directory
     * not changed.
     *
     * @param path The new working directory to display or null to detach any working directory from the browser
     */
    public void setWorkdir(final Path path) {
        log.debug("setWorkdir:" + path);
        if(null == path) {
            // Clear the browser view if no working directory is given
            this.workdir = null;
            this.invoke(new Runnable() {
                public void run() {
//                    navigationPopup.setStringValue("");
                    pathPopupButton.removeAllItems();
                }
            });
            this.invoke(new Runnable() {
                public void run() {
                    reloadData(false);
                }
            });
            final Local bookmark = this.getRepresentedFile();
            if(bookmark != null && bookmark.exists()) {
                // Delete this history bookmark if there was any error connecting
                bookmark.delete();
            }
            this.window.setTitle(
                    (String) NSBundle.mainBundle().infoDictionary().objectForKey("CFBundleName"));
            this.window.setRepresentedFilename(""); //can't send null
            return;
        }
        if(!this.hasSession()) {
            // The connection has already been closed asynchronously;
            // this can happen if the user closes a connection that is about to be opened
            return;
        }
        if(path.isCached()) {
            //Reset the readable attribute
            path.childs().attributes().setReadable(true);
        }
        if(!path.childs().attributes().isReadable()) {
            // the path given cannot be read either because it doesn't exist
            // or you don't have permission; don't update browser view
            return;
        }
        // Remove any custom file filter
        this.setFileFilter(null);
        // Update the current working directory
        this.addPathToHistory(this.workdir = path);
        this.invoke(new Runnable() {
            public void run() {
//                navigationPopup.setStringValue(workdir().getAbsolute());
                pathPopupButton.removeAllItems();
                // Update the path selection menu above the browser
                if(isMounted()) {
                    Path p = workdir;
                    while(true) {
                        pathPopupButton.addItem(p.getAbsolute());
                        pathPopupButton.lastItem().setRepresentedObject(p);
                        if(p.isRoot()) {
                            pathPopupButton.lastItem().setImage(DISK_ICON);
                            break;
                        }
                        pathPopupButton.lastItem().setImage(FOLDER_ICON);
                        p = (Path) p.getParent();
                    }
                }
            }
        });
        this.invoke(new Runnable() {
            public void run() {
                // Mark the browser data source as dirty
                reloadData(false);
            }
        });
    }

    /**
     * Keeps a ordered backward history of previously visited paths
     */
    private List backHistory = new Collection();

    /**
     * Keeps a ordered forward history of previously visited paths
     */
    private List forwardHistory = new Collection();

    /**
     * @param p
     */
    public void addPathToHistory(Path p) {
//        if(!fullHistory.contains(p)) {
//            fullHistory.add(p);
//        }
        if(backHistory.size() > 0) {
            // Do not add if this was a reload
            if(p.equals(backHistory.get(backHistory.size() - 1))) {
                return;
            }
        }
        backHistory.add(p);
    }

    /**
     * Returns the prevously browsed path and moves it to the forward history
     *
     * @return The previously browsed path or null if there is none
     */
    public Path getPreviousPath() {
        int size = backHistory.size();
        if(size > 1) {
            forwardHistory.add(backHistory.get(size - 1));
            Path p = (Path) backHistory.get(size - 2);
            //delete the fetched path - otherwise we produce a loop
            backHistory.remove(size - 1);
            backHistory.remove(size - 2);
            return p;
        }
        else if(1 == size) {
            forwardHistory.add(backHistory.get(size - 1));
            return (Path) backHistory.get(size - 1);
        }
        return null;
    }

    /**
     * @return The last path browsed before #getPrevoiusPath was called
     * @see #getPreviousPath()
     */
    public Path getForwardPath() {
        int size = forwardHistory.size();
        if(size > 0) {
            Path p = (Path) forwardHistory.get(size - 1);
            forwardHistory.remove(size - 1);
            return p;
        }
        return null;
    }

    /**
     * @return The ordered array of prevoiusly visited directories
     */
    public Path[] getBackHistory() {
        return (Path[]) backHistory.toArray(new Path[backHistory.size()]);
    }

    /**
     * @return The ordered array of prevoiusly visited directories
     */
    public Path[] getForwardHistory() {
        return (Path[]) forwardHistory.toArray(new Path[forwardHistory.size()]);
    }

    /**
     * @return
     */
    public Path[] getFullHistory() {
        return (Path[]) session.cache().keys();
    }

    /**
     *
     */
    private ConnectionListener listener = null;

    /**
     * Initializes a session for the passed host. Setting up the listeners and adding any callback
     * controllers needed for login, trust management and hostkey verification.
     *
     * @param host
     * @return A session object bound to this browser controller
     */
    private Session init(final Host host) {
        if(this.hasSession()) {
            this.session.removeConnectionListener(listener);
            this.session = null;
        }
        this.session = SessionFactory.createSession(host);
        if(this.session instanceof ch.cyberduck.core.sftp.SFTPSession) {
            ((ch.cyberduck.core.sftp.SFTPSession) session).setHostKeyVerificationController(
                    new CDHostKeyController(this));
        }
        if(this.session instanceof ch.cyberduck.core.ftps.FTPSSession) {
            ((ch.cyberduck.core.ftps.FTPSSession) this.session).setTrustManager(
                    new CDX509TrustManagerController(this));
        }
        this.session.setLoginController(new CDLoginController(this));
        this.setWorkdir(null);
        this.setEncoding(this.session.getEncoding());
        this.window.setTitle(host.getProtocol() + ":" + host.getHostname());
        ((CDMainController) NSApplication.sharedApplication().delegate()).exportBookmark(host,
                this.getRepresentedFile());
        if(this.getRepresentedFile().exists()) {
            // Set the window title
            this.window.setRepresentedFilename(this.getRepresentedFile().getAbsolute());
        }
        this.session.addProgressListener(new ProgressListener() {
            public void message(final String msg) {
                invoke(new Runnable() {
                    public void run() {
                        // Update the status label at the bottom of the browser window
                        statusLabel.setAttributedStringValue(new NSAttributedString(msg,
                                TRUNCATE_MIDDLE_ATTRIBUTES));
                        statusLabel.display();
                    }
                });
            }
        });
        session.addConnectionListener(listener = new ConnectionAdapter() {
            public void connectionDidOpen() {
                getSelectedBrowserView().setNeedsDisplay();
                CDBrowserController.this.invoke(new Runnable() {
                    public void run() {
                        window.setTitle(host.getProtocol() + ":" + host.getCredentials().getUsername()
                                + "@" + host.getHostname());
                        if(Preferences.instance().getBoolean("browser.confirmDisconnect")) {
                            window.setDocumentEdited(true);
                        }
                        securityLabel.setImage(session.isSecure() ? NSImage.imageNamed("locked.tiff")
                                : NSImage.imageNamed("unlocked.tiff"));
                        securityLabel.setEnabled(true);
                    }
                });
                Growl.instance().notify("Connection opened", host.getHostname());
            }

            public void connectionWillClose() {
                ;
            }

            public void connectionDidClose() {
                getSelectedBrowserView().setNeedsDisplay();
                CDBrowserController.this.invoke(new Runnable() {
                    public void run() {
                        window.setDocumentEdited(false);
                        securityLabel.setImage(NSImage.imageNamed("unlocked.tiff"));
                        securityLabel.setEnabled(false);
                    }
                });
            }

            public void activityStarted() {
                CDBrowserController.this.invoke(new Runnable() {
                    public void run() {
                        statusLabel.display();
                        window.toolbar().validateVisibleItems();
                    }
                });
            }

            public void activityStopped() {
                CDBrowserController.this.invoke(new Runnable() {
                    public void run() {
                        statusLabel.display();
                        window.toolbar().validateVisibleItems();
                    }
                });
            }
        });
        this.getFocus();
        return session;
    }

    /**
     * @return The history bookmark file in the application support directory
     */
    private Local getRepresentedFile() {
        if(this.hasSession()) {
            return new Local(HistoryMenuDelegate.HISTORY_FOLDER, this.session.getHost().getNickname() + ".duck");
        }
        return null;
    }

    /**
     *
     */
    private Session session;

    /**
     * @param h
     * @return The session to be used for any further operations
     */
    public Session mount(Host h) {
        final HostCollection c = HostCollection.instance();
        if(c.contains(h)) {
            // Use the bookmarked reference if any. Otherwise if a clone thereof is used
            // it confuses the user, that settings to the bookmark will not affect the
            // currently mounted browser
            Host bookmark = (Host) c.get(c.indexOf(h));
            if(h.getURL().equals(bookmark.getURL())) {
                h = bookmark;
            }
        }
        final Host host = h;
        log.debug("mount:" + host);
        if(this.isMounted()) {
            if(this.session.getHost().getURL().equals(host.getURL())) {
                // The host is already mounted
                if(host.hasReasonableDefaultPath()) {
                    // Change to its default path
                    this.background(new BackgroundAction() {
                        public void run() {
                            Path home = PathFactory.createPath(session, host.getDefaultPath());
                            home.attributes.setType(Path.DIRECTORY_TYPE);
                            home.invalidate();
                            setWorkdir(home);
                        }

                        public void cleanup() {
                            ;
                        }
                    });
                    return session;
                }
            }
        }
        if(this.unmount(new CDSheetCallback() {
            public void callback(int returncode) {
                if(returncode == DEFAULT_OPTION) {
                    if(isBusy()) {
                        interrupt();
                    }
                    // The user has approved closing the current session
                    background(new BackgroundAction() {
                        public void run() {
                            unmount(true);
                        }

                        public void cleanup() {
                            mount(host);
                        }
                    });
                }
            }
        })) {
            // The browser has no session, we are allowed to proceed
            // Initialize the browser with the new session attaching all listeners
            final Session session = this.init(host);
            this.background(new BackgroundAction() {
                public void run() {
                    // Mount this session and set the working directory in the background
                    setWorkdir(session.mount());
                }

                public void cleanup() {
                    ;
                }
            });
            return session;
        }
        // The current session is still valid
        return null;
    }

    /**
     * Will close the session but still display the current working directory without any confirmation
     * from the user
     *
     * @param forever The session won't be remounted in any case; will clear the cache
     */
    public void unmount(final boolean forever) {
        // This is not synchronized to the <code>mountingLock</code> intentionally; this allows to unmount
        // sessions not yet connected
        if(this.hasSession()) {
            //Close the connection gracefully
            this.session.close();
            if(forever) {
                this.session.cache().clear();
                this.session.getHost().getCredentials().setPassword(null);
                this.session = null;
            }
        }
    }

    /**
     * @return True if the unmount process has finished, false if the user has to agree first
     *         to close the connection
     */
    public boolean unmount(final CDSheetCallback callback) {
        log.debug("unmount");
        if(this.isConnected() || this.isBusy()) {
            if(Preferences.instance().getBoolean("browser.confirmDisconnect")) {
                // Defer the unmount to the callback function
                this.alert(NSAlertPanel.criticalAlertPanel(NSBundle.localizedString("Disconnect from", "Alert sheet title") + " " + this.session.getHost().getHostname(), //title
                        NSBundle.localizedString("The connection will be closed.", "Alert sheet text"), // message
                        NSBundle.localizedString("Disconnect", "Alert sheet default button"), // defaultbutton
                        NSBundle.localizedString("Cancel", "Alert sheet alternate button"), // alternate button
                        null //other button
                ), callback);
                return false;
            }
            if(this.isBusy()) {
                this.interrupt();
            }
            this.unmount(true);
        }
        // Unmount succeeded
        return true;
    }

    /**
     * Interrupt any operation in progress;
     * just closes the socket without any quit message sent to the server
     */
    protected void interrupt() {
        if(this.hasSession()) {
            if(this.activityRunning) {
                backgroundAction.cancel();
            }
            this.background(new BackgroundActionImpl(this) {
                public void run() {
                    session.interrupt();
                }

                public void cleanup() {
                    ;
                }
            });
        }
        this.interrupted = true;
    }

    /**
     * @param sender
     */
    public void printDocument(final Object sender) {
        NSPrintOperation op = NSPrintOperation.printOperationWithView(this.getSelectedBrowserView());
        op.runModalOperation(this.window, this,
                new NSSelector("printOperationDidRun",
                        new Class[]{NSPrintOperation.class, boolean.class, Object.class}), null);
    }

    public void printOperationDidRun(NSPrintOperation printOperation, boolean success, Object contextInfo) {
        if(success) {
            log.info("Successfully printed" + contextInfo);
        }
    }

    /**
     * @param app
     * @return NSApplication.TerminateLater if the application should not yet be terminated
     */
    public static int applicationShouldTerminate(NSApplication app) {
        // Determine if there are any open connections
        NSArray windows = NSApplication.sharedApplication().windows();
        int count = windows.count();
        // Determine if there are any open connections
        while(0 != count--) {
            final NSWindow window = (NSWindow) windows.objectAtIndex(count);
            CDBrowserController controller = CDBrowserController.controllerForWindow(window);
            if(null != controller) {
                if(!controller.unmount(new CDSheetCallback() {
                    public void callback(int returncode) {
                        if(returncode == DEFAULT_OPTION) { //Disconnect
                            window.close();
                            CDBrowserController.applicationShouldTerminate(null);
                        }
                        if(returncode == ALTERNATE_OPTION) { //Cancel
                            NSApplication.sharedApplication().replyToApplicationShouldTerminate(false);
                        }
                    }
                })) {
                    return NSApplication.TerminateLater;
                }
            }
        }
        return CDTransferController.applicationShouldTerminate(app);
    }

    public boolean windowShouldClose(final NSWindow sender) {
        return this.unmount(new CDSheetCallback() {
            public void callback(int returncode) {
                if(returncode == DEFAULT_OPTION) {
                    if(isBusy()) {
                        interrupt();
                    }
                    background(new BackgroundAction() {
                        public void run() {
                            unmount(true);
                        }

                        public void cleanup() {
                            sender.close();
                        }
                    });
                }
            }
        });
    }

    private void validateNavigationButtons() {
        this.navigationButton.setEnabled(this.isMounted() && this.getBackHistory().length > 1,
                NAVIGATION_LEFT_SEGMENT_BUTTON);
        this.navigationButton.setEnabled(this.isMounted() && this.getForwardHistory().length > 0,
                NAVIGATION_RIGHT_SEGMENT_BUTTON);
        this.upButton.setEnabled(this.isMounted() && !this.workdir().isRoot(),
                NAVIGATION_UP_SEGMENT_BUTTON);

        this.pathPopupButton.setEnabled(this.isMounted());
        this.searchField.setEnabled(this.isMounted());
        this.encodingPopup.setEnabled(!this.isBusy());
    }

    /**
     * @param item
     * @return true if the menu should be enabled
     */
    public boolean validateMenuItem(NSMenuItem item) {
        String identifier = item.action().name();
        if(identifier.equals("pasteFromFinder:")) {
            boolean valid = false;
            if(this.isMounted()) {
                if(NSPasteboard.generalPasteboard().availableTypeFromArray(new NSArray(NSPasteboard.FilenamesPboardType)) != null) {
                    Object o = NSPasteboard.generalPasteboard().propertyListForType(NSPasteboard.FilenamesPboardType);
                    if(o != null) {
                        NSArray elements = (NSArray) o;
                        if(elements.count() == 1) {
                            item.setTitle(NSBundle.localizedString("Paste", "Menu item") + " \""
                                    + elements.objectAtIndex(0) + "\"");
                        }
                        else {
                            item.setTitle(NSBundle.localizedString("Paste from Finder", "Menu item") + " (" +
                                    elements.count() + " " +
                                    NSBundle.localizedString("files", "") + ")");
                        }
                        valid = true;
                    }
                }
            }
            if(!valid) {
                item.setTitle(NSBundle.localizedString("Paste from Finder", "Menu item"));
            }
        }
        if(identifier.equals("paste:")) {
            boolean valid = false;
            if(this.isMounted()) {
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
            }
            if(!valid) {
                item.setTitle(NSBundle.localizedString("Paste", "Menu item"));
            }
        }
        if(identifier.equals("cut:")) {
            if(this.isMounted() && this.getSelectionCount() > 0) {
                if(this.getSelectionCount() == 1) {
                    Path p = this.getSelectedPath();
                    item.setTitle(NSBundle.localizedString("Cut", "Menu item") + " \"" + p.getName() + "\"");
                }
                else {
                    item.setTitle(NSBundle.localizedString("Cut", "Menu item")
                            + " " + this.getSelectionCount() + " " +
                            NSBundle.localizedString("files", ""));
                }
            }
            else
                item.setTitle(NSBundle.localizedString("Cut", "Menu item"));
        }
        if(identifier.equals("showHiddenFilesClicked:")) {
            item.setState(this.getFileFilter() instanceof NullPathFilter ? NSCell.OnState : NSCell.OffState);
        }
        if(identifier.equals("encodingMenuClicked:")) {
            if(this.isMounted()) {
                item.setState(this.session.getEncoding().equalsIgnoreCase(
                        item.title()) ? NSCell.OnState : NSCell.OffState);
            }
            else {
                item.setState(Preferences.instance().getProperty("browser.charset.encoding").equalsIgnoreCase(
                        item.title()) ? NSCell.OnState : NSCell.OffState);
            }
        }
        if(identifier.equals("browserSwitchMenuClicked:")) {
            if(item.tag() == Preferences.instance().getInteger("browser.view")) {
                item.setState(NSCell.OnState);
            }
            else {
                item.setState(NSCell.OffState);
            }
        }
        return this.validateItem(identifier);
    }

    /**
     * @param identifier the method selector
     * @return true if the item by that identifier should be enabled
     */
    private boolean validateItem(String identifier) {
        if(identifier.equals("cut:")) {
            return this.isMounted() && this.getSelectionCount() > 0;
        }
        if(identifier.equals("pasteFromFinder:")) {
            if(this.isMounted()) {
                NSPasteboard pboard = NSPasteboard.generalPasteboard();
                if(pboard.availableTypeFromArray(new NSArray(NSPasteboard.FilenamesPboardType)) != null) {
                    Object o = pboard.propertyListForType(NSPasteboard.FilenamesPboardType);
                    if(o != null) {
                        return true;
                    }
                }
            }
            return false;
        }
        if(identifier.equals("paste:")) {
            if(this.isMounted()) {
                NSPasteboard pboard = NSPasteboard.pasteboardWithName(CDPasteboards.TransferPasteboard);
                if(pboard.availableTypeFromArray(new NSArray(CDPasteboards.TransferPasteboardType)) != null) {
                    Object o = pboard.propertyListForType(CDPasteboards.TransferPasteboardType);
                    if(o != null) {
                        return true;
                    }
                }
            }
            return false;
        }
        if(identifier.equals("encodingMenuClicked:")) {
            return !isBusy();
        }
        if(identifier.equals("connectBookmarkButtonClicked:")) {
            return bookmarkTable.numberOfSelectedRows() == 1;
        }
        if(identifier.equals("deleteBookmarkButtonClicked:")) {
            return bookmarkTable.selectedRow() != -1;
        }
        if(identifier.equals("editBookmarkButtonClicked:")) {
            return bookmarkTable.numberOfSelectedRows() == 1;
        }
        if(identifier.equals("editButtonClicked:")) {
            if(this.isMounted() && this.getSelectionCount() > 0) {
                String editorPath = NSWorkspace.sharedWorkspace().absolutePathForAppBundleWithIdentifier(
                        Preferences.instance().getProperty("editor.bundleIdentifier"));
                if(editorPath != null) {
                    for(Iterator i = this.getSelectedPaths().iterator(); i.hasNext();) {
                        final Path selected = (Path) i.next();
                        if(!this.isEditable(selected)) {
                            return false;
                        }
                    }
                    return true;
                }
            }
            return false;
        }
        if(identifier.equals("editMenuClicked:")) {
            if(this.isMounted()) {
                for(Iterator i = this.getSelectedPaths().iterator(); i.hasNext();) {
                    final Path selected = (Path) i.next();
                    if(!this.isEditable(selected)) {
                        return false;
                    }
                }
                return true;
            }
            return false;
        }
        if(identifier.equals("sendCustomCommandClicked:")) {
            return this.session instanceof ch.cyberduck.core.ftp.FTPSession && this.isConnected();
        }
        if(identifier.equals("gotoButtonClicked:")) {
            return this.isMounted();
        }
        if(identifier.equals("infoButtonClicked:")) {
            return this.isMounted() && this.getSelectionCount() > 0;
        }
        if(identifier.equals("createFolderButtonClicked:")) {
            return this.isMounted();
        }
        if(identifier.equals("createFileButtonClicked:")) {
            return this.isMounted();
        }
        if(identifier.equals("duplicateFileButtonClicked:")) {
            return this.isMounted() && this.getSelectionCount() == 1 && this.getSelectedPath().attributes.isFile();
        }
        if(identifier.equals("renameFileButtonClicked:")) {
            return this.isMounted() && this.getSelectionCount() == 1;
        }
        if(identifier.equals("deleteFileButtonClicked:")) {
            return this.isMounted() && this.getSelectionCount() > 0;
        }
        if(identifier.equals("reloadButtonClicked:")) {
            return this.isMounted();
        }
        if(identifier.equals("downloadButtonClicked:")) {
            return this.isMounted() && this.getSelectionCount() > 0;
        }
        if(identifier.equals("uploadButtonClicked:")) {
            return this.isMounted();
        }
        if(identifier.equals("syncButtonClicked:")) {
            return this.isMounted();
        }
        if(identifier.equals("downloadAsButtonClicked:")) {
            return this.isMounted() && this.getSelectionCount() == 1;
        }
        if(identifier.equals("downloadToButtonClicked:")) {
            return this.isMounted() && this.getSelectionCount() > 0;
        }
        if(identifier.equals("insideButtonClicked:")) {
            return this.isMounted() && this.getSelectionCount() > 0;
        }
        if(identifier.equals("upButtonClicked:")) {
            return this.isMounted() && !this.workdir().isRoot();
        }
        if(identifier.equals("backButtonClicked:")) {
            return this.isMounted() && this.getBackHistory().length > 1;
        }
        if(identifier.equals("forwardButtonClicked:")) {
            return this.isMounted() && this.getForwardHistory().length > 0;
        }
        if(identifier.equals("copyURLButtonClicked:")) {
            return this.isMounted();
        }
        if(identifier.equals("printDocument:")) {
            return this.isMounted();
        }
        if(identifier.equals("disconnectButtonClicked:")) {
            if(!this.isConnected()) {
                return this.isBusy();
            }
            return this.isConnected();
        }
        if(identifier.equals("interruptButtonClicked:")) {
            return this.isBusy();
        }
        if(identifier.equals("gotofolderButtonClicked:")) {
            return this.isMounted();
        }
        this.validateNavigationButtons();
        return true; // by default everything is enabled
    }

    // ----------------------------------------------------------
    // Toolbar Delegate
    // ----------------------------------------------------------

    public boolean validateToolbarItem(NSToolbarItem item) {
        String identifier = item.action().name();
        if(identifier.equals("editButtonClicked:")) {
            String editorPath = NSWorkspace.sharedWorkspace().absolutePathForAppBundleWithIdentifier(
                    Preferences.instance().getProperty("editor.bundleIdentifier"));
            if(editorPath != null) {
                item.setImage(NSWorkspace.sharedWorkspace().iconForFile(editorPath));

            }
            else {
                item.setImage(NSImage.imageNamed("pencil.tiff"));
            }
        }
        if(identifier.equals("disconnectButtonClicked:")) {
            if(this.isBusy()) {
                item.setLabel(NSBundle.localizedString(TOOLBAR_INTERRUPT, "Toolbar item"));
                item.setPaletteLabel(NSBundle.localizedString(TOOLBAR_INTERRUPT, "Toolbar item"));
                item.setToolTip(NSBundle.localizedString("Cancel current operation in progress", "Toolbar item tooltip"));
                item.setImage(NSImage.imageNamed("stop.tiff"));
            }
            else {
                item.setLabel(NSBundle.localizedString(TOOLBAR_DISCONNECT, "Toolbar item"));
                item.setPaletteLabel(NSBundle.localizedString(TOOLBAR_DISCONNECT, "Toolbar item"));
                item.setToolTip(NSBundle.localizedString("Disconnect from server", "Toolbar item tooltip"));
                item.setImage(NSImage.imageNamed("eject.tiff"));
            }
        }
        return this.validateItem(identifier);
    }

    private static final String TOOLBAR_NEW_CONNECTION = "New Connection";
    private static final String TOOLBAR_BROWSER_VIEW = "Browser View";
    private static final String TOOLBAR_BOOKMARKS = "Bookmarks";
    private static final String TOOLBAR_TRANSFERS = "Transfers";
    private static final String TOOLBAR_QUICK_CONNECT = "Quick Connect";
    private static final String TOOLBAR_NAVIGATION = "Location";
    private static final String TOOLBAR_TOOLS = "Tools";
    private static final String TOOLBAR_HISTORY = "History";
    private static final String TOOLBAR_REFRESH = "Refresh";
    private static final String TOOLBAR_ENCODING = "Encoding";
    private static final String TOOLBAR_SYNCHRONIZE = "Synchronize";
    private static final String TOOLBAR_DOWNLOAD = "Download";
    private static final String TOOLBAR_UPLOAD = "Upload";
    private static final String TOOLBAR_EDIT = "Edit";
    private static final String TOOLBAR_DELETE = "Delete";
    private static final String TOOLBAR_NEW_FOLDER = "New Folder";
    private static final String TOOLBAR_GET_INFO = "Get Info";
    private static final String TOOLBAR_DISCONNECT = "Disconnect";
    private static final String TOOLBAR_INTERRUPT = "Stop";
    private static final String TOOLBAR_GO_TO_FOLDER = "Go to Folder";

    /**
     *
     */
    private final EditMenuDelegate editMenuDelegate
            = new EditMenuDelegate();

    public NSToolbarItem toolbarItemForItemIdentifier(NSToolbar toolbar, String itemIdentifier, boolean flag) {
        NSToolbarItem item = new NSToolbarItem(itemIdentifier);
        if(itemIdentifier.equals(TOOLBAR_BROWSER_VIEW)) {
            item.setLabel(NSBundle.localizedString("View", "Toolbar item"));
            item.setPaletteLabel(NSBundle.localizedString("View", "Toolbar item"));
            item.setToolTip(NSBundle.localizedString("Switch Browser View", "Toolbar item tooltip"));
            item.setView(this.browserSwitchView);
            // Add a menu representation for text mode of toolbar
            NSMenuItem viewMenu = new NSMenuItem();
            viewMenu.setTitle(NSBundle.localizedString("View", "Toolbar item"));
            NSMenu viewSubmenu = new NSMenu();
            viewSubmenu.addItem(new NSMenuItem(NSBundle.localizedString("List", "Toolbar item"),
                    new NSSelector("browserSwitchMenuClicked", new Class[]{Object.class}),
                    ""));
            viewSubmenu.itemWithTitle(NSBundle.localizedString("List", "Toolbar item")).setTag(0);
            viewSubmenu.addItem(new NSMenuItem(NSBundle.localizedString("Outline", "Toolbar item"),
                    new NSSelector("browserSwitchMenuClicked", new Class[]{Object.class}),
                    ""));
            viewSubmenu.itemWithTitle(NSBundle.localizedString("Outline", "Toolbar item")).setTag(1);
            viewMenu.setSubmenu(viewSubmenu);
            item.setMenuFormRepresentation(viewMenu);
            item.setMinSize(this.browserSwitchView.frame().size());
            item.setMaxSize(this.browserSwitchView.frame().size());
            return item;
        }
        if(itemIdentifier.equals(TOOLBAR_NEW_CONNECTION)) {
            item.setLabel(NSBundle.localizedString(TOOLBAR_NEW_CONNECTION, "Toolbar item"));
            item.setPaletteLabel(NSBundle.localizedString(TOOLBAR_NEW_CONNECTION, "Toolbar item"));
            item.setToolTip(NSBundle.localizedString("Connect to server", "Toolbar item tooltip"));
            item.setImage(NSImage.imageNamed("connect.tiff"));
            item.setTarget(this);
            item.setAction(new NSSelector("connectButtonClicked", new Class[]{Object.class}));
            return item;
        }
        if(itemIdentifier.equals(TOOLBAR_BOOKMARKS)) {
            item.setLabel(NSBundle.localizedString(TOOLBAR_BOOKMARKS, "Toolbar item"));
            item.setPaletteLabel(NSBundle.localizedString(TOOLBAR_BOOKMARKS, "Toolbar item"));
            item.setToolTip(NSBundle.localizedString("Toggle Bookmarks", "Toolbar item tooltip"));
            item.setImage(NSImage.imageNamed("drawer.tiff"));
            item.setTarget(this);
            item.setAction(new NSSelector("toggleBookmarkDrawer", new Class[]{Object.class}));
            return item;
        }
        if(itemIdentifier.equals(TOOLBAR_TRANSFERS)) {
            item.setLabel(NSBundle.localizedString(TOOLBAR_TRANSFERS, "Toolbar item"));
            item.setPaletteLabel(NSBundle.localizedString(TOOLBAR_TRANSFERS, "Toolbar item"));
            item.setToolTip(NSBundle.localizedString("Show Transfers window", "Toolbar item tooltip"));
            item.setImage(NSImage.imageNamed("queue.tiff"));
            item.setTarget(this);
            item.setAction(new NSSelector("showTransferQueueClicked", new Class[]{Object.class}));
            return item;
        }
        if(itemIdentifier.equals(TOOLBAR_TOOLS)) {
            item.setLabel(NSBundle.localizedString("Action", "Toolbar item"));
            item.setPaletteLabel(NSBundle.localizedString("Action", "Toolbar item"));
            item.setView(this.actionPopupButton);
            // Add a menu representation for text mode of toolbar
            NSMenuItem toolMenu = new NSMenuItem();
            toolMenu.setTitle(NSBundle.localizedString("Action", "Toolbar item"));
            NSMenu toolSubmenu = new NSMenu();
            for(int i = 1; i < this.actionPopupButton.menu().numberOfItems(); i++) {
                NSMenuItem template = this.actionPopupButton.menu().itemAtIndex(i);
                toolSubmenu.addItem(new NSMenuItem(template.title(),
                        template.action(),
                        template.keyEquivalent()));
            }
            toolMenu.setSubmenu(toolSubmenu);
            item.setMenuFormRepresentation(toolMenu);
            item.setMinSize(this.actionPopupButton.frame().size());
            item.setMaxSize(this.actionPopupButton.frame().size());
            return item;
        }
//        if(itemIdentifier.equals(TOOLBAR_HISTORY)) {
//            item.setLabel(NSBundle.localizedString("History", "Toolbar item"));
//            item.setPaletteLabel(NSBundle.localizedString("History", "Toolbar item"));
//            item.setView(this.historyPopupButton);
//            // Add a menu representation for text mode of toolbar
//            NSMenuItem menu = new NSMenuItem();
//            menu.setTitle(NSBundle.localizedString("History", "Toolbar item"));
//            NSMenu historyMenu = new NSMenu();
//            historyMenu.setDelegate(historyPopupButtonMenuDelegate);
//            menu.setSubmenu(historyMenu);
//            item.setMenuFormRepresentation(menu);
//            item.setMinSize(this.historyPopupButton.frame().size());
//            item.setMaxSize(this.historyPopupButton.frame().size());
//            return item;
//        }
        if(itemIdentifier.equals(TOOLBAR_QUICK_CONNECT)) {
            item.setLabel(NSBundle.localizedString(TOOLBAR_QUICK_CONNECT, "Toolbar item"));
            item.setPaletteLabel(NSBundle.localizedString(TOOLBAR_QUICK_CONNECT, "Toolbar item"));
            item.setToolTip(NSBundle.localizedString("Connect to server", "Toolbar item tooltip"));
            item.setView(this.quickConnectPopup);
            item.setMinSize(this.quickConnectPopup.frame().size());
            item.setMaxSize(this.quickConnectPopup.frame().size());
            return item;
        }
//        if(itemIdentifier.equals(TOOLBAR_NAVIGATION)) {
//            item.setLabel(NSBundle.localizedString(TOOLBAR_NAVIGATION, "Toolbar item"));
//            item.setPaletteLabel(NSBundle.localizedString(TOOLBAR_NAVIGATION, "Toolbar item"));
//            item.setView(this.navigationPopup);
//            item.setMinSize(this.navigationPopup.frame().size());
//            item.setMaxSize(this.navigationPopup.frame().size());
//            return item;
//        }
        if(itemIdentifier.equals(TOOLBAR_ENCODING)) {
            item.setLabel(NSBundle.localizedString(TOOLBAR_ENCODING, "Toolbar item"));
            item.setPaletteLabel(NSBundle.localizedString(TOOLBAR_ENCODING, "Toolbar item"));
            item.setToolTip(NSBundle.localizedString("Character Encoding", "Toolbar item tooltip"));
            item.setView(this.encodingPopup);
            // Add a menu representation for text mode of toolbar
            NSMenuItem encodingMenu = new NSMenuItem(NSBundle.localizedString(TOOLBAR_ENCODING, "Toolbar item"),
                    new NSSelector("encodingMenuClicked", new Class[]{Object.class}),
                    "");
            String[] charsets = ((CDMainController) NSApplication.sharedApplication().delegate()).availableCharsets();
            NSMenu charsetMenu = new NSMenu();
            for(int i = 0; i < charsets.length; i++) {
                charsetMenu.addItem(new NSMenuItem(charsets[i],
                        new NSSelector("encodingMenuClicked", new Class[]{Object.class}),
                        ""));
            }
            encodingMenu.setSubmenu(charsetMenu);
            item.setMenuFormRepresentation(encodingMenu);
            item.setMinSize(this.encodingPopup.frame().size());
            item.setMaxSize(this.encodingPopup.frame().size());
            return item;
        }
        if(itemIdentifier.equals(TOOLBAR_REFRESH)) {
            item.setLabel(NSBundle.localizedString(TOOLBAR_REFRESH, "Toolbar item"));
            item.setPaletteLabel(NSBundle.localizedString(TOOLBAR_REFRESH, "Toolbar item"));
            item.setToolTip(NSBundle.localizedString("Refresh directory listing", "Toolbar item tooltip"));
            item.setImage(NSImage.imageNamed("reload.tiff"));
            item.setTarget(this);
            item.setAction(new NSSelector("reloadButtonClicked", new Class[]{Object.class}));
            return item;
        }
        if(itemIdentifier.equals(TOOLBAR_DOWNLOAD)) {
            item.setLabel(NSBundle.localizedString(TOOLBAR_DOWNLOAD, "Toolbar item"));
            item.setPaletteLabel(NSBundle.localizedString(TOOLBAR_DOWNLOAD, "Toolbar item"));
            item.setToolTip(NSBundle.localizedString("Download file", "Toolbar item tooltip"));
            item.setImage(NSImage.imageNamed("downloadFile.tiff"));
            item.setTarget(this);
            item.setAction(new NSSelector("downloadButtonClicked", new Class[]{Object.class}));
            return item;
        }
        if(itemIdentifier.equals(TOOLBAR_UPLOAD)) {
            item.setLabel(NSBundle.localizedString(TOOLBAR_UPLOAD, "Toolbar item"));
            item.setPaletteLabel(NSBundle.localizedString(TOOLBAR_UPLOAD, "Toolbar item"));
            item.setToolTip(NSBundle.localizedString("Upload local file to the remote host", "Toolbar item tooltip"));
            item.setImage(NSImage.imageNamed("uploadFile.tiff"));
            item.setTarget(this);
            item.setAction(new NSSelector("uploadButtonClicked", new Class[]{Object.class}));
            return item;
        }
        if(itemIdentifier.equals(TOOLBAR_SYNCHRONIZE)) {
            item.setLabel(NSBundle.localizedString(TOOLBAR_SYNCHRONIZE, "Toolbar item"));
            item.setPaletteLabel(NSBundle.localizedString(TOOLBAR_SYNCHRONIZE, "Toolbar item"));
            item.setToolTip(NSBundle.localizedString("Synchronize files", "Toolbar item tooltip"));
            item.setImage(NSImage.imageNamed("sync32.tiff"));
            item.setTarget(this);
            item.setAction(new NSSelector("syncButtonClicked", new Class[]{Object.class}));
            return item;
        }
        if(itemIdentifier.equals(TOOLBAR_GET_INFO)) {
            item.setLabel(NSBundle.localizedString(TOOLBAR_GET_INFO, "Toolbar item"));
            item.setPaletteLabel(NSBundle.localizedString(TOOLBAR_GET_INFO, "Toolbar item"));
            item.setToolTip(NSBundle.localizedString("Show file attributes", "Toolbar item tooltip"));
            item.setImage(NSImage.imageNamed("info.tiff"));
            item.setTarget(this);
            item.setAction(new NSSelector("infoButtonClicked", new Class[]{Object.class}));
            return item;
        }
        if(itemIdentifier.equals(TOOLBAR_EDIT)) {
            item.setLabel(NSBundle.localizedString(TOOLBAR_EDIT, "Toolbar item"));
            item.setPaletteLabel(NSBundle.localizedString(TOOLBAR_EDIT, "Toolbar item"));
            item.setToolTip(NSBundle.localizedString("Edit file in external editor", "Toolbar item tooltip"));
            item.setImage(NSImage.imageNamed("pencil.tiff"));
            String editorPath = NSWorkspace.sharedWorkspace().absolutePathForAppBundleWithIdentifier(
                    Preferences.instance().getProperty("editor.bundleIdentifier"));
            if(editorPath != null) {
                item.setImage(NSWorkspace.sharedWorkspace().iconForFile(editorPath));
            }
            item.setTarget(this);
            item.setAction(new NSSelector("editButtonClicked", new Class[]{Object.class}));
            // Add a menu representation for text mode of toolbar
            NSMenuItem toolbarMenu = new NSMenuItem(NSBundle.localizedString(TOOLBAR_EDIT, "Toolbar item"),
                    new NSSelector("editMenuClicked", new Class[]{Object.class}),
                    "");
            NSMenu editMenu = new NSMenu();
            editMenu.setAutoenablesItems(true);
            editMenu.setDelegate(editMenuDelegate);
            toolbarMenu.setSubmenu(editMenu);
            item.setMenuFormRepresentation(toolbarMenu);
            return item;
        }
        if(itemIdentifier.equals(TOOLBAR_DELETE)) {
            item.setLabel(NSBundle.localizedString(TOOLBAR_DELETE, "Toolbar item"));
            item.setPaletteLabel(NSBundle.localizedString(TOOLBAR_DELETE, "Toolbar item"));
            item.setToolTip(NSBundle.localizedString("Delete file", "Toolbar item tooltip"));
            item.setImage(NSImage.imageNamed("deleteFile.tiff"));
            item.setTarget(this);
            item.setAction(new NSSelector("deleteFileButtonClicked", new Class[]{Object.class}));
            return item;
        }
        if(itemIdentifier.equals(TOOLBAR_NEW_FOLDER)) {
            item.setLabel(NSBundle.localizedString(TOOLBAR_NEW_FOLDER, "Toolbar item"));
            item.setPaletteLabel(NSBundle.localizedString(TOOLBAR_NEW_FOLDER, "Toolbar item"));
            item.setToolTip(NSBundle.localizedString("Create New Folder", "Toolbar item tooltip"));
            item.setImage(NSImage.imageNamed("folder_new.tiff"));
            item.setTarget(this);
            item.setAction(new NSSelector("createFolderButtonClicked", new Class[]{Object.class}));
            return item;
        }
        if(itemIdentifier.equals(TOOLBAR_DISCONNECT)) {
            item.setLabel(NSBundle.localizedString(TOOLBAR_DISCONNECT, "Toolbar item"));
            item.setPaletteLabel(NSBundle.localizedString(TOOLBAR_DISCONNECT, "Toolbar item"));
            item.setToolTip(NSBundle.localizedString("Disconnect from server", "Toolbar item tooltip"));
            item.setImage(NSImage.imageNamed("eject.tiff"));
            item.setTarget(this);
            item.setAction(new NSSelector("disconnectButtonClicked", new Class[]{Object.class}));
            return item;
        }
        if(itemIdentifier.equals(TOOLBAR_GO_TO_FOLDER)) {
            item.setLabel(NSBundle.localizedString(TOOLBAR_GO_TO_FOLDER, "Toolbar item"));
            item.setPaletteLabel(NSBundle.localizedString(TOOLBAR_GO_TO_FOLDER, "Toolbar item"));
            item.setToolTip(NSBundle.localizedString("Go to Folder", "Toolbar item tooltip"));
            item.setImage(NSImage.imageNamed("goto.tiff"));
            item.setTarget(this);
            item.setAction(new NSSelector("gotoButtonClicked", new Class[]{Object.class}));
            return item;
        }
        // itemIdent refered to a toolbar item that is not provide or supported by us or cocoa.
        // Returning null will inform the toolbar this kind of item is not supported.
        return null;
    }

    /**
     * @param toolbar
     * @return The default configuration of toolbar items
     */
    public NSArray toolbarDefaultItemIdentifiers(NSToolbar toolbar) {
        return new NSArray(new Object[]{
                TOOLBAR_NEW_CONNECTION,
                NSToolbarItem.SeparatorItemIdentifier,
                TOOLBAR_BOOKMARKS,
                TOOLBAR_QUICK_CONNECT,
                TOOLBAR_TOOLS,
                NSToolbarItem.SeparatorItemIdentifier,
                TOOLBAR_REFRESH,
                TOOLBAR_EDIT,
                NSToolbarItem.FlexibleSpaceItemIdentifier,
                TOOLBAR_DISCONNECT
        });
    }

    /**
     * @param toolbar
     * @return All available toolbar items
     */
    public NSArray toolbarAllowedItemIdentifiers(NSToolbar toolbar) {
        return new NSArray(new Object[]{
                TOOLBAR_NEW_CONNECTION,
                TOOLBAR_BROWSER_VIEW,
                TOOLBAR_BOOKMARKS,
                TOOLBAR_TRANSFERS,
                TOOLBAR_QUICK_CONNECT,
//                TOOLBAR_NAVIGATION,
//                TOOLBAR_HISTORY,
                TOOLBAR_TOOLS,
                TOOLBAR_REFRESH,
                TOOLBAR_ENCODING,
                TOOLBAR_SYNCHRONIZE,
                TOOLBAR_DOWNLOAD,
                TOOLBAR_UPLOAD,
                TOOLBAR_EDIT,
                TOOLBAR_DELETE,
                TOOLBAR_NEW_FOLDER,
                TOOLBAR_GET_INFO,
                TOOLBAR_DISCONNECT,
//                TOOLBAR_GO_TO_FOLDER,
                NSToolbarItem.CustomizeToolbarItemIdentifier,
                NSToolbarItem.SpaceItemIdentifier,
                NSToolbarItem.SeparatorItemIdentifier,
                NSToolbarItem.FlexibleSpaceItemIdentifier
        });
    }

    /**
     * Overrriden to remove any listeners from the session
     */
    protected void invalidate() {
        if(this.hasSession()) {
            this.session.removeConnectionListener(this.listener);
        }
        this.toolbar.setDelegate(null);

        this.bookmarkDrawer.setContentView(null);

        this.bookmarkTable.setDataSource(null);
        HostCollection.instance().removeListener(this.bookmarkCollectionListener);
        this.bookmarkModel = null;
        this.bookmarkTable.setDelegate(null);
        this.bookmarkTableDelegate = null;
        this.bookmarkTable = null;

        this.browserListView.setDataSource(null);
        this.browserListModel = null;
        this.browserListView.setDelegate(null);
        this.browserListViewDelegate = null;
        this.browserListView = null;

        this.browserOutlineView.setDataSource(null);
        this.browserOutlineModel = null;
        this.browserOutlineView.setDelegate(null);
        this.browserOutlineViewDelegate = null;
        this.browserOutlineView = null;

        this.browserSwitchView.setTarget(null);
        this.browserSwitchView = null;
        this.browserTabView = null;

        this.addBookmarkButton.setTarget(null);
        this.deleteBookmarkButton.setTarget(null);
        this.editBookmarkButton.setTarget(null);

        this.actionPopupButton.setTarget(null);
//        this.historyPopupButton.setTarget(null);

        this.navigationButton.setTarget(null);
        this.upButton.setTarget(null);
        this.pathPopupButton.setTarget(null);
        this.encodingPopup.setTarget(null);

        this.quickConnectPopup.setDataSource(null);
        this.quickConnectPopupModel = null;
        this.quickConnectPopup.setTarget(null);
        this.quickConnectPopup = null;

//        this.navigationPopup.setDataSource(null);
//        this.navigationPopupModel = null;
//        this.navigationPopup.setTarget(null);
//        this.navigationPopup = null;

        super.invalidate();
    }
}