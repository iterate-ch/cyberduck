package ch.cyberduck.ui.cocoa;

/*
 * Copyright (c) 2002-2010 David Kocher. All rights reserved.
 *
 * http://cyberduck.ch/
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
 *
 * Bug fixes, suggestions and comments should be sent to:
 * dkocher@cyberduck.ch
 */

import ch.cyberduck.core.Preferences;
import ch.cyberduck.ui.cocoa.application.*;
import ch.cyberduck.ui.cocoa.foundation.*;

import org.apache.log4j.Logger;
import org.rococoa.Foundation;
import org.rococoa.Rococoa;
import org.rococoa.cocoa.foundation.NSPoint;
import org.rococoa.cocoa.foundation.NSRect;
import org.rococoa.cocoa.foundation.NSSize;
import org.rococoa.cocoa.foundation.NSUInteger;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * A window controller with a toolbar populated from a tabbed view.
 *
 * @version $Id$
 */
public abstract class ToolbarWindowController extends WindowController implements NSToolbar.Delegate, NSTabView.Delegate {
    private static Logger log = Logger.getLogger(ToolbarWindowController.class);

    protected NSTabView tabView;

    @Override
    public void windowDidBecomeKey(NSNotification notification) {
        this.resize();
        super.windowDidBecomeKey(notification);
    }

    protected abstract List<NSView> getPanels();

    @Override
    public void awakeFromNib() {
        // Insert all panels into tab view
        int i = -1;
        for(NSView panel : this.getPanels()) {
            tabView.tabViewItemAtIndex(++i).setView(panel);
        }
        // Create toolbar item for every tab view
        this.window().setToolbar(this.createToolbar());

        // Change selection to last selected item in preferences
        this.setSelectedTab(Preferences.instance().getInteger(this.getToolbarName() + ".selected"));

        super.awakeFromNib();
    }

    /**
     * Change the toolbar selection and display the tab index.
     *
     * @param tab The index of the tab to be selected
     */
    protected void setSelectedTab(int tab) {
        if(-1 == tab) {
            tab = 0;
        }
        tabView.selectTabViewItemAtIndex(tab);
        NSTabViewItem page = tabView.selectedTabViewItem();
        if(page == null) {
            page = tabView.tabViewItemAtIndex(0);
        }
        toolbar.setSelectedItemIdentifier(page.identifier());
    }

    /**
     * @return The item identifier of the tab selected.
     */
    protected String getSelectedTab() {
        return toolbar.selectedItemIdentifier();
    }

    @Override
    protected void invalidate() {
        toolbar.setDelegate(null);
        tabView.setDelegate(null);
        super.invalidate();
    }

    private String windowTitle;

    @Override
    public void setWindow(NSWindow window) {
        windowTitle = window.title();
        window.setDelegate(this.id());
        window.setShowsToolbarButton(false);
        super.setWindow(window);
//        window.setMinSize(new NSSize(this.getMinWindowWidth(), this.getMinWindowHeight()));
        window.setMaxSize(new NSSize(this.getMaxWindowWidth(), this.getMaxWindowHeight()));
    }

    protected NSToolbar toolbar;

    private NSMutableArray items = NSMutableArray.array();

    protected NSToolbar createToolbar() {
        toolbar = NSToolbar.toolbarWithIdentifier(this.getToolbarName());
        // Set up toolbar properties: Allow customization, give a default display mode, and remember state in user defaults
        toolbar.setAllowsUserCustomization(false);
        toolbar.setSizeMode(this.getToolbarSize());
        toolbar.setDisplayMode(this.getToolbarMode());
        toolbar.setDelegate(this.id());
        for(int i = 0; i < this.getPanels().size(); i++) {
            final NSTabViewItem tab = tabView.tabViewItemAtIndex(i);
            items.insertObject_atIndex(tab.identifier(), new NSUInteger(i));
        }
        return toolbar;
    }

    protected NSUInteger getToolbarSize() {
        return NSToolbar.NSToolbarSizeModeRegular;
    }

    protected NSUInteger getToolbarMode() {
        return NSToolbar.NSToolbarDisplayModeIconAndLabel;
    }

    public void setTabView(NSTabView tabView) {
        this.tabView = tabView;
        this.tabView.setAutoresizingMask(new NSUInteger(NSView.NSViewWidthSizable | NSView.NSViewHeightSizable));
        this.tabView.setDelegate(this.id());
    }

    private String getToolbarName() {
        return this.getBundleName().toLowerCase() + ".toolbar";
    }

    /**
     * Keep reference to weak toolbar items. A toolbar may ask again for a kind of toolbar
     * item already supplied to it, in which case this method may return the same toolbar
     * item it returned before
     */
    private Map<String, NSToolbarItem> cache
            = new HashMap<String, NSToolbarItem>();

    public NSToolbarItem toolbar_itemForItemIdentifier_willBeInsertedIntoToolbar(NSToolbar toolbar, final String itemIdentifier, boolean flag) {
        if(!cache.containsKey(itemIdentifier)) {
            cache.put(itemIdentifier, NSToolbarItem.itemWithIdentifier(itemIdentifier));
        }
        final NSToolbarItem toolbarItem = cache.get(itemIdentifier);
        final NSTabViewItem tab = tabView.tabViewItemAtIndex(tabView.indexOfTabViewItemWithIdentifier(itemIdentifier));
        if(null == tab) {
            log.warn("No tab for toolbar item " + itemIdentifier);
            return null;
        }
        toolbarItem.setLabel(tab.label());
        toolbarItem.setPaletteLabel(tab.label());
        toolbarItem.setTag(tabView.indexOfTabViewItemWithIdentifier(itemIdentifier));
        toolbarItem.setToolTip(tab.label());
        toolbarItem.setImage(IconCache.iconNamed(itemIdentifier, 32));
        toolbarItem.setTarget(this.id());
        toolbarItem.setAction(Foundation.selector("select:"));

        return toolbarItem;
    }

    public NSArray toolbarAllowedItemIdentifiers(NSToolbar toolbar) {
        return items;
    }

    public NSArray toolbarDefaultItemIdentifiers(NSToolbar toolbar) {
        return this.toolbarAllowedItemIdentifiers(toolbar);
    }

    public NSArray toolbarSelectableItemIdentifiers(NSToolbar toolbar) {
        return this.toolbarAllowedItemIdentifiers(toolbar);
    }

    public boolean validateToolbarItem(final NSToolbarItem item) {
        return this.validateTabWithIdentifier(item.itemIdentifier());
    }

    protected boolean validateTabWithIdentifier(String itemIdentifier) {
        return true;
    }

    protected String getTitle(NSTabViewItem item) {
        return item.label();
    }

    public void select(NSToolbarItem sender) {
        tabView.selectTabViewItemAtIndex(sender.tag());
    }

    /**
     *
     */
    private void resize() {
        NSRect windowFrame = NSWindow.contentRectForFrameRect_styleMask(this.window().frame(), this.window().styleMask());

        double height = this.getMinWindowHeight();

        NSRect frameRect = new NSRect(
                new NSPoint(windowFrame.origin.x.doubleValue(), windowFrame.origin.y.doubleValue() + windowFrame.size.height.doubleValue() - height),
                new NSSize(windowFrame.size.width.doubleValue(), height)
        );
        this.window().setFrame_display_animate(NSWindow.frameRectForContentRect_styleMask(frameRect, this.window().styleMask()),
                true, this.window().isVisible());
    }

    public NSSize windowWillResize_toSize(NSWindow window, NSSize newSize) {
        // Only allow horizontal sizing
        return new NSSize(newSize.width.doubleValue(), window.frame().size.height.doubleValue());
    }

    /**
     * @param window
     * @return
     */
    private double toolbarHeightForWindow(NSWindow window) {
        NSRect windowFrame = NSWindow.contentRectForFrameRect_styleMask(this.window().frame(), this.window().styleMask());
        return windowFrame.size.height.doubleValue() - this.window().contentView().frame().size.height.doubleValue();
    }

    public void tabView_didSelectTabViewItem(NSTabView tabView, NSTabViewItem tabViewItem) {
        this.window.setTitle(windowTitle + " – " + this.getTitle(tabViewItem));
        this.resize();
        Preferences.instance().setProperty(this.getToolbarName() + ".selected",
                tabView.indexOfTabViewItem(tabViewItem));
    }

    protected double getMaxWindowHeight() {
        return window.maxSize().height.doubleValue();
    }

    protected double getMaxWindowWidth() {
        return window.maxSize().width.doubleValue();
    }

    protected double getMinWindowHeight() {
        NSRect contentRect = this.getContentRect();
        //Border top + toolbar
        final double height = contentRect.size.height.doubleValue()
                + 40 + this.toolbarHeightForWindow(this.window());
//        log.debug("getMinWindowHeight:" + height);
        return height;
    }

    protected double getMinWindowWidth() {
        NSRect contentRect = this.getContentRect();
        final double width = contentRect.size.width.doubleValue();
//        log.debug("getMinWindowWidth:" + width);
        return width;
    }

    private NSRect getContentRect() {
        NSRect contentRect = new NSRect(0, 0);
        final NSView view = tabView.selectedTabViewItem().view();
        final NSEnumerator enumerator = view.subviews().objectEnumerator();
        NSObject next;
        while(null != (next = enumerator.nextObject())) {
            final NSView subview = Rococoa.cast(next, NSView.class);
            contentRect = FoundationKitFunctionsLibrary.NSUnionRect(contentRect, subview.frame());
        }
        return contentRect;
//        return this.window().contentView().frame();
    }
}
