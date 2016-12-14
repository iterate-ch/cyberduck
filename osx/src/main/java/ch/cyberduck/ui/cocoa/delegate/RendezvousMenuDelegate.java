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

import ch.cyberduck.binding.Action;
import ch.cyberduck.binding.application.NSImage;
import ch.cyberduck.binding.application.NSMenu;
import ch.cyberduck.binding.application.NSMenuItem;
import ch.cyberduck.core.AbstractHostCollection;
import ch.cyberduck.core.BookmarkNameProvider;
import ch.cyberduck.core.Host;
import ch.cyberduck.core.LocaleFactory;
import ch.cyberduck.core.bonjour.RendezvousCollection;
import ch.cyberduck.core.preferences.Preferences;
import ch.cyberduck.core.preferences.PreferencesFactory;
import ch.cyberduck.core.resources.IconCacheFactory;
import ch.cyberduck.ui.cocoa.controller.MainController;

import org.apache.log4j.Logger;
import org.rococoa.Foundation;
import org.rococoa.Selector;
import org.rococoa.cocoa.foundation.NSInteger;

public class RendezvousMenuDelegate extends CollectionMenuDelegate<Host> {
    private static final Logger log = Logger.getLogger(RendezvousMenuDelegate.class);

    private final AbstractHostCollection collection;

    private final MenuCallback callback;

    private final Preferences preferences
            = PreferencesFactory.get();

    public RendezvousMenuDelegate() {
        this(new MenuCallback() {
            @Override
            public void selected(final NSMenuItem sender) {
                MainController.newDocument().mount(RendezvousCollection.defaultCollection().lookup(sender.representedObject()));
            }
        });
    }

    public RendezvousMenuDelegate(final MenuCallback callback) {
        this(RendezvousCollection.defaultCollection(), callback);
    }

    public RendezvousMenuDelegate(final AbstractHostCollection collection, final MenuCallback callback) {
        super(collection);
        this.collection = collection;
        this.callback = callback;
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
            final Host h = this.itemForIndex(index);
            item.setTitle(BookmarkNameProvider.toString(h));
            item.setTarget(this.id());
            item.setEnabled(true);
            item.setImage(IconCacheFactory.<NSImage>get().iconNamed(h.getProtocol().icon(), preferences.getInteger("bookmark.menu.icon.size")));
            item.setTarget(this.id());
            item.setAction(this.getDefaultAction());
            item.setRepresentedObject(h.getUuid());
        }
        return super.menuUpdateItemAtIndex(menu, item, index, cancel);
    }

    @Action
    public void menuItemClicked(NSMenuItem sender) {
        if(log.isDebugEnabled()) {
            log.debug(String.format("Menu item clicked %s", sender));
        }
        callback.selected(sender);
    }

    @Override
    public Selector getDefaultAction() {
        return Foundation.selector("menuItemClicked:");
    }
}
