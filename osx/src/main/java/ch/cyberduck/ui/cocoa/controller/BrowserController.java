package ch.cyberduck.ui.cocoa.controller;

/*
 * Copyright (c) 2002-2016 iterate GmbH. All rights reserved.
 * https://cyberduck.io/
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 */

import ch.cyberduck.binding.AbstractTableDelegate;
import ch.cyberduck.binding.Action;
import ch.cyberduck.binding.Delegate;
import ch.cyberduck.binding.DisabledSheetCallback;
import ch.cyberduck.binding.Outlet;
import ch.cyberduck.binding.ProxyController;
import ch.cyberduck.binding.SheetInvoker;
import ch.cyberduck.binding.WindowController;
import ch.cyberduck.binding.application.*;
import ch.cyberduck.binding.foundation.NSArray;
import ch.cyberduck.binding.foundation.NSAttributedString;
import ch.cyberduck.binding.foundation.NSDictionary;
import ch.cyberduck.binding.foundation.NSEnumerator;
import ch.cyberduck.binding.foundation.NSIndexSet;
import ch.cyberduck.binding.foundation.NSNotification;
import ch.cyberduck.binding.foundation.NSNotificationCenter;
import ch.cyberduck.binding.foundation.NSObject;
import ch.cyberduck.binding.foundation.NSRange;
import ch.cyberduck.binding.foundation.NSString;
import ch.cyberduck.core.*;
import ch.cyberduck.core.aquaticprime.LicenseFactory;
import ch.cyberduck.core.bonjour.RendezvousCollection;
import ch.cyberduck.core.editor.DefaultEditorListener;
import ch.cyberduck.core.editor.Editor;
import ch.cyberduck.core.editor.EditorFactory;
import ch.cyberduck.core.exception.AccessDeniedException;
import ch.cyberduck.core.features.Location;
import ch.cyberduck.core.features.Move;
import ch.cyberduck.core.features.Touch;
import ch.cyberduck.core.local.Application;
import ch.cyberduck.core.local.BrowserLauncherFactory;
import ch.cyberduck.core.local.DisabledApplicationQuitCallback;
import ch.cyberduck.core.local.TemporaryFileServiceFactory;
import ch.cyberduck.core.pasteboard.HostPasteboard;
import ch.cyberduck.core.pasteboard.PathPasteboard;
import ch.cyberduck.core.pasteboard.PathPasteboardFactory;
import ch.cyberduck.core.pool.SessionPool;
import ch.cyberduck.core.preferences.Preferences;
import ch.cyberduck.core.preferences.PreferencesFactory;
import ch.cyberduck.core.resources.IconCacheFactory;
import ch.cyberduck.core.serializer.HostDictionary;
import ch.cyberduck.core.ssl.X509TrustManager;
import ch.cyberduck.core.threading.BackgroundAction;
import ch.cyberduck.core.threading.BrowserTransferBackgroundAction;
import ch.cyberduck.core.threading.DefaultMainAction;
import ch.cyberduck.core.threading.DisconnectBackgroundAction;
import ch.cyberduck.core.threading.TransferBackgroundAction;
import ch.cyberduck.core.threading.WindowMainAction;
import ch.cyberduck.core.threading.WorkerBackgroundAction;
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
import ch.cyberduck.core.worker.MountWorker;
import ch.cyberduck.core.worker.SearchWorker;
import ch.cyberduck.core.worker.SessionListWorker;
import ch.cyberduck.ui.browser.Column;
import ch.cyberduck.ui.browser.DownloadDirectoryFinder;
import ch.cyberduck.ui.browser.PathReloadFinder;
import ch.cyberduck.ui.browser.RegexFilter;
import ch.cyberduck.ui.browser.SearchFilterFactory;
import ch.cyberduck.ui.browser.UploadDirectoryFinder;
import ch.cyberduck.ui.browser.UploadTargetFinder;
import ch.cyberduck.ui.cocoa.PromptLimitedListProgressListener;
import ch.cyberduck.ui.cocoa.datasource.BookmarkTableDataSource;
import ch.cyberduck.ui.cocoa.datasource.BrowserListViewDataSource;
import ch.cyberduck.ui.cocoa.datasource.BrowserOutlineViewDataSource;
import ch.cyberduck.ui.cocoa.datasource.BrowserTableDataSource;
import ch.cyberduck.ui.cocoa.delegate.ArchiveMenuDelegate;
import ch.cyberduck.ui.cocoa.delegate.CopyURLMenuDelegate;
import ch.cyberduck.ui.cocoa.delegate.EditMenuDelegate;
import ch.cyberduck.ui.cocoa.delegate.OpenURLMenuDelegate;
import ch.cyberduck.ui.cocoa.delegate.URLMenuDelegate;
import ch.cyberduck.ui.cocoa.quicklook.QLPreviewPanel;
import ch.cyberduck.ui.cocoa.quicklook.QLPreviewPanelController;
import ch.cyberduck.ui.cocoa.quicklook.QuickLook;
import ch.cyberduck.ui.cocoa.quicklook.QuickLookFactory;
import ch.cyberduck.ui.cocoa.toolbar.BrowserToolbarFactory;
import ch.cyberduck.ui.cocoa.toolbar.BrowserToolbarValidator;
import ch.cyberduck.ui.cocoa.view.BookmarkCell;
import ch.cyberduck.ui.cocoa.view.OutlineCell;

import org.apache.commons.collections4.CollectionUtils;
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
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

