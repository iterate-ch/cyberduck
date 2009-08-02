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

import ch.cyberduck.core.Path;
import ch.cyberduck.ui.cocoa.model.CDPathReference;
import ch.cyberduck.core.i18n.Locale;
import ch.cyberduck.ui.cocoa.CDBrowserController;
import ch.cyberduck.ui.cocoa.CDIconCache;
import ch.cyberduck.ui.cocoa.application.NSMenu;
import ch.cyberduck.ui.cocoa.application.NSMenuItem;

import org.apache.log4j.Logger;
import org.rococoa.Foundation;

import java.util.List;

/**
 * @version $Id$
 */
public abstract class PathHistoryMenuDelegate extends MenuDelegate {
    private static Logger log = Logger.getLogger(PathHistoryMenuDelegate.class);

    protected CDBrowserController controller;

    public PathHistoryMenuDelegate(CDBrowserController controller) {
        this.controller = controller;
    }

    public int numberOfItemsInMenu(NSMenu menu) {
        final List<Path> history = this.getHistory();
        if(history.size() > 0) {
            // The number of history plus a delimiter and the 'Clear' menu
            return history.size() + 2;
        }
        return 0;
    }

    public abstract List<Path> getHistory();

    public boolean menuUpdateItemAtIndex(NSMenu menu, NSMenuItem sender, int index, boolean shouldCancel) {
        final List<Path> history = this.getHistory();
        final int length = history.size();
        if(index < length) {
            Path item = history.get(index);
            // This is a hack. We insert a new NSMenuItem as NSMenu has
            // a bug caching old entries since we introduced the separator item below
            menu.removeItemAtIndex(index);
            NSMenuItem path = NSMenuItem.itemWithTitle(
                    item.getName(), Foundation.selector("pathMenuItemClicked:"), "");
            path.setRepresentedObject(item.getAbsolute());
            path.setTarget(this.id());
            path.setEnabled(true);
            path.setImage(CDIconCache.instance().iconForPath(item, 16));
            menu.insertItem(path, index);
            return !shouldCancel;
        }
        if(index == length) {
            menu.removeItemAtIndex(index);
            // There is no way in this wonderful API to add a separator item
            // without creating a new NSMenuItem first
            NSMenuItem separator = NSMenuItem.separatorItem();
            menu.insertItem(separator, index);
            return !shouldCancel;
        }
        if(index == length + 1) {
            menu.removeItemAtIndex(index);
            NSMenuItem clear = NSMenuItem.itemWithTitle(
                    Locale.localizedString("Clear Menu"), Foundation.selector("clearMenuItemClicked:"), "");
            clear.setTarget(this.id());
            clear.setEnabled(true);
            menu.insertItem(clear, index);
            return !shouldCancel;
        }
        return true;
    }

    public void pathMenuItemClicked(NSMenuItem sender) {
        controller.setWorkdir(controller.lookup(new CDPathReference(sender.representedObject())));
    }

    public abstract void clearMenuItemClicked(NSMenuItem sender);
}