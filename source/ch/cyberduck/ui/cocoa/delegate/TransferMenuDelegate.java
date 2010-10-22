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

import ch.cyberduck.core.Local;
import ch.cyberduck.core.LocalFactory;
import ch.cyberduck.core.Path;
import ch.cyberduck.ui.cocoa.IconCache;
import ch.cyberduck.ui.cocoa.application.NSMenu;
import ch.cyberduck.ui.cocoa.application.NSMenuItem;

import org.rococoa.Foundation;
import org.rococoa.cocoa.foundation.NSInteger;

import org.apache.log4j.Logger;

import java.util.List;

/**
 * @version $Id$
 */
public class TransferMenuDelegate extends AbstractMenuDelegate {
    private static Logger log = Logger.getLogger(TransferMenuDelegate.class);

    /**
     *
     */
    private List<Path> roots;

    public TransferMenuDelegate(List<Path> roots) {
        this.roots = roots;
    }

    public NSInteger numberOfItemsInMenu(NSMenu menu) {
        if(this.isPopulated()) {
            // If you return a negative value, the number of items is left unchanged
            // and menu:updateItem:atIndex:shouldCancel: is not called.
            return new NSInteger(-1);
        }
        return new NSInteger(roots.size());
    }

    @Override
    public boolean menuUpdateItemAtIndex(NSMenu menu, NSMenuItem item, NSInteger index, boolean cancel) {
        final Path path = roots.get(index.intValue());
        item.setTitle(path.getName());
        if(path.getLocal().exists()) {
            item.setEnabled(true);
            item.setTarget(this.id());
            item.setAction(Foundation.selector("reveal:"));
        }
        else {
            item.setEnabled(false);
            item.setTarget(null);
        }
        //item.setState(path.getLocal().exists() ? NSCell.NSOnState : NSCell.NSOffState);
        item.setRepresentedObject(path.getLocal().getAbsolute());
        item.setImage(IconCache.instance().iconForPath(path, 16, false));
        return super.menuUpdateItemAtIndex(menu, item, index, cancel);
    }

    public void reveal(final NSMenuItem sender) {
        Local l = LocalFactory.createLocal(sender.representedObject());
        l.reveal();
    }
}