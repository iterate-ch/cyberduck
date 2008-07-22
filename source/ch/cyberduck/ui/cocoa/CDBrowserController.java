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

import com.apple.cocoa.application.*;
import com.apple.cocoa.foundation.*;

import ch.cyberduck.core.*;
import ch.cyberduck.core.Collection;
import ch.cyberduck.core.io.BandwidthThrottle;
import ch.cyberduck.core.sftp.SFTPSession;
import ch.cyberduck.core.ssl.SSLSession;
import ch.cyberduck.core.util.URLSchemeHandlerConfiguration;
import ch.cyberduck.ui.cocoa.delegate.EditMenuDelegate;
import ch.cyberduck.ui.cocoa.growl.Growl;
import ch.cyberduck.ui.cocoa.odb.Editor;
import ch.cyberduck.ui.cocoa.odb.EditorFactory;
import ch.cyberduck.ui.cocoa.quicklook.QuickLook;
import ch.cyberduck.ui.cocoa.threading.BackgroundAction;
import ch.cyberduck.ui.cocoa.threading.BackgroundActionRegistry;
import ch.cyberduck.ui.cocoa.threading.WindowMainAction;

import org.apache.log4j.Logger;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.security.cert.X509Certificate;
import java.text.MessageFormat;
import java.util.*;

import com.enterprisedt.net.ftp.FTPConnectMode;

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

    /**
     * Applescriptability
     *
     * @return
     */
    public String getWorkingDirectory() {
        if(this.isMounted()) {
            return this.workdir().getAbsolute();
        }
        return null;
    }

    /**
     * Applescriptability
     *
     * @param command
     * @return
     */
    public Object handleMountScriptCommand(NSScriptCommand command) {
        log.debug("handleMountScriptCommand:" + command);
        NSDictionary args = command.evaluatedArguments();
        Object portObj = args.objectForKey("Port");
        Host host;
        Object bookmarkObj = args.objectForKey("Bookmark");
        if(bookmarkObj != null) {
            int index = HostCollection.defaultCollection().indexOf(bookmarkObj);
            if(index < 0) {
                return null;
            }
            host = (Host) HostCollection.defaultCollection().get(index);
        }
        else {
            if(portObj != null) {
                Object protocolObj = args.objectForKey("Protocol");
                if(protocolObj != null) {
                    host = new Host(
                            Protocol.forScheme((String) args.objectForKey("Protocol")),
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
                    host = new Host(
                            Protocol.forName((String) args.objectForKey("Protocol")),
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
                host.setCredentials(new Credentials(
                        (String) args.objectForKey("Username"), (String) args.objectForKey("Password"))
                );
            }
            Object modeObj = args.objectForKey("Mode");
            if(modeObj != null) {
                if(modeObj.equals(FTPConnectMode.ACTIVE.toString())) {
                    host.setFTPConnectMode(FTPConnectMode.ACTIVE);
                }
                if(modeObj.equals(FTPConnectMode.PASV.toString())) {
                    host.setFTPConnectMode(FTPConnectMode.PASV);
                }
            }
        }
        this.setWorkdir(this.init(host).mount());
        BackgroundActionRegistry.instance().block();
        return null;
    }

    /**
     * Applescriptability
     *
     * @param command
     * @return
     */
    public Object handleCloseScriptCommand(NSScriptCommand command) {
        log.debug("handleCloseScriptCommand:" + command);
        this.unmount(true);
        BackgroundActionRegistry.instance().block();
        this.window().close();
        return null;
    }

    /**
     * Applescriptability
     *
     * @param command
     * @return
     */
    public Object handleDisconnectScriptCommand(NSScriptCommand command) {
        log.debug("handleDisconnectScriptCommand:" + command);
        this.unmount();
        BackgroundActionRegistry.instance().block();
        return null;
    }

    /**
     * Applescriptability
     *
     * @param command
     * @return
     */
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
                            folder, Path.DIRECTORY_TYPE);
                }
                else {
                    path = PathFactory.createPath(this.session,
                            this.workdir().getAbsolute(),
                            folder, Path.DIRECTORY_TYPE);
                }
            }
            for(AbstractPath i : path.childs()) {
                result.addObject(i.getName());
            }
        }
        return result;
    }

    /**
     * Applescriptability
     *
     * @param command
     * @return
     */
    public Object handleGotoScriptCommand(NSScriptCommand command) {
        log.debug("handleGotoScriptCommand:" + command);
        if(this.isMounted()) {
            NSDictionary args = command.evaluatedArguments();
            CDGotoController c = new CDGotoController(this);
            c.gotoFolder(this.workdir(), (String) args.objectForKey("Path"));
        }
        BackgroundActionRegistry.instance().block();
        return null;
    }

    /**
     * Applescriptability
     *
     * @param command
     * @return
     */
    public Object handleRenameScriptCommand(NSScriptCommand command) {
        log.debug("handleRenameScriptCommand:" + command);
        if(this.isMounted()) {
            NSDictionary args = command.evaluatedArguments();
            String from = (String) args.objectForKey("Path");
            if(!from.startsWith(Path.DELIMITER)) {
                from = this.workdir().getAbsolute() + Path.DELIMITER + from;
            }
            String to = (String) args.objectForKey("Name");
            if(!to.startsWith(Path.DELIMITER)) {
                to = this.workdir().getAbsolute() + Path.DELIMITER + to;
            }
            this.renamePath(PathFactory.createPath(session, from, Path.FILE_TYPE),
                    PathFactory.createPath(session, to, Path.FILE_TYPE));
        }
        BackgroundActionRegistry.instance().block();
        return null;
    }

    /**
     * Applescriptability
     *
     * @param command
     * @return
     */
    public Object handleCreateFolderScriptCommand(NSScriptCommand command) {
        log.debug("handleCreateFolderScriptCommand:" + command);
        if(this.isMounted()) {
            NSDictionary args = command.evaluatedArguments();
            CDFolderController c = new CDFolderController(this);
            c.createFolder(this.workdir(), (String) args.objectForKey("Path"));
        }
        BackgroundActionRegistry.instance().block();
        return null;
    }

    /**
     * Applescriptability
     *
     * @param command
     * @return
     */
    public Integer handleExistsScriptCommand(NSScriptCommand command) {
        log.debug("handleExistsScriptCommand:" + command);
        if(this.isMounted()) {
            NSDictionary args = command.evaluatedArguments();
            Path path = PathFactory.createPath(this.session,
                    this.workdir().getAbsolute(),
                    (String) args.objectForKey("Path"), Path.FILE_TYPE);
            return new Integer(path.exists() ? 1 : 0);
        }
        return new Integer(0);
    }

    /**
     * Applescriptability
     *
     * @param command
     * @return
     */
    public Object handleCreateFileScriptCommand(NSScriptCommand command) {
        log.debug("handleCreateFileScriptCommand:" + command);
        if(this.isMounted()) {
            NSDictionary args = command.evaluatedArguments();
            CDCreateFileController c = new CDCreateFileController(this);
            c.createFile(this.workdir(), (String) args.objectForKey("Path"), false);
        }
        BackgroundActionRegistry.instance().block();
        return null;
    }

    /**
     * Applescriptability
     *
     * @param command
     * @return
     */
    public Object handleEditScriptCommand(NSScriptCommand command) {
        log.debug("handleEditScriptCommand:" + command);
        if(this.isMounted()) {
            NSDictionary args = command.evaluatedArguments();
            Path path = PathFactory.createPath(this.session,
                    this.workdir().getAbsolute(),
                    (String) args.objectForKey("Path"), Path.FILE_TYPE);
            Editor editor = EditorFactory.createEditor(this, path.getLocal(), path);
            editor.open();
        }
        BackgroundActionRegistry.instance().block();
        return null;
    }

    /**
     * Applescriptability
     *
     * @param command
     * @return
     */
    public Object handleDeleteScriptCommand(NSScriptCommand command) {
        log.debug("handleDeleteScriptCommand:" + command);
        if(this.isMounted()) {
            NSDictionary args = command.evaluatedArguments();
            Path path = PathFactory.createPath(this.session,
                    this.workdir().getAbsolute(),
                    (String) args.objectForKey("Path"), Path.FILE_TYPE);
            if(path.list().attributes().isReadable()) {
                path.attributes.setType(Path.DIRECTORY_TYPE);
            }
            path.delete();
        }
        BackgroundActionRegistry.instance().block();
        return null;
    }

    /**
     * Applescriptability
     *
     * @param command
     * @return
     */
    public Object handleRefreshScriptCommand(NSScriptCommand command) {
        log.debug("handleRefreshScriptCommand:" + command);
        if(this.isMounted()) {
            this.reloadButtonClicked(null);
        }
        BackgroundActionRegistry.instance().block();
        return null;
    }

    /**
     * Applescriptability
     *
     * @param command
     * @return
     */
    public TransferAction handleSyncScriptCommand(NSScriptCommand command) {
        log.debug("handleSyncScriptCommand:" + command);
        if(this.isMounted()) {
            NSDictionary args = command.evaluatedArguments();
            final Path path = PathFactory.createPath(this.session,
                    (String) args.objectForKey("Path"), Path.DIRECTORY_TYPE);
            Object localObj = args.objectForKey("Local");
            if(localObj != null) {
                path.setLocal(new Local((String) localObj));
            }
            final Transfer q = new SyncTransfer(path);
            this.transfer(q, true, new TransferPrompt() {
                public TransferAction prompt() {
                    return TransferAction.ACTION_OVERWRITE;
                }
            });
        }
        BackgroundActionRegistry.instance().block();
        return null;
    }

    /**
     * Applescriptability
     *
     * @param command
     * @return
     */
    public TransferAction handleDownloadScriptCommand(NSScriptCommand command) {
        log.debug("handleDownloadScriptCommand:" + command);
        if(this.isMounted()) {
            NSDictionary args = command.evaluatedArguments();
            final Path path = PathFactory.createPath(this.session,
                    this.workdir().getAbsolute(),
                    (String) args.objectForKey("Path"), Path.FILE_TYPE);
            if(path.list().attributes().isReadable()) {
                path.attributes.setType(Path.DIRECTORY_TYPE);
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
            this.transfer(q, true, new TransferPrompt() {
                public TransferAction prompt() {
                    return TransferAction.ACTION_OVERWRITE;
                }
            });
        }
        BackgroundActionRegistry.instance().block();
        return null;
    }

    /**
     * Applescriptability
     *
     * @param command
     * @return
     */
    public TransferAction handleUploadScriptCommand(NSScriptCommand command) {
        log.debug("handleUploadScriptCommand:" + command);
        if(this.isMounted()) {
            NSDictionary args = command.evaluatedArguments();
            final Path path = PathFactory.createPath(this.session,
                    this.workdir().getAbsolute(),
                    new Local((String) args.objectForKey("Path")));
            Object remoteObj = args.objectForKey("Remote");
            if(remoteObj != null) {
                path.setPath((String) remoteObj, path.getName());
            }
            Object nameObj = args.objectForKey("Name");
            if(nameObj != null) {
                path.setPath(this.workdir().getAbsolute(), (String) nameObj);
            }
            final Transfer q = new UploadTransfer(path);
            this.transfer(q, true, new TransferPrompt() {
                public TransferAction prompt() {
                    return TransferAction.ACTION_OVERWRITE;
                }
            });
        }
        BackgroundActionRegistry.instance().block();
        return null;
    }

    // ----------------------------------------------------------
    // Constructor
    // ----------------------------------------------------------

    public CDBrowserController() {
        this.loadBundle();
    }

    protected String getBundleName() {
        return "Browser";
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

        if(Preferences.instance().getBoolean("browser.logDrawer.isOpen")) {
            this.logDrawer.open();
        }
        // Configure Toolbar
        this.toolbar = new NSToolbar("Cyberduck Toolbar");
        this.toolbar.setDelegate(this);
        this.toolbar.setAllowsUserCustomization(true);
        this.toolbar.setAutosavesConfiguration(true);
        this.window().setToolbar(toolbar);

        this.window().makeFirstResponder(this.quickConnectPopup);

        this.toggleBookmarks(true);

        if(this.getSelectedTabView() != TAB_BOOKMARKS) {
            this.browserSwitchClicked(Preferences.instance().getInteger("browser.view"));
        }

        this.validateNavigationButtons();
    }

    protected Comparator<Path> getComparator() {
        return ((CDAbstractPathTableDelegate) this.getSelectedBrowserView().delegate()).getSortingComparator();
    }

    /**
     * Hide files beginning with '.'
     */
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

    protected void setPathFilter(final String searchString) {
        log.debug("setPathFilter:" + searchString);
        if(!StringUtils.hasText(searchString)) {
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
                    if(file.getName().toLowerCase().indexOf(searchString.toLowerCase()) != -1) {
                        // Matching filename
                        return true;
                    }
                    if(file.attributes.isDirectory() && getSelectedBrowserView() == browserOutlineView) {
                        // #471. Expanded item childs may match search string
                        return file.isCached();
                    }
                    return false;
                }
            };
        }
        this.reloadData(true);
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
        if(this.getSelectedTabView() == TAB_BOOKMARKS) {
            if(this.isMounted()) {
                int row = this.bookmarkModel.getSource().indexOf(this.getSession().getHost());
                if(row != -1) {
                    this.bookmarkTable.selectRow(row, false);
                    this.bookmarkTable.scrollRowToVisible(row);
                }
            }
            this.updateStatusLabel(this.bookmarkTable.numberOfRows() + " " + NSBundle.localizedString("Bookmarks", ""));
            this.window().makeFirstResponder(bookmarkTable);
        }
        else {
            if(this.isMounted()) {
                this.window().makeFirstResponder(this.getSelectedBrowserView());
            }
            else {
                this.window().makeFirstResponder(this.quickConnectPopup);
            }
            this.updateStatusLabel(null);
        }
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
            this.reloadData(Collections.<Path>emptyList());
        }
    }

    /**
     * @param selected The items to be selected
     * @see #setSelectedPaths(java.util.Collection)
     */
    protected void reloadData(final List<Path> selected) {
        log.debug("reloadData");
        if(this.isMounted()) {
            if(!this.workdir().isCached()) {
                this.background(new BrowserBackgroundAction(this) {
                    public void run() {
                        workdir().childs();
                    }

                    public void cleanup() {
                        reloadData(selected);
                    }

                    public String getActivity() {
                        return MessageFormat.format(NSBundle.localizedString("Listing directory {0}", "Status", ""),
                                workdir.getName());
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
        this.setSelectedPaths(selected);
        this.updateStatusLabel(null);
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
        List<Path> list = new Collection<Path>();
        list.add(selected);
        this.setSelectedPaths(list);
    }

    protected void setSelectedPaths(List<Path> selected) {
        log.debug("setSelectedPaths");
        this.deselectAll();
        if(!selected.isEmpty()) {
            switch(this.browserSwitchView.selectedSegment()) {
                case SWITCH_LIST_VIEW: {
                    //selection handling
                    for(Iterator<Path> iter = selected.iterator(); iter.hasNext();) {
                        this.selectRow(iter.next(), true);
                    }
                    break;
                }
                case SWITCH_OUTLINE_VIEW: {
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
        List<Path> selected = this.getSelectedPaths();
        if(selected.size() > 0) {
            return selected.get(0);
        }
        return null;
    }

    /**
     * @return All selected paths or an empty list if there is no selection
     */
    protected Collection<Path> getSelectedPaths() {
        Collection<Path> selectedFiles = new Collection<Path>();
        if(this.isMounted()) {
            NSIndexSet iterator = this.getSelectedBrowserView().selectedRowIndexes();
            for(int index = iterator.firstIndex(); index != NSIndexSet.NotFound; index = iterator.indexGreaterThanIndex(index)) {
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
            case SWITCH_LIST_VIEW: {
                final AttributedList<Path> childs = this.browserListModel.childs(this.workdir());
                if(row < childs.size()) {
                    item = childs.get(row);
                }
                break;
            }
            case SWITCH_OUTLINE_VIEW: {
                if(row < this.browserOutlineView.numberOfRows()) {
                    item = (Path) this.browserOutlineView.itemAtRow(row);
                }
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

    private CDTranscriptController transcript;

    private NSDrawer logDrawer;

    private NSDrawer.Notifications logDrawerNotifications = new NSDrawer.Notifications() {
        public void drawerWillOpen(NSNotification notification) {
            logDrawer.setContentSize(new NSSize(
                    logDrawer.contentSize().width(),
                    Preferences.instance().getFloat("browser.logDrawer.size.height")
            ));
        }

        public void drawerDidOpen(NSNotification notification) {
            Preferences.instance().setProperty("browser.logDrawer.isOpen", true);
        }

        public void drawerWillClose(NSNotification notification) {
            Preferences.instance().setProperty("browser.logDrawer.size.height",
                    logDrawer.contentSize().height());
        }

        public void drawerDidClose(NSNotification notification) {
            Preferences.instance().setProperty("browser.logDrawer.isOpen", false);
        }
    };

    public void setLogDrawer(NSDrawer logDrawer) {
        this.logDrawer = logDrawer;
        this.transcript = new CDTranscriptController();
        this.logDrawer.setContentView(this.transcript.getLogView());
        NSNotificationCenter.defaultCenter().addObserver(logDrawerNotifications,
                new NSSelector("drawerWillOpen", new Class[]{NSNotification.class}),
                NSDrawer.DrawerWillOpenNotification,
                this.logDrawer);
        NSNotificationCenter.defaultCenter().addObserver(logDrawerNotifications,
                new NSSelector("drawerDidOpen", new Class[]{NSNotification.class}),
                NSDrawer.DrawerDidOpenNotification,
                this.logDrawer);
        NSNotificationCenter.defaultCenter().addObserver(logDrawerNotifications,
                new NSSelector("drawerWillClose", new Class[]{NSNotification.class}),
                NSDrawer.DrawerWillCloseNotification,
                this.logDrawer);
        NSNotificationCenter.defaultCenter().addObserver(logDrawerNotifications,
                new NSSelector("drawerDidClose", new Class[]{NSNotification.class}),
                NSDrawer.DrawerDidCloseNotification,
                this.logDrawer);
    }

    private static final int TAB_BOOKMARKS = 0;
    private static final int TAB_LIST_VIEW = 1;
    private static final int TAB_OUTLINE_VIEW = 2;

    /**
     * @return
     */
    private int getSelectedTabView() {
        return this.browserTabView.indexOfTabViewItem(this.browserTabView.selectedTabViewItem());
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
            case SWITCH_LIST_VIEW: {
                return this.browserListView;
            }
            case SWITCH_OUTLINE_VIEW: {
                return this.browserOutlineView;
            }
        }
        return null;
    }

//    private NSTableView bookmarkSourceView;
//    private CDTableDelegate bookmarkSourceDelegate;
//    private CDListDataSource bookmarkSourceModel;
//
//    public void setBookmarkSourceView(final NSTableView bookmarkSourceView) {
//        this.bookmarkSourceView = bookmarkSourceView;
//        this.bookmarkSourceView.setDelegate(this.bookmarkSourceDelegate = new CDAbstractTableDelegate() {
//            public void tableColumnClicked(NSTableView view, NSTableColumn tableColumn) {
//
//            }
//
//            public void tableRowDoubleClicked(Object sender) {
//
//            }
//
//            public void selectionDidChange(NSNotification notification) {
//                final int row = bookmarkSourceView.selectedRow();
//                if(0 == row) {
//                    bookmarkModel.setSource(new Collection() {
//                        public Object get(int row) {
//                            return Rendezvous.instance().getService(row);
//                        }
//
//                        public int size() {
//                            return Rendezvous.instance().numberOfServices();
//                        }
//                    });
//                }
//                else if(1 == row) {
//                    bookmarkModel.setSource(HistoryCollection.HISTORY);
//                }
//                else if(2 == row) {
//                    bookmarkModel.setSource(BookmarkCollection.BOOKMARKS);
//                }
//                else {
//                    bookmarkModel.setSource(new Collection());
//                }
//                bookmarkTable.reloadData();
//                getFocus();
//            }
//
//            public void enterKeyPressed(Object sender) {
//
//            }
//
//            public void deleteKeyPressed(Object sender) {
//
//            }
//        });
//        NSSelector setResizableMaskSelector
//                = new NSSelector("setResizingMask", new Class[]{int.class});
//        {
//            NSTableColumn c = new NSTableColumn();
//            c.setIdentifier("ICON");
//            c.headerCell().setStringValue("");
//            c.setMinWidth(32f);
//            c.setWidth(32f);
//            c.setMaxWidth(32f);
//            if(setResizableMaskSelector.implementedByClass(NSTableColumn.class)) {
//                c.setResizingMask(NSTableColumn.AutoresizingMask);
//            }
//            else {
//                c.setResizable(false);
//            }
//            c.setDataCell(new NSImageCell());
//            this.bookmarkSourceView.addTableColumn(c);
//        }
//        {
//            NSTableColumn c = new NSTableColumn();
//            c.setIdentifier("NAME");
//            c.headerCell().setStringValue(NSBundle.localizedString("Bookmarks", "A column in the browser"));
//            c.setMinWidth(150f);
//            if(setResizableMaskSelector.implementedByClass(NSTableColumn.class)) {
//                c.setResizingMask(NSTableColumn.AutoresizingMask);
//            }
//            else {
//                c.setResizable(true);
//            }
//            c.setDataCell(new NSTextFieldCell());
//            this.bookmarkSourceView.addTableColumn(c);
//        }
//        this.bookmarkSourceView.setDataSource(this.bookmarkSourceModel = new CDListDataSource() {
//            public int numberOfRowsInTableView(NSTableView view) {
//                return 3;
//            }
//
//            public Object tableViewObjectValueForLocation(NSTableView view, NSTableColumn tableColumn, int row) {
//                if(row < this.numberOfRowsInTableView(view)) {
//                    String identifier = (String) tableColumn.identifier();
//                    if(identifier.equals("ICON")) {
//                        if(0 == row) {
//                            return NSImage.imageNamed("rendezvous16.tiff");
//                        }
//                        if(1 == row) {
//                            return NSImage.imageNamed("history.tiff");
//                        }
//                        if(2 == row) {
//                            return NSImage.imageNamed("bookmarks.tiff");
//                        }
//                    }
//                    if(identifier.equals("NAME")) {
//                        if(0 == row) {
//                            return NSBundle.localizedString("Bonjour");
//                        }
//                        if(1 == row) {
//                            return NSBundle.localizedString("History");
//                        }
//                        if(2 == row) {
//                            return NSBundle.localizedString("Bookmarks");
//                        }
//                    }
//                    throw new IllegalArgumentException("Unknown identifier: " + identifier);
//                }
//                return null;
//            }
//        });
////        this.bookmarkSourceTableView.setStyle(NSTableViewSelectionHighlightStyleSourceList)
//
//        // setting appearance attributes
//        this.bookmarkSourceView.setUsesAlternatingRowBackgroundColors(Preferences.instance().getBoolean("browser.alternatingRows"));
//        this.bookmarkSourceView.setGridStyleMask(NSTableView.GridNone);
//
//        // selection properties
//        this.bookmarkSourceView.setAllowsMultipleSelection(false);
//        this.bookmarkSourceView.setAllowsEmptySelection(true);
//        this.bookmarkSourceView.setAllowsColumnResizing(false);
//        this.bookmarkSourceView.setAllowsColumnSelection(false);
//        this.bookmarkSourceView.setAllowsColumnReordering(false);
//        this.bookmarkSourceView.sizeToFit();
//    }

    /**
     * @return The datasource of the currently selected browser view
     */
    public CDBrowserTableDataSource getSelectedBrowserModel() {
        return (CDBrowserTableDataSource) this.getSelectedBrowserView().dataSource();
    }

    private NSButton bonjourButton;

    public void setBonjourButton(NSButton bonjourButton) {
        this.bonjourButton = bonjourButton;
        this.bonjourButton.setImage(NSImage.imageNamed("rendezvous16.tiff"));
        this.setRecessedBezelStyle(this.bonjourButton);
        this.bonjourButton.setTarget(this);
        this.bonjourButton.setAction(new NSSelector("bookmarkButtonClicked", new Class[]{Object.class}));
    }

    private NSButton historyButton;

    public void setHistoryButton(NSButton historyButton) {
        this.historyButton = historyButton;
        this.historyButton.setImage(NSImage.imageNamed("history.tiff"));
        this.setRecessedBezelStyle(this.historyButton);
        this.historyButton.setTarget(this);
        this.historyButton.setAction(new NSSelector("bookmarkButtonClicked", new Class[]{Object.class}));
    }

    private NSButton bookmarkButton;

    public void setBookmarkButton(NSButton bookmarkButton) {
        this.bookmarkButton = bookmarkButton;
        this.bookmarkButton.setImage(NSImage.imageNamed("bookmarks.tiff"));
        this.setRecessedBezelStyle(this.bookmarkButton);
        this.bookmarkButton.setTarget(this);
        this.bookmarkButton.setAction(new NSSelector("bookmarkButtonClicked", new Class[]{Object.class}));
        this.bookmarkButton.setState(NSCell.OnState); // Set as default selected bookmark source
    }

    public void bookmarkButtonClicked(final NSButton sender) {
        if(sender != bonjourButton) {
            bonjourButton.setState(NSCell.OffState);
        }
        if(sender != historyButton) {
            historyButton.setState(NSCell.OffState);
        }
        if(sender != bookmarkButton) {
            bookmarkButton.setState(NSCell.OffState);
        }
        sender.setState(NSCell.OnState);

        this.updateBookmarkSource();
    }

    private void setRecessedBezelStyle(final NSButton b) {
        b.setBezelStyle(13); //NSRecessedBezelStyle
        b.setButtonType(NSButton.PushOnPushOff);
        b.setImagePosition(NSCell.ImageLeft);
        b.setFont(NSFont.boldSystemFontOfSize(11f));
        b.setShowsBorderOnlyWhileMouseInside(true);
        b.sizeToFit();
    }

    private void updateBookmarkSource() {
        if(bonjourButton.state() == NSCell.OnState) {
            bookmarkModel.setSource(RendezvousCollection.defaultCollection());
        }
        else if(historyButton.state() == NSCell.OnState) {
            bookmarkModel.setSource(HistoryCollection.defaultCollection());
        }
        else if(bookmarkButton.state() == NSCell.OnState) {
            bookmarkModel.setSource(HostCollection.defaultCollection());
        }
        addBookmarkButton.setEnabled(bookmarkModel.isEditable());
        this.setBookmarkFilter(null);
        bookmarkTable.deselectAll(null);
        bookmarkTable.reloadData();
        this.getFocus();
    }

    private NSSegmentedControl bookmarkSwitchView;

    private static final int SWITCH_BOOKMARK_VIEW = 0;

    public void setBookmarkSwitchView(NSSegmentedControl bookmarkSwitchView) {
        this.bookmarkSwitchView = bookmarkSwitchView;
        this.bookmarkSwitchView.setSegmentCount(1);
        this.bookmarkSwitchView.setImage(NSImage.imageNamed("bookmarks.tiff"), SWITCH_BOOKMARK_VIEW);
        final NSSegmentedCell cell = (NSSegmentedCell) this.bookmarkSwitchView.cell();
        cell.setTrackingMode(NSSegmentedCell.NSSegmentSwitchTrackingSelectAny);
        cell.setControlSize(NSCell.RegularControlSize);
        this.bookmarkSwitchView.setTarget(this);
        this.bookmarkSwitchView.setAction(new NSSelector("bookmarkSwitchClicked", new Class[]{Object.class}));
        this.bookmarkSwitchView.setSelected(true, SWITCH_BOOKMARK_VIEW);
    }

    public void bookmarkSwitchClicked(final Object sender) {
        this.toggleBookmarks(this.getSelectedTabView() != TAB_BOOKMARKS);
    }

    /**
     * @param open Should open the bookmarks
     */
    public void toggleBookmarks(final boolean open) {
        log.debug("bookmarkSwitchClicked:" + open);
        this.bookmarkSwitchView.setSelected(open, SWITCH_BOOKMARK_VIEW);
        if(open) {
            // Display bookmarks
            this.browserTabView.selectTabViewItemAtIndex(TAB_BOOKMARKS);
            this.updateBookmarkSource();
        }
        else {
            this.setBookmarkFilter(null);
            this.selectBrowser(Preferences.instance().getInteger("browser.view"));
        }
        this.getFocus();
        this.validateNavigationButtons();
    }

    private NSSegmentedControl browserSwitchView;

    private static final int SWITCH_LIST_VIEW = 0;
    private static final int SWITCH_OUTLINE_VIEW = 1;

    public void setBrowserSwitchView(NSSegmentedControl browserSwitchView) {
        this.browserSwitchView = browserSwitchView;
        this.browserSwitchView.setSegmentCount(2); // list, outline
        this.browserSwitchView.setImage(NSImage.imageNamed("list.tiff"), SWITCH_LIST_VIEW);
        this.browserSwitchView.setImage(NSImage.imageNamed("outline.tiff"), SWITCH_OUTLINE_VIEW);
        this.browserSwitchView.setTarget(this);
        this.browserSwitchView.setAction(new NSSelector("browserSwitchButtonClicked", new Class[]{Object.class}));
        final NSSegmentedCell cell = (NSSegmentedCell) this.browserSwitchView.cell();
        cell.setTrackingMode(NSSegmentedCell.NSSegmentSwitchTrackingSelectOne);
        cell.setControlSize(NSCell.RegularControlSize);
        this.browserSwitchView.setSelected(Preferences.instance().getInteger("browser.view"));
    }

    public void browserSwitchButtonClicked(final NSSegmentedControl sender) {
        this.browserSwitchClicked(sender.selectedSegment());
    }

    public void browserSwitchMenuClicked(final NSMenuItem sender) {
        this.browserSwitchView.setSelected(true, sender.tag());
        this.browserSwitchClicked(sender.tag());
    }

    private void browserSwitchClicked(final int selected) {
        // Close bookmarks
        this.toggleBookmarks(false);
        // Highlight selected browser view
        this.selectBrowser(selected);
        this.reloadData(false);
        this.getFocus();
        // Save selected browser view
        Preferences.instance().setProperty("browser.view", selected);
    }

    private void selectBrowser(int selected) {
        this.browserSwitchView.setSelected(selected);
        switch(selected) {
            case SWITCH_LIST_VIEW:
                this.browserTabView.selectTabViewItemAtIndex(TAB_LIST_VIEW);
                break;
            case SWITCH_OUTLINE_VIEW:
                this.browserTabView.selectTabViewItemAtIndex(TAB_OUTLINE_VIEW);
                break;
        }
    }

    private abstract class AbstractBrowserTableDelegate extends CDAbstractPathTableDelegate {

        private Collection<Local> temporaryQuickLookFiles = new Collection<Local>() {
            public void collectionItemRemoved(Local o) {
                (o).delete(false);
            }
        };

        public AbstractBrowserTableDelegate() {
            CDBrowserController.this.addListener(new CDWindowListener() {
                public void windowWillClose() {
                    temporaryQuickLookFiles.clear();
                }
            });
        }

        public boolean isColumnEditable(NSTableColumn column) {
            if(Preferences.instance().getBoolean("browser.editable")) {
                if(column.identifier().equals(CDBrowserTableDataSource.FILENAME_COLUMN)) {
                    Path selected = getSelectedPath();
                    if(null == selected) {
                        return false;
                    }
                    return true;
                }
            }
            return false;
        }

        public void tableRowDoubleClicked(final Object sender) {
            CDBrowserController.this.insideButtonClicked(sender);
        }

        public void spaceKeyPressed(final Object sender) {
            if(QuickLook.isAvailable()) {
                if(QuickLook.isOpen()) {
                    QuickLook.close();
                }
                else {
                    this.updateQuickLookSelection(
                            CDBrowserController.this.getSelectedPaths()
                    );
                }
            }
        }

        private void updateQuickLookSelection(final Collection<Path> selected) {
            if(QuickLook.isAvailable()) {
                final Collection<Path> downloads = new Collection<Path>();
                for(Path path: selected){
                    if(!path.attributes.isFile()) {
                        continue;
                    }
                    final Local folder = new Local(new File(NSPathUtilities.temporaryDirectory(),
                            path.getParent().getAbsolute()));
                    folder.mkdir(true);
                    path.setLocal(new Local(folder, path.getAbsolute()));
                    downloads.add(path);
                }
                if(downloads.size() > 0) {
                    background(new BrowserBackgroundAction(CDBrowserController.this) {
                        public void run() {
                            for(Path download: downloads) {
                                if(this.isCanceled()) {
                                    break;
                                }
                                if(download.getLocal().attributes.getSize() != download.attributes.getSize()) {
                                    download.download(true);
                                }
                            }
                        }

                        public void cleanup() {
                            final Collection<Local> previews = new Collection<Local>();
                            for(Path download: downloads) {
                                if(download.getStatus().isComplete()) {
                                    previews.add(download.getLocal());
                                }
                            }
                            // Keep references to delete later
                            temporaryQuickLookFiles.addAll(previews);
                            // Change files in Quick Look
                            QuickLook.select((Local[]) previews.toArray(new Local[previews.size()]));
                            // Open Quick Look Preview Panel
                            QuickLook.open();
                            // Revert status label
                            CDBrowserController.this.updateStatusLabel(null);
                            // Restore the focus to our window to demo the selection changing, scrolling
                            // (left/right) and closing (space) functionality
                            CDBrowserController.this.window().makeKeyWindow();
                        }

                        public String getActivity() {
                            return NSBundle.localizedString("Quick Look", "Status", "");
                        }
                    });
                }
            }
        }

        public void enterKeyPressed(final Object sender) {
            ;
        }

        public void deleteKeyPressed(final Object sender) {
            CDBrowserController.this.deleteFileButtonClicked(sender);
        }

        public void tableColumnClicked(NSTableView view, NSTableColumn tableColumn) {
            List<Path> selected = CDBrowserController.this.getSelectedPaths();
            if(this.selectedColumnIdentifier().equals(tableColumn.identifier())) {
                this.setSortedAscending(!this.isSortedAscending());
            }
            else {
                // Remove sorting indicator on previously selected column
                this.setBrowserColumnSortingIndicator(null, this.selectedColumnIdentifier());
                // Set the newly selected column
                this.setSelectedColumn(tableColumn);
            }
            this.setBrowserColumnSortingIndicator(
                    this.isSortedAscending() ?
                            NSImage.imageNamed("NSAscendingSortIndicator") :
                            NSImage.imageNamed("NSDescendingSortIndicator"),
                    tableColumn.identifier().toString());
            reloadData(selected);
        }

        public void selectionDidChange(NSNotification notification) {
            final Collection<Path> selected = getSelectedPaths();
            if(1 == selected.size()) {
                final Path p = selected.get(0);
                if(p.attributes.isFile()) {
                    EditorFactory.setSelectedEditor(EditorFactory.editorBundleIdentifierForFile(
                            p.getLocal()));
                }
            }
            if(Preferences.instance().getBoolean("browser.info.isInspector")) {
                if(inspector != null && inspector.window() != null && inspector.window().isVisible()) {
                    if(selected.size() > 0) {
                        background(new BrowserBackgroundAction(CDBrowserController.this) {
                            public void run() {
                                for(Path p : selected) {
                                    if(this.isCanceled()) {
                                        break;
                                    }
                                    if(p.attributes.getPermission() == null) {
                                        p.readPermission();
                                    }
                                }
                            }

                            public void cleanup() {
                                inspector.setFiles(selected);
                            }

                            public String getActivity() {
                                return MessageFormat.format(NSBundle.localizedString("Getting permission of {0}", "Status", ""), null);
                            }
                        });
                    }
                }
            }
            if(QuickLook.isOpen()) {
                this.updateQuickLookSelection(selected);
            }
        }

        private void setBrowserColumnSortingIndicator(NSImage image, String columnIdentifier) {
            if(browserListView.tableColumnWithIdentifier(columnIdentifier) != null) {
                browserListView.setIndicatorImage(image, browserListView.tableColumnWithIdentifier(columnIdentifier));
            }
            if(browserOutlineView.tableColumnWithIdentifier(columnIdentifier) != null) {
                browserOutlineView.setIndicatorImage(image, browserOutlineView.tableColumnWithIdentifier(columnIdentifier));
            }
        }
    }

    private CDBrowserOutlineViewModel browserOutlineModel;
    private NSOutlineView browserOutlineView; // IBOutlet
    private CDTableDelegate<Path> browserOutlineViewDelegate;

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
            public void outlineViewWillDisplayCell(NSOutlineView outlineView, NSCell cell,
                                                   NSTableColumn tableColumn, Path item) {
                String identifier = (String) tableColumn.identifier();
                if(item != null) {
                    if(identifier.equals(CDBrowserTableDataSource.FILENAME_COLUMN)) {
                        ((CDOutlineCell) cell).setIcon(browserOutlineModel.iconForPath(item));
                        ((CDOutlineCell) cell).setEditable(true);
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
                log.debug("outlineViewShouldExpandItem:" + item);
                NSEvent event = NSApplication.sharedApplication().currentEvent();
                if(event != null) {
                    if(NSEvent.LeftMouseDragged == event.type()) {
                        final int draggingColumn = view.columnAtPoint(view.convertPointFromView(event.locationInWindow(), null));
                        if(draggingColumn != 0) {
                            log.debug("Returning false to #outlineViewShouldExpandItem for column:" + draggingColumn);
                            // See ticket #60
                            return false;
                        }
                        if(!Preferences.instance().getBoolean("browser.view.autoexpand")) {
                            log.debug("Returning false to #outlineViewShouldExpandItem:" + item.getName() + " while dragging because browser.view.autoexpand == false");
                            // See tickets #98 and #633
                            return false;
                        }
                    }
                }
                return true;
            }

            /**
             * @see NSOutlineView.Notifications
             */
            public void outlineViewItemDidExpand(NSNotification notification) {
                updateStatusLabel(null);
            }

            /**
             * @see NSOutlineView.Notifications
             */
            public void outlineViewItemDidCollapse(NSNotification notification) {
                updateStatusLabel(null);
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
                cell.setTarget(browserOutlineView.target());
                cell.setAction(browserOutlineView.action());
            }
            c.setDataCell(cell);
            this.browserOutlineView.addTableColumn(c);
            this.browserOutlineView.setOutlineTableColumn(c);
        }
    }

    private CDBrowserListViewModel browserListModel;
    private NSTableView browserListView; // IBOutlet
    private CDTableDelegate<Path> browserListViewDelegate;

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

            public void tableViewWillDisplayCell(NSTableView view, NSTextFieldCell cell, NSTableColumn tableColumn, int row) {
                if(cell instanceof NSTextFieldCell) {
                    if(!CDBrowserController.this.isConnected()) {// || CDBrowserController.this.activityRunning) {
                        cell.setTextColor(NSColor.disabledControlTextColor());
                    }
                    else {
                        cell.setTextColor(NSColor.controlTextColor());
                    }
                }
            }

            public String tableViewToolTipForCell(NSTableView view, NSCell cell, NSMutableRect rect,
                                                  NSTableColumn tc, int row, NSPoint mouseLocation) {
                if(row < browserListModel.childs(CDBrowserController.this.workdir()).size()) {
                    Path p = browserListModel.childs(CDBrowserController.this.workdir()).get(row);
                    return this.tooltip(p);
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
        table.removeTableColumn(table.tableColumnWithIdentifier(CDBrowserTableDataSource.GROUP_COLUMN));
        if(Preferences.instance().getBoolean("browser.columnGroup")) {
            NSTableColumn c = new NSTableColumn();
            c.headerCell().setStringValue(NSBundle.localizedString("Group", "A column in the browser"));
            c.setIdentifier(CDBrowserTableDataSource.GROUP_COLUMN);
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
    private CDTableDelegate<Host> bookmarkTableDelegate;

    public void setBookmarkTable(NSTableView view) {
        this.bookmarkTable = view;
        this.bookmarkTable.setDataSource(this.bookmarkModel = new CDBookmarkTableDataSource(
                this, HostCollection.defaultCollection())
        );
        this.bookmarkTable.setDelegate(this.bookmarkTableDelegate = new CDAbstractTableDelegate<Host>() {
            public String tooltip(Host bookmark) {
                return bookmark.toURL();
            }
                
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

            public void selectionDidChange(NSNotification notification) {
                addBookmarkButton.setEnabled(bookmarkModel.isEditable());
                editBookmarkButton.setEnabled(bookmarkModel.isEditable()
                        && bookmarkTable.numberOfSelectedRows() == 1);
                deleteBookmarkButton.setEnabled(bookmarkModel.isEditable()
                        && bookmarkTable.selectedRow() != -1);
            }
        });
        // receive drag events from types
        this.bookmarkTable.registerForDraggedTypes(new NSArray(new Object[]
                {
                        NSPasteboard.StringPboardType,
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
                c.setResizable(false);
            }
            c.setDataCell(new NSImageCell());
            this.bookmarkTable.addTableColumn(c);
        }
        {
            NSTableColumn c = new NSTableColumn();
            c.setIdentifier(CDBookmarkTableDataSource.BOOKMARK_COLUMN);
            c.headerCell().setStringValue(NSBundle.localizedString("Bookmarks", "A column in the browser"));
            c.setMinWidth(150f);
            if(setResizableMaskSelector.implementedByClass(NSTableColumn.class)) {
                c.setResizingMask(NSTableColumn.AutoresizingMask);
            }
            else {
                c.setResizable(true);
            }
            c.setDataCell(new CDBookmarkCell());
            this.bookmarkTable.addTableColumn(c);
        }
        {
            NSTableColumn c = new NSTableColumn();
            c.setIdentifier(CDBookmarkTableDataSource.STATUS_COLUMN);
            c.headerCell().setStringValue("");
            c.setMinWidth(20f);
            c.setWidth(20f);
            c.setMaxWidth(20f);
            if(setResizableMaskSelector.implementedByClass(NSTableColumn.class)) {
                c.setResizingMask(NSTableColumn.AutoresizingMask);
            }
            else {
                c.setResizable(false);
            }
            c.setDataCell(new NSImageCell());
            c.dataCell().setAlignment(NSText.CenterTextAlignment);
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

        HistoryCollection.defaultCollection().addListener(new CollectionListener<Host>() {
            public void collectionItemAdded(Host item) {
                this.reloadBookmarks();
            }

            public void collectionItemRemoved(Host item) {
                this.reloadBookmarks();
            }

            public void collectionItemChanged(Host item) {
                this.reloadBookmarks();
            }

            private void reloadBookmarks() {
                if(bookmarkModel.getSource().equals(HistoryCollection.defaultCollection())) {
                    bookmarkTable.deselectAll(null);
                    bookmarkTable.reloadData();
                }
            }
        });

        HostCollection.defaultCollection().addListener(new CollectionListener<Host>() {
            public void collectionItemAdded(Host item) {
                this.reloadBookmarks();
            }

            public void collectionItemRemoved(Host item) {
                this.reloadBookmarks();
            }

            public void collectionItemChanged(Host item) {
                this.reloadBookmarks();
            }

            private void reloadBookmarks() {
                if(bookmarkModel.getSource().equals(HostCollection.defaultCollection())) {
                    bookmarkTable.deselectAll(null);
                    bookmarkTable.reloadData();
                }
            }
        });

        Rendezvous.instance().addListener(new RendezvousListener() {
            public void serviceResolved(String servicename, String hostname) {
                this.reloadBookmarks();
            }

            public void serviceLost(String servicename) {
                this.reloadBookmarks();
            }

            private void reloadBookmarks() {
                if(bookmarkModel.getSource().equals(RendezvousCollection.defaultCollection())) {
                    bookmarkTable.deselectAll(null);
                    bookmarkTable.reloadData();
                }
            }
        });
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
            public int numberOfItemsInComboBox(final NSComboBox combo) {
                return HostCollection.defaultCollection().size();
            }

            public String comboBoxObjectValueForItemAtIndex(final NSComboBox sender, final int row) {
                if(row < numberOfItemsInComboBox(sender)) {
                    return ((Host) HostCollection.defaultCollection().get(row)).getNickname();
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
        int size = HostCollection.defaultCollection().size();
        this.quickConnectPopup.setNumberOfVisibleItems(size > 10 ? 10 : size);
    }

    public void quickConnectSelectionChanged(final NSControl sender) {
        if(null == sender) {
            return;
        }
        String input = (sender).stringValue();
        if(!StringUtils.hasText(input)) {
            return;
        }
        input = input.trim();
        // First look for equivalent bookmarks
        for(Iterator<Host> iter = HostCollection.defaultCollection().iterator(); iter.hasNext();) {
            Host h = iter.next();
            if(h.getNickname().equals(input)) {
                this.mount(h);
                return;
            }
        }
        // Try to parse the input as a URL and extract protocol, hostname, username and password if any.
        this.mount(Host.parse(input));
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
//            this.background(new BrowserBackgroundAction() {
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
        this.searchField.setEnabled(false);
        NSNotificationCenter.defaultCenter().addObserver(this,
                new NSSelector("searchFieldTextDidChange", new Class[]{NSNotification.class}),
                NSControl.ControlTextDidChangeNotification,
                this.searchField);
    }


    public void searchFieldTextDidChange(NSNotification notification) {
        NSDictionary userInfo = notification.userInfo();
        if(null != userInfo) {
            Object o = userInfo.allValues().lastObject();
            if(null != o) {
                final String searchString = ((NSText) o).string();
                if(this.getSelectedTabView() == TAB_BOOKMARKS) {
                    this.setBookmarkFilter(searchString);
                }
                else { // TAB_LIST_VIEW || TAB_OUTLINE_VIEW
                    this.setPathFilter(searchString);
                }
            }
        }
    }

    private void setBookmarkFilter(final String searchString) {
        if(!StringUtils.hasText(searchString)) {
            this.searchField.setStringValue("");
            this.bookmarkModel.setFilter(null);
        }
        else {
            this.bookmarkModel.setFilter(new HostFilter() {
                public boolean accept(Host host) {
                    return host.getNickname().toLowerCase().contains(searchString.toLowerCase())
                            || host.getHostname().toLowerCase().contains(searchString.toLowerCase());
                }
            });
        }
        this.bookmarkTable.reloadData();

    }

    // ----------------------------------------------------------
    // Manage Bookmarks
    // ----------------------------------------------------------

    public void connectBookmarkButtonClicked(final Object sender) {
        if(bookmarkTable.numberOfSelectedRows() == 1) {
            final Host selected = (Host) bookmarkModel.getSource().get(bookmarkTable.selectedRow());
            this.mount(selected);
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
        CDBookmarkController c = CDBookmarkController.Factory.create(
                (Host) bookmarkModel.getSource().get(bookmarkTable.selectedRow())
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
        final Host item;
        if(this.isMounted()) {
            Path selected = this.getSelectedPath();
            if(null == selected || !selected.attributes.isDirectory()) {
                selected = this.workdir();
            }
            item = new Host(this.session.getHost().getAsDictionary());
            item.setDefaultPath(selected.getAbsolute());
        }
        else {
            item = new Host(Protocol.forName(Preferences.instance().getProperty("connection.protocol.default")),
                    Preferences.instance().getProperty("connection.hostname.default"),
                    Preferences.instance().getInteger("connection.port.default"));
        }

        this.toggleBookmarks(true);

        bookmarkModel.setFilter(null);
        bookmarkModel.getSource().add(item);
        final int index = bookmarkModel.getSource().lastIndexOf(item);
        bookmarkTable.selectRow(index, false);
        bookmarkTable.scrollRowToVisible(index);
        CDBookmarkController c = CDBookmarkController.Factory.create(
                (Host) bookmarkModel.getSource().get(index)
        );
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
        if(bookmarkModel.isEditable()) {
            final NSEnumerator iterator = bookmarkTable.selectedRowEnumerator();
            int[] indexes = new int[bookmarkTable.numberOfSelectedRows()];
            int i = 0;
            while(iterator.hasMoreElements()) {
                indexes[i] = ((Number) iterator.nextElement()).intValue();
                i++;
            }
            bookmarkTable.deselectAll(null);
            int j = 0;
            for(i = 0; i < indexes.length; i++) {
                int row = indexes[i] - j;
                bookmarkTable.selectRow(row, false);
                bookmarkTable.scrollRowToVisible(row);
                Host host = (Host) bookmarkModel.getSource().get(row);
                switch(NSAlertPanel.runCriticalAlert(NSBundle.localizedString("Delete Bookmark", ""),
                        NSBundle.localizedString("Do you want to delete the selected bookmark?", "")
                                + " (" + host.getNickname() + ")",
                        NSBundle.localizedString("Delete", ""),
                        NSBundle.localizedString("Cancel", ""),
                        null)) {
                    case CDSheetCallback.DEFAULT_OPTION:
                        bookmarkModel.getSource().remove(row);
                        j++;
                }
            }
            bookmarkTable.deselectAll(null);
        }
    }

    // ----------------------------------------------------------
    // Browser navigation
    // ----------------------------------------------------------

    private static final int NAVIGATION_LEFT_SEGMENT_BUTTON = 0;
    private static final int NAVIGATION_RIGHT_SEGMENT_BUTTON = 1;

    private static final int NAVIGATION_UP_SEGMENT_BUTTON = 0;

    private NSSegmentedControl navigationButton; // IBOutlet

//    private NSMenu navigationBackMenu;
//    private MenuDelegate navigationBackMenuDelegate;
//    private NSMenu navigationForwardMenu;
//    private MenuDelegate navigationForwardMenuDelegate;

    public void setNavigationButton(NSSegmentedControl navigationButton) {
        this.navigationButton = navigationButton;
        this.navigationButton.setTarget(this);
        this.navigationButton.setAction(new NSSelector("navigationButtonClicked", new Class[]{Object.class}));

//        this.navigationBackMenu = new NSMenu();
//        this.navigationBackMenu.setDelegate(this.navigationBackMenuDelegate = new BackPathHistoryMenuDelegate(this));
//        this.navigationButton.setMenu(this.navigationBackMenu, NAVIGATION_LEFT_SEGMENT_BUTTON);

//        this.navigationForwardMenu = new NSMenu();
//        this.navigationForwardMenu.setDelegate(this.navigationForwardMenuDelegate = new ForwardPathHistoryMenuDelegate(this));
//        this.navigationButton.setMenu(this.navigationForwardMenu, NAVIGATION_RIGHT_SEGMENT_BUTTON);
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

    public void backButtonClicked(final NSSegmentedControl sender) {
        final Path selected = this.getPreviousPath();
        if(selected != null) {
            final Path previous = this.workdir();
            this.setWorkdir(selected);
            if(previous.getParent().equals(selected)) {
                this.setSelectedPath(previous);
            }
        }
    }

    public void forwardButtonClicked(final NSSegmentedControl sender) {
        final Path selected = this.getForwardPath();
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
        final Path previous = this.workdir();
        this.setWorkdir((Path) previous.getParent());
        this.setSelectedPath(previous);
    }

    private Path workdir;

    private NSPopUpButton pathPopupButton; // IBOutlet

    public void setPathPopup(NSPopUpButton pathPopupButton) {
        this.pathPopupButton = pathPopupButton;
        this.pathPopupButton.setTarget(this);
        this.pathPopupButton.setAction(new NSSelector("pathPopupSelectionChanged", new Class[]{Object.class}));
    }

    private void addPathToNavigation(final Path p) {
        pathPopupButton.addItem(p.getAbsolute());
        pathPopupButton.lastItem().setRepresentedObject(p);
        pathPopupButton.lastItem().setImage(CDIconCache.instance().iconForPath(p, 16));
    }

    private void validateNavigationButtons() {
        if(!this.isMounted()) {
            pathPopupButton.removeAllItems();
        }
        else {
            pathPopupButton.removeAllItems();
            this.addPathToNavigation(workdir);
            Path p = workdir;
            while(!p.getParent().equals(p)) {
                this.addPathToNavigation(p);
                p = (Path) p.getParent();
            }
            this.addPathToNavigation(p);
        }

        this.navigationButton.setEnabled(this.isMounted() && this.getBackHistory().size() > 1,
                NAVIGATION_LEFT_SEGMENT_BUTTON);
        this.navigationButton.setEnabled(this.isMounted() && this.getForwardHistory().size() > 0,
                NAVIGATION_RIGHT_SEGMENT_BUTTON);
        this.upButton.setEnabled(this.isMounted() && !this.workdir().isRoot(),
                NAVIGATION_UP_SEGMENT_BUTTON);

        this.pathPopupButton.setEnabled(this.isMounted());
        final boolean enabled = this.isMounted() || this.getSelectedTabView() == TAB_BOOKMARKS;
        this.searchField.setEnabled(enabled);
        if(!enabled) {
            this.searchField.setStringValue("");
        }
    }

    public void pathPopupSelectionChanged(final Object sender) {
        final Path selected = (Path) pathPopupButton.itemAtIndex(
                pathPopupButton.indexOfSelectedItem()).representedObject();
        final Path previous = this.workdir();
        if(selected != null) {
            this.setWorkdir(selected);
            if(previous.getParent().equals(selected)) {
                this.setSelectedPath(previous);
            }
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
            this.background(new BrowserBackgroundAction(this) {
                public void run() {
                    unmount(false);
                }

                public void cleanup() {
                    session.getHost().setEncoding(encoding);
                    reloadButtonClicked(null);
                }

                public String getActivity() {
                    return NSBundle.localizedString("Disconnecting", "Status", "");
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

    public void toggleLogDrawer(final Object sender) {
        this.logDrawer.toggle(this);
    }

    // ----------------------------------------------------------
    // Status
    // ----------------------------------------------------------

    protected NSProgressIndicator spinner; // IBOutlet

    public void setSpinner(NSProgressIndicator spinner) {
        this.spinner = spinner;
        this.spinner.setDisplayedWhenStopped(false);
        this.spinner.setIndeterminate(true);
    }

    private NSTextField statusLabel; // IBOutlet

    public void setStatusLabel(NSTextField statusLabel) {
        this.statusLabel = statusLabel;
    }

    public void updateStatusLabel(String label) {
        if(null == label) {
            label = "";
            if(this.isMounted()) {
                if(this.isConnected()) {
                    label = this.getSelectedBrowserView().numberOfRows() + " " + NSBundle.localizedString("Files", "");
                }
                else {
                    label = NSBundle.localizedString("Disconnected", "Status", "");
                }
            }
        }
        final String status = label;
        // Update the status label at the bottom of the browser window
        statusLabel.setAttributedStringValue(new NSAttributedString(
                status,
                TRUNCATE_MIDDLE_ATTRIBUTES));
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
        if(session instanceof SSLSession) {
            final X509Certificate[] certificates = ((SSLSession) this.session).getTrustManager().getAcceptedIssuers();
            if(0 == certificates.length) {
                log.warn("No accepted certificates found");
                return;
            }
            Keychain.instance().displayCertificates(certificates);
        }
    }

    // ----------------------------------------------------------
    // Selector methods for the toolbar items
    // ----------------------------------------------------------

    public void quicklookButtonClicked(final Object sender) {
        if(QuickLook.isOpen()) {
            QuickLook.close();
        }
        else {
            final AbstractBrowserTableDelegate delegate
                    = (AbstractBrowserTableDelegate) this.getSelectedBrowserView().delegate();
            delegate.updateQuickLookSelection(this.getSelectedPaths());
        }
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
                case SWITCH_LIST_VIEW: {
                    this.workdir().invalidate();
                    break;
                }
                case SWITCH_OUTLINE_VIEW: {
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
            this.setWorkdir(this.workdir());
        }
    }

    /**
     * Open a new browser with the current selected folder as the working directory
     * @param sender
     */
    public void newBrowserButtonClicked(final Object sender) {
        Path selected = this.getSelectedPath();
        if(null == selected || !selected.attributes.isDirectory()) {
            selected = this.workdir();
        }
        CDBrowserController c = new CDBrowserController();
        c.cascade();
        c.window().makeKeyAndOrderFront(null);
        final Host host = new Host(this.getSession().getHost().getAsDictionary());
        host.setDefaultPath(selected.getAbsolute());
        c.mount(host);
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
    protected void duplicatePaths(final Map<Path, Path> selected, final boolean edit) {
        final Map<Path, Path> normalized = this.checkHierarchy(selected);
        this.checkOverwrite(normalized.values(), new BrowserBackgroundAction(this) {
            public void run() {
                Iterator<Path> sourcesIter = normalized.keySet().iterator();
                Iterator<Path> destinationsIter = normalized.values().iterator();
                while(sourcesIter.hasNext()) {
                    if(this.isCanceled()) {
                        break;
                    }
                    final Path source = sourcesIter.next();
                    final Path destination = destinationsIter.next();
                    source.copy(destination);
                    source.getParent().invalidate();
                    destination.getParent().invalidate();
                    if(!isConnected()) {
                        break;
                    }
                }
            }

            public void cleanup() {
                for(Iterator<Path> iter = normalized.values().iterator(); iter.hasNext();) {
                    Path duplicate = iter.next();
                    if(edit) {
                        Editor editor = EditorFactory.createEditor(CDBrowserController.this, duplicate.getLocal(),
                                duplicate);
                        editor.open();
                    }
                    if(duplicate.getName().charAt(0) == '.') {
                        setShowHiddenFiles(true);
                    }
                }
                reloadData(new ArrayList<Path>(normalized.values()));
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
    protected void renamePaths(final Map<Path, Path> selected) {
        final Map<Path, Path> normalized = this.checkHierarchy(selected);
        this.checkMove(normalized.values(), new BrowserBackgroundAction(this) {
            public void run() {
                Iterator<Path> originalIterator = normalized.keySet().iterator();
                Iterator<Path> renamedIterator = normalized.values().iterator();
                while(originalIterator.hasNext()) {
                    if(this.isCanceled()) {
                        break;
                    }
                    final Path original = originalIterator.next();
                    original.getParent().invalidate();
                    original.rename(renamedIterator.next());
                    original.getParent().invalidate();
                    if(!isConnected()) {
                        break;
                    }
                }
            }

            public String getActivity() {
                return MessageFormat.format(NSBundle.localizedString("Renaming {0}", "Status", ""), "");
            }

            public void cleanup() {
                reloadData(new ArrayList<Path>(normalized.values()));
            }
        });
    }

    /**
     * Displays a warning dialog about already existing files
     *
     * @param selected The files to check for existance
     */
    private void checkOverwrite(final java.util.Collection<Path> selected, final BackgroundAction action) {
        if(selected.size() > 0) {
            StringBuffer alertText = new StringBuffer(
                    NSBundle.localizedString("A file with the same name already exists. Do you want to replace the existing file?", ""));
            int i = 0;
            Iterator<Path> iter = null;
            boolean alert = false;
            for(iter = selected.iterator(); i < 10 && iter.hasNext();) {
                Path item = iter.next();
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
                c.beginSheet();
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
    private void checkMove(final java.util.Collection<Path> selected, final BackgroundAction action) {
        if(selected.size() > 0) {
            if(Preferences.instance().getBoolean("browser.confirmMove")) {
                StringBuffer alertText = new StringBuffer(
                        NSBundle.localizedString("Do you want to move the selected files?", ""));
                int i = 0;
                Iterator<Path> iter = null;
                for(iter = selected.iterator(); i < 10 && iter.hasNext();) {
                    Path item = iter.next();
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
                c.beginSheet();
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
    private Map<Path, Path> checkHierarchy(final Map<Path, Path> selected) {
        final Map<Path, Path> normalized = new HashMap<Path, Path>();
        Iterator<Path> sourcesIter = selected.keySet().iterator();
        Iterator<Path> destinationsIter = selected.values().iterator();
        while(sourcesIter.hasNext()) {
            Path f = sourcesIter.next();
            Path r = destinationsIter.next();
            boolean duplicate = false;
            for(Iterator<Path> normalizedIter = normalized.keySet().iterator(); normalizedIter.hasNext();) {
                Path n = normalizedIter.next();
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
    protected List<Path> checkHierarchy(final List<Path> selected) {
        final List<Path> normalized = new Collection<Path>();
        for(Iterator<Path> iter = selected.iterator(); iter.hasNext();) {
            Path f = iter.next();
            boolean duplicate = false;
            for(Iterator<Path> normalizedIter = normalized.iterator(); normalizedIter.hasNext();) {
                Path n = normalizedIter.next();
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
    public void deletePaths(final List<Path> selected) {
        final List<Path> normalized = this.checkHierarchy(selected);
        if(normalized.size() > 0) {
            StringBuffer alertText =
                    new StringBuffer(NSBundle.localizedString("Really delete the following files? This cannot be undone.", "Confirm deleting files."));
            int i = 0;
            Iterator<Path> iter = null;
            for(iter = normalized.iterator(); i < 10 && iter.hasNext();) {
                alertText.append("\n" + Character.toString('\u2022') + " " + iter.next().getName());
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
            c.beginSheet();
        }
    }

    private void deletePathsImpl(final List<Path> files) {
        this.background(new BrowserBackgroundAction(this) {
            public void run() {
                for(Iterator<Path> iter = files.iterator(); iter.hasNext();) {
                    if(this.isCanceled()) {
                        break;
                    }
                    Path f = iter.next();
                    f.delete();
                    f.getParent().invalidate();
                    if(!isConnected()) {
                        break;
                    }
                }
            }

            public String getActivity() {
                return MessageFormat.format(NSBundle.localizedString("Deleting {0}", "Status", ""), "");
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
        controller.beginSheet();
    }

    public void createFileButtonClicked(final Object sender) {
        CDSheetController controller = new CDCreateFileController(this);
        controller.beginSheet();
    }

    public void duplicateFileButtonClicked(final Object sender) {
        CDSheetController controller = new CDDuplicateFileController(this);
        controller.beginSheet();
    }

    public void createFolderButtonClicked(final Object sender) {
        CDSheetController controller = new CDFolderController(this);
        controller.beginSheet();
    }

    public void renameFileButtonClicked(final Object sender) {
        final NSTableView browser = this.getSelectedBrowserView();
        browser.editLocation(
                browser.columnWithIdentifier(CDBrowserTableDataSource.FILENAME_COLUMN),
                browser.selectedRow(), null, true);
        //editColumn:mBrowserEditingColumn row:mBrowserEditingRow withEvent:nil select:YES
    }

    public void sendCustomCommandClicked(final Object sender) {
        CDSheetController controller = new CDCommandController(this, this.session);
        controller.beginSheet();
    }

    public void editMenuClicked(final NSMenuItem sender) {
        for(Path selected: this.getSelectedPaths()) {
            String identifier = EditorFactory.SUPPORTED_ODB_EDITORS.get(sender.title());
            if(identifier != null) {
                Editor editor = EditorFactory.createEditor(
                        this, identifier.toString(), selected);
                editor.open();
            }
        }
    }

    public void editButtonClicked(final Object sender) {
        for(Path selected: this.getSelectedPaths()) {
            Editor editor = EditorFactory.createEditor(this, selected.getLocal(), selected);
            editor.open();
        }
    }

    public void openBrowserButtonClicked(final Object sender) {
        try {
            String url = this.session.getHost().getWebURL();
            final String protocol = "http://";
            if(!url.startsWith(protocol)) {
                url = protocol + url;
            }
            String selected;
            final String parent = this.session.getHost().getDefaultPath();
            if(this.getSelectionCount() == 1) {
                selected = this.getSelectedPath().getAbsolute();
            }
            else {
                selected = this.workdir().getAbsolute();
            }
            if(selected.startsWith(parent)) {
                selected = selected.substring(parent.length());
            }
            NSWorkspace.sharedWorkspace().openURL(
                    new URL(url + selected)
            );
        }
        catch(MalformedURLException e) {
            log.error("Cannot open in web browser:" + e.getMessage());
        }
    }

    private CDInfoController inspector = null;

    public void infoButtonClicked(final Object sender) {
        if(this.getSelectionCount() > 0) {
            final List<Path> selected = this.getSelectedPaths();
            this.background(new BrowserBackgroundAction(this) {
                public void run() {
                    for(Iterator<Path> iter = selected.iterator(); iter.hasNext();) {
                        if(this.isCanceled()) {
                            break;
                        }
                        final Path selected = iter.next();
                        if(selected.attributes.getPermission() == null) {
                            selected.readPermission();
                        }
                    }
                }

                public void cleanup() {
                    if(Preferences.instance().getBoolean("browser.info.isInspector")) {
                        if(null == inspector || null == inspector.window()) {
                            inspector = CDInfoController.Factory.create(CDBrowserController.this, selected);
                        }
                        else {
                            inspector.setFiles(selected);
                        }
                        inspector.window().makeKeyAndOrderFront(null);
                    }
                    else {
                        CDInfoController c = CDInfoController.Factory.create(CDBrowserController.this, selected);
                        c.window().makeKeyAndOrderFront(null);
                    }
                }
            });
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


    public void downloadToPanelDidEnd(NSOpenPanel sheet, int returncode, final Object contextInfo) {
        sheet.close();
        if(returncode == CDSheetCallback.DEFAULT_OPTION) {
            final Session session = this.getTransferSession();
            final List<Path> roots = new Collection<Path>();
            for(Path selected: this.getSelectedPaths()) {
                Path path = PathFactory.createPath(session, selected.getAsDictionary());
                path.setLocal(new Local(sheet.filename(), path.getLocal().getName()));
                roots.add(path);
            }
            final Transfer q = new DownloadTransfer(roots);
            this.transfer(q);
        }
        lastSelectedDownloadDirectory = sheet.filename();
        downloadToPanel = null;
    }

    private NSSavePanel downloadAsPanel;

    public void downloadAsButtonClicked(final Object sender) {
        final Session session = this.getTransferSession();
        for(Path selected: this.getSelectedPaths()) {
            Path path = PathFactory.createPath(session, selected.getAsDictionary());
            downloadAsPanel = NSSavePanel.savePanel();
            downloadAsPanel.setMessage(NSBundle.localizedString("Download the selected file to...", ""));
            downloadAsPanel.setNameFieldLabel(NSBundle.localizedString("Download As:", ""));
            downloadAsPanel.setPrompt(NSBundle.localizedString("Download", ""));
            downloadAsPanel.setTitle(NSBundle.localizedString("Download", ""));
            downloadAsPanel.setCanCreateDirectories(true);
            downloadAsPanel.beginSheetForDirectory(null,
                    path.getLocal().getName(),
                    this.window,
                    this,
                    new NSSelector("downloadAsPanelDidEnd", new Class[]{NSOpenPanel.class, int.class, Object.class}),
                    path);
        }
    }

    public void downloadAsPanelDidEnd(NSSavePanel sheet, int returncode, final Path contextInfo) {
        sheet.close();
        if(returncode == CDSheetCallback.DEFAULT_OPTION) {
            String filename;
            if((filename = sheet.filename()) != null) {
                Path path = contextInfo;
                path.setLocal(new Local(filename));
                final Transfer q = new DownloadTransfer(path);
                this.transfer(q);
            }
        }
        downloadAsPanel = null;
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
                + NSBundle.localizedString("with", "Synchronize <file> with <file>"));
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

    public void syncPanelDidEnd(NSOpenPanel sheet, int returncode, final Path selection) {
        sheet.close();
        if(returncode == CDSheetCallback.DEFAULT_OPTION) {
            if(sheet.filenames().count() > 0) {
                Path root = PathFactory.createPath(this.getTransferSession(), selection.getAsDictionary());
                root.setLocal(new Local(sheet.filenames().lastObject().toString()));
                final Transfer q = new SyncTransfer(root);
                this.transfer(q, selection);
            }
        }
        syncPanel = null;
    }

    public void downloadButtonClicked(final Object sender) {
        final Session session = this.getTransferSession();
        final List<Path> roots = new Collection<Path>();
        for(Path selected: this.getSelectedPaths()) {
            Path path = PathFactory.createPath(session, selected.getAsDictionary());
            path.setLocal(null);
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
            Path destination = this.getSelectedPath();
            if(null == destination) {
                destination = this.workdir();
            }
            else if(!destination.attributes.isDirectory()) {
                destination = (Path)destination.getParent();
            }
            // selected files on the local filesystem
            NSArray selected = sheet.filenames();
            java.util.Enumeration iterator = selected.objectEnumerator();
            final Session session = this.getTransferSession();
            final List<Path> roots = new Collection<Path>();
            while(iterator.hasMoreElements()) {
                roots.add(PathFactory.createPath(session,
                        destination.getAbsolute(),
                        new Local((String) iterator.nextElement())));
            }
            final Transfer q = new UploadTransfer(roots);
            this.transfer(q, destination);
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
        final Host h = new Host(this.session.getHost().getAsDictionary());
        // Copy credentials of the browser
        h.setCredentials(this.session.getHost().getCredentials());
        return SessionFactory.createSession(h);
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
                        CDMainApplication.invoke(new WindowMainAction(CDBrowserController.this) {
                            public void run() {
                                reloadData(true);
                            }

                            public boolean isValid() {
                                return super.isValid() && CDBrowserController.this.isConnected();
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
        this.transfer(transfer, transfer.getSession().getMaxConnections() == 1);
    }

    /**
     * @param transfer
     * @param useBrowserConnection
     */
    protected void transfer(final Transfer transfer, final boolean useBrowserConnection) {
        this.transfer(transfer, useBrowserConnection, CDTransferPrompt.create(this, transfer));
    }

    protected void transfer(final Transfer transfer, final boolean useBrowserConnection, final TransferPrompt prompt) {
        if(useBrowserConnection) {
            final Speedometer meter = new Speedometer(transfer);
            final TransferListener l;
            final long delay = 0;
            final long period = 500; //in milliseconds
            transfer.addListener(l = new TransferAdapter() {
                /**
                 * Timer to update the progress indicator
                 */
                private Timer progressTimer;

                public void willTransferPath(Path path) {
                    meter.reset();
                    progressTimer = new Timer();
                    progressTimer.scheduleAtFixedRate(new TimerTask() {
                        public void run() {
                            CDMainApplication.invoke(new WindowMainAction(CDBrowserController.this) {
                                public void run() {
                                    CDBrowserController.this.updateStatusLabel(meter.getProgress());
                                }
                            });
                        }
                    }, delay, period);
                }

                public void didTransferPath(Path path) {
                    progressTimer.cancel();
                    meter.reset();
                }

                public void bandwidthChanged(BandwidthThrottle bandwidth) {
                    meter.reset();
                }
            });
            this.addListener(new CDWindowListener() {
                public void windowWillClose() {
                    transfer.removeListener(l);
                }
            });
            this.background(new BrowserBackgroundAction(this) {
                public void run() {
                    TransferOptions options = new TransferOptions();
                    options.closeSession = false;
                    transfer.start(prompt, options);
                }

                public void cancel() {
                    transfer.cancel();
                    super.cancel();
                }

                public void cleanup() {
                    updateStatusLabel(null);
                }

                public String getActivity() {
                    return transfer.getName();
                }
            });
        }
        else {
            CDTransferController.instance().startTransfer(transfer);
        }
    }

    public void insideButtonClicked(final Object sender) {
        final Path selected = this.getSelectedPath(); //last row selected
        if(null == selected) {
            return;
        }
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

    public void connectButtonClicked(final Object sender) {
        final CDSheetController controller = CDConnectionController.instance(this);
        this.addListener(new CDWindowListener() {
            public void windowWillClose() {
                controller.invalidate();
            }
        });
        controller.beginSheet();
    }

    public void interruptButtonClicked(final Object sender) {
        // Remove all pending actions
        BackgroundAction[] l = (BackgroundAction[]) BackgroundActionRegistry.instance().toArray(
                new BackgroundAction[BackgroundActionRegistry.instance().size()]);
        for(int i = 0; i < l.length; i++) {
            l[i].cancel();
        }
        // Interrupt any pending operation by forcefully closing the socket
        this.interrupt();
    }

    public void disconnectButtonClicked(final Object sender) {
        if(this.isActivityRunning()) {
            this.interruptButtonClicked(sender);
        }
        else {
            this.disconnect();
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
        final List<Path> roots = new Collection<Path>();
        for(Path selected: this.getSelectedPaths()) {
            roots.add(selected);
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

    public void paste(final Object sender) {
        final NSPasteboard pboard = NSPasteboard.pasteboardWithName(CDPasteboards.TransferPasteboard);
        if(pboard.availableTypeFromArray(new NSArray(CDPasteboards.TransferPasteboardType)) != null) {
            Object o = pboard.propertyListForType(CDPasteboards.TransferPasteboardType);// get the data from paste board
            if(o != null) {
                final Map<Path, Path> files = new HashMap<Path, Path>();
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
                    for(Iterator<Path> iter = q.getRoots().iterator(); iter.hasNext();) {
                        final Path next = iter.next();
                        Path current = PathFactory.createPath(getSession(),
                                next.getAbsolute(), next.attributes.getType());
                        Path renamed = PathFactory.createPath(getSession(),
                                parent.getAbsolute(), current.getName(), next.attributes.getType());
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
                final List<Path> roots = new Collection<Path>();
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
        final StringBuffer url = new StringBuffer();
        if(this.getSelectionCount() > 0) {
            for(Iterator<Path> iter = this.getSelectedPaths().iterator(); iter.hasNext();) {
                url.append(iter.next().toURL());
                if(iter.hasNext()) {
                    url.append("\n");
                }
            }
        }
        else {
            url.append(this.workdir().toURL());
        }
        NSPasteboard pboard = NSPasteboard.generalPasteboard();
        pboard.declareTypes(new NSArray(NSPasteboard.StringPboardType), null);
        if(!pboard.setStringForType(url.toString(), NSPasteboard.StringPboardType)) {
            log.error("Error writing URL to NSPasteboard.StringPboardType.");
        }
    }

    public void openTerminalButtonClicked(final Object sender) {
        final boolean identity = this.getSession().getHost().getCredentials().usesPublicKeyAuthentication();
        String workdir = null;
        if(this.getSelectionCount() == 1) {
            Path selected = this.getSelectedPath();
            if(selected.attributes.isDirectory()) {
                workdir = selected.getAbsolute();
            }
        }
        if(null == workdir) {
            workdir = this.workdir.getAbsolute();
        }
        final String command
                = "tell application \"Terminal\"\n"
                + "do script \"ssh -t "
                + (identity ? "-i " + new Local(this.getSession().getHost().getCredentials().getPrivateKeyFile()).getAbsolute() : "")
                + " "
                + this.getSession().getHost().getCredentials().getUsername()
                + "@"
                + this.getSession().getHost().getHostname()
                + " "
                + "-p " + this.getSession().getHost().getPort()
                + " "
                + "\\\"cd " + workdir + " && exec \\\\$SHELL\\\"\""
                + "\n"
                + "end tell";
        NSAppleScript as = new NSAppleScript(command);
        final NSMutableDictionary result = new NSMutableDictionary();
        as.execute(result);
        if(!(result.count() == 0)) {
            final Enumeration errors = result.keyEnumerator();
            while(errors.hasMoreElements()) {
                log.error(result.valueForKey(errors.nextElement().toString()));
            }
        }
        NSWorkspace.sharedWorkspace().launchApplication(
                NSWorkspace.sharedWorkspace().absolutePathForAppBundleWithIdentifier("com.apple.Terminal")
        );
    }

    /**
     * @return true if there is any network activity running in the background
     */
    public boolean isActivityRunning() {
        final BackgroundAction current = BackgroundActionRegistry.instance().getCurrent();
        if(null == current) {
            return false;
        }
        if(current instanceof BrowserBackgroundAction) {
            return ((BrowserBackgroundAction) current).getController() == this;
        }
        return false;
    }

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
     * @param directory The new working directory to display or null to detach any working directory from the browser
     */
    public void setWorkdir(final Path directory) {
        log.debug("setWorkdir:" + directory);
        if(null == directory) {
            // Clear the browser view if no working directory is given
            this.workdir = null;
            this.validateNavigationButtons();
            this.reloadData(false);
            return;
        }
        this.background(new BrowserBackgroundAction(this) {
            public String getActivity() {
                return MessageFormat.format(NSBundle.localizedString("Listing directory {0}", "Status", ""),
                        directory.getName());
            }

            public void run() {
                if(directory.isCached()) {
                    //Reset the readable attribute
                    directory.childs().attributes().setReadable(true);
                }
                // Get the directory listing in the background
                directory.childs();
                if(directory.childs().attributes().isReadable()) {
                    // Update the working directory if listing is successful
                    workdir = directory;
                    // Update the current working directory
                    addPathToHistory(workdir);
                }
            }

            public void cleanup() {
                // Remove any custom file filter
                setPathFilter(null);

                if(Preferences.instance().getBoolean("browser.closeDrawer")) {
                    // Change to last selected browser view
                    browserSwitchClicked(Preferences.instance().getInteger("browser.view"));
                }

                validateNavigationButtons();

                // Mark the browser data source as dirty
                reloadData(false);
            }
        });
    }


    /**
     * Keeps a ordered backward history of previously visited paths
     */
    private List<Path> backHistory = new Collection<Path>();

    /**
     * Keeps a ordered forward history of previously visited paths
     */
    private List<Path> forwardHistory = new Collection<Path>();

    /**
     * @param p
     */
    public void addPathToHistory(final Path p) {
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
            Path p = backHistory.get(size - 2);
            //delete the fetched path - otherwise we produce a loop
            backHistory.remove(size - 1);
            backHistory.remove(size - 2);
            return p;
        }
        else if(1 == size) {
            forwardHistory.add(backHistory.get(size - 1));
            return backHistory.get(size - 1);
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
            Path p = forwardHistory.get(size - 1);
            forwardHistory.remove(size - 1);
            return p;
        }
        return null;
    }

    /**
     * @return The ordered array of prevoiusly visited directories
     */
    public List<Path> getBackHistory() {
        return backHistory;
    }

    /**
     * Remove all entries from the back path history
     */
    public void clearBackHistory() {
        backHistory.clear();
    }

    /**
     * @return The ordered array of prevoiusly visited directories
     */
    public List<Path> getForwardHistory() {
        return forwardHistory;
    }

    /**
     * Remove all entries from the forward path history
     */
    public void clearForwardHistory() {
        forwardHistory.clear();
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
        this.session.setLoginController(new CDLoginController(this));
        this.setWorkdir(null);
        this.setEncoding(this.session.getEncoding());
        this.session.addProgressListener(new ProgressListener() {
            public void message(final String message) {
                CDMainApplication.invoke(new WindowMainAction(CDBrowserController.this) {
                    public void run() {
                        updateStatusLabel(message);
                    }
                });
            }
        });
        session.addConnectionListener(listener = new ConnectionAdapter() {
            public void connectionWillOpen() {
                CDMainApplication.invoke(new WindowMainAction(CDBrowserController.this) {
                    public void run() {
                        bookmarkTable.setNeedsDisplay();
                        if(StringUtils.hasText(host.getCredentials().getUsername())) {
                            window.setTitle(host.getProtocol().getScheme() + ":" + host.getCredentials().getUsername()
                                    + "@" + host.getHostname());
                        }
                        else {
                            window.setTitle(host.getProtocol().getScheme() + ":"
                                    + host.getHostname());
                        }
                        window.setRepresentedFilename("");
                    }
                });
            }

            public void connectionDidOpen() {
                CDMainApplication.invoke(new WindowMainAction(CDBrowserController.this) {
                    public void run() {
                        getSelectedBrowserView().setNeedsDisplay();
                        bookmarkTable.setNeedsDisplay();

                        Growl.instance().notify("Connection opened", host.getHostname());

                        HistoryCollection.defaultCollection().add(host);

                        // Set the window title
                        window.setRepresentedFilename(host.getFile().getAbsolute());

                        if(Preferences.instance().getBoolean("browser.confirmDisconnect")) {
                            window.setDocumentEdited(true);
                        }
                        securityLabel.setImage(session.isSecure() ? NSImage.imageNamed("locked.tiff")
                                : NSImage.imageNamed("unlocked.tiff"));
                        securityLabel.setEnabled(session instanceof SSLSession);
                    }
                });
            }

            public void connectionDidClose() {
                CDMainApplication.invoke(new WindowMainAction(CDBrowserController.this) {
                    public void run() {
                        getSelectedBrowserView().setNeedsDisplay();
                        bookmarkTable.setNeedsDisplay();

                        if(!isMounted()) {
                            window.setTitle((String) NSBundle.mainBundle().infoDictionary().objectForKey("CFBundleName"));
                            window.setRepresentedFilename("");
                        }
                        window.setDocumentEdited(false);

                        securityLabel.setImage(NSImage.imageNamed("unlocked.tiff"));
                        securityLabel.setEnabled(false);
                    }
                });
            }
        });
        transcript.clear();
        backHistory.clear();
        forwardHistory.clear();
        session.addTranscriptListener(new TranscriptListener() {
            public void log(final boolean request, final String message) {
                if(logDrawer.state() == NSDrawer.OpenState) {
                    CDMainApplication.invoke(new WindowMainAction(CDBrowserController.this) {
                        public void run() {
                            transcript.log(request, message);
                        }
                    });
                }
            }
        });
        return session;
    }

    /**
     *
     */
    private Session session;

    /**
     * @param h
     * @return The session to be used for any further operations
     */
    public void mount(final Host host) {
        log.debug("mount:" + host);
        this.unmount(new Runnable() {
            public void run() {
                // The browser has no session, we are allowed to proceed
                // Initialize the browser with the new session attaching all listeners
                final Session session = init(host);

                background(new BrowserBackgroundAction(CDBrowserController.this) {
                    private Path mount;

                    public void run() {
                        // Mount this session
                        mount = session.mount();
                    }

                    public void cleanup() {
                        // Set the working directory
                        setWorkdir(mount);
                    }

                    public String getActivity() {
                        return MessageFormat.format(NSBundle.localizedString("Mounting {0}", "Status", ""),
                                host.getHostname());
                    }
                });
            }
        });
    }

    /**
     * Will close the session but still display the current working directory without any confirmation
     * from the user
     *
     * @param forever The session won't be remounted in any case; will clear the cache
     */
    private void unmount(final boolean forever) {
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

    public boolean unmount() {
        return this.unmount(new Runnable() {
            public void run() {
                ;
            }
        });
    }

    /**
     * @param disconnected Callback after the session has been disconnected
     * @return True if the unmount process has finished, false if the user has to agree first
     *         to close the connection
     */
    public boolean unmount(final Runnable disconnected) {
        return this.unmount(new CDSheetCallback() {
            public void callback(int returncode) {
                if(returncode == DEFAULT_OPTION) {
                    unmountImpl(disconnected);
                }
            }
        }, disconnected);
    }

    /**
     * @param disconnected
     */
    private void unmountImpl(final Runnable disconnected) {
        if(this.isActivityRunning()) {
            this.interrupt();
        }
        this.background(new BrowserBackgroundAction(this) {
            public void run() {
                unmount(true);
            }

            public void cleanup() {
                disconnected.run();
            }

            public String getActivity() {
                return NSBundle.localizedString("Disconnecting", "Status", "");
            }
        });
    }

    /**
     * @param callback
     * @param disconnected
     * @return
     */
    public boolean unmount(final CDSheetCallback callback, final Runnable disconnected) {
        log.debug("unmount");
        if(this.isConnected() || this.isActivityRunning()) {
            if(Preferences.instance().getBoolean("browser.confirmDisconnect")) {
                // Defer the unmount to the callback function
                this.alert(NSAlertPanel.criticalAlertPanel(NSBundle.localizedString("Disconnect from", "Alert sheet title") + " " + this.session.getHost().getHostname(), //title
                        NSBundle.localizedString("The connection will be closed.", "Alert sheet text"), // message
                        NSBundle.localizedString("Disconnect", "Alert sheet default button"), // defaultbutton
                        NSBundle.localizedString("Cancel", "Alert sheet alternate button"), // alternate button
                        null //other button
                ), callback);
                // No unmount yet
                return false;
            }
            this.unmountImpl(disconnected);
            // Unmount in progress
            return true;
        }
        disconnected.run();
        // Unmount succeeded
        return true;
    }

    /**
     * Interrupt any operation in progress;
     * just closes the socket without any quit message sent to the server
     */
    private void interrupt() {
        if(this.hasSession()) {
            if(this.isActivityRunning()) {
                final BackgroundAction current = BackgroundActionRegistry.instance().getCurrent();
                if(null != current) {
                    current.cancel();
                }
            }
            this.background(new BrowserBackgroundAction(this) {
                public void run() {
                    if(hasSession()) {
                        // Aggressively close the connection to interrupt the current task
                        session.interrupt();
                    }
                }

                public void cleanup() {
                    ;
                }

                public String getActivity() {
                    return NSBundle.localizedString("Disconnecting", "Status", "");
                }

                public int retry() {
                    return 0;
                }

                private final Object lock = new Object();

                public Object lock() {
                    // No synchronization with other tasks
                    return lock;
                }
            });
        }
    }

    /**
     * Unmount this session
     */
    private void disconnect() {
        this.background(new BrowserBackgroundAction(this) {
            public void run() {
                unmount(false);
            }

            public void cleanup() {
                ;
            }

            public String getActivity() {
                return NSBundle.localizedString("Disconnecting", "Status", "");
            }
        });
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
    public static int applicationShouldTerminate(final NSApplication app) {
        // Determine if there are any open connections
        NSArray windows = app.windows();
        int count = windows.count();
        // Determine if there are any open connections
        while(0 != count--) {
            final NSWindow window = (NSWindow) windows.objectAtIndex(count);
            final CDBrowserController controller = CDBrowserController.controllerForWindow(window);
            if(null != controller) {
                if(!controller.unmount(new CDSheetCallback() {
                    public void callback(final int returncode) {
                        if(returncode == DEFAULT_OPTION) { //Disconnect
                            window.close();
                            if(NSApplication.TerminateNow == CDBrowserController.applicationShouldTerminate(app)) {
                                app.terminate(null);
                            }
                        }
                        if(returncode == ALTERNATE_OPTION) { //Cancel
                            app.replyToApplicationShouldTerminate(false);
                        }
                    }
                }, new Runnable() {
                    public void run() {
                        ;
                    }
                })) {
                    return NSApplication.TerminateLater;
                }
            }
        }
        return NSApplication.TerminateNow;
    }

    public boolean windowShouldClose(final NSWindow sender) {
        return this.unmount(new Runnable() {
            public void run() {
                sender.close();
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
                            if(q.numberOfRoots() == 1) {
                                item.setTitle(NSBundle.localizedString("Paste", "Menu item") + " \""
                                        + q.getRoot().getName() + "\"");
                            }
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
            if(this.isMounted()) {
                Path selected = this.getSelectedPath();
                if(null == selected) {
                    item.setTitle(NSBundle.localizedString("Cut", "Menu item")
                            + " " + this.getSelectionCount() + " " +
                            NSBundle.localizedString("files", ""));
                }
                else {
                    item.setTitle(NSBundle.localizedString("Cut", "Menu item") + " \"" + selected.getName() + "\"");
                }
            }
            else {
                item.setTitle(NSBundle.localizedString("Cut", "Menu item"));
            }
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
            return !isActivityRunning();
        }
        if(identifier.equals("connectBookmarkButtonClicked:")) {
            return bookmarkTable.numberOfSelectedRows() == 1;
        }
        if(identifier.equals("deleteBookmarkButtonClicked:")) {
            return bookmarkTable.selectedRow() != -1;
        }
        if(identifier.equals("editBookmarkButtonClicked:")) {
            return bookmarkModel.isEditable() && bookmarkTable.numberOfSelectedRows() == 1;
        }
        if(identifier.equals("editButtonClicked:")) {
            if(this.isMounted() && this.getSelectionCount() > 0) {
                String editor = EditorFactory.getSelectedEditor();
                if(null == editor) {
                    return false;
                }
                for(Path selected: this.getSelectedPaths()) {
                    if(!this.isEditable(selected)) {
                        return false;
                    }
                }
                return true;
            }
            return false;
        }
        if(identifier.equals("editMenuClicked:")) {
            if(this.isMounted() && this.getSelectionCount() > 0) {
                for(Path selected: this.getSelectedPaths()) {
                    if(!this.isEditable(selected)) {
                        return false;
                    }
                }
                return true;
            }
            return false;
        }
        if(identifier.equals("quicklookButtonClicked:")) {
            return QuickLook.isAvailable() && this.isMounted() && this.getSelectionCount() > 0;
        }
        if(identifier.equals("openBrowserButtonClicked:")) {
            return this.isMounted();
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
            return this.isMounted() && this.workdir().isMkdirSupported();
        }
        if(identifier.equals("createFileButtonClicked:")) {
            return this.isMounted();
        }
        if(identifier.equals("duplicateFileButtonClicked:")) {
            if(this.isMounted() && this.getSelectionCount() == 1) {
                final Path selected = this.getSelectedPath();
                if(null == selected) {
                    return false;
                }
                return selected.attributes.isFile();
            }
            return false;
        }
        if(identifier.equals("renameFileButtonClicked:")) {
            if(this.isMounted() && this.getSelectionCount() == 1) {
                final Path selected = this.getSelectedPath();
                if(null == selected) {
                    return false;
                }
                return true;
            }
            return false;
        }
        if(identifier.equals("deleteFileButtonClicked:")) {
            return this.isMounted() && this.getSelectionCount() > 0;
        }
        if(identifier.equals("reloadButtonClicked:")) {
            return this.isMounted();
        }
        if(identifier.equals("newBrowserButtonClicked:")) {
            return this.isMounted();
        }
        if(identifier.equals("uploadButtonClicked:")) {
            return this.isMounted();
        }
        if(identifier.equals("syncButtonClicked:")) {
            return this.isMounted();
        }
        if(identifier.equals("downloadAsButtonClicked:")) {
            if(this.isMounted() && this.getSelectionCount() == 1) {
                final Path selected = this.getSelectedPath();
                if(null == selected) {
                    return false;
                }
                return !selected.attributes.isVolume();
            }
            return false;
        }
        if(identifier.equals("downloadToButtonClicked:") || identifier.equals("downloadButtonClicked:")) {
            if(this.isMounted() && this.getSelectionCount() > 0) {
                final Path selected = this.getSelectedPath();
                if(null == selected) {
                    return false;
                }
                return !selected.attributes.isVolume();
            }
            return false;
        }
        if(identifier.equals("insideButtonClicked:")) {
            return this.isMounted() && this.getSelectionCount() > 0;
        }
        if(identifier.equals("upButtonClicked:")) {
            return this.isMounted() && !this.workdir().isRoot();
        }
        if(identifier.equals("backButtonClicked:")) {
            return this.isMounted() && this.getBackHistory().size() > 1;
        }
        if(identifier.equals("forwardButtonClicked:")) {
            return this.isMounted() && this.getForwardHistory().size() > 0;
        }
        if(identifier.equals("copyURLButtonClicked:")) {
            return this.isMounted();
        }
        if(identifier.equals("printDocument:")) {
            return this.isMounted();
        }
        if(identifier.equals("disconnectButtonClicked:")) {
            if(!this.isConnected()) {
                return this.isActivityRunning();
            }
            return this.isConnected();
        }
        if(identifier.equals("interruptButtonClicked:")) {
            return this.isActivityRunning();
        }
        if(identifier.equals("gotofolderButtonClicked:")) {
            return this.isMounted();
        }
        if(identifier.equals("openTerminalButtonClicked:")) {
            return this.isMounted() && this.getSession() instanceof SFTPSession;
        }
        return true; // by default everything is enabled
    }

    // ----------------------------------------------------------
    // Toolbar Delegate
    // ----------------------------------------------------------

    public boolean validateToolbarItem(NSToolbarItem item) {
        String identifier = item.action().name();
        if(identifier.equals("editButtonClicked:")) {
            final String selectedEditor = EditorFactory.getSelectedEditor();
            if(null == selectedEditor) {
                item.setImage(NSImage.imageNamed("pencil.tiff"));
            }
            else {
                item.setImage(NSWorkspace.sharedWorkspace().iconForFile(
                        NSWorkspace.sharedWorkspace().absolutePathForAppBundleWithIdentifier(selectedEditor))
                );
            }
        }
        if(identifier.equals("disconnectButtonClicked:")) {
            if(this.isActivityRunning()) {
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
    private static final String TOOLBAR_TRANSFERS = "Transfers";
    private static final String TOOLBAR_QUICK_CONNECT = "Quick Connect";
    //    private static final String TOOLBAR_NAVIGATION = "Location";
    private static final String TOOLBAR_TOOLS = "Tools";
    //    private static final String TOOLBAR_HISTORY = "History";
    private static final String TOOLBAR_REFRESH = "Refresh";
    private static final String TOOLBAR_ENCODING = "Encoding";
    private static final String TOOLBAR_SYNCHRONIZE = "Synchronize";
    private static final String TOOLBAR_DOWNLOAD = "Download";
    private static final String TOOLBAR_UPLOAD = "Upload";
    private static final String TOOLBAR_EDIT = "Edit";
    private static final String TOOLBAR_DELETE = "Delete";
    private static final String TOOLBAR_NEW_FOLDER = "New Folder";
    private static final String TOOLBAR_NEW_BOOKMARK = "New Bookmark";
    private static final String TOOLBAR_GET_INFO = "Get Info";
    private static final String TOOLBAR_WEBVIEW = "Open";
    private static final String TOOLBAR_DISCONNECT = "Disconnect";
    private static final String TOOLBAR_INTERRUPT = "Stop";
    private static final String TOOLBAR_GO_TO_FOLDER = "Go to Folder";
    private static final String TOOLBAR_TERMINAL = "Terminal";

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
        if(itemIdentifier.equals(TOOLBAR_TRANSFERS)) {
            item.setLabel(NSBundle.localizedString(TOOLBAR_TRANSFERS, "Toolbar item"));
            item.setPaletteLabel(NSBundle.localizedString(TOOLBAR_TRANSFERS, "Toolbar item"));
            item.setToolTip(NSBundle.localizedString("Show Transfers window", "Toolbar item tooltip"));
            item.setImage(NSImage.imageNamed("queue.tiff"));
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
        if(itemIdentifier.equals(TOOLBAR_WEBVIEW)) {
            item.setLabel(NSBundle.localizedString(TOOLBAR_WEBVIEW, "Toolbar item"));
            item.setPaletteLabel(NSBundle.localizedString("Open in Web Browser", "Toolbar item"));
            item.setToolTip(NSBundle.localizedString("Open in Web Browser", "Toolbar item tooltip"));
            final String browser = URLSchemeHandlerConfiguration.instance().getDefaultHandlerForURLScheme("http");
            if(null == browser) {
                item.setEnabled(false);
                item.setImage(NSImage.imageNamed("notfound.tiff"));
            }
            else {
                String path = NSWorkspace.sharedWorkspace().absolutePathForAppBundleWithIdentifier(browser);
                if(null == path) {
                    item.setImage(NSImage.imageNamed("notfound.tiff"));
                }
                else {
                    item.setImage(NSWorkspace.sharedWorkspace().iconForFile(path));
                }
            }
            item.setTarget(this);
            item.setAction(new NSSelector("openBrowserButtonClicked", new Class[]{Object.class}));
            return item;
        }
        if(itemIdentifier.equals(TOOLBAR_EDIT)) {
            item.setLabel(NSBundle.localizedString(TOOLBAR_EDIT, "Toolbar item"));
            item.setPaletteLabel(NSBundle.localizedString(TOOLBAR_EDIT, "Toolbar item"));
            item.setToolTip(NSBundle.localizedString("Edit file in external editor", "Toolbar item tooltip"));
            item.setImage(NSImage.imageNamed("pencil.tiff"));
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
            item.setImage(CDIconCache.FOLDER_NEW_ICON);
            item.setTarget(this);
            item.setAction(new NSSelector("createFolderButtonClicked", new Class[]{Object.class}));
            return item;
        }
        if(itemIdentifier.equals(TOOLBAR_NEW_BOOKMARK)) {
            item.setLabel(NSBundle.localizedString(TOOLBAR_NEW_BOOKMARK, "Toolbar item"));
            item.setPaletteLabel(NSBundle.localizedString(TOOLBAR_NEW_BOOKMARK, "Toolbar item"));
            item.setToolTip(NSBundle.localizedString("New Bookmark", "Toolbar item tooltip"));
            item.setImage(NSImage.imageNamed("bookmark40.tiff"));
            item.setTarget(this);
            item.setAction(new NSSelector("addBookmarkButtonClicked", new Class[]{Object.class}));
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
        if(itemIdentifier.equals(TOOLBAR_TERMINAL)) {
            final String t = NSWorkspace.sharedWorkspace().absolutePathForAppBundleWithIdentifier("com.apple.Terminal");
            item.setLabel(NSPathUtilities.displayNameAtPath(t));
            item.setPaletteLabel(NSPathUtilities.displayNameAtPath(t));
            item.setImage(CDIconCache.instance().iconForPath(new Local(t), 128));
            item.setTarget(this);
            item.setAction(new NSSelector("openTerminalButtonClicked", new Class[]{Object.class}));
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
                TOOLBAR_NEW_BOOKMARK,
                TOOLBAR_GET_INFO,
                TOOLBAR_WEBVIEW,
                TOOLBAR_TERMINAL,
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
        super.invalidate();
    }
}