package ch.cyberduck.ui.cocoa.delegate;

/*
 *  Copyright (c) 2008 David Kocher. All rights reserved.
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

import ch.cyberduck.binding.application.NSMenu;
import ch.cyberduck.binding.application.NSMenuItem;
import ch.cyberduck.core.Archive;

import org.rococoa.Foundation;
import org.rococoa.Selector;
import org.rococoa.cocoa.foundation.NSInteger;

public class ArchiveMenuDelegate extends AbstractMenuDelegate {

    @Override
    public NSInteger numberOfItemsInMenu(NSMenu menu) {
        if(this.isPopulated()) {
            // If you return a negative value, the number of items is left unchanged
            // and menu:updateItem:atIndex:shouldCancel: is not called.
            return new NSInteger(-1);
        }
        return new NSInteger(Archive.getKnownArchives().length);
    }

    @Override
    public boolean menuUpdateItemAtIndex(NSMenu menu, NSMenuItem item, NSInteger index, boolean cancel) {
        final Archive archive = Archive.getKnownArchives()[index.intValue()];
        item.setRepresentedObject(archive.getIdentifier());
        item.setTitle(archive.getIdentifier());
        item.setAction(this.getDefaultAction());
        return super.menuUpdateItemAtIndex(menu, item, index, cancel);
    }

    @Override
    public Selector getDefaultAction() {
        return Foundation.selector("archiveMenuClicked:");
    }
}
