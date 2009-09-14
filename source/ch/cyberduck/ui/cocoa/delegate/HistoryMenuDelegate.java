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
import ch.cyberduck.ui.cocoa.CDBrowserController;
import ch.cyberduck.ui.cocoa.CDIconCache;
import ch.cyberduck.ui.cocoa.CDMainController;
import ch.cyberduck.ui.cocoa.application.NSMenu;
import ch.cyberduck.ui.cocoa.application.NSMenuItem;

import org.apache.log4j.Logger;
import org.rococoa.Foundation;
import org.rococoa.cocoa.foundation.NSInteger;

/**
 * @version $Id$
 */
public class HistoryMenuDelegate extends MenuDelegate {
    private static Logger log = Logger.getLogger(HistoryMenuDelegate.class);

    public NSInteger numberOfItemsInMenu(NSMenu menu) {
        if(HistoryCollection.defaultCollection().size() > 0) {
            // The number of history plus a delimiter and the 'Clear' menu
            return new NSInteger(HistoryCollection.defaultCollection().size() + 2);
        }
        return new NSInteger(1);
    }

    @Override
    public boolean menuUpdateItemAtIndex(NSMenu menu, NSMenuItem sender, NSInteger index, boolean shouldCancel) {
        if(HistoryCollection.defaultCollection().size() == 0) {
            sender.setTitle(Locale.localizedString("No recently connected servers available"));
            sender.setTarget(null);
            sender.setAction(null);
            sender.setImage(null);
            sender.setEnabled(false);
            return false;
        }
        if(index.intValue() < HistoryCollection.defaultCollection().size()) {
            Host h = HistoryCollection.defaultCollection().get(index.intValue());
            // This is a hack. We insert a new NSMenuItem as NSMenu has
            // a bug caching old entries since we introduced the separator item below
            menu.removeItemAtIndex(index);
            NSMenuItem bookmark = menu.insertItemWithTitle_action_keyEquivalent_atIndex(
                    h.getNickname(), Foundation.selector("historyMenuItemClicked:"), "", index);
            bookmark.setRepresentedObject(h.getNickname());
            bookmark.setTarget(this.id());
            bookmark.setEnabled(true);
            bookmark.setImage(CDIconCache.iconNamed(h.getProtocol().icon(), 16));
            return !shouldCancel;
        }
        if(index.intValue() == HistoryCollection.defaultCollection().size()) {
            menu.removeItemAtIndex(index);
            menu.insertItem_atIndex(NSMenuItem.separatorItem(), index);
            return !shouldCancel;
        }
        if(index.intValue() == HistoryCollection.defaultCollection().size() + 1) {
            menu.removeItemAtIndex(index);
            NSMenuItem clear = menu.insertItemWithTitle_action_keyEquivalent_atIndex(Locale.localizedString("Clear Menu"),
                    Foundation.selector("clearMenuItemClicked:"), "", index);
            clear.setTarget(this.id());
            clear.setEnabled(true);
            return !shouldCancel;
        }
        return true;
    }

    public void historyMenuItemClicked(NSMenuItem sender) {
        log.debug("historyMenuItemClicked:" + sender);
        CDBrowserController controller = CDMainController.newDocument();
        controller.mount(HistoryCollection.defaultCollection().get(
                HistoryCollection.defaultCollection().indexOf(sender.representedObject())
        ));
    }

    public void clearMenuItemClicked(NSMenuItem sender) {
        // Delete all bookmark files
        HistoryCollection.defaultCollection().clear();
    }
}
