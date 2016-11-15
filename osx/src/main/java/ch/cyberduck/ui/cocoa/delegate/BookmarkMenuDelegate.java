package ch.cyberduck.ui.cocoa.delegate;

/*
 *  Copyright (c) 2006 David Kocher. All rights reserved.
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

import ch.cyberduck.binding.Action;
import ch.cyberduck.binding.Delegate;
import ch.cyberduck.binding.application.NSImage;
import ch.cyberduck.binding.application.NSMenu;
import ch.cyberduck.binding.application.NSMenuItem;
import ch.cyberduck.core.AbstractHostCollection;
import ch.cyberduck.core.BookmarkCollection;
import ch.cyberduck.core.BookmarkNameProvider;
import ch.cyberduck.core.HistoryCollection;
import ch.cyberduck.core.Host;
import ch.cyberduck.core.LocaleFactory;
import ch.cyberduck.core.local.ApplicationLauncherFactory;
import ch.cyberduck.core.preferences.Preferences;
import ch.cyberduck.core.preferences.PreferencesFactory;
import ch.cyberduck.core.resources.IconCacheFactory;
import ch.cyberduck.ui.cocoa.MainController;

import org.apache.log4j.Logger;
import org.rococoa.Foundation;
import org.rococoa.Selector;
import org.rococoa.cocoa.foundation.NSInteger;

public class BookmarkMenuDelegate extends CollectionMenuDelegate<Host> {
    private static final Logger log = Logger.getLogger(BookmarkMenuDelegate.class);

    private static final int BOOKMARKS_INDEX = 8;

    private final Preferences preferences
            = PreferencesFactory.get();

    private final AbstractHostCollection collection;

    private final int index;

    private final MenuCallback callback;

    private final NSMenu historyMenu = NSMenu.menu();

    @Delegate
    private final HistoryMenuDelegate historyMenuDelegate;

    private final NSMenu rendezvousMenu = NSMenu.menu();

    @Delegate
    private final RendezvousMenuDelegate rendezvousMenuDelegate;

    public BookmarkMenuDelegate() {
        this(new HistoryMenuDelegate(), new RendezvousMenuDelegate());
    }

    public BookmarkMenuDelegate(final HistoryMenuDelegate history, final RendezvousMenuDelegate rendezvous) {
        this(new MenuCallback() {
            @Override
            public void selected(final NSMenuItem sender) {
                MainController.newDocument().mount(BookmarkCollection.defaultCollection().lookup(sender.representedObject()));
            }
        }, history, rendezvous);
    }

    public BookmarkMenuDelegate(final MenuCallback callback) {
        this(callback, new HistoryMenuDelegate(), new RendezvousMenuDelegate());
    }

    public BookmarkMenuDelegate(final MenuCallback callback, final HistoryMenuDelegate history, final RendezvousMenuDelegate rendezvous) {
        this(BookmarkCollection.defaultCollection(), BOOKMARKS_INDEX, callback, history, rendezvous);
    }

    public BookmarkMenuDelegate(final AbstractHostCollection collection, final int index,
                                final MenuCallback callback, final HistoryMenuDelegate history, final RendezvousMenuDelegate rendezvous) {
        super(collection);
        this.collection = collection;
        this.index = index;
        this.historyMenuDelegate = history;
        this.rendezvousMenuDelegate = rendezvous;
        this.historyMenu.setDelegate(historyMenuDelegate.id());
        this.rendezvousMenu.setDelegate(rendezvousMenuDelegate.id());
        this.callback = callback;
    }

    @Override
    public NSInteger numberOfItemsInMenu(NSMenu menu) {
        if(this.isPopulated()) {
            // If you return a negative value, the number of items is left unchanged
            // and menu:updateItem:atIndex:shouldCancel: is not called.
            return new NSInteger(-1);
        }
        /**
         * Toogle Bookmarks
         * Sort By
         * ----------------
         * New Bookmark
         * Edit Bookmark
         * Delete Bookmark
         * Duplicate Bookmark
         * ----------------
         * History
         * Bonjour
         * ----------------
         * ...
         */
        return new NSInteger(collection.size() + index + 3);
    }

    @Override
    public Host itemForIndex(final NSInteger row) {
        return collection.get(row.intValue() - (index + 3));
    }

    @Override
    public boolean menuUpdateItemAtIndex(NSMenu menu, NSMenuItem item, NSInteger row, boolean cancel) {
        if(row.intValue() == index) {
            item.setEnabled(true);
            item.setTitle(LocaleFactory.get().localize("History", "Main"));
            item.setImage(IconCacheFactory.<NSImage>get().iconNamed("history.tiff", 16));
            item.setTarget(this.id());
            item.setAction(Foundation.selector("historyMenuClicked:"));
            item.setSubmenu(historyMenu);
        }
        if(row.intValue() == index + 1) {
            item.setEnabled(true);
            item.setTitle(LocaleFactory.get().localize("Bonjour", "Main"));
            item.setImage(IconCacheFactory.<NSImage>get().iconNamed("rendezvous.tiff", 16));
            item.setSubmenu(rendezvousMenu);
        }
        if(row.intValue() == index + 2) {
            menu.removeItemAtIndex(row);
            menu.insertItem_atIndex(this.seperator(), row);
        }
        if(row.intValue() > index + 2) {
            Host h = this.itemForIndex(row);
            item.setTitle(BookmarkNameProvider.toString(h));
            item.setTarget(this.id());
            item.setImage(IconCacheFactory.<NSImage>get().iconNamed(h.getProtocol().icon(), preferences.getInteger("bookmark.menu.icon.size")));
            item.setAction(this.getDefaultAction());
            item.setRepresentedObject(h.getUuid());
        }
        return super.menuUpdateItemAtIndex(menu, item, row, cancel);
    }

    @Action
    public void bookmarkMenuItemClicked(final NSMenuItem sender) {
        if(log.isDebugEnabled()) {
            log.debug(String.format("Menu item clicked %s", sender));
        }
        callback.selected(sender);
    }

    @Action
    public void historyMenuClicked(NSMenuItem sender) {
        ApplicationLauncherFactory.get().open(HistoryCollection.defaultCollection().getFolder());
    }

    @Override
    public Selector getDefaultAction() {
        return Foundation.selector("bookmarkMenuItemClicked:");
    }
}
