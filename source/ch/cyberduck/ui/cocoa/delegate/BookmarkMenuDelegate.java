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
import com.apple.cocoa.foundation.NSSelector;

import ch.cyberduck.core.Host;
import ch.cyberduck.core.HostCollection;
import ch.cyberduck.ui.cocoa.CDBrowserController;
import ch.cyberduck.ui.cocoa.CDIconCache;
import ch.cyberduck.ui.cocoa.CDMainController;

import org.apache.log4j.Logger;

/**
 * @version $Id$
 */
public class BookmarkMenuDelegate extends MenuDelegate {
    private static Logger log = Logger.getLogger(BookmarkMenuDelegate.class);

    /**
     * @see com.apple.cocoa.application.NSMenu.Delegate
     */
    public int numberOfItemsInMenu(NSMenu menu) {
        return HostCollection.defaultCollection().size() + 10;
        //index 0-2 are static menu items, 3 is sepeartor, 4 is iDisk with submenu, 5 is History with submenu,
        // 6 is Bonjour with submenu, 7 is sepearator
    }

    /**
     * Called to let you update a menu item before it is displayed. If your
     * numberOfItemsInMenu delegate method returns a positive value,
     * then your menuUpdateItemAtIndex method is called for each item in the menu.
     * You can then update the menu title, image, and so forth for the menu item.
     * Return true to continue the process. If you return false, your menuUpdateItemAtIndex
     * is not called again. In that case, it is your responsibility to trim any extra items from the menu.
     *
     * @see com.apple.cocoa.application.NSMenu.Delegate
     */
    public boolean menuUpdateItemAtIndex(NSMenu menu, NSMenuItem item, int index, boolean shouldCancel) {
        if(index >= this.numberOfItemsInMenu(menu)) {
            log.warn("Invalid index in menuUpdateItemAtIndex:" + index);
            return false;
        }
        if(index == 6) {
            item.setEnabled(true);
            item.setImage(CDIconCache.instance().iconForName("me", 16));
        }
        if(index == 7) {
            item.setEnabled(true);
            item.setImage(CDIconCache.instance().iconForName("history", 16));
        }
        if(index == 8) {
            item.setEnabled(true);
            item.setImage(CDIconCache.instance().iconForName("rendezvous", 16));
        }
        if(index > 9) {
            Host h = HostCollection.defaultCollection().get(index - 10);
            item.setTitle(h.getNickname());
            item.setTarget(this);
            item.setImage(CDIconCache.instance().iconForName("cyberduck-document", 16));
            item.setAction(new NSSelector("bookmarkMenuItemClicked", new Class[]{Object.class}));
            item.setRepresentedObject(h);
        }
        return true;
    }

    public void bookmarkMenuItemClicked(final NSMenuItem sender) {
        log.debug("bookmarkMenuItemClicked:" + sender);
        CDBrowserController controller
                = ((CDMainController) (NSApplication.sharedApplication().delegate())).newDocument();
        controller.mount((Host) sender.representedObject());
    }
}