public class BrowserController extends WindowController
        implements ProgressListener, TranscriptListener, NSToolbar.Delegate, NSMenu.Validation, QLPreviewPanelController {
    private static final Logger log = Logger.getLogger(BrowserController.class);

    private final BookmarkCollection bookmarks
            = BookmarkCollection.defaultCollection();

    private final BrowserToolbarFactory browserToolbarFactory = new BrowserToolbarFactory(this);

    private final NSNotificationCenter notificationCenter = NSNotificationCenter.defaultCenter();

    public final BrowserToolbarValidator browserToolbarValidator = new BrowserToolbarValidator(this);

    /**
     * Connection pool
     */
    private SessionPool session = SessionPool.DISCONNECTED;

    /**
     * Log Drawer
     */
    private TranscriptController transcript;

    private final QuickLook quicklook = QuickLookFactory.get();

    private final Preferences preferences
            = PreferencesFactory.get();

    /**
     * Hide files beginning with '.'
     */
    private boolean showHiddenFiles;

    private Filter<Path> filenameFilter;

    {
        if(PreferencesFactory.get().getBoolean("browser.showHidden")) {
            this.filenameFilter = new NullFilter<Path>();
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

    @Delegate
    private BrowserOutlineViewDataSource browserOutlineModel;

    @Outlet
    private NSOutlineView browserOutlineView;

    @Delegate
    private AbstractBrowserTableDelegate browserOutlineViewDelegate;

    @Delegate
    private BrowserListViewDataSource browserListModel;

    @Outlet
    private NSTableView browserListView;

    @Delegate
    private AbstractBrowserTableDelegate browserListViewDelegate;

    private NSToolbar toolbar;

    private final Navigation navigation = new Navigation();

    private PathPasteboard pasteboard;

    private final ListProgressListener listener
            = new PromptLimitedListProgressListener(this);

    /**
     * Caching files listings of previously listed directories
     */
    private final PathCache cache
            = new PathCache(preferences.getInteger("browser.cache.size"));

    public BrowserController() {
        this.loadBundle();
    }

    @Override
    protected String getBundleName() {
        return "Browser";
    }

    protected void validateToolbar() {
        toolbar.validateVisibleItems();
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
        this.window.setToolbar(toolbar);
        this.window.makeFirstResponder(quickConnectPopup);
        this._updateBrowserColumns(browserListView, browserListViewDelegate);
        this._updateBrowserColumns(browserOutlineView, browserOutlineViewDelegate);
        if(preferences.getBoolean("browser.transcript.open")) {
            this.logDrawer.open();
        }
        if(LicenseFactory.find().equals(LicenseFactory.EMPTY_LICENSE)) {
            this.addDonateWindowTitle();
        }
        this.selectBookmarks(BookmarkSwitchSegement.bookmarks);
    }

    public Comparator<Path> getComparator() {
        return this.getSelectedBrowserDelegate().getSortingComparator();
    }

    public Filter<Path> getFilter() {
        return this.filenameFilter;
    }

    public PathPasteboard getPasteboard() {
        return pasteboard;
    }

    protected void setFilter(final Filter<Path> filter) {
        if(log.isDebugEnabled()) {
            log.debug(String.format("Set path filter to %s", filter));
        }
        if(null == filter) {
            this.searchField.setStringValue(StringUtils.EMPTY);
            this.filenameFilter = SearchFilterFactory.create(this.showHiddenFiles);
        }
        else {
            this.filenameFilter = filter;
        }
    }

    public void setShowHiddenFiles(boolean showHidden) {
        if(showHidden) {
            this.filenameFilter = SearchFilterFactory.NULL_FILTER;
            this.showHiddenFiles = true;
        }
        else {
            this.filenameFilter = SearchFilterFactory.HIDDEN_FILTER;
            this.showHiddenFiles = false;
        }
    }

    /**
     * Marks the current browser as the first responder
     */
    private void getFocus() {
        NSView view;
        if(this.getSelectedTabView() == BrowserTab.bookmarks) {
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
        window.makeFirstResponder(view);
    }

    /**
     * Make the browser reload its content. Will make use of the cache.
     */
    protected void reload() {
        if(this.isMounted()) {
            this.reload(workdir, Collections.singleton(workdir), this.getSelectedPaths(), false);
        }
        else {
            final NSTableView browser = this.getSelectedBrowserView();
            final BrowserTableDataSource model = this.getSelectedBrowserModel();
            model.render(browser, Collections.emptyList());
            this.setStatus();
        }
    }

    /**
     * Make the browser reload its content. Invalidates the cache.
     *
     * @param workdir  Use working directory as the current root of the browser
     * @param selected The items to be selected
     */
    protected void reload(final Path workdir, final List<Path> changed, final List<Path> selected) {
        this.reload(workdir, new PathReloadFinder().find(changed), selected, true);
    }

    /**
     * Make the browser reload its content. Invalidates the cache.
     *
     * @param workdir  Use working directory as the current root of the browser
     * @param folders  Folders to render
     * @param selected The items to be selected
     */
    protected void reload(final Path workdir, final Set<Path> folders, final List<Path> selected) {
        this.reload(workdir, folders, selected, true);
    }

    /**
     * Make the browser reload its content. Invalidates the cache.
     *
     * @param workdir    Use working directory as the current root of the browser
     * @param folders    Folders to render
     * @param selected   The items to be selected
     * @param invalidate Invalidate the cache before rendering
     */
    protected void reload(final Path workdir, final Set<Path> folders, final List<Path> selected, final boolean invalidate) {
        if(log.isDebugEnabled()) {
            log.debug(String.format("Reload data with selected files %s", selected));
        }
        final BrowserTableDataSource model = this.getSelectedBrowserModel();
        final NSTableView browser = this.getSelectedBrowserView();
        if(folders.isEmpty()) {
            // Render empty browser
            model.render(browser, Collections.emptyList());
        }
        if(null == workdir) {
            this.setNavigation(false);
            // Render empty browser
            model.render(browser, Collections.emptyList());
        }
        else {
            for(final Path folder : folders) {
                if(invalidate) {
                    // Invalidate cache
                    cache.invalidate(folder);
                }
                else {
                    if(cache.isCached(folder)) {
                        reload(browser, model, workdir, selected, folder);
                        return;
                    }
                }
                // Delay render until path is cached in the background
                this.background(new WorkerBackgroundAction<AttributedList<Path>>(this, session,
                        new SessionListWorker(cache, folder, listener) {
                                    @Override
                                    public void cleanup(final AttributedList<Path> list) {
                                        // Put into cache
                                        super.cleanup(list);
                                        // Update the working directory if listing is successful
                                        if(!(AttributedList.<Path>emptyList() == list)) {
                                            // Reload browser
                                            reload(browser, model, workdir, selected, folder);
                                        }
                                    }
                                }
                        )
                );
            }
            this.setStatus();
        }
    }

    /**
     * @param browser  Browser view
     * @param model    Browser Model
     * @param workdir  Use working directory as the current root of the browser
     * @param selected Selected files in browser
     * @param folder   Folder to render
     */
    private void reload(final NSTableView browser, final BrowserTableDataSource model, final Path workdir, final List<Path> selected, final Path folder) {
        this.workdir = workdir;
        this.setNavigation(workdir != null);
        this.setStatus();
        model.render(browser, Collections.singletonList(folder));
        this.select(selected);
    }

    private void select(final List<Path> selected) {
        final NSTableView browser = this.getSelectedBrowserView();
        if(CollectionUtils.isEqualCollection(this.getSelectedPaths(), selected)) {
            return;
        }
        browser.deselectAll(null);
        for(Path path : selected) {
            this.select(path, true, true);
        }
    }

    /**
     * @param file   Path to select
     * @param expand Keep previous selection
     * @param scroll Scroll to selection
     */
    private void select(final Path file, final boolean expand, final boolean scroll) {
        final NSTableView browser = this.getSelectedBrowserView();
        final BrowserTableDataSource model = this.getSelectedBrowserModel();
        if(log.isDebugEnabled()) {
            log.debug(String.format("Select row for reference %s", file));
        }
        int row = model.indexOf(browser, file);
        if(-1 == row) {
            log.warn(String.format("Failed to find row for %s", file));
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
                this.background(new QuicklookTransferBackgroundAction(this, quicklook, session, download, options, downloads));
            }
        }
    }

    /**
     * @return The first selected path found or null if there is no selection
     */
    public Path getSelectedPath() {
        final List<Path> s = this.getSelectedPaths();
        if(s.size() > 0) {
            return s.get(0);
        }
        return null;
    }

    /**
     * @return All selected paths or an empty list if there is no selection
     */
    public List<Path> getSelectedPaths() {
        final AbstractBrowserTableDelegate delegate = this.getSelectedBrowserDelegate();
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

    public int getSelectionCount() {
        return this.getSelectedBrowserView().numberOfSelectedRows().intValue();
    }

    private static NSPoint cascade = new NSPoint(0, 0);

    @Override
    public void setWindow(NSWindow window) {
        // Save frame rectangle
        window.setFrameAutosaveName("Browser");
        window.setTitle(preferences.getProperty("application.name"));
        window.setMiniwindowImage(IconCacheFactory.<NSImage>get().iconNamed("cyberduck-document.icns"));
        window.setMovableByWindowBackground(true);
        window.setCollectionBehavior(window.collectionBehavior() | NSWindow.NSWindowCollectionBehavior.NSWindowCollectionBehaviorFullScreenPrimary);
        window.setContentMinSize(new NSSize(600d, 200d));
        if(window.respondsToSelector(Foundation.selector("setTabbingIdentifier:"))) {
            window.setTabbingIdentifier(preferences.getProperty("browser.window.tabbing.identifier"));
        }
        super.setWindow(window);
        // Accept file promises from history tab
        window.registerForDraggedTypes(NSArray.arrayWithObject(NSPasteboard.FilesPromisePboardType));
        cascade = this.cascade(cascade);
    }

    @Override
    public void windowWillClose(final NSNotification notification) {
        // Convert from lower left to top left coordinates
        cascade = new NSPoint(this.window().frame().origin.x.doubleValue(),
                this.window().frame().origin.y.doubleValue() + this.window().frame().size.height.doubleValue());
        super.windowWillClose(notification);
    }

    /**
     * NSDraggingDestination protocol implementation
     *
     * @return NSDragOperation
     */
    @Action
    public NSUInteger draggingEntered(final NSDraggingInfo sender) {
        return this.draggingUpdated(sender);
    }

    /**
     * NSDraggingDestination protocol implementation
     */
    @Action
    public NSUInteger draggingUpdated(final NSDraggingInfo sender) {
        final NSPasteboard pasteboard = sender.draggingPasteboard();
        if(pasteboard.types().indexOfObject(NSString.stringWithString(NSPasteboard.FilesPromisePboardType)) != null) {
            final NSView hit = sender.draggingDestinationWindow().contentView().hitTest(sender.draggingLocation());
            if(hit != null) {
                if(hit.equals(bookmarkSwitchView)) {
                    return NSDraggingInfo.NSDragOperationCopy;
                }
            }
        }
        return NSDraggingInfo.NSDragOperationNone;
    }

    /**
     * NSDraggingDestination protocol implementation
     */
    @Action
    public boolean prepareForDragOperation(final NSDraggingInfo sender) {
        // Continue to performDragOperation
        return true;
    }

    /**
     * NSDraggingDestination protocol implementation
     */
    @Action
    public boolean performDragOperation(final NSDraggingInfo sender) {
        for(Host bookmark : HostPasteboard.getPasteboard()) {
            final Host duplicate = new HostDictionary().deserialize(bookmark.serialize(SerializerFactory.get()));
            // Make sure a new UUID is assigned for duplicate
            duplicate.setUuid(null);
            bookmarks.add(0, duplicate);
        }
        return true;
    }

    @Outlet
    private NSDrawer logDrawer;

    @Action
    public void drawerDidOpen(NSNotification notification) {
        preferences.setProperty("browser.transcript.open", true);
    }

    @Action
    public void drawerDidClose(NSNotification notification) {
        preferences.setProperty("browser.transcript.open", false);
        transcript.clear();
    }

    @Action
    public NSSize drawerWillResizeContents_toSize(final NSDrawer sender, final NSSize contentSize) {
        return contentSize;
    }

    @Action
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

    private NSTitlebarAccessoryViewController accessoryView;

    @Action
    public void setDonateButton(NSButton button) {
        if(!Factory.Platform.osversion.matches("10\\.(7|8|9).*")) {
            button.setTitle(LocaleFactory.localizedString("Get a registration key!", "License"));
            button.setAction(Foundation.selector("donateMenuClicked:"));
            button.sizeToFit();
            NSView view = NSView.create();
            view.setFrameSize(new NSSize(button.frame().size.width.doubleValue() + 10d, button.frame().size.height.doubleValue()));
            view.addSubview(button);
            accessoryView = NSTitlebarAccessoryViewController.create();
            accessoryView.setLayoutAttribute(NSTitlebarAccessoryViewController.NSLayoutAttributeRight);
            accessoryView.setView(view);
        }
    }

    private void addDonateWindowTitle() {
        if(!Factory.Platform.osversion.matches("10\\.(7|8|9).*")) {
            window.addTitlebarAccessoryViewController(accessoryView);
        }
    }

    public void removeDonateWindowTitle() {
        if(!Factory.Platform.osversion.matches("10\\.(7|8|9).*")) {
            accessoryView.removeFromParentViewController();
        }
    }

    public enum BrowserTab {
        bookmarks,
        list,
        outline;

        public static BrowserTab byPosition(final int position) {
            return BrowserTab.values()[position];
        }
    }

    public BrowserTab getSelectedTabView() {
        return BrowserTab.byPosition(browserTabView.indexOfTabViewItem(browserTabView.selectedTabViewItem()));
    }

    private NSTabView browserTabView;

    @Action
    public void setBrowserTabView(NSTabView browserTabView) {
        this.browserTabView = browserTabView;
    }

    /**
     * @return The currently selected browser view (which is either an outlineview or a plain tableview)
     */
    public NSTableView getSelectedBrowserView() {
        switch(BrowserSwitchSegement.byPosition(preferences.getInteger("browser.view"))) {
            case list:
                return browserListView;
            case outline:
            default:
                return browserOutlineView;
        }
    }

    /**
     * @return The datasource of the currently selected browser view
     */
    public BrowserTableDataSource getSelectedBrowserModel() {
        switch(BrowserSwitchSegement.byPosition(preferences.getInteger("browser.view"))) {
            case list:
                return browserListModel;
            case outline:
            default:
                return browserOutlineModel;
        }
    }

    public AbstractBrowserTableDelegate getSelectedBrowserDelegate() {
        switch(BrowserSwitchSegement.byPosition(preferences.getInteger("browser.view"))) {
            case list:
                return browserListViewDelegate;
            case outline:
            default:
                return browserOutlineViewDelegate;
        }
    }

    @Outlet
    private NSMenu editMenu;

    @Delegate
    private EditMenuDelegate editMenuDelegate;

    @Action
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

    public EditMenuDelegate getEditMenuDelegate() {
        return editMenuDelegate;
    }

    @Outlet
    private NSMenu urlMenu;

    @Delegate
    private URLMenuDelegate urlMenuDelegate;

    @Action
    public void setUrlMenu(NSMenu urlMenu) {
        this.urlMenu = urlMenu;
        this.urlMenuDelegate = new CopyURLMenuDelegate() {
            @Override
            protected SessionPool getSession() {
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

    @Delegate
    private URLMenuDelegate openUrlMenuDelegate;

    @Action
    public void setOpenUrlMenu(NSMenu openUrlMenu) {
        this.openUrlMenu = openUrlMenu;
        this.openUrlMenuDelegate = new OpenURLMenuDelegate() {
            @Override
            protected SessionPool getSession() {
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

    @Delegate
    private ArchiveMenuDelegate archiveMenuDelegate;

    @Action
    public void setArchiveMenu(NSMenu archiveMenu) {
        this.archiveMenu = archiveMenu;
        this.archiveMenuDelegate = new ArchiveMenuDelegate();
        this.archiveMenu.setDelegate(archiveMenuDelegate.id());
    }

    @Action
    public void sortBookmarksByNickame(final ID sender) {
        bookmarks.sortByNickname();
        this.reloadBookmarks();
    }

    @Action
    public void sortBookmarksByHostname(final ID sender) {
        bookmarks.sortByHostname();
        this.reloadBookmarks();
    }

    @Action
    public void sortBookmarksByProtocol(final ID sender) {
        bookmarks.sortByProtocol();
        this.reloadBookmarks();
    }

    private NSSegmentedControl bookmarkSwitchView;

    private enum BookmarkSwitchSegement {
        browser {
            @Override
            public NSImage image() {
                final NSImage image = IconCacheFactory.<NSImage>get().iconNamed(String.format("%s.tiff", "outline"), 16);
                image.setTemplate(true);
                return image;
            }
        },
        bookmarks,
        history,
        rendezvous;

        public NSImage image() {
            return IconCacheFactory.<NSImage>get().iconNamed(String.format("%s.tiff", name()), 16);
        }

        public static BookmarkSwitchSegement byPosition(final int position) {
            return BookmarkSwitchSegement.values()[position];
        }
    }

    @Action
    public void setBookmarkSwitchView(NSSegmentedControl bookmarkSwitchView) {
        this.bookmarkSwitchView = bookmarkSwitchView;
        this.bookmarkSwitchView.setSegmentCount(4);
        final NSSegmentedCell cell = Rococoa.cast(this.bookmarkSwitchView.cell(), NSSegmentedCell.class);
        cell.setToolTip_forSegment(LocaleFactory.localizedString("Browser"), BookmarkSwitchSegement.browser.ordinal());
        this.bookmarkSwitchView.setImage_forSegment(BookmarkSwitchSegement.browser.image(), BookmarkSwitchSegement.browser.ordinal());
        cell.setToolTip_forSegment(LocaleFactory.localizedString("Bookmarks"), BookmarkSwitchSegement.bookmarks.ordinal());
        this.bookmarkSwitchView.setImage_forSegment(BookmarkSwitchSegement.bookmarks.image(), BookmarkSwitchSegement.bookmarks.ordinal());
        cell.setToolTip_forSegment(LocaleFactory.localizedString("History"), BookmarkSwitchSegement.history.ordinal());
        this.bookmarkSwitchView.setImage_forSegment(BookmarkSwitchSegement.history.image(), BookmarkSwitchSegement.history.ordinal());
        cell.setToolTip_forSegment(LocaleFactory.localizedString("Bonjour"), BookmarkSwitchSegement.rendezvous.ordinal());
        this.bookmarkSwitchView.setImage_forSegment(BookmarkSwitchSegement.rendezvous.image(), BookmarkSwitchSegement.rendezvous.ordinal());
        this.bookmarkSwitchView.setTarget(this.id());
        this.bookmarkSwitchView.setAction(Foundation.selector("bookmarkSwitchButtonClicked:"));
    }

    @Action
    public void bookmarkSwitchMenuClicked(final NSMenuItem sender) {
        switch(this.getSelectedTabView()) {
            case bookmarks:
                this.selectBrowser(BrowserSwitchSegement.byPosition(preferences.getInteger("browser.view")));
                break;
            case list:
            case outline:
                this.selectBookmarks(BookmarkSwitchSegement.bookmarks);
                break;
        }
    }

    @Action
    public void bookmarkSwitchButtonClicked(final ID sender) {
        // Toggle
        final BookmarkSwitchSegement selected = BookmarkSwitchSegement.byPosition(bookmarkSwitchView.selectedSegment());
        switch(selected) {
            case browser:
                this.selectBrowser(BrowserSwitchSegement.outline);
                break;
            case bookmarks:
            case history:
            case rendezvous:
                this.selectBookmarks(selected);
                break;
        }
    }

    public enum BrowserSwitchSegement {
        list,
        outline;

        public NSImage image() {
            return IconCacheFactory.<NSImage>get().iconNamed(String.format("%s.tiff", name()), 16);
        }

        public static BrowserSwitchSegement byPosition(final int position) {
            return BrowserSwitchSegement.values()[position];
        }
    }

    @Action
    public void browserSwitchButtonClicked(final NSSegmentedControl sender) {
        // Highlight selected browser view
        this.selectBrowser(BrowserSwitchSegement.byPosition(sender.selectedSegment()));
    }

    @Action
    public void browserSwitchMenuClicked(final NSMenuItem sender) {
        // Highlight selected browser view
        this.selectBrowser(BrowserSwitchSegement.byPosition(sender.tag()));
    }

    private void selectBrowser(final BrowserSwitchSegement selected) {
        bookmarkSwitchView.setSelectedSegment(BookmarkSwitchSegement.browser.ordinal());
        this.setNavigation(this.isMounted());
        switch(selected) {
            case list:
                browserTabView.selectTabViewItemAtIndex(BrowserTab.list.ordinal());
                break;
            case outline:
                browserTabView.selectTabViewItemAtIndex(BrowserTab.outline.ordinal());
                break;
        }
        // Save selected browser view
        preferences.setProperty("browser.view", selected.ordinal());
        // Remove any custom file filter
        this.setFilter(null);
        // Update from model
        this.reload();
        // Focus on browser view
        this.getFocus();
    }

    private void selectBookmarks(final BookmarkSwitchSegement selected) {
        bookmarkSwitchView.setSelectedSegment(selected.ordinal());
        this.setNavigation(false);
        // Display bookmarks
        browserTabView.selectTabViewItemAtIndex(BrowserTab.bookmarks.ordinal());
        final AbstractHostCollection source;
        switch(selected) {
            case history:
                source = HistoryCollection.defaultCollection();
                break;
            case rendezvous:
                source = RendezvousCollection.defaultCollection();
                break;
            case bookmarks:
            default:
                source = bookmarks;
                break;

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

    private abstract class AbstractBrowserOutlineViewDelegate extends AbstractBrowserTableDelegate
            implements NSOutlineView.Delegate {

        protected AbstractBrowserOutlineViewDelegate(final NSTableColumn selectedColumn) {
            super(selectedColumn);
        }

        @Action
        public String outlineView_toolTipForCell_rect_tableColumn_item_mouseLocation(NSOutlineView t, NSCell cell,
                                                                                     ID rect, NSTableColumn c,
                                                                                     NSObject item, NSPoint mouseLocation) {
            return this.tooltip(cache.lookup(new NSObjectPathReference(item)));
        }

        @Action
        public String outlineView_typeSelectStringForTableColumn_item(final NSOutlineView view,
                                                                      final NSTableColumn tableColumn,
                                                                      final NSObject item) {
            if(tableColumn.identifier().equals(Column.filename.name())) {
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

    private abstract class AbstractBrowserListViewDelegate<E> extends AbstractBrowserTableDelegate
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
            if(tableColumn.identifier().equals(Column.filename.name())) {
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
            log.warn(String.format("No item at row %d", row));
            return null;
        }
    }

    private abstract class AbstractBrowserTableDelegate extends AbstractPathTableDelegate {

        protected AbstractBrowserTableDelegate(final NSTableColumn selectedColumn) {
            super(selectedColumn);
        }

        @Override
        public boolean isColumnRowEditable(NSTableColumn column, int row) {
            if(preferences.getBoolean("browser.editable")) {
                return column.identifier().equals(Column.filename.name());
            }
            return false;
        }

        @Override
        public void tableRowDoubleClicked(final ID sender) {
            BrowserController.this.insideButtonClicked(sender);
        }

        @Action
        public void spaceKeyPressed(final ID sender) {
            quicklookButtonClicked(sender);
        }

        @Override
        public void deleteKeyPressed(final ID sender) {
            BrowserController.this.deleteFileButtonClicked(sender);
        }

        @Override
        public void tableColumnClicked(final NSTableView view, final NSTableColumn tableColumn) {
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
            reload();
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

    @Action
    public void setBrowserOutlineView(NSOutlineView view) {
        browserOutlineView = view;
        // receive drag events from types
        browserOutlineView.registerForDraggedTypes(NSArray.arrayWithObjects(
                NSPasteboard.URLPboardType,
                // Accept files dragged from the Finder for uploading
                NSPasteboard.FilenamesPboardType,
                // Accept file promises made myself
                NSPasteboard.FilesPromisePboardType
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
            NSTableColumn c = browserOutlineColumnsFactory.create(Column.filename.name());
            c.headerCell().setStringValue(LocaleFactory.localizedString("Filename"));
            c.setMinWidth(new CGFloat(100));
            c.setWidth(preferences.getFloat(String.format("browser.column.%s.width",
                    Column.filename.name())));
            c.setMaxWidth(new CGFloat(1000));
            c.setResizingMask(NSTableColumn.NSTableColumnAutoresizingMask | NSTableColumn.NSTableColumnUserResizingMask);
            c.setDataCell(outlineCellPrototype);
            browserOutlineView.addTableColumn(c);
            browserOutlineView.setOutlineTableColumn(c);
        }
        browserOutlineView.setDataSource((browserOutlineModel = new BrowserOutlineViewDataSource(this, cache)).id());
        browserOutlineView.setDelegate((browserOutlineViewDelegate = new AbstractBrowserOutlineViewDelegate(
                browserOutlineView.tableColumnWithIdentifier(Column.filename.name())
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
                if(tableColumn.identifier().equals(Column.filename.name())) {
                    cell.setEditable(session.getFeature(Move.class).isSupported(path));
                    (Rococoa.cast(cell, OutlineCell.class)).setIcon(browserOutlineModel.iconForPath(path));
                }
                if(!BrowserController.this.isConnected() || !SearchFilterFactory.HIDDEN_FILTER.accept(path)) {
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
                                log.debug(String.format("Returning false to #outlineViewShouldExpandItem for column %s", draggingColumn));
                            }
                            // See ticket #60
                            return false;
                        }
                    }
                }
                return true;
            }

            @Override
            public boolean outlineView_isGroupItem(final NSOutlineView view, final NSObject item) {
                return false;
            }

            @Override
            public void outlineViewItemWillExpand(final NSNotification notification) {
                final NSObject object = Rococoa.cast(notification.userInfo(), NSDictionary.class).objectForKey("NSObject");
                final NSObjectPathReference reference = new NSObjectPathReference(object);
                final Path directory = cache.lookup(reference);
                if(null == directory) {
                    return;
                }
                reload(workdir, Collections.singleton(directory), getSelectedPaths(), false);
            }

            /**
             * @see NSOutlineView.Delegate
             */
            @Override
            public void outlineViewItemDidExpand(final NSNotification notification) {
                //
            }

            @Override
            public void outlineViewItemWillCollapse(final NSNotification notification) {
                //
            }

            /**
             * @see NSOutlineView.Delegate
             */
            @Override
            public void outlineViewItemDidCollapse(final NSNotification notification) {
                setStatus();
            }

            @Override
            protected boolean isTypeSelectSupported() {
                return true;
            }

        }).id());
    }

    @Action
    public void setBrowserListView(NSTableView view) {
        browserListView = view;
        // receive drag events from types
        browserListView.registerForDraggedTypes(NSArray.arrayWithObjects(
                NSPasteboard.URLPboardType,
                // Accept files dragged from the Finder for uploading
                NSPasteboard.FilenamesPboardType,
                // Accept file promises made myself
                NSPasteboard.FilesPromisePboardType
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
            NSTableColumn c = browserListColumnsFactory.create(Column.icon.name());
            c.headerCell().setStringValue(StringUtils.EMPTY);
            c.setMinWidth((20));
            c.setWidth(preferences.getFloat(String.format("browser.column.%s.width",
                    Column.icon.name())));
            c.setMaxWidth((20));
            c.setResizingMask(NSTableColumn.NSTableColumnAutoresizingMask);
            c.setDataCell(imageCellPrototype);
            c.dataCell().setAlignment(NSText.NSCenterTextAlignment);
            browserListView.addTableColumn(c);
        }
        {
            NSTableColumn c = browserListColumnsFactory.create(Column.filename.name());
            c.headerCell().setStringValue(LocaleFactory.localizedString("Filename"));
            c.setMinWidth((100));
            c.setWidth(preferences.getFloat(String.format("browser.column.%s.width",
                    Column.filename.name())));
            c.setMaxWidth((1000));
            c.setResizingMask(NSTableColumn.NSTableColumnAutoresizingMask | NSTableColumn.NSTableColumnUserResizingMask);
            c.setDataCell(filenameCellPrototype);
            this.browserListView.addTableColumn(c);
        }

        browserListView.setDataSource((browserListModel = new BrowserListViewDataSource(this, cache)).id());
        browserListView.setDelegate((browserListViewDelegate = new AbstractBrowserListViewDelegate<Path>(
                browserListView.tableColumnWithIdentifier(Column.filename.name())
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
                if(identifier.equals(Column.filename.name())) {
                    cell.setEditable(session.getFeature(Move.class).isSupported(path));
                }
                if(cell.isKindOfClass(Foundation.getClass(NSTextFieldCell.class.getSimpleName()))) {
                    if(!BrowserController.this.isConnected() || !SearchFilterFactory.HIDDEN_FILTER.accept(path)) {
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

    private void _updateBrowserColumns(final NSTableView table, final AbstractBrowserTableDelegate delegate) {
        table.removeTableColumn(table.tableColumnWithIdentifier(Column.size.name()));
        if(preferences.getBoolean(String.format("browser.column.%s", Column.size.name()))) {
            NSTableColumn c = browserListColumnsFactory.create(Column.size.name());
            c.headerCell().setStringValue(LocaleFactory.localizedString("Size"));
            c.setMinWidth(50f);
            c.setWidth(preferences.getFloat(String.format("browser.column.%s.width",
                    Column.size.name())));
            c.setMaxWidth(150f);
            c.setResizingMask(NSTableColumn.NSTableColumnAutoresizingMask | NSTableColumn.NSTableColumnUserResizingMask);
            c.setDataCell(textCellPrototype);
            table.addTableColumn(c);
        }
        table.removeTableColumn(table.tableColumnWithIdentifier(Column.modified.name()));
        if(preferences.getBoolean(String.format("browser.column.%s", Column.modified.name()))) {
            NSTableColumn c = browserListColumnsFactory.create(Column.modified.name());
            c.headerCell().setStringValue(LocaleFactory.localizedString("Modified"));
            c.setMinWidth(100f);
            c.setWidth(preferences.getFloat(String.format("browser.column.%s.width",
                    Column.modified.name())));
            c.setMaxWidth(500);
            c.setResizingMask(NSTableColumn.NSTableColumnAutoresizingMask | NSTableColumn.NSTableColumnUserResizingMask);
            c.setDataCell(textCellPrototype);
            table.addTableColumn(c);
        }
        table.removeTableColumn(table.tableColumnWithIdentifier(Column.owner.name()));
        if(preferences.getBoolean(String.format("browser.column.%s", Column.owner.name()))) {
            NSTableColumn c = browserListColumnsFactory.create(Column.owner.name());
            c.headerCell().setStringValue(LocaleFactory.localizedString("Owner"));
            c.setMinWidth(50);
            c.setWidth(preferences.getFloat(String.format("browser.column.%s.width",
                    Column.owner.name())));
            c.setMaxWidth(500);
            c.setResizingMask(NSTableColumn.NSTableColumnAutoresizingMask | NSTableColumn.NSTableColumnUserResizingMask);
            c.setDataCell(textCellPrototype);
            table.addTableColumn(c);
        }
        table.removeTableColumn(table.tableColumnWithIdentifier(Column.group.name()));
        if(preferences.getBoolean(String.format("browser.column.%s", Column.group.name()))) {
            NSTableColumn c = browserListColumnsFactory.create(Column.group.name());
            c.headerCell().setStringValue(LocaleFactory.localizedString("Group"));
            c.setMinWidth(50);
            c.setWidth(preferences.getFloat(String.format("browser.column.%s.width",
                    Column.group.name())));
            c.setMaxWidth(500);
            c.setResizingMask(NSTableColumn.NSTableColumnAutoresizingMask | NSTableColumn.NSTableColumnUserResizingMask);
            c.setDataCell(textCellPrototype);
            table.addTableColumn(c);
        }
        table.removeTableColumn(table.tableColumnWithIdentifier(Column.permission.name()));
        if(preferences.getBoolean(String.format("browser.column.%s", Column.permission.name()))) {
            NSTableColumn c = browserListColumnsFactory.create(Column.permission.name());
            c.headerCell().setStringValue(LocaleFactory.localizedString("Permissions"));
            c.setMinWidth(100);
            c.setWidth(preferences.getFloat(String.format("browser.column.%s.width",
                    Column.permission.name())));
            c.setMaxWidth(800);
            c.setResizingMask(NSTableColumn.NSTableColumnAutoresizingMask | NSTableColumn.NSTableColumnUserResizingMask);
            c.setDataCell(textCellPrototype);
            table.addTableColumn(c);
        }
        table.removeTableColumn(table.tableColumnWithIdentifier(Column.kind.name()));
        if(preferences.getBoolean(String.format("browser.column.%s", Column.kind.name()))) {
            NSTableColumn c = browserListColumnsFactory.create(Column.kind.name());
            c.headerCell().setStringValue(LocaleFactory.localizedString("Kind"));
            c.setMinWidth(50);
            c.setWidth(preferences.getFloat(String.format("browser.column.%s.width",
                    Column.kind.name())));
            c.setMaxWidth(500);
            c.setResizingMask(NSTableColumn.NSTableColumnAutoresizingMask | NSTableColumn.NSTableColumnUserResizingMask);
            c.setDataCell(textCellPrototype);
            table.addTableColumn(c);
        }
        table.removeTableColumn(table.tableColumnWithIdentifier(Column.extension.name()));
        if(preferences.getBoolean(String.format("browser.column.%s", Column.extension.name()))) {
            NSTableColumn c = browserListColumnsFactory.create(Column.extension.name());
            c.headerCell().setStringValue(LocaleFactory.localizedString("Extension"));
            c.setMinWidth(50);
            c.setWidth(preferences.getFloat(String.format("browser.column.%s.width",
                    Column.extension.name())));
            c.setMaxWidth(500);
            c.setResizingMask(NSTableColumn.NSTableColumnAutoresizingMask | NSTableColumn.NSTableColumnUserResizingMask);
            c.setDataCell(textCellPrototype);
            table.addTableColumn(c);
        }
        table.removeTableColumn(table.tableColumnWithIdentifier(Column.region.name()));
        if(preferences.getBoolean(String.format("browser.column.%s", Column.region.name()))) {
            NSTableColumn c = browserListColumnsFactory.create(Column.region.name());
            c.headerCell().setStringValue(LocaleFactory.localizedString("Region"));
            c.setMinWidth(50);
            c.setWidth(preferences.getFloat(String.format("browser.column.%s.width",
                    Column.region.name())));
            c.setMaxWidth(500);
            c.setResizingMask(NSTableColumn.NSTableColumnAutoresizingMask | NSTableColumn.NSTableColumnUserResizingMask);
            c.setDataCell(textCellPrototype);
            table.addTableColumn(c);
        }
        table.removeTableColumn(table.tableColumnWithIdentifier(Column.version.name()));
        if(preferences.getBoolean(String.format("browser.column.%s", Column.version.name()))) {
            NSTableColumn c = browserListColumnsFactory.create(Column.version.name());
            c.headerCell().setStringValue(LocaleFactory.localizedString("Version"));
            c.setMinWidth(50);
            c.setWidth(preferences.getFloat(String.format("browser.column.%s.width",
                    Column.version.name())));
            c.setMaxWidth(500);
            c.setResizingMask(NSTableColumn.NSTableColumnAutoresizingMask | NSTableColumn.NSTableColumnUserResizingMask);
            c.setDataCell(textCellPrototype);
            table.addTableColumn(c);
        }
        NSTableColumn selected = table.tableColumnWithIdentifier(preferences.getProperty("browser.sort.column"));
        if(null == selected) {
            selected = table.tableColumnWithIdentifier(Column.filename.name());
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
        this.reload();
    }

    @Delegate
    private BookmarkTableDataSource bookmarkModel;

    @Outlet
    private NSTableView bookmarkTable;

    @Delegate
    private AbstractTableDelegate<Host> bookmarkTableDelegate;

    @Action
    public void setBookmarkTable(NSTableView view) {
        bookmarkTable = view;
        bookmarkTable.setSelectionHighlightStyle(NSTableView.NSTableViewSelectionHighlightStyleSourceList);
        bookmarkTable.setDataSource((this.bookmarkModel = new BookmarkTableDataSource(this)).id());
        {
            NSTableColumn c = bookmarkTableColumnFactory.create(BookmarkTableDataSource.Column.icon.name());
            c.headerCell().setStringValue(StringUtils.EMPTY);
            c.setResizingMask(NSTableColumn.NSTableColumnNoResizing);
            c.setDataCell(imageCellPrototype);
            bookmarkTable.addTableColumn(c);
        }
        {
            NSTableColumn c = bookmarkTableColumnFactory.create(BookmarkTableDataSource.Column.bookmark.name());
            c.headerCell().setStringValue(LocaleFactory.localizedString("Bookmarks"));
            c.setMinWidth(150);
            c.setResizingMask(NSTableColumn.NSTableColumnAutoresizingMask);
            c.setDataCell(BookmarkCell.bookmarkCell());
            bookmarkTable.addTableColumn(c);
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
            bookmarkTable.addTableColumn(c);
        }
        bookmarkTable.setDelegate((bookmarkTableDelegate = new AbstractTableDelegate<Host>(
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

            @Action
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

            @Action
            public String tableView_typeSelectStringForTableColumn_row(NSTableView view,
                                                                       NSTableColumn tableColumn,
                                                                       NSInteger row) {
                return BookmarkNameProvider.toString(bookmarkModel.getSource().get(row.intValue()));
            }

            @Action
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
        bookmarkTable.registerForDraggedTypes(NSArray.arrayWithObjects(
                NSPasteboard.URLPboardType,
                NSPasteboard.StringPboardType,
                // Accept bookmark files dragged from the Finder
                NSPasteboard.FilenamesPboardType,
                // Accept file promises made myself
                NSPasteboard.FilesPromisePboardType
        ));
        this._updateBookmarkCell();

        final int size = preferences.getInteger("bookmark.icon.size");
        if(BookmarkCell.SMALL_BOOKMARK_SIZE == size) {
            bookmarkTable.setRowHeight(new CGFloat(18));
        }
        else if(BookmarkCell.MEDIUM_BOOKMARK_SIZE == size) {
            bookmarkTable.setRowHeight(new CGFloat(45));
        }
        else {
            bookmarkTable.setRowHeight(new CGFloat(70));
        }

        // setting appearance attributes()
        bookmarkTable.setUsesAlternatingRowBackgroundColors(preferences.getBoolean("browser.alternatingRows"));
        bookmarkTable.setGridStyleMask(NSTableView.NSTableViewGridNone);

        // selection properties
        bookmarkTable.setAllowsMultipleSelection(true);
        bookmarkTable.setAllowsEmptySelection(true);
        bookmarkTable.setAllowsColumnResizing(false);
        bookmarkTable.setAllowsColumnSelection(false);
        bookmarkTable.setAllowsColumnReordering(false);
        bookmarkTable.sizeToFit();
    }

    public NSTableView getBookmarkTable() {
        return bookmarkTable;
    }

    public BookmarkTableDataSource getBookmarkModel() {
        return bookmarkModel;
    }

    @Outlet
    private NSComboBox quickConnectPopup;

    private final ProxyController quickConnectPopupModel = new QuickConnectModel();

    @Action
    public void setQuickConnectPopup(NSComboBox quickConnectPopup) {
        this.quickConnectPopup = quickConnectPopup;
        this.quickConnectPopup.setTarget(this.id());
        this.quickConnectPopup.setCompletes(true);
        this.quickConnectPopup.setAction(Foundation.selector("quickConnectSelectionChanged:"));
        // Make sure action is not sent twice.
        this.quickConnectPopup.cell().setSendsActionOnEndEditing(false);
        this.quickConnectPopup.setUsesDataSource(true);
        this.quickConnectPopup.setDataSource(quickConnectPopupModel.id());
        this.quickConnectPopup.setFocusRingType(NSView.NSFocusRingType.NSFocusRingTypeNone.ordinal());
        notificationCenter.addObserver(this.id(),
                Foundation.selector("quickConnectWillPopUp:"),
                NSComboBox.ComboBoxWillPopUpNotification,
                this.quickConnectPopup);
        this.quickConnectWillPopUp(null);
    }

    public NSComboBox getQuickConnectPopup() {
        return quickConnectPopup;
    }

    private class QuickConnectModel extends ProxyController implements NSComboBox.DataSource {
        @Override
        public NSInteger numberOfItemsInComboBox(final NSComboBox combo) {
            return new NSInteger(bookmarks.size());
        }

        @Override
        public NSObject comboBox_objectValueForItemAtIndex(final NSComboBox sender, final NSInteger row) {
            return NSString.stringWithString(
                    BookmarkNameProvider.toString(bookmarks.get(row.intValue()))
            );
        }
    }

    @Action
    public void quickConnectWillPopUp(NSNotification notification) {
        int size = bookmarks.size();
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
        for(Host h : bookmarks) {
            if(BookmarkNameProvider.toString(h).equals(input)) {
                this.mount(h);
                return;
            }
        }
        // Try to parse the input as a URL and extract protocol, hostname, username and password if any.
        this.mount(HostParser.parse(input));
    }

    @Outlet
    private NSSearchField searchField;

    @Action
    public void setSearchField(NSSearchField searchField) {
        this.searchField = searchField;
        if(this.searchField.respondsToSelector(Foundation.selector("setSendsWholeSearchString:"))) {
            // calls its search action method when the user clicks the search button (or presses Return)
            this.searchField.setSendsWholeSearchString(false);
        }
        if(this.searchField.respondsToSelector(Foundation.selector("setSendsSearchStringImmediately:"))) {
            this.searchField.setSendsSearchStringImmediately(false);
        }
        this.searchField.setTarget(this.id());
        this.searchField.setAction(Foundation.selector("searchFieldTextDidChange:"));
        // Make sure action is not sent twice.
        this.searchField.cell().setSendsActionOnEndEditing(false);
        this.notificationCenter.addObserver(this.id(),
                Foundation.selector("searchFieldTextDidEndEditing:"),
                NSControl.NSControlTextDidEndEditingNotification,
                this.searchField);
    }

    @Action
    public void searchButtonClicked(final ID sender) {
        this.window().makeFirstResponder(searchField);
    }

    @Action
    public void searchFieldTextDidChange(NSNotification notification) {
        final String input = searchField.stringValue();
        switch(this.getSelectedTabView()) {
            case bookmarks:
                this.setBookmarkFilter(input);
                break;
            case list:
            case outline:
                // Setup search filter
                this.setFilter(SearchFilterFactory.create(input, showHiddenFiles));
                // Reload with current cache
                this.reload();
                break;
        }
    }

    @Action
    public void searchFieldTextDidEndEditing(NSNotification notification) {
        switch(this.getSelectedTabView()) {
            case list:
            case outline:
                // Setup search filter
                final String input = searchField.stringValue();
                // Setup search filter
                final Filter<Path> filter = SearchFilterFactory.create(input, showHiddenFiles);
                this.setFilter(filter);
                if(StringUtils.isBlank(input)) {
                    // Reload with current cache
                    this.reload();
                }
                else {
                    final NSObject action = notification.userInfo().objectForKey("NSTextMovement");
                    if(null == action) {
                        return;
                    }
                    if(Integer.valueOf(action.toString()) == NSText.NSReturnTextMovement) {
                        final NSAlert alert = NSAlert.alert(
                                MessageFormat.format(LocaleFactory.localizedString("Search for {0}"), input),
                                MessageFormat.format(LocaleFactory.localizedString("Do you want to search in {0} recursively?"), workdir.getName()),
                                LocaleFactory.localizedString("Search"),
                                LocaleFactory.localizedString("Cancel"),
                                null
                        );
                        this.alert(alert, new DisabledSheetCallback() {
                            @Override
                            public void callback(int returncode) {
                                if(returncode == DEFAULT_OPTION) {
                                    // Delay render until path is cached in the background
                                    background(new WorkerBackgroundAction<AttributedList<Path>>(BrowserController.this, session,
                                            new SearchWorker(workdir, filenameFilter, cache, listener) {
                                                @Override
                                                public void cleanup(final AttributedList<Path> list) {
                                                    // Reload browser
                                                    reload();
                                                }
                                            })
                                    );
                                }
                            }
                        });
                    }
                }
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
                            || ((null != host.getCredentials().getUsername()) && StringUtils.lowerCase(host.getCredentials().getUsername()).contains(searchString.toLowerCase(Locale.ROOT)))
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

    @Action
    public void setEditBookmarkButton(NSButton editBookmarkButton) {
        this.editBookmarkButton = editBookmarkButton;
        this.editBookmarkButton.setEnabled(false);
        this.editBookmarkButton.setTarget(this.id());
        this.editBookmarkButton.setAction(Foundation.selector("editBookmarkButtonClicked:"));
    }

    @Action
    public void editBookmarkButtonClicked(final ID sender) {
        final BookmarkController c = BookmarkControllerFactory.create(bookmarks,
                bookmarkModel.getSource().get(bookmarkTable.selectedRow().intValue())
        );
        c.window().makeKeyAndOrderFront(null);
    }

    @Action
    public void duplicateBookmarkButtonClicked(final ID sender) {
        final Host selected = bookmarkModel.getSource().get(bookmarkTable.selectedRow().intValue());
        this.selectBookmarks(BookmarkSwitchSegement.bookmarks);
        final Host duplicate = new HostDictionary().deserialize(selected.serialize(SerializerFactory.get()));
        // Make sure a new UUID is asssigned for duplicate
        duplicate.setUuid(null);
        this.addBookmark(duplicate);
    }

    @Outlet
    private NSButton addBookmarkButton;

    @Action
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
            bookmark = new HostDictionary().deserialize(session.getHost().serialize(SerializerFactory.get()));
            // Make sure a new UUID is asssigned for duplicate
            bookmark.setUuid(null);
            bookmark.setDefaultPath(selected.getAbsolute());
        }
        else {
            bookmark = new Host(ProtocolFactory.forName(preferences.getProperty("connection.protocol.default")),
                    preferences.getProperty("connection.hostname.default"),
                    preferences.getInteger("connection.port.default"));
        }
        this.selectBookmarks(BookmarkSwitchSegement.bookmarks);
        this.addBookmark(bookmark);
    }

    public void addBookmark(Host item) {
        bookmarkModel.setFilter(null);
        bookmarkModel.getSource().add(item);
        final int row = bookmarkModel.getSource().lastIndexOf(item);
        final NSInteger index = new NSInteger(row);
        bookmarkTable.selectRowIndexes(NSIndexSet.indexSetWithIndex(index), false);
        bookmarkTable.scrollRowToVisible(index);
        final BookmarkController c = BookmarkControllerFactory.create(bookmarks, item);
        c.window().makeKeyAndOrderFront(null);
    }

    @Outlet
    private NSButton deleteBookmarkButton;

    @Action
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
        this.alert(alert, new DisabledSheetCallback() {
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

    public Navigation getNavigation() {
        return navigation;
    }

    private enum NavigationSegment {
        back(0),
        forward(1),
        up(0);

        private final int position;

        NavigationSegment(final int position) {
            this.position = position;
        }

        public int position() {
            return position;
        }

        public static NavigationSegment byPosition(final int position) {
            return NavigationSegment.values()[position];
        }
    }

    private NSSegmentedControl navigationButton;

    @Action
    public void setNavigationButton(NSSegmentedControl navigationButton) {
        this.navigationButton = navigationButton;
        this.navigationButton.setTarget(this.id());
        this.navigationButton.setAction(Foundation.selector("navigationButtonClicked:"));
        final NSSegmentedCell cell = Rococoa.cast(this.navigationButton.cell(), NSSegmentedCell.class);
        this.navigationButton.setImage_forSegment(IconCacheFactory.<NSImage>get().iconNamed("nav-backward.tiff"),
                NavigationSegment.back.position());
        cell.setToolTip_forSegment(LocaleFactory.localizedString("Back", "Main"), NavigationSegment.back.position());
        this.navigationButton.setImage_forSegment(IconCacheFactory.<NSImage>get().iconNamed("nav-forward.tiff"),
                NavigationSegment.forward.position());
        cell.setToolTip_forSegment(LocaleFactory.localizedString("Forward", "Main"), NavigationSegment.forward.position());
    }

    @Action
    public void navigationButtonClicked(NSSegmentedControl sender) {
        switch(NavigationSegment.byPosition(sender.selectedSegment())) {
            case back:
                this.backButtonClicked(sender.id());
                break;
            case forward:
                this.forwardButtonClicked(sender.id());
                break;
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

    @Action
    public void setUpButton(NSSegmentedControl upButton) {
        this.upButton = upButton;
        this.upButton.setTarget(this.id());
        this.upButton.setAction(Foundation.selector("upButtonClicked:"));
        this.upButton.setImage_forSegment(IconCacheFactory.<NSImage>get().iconNamed("nav-up.tiff"),
                NavigationSegment.up.position());
    }

    @Action
    public void upButtonClicked(final ID sender) {
        final Path previous = this.workdir();
        this.setWorkdir(previous.getParent(), previous);
    }

    private Path workdir;

    @Outlet
    private NSPopUpButton pathPopupButton;

    @Action
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

    @Action
    public void setEncodingPopup(NSPopUpButton encodingPopup) {
        this.encodingPopup = encodingPopup;
        this.encodingPopup.setTarget(this.id());
        this.encodingPopup.setAction(Foundation.selector("encodingButtonClicked:"));
        this.encodingPopup.removeAllItems();
        this.encodingPopup.addItemsWithTitles(NSArray.arrayWithObjects(new DefaultCharsetProvider().availableCharsets()));
        this.encodingPopup.selectItemWithTitle(preferences.getProperty("browser.charset.encoding"));
    }

    public NSPopUpButton getEncodingPopup() {
        return encodingPopup;
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
            if(session.getHost().getEncoding().equals(encoding)) {
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

    @Action
    public void setStatusSpinner(NSProgressIndicator statusSpinner) {
        this.statusSpinner = statusSpinner;
        this.statusSpinner.setDisplayedWhenStopped(false);
        this.statusSpinner.setIndeterminate(true);
    }

    @Outlet
    protected NSProgressIndicator browserSpinner;

    @Action
    public void setBrowserSpinner(NSProgressIndicator browserSpinner) {
        this.browserSpinner = browserSpinner;
    }

    @Outlet
    private NSTextField statusLabel;

    @Action
    public void setStatusLabel(NSTextField statusLabel) {
        this.statusLabel = statusLabel;
    }

    public void setStatus() {
        final BackgroundAction current = this.getActions().getCurrent();
        this.message(null != current ? current.getActivity() : null);
    }

    @Override
    public void stop(final BackgroundAction action) {
        this.invoke(new DefaultMainAction() {
            @Override
            public void run() {
                statusSpinner.stopAnimation(null);
            }
        });
        super.stop(action);
    }

    @Override
    public void start(final BackgroundAction action) {
        this.invoke(new DefaultMainAction() {
            @Override
            public void run() {
                statusSpinner.startAnimation(null);
            }
        });
        super.start(action);
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
            if(getSelectedTabView() == BrowserTab.bookmarks) {
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
    public void log(final Type request, final String message) {
        transcript.log(request, message);
    }

    @Outlet
    private NSButton securityLabel;

    @Action
    public void setSecurityLabel(NSButton securityLabel) {
        this.securityLabel = securityLabel;
        this.securityLabel.setEnabled(false);
        this.securityLabel.setTarget(this.id());
        this.securityLabel.setAction(Foundation.selector("securityLabelClicked:"));
    }

    @Action
    public void securityLabelClicked(final ID sender) {
        final List<X509Certificate> certificates = Arrays.asList(session.getFeature(X509TrustManager.class).getAcceptedIssuers());
        try {
            CertificateStoreFactory.get(this).display(certificates);
        }
        catch(CertificateException e) {
            log.warn(String.format("Failure decoding certificate %s", e.getMessage()));
        }
    }

    // ----------------------------------------------------------
    // Selector methods for the toolbar items
    // ----------------------------------------------------------

    @Action
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
            final Set<Path> folders = new HashSet<Path>();
            switch(BrowserSwitchSegement.byPosition(preferences.getInteger("browser.view"))) {
                case outline: {
                    for(int i = 0; i < browserOutlineView.numberOfRows().intValue(); i++) {
                        final NSObject item = browserOutlineView.itemAtRow(new NSInteger(i));
                        if(browserOutlineView.isItemExpanded(item)) {
                            final Path folder = cache.lookup(new NSObjectPathReference(item));
                            if(null == folder) {
                                continue;
                            }
                            folders.add(folder);
                        }
                    }
                    break;
                }
            }
            folders.add(workdir);
            this.reload(workdir, folders, this.getSelectedPaths(), true);
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
     * @param selected File
     * @return True if the selected path is editable (not a directory and no known binary file)
     */
    public boolean isEditable(final Path selected) {
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
        final GotoController sheet = new GotoController(this, cache);
        sheet.beginSheet();
    }

    @Action
    public void createFileButtonClicked(final ID sender) {
        final CreateFileController sheet = new CreateFileController(this, cache);
        sheet.beginSheet();
    }

    @Action
    public void createSymlinkButtonClicked(final ID sender) {
        final CreateSymlinkController sheet = new CreateSymlinkController(this, cache);
        sheet.beginSheet();
    }

    @Action
    public void duplicateFileButtonClicked(final ID sender) {
        final DuplicateFileController sheet = new DuplicateFileController(this, cache);
        sheet.beginSheet();
    }

    @Action
    public void createFolderButtonClicked(final ID sender) {
        final Location feature = session.getFeature(Location.class);
        final FolderController sheet = new FolderController(this, cache,
                feature != null ? feature.getLocations() : Collections.emptySet());
        sheet.beginSheet();
    }

    @Action
    public void createEncryptedVaultButtonClicked(final ID sender) {
        this.createFolderButtonClicked(sender);
    }

    @Action
    public void renameFileButtonClicked(final ID sender) {
        final NSTableView browser = this.getSelectedBrowserView();
        browser.editRow(browser.columnWithIdentifier(Column.filename.name()),
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
        CommandController controller = new CommandController(this, session);
        final SheetInvoker sheet = new SheetInvoker(new DisabledSheetCallback(), this, controller);
        sheet.beginSheet();
    }

    @Action
    public void editMenuClicked(final NSMenuItem sender) {
        final EditorFactory factory = EditorFactory.instance();
        for(Path selected : this.getSelectedPaths()) {
            final Editor editor = factory.create(this, session,
                    new Application(sender.representedObject()), selected);
            this.edit(editor);
        }
    }

    @Action
    public void editButtonClicked(final ID sender) {
        for(Path selected : this.getSelectedPaths()) {
            this.edit(selected);
        }
    }

    public void edit(final Path file) {
        this.edit(EditorFactory.instance().create(this, session, file));
    }

    protected void edit(final Editor editor) {
        this.background(new WorkerBackgroundAction<Transfer>(this, session, editor.open(
                new DisabledApplicationQuitCallback(), new DisabledTransferErrorCallback(), new DefaultEditorListener(this, session, editor))));
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
        new RevertController(this).revert(this.getSelectedPaths());
    }

    @Action
    public void deleteFileButtonClicked(final ID sender) {
        new DeleteController(this).delete(this.getSelectedPaths());
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
        downloadToPanel.beginSheetForDirectory(new DownloadDirectoryFinder().find(session.getHost()).getAbsolute(),
                null, this.window, this.id(),
                Foundation.selector("downloadToPanelDidEnd:returnCode:contextInfo:"), null);
    }

    @Action
    public void downloadToPanelDidEnd_returnCode_contextInfo(final NSOpenPanel sheet, final int returncode, final ID contextInfo) {
        sheet.close();
        if(returncode == SheetCallback.DEFAULT_OPTION) {
            if(sheet.filename() != null) {
                final Local target = LocalFactory.get(sheet.filename());
                new DownloadDirectoryFinder().save(session.getHost(), target);
                final List<TransferItem> downloads = new ArrayList<TransferItem>();
                for(Path file : this.getSelectedPaths()) {
                    downloads.add(new TransferItem(file, LocalFactory.get(target, file.getName())));
                }
                this.transfer(new DownloadTransfer(session.getHost(), downloads), Collections.emptyList());
            }
        }
        downloadToPanel = null;
    }

    @Outlet
    private NSSavePanel downloadAsPanel;

    @Action
    public void downloadAsButtonClicked(final ID sender) {
        downloadAsPanel = NSSavePanel.savePanel();
        downloadAsPanel.setMessage(LocaleFactory.localizedString("Download the selected file to…"));
        downloadAsPanel.setNameFieldLabel(LocaleFactory.localizedString("Download As:"));
        downloadAsPanel.setPrompt(LocaleFactory.localizedString("Download", "Transfer"));
        downloadAsPanel.setCanCreateDirectories(true);
        downloadAsPanel.beginSheetForDirectory(new DownloadDirectoryFinder().find(session.getHost()).getAbsolute(),
                this.getSelectedPath().getName(), this.window, this.id(),
                Foundation.selector("downloadAsPanelDidEnd:returnCode:contextInfo:"), null);
    }

    @Action
    public void downloadAsPanelDidEnd_returnCode_contextInfo(final NSSavePanel sheet, final int returncode, final ID contextInfo) {
        sheet.close();
        if(returncode == SheetCallback.DEFAULT_OPTION) {
            if(sheet.filename() != null) {
                final Local target = LocalFactory.get(sheet.filename());
                new DownloadDirectoryFinder().save(session.getHost(), target.getParent());
                final List<TransferItem> downloads
                        = Collections.singletonList(new TransferItem(this.getSelectedPath(), target));
                this.transfer(new DownloadTransfer(session.getHost(), downloads), Collections.emptyList());
            }
        }
    }

    @Outlet
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
        syncPanel.beginSheetForDirectory(new UploadDirectoryFinder().find(session.getHost()).getAbsolute(),
                null, this.window, this.id(),
                Foundation.selector("syncPanelDidEnd:returnCode:contextInfo:"), null //context info
        );
    }

    @Action
    public void syncPanelDidEnd_returnCode_contextInfo(final NSOpenPanel sheet, final int returncode, final ID contextInfo) {
        sheet.close();
        if(returncode == SheetCallback.DEFAULT_OPTION) {
            if(sheet.filename() != null) {
                final Local target = LocalFactory.get(sheet.filename());
                new UploadDirectoryFinder().save(session.getHost(), target.getParent());
                final Path selected;
                if(this.getSelectionCount() == 1 && this.getSelectedPath().isDirectory()) {
                    selected = this.getSelectedPath();
                }
                else {
                    selected = this.workdir();
                }
                this.transfer(new SyncTransfer(session.getHost(), new TransferItem(selected, target)));
            }
        }
    }

    @Action
    public void downloadButtonClicked(final ID sender) {
        final List<TransferItem> downloads = new ArrayList<TransferItem>();
        final Local folder = new DownloadDirectoryFinder().find(session.getHost());
        for(Path file : this.getSelectedPaths()) {
            downloads.add(new TransferItem(
                    file, LocalFactory.get(folder, file.getName())));
        }
        this.transfer(new DownloadTransfer(session.getHost(), downloads), Collections.emptyList());
    }

    private NSOpenPanel uploadPanel;

    private NSButton uploadPanelHiddenFilesCheckbox;

    @Action
    public void uploadButtonClicked(final ID sender) {
        uploadPanel = NSOpenPanel.openPanel();
        uploadPanel.setCanChooseDirectories(true);
        uploadPanel.setCanChooseFiles(session.getFeature(Touch.class).isSupported(
                new UploadTargetFinder(workdir).find(this.getSelectedPath())
        ));
        uploadPanel.setCanCreateDirectories(false);
        uploadPanel.setTreatsFilePackagesAsDirectories(true);
        uploadPanel.setAllowsMultipleSelection(true);
        uploadPanel.setPrompt(LocaleFactory.localizedString("Upload", "Transfer"));
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
        uploadPanel.beginSheetForDirectory(new UploadDirectoryFinder().find(session.getHost()).getAbsolute(),
                null, this.window,
                this.id(),
                Foundation.selector("uploadPanelDidEnd:returnCode:contextInfo:"),
                null);
    }

    @Action
    public void uploadPanelSetShowHiddenFiles(ID sender) {
        uploadPanel.setShowsHiddenFiles(uploadPanelHiddenFilesCheckbox.state() == NSCell.NSOnState);
    }

    @Action
    public void uploadPanelDidEnd_returnCode_contextInfo(final NSOpenPanel sheet, final int returncode, final ID contextInfo) {
        sheet.close();
        if(returncode == SheetCallback.DEFAULT_OPTION) {
            final Path destination = new UploadTargetFinder(workdir).find(this.getSelectedPath());
            // Selected files on the local filesystem
            final NSArray selected = sheet.filenames();
            final NSEnumerator iterator = selected.objectEnumerator();
            final List<TransferItem> uploads = new ArrayList<TransferItem>();
            NSObject next;
            while((next = iterator.nextObject()) != null) {
                final Local local = LocalFactory.get(next.toString());
                new UploadDirectoryFinder().save(session.getHost(), local.getParent());
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

    public void transfer(final Transfer transfer) {
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
    public void transfer(final Transfer transfer, final List<Path> selected) {
        // Determine from current browser session if new connection should be opened for transfers
        this.transfer(transfer, selected, transfer.getHost().getTransferType().equals(Host.TransferType.browser));
    }

    /**
     * @param transfer Transfer Operation
     * @param browser  Transfer in browser window
     */
    public void transfer(final Transfer transfer, final List<Path> selected, boolean browser) {
        final TransferCallback callback = new TransferCallback() {
            @Override
            public void complete(final Transfer transfer) {
                invoke(new WindowMainAction(BrowserController.this) {
                    @Override
                    public void run() {
                        reload(workdir, selected, selected);
                    }
                });
            }
        };
        if(browser) {
            this.background(new BrowserTransferBackgroundAction(this, session, transfer, callback));
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
        final ConnectionController controller = ConnectionControllerFactory.create(this);
        final SheetInvoker sheet = new SheetInvoker(new SheetCallback() {
            @Override
            public void callback(final int returncode) {
                if(returncode == SheetCallback.DEFAULT_OPTION) {
                    mount(controller.getBookmark());
                }
            }
        }, this, controller);
        sheet.beginSheet();
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
                    selectBookmarks(BookmarkSwitchSegement.bookmarks);
                }
                else {
                    selectBrowser(BrowserSwitchSegement.byPosition(preferences.getInteger("browser.view")));
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
        this.reload();
    }

    /**
     * @return This browser's session or null if not mounted
     */
    public SessionPool getSession() {
        return session;
    }

    public Cache<Path> getCache() {
        return cache;
    }

    /**
     * @return true if the remote file system has been mounted
     */
    public boolean isMounted() {
        if(session == SessionPool.DISCONNECTED) {
            return false;
        }
        return workdir != null;
    }

    /**
     * @return true if mounted and the connection to the server is alive
     */
    public boolean isConnected() {
        if(this.isMounted()) {
            return session.getState() == Session.State.open;
        }
        return false;
    }

    /**
     * NSService
     * <p>
     * Indicates whether the receiver can send and receive the specified pasteboard types.
     * <p>
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
     * <p>
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
     * <p>
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
                new MoveController(this).rename(files);
            }
            if(pasteboard.isCopy()) {
                new DuplicateFileController(this, cache).duplicate(files);
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
                        final Local local = LocalFactory.get(elements.objectAtIndex(new NSUInteger(i)).toString());
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
            final TerminalService terminal = TerminalServiceFactory.get();
            terminal.open(session.getHost(), workdir);
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
     * @param format Archive format
     */
    private void archiveClicked(final Archive format) {
        new ArchiveController(this).archive(format, this.getSelectedPaths());
    }

    @Action
    public void unarchiveButtonClicked(final ID sender) {
        new ArchiveController(this).unarchive(this.getSelectedPaths());
    }

    /**
     * Accessor to the working directory
     *
     * @return The current working directory or null if no file system is mounted
     */
    public Path workdir() {
        return workdir;
    }

    public void setWorkdir(final Path directory) {
        this.setWorkdir(directory, Collections.emptyList());
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
        // Remove any custom file filter
        this.setFilter(null);
        final NSTableView browser = this.getSelectedBrowserView();
        window.endEditingFor(browser);
        if(null == directory) {
            this.reload(null, Collections.emptySet(), selected, false);
        }
        else {
            this.reload(directory, Collections.singleton(directory), selected, false);
        }
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
        navigationButton.setEnabled_forSegment(enabled && navigation.getBack().size() > 1, NavigationSegment.back.position());
        navigationButton.setEnabled_forSegment(enabled && navigation.getForward().size() > 0, NavigationSegment.forward.position());
        upButton.setEnabled_forSegment(enabled && !workdir.isRoot(), NavigationSegment.up.position());
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
     * @param bookmark Bookmark
     * @return A session object bound to this browser controller
     */
    private SessionPool init(final Host bookmark) {
        session = SessionPoolFactory.create(this, cache, bookmark);
        transcript.clear();
        navigation.clear();
        pasteboard = PathPasteboardFactory.getPasteboard(bookmark);
        this.setWorkdir(null);
        this.setEncoding(bookmark.getEncoding());
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
                final SessionPool session = init(host);
                background(new WorkerBackgroundAction<Path>(BrowserController.this, session,
                        new MountWorker(host, cache, listener) {
                            @Override
                            public void cleanup(final Path workdir) {
                                super.cleanup(workdir);
                                if(null == workdir) {
                                    doUnmount(new Runnable() {
                                        @Override
                                        public void run() {
                                            //
                                        }
                                    });
                                }
                                else {
                                    // Update status icon
                                    bookmarkTable.setNeedsDisplay();
                                    // Set the working directory
                                    setWorkdir(workdir);
                                    // Close bookmarks
                                    selectBrowser(BrowserSwitchSegement.byPosition(preferences.getInteger("browser.view")));
                                    // Set the window title
                                    window.setRepresentedFilename(HistoryCollection.defaultCollection().getFile(host).getAbsolute());
                                    if(preferences.getBoolean("browser.disconnect.confirm")) {
                                        window.setDocumentEdited(true);
                                    }
                                    securityLabel.setImage(host.getProtocol().isSecure() ? IconCacheFactory.<NSImage>get().iconNamed("NSLockLockedTemplate")
                                            : IconCacheFactory.<NSImage>get().iconNamed("NSLockUnlockedTemplate"));
                                    securityLabel.setEnabled(session.getFeature(X509TrustManager.class) != null);
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
     * @param disconnected Callback after the session has been disconnected
     * @return True if the unmount process has finished, false if the user has to agree first
     * to close the connection
     */
    public boolean unmount(final Runnable disconnected) {
        return this.unmount(new DisabledSheetCallback() {
            @Override
            public void callback(int returncode) {
                if(returncode == DEFAULT_OPTION) {
                    doUnmount(disconnected);
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
                        MessageFormat.format(LocaleFactory.localizedString("Disconnect from {0}"), session.getHost().getHostname()), //title
                        LocaleFactory.localizedString("The connection will be closed."), // message
                        LocaleFactory.localizedString("Disconnect"), // defaultbutton
                        LocaleFactory.localizedString("Cancel"), // alternate button
                        null //other button
                );
                alert.setShowsSuppressionButton(true);
                alert.suppressionButton().setTitle(LocaleFactory.localizedString("Don't ask again", "Configuration"));
                this.alert(alert, new DisabledSheetCallback() {
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
        this.doUnmount(disconnected);
        // Unmount succeeded
        return true;
    }

    /**
     * @param disconnected Action to run after disconnected
     */
    private void doUnmount(final Runnable disconnected) {
        this.disconnect(new Runnable() {
            @Override
            public void run() {
                session.shutdown();
                session = SessionPool.DISCONNECTED;
                cache.clear();
                setWorkdir(null);
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
        final InfoController c = InfoControllerFactory.get(this);
        if(null != c) {
            c.window().close();
        }
        this.background(new DisconnectBackgroundAction(this, session) {
            @Override
            public void cleanup() {
                super.cleanup();
                window.setDocumentEdited(false);
                disconnected.run();
            }
        });
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
            if(!controller.unmount(new DisabledSheetCallback() {
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
    @Override
    public boolean validateMenuItem(final NSMenuItem item) {
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
            item.setState(this.getFilter() instanceof NullFilter ? NSCell.NSOnState : NSCell.NSOffState);
        }
        else if(action.equals(Foundation.selector("encodingMenuClicked:"))) {
            if(this.isMounted()) {
                item.setState(session.getHost().getEncoding().equalsIgnoreCase(
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
        return this.validate(action);
    }

    private boolean validate(final Selector action) {
        return browserToolbarValidator.validate(action);
    }

    @Override
    public boolean validateToolbarItem(final NSToolbarItem item) {
        return browserToolbarValidator.validate(item);
    }

    @Override
    public NSToolbarItem toolbar_itemForItemIdentifier_willBeInsertedIntoToolbar(final NSToolbar toolbar, final String itemIdentifier, boolean inserted) {
        if(log.isDebugEnabled()) {
            log.debug("toolbar_itemForItemIdentifier_willBeInsertedIntoToolbar:" + itemIdentifier);
        }
        return browserToolbarFactory.create(itemIdentifier);
    }

    /**
     * @param toolbar Window toolbar
     * @return The default configuration of toolbar items
     */
    @Override
    public NSArray toolbarDefaultItemIdentifiers(final NSToolbar toolbar) {
        return browserToolbarFactory.getDefault();
    }

    /**
     * @param toolbar Window toolbar
     * @return All available toolbar items
     */
    @Override
    public NSArray toolbarAllowedItemIdentifiers(final NSToolbar toolbar) {
        return browserToolbarFactory.getAllowed();
    }

    @Override
    public NSArray toolbarSelectableItemIdentifiers(NSToolbar toolbar) {
        return NSArray.array();
    }

    /**
     * Overrriden to remove any listeners from the session
     */
    @Override
    public void invalidate() {
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

        browserListColumnsFactory.clear();
        browserOutlineColumnsFactory.clear();
        bookmarkTableColumnFactory.clear();

        quickConnectPopup.setDelegate(null);
        quickConnectPopup.setDataSource(null);

        archiveMenu.setDelegate(null);
        editMenu.setDelegate(null);

        notificationCenter.removeObserver(this.id());

        super.invalidate();
    }

    private final class QuicklookTransferBackgroundAction extends TransferBackgroundAction {
        private final QuickLook quicklook;
        private final List<TransferItem> downloads;

        public QuicklookTransferBackgroundAction(final Controller controller, final QuickLook quicklook, final SessionPool session, final Transfer download,
                                                 final TransferOptions options, final List<TransferItem> downloads) {
            super(controller, session, new TransferAdapter() {
                @Override
                public void progress(final TransferProgress status) {
                    controller.message(status.getProgress());
                }
            }, controller, download, options, new TransferPrompt() {
                @Override
                public TransferAction prompt(final TransferItem item) {
                    return TransferAction.comparison;
                }

                @Override
                public boolean isSelected(final TransferItem file) {
                    return true;
                }

                @Override
                public void message(final String message) {
                    controller.message(message);
                }
            }, new DisabledTransferErrorCallback());
            this.quicklook = quicklook;
            this.downloads = downloads;
        }

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
    }
}
