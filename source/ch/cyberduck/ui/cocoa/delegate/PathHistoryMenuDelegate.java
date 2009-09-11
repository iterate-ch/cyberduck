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
import org.rococoa.cocoa.foundation.NSInteger;

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

    public NSInteger numberOfItemsInMenu(NSMenu menu) {
        final List<Path> history = this.getHistory();
        if(history.size() > 0) {
            // The number of history plus a delimiter and the 'Clear' menu
            return new NSInteger(history.size() + 2);
        }
        return new NSInteger(0);
    }

    public abstract List<Path> getHistory();

    public boolean menuUpdateItemAtIndex(NSMenu menu, NSMenuItem sender, NSInteger index, boolean shouldCancel) {
        final List<Path> history = this.getHistory();
        final int length = history.size();
        if(index.intValue() < length) {
            Path item = history.get(index.intValue());
            // This is a hack. We insert a new NSMenuItem as NSMenu has
            // a bug caching old entries since we introduced the separator item below
            menu.removeItemAtIndex(index);
            NSMenuItem path = menu.insertItemWithTitle_action_keyEquivalent_atIndex(
                    item.getName(), Foundation.selector("pathMenuItemClicked:"), "", index);
            path.setRepresentedObject(item.getAbsolute());
            path.setTarget(this.id());
            path.setEnabled(true);
            path.setImage(CDIconCache.instance().iconForPath(item, 16));
            return !shouldCancel;
        }
        if(index.intValue() == length) {
            menu.removeItemAtIndex(index);
            menu.insertItem_atIndex(NSMenuItem.separatorItem(), index);
            return !shouldCancel;
        }
        if(index.intValue() == length + 1) {
            menu.removeItemAtIndex(index);
            NSMenuItem clear = menu.insertItemWithTitle_action_keyEquivalent_atIndex(
                    Locale.localizedString("Clear Menu"), Foundation.selector("clearMenuItemClicked:"), "", index);
            clear.setTarget(this.id());
            clear.setEnabled(true);
            return !shouldCancel;
        }
        return true;
    }

    public void pathMenuItemClicked(NSMenuItem sender) {
        controller.setWorkdir(controller.lookup(new CDPathReference(sender.representedObject())));
    }

    public abstract void clearMenuItemClicked(NSMenuItem sender);
}