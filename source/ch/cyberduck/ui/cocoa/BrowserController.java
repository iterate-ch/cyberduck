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

import ch.cyberduck.core.*;
import ch.cyberduck.core.aquaticprime.LicenseFactory;
import ch.cyberduck.core.editor.Editor;
import ch.cyberduck.core.editor.EditorFactory;
import ch.cyberduck.core.exception.AccessDeniedException;
import ch.cyberduck.core.exception.BackgroundException;
import ch.cyberduck.core.exception.ConnectionCanceledException;
import ch.cyberduck.core.features.Command;
import ch.cyberduck.core.features.Compress;
import ch.cyberduck.core.features.Location;
import ch.cyberduck.core.features.Move;
import ch.cyberduck.core.features.Symlink;
import ch.cyberduck.core.features.Touch;
import ch.cyberduck.core.features.Versioning;
import ch.cyberduck.core.local.Application;
import ch.cyberduck.core.local.ApplicationFinder;
import ch.cyberduck.core.local.ApplicationFinderFactory;
import ch.cyberduck.core.local.BrowserLauncherFactory;
import ch.cyberduck.core.local.TemporaryFileServiceFactory;
import ch.cyberduck.core.serializer.HostDictionary;
import ch.cyberduck.core.sftp.SFTPSession;
import ch.cyberduck.core.ssl.SSLSession;
import ch.cyberduck.core.threading.BackgroundAction;
import ch.cyberduck.core.threading.DefaultMainAction;
import ch.cyberduck.core.threading.MainAction;
import ch.cyberduck.core.transfer.CopyTransfer;
import ch.cyberduck.core.transfer.DisabledTransferErrorCallback;
import ch.cyberduck.core.transfer.DownloadTransfer;
import ch.cyberduck.core.transfer.SyncTransfer;
import ch.cyberduck.core.transfer.Transfer;
import ch.cyberduck.core.transfer.TransferAction;
import ch.cyberduck.core.transfer.TransferAdapter;
import ch.cyberduck.core.transfer.TransferCallback;
import ch.cyberduck.core.transfer.TransferItem;
import ch.cyberduck.core.transfer.TransferOptions;
import ch.cyberduck.core.transfer.TransferProgress;
import ch.cyberduck.core.transfer.TransferPrompt;
import ch.cyberduck.core.transfer.UploadTransfer;
import ch.cyberduck.core.urlhandler.SchemeHandlerFactory;
import ch.cyberduck.ui.LoginControllerFactory;
import ch.cyberduck.ui.action.DeleteWorker;
import ch.cyberduck.ui.action.DisconnectWorker;
import ch.cyberduck.ui.action.MountWorker;
import ch.cyberduck.ui.action.MoveWorker;
import ch.cyberduck.ui.action.RevertWorker;
import ch.cyberduck.ui.browser.RegexFilter;
import ch.cyberduck.ui.browser.SearchFilter;
import ch.cyberduck.ui.browser.UploadTargetFinder;
import ch.cyberduck.ui.cocoa.application.*;
import ch.cyberduck.ui.cocoa.delegate.ArchiveMenuDelegate;
import ch.cyberduck.ui.cocoa.delegate.CopyURLMenuDelegate;
import ch.cyberduck.ui.cocoa.delegate.EditMenuDelegate;
import ch.cyberduck.ui.cocoa.delegate.OpenURLMenuDelegate;
import ch.cyberduck.ui.cocoa.delegate.URLMenuDelegate;
import ch.cyberduck.ui.cocoa.foundation.NSArray;
import ch.cyberduck.ui.cocoa.foundation.NSAttributedString;
import ch.cyberduck.ui.cocoa.foundation.NSEnumerator;
import ch.cyberduck.ui.cocoa.foundation.NSIndexSet;
import ch.cyberduck.ui.cocoa.foundation.NSNotification;
import ch.cyberduck.ui.cocoa.foundation.NSNotificationCenter;
import ch.cyberduck.ui.cocoa.foundation.NSObject;
import ch.cyberduck.ui.cocoa.foundation.NSRange;
import ch.cyberduck.ui.cocoa.foundation.NSString;
import ch.cyberduck.ui.cocoa.quicklook.QLPreviewPanel;
import ch.cyberduck.ui.cocoa.quicklook.QLPreviewPanelController;
import ch.cyberduck.ui.cocoa.quicklook.QuickLook;
import ch.cyberduck.ui.cocoa.quicklook.QuickLookFactory;
import ch.cyberduck.ui.cocoa.threading.BrowserControllerBackgroundAction;
import ch.cyberduck.ui.cocoa.threading.WindowMainAction;
import ch.cyberduck.ui.cocoa.view.BookmarkCell;
import ch.cyberduck.ui.cocoa.view.OutlineCell;
import ch.cyberduck.ui.pasteboard.PathPasteboard;
import ch.cyberduck.ui.pasteboard.PathPasteboardFactory;
import ch.cyberduck.ui.resources.IconCacheFactory;
import ch.cyberduck.ui.threading.TransferBackgroundAction;
import ch.cyberduck.ui.threading.WorkerBackgroundAction;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.rococoa.Foundation;
import org.rococoa.ID;
import org.rococoa.Rococoa;
import org.rococoa.Selector;
import org.rococoa.cocoa.CGFloat;
import org.rococoa.cocoa.foundation.NSInteger;
import org.rococoa.cocoa.foundation.NSPoint;
import org.rococoa.cocoa.foundation.NSRect;
import org.rococoa.cocoa.foundation.NSSize;
import org.rococoa.cocoa.foundation.NSUInteger;

import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * @version $Id$
 */
