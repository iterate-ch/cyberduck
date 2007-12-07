package ch.cyberduck.ui.cocoa.delegate;

/*
 *  Copyright (c) 2007 David Kocher. All rights reserved.
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

import com.apple.cocoa.application.NSMenu;
import com.apple.cocoa.application.NSMenuItem;
import com.apple.cocoa.foundation.NSBundle;
import com.apple.cocoa.foundation.NSSelector;

import ch.cyberduck.core.Path;
import ch.cyberduck.ui.cocoa.CDBrowserController;
import ch.cyberduck.ui.cocoa.CDIconCache;

import org.apache.log4j.Logger;

/**
 * @version $Id$
 */
public abstract class PathHistoryMenuDelegate extends MenuDelegate {
    private static Logger log = Logger.getLogger(PathHistoryMenuDelegate.class);

    protected CDBrowserController controller;

    public PathHistoryMenuDelegate(CDBrowserController controller) {
        this.controller = controller;
    }

    /**
     * @see com.apple.cocoa.application.NSMenu.Delegate
     */
    public int numberOfItemsInMenu(NSMenu menu) {
        final Path[] history = this.getHistory();
        if(history.length > 0) {
            // The number of history plus a delimiter and the 'Clear' menu
            return history.length + 2;
        }
        return 0;
    }

    public abstract Path[] getHistory();

    /**
     * @see com.apple.cocoa.application.NSMenu.Delegate
     */
    public boolean menuUpdateItemAtIndex(NSMenu menu, NSMenuItem sender, int index, boolean shouldCancel) {
        final Path[] history = this.getHistory();
        if(index < history.length) {
            Path item = (Path) history[index];
            // This is a hack. We insert a new NSMenuItem as NSMenu has
            // a bug caching old entries since we introduced the separator item below
            menu.removeItemAtIndex(index);
            NSMenuItem path = new NSMenuItem();
            path.setTitle(item.getName());
            path.setRepresentedObject(item);
            path.setTarget(this);
            path.setEnabled(true);
            path.setImage(CDIconCache.instance().iconForPath(item, 16));
            path.setAction(new NSSelector("pathMenuItemClicked", new Class[]{NSMenuItem.class}));
            menu.insertItemAtIndex(path, index);
            return !shouldCancel;
        }
        if(index == history.length) {
            menu.removeItemAtIndex(index);
            // There is no way in this wonderful API to add a separator item
            // without creating a new NSMenuItem first
            NSMenuItem separator = new NSMenuItem().separatorItem();
            menu.insertItemAtIndex(separator, index);
            return !shouldCancel;
        }
        if(index == history.length + 1) {
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

    public void pathMenuItemClicked(NSMenuItem sender) {
        controller.setWorkdir((Path) sender.representedObject());
    }

    public abstract void clearMenuItemClicked(NSMenuItem sender);
}