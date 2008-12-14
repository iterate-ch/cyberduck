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

import com.apple.cocoa.application.NSApplication;
import com.apple.cocoa.application.NSMenu;
import com.apple.cocoa.application.NSMenuItem;
import com.apple.cocoa.foundation.NSBundle;
import com.apple.cocoa.foundation.NSSelector;

import ch.cyberduck.core.HistoryCollection;
import ch.cyberduck.core.Host;
import ch.cyberduck.ui.cocoa.CDBrowserController;
import ch.cyberduck.ui.cocoa.CDIconCache;
import ch.cyberduck.ui.cocoa.CDMainController;

import org.apache.log4j.Logger;

/**
 * @version $Id$
 */
public class HistoryMenuDelegate extends MenuDelegate {
    private static Logger log = Logger.getLogger(HistoryMenuDelegate.class);

    /**
     * @see com.apple.cocoa.application.NSMenu.Delegate
     */
    public int numberOfItemsInMenu(NSMenu menu) {
        if(HistoryCollection.defaultCollection().size() > 0) {
            // The number of history plus a delimiter and the 'Clear' menu
            return HistoryCollection.defaultCollection().size() + 2;
        }
        return 1;
    }

    /**
     * @see com.apple.cocoa.application.NSMenu.Delegate
     */
    public boolean menuUpdateItemAtIndex(NSMenu menu, NSMenuItem sender, int index, boolean shouldCancel) {
        if(HistoryCollection.defaultCollection().size() == 0) {
            sender.setTitle(NSBundle.localizedString("No recently connected servers available", ""));
            sender.setTarget(null);
            sender.setAction(null);
            sender.setImage(null);
            sender.setEnabled(false);
            return false;
        }
        if(index < HistoryCollection.defaultCollection().size()) {
            Host h = HistoryCollection.defaultCollection().get(index);
            // This is a hack. We insert a new NSMenuItem as NSMenu has
            // a bug caching old entries since we introduced the separator item below
            menu.removeItemAtIndex(index);
            NSMenuItem bookmark = new NSMenuItem();
            bookmark.setTitle(h.getNickname());
            bookmark.setRepresentedObject(h);
            bookmark.setTarget(this);
            bookmark.setEnabled(true);
            bookmark.setImage(CDIconCache.instance().iconForName("cyberduck-document", 16));
            bookmark.setAction(new NSSelector("historyMenuItemClicked", new Class[]{NSMenuItem.class}));
            menu.insertItemAtIndex(bookmark, index);
            return !shouldCancel;
        }
        if(index == HistoryCollection.defaultCollection().size()) {
            menu.removeItemAtIndex(index);
            // There is no way in this wonderful API to add a separator item
            // without creating a new NSMenuItem first
            NSMenuItem separator = new NSMenuItem().separatorItem();
            menu.insertItemAtIndex(separator, index);
            return !shouldCancel;
        }
        if(index == HistoryCollection.defaultCollection().size() + 1) {
            menu.removeItemAtIndex(index);
            NSMenuItem clear = new NSMenuItem();
            clear.setTitle(NSBundle.localizedString("Clear Menu", ""));
            clear.setTarget(this);
            clear.setEnabled(true);
            clear.setAction(new NSSelector("clearMenuItemClicked", new Class[]{NSMenuItem.class}));
            menu.insertItemAtIndex(clear, index);
            return !shouldCancel;
        }
        return true;
    }

    public void historyMenuItemClicked(NSMenuItem sender) {
        CDBrowserController controller
                = ((CDMainController) NSApplication.sharedApplication().delegate()).newDocument();
        controller.mount((Host) sender.representedObject());
    }

    public void clearMenuItemClicked(NSMenuItem sender) {
        // Delete all bookmark files
        HistoryCollection.defaultCollection().clear();
    }
}