public class BrowserController extends WindowController
        implements ProgressListener, TranscriptListener, NSToolbar.Delegate, QLPreviewPanelController {
    private static Logger log = Logger.getLogger(BrowserController.class);

    /**
     * No file filter.
     */
    private static final Filter<Path> NULL_FILTER = new NullPathFilter<Path>();

    /**
     * Filter hidden files.
     */
    private static final Filter<Path> HIDDEN_FILTER = new RegexFilter();

    /**
     *
     */
    private Session<?> session;

    /**
     * Log Drawer
     */
    private TranscriptController transcript;

    private final QuickLook quicklook = QuickLookFactory.get();

    private Preferences preferences
            = Preferences.instance();

    /**
     * Hide files beginning with '.'
     */
    private boolean showHiddenFiles;

    private Filter<Path> filenameFilter;

    {
        if(Preferences.instance().getBoolean("browser.showHidden")) {
            this.filenameFilter = new NullPathFilter<Path>();
            this.showHiddenFiles = true;
        }
        else {
            this.filenameFilter = new RegexFilter();
            this.showHiddenFiles = false;
        }
    }

    private final NSTextFieldCell outlineCellPrototype = OutlineCell.outlineCell();
    private final NSImageCell imageCellPrototype = NSImageCell.imageCell();
    private final NSTextFieldCell textCellPrototype = NSTextFieldCell.textFieldCell();
    private final NSTextFieldCell filenameCellPrototype = NSTextFieldCell.textFieldCell();

    private final TableColumnFactory browserListColumnsFactory = new TableColumnFactory();
    private final TableColumnFactory browserOutlineColumnsFactory = new TableColumnFactory();
    private final TableColumnFactory bookmarkTableColumnFactory = new TableColumnFactory();

    // setting appearance attributes()
    private final NSLayoutManager layoutManager = NSLayoutManager.layoutManager();

    private BrowserOutlineViewModel browserOutlineModel;
    @Outlet
    private NSOutlineView browserOutlineView;
    private AbstractBrowserTableDelegate<Path> browserOutlineViewDelegate;

    private BrowserListViewModel browserListModel;
    @Outlet
    private NSTableView browserListView;
    private AbstractBrowserTableDelegate<Path> browserListViewDelegate;

    private NSToolbar toolbar;

    private final Navigation navigation = new Navigation();

    private PathPasteboard pasteboard;

    /**
     * Caching files listings of previously listed directories
     */
    private Cache<Path> cache
            = new Cache<Path>();

    public BrowserController() {
        this.loadBundle();
    }

    @Override
    protected String getBundleName() {
        return "Browser";
    }

    protected void validateToolbar() {
        this.window().toolbar().validateVisibleItems();
    }

    public static void updateBookmarkTableRowHeight() {
        for(BrowserController controller : MainController.getBrowsers()) {
            controller._updateBookmarkCell();
        }
    }

    public static void updateBrowserTableAttributes() {
        for(BrowserController controller : MainController.getBrowsers()) {
            controller._updateBrowserAttributes(controller.browserListView);
            controller._updateBrowserAttributes(controller.browserOutlineView);
        }
    }

    public static void updateBrowserTableColumns() {
        for(BrowserController controller : MainController.getBrowsers()) {
            controller._updateBrowserColumns(controller.browserListView, controller.browserListViewDelegate);
            controller._updateBrowserColumns(controller.browserOutlineView, controller.browserOutlineViewDelegate);
        }
    }

    @Override
    public void awakeFromNib() {
        super.awakeFromNib();
        // Configure Toolbar
        this.toolbar = NSToolbar.toolbarWithIdentifier("Cyberduck Toolbar");
        this.toolbar.setDelegate((this.id()));
        this.toolbar.setAllowsUserCustomization(true);
        this.toolbar.setAutosavesConfiguration(true);
        this.window().setToolbar(toolbar);
        this.window().makeFirstResponder(quickConnectPopup);
        this._updateBrowserColumns(browserListView, browserListViewDelegate);
        this._updateBrowserColumns(browserOutlineView, browserOutlineViewDelegate);
        if(preferences.getBoolean("browser.transcript.open")) {
            this.logDrawer.open();
        }
        if(LicenseFactory.find().equals(LicenseFactory.EMPTY_LICENSE)) {
            this.addDonateWindowTitle();
        }
        this.setNavigation(false);
        this.selectBookmarks();
    }

    protected Comparator<Path> getComparator() {
        return this.getSelectedBrowserDelegate().getSortingComparator();
    }

    protected Filter<Path> getFilter() {
        return this.filenameFilter;
    }

    public PathPasteboard getPasteboard() {
        return pasteboard;
    }

    protected void setPathFilter(final String search) {
        if(log.isDebugEnabled()) {
            log.debug(String.format("Set path filter to %s", search));
        }
        if(StringUtils.isBlank(search)) {
            this.searchField.setStringValue(StringUtils.EMPTY);
            // Revert to the last used default filter
            if(this.isShowHiddenFiles()) {
                this.filenameFilter = NULL_FILTER;
            }
            else {
                this.filenameFilter = HIDDEN_FILTER;
            }
        }
        else {
            // Setting up a custom filter for the directory listing
            this.filenameFilter = new SearchFilter(cache, search);
        }
    }

    public void setShowHiddenFiles(boolean showHidden) {
        if(showHidden) {
            this.filenameFilter = NULL_FILTER;
            this.showHiddenFiles = true;
        }
        else {
            this.filenameFilter = HIDDEN_FILTER;
            this.showHiddenFiles = false;
        }
    }

    public boolean isShowHiddenFiles() {
        return this.showHiddenFiles;
    }

    /**
     * Marks the current browser as the first responder
     */
    private void getFocus() {
        NSView view;
        if(this.getSelectedTabView() == TAB_BOOKMARKS) {
            view = bookmarkTable;
        }
        else {
            if(this.isMounted()) {
                view = this.getSelectedBrowserView();
            }
            else {
                view = quickConnectPopup;
            }
        }
        this.setStatus();
        this.window().makeFirstResponder(view);
    }

    /**
     * @param preserveSelection All selected files should be reselected after reloading the view
     */
    public void reload(boolean preserveSelection) {
        this.reload(preserveSelection, true);
    }

    /**
     * @param preserveSelection All selected files should be reselected after reloading the view
     * @param scroll            Scroll to current selection
     */
    public void reload(boolean preserveSelection, boolean scroll) {
        this.reload(Collections.<Path>emptyList(), preserveSelection, scroll);
    }

    /**
     * @param changed           Modified files. Invalidate its parents
     * @param preserveSelection All selected files should be reselected after reloading the view
     */
    public void reload(final List<Path> changed, boolean preserveSelection) {
        this.reload(changed, preserveSelection, true);
    }

    /**
     * @param preserveSelection All selected files should be reselected after reloading the view
     * @param scroll            Scroll to current selection
     */
    public void reload(final List<Path> changed, boolean preserveSelection, boolean scroll) {
        if(preserveSelection) {
            //Remember the previously selected paths
            this.reload(changed, this.getSelectedPaths(), scroll);
        }
        else {
            this.reload(changed, Collections.<Path>emptyList(), scroll);
        }
    }

    /**
     * Make the broser reload its content. Will make use of the cache.
     *
     * @param selected The items to be selected
     */
    protected void reload(final List<Path> selected) {
        this.reload(Collections.<Path>emptyList(), selected);
    }

    /**
     * Make the broser reload its content. Will make use of the cache.
     *
     * @param selected The items to be selected
     */
    protected void reload(final List<Path> changed, final List<Path> selected) {
        this.reload(changed, selected, true);
    }

    protected void reload(final List<Path> changed, final List<Path> selected, boolean scroll) {
        if(log.isDebugEnabled()) {
            log.debug(String.format("Reload data with selected files %s", selected));
        }
        for(Path p : changed) {
            // This will force the model to list this directory
            cache.invalidate(p.getParent().getReference());
        }
        // Tell the browser view to reload the data. This will request all paths from the browser model
        // which will refetch paths from the server marked as invalid.
        final NSTableView browser = this.getSelectedBrowserView();
        browser.reloadData();
        if(changed.isEmpty()) {
            browser.deselectAll(null);
            for(Path path : selected) {
                this.selectRow(path.getReference(), true, scroll);
                // Only scroll to the first in the list
                scroll = false;
            }
        }
        this.setStatus();
    }

    private void selectRow(final PathReference reference, final boolean expand, final boolean scroll) {
        if(log.isDebugEnabled()) {
            log.debug(String.format("Select row with reference %s", reference));
        }
        final NSTableView browser = this.getSelectedBrowserView();
        int row = this.getSelectedBrowserModel().indexOf(browser, reference);
        if(log.isDebugEnabled()) {
            log.debug(String.format("Select row at index :%d", row));
        }
        if(-1 == row) {
            return;
        }
        final NSInteger index = new NSInteger(row);
        browser.selectRowIndexes(NSIndexSet.indexSetWithIndex(index), expand);
        if(scroll) {
            browser.scrollRowToVisible(index);
        }
    }

    private void updateQuickLookSelection(final List<Path> selected) {
        if(quicklook.isAvailable()) {
            final List<TransferItem> downloads = new ArrayList<TransferItem>();
            for(Path path : selected) {
                if(!path.isFile()) {
                    continue;
                }
                downloads.add(new TransferItem(
                        path, TemporaryFileServiceFactory.get().create(session.getHost().getUuid(), path)));
            }
            if(downloads.size() > 0) {
                final Transfer download = new DownloadTransfer(session.getHost(), downloads);
                final TransferOptions options = new TransferOptions();
                background(new TransferBackgroundAction(this, session, new TransferAdapter() {
                    @Override
                    public void progress(final TransferProgress status) {
                        message(status.getProgress());
                    }
                }, this, download, options,
                        new TransferPrompt() {
                            @Override
                            public TransferAction prompt() {
                                return TransferAction.comparison;
                            }

                            @Override
                            public boolean isSelected(final TransferItem file) {
                                return true;
                            }

                            @Override
                            public void message(final String message) {
                                BrowserController.this.message(message);
                            }
                        }, new DisabledTransferErrorCallback()
                ) {
                    @Override
                    public void cleanup() {
                        super.cleanup();
                        final List<Local> previews = new ArrayList<Local>();
                        for(TransferItem download : downloads) {
                            previews.add(download.local);
                        }
                        // Change files in Quick Look
                        quicklook.select(previews);
                        // Open Quick Look Preview Panel
                        quicklook.open();
                    }

                    @Override
                    public String getActivity() {
                        return LocaleFactory.localizedString("Quick Look", "Status");
                    }
                });
            }
        }
    }

    /**
     * @return The first selected path found or null if there is no selection
     */
    protected Path getSelectedPath() {
        final List<Path> s = this.getSelectedPaths();
        if(s.size() > 0) {
            return s.get(0);
        }
        return null;
    }

    /**
     * @return All selected paths or an empty list if there is no selection
     */
    protected List<Path> getSelectedPaths() {
        final BrowserTableDataSource model = this.getSelectedBrowserModel();
        final AbstractBrowserTableDelegate<Path> delegate = this.getSelectedBrowserDelegate();
        final NSTableView view = this.getSelectedBrowserView();
        final NSIndexSet iterator = view.selectedRowIndexes();
        final List<Path> selected = new ArrayList<Path>();
        for(NSUInteger index = iterator.firstIndex(); !index.equals(NSIndexSet.NSNotFound); index = iterator.indexGreaterThanIndex(index)) {
            final Path file = delegate.pathAtRow(index.intValue());
            if(null == file) {
                break;
            }
            selected.add(file);
        }
        return selected;
    }

    protected int getSelectionCount() {
        return this.getSelectedBrowserView().numberOfSelectedRows().intValue();
    }

    private void deselectAll() {
        if(log.isDebugEnabled()) {
            log.debug("Deselect all files in browser");
        }
        final NSTableView browser = this.getSelectedBrowserView();
        if(null == browser) {
            return;
        }
        browser.deselectAll(null);
    }

    @Override
    public void setWindow(NSWindow window) {
        window.setTitle(preferences.getProperty("application.name"));
        window.setMiniwindowImage(IconCacheFactory.<NSImage>get().iconNamed("cyberduck-document.icns"));
        window.setMovableByWindowBackground(true);
        window.setCollectionBehavior(window.collectionBehavior() | NSWindow.NSWindowCollectionBehavior.NSWindowCollectionBehaviorFullScreenPrimary);
        window.setContentMinSize(new NSSize(400d, 200d));
        super.setWindow(window);
    }

    @Outlet
    private NSDrawer logDrawer;

    public void drawerDidOpen(NSNotification notification) {
        preferences.setProperty("browser.transcript.open", true);
    }

    public void drawerDidClose(NSNotification notification) {
        preferences.setProperty("browser.transcript.open", false);
        transcript.clear();
    }

    public NSSize drawerWillResizeContents_toSize(final NSDrawer sender, final NSSize contentSize) {
        return contentSize;
    }

    public void setLogDrawer(NSDrawer drawer) {
        this.logDrawer = drawer;
        this.transcript = new TranscriptController() {
            @Override
            public boolean isOpen() {
                return logDrawer.state() == NSDrawer.OpenState;
            }
        };
        this.logDrawer.setContentView(this.transcript.getLogView());
        this.logDrawer.setDelegate(this.id());
    }

    public TranscriptController getTranscript() {
        return transcript;
    }

    private NSButton donateButton;

    public void setDonateButton(NSButton donateButton) {
        this.donateButton = donateButton;
        this.donateButton.setTitle(LocaleFactory.localizedString("Get a donation key!", "License"));
        this.donateButton.setAction(Foundation.selector("donateMenuClicked:"));
        this.donateButton.sizeToFit();
    }

    private void addDonateWindowTitle() {
        NSView parent = this.window().contentView().superview();
        NSSize bounds = parent.frame().size;
        NSSize size = donateButton.frame().size;
        donateButton.setFrame(new NSRect(
                        new NSPoint(
                                bounds.width.intValue() - size.width.intValue() - 40,
                                bounds.height.intValue() - size.height.intValue() + 3),
                        new NSSize(
                                size.width.intValue(),
                                size.height.intValue())
                )
        );
        donateButton.setAutoresizingMask(new NSUInteger(NSView.NSViewMinXMargin | NSView.NSViewMinYMargin));
        parent.addSubview(donateButton);
    }

    public void removeDonateWindowTitle() {
        donateButton.removeFromSuperview();
    }

    private static final int TAB_BOOKMARKS = 0;
    private static final int TAB_LIST_VIEW = 1;
    private static final int TAB_OUTLINE_VIEW = 2;

    private int getSelectedTabView() {
        return browserTabView.indexOfTabViewItem(browserTabView.selectedTabViewItem());
    }

    private NSTabView browserTabView;

    public void setBrowserTabView(NSTabView browserTabView) {
        this.browserTabView = browserTabView;
    }

    /**
     * @return The currently selected browser view (which is either an outlineview or a plain tableview)
     */
    public NSTableView getSelectedBrowserView() {
        switch(preferences.getInteger("browser.view")) {
            case SWITCH_LIST_VIEW: {
                return browserListView;
            }
            case SWITCH_OUTLINE_VIEW: {
                return browserOutlineView;
            }
        }
        throw new FactoryException("No selected browser view");
    }

    /**
     * @return The datasource of the currently selected browser view
     */
    public BrowserTableDataSource getSelectedBrowserModel() {
        switch(this.browserSwitchView.selectedSegment()) {
            case SWITCH_LIST_VIEW: {
                return browserListModel;
            }
            case SWITCH_OUTLINE_VIEW: {
                return browserOutlineModel;
            }
        }
        throw new FactoryException("No selected browser view");
    }

    public AbstractBrowserTableDelegate<Path> getSelectedBrowserDelegate() {
        switch(this.browserSwitchView.selectedSegment()) {
            case SWITCH_LIST_VIEW: {
                return browserListViewDelegate;
            }
            case SWITCH_OUTLINE_VIEW: {
                return browserOutlineViewDelegate;
            }
        }
        throw new FactoryException("No selected browser view");
    }

    @Outlet
    private NSMenu editMenu;
    private EditMenuDelegate editMenuDelegate;

    public void setEditMenu(NSMenu editMenu) {
        this.editMenu = editMenu;
        this.editMenuDelegate = new EditMenuDelegate() {
            @Override
            protected Path getEditable() {
                final Path selected = BrowserController.this.getSelectedPath();
                if(null == selected) {
                    return null;
                }
                if(isEditable(selected)) {
                    return selected;
                }
                return null;
            }

            @Override
            protected ID getTarget() {
                return BrowserController.this.id();
            }
        };
        this.editMenu.setDelegate(editMenuDelegate.id());
    }

    @Outlet
    private NSMenu urlMenu;
    private URLMenuDelegate urlMenuDelegate;

    public void setUrlMenu(NSMenu urlMenu) {
        this.urlMenu = urlMenu;
        this.urlMenuDelegate = new CopyURLMenuDelegate() {
            @Override
            protected Session<?> getSession() {
                return BrowserController.this.getSession();
            }

            @Override
            protected List<Path> getSelected() {
                final List<Path> s = BrowserController.this.getSelectedPaths();
                if(s.isEmpty()) {
                    if(BrowserController.this.isMounted()) {
                        return Collections.singletonList(BrowserController.this.workdir());
                    }
                }
                return s;
            }
        };
        this.urlMenu.setDelegate(urlMenuDelegate.id());
    }

    @Outlet
    private NSMenu openUrlMenu;
    private URLMenuDelegate openUrlMenuDelegate;

    public void setOpenUrlMenu(NSMenu openUrlMenu) {
        this.openUrlMenu = openUrlMenu;
        this.openUrlMenuDelegate = new OpenURLMenuDelegate() {
            @Override
            protected Session<?> getSession() {
                return BrowserController.this.getSession();
            }

            @Override
            protected List<Path> getSelected() {
                final List<Path> s = BrowserController.this.getSelectedPaths();
                if(s.isEmpty()) {
                    if(BrowserController.this.isMounted()) {
                        return Collections.singletonList(BrowserController.this.workdir());
                    }
                }
                return s;
            }
        };
        this.openUrlMenu.setDelegate(openUrlMenuDelegate.id());
    }

    @Outlet
    private NSMenu archiveMenu;
    private ArchiveMenuDelegate archiveMenuDelegate;

    public void setArchiveMenu(NSMenu archiveMenu) {
        this.archiveMenu = archiveMenu;
        this.archiveMenuDelegate = new ArchiveMenuDelegate();
        this.archiveMenu.setDelegate(archiveMenuDelegate.id());
    }

    @Outlet
    private NSButton bonjourButton;

    public void setBonjourButton(NSButton bonjourButton) {
        this.bonjourButton = bonjourButton;
        NSImage img = IconCacheFactory.<NSImage>get().iconNamed("rendezvous.tiff", 16);
        img.setTemplate(false);
        this.bonjourButton.setImage(img);
        this.setRecessedBezelStyle(this.bonjourButton);
        this.bonjourButton.setTarget(this.id());
        this.bonjourButton.setAction(Foundation.selector("bookmarkButtonClicked:"));
    }

    @Outlet
    private NSButton historyButton;

    public void setHistoryButton(NSButton historyButton) {
        this.historyButton = historyButton;
        NSImage img = IconCacheFactory.<NSImage>get().iconNamed("history.tiff", 16);
        img.setTemplate(false);
        this.historyButton.setImage(img);
        this.setRecessedBezelStyle(this.historyButton);
        this.historyButton.setTarget(this.id());
        this.historyButton.setAction(Foundation.selector("bookmarkButtonClicked:"));
    }

    @Outlet
    private NSButton bookmarkButton;

    public void setBookmarkButton(NSButton bookmarkButton) {
        this.bookmarkButton = bookmarkButton;
        NSImage img = IconCacheFactory.<NSImage>get().iconNamed("bookmarks.tiff", 16);
        img.setTemplate(false);
        this.bookmarkButton.setImage(img);
        this.setRecessedBezelStyle(this.bookmarkButton);
        this.bookmarkButton.setTarget(this.id());
        this.bookmarkButton.setAction(Foundation.selector("bookmarkButtonClicked:"));
        this.bookmarkButton.setState(NSCell.NSOnState); // Set as default selected bookmark source
    }

    public void bookmarkButtonClicked(final NSButton sender) {
        if(sender != bonjourButton) {
            bonjourButton.setState(NSCell.NSOffState);
        }
        if(sender != historyButton) {
            historyButton.setState(NSCell.NSOffState);
        }
        if(sender != bookmarkButton) {
            bookmarkButton.setState(NSCell.NSOffState);
        }
        sender.setState(NSCell.NSOnState);
        this.selectBookmarks();
    }

    private void setRecessedBezelStyle(final NSButton b) {
        b.setBezelStyle(NSButton.NSRecessedBezelStyle);
        b.setButtonType(NSButton.NSMomentaryPushButtonButton);
        b.setImagePosition(NSCell.NSImageLeft);
        b.setFont(NSFont.boldSystemFontOfSize(11f));
        b.setShowsBorderOnlyWhileMouseInside(true);
    }

    public void sortBookmarksByNickame(final ID sender) {
        BookmarkCollection.defaultCollection().sortByNickname();
        this.reloadBookmarks();
    }

    public void sortBookmarksByHostname(final ID sender) {
        BookmarkCollection.defaultCollection().sortByHostname();
        this.reloadBookmarks();
    }

    public void sortBookmarksByProtocol(final ID sender) {
        BookmarkCollection.defaultCollection().sortByProtocol();
        this.reloadBookmarks();
    }

    private NSSegmentedControl bookmarkSwitchView;

    private static final int SWITCH_BOOKMARK_VIEW = 0;

    public void setBookmarkSwitchView(NSSegmentedControl bookmarkSwitchView) {
        this.bookmarkSwitchView = bookmarkSwitchView;
        this.bookmarkSwitchView.setSegmentCount(1);
        this.bookmarkSwitchView.setToolTip(LocaleFactory.localizedString("Bookmarks"));
        final NSImage image = IconCacheFactory.<NSImage>get().iconNamed("book.tiff");
        this.bookmarkSwitchView.setImage_forSegment(image, SWITCH_BOOKMARK_VIEW);
        final NSSegmentedCell cell = Rococoa.cast(this.bookmarkSwitchView.cell(), NSSegmentedCell.class);
        cell.setTrackingMode(NSSegmentedCell.NSSegmentSwitchTrackingSelectAny);
        cell.setControlSize(NSCell.NSRegularControlSize);
        this.bookmarkSwitchView.setTarget(this.id());
        this.bookmarkSwitchView.setAction(Foundation.selector("bookmarkSwitchClicked:"));
        this.bookmarkSwitchView.setSelectedSegment(SWITCH_BOOKMARK_VIEW);
    }

    @Action
    public void bookmarkSwitchClicked(final ID sender) {
        // Toggle
        final boolean open = this.getSelectedTabView() != TAB_BOOKMARKS;
        bookmarkSwitchView.setSelected_forSegment(open, SWITCH_BOOKMARK_VIEW);
        this.setNavigation(!open && this.isMounted());
        if(open) {
            this.selectBookmarks();
        }
        else {
            this.selectBrowser(preferences.getInteger("browser.view"));
        }
    }

    private NSSegmentedControl browserSwitchView;

    private static final int SWITCH_LIST_VIEW = 0;
    private static final int SWITCH_OUTLINE_VIEW = 1;

    public void setBrowserSwitchView(NSSegmentedControl view) {
        browserSwitchView = view;
        browserSwitchView.setSegmentCount(2); // list, outline
        final NSImage list = IconCacheFactory.<NSImage>get().iconNamed("list.tiff");
        list.setTemplate(true);
        browserSwitchView.setImage_forSegment(list, SWITCH_LIST_VIEW);
        final NSImage outline = IconCacheFactory.<NSImage>get().iconNamed("outline.tiff");
        outline.setTemplate(true);
        browserSwitchView.setImage_forSegment(outline, SWITCH_OUTLINE_VIEW);
        browserSwitchView.setTarget(this.id());
        browserSwitchView.setAction(Foundation.selector("browserSwitchButtonClicked:"));
        final NSSegmentedCell cell = Rococoa.cast(browserSwitchView.cell(), NSSegmentedCell.class);
        cell.setTrackingMode(NSSegmentedCell.NSSegmentSwitchTrackingSelectOne);
        cell.setControlSize(NSCell.NSRegularControlSize);
        browserSwitchView.setSelectedSegment(preferences.getInteger("browser.view"));
    }

    @Action
    public void browserSwitchButtonClicked(final NSSegmentedControl sender) {
        // Highlight selected browser view
        this.selectBrowser(sender.selectedSegment());
    }

    @Action
    public void browserSwitchMenuClicked(final NSMenuItem sender) {
        // Highlight selected browser view
        this.selectBrowser(sender.tag());
    }

    private void selectBrowser(int selected) {
        bookmarkSwitchView.setSelected_forSegment(false, SWITCH_BOOKMARK_VIEW);
        browserSwitchView.setSelectedSegment(selected);
        switch(selected) {
            case SWITCH_LIST_VIEW:
                browserTabView.selectTabViewItemAtIndex(TAB_LIST_VIEW);
                break;
            case SWITCH_OUTLINE_VIEW:
                browserTabView.selectTabViewItemAtIndex(TAB_OUTLINE_VIEW);
                break;
        }
        // Save selected browser view
        preferences.setProperty("browser.view", selected);
        // Remove any custom file filter
        this.setPathFilter(null);
        // Update from model
        this.reload(true);
        // Focus on browser view
        this.getFocus();
    }

    private void selectBookmarks() {
        bookmarkSwitchView.setSelected_forSegment(true, SWITCH_BOOKMARK_VIEW);
        // Display bookmarks
        browserTabView.selectTabViewItemAtIndex(TAB_BOOKMARKS);
        final AbstractHostCollection source;
        if(bookmarkButton.state() == NSCell.NSOnState) {
            source = BookmarkCollection.defaultCollection();
        }
        else if(bonjourButton.state() == NSCell.NSOnState) {
            source = RendezvousCollection.defaultCollection();
        }
        else if(historyButton.state() == NSCell.NSOnState) {
            source = HistoryCollection.defaultCollection();
        }
        else {
            source = AbstractHostCollection.empty();
        }
        if(!source.isLoaded()) {
            browserSpinner.startAnimation(null);
            source.addListener(new AbstractCollectionListener<Host>() {
                @Override
                public void collectionLoaded() {
                    invoke(new WindowMainAction(BrowserController.this) {
                        @Override
                        public void run() {
                            browserSpinner.stopAnimation(null);
                            bookmarkTable.setGridStyleMask(NSTableView.NSTableViewSolidHorizontalGridLineMask);
                        }
                    });
                    source.removeListener(this);
                }
            });
        }
        else {
            browserSpinner.stopAnimation(null);
            bookmarkTable.setGridStyleMask(NSTableView.NSTableViewSolidHorizontalGridLineMask);
        }
        bookmarkModel.setSource(source);
        this.setBookmarkFilter(null);
        this.reloadBookmarks();
        if(this.isMounted()) {
            int row = this.bookmarkModel.getSource().indexOf(session.getHost());
            if(row != -1) {
                this.bookmarkTable.selectRowIndexes(NSIndexSet.indexSetWithIndex(new NSInteger(row)), false);
                this.bookmarkTable.scrollRowToVisible(new NSInteger(row));
            }
        }
        this.getFocus();
    }

    /**
     * Reload bookmark table from currently selected model
     */
    public void reloadBookmarks() {
        bookmarkTable.reloadData();
        this.setStatus();
    }

    private abstract class AbstractBrowserOutlineViewDelegate<E> extends AbstractBrowserTableDelegate<E>
            implements NSOutlineView.Delegate {

        protected AbstractBrowserOutlineViewDelegate(final NSTableColumn selectedColumn) {
            super(selectedColumn);
        }

        public String outlineView_toolTipForCell_rect_tableColumn_item_mouseLocation(NSOutlineView t, NSCell cell,
                                                                                     ID rect, NSTableColumn c,
                                                                                     NSObject item, NSPoint mouseLocation) {
            return this.tooltip(cache.lookup(new NSObjectPathReference(item)));
        }

        public String outlineView_typeSelectStringForTableColumn_item(final NSOutlineView view,
                                                                      final NSTableColumn tableColumn,
                                                                      final NSObject item) {
            if(tableColumn.identifier().equals(BrowserTableDataSource.Column.filename.name())) {
                return browserOutlineModel.outlineView_objectValueForTableColumn_byItem(view, tableColumn, item).toString();
            }
            return null;
        }

        @Override
        protected void setBrowserColumnSortingIndicator(NSImage image, String columnIdentifier) {
            browserOutlineView.setIndicatorImage_inTableColumn(image,
                    browserOutlineView.tableColumnWithIdentifier(columnIdentifier));
        }

        @Override
        protected Path pathAtRow(final int row) {
            if(row < browserOutlineView.numberOfRows().intValue()) {
                return cache.lookup(new NSObjectPathReference(browserOutlineView.itemAtRow(new NSInteger(row))));
            }
            log.warn(String.format("No item at row %d", row));
            return null;
        }
    }

    private abstract class AbstractBrowserListViewDelegate<E> extends AbstractBrowserTableDelegate<E>
            implements NSTableView.Delegate {

        protected AbstractBrowserListViewDelegate(final NSTableColumn selectedColumn) {
            super(selectedColumn);
        }

        public String tableView_toolTipForCell_rect_tableColumn_row_mouseLocation(NSTableView t, NSCell cell,
                                                                                  ID rect, NSTableColumn c,
                                                                                  NSInteger row, NSPoint mouseLocation) {
            return this.tooltip(browserListModel.get(workdir()).get(row.intValue()));
        }

        @Override
        protected void setBrowserColumnSortingIndicator(NSImage image, String columnIdentifier) {
            browserListView.setIndicatorImage_inTableColumn(image,
                    browserListView.tableColumnWithIdentifier(columnIdentifier));
        }

        public String tableView_typeSelectStringForTableColumn_row(final NSTableView view,
                                                                   final NSTableColumn tableColumn,
                                                                   final NSInteger row) {
            if(tableColumn.identifier().equals(BrowserTableDataSource.Column.filename.name())) {
                return browserListModel.tableView_objectValueForTableColumn_row(view, tableColumn, row).toString();
            }
            return null;
        }

        @Override
        protected Path pathAtRow(int row) {
            final AttributedList<Path> children = browserListModel.get(workdir());
            if(row < children.size()) {
                return children.get(row);
            }
            log.warn("No item at row:" + row);
            return null;
        }
    }

    private abstract class AbstractBrowserTableDelegate<E> extends AbstractPathTableDelegate {

        protected AbstractBrowserTableDelegate(final NSTableColumn selectedColumn) {
            super(selectedColumn);
        }

        @Override
        public boolean isColumnRowEditable(NSTableColumn column, int row) {
            if(preferences.getBoolean("browser.editable")) {
                return column.identifier().equals(BrowserTableDataSource.Column.filename.name());
            }
            return false;
        }

        @Override
        public void tableRowDoubleClicked(final ID sender) {
            BrowserController.this.insideButtonClicked(sender);
        }

        public void spaceKeyPressed(final ID sender) {
            quicklookButtonClicked(sender);
        }

        @Override
        public void deleteKeyPressed(final ID sender) {
            BrowserController.this.deleteFileButtonClicked(sender);
        }

        @Override
        public void tableColumnClicked(NSTableView view, NSTableColumn tableColumn) {
            if(this.selectedColumnIdentifier().equals(tableColumn.identifier())) {
                this.setSortedAscending(!this.isSortedAscending());
            }
            else {
                // Remove sorting indicator on previously selected column
                this.setBrowserColumnSortingIndicator(null, this.selectedColumnIdentifier());
                // Set the newly selected column
                this.setSelectedColumn(tableColumn);
                // Update the default value
                preferences.setProperty("browser.sort.column", this.selectedColumnIdentifier());
            }
            this.setBrowserColumnSortingIndicator(
                    this.isSortedAscending() ?
                            IconCacheFactory.<NSImage>get().iconNamed("NSAscendingSortIndicator") :
                            IconCacheFactory.<NSImage>get().iconNamed("NSDescendingSortIndicator"),
                    tableColumn.identifier()
            );
            reload(true);
        }

        @Override
        public void columnDidResize(final String columnIdentifier, final float width) {
            preferences.setProperty(String.format("browser.column.%s.width", columnIdentifier), width);
        }

        @Override
        public void selectionDidChange(NSNotification notification) {
            final List<Path> selected = getSelectedPaths();
            if(quicklook.isOpen()) {
                updateQuickLookSelection(selected);
            }
            if(preferences.getBoolean("browser.info.inspector")) {
                InfoController c = InfoControllerFactory.get(BrowserController.this);
                if(null != c) {
                    // Currently open info panel
                    c.setFiles(selected);
                }
            }
        }

        protected abstract Path pathAtRow(int row);

        protected abstract void setBrowserColumnSortingIndicator(NSImage image, String columnIdentifier);

        private static final double kSwipeGestureLeft = 1.000000;
        private static final double kSwipeGestureRight = -1.000000;
        private static final double kSwipeGestureUp = 1.000000;
        private static final double kSwipeGestureDown = -1.000000;

        /**
         * Available in Mac OS X v10.6 and later.
         *
         * @param event Swipe event
         */
        @Action
        public void swipeWithEvent(NSEvent event) {
            if(event.deltaX().doubleValue() == kSwipeGestureLeft) {
                BrowserController.this.backButtonClicked(event.id());
            }
            else if(event.deltaX().doubleValue() == kSwipeGestureRight) {
                BrowserController.this.forwardButtonClicked(event.id());
            }
            else if(event.deltaY().doubleValue() == kSwipeGestureUp) {
                NSInteger row = getSelectedBrowserView().selectedRow();
                NSInteger next;
                if(-1 == row.intValue()) {
                    // No current selection
                    next = new NSInteger(0);
                }
                else {
                    next = new NSInteger(row.longValue() - 1);
                }
                BrowserController.this.getSelectedBrowserView().selectRowIndexes(
                        NSIndexSet.indexSetWithIndex(next), false);
            }
            else if(event.deltaY().doubleValue() == kSwipeGestureDown) {
                NSInteger row = getSelectedBrowserView().selectedRow();
                NSInteger next;
                if(-1 == row.intValue()) {
                    // No current selection
                    next = new NSInteger(0);
                }
                else {
                    next = new NSInteger(row.longValue() + 1);
                }
                BrowserController.this.getSelectedBrowserView().selectRowIndexes(
                        NSIndexSet.indexSetWithIndex(next), false);
            }
        }
    }

    /**
     * QuickLook support for 10.6+
     *
     * @param panel The Preview Panel looking for a controller.
     * @return
     * @ Sent to each object in the responder chain to find a controller.
     */
    @Override
    public boolean acceptsPreviewPanelControl(QLPreviewPanel panel) {
        return true;
    }

    /**
     * QuickLook support for 10.6+
     * The receiver should setup the preview panel (data source, delegate, binding, etc.) here.
     *
     * @param panel The Preview Panel the receiver will control.
     * @ Sent to the object taking control of the Preview Panel.
     */
    @Override
    public void beginPreviewPanelControl(QLPreviewPanel panel) {
        quicklook.willBeginQuickLook();
    }

    /**
     * QuickLook support for 10.6+
     * The receiver should unsetup the preview panel (data source, delegate, binding, etc.) here.
     *
     * @param panel The Preview Panel that the receiver will stop controlling.
     * @ Sent to the object in control of the Preview Panel just before stopping its control.
     */
    @Override
    public void endPreviewPanelControl(QLPreviewPanel panel) {
        quicklook.didEndQuickLook();
    }

    public void setBrowserOutlineView(NSOutlineView view) {
        browserOutlineView = view;
        // receive drag events from types
        browserOutlineView.registerForDraggedTypes(NSArray.arrayWithObjects(
                NSPasteboard.URLPboardType,
                NSPasteboard.FilenamesPboardType, //accept files dragged from the Finder for uploading
                NSPasteboard.FilesPromisePboardType //accept file promises made myself but then interpret them as TransferPasteboardType
        ));
        // setting appearance attributes()
        this._updateBrowserAttributes(browserOutlineView);
        // selection properties
        browserOutlineView.setAllowsMultipleSelection(true);
        browserOutlineView.setAllowsEmptySelection(true);
        browserOutlineView.setAllowsColumnResizing(true);
        browserOutlineView.setAllowsColumnSelection(false);
        browserOutlineView.setAllowsColumnReordering(true);

        browserOutlineView.setRowHeight(new CGFloat(layoutManager.defaultLineHeightForFont(
                NSFont.systemFontOfSize(preferences.getFloat("browser.font.size"))).intValue() + 2));

        {
            NSTableColumn c = browserOutlineColumnsFactory.create(BrowserTableDataSource.Column.filename.name());
            c.headerCell().setStringValue(LocaleFactory.localizedString("Filename"));
            c.setMinWidth(new CGFloat(100));
            c.setWidth(preferences.getFloat(String.format("browser.column.%s.width",
                    BrowserTableDataSource.Column.filename.name())));
            c.setMaxWidth(new CGFloat(1000));
            c.setResizingMask(NSTableColumn.NSTableColumnAutoresizingMask | NSTableColumn.NSTableColumnUserResizingMask);
            c.setDataCell(outlineCellPrototype);
            browserOutlineView.addTableColumn(c);
            browserOutlineView.setOutlineTableColumn(c);
        }
        browserOutlineView.setDataSource((browserOutlineModel = new BrowserOutlineViewModel(this, cache)).id());
        browserOutlineView.setDelegate((browserOutlineViewDelegate = new AbstractBrowserOutlineViewDelegate<Path>(
                browserOutlineView.tableColumnWithIdentifier(BrowserTableDataSource.Column.filename.name())
        ) {
            @Override
            public void enterKeyPressed(final ID sender) {
                if(preferences.getBoolean("browser.enterkey.rename")) {
                    if(browserOutlineView.numberOfSelectedRows().intValue() == 1) {
                        renameFileButtonClicked(sender);
                    }
                }
                else {
                    this.tableRowDoubleClicked(sender);
                }
            }

            /**
             * @see NSOutlineView.Delegate
             */
            @Override
            public void outlineView_willDisplayCell_forTableColumn_item(NSOutlineView view, NSTextFieldCell cell,
                                                                        NSTableColumn tableColumn, NSObject item) {
                if(null == item) {
                    return;
                }
                final Path path = cache.lookup(new NSObjectPathReference(item));
                if(null == path) {
                    return;
                }
                if(tableColumn.identifier().equals(BrowserTableDataSource.Column.filename.name())) {
                    cell.setEditable(session.getFeature(Move.class).isSupported(path));
                    (Rococoa.cast(cell, OutlineCell.class)).setIcon(browserOutlineModel.iconForPath(path));
                }
                if(!BrowserController.this.isConnected() || !HIDDEN_FILTER.accept(path)) {
                    cell.setTextColor(NSColor.disabledControlTextColor());
                }
                else {
                    cell.setTextColor(NSColor.controlTextColor());
                }
            }

            /**
             * @see NSOutlineView.Delegate
             */
            @Override
            public boolean outlineView_shouldExpandItem(final NSOutlineView view, final NSObject item) {
                NSEvent event = NSApplication.sharedApplication().currentEvent();
                if(event != null) {
                    if(NSEvent.NSLeftMouseDragged == event.type()) {
                        if(!preferences.getBoolean("browser.view.autoexpand")) {
                            if(log.isDebugEnabled()) {
                                log.debug("Returning false to #outlineViewShouldExpandItem while dragging because browser.view.autoexpand == false");
                            }
                            // See tickets #98 and #633
                            return false;
                        }
                        final NSInteger draggingColumn = view.columnAtPoint(view.convertPoint_fromView(event.locationInWindow(), null));
                        if(draggingColumn.intValue() != 0) {
                            if(log.isDebugEnabled()) {
                                log.debug("Returning false to #outlineViewShouldExpandItem for column:" + draggingColumn);
                            }
                            // See ticket #60
                            return false;
                        }
                    }
                }
                return true;
            }

            /**
             * @see NSOutlineView.Delegate
             */
            @Override
            public void outlineViewItemDidExpand(NSNotification notification) {
                setStatus();
            }

            /**
             * @see NSOutlineView.Delegate
             */
            @Override
            public void outlineViewItemDidCollapse(NSNotification notification) {
                setStatus();
            }

            @Override
            protected boolean isTypeSelectSupported() {
                return true;
            }

        }).id());
    }

    public void setBrowserListView(NSTableView view) {
        browserListView = view;
        // receive drag events from types
        browserListView.registerForDraggedTypes(NSArray.arrayWithObjects(
                NSPasteboard.URLPboardType,
                NSPasteboard.FilenamesPboardType, //accept files dragged from the Finder for uploading
                NSPasteboard.FilesPromisePboardType //accept file promises made myself but then interpret them as TransferPasteboardType
        ));
        // setting appearance attributes()
        this._updateBrowserAttributes(browserListView);
        // selection properties
        browserListView.setAllowsMultipleSelection(true);
        browserListView.setAllowsEmptySelection(true);
        browserListView.setAllowsColumnResizing(true);
        browserListView.setAllowsColumnSelection(false);
        browserListView.setAllowsColumnReordering(true);

        browserListView.setRowHeight(new CGFloat(layoutManager.defaultLineHeightForFont(
                NSFont.systemFontOfSize(preferences.getFloat("browser.font.size"))).intValue() + 2));

        {
            NSTableColumn c = browserListColumnsFactory.create(BrowserTableDataSource.Column.icon.name());
            c.headerCell().setStringValue(StringUtils.EMPTY);
            c.setMinWidth((20));
            c.setWidth(preferences.getFloat(String.format("browser.column.%s.width",
                    BrowserTableDataSource.Column.icon.name())));
            c.setMaxWidth((20));
            c.setResizingMask(NSTableColumn.NSTableColumnAutoresizingMask);
            c.setDataCell(imageCellPrototype);
            c.dataCell().setAlignment(NSText.NSCenterTextAlignment);
            browserListView.addTableColumn(c);
        }
        {
            NSTableColumn c = browserListColumnsFactory.create(BrowserTableDataSource.Column.filename.name());
            c.headerCell().setStringValue(LocaleFactory.localizedString("Filename"));
            c.setMinWidth((100));
            c.setWidth(preferences.getFloat(String.format("browser.column.%s.width",
                    BrowserTableDataSource.Column.filename.name())));
            c.setMaxWidth((1000));
            c.setResizingMask(NSTableColumn.NSTableColumnAutoresizingMask | NSTableColumn.NSTableColumnUserResizingMask);
            c.setDataCell(filenameCellPrototype);
            this.browserListView.addTableColumn(c);
        }

        browserListView.setDataSource((browserListModel = new BrowserListViewModel(this, cache)).id());
        browserListView.setDelegate((browserListViewDelegate = new AbstractBrowserListViewDelegate<Path>(
                browserListView.tableColumnWithIdentifier(BrowserTableDataSource.Column.filename.name())
        ) {
            @Override
            public void enterKeyPressed(final ID sender) {
                if(preferences.getBoolean("browser.enterkey.rename")) {
                    if(browserListView.numberOfSelectedRows().intValue() == 1) {
                        renameFileButtonClicked(sender);
                    }
                }
                else {
                    this.tableRowDoubleClicked(sender);
                }
            }

            @Override
            public void tableView_willDisplayCell_forTableColumn_row(NSTableView view, NSTextFieldCell cell, NSTableColumn tableColumn, NSInteger row) {
                final String identifier = tableColumn.identifier();
                final Path path = browserListModel.get(BrowserController.this.workdir()).get(row.intValue());
                if(identifier.equals(BrowserTableDataSource.Column.filename.name())) {
                    cell.setEditable(session.getFeature(Move.class).isSupported(path));
                }
                if(cell.isKindOfClass(Foundation.getClass(NSTextFieldCell.class.getSimpleName()))) {
                    if(!BrowserController.this.isConnected() || !HIDDEN_FILTER.accept(path)) {
                        cell.setTextColor(NSColor.disabledControlTextColor());
                    }
                    else {
                        cell.setTextColor(NSColor.controlTextColor());
                    }
                }
            }

            @Override
            protected boolean isTypeSelectSupported() {
                return true;
            }
        }).id());
    }

    protected void _updateBrowserAttributes(NSTableView tableView) {
        tableView.setUsesAlternatingRowBackgroundColors(preferences.getBoolean("browser.alternatingRows"));
        if(preferences.getBoolean("browser.horizontalLines") && preferences.getBoolean("browser.verticalLines")) {
            tableView.setGridStyleMask(new NSUInteger(NSTableView.NSTableViewSolidHorizontalGridLineMask.intValue() | NSTableView.NSTableViewSolidVerticalGridLineMask.intValue()));
        }
        else if(preferences.getBoolean("browser.verticalLines")) {
            tableView.setGridStyleMask(NSTableView.NSTableViewSolidVerticalGridLineMask);
        }
        else if(preferences.getBoolean("browser.horizontalLines")) {
            tableView.setGridStyleMask(NSTableView.NSTableViewSolidHorizontalGridLineMask);
        }
        else {
            tableView.setGridStyleMask(NSTableView.NSTableViewGridNone);
        }
    }

    protected void _updateBookmarkCell() {
        final int size = preferences.getInteger("bookmark.icon.size");
        final double width = size * 1.5;
        final NSTableColumn c = bookmarkTable.tableColumnWithIdentifier(BookmarkTableDataSource.Column.icon.name());
        c.setMinWidth(width);
        c.setMaxWidth(width);
        c.setWidth(width);
        // Notify the table about the changed row height.
        bookmarkTable.noteHeightOfRowsWithIndexesChanged(
                NSIndexSet.indexSetWithIndexesInRange(NSRange.NSMakeRange(new NSUInteger(0), new NSUInteger(bookmarkTable.numberOfRows()))));
    }

    private void _updateBrowserColumns(final NSTableView table, final AbstractBrowserTableDelegate<Path> delegate) {
        table.removeTableColumn(table.tableColumnWithIdentifier(BrowserTableDataSource.Column.size.name()));
        if(preferences.getBoolean(String.format("browser.column.%s", BrowserTableDataSource.Column.size.name()))) {
            NSTableColumn c = browserListColumnsFactory.create(BrowserTableDataSource.Column.size.name());
            c.headerCell().setStringValue(LocaleFactory.localizedString("Size"));
            c.setMinWidth(50f);
            c.setWidth(preferences.getFloat(String.format("browser.column.%s.width",
                    BrowserTableDataSource.Column.size.name())));
            c.setMaxWidth(150f);
            c.setResizingMask(NSTableColumn.NSTableColumnAutoresizingMask | NSTableColumn.NSTableColumnUserResizingMask);
            c.setDataCell(textCellPrototype);
            table.addTableColumn(c);
        }
        table.removeTableColumn(table.tableColumnWithIdentifier(BrowserTableDataSource.Column.modified.name()));
        if(preferences.getBoolean(String.format("browser.column.%s", BrowserTableDataSource.Column.modified.name()))) {
            NSTableColumn c = browserListColumnsFactory.create(BrowserTableDataSource.Column.modified.name());
            c.headerCell().setStringValue(LocaleFactory.localizedString("Modified"));
            c.setMinWidth(100f);
            c.setWidth(preferences.getFloat(String.format("browser.column.%s.width",
                    BrowserTableDataSource.Column.modified.name())));
            c.setMaxWidth(500);
            c.setResizingMask(NSTableColumn.NSTableColumnAutoresizingMask | NSTableColumn.NSTableColumnUserResizingMask);
            c.setDataCell(textCellPrototype);
            table.addTableColumn(c);
        }
        table.removeTableColumn(table.tableColumnWithIdentifier(BrowserTableDataSource.Column.owner.name()));
        if(preferences.getBoolean(String.format("browser.column.%s", BrowserTableDataSource.Column.owner.name()))) {
            NSTableColumn c = browserListColumnsFactory.create(BrowserTableDataSource.Column.owner.name());
            c.headerCell().setStringValue(LocaleFactory.localizedString("Owner"));
            c.setMinWidth(50);
            c.setWidth(preferences.getFloat(String.format("browser.column.%s.width",
                    BrowserTableDataSource.Column.owner.name())));
            c.setMaxWidth(500);
            c.setResizingMask(NSTableColumn.NSTableColumnAutoresizingMask | NSTableColumn.NSTableColumnUserResizingMask);
            c.setDataCell(textCellPrototype);
            table.addTableColumn(c);
        }
        table.removeTableColumn(table.tableColumnWithIdentifier(BrowserTableDataSource.Column.group.name()));
        if(preferences.getBoolean(String.format("browser.column.%s", BrowserTableDataSource.Column.group.name()))) {
            NSTableColumn c = browserListColumnsFactory.create(BrowserTableDataSource.Column.group.name());
            c.headerCell().setStringValue(LocaleFactory.localizedString("Group"));
            c.setMinWidth(50);
            c.setWidth(preferences.getFloat(String.format("browser.column.%s.width",
                    BrowserTableDataSource.Column.group.name())));
            c.setMaxWidth(500);
            c.setResizingMask(NSTableColumn.NSTableColumnAutoresizingMask | NSTableColumn.NSTableColumnUserResizingMask);
            c.setDataCell(textCellPrototype);
            table.addTableColumn(c);
        }
        table.removeTableColumn(table.tableColumnWithIdentifier(BrowserTableDataSource.Column.permission.name()));
        if(preferences.getBoolean(String.format("browser.column.%s", BrowserTableDataSource.Column.permission.name()))) {
            NSTableColumn c = browserListColumnsFactory.create(BrowserTableDataSource.Column.permission.name());
            c.headerCell().setStringValue(LocaleFactory.localizedString("Permissions"));
            c.setMinWidth(100);
            c.setWidth(preferences.getFloat(String.format("browser.column.%s.width",
                    BrowserTableDataSource.Column.permission.name())));
            c.setMaxWidth(800);
            c.setResizingMask(NSTableColumn.NSTableColumnAutoresizingMask | NSTableColumn.NSTableColumnUserResizingMask);
            c.setDataCell(textCellPrototype);
            table.addTableColumn(c);
        }
        table.removeTableColumn(table.tableColumnWithIdentifier(BrowserTableDataSource.Column.kind.name()));
        if(preferences.getBoolean(String.format("browser.column.%s", BrowserTableDataSource.Column.kind.name()))) {
            NSTableColumn c = browserListColumnsFactory.create(BrowserTableDataSource.Column.kind.name());
            c.headerCell().setStringValue(LocaleFactory.localizedString("Kind"));
            c.setMinWidth(50);
            c.setWidth(preferences.getFloat(String.format("browser.column.%s.width",
                    BrowserTableDataSource.Column.kind.name())));
            c.setMaxWidth(500);
            c.setResizingMask(NSTableColumn.NSTableColumnAutoresizingMask | NSTableColumn.NSTableColumnUserResizingMask);
            c.setDataCell(textCellPrototype);
            table.addTableColumn(c);
        }
        table.removeTableColumn(table.tableColumnWithIdentifier(BrowserTableDataSource.Column.extension.name()));
        if(preferences.getBoolean(String.format("browser.column.%s", BrowserTableDataSource.Column.extension.name()))) {
            NSTableColumn c = browserListColumnsFactory.create(BrowserTableDataSource.Column.extension.name());
            c.headerCell().setStringValue(LocaleFactory.localizedString("Extension"));
            c.setMinWidth(50);
            c.setWidth(preferences.getFloat(String.format("browser.column.%s.width",
                    BrowserTableDataSource.Column.extension.name())));
            c.setMaxWidth(500);
            c.setResizingMask(NSTableColumn.NSTableColumnAutoresizingMask | NSTableColumn.NSTableColumnUserResizingMask);
            c.setDataCell(textCellPrototype);
            table.addTableColumn(c);
        }
        table.removeTableColumn(table.tableColumnWithIdentifier(BrowserTableDataSource.Column.region.name()));
        if(preferences.getBoolean(String.format("browser.column.%s", BrowserTableDataSource.Column.region.name()))) {
            NSTableColumn c = browserListColumnsFactory.create(BrowserTableDataSource.Column.region.name());
            c.headerCell().setStringValue(LocaleFactory.localizedString("Region"));
            c.setMinWidth(50);
            c.setWidth(preferences.getFloat(String.format("browser.column.%s.width",
                    BrowserTableDataSource.Column.region.name())));
            c.setMaxWidth(500);
            c.setResizingMask(NSTableColumn.NSTableColumnAutoresizingMask | NSTableColumn.NSTableColumnUserResizingMask);
            c.setDataCell(textCellPrototype);
            table.addTableColumn(c);
        }
        table.removeTableColumn(table.tableColumnWithIdentifier(BrowserTableDataSource.Column.version.name()));
        if(preferences.getBoolean(String.format("browser.column.%s", BrowserTableDataSource.Column.version.name()))) {
            NSTableColumn c = browserListColumnsFactory.create(BrowserTableDataSource.Column.version.name());
            c.headerCell().setStringValue(LocaleFactory.localizedString("Version"));
            c.setMinWidth(50);
            c.setWidth(preferences.getFloat(String.format("browser.column.%s.width",
                    BrowserTableDataSource.Column.version.name())));
            c.setMaxWidth(500);
            c.setResizingMask(NSTableColumn.NSTableColumnAutoresizingMask | NSTableColumn.NSTableColumnUserResizingMask);
            c.setDataCell(textCellPrototype);
            table.addTableColumn(c);
        }
        NSTableColumn selected = table.tableColumnWithIdentifier(preferences.getProperty("browser.sort.column"));
        if(null == selected) {
            selected = table.tableColumnWithIdentifier(BrowserTableDataSource.Column.filename.name());
        }
        delegate.setSelectedColumn(selected);
        table.setIndicatorImage_inTableColumn(this.getSelectedBrowserDelegate().isSortedAscending() ?
                        IconCacheFactory.<NSImage>get().iconNamed("NSAscendingSortIndicator") :
                        IconCacheFactory.<NSImage>get().iconNamed("NSDescendingSortIndicator"),
                selected
        );
        table.sizeToFit();
        table.setAutosaveName("browser.autosave");
        table.setAutosaveTableColumns(true);
        this.reload(false);
    }

    private BookmarkTableDataSource bookmarkModel;

    private NSTableView bookmarkTable;
    private AbstractTableDelegate<Host> bookmarkTableDelegate;

    public void setBookmarkTable(NSTableView view) {
        this.bookmarkTable = view;
        this.bookmarkTable.setSelectionHighlightStyle(NSTableView.NSTableViewSelectionHighlightStyleSourceList);
        this.bookmarkTable.setDataSource((this.bookmarkModel = new BookmarkTableDataSource(this)).id());
        {
            NSTableColumn c = bookmarkTableColumnFactory.create(BookmarkTableDataSource.Column.icon.name());
            c.headerCell().setStringValue(StringUtils.EMPTY);
            c.setResizingMask(NSTableColumn.NSTableColumnNoResizing);
            c.setDataCell(imageCellPrototype);
            this.bookmarkTable.addTableColumn(c);
        }
        {
            NSTableColumn c = bookmarkTableColumnFactory.create(BookmarkTableDataSource.Column.bookmark.name());
            c.headerCell().setStringValue(LocaleFactory.localizedString("Bookmarks"));
            c.setMinWidth(150);
            c.setResizingMask(NSTableColumn.NSTableColumnAutoresizingMask);
            c.setDataCell(BookmarkCell.bookmarkCell());
            this.bookmarkTable.addTableColumn(c);
        }
        {
            NSTableColumn c = bookmarkTableColumnFactory.create(BookmarkTableDataSource.Column.status.name());
            c.headerCell().setStringValue(StringUtils.EMPTY);
            c.setMinWidth(40);
            c.setWidth(40);
            c.setMaxWidth(40);
            c.setResizingMask(NSTableColumn.NSTableColumnAutoresizingMask);
            c.setDataCell(imageCellPrototype);
            c.dataCell().setAlignment(NSText.NSCenterTextAlignment);
            this.bookmarkTable.addTableColumn(c);
        }
        this.bookmarkTable.setDelegate((this.bookmarkTableDelegate = new AbstractTableDelegate<Host>(
                bookmarkTable.tableColumnWithIdentifier(BookmarkTableDataSource.Column.bookmark.name())
        ) {
            @Override
            public String tooltip(Host bookmark) {
                return new HostUrlProvider().get(bookmark);
            }

            @Override
            public void tableRowDoubleClicked(final ID sender) {
                BrowserController.this.connectBookmarkButtonClicked(sender);
            }

            @Override
            public void enterKeyPressed(final ID sender) {
                this.tableRowDoubleClicked(sender);
            }

            @Override
            public void deleteKeyPressed(final ID sender) {
                if(bookmarkModel.getSource().allowsDelete()) {
                    BrowserController.this.deleteBookmarkButtonClicked(sender);
                }
            }

            @Override
            public void tableColumnClicked(NSTableView view, NSTableColumn tableColumn) {

            }

            @Override
            public void selectionDidChange(NSNotification notification) {
                addBookmarkButton.setEnabled(bookmarkModel.getSource().allowsAdd());
                final int selected = bookmarkTable.numberOfSelectedRows().intValue();
                editBookmarkButton.setEnabled(bookmarkModel.getSource().allowsEdit() && selected == 1);
                deleteBookmarkButton.setEnabled(bookmarkModel.getSource().allowsDelete() && selected > 0);
            }

            public CGFloat tableView_heightOfRow(NSTableView view, NSInteger row) {
                final int size = preferences.getInteger("bookmark.icon.size");
                if(BookmarkCell.SMALL_BOOKMARK_SIZE == size) {
                    return new CGFloat(18);
                }
                if(BookmarkCell.MEDIUM_BOOKMARK_SIZE == size) {
                    return new CGFloat(45);
                }
                return new CGFloat(70);
            }

            @Override
            public boolean isTypeSelectSupported() {
                return true;
            }

            public String tableView_typeSelectStringForTableColumn_row(NSTableView view,
                                                                       NSTableColumn tableColumn,
                                                                       NSInteger row) {
                return BookmarkNameProvider.toString(bookmarkModel.getSource().get(row.intValue()));
            }

            public boolean tableView_isGroupRow(NSTableView view, NSInteger row) {
                return false;
            }

            private static final double kSwipeGestureLeft = 1.000000;
            private static final double kSwipeGestureRight = -1.000000;
            private static final double kSwipeGestureUp = 1.000000;
            private static final double kSwipeGestureDown = -1.000000;

            /**
             * Available in Mac OS X v10.6 and later.
             *
             * @param event Swipe event
             */
            @Action
            public void swipeWithEvent(NSEvent event) {
                if(event.deltaY().doubleValue() == kSwipeGestureUp) {
                    NSInteger row = bookmarkTable.selectedRow();
                    NSInteger next;
                    if(-1 == row.intValue()) {
                        // No current selection
                        next = new NSInteger(0);
                    }
                    else {
                        next = new NSInteger(row.longValue() - 1);
                    }
                    bookmarkTable.selectRowIndexes(
                            NSIndexSet.indexSetWithIndex(next), false);
                }
                else if(event.deltaY().doubleValue() == kSwipeGestureDown) {
                    NSInteger row = bookmarkTable.selectedRow();
                    NSInteger next;
                    if(-1 == row.intValue()) {
                        // No current selection
                        next = new NSInteger(0);
                    }
                    else {
                        next = new NSInteger(row.longValue() + 1);
                    }
                    bookmarkTable.selectRowIndexes(
                            NSIndexSet.indexSetWithIndex(next), false);
                }
            }
        }).id());
        // receive drag events from types
        this.bookmarkTable.registerForDraggedTypes(NSArray.arrayWithObjects(
                NSPasteboard.URLPboardType,
                NSPasteboard.StringPboardType,
                NSPasteboard.FilenamesPboardType, //accept bookmark files dragged from the Finder
                NSPasteboard.FilesPromisePboardType,
                "HostPBoardType" //moving bookmarks
        ));
        this._updateBookmarkCell();

        final int size = preferences.getInteger("bookmark.icon.size");
        if(BookmarkCell.SMALL_BOOKMARK_SIZE == size) {
            this.bookmarkTable.setRowHeight(new CGFloat(18));
        }
        else if(BookmarkCell.MEDIUM_BOOKMARK_SIZE == size) {
            this.bookmarkTable.setRowHeight(new CGFloat(45));
        }
        else {
            this.bookmarkTable.setRowHeight(new CGFloat(70));
        }

        // setting appearance attributes()
        this.bookmarkTable.setUsesAlternatingRowBackgroundColors(preferences.getBoolean("browser.alternatingRows"));
        this.bookmarkTable.setGridStyleMask(NSTableView.NSTableViewGridNone);

        // selection properties
        this.bookmarkTable.setAllowsMultipleSelection(true);
        this.bookmarkTable.setAllowsEmptySelection(true);
        this.bookmarkTable.setAllowsColumnResizing(false);
        this.bookmarkTable.setAllowsColumnSelection(false);
        this.bookmarkTable.setAllowsColumnReordering(false);
        this.bookmarkTable.sizeToFit();
    }

    @Outlet
    private NSPopUpButton actionPopupButton;

    public void setActionPopupButton(NSPopUpButton actionPopupButton) {
        this.actionPopupButton = actionPopupButton;
        this.actionPopupButton.setPullsDown(true);
        this.actionPopupButton.setAutoenablesItems(true);
        final NSInteger index = new NSInteger(0);
        this.actionPopupButton.insertItemWithTitle_atIndex(StringUtils.EMPTY, index);
        this.actionPopupButton.itemAtIndex(index).setImage(IconCacheFactory.<NSImage>get().iconNamed("gear.tiff"));
    }

    @Outlet
    private NSComboBox quickConnectPopup;

    private ProxyController quickConnectPopupModel = new QuickConnectModel();

    public void setQuickConnectPopup(NSComboBox quickConnectPopup) {
        this.quickConnectPopup = quickConnectPopup;
        this.quickConnectPopup.setTarget(this.id());
        this.quickConnectPopup.setCompletes(true);
        this.quickConnectPopup.setAction(Foundation.selector("quickConnectSelectionChanged:"));
        // Make sure action is not sent twice.
        this.quickConnectPopup.cell().setSendsActionOnEndEditing(false);
        this.quickConnectPopup.setUsesDataSource(true);
        this.quickConnectPopup.setDataSource(quickConnectPopupModel.id());
        NSNotificationCenter.defaultCenter().addObserver(this.id(),
                Foundation.selector("quickConnectWillPopUp:"),
                NSComboBox.ComboBoxWillPopUpNotification,
                this.quickConnectPopup);
        this.quickConnectWillPopUp(null);
    }

    private static class QuickConnectModel extends ProxyController implements NSComboBox.DataSource {
        @Override
        public NSInteger numberOfItemsInComboBox(final NSComboBox combo) {
            return new NSInteger(BookmarkCollection.defaultCollection().size());
        }

        @Override
        public NSObject comboBox_objectValueForItemAtIndex(final NSComboBox sender, final NSInteger row) {
            return NSString.stringWithString(
                    BookmarkNameProvider.toString(BookmarkCollection.defaultCollection().get(row.intValue()))
            );
        }
    }

    public void quickConnectWillPopUp(NSNotification notification) {
        int size = BookmarkCollection.defaultCollection().size();
        quickConnectPopup.setNumberOfVisibleItems(size > 10 ? new NSInteger(10) : new NSInteger(size));
    }

    @Action
    public void quickConnectSelectionChanged(final ID sender) {
        String input = quickConnectPopup.stringValue();
        if(StringUtils.isBlank(input)) {
            return;
        }
        input = input.trim();
        // First look for equivalent bookmarks
        for(Host h : BookmarkCollection.defaultCollection()) {
            if(BookmarkNameProvider.toString(h).equals(input)) {
                this.mount(h);
                return;
            }
        }
        // Try to parse the input as a URL and extract protocol, hostname, username and password if any.
        this.mount(HostParser.parse(input));
    }

    @Outlet
    private NSTextField searchField;

    public void setSearchField(NSTextField searchField) {
        this.searchField = searchField;
        NSNotificationCenter.defaultCenter().addObserver(this.id(),
                Foundation.selector("searchFieldTextDidChange:"),
                NSControl.NSControlTextDidChangeNotification,
                this.searchField);
    }

    @Action
    public void searchButtonClicked(final ID sender) {
        this.window().makeFirstResponder(searchField);
    }

    public void searchFieldTextDidChange(NSNotification notification) {
        if(this.getSelectedTabView() == TAB_BOOKMARKS) {
            this.setBookmarkFilter(searchField.stringValue());
        }
        else { // TAB_LIST_VIEW || TAB_OUTLINE_VIEW
            this.setPathFilter(searchField.stringValue());
            this.reload(true);
        }
    }

    private void setBookmarkFilter(final String searchString) {
        if(StringUtils.isBlank(searchString)) {
            searchField.setStringValue(StringUtils.EMPTY);
            bookmarkModel.setFilter(null);
        }
        else {
            bookmarkModel.setFilter(new HostFilter() {
                @Override
                public boolean accept(Host host) {
                    return StringUtils.lowerCase(BookmarkNameProvider.toString(host)).contains(searchString.toLowerCase(Locale.ROOT))
                            || ((null != host.getComment()) && StringUtils.lowerCase(host.getComment()).contains(searchString.toLowerCase(Locale.ROOT)))
                            || StringUtils.lowerCase(host.getHostname()).contains(searchString.toLowerCase(Locale.ROOT));
                }
            });
        }
        this.reloadBookmarks();
    }

    // ----------------------------------------------------------
    // Manage Bookmarks
    // ----------------------------------------------------------

    @Action
    public void connectBookmarkButtonClicked(final ID sender) {
        if(bookmarkTable.numberOfSelectedRows().intValue() == 1) {
            final Host selected = bookmarkModel.getSource().get(bookmarkTable.selectedRow().intValue());
            this.mount(selected);
        }
    }

    @Outlet
    private NSButton editBookmarkButton;

    public void setEditBookmarkButton(NSButton editBookmarkButton) {
        this.editBookmarkButton = editBookmarkButton;
        this.editBookmarkButton.setEnabled(false);
        this.editBookmarkButton.setTarget(this.id());
        this.editBookmarkButton.setAction(Foundation.selector("editBookmarkButtonClicked:"));
    }

    @Action
    public void editBookmarkButtonClicked(final ID sender) {
        final BookmarkController c = BookmarkControllerFactory.create(
                bookmarkModel.getSource().get(bookmarkTable.selectedRow().intValue())
        );
        c.window().makeKeyAndOrderFront(null);
    }

    @Action
    public void duplicateBookmarkButtonClicked(final ID sender) {
        final Host selected = bookmarkModel.getSource().get(bookmarkTable.selectedRow().intValue());
        this.selectBookmarks();
        final Host duplicate = new HostDictionary().deserialize(selected.serialize(SerializerFactory.get()));
        // Make sure a new UUID is asssigned for duplicate
        duplicate.setUuid(null);
        this.addBookmark(duplicate);
    }

    @Outlet
    private NSButton addBookmarkButton;

    public void setAddBookmarkButton(NSButton addBookmarkButton) {
        this.addBookmarkButton = addBookmarkButton;
        this.addBookmarkButton.setTarget(this.id());
        this.addBookmarkButton.setAction(Foundation.selector("addBookmarkButtonClicked:"));
    }

    @Action
    public void addBookmarkButtonClicked(final ID sender) {
        final Host bookmark;
        if(this.isMounted()) {
            Path selected = this.getSelectedPath();
            if(null == selected || !selected.isDirectory()) {
                selected = this.workdir();
            }
            bookmark = new HostDictionary().deserialize(this.session.getHost().serialize(SerializerFactory.get()));
            // Make sure a new UUID is asssigned for duplicate
            bookmark.setUuid(null);
            bookmark.setDefaultPath(selected.getAbsolute());
        }
        else {
            bookmark = new Host(ProtocolFactory.forName(preferences.getProperty("connection.protocol.default")),
                    preferences.getProperty("connection.hostname.default"),
                    preferences.getInteger("connection.port.default"));
        }
        this.selectBookmarks();
        this.addBookmark(bookmark);
    }

    public void addBookmark(Host item) {
        bookmarkModel.setFilter(null);
        bookmarkModel.getSource().add(item);
        final int row = bookmarkModel.getSource().lastIndexOf(item);
        final NSInteger index = new NSInteger(row);
        bookmarkTable.selectRowIndexes(NSIndexSet.indexSetWithIndex(index), false);
        bookmarkTable.scrollRowToVisible(index);
        final BookmarkController c = BookmarkControllerFactory.create(item);
        c.window().makeKeyAndOrderFront(null);
    }

    @Outlet
    private NSButton deleteBookmarkButton;

    public void setDeleteBookmarkButton(NSButton deleteBookmarkButton) {
        this.deleteBookmarkButton = deleteBookmarkButton;
        this.deleteBookmarkButton.setEnabled(false);
        this.deleteBookmarkButton.setTarget(this.id());
        this.deleteBookmarkButton.setAction(Foundation.selector("deleteBookmarkButtonClicked:"));
    }

    @Action
    public void deleteBookmarkButtonClicked(final ID sender) {
        NSIndexSet iterator = bookmarkTable.selectedRowIndexes();
        final List<Host> selected = new ArrayList<Host>();
        for(NSUInteger index = iterator.firstIndex(); !index.equals(NSIndexSet.NSNotFound); index = iterator.indexGreaterThanIndex(index)) {
            selected.add(bookmarkModel.getSource().get(index.intValue()));
        }
        StringBuilder alertText = new StringBuilder(
                LocaleFactory.localizedString("Do you want to delete the selected bookmark?"));
        int i = 0;
        Iterator<Host> iter = selected.iterator();
        while(i < 10 && iter.hasNext()) {
            alertText.append("\n").append(Character.toString('\u2022')).append(" ").append(
                    BookmarkNameProvider.toString(iter.next())
            );
            i++;
        }
        if(iter.hasNext()) {
            alertText.append("\n").append(Character.toString('\u2022')).append(" " + "…");
        }
        final NSAlert alert = NSAlert.alert(LocaleFactory.localizedString("Delete Bookmark"),
                alertText.toString(),
                LocaleFactory.localizedString("Delete"),
                LocaleFactory.localizedString("Cancel"),
                null);
        this.alert(alert, new SheetCallback() {
            @Override
            public void callback(int returncode) {
                if(returncode == DEFAULT_OPTION) {
                    bookmarkTable.deselectAll(null);
                    bookmarkModel.getSource().removeAll(selected);
                }
            }
        });
    }

    // ----------------------------------------------------------
    // Browser navigation
    // ----------------------------------------------------------

    private static final int NAVIGATION_LEFT_SEGMENT_BUTTON = 0;
    private static final int NAVIGATION_RIGHT_SEGMENT_BUTTON = 1;

    private static final int NAVIGATION_UP_SEGMENT_BUTTON = 0;

    private NSSegmentedControl navigationButton;

    public void setNavigationButton(NSSegmentedControl navigationButton) {
        this.navigationButton = navigationButton;
        this.navigationButton.setTarget(this.id());
        this.navigationButton.setAction(Foundation.selector("navigationButtonClicked:"));
        this.navigationButton.setImage_forSegment(IconCacheFactory.<NSImage>get().iconNamed("nav-backward.tiff"),
                NAVIGATION_LEFT_SEGMENT_BUTTON);
        this.navigationButton.setImage_forSegment(IconCacheFactory.<NSImage>get().iconNamed("nav-forward.tiff"),
                NAVIGATION_RIGHT_SEGMENT_BUTTON);
    }

    @Action
    public void navigationButtonClicked(NSSegmentedControl sender) {
        switch(sender.selectedSegment()) {
            case NAVIGATION_LEFT_SEGMENT_BUTTON: {
                this.backButtonClicked(sender.id());
                break;
            }
            case NAVIGATION_RIGHT_SEGMENT_BUTTON: {
                this.forwardButtonClicked(sender.id());
                break;
            }
        }
    }

    @Action
    public void backButtonClicked(final ID sender) {
        final Path selected = navigation.back();
        if(selected != null) {
            final Path previous = this.workdir();
            if(previous.getParent().equals(selected)) {
                this.setWorkdir(selected, previous);
            }
            else {
                this.setWorkdir(selected);
            }
        }
    }

    @Action
    public void forwardButtonClicked(final ID sender) {
        final Path selected = navigation.forward();
        if(selected != null) {
            this.setWorkdir(selected);
        }
    }

    @Outlet
    private NSSegmentedControl upButton;

    public void setUpButton(NSSegmentedControl upButton) {
        this.upButton = upButton;
        this.upButton.setTarget(this.id());
        this.upButton.setAction(Foundation.selector("upButtonClicked:"));
        this.upButton.setImage_forSegment(IconCacheFactory.<NSImage>get().iconNamed("nav-up.tiff"),
                NAVIGATION_UP_SEGMENT_BUTTON);
    }

    public void upButtonClicked(final ID sender) {
        final Path previous = this.workdir();
        this.setWorkdir(previous.getParent(), previous);
    }

    private Path workdir;

    @Outlet
    private NSPopUpButton pathPopupButton;

    public void setPathPopup(NSPopUpButton pathPopupButton) {
        this.pathPopupButton = pathPopupButton;
        this.pathPopupButton.setTarget(this.id());
        this.pathPopupButton.setAction(Foundation.selector("pathPopupSelectionChanged:"));
    }

    @Action
    public void pathPopupSelectionChanged(final NSPopUpButton sender) {
        final String selected = sender.selectedItem().representedObject();
        if(selected != null) {
            final Path workdir = this.workdir();
            Path p = workdir;
            while(!p.getAbsolute().equals(selected)) {
                p = p.getParent();
            }
            this.setWorkdir(p);
            if(workdir.getParent().equals(p)) {
                this.setWorkdir(p, workdir);
            }
            else {
                this.setWorkdir(p);
            }
        }
    }

    @Outlet
    private NSPopUpButton encodingPopup;

    public void setEncodingPopup(NSPopUpButton encodingPopup) {
        this.encodingPopup = encodingPopup;
        this.encodingPopup.setTarget(this.id());
        this.encodingPopup.setAction(Foundation.selector("encodingButtonClicked:"));
        this.encodingPopup.removeAllItems();
        this.encodingPopup.addItemsWithTitles(NSArray.arrayWithObjects(MainController.availableCharsets()));
        this.encodingPopup.selectItemWithTitle(preferences.getProperty("browser.charset.encoding"));
    }

    @Action
    public void encodingButtonClicked(final NSPopUpButton sender) {
        this.encodingChanged(sender.titleOfSelectedItem());
    }

    @Action
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
            session.getHost().setEncoding(encoding);
            this.mount(session.getHost());
        }
    }

    /**
     * @param encoding Character encoding
     */
    private void setEncoding(final String encoding) {
        this.encodingPopup.selectItemWithTitle(encoding);
    }

    // ----------------------------------------------------------
    // Drawers
    // ----------------------------------------------------------

    @Action
    public void toggleLogDrawer(final ID sender) {
        this.logDrawer.toggle(this.id());
    }

    // ----------------------------------------------------------
    // Status
    // ----------------------------------------------------------

    @Outlet
    protected NSProgressIndicator statusSpinner;

    public void setStatusSpinner(NSProgressIndicator statusSpinner) {
        this.statusSpinner = statusSpinner;
        this.statusSpinner.setDisplayedWhenStopped(false);
        this.statusSpinner.setIndeterminate(true);
    }

    @Outlet
    protected NSProgressIndicator browserSpinner;

    public void setBrowserSpinner(NSProgressIndicator browserSpinner) {
        this.browserSpinner = browserSpinner;
    }

    public NSProgressIndicator getBrowserSpinner() {
        return browserSpinner;
    }

    @Outlet
    private NSTextField statusLabel;

    public void setStatusLabel(NSTextField statusLabel) {
        this.statusLabel = statusLabel;
    }

    public void setStatus() {
        final BackgroundAction current = this.getActions().getCurrent();
        this.message(null != current ? current.getActivity() : null);
    }

    @Override
    public void stop(final BackgroundAction action) {
        statusSpinner.stopAnimation(null);
    }

    @Override
    public void start(final BackgroundAction action) {
        statusSpinner.startAnimation(null);
    }

    /**
     * @param label Status message
     */
    @Override
    public void message(final String label) {
        if(StringUtils.isNotBlank(label)) {
            // Update the status label at the bottom of the browser window
            statusLabel.setAttributedStringValue(NSAttributedString.attributedStringWithAttributes(label,
                    TRUNCATE_MIDDLE_ATTRIBUTES));
        }
        else {
            if(getSelectedTabView() == TAB_BOOKMARKS) {
                statusLabel.setAttributedStringValue(
                        NSAttributedString.attributedStringWithAttributes(String.format("%s %s", bookmarkTable.numberOfRows(),
                                        LocaleFactory.localizedString("Bookmarks")),
                                TRUNCATE_MIDDLE_ATTRIBUTES
                        )
                );
            }
            else {
                // Browser view
                if(isConnected()) {
                    statusLabel.setAttributedStringValue(
                            NSAttributedString.attributedStringWithAttributes(MessageFormat.format(LocaleFactory.localizedString("{0} Files"),
                                            String.valueOf(getSelectedBrowserView().numberOfRows())),
                                    TRUNCATE_MIDDLE_ATTRIBUTES
                            )
                    );
                }
                else {
                    statusLabel.setStringValue(StringUtils.EMPTY);
                }
            }
        }
    }

    @Override
    public void log(final boolean request, final String message) {
        transcript.log(request, message);
    }

    @Outlet
    private NSButton securityLabel;

    public void setSecurityLabel(NSButton securityLabel) {
        this.securityLabel = securityLabel;
        this.securityLabel.setEnabled(false);
        this.securityLabel.setTarget(this.id());
        this.securityLabel.setAction(Foundation.selector("securityLabelClicked:"));
    }

    @Action
    public void securityLabelClicked(final ID sender) {
        if(session instanceof SSLSession) {
            final SSLSession<?> secured = (SSLSession) session;
            final List<X509Certificate> certificates = secured.getAcceptedIssuers();
            try {
                CertificateStoreFactory.get().display(certificates);
            }
            catch(CertificateException e) {
                log.warn(String.format("Failure decoding certificate %s", e.getMessage()));
            }
        }
    }

    // ----------------------------------------------------------
    // Selector methods for the toolbar items
    // ----------------------------------------------------------

    public void quicklookButtonClicked(final ID sender) {
        if(quicklook.isOpen()) {
            quicklook.close();
        }
        else {
            this.updateQuickLookSelection(this.getSelectedPaths());
        }
    }

    /**
     * Marks all expanded directories as invalid and tells the
     * browser table to reload its data
     *
     * @param sender Toolbar button
     */
    @Action
    public void reloadButtonClicked(final ID sender) {
        if(this.isMounted()) {
            switch(browserSwitchView.selectedSegment()) {
                case SWITCH_OUTLINE_VIEW: {
                    for(int i = 0; i < browserOutlineView.numberOfRows().intValue(); i++) {
                        final NSObject item = browserOutlineView.itemAtRow(new NSInteger(i));
                        if(browserOutlineView.isItemExpanded(item)) {
                            cache.invalidate(new NSObjectPathReference(item));
                        }
                    }
                    break;
                }
            }
            cache.invalidate(this.workdir().getReference());
            this.reload(true);
        }
    }

    /**
     * Open a new browser with the current selected folder as the working directory
     *
     * @param sender Toolbar button
     */
    @Action
    public void newBrowserButtonClicked(final ID sender) {
        Path selected = this.getSelectedPath();
        if(null == selected || !selected.isDirectory()) {
            selected = this.workdir();
        }
        BrowserController c = MainController.newDocument(true);
        final Host host = new HostDictionary().deserialize(session.getHost().serialize(SerializerFactory.get()));
        host.setDefaultPath(selected.getAbsolute());
        c.mount(host);
    }

    /**
     * @param source      The original file to duplicate
     * @param destination The destination of the duplicated file
     */
    protected void duplicatePath(final Path source, final Path destination) {
        this.duplicatePaths(Collections.singletonMap(source, destination));
    }

    /**
     * @param selected A map with the original files as the key and the destination
     *                 files as the value
     */
    protected void duplicatePaths(final Map<Path, Path> selected) {
        this.checkOverwrite(new ArrayList<Path>(selected.values()), new DefaultMainAction() {
            @Override
            public void run() {
                transfer(new CopyTransfer(session.getHost(), session.getHost(), selected),
                        new ArrayList<Path>(selected.values()), true);
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
        this.checkMove(selected, new DefaultMainAction() {
            @Override
            public void run() {
                final ArrayList<Path> changed = new ArrayList<Path>();
                changed.addAll(selected.keySet());
                changed.addAll(selected.values());
                background(new WorkerBackgroundAction(BrowserController.this, session, cache,
                                new MoveWorker(session, selected) {
                                    @Override
                                    public void cleanup(final Boolean result) {
                                        reload(changed, new ArrayList<Path>(selected.values()));
                                    }
                                }
                        )
                );
            }
        });
    }

    /**
     * Displays a warning dialog about already existing files
     *
     * @param selected The files to check for existance
     */
    private void checkOverwrite(final List<Path> selected, final MainAction action) {
        StringBuilder alertText = new StringBuilder(
                LocaleFactory.localizedString("A file with the same name already exists. Do you want to replace the existing file?"));
        int i = 0;
        Iterator<Path> iter;
        boolean shouldWarn = false;
        for(iter = selected.iterator(); iter.hasNext(); ) {
            final Path item = iter.next();
            if(cache.lookup(item.getReference()) != null) {
                if(i < 10) {
                    alertText.append("\n").append(Character.toString('\u2022')).append(" ").append(item.getName());
                }
                shouldWarn = true;
            }
            i++;
        }
        if(i >= 10) {
            alertText.append("\n").append(Character.toString('\u2022')).append(" ...)");
        }
        if(shouldWarn) {
            NSAlert alert = NSAlert.alert(
                    LocaleFactory.localizedString("Overwrite"), //title
                    alertText.toString(),
                    LocaleFactory.localizedString("Overwrite"), // defaultbutton
                    LocaleFactory.localizedString("Cancel"), //alternative button
                    null //other button
            );
            this.alert(alert, new SheetCallback() {
                @Override
                public void callback(final int returncode) {
                    if(returncode == DEFAULT_OPTION) {
                        action.run();
                    }
                }
            });
        }
        else {
            action.run();
        }
    }

    /**
     * Displays a warning dialog about files to be moved
     *
     * @param selected The files to check for existence
     */
    private void checkMove(final Map<Path, Path> selected, final MainAction action) {
        if(preferences.getBoolean("browser.move.confirm")) {
            StringBuilder alertText = new StringBuilder(
                    LocaleFactory.localizedString("Do you want to move the selected files?"));
            int i = 0;
            boolean rename = false;
            Iterator<Map.Entry<Path, Path>> iter;
            for(iter = selected.entrySet().iterator(); i < 10 && iter.hasNext(); ) {
                final Map.Entry<Path, Path> next = iter.next();
                if(next.getKey().getParent().equals(next.getValue().getParent())) {
                    rename = true;
                }
                alertText.append(String.format("\n%s %s", Character.toString('\u2022'), next.getKey().getName()));
                i++;
            }
            if(iter.hasNext()) {
                alertText.append(String.format("\n%s ...)", Character.toString('\u2022')));
            }
            final NSAlert alert = NSAlert.alert(
                    rename ? LocaleFactory.localizedString("Rename") : LocaleFactory.localizedString("Move"), //title
                    alertText.toString(),
                    rename ? LocaleFactory.localizedString("Rename") : LocaleFactory.localizedString("Move"), // default button
                    LocaleFactory.localizedString("Cancel"), //alternative button
                    null //other button
            );
            alert.setShowsSuppressionButton(true);
            alert.suppressionButton().setTitle(LocaleFactory.localizedString("Don't ask again", "Configuration"));
            this.alert(alert, new SheetCallback() {
                @Override
                public void callback(final int returncode) {
                    if(alert.suppressionButton().state() == NSCell.NSOnState) {
                        // Never show again.
                        preferences.setProperty("browser.move.confirm", false);
                    }
                    if(returncode == DEFAULT_OPTION) {
                        checkOverwrite(new ArrayList<Path>(selected.values()), action);
                    }
                }
            });
        }
        else {
            this.checkOverwrite(new ArrayList<Path>(selected.values()), action);
        }
    }

    /**
     * Recursively deletes the file
     *
     * @param file File or directory
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
        final List<Path> normalized = PathNormalizer.normalize(selected);
        if(normalized.isEmpty()) {
            return;
        }
        StringBuilder alertText =
                new StringBuilder(LocaleFactory.localizedString("Really delete the following files? This cannot be undone."));
        int i = 0;
        Iterator<Path> iter;
        for(iter = normalized.iterator(); i < 10 && iter.hasNext(); ) {
            alertText.append("\n").append(Character.toString('\u2022')).append(" ").append(iter.next().getName());
            i++;
        }
        if(iter.hasNext()) {
            alertText.append("\n").append(Character.toString('\u2022')).append(" " + "…");
        }
        NSAlert alert = NSAlert.alert(LocaleFactory.localizedString("Delete"), //title
                alertText.toString(),
                LocaleFactory.localizedString("Delete"), // defaultbutton
                LocaleFactory.localizedString("Cancel"), //alternative button
                null //other button
        );
        this.alert(alert, new SheetCallback() {
            @Override
            public void callback(final int returncode) {
                if(returncode == DEFAULT_OPTION) {
                    BrowserController.this.deletePathsImpl(normalized);
                }
            }
        });
    }

    private void deletePathsImpl(final List<Path> files) {
        this.background(new WorkerBackgroundAction(this, session, cache,
                        new DeleteWorker(session, LoginControllerFactory.get(BrowserController.this), files) {
                            @Override
                            public void cleanup(final Boolean result) {
                                reload(files, false);
                            }
                        }
                )
        );
    }

    public void revertPaths(final List<Path> files) {
        this.background(new WorkerBackgroundAction(this, session, cache,
                new RevertWorker(session, files) {
                    @Override
                    public void cleanup(final Boolean result) {
                        reload(files, false);
                    }
                }
        ));
    }

    /**
     * @param selected File
     * @return True if the selected path is editable (not a directory and no known binary file)
     */
    protected boolean isEditable(final Path selected) {
        if(this.isMounted()) {
            if(session.getHost().getCredentials().isAnonymousLogin()) {
                return false;
            }
            return selected.isFile();
        }
        return false;
    }

    @Action
    public void gotoButtonClicked(final ID sender) {
        SheetController sheet = new GotoController(this, cache);
        sheet.beginSheet();
    }

    @Action
    public void createFileButtonClicked(final ID sender) {
        SheetController sheet = new CreateFileController(this, cache);
        sheet.beginSheet();
    }

    @Action
    public void createSymlinkButtonClicked(final ID sender) {
        SheetController sheet = new CreateSymlinkController(this, cache);
        sheet.beginSheet();
    }

    @Action
    public void duplicateFileButtonClicked(final ID sender) {
        SheetController sheet = new DuplicateFileController(this, cache);
        sheet.beginSheet();
    }

    @Action
    public void createFolderButtonClicked(final ID sender) {
        final Location feature = session.getFeature(Location.class);
        SheetController sheet = new FolderController(this, cache, feature != null ? feature.getLocations() : Collections.<String>emptySet());
        sheet.beginSheet();
    }

    @Action
    public void renameFileButtonClicked(final ID sender) {
        final NSTableView browser = this.getSelectedBrowserView();
        browser.editRow(browser.columnWithIdentifier(BrowserTableDataSource.Column.filename.name()),
                browser.selectedRow(), true);
        final Path selected = this.getSelectedPath();
        if(StringUtils.isNotBlank(selected.getExtension())) {
            NSText view = browser.currentEditor();
            int index = selected.getName().indexOf(selected.getExtension()) - 1;
            if(index > 0) {
                view.setSelectedRange(NSRange.NSMakeRange(new NSUInteger(0), new NSUInteger(index)));
            }
        }
    }

    @Action
    public void sendCustomCommandClicked(final ID sender) {
        SheetController sheet = new CommandController(this, this.session);
        sheet.beginSheet();
    }

    @Action
    public void editMenuClicked(final NSMenuItem sender) {
        for(Path selected : this.getSelectedPaths()) {
            final Editor editor = EditorFactory.instance().create(this, session,
                    new Application(sender.representedObject()), selected);
            editor.open();
        }
    }

    @Action
    public void editButtonClicked(final ID sender) {
        for(Path selected : this.getSelectedPaths()) {
            final Editor editor = EditorFactory.instance().create(this, session, selected);
            editor.open();
        }
    }

    @Action
    public void openBrowserButtonClicked(final ID sender) {
        final DescriptiveUrlBag list;
        if(this.getSelectionCount() == 1) {
            list = session.getFeature(UrlProvider.class).toUrl(this.getSelectedPath());
        }
        else {
            list = session.getFeature(UrlProvider.class).toUrl(this.workdir());
        }
        if(!list.isEmpty()) {
            BrowserLauncherFactory.get().open(list.find(DescriptiveUrl.Type.http).getUrl());
        }
    }

    @Action
    public void infoButtonClicked(final ID sender) {
        if(this.getSelectionCount() > 0) {
            InfoController c = InfoControllerFactory.create(this, this.getSelectedPaths());
            c.window().makeKeyAndOrderFront(null);
        }
    }

    @Action
    public void revertFileButtonClicked(final ID sender) {
        this.revertPaths(this.getSelectedPaths());
    }

    @Action
    public void deleteFileButtonClicked(final ID sender) {
        this.deletePaths(this.getSelectedPaths());
    }

    private NSOpenPanel downloadToPanel;

    @Action
    public void downloadToButtonClicked(final ID sender) {
        downloadToPanel = NSOpenPanel.openPanel();
        downloadToPanel.setCanChooseDirectories(true);
        downloadToPanel.setCanCreateDirectories(true);
        downloadToPanel.setCanChooseFiles(false);
        downloadToPanel.setAllowsMultipleSelection(false);
        downloadToPanel.setPrompt(LocaleFactory.localizedString("Choose"));
        downloadToPanel.beginSheetForDirectory(
                session.getHost().getDownloadFolder().getAbsolute(),
                null, this.window, this.id(),
                Foundation.selector("downloadToPanelDidEnd:returnCode:contextInfo:"),
                null);
    }

    public void downloadToPanelDidEnd_returnCode_contextInfo(final NSOpenPanel sheet, final int returncode, final ID contextInfo) {
        sheet.orderOut(this.id());
        if(returncode == SheetCallback.DEFAULT_OPTION) {
            String folder;
            if((folder = sheet.filename()) != null) {
                final List<TransferItem> downloads = new ArrayList<TransferItem>();
                for(Path file : this.getSelectedPaths()) {
                    downloads.add(new TransferItem(
                            file, LocalFactory.createLocal(LocalFactory.createLocal(folder), file.getName())));
                }
                this.transfer(new DownloadTransfer(session.getHost(), downloads), Collections.<Path>emptyList());
            }
        }
        downloadToPanel = null;
    }

    private NSSavePanel downloadAsPanel;

    @Action
    public void downloadAsButtonClicked(final ID sender) {
        downloadAsPanel = NSSavePanel.savePanel();
        downloadAsPanel.setMessage(LocaleFactory.localizedString("Download the selected file to…"));
        downloadAsPanel.setNameFieldLabel(LocaleFactory.localizedString("Download As:"));
        downloadAsPanel.setPrompt(LocaleFactory.localizedString("Download"));
        downloadAsPanel.setCanCreateDirectories(true);
        downloadAsPanel.beginSheetForDirectory(session.getHost().getDownloadFolder().getAbsolute(),
                this.getSelectedPath().getName(), this.window, this.id(),
                Foundation.selector("downloadAsPanelDidEnd:returnCode:contextInfo:"),
                null);
    }

    public void downloadAsPanelDidEnd_returnCode_contextInfo(NSSavePanel sheet, int returncode, final ID contextInfo) {
        sheet.orderOut(this.id());
        if(returncode == SheetCallback.DEFAULT_OPTION) {
            String filename;
            if((filename = sheet.filename()) != null) {
                final Path selected = this.getSelectedPath();
                this.transfer(new DownloadTransfer(session.getHost(), selected,
                        LocalFactory.createLocal(filename)), Collections.<Path>emptyList());
            }
        }
    }

    private NSOpenPanel syncPanel;

    @Action
    public void syncButtonClicked(final ID sender) {
        final Path selection;
        if(this.getSelectionCount() == 1 &&
                this.getSelectedPath().isDirectory()) {
            selection = this.getSelectedPath();
        }
        else {
            selection = this.workdir();
        }
        syncPanel = NSOpenPanel.openPanel();
        syncPanel.setCanChooseDirectories(selection.isDirectory());
        syncPanel.setTreatsFilePackagesAsDirectories(true);
        syncPanel.setCanChooseFiles(selection.isFile());
        syncPanel.setCanCreateDirectories(true);
        syncPanel.setAllowsMultipleSelection(false);
        syncPanel.setMessage(MessageFormat.format(LocaleFactory.localizedString("Synchronize {0} with"),
                selection.getName()));
        syncPanel.setPrompt(LocaleFactory.localizedString("Choose"));
        syncPanel.beginSheetForDirectory(session.getHost().getDownloadFolder().getAbsolute(),
                null, this.window, this.id(),
                Foundation.selector("syncPanelDidEnd:returnCode:contextInfo:"), null //context info
        );
    }

    public void syncPanelDidEnd_returnCode_contextInfo(final NSOpenPanel sheet, final int returncode, final ID contextInfo) {
        sheet.orderOut(this.id());
        if(returncode == SheetCallback.DEFAULT_OPTION) {
            if(sheet.filenames().count().intValue() > 0) {
                final Path selected;
                if(this.getSelectionCount() == 1 && this.getSelectedPath().isDirectory()) {
                    selected = this.getSelectedPath();
                }
                else {
                    selected = this.workdir();
                }
                this.transfer(new SyncTransfer(session.getHost(),
                                new TransferItem(selected, LocalFactory.createLocal(sheet.filenames().lastObject().toString())))
                );
            }
        }
    }

    @Action
    public void downloadButtonClicked(final ID sender) {
        final List<TransferItem> downloads = new ArrayList<TransferItem>();
        for(Path file : this.getSelectedPaths()) {
            downloads.add(new TransferItem(
                    file, LocalFactory.createLocal(session.getHost().getDownloadFolder(), file.getName())));
        }
        this.transfer(new DownloadTransfer(session.getHost(), downloads), Collections.<Path>emptyList());
    }

    private NSOpenPanel uploadPanel;

    private NSButton uploadPanelHiddenFilesCheckbox;

    @Action
    public void uploadButtonClicked(final ID sender) {
        uploadPanel = NSOpenPanel.openPanel();
        uploadPanel.setCanChooseDirectories(true);
        uploadPanel.setCanCreateDirectories(false);
        uploadPanel.setTreatsFilePackagesAsDirectories(true);
        uploadPanel.setCanChooseFiles(true);
        uploadPanel.setAllowsMultipleSelection(true);
        uploadPanel.setPrompt(LocaleFactory.localizedString("Upload"));
        if(uploadPanel.respondsToSelector(Foundation.selector("setShowsHiddenFiles:"))) {
            uploadPanelHiddenFilesCheckbox = NSButton.buttonWithFrame(new NSRect(0, 0));
            uploadPanelHiddenFilesCheckbox.setTitle(LocaleFactory.localizedString("Show Hidden Files"));
            uploadPanelHiddenFilesCheckbox.setTarget(this.id());
            uploadPanelHiddenFilesCheckbox.setAction(Foundation.selector("uploadPanelSetShowHiddenFiles:"));
            uploadPanelHiddenFilesCheckbox.setButtonType(NSButton.NSSwitchButton);
            uploadPanelHiddenFilesCheckbox.setState(NSCell.NSOffState);
            uploadPanelHiddenFilesCheckbox.sizeToFit();
            uploadPanel.setAccessoryView(uploadPanelHiddenFilesCheckbox);
        }
        uploadPanel.beginSheetForDirectory(session.getHost().getDownloadFolder().getAbsolute(),
                null, this.window,
                this.id(),
                Foundation.selector("uploadPanelDidEnd:returnCode:contextInfo:"),
                null);
    }

    public void uploadPanelSetShowHiddenFiles(ID sender) {
        uploadPanel.setShowsHiddenFiles(uploadPanelHiddenFilesCheckbox.state() == NSCell.NSOnState);
    }

    public void uploadPanelDidEnd_returnCode_contextInfo(final NSOpenPanel sheet, final int returncode, final ID contextInfo) {
        sheet.orderOut(this.id());
        if(returncode == SheetCallback.DEFAULT_OPTION) {
            final Path destination = new UploadTargetFinder(workdir).find(this.getSelectedPath());
            // Selected files on the local filesystem
            final NSArray selected = sheet.filenames();
            final NSEnumerator iterator = selected.objectEnumerator();
            final List<TransferItem> uploads = new ArrayList<TransferItem>();
            NSObject next;
            while((next = iterator.nextObject()) != null) {
                final Local local = LocalFactory.createLocal(next.toString());
                uploads.add(new TransferItem(
                        new Path(destination, local.getName(),
                                local.isDirectory() ? EnumSet.of(Path.Type.directory) : EnumSet.of(Path.Type.file)), local
                ));
            }
            this.transfer(new UploadTransfer(session.getHost(), uploads));
        }
        uploadPanel = null;
        uploadPanelHiddenFilesCheckbox = null;
    }

    protected void transfer(final Transfer transfer) {
        final List<Path> selected = new ArrayList<Path>();
        for(TransferItem i : transfer.getRoots()) {
            selected.add(i.remote);
        }
        this.transfer(transfer, selected);
    }

    /**
     * Transfers the files either using the queue or using the browser session if #connection.pool.max is 1
     *
     * @param transfer Transfer Operation
     */
    protected void transfer(final Transfer transfer, final List<Path> selected) {
        // Determine from current browser sesssion if new connection should be opened for transfers
        this.transfer(transfer, selected, session.getMaxConnections() == 1);
    }

    /**
     * @param transfer Transfer Operation
     * @param browser  Transfer in browser window
     */
    protected void transfer(final Transfer transfer, final List<Path> selected, boolean browser) {
        final TransferCallback callback = new TransferCallback() {
            @Override
            public void complete(final Transfer transfer) {
                invoke(new WindowMainAction(BrowserController.this) {
                    @Override
                    public void run() {
                        reload(selected, selected, true);
                    }
                });
            }
        };
        if(browser) {
            this.background(new TransferBackgroundAction(this, session, new TransferAdapter() {
                @Override
                public void progress(final TransferProgress status) {
                    message(status.getProgress());
                }
            }, this, transfer, new TransferOptions()) {
                @Override
                public void finish() {
                    if(transfer.isComplete()) {
                        callback.complete(transfer);
                    }
                    super.finish();
                }
            });
        }
        else {
            TransferControllerFactory.get().start(transfer, new TransferOptions(), callback);
        }
    }

    @Action
    public void insideButtonClicked(final ID sender) {
        final Path selected = this.getSelectedPath(); //first row selected
        if(null == selected) {
            return;
        }
        if(selected.isDirectory()) {
            this.setWorkdir(selected);
        }
        else if(selected.isFile() || this.getSelectionCount() > 1) {
            if(preferences.getBoolean("browser.doubleclick.edit")) {
                this.editButtonClicked(null);
            }
            else {
                this.downloadButtonClicked(null);
            }
        }
    }

    @Action
    public void connectButtonClicked(final ID sender) {
        final SheetController controller = ConnectionControllerFactory.create(this);
        this.addListener(new WindowListener() {
            @Override
            public void windowWillClose() {
                controller.invalidate();
            }
        });
        controller.beginSheet();
    }

    @Action
    public void disconnectButtonClicked(final ID sender) {
        if(this.isActivityRunning()) {
            // Remove all pending actions
            for(BackgroundAction action : this.getActions().toArray(
                    new BackgroundAction[this.getActions().size()])) {
                action.cancel();
            }
        }
        this.disconnect(new Runnable() {
            @Override
            public void run() {
                if(preferences.getBoolean("browser.disconnect.bookmarks.show")) {
                    selectBookmarks();
                }
                else {
                    selectBrowser(preferences.getInteger("browser.view"));
                }
            }
        });
    }

    @Action
    public void showHiddenFilesClicked(final NSMenuItem sender) {
        if(sender.state() == NSCell.NSOnState) {
            this.setShowHiddenFiles(false);
            sender.setState(NSCell.NSOffState);
        }
        else if(sender.state() == NSCell.NSOffState) {
            this.setShowHiddenFiles(true);
            sender.setState(NSCell.NSOnState);
        }
        if(this.isMounted()) {
            this.reload(true);
        }
    }

    /**
     * @return This browser's session or null if not mounted
     */
    public Session<?> getSession() {
        return session;
    }

    public Cache getCache() {
        return cache;
    }

    /**
     * @return true if the remote file system has been mounted
     */
    public boolean isMounted() {
        return session != null && workdir != null;
    }

    /**
     * @return true if mounted and the connection to the server is alive
     */
    public boolean isConnected() {
        if(this.isMounted()) {
            return session.isConnected();
        }
        return false;
    }

    /**
     * NSService
     * <p/>
     * Indicates whether the receiver can send and receive the specified pasteboard types.
     * <p/>
     * Either sendType or returnType—but not both—may be empty. If sendType is empty,
     * the service doesn’t require input from the application requesting the service.
     * If returnType is empty, the service doesn’t return data.
     *
     * @param sendType   The pasteboard type the application needs to send.
     * @param returnType The pasteboard type the application needs to receive.
     * @return The object that can send and receive the specified types or nil
     * if the receiver knows of no object that can send and receive data of that type.
     */
    public ID validRequestorForSendType_returnType(String sendType, String returnType) {
        log.debug("validRequestorForSendType_returnType:" + sendType + "," + returnType);
        if(StringUtils.isNotEmpty(sendType)) {
            // Cannot send any data type
            return null;
        }
        if(StringUtils.isNotEmpty(returnType)) {
            // Can receive filenames
            if(NSPasteboard.FilenamesPboardType.equals(sendType)) {
                return this.id();
            }
        }
        return null;
    }

    /**
     * NSService
     * <p/>
     * Reads data from the pasteboard and uses it to replace the current selection.
     *
     * @param pboard Pasteboard
     * @return YES if your implementation was able to read the pasteboard data successfully; otherwise, NO.
     */
    public boolean readSelectionFromPasteboard(NSPasteboard pboard) {
        return this.upload(pboard);
    }

    /**
     * NSService
     * <p/>
     * Writes the current selection to the pasteboard.
     *
     * @param pboard Pasteboard
     * @param types  Types in pasteboard
     * @return YES if your implementation was able to write one or more types to the pasteboard; otherwise, NO.
     */
    public boolean writeSelectionToPasteboard_types(NSPasteboard pboard, NSArray types) {
        return false;
    }

    @Action
    public void copy(final ID sender) {
        pasteboard.clear();
        pasteboard.setCopy(true);
        final List<Path> s = this.getSelectedPaths();
        for(Path p : s) {
            // Writing data for private use when the item gets dragged to the transfer queue.
            pasteboard.add(p);
        }
        final NSPasteboard clipboard = NSPasteboard.generalPasteboard();
        if(s.size() == 0) {
            s.add(this.workdir());
        }
        clipboard.declareTypes(NSArray.arrayWithObject(
                NSString.stringWithString(NSPasteboard.StringPboardType)), null);
        StringBuilder copy = new StringBuilder();
        for(Iterator<Path> i = s.iterator(); i.hasNext(); ) {
            copy.append(i.next().getAbsolute());
            if(i.hasNext()) {
                copy.append("\n");
            }
        }
        if(!clipboard.setStringForType(copy.toString(), NSPasteboard.StringPboardType)) {
            log.error("Error writing to NSPasteboard.StringPboardType.");
        }
    }

    @Action
    public void cut(final ID sender) {
        pasteboard.clear();
        pasteboard.setCut(true);
        for(Path s : this.getSelectedPaths()) {
            // Writing data for private use when the item gets dragged to the transfer queue.
            pasteboard.add(s);
        }
        final NSPasteboard clipboard = NSPasteboard.generalPasteboard();
        clipboard.declareTypes(NSArray.arrayWithObject(NSString.stringWithString(NSPasteboard.StringPboardType)), null);
        if(!clipboard.setStringForType(this.getSelectedPath().getAbsolute(), NSPasteboard.StringPboardType)) {
            log.error("Error writing to NSPasteboard.StringPboardType.");
        }
    }

    @Action
    public void paste(final ID sender) {
        if(pasteboard.isEmpty()) {
            NSPasteboard pboard = NSPasteboard.generalPasteboard();
            this.upload(pboard);
        }
        else {
            final Map<Path, Path> files = new HashMap<Path, Path>();
            final Path parent = this.workdir();
            for(final Path next : pasteboard) {
                Path renamed = new Path(parent, next.getName(), next.getType());
                files.put(next, renamed);
            }
            pasteboard.clear();
            if(pasteboard.isCut()) {
                this.renamePaths(files);
            }
            if(pasteboard.isCopy()) {
                this.duplicatePaths(files);
            }
        }
    }

    /**
     * @param pboard Pasteboard with filenames
     * @return True if filenames are found in pasteboard and upload has started
     */
    private boolean upload(NSPasteboard pboard) {
        if(!this.isMounted()) {
            return false;
        }
        if(pboard.availableTypeFromArray(NSArray.arrayWithObject(NSPasteboard.FilenamesPboardType)) != null) {
            NSObject o = pboard.propertyListForType(NSPasteboard.FilenamesPboardType);
            if(o != null) {
                if(o.isKindOfClass(Rococoa.createClass("NSArray", NSArray._Class.class))) {
                    final NSArray elements = Rococoa.cast(o, NSArray.class);
                    final Path workdir = this.workdir();
                    final List<TransferItem> uploads = new ArrayList<TransferItem>();
                    for(int i = 0; i < elements.count().intValue(); i++) {
                        final Local local = LocalFactory.createLocal(elements.objectAtIndex(new NSUInteger(i)).toString());
                        uploads.add(new TransferItem(new Path(workdir, local.getName(),
                                local.isDirectory() ? EnumSet.of(Path.Type.directory) : EnumSet.of(Path.Type.file)), local));
                    }
                    this.transfer(new UploadTransfer(session.getHost(), uploads));
                }
            }
        }
        return false;
    }

    @Action
    public void openTerminalButtonClicked(final ID sender) {
        Path workdir = null;
        if(this.getSelectionCount() == 1) {
            Path selected = this.getSelectedPath();
            if(selected.isDirectory()) {
                workdir = selected;
            }
        }
        if(null == workdir) {
            workdir = this.workdir();
        }
        try {
            TerminalServiceFactory.get().open(session.getHost(), workdir);
        }
        catch(AccessDeniedException e) {
            this.alert(session.getHost(), e, new StringBuilder());
        }
    }

    @Action
    public void archiveMenuClicked(final NSMenuItem sender) {
        final Archive archive = Archive.forName(sender.representedObject());
        this.archiveClicked(archive);
    }

    @Action
    public void archiveButtonClicked(final NSToolbarItem sender) {
        this.archiveClicked(Archive.TARGZ);
    }

    /**
     * @param archive Archive format
     */
    private void archiveClicked(final Archive archive) {
        final List<Path> changed = this.getSelectedPaths();
        this.checkOverwrite(Collections.singletonList(archive.getArchive(changed)), new DefaultMainAction() {
            @Override
            public void run() {
                background(new BrowserControllerBackgroundAction(BrowserController.this) {
                    @Override
                    public Boolean run() throws BackgroundException {
                        final Compress feature = session.getFeature(Compress.class);
                        feature.archive(archive, workdir, changed, this);
                        return true;
                    }

                    @Override
                    public void cleanup() {
                        super.cleanup();
                        // Update Selection
                        reload(changed, Collections.singletonList(archive.getArchive(changed)));
                    }

                    @Override
                    public String getActivity() {
                        return archive.getCompressCommand(workdir, changed);
                    }
                });
            }
        });
    }

    @Action
    public void unarchiveButtonClicked(final ID sender) {
        final List<Path> expanded = new ArrayList<Path>();
        final List<Path> selected = this.getSelectedPaths();
        for(final Path s : selected) {
            final Archive archive = Archive.forName(s.getName());
            if(null == archive) {
                continue;
            }
            this.checkOverwrite(archive.getExpanded(Collections.singletonList(s)), new DefaultMainAction() {
                @Override
                public void run() {
                    background(new BrowserControllerBackgroundAction(BrowserController.this) {
                        @Override
                        public Boolean run() throws BackgroundException {
                            final Compress feature = session.getFeature(Compress.class);
                            feature.unarchive(archive, s, this);
                            return true;
                        }

                        @Override
                        public void cleanup() {
                            super.cleanup();
                            expanded.addAll(archive.getExpanded(Collections.singletonList(s)));
                            // Update Selection
                            reload(selected, expanded);
                        }

                        @Override
                        public String getActivity() {
                            return archive.getDecompressCommand(s);
                        }
                    });
                }
            });
        }
    }

    /**
     * Accessor to the working directory
     *
     * @return The current working directory or null if no file system is mounted
     */
    protected Path workdir() {
        return workdir;
    }

    public void setWorkdir(final Path directory) {
        this.setWorkdir(directory, Collections.<Path>emptyList());
    }

    public void setWorkdir(final Path directory, Path selected) {
        this.setWorkdir(directory, Collections.singletonList(selected));
    }

    /**
     * Sets the current working directory. This will update the path selection menu and also add this path to the browsing history.
     *
     * @param directory The new working directory to display or null to detach any working directory from the browser
     * @param selected  Selected files in browser
     */
    public void setWorkdir(final Path directory, final List<Path> selected) {
        if(log.isDebugEnabled()) {
            log.debug(String.format("Set working directory to %s", directory));
        }
        final NSTableView browser = this.getSelectedBrowserView();
        window.endEditingFor(browser);
        // Update the working directory if listing is successful
        workdir = directory;
        // Change to last selected browser view
        this.reload(workdir != null ? selected : Collections.<Path>emptyList());
        this.setNavigation(this.isMounted());
    }

    private void setNavigation(boolean enabled) {
        if(!enabled) {
            searchField.setStringValue(StringUtils.EMPTY);
        }
        pathPopupButton.removeAllItems();
        if(enabled) {
            // Update the current working directory
            navigation.add(workdir);
            Path p = workdir;
            do {
                this.addNavigation(p);
                p = p.getParent();
            }
            while(!p.isRoot());
            this.addNavigation(p);
        }
        pathPopupButton.setEnabled(enabled);
        navigationButton.setEnabled_forSegment(enabled && navigation.getBack().size() > 1, NAVIGATION_LEFT_SEGMENT_BUTTON);
        navigationButton.setEnabled_forSegment(enabled && navigation.getForward().size() > 0, NAVIGATION_RIGHT_SEGMENT_BUTTON);
        upButton.setEnabled_forSegment(enabled && !workdir.isRoot(), NAVIGATION_UP_SEGMENT_BUTTON);
    }

    private void addNavigation(final Path p) {
        pathPopupButton.addItemWithTitle(p.getAbsolute());
        pathPopupButton.lastItem().setRepresentedObject(p.getAbsolute());
        if(p.isVolume()) {
            pathPopupButton.lastItem().setImage(IconCacheFactory.<NSImage>get().volumeIcon(session.getHost().getProtocol(), 16));
        }
        else {
            pathPopupButton.lastItem().setImage(IconCacheFactory.<NSImage>get().fileIcon(p, 16));
        }
    }

    /**
     * Initializes a session for the passed host. Setting up the listeners and adding any callback
     * controllers needed for login, trust management and hostkey verification.
     *
     * @param host Bookmark
     * @return A session object bound to this browser controller
     */
    private Session init(final Host host) {
        session = SessionFactory.create(host);
        transcript.clear();
        navigation.clear();
        pasteboard = PathPasteboardFactory.getPasteboard(session);
        this.setWorkdir(null);
        this.setEncoding(session.getEncoding());
        return session;
    }

    /**
     * Open connection in browser
     *
     * @param host Bookmark
     */
    public void mount(final Host host) {
        if(log.isDebugEnabled()) {
            log.debug(String.format("Mount session for %s", host));
        }
        this.unmount(new Runnable() {
            @Override
            public void run() {
                // The browser has no session, we are allowed to proceed
                // Initialize the browser with the new session attaching all listeners
                final Session session = init(host);
                background(new WorkerBackgroundAction(BrowserController.this, session, cache,
                        new MountWorker(session, cache, new PromptLimitedListProgressListener(BrowserController.this)) {
                            @Override
                            public void cleanup(final Path workdir) {
                                if(null == workdir) {
                                    unmount();
                                }
                                else {
                                    // Update status icon
                                    bookmarkTable.setNeedsDisplay();
                                    // Set the working directory
                                    setWorkdir(workdir);
                                    // Close bookmarks
                                    selectBrowser(preferences.getInteger("browser.view"));
                                    // Set the window title
                                    window.setRepresentedFilename(HistoryCollection.defaultCollection().getFile(host).getAbsolute());
                                    if(preferences.getBoolean("browser.disconnect.confirm")) {
                                        window.setDocumentEdited(true);
                                    }
                                    securityLabel.setImage(session.isSecured() ? IconCacheFactory.<NSImage>get().iconNamed("locked.tiff")
                                            : IconCacheFactory.<NSImage>get().iconNamed("unlocked.tiff"));
                                    securityLabel.setEnabled(session instanceof SSLSession);
                                }
                            }
                        }
                ) {
                    @Override
                    public void init() {
                        super.init();
                        window.setTitle(BookmarkNameProvider.toString(host, true));
                        window.setRepresentedFilename(StringUtils.EMPTY);
                        // Update status icon
                        bookmarkTable.setNeedsDisplay();
                    }
                });
            }
        });
    }

    /**
     * Close connection
     *
     * @return True if succeeded
     */
    public boolean unmount() {
        return this.unmount(new Runnable() {
            @Override
            public void run() {
                //
            }
        });
    }

    /**
     * @param disconnected Callback after the session has been disconnected
     * @return True if the unmount process has finished, false if the user has to agree first
     * to close the connection
     */
    public boolean unmount(final Runnable disconnected) {
        return this.unmount(new SheetCallback() {
            @Override
            public void callback(int returncode) {
                if(returncode == DEFAULT_OPTION) {
                    unmountImpl(disconnected);
                }
            }
        }, disconnected);
    }

    /**
     * @param callback     Confirmation callback
     * @param disconnected Action to run after disconnected
     * @return True if succeeded
     */
    public boolean unmount(final SheetCallback callback, final Runnable disconnected) {
        if(log.isDebugEnabled()) {
            log.debug(String.format("Unmount session %s", session));
        }
        if(this.isConnected() || this.isActivityRunning()) {
            if(preferences.getBoolean("browser.disconnect.confirm")) {
                // Defer the unmount to the callback function
                final NSAlert alert = NSAlert.alert(
                        MessageFormat.format(LocaleFactory.localizedString("Disconnect from {0}"), this.session.getHost().getHostname()), //title
                        LocaleFactory.localizedString("The connection will be closed."), // message
                        LocaleFactory.localizedString("Disconnect"), // defaultbutton
                        LocaleFactory.localizedString("Cancel"), // alternate button
                        null //other button
                );
                alert.setShowsSuppressionButton(true);
                alert.suppressionButton().setTitle(LocaleFactory.localizedString("Don't ask again", "Configuration"));
                this.alert(alert, new SheetCallback() {
                    @Override
                    public void callback(int returncode) {
                        if(alert.suppressionButton().state() == NSCell.NSOnState) {
                            // Never show again.
                            preferences.setProperty("browser.disconnect.confirm", false);
                        }
                        callback.callback(returncode);
                    }
                });
                // No unmount yet
                return false;
            }
        }
        this.unmountImpl(disconnected);
        // Unmount succeeded
        return true;
    }

    /**
     * @param disconnected Action to run after disconnected
     */
    private void unmountImpl(final Runnable disconnected) {
        this.disconnect(new Runnable() {
            @Override
            public void run() {
                if(session != null) {
                    // Clear the cache on the main thread to make sure the browser model is not in an invalid state
                    cache.clear();
                    PathPasteboardFactory.delete(session);
                }
                session = null;
                window.setTitle(preferences.getProperty("application.name"));
                window.setRepresentedFilename(StringUtils.EMPTY);
                disconnected.run();
            }
        });
    }

    /**
     * Unmount this session
     */
    private void disconnect(final Runnable disconnected) {
        final InfoController c = InfoControllerFactory.get(BrowserController.this);
        if(null != c) {
            c.window().close();
        }
        if(session != null) {
            this.background(new WorkerBackgroundAction<Void>(this, session, cache, new DisconnectWorker(session)) {
                @Override
                public void prepare() throws ConnectionCanceledException {
                    if(!session.isConnected()) {
                        throw new ConnectionCanceledException();
                    }
                    super.prepare();
                }

                @Override
                protected boolean connect(Session session) throws BackgroundException {
                    return false;
                }

                @Override
                public void cleanup() {
                    super.cleanup();
                    window.setDocumentEdited(false);
                    disconnected.run();
                }
            });
        }
        else {
            disconnected.run();
        }
    }

    @Action
    public void printDocument(final ID sender) {
        this.print(this.getSelectedBrowserView());
    }

    /**
     * @param app Singleton
     * @return NSApplication.TerminateLater if the application should not yet be terminated
     */
    public static NSUInteger applicationShouldTerminate(final NSApplication app) {
        // Determine if there are any open connections
        for(final BrowserController controller : MainController.getBrowsers()) {
            if(!controller.unmount(new SheetCallback() {
                                       @Override
                                       public void callback(final int returncode) {
                                           if(returncode == DEFAULT_OPTION) { //Disconnect
                                               controller.window().close();
                                               if(NSApplication.NSTerminateNow.equals(BrowserController.applicationShouldTerminate(app))) {
                                                   app.replyToApplicationShouldTerminate(true);
                                               }
                                           }
                                           else {
                                               app.replyToApplicationShouldTerminate(false);
                                           }

                                       }
                                   }, new Runnable() {
                                       @Override
                                       public void run() {
                                           //
                                       }
                                   }
            )) {
                return NSApplication.NSTerminateCancel;
            }
        }
        return NSApplication.NSTerminateNow;
    }

    @Override
    public boolean windowShouldClose(final NSWindow sender) {
        return this.unmount(new Runnable() {
            @Override
            public void run() {
                sender.close();
            }
        });
    }

    /**
     * @param item Menu item
     * @return True if the menu should be enabled
     */
    public boolean validateMenuItem(NSMenuItem item) {
        final Selector action = item.action();
        if(action.equals(Foundation.selector("paste:"))) {
            final String title = "Paste {0}";
            item.setTitle(MessageFormat.format(LocaleFactory.localizedString(title), StringUtils.EMPTY).trim());
            if(this.isMounted()) {
                if(pasteboard.isEmpty()) {
                    if(NSPasteboard.generalPasteboard().availableTypeFromArray(NSArray.arrayWithObject(NSPasteboard.FilenamesPboardType)) != null) {
                        NSObject o = NSPasteboard.generalPasteboard().propertyListForType(NSPasteboard.FilenamesPboardType);
                        if(o != null) {
                            if(o.isKindOfClass(Rococoa.createClass("NSArray", NSArray._Class.class))) {
                                final NSArray elements = Rococoa.cast(o, NSArray.class);
                                if(elements.count().intValue() == 1) {
                                    item.setTitle(MessageFormat.format(LocaleFactory.localizedString(title),
                                            "\"" + elements.objectAtIndex(new NSUInteger(0)) + "\"").trim());
                                }
                                else {
                                    item.setTitle(MessageFormat.format(LocaleFactory.localizedString(title),
                                            MessageFormat.format(LocaleFactory.localizedString("{0} Files"),
                                                    String.valueOf(elements.count().intValue()))
                                    ).trim());
                                }
                            }
                        }
                    }
                }
                else {
                    if(pasteboard.size() == 1) {
                        item.setTitle(MessageFormat.format(LocaleFactory.localizedString(title),
                                "\"" + pasteboard.get(0).getName() + "\"").trim());
                    }
                    else {
                        item.setTitle(MessageFormat.format(LocaleFactory.localizedString(title),
                                MessageFormat.format(LocaleFactory.localizedString("{0} Files"), String.valueOf(pasteboard.size()))).trim());
                    }
                }
            }
        }
        else if(action.equals(Foundation.selector("cut:")) || action.equals(Foundation.selector("copy:"))) {
            String title = null;
            if(action.equals(Foundation.selector("cut:"))) {
                title = "Cut {0}";
            }
            else if(action.equals(Foundation.selector("copy:"))) {
                title = "Copy {0}";
            }
            if(this.isMounted()) {
                int count = this.getSelectionCount();
                if(0 == count) {
                    item.setTitle(MessageFormat.format(LocaleFactory.localizedString(title), StringUtils.EMPTY).trim());
                }
                else if(1 == count) {
                    item.setTitle(MessageFormat.format(LocaleFactory.localizedString(title),
                            "\"" + this.getSelectedPath().getName() + "\"").trim());
                }
                else {
                    item.setTitle(MessageFormat.format(LocaleFactory.localizedString(title),
                            MessageFormat.format(LocaleFactory.localizedString("{0} Files"), String.valueOf(this.getSelectionCount()))).trim());
                }
            }
            else {
                item.setTitle(MessageFormat.format(LocaleFactory.localizedString(title), StringUtils.EMPTY).trim());
            }
        }
        else if(action.equals(Foundation.selector("showHiddenFilesClicked:"))) {
            item.setState(this.getFilter() instanceof NullPathFilter ? NSCell.NSOnState : NSCell.NSOffState);
        }
        else if(action.equals(Foundation.selector("encodingMenuClicked:"))) {
            if(this.isMounted()) {
                item.setState(this.session.getEncoding().equalsIgnoreCase(
                        item.title()) ? NSCell.NSOnState : NSCell.NSOffState);
            }
            else {
                item.setState(preferences.getProperty("browser.charset.encoding").equalsIgnoreCase(
                        item.title()) ? NSCell.NSOnState : NSCell.NSOffState);
            }
        }
        else if(action.equals(Foundation.selector("browserSwitchMenuClicked:"))) {
            if(item.tag() == preferences.getInteger("browser.view")) {
                item.setState(NSCell.NSOnState);
            }
            else {
                item.setState(NSCell.NSOffState);
            }
        }
        else if(action.equals(Foundation.selector("archiveMenuClicked:"))) {
            final Archive archive = Archive.forName(item.representedObject());
            item.setTitle(archive.getTitle(this.getSelectedPaths()));
        }
        else if(action.equals(Foundation.selector("quicklookButtonClicked:"))) {
            item.setKeyEquivalent(" ");
            item.setKeyEquivalentModifierMask(0);
        }
        return this.validateItem(action);
    }

    /**
     * @return Browser tab active
     */
    private boolean isBrowser() {
        return this.getSelectedTabView() == TAB_LIST_VIEW
                || this.getSelectedTabView() == TAB_OUTLINE_VIEW;
    }

    /**
     * @return Bookmarks tab active
     */
    private boolean isBookmarks() {
        return this.getSelectedTabView() == TAB_BOOKMARKS;
    }

    /**
     * @param action the method selector
     * @return true if the item by that identifier should be enabled
     */
    private boolean validateItem(final Selector action) {
        if(action.equals(Foundation.selector("cut:"))) {
            return this.isBrowser() && this.isMounted() && this.getSelectionCount() > 0;
        }
        else if(action.equals(Foundation.selector("copy:"))) {
            return this.isBrowser() && this.isMounted() && this.getSelectionCount() > 0;
        }
        else if(action.equals(Foundation.selector("paste:"))) {
            if(this.isBrowser() && this.isMounted()) {
                if(pasteboard.isEmpty()) {
                    NSPasteboard pboard = NSPasteboard.generalPasteboard();
                    if(pboard.availableTypeFromArray(NSArray.arrayWithObject(NSPasteboard.FilenamesPboardType)) != null) {
                        Object o = pboard.propertyListForType(NSPasteboard.FilenamesPboardType);
                        if(o != null) {
                            return true;
                        }
                    }
                    return false;
                }
                return true;
            }
            return false;
        }
        else if(action.equals(Foundation.selector("encodingMenuClicked:"))) {
            return this.isBrowser() && !this.isActivityRunning();
        }
        else if(action.equals(Foundation.selector("connectBookmarkButtonClicked:"))) {
            if(this.isBookmarks()) {
                return bookmarkTable.numberOfSelectedRows().intValue() == 1;
            }
            return false;
        }
        else if(action.equals(Foundation.selector("addBookmarkButtonClicked:"))) {
            if(this.isBookmarks()) {
                return bookmarkModel.getSource().allowsAdd();
            }
            return true;
        }
        else if(action.equals(Foundation.selector("deleteBookmarkButtonClicked:"))) {
            if(this.isBookmarks()) {
                return bookmarkModel.getSource().allowsDelete() && bookmarkTable.selectedRow().intValue() != -1;
            }
            return false;
        }
        else if(action.equals(Foundation.selector("duplicateBookmarkButtonClicked:"))) {
            if(this.isBookmarks()) {
                return bookmarkModel.getSource().allowsEdit() && bookmarkTable.numberOfSelectedRows().intValue() == 1;
            }
            return false;
        }
        else if(action.equals(Foundation.selector("editBookmarkButtonClicked:"))) {
            if(this.isBookmarks()) {
                return bookmarkModel.getSource().allowsEdit() && bookmarkTable.numberOfSelectedRows().intValue() == 1;
            }
            return false;
        }
        else if(action.equals(Foundation.selector("editButtonClicked:"))) {
            if(this.isBrowser() && this.isMounted() && this.getSelectionCount() > 0) {
                for(Path s : this.getSelectedPaths()) {
                    if(!this.isEditable(s)) {
                        return false;
                    }
                    // Choose editor for selected file
                    if(null == EditorFactory.instance().getEditor(s.getName())) {
                        return false;
                    }
                }
                return true;
            }
            return false;
        }
        else if(action.equals(Foundation.selector("editMenuClicked:"))) {
            if(this.isBrowser() && this.isMounted() && this.getSelectionCount() > 0) {
                for(Path s : this.getSelectedPaths()) {
                    if(!this.isEditable(s)) {
                        return false;
                    }
                }
                return true;
            }
            return false;
        }
        else if(action.equals(Foundation.selector("searchButtonClicked:"))) {
            return this.isMounted() || this.isBookmarks();
        }
        else if(action.equals(Foundation.selector("quicklookButtonClicked:"))) {
            return this.isBrowser() && this.isMounted() && quicklook.isAvailable() && this.getSelectionCount() > 0;
        }
        else if(action.equals(Foundation.selector("openBrowserButtonClicked:"))) {
            return this.isMounted();
        }
        else if(action.equals(Foundation.selector("sendCustomCommandClicked:"))) {
            return this.isBrowser() && this.isMounted() && session.getFeature(Command.class) != null;
        }
        else if(action.equals(Foundation.selector("gotoButtonClicked:"))) {
            return this.isBrowser() && this.isMounted();
        }
        else if(action.equals(Foundation.selector("infoButtonClicked:"))) {
            return this.isBrowser() && this.isMounted() && this.getSelectionCount() > 0;
        }
        else if(action.equals(Foundation.selector("createFolderButtonClicked:"))) {
            return this.isBrowser() && this.isMounted();
        }
        else if(action.equals(Foundation.selector("createFileButtonClicked:"))) {
            return this.isBrowser() && this.isMounted() && session.getFeature(Touch.class).isSupported(
                    new UploadTargetFinder(workdir).find(this.getSelectedPath())
            );
        }
        else if(action.equals(Foundation.selector("createSymlinkButtonClicked:"))) {
            return this.isBrowser() && this.isMounted() && session.getFeature(Symlink.class) != null
                    && this.getSelectionCount() == 1;
        }
        else if(action.equals(Foundation.selector("duplicateFileButtonClicked:"))) {
            return this.isBrowser() && this.isMounted() && this.getSelectionCount() == 1;
        }
        else if(action.equals(Foundation.selector("renameFileButtonClicked:"))) {
            if(this.isBrowser() && this.isMounted() && this.getSelectionCount() == 1) {
                final Path selected = this.getSelectedPath();
                if(null == selected) {
                    return false;
                }
                return session.getFeature(Move.class).isSupported(selected);
            }
            return false;
        }
        else if(action.equals(Foundation.selector("deleteFileButtonClicked:"))) {
            return this.isBrowser() && this.isMounted() && this.getSelectionCount() > 0;
        }
        else if(action.equals(Foundation.selector("revertFileButtonClicked:"))) {
            if(this.isBrowser() && this.isMounted() && this.getSelectionCount() == 1) {
                return session.getFeature(Versioning.class) != null;
            }
            return false;
        }
        else if(action.equals(Foundation.selector("reloadButtonClicked:"))) {
            return this.isBrowser() && this.isMounted();
        }
        else if(action.equals(Foundation.selector("newBrowserButtonClicked:"))) {
            return this.isMounted();
        }
        else if(action.equals(Foundation.selector("uploadButtonClicked:"))) {
            return this.isBrowser() && this.isMounted() && session.getFeature(Touch.class).isSupported(
                    new UploadTargetFinder(workdir).find(this.getSelectedPath())
            );
        }
        else if(action.equals(Foundation.selector("syncButtonClicked:"))) {
            return this.isBrowser() && this.isMounted();
        }
        else if(action.equals(Foundation.selector("downloadAsButtonClicked:"))) {
            return this.isBrowser() && this.isMounted() && this.getSelectionCount() == 1;
        }
        else if(action.equals(Foundation.selector("downloadToButtonClicked:")) || action.equals(Foundation.selector("downloadButtonClicked:"))) {
            return this.isBrowser() && this.isMounted() && this.getSelectionCount() > 0;
        }
        else if(action.equals(Foundation.selector("insideButtonClicked:"))) {
            return this.isBrowser() && this.isMounted() && this.getSelectionCount() > 0;
        }
        else if(action.equals(Foundation.selector("upButtonClicked:"))) {
            return this.isBrowser() && this.isMounted() && !this.workdir().isRoot();
        }
        else if(action.equals(Foundation.selector("backButtonClicked:"))) {
            return this.isBrowser() && this.isMounted() && navigation.getBack().size() > 1;
        }
        else if(action.equals(Foundation.selector("forwardButtonClicked:"))) {
            return this.isBrowser() && this.isMounted() && navigation.getForward().size() > 0;
        }
        else if(action.equals(Foundation.selector("printDocument:"))) {
            return this.isBrowser() && this.isMounted();
        }
        else if(action.equals(Foundation.selector("disconnectButtonClicked:"))) {
            if(this.isBrowser()) {
                if(!this.isConnected()) {
                    return this.isActivityRunning();
                }
                return this.isConnected();
            }
        }
        else if(action.equals(Foundation.selector("gotofolderButtonClicked:"))) {
            return this.isBrowser() && this.isMounted();
        }
        else if(action.equals(Foundation.selector("openTerminalButtonClicked:"))) {
            return this.isBrowser() && this.isMounted()
                    && session instanceof SFTPSession && TerminalServiceFactory.get() != null;
        }
        else if(action.equals(Foundation.selector("archiveButtonClicked:")) || action.equals(Foundation.selector("archiveMenuClicked:"))) {
            if(this.isBrowser() && this.isMounted()) {
                if(session.getFeature(Compress.class) == null) {
                    return false;
                }
                if(this.getSelectionCount() > 0) {
                    for(Path s : this.getSelectedPaths()) {
                        if(s.isFile() && Archive.isArchive(s.getName())) {
                            // At least one file selected is already an archive. No distinct action possible
                            return false;
                        }
                    }
                    return true;
                }
            }
            return false;
        }
        else if(action.equals(Foundation.selector("unarchiveButtonClicked:"))) {
            if(this.isBrowser() && this.isMounted()) {
                if(session.getFeature(Compress.class) == null) {
                    return false;
                }
                if(this.getSelectionCount() > 0) {
                    for(Path s : this.getSelectedPaths()) {
                        if(s.isDirectory()) {
                            return false;
                        }
                        if(!Archive.isArchive(s.getName())) {
                            return false;
                        }
                    }
                    return true;
                }
            }
            return false;
        }
        return true; // by default everything is enabled
    }

    private static final String TOOLBAR_NEW_CONNECTION = "New Connection";
    private static final String TOOLBAR_BROWSER_VIEW = "Browser View";
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
    private static final String TOOLBAR_NEW_BOOKMARK = "New Bookmark";
    private static final String TOOLBAR_GET_INFO = "Get Info";
    private static final String TOOLBAR_WEBVIEW = "Open";
    private static final String TOOLBAR_DISCONNECT = "Disconnect";
    private static final String TOOLBAR_TERMINAL = "Terminal";
    private static final String TOOLBAR_ARCHIVE = "Archive";
    private static final String TOOLBAR_QUICKLOOK = "Quick Look";
    private static final String TOOLBAR_LOG = "Log";

    @Override
    public boolean validateToolbarItem(final NSToolbarItem item) {
        final String identifier = item.itemIdentifier();
        if(identifier.equals(TOOLBAR_EDIT)) {
            Application editor = null;
            final Path selected = this.getSelectedPath();
            if(null != selected) {
                if(this.isEditable(selected)) {
                    // Choose editor for selected file
                    editor = EditorFactory.instance().getEditor(selected.getName());
                }
            }
            if(null == editor) {
                // No editor found
                item.setImage(IconCacheFactory.<NSImage>get().iconNamed("pencil.tiff", 32));
            }
            else {
                item.setImage(IconCacheFactory.<NSImage>get().applicationIcon(editor, 32));
            }
        }
        else if(identifier.equals(TOOLBAR_DISCONNECT)) {
            if(this.isActivityRunning()) {
                item.setLabel(LocaleFactory.localizedString("Stop"));
                item.setPaletteLabel(LocaleFactory.localizedString("Stop"));
                item.setToolTip(LocaleFactory.localizedString("Cancel current operation in progress"));
                item.setImage(IconCacheFactory.<NSImage>get().iconNamed("stop", 32));
            }
            else {
                item.setLabel(LocaleFactory.localizedString(TOOLBAR_DISCONNECT));
                item.setPaletteLabel(LocaleFactory.localizedString(TOOLBAR_DISCONNECT));
                item.setToolTip(LocaleFactory.localizedString("Disconnect from server"));
                item.setImage(IconCacheFactory.<NSImage>get().iconNamed("eject.tiff", 32));
            }
        }
        else if(identifier.equals(TOOLBAR_ARCHIVE)) {
            final Path selected = getSelectedPath();
            if(null != selected) {
                if(Archive.isArchive(selected.getName())) {
                    item.setLabel(LocaleFactory.localizedString("Unarchive", "Archive"));
                    item.setPaletteLabel(LocaleFactory.localizedString("Unarchive"));
                    item.setAction(Foundation.selector("unarchiveButtonClicked:"));
                }
                else {
                    item.setLabel(LocaleFactory.localizedString("Archive", "Archive"));
                    item.setPaletteLabel(LocaleFactory.localizedString("Archive"));
                    item.setAction(Foundation.selector("archiveButtonClicked:"));
                }
            }
        }
        return validateItem(item.action());
    }

    /**
     * Keep reference to weak toolbar items. A toolbar may ask again for a kind of toolbar
     * item already supplied to it, in which case this method may return the same toolbar
     * item it returned before
     */
    private Map<String, NSToolbarItem> toolbarItems
            = new HashMap<String, NSToolbarItem>();

    @Override
    public NSToolbarItem toolbar_itemForItemIdentifier_willBeInsertedIntoToolbar(NSToolbar toolbar, final String itemIdentifier, boolean inserted) {
        if(log.isDebugEnabled()) {
            log.debug("toolbar_itemForItemIdentifier_willBeInsertedIntoToolbar:" + itemIdentifier);
        }
        if(!toolbarItems.containsKey(itemIdentifier)) {
            toolbarItems.put(itemIdentifier, NSToolbarItem.itemWithIdentifier(itemIdentifier));
        }
        final NSToolbarItem item = toolbarItems.get(itemIdentifier);
        if(itemIdentifier.equals(TOOLBAR_BROWSER_VIEW)) {
            item.setLabel(LocaleFactory.localizedString("View"));
            item.setPaletteLabel(LocaleFactory.localizedString("View"));
            item.setToolTip(LocaleFactory.localizedString("Switch Browser View"));
            item.setView(browserSwitchView);
            // Add a menu representation for text mode of toolbar
            NSMenuItem viewMenu = NSMenuItem.itemWithTitle(LocaleFactory.localizedString("View"), null, StringUtils.EMPTY);
            NSMenu viewSubmenu = NSMenu.menu();
            viewSubmenu.addItemWithTitle_action_keyEquivalent(LocaleFactory.localizedString("List"),
                    Foundation.selector("browserSwitchMenuClicked:"), StringUtils.EMPTY);
            viewSubmenu.itemWithTitle(LocaleFactory.localizedString("List")).setTag(0);
            viewSubmenu.addItemWithTitle_action_keyEquivalent(LocaleFactory.localizedString("Outline"),
                    Foundation.selector("browserSwitchMenuClicked:"), StringUtils.EMPTY);
            viewSubmenu.itemWithTitle(LocaleFactory.localizedString("Outline")).setTag(1);
            viewMenu.setSubmenu(viewSubmenu);
            item.setMenuFormRepresentation(viewMenu);
            return item;
        }
        else if(itemIdentifier.equals(TOOLBAR_NEW_CONNECTION)) {
            item.setLabel(LocaleFactory.localizedString(TOOLBAR_NEW_CONNECTION));
            item.setPaletteLabel(LocaleFactory.localizedString(TOOLBAR_NEW_CONNECTION));
            item.setToolTip(LocaleFactory.localizedString("Connect to server"));
            item.setImage(IconCacheFactory.<NSImage>get().iconNamed("connect.tiff", 32));
            item.setTarget(this.id());
            item.setAction(Foundation.selector("connectButtonClicked:"));
            return item;
        }
        else if(itemIdentifier.equals(TOOLBAR_TRANSFERS)) {
            item.setLabel(LocaleFactory.localizedString(TOOLBAR_TRANSFERS));
            item.setPaletteLabel(LocaleFactory.localizedString(TOOLBAR_TRANSFERS));
            item.setToolTip(LocaleFactory.localizedString("Show Transfers window"));
            item.setImage(IconCacheFactory.<NSImage>get().iconNamed("queue.tiff", 32));
            item.setAction(Foundation.selector("showTransferQueueClicked:"));
            return item;
        }
        else if(itemIdentifier.equals(TOOLBAR_TOOLS)) {
            item.setLabel(LocaleFactory.localizedString("Action"));
            item.setPaletteLabel(LocaleFactory.localizedString("Action"));
            if(inserted || !Factory.VERSION_PLATFORM.matches("10\\.5.*")) {
                item.setView(actionPopupButton);
                // Add a menu representation for text mode of toolbar
                NSMenuItem toolMenu = NSMenuItem.itemWithTitle(LocaleFactory.localizedString("Action"), null, StringUtils.EMPTY);
                NSMenu toolSubmenu = NSMenu.menu();
                for(int i = 1; i < actionPopupButton.menu().numberOfItems().intValue(); i++) {
                    NSMenuItem template = actionPopupButton.menu().itemAtIndex(new NSInteger(i));
                    toolSubmenu.addItem(NSMenuItem.itemWithTitle(template.title(),
                            template.action(),
                            template.keyEquivalent()));
                }
                toolMenu.setSubmenu(toolSubmenu);
                item.setMenuFormRepresentation(toolMenu);
            }
            else {
                NSToolbarItem temporary = NSToolbarItem.itemWithIdentifier(itemIdentifier);
                temporary.setPaletteLabel(LocaleFactory.localizedString("Action"));
                temporary.setImage(IconCacheFactory.<NSImage>get().iconNamed("advanced.tiff", 32));
                return temporary;
            }
            return item;
        }
        else if(itemIdentifier.equals(TOOLBAR_QUICK_CONNECT)) {
            item.setLabel(LocaleFactory.localizedString(TOOLBAR_QUICK_CONNECT));
            item.setPaletteLabel(LocaleFactory.localizedString(TOOLBAR_QUICK_CONNECT));
            item.setToolTip(LocaleFactory.localizedString("Connect to server"));
            item.setView(quickConnectPopup);
            return item;
        }
        else if(itemIdentifier.equals(TOOLBAR_ENCODING)) {
            item.setLabel(LocaleFactory.localizedString(TOOLBAR_ENCODING));
            item.setPaletteLabel(LocaleFactory.localizedString(TOOLBAR_ENCODING));
            item.setToolTip(LocaleFactory.localizedString("Character Encoding"));
            item.setView(this.encodingPopup);
            // Add a menu representation for text mode of toolbar
            NSMenuItem encodingMenu = NSMenuItem.itemWithTitle(LocaleFactory.localizedString(TOOLBAR_ENCODING),
                    Foundation.selector("encodingMenuClicked:"), StringUtils.EMPTY);
            String[] charsets = MainController.availableCharsets();
            NSMenu charsetMenu = NSMenu.menu();
            for(String charset : charsets) {
                charsetMenu.addItemWithTitle_action_keyEquivalent(charset, Foundation.selector("encodingMenuClicked:"), StringUtils.EMPTY);
            }
            encodingMenu.setSubmenu(charsetMenu);
            item.setMenuFormRepresentation(encodingMenu);
            item.setMinSize(this.encodingPopup.frame().size);
            item.setMaxSize(this.encodingPopup.frame().size);
            return item;
        }
        else if(itemIdentifier.equals(TOOLBAR_REFRESH)) {
            item.setLabel(LocaleFactory.localizedString(TOOLBAR_REFRESH));
            item.setPaletteLabel(LocaleFactory.localizedString(TOOLBAR_REFRESH));
            item.setToolTip(LocaleFactory.localizedString("Refresh directory listing"));
            item.setImage(IconCacheFactory.<NSImage>get().iconNamed("reload.tiff", 32));
            item.setTarget(this.id());
            item.setAction(Foundation.selector("reloadButtonClicked:"));
            return item;
        }
        else if(itemIdentifier.equals(TOOLBAR_DOWNLOAD)) {
            item.setLabel(LocaleFactory.localizedString(TOOLBAR_DOWNLOAD));
            item.setPaletteLabel(LocaleFactory.localizedString(TOOLBAR_DOWNLOAD));
            item.setToolTip(LocaleFactory.localizedString("Download file"));
            item.setImage(IconCacheFactory.<NSImage>get().iconNamed("download.tiff", 32));
            item.setTarget(this.id());
            item.setAction(Foundation.selector("downloadButtonClicked:"));
            return item;
        }
        else if(itemIdentifier.equals(TOOLBAR_UPLOAD)) {
            item.setLabel(LocaleFactory.localizedString(TOOLBAR_UPLOAD));
            item.setPaletteLabel(LocaleFactory.localizedString(TOOLBAR_UPLOAD));
            item.setToolTip(LocaleFactory.localizedString("Upload local file to the remote host"));
            item.setImage(IconCacheFactory.<NSImage>get().iconNamed("upload.tiff", 32));
            item.setTarget(this.id());
            item.setAction(Foundation.selector("uploadButtonClicked:"));
            return item;
        }
        else if(itemIdentifier.equals(TOOLBAR_SYNCHRONIZE)) {
            item.setLabel(LocaleFactory.localizedString(TOOLBAR_SYNCHRONIZE));
            item.setPaletteLabel(LocaleFactory.localizedString(TOOLBAR_SYNCHRONIZE));
            item.setToolTip(LocaleFactory.localizedString("Synchronize files"));
            item.setImage(IconCacheFactory.<NSImage>get().iconNamed("sync.tiff", 32));
            item.setTarget(this.id());
            item.setAction(Foundation.selector("syncButtonClicked:"));
            return item;
        }
        else if(itemIdentifier.equals(TOOLBAR_GET_INFO)) {
            item.setLabel(LocaleFactory.localizedString(TOOLBAR_GET_INFO));
            item.setPaletteLabel(LocaleFactory.localizedString(TOOLBAR_GET_INFO));
            item.setToolTip(LocaleFactory.localizedString("Show file attributes"));
            item.setImage(IconCacheFactory.<NSImage>get().iconNamed("info.tiff", 32));
            item.setTarget(this.id());
            item.setAction(Foundation.selector("infoButtonClicked:"));
            return item;
        }
        else if(itemIdentifier.equals(TOOLBAR_WEBVIEW)) {
            item.setLabel(LocaleFactory.localizedString(TOOLBAR_WEBVIEW));
            item.setPaletteLabel(LocaleFactory.localizedString("Open in Web Browser"));
            item.setToolTip(LocaleFactory.localizedString("Open in Web Browser"));
            final Application browser = SchemeHandlerFactory.get().getDefaultHandler(Scheme.http);
            if(null == browser) {
                item.setEnabled(false);
                item.setImage(IconCacheFactory.<NSImage>get().iconNamed("notfound.tiff", 32));
            }
            else {
                item.setImage(IconCacheFactory.<NSImage>get().applicationIcon(browser, 32));
            }
            item.setTarget(this.id());
            item.setAction(Foundation.selector("openBrowserButtonClicked:"));
            return item;
        }
        else if(itemIdentifier.equals(TOOLBAR_EDIT)) {
            item.setLabel(LocaleFactory.localizedString(TOOLBAR_EDIT));
            item.setPaletteLabel(LocaleFactory.localizedString(TOOLBAR_EDIT));
            item.setToolTip(LocaleFactory.localizedString("Edit file in external editor"));
            item.setImage(IconCacheFactory.<NSImage>get().iconNamed("pencil.tiff"));
            item.setTarget(this.id());
            item.setAction(Foundation.selector("editButtonClicked:"));
            // Add a menu representation for text mode of toolbar
            NSMenuItem toolbarMenu = NSMenuItem.itemWithTitle(LocaleFactory.localizedString(TOOLBAR_EDIT),
                    Foundation.selector("editButtonClicked:"), StringUtils.EMPTY);
            NSMenu editMenu = NSMenu.menu();
            editMenu.setAutoenablesItems(true);
            editMenu.setDelegate(editMenuDelegate.id());
            toolbarMenu.setSubmenu(editMenu);
            item.setMenuFormRepresentation(toolbarMenu);
            return item;
        }
        else if(itemIdentifier.equals(TOOLBAR_DELETE)) {
            item.setLabel(LocaleFactory.localizedString(TOOLBAR_DELETE));
            item.setPaletteLabel(LocaleFactory.localizedString(TOOLBAR_DELETE));
            item.setToolTip(LocaleFactory.localizedString("Delete file"));
            item.setImage(IconCacheFactory.<NSImage>get().iconNamed("delete.tiff", 32));
            item.setTarget(this.id());
            item.setAction(Foundation.selector("deleteFileButtonClicked:"));
            return item;
        }
        else if(itemIdentifier.equals(TOOLBAR_NEW_FOLDER)) {
            item.setLabel(LocaleFactory.localizedString(TOOLBAR_NEW_FOLDER));
            item.setPaletteLabel(LocaleFactory.localizedString(TOOLBAR_NEW_FOLDER));
            item.setToolTip(LocaleFactory.localizedString("Create New Folder"));
            item.setImage(IconCacheFactory.<NSImage>get().iconNamed("newfolder.tiff", 32));
            item.setTarget(this.id());
            item.setAction(Foundation.selector("createFolderButtonClicked:"));
            return item;
        }
        else if(itemIdentifier.equals(TOOLBAR_NEW_BOOKMARK)) {
            item.setLabel(LocaleFactory.localizedString(TOOLBAR_NEW_BOOKMARK));
            item.setPaletteLabel(LocaleFactory.localizedString(TOOLBAR_NEW_BOOKMARK));
            item.setToolTip(LocaleFactory.localizedString("New Bookmark"));
            item.setImage(IconCacheFactory.<NSImage>get().iconNamed("bookmark", 32));
            item.setTarget(this.id());
            item.setAction(Foundation.selector("addBookmarkButtonClicked:"));
            return item;
        }
        else if(itemIdentifier.equals(TOOLBAR_DISCONNECT)) {
            item.setLabel(LocaleFactory.localizedString(TOOLBAR_DISCONNECT));
            item.setPaletteLabel(LocaleFactory.localizedString(TOOLBAR_DISCONNECT));
            item.setToolTip(LocaleFactory.localizedString("Disconnect from server"));
            item.setImage(IconCacheFactory.<NSImage>get().iconNamed("eject.tiff", 32));
            item.setTarget(this.id());
            item.setAction(Foundation.selector("disconnectButtonClicked:"));
            return item;
        }
        else if(itemIdentifier.equals(TOOLBAR_TERMINAL)) {
            final ApplicationFinder finder = ApplicationFinderFactory.get();
            final Application application
                    = finder.getDescription(preferences.getProperty("terminal.bundle.identifier"));

            item.setLabel(application.getName());
            item.setPaletteLabel(application.getName());
            item.setImage(IconCacheFactory.<NSImage>get().applicationIcon(application, 32));
            item.setTarget(this.id());
            item.setAction(Foundation.selector("openTerminalButtonClicked:"));
            return item;
        }
        else if(itemIdentifier.equals(TOOLBAR_ARCHIVE)) {
            item.setLabel(LocaleFactory.localizedString("Archive", "Archive"));
            item.setPaletteLabel(LocaleFactory.localizedString("Archive", "Archive"));
            item.setImage(IconCacheFactory.<NSImage>get().applicationIcon(new Application("com.apple.archiveutility"), 32));
            item.setTarget(this.id());
            item.setAction(Foundation.selector("archiveButtonClicked:"));
            return item;
        }
        else if(itemIdentifier.equals(TOOLBAR_QUICKLOOK)) {
            item.setLabel(LocaleFactory.localizedString(TOOLBAR_QUICKLOOK));
            item.setPaletteLabel(LocaleFactory.localizedString(TOOLBAR_QUICKLOOK));
            if(quicklook.isAvailable()) {
                quicklookButton = NSButton.buttonWithFrame(new NSRect(29, 23));
                quicklookButton.setBezelStyle(NSButtonCell.NSTexturedRoundedBezelStyle);
                quicklookButton.setImage(IconCacheFactory.<NSImage>get().iconNamed("NSQuickLookTemplate"));
                quicklookButton.sizeToFit();
                quicklookButton.setTarget(this.id());
                quicklookButton.setAction(Foundation.selector("quicklookButtonClicked:"));
                item.setView(quicklookButton);
            }
            else {
                item.setEnabled(false);
                item.setImage(IconCacheFactory.<NSImage>get().iconNamed("notfound.tiff", 32));
            }
            return item;
        }
        else if(itemIdentifier.equals(TOOLBAR_LOG)) {
            item.setLabel(LocaleFactory.localizedString(TOOLBAR_LOG));
            item.setPaletteLabel(LocaleFactory.localizedString(TOOLBAR_LOG));
            item.setToolTip(LocaleFactory.localizedString("Toggle Log Drawer"));
            item.setImage(IconCacheFactory.<NSImage>get().iconNamed("log", 32));
            item.setTarget(this.id());
            item.setAction(Foundation.selector("toggleLogDrawer:"));
            return item;
        }
        // Returning null will inform the toolbar this kind of item is not supported.
        return null;
    }

    @Outlet
    private NSButton quicklookButton;

    /**
     * @param toolbar Window toolbar
     * @return The default configuration of toolbar items
     */
    @Override
    public NSArray toolbarDefaultItemIdentifiers(NSToolbar toolbar) {
        return NSArray.arrayWithObjects(
                TOOLBAR_NEW_CONNECTION,
                NSToolbarItem.NSToolbarSeparatorItemIdentifier,
                TOOLBAR_QUICK_CONNECT,
                TOOLBAR_TOOLS,
                NSToolbarItem.NSToolbarSeparatorItemIdentifier,
                TOOLBAR_REFRESH,
                TOOLBAR_EDIT,
                NSToolbarItem.NSToolbarFlexibleSpaceItemIdentifier,
                TOOLBAR_DISCONNECT
        );
    }

    /**
     * @param toolbar Window toolbar
     * @return All available toolbar items
     */
    @Override
    public NSArray toolbarAllowedItemIdentifiers(NSToolbar toolbar) {
        return NSArray.arrayWithObjects(
                TOOLBAR_NEW_CONNECTION,
                TOOLBAR_BROWSER_VIEW,
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
                TOOLBAR_NEW_BOOKMARK,
                TOOLBAR_GET_INFO,
                TOOLBAR_WEBVIEW,
                TOOLBAR_TERMINAL,
                TOOLBAR_ARCHIVE,
                TOOLBAR_QUICKLOOK,
                TOOLBAR_LOG,
                TOOLBAR_DISCONNECT,
                NSToolbarItem.NSToolbarCustomizeToolbarItemIdentifier,
                NSToolbarItem.NSToolbarSpaceItemIdentifier,
                NSToolbarItem.NSToolbarSeparatorItemIdentifier,
                NSToolbarItem.NSToolbarFlexibleSpaceItemIdentifier
        );
    }

    @Override
    public NSArray toolbarSelectableItemIdentifiers(NSToolbar toolbar) {
        return NSArray.array();
    }

    /**
     * Overrriden to remove any listeners from the session
     */
    @Override
    protected void invalidate() {
        if(quicklook.isAvailable()) {
            if(quicklook.isOpen()) {
                quicklook.close();
            }
        }
        bookmarkTable.setDelegate(null);
        bookmarkTable.setDataSource(null);
        bookmarkModel.invalidate();

        browserListView.setDelegate(null);
        browserListView.setDataSource(null);
        browserListModel.invalidate();

        browserOutlineView.setDelegate(null);
        browserOutlineView.setDataSource(null);
        browserOutlineModel.invalidate();

        toolbar.setDelegate(null);
        toolbarItems.clear();

        browserListColumnsFactory.clear();
        browserOutlineColumnsFactory.clear();
        bookmarkTableColumnFactory.clear();

        quickConnectPopup.setDelegate(null);
        quickConnectPopup.setDataSource(null);

        archiveMenu.setDelegate(null);
        editMenu.setDelegate(null);

        super.invalidate();
    }
}
