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

import ch.cyberduck.core.Host;
import ch.cyberduck.core.HostCollection;
import ch.cyberduck.ui.cocoa.BrowserController;
import ch.cyberduck.ui.cocoa.IconCache;
import ch.cyberduck.ui.cocoa.MainController;
import ch.cyberduck.ui.cocoa.application.NSMenu;
import ch.cyberduck.ui.cocoa.application.NSMenuItem;

import org.apache.log4j.Logger;
import org.rococoa.Foundation;
import org.rococoa.cocoa.foundation.NSInteger;

/**
 * @version $Id$
 */
public class BookmarkMenuDelegate extends CollectionMenuDelegate<Host> {
    private static Logger log = Logger.getLogger(BookmarkMenuDelegate.class);

    public BookmarkMenuDelegate() {
        super(HostCollection.defaultCollection());
    }

    public NSInteger numberOfItemsInMenu(NSMenu menu) {
        if(this.isPopulated()) {
            // If you return a negative value, the number of items is left unchanged
            // and menu:updateItem:atIndex:shouldCancel: is not called.
            return new NSInteger(-1);
        }
        return new NSInteger(HostCollection.defaultCollection().size() + 9);
        //index 0-2 are static menu items, 3 is sepeartor, 4 is iDisk with submenu, 5 is History with submenu,
        // 6 is Bonjour with submenu, 7 is sepearator
    }

    private static final int BOOKMARKS_INDEX = 6;

    @Override
    public boolean menuUpdateItemAtIndex(NSMenu menu, NSMenuItem item, NSInteger index, boolean shouldCancel) {
        if(index.intValue() == BOOKMARKS_INDEX) {
            item.setEnabled(true);
            item.setImage(IconCache.iconNamed("history", 16));
        }
        if(index.intValue() == BOOKMARKS_INDEX + 1) {
            item.setEnabled(true);
            item.setImage(IconCache.iconNamed("rendezvous", 16));
        }
        if(index.intValue() > BOOKMARKS_INDEX + 2) {
            Host h = HostCollection.defaultCollection().get(index.intValue() - (BOOKMARKS_INDEX + 3));
            item.setTitle(h.getNickname());
            item.setTarget(this.id());
            item.setImage(IconCache.iconNamed(h.getProtocol().icon(), 16));
            item.setAction(Foundation.selector("bookmarkMenuItemClicked:"));
            item.setRepresentedObject(h.getNickname());
        }
        return super.menuUpdateItemAtIndex(menu, item, index, shouldCancel);
    }

    public void bookmarkMenuItemClicked(final NSMenuItem sender) {
        log.debug("bookmarkMenuItemClicked:" + sender);
        BrowserController controller = MainController.newDocument();
        final int row = HostCollection.defaultCollection().indexOf(sender.representedObject());
        controller.mount(HostCollection.defaultCollection().get(row));
    }
}
