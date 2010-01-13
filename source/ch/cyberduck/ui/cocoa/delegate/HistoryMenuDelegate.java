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

import ch.cyberduck.core.HistoryCollection;
import ch.cyberduck.core.Host;
import ch.cyberduck.core.i18n.Locale;
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
public class HistoryMenuDelegate extends CollectionMenuDelegate<Host> {
    private static Logger log = Logger.getLogger(HistoryMenuDelegate.class);

    public HistoryMenuDelegate() {
        super(HistoryCollection.defaultCollection());
    }

    public NSInteger numberOfItemsInMenu(NSMenu menu) {
        if(this.isPopulated()) {
            // If you return a negative value, the number of items is left unchanged
            // and menu:updateItem:atIndex:shouldCancel: is not called.
            return new NSInteger(-1);
        }
        if(HistoryCollection.defaultCollection().size() > 0) {
            // The number of history plus a delimiter and the 'Clear' menu
            return new NSInteger(HistoryCollection.defaultCollection().size() + 2);
        }
        return new NSInteger(1);
    }

    @Override
    public boolean menuUpdateItemAtIndex(NSMenu menu, NSMenuItem item, NSInteger index, boolean shouldCancel) {
        if(shouldCancel) {
            return false;
        }
        final int size = HistoryCollection.defaultCollection().size();
        if(size == 0) {
            item.setTitle(Locale.localizedString("No recently connected servers available"));
            item.setTarget(null);
            item.setAction(null);
            item.setImage(null);
            item.setEnabled(false);
            return false;
        }
        if(index.intValue() < size) {
            Host h = HistoryCollection.defaultCollection().get(index.intValue());
            item.setTitle(h.getNickname());
            item.setAction(Foundation.selector("historyMenuItemClicked:"));
            item.setRepresentedObject(h.getNickname());
            item.setTarget(this.id());
            item.setEnabled(true);
            item.setImage(IconCache.iconNamed(h.getProtocol().icon(), 16));
            return !shouldCancel;
        }
        if(index.intValue() == size) {
            menu.removeItemAtIndex(index);
            menu.insertItem_atIndex(NSMenuItem.separatorItem(), index);
            return !shouldCancel;
        }
        if(index.intValue() == size + 1) {
            item.setTitle(Locale.localizedString("Clear Menu"));
            item.setAction(Foundation.selector("clearMenuItemClicked:"));
            item.setTarget(this.id());
            item.setEnabled(true);
            return !shouldCancel;
        }
        return super.menuUpdateItemAtIndex(menu, item, index, shouldCancel);
    }

    public void historyMenuItemClicked(NSMenuItem sender) {
        log.debug("historyMenuItemClicked:" + sender);
        BrowserController controller = MainController.newDocument();
        controller.mount(HistoryCollection.defaultCollection().get(
                HistoryCollection.defaultCollection().indexOf(sender.representedObject())
        ));
    }

    public void clearMenuItemClicked(NSMenuItem sender) {
        // Delete all bookmark files
        HistoryCollection.defaultCollection().clear();
    }
}
