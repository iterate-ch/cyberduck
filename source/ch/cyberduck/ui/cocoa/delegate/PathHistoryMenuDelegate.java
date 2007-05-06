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
import ch.cyberduck.ui.cocoa.CDBrowserController;

import com.apple.cocoa.application.NSImage;
import com.apple.cocoa.application.NSMenu;
import com.apple.cocoa.application.NSMenuItem;
import com.apple.cocoa.foundation.NSBundle;
import com.apple.cocoa.foundation.NSSelector;
import com.apple.cocoa.foundation.NSSize;

import org.apache.log4j.Logger;

import java.io.Serializable;

/**
 * @version $Id$
 */
public class PathHistoryMenuDelegate /*extends MenuDelegate */{
    private static Logger log = Logger.getLogger(PathHistoryMenuDelegate.class);

    private CDBrowserController controller;

    public PathHistoryMenuDelegate(CDBrowserController controller) {
        this.controller = controller;
    }

    private final NSImage FOLDER_ICON_SMALL;

    {
        FOLDER_ICON_SMALL = NSImage.imageNamed("folder16.tiff");
        FOLDER_ICON_SMALL.setScalesWhenResized(true);
        FOLDER_ICON_SMALL.setSize(new NSSize(16f, 16f));
    }

    /**
     * @see com.apple.cocoa.application.NSMenu.Delegate
     */
    public int numberOfItemsInMenu(NSMenu menu) {
        if(controller.getFullHistory().length > 0) {
            // The number of history plus a delimiter and the 'Clear' menu
            return controller.getFullHistory().length + 2;
        }
        return 0;
    }

    /**
     * @see com.apple.cocoa.application.NSMenu.Delegate
     */
    public boolean menuUpdateItemAtIndex(NSMenu menu, NSMenuItem sender, int index, boolean shouldCancel) {
        if(index < controller.getFullHistory().length) {
            Path item = (Path) controller.getFullHistory()[index];
            // This is a hack. We insert a new NSMenuItem as NSMenu has
            // a bug caching old entries since we introduced the separator item below
            menu.removeItemAtIndex(index);
            NSMenuItem path = new NSMenuItem();
            path.setTitle(item.getName());
            path.setRepresentedObject(item);
            path.setTarget(this);
            path.setEnabled(true);
            path.setImage(FOLDER_ICON_SMALL);
            path.setAction(new NSSelector("pathMenuItemClicked", new Class[]{NSMenuItem.class}));
            menu.insertItemAtIndex(path, index);
            return !shouldCancel;
        }
        if(index == controller.getFullHistory().length) {
            menu.removeItemAtIndex(index);
            // There is no way in this wonderful API to add a separator item
            // without creating a new NSMenuItem first
            NSMenuItem separator = new NSMenuItem().separatorItem();
            menu.insertItemAtIndex(separator, index);
            return !shouldCancel;
        }
        if(index == controller.getFullHistory().length + 1) {
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
        controller.setWorkdir((Path)sender.representedObject());
    }

    public void clearMenuItemClicked(NSMenuItem sender) {
        controller.getSession().cache().clear();
    }
}