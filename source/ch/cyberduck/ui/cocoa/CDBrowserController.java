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
import ch.cyberduck.ui.cocoa.odb.Editor;
import ch.cyberduck.ui.cocoa.threading.BackgroundActionImpl;
import ch.cyberduck.ui.cocoa.threading.BackgroundAction;

import com.apple.cocoa.application.*;
import com.apple.cocoa.foundation.*;

import org.apache.log4j.Logger;

import java.io.File;
import java.io.IOException;
import java.util.*;

/**
 * @version $Id$
 */
public class CDBrowserController extends CDWindowController
        implements NSToolbarItem.ItemValidation {
    private static Logger log = Logger.getLogger(CDBrowserController.class);

    protected static final File HISTORY_FOLDER
            = new File(Preferences.instance().getProperty("application.support.path"), "History");

    static {
        HISTORY_FOLDER.mkdirs();
    }

    /**
     * Applescriptability
     *
     * @return The NSIndexSpecifier for all browsers or null if there is none
     */
    public NSScriptObjectSpecifier objectSpecifier() {
        log.debug("objectSpecifier");
        NSArray orderedDocs = (NSArray) NSKeyValue.valueForKey(NSApplication.sharedApplication(), "orderedBrowsers");
        int index = orderedDocs.indexOfObject(this);
        if((index >= 0) && (index < orderedDocs.count())) {
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
            host = (Host)bookmarks.get(index);
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
                    host.setFTPConnectMode(com.enterprisedt.net.ftp.FTPConnectMode.ACTIVE);
                if(modeObj.equals(FTPConnectMode.PASV.toString()))
                    host.setFTPConnectMode(com.enterprisedt.net.ftp.FTPConnectMode.PASV);
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
            for(Iterator i = path.list().iterator(); i.hasNext();) {
                result.addObject(((Path) i.next()).getName());
            }
        }
        return result;
    }

    public Object handleGotoScriptCommand(NSScriptCommand command) {
        log.debug("handleGotoScriptCommand:" + command);
        if(this.isMounted()) {
            NSDictionary args = command.evaluatedArguments();
            CDGotoController c = new CDGotoController(this);
            c.gotoFolder(this.workdir(), (String) args.objectForKey("Path"));
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
            Editor editor = new Editor(Preferences.instance().getProperty("editor.bundleIdentifier"), this);
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
            final Path path = PathFactory.createPath((Session)this.session,
                    (String) args.objectForKey("Path"));
            path.attributes.setType(Path.DIRECTORY_TYPE);
            Object localObj = args.objectForKey("Local");
            if(localObj != null) {
                path.setLocal(new Local((String) localObj));
            }
            final Queue q = new SyncQueue(path);
            q.setResumeReqested(false);
            q.setReloadRequested(false);
            q.run(ValidatorFactory.create(q, this));
        }
        return null;
    }

    public Object handleDownloadScriptCommand(NSScriptCommand command) {
        log.debug("handleDownloadScriptCommand:" + command);
        if(this.isMounted()) {
            NSDictionary args = command.evaluatedArguments();
            final Path path = PathFactory.createPath((Session)this.session,
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
                path.setLocal(new Local(path.getLocal().getParent(), (String) nameObj));
            }
            Queue q = new DownloadQueue(path);
            q.setResumeReqested(false);
            q.setReloadRequested(false);
            q.run(ValidatorFactory.create(q, this));
        }
        return null;
    }

    public Object handleUploadScriptCommand(NSScriptCommand command) {
        log.debug("handleUploadScriptCommand:" + command);
        if(this.isMounted()) {
            NSDictionary args = command.evaluatedArguments();
            final Path path = PathFactory.createPath((Session)this.session,
                    this.workdir().getAbsolute(),
                    new Local((String) args.objectForKey("Path")));
            if(path.getLocal().isFile()) {
                path.attributes.setType(Path.FILE_TYPE);
            }
            if(path.getLocal().isDirectory()) {
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
            final Queue q = new UploadQueue(path);
            q.setResumeReqested(false);
            q.setReloadRequested(false);
            q.run(ValidatorFactory.create(q, this));
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
        this._updateBrowserColumns(this.browserListView);
        this._updateBrowserColumns(this.browserOutlineView);

        // Configure window
        this.window.setTitle(
                (String)NSBundle.mainBundle().infoDictionary().objectForKey("CFBundleName"));
        if(Preferences.instance().getBoolean("browser.bookmarkDrawer.isOpen")) {
            this.bookmarkDrawer.open();
        }
        // Configure Toolbar
        this.toolbar = new NSToolbar("Cyberduck Toolbar");
        this.toolbar.setDelegate(this);
        this.toolbar.setAllowsUserCustomization(true);
        this.toolbar.setAutosavesConfiguration(true);
        this.window.setToolbar(toolbar);

        this.browserSwitchClicked(this.browserSwitchView);

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
                public boolean accept(Path file) {
                    return file.getName().indexOf(searchString) != -1;
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
    protected void reloadData(final boolean preserveSelection) {
        log.debug("reloadData:" + preserveSelection);
        if(this.isMounted()) {
            if(!this.workdir().isCached() || this.workdir().cache().attributes().isDirty()) {
                // Reloading a workdir that is not cached yet would cause the interface to freeze;
                // Delay until path is cached in the background
                this.background(new BackgroundAction() {
                    public void run() {
                        workdir().list();
                    }

                    public void cleanup() {
                        reloadData(preserveSelection);
                    }
                });
                return;
            }
        }
        List selected = null;
        if(preserveSelection) {
            //Remember the previously selected paths
            selected = this.getSelectedPaths();
        }
        this.deselectAll();
        // Tell the browser view to reload the data. This will request all paths from the browser model
        // which will refetch paths from the server marked as invalid.
        final NSTableView browser = this.getSelectedBrowserView();
        browser.reloadData();
        if(this.isMounted()) {
            this.statusLabel.setAttributedStringValue(new NSAttributedString(
                    browser.numberOfRows() + " " + NSBundle.localizedString("files", ""),
                    TRUNCATE_MIDDLE_ATTRIBUTES));
            this.statusLabel.display();
        }
        if(preserveSelection) {
            this.setSelectedPaths(selected);
        }
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
        List list = new ArrayList();
        list.add(selected);
        this.setSelectedPaths(list);
    }

    protected void setSelectedPaths(List selected) {
        this.deselectAll();
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
    protected List getSelectedPaths() {
        List selectedFiles = new ArrayList();
        if(this.isMounted()) {
            switch(this.browserSwitchView.selectedSegment()) {
                case LIST_VIEW: {
                    NSEnumerator iterator = this.browserListView.selectedRowEnumerator();
                    List childs = this.browserListModel.childs(this.workdir());
                    while(iterator.hasMoreElements()) {
                        int row = ((Integer) iterator.nextElement()).intValue();
                        Path selected = (Path) childs.get(row);
                        if(null == selected) {
                            break;
                        }
                        selectedFiles.add(selected);
                    }
                    break;
                }
                case OUTLINE_VIEW: {
                    NSEnumerator iterator = this.browserOutlineView.selectedRowEnumerator();
                    while(iterator.hasMoreElements()) {
                        int row = ((Integer) iterator.nextElement()).intValue();
                        Path selected = (Path) this.browserOutlineView.itemAtRow(row);
                        if(null == selected) {
                            break;
                        }
                        selectedFiles.add(selected);
                    }
                    break;
                }
            }
        }
        return selectedFiles;
    }

    protected int getSelectionCount() {
        return this.getSelectedBrowserView().numberOfSelectedRows();
    }

    private void deselectAll() {
        this.getSelectedBrowserView().deselectAll(null);
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
        this.browserSwitchView.setAction(new NSSelector("browserSwitchClicked", new Class[]{Object.class}));
        ((NSSegmentedCell) this.browserSwitchView.cell()).setTrackingMode(NSSegmentedCell.NSSegmentSwitchTrackingSelectOne);
        this.browserSwitchView.cell().setControlSize(NSCell.RegularControlSize);
        this.browserSwitchView.setSelected(Preferences.instance().getInteger("browser.view"));
    }

    public void browserSwitchClicked(final Object sender) {
        if(sender instanceof NSMenuItem) {
            this.browserSwitchView.setSelected(((NSMenuItem) sender).tag());
            this.browserTabView.selectTabViewItemAtIndex(((NSMenuItem) sender).tag());
            Preferences.instance().setProperty("browser.view", ((NSMenuItem) sender).tag());
        }
        if(sender instanceof NSSegmentedControl) {
            this.browserTabView.selectTabViewItemAtIndex(((NSSegmentedControl) sender).selectedSegment());
            Preferences.instance().setProperty("browser.view", ((NSSegmentedControl) sender).selectedSegment());
        }
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
                view.setIndicatorImage(null, view.tableColumnWithIdentifier(this.selectedColumnIdentifier()));
                this.setSelectedColumn(tableColumn);
            }
            view.setIndicatorImage(this.isSortedAscending() ?
                    NSImage.imageNamed("NSAscendingSortIndicator") :
                    NSImage.imageNamed("NSDescendingSortIndicator"),
                    tableColumn);
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
                if(inspector != null && inspector.window().isVisible()) {
                    List files = new ArrayList();
                    for(Iterator i = getSelectedPaths().iterator(); i.hasNext();) {
                        files.add(i.next());
                    }
                    inspector.setFiles(files);
                }
            }
        }
    }

    private CDBrowserOutlineViewModel browserOutlineModel;
    private NSOutlineView browserOutlineView; // IBOutlet
    private CDTableDelegate browserOutlineViewDelegate;

    public void setBrowserOutlineView(NSOutlineView browserOutlineView) {
        this.browserOutlineView = browserOutlineView;
        // receive drag events from types
        this.browserOutlineView.registerForDraggedTypes(new NSArray(new Object[]{
                "QueuePboardType",
                NSPasteboard.FilenamesPboardType, //accept files dragged from the Finder for uploading
                NSPasteboard.FilesPromisePboardType} //accept file promises made myself but then interpret them as QueuePboardType
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
                if(CDBrowserController.this.browserOutlineView.numberOfSelectedRows() == 1) {
                    CDBrowserController.this.browserOutlineView.editLocation(
                            CDBrowserController.this.browserOutlineView.columnWithIdentifier(CDBrowserTableDataSource.FILENAME_COLUMN),
                            CDBrowserController.this.browserOutlineView.selectedRow(),
                            null, true);
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
                        ((CDOutlineCell) cell).setIcon(browserOutlineModel.iconforPath(item));
                        ((CDOutlineCell) cell).setAttributedStringValue(new NSAttributedString(item.getName(),
                                CDTableCell.PARAGRAPH_DICTIONARY_LEFT_ALIGNEMENT));
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
                                                    Object item, NSPoint mouseLocation) {
                if(item instanceof Path) {
                    Path p = (Path) item;
                    return this.tooltipForPath(p);
                }
                return null;
            }
        });
        NSSelector setResizableMaskSelector = new NSSelector("setResizingMask", new Class[]{int.class});
        {
            NSTableColumn c = new NSTableColumn();
            c.headerCell().setStringValue(NSBundle.localizedString("Filename", "A column in the browser"));
            c.setIdentifier("FILENAME");
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
                "QueuePboardType",
                NSPasteboard.FilenamesPboardType, //accept files dragged from the Finder for uploading
                NSPasteboard.FilesPromisePboardType} //accept file promises made myself but then interpret them as QueuePboardType
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
                if(CDBrowserController.this.browserListView.numberOfSelectedRows() == 1) {
                    CDBrowserController.this.browserListView.editLocation(
                            CDBrowserController.this.browserListView.columnWithIdentifier(CDBrowserTableDataSource.FILENAME_COLUMN),
                            CDBrowserController.this.browserListView.selectedRow(),
                            null, true);
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
                    return this.tooltipForPath(p);
                }
                return null;
            }
        });
        NSSelector setResizableMaskSelector
                = new NSSelector("setResizingMask", new Class[]{int.class});
        {
            NSTableColumn c = new NSTableColumn();
            c.setIdentifier(CDBrowserTableDataSource.TYPE_COLUMN);
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
        if(Preferences.instance().getBoolean("browser.horizontalLines") && Preferences.instance().getBoolean("browser.verticalLines"))
        {
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
            c.setWidth(180f);
            c.setMaxWidth(500f);
            if(setResizableMaskSelector.implementedByClass(NSTableColumn.class)) {
                c.setResizingMask(NSTableColumn.AutoresizingMask | NSTableColumn.UserResizingMask);
            }
            else {
                c.setResizable(true);
            }
            c.setDataCell(new NSTextFieldCell());
            c.dataCell().setFormatter(
                    new NSGregorianDateFormatter(
                            (String) NSUserDefaults.standardUserDefaults().objectForKey(NSUserDefaults.ShortTimeDateFormatString),
                            true)
            );
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
                if(bookmarkTable.numberOfSelectedRows() == 1) {
                    final Host selected = (Host) HostCollection.instance().get(bookmarkTable.selectedRow());
//                    if(CDBrowserController.this.isMounted()) {
//                        if(Preferences.instance().getBoolean("browser.openBookmarkinNewWindowIfMounted")) {
//                            CDBrowserController browser =
//                                    ((CDMainController) NSApplication.sharedApplication().delegate()).newDocument(true);
//                            browser.mount(selected);
//                            return;
//                        }
//                    }
                    CDBrowserController.this.mount(selected);
                    if(Preferences.instance().getBoolean("browser.closeDrawer")) {
                        bookmarkDrawer.close();
                    }
                }
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

        this.bookmarkTable.setRowHeight(45f);
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

    private NSMenu contextEditMenu;
    private NSObject contextEditMenuDelegate; // NSMenu.Delegate

    public void setContextEditMenu(NSMenu contextEditMenu) {
        this.contextEditMenu = contextEditMenu;
        this.contextEditMenu.setAutoenablesItems(true);
        this.contextEditMenu.setDelegate(this.contextEditMenuDelegate = new EditMenuDelegate());
    }

    /**
     * Used to provide a custom icon for the edit menu and disable the menu if no external editor
     * can be found
     */
    protected class EditMenuDelegate extends NSObject {

        public int numberOfItemsInMenu(NSMenu menu) {
            int n = Editor.INSTALLED_EDITORS.size();
            if(0 == n) {
                return 1;
            }
            return n;
        }

        public boolean menuUpdateItemAtIndex(NSMenu menu, NSMenuItem item, int index, boolean shouldCancel) {
            if(Editor.INSTALLED_EDITORS.size() == 0) {
                item.setTitle(NSBundle.localizedString("No external editor available"));
                return false;
            }
            String identifier = (String) Editor.INSTALLED_EDITORS.values().toArray(new String[]{})[index];
            String editor = (String) Editor.INSTALLED_EDITORS.keySet().toArray(new String[]{})[index];
            item.setTitle(editor);
            if(editor.equals(Preferences.instance().getProperty("editor.name"))) {
                item.setKeyEquivalent("j");
                item.setKeyEquivalentModifierMask(NSEvent.CommandKeyMask);
            }
            else {
                item.setKeyEquivalent("");
            }
            String path = NSWorkspace.sharedWorkspace().absolutePathForAppBundleWithIdentifier(
                    identifier);
            if(path != null) {
                NSImage icon = NSWorkspace.sharedWorkspace().iconForFile(path);
                icon.setSize(new NSSize(16f, 16f));
                item.setImage(icon);
            }
            else {
                item.setImage(NSImage.imageNamed("pencil.tiff"));
            }
            item.setAction(new NSSelector("editButtonContextMenuClicked", new Class[]{Object.class}));
            return !shouldCancel;
        }
    }

    private NSPopUpButton actionPopupButton;

    public void setActionPopupButton(NSPopUpButton actionPopupButton) {
        this.actionPopupButton = actionPopupButton;
        this.actionPopupButton.setPullsDown(true);
        this.actionPopupButton.setAutoenablesItems(true);
        this.actionPopupButton.itemAtIndex(0).setImage(NSImage.imageNamed("gear.tiff"));
    }

    private NSComboBox quickConnectPopup; // IBOutlet

    private NSObject quickConnectPopupModel;

    public void setQuickConnectPopup(NSComboBox quickConnectPopup) {
        this.quickConnectPopup = quickConnectPopup;
        this.quickConnectPopup.setTarget(this);
        this.quickConnectPopup.setCompletes(true);
        this.quickConnectPopup.setAction(new NSSelector("quickConnectSelectionChanged", new Class[]{Object.class}));
        this.quickConnectPopup.setUsesDataSource(true);
        this.quickConnectPopup.setDataSource(this.quickConnectPopupModel = new NSObject/*NSComboBox.DataSource*/() {
            public int numberOfItemsInComboBox(NSComboBox combo) {
                return HostCollection.instance().size();
            }

            public Object comboBoxObjectValueForItemAtIndex(NSComboBox combo, int row) {
                if(row < numberOfItemsInComboBox(combo)) {
                    return ((Host)HostCollection.instance().get(row)).getNickname();
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
                (Host)HostCollection.instance().get(bookmarkTable.selectedRow())
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
        this.bookmarkTable.selectRow(HostCollection.instance().lastIndexOf(item), false);
        this.bookmarkTable.scrollRowToVisible(HostCollection.instance().lastIndexOf(item));
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
            indexes[i] = ((Integer) iterator.nextElement()).intValue();
            i++;
        }
        this.bookmarkTable.deselectAll(null);
        int j = 0;
        for(i = 0; i < indexes.length; i++) {
            int row = indexes[i] - j;
            this.bookmarkTable.selectRow(row, false);
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
        final Path selected = this.session.getPreviousPath();
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
        final Path selected = this.session.getForwardPath();
        if(selected != null) {
            this.background(new BackgroundAction() {
                public void run() {
                    setWorkdir(selected);
                }

                public void cleanup () {
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
                setWorkdir(previous.getParent());
            }

            public void cleanup() {
                setSelectedPath(previous);
            }
        });
    }

    private static final NSImage DISK_ICON = NSImage.imageNamed("disk.tiff");

    private List pathPopupItems = new ArrayList();
    private Path workdir;

    private NSPopUpButton pathPopupButton; // IBOutlet

    public void setPathPopup(NSPopUpButton pathPopupButton) {
        this.pathPopupButton = pathPopupButton;
        this.pathPopupButton.setTarget(this);
        this.pathPopupButton.setAction(new NSSelector("pathPopupSelectionChanged",
                new Class[]{Object.class}));
    }

    public void pathPopupSelectionChanged(final Object sender) {
        final Path selected = (Path) pathPopupItems.get(pathPopupButton.indexOfSelectedItem());
        final Path previous = this.workdir();
        if(selected != null) {
            this.background(new BackgroundAction() {
                public void run() {
                    setWorkdir(selected);
                }

                public void cleanup () {
                    if(previous.getParent().equals(selected)) {
                        setSelectedPath(previous);
                    }
                }
            });
        }
    }

    private static final NSImage FOLDER_ICON = NSImage.imageNamed("folder16.tiff");

    private void addPathToPopup(Path p) {
        this.pathPopupItems.add(p);
        this.pathPopupButton.addItem(p.getAbsolute());
        if(p.isRoot()) {
            this.pathPopupButton.itemAtIndex(
                    this.pathPopupButton.numberOfItems() - 1).setImage(DISK_ICON);
        }
        else {
            this.pathPopupButton.itemAtIndex(
                    this.pathPopupButton.numberOfItems() - 1).setImage(FOLDER_ICON);
        }
    }

    private NSPopUpButton encodingPopup;

    public void setEncodingPopup(NSPopUpButton encodingPopup) {
        this.encodingPopup = encodingPopup;
        this.encodingPopup.setTarget(this);
        this.encodingPopup.setAction(new NSSelector("encodingButtonClicked", new Class[]{Object.class}));
        this.encodingPopup.removeAllItems();
        this.encodingPopup.addItemsWithTitles(new NSArray(
                ((CDMainController)NSApplication.sharedApplication().delegate()).availableCharsets()));
         this.encodingPopup.setTitle(Preferences.instance().getProperty("browser.charset.encoding"));
    }

    public void encodingButtonClicked(final Object sender) {
        String e = null;
        if(sender instanceof NSMenuItem) {
            e = ((NSMenuItem) sender).title();
        }
        if(sender instanceof NSPopUpButton) {
            e = this.encodingPopup.titleOfSelectedItem();
        }
        final String encoding = e;
        if(null == encoding) {
            return;
        }
        this.setEncoding(encoding);
        if(this.isMounted()) {
            if(this.session.getHost().getEncoding().equals(encoding)) {
                return;
            }
            this.interrupt();
            this.background(new BackgroundAction() {
                public void run() {
                    unmount(false);
                }

                public void cleanup() {
                    session.getHost().setEncoding(encoding);
                    reloadButtonClicked(sender);
                }
            });
        }
    }

    /**
     * @param encoding
     */
    private void setEncoding(final String encoding) {
        this.encodingPopup.setTitle(encoding);
    }

    // ----------------------------------------------------------
    // Drawers
    // ----------------------------------------------------------

    public void toggleBookmarkDrawer(final Object sender) {
        this.bookmarkDrawer.toggle(this);
        if(this.bookmarkDrawer.state() == NSDrawer.OpenState || this.bookmarkDrawer.state() == NSDrawer.OpeningState) {
            this.window.makeFirstResponder(this.bookmarkTable);
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

            private NSTextView textView;

            public void setTextView(NSTextView textView) {
                this.textView = textView;
                this.textView.textStorage().appendAttributedString(
                        new NSAttributedString(session.getSecurityInformation(), FIXED_WITH_FONT_ATTRIBUTES));
            }
        };
        synchronized(NSApplication.sharedApplication()) {
            if (!NSApplication.loadNibNamed("Security", c)) {
                log.fatal("Couldn't load Security.nib");
            }
        }
    }

    // ----------------------------------------------------------
    // Selector methods for the toolbar items
    // ----------------------------------------------------------

    public void showTransferQueueClicked(final Object sender) {
        CDQueueController controller = CDQueueController.instance();
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
     *
     * @param path
     * @param renamed
     */
    protected void renamePath(final Path path, final Path renamed) {
        this.renamePaths(Collections.singletonMap(path, renamed));
    }

    /**
     *
     * @param files
     */
    protected void renamePaths(final Map files) {
        if(files.size() > 0) {
            StringBuffer alertText = new StringBuffer(
                    NSBundle.localizedString("A file with the same name already exists. Do you want to replace the existing file?", ""));
            int i = 0;
            Iterator iter = null;
            boolean alert = false;
            for(iter = files.values().iterator(); i < 10 && iter.hasNext();) {
                Path item = (Path) iter.next();
                if(item.exists()) {
                    alertText.append("\n"+Character.toString('\u2022')+" "+item.getName());
                    alert = true;
                }
                i++;
            }
            if(iter.hasNext()) {
                alertText.append("\n"+Character.toString('\u2022')+" ...)");
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
                            CDBrowserController.this.renamePathsImpl(files);
                        }
                    }
                };
                c.beginSheet(true);
            }
            else {
                this.renamePathsImpl(files);
            }
        }
    }

    private void renamePathsImpl(final Map files) {
        this.background(new BackgroundAction() {
            public void run() {
                Iterator originalIterator = files.keySet().iterator();
                Iterator renamedIterator = files.values().iterator();
                while(originalIterator.hasNext()) {
                    ((Path)originalIterator.next()).rename(((Path)renamedIterator.next()).getAbsolute());
                    if(!isConnected()) {
                        break;
                    }
                }
            }

            public void cleanup() {
                reloadData(false);
                setSelectedPaths(new ArrayList(files.values()));
            }
        });
    }

    /**
     *
     * @param file
     */
    public void deletePath(final Path file) {
        this.deletePaths(Collections.singletonList(file));
    }

    /**
     *
     * @param files
     */
    public void deletePaths(final List files) {
        if(files.size() > 0) {
            StringBuffer alertText =
                    new StringBuffer(NSBundle.localizedString("Really delete the following files? This cannot be undone.", "Confirm deleting files."));
            int i = 0;
            Iterator iter = null;
            for(iter = files.iterator(); i < 10 && iter.hasNext();) {
                alertText.append("\n"+Character.toString('\u2022')+" "+((Path) iter.next()).getName());
                i++;
            }
            if(iter.hasNext()) {
                alertText.append("\n"+Character.toString('\u2022')+" "+"(...)");
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
                        CDBrowserController.this.deletePathsImpl(files);
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
                    ((Path) iter.next()).delete();
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

    public void editButtonContextMenuClicked(final Object sender) {
        this.editButtonClicked(sender);
    }

    public void editButtonClicked(final Object sender) {
        for(Iterator i = this.getSelectedPaths().iterator(); i.hasNext();) {
            final Path selected = (Path) i.next();
            if(this.isEditable(selected)) {
                Editor editor = null;
                if(sender instanceof NSMenuItem) {
                    Object identifier = Editor.SUPPORTED_EDITORS.get(((NSMenuItem) sender).title());
                    if(identifier != null) {
                        editor = new Editor((String) identifier, this);
                    }
                }
                if(null == editor) {
                    editor = new Editor(Preferences.instance().getProperty("editor.bundleIdentifier"), this);
                }
                editor.open(selected);
            }
        }
    }

    /**
     * @param selected
     * @return True if the selected path is editable (not a directory and no known binary file)
     */
    private boolean isEditable(final Path selected) {
        if(selected.attributes.isFile()) {
            if(null == selected.getExtension()) {
                return true;
            }
            if(selected.getExtension() != null) {
                StringTokenizer binaryTypes = new StringTokenizer(Preferences.instance().getProperty("editor.disabledFiles"), " ");
                while(binaryTypes.hasMoreTokens()) {
                    if(binaryTypes.nextToken().equalsIgnoreCase(selected.getExtension())) {
                        return false;
                    }
                }
            }
            return true;
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


    private CDInfoController inspector = null;

    public void infoButtonClicked(final Object sender) {
        if(this.getSelectionCount() > 0) {
            List files = this.getSelectedPaths();
            if(Preferences.instance().getBoolean("browser.info.isInspector")) {
                if(null == this.inspector) {
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

    public void downloadToButtonClicked(final Object sender) {
        NSOpenPanel panel = NSOpenPanel.openPanel();
        panel.setCanChooseDirectories(true);
        panel.setCanCreateDirectories(true);
        panel.setCanChooseFiles(false);
        panel.setAllowsMultipleSelection(false);
        panel.setPrompt(NSBundle.localizedString("Download To", ""));
        panel.setTitle(NSBundle.localizedString("Download To", ""));
        panel.beginSheetForDirectory(
                lastSelectedDownloadDirectory, //trying to be smart
                null,
                null,
                this.window,
                this,
                new NSSelector("downloadToPanelDidEnd", new Class[]{NSOpenPanel.class, int.class, Object.class}),
                null);
    }


    public void downloadToPanelDidEnd(NSOpenPanel sheet, int returncode, Object contextInfo) {
        if(returncode == CDSheetCallback.DEFAULT_OPTION) {
            Queue q = new DownloadQueue();
            Session session;
            if(Preferences.instance().getInteger("connection.pool.max") == 1) {
                session = this.getSession();
            }
            else {
                session = (Session) this.getSession().clone();
            }
            for(Iterator i = this.getSelectedPaths().iterator(); i.hasNext();) {
                Path path = (Path) ((Path) i.next()).clone(session);
                path.setLocal(new Local(sheet.filename(), path.getLocal().getName()));
                q.addRoot(path);
            }
            this.transfer(q);
        }
        lastSelectedDownloadDirectory = sheet.filename();
    }


    public void downloadAsButtonClicked(final Object sender) {
        Session session;
        if(Preferences.instance().getInteger("connection.pool.max") == 1) {
            session = this.getSession();
        }
        else {
            session = (Session) this.getSession().clone();
        }
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
        if(returncode == CDSheetCallback.DEFAULT_OPTION) {
            String filename;
            if((filename = sheet.filename()) != null) {
                Path path = (Path) contextInfo;
                path.setLocal(new Local(filename));
                Queue q = new DownloadQueue();
                q.addRoot(path);
                this.transfer(q);
            }
        }
    }

    public void syncButtonClicked(final Object sender) {
        Path selection;
        Session session;
        if(Preferences.instance().getInteger("connection.pool.max") == 1) {
            session = this.getSession();
        }
        else {
            session = (Session) this.getSession().clone();
        }
        if(this.getSelectionCount() == 1 &&
                this.getSelectedPath().attributes.isDirectory()) {
            selection = (Path) this.getSelectedPath().clone(session);
        }
        else {
            selection = (Path) this.workdir().clone(session);
        }
        NSOpenPanel panel = NSOpenPanel.openPanel();
        panel.setCanChooseDirectories(selection.attributes.isDirectory());
        panel.setCanChooseFiles(selection.attributes.isFile());
        panel.setCanCreateDirectories(true);
        panel.setAllowsMultipleSelection(false);
        panel.setMessage(NSBundle.localizedString("Synchronize", "")
                + " " + selection.getName() + " "
                + NSBundle.localizedString("with", "Synchronize <file> with <file>") + "...");
        panel.setPrompt(NSBundle.localizedString("Choose", ""));
        panel.setTitle(NSBundle.localizedString("Synchronize", ""));
        panel.beginSheetForDirectory(null,
                null,
                null,
                this.window, //parent window
                this,
                new NSSelector("syncPanelDidEnd", new Class[]{NSOpenPanel.class, int.class, Object.class}),
                selection //context info
        );
    }

    public void syncPanelDidEnd(NSOpenPanel sheet, int returncode, Object contextInfo) {
        if(returncode == CDSheetCallback.DEFAULT_OPTION) {
            final Path selection = (Path) contextInfo;
            if(sheet.filenames().count() > 0) {
                selection.setLocal(new Local((String) sheet.filenames().lastObject()));
                final Queue q = new SyncQueue();
                q.addListener(new QueueAdapter() {
                    public void queueStopped() {
                        if(isMounted()) {
                            CDBrowserController.this.invoke(new Runnable() {
                                public void run() {
                                    selection.invalidate();
                                    reloadData(true);
                                }
                            });
                        }
                        q.removeListener(this);
                    }
                });
                q.addRoot(selection);
                this.transfer(q);
            }
        }
    }

    public void downloadButtonClicked(final Object sender) {
        Queue q = new DownloadQueue();
        Session session;
        if(Preferences.instance().getInteger("connection.pool.max") == 1) {
            session = this.getSession();
        }
        else {
            session = (Session) this.getSession().clone();
        }
        for(Iterator i = this.getSelectedPaths().iterator(); i.hasNext();) {
            Path path = (Path) ((Path) i.next()).clone(session);
            q.addRoot(path);
        }
        this.transfer(q);
    }

    private static String lastSelectedUploadDirectory = null;

    public void uploadButtonClicked(final Object sender) {
        NSOpenPanel panel = NSOpenPanel.openPanel();
        panel.setCanChooseDirectories(true);
        panel.setCanCreateDirectories(false);
        panel.setCanChooseFiles(true);
        panel.setAllowsMultipleSelection(true);
        panel.setPrompt(NSBundle.localizedString("Upload", ""));
        panel.setTitle(NSBundle.localizedString("Upload", ""));
        panel.beginSheetForDirectory(
                lastSelectedUploadDirectory, //trying to be smart
                null,
                null,
                this.window,
                this,
                new NSSelector("uploadPanelDidEnd", new Class[]{NSOpenPanel.class, int.class, Object.class}),
                null);
    }

    public void uploadPanelDidEnd(NSOpenPanel sheet, int returncode, Object contextInfo) {
        if(returncode == CDSheetCallback.DEFAULT_OPTION) {
            final Path workdir = this.workdir();
            // selected files on the local filesystem
            NSArray selected = sheet.filenames();
            java.util.Enumeration iterator = selected.objectEnumerator();
            final Queue q = new UploadQueue();
            q.addListener(new QueueAdapter() {
                public void queueStopped() {
                    if(isMounted()) {
                        CDBrowserController.this.invoke(new Runnable() {
                            public void run() {
                                workdir.invalidate();
                                reloadData(true);
                            }
                        });
                    }
                    q.removeListener(this);
                }
            });
            Session session;
            if(Preferences.instance().getInteger("connection.pool.max") == 1) {
                session = this.getSession();
            }
            else {
                session = (Session) this.getSession().clone();
            }
            while(iterator.hasMoreElements()) {
                q.addRoot(PathFactory.createPath(session,
                        workdir.getAbsolute(),
                        new Local((String) iterator.nextElement())));
            }
            this.transfer(q);
        }
        lastSelectedUploadDirectory = new File(sheet.filename()).getParent();
    }

    /**
     * Trasnfers the files either using the queue or using
     * the browser session if #connection.pool.max is 1
     * @param q
     * @see CDQueueController
     */
    protected void transfer(final Queue q) {
        if(Preferences.instance().getInteger("connection.pool.max") == 1) {
            this.background(new BackgroundAction() {
                public void run() {
                    q.run(ValidatorFactory.create(q, CDBrowserController.this));
                }

                public void cleanup() {
                    ;
                }
            });
        }
        else {
            CDQueueController.instance().startItem(q);
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
        CDSheetController controller = new CDConnectionController(this);
        controller.beginSheet(false);
    }

    public void interruptButtonClicked(final Object sender) {
        this.interrupt();
    }

    public void disconnectButtonClicked(final Object sender) {
        if(this.activityRunning) {
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

    public void showHiddenFilesClicked(final Object sender) {
        if(sender instanceof NSMenuItem) {
            NSMenuItem item = (NSMenuItem) sender;
            if(item.state() == NSCell.OnState) {
                this.setShowHiddenFiles(false);
                item.setState(NSCell.OffState);
            }
            else if(item.state() == NSCell.OffState) {
                this.setShowHiddenFiles(true);
                item.setState(NSCell.OnState);
            }
            if(this.isMounted()) {
                this.reloadData(true);
            }
        }
    }

    /**
     * @return true if a connection is being opened or is already initialized
     */
    public boolean hasSession() {
        return this.session != null;
    }

    public Session getSession() {
        return this.session;
    }

    /**
     * @return true if the remote file system has been mounted
     */
    public boolean isMounted() {
        return this.hasSession() && this.workdir() != null;
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

    public void paste(final Object sender) {
        final NSPasteboard pboard = NSPasteboard.pasteboardWithName("QueuePBoard");
        if(pboard.availableTypeFromArray(new NSArray("QueuePBoardType")) != null) {
            Object o = pboard.propertyListForType("QueuePBoardType");// get the data from paste board
            if(o != null) {
                final NSArray elements = (NSArray) o;
                final Map files = new HashMap();
                Path parent = this.workdir();
                if(this.getSelectionCount() == 1) {
                    Path selected = this.getSelectedPath();
                    if(selected.attributes.isDirectory()) {
                        parent = selected;
                    }
                    else {
                        parent = selected.getParent();
                    }
                }
                for(int i = 0; i < elements.count(); i++) {
                    NSDictionary dict = (NSDictionary) elements.objectAtIndex(i);
                    Queue q = QueueFactory.create(dict);
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
                final Queue q = new UploadQueue();
                final Path workdir = this.workdir();
                Session session;
                if(Preferences.instance().getInteger("connection.pool.max") == 1) {
                    session = this.getSession();
                }
                else {
                    session = (Session) this.getSession().clone();
                }
                for(int i = 0; i < elements.count(); i++) {
                    Path p = PathFactory.createPath(session,
                            workdir.getAbsolute(),
                            new Local((String) elements.objectAtIndex(i)));
                    q.addRoot(p);
                }
                if(q.numberOfRoots() > 0) {
                    q.addListener(new QueueAdapter() {
                        public void queueStopped() {
                            if(isMounted()) {
                                CDBrowserController.this.invoke(new Runnable() {
                                    public void run() {
                                        workdir.invalidate();
                                        CDBrowserController.this.reloadData(true);
                                    }
                                });
                            }
                            q.removeListener(this);
                        }
                    });
                    this.transfer(q);
                }
            }
        }
    }

    public void copyURLButtonClicked(final Object sender) {
        Host h = this.session.getHost();
        StringBuffer url = new StringBuffer(h.getURL());
        if(this.getSelectionCount() > 0) {
            Path p = this.getSelectedPath();
            url.append(p.getAbsolute());
        }
        else {
            url.append(this.workdir().getAbsolute());
        }
        NSPasteboard pboard = NSPasteboard.generalPasteboard();
        pboard.declareTypes(new NSArray(NSPasteboard.StringPboardType), null);
        if(!pboard.setStringForType(url.toString(), NSPasteboard.StringPboardType)) {
            log.error("Error writing URL to NSPasteboard.StringPboardType.");
        }
    }

    public void cut(final Object sender) {
        if(this.getSelectionCount() > 0) {
            Queue q = new DownloadQueue();
            for(Iterator i = this.getSelectedPaths().iterator(); i.hasNext();) {
                q.addRoot((Path) i.next());
            }
            // Writing data for private use when the item gets dragged to the transfer queue.
            NSPasteboard queuePboard = NSPasteboard.pasteboardWithName("QueuePBoard");
            queuePboard.declareTypes(new NSArray("QueuePBoardType"), null);
            if(queuePboard.setPropertyListForType(new NSArray(q.getAsDictionary()), "QueuePBoardType")) {
                log.debug("QueuePBoardType data sucessfully written to pasteboard");
            }
            Path p = this.getSelectedPath();
            NSPasteboard pboard = NSPasteboard.generalPasteboard();
            pboard.declareTypes(new NSArray(NSPasteboard.StringPboardType), null);
            if(!pboard.setStringForType(p.getAbsolute(), NSPasteboard.StringPboardType)) {
                log.error("Error writing absolute path of selected item to NSPasteboard.StringPboardType.");
            }
        }
    }

    /**
     * A task is in progress; e.g. a file listing is expected from the server
     */
    private boolean activityRunning;

    /**
     * A lock to make sure that actions are not run in parallel
     */
    protected final Object backgroundLock = new Object();

    /**
     * @param runnable
     * @pre must always be invoked form the main interface thread
     */
    public void background(final BackgroundAction runnable) {
        super.background(new BackgroundActionImpl(this) {
            public void run() {
                activityRunning = true;
                spinner.startAnimation(this);
                session.addErrorListener(this);
                session.addTranscriptListener(this);
                try {
                    runnable.run();
                }
                finally {
                    activityRunning = false;
                    spinner.stopAnimation(CDBrowserController.this);
                    if(hasSession()) {
                        // It is important _not_ to do this in #cleanup as otherwise
                        // the listeners are still registered when the next BackgroundAction
                        // is already running
                        session.removeTranscriptListener(this);
                        session.removeErrorListener(this);
                    }
                }
            }

            public void cleanup() {
                try {
                    statusLabel.setAttributedStringValue(new NSAttributedString(
                            getSelectedBrowserView().numberOfRows() + " " + NSBundle.localizedString("files", ""),
                            CDWindowController.TRUNCATE_MIDDLE_ATTRIBUTES));
                    statusLabel.display();
                }
                finally {
                    runnable.cleanup();
                }
            }
        }, backgroundLock);
    }

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
    protected void setWorkdir(final Path path) {
        log.debug("setWorkdir:" + path);
        if(null == path) {
            // Clear the browser view if no working directory is given
            this.workdir = null;
            this.invoke(new Runnable() {
                public void run() {
                    pathPopupItems.clear();
                    pathPopupButton.removeAllItems();
                }
            });
            this.invoke(new Runnable() {
                public void run() {
                    reloadData(false);
                }
            });
            final File bookmark = this.getRepresentedFile();
            if(bookmark != null && bookmark.exists()) {
                // Delete this history bookmark if there was any error connecting
                bookmark.delete();
            }
            this.window.setTitle(
                    (String)NSBundle.mainBundle().infoDictionary().objectForKey("CFBundleName"));
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
            path.cache().attributes().setReadable(true);
        }
        if(!path.list().attributes().isReadable()) {
            // the path given cannot be read either because it doesn't exist
            // or you don't have permission; don't update browser view
            return;
        }
        // Remove any custom file filter
        this.setFileFilter(null);
        // Update the current working directory
        this.workdir = path;
        this.session.addPathToHistory(this.workdir);
        this.invoke(new Runnable() {
            public void run() {
                pathPopupItems.clear();
                pathPopupButton.removeAllItems();
                // Update the path selection menu above the browser
                if(isMounted()) {
                    addPathToPopup(workdir);
                    for(Path p = workdir; !p.isRoot();) {
                        p = p.getParent();
                        addPathToPopup(p);
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
        this.setEncoding(host.getEncoding());
        this.window.setTitle(host.getProtocol() + ":" + host.getHostname());
        HostCollection.instance().exportBookmark(host, this.getRepresentedFile());
        if(this.getRepresentedFile().exists()) {
            // Set the window title
            this.window.setRepresentedFilename(this.getRepresentedFile().getAbsolutePath());
        }
        session.addConnectionListener(listener = new ConnectionListener() {
            /**
             * The listener used to watch for messages and errors during the session
             */
            private ProgressListener progress;

            public void connectionWillOpen() {
                session.addProgressListener(progress = new ProgressListener() {
                    public void message(final String msg) {
                        // Update the status label at the bottom of the browser window
                        statusLabel.setAttributedStringValue(new NSAttributedString(msg,
                                TRUNCATE_MIDDLE_ATTRIBUTES));
                    }
                });
            }

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
            }

            public void connectionWillClose() {
                ;
            }

            public void connectionDidClose() {
                getSelectedBrowserView().setNeedsDisplay();
                session.removeProgressListener(progress);
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
     *
     * @return The history bookmark file in the application support directory
     */
    private File getRepresentedFile() {
        if(this.hasSession()) {
            return new File(HISTORY_FOLDER, this.session.getHost().getHostname() + ".duck");
        }
        return null;
    }

    /**
     *
     */
    private Session session;

    /**
     * @param host
     * @return The session to be used for any further operations
     */
    protected Session mount(final Host host) {
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
                    interrupt();
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
        if(this.isConnected() || this.activityRunning) {
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
            this.interrupt();
            this.background(new BackgroundAction() {
                public void run() {
                    unmount(true);
                }

                public void cleanup() {
                    ;
                }
            });
        }
        // Unmount succeeded
        return true;
    }

    protected void interrupt() {
        if(this.hasSession()) {
            if(this.activityRunning) {
                //Interrupt any operation in progress; just
                //closes the socket without any quit message
                this.session.interrupt();
            }
        }
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
            log.info("Successfully printed"+contextInfo);
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
        return CDQueueController.applicationShouldTerminate(app);
    }

    public boolean windowShouldClose(final NSWindow sender) {
        return this.unmount(new CDSheetCallback() {
            public void callback(int returncode) {
                if(returncode == DEFAULT_OPTION) {
                    interrupt();
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
        this.navigationButton.setEnabled(this.isMounted() && session.getBackHistory().length > 1,
                NAVIGATION_LEFT_SEGMENT_BUTTON);
        this.navigationButton.setEnabled(this.isMounted() && session.getForwardHistory().length > 0,
                NAVIGATION_RIGHT_SEGMENT_BUTTON);
        this.upButton.setEnabled(this.isMounted() && !this.workdir().isRoot(),
                NAVIGATION_UP_SEGMENT_BUTTON);

        this.pathPopupButton.setEnabled(this.isMounted());
        this.searchField.setEnabled(this.isMounted());
        this.encodingPopup.setEnabled(!this.activityRunning);
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
                if(NSPasteboard.generalPasteboard().availableTypeFromArray(new NSArray(NSPasteboard.FilenamesPboardType)) != null)
                {
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
                NSPasteboard pboard = NSPasteboard.pasteboardWithName("QueuePBoard");
                if(pboard.availableTypeFromArray(new NSArray("QueuePBoardType")) != null) {
                    Object o = pboard.propertyListForType("QueuePBoardType");
                    if(o != null) {
                        NSArray elements = (NSArray) o;
                        for(int i = 0; i < elements.count(); i++) {
                            NSDictionary dict = (NSDictionary) elements.objectAtIndex(i);
                            Queue q = QueueFactory.create(dict);
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
        if(identifier.equals("editButtonClicked:")) {
            String editorPath = NSWorkspace.sharedWorkspace().absolutePathForAppBundleWithIdentifier(
                    Preferences.instance().getProperty("editor.bundleIdentifier"));
            if(editorPath != null) {
                NSImage icon = NSWorkspace.sharedWorkspace().iconForFile(editorPath);
                icon.setSize(new NSSize(16f, 16f));
                item.setImage(icon);
            }
        }
        if(identifier.equals("editButtonContextMenuClicked:")) {
            String bundleIdentifier = (String) Editor.SUPPORTED_EDITORS.get(item.title());
            if(null != bundleIdentifier) {
                String editorPath = NSWorkspace.sharedWorkspace().absolutePathForAppBundleWithIdentifier(identifier);
                if(editorPath != null) {
                    NSImage icon = NSWorkspace.sharedWorkspace().iconForFile(editorPath);
                    icon.setSize(new NSSize(16f, 16f));
                    item.setImage(icon);
                }
            }
        }
        if(identifier.equals("showHiddenFilesClicked:")) {
            item.setState((this.getFileFilter() instanceof NullPathFilter) ? NSCell.OnState : NSCell.OffState);
        }
        if(identifier.equals("encodingButtonClicked:")) {
            if(this.isMounted()) {
                item.setState(this.session.getHost().getEncoding().equalsIgnoreCase(item.title()) ? NSCell.OnState : NSCell.OffState);
            }
            else {
                item.setState(Preferences.instance().getProperty("browser.charset.encoding").equalsIgnoreCase(item.title()) ? NSCell.OnState : NSCell.OffState);
            }
        }
        if(identifier.equals("browserSwitchClicked:")) {
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
        if(identifier.equals("copy:")) {
            return this.isMounted() && this.getSelectionCount() > 0;
        }
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
                NSPasteboard pboard = NSPasteboard.pasteboardWithName("QueuePBoard");
                if(pboard.availableTypeFromArray(new NSArray("QueuePBoardType")) != null) {
                    Object o = pboard.propertyListForType("QueuePBoardType");
                    if(o != null) {
                        return true;
                    }
                }
            }
            return false;
        }
        if(identifier.equals("encodingButtonClicked:")) {
            return !activityRunning;
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
                    Path selected = this.getSelectedPath();
                    if(selected != null) {
                        return this.isEditable(selected);
                    }
                }
            }
            return false;
        }
        if(identifier.equals("editButtonContextMenuClicked:")) {
            if(this.isMounted()) {
                Path selected = this.getSelectedPath();
                if(selected != null) {
                    return this.isEditable(selected);
                }
            }
            return false;
        }
        if(identifier.equals("sendCustomCommandClicked:")) {
            return (this.session instanceof ch.cyberduck.core.ftp.FTPSession) && this.isConnected();
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
            return this.isMounted() && session.getBackHistory().length > 1;
        }
        if(identifier.equals("forwardButtonClicked:")) {
            return this.isMounted() && session.getForwardHistory().length > 0;
        }
        if(identifier.equals("copyURLButtonClicked:")) {
            return this.isMounted();
        }
        if(identifier.equals("printDocument:")) {
            return this.isMounted();
        }
        if(identifier.equals("disconnectButtonClicked:")) {
            if(!this.isConnected()) {
                return this.activityRunning;
            }
            return this.isConnected();
        }
        if(identifier.equals("interruptButtonClicked:")) {
            return this.activityRunning;
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
            if(activityRunning) {
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
    private static final String TOOLBAR_TOOLS = "Tools";
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

    public NSToolbarItem toolbarItemForItemIdentifier(NSToolbar toolbar, String itemIdentifier, boolean flag) {
        NSToolbarItem item = new NSToolbarItem(itemIdentifier);
        if(itemIdentifier.equals(TOOLBAR_BROWSER_VIEW)) {
            item.setLabel(NSBundle.localizedString("View", "Toolbar item"));
            item.setPaletteLabel(NSBundle.localizedString("View", "Toolbar item"));
            item.setToolTip(NSBundle.localizedString("Switch Browser View", "Toolbar item tooltip"));
            item.setView(this.browserSwitchView);
            NSMenuItem viewMenu = new NSMenuItem();
            viewMenu.setTitle(NSBundle.localizedString("View", "Toolbar item"));
            NSMenu viewSubmenu = new NSMenu();
            viewSubmenu.addItem(new NSMenuItem(NSBundle.localizedString("List", "Toolbar item"),
                    new NSSelector("browserSwitchClicked", new Class[]{Object.class}),
                    ""));
            viewSubmenu.itemWithTitle(NSBundle.localizedString("List", "Toolbar item")).setTag(0);
            viewSubmenu.addItem(new NSMenuItem(NSBundle.localizedString("Outline", "Toolbar item"),
                    new NSSelector("browserSwitchClicked", new Class[]{Object.class}),
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
        if(itemIdentifier.equals(TOOLBAR_QUICK_CONNECT)) {
            item.setLabel(NSBundle.localizedString(TOOLBAR_QUICK_CONNECT, "Toolbar item"));
            item.setPaletteLabel(NSBundle.localizedString(TOOLBAR_QUICK_CONNECT, "Toolbar item"));
            item.setToolTip(NSBundle.localizedString("Connect to server", "Toolbar item tooltip"));
            item.setView(this.quickConnectPopup);
            item.setMinSize(this.quickConnectPopup.frame().size());
            item.setMaxSize(this.quickConnectPopup.frame().size());
            return item;
        }
        if(itemIdentifier.equals(TOOLBAR_ENCODING)) {
            item.setLabel(NSBundle.localizedString(TOOLBAR_ENCODING, "Toolbar item"));
            item.setPaletteLabel(NSBundle.localizedString(TOOLBAR_ENCODING, "Toolbar item"));
            item.setToolTip(NSBundle.localizedString("Character Encoding", "Toolbar item tooltip"));
            item.setView(this.encodingPopup);
            NSMenuItem encodingMenu = new NSMenuItem(NSBundle.localizedString(TOOLBAR_ENCODING, "Toolbar item"),
                    new NSSelector("encodingButtonClicked", new Class[]{Object.class}),
                    "");
            String[] charsets = ((CDMainController)NSApplication.sharedApplication().delegate()).availableCharsets();
            NSMenu charsetMenu = new NSMenu();
            for(int i = 0; i < charsets.length; i++) {
                charsetMenu.addItem(new NSMenuItem(charsets[i],
                        new NSSelector("encodingButtonClicked", new Class[]{Object.class}),
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
            NSMenuItem toolbarMenu = new NSMenuItem(NSBundle.localizedString(TOOLBAR_EDIT, "Toolbar item"),
                    new NSSelector("editButtonClicked", new Class[]{Object.class}),
                    "");
            NSMenu editMenu = new NSMenu();
            editMenu.setAutoenablesItems(true);
            java.util.Map editors = Editor.SUPPORTED_EDITORS;
            java.util.Iterator editorNames = editors.keySet().iterator();
            java.util.Iterator editorIdentifiers = editors.values().iterator();
            while(editorNames.hasNext()) {
                String editor = (String) editorNames.next();
                String identifier = (String) editorIdentifiers.next();
                boolean enabled = NSWorkspace.sharedWorkspace().absolutePathForAppBundleWithIdentifier(
                        identifier) != null;
                if(enabled) {
                    editMenu.addItem(new NSMenuItem(editor,
                            new NSSelector("editButtonContextMenuClicked", new Class[]{Object.class}),
                            ""));
                }
            }
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
            this.session.getHost().getCredentials().setPassword(null);
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

        this.contextEditMenu.setDelegate(null);
        this.contextEditMenuDelegate = null;
        this.contextEditMenu = null;

        this.browserSwitchView.setTarget(null);
        this.browserSwitchView = null;
        this.browserTabView = null;

        this.addBookmarkButton.setTarget(null);
        this.deleteBookmarkButton.setTarget(null);
        this.editBookmarkButton.setTarget(null);

        this.actionPopupButton.setTarget(null);

        this.navigationButton.setTarget(null);
        this.upButton.setTarget(null);
        this.pathPopupButton.setTarget(null);
        this.encodingPopup.setTarget(null);

        this.quickConnectPopup.setDataSource(null);
        this.quickConnectPopupModel = null;
        this.quickConnectPopup.setTarget(null);
        this.quickConnectPopup = null;

        super.invalidate();
    }
}