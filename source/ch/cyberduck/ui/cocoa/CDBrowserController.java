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

import java.io.File;
import java.util.*;

import org.apache.log4j.Logger;

import ch.cyberduck.core.*;
import ch.cyberduck.core.Queue;
import ch.cyberduck.core.ftp.FTPSession;
import ch.cyberduck.ui.cocoa.odb.Editor;

/**
 * @version $Id$
 */
public class CDBrowserController extends CDWindowController implements Observer {
    private static Logger log = Logger.getLogger(CDBrowserController.class);

    private static final File HISTORY_FOLDER = new File(
            NSPathUtilities.stringByExpandingTildeInPath(
                    "~/Library/Application Support/Cyberduck/History"));

    static {
        HISTORY_FOLDER.mkdirs();
    }

    /**
     * Keep references of controller objects because otherweise they get garbage collected
     * if not referenced here.
     */
    private static NSMutableArray instances = new NSMutableArray();

    // ----------------------------------------------------------
    // Applescriptability
    // ----------------------------------------------------------

    public NSScriptObjectSpecifier objectSpecifier() {
        log.debug("objectSpecifier");
        NSArray orderedDocs = (NSArray) NSKeyValue.valueForKey(NSApplication.sharedApplication(), "orderedBrowsers");
        int index = orderedDocs.indexOfObject(this);
        if ((index >= 0) && (index < orderedDocs.count())) {
            NSScriptClassDescription desc = (NSScriptClassDescription) NSScriptClassDescription.classDescriptionForClass(NSApplication.class);
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
        Host host = null;
        Object portObj = args.objectForKey("Port");
        if (portObj != null) {
            Object protocolObj = args.objectForKey("Protocol");
            if (protocolObj != null) {
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
            if (protocolObj != null) {
                host = new Host((String) args.objectForKey("Protocol"),
                        (String) args.objectForKey("Host"));
            }
            else {
                host = new Host((String) args.objectForKey("Host"));
            }
        }
        Object pathObj = args.objectForKey("InitialPath");
        if (pathObj != null) {
            host.setDefaultPath((String) args.objectForKey("InitialPath"));
        }
        Object userObj = args.objectForKey("Username");
        if (userObj != null) {
            host.setCredentials((String) args.objectForKey("Username"), (String) args.objectForKey("Password"));
        }
        Session session = SessionFactory.createSession(host);
        session.addObserver((Observer) this);
        session.mount(this.encoding, this.getFileFilter());
        return null;
    }

    public Object handleCloseScriptCommand(NSScriptCommand command) {
        log.debug("handleCloseScriptCommand:" + command);
        this.unmount();
        this.window().close();
        return null;
    }

    public Object handleDisconnectScriptCommand(NSScriptCommand command) {
        log.debug("handleDisconnectScriptCommand:" + command);
        this.unmount();
        return null;
    }

    public NSArray handleListScriptCommand(NSScriptCommand command) {
        log.debug("handleListScriptCommand:" + command);
        NSMutableArray result = new NSMutableArray();
        if (this.isMounted()) {
            NSDictionary args = command.evaluatedArguments();
            Object pathObj = args.objectForKey("Path");
            Path path = this.workdir();
            if (pathObj != null) {
                String folder = (String) args.objectForKey("Path");
                if (folder.charAt(0) == '/') {
                    path = PathFactory.createPath(this.session,
                            folder);
                }
                else {
                    path = PathFactory.createPath(this.session,
                            this.workdir().getAbsolute(),
                            folder);
                }
            }
            for (Iterator i = path.list(this.encoding, false, this.getFileFilter()).iterator(); i.hasNext();) {
                result.addObject(((Path) i.next()).getName());
            }
        }
        return result;
    }

    public Object handleGotoScriptCommand(NSScriptCommand command) {
        log.debug("handleGotoScriptCommand:" + command);
        if (this.isMounted()) {
            NSDictionary args = command.evaluatedArguments();
            CDGotoController c = new CDGotoController(this.workdir());
            c.gotoFolder(this.workdir(), (String) args.objectForKey("Path"));
        }
        return null;
    }

    public Object handleCreateFolderScriptCommand(NSScriptCommand command) {
        log.debug("handleCreateFolderScriptCommand:" + command);
        if (this.isMounted()) {
            NSDictionary args = command.evaluatedArguments();
            CDFolderController c = new CDFolderController();
            c.create(this.workdir(), (String) args.objectForKey("Path"));
        }
        return null;
    }

    public Object handleExistsScriptCommand(NSScriptCommand command) {
        log.debug("handleExistsScriptCommand:" + command);
        if (this.isMounted()) {
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
        if (this.isMounted()) {
            NSDictionary args = command.evaluatedArguments();
            CDCreateFileController c = new CDCreateFileController();
            c.create(this.workdir(), (String) args.objectForKey("Path"));
        }
        return null;
    }

    public Object handleEditScriptCommand(NSScriptCommand command) {
        log.debug("handleEditScriptCommand:" + command);
        if (this.isMounted()) {
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
        if (this.isMounted()) {
            NSDictionary args = command.evaluatedArguments();
            Path path = PathFactory.createPath(this.session,
                    this.workdir().getAbsolute(),
                    (String) args.objectForKey("Path"));
            path.delete();
            path.getParent().list(this.encoding, true, this.getFileFilter());
        }
        return null;
    }

    public Object handleRefreshScriptCommand(NSScriptCommand command) {
        log.debug("handleRefreshScriptCommand:" + command);
        if (this.isMounted()) {
            this.reloadButtonClicked(null);
        }
        return null;
    }

    public Object handleSyncScriptCommand(NSScriptCommand command) {
        log.debug("handleSyncScriptCommand:" + command);
        if (this.isMounted()) {
            NSDictionary args = command.evaluatedArguments();
            final Path path = PathFactory.createPath(this.session,
                    (String) args.objectForKey("Path"));
            path.attributes.setType(Path.DIRECTORY_TYPE);
            Object localObj = args.objectForKey("Local");
            if (localObj != null) {
                path.setLocal(new Local((String) localObj, path.getName()));
            }
            Queue q = new SyncQueue(path);
            q.process(false, true);
        }
        return null;
    }

    public Object handleDownloadScriptCommand(NSScriptCommand command) {
        log.debug("handleDownloadScriptCommand:" + command);
        if (this.isMounted()) {
            NSDictionary args = command.evaluatedArguments();
            final Path path = PathFactory.createPath(this.session,
                    this.workdir().getAbsolute(),
                    (String) args.objectForKey("Path"));
            path.attributes.setType(Path.FILE_TYPE);
            Object localObj = args.objectForKey("Local");
            if (localObj != null) {
                path.setLocal(new Local((String) localObj, path.getName()));
            }
            Object nameObj = args.objectForKey("Name");
            if (nameObj != null) {
                path.setLocal(new Local(path.getLocal().getParent(), (String) nameObj));
            }
            Queue q = new DownloadQueue(path);
            q.process(false, true);
        }
        return null;
    }

    public Object handleUploadScriptCommand(NSScriptCommand command) {
        log.debug("handleUploadScriptCommand:" + command);
        if (this.isMounted()) {
            NSDictionary args = command.evaluatedArguments();
            final Path path = PathFactory.createPath(this.session,
                    this.workdir().getAbsolute(),
                    new Local((String) args.objectForKey("Path")));
            path.attributes.setType(Path.FILE_TYPE);
            Object remoteObj = args.objectForKey("Remote");
            if (remoteObj != null) {
                path.setPath((String) remoteObj, path.getName());
            }
            Object nameObj = args.objectForKey("Name");
            if (nameObj != null) {
                path.setPath(this.workdir().getAbsolute(), (String) nameObj);
            }
            Queue q = new UploadQueue(path);
            q.process(false, true);
        }
        return null;
    }

    // ----------------------------------------------------------
    // Constructor
    // ----------------------------------------------------------

    public CDBrowserController() {
        instances.addObject(this);
        if (false == NSApplication.loadNibNamed("Browser", this)) {
            log.fatal("Couldn't load Browser.nib");
        }
    }

    public static CDBrowserController controllerForWindow(NSWindow window) {
        if (window.isVisible()) {
            Object delegate = window.delegate();
            if (delegate != null && delegate instanceof CDBrowserController) {
                return (CDBrowserController) delegate;
            }
        }
        return null;
    }

    public static void validateToolbarItems() {
        NSArray windows = NSApplication.sharedApplication().windows();
        int count = windows.count();
        while (0 != count--) {
            NSWindow window = (NSWindow) windows.objectAtIndex(count);
            CDBrowserController controller = CDBrowserController.controllerForWindow(window);
            if (null != controller) {
                window.toolbar().validateVisibleItems();
            }
        }
    }

    public static void updateBrowserTableAttributes() {
        NSArray windows = NSApplication.sharedApplication().windows();
        int count = windows.count();
        while (0 != count--) {
            NSWindow window = (NSWindow) windows.objectAtIndex(count);
            CDBrowserController controller = CDBrowserController.controllerForWindow(window);
            if (null != controller) {
                controller._updateBrowserListTableAttributes();
                controller._updateBrowserOutlineTableAttributes();
            }
        }
    }

    public static void updateBrowserTableColumns() {
        NSArray windows = NSApplication.sharedApplication().windows();
        int count = windows.count();
        while (0 != count--) {
            NSWindow window = (NSWindow) windows.objectAtIndex(count);
            CDBrowserController controller = CDBrowserController.controllerForWindow(window);
            if (null != controller) {
                controller._updateBrowserTableColumns(controller.browserListView);
                controller._updateBrowserTableColumns(controller.browserOutlineView);
            }
        }
    }

    public void awakeFromNib() {
        super.awakeFromNib();

        this._updateBrowserTableColumns(this.browserListView);
        this._updateBrowserTableColumns(this.browserOutlineView);

        // Configure window
        this.window().setTitle("Cyberduck " + NSBundle.bundleForClass(this.getClass()).objectForInfoDictionaryKey("CFBundleVersion"));
        this.window().setInitialFirstResponder(quickConnectPopup);
        // Drawer states
        if (Preferences.instance().getBoolean("bookmarkDrawer.isOpen")) {
            this.bookmarkDrawer.open();
        }
        // Configure Toolbar
        this.toolbar = new NSToolbar("Cyberduck Toolbar");
        this.toolbar.setDelegate(this);
        this.toolbar.setAllowsUserCustomization(true);
        this.toolbar.setAutosavesConfiguration(true);
        this.window().setToolbar(toolbar);

        this.browserSwitchClicked(this.browserSwitchView);
		this.window().makeFirstResponder(this.quickConnectPopup);
	}

	private String encoding = Preferences.instance().getProperty("browser.charset.encoding");

    protected String getEncoding() {
        return this.encoding;
    }

	private Filter filenameFilter;

	{
		if(Preferences.instance().getBoolean("browser.showHidden")) {
			filenameFilter = new NullFilter();
        }
		else {
			filenameFilter = new HiddenFilesFilter();
        }
	}

    protected Filter getFileFilter() {
		return this.filenameFilter;
    }

    public void setShowHiddenFiles(boolean showHidden) {
        if(showHidden) {
            filenameFilter = new NullFilter();
        }
        else {
            filenameFilter = new HiddenFilesFilter();
        }
    }

    public boolean getShowHiddenFiles() {
        return filenameFilter instanceof NullFilter;
    }

    private CDInfoController inspector = null;

	private void getFocus() {
		log.debug("getFocus");
		switch(this.browserSwitchView.selectedSegment()) {
			case LIST_VIEW: {
				this.window().makeFirstResponder(this.browserListView);
				break;
			}
			case OUTLINE_VIEW: {
				this.window().makeFirstResponder(this.browserOutlineView);
				break;
			}
			case COLUMN_VIEW: {
				this.window().makeFirstResponder(this.browserColumnView);
				break;
			}
		}
	}

    private void reloadPathPopup() {
        pathPopupItems.clear();
        pathPopupButton.removeAllItems();
        if(this.isMounted()) {
            this.addPathToPopup(workdir);
            for (Path p = workdir; !p.isRoot();) {
                p = p.getParent();
                this.addPathToPopup(p);
            }
        }
    }

	private void reloadData() {
		log.debug("reloadData");
        this.reloadPathPopup();
		this.sort();
		switch(this.browserSwitchView.selectedSegment()) {
			case LIST_VIEW: {
				this.browserListView.reloadData();
				if(this.isMounted()) {
					this.infoLabel.setStringValue(this.browserListView.numberOfRows() + " " +
												  NSBundle.localizedString("files", ""));
				}
				else this.infoLabel.setStringValue("");
				break;
			}
			case OUTLINE_VIEW: {
				this.browserOutlineView.reloadData();
				if(this.isMounted()) {
                    for(int i = 0; i < this.browserOutlineView.numberOfRows(); i++) {
                        Path p = (Path)this.browserOutlineView.itemAtRow(i);
                        if(p.getSession().cache().isExpanded(p.getAbsolute())) {
                            this.browserOutlineView.expandItem(p);
                        }
                    }
					this.infoLabel.setStringValue(this.browserOutlineView.numberOfRows() + " " +
												  NSBundle.localizedString("files", ""));
				}
				else this.infoLabel.setStringValue("");
				break;
			}
			case COLUMN_VIEW: {
                if(this.isMounted()) {
                    this.browserColumnView.setPath(workdir().getAbsolute());
                    this.browserColumnView.reloadColumn(browserColumnView.lastColumn());
                    this.browserColumnView.setPath(workdir().getAbsolute());
                    this.infoLabel.setStringValue(browserListModel.childs(workdir()).size() + " " +
                            NSBundle.localizedString("files", ""));
                    this.browserColumnView.validateVisibleColumns();
                }
                else infoLabel.setStringValue("");
				break;
			}
		}
    }

	private void selectRow(int row, boolean expand) {
		log.debug("selectRow:"+row);
		switch(this.browserSwitchView.selectedSegment()) {
			case LIST_VIEW: {
				this.browserListView.selectRow(row, expand);
				break;
			}
			case OUTLINE_VIEW: {
				this.browserOutlineView.selectRow(row, expand);
				break;
			}
			case COLUMN_VIEW: {
				break;
			}
		}
		this.getFocus();
	}

	private Path getSelectedPath() {
		switch(this.browserSwitchView.selectedSegment()) {
			case LIST_VIEW: {
				return (Path)this.browserListModel.childs(this.workdir()).get(this.browserListView.selectedRow());
			}
			case OUTLINE_VIEW: {
				return (Path)this.browserOutlineView.itemAtRow(this.browserOutlineView.selectedRow());
			}
			case COLUMN_VIEW: {
				return ((CDBrowserCell)this.browserColumnView.selectedCell()).getPath();
			}
		}
		return null;
	}

    private int getClickedRow() {
        switch(this.browserSwitchView.selectedSegment()) {
            case LIST_VIEW: {
                return this.browserListView.clickedRow();
            }
            case OUTLINE_VIEW: {
                return this.browserOutlineView.clickedRow();
            }
            case COLUMN_VIEW: {

            }
        }
        return -1;
    }

	protected List getSelectedPaths() {
		switch(this.browserSwitchView.selectedSegment()) {
			case LIST_VIEW: {
				NSEnumerator iterator = this.browserListView.selectedRowEnumerator();
				List selectedFiles = new ArrayList();
                List childs = this.browserListModel.childs(this.workdir());
				while (iterator.hasMoreElements()) {
					int selectedIndex = ((Integer) iterator.nextElement()).intValue();
					selectedFiles.add(childs.get(selectedIndex));
				}
				return selectedFiles;
			}
			case OUTLINE_VIEW: {
				NSEnumerator iterator = this.browserOutlineView.selectedRowEnumerator();
				List files = new ArrayList();
				while (iterator.hasMoreElements()) {
					int selected = ((Integer) iterator.nextElement()).intValue();
					files.add(this.browserOutlineView.itemAtRow(selected));
				}
				return files;
			}
			case COLUMN_VIEW: {
				java.util.Enumeration iterator = this.browserColumnView.selectedCells().objectEnumerator();
				List files = new ArrayList();
				while (iterator.hasMoreElements()) {
					files.add(((CDBrowserCell)iterator.nextElement()).getPath());
				}
				return files;
			}
		}
		return null;
	}

	private int getSelectionCount() {
		switch(this.browserSwitchView.selectedSegment()) {
			case LIST_VIEW: {
				return this.browserListView.numberOfSelectedRows();
			}
			case OUTLINE_VIEW: {
				return this.browserOutlineView.numberOfSelectedRows();
			}
			case COLUMN_VIEW: {
				NSArray selectedCells = this.browserColumnView.selectedCells();
				if(selectedCells != null)
					return selectedCells.count();
			}
		}
		return 0;
	}

	private void deselectAll() {
		switch(this.browserSwitchView.selectedSegment()) {
			case LIST_VIEW: {
				this.browserListView.deselectAll(null);
				break;
			}
			case OUTLINE_VIEW: {
				this.browserOutlineView.deselectAll(null);
				break;
			}
			case COLUMN_VIEW: {
				break;
			}
		}
	}

	public void browserColumnViewRowClicked(Object sender) {
		this.browserSelectionDidChange(null);
		if(!((NSBrowserCell)this.browserColumnView.selectedCell()).isLeaf()) {
			this.browserRowDoubleClicked(sender);
		}
    }

    public void browserRowDoubleClicked(Object sender) {
        if(this.getClickedRow() != -1) { // make sure double click was not in table header
            this.insideButtonClicked(sender);
        }
	}

	private void sort() {
		switch(this.browserSwitchView.selectedSegment()) {
			case LIST_VIEW: {
				NSTableColumn selectedColumn = this.browserListModel.selectedColumn();
                if(null == selectedColumn) {
                    selectedColumn = this.browserListView.tableColumnWithIdentifier("FILENAME");
                }
				this.browserListView.setIndicatorImage(this.browserListModel.isSortedAscending() ?
														  NSImage.imageNamed("NSAscendingSortIndicator") :
														  NSImage.imageNamed("NSDescendingSortIndicator"), selectedColumn);
				this.browserListModel.sort(selectedColumn, this.browserListModel.isSortedAscending());
//                this.browserListView.setHighlightedTableColumn(selectedColumn);
				break;
			}
			case OUTLINE_VIEW: {
				NSTableColumn selectedColumn = this.browserOutlineModel.selectedColumn();
                if(null == selectedColumn) {
                    selectedColumn = this.browserOutlineView.tableColumnWithIdentifier("FILENAME");
                }
				this.browserOutlineView.setIndicatorImage(this.browserOutlineModel.isSortedAscending() ?
														  NSImage.imageNamed("NSAscendingSortIndicator") :
														  NSImage.imageNamed("NSDescendingSortIndicator"), selectedColumn);
				this.browserOutlineModel.sort(selectedColumn, this.browserOutlineModel.isSortedAscending());
//                this.browserOutlineView.setHighlightedTableColumn(selectedColumn);
				break;
			}
			case COLUMN_VIEW: {
				break;
			}
		}
	}

    public void update(final Observable o, final Object arg) {
        if(!Thread.currentThread().getName().equals("main")
        && !Thread.currentThread().getName().equals("AWT-AppKit")) {
            this.invoke(new Runnable() {
                public void run(){
                    update(o, arg);
                }
            });
            return;
        }
        if (arg instanceof Path) {
            this.workdir = (Path) arg;
            this.reloadData();
        }
        else if (arg instanceof Message) {
            final Message msg = (Message) arg;
            if (msg.getTitle().equals(Message.ERROR)) {
                this.progressIndicator.stopAnimation(this);
                this.statusIcon.setImage(NSImage.imageNamed("alert.tiff"));
                this.statusIcon.setNeedsDisplay(true);
                this.statusLabel.setAttributedStringValue(new NSAttributedString((String) msg.getContent(),
                        TRUNCATE_MIDDLE_PARAGRAPH_DICTIONARY));
                this.statusLabel.setNeedsDisplay(true);
                this.beginSheet(NSAlertPanel.criticalAlertPanel(NSBundle.localizedString("Error", "Alert sheet title"), //title
                        (String) msg.getContent(), // message
                        NSBundle.localizedString("OK", "Alert default button"), // defaultbutton
                        null, //alternative button
                        null) //other button
                );
            }
            else if (msg.getTitle().equals(Message.PROGRESS)) {
                this.statusLabel.setAttributedStringValue(new NSAttributedString((String) msg.getContent(),
                        TRUNCATE_MIDDLE_PARAGRAPH_DICTIONARY));
                this.statusLabel.display();
            }
            else if (msg.getTitle().equals(Message.REFRESH)) {
                this.reloadButtonClicked(null);
            }
            else if (msg.getTitle().equals(Message.OPEN)) {
                progressIndicator.startAnimation(this);
                statusIcon.setImage(null);
                statusIcon.setNeedsDisplay(true);
            }
            else if (msg.getTitle().equals(Message.CLOSE)) {
                progressIndicator.stopAnimation(this);
                statusIcon.setImage(null);
                statusIcon.setNeedsDisplay(true);
            }
            else if (msg.getTitle().equals(Message.START)) {
                statusIcon.setImage(null);
                statusIcon.setNeedsDisplay(true);
                progressIndicator.startAnimation(this);
            }
            else if (msg.getTitle().equals(Message.STOP)) {
                progressIndicator.stopAnimation(this);
                statusLabel.setAttributedStringValue(new NSAttributedString(NSBundle.localizedString("Idle", "Status", ""),
                        TRUNCATE_MIDDLE_PARAGRAPH_DICTIONARY));
                this.statusLabel.display();
            }
        }
    }

    // ----------------------------------------------------------
    // Outlets
    // ----------------------------------------------------------

	private NSToolbar toolbar;

	private NSTabView browserTabView;

    public void setBrowserTabView(NSTabView browserTabView) {
        this.browserTabView = browserTabView;
    }

    private NSTextView logView;

    public void setLogView(NSTextView logView) {
        this.logView = logView;
    }

//	private NSButton interruptButton;
//
//	public void setInterruptButton(NSButton interruptButton) {
//		this.interruptButton = interruptButton;
//		this.interruptButton.setTarget(this);
//      this.interruptButton.setAction(new NSSelector("interruptButtonClicked", new Class[]{Object.class}));
//		this.interruptButton.setEnabled(true);
//	}
//
//	public void interruptButtonClicked(Object sender) {
//		if(this.isMounted()) {
//			this.session.interrupt();
//		}
//	}

	public NSView getSelectedBrowserView() {
		switch(this.browserSwitchView.selectedSegment()) {
			case LIST_VIEW: {
				return this.browserListView;
			}
			case OUTLINE_VIEW: {
				return this.browserOutlineView;
			}
			case COLUMN_VIEW: {
				return this.browserColumnView;
			}
		}
		return null;
	}

    private NSSegmentedControl browserSwitchView;

	private static final int LIST_VIEW = 0;
	private static final int OUTLINE_VIEW = 1;
	private static final int COLUMN_VIEW = 2;

    public void setBrowserSwitchView(NSSegmentedControl browserSwitchView) {
        this.browserSwitchView = browserSwitchView;
        this.browserSwitchView.setSegmentCount(2); // list, outline, column
		this.browserSwitchView.setImage(NSImage.imageNamed("list.tiff"), LIST_VIEW);
		this.browserSwitchView.setImage(NSImage.imageNamed("outline.tiff"), OUTLINE_VIEW);
//		this.browserSwitchView.setImage(NSImage.imageNamed("column.tiff"), COLUMN_VIEW);
        this.browserSwitchView.setTarget(this);
        this.browserSwitchView.setAction(new NSSelector("browserSwitchClicked", new Class[]{Object.class}));
        ((NSSegmentedCell) this.browserSwitchView.cell()).setTrackingMode(NSSegmentedCell.NSSegmentSwitchTrackingSelectOne);
        this.browserSwitchView.cell().setControlSize(NSCell.RegularControlSize);
        this.browserSwitchView.setSelected(Preferences.instance().getInteger("browser.view"));
    }

    public void browserSwitchClicked(Object sender) {
        log.debug("browserSwitchClicked");
        if (sender instanceof NSMenuItem) {
			this.browserSwitchView.setSelected(((NSMenuItem)sender).tag());
			this.browserTabView.selectTabViewItemAtIndex(((NSMenuItem)sender).tag());
			Preferences.instance().setProperty("browser.view", ((NSMenuItem)sender).tag());
        }
        if (sender instanceof NSSegmentedControl) {
			this.browserTabView.selectTabViewItemAtIndex(((NSSegmentedControl)sender).selectedSegment());
			Preferences.instance().setProperty("browser.view", ((NSSegmentedControl)sender).selectedSegment());
		}
		this.reloadData();
        this.getFocus();
        this.quickConnectPopup.setNextKeyView(this.getSelectedBrowserView());
        this.searchField.setNextKeyView(this.getSelectedBrowserView());
    }

    private CDBrowserOutlineViewModel browserOutlineModel;
    private NSOutlineView browserOutlineView; // IBOutlet

    public void setBrowserOutlineView(NSOutlineView browserOutlineView) {
        this.browserOutlineView = browserOutlineView;
        this.browserOutlineView.setTarget(this);
        this.browserOutlineView.setDoubleAction(new NSSelector("browserRowDoubleClicked", new Class[]{Object.class}));
        // receive drag events from types
        this.browserOutlineView.registerForDraggedTypes(new NSArray(new Object[]{
            "QueuePboardType",
            NSPasteboard.FilenamesPboardType, //accept files dragged from the Finder for uploading
            NSPasteboard.FilesPromisePboardType} //accept file promises made myself but then interpret them as QueuePboardType
        ));

        // setting appearance attributes
        this.browserOutlineView.setRowHeight(17f);
        this._updateBrowserOutlineTableAttributes();
        // selection properties
        this.browserOutlineView.setAllowsMultipleSelection(true);
        this.browserOutlineView.setAllowsEmptySelection(true);
        this.browserOutlineView.setAllowsColumnResizing(true);
        this.browserOutlineView.setAllowsColumnSelection(false);
        this.browserOutlineView.setAllowsColumnReordering(true);

        if (Preferences.instance().getBoolean("browser.info.isInspector")) {
            (NSNotificationCenter.defaultCenter()).addObserver(this,
                    new NSSelector("browserSelectionDidChange", new Class[]{NSNotification.class}),
                    NSOutlineView.OutlineViewSelectionDidChangeNotification,
                    this.browserOutlineView);
        }
        this.browserOutlineView.setDataSource(this.browserOutlineModel = new CDBrowserOutlineViewModel(this));
        this.browserOutlineView.setDelegate(this.browserOutlineModel);
        {
            NSTableColumn c = new NSTableColumn();
            c.headerCell().setStringValue(NSBundle.localizedString("Filename", "A column in the browser"));
            c.setIdentifier("FILENAME");
            c.setMinWidth(100f);
            c.setWidth(250f);
            c.setMaxWidth(1000f);
            NSSelector setResizableMaskSelector = new NSSelector("setResizingMask", new Class[]{int.class});
            if(setResizableMaskSelector.implementedByClass(NSTableColumn.class)) {
                c.setResizingMask(NSTableColumn.AutoresizingMask);
            }
            else {
                c.setResizable(true);
            }
            c.setDataCell(new CDOutlineCell());
            c.dataCell().setAlignment(NSText.LeftTextAlignment);
            this.browserOutlineView.addTableColumn(c);
            this.browserOutlineView.setOutlineTableColumn(c);
        }
    }

    private CDBrowserListViewModel browserListModel;
    private NSTableView browserListView; // IBOutlet

    public void setBrowserListView(NSTableView browserListView) {
        this.browserListView = browserListView;
        this.browserListView.setTarget(this);
        this.browserListView.setDoubleAction(new NSSelector("browserRowDoubleClicked", new Class[]{Object.class}));
        // receive drag events from types
        this.browserListView.registerForDraggedTypes(new NSArray(new Object[]{
            "QueuePboardType",
            NSPasteboard.FilenamesPboardType, //accept files dragged from the Finder for uploading
            NSPasteboard.FilesPromisePboardType} //accept file promises made myself but then interpret them as QueuePboardType
        ));

        // setting appearance attributes
        this.browserListView.setRowHeight(17f);
        this._updateBrowserListTableAttributes();
        // selection properties
        this.browserListView.setAllowsMultipleSelection(true);
        this.browserListView.setAllowsEmptySelection(true);
        this.browserListView.setAllowsColumnResizing(true);
        this.browserListView.setAllowsColumnSelection(false);
        this.browserListView.setAllowsColumnReordering(true);

        if (Preferences.instance().getBoolean("browser.info.isInspector")) {
            (NSNotificationCenter.defaultCenter()).addObserver(this,
                    new NSSelector("browserSelectionDidChange", new Class[]{NSNotification.class}),
                    NSTableView.TableViewSelectionDidChangeNotification,
                    this.browserListView);
        }
        this.browserListView.setDataSource(this.browserListModel = new CDBrowserListViewModel(this));
        this.browserListView.setDelegate(this.browserListModel);
        NSSelector setResizableMaskSelector
                = new NSSelector("setResizingMask", new Class[]{int.class});
        {
            NSTableColumn c = new NSTableColumn();
            c.setIdentifier("TYPE");
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
            c.setEditable(false);
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
            c.setEditable(false);
            c.setDataCell(new NSTextFieldCell());
            c.dataCell().setAlignment(NSText.LeftTextAlignment);
            this.browserListView.addTableColumn(c);
        }
    }

    private CDBrowserColumnViewModel browserColumnModel;
    private NSBrowser browserColumnView; // IBOutlet

    public void setBrowserColumnView(NSBrowser browserColumnView) {
        this.browserColumnView = browserColumnView;
        this.browserColumnView.setTarget(this);
        this.browserColumnView.setAction(new NSSelector("browserColumnViewRowClicked", new Class[]{Object.class}));
        this.browserColumnView.setDoubleAction(new NSSelector("browserRowDoubleClicked", new Class[]{Object.class}));
        this.browserColumnView.setAcceptsArrowKeys(true);
		this.browserColumnView.setSendsActionOnArrowKeys(true);
        this.browserColumnView.setMaxVisibleColumns(5);
        this.browserColumnView.setAllowsEmptySelection(true);
        this.browserColumnView.setAllowsMultipleSelection(true);
		this.browserColumnView.setAllowsBranchSelection(true);
        this.browserColumnView.setPathSeparator("/");
        this.browserColumnView.setReusesColumns(false);
        this.browserColumnView.setSeparatesColumns(false);
        this.browserColumnView.setTitled(false);
        this.browserColumnView.setHasHorizontalScroller(false);

        this.browserColumnView.setDelegate(this.browserColumnModel = new CDBrowserColumnViewModel(this));
        // Make the browser user our custom browser cell.
        this.browserColumnView.setNewCellClass(CDBrowserCell.class);
        this.browserColumnView.setNewMatrixClass(CDBrowserMatrix.class);
    }

    public void browserSelectionDidChange(NSNotification notification) {
        if (this.inspector != null && this.inspector.window().isVisible()) {
            List files = new ArrayList();
			for(Iterator i = this.getSelectedPaths().iterator(); i.hasNext(); ) {
                files.add(i.next());
            }
            this.inspector.setFiles(files);
        }
    }

    protected void _updateBrowserOutlineTableAttributes() {
        NSSelector setUsesAlternatingRowBackgroundColorsSelector =
                new NSSelector("setUsesAlternatingRowBackgroundColors", new Class[]{boolean.class});
        if (setUsesAlternatingRowBackgroundColorsSelector.implementedByClass(NSTableView.class)) {
            this.browserOutlineView.setUsesAlternatingRowBackgroundColors(Preferences.instance().getBoolean("browser.alternatingRows"));
        }
        NSSelector setGridStyleMaskSelector =
                new NSSelector("setGridStyleMask", new Class[]{int.class});
        if (setGridStyleMaskSelector.implementedByClass(NSTableView.class)) {
            if (Preferences.instance().getBoolean("browser.horizontalLines") && Preferences.instance().getBoolean("browser.verticalLines")) {
                this.browserOutlineView.setGridStyleMask(NSTableView.SolidHorizontalGridLineMask | NSTableView.SolidVerticalGridLineMask);
            }
            else if (Preferences.instance().getBoolean("browser.verticalLines")) {
                this.browserOutlineView.setGridStyleMask(NSTableView.SolidVerticalGridLineMask);
            }
            else if (Preferences.instance().getBoolean("browser.horizontalLines")) {
                this.browserOutlineView.setGridStyleMask(NSTableView.SolidHorizontalGridLineMask);
            }
            else {
                this.browserOutlineView.setGridStyleMask(NSTableView.GridNone);
            }
        }
    }

    protected void _updateBrowserListTableAttributes() {
        NSSelector setUsesAlternatingRowBackgroundColorsSelector =
                new NSSelector("setUsesAlternatingRowBackgroundColors", new Class[]{boolean.class});
        if (setUsesAlternatingRowBackgroundColorsSelector.implementedByClass(NSTableView.class)) {
            this.browserListView.setUsesAlternatingRowBackgroundColors(Preferences.instance().getBoolean("browser.alternatingRows"));
        }
        NSSelector setGridStyleMaskSelector =
                new NSSelector("setGridStyleMask", new Class[]{int.class});
        if (setGridStyleMaskSelector.implementedByClass(NSTableView.class)) {
            if (Preferences.instance().getBoolean("browser.horizontalLines") && Preferences.instance().getBoolean("browser.verticalLines")) {
                this.browserListView.setGridStyleMask(NSTableView.SolidHorizontalGridLineMask | NSTableView.SolidVerticalGridLineMask);
            }
            else if (Preferences.instance().getBoolean("browser.verticalLines")) {
                this.browserListView.setGridStyleMask(NSTableView.SolidVerticalGridLineMask);
            }
            else if (Preferences.instance().getBoolean("browser.horizontalLines")) {
                this.browserListView.setGridStyleMask(NSTableView.SolidHorizontalGridLineMask);
            }
            else {
                this.browserListView.setGridStyleMask(NSTableView.GridNone);
            }
        }
    }

    protected void _updateBrowserTableColumns(NSTableView table) {
        log.debug("_updateBrowserTableColumns");
        table.removeTableColumn(table.tableColumnWithIdentifier("SIZE"));
        NSSelector setResizableMaskSelector
                = new NSSelector("setResizingMask", new Class[]{int.class});
        if (Preferences.instance().getBoolean("browser.columnSize")) {
            NSTableColumn c = new NSTableColumn();
            c.headerCell().setStringValue(NSBundle.localizedString("Size", "A column in the browser"));
            c.setIdentifier("SIZE");
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
        table.removeTableColumn(table.tableColumnWithIdentifier("MODIFIED"));
        if (Preferences.instance().getBoolean("browser.columnModification")) {
            NSTableColumn c = new NSTableColumn();
            c.headerCell().setStringValue(NSBundle.localizedString("Modified", "A column in the browser"));
            c.setIdentifier("MODIFIED");
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
            c.dataCell().setFormatter(new NSGregorianDateFormatter((String) NSUserDefaults.standardUserDefaults().objectForKey(NSUserDefaults.ShortTimeDateFormatString),
                    true));
            table.addTableColumn(c);
        }
        table.removeTableColumn(table.tableColumnWithIdentifier("OWNER"));
        if (Preferences.instance().getBoolean("browser.columnOwner")) {
            NSTableColumn c = new NSTableColumn();
            c.headerCell().setStringValue(NSBundle.localizedString("Owner", "A column in the browser"));
            c.setIdentifier("OWNER");
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
        table.removeTableColumn(table.tableColumnWithIdentifier("PERMISSIONS"));
        if (Preferences.instance().getBoolean("browser.columnPermissions")) {
            NSTableColumn c = new NSTableColumn();
            c.headerCell().setStringValue(NSBundle.localizedString("Permissions", "A column in the browser"));
            c.setIdentifier("PERMISSIONS");
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
        table.sizeToFit();
        this.reloadData();
    }

    public void reloadBookmarks() {
        this.bookmarkTable.reloadData();
        this.bookmarkTable.selectRow(this.bookmarkModel.size()-1, false);
    }

    private CDBookmarkTableDataSource bookmarkModel;
    private NSTableView bookmarkTable; // IBOutlet

    public void setBookmarkTable(NSTableView bookmarkTable) {
        this.bookmarkTable = bookmarkTable;
        this.bookmarkTable.setTarget(this);
        this.bookmarkTable.setDoubleAction(new NSSelector("bookmarkTableRowDoubleClicked", new Class[]{Object.class}));

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
            c.setIdentifier("ICON");
            c.headerCell().setStringValue("");
            c.setMinWidth(32f);
            c.setWidth(32f);
            c.setMaxWidth(32f);
            c.setEditable(false);
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
            c.setIdentifier("BOOKMARK");
            c.headerCell().setStringValue(NSBundle.localizedString("Bookmarks", "A column in the browser"));
            c.setMinWidth(50f);
            c.setWidth(200f);
            c.setMaxWidth(500f);
            c.setEditable(false);
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
        NSSelector setUsesAlternatingRowBackgroundColorsSelector =
                new NSSelector("setUsesAlternatingRowBackgroundColors", new Class[]{boolean.class});
        if (setUsesAlternatingRowBackgroundColorsSelector.implementedByClass(NSTableView.class)) {
            this.bookmarkTable.setUsesAlternatingRowBackgroundColors(Preferences.instance().getBoolean("browser.alternatingRows"));
        }
        NSSelector setGridStyleMaskSelector =
                new NSSelector("setGridStyleMask", new Class[]{int.class});
        if (setGridStyleMaskSelector.implementedByClass(NSTableView.class)) {
            this.bookmarkTable.setGridStyleMask(NSTableView.SolidHorizontalGridLineMask);
        }

        // selection properties
        this.bookmarkTable.setAllowsMultipleSelection(true);
        this.bookmarkTable.setAllowsEmptySelection(true);
        this.bookmarkTable.setAllowsColumnResizing(false);
        this.bookmarkTable.setAllowsColumnSelection(false);
        this.bookmarkTable.setAllowsColumnReordering(false);
        this.bookmarkTable.sizeToFit();

        (NSNotificationCenter.defaultCenter()).addObserver(this,
                new NSSelector("bookmarkSelectionDidChange", new Class[]{NSNotification.class}),
                NSTableView.TableViewSelectionDidChangeNotification,
                this.bookmarkTable);
        this.bookmarkTable.setDataSource(this.bookmarkModel = CDBookmarkTableDataSource.instance());
        this.bookmarkTable.setDelegate(this.bookmarkModel);
    }

    public void bookmarkSelectionDidChange(NSNotification notification) {
        log.debug("bookmarkSelectionDidChange");
        editBookmarkButton.setEnabled(bookmarkTable.numberOfSelectedRows() == 1);
        deleteBookmarkButton.setEnabled(bookmarkTable.selectedRow() != -1);
    }

    public void bookmarkTableRowDoubleClicked(Object sender) {
        log.debug("bookmarkTableRowDoubleClicked");
        if (this.bookmarkTable.selectedRow() != -1) {
            Host h = (Host) bookmarkModel.get(bookmarkTable.selectedRow());
            this.mount(h, h.getEncoding());
        }
    }

    private NSMenu editMenu;
    private NSObject editMenuDelegate;

    public void setEditMenu(NSMenu editMenu) {
        this.editMenu = editMenu;
        this.editMenu.setAutoenablesItems(true);
        NSSelector setDelegateSelector =
                new NSSelector("setDelegate", new Class[]{Object.class});
        if(setDelegateSelector.implementedByClass(NSMenu.class)) {
            this.editMenu.setDelegate(this.editMenuDelegate = new EditMenuDelegate());
        }
    }

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
            String identifier = (String)Editor.INSTALLED_EDITORS.values().toArray(new String[]{})[index];
            String editor = (String)Editor.INSTALLED_EDITORS.keySet().toArray(new String[]{})[index];
            item.setTitle(editor);
            if(editor.equals(Preferences.instance().getProperty("editor.name"))) {
                item.setKeyEquivalent("j");
                item.setKeyEquivalentModifierMask(NSEvent.CommandKeyMask);
            }
            else {
                item.setKeyEquivalent("");
            }
            NSSelector absolutePathForAppBundleWithIdentifierSelector =
                    new NSSelector("absolutePathForAppBundleWithIdentifier", new Class[]{String.class});
            if (absolutePathForAppBundleWithIdentifierSelector.implementedByClass(NSWorkspace.class)) {
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
            }
            item.setAction(new NSSelector("editButtonClicked", new Class[]{Object.class}));
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
	private NSObject quickConnectPopupDataSource;

    public void setQuickConnectPopup(NSComboBox quickConnectPopup) {
        this.quickConnectPopup = quickConnectPopup;
        this.quickConnectPopup.setTarget(this);
        this.quickConnectPopup.setCompletes(true);
        this.quickConnectPopup.setAction(new NSSelector("quickConnectSelectionChanged", new Class[]{Object.class}));
        this.quickConnectPopup.setUsesDataSource(true);
        this.quickConnectPopup.setDataSource(this.quickConnectPopupDataSource = new NSObject() {
            public int numberOfItemsInComboBox(NSComboBox combo) {
                return CDBookmarkTableDataSource.instance().size();
            }

            public Object comboBoxObjectValueForItemAtIndex(NSComboBox combo, int row) {
                if (row < this.numberOfItemsInComboBox(combo)) {
                    return ((Host) CDBookmarkTableDataSource.instance().get(row)).getHostname();
                }
                return null;
            }
        });
    }

    public void quickConnectSelectionChanged(Object sender) {
        log.debug("quickConnectSelectionChanged");
        String input = ((NSControl) sender).stringValue();
        try {
            for (Iterator iter = bookmarkModel.iterator(); iter.hasNext();) {
                Host h = (Host) iter.next();
                if (h.getHostname().equals(input)) {
                    this.mount(h);
                    return;
                }
            }
            this.mount(Host.parse(input));
        }
        catch (java.net.MalformedURLException e) {
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
        if (null != userInfo) {
            Object o = userInfo.allValues().lastObject();
            if (null != o) {
                final String searchString = ((NSText) o).string();
                if (null == searchString || searchString.length() == 0) {
					this.filenameFilter = new HiddenFilesFilter();
                }
                else {
					this.filenameFilter = new Filter() {
						public boolean accept(Path file) {
							return file.getName().toLowerCase().indexOf(searchString.toLowerCase()) != -1;
						}
					};
                }
				this.reloadData();
            }
        }
    }

    // ----------------------------------------------------------
    // Manage Bookmarks
    // ----------------------------------------------------------

    private NSButton editBookmarkButton; // IBOutlet

    public void setEditBookmarkButton(NSButton editBookmarkButton) {
        this.editBookmarkButton = editBookmarkButton;
        this.editBookmarkButton.setImage(NSImage.imageNamed("edit.tiff"));
        this.editBookmarkButton.setAlternateImage(NSImage.imageNamed("editPressed.tiff"));
        this.editBookmarkButton.setEnabled(false);
        this.editBookmarkButton.setTarget(this);
        this.editBookmarkButton.setAction(new NSSelector("editBookmarkButtonClicked", new Class[]{Object.class}));
    }

    public void editBookmarkButtonClicked(Object sender) {
        this.bookmarkDrawer.open();
        CDBookmarkController controller = new CDBookmarkController(bookmarkTable,
                (Host) bookmarkModel.get(bookmarkTable.selectedRow()));
        controller.window().makeKeyAndOrderFront(null);
    }

    private NSButton addBookmarkButton; // IBOutlet

    public void setAddBookmarkButton(NSButton addBookmarkButton) {
        this.addBookmarkButton = addBookmarkButton;
        this.addBookmarkButton.setImage(NSImage.imageNamed("add"));
        this.addBookmarkButton.setAlternateImage(NSImage.imageNamed("addPressed.tiff"));
        this.addBookmarkButton.setTarget(this);
        this.addBookmarkButton.setAction(new NSSelector("addBookmarkButtonClicked", new Class[]{Object.class}));
    }

    public void addBookmarkButtonClicked(Object sender) {
        this.bookmarkDrawer.open();
        Host item;
        if (this.isMounted()) {
            item = this.session.getHost().copy();
            item.setDefaultPath(this.workdir().getAbsolute());
        }
        else {
            item = new Host(Preferences.instance().getProperty("connection.protocol.default"),
                    "localhost",
                    Preferences.instance().getInteger("connection.port.default"));
        }
        this.bookmarkModel.add(item);
        this.bookmarkTable.reloadData();
        this.bookmarkTable.selectRow(bookmarkModel.lastIndexOf(item), false);
        this.bookmarkTable.scrollRowToVisible(bookmarkModel.lastIndexOf(item));
        CDBookmarkController controller = new CDBookmarkController(bookmarkTable, item);
        controller.window().makeKeyAndOrderFront(null);
    }

    private NSButton deleteBookmarkButton; // IBOutlet

    public void setDeleteBookmarkButton(NSButton deleteBookmarkButton) {
        this.deleteBookmarkButton = deleteBookmarkButton;
        this.deleteBookmarkButton.setImage(NSImage.imageNamed("remove.tiff"));
        this.deleteBookmarkButton.setAlternateImage(NSImage.imageNamed("removePressed.tiff"));
        this.deleteBookmarkButton.setEnabled(false);
        this.deleteBookmarkButton.setTarget(this);
        this.deleteBookmarkButton.setAction(new NSSelector("deleteBookmarkButtonClicked",
                new Class[]{Object.class}));
    }

    public void deleteBookmarkButtonClicked(Object sender) {
        this.bookmarkDrawer.open();
        NSEnumerator iterator = bookmarkTable.selectedRowEnumerator();
        int j = 0;
        while (iterator.hasMoreElements()) {
            int i = ((Integer) iterator.nextElement()).intValue();
            Host host = (Host) this.bookmarkModel.get(i - j);
            switch (NSAlertPanel.runCriticalAlert(NSBundle.localizedString("Delete Bookmark", ""),
                    NSBundle.localizedString("Do you want to delete the selected bookmark?", "")
                    + " (" + host.getNickname() + ")",
                    NSBundle.localizedString("Delete", ""),
                    NSBundle.localizedString("Cancel", ""),
                    null)) {
                case NSAlertPanel.DefaultReturn:
                    bookmarkModel.remove(i - j);
                    j++;
                    break;
                case NSAlertPanel.AlternateReturn:
                    break;
            }
        }
        this.bookmarkTable.reloadData();
    }

    // ----------------------------------------------------------
    // Browser navigation
    // ----------------------------------------------------------

    private NSButton upButton; // IBOutlet

    public void setUpButton(NSButton upButton) {
        this.upButton = upButton;
        this.upButton.setImage(NSImage.imageNamed("arrowUpBlack16.tiff"));
        this.upButton.setTarget(this);
        this.upButton.setAction(new NSSelector("upButtonClicked", new Class[]{Object.class}));
    }

    private NSButton backButton; // IBOutlet

    public void setBackButton(NSButton backButton) {
        this.backButton = backButton;
        this.backButton.setImage(NSImage.imageNamed("arrowLeftBlack16.tiff"));
        this.backButton.setTarget(this);
        this.backButton.setAction(new NSSelector("backButtonClicked", new Class[]{Object.class}));
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
        // receive drag events from types
//        this.pathPopupButton.registerForDraggedTypes(new NSArray(new Object[]{
//            NSPasteboard.FilenamesPboardType //accept files dragged from the Finder for uploading
//        }
//        ));
    }

    public void pathPopupSelectionChanged(Object sender) {
        Path p = (Path) pathPopupItems.get(pathPopupButton.indexOfSelectedItem());
        this.deselectAll();
        p.list(this.encoding, false, this.getFileFilter());
    }

    private static final NSImage FOLDER_ICON = NSImage.imageNamed("folder16.tiff");

    private void addPathToPopup(Path p) {
        this.pathPopupItems.add(p);
        this.pathPopupButton.addItem(p.getAbsolute());
        if (p.isRoot()) {
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
        while (iterator.hasNext()) {
            items[i] = ((java.nio.charset.Charset) iterator.next()).name();
            i++;
        }
        this.encodingPopup.addItemsWithTitles(new NSArray(items));
        this.encodingPopup.setTitle(Preferences.instance().getProperty("browser.charset.encoding"));
    }

    public void setEncoding(String encoding)  {
        this.setEncoding(encoding, true);
    }

    public void setEncoding(String encoding, boolean force) {
        this.encoding = encoding;
        log.info("Encoding changed to:" + this.encoding);
        this.encodingPopup.setTitle(this.encoding);
        if(force) {
            if (this.isMounted()) {
                this.session.close();
                this.reloadButtonClicked(null);
            }
        }
    }

    public void encodingButtonClicked(Object sender) {
        if (sender instanceof NSMenuItem) {
            this.setEncoding(((NSMenuItem) sender).title());
        }
        if (sender instanceof NSPopUpButton) {
            this.setEncoding(this.encodingPopup.titleOfSelectedItem());
        }
    }

    // ----------------------------------------------------------
    // Drawers
    // ----------------------------------------------------------

    private NSDrawer logDrawer; // IBOutlet

    public void setLogDrawer(NSDrawer logDrawer) {
        this.logDrawer = logDrawer;
    }

    public void toggleLogDrawer(Object sender) {
        this.logDrawer.toggle(this);
    }

    private NSDrawer bookmarkDrawer; // IBOutlet

    public void setBookmarkDrawer(NSDrawer bookmarkDrawer) {
        this.bookmarkDrawer = bookmarkDrawer;
        this.bookmarkDrawer.setDelegate(this);
    }

    public void toggleBookmarkDrawer(Object sender) {
        this.bookmarkDrawer.toggle(this);
        Preferences.instance().setProperty("bookmarkDrawer.isOpen", this.bookmarkDrawer.state() == NSDrawer.OpenState || this.bookmarkDrawer.state() == NSDrawer.OpeningState);
        if (this.bookmarkDrawer.state() == NSDrawer.OpenState || this.bookmarkDrawer.state() == NSDrawer.OpeningState) {
            this.window().makeFirstResponder(this.bookmarkTable);
        }
        else {
            if (this.isMounted()) {
				this.getFocus();
            }
            else {
                this.window().makeFirstResponder(this.quickConnectPopup);
            }
        }
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

    public void showTransferQueueClicked(Object sender) {
        CDQueueController controller = CDQueueController.instance();
        controller.window().makeKeyAndOrderFront(null);
    }

	public void reloadButtonClicked(Object sender) {
		if (this.isMounted()) {
			this.deselectAll();
			this.workdir().list(this.encoding, true, this.getFileFilter());
		}
	}
	
    public void editButtonClicked(Object sender) {
        for(Iterator i = this.getSelectedPaths().iterator(); i.hasNext(); ) {
            Path path = (Path) i.next();
            if (path.attributes.isFile()) {
                Editor editor = null;
                if (sender instanceof NSMenuItem) {
					Object identifier = Editor.SUPPORTED_EDITORS.get(((NSMenuItem)sender).title());
					if(identifier != null) {
						editor = new Editor((String)identifier);
					}
				}
				if(null == editor) {
                    editor = new Editor(Preferences.instance().getProperty("editor.bundleIdentifier"));
				}
				editor.open(path);
            }
        }
    }

    public void gotoButtonClicked(Object sender) {
        log.debug("gotoButtonClicked");
        CDGotoController controller = new CDGotoController(this.workdir());
        this.beginSheet(controller.window(), //sheet
                controller, //modal delegate
                new NSSelector("sheetDidEnd",
                        new Class[]{NSPanel.class, int.class, Object.class}), // did end selector
                this.workdir()); //contextInfo
    }

    public void createFileButtonClicked(Object sender) {
        log.debug("createFileButtonClicked:");
        CDFileController controller = new CDCreateFileController();
        this.beginSheet(controller.window(), //sheet
                controller, //modal delegate
                new NSSelector("sheetDidEnd",
                        new Class[]{NSPanel.class, int.class, Object.class}), // did end selector
                this.workdir()); //contextInfo
    }
	
	public void duplicateFileButtonClicked(Object sender) {
        if (this.getSelectionCount() > 0) {
            Path selected = this.getSelectedPath();
			CDFileController controller = new CDDuplicateFileController(selected);
			this.beginSheet(controller.window(), //sheet
							controller, //modal delegate
							new NSSelector("sheetDidEnd",
										   new Class[]{NSPanel.class, int.class, Object.class}), // did end selector
							selected.getParent()); //contextInfo
		}
	}

    public void sendCustomCommandClicked(Object sender) {
        CDCommandController controller = new CDCommandController(this.session);
        this.beginSheet(controller.window(), //sheet
                controller, //modal delegate
                new NSSelector("sheetDidEnd",
                        new Class[]{NSPanel.class, int.class, Object.class}), // did end selector
                null); //contextInfo
    }


    public void createFolderButtonClicked(Object sender) {
        log.debug("createFolderButtonClicked");
        CDFolderController controller = new CDFolderController();
        this.beginSheet(controller.window(), //sheet
                controller, //modal delegate
                new NSSelector("sheetDidEnd",
                        new Class[]{NSPanel.class, int.class, Object.class}), // did end selector
                this.workdir()); //contextInfo
    }
	
    public void infoButtonClicked(Object sender) {
        log.debug("infoButtonClicked");
        if (this.getSelectionCount() > 0) {
            List files = this.getSelectedPaths();
            if (Preferences.instance().getBoolean("browser.info.isInspector")) {
                if (null == this.inspector) {
                    this.inspector = new CDInfoController();
                }
				this.inspector.setFiles(files);
                this.inspector.window().makeKeyAndOrderFront(null);
            }
            else {
                CDInfoController c = new CDInfoController();
				c.setFiles(files);
                c.window().makeKeyAndOrderFront(null);
            }
        }
    }

    public void deleteKeyPerformed(Object sender) {
        log.debug("deleteKeyPerformed:" + sender);
        if (sender == this.bookmarkTable) {
            this.deleteBookmarkButtonClicked(sender);
        }
		else {
            this.deleteFileButtonClicked(sender);
		}
    }

    public void deleteFileButtonClicked(Object sender) {
        log.debug("deleteFileButtonClicked:" + sender);
        List files = new ArrayList();
        StringBuffer alertText = new StringBuffer(NSBundle.localizedString("Really delete the following files? This cannot be undone.", "Confirm deleting files."));
        if (sender instanceof Path) {
            Path p = (Path) sender;
            files.add(p);
            alertText.append("\n- " + p.getName());
        }
        else if(this.getSelectionCount() > 0) {
			int i = 0;
			Iterator iter = null;
			for(iter = this.getSelectedPaths().iterator(); i < 10 && iter.hasNext(); ) {
				Path p = (Path)iter.next();
                files.add(p);
                alertText.append("\n- " + p.getName());
                i++;
            }
            if(iter.hasNext()) {
                alertText.append("\n- (...)");
                while (iter.hasNext()) {
                    files.add(iter.next());
                }
            }
        }
        if (files.size() > 0) {
            this.beginSheet(NSAlertPanel.criticalAlertPanel(NSBundle.localizedString("Delete", "Alert sheet title"), //title
                    alertText.toString(),
                    NSBundle.localizedString("Delete", "Alert sheet default button"), // defaultbutton
                    NSBundle.localizedString("Cancel", "Alert sheet alternate button"), //alternative button
                    null //other button
            ),
                    this,
                    new NSSelector
                            ("deleteSheetDidEnd",
                                    new Class[]
                                    {
                                        NSWindow.class, int.class, Object.class
                                    }),
                    files
            );// end selector
        }
    }

	public void deleteSheetDidEnd(NSWindow sheet, int returncode, Object contextInfo) {
        sheet.orderOut(null);
		if (returncode == NSAlertPanel.DefaultReturn) {
			final List files = (List)contextInfo;
			if(files.size() > 0) {
				this.deselectAll();
				Iterator i = files.iterator();
				Path p = null;
				while(i.hasNext()) {
					p = (Path)i.next();
					p.delete();
				}
				p.getParent().list(encoding, true, this.getFileFilter());
			}
		}
	}

    public void downloadAsButtonClicked(Object sender) {
        Session session = this.session.copy();
        for(Iterator i = this.getSelectedPaths().iterator(); i.hasNext(); ) {
            Path path = ((Path)i.next()).copy(session);
            NSSavePanel panel = NSSavePanel.savePanel();
            NSSelector setMessageSelector =
                    new NSSelector("setMessage", new Class[]{String.class});
            if (setMessageSelector.implementedByClass(NSOpenPanel.class)) {
                panel.setMessage(NSBundle.localizedString("Download the selected file to...", ""));
            }
            NSSelector setNameFieldLabelSelector =
                    new NSSelector("setNameFieldLabel", new Class[]{String.class});
            if (setNameFieldLabelSelector.implementedByClass(NSOpenPanel.class)) {
                panel.setNameFieldLabel(NSBundle.localizedString("Download As:", ""));
            }
            panel.setPrompt(NSBundle.localizedString("Download", ""));
            panel.setTitle(NSBundle.localizedString("Download", ""));
            panel.setCanCreateDirectories(true);
            panel.beginSheetForDirectory(null,
                    path.getLocal().getName(),
                    this.window(),
                    this,
                    new NSSelector("saveAsPanelDidEnd", new Class[]{NSOpenPanel.class, int.class, Object.class}),
                    path);
        }
    }

    public void saveAsPanelDidEnd(NSSavePanel sheet, int returncode, Object contextInfo) {
        if (returncode == NSAlertPanel.DefaultReturn) {
			String filename = null;
			if ((filename = sheet.filename()) != null) {
				Path path = (Path) contextInfo;
				path.setLocal(new Local(filename));
				Queue q = new DownloadQueue();
				q.addRoot(path);
				CDQueueController.instance().startItem(q);
			}
        }
    }

    public void syncButtonClicked(Object sender) {
        log.debug("syncButtonClicked");
        Path selection;
        if(this.getSelectionCount() == 1 &&
                this.getSelectedPath().attributes.isDirectory()) {
            selection = (this.getSelectedPath().copy(this.session.copy()));
        }
        else {
            selection = this.workdir().copy(this.session.copy());
        }
        NSOpenPanel panel = NSOpenPanel.openPanel();
        panel.setCanChooseDirectories(selection.attributes.isDirectory());
        panel.setCanChooseFiles(selection.attributes.isFile());
        panel.setCanCreateDirectories(true);
        panel.setAllowsMultipleSelection(false);
        NSSelector setMessageSelector =
                new NSSelector("setMessage", new Class[]{String.class});
        if (setMessageSelector.implementedByClass(NSOpenPanel.class)) {
            panel.setMessage(NSBundle.localizedString("Synchronize", "")
                    + " " + selection.getName() + " "
                    + NSBundle.localizedString("with", "Synchronize <file> with <file>") + "...");
        }
        panel.setPrompt(NSBundle.localizedString("Choose", ""));
        panel.setTitle(NSBundle.localizedString("Synchronize", ""));
        panel.beginSheetForDirectory(null,
                null,
                null,
                this.window(), //parent window
                this,
                new NSSelector("syncPanelDidEnd", new Class[]{NSOpenPanel.class, int.class, Object.class}),
                selection //context info
        );
    }

    public void syncPanelDidEnd(NSOpenPanel sheet, int returncode, Object contextInfo) {
        if (returncode == NSAlertPanel.DefaultReturn) {
			Path selection = (Path) contextInfo;
			if (sheet.filenames().count() > 0) {
				selection.setLocal(new Local((String) sheet.filenames().lastObject()));
				Queue q = new SyncQueue((Observer) this);
				q.addRoot(selection);
				CDQueueController.instance().startItem(q);
			}
        }
    }

    public void downloadButtonClicked(Object sender) {
        Queue q = new DownloadQueue();
        Session session = this.session.copy();
        for(Iterator i = this.getSelectedPaths().iterator(); i.hasNext(); ) {
            Path path = ((Path)i.next()).copy(session);
            q.addRoot(path);
        }
        CDQueueController.instance().startItem(q);
    }
	
	private String lastSelectedUploadDirectory = null;

    public void uploadButtonClicked(Object sender) {
        log.debug("uploadButtonClicked");
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
                this.window(),
                this,
                new NSSelector("uploadPanelDidEnd", new Class[]{NSOpenPanel.class, int.class, Object.class}),
                null);
    }

    public void uploadPanelDidEnd(NSOpenPanel sheet, int returncode, Object contextInfo) {
        if (returncode == NSAlertPanel.DefaultReturn) {
			Path workdir = this.workdir();
			// selected files on the local filesystem
			NSArray selected = sheet.filenames();
			java.util.Enumeration iterator = selected.objectEnumerator();
			Queue q = new UploadQueue((Observer) this);
			Session session = workdir.getSession().copy();
			while (iterator.hasMoreElements()) {
				q.addRoot(PathFactory.createPath(session,
												 workdir.getAbsolute(),
												 new Local((String) iterator.nextElement())));
			}
			this.lastSelectedUploadDirectory = q.getRoot().getLocal().getParentFile().getAbsolutePath();
			CDQueueController.instance().startItem(q);
        }
    }

    public void insideButtonClicked(Object sender) {
        log.debug("insideButtonClicked");
        this.searchField.setStringValue("");
        if (this.getSelectionCount() > 0) {
            Path p = this.getSelectedPath(); //last row selected
            if (p.attributes.isDirectory()) {
                this.deselectAll();
                p.list(this.encoding, false, this.getFileFilter());
            }
            if (p.attributes.isFile() || this.getSelectionCount() > 1) {
                if (Preferences.instance().getBoolean("browser.doubleclick.edit")) {
                    this.editButtonClicked(null);
                }
                else {
                    this.downloadButtonClicked(null);
                }
            }
        }
	}

    public void backButtonClicked(Object sender) {
        log.debug("backButtonClicked");
		this.deselectAll();
        this.session.getPreviousPath().list(this.encoding, false, this.getFileFilter());
    }

    public void upButtonClicked(Object sender) {
        log.debug("upButtonClicked");
		this.deselectAll();
        Path previous = this.workdir();
        List listing = this.workdir().getParent().list(this.encoding, false, this.getFileFilter());
		this.selectRow(listing.indexOf(previous), false);
    }

    private CDWindowController connectionController;

    public void connectButtonClicked(Object sender) {
        log.debug("connectButtonClicked");
        if(null == connectionController) {
            connectionController = new CDConnectionController(this);
        }
        this.beginSheet(connectionController.window(), connectionController,
                new NSSelector("connectionSheetDidEnd", new Class[]{NSWindow.class, int.class, Object.class}),
                null);
    }

    public void disconnectButtonClicked(Object sender) {
		this.unmount();
		this.reloadData();
    }

    public void showHiddenFilesClicked(Object sender) {
        if (sender instanceof NSMenuItem) {
            NSMenuItem item = (NSMenuItem) sender;
            if(item.state() == NSCell.OnState) {
                this.filenameFilter = new HiddenFilesFilter();
                item.setState(NSCell.OffState);
			}
			else if(item.state() == NSCell.OffState) {
                this.filenameFilter = new NullFilter();
                item.setState(NSCell.OnState);
			}
            if (this.isMounted()) {
				this.deselectAll();
                this.workdir().list(this.encoding, false, this.filenameFilter);
            }
        }
    }

    /**
     *
     * @return true if a connection is being opened or is already initialized
     */
    public boolean hasSession() {
        return this.session != null;
    }

    /**
     *
     * @return true if the remote file system has been mounted
     */
    public boolean isMounted() {
        return this.workdir() != null;
    }

    /**
     *
     * @return true if mounted and the connection to the server is alive
     */
    public boolean isConnected() {
        boolean connected = false;
        if (this.hasSession()) {
            connected = this.session.isConnected();
        }
        return connected;
    }

    public void paste(Object sender) {
        log.debug("paste");
        NSPasteboard pboard = NSPasteboard.pasteboardWithName("QueuePBoard");
        if (pboard.availableTypeFromArray(new NSArray("QueuePBoardType")) != null) {
            Object o = pboard.propertyListForType("QueuePBoardType");// get the data from paste board
            if (o != null) {
                this.deselectAll();
                NSArray elements = (NSArray) o;
                for (int i = 0; i < elements.count(); i++) {
                    NSDictionary dict = (NSDictionary) elements.objectAtIndex(i);
                    Queue q = Queue.createQueue(dict);
                    Path workdir = this.workdir();
                    for (Iterator iter = q.getRoots().iterator(); iter.hasNext();) {
                        Path p = (Path) iter.next();
                        PathFactory.createPath(workdir.getSession(), p.getAbsolute()).rename(workdir.getAbsolute() + "/" + p.getName());
                        p.getParent().invalidate();
                    }
                }
                pboard.setPropertyListForType(null, "QueuePBoardType");
				workdir.list(this.encoding, true, this.getFileFilter());
                this.reloadData();
            }
        }
    }

	public void copyURLButtonClicked(Object sender) {
        log.debug("copyURLButtonClicked");
        Host h = this.session.getHost();
		StringBuffer url = new StringBuffer(h.getURL());
        if (this.getSelectionCount() > 0) {
            Path p = this.getSelectedPath();
			url.append(p.getAbsolute());
		}
		else {
			url.append(this.workdir().getAbsolute());
		}
        NSPasteboard pboard = NSPasteboard.pasteboardWithName(NSPasteboard.GeneralPboard);
        pboard.declareTypes(new NSArray(NSPasteboard.StringPboardType), null);
        if (!pboard.setStringForType(url.toString(), NSPasteboard.StringPboardType)) {
            log.error("Error writing URL to NSPasteboard.StringPboardType.");
        }
    }
		
    public void copy(Object sender) {
        if (this.getSelectionCount() > 0) {
            Path p = this.getSelectedPath();
            NSPasteboard pboard = NSPasteboard.pasteboardWithName(NSPasteboard.GeneralPboard);
            pboard.declareTypes(new NSArray(NSPasteboard.StringPboardType), null);
            if (!pboard.setStringForType(p.getAbsolute(), NSPasteboard.StringPboardType)) {
                log.error("Error writing absolute path of selected item to NSPasteboard.StringPboardType.");
            }
        }
    }

    public void cut(Object sender) {
        if (this.getSelectionCount() > 0) {
            Session session = this.session.copy();
            Queue q = new DownloadQueue();
			for(Iterator i = this.getSelectedPaths().iterator(); i.hasNext(); ) {
				Path path = (Path)i.next();
                q.addRoot(path.copy(session));
            }
            // Writing data for private use when the item gets dragged to the transfer queue.
            NSPasteboard queuePboard = NSPasteboard.pasteboardWithName("QueuePBoard");
            queuePboard.declareTypes(new NSArray("QueuePBoardType"), null);
            if (queuePboard.setPropertyListForType(new NSArray(q.getAsDictionary()), "QueuePBoardType")) {
                log.debug("QueuePBoardType data sucessfully written to pasteboard");
            }
            Path p = this.getSelectedPath();
            NSPasteboard pboard = NSPasteboard.pasteboardWithName(NSPasteboard.GeneralPboard);
            pboard.declareTypes(new NSArray(NSPasteboard.StringPboardType), null);
            if (!pboard.setStringForType(p.getAbsolute(), NSPasteboard.StringPboardType)) {
                log.error("Error writing absolute path of selected item to NSPasteboard.StringPboardType.");
            }
        }
    }

    protected Path workdir() {
        return this.workdir;
    }

    private Observer transcript;

    private void init(Host host, Session session) {
		this.workdir = null;
		this.reloadData();
        session.addObserver(transcript = new CDTranscriptImpl(this.logView));
        this.window().setTitle(host.getProtocol() + ":" + host.getCredentials().getUsername() + "@" + host.getHostname());
        File bookmark = new File(HISTORY_FOLDER + "/" + host.getHostname() + ".duck");
        CDBookmarkTableDataSource.instance().exportBookmark(host, bookmark);
        this.window().setRepresentedFilename(bookmark.getAbsolutePath());
        session.addObserver((Observer) this);
        this.getFocus();
    }

    private Session session;

    public Session mount(Host host) {
        return this.mount(host, this.encoding);
    }

    public Session mount(Host host, final String encoding) {
        log.debug("mount:" + host);
        if (this.unmount(new NSSelector("mountSheetDidEnd",
                new Class[]{NSWindow.class, int.class, Object.class}), host// end selector
        )) {
            if(this.hasSession()) {
                session.deleteObserver((Observer) this);
                session.deleteObserver(this.transcript);
            }
            session = SessionFactory.createSession(host);
            this.init(host, session);
            this.setEncoding(encoding, false);
            if (session instanceof ch.cyberduck.core.sftp.SFTPSession) {
                ((ch.cyberduck.core.sftp.SFTPSession) session).setHostKeyVerificationController(new CDHostKeyController(this));
            }
            if (session instanceof ch.cyberduck.core.ftps.FTPSSession) {
                ((ch.cyberduck.core.ftps.FTPSSession) session).setTrustManager(
                        new CDX509TrustManagerController(this));
            }
            host.setLoginController(new CDLoginController(this));
            new Thread("Session") {
                public void run() {
                    session.mount(encoding, getFileFilter());
                }
            }.start();
            return session;
        }
        return null;
    }

    public void mountSheetDidEnd(NSWindow sheet, int returncode, Object contextInfo) {
        sheet.orderOut(null);
        this.unmountSheetDidEnd(sheet, returncode, contextInfo);
        if (returncode == NSAlertPanel.DefaultReturn) {
            this.mount((Host) contextInfo);
        }
    }

    public void unmount() {
        if (this.hasSession()) {
            this.session.close();
        }
    }

    /**
     * @return True if the unmount process has finished, false if the user has to agree first to close the connection
     */
    public boolean unmount(NSSelector selector, Object context) {
        log.debug("unmount");
        if (this.isConnected()) {
            if (Preferences.instance().getBoolean("browser.confirmDisconnect")) {
                this.beginSheet(NSAlertPanel.criticalAlertPanel(NSBundle.localizedString("Disconnect from", "Alert sheet title") + " " + this.session.getHost().getHostname(), //title
                        NSBundle.localizedString("The connection will be closed.", "Alert sheet text"), // message
                        NSBundle.localizedString("Disconnect", "Alert sheet default button"), // defaultbutton
                        NSBundle.localizedString("Cancel", "Alert sheet alternate button"), // alternate button
                        null //other button
                ),
                        this,
                        selector,
                        context);
                return false;
            }
            this.unmount();
        }
        else {
            if(this.hasSession()) {
                this.unmount();
            }
        }
        return true;
    }

    public boolean loadDataRepresentation(NSData data, String type) {
        if (type.equals("Cyberduck Bookmark")) {
            String[] errorString = new String[]{null};
            Object propertyListFromXMLData =
                    NSPropertyListSerialization.propertyListFromData(data,
                            NSPropertyListSerialization.PropertyListImmutable,
                            new int[]{NSPropertyListSerialization.PropertyListXMLFormat},
                            errorString);
            if (errorString[0] != null) {
                log.error("Problem reading bookmark file: " + errorString[0]);
            }
            else {
                log.debug("Successfully read bookmark file: " + propertyListFromXMLData);
            }
            if (propertyListFromXMLData instanceof NSDictionary) {
                this.mount(new Host((NSDictionary) propertyListFromXMLData));
            }
            return true;
        }
        return false;
    }

    public NSData dataRepresentationOfType(String type) {
        if (this.isMounted()) {
            if (type.equals("Cyberduck Bookmark")) {
                Host bookmark = this.session.getHost();
                NSMutableData collection = new NSMutableData();
                String[] errorString = new String[]{null};
                collection.appendData(NSPropertyListSerialization.dataFromPropertyList(bookmark.getAsDictionary(),
                        NSPropertyListSerialization.PropertyListXMLFormat,
                        errorString));
                if (errorString[0] != null) {
                    log.error("Problem writing bookmark file: " + errorString[0]);
                }
                return collection;
            }
        }
        return null;
    }

    // ----------------------------------------------------------
    // Window delegate methods
    // ----------------------------------------------------------

    public static int applicationShouldTerminate(NSApplication app) {
        // Determine if there are any open connections
        NSArray windows = NSApplication.sharedApplication().windows();
        int count = windows.count();
        // Determine if there are any open connections
        while (0 != count--) {
            NSWindow window = (NSWindow) windows.objectAtIndex(count);
            CDBrowserController controller = CDBrowserController.controllerForWindow(window);
            if (null != controller) {
                if (!controller.unmount(new NSSelector("terminateReviewSheetDidEnd",
                        new Class[]{NSWindow.class, int.class, Object.class}),
                        null)) {
                    return NSApplication.TerminateLater;
                }
            }
        }
        return CDQueueController.applicationShouldTerminate(app);
    }

    public boolean windowShouldClose(NSWindow sender) {
        return this.unmount(new NSSelector("closeSheetDidEnd",
                new Class[]{NSWindow.class, int.class, Object.class}), null // end selector
        );
    }

    public void unmountSheetDidEnd(NSWindow sheet, int returncode, Object contextInfo) {
        sheet.orderOut(null);
        if (returncode == NSAlertPanel.DefaultReturn) {
            this.unmount();
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
        sheet.orderOut(null);
        this.closeSheetDidEnd(sheet, returncode, contextInfo);
        if (returncode == NSAlertPanel.DefaultReturn) { //Disconnect
            CDBrowserController.applicationShouldTerminate(null);
        }
        if (returncode == NSAlertPanel.AlternateReturn) { //Cancel
            NSApplication.sharedApplication().replyToApplicationShouldTerminate(false);
        }
    }

    public void windowWillClose(NSNotification notification) {
        NSNotificationCenter.defaultCenter().removeObserver(this);
        if (this.hasSession()) {
            this.session.deleteObserver((Observer) this);
        }
        instances.removeObject(this);
    }

    public boolean validateMenuItem(NSMenuItem item) {
        String identifier = item.action().name();
        if (item.action().name().equals("paste:")) {
            if (this.isMounted()) {
                NSPasteboard pboard = NSPasteboard.pasteboardWithName("QueuePBoard");
                if (pboard.availableTypeFromArray(new NSArray("QueuePBoardType")) != null
                        && pboard.propertyListForType("QueuePBoardType") != null) {
                    NSArray elements = (NSArray) pboard.propertyListForType("QueuePBoardType");
                    for (int i = 0; i < elements.count(); i++) {
                        NSDictionary dict = (NSDictionary) elements.objectAtIndex(i);
                        Queue q = Queue.createQueue(dict);
                        if (q.numberOfRoots() == 1)
                            item.setTitle(NSBundle.localizedString("Paste", "Menu item") + " \"" + q.getRoot().getName() + "\"");
                        else {
                            item.setTitle(NSBundle.localizedString("Paste", "Menu item")
                                    + " " + q.numberOfRoots() + " " +
                                    NSBundle.localizedString("files", ""));
                        }
                    }
                }
                else {
                    item.setTitle(NSBundle.localizedString("Paste", "Menu item"));
                }
            }
        }
        if (identifier.equals("cut:")) {
            if (this.isMounted() && this.getSelectionCount() > 0) {
                if (this.getSelectionCount() == 1) {
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
        if (identifier.equals("editButtonClicked:")) {
            if(this.isMounted() && this.getSelectionCount() > 0) {
                if(this.getSelectedPath().attributes.isFile()) {
                    String bundleIdentifier = (String)Editor.SUPPORTED_EDITORS.get(item.title());
                    if(null != bundleIdentifier)
                        return NSWorkspace.sharedWorkspace().absolutePathForAppBundleWithIdentifier(
                                bundleIdentifier) != null;
                }
            }
        }
        if (identifier.equals("showHiddenFilesClicked:")) {
            item.setState((this.getFileFilter() instanceof NullFilter) ? NSCell.OnState : NSCell.OffState);
        }
        if (identifier.equals("encodingButtonClicked:")) {
            item.setState(this.encoding.equalsIgnoreCase(item.title()) ? NSCell.OnState : NSCell.OffState);
        }
        if (identifier.equals("browserSwitchClicked:")) {
			if(item.tag() == Preferences.instance().getInteger("browser.view")) {
				item.setState(NSCell.OnState);
            }
			else {
				item.setState(NSCell.OffState);
            }
        }
        return this.validateItem(identifier);
    }

    private boolean validateItem(String identifier) {
        if (identifier.equals("copy:")) {
            return this.isMounted() && this.getSelectionCount() > 0;
        }
        if (identifier.equals("cut:")) {
            return this.isMounted() && this.getSelectionCount() > 0;
        }
        if (identifier.equals("paste:")) {
            NSPasteboard pboard = NSPasteboard.pasteboardWithName("QueuePBoard");
            return this.isMounted()
                    && pboard.availableTypeFromArray(new NSArray("QueuePBoardType")) != null
                    && pboard.propertyListForType("QueuePBoardType") != null;
        }
        if (identifier.equals("showHiddenFilesClicked:")) {
            return true;
        }
        if (identifier.equals("encodingButtonClicked:")) {
            return true;
        }
        if (identifier.equals("addBookmarkButtonClicked:")) {
            return true;
        }
        if (identifier.equals("deleteBookmarkButtonClicked:")) {
            return bookmarkTable.selectedRow() != -1;
        }
        if (identifier.equals("editBookmarkButtonClicked:")) {
            return bookmarkTable.numberOfSelectedRows() == 1;
        }
		if(identifier.equals("Edit") || identifier.equals("editButtonClicked:")) {
            if(this.isMounted() && this.getSelectionCount() > 0) {
                Path selected = this.getSelectedPath();
                if(selected.attributes.isFile()) {
                    if(null == selected.getExtension()) {
                        return true;
                    }
                    if (Preferences.instance().getProperty("editor.disabledFiles").indexOf(selected.getExtension()) != -1) {
                        return false;
                    }
                    return true;
                }
            }
            return false;
		}
        if(identifier.equals("sendCustomCommandClicked:")) {
            return (this.session instanceof FTPSession) && this.isConnected();
        }
        if (identifier.equals("gotoButtonClicked:")) {
            return this.isMounted();
        }
        if (identifier.equals("Get Info") || identifier.equals("infoButtonClicked:")) {
            return this.isMounted() && this.getSelectionCount() > 0;
        }
        if (identifier.equals("New Folder") || identifier.equals("createFolderButtonClicked:")) {
            return this.isMounted();
        }
        if (identifier.equals("New File") || identifier.equals("createFileButtonClicked:")) {
            return this.isMounted();
        }
        if (identifier.equals("Duplicate File") || identifier.equals("duplicateFileButtonClicked:")) {
            return this.isMounted() && this.getSelectionCount() > 0;
        }
        if (identifier.equals("Delete") || identifier.equals("deleteFileButtonClicked:")) {
            return this.isMounted() && this.getSelectionCount() > 0;
        }
        if (identifier.equals("Refresh") || identifier.equals("reloadButtonClicked:")) {
            return this.isMounted();
        }
        if (identifier.equals("Download") || identifier.equals("downloadButtonClicked:")) {
            return this.isMounted() && this.getSelectionCount() > 0;
        }
        if (identifier.equals("Upload") || identifier.equals("uploadButtonClicked:")) {
            return this.isMounted();
        }
        if (identifier.equals("Synchronize") || identifier.equals("syncButtonClicked:")) {
            return this.isMounted();
        }
        if (identifier.equals("Download As") || identifier.equals("downloadAsButtonClicked:")) {
            return this.isMounted() && this.getSelectionCount() == 1;
        }
        if (identifier.equals("insideButtonClicked:")) {
            return this.isMounted() && this.getSelectionCount() > 0;
        }
        if (identifier.equals("upButtonClicked:")) {
            return this.isMounted() && !this.workdir().isRoot();
        }
        if (identifier.equals("backButtonClicked:")) {
            return this.isMounted();
        }
        if (identifier.equals("copyURLButtonClicked:")) {
            return this.isMounted();
        }
        if (identifier.equals("Disconnect") || identifier.equals("disconnectButtonClicked:")) {
            return this.isMounted() && this.isConnected();
        }
        return true; // by default everything is enabled
    }

    // ----------------------------------------------------------
    // Toolbar Delegate
    // ----------------------------------------------------------

    public boolean validateToolbarItem(NSToolbarItem item) {
        boolean enabled = this.pathPopupItems.size() > 0;
        this.backButton.setEnabled(enabled);
        this.upButton.setEnabled(enabled);
        this.pathPopupButton.setEnabled(enabled);
        this.searchField.setEnabled(enabled);
        String identifier = item.itemIdentifier();
        if(identifier.equals("Edit")) {
            NSSelector absolutePathForAppBundleWithIdentifierSelector =
                    new NSSelector("absolutePathForAppBundleWithIdentifier", new Class[]{String.class});
            if (absolutePathForAppBundleWithIdentifierSelector.implementedByClass(NSWorkspace.class)) {
                String editorPath = NSWorkspace.sharedWorkspace().absolutePathForAppBundleWithIdentifier(
                        Preferences.instance().getProperty("editor.bundleIdentifier"));
                if (editorPath != null) {
                    item.setImage(NSWorkspace.sharedWorkspace().iconForFile(editorPath));
                }
                else {
                    item.setImage(NSImage.imageNamed("pencil.tiff"));
                }
            }
        }
        return this.validateItem(identifier);
    }

    public NSToolbarItem toolbarItemForItemIdentifier(NSToolbar toolbar, String itemIdentifier, boolean flag) {
        NSToolbarItem item = new NSToolbarItem(itemIdentifier);
        if (itemIdentifier.equals("Browser View")) {
            item.setLabel(NSBundle.localizedString("View", "Toolbar item"));
            item.setPaletteLabel(NSBundle.localizedString("View", "Toolbar item"));
            item.setToolTip(NSBundle.localizedString("Switch Browser View", "Toolbar item tooltip"));
            item.setView(browserSwitchView);
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
			item.setMinSize(browserSwitchView.frame().size());
            item.setMaxSize(browserSwitchView.frame().size());
            return item;
        }
        if (itemIdentifier.equals("New Connection")) {
            item.setLabel(NSBundle.localizedString("New Connection", "Toolbar item"));
            item.setPaletteLabel(NSBundle.localizedString("New Connection", "Toolbar item"));
            item.setToolTip(NSBundle.localizedString("Connect to server", "Toolbar item tooltip"));
            item.setImage(NSImage.imageNamed("connect.tiff"));
            item.setTarget(this);
            item.setAction(new NSSelector("connectButtonClicked", new Class[]{Object.class}));
            return item;
        }
        if (itemIdentifier.equals("Bookmarks")) {
            item.setLabel(NSBundle.localizedString("Bookmarks", "Toolbar item"));
            item.setPaletteLabel(NSBundle.localizedString("Bookmarks", "Toolbar item"));
            item.setToolTip(NSBundle.localizedString("Toggle Bookmarks", "Toolbar item tooltip"));
            item.setImage(NSImage.imageNamed("drawer.tiff"));
            item.setTarget(this);
            item.setAction(new NSSelector("toggleBookmarkDrawer", new Class[]{Object.class}));
            return item;
        }
        if (itemIdentifier.equals("Transfers")) {
            item.setLabel(NSBundle.localizedString("Transfers", "Toolbar item"));
            item.setPaletteLabel(NSBundle.localizedString("Transfers", "Toolbar item"));
            item.setToolTip(NSBundle.localizedString("Show Transfers window", "Toolbar item tooltip"));
            item.setImage(NSImage.imageNamed("queue.tiff"));
            item.setTarget(this);
            item.setAction(new NSSelector("showTransferQueueClicked", new Class[]{Object.class}));
            return item;
        }
		if (itemIdentifier.equals("Tools")) {
            item.setLabel(NSBundle.localizedString("Action", "Toolbar item"));
            item.setPaletteLabel(NSBundle.localizedString("Action", "Toolbar item"));
            item.setView(actionPopupButton);
            NSMenuItem toolMenu = new NSMenuItem();
			toolMenu.setTitle(NSBundle.localizedString("Action", "Toolbar item"));
            NSMenu toolSubmenu = new NSMenu();
			for(int i = 1; i < actionPopupButton.menu().numberOfItems(); i++) {
				NSMenuItem template = actionPopupButton.menu().itemAtIndex(i);
				toolSubmenu.addItem(new NSMenuItem(template.title(),
												   template.action(),
												   template.keyEquivalent()));
			}
            toolMenu.setSubmenu(toolSubmenu);
            item.setMenuFormRepresentation(toolMenu);
            item.setMinSize(actionPopupButton.frame().size());
            item.setMaxSize(actionPopupButton.frame().size());
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
        if (itemIdentifier.equals("Encoding")) {
            item.setLabel(NSBundle.localizedString("Encoding", "Toolbar item"));
            item.setPaletteLabel(NSBundle.localizedString("Encoding", "Toolbar item"));
            item.setToolTip(NSBundle.localizedString("Character Encoding", "Toolbar item tooltip"));
            item.setView(encodingPopup);
            NSMenuItem encodingMenu = new NSMenuItem(NSBundle.localizedString("Encoding", "Toolbar item"),
                    new NSSelector("encodingButtonClicked", new Class[]{Object.class}),
                    "");
            java.util.SortedMap charsets = java.nio.charset.Charset.availableCharsets();
            java.util.Iterator iter = charsets.values().iterator();
            NSMenu charsetMenu = new NSMenu();
            while (iter.hasNext()) {
                charsetMenu.addItem(new NSMenuItem(((java.nio.charset.Charset) iter.next()).name(),
                        new NSSelector("encodingButtonClicked", new Class[]{Object.class}),
                        ""));
            }
            encodingMenu.setSubmenu(charsetMenu);
            item.setMenuFormRepresentation(encodingMenu);
            item.setMinSize(encodingPopup.frame().size());
            item.setMaxSize(encodingPopup.frame().size());
            return item;
        }
        if (itemIdentifier.equals("Refresh")) {
            item.setLabel(NSBundle.localizedString("Refresh", "Toolbar item"));
            item.setPaletteLabel(NSBundle.localizedString("Refresh", "Toolbar item"));
            item.setToolTip(NSBundle.localizedString("Refresh directory listing", "Toolbar item tooltip"));
            item.setImage(NSImage.imageNamed("reload.tiff"));
            item.setTarget(this);
            item.setAction(new NSSelector("reloadButtonClicked", new Class[]{Object.class}));
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
        if (itemIdentifier.equals("Synchronize")) {
            item.setLabel(NSBundle.localizedString("Synchronize", "Toolbar item"));
            item.setPaletteLabel(NSBundle.localizedString("Synchronize", "Toolbar item"));
            item.setToolTip(NSBundle.localizedString("Synchronize files", "Toolbar item tooltip"));
            item.setImage(NSImage.imageNamed("sync32.tiff"));
            item.setTarget(this);
            item.setAction(new NSSelector("syncButtonClicked", new Class[]{Object.class}));
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
                String editorPath = NSWorkspace.sharedWorkspace().absolutePathForAppBundleWithIdentifier(
                        Preferences.instance().getProperty("editor.bundleIdentifier"));
                if (editorPath != null) {
                    item.setImage(NSWorkspace.sharedWorkspace().iconForFile(editorPath));
                }
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
                String editor = (String)editorNames.next();
                String identifier = (String)editorIdentifiers.next();
                boolean enabled = NSWorkspace.sharedWorkspace().absolutePathForAppBundleWithIdentifier(
                        identifier) != null;
                if(enabled) {
                    editMenu.addItem(new NSMenuItem(editor,
                            new NSSelector("editButtonClicked", new Class[]{Object.class}),
                            ""));
                }
            }
            toolbarMenu.setSubmenu(editMenu);
            item.setMenuFormRepresentation(toolbarMenu);
            return item;
        }
        if (itemIdentifier.equals("Delete")) {
            item.setLabel(NSBundle.localizedString("Delete", "Toolbar item"));
            item.setPaletteLabel(NSBundle.localizedString("Delete", "Toolbar item"));
            item.setToolTip(NSBundle.localizedString("Delete file", "Toolbar item tooltip"));
            item.setImage(NSImage.imageNamed("deleteFile.tiff"));
            item.setTarget(this);
            item.setAction(new NSSelector("deleteFileButtonClicked", new Class[]{Object.class}));
            return item;
        }
        if (itemIdentifier.equals("New Folder")) {
            item.setLabel(NSBundle.localizedString("New Folder", "Toolbar item"));
            item.setPaletteLabel(NSBundle.localizedString("New Folder", "Toolbar item"));
            item.setToolTip(NSBundle.localizedString("Create New Folder", "Toolbar item tooltip"));
            item.setImage(NSImage.imageNamed("newfolder.tiff"));
            item.setTarget(this);
            item.setAction(new NSSelector("createFolderButtonClicked", new Class[]{Object.class}));
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
			"Tools",
            NSToolbarItem.SeparatorItemIdentifier,
			"Refresh",
			"Edit",
			NSToolbarItem.FlexibleSpaceItemIdentifier,
			"Disconnect"
		});
	}

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
}