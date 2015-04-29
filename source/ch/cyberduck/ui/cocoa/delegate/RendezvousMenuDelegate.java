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

import ch.cyberduck.binding.application.NSImage;
import ch.cyberduck.binding.application.NSMenu;
import ch.cyberduck.binding.application.NSMenuItem;
import ch.cyberduck.core.BookmarkNameProvider;
import ch.cyberduck.core.Host;
import ch.cyberduck.core.LocaleFactory;
import ch.cyberduck.core.bonjour.RendezvousCollection;
import ch.cyberduck.core.resources.IconCacheFactory;
import ch.cyberduck.ui.cocoa.MainController;

import org.apache.log4j.Logger;
import org.rococoa.Foundation;
import org.rococoa.Selector;
import org.rococoa.cocoa.foundation.NSInteger;

/**
 * @version $Id$
 */
public class RendezvousMenuDelegate extends CollectionMenuDelegate<Host> {
    private static final Logger log = Logger.getLogger(RendezvousMenuDelegate.class);

    private RendezvousCollection collection;

    public RendezvousMenuDelegate() {
        super(RendezvousCollection.defaultCollection());
        collection = RendezvousCollection.defaultCollection();
    }

    @Override
    public boolean menuUpdateItemAtIndex(NSMenu menu, NSMenuItem item, NSInteger index, boolean cancel) {
        if(collection.size() == 0) {
            item.setTitle(LocaleFactory.localizedString("No Bonjour services available"));
            item.setTarget(null);
            item.setAction(null);
            item.setImage(null);
            item.setEnabled(false);
        }
        else {
            final Host h = collection.get(index.intValue());
            item.setTitle(BookmarkNameProvider.toString(h));
            item.setTarget(this.id());
            item.setEnabled(true);
            item.setImage(IconCacheFactory.<NSImage>get().iconNamed(h.getProtocol().icon(), 16));
            item.setTarget(this.id());
            item.setAction(this.getDefaultAction());
            item.setRepresentedObject(h.getUuid());
        }
        return super.menuUpdateItemAtIndex(menu, item, index, cancel);
    }

    public void menuItemClicked(NSMenuItem sender) {
        if(log.isDebugEnabled()) {
            log.debug(String.format("Menu item clicked %s", sender));
        }
        MainController.newDocument().mount(collection.lookup(sender.representedObject()));
    }

    @Override
    protected Selector getDefaultAction() {
        return Foundation.selector("menuItemClicked:");
    }
}
