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
import ch.cyberduck.binding.BundleController;
import ch.cyberduck.binding.application.NSColor;
import ch.cyberduck.binding.application.NSFont;
import ch.cyberduck.binding.application.NSImage;
import ch.cyberduck.binding.application.NSMenu;
import ch.cyberduck.binding.application.NSMenuItem;
import ch.cyberduck.binding.foundation.NSArray;
import ch.cyberduck.binding.foundation.NSAttributedString;
import ch.cyberduck.binding.foundation.NSDictionary;
import ch.cyberduck.core.AbstractHostCollection;
import ch.cyberduck.core.BookmarkNameProvider;
import ch.cyberduck.core.HistoryCollection;
import ch.cyberduck.core.Host;
import ch.cyberduck.core.LocaleFactory;
import ch.cyberduck.core.UserDateFormatterFactory;
import ch.cyberduck.core.preferences.Preferences;
import ch.cyberduck.core.preferences.PreferencesFactory;
import ch.cyberduck.core.resources.IconCacheFactory;
import ch.cyberduck.ui.cocoa.MainController;

import org.apache.log4j.Logger;
import org.rococoa.Foundation;
import org.rococoa.Selector;
import org.rococoa.cocoa.foundation.NSInteger;

import java.util.Date;

public class HistoryMenuDelegate extends CollectionMenuDelegate<Host> {
    private static final Logger log = Logger.getLogger(HistoryMenuDelegate.class);

    protected static final NSDictionary TIMESTAMP_FONT_ATTRIBUTES = NSDictionary.dictionaryWithObjectsForKeys(
            NSArray.arrayWithObjects(NSFont.userFontOfSize(NSFont.smallSystemFontSize()), NSColor.darkGrayColor(),
                    BundleController.PARAGRAPH_STYLE_LEFT_ALIGNMENT_TRUNCATE_MIDDLE),
            NSArray.arrayWithObjects(NSAttributedString.FontAttributeName, NSAttributedString.ForegroundColorAttributeName,
                    NSAttributedString.ParagraphStyleAttributeName)
    );

    private final AbstractHostCollection collection
            = HistoryCollection.defaultCollection();

    private final MenuCallback callback;

    private final Preferences preferences
            = PreferencesFactory.get();

    public HistoryMenuDelegate() {
        this(new MenuCallback() {
            @Override
            public void selected(final NSMenuItem sender) {
                MainController.newDocument().mount(HistoryCollection.defaultCollection().lookup(sender.representedObject()));
            }
        });
    }

    public HistoryMenuDelegate(final MenuCallback callback) {
        this(HistoryCollection.defaultCollection(), callback);
    }

    public HistoryMenuDelegate(final AbstractHostCollection collection, final MenuCallback callback) {
        super(collection);
        this.callback = callback;
    }

    @Override
    public NSInteger numberOfItemsInMenu(NSMenu menu) {
        if(this.isPopulated()) {
            // If you return a negative value, the number of items is left unchanged
            // and menu:updateItem:atIndex:shouldCancel: is not called.
            return new NSInteger(-1);
        }
        if(collection.size() > 0) {
            // The number of history plus a delimiter and the 'Clear' menu
            return new NSInteger(collection.size() * 2 + 2);
        }
        return new NSInteger(1);
    }

    @Override
    public Host itemForIndex(final NSInteger index) {
        return collection.get(index.intValue() / 2);
    }

    /**
     * @return False if no more updates needed.
     */
    @Override
    public boolean menuUpdateItemAtIndex(NSMenu menu, NSMenuItem item, NSInteger row, boolean cancel) {
        final int size = collection.size();
        if(size == 0) {
            item.setTitle(LocaleFactory.localizedString("No recently connected servers available"));
            item.setTarget(null);
            item.setAction(null);
            item.setImage(null);
            item.setEnabled(false);
            // No more menu updates.
            return false;
        }
        else if(row.intValue() < size * 2) {
            boolean label = row.intValue() % 2 == 0;
            final Host h = this.itemForIndex(row);
            if(label) {
                item.setTitle(BookmarkNameProvider.toString(h));
                item.setTarget(this.id());
                item.setAction(this.getDefaultAction());
                item.setRepresentedObject(h.getUuid());
                item.setEnabled(true);
                item.setImage(IconCacheFactory.<NSImage>get().iconNamed(h.getProtocol().icon(), preferences.getInteger("bookmark.menu.icon.size")));
            }
            else {
                // Dummy menu item with timestamp
                final Date timestamp = h.getTimestamp();
                if(null != timestamp) {
                    item.setAttributedTitle(NSAttributedString.attributedStringWithAttributes(
                            UserDateFormatterFactory.get().getLongFormat(timestamp.getTime()), TIMESTAMP_FONT_ATTRIBUTES));
                }
                else {
                    item.setAttributedTitle(NSAttributedString.attributedStringWithAttributes(
                            LocaleFactory.localizedString("Unknown"), TIMESTAMP_FONT_ATTRIBUTES));
                }
            }
        }
        else if(row.intValue() == size * 2) {
            menu.removeItemAtIndex(row);
            menu.insertItem_atIndex(this.seperator(), row);
        }
        else if(row.intValue() == size * 2 + 1) {
            item.setTitle(LocaleFactory.localizedString("Clear Menu"));
            item.setTarget(this.id());
            item.setAction(Foundation.selector("clearMenuItemClicked:"));
            item.setEnabled(true);
        }
        return super.menuUpdateItemAtIndex(menu, item, row, cancel);
    }

    @Action
    public void menuItemClicked(NSMenuItem sender) {
        if(log.isDebugEnabled()) {
            log.debug(String.format("Menu item clicked %s", sender));
        }
        callback.selected(sender);
    }

    @Action
    public void clearMenuItemClicked(NSMenuItem sender) {
        // Delete all bookmark files
        collection.clear();
    }

    @Override
    public Selector getDefaultAction() {
        return Foundation.selector("menuItemClicked:");
    }
}
