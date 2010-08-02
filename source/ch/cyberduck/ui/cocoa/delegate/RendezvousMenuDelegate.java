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

import ch.cyberduck.core.Host;
import ch.cyberduck.core.RendezvousCollection;
import ch.cyberduck.core.i18n.Locale;
import ch.cyberduck.ui.cocoa.BrowserController;
import ch.cyberduck.ui.cocoa.IconCache;
import ch.cyberduck.ui.cocoa.MainController;
import ch.cyberduck.ui.cocoa.application.NSMenu;
import ch.cyberduck.ui.cocoa.application.NSMenuItem;

import org.rococoa.Foundation;
import org.rococoa.cocoa.foundation.NSInteger;

import org.apache.log4j.Logger;

/**
 * @version $Id$
 */
public class RendezvousMenuDelegate extends CollectionMenuDelegate<Host> {
    private static Logger log = Logger.getLogger(RendezvousMenuDelegate.class);

    public RendezvousMenuDelegate() {
        super(RendezvousCollection.defaultCollection());
    }

    public NSInteger numberOfItemsInMenu(NSMenu menu) {
        if(this.isPopulated()) {
            // If you return a negative value, the number of items is left unchanged
            // and menu:updateItem:atIndex:shouldCancel: is not called.
            return new NSInteger(-1);
        }
        if(RendezvousCollection.defaultCollection().size() > 0) {
            // The number of history plus a delimiter and the 'Clear' menu
            return new NSInteger(RendezvousCollection.defaultCollection().size());
        }
        return new NSInteger(1);
    }

    @Override
    public boolean menuUpdateItemAtIndex(NSMenu menu, NSMenuItem item, NSInteger index, boolean cancel) {
        if(RendezvousCollection.defaultCollection().size() == 0) {
            item.setTitle(Locale.localizedString("No Bonjour services available"));
            item.setEnabled(false);
        }
        else {
            final Host h = RendezvousCollection.defaultCollection().get(index.intValue());
            item.setTitle(h.getNickname());
            item.setTarget(this.id());
            item.setEnabled(true);
            item.setImage(IconCache.iconNamed(h.getProtocol().icon(), 16));
            item.setAction(Foundation.selector("rendezvousMenuClicked:"));
            item.setRepresentedObject(h.getUuid());
        }
        return super.menuUpdateItemAtIndex(menu, item, index, cancel);
    }

    public void rendezvousMenuClicked(NSMenuItem sender) {
        log.debug("rendezvousMenuClicked:" + sender);
        BrowserController controller = MainController.newDocument();
        controller.mount(RendezvousCollection.defaultCollection().lookup(sender.representedObject()));
    }
}
