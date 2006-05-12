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

import com.enterprisedt.net.ftp.FTPException;
import com.sshtools.j2ssh.SshException;

import ch.cyberduck.core.*;
import ch.cyberduck.ui.cocoa.odb.Editor;

import com.apple.cocoa.application.*;
import com.apple.cocoa.foundation.*;

import org.apache.log4j.Logger;

import java.io.File;
import java.io.IOException;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.StringTokenizer;

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
        Host host;
        Object portObj = args.objectForKey("Port");
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
            List childs = path.list();
            if(null == childs) {
                return result;
            }
            for(Iterator i = childs.iterator(); i.hasNext();) {
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
            c.createFile(this.workdir(), (String) args.objectForKey("Path"));
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
            Editor editor = new Editor(Preferences.instance().getProperty("editor.bundleIdentifier"));
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
            final Path path = PathFactory.createPath(this.session,
                    (String) args.objectForKey("Path"));
            path.attributes.setType(Path.DIRECTORY_TYPE);
            Object localObj = args.objectForKey("Local");
            if(localObj != null) {
                path.setLocal(new Local((String) localObj));
            }
            Queue q = new SyncQueue(path);
            q.run(false, true);
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
                path.setLocal(new Local(path.getLocal().getParent(), (String) nameObj));
            }
            Queue q = new DownloadQueue(path);
            q.run(false, true);
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
            Queue q = new UploadQueue(path);
            q.run(false, true);
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
        this.window.setTitle("Cyberduck");
        if(Preferences.instance().getBoolean("bookmarkDrawer.isOpen")) {
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
    private Filter filenameFilter;

    {
        if(Preferences.instance().getBoolean("browser.showHidden")) {
            this.filenameFilter = new NullFilter();
            this.showHiddenFiles = true;
        }
        else {
            this.filenameFilter = new HiddenFilesFilter();
            this.showHiddenFiles = false;
        }
    }

    protected Filter getFileFilter() {
        return this.filenameFilter;
    }

    protected void setFileFilter(final String searchString) {
        if(null == searchString || searchString.length() == 0) {
            this.searchField.setStringValue("");
            // Revert to the last used default filter
            if(this.getShowHiddenFiles()) {
                this.filenameFilter = new NullFilter();
            }
            else {
                this.filenameFilter = new HiddenFilesFilter();
            }
        }
        else {
            // Setting up a custom filter for the directory listing
            this.filenameFilter = new Filter() {
                public boolean accept(Path file) {
                    return file.getName().toLowerCase().indexOf(searchString.toLowerCase()) != -1;
                }
            };
        }
    }

    public void setShowHiddenFiles(boolean showHidden) {
        if(showHidden) {
            this.filenameFilter = new NullFilter();
            this.showHiddenFiles = true;
        }
        else {
            this.filenameFilter = new HiddenFilesFilter();
            this.showHiddenFiles = false;
        }
    }

    public boolean getShowHiddenFiles() {
        return this.showHiddenFiles;
    }

    private void getFocus() {
        this.window.makeFirstResponder(this.getSelectedBrowserView());
    }

    /**
     *
     * @param preserveSelection All selected files should be reselected after reloading the view
     */
    protected void reloadData(final boolean preserveSelection) {
        if(!Thread.currentThread().getName().equals("main") && !Thread.currentThread().getName().equals("AWT-AppKit")) {
            this.invoke(new Runnable() {
                public void run() {
                    CDBrowserController.this.reloadData(preserveSelection);
                }
            });
            return;
        }
        log.debug("reloadData:"+preserveSelection);
        List selected = null;
        if(preserveSelection) {
            //Remember the selected paths
            selected = this.getSelectedPaths();
        }
        this.deselectAll();
        // Tell the browser view to reload the data. This will request all paths from the browser model
        // which will refetch paths from the server marked as invalid.
        final NSTableView browser = this.getSelectedBrowserView();
        browser.reloadData();
        if(this.isMounted()) {
            this.infoLabel.setStringValue(this.getSelectedBrowserView().numberOfRows() + " " +
                    NSBundle.localizedString("files", ""));
        }
        else {
            this.infoLabel.setStringValue("");
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
        if(this.getSelectedBrowserModel().contains(this.getSelectedBrowserView(), path)) {
            this.selectRow(this.getSelectedBrowserModel().indexOf(this.getSelectedBrowserView(), path), expand);
        }
    }

    /**
     * @param row
     * @param expand Expand the existing selection
     */
    private void selectRow(int row, boolean expand) {
        log.debug("selectRow:" + row);
        this.getSelectedBrowserView().selectRow(row, expand);
        this.getSelectedBrowserView().scrollRowToVisible(row);
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
        super.setWindow(window);
        this.window.setDelegate(this);
    }

    private NSTextView logView;

    public void setLogView(NSTextView logView) {
        this.logView = logView;
    }

    private NSDrawer logDrawer; // IBOutlet

    public void setLogDrawer(NSDrawer logDrawer) {
        this.logDrawer = logDrawer;
    }

    private NSDrawer bookmarkDrawer;

    public void setBookmarkDrawer(NSDrawer bookmarkDrawer) {
        this.bookmarkDrawer = bookmarkDrawer;
    }

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
        public boolean isColumnEditable(NSTableColumn tableColumn) {
            if(Preferences.instance().getBoolean("browser.editable")) {
                return tableColumn.identifier().equals(CDBrowserTableDataSource.FILENAME_COLUMN);
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

            public void outlineViewWillDisplayCell(NSOutlineView outlineView, Object cell,
                                                   NSTableColumn tableColumn, Path item) {
                String identifier = (String) tableColumn.identifier();
                if(item != null) {
                    if(identifier.equals(CDBrowserTableDataSource.FILENAME_COLUMN)) {
                        NSImage icon;
                        if(item.attributes.isSymbolicLink()) {
                            icon = CDBrowserTableDataSource.SYMLINK_ICON;
                        }
                        else if(item.attributes.isDirectory()) {
                            icon = FOLDER_ICON;
                            if (!item.attributes.isExecutable()) {
                                icon = CDBrowserTableDataSource.FOLDER_NOACCESS_ICON;
                            }
                            else if (!item.attributes.isReadable()) {
                                if (item.attributes.isWritable()) {
                                    icon = CDBrowserTableDataSource.FOLDER_WRITEONLY_ICON;
                                }
                            }
                        }
                        else if(item.attributes.isFile()) {
                            icon = CDIconCache.instance().get(item.getExtension());
                        }
                        else {
                            icon = CDBrowserTableDataSource.NOT_FOUND_ICON;
                        }
                        icon.setSize(new NSSize(16f, 16f));
                        ((CDOutlineCell) cell).setIcon(icon);
                        ((CDOutlineCell) cell).setAttributedStringValue(new NSAttributedString(item.getName(),
                                CDTableCell.TABLE_CELL_PARAGRAPH_DICTIONARY));
                    }
                    if(cell instanceof NSTextFieldCell) {
                        if(CDBrowserController.this.isConnected()) {
                            ((NSTextFieldCell) cell).setTextColor(NSColor.controlTextColor());
                        }
                        else {
                            ((NSTextFieldCell) cell).setTextColor(NSColor.disabledControlTextColor());
                        }
                    }
                }
            }

            public void outlineViewItemDidExpand(NSNotification notification) {
                infoLabel.setStringValue(CDBrowserController.this.browserOutlineView.numberOfRows() + " " +
                        NSBundle.localizedString("files", ""));
            }

            public void outlineViewItemDidCollapse(NSNotification notification) {
                infoLabel.setStringValue(CDBrowserController.this.browserOutlineView.numberOfRows() + " " +
                        NSBundle.localizedString("files", ""));
            }

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
                c.setResizingMask(NSTableColumn.AutoresizingMask);
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
            c.dataCell().setAlignment(NSText.LeftTextAlignment);
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
                    if(CDBrowserController.this.isConnected()) {
                        ((NSTextFieldCell) cell).setTextColor(NSColor.controlTextColor());
                    }
                    else {
                        ((NSTextFieldCell) cell).setTextColor(NSColor.disabledControlTextColor());
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
            c.setIdentifier("FILENAME");
            c.setMinWidth(100f);
            c.setWidth(250f);
            c.setMaxWidth(1000f);
            if(setResizableMaskSelector.implementedByClass(NSTableColumn.class)) {
                c.setResizingMask(NSTableColumn.AutoresizingMask);
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
            c.dataCell().setAlignment(NSText.LeftTextAlignment);
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
            c.dataCell().setAlignment(NSText.RightTextAlignment);
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
                c.setResizingMask(NSTableColumn.AutoresizingMask);
            }
            else {
                c.setResizable(true);
            }
            c.setDataCell(new NSTextFieldCell());
            c.dataCell().setAlignment(NSText.LeftTextAlignment);
            c.dataCell().setFormatter(
                    new NSGregorianDateFormatter((String) NSUserDefaults.standardUserDefaults().objectForKey(NSUserDefaults.ShortTimeDateFormatString),
                    true));
            table.addTableColumn(c);
        }
        table.removeTableColumn(table.tableColumnWithIdentifier(CDBrowserTableDataSource.OWNER_COLUMN));
        if(Preferences.instance().getBoolean("browser.columnOwner")) {
            NSTableColumn c = new NSTableColumn();
            c.headerCell().setStringValue(NSBundle.localizedString("Owner", "A column in the browser"));
            c.setIdentifier(CDBrowserTableDataSource.OWNER_COLUMN);
            c.setMinWidth(100f);
            c.setWidth(80f);
            c.setMaxWidth(500f);
            if(setResizableMaskSelector.implementedByClass(NSTableColumn.class)) {
                c.setResizingMask(NSTableColumn.AutoresizingMask);
            }
            else {
                c.setResizable(true);
            }
            c.setDataCell(new NSTextFieldCell());
            c.dataCell().setAlignment(NSText.LeftTextAlignment);
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
                c.setResizingMask(NSTableColumn.AutoresizingMask);
            }
            else {
                c.setResizable(true);
            }
            c.setDataCell(new NSTextFieldCell());
            c.dataCell().setAlignment(NSText.LeftTextAlignment);
            table.addTableColumn(c);
        }
        table.setIndicatorImage(((CDTableDelegate) table.delegate()).isSortedAscending() ?
                NSImage.imageNamed("NSAscendingSortIndicator") :
                NSImage.imageNamed("NSDescendingSortIndicator"),
                table.tableColumnWithIdentifier(Preferences.instance().getProperty("browser.sort.column")));
        table.sizeToFit();
        this.reloadData(false);
    }

    private CDBookmarkTableDataSource bookmarkModel;
    private NSTableView bookmarkTable; // IBOutlet
    private CDTableDelegate bookmarkTableDelegate;
    private CollectionListener bookmarkCollectionListener;

    public void setBookmarkTable(NSTableView view) {
        this.bookmarkTable = view;

        this.bookmarkTable.setDataSource(this.bookmarkModel = CDBookmarkTableDataSource.instance());
        this.bookmarkModel.addListener(this.bookmarkCollectionListener = new CollectionListener() {
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
                    mount((Host) bookmarkModel.get(bookmarkTable.selectedRow()));
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

    private NSMenu editMenu;
    private NSObject editMenuDelegate; // NSMenu.Delegate

    public void setEditMenu(NSMenu editMenu) {
        this.editMenu = editMenu;
        this.editMenu.setAutoenablesItems(true);
        this.editMenu.setDelegate(this.editMenuDelegate = new EditMenuDelegate());
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
                icon.setScalesWhenResized(true);
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

    public void setQuickConnectPopup(NSComboBox quickConnectPopup) {
        this.quickConnectPopup = quickConnectPopup;
        this.quickConnectPopup.setTarget(this);
        this.quickConnectPopup.setCompletes(true);
        this.quickConnectPopup.setAction(new NSSelector("quickConnectSelectionChanged", new Class[]{Object.class}));
        this.quickConnectPopup.setUsesDataSource(true);
        this.quickConnectPopup.setDataSource(CDBookmarkTableDataSource.instance());
        NSNotificationCenter.defaultCenter().addObserver(this,
                new NSSelector("quickConnectWillPopUp", new Class[]{Object.class}),
                NSComboBox.ComboBoxWillPopUpNotification,
                this.quickConnectPopup);
        this.quickConnectWillPopUp(null);
    }

    public void quickConnectWillPopUp(NSNotification notification) {
        int size = CDBookmarkTableDataSource.instance().size();
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
            for(Iterator iter = this.bookmarkModel.iterator(); iter.hasNext();) {
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
        CDBookmarkController c = new CDBookmarkController(
                (Host) this.bookmarkModel.get(bookmarkTable.selectedRow())
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
        this.bookmarkModel.add(item);
        this.bookmarkTable.selectRow(this.bookmarkModel.lastIndexOf(item), false);
        this.bookmarkTable.scrollRowToVisible(this.bookmarkModel.lastIndexOf(item));
        CDBookmarkController c = new CDBookmarkController(item);
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
            Host host = (Host) this.bookmarkModel.get(row);
            switch(NSAlertPanel.runCriticalAlert(NSBundle.localizedString("Delete Bookmark", ""),
                    NSBundle.localizedString("Do you want to delete the selected bookmark?", "")
                            + " (" + host.getNickname() + ")",
                    NSBundle.localizedString("Delete", ""),
                    NSBundle.localizedString("Cancel", ""),
                    null)) {
                case CDSheetCallback.DEFAULT_OPTION:
                    this.bookmarkModel.remove(row);
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
        Path selected = this.session.getPreviousPath();
        if(selected != null) {
            this.setWorkdir(selected);
        }
    }

    public void forwardButtonClicked(final Object sender) {
        Path selected = this.session.getForwardPath();
        if(selected != null) {
            this.setWorkdir(selected);
        }
    }

    private NSSegmentedControl upButton; // IBOutlet

    public void setUpButton(NSSegmentedControl upButton) {
        this.upButton = upButton;
        this.upButton.setTarget(this);
        this.upButton.setAction(new NSSelector("upButtonClicked", new Class[]{Object.class}));
    }

    public void upButtonClicked(final Object sender) {
        Path previous = this.workdir();
        this.setWorkdir(this.workdir().getParent());
        this.setSelectedPath(previous);
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
        Path selected = (Path) pathPopupItems.get(pathPopupButton.indexOfSelectedItem());
        if(selected != null)
            this.setWorkdir(selected);
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
        java.util.SortedMap charsets = java.nio.charset.Charset.availableCharsets();
        String[] items = new String[charsets.size()];
        java.util.Iterator iterator = charsets.values().iterator();
        int i = 0;
        while(iterator.hasNext()) {
            items[i] = ((java.nio.charset.Charset) iterator.next()).name();
            i++;
        }
        this.encodingPopup.addItemsWithTitles(new NSArray(items));
        this.encodingPopup.setTitle(Preferences.instance().getProperty("browser.charset.encoding"));
    }

    public void encodingButtonClicked(final Object sender) {
        String encoding = null;
        if(sender instanceof NSMenuItem) {
            encoding = ((NSMenuItem) sender).title();
        }
        if(sender instanceof NSPopUpButton) {
            encoding = this.encodingPopup.titleOfSelectedItem();
        }
        if(null == encoding) {
            return;
        }
        this.setEncoding(encoding);
        if(this.isMounted()) {
            if(this.session.getHost().getEncoding().equals(encoding)) {
                return;
            }
            this.unmount();
            this.session.getHost().setEncoding(encoding);
            this.reloadButtonClicked(sender);
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

    public void toggleLogDrawer(final Object sender) {
        this.logDrawer.toggle(this);
    }

    public void toggleBookmarkDrawer(final Object sender) {
        this.bookmarkDrawer.toggle(this);
        Preferences.instance().setProperty("bookmarkDrawer.isOpen", this.bookmarkDrawer.state() == NSDrawer.OpenState || this.bookmarkDrawer.state() == NSDrawer.OpeningState);
        if(this.bookmarkDrawer.state() == NSDrawer.OpenState || this.bookmarkDrawer.state() == NSDrawer.OpeningState) {
            this.window.makeFirstResponder(this.bookmarkTable);
        }
        else {
            if(this.isMounted()) {
                this.getFocus();
            }
            else {
                this.window.makeFirstResponder(this.quickConnectPopup);
            }
        }
    }

    // ----------------------------------------------------------
    // Status
    // ----------------------------------------------------------

    /**
     * A task is in progress; e.g. a file listing is expected from the server
     */
    private boolean activityRunning;

    private NSProgressIndicator progressIndicator; // IBOutlet

    public void setProgressIndicator(NSProgressIndicator progressIndicator) {
        this.progressIndicator = progressIndicator;
        this.progressIndicator.setIndeterminate(true);
        this.progressIndicator.setUsesThreadedAnimation(true);
    }

    private NSTextField statusLabel; // IBOutlet

    public void setStatusLabel(NSTextField statusLabel) {
        this.statusLabel = statusLabel;
        this.statusLabel.setAttributedStringValue(new NSAttributedString(NSBundle.localizedString("Idle", "Status", ""),
                TRUNCATE_MIDDLE_PARAGRAPH_DICTIONARY));
    }

    private NSTextField infoLabel; // IBOutlet

    public void setInfoLabel(NSTextField infoLabel) {
        this.infoLabel = infoLabel;
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
                            if(this.browserOutlineView.isItemExpanded(p)) {
                                p.invalidate();
                            }
                        }
                    }
                    break;
                }
            }
            this.reloadData(true);
        }
    }

    /**
     *
     * @param path
     * @param renamed
     */
    protected void renamePath(final Path path, final Path renamed) {
        if(renamed.exists()) {
            NSWindow sheet = NSAlertPanel.criticalAlertPanel(NSBundle.localizedString("Overwrite", "Alert sheet title"), //title
                    NSBundle.localizedString("A file with the same name already exists. Do you want to replace the existing file?", ""),
                    NSBundle.localizedString("Overwrite", "Alert sheet default button"), // defaultbutton
                    NSBundle.localizedString("Cancel", "Alert sheet alternate button"), //alternative button
                    null //other button
            );
            CDSheetController c = new CDSheetController(this, sheet) {
                public void callback(int returncode) {
                    if(returncode == DEFAULT_OPTION) {
                        path.rename(renamed.getAbsolute());
                    }
                }
            };
            c.beginSheet(true);
        }
        else {
            path.rename(renamed.getAbsolute());
        }
    }

    public void editButtonContextMenuClicked(final Object sender) {
        this.editButtonClicked(sender);
    }

    public void editButtonClicked(final Object sender) {
        for(Iterator i = this.getSelectedPaths().iterator(); i.hasNext();) {
            Path selected = (Path) i.next();
            if(this.isEditable(selected)) {
                Editor editor = null;
                if(sender instanceof NSMenuItem) {
                    Object identifier = Editor.SUPPORTED_EDITORS.get(((NSMenuItem) sender).title());
                    if(identifier != null) {
                        editor = new Editor((String) identifier);
                    }
                }
                if(null == editor) {
                    editor = new Editor(Preferences.instance().getProperty("editor.bundleIdentifier"));
                }
                editor.open(selected);
            }
        }
    }

    /**
     * @param selected
     * @return True if the selected path is editable (not a directory and no known binary file)
     */
    private boolean isEditable(Path selected) {
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

    public void sendCustomCommandClicked(final Object sender) {
        CDSheetController controller = new CDCommandController(this, this.session);
        controller.beginSheet(false);
    }


    public void createFolderButtonClicked(final Object sender) {
        CDSheetController controller = new CDFolderController(this);
        controller.beginSheet(false);
    }

    private CDInfoController inspector = null;

    public void infoButtonClicked(final Object sender) {
        if(this.getSelectionCount() > 0) {
            List files = this.getSelectedPaths();
            if(Preferences.instance().getBoolean("browser.info.isInspector")) {
                if(null == this.inspector) {
                    this.inspector = new CDInfoController(this);
                }
                this.inspector.setFiles(files);
                this.inspector.window().makeKeyAndOrderFront(null);
            }
            else {
                CDInfoController c = new CDInfoController(this);
                c.setFiles(files);
                c.window().makeKeyAndOrderFront(null);
            }
        }
    }

    public void deleteFileButtonClicked(final Object sender) {
        final List files = new ArrayList();
        StringBuffer alertText = new StringBuffer(NSBundle.localizedString("Really delete the following files? This cannot be undone.", "Confirm deleting files."));
        if(sender instanceof Path) {
            Path p = (Path) sender;
            files.add(p);
            alertText.append("\n- " + p.getName());
        }
        else if(this.getSelectionCount() > 0) {
            int i = 0;
            Iterator iter;
            for(iter = this.getSelectedPaths().iterator(); i < 10 && iter.hasNext();) {
                Path p = (Path) iter.next();
                files.add(p);
                alertText.append("\n- " + p.getName());
                i++;
            }
            if(iter.hasNext()) {
                alertText.append("\n- (...)");
                while(iter.hasNext()) {
                    files.add(iter.next());
                }
            }
        }
        if(files.size() > 0) {
            NSWindow sheet = NSAlertPanel.criticalAlertPanel(NSBundle.localizedString("Delete", "Alert sheet title"), //title
                    alertText.toString(),
                    NSBundle.localizedString("Delete", "Alert sheet default button"), // defaultbutton
                    NSBundle.localizedString("Cancel", "Alert sheet alternate button"), //alternative button
                    null //other button
            );
            CDSheetController c = new CDSheetController(this, sheet) {
                public void callback(int returncode) {
                    if(returncode == DEFAULT_OPTION) {
                        if(files.size() > 0) {
                            Iterator i = files.iterator();
                            Path p;
                            while(i.hasNext()) {
                                p = (Path) i.next();
                                p.delete();
                            }
                            reloadData(true);
                        }
                    }
                }
            };
            c.beginSheet(false);
        }
    }

    public void downloadAsButtonClicked(final Object sender) {
        Session session = (Session) this.session.clone();
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
                    new NSSelector("saveAsPanelDidEnd", new Class[]{NSOpenPanel.class, int.class, Object.class}),
                    path);
        }
    }

    public void saveAsPanelDidEnd(NSSavePanel sheet, int returncode, Object contextInfo) {
        if(returncode == CDSheetCallback.DEFAULT_OPTION) {
            String filename;
            if((filename = sheet.filename()) != null) {
                Path path = (Path) contextInfo;
                path.setLocal(new Local(filename));
                Queue q = new DownloadQueue();
                q.addRoot(path);
                CDQueueController.instance().startItem(q);
            }
        }
    }

    public void syncButtonClicked(final Object sender) {
        Path selection;
        if(this.getSelectionCount() == 1 &&
                this.getSelectedPath().attributes.isDirectory()) {
            selection = (Path) this.getSelectedPath().clone((Session) this.session.clone());
        }
        else {
            selection = (Path) this.workdir().clone((Session) this.session.clone());
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
            Path selection = (Path) contextInfo;
            if(sheet.filenames().count() > 0) {
                selection.setLocal(new Local((String) sheet.filenames().lastObject()));
                final Queue q = new SyncQueue();
                q.addListener(new QueueListener() {
                    public void queueStarted() {
                    }

                    public void queueStopped() {
                        if(isMounted()) {
                            getSession().cache().invalidate(q.getRoot().getParent());
                            reloadData(true);
                        }
                        q.removeListener(this);
                    }
                });
                q.addRoot(selection);
                CDQueueController.instance().startItem(q);
            }
        }
    }

    public void downloadButtonClicked(final Object sender) {
        Queue q = new DownloadQueue();
        Session session = (Session) this.session.clone();
        for(Iterator i = this.getSelectedPaths().iterator(); i.hasNext();) {
            Path path = (Path) ((Path) i.next()).clone(session);
            q.addRoot(path);
        }
        CDQueueController.instance().startItem(q);
    }

    private String lastSelectedUploadDirectory = null;

    public void uploadButtonClicked(final Object sender) {
        NSOpenPanel panel = NSOpenPanel.openPanel();
        panel.setCanChooseDirectories(true);
        panel.setCanCreateDirectories(false);
        panel.setCanChooseFiles(true);
        panel.setAllowsMultipleSelection(true);
        panel.setPrompt(NSBundle.localizedString("Upload", ""));
        panel.setTitle(NSBundle.localizedString("Upload", ""));
        panel.beginSheetForDirectory(
                this.lastSelectedUploadDirectory, //trying to be smart
                null,
                null,
                this.window,
                this,
                new NSSelector("uploadPanelDidEnd", new Class[]{NSOpenPanel.class, int.class, Object.class}),
                null);
    }

    public void uploadPanelDidEnd(NSOpenPanel sheet, int returncode, Object contextInfo) {
        if(returncode == CDSheetCallback.DEFAULT_OPTION) {
            Path workdir = this.workdir();
            // selected files on the local filesystem
            NSArray selected = sheet.filenames();
            java.util.Enumeration iterator = selected.objectEnumerator();
            final Queue q = new UploadQueue();
            q.addListener(new QueueListener() {
                public void queueStarted() {
                }

                public void queueStopped() {
                    if(isMounted()) {
                        getSession().cache().invalidate(q.getRoot().getParent());
                        reloadData(true);
                    }
                    q.removeListener(this);
                }
            });
            Session session = (Session) this.getSession().clone();
            while(iterator.hasMoreElements()) {
                q.addRoot(PathFactory.createPath(session,
                        workdir.getAbsolute(),
                        new Local((String) iterator.nextElement())));
            }
            this.lastSelectedUploadDirectory = q.getRoot().getLocal().getParentFile().getAbsolutePath();
            CDQueueController.instance().startItem(q);
        }
    }

    public void insideButtonClicked(final Object sender) {
        if(this.getSelectionCount() > 0) {
            Path selected = this.getSelectedPath(); //last row selected
            if(selected.attributes.isDirectory()) {
                this.setWorkdir(selected);
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

    public void disconnectButtonClicked(final Object sender) {
        this.unmount();
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
        NSPasteboard pboard = NSPasteboard.pasteboardWithName("QueuePBoard");
        if(pboard.availableTypeFromArray(new NSArray("QueuePBoardType")) != null) {
            Object o = pboard.propertyListForType("QueuePBoardType");// get the data from paste board
            if(o != null) {
                NSArray elements = (NSArray) o;
                for(int i = 0; i < elements.count(); i++) {
                    NSDictionary dict = (NSDictionary) elements.objectAtIndex(i);
                    Queue q = QueueFactory.createQueue(dict);
                    List selected = new ArrayList();
                    for(Iterator iter = q.getRoots().iterator(); iter.hasNext();) {
                        Path current = PathFactory.createPath(this.getSession(),
                                ((Path) iter.next()).getAbsolute());
                        Path renamed = PathFactory.createPath(this.getSession(),
                                workdir().getAbsolute(), current.getName());
                        this.renamePath(current, renamed);
                        selected.add(renamed);
                    }
                    this.workdir().invalidate();
                    this.reloadData(false);
                    this.setSelectedPaths(selected);

                }
                pboard.setPropertyListForType(null, "QueuePBoardType");
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
                Session session = (Session) this.getSession().clone();
                for(int i = 0; i < elements.count(); i++) {
                    Path p = PathFactory.createPath(session,
                            workdir().getAbsolute(),
                            new Local((String) elements.objectAtIndex(i)));
                    q.addRoot(p);
                }
                if(q.numberOfRoots() > 0) {
                    CDQueueController.instance().startItem(q);
                    q.addListener(new QueueListener() {
                        public void queueStarted() {
                        }

                        public void queueStopped() {
                            if(isMounted()) {
                                getSession().cache().invalidate(q.getRoot().getParent());
                                reloadData(true);
                            }
                            q.removeListener(this);
                        }
                    });
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
    protected void setWorkdir(Path path) {
        log.debug("setWorkdir:"+path);
        if(!this.hasSession()) {
            // The connection has already been closed asynchronously
            log.warn("Delayed notification to set current working directory - session already closed asynchronously");
            return;
        }
        this.setFileFilter(null); // Remove any custom file filter
        if(null == path) {
            // Clear the browser view if no working directory is given
            this.workdir = null;
            this.pathPopupItems.clear();
            this.pathPopupButton.removeAllItems();
            this.reloadData(false);
            return;
        }
        if(null == path.list()) {
            // Invalid path given; don't update browser view
            return;
        }
//            if(!this.hasSession() || !this.session.isConnected()) {
//                // The connection has already been closed asynchronous
//                log.warn("Got new directory listing but session already closed asynchronously");
//                return;
//            }
        this.workdir = path;
        this.session.addPathToHistory(this.workdir);
        this.pathPopupItems.clear();
        this.pathPopupButton.removeAllItems();
        this.addPathToPopup(this.workdir);
        for(Path p = this.workdir; !p.isRoot();) {
            p = p.getParent();
            this.addPathToPopup(p);
        }
        this.reloadData(false);
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
        this.bookmarkModel.exportBookmark(host, this.getRepresentedFile());
        if(this.getRepresentedFile().exists()) {
            // Set the window title
            this.window.setRepresentedFilename(this.getRepresentedFile().getAbsolutePath());
        }
        session.addConnectionListener(listener = new ConnectionListener() {
            /**
             * The listener used to watch for messages and errors during the session
             */
            private ProgressListener progress;
            /**
             * Only used for connection errors upon the initial handshake
             */
            private ProgressListener error;
            /**
             * Writes the transcript to the log viewer
             */
            private TranscriptListener transcript;

            public void connectionWillOpen() {
                session.addProgressListener(error = new ProgressListener() {
                    public void message(final String message) {
                        ;
                    }

                    public void error(final Exception e) {
                        if(e instanceof IOException) {
                            if(hasSession()) {
                                final File bookmark = getRepresentedFile();
                                if(bookmark.exists()) {
                                    // Delete this history bookmark if there was any error connecting
                                    bookmark.delete();
                                }
                                window.setRepresentedFilename(""); //can't send null
                            }
                        }
                        session.removeProgressListener(this);
                    }
                });
                session.addProgressListener(progress = new ProgressListener() {
                    public void message(final String msg) {
                        if(!Thread.currentThread().getName().equals("main") && !Thread.currentThread().getName().equals("AWT-AppKit"))
                        {
                            invoke(new Runnable() {
                                public void run() {
                                    //Run in main thread
                                    message(msg);
                                }
                            });
                            return;
                        }
                        // Update the status label at the bottom of the browser window
                        statusLabel.setAttributedStringValue(new NSAttributedString(msg,
                                TRUNCATE_MIDDLE_PARAGRAPH_DICTIONARY));
                        statusLabel.display();
                    }


                    public void error(final Exception e) {
                        if(!Thread.currentThread().getName().equals("main") && !Thread.currentThread().getName().equals("AWT-AppKit"))
                        {
                            invoke(new Runnable() {
                                public void run() {
                                    //Run in main thread
                                    error(e);
                                }
                            });
                            return;
                        }
                        String alert = e.getMessage();
                        String title = NSBundle.localizedString("Error", "");
                        boolean diagnostics = false;
                        if(e instanceof FTPException) {
                            title = "FTP " + NSBundle.localizedString("Error", "");
                        }
                        else if(e instanceof SshException) {
                            title = "SSH " + NSBundle.localizedString("Error", "");
                        }
                        else if(e instanceof SocketException) {
                            title = "Network " + NSBundle.localizedString("Error", "");
                            diagnostics = true;
                        }
                        else if(e instanceof IOException) {
                            title = "I/O " + NSBundle.localizedString("Error", "");
                            diagnostics = true;
                        }
                        alert(NSAlertPanel.criticalAlertPanel(title, //title
                                alert, // alert text
                                NSBundle.localizedString("OK", "Alert default button"), // defaultbutton
                                diagnostics ? null : NSBundle.localizedString("Disconnect", ""), //alternative button
                                diagnostics ? NSBundle.localizedString("Open Network Diagnostics",
                                        "Run interactive network diagnostics") : null), //other button
                                new CDSheetCallback() {
                                    public void callback(int returncode) {
                                        if(returncode == ALTERNATE_OPTION) {
                                            host.diagnose();
                                        }
                                        if(returncode == CANCEL_OPTION) {
                                            session.interrupt();
                                        }
                                    }
                                }, true);
                    }
                });
                session.addTranscriptListener(transcript = new TranscriptListener() {
                    public void log(String message) {
                        logView.textStorage().appendAttributedString(
                                new NSAttributedString(message + "\n", FIXED_WITH_FONT_ATTRIBUTES));
                    }
                });
                window.toolbar().validateVisibleItems();
            }

            public void connectionDidOpen() {
                if(!Thread.currentThread().getName().equals("main") && !Thread.currentThread().getName().equals("AWT-AppKit"))
                {
                    invoke(new Runnable() {
                        public void run() {
                            //Run in main thread
                            connectionDidOpen();
                        }
                    });
                    return;
                }
                getSelectedBrowserView().setNeedsDisplay(true);
                window.setTitle(host.getProtocol() + ":" + host.getCredentials().getUsername()
                        + "@" + host.getHostname());
                if(Preferences.instance().getBoolean("browser.confirmDisconnect")) {
                    window.setDocumentEdited(true);
                }
                window.toolbar().validateVisibleItems();
                // This progress listener was only used to handle initial connection errors
                session.removeProgressListener(error);
            }

            public void connectionWillClose() {
                ;
            }

            public void connectionDidClose() {
                if(!Thread.currentThread().getName().equals("main") && !Thread.currentThread().getName().equals("AWT-AppKit"))
                {
                    invoke(new Runnable() {
                        public void run() {
                            //Run in main thread
                            connectionDidClose();
                        }
                    });
                    return;
                }
                getSelectedBrowserView().setNeedsDisplay(true);
                window.setDocumentEdited(false);
                window.toolbar().validateVisibleItems();
                session.removeProgressListener(progress);
                session.removeTranscriptListener(transcript);
                progressIndicator.stopAnimation(this);
            }

            public void activityStarted() {
                if(!Thread.currentThread().getName().equals("main") && !Thread.currentThread().getName().equals("AWT-AppKit"))
                {
                    invoke(new Runnable() {
                        public void run() {
                            //Run in main thread
                            activityStarted();
                        }
                    });
                    return;
                }
                activityRunning = true;
                progressIndicator.startAnimation(this);
            }

            public void activityStopped() {
                if(!Thread.currentThread().getName().equals("main") && !Thread.currentThread().getName().equals("AWT-AppKit"))
                {
                    invoke(new Runnable() {
                        public void run() {
                            //Run in main thread
                            activityStopped();
                        }
                    });
                    return;
                }
                activityRunning = false;
                progressIndicator.stopAnimation(this);
                statusLabel.setAttributedStringValue(new NSAttributedString(NSBundle.localizedString("Idle", "Status", ""),
                        TRUNCATE_MIDDLE_PARAGRAPH_DICTIONARY));
                statusLabel.display();
            }
        });
        this.getFocus();
        return session;
    }

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
     * A mountingLock object used to make sure only one session can use this browser at once.
     */
    private final Object mountingLock = new Object();

    /**
     * @param host
     * @return The session to be used for any further operations
     */
    protected Session mount(final Host host) {
        synchronized(mountingLock) {
            log.debug("mount:" + host);
            if(this.isMounted()) {
                if(this.session.getHost().getURL().equals(host.getURL())) {
                    if(host.hasReasonableDefaultPath()) {
                        Path home = PathFactory.createPath(session, host.getDefaultPath());
                        home.attributes.setType(Path.DIRECTORY_TYPE);
                        home.invalidate();
                        this.setWorkdir(home);
                        return session;
                    }
                }
            }
            if(this.unmount(new CDSheetCallback() {
                public void callback(int returncode) {
                    if(returncode == DEFAULT_OPTION) {
                        unmount();
                        mount(host);
                    }
                }
            })) {
                final Session session = this.init(host);
                new Thread(session.toString()) {
                    public void run() {
                        // Synchronize so that no other session can be mounted
                        // if we haven't finished here yet.
                        synchronized(mountingLock) {
                            // Mount this session and set the working directory
                            setWorkdir(session.mount());
                        }
                    }
                }.start();
                return session;
            }
            return null;
        }
    }

    /**
     * Will close the session but still display the current working directory without any confirmation
     * from the user
     */
    public void unmount() {
        // This is not synchronized to the <code>mountingLock</code> intentionally; this allows to unmount
        // sessions not yet connected
        if(this.hasSession()) {
            if(this.activityRunning) {
                //Interrupt any operation in progress; just closes the socket without any goodbye message
                this.session.interrupt();
            }
            else {
                //Close the connection gracefully
                this.session.close();
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
                this.alert(NSAlertPanel.criticalAlertPanel(NSBundle.localizedString("Disconnect from", "Alert sheet title") + " " + this.session.getHost().getHostname(), //title
                        NSBundle.localizedString("The connection will be closed.", "Alert sheet text"), // message
                        NSBundle.localizedString("Disconnect", "Alert sheet default button"), // defaultbutton
                        NSBundle.localizedString("Cancel", "Alert sheet alternate button"), // alternate button
                        null //other button
                ), callback);
                return false;
            }
            this.unmount();
        }
        return true;
    }

    /**
     *
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

        }
    }

    /**
     *
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
                    unmount();
                    sender.close();
                }
            }
        });
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
                icon.setScalesWhenResized(true);
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
                    icon.setScalesWhenResized(true);
                    icon.setSize(new NSSize(16f, 16f));
                    item.setImage(icon);
                }
            }
        }
        if(identifier.equals("showHiddenFilesClicked:")) {
            item.setState((this.getFileFilter() instanceof NullFilter) ? NSCell.OnState : NSCell.OffState);
        }
        if(identifier.equals("encodingButtonClicked:")) {
            if(this.isMounted()) {
                item.setState(this.session.getHost().getEncoding().equalsIgnoreCase(item.title()) ? NSCell.OnState : NSCell.OffState);
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
        if(identifier.equals("New Connection")) {
            return true;
        }
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
        if(identifier.equals("showHiddenFilesClicked:")) {
            return true;
        }
        if(identifier.equals("encodingButtonClicked:")) {
            return true;
        }
        if(identifier.equals("addBookmarkButtonClicked:")) {
            return true;
        }
        if(identifier.equals("deleteBookmarkButtonClicked:")) {
            return bookmarkTable.selectedRow() != -1;
        }
        if(identifier.equals("editBookmarkButtonClicked:")) {
            return bookmarkTable.numberOfSelectedRows() == 1;
        }
        if(identifier.equals("Edit") || identifier.equals("editButtonClicked:")) {
            if(this.isMounted() && this.getSelectionCount() > 0) {
                String editorPath = NSWorkspace.sharedWorkspace().absolutePathForAppBundleWithIdentifier(
                        Preferences.instance().getProperty("editor.bundleIdentifier"));
                if(editorPath != null) {
                    return this.isEditable(this.getSelectedPath());
                }
            }
            return false;
        }
        if(identifier.equals("editButtonContextMenuClicked:")) {
            if(this.isMounted() && this.getSelectionCount() > 0) {
                return this.isEditable(this.getSelectedPath());
            }
            return false;
        }
        if(identifier.equals("sendCustomCommandClicked:")) {
            return (this.session instanceof ch.cyberduck.core.ftp.FTPSession) && this.isConnected();
        }
        if(identifier.equals("gotoButtonClicked:")) {
            return this.isMounted();
        }
        if(identifier.equals("Get Info") || identifier.equals("infoButtonClicked:")) {
            return this.isMounted() && this.getSelectionCount() > 0;
        }
        if(identifier.equals("New Folder") || identifier.equals("createFolderButtonClicked:")) {
            return this.isMounted();
        }
        if(identifier.equals("New File") || identifier.equals("createFileButtonClicked:")) {
            return this.isMounted();
        }
        if(identifier.equals("Duplicate File") || identifier.equals("duplicateFileButtonClicked:")) {
            return this.isMounted() && this.getSelectionCount() > 0 && this.getSelectedPath().attributes.isFile();
        }
        if(identifier.equals("Delete") || identifier.equals("deleteFileButtonClicked:")) {
            return this.isMounted() && this.getSelectionCount() > 0;
        }
        if(identifier.equals("Refresh") || identifier.equals("reloadButtonClicked:")) {
            return this.isMounted();
        }
        if(identifier.equals("Download") || identifier.equals("downloadButtonClicked:")) {
            return this.isMounted() && this.getSelectionCount() > 0;
        }
        if(identifier.equals("Upload") || identifier.equals("uploadButtonClicked:")) {
            return this.isMounted();
        }
        if(identifier.equals("Synchronize") || identifier.equals("syncButtonClicked:")) {
            return this.isMounted();
        }
        if(identifier.equals("Download As") || identifier.equals("downloadAsButtonClicked:")) {
            return this.isMounted() && this.getSelectionCount() == 1;
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
        if(identifier.equals("Disconnect") || identifier.equals("disconnectButtonClicked:")) {
            if(!this.isConnected()) {
                return this.activityRunning;
            }
            return this.isConnected();
        }
        if(identifier.equals("printDocument:")) {
            return this.isMounted();
        }
        return true; // by default everything is enabled
    }

    // ----------------------------------------------------------
    // Toolbar Delegate
    // ----------------------------------------------------------

    public boolean validateToolbarItem(NSToolbarItem item) {
        this.navigationButton.setEnabled(this.isMounted() && session.getBackHistory().length > 1,
                NAVIGATION_LEFT_SEGMENT_BUTTON);
        this.navigationButton.setEnabled(this.isMounted() && session.getForwardHistory().length > 0,
                NAVIGATION_RIGHT_SEGMENT_BUTTON);
        this.upButton.setEnabled(this.isMounted() && !this.workdir().isRoot(),
                NAVIGATION_UP_SEGMENT_BUTTON);

        this.pathPopupButton.setEnabled(this.isMounted());
        this.searchField.setEnabled(this.isMounted());

        String identifier = item.itemIdentifier();
        if(identifier.equals("Edit")) {
            String editorPath = NSWorkspace.sharedWorkspace().absolutePathForAppBundleWithIdentifier(
                    Preferences.instance().getProperty("editor.bundleIdentifier"));
            if(editorPath != null) {
                item.setImage(NSWorkspace.sharedWorkspace().iconForFile(editorPath));

            }
            else {
                item.setImage(NSImage.imageNamed("pencil.tiff"));
            }
        }
        return this.validateItem(identifier);
    }

    public NSToolbarItem toolbarItemForItemIdentifier(NSToolbar toolbar, String itemIdentifier, boolean flag) {
        NSToolbarItem item = new NSToolbarItem(itemIdentifier);
        if(itemIdentifier.equals("Browser View")) {
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
//			viewSubmenu.addItem(new NSMenuItem(NSBundle.localizedString("Column", "Toolbar item"),
//											   new NSSelector("browserSwitchClicked", new Class[]{Object.class}),
//											   ""));
//			viewSubmenu.itemWithTitle(NSBundle.localizedString("Column", "Toolbar item")).setTag(2);
            viewMenu.setSubmenu(viewSubmenu);
            item.setMenuFormRepresentation(viewMenu);
            item.setMinSize(this.browserSwitchView.frame().size());
            item.setMaxSize(this.browserSwitchView.frame().size());
            return item;
        }
        if(itemIdentifier.equals("New Connection")) {
            item.setLabel(NSBundle.localizedString("New Connection", "Toolbar item"));
            item.setPaletteLabel(NSBundle.localizedString("New Connection", "Toolbar item"));
            item.setToolTip(NSBundle.localizedString("Connect to server", "Toolbar item tooltip"));
            item.setImage(NSImage.imageNamed("connect.tiff"));
            item.setTarget(this);
            item.setAction(new NSSelector("connectButtonClicked", new Class[]{Object.class}));
            return item;
        }
        if(itemIdentifier.equals("Bookmarks")) {
            item.setLabel(NSBundle.localizedString("Bookmarks", "Toolbar item"));
            item.setPaletteLabel(NSBundle.localizedString("Bookmarks", "Toolbar item"));
            item.setToolTip(NSBundle.localizedString("Toggle Bookmarks", "Toolbar item tooltip"));
            item.setImage(NSImage.imageNamed("drawer.tiff"));
            item.setTarget(this);
            item.setAction(new NSSelector("toggleBookmarkDrawer", new Class[]{Object.class}));
            return item;
        }
        if(itemIdentifier.equals("Transfers")) {
            item.setLabel(NSBundle.localizedString("Transfers", "Toolbar item"));
            item.setPaletteLabel(NSBundle.localizedString("Transfers", "Toolbar item"));
            item.setToolTip(NSBundle.localizedString("Show Transfers window", "Toolbar item tooltip"));
            item.setImage(NSImage.imageNamed("queue.tiff"));
            item.setTarget(this);
            item.setAction(new NSSelector("showTransferQueueClicked", new Class[]{Object.class}));
            return item;
        }
        if(itemIdentifier.equals("Tools")) {
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
        if(itemIdentifier.equals("Quick Connect")) {
            item.setLabel(NSBundle.localizedString("Quick Connect", "Toolbar item"));
            item.setPaletteLabel(NSBundle.localizedString("Quick Connect", "Toolbar item"));
            item.setToolTip(NSBundle.localizedString("Connect to server", "Toolbar item tooltip"));
            item.setView(this.quickConnectPopup);
            item.setMinSize(this.quickConnectPopup.frame().size());
            item.setMaxSize(this.quickConnectPopup.frame().size());
            return item;
        }
        if(itemIdentifier.equals("Encoding")) {
            item.setLabel(NSBundle.localizedString("Encoding", "Toolbar item"));
            item.setPaletteLabel(NSBundle.localizedString("Encoding", "Toolbar item"));
            item.setToolTip(NSBundle.localizedString("Character Encoding", "Toolbar item tooltip"));
            item.setView(this.encodingPopup);
            NSMenuItem encodingMenu = new NSMenuItem(NSBundle.localizedString("Encoding", "Toolbar item"),
                    new NSSelector("encodingButtonClicked", new Class[]{Object.class}),
                    "");
            java.util.SortedMap charsets = java.nio.charset.Charset.availableCharsets();
            java.util.Iterator iter = charsets.values().iterator();
            NSMenu charsetMenu = new NSMenu();
            while(iter.hasNext()) {
                charsetMenu.addItem(new NSMenuItem(((java.nio.charset.Charset) iter.next()).name(),
                        new NSSelector("encodingButtonClicked", new Class[]{Object.class}),
                        ""));
            }
            encodingMenu.setSubmenu(charsetMenu);
            item.setMenuFormRepresentation(encodingMenu);
            item.setMinSize(this.encodingPopup.frame().size());
            item.setMaxSize(this.encodingPopup.frame().size());
            return item;
        }
        if(itemIdentifier.equals("Refresh")) {
            item.setLabel(NSBundle.localizedString("Refresh", "Toolbar item"));
            item.setPaletteLabel(NSBundle.localizedString("Refresh", "Toolbar item"));
            item.setToolTip(NSBundle.localizedString("Refresh directory listing", "Toolbar item tooltip"));
            item.setImage(NSImage.imageNamed("reload.tiff"));
            item.setTarget(this);
            item.setAction(new NSSelector("reloadButtonClicked", new Class[]{Object.class}));
            return item;
        }
        if(itemIdentifier.equals("Download")) {
            item.setLabel(NSBundle.localizedString("Download", "Toolbar item"));
            item.setPaletteLabel(NSBundle.localizedString("Download", "Toolbar item"));
            item.setToolTip(NSBundle.localizedString("Download file", "Toolbar item tooltip"));
            item.setImage(NSImage.imageNamed("downloadFile.tiff"));
            item.setTarget(this);
            item.setAction(new NSSelector("downloadButtonClicked", new Class[]{Object.class}));
            return item;
        }
        if(itemIdentifier.equals("Upload")) {
            item.setLabel(NSBundle.localizedString("Upload", "Toolbar item"));
            item.setPaletteLabel(NSBundle.localizedString("Upload", "Toolbar item"));
            item.setToolTip(NSBundle.localizedString("Upload local file to the remote host", "Toolbar item tooltip"));
            item.setImage(NSImage.imageNamed("uploadFile.tiff"));
            item.setTarget(this);
            item.setAction(new NSSelector("uploadButtonClicked", new Class[]{Object.class}));
            return item;
        }
        if(itemIdentifier.equals("Synchronize")) {
            item.setLabel(NSBundle.localizedString("Synchronize", "Toolbar item"));
            item.setPaletteLabel(NSBundle.localizedString("Synchronize", "Toolbar item"));
            item.setToolTip(NSBundle.localizedString("Synchronize files", "Toolbar item tooltip"));
            item.setImage(NSImage.imageNamed("sync32.tiff"));
            item.setTarget(this);
            item.setAction(new NSSelector("syncButtonClicked", new Class[]{Object.class}));
            return item;
        }
        if(itemIdentifier.equals("Get Info")) {
            item.setLabel(NSBundle.localizedString("Get Info", "Toolbar item"));
            item.setPaletteLabel(NSBundle.localizedString("Get Info", "Toolbar item"));
            item.setToolTip(NSBundle.localizedString("Show file attributes", "Toolbar item tooltip"));
            item.setImage(NSImage.imageNamed("info.tiff"));
            item.setTarget(this);
            item.setAction(new NSSelector("infoButtonClicked", new Class[]{Object.class}));
            return item;
        }
        if(itemIdentifier.equals("Edit")) {
            item.setLabel(NSBundle.localizedString("Edit", "Toolbar item"));
            item.setPaletteLabel(NSBundle.localizedString("Edit", "Toolbar item"));
            item.setToolTip(NSBundle.localizedString("Edit file in external editor", "Toolbar item tooltip"));
            item.setImage(NSImage.imageNamed("pencil.tiff"));
            String editorPath = NSWorkspace.sharedWorkspace().absolutePathForAppBundleWithIdentifier(
                    Preferences.instance().getProperty("editor.bundleIdentifier"));
            if(editorPath != null) {
                item.setImage(NSWorkspace.sharedWorkspace().iconForFile(editorPath));
            }
            item.setTarget(this);
            item.setAction(new NSSelector("editButtonClicked", new Class[]{Object.class}));
            NSMenuItem toolbarMenu = new NSMenuItem(NSBundle.localizedString("Edit", "Toolbar item"),
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
        if(itemIdentifier.equals("Delete")) {
            item.setLabel(NSBundle.localizedString("Delete", "Toolbar item"));
            item.setPaletteLabel(NSBundle.localizedString("Delete", "Toolbar item"));
            item.setToolTip(NSBundle.localizedString("Delete file", "Toolbar item tooltip"));
            item.setImage(NSImage.imageNamed("deleteFile.tiff"));
            item.setTarget(this);
            item.setAction(new NSSelector("deleteFileButtonClicked", new Class[]{Object.class}));
            return item;
        }
        if(itemIdentifier.equals("New Folder")) {
            item.setLabel(NSBundle.localizedString("New Folder", "Toolbar item"));
            item.setPaletteLabel(NSBundle.localizedString("New Folder", "Toolbar item"));
            item.setToolTip(NSBundle.localizedString("Create New Folder", "Toolbar item tooltip"));
            item.setImage(NSImage.imageNamed("folder_new.tiff"));
            item.setTarget(this);
            item.setAction(new NSSelector("createFolderButtonClicked", new Class[]{Object.class}));
            return item;
        }
        if(itemIdentifier.equals("Disconnect")) {
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

    /**
     * @param toolbar
     * @return The default configuration of toolbar items
     */
    public NSArray toolbarDefaultItemIdentifiers(NSToolbar toolbar) {
        return new NSArray(new Object[]{
                "New Connection",
                NSToolbarItem.SeparatorItemIdentifier,
                "Bookmarks",
                "Quick Connect",
                "Tools",
                NSToolbarItem.SeparatorItemIdentifier,
                "Refresh",
                "Edit",
                NSToolbarItem.FlexibleSpaceItemIdentifier,
                "Disconnect"
        });
    }

    /**
     * @param toolbar
     * @return All available toolbar items
     */
    public NSArray toolbarAllowedItemIdentifiers(NSToolbar toolbar) {
        return new NSArray(new Object[]{
                "New Connection",
                "Browser View",
                "Bookmarks",
                "Transfers",
                "Quick Connect",
                "Tools",
                "Refresh",
                "Encoding",
                "Synchronize",
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

    /**
     * Overrriden to remove any listeners from the session
     */
    protected void invalidate() {
        if(this.hasSession()) {
            this.session.removeConnectionListener(this.listener);
            this.session.getHost().getCredentials().setPassword(null);
            this.session.cache().clear();
            this.session = null;
        }
        this.toolbar.setDelegate(null);

        this.bookmarkDrawer.setContentView(null);

        this.bookmarkTable.setDataSource(null);
        this.bookmarkModel.removeListener(this.bookmarkCollectionListener);
        this.bookmarkModel = null;
        this.bookmarkTable.setDelegate(null);
        this.bookmarkTableDelegate = null;
        this.bookmarkTable = null;

        this.browserListModel.invalidate();
        this.browserListView.setDataSource(null);
        this.browserListModel = null;
        this.browserListView.setDelegate(null);
        this.browserListViewDelegate = null;
        this.browserListView = null;

        this.browserOutlineModel.invalidate();
        this.browserOutlineView.setDataSource(null);
        this.browserOutlineModel = null;
        this.browserOutlineView.setDelegate(null);
        this.browserOutlineViewDelegate = null;
        this.browserOutlineView = null;

        this.editMenu.setDelegate(null);
        this.editMenuDelegate = null;
        this.editMenu = null;

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
        this.quickConnectPopup.setTarget(null);
        this.quickConnectPopup = null;

        super.invalidate();
    }
}