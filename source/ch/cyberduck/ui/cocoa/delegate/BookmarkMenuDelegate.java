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
import ch.cyberduck.core.resources.IconCacheFactory;
import ch.cyberduck.ui.cocoa.Action;
import ch.cyberduck.ui.cocoa.Delegate;
import ch.cyberduck.ui.cocoa.MainController;

import org.apache.log4j.Logger;
import org.rococoa.Foundation;
import org.rococoa.Selector;
import org.rococoa.cocoa.foundation.NSInteger;

/**
 * @version $Id$
 */
public class BookmarkMenuDelegate extends CollectionMenuDelegate<Host> {
    private static final Logger log = Logger.getLogger(BookmarkMenuDelegate.class);

    private static final int BOOKMARKS_INDEX = 8;

    private AbstractHostCollection collection;

    private int index;

    private NSMenu historyMenu = NSMenu.menu();

    @Delegate
    private HistoryMenuDelegate historyMenuDelegate
            = new HistoryMenuDelegate();

    private NSMenu rendezvousMenu = NSMenu.menu();

    @Delegate
    private RendezvousMenuDelegate rendezvousMenuDelegate
            = new RendezvousMenuDelegate();

    public BookmarkMenuDelegate() {
        this(BookmarkCollection.defaultCollection(), BOOKMARKS_INDEX);
    }

    public BookmarkMenuDelegate(final AbstractHostCollection collection, final int index) {
        super(collection);
        this.index = index;
        this.collection = collection;
        this.historyMenu.setDelegate(historyMenuDelegate.id());
        this.rendezvousMenu.setDelegate(rendezvousMenuDelegate.id());
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
            menu.insertItem_atIndex(NSMenuItem.separatorItem(), row);
        }
        if(row.intValue() > index + 2) {
            Host h = collection.get(row.intValue() - (index + 3));
            item.setTitle(BookmarkNameProvider.toString(h));
            item.setTarget(this.id());
            item.setImage(IconCacheFactory.<NSImage>get().iconNamed(h.getProtocol().icon(), 16));
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
        MainController.newDocument().mount(collection.lookup(sender.representedObject()));
    }

    @Action
    public void historyMenuClicked(NSMenuItem sender) {
        ApplicationLauncherFactory.get().open(HistoryCollection.defaultCollection().getFolder());
    }

    @Override
    protected Selector getDefaultAction() {
        return Foundation.selector("bookmarkMenuItemClicked:");
    }
}
