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

import ch.cyberduck.binding.application.NSColor;
import ch.cyberduck.binding.application.NSFont;
import ch.cyberduck.binding.application.NSImage;
import ch.cyberduck.binding.application.NSMenu;
import ch.cyberduck.binding.application.NSMenuItem;
import ch.cyberduck.binding.foundation.NSArray;
import ch.cyberduck.binding.foundation.NSAttributedString;
import ch.cyberduck.binding.foundation.NSDictionary;
import ch.cyberduck.core.BookmarkNameProvider;
import ch.cyberduck.core.HistoryCollection;
import ch.cyberduck.core.Host;
import ch.cyberduck.core.LocaleFactory;
import ch.cyberduck.core.UserDateFormatterFactory;
import ch.cyberduck.core.resources.IconCacheFactory;
import ch.cyberduck.ui.cocoa.Action;
import ch.cyberduck.ui.cocoa.MainController;
import ch.cyberduck.ui.cocoa.TableCellAttributes;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.rococoa.Foundation;
import org.rococoa.Selector;
import org.rococoa.cocoa.foundation.NSInteger;

import java.util.Date;

/**
 * @version $Id$
 */
public class HistoryMenuDelegate extends CollectionMenuDelegate<Host> {
    private static final Logger log = Logger.getLogger(HistoryMenuDelegate.class);

    protected static final NSDictionary TIMESTAMP_FONT_ATTRIBUTES = NSDictionary.dictionaryWithObjectsForKeys(
            NSArray.arrayWithObjects(NSFont.userFontOfSize(NSFont.smallSystemFontSize()), NSColor.darkGrayColor(),
                    TableCellAttributes.PARAGRAPH_STYLE_LEFT_ALIGNMENT_TRUNCATE_MIDDLE),
            NSArray.arrayWithObjects(NSAttributedString.FontAttributeName, NSAttributedString.ForegroundColorAttributeName,
                    NSAttributedString.ParagraphStyleAttributeName)
    );

    private HistoryCollection collection;

    public HistoryMenuDelegate() {
        super(HistoryCollection.defaultCollection());
        collection = HistoryCollection.defaultCollection();
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

    /**
     * @return False if no more updates needed.
     */
    @Override
    public boolean menuUpdateItemAtIndex(NSMenu menu, NSMenuItem item, NSInteger index, boolean cancel) {
        if(cancel) {
            return false;
        }
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
        else if(index.intValue() < size * 2) {
            boolean label = index.intValue() % 2 == 0;
            final Host h = collection.get(index.intValue() / 2);
            if(label) {
                item.setTitle(BookmarkNameProvider.toString(h));
                item.setTarget(this.id());
                item.setAction(this.getDefaultAction());
                item.setRepresentedObject(h.getUuid());
                item.setEnabled(true);
                item.setImage(IconCacheFactory.<NSImage>get().iconNamed(h.getProtocol().icon(), 16));
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
        else if(index.intValue() == size * 2) {
            // How to change an existing item to a separator item?
            item.setTitle(StringUtils.EMPTY);
            item.setTarget(null);
            item.setAction(null);
            item.setImage(null);
            item.setEnabled(false);
        }
        else if(index.intValue() == size * 2 + 1) {
            item.setTitle(LocaleFactory.localizedString("Clear Menu"));
            item.setTarget(this.id());
            item.setAction(Foundation.selector("clearMenuItemClicked:"));
            item.setEnabled(true);
        }
        return super.menuUpdateItemAtIndex(menu, item, index, cancel);
    }

    @Action
    public void menuItemClicked(NSMenuItem sender) {
        if(log.isDebugEnabled()) {
            log.debug(String.format("Menu item clicked %s", sender));
        }
        MainController.newDocument().mount(collection.lookup(sender.representedObject()));
    }

    public void clearMenuItemClicked(NSMenuItem sender) {
        // Delete all bookmark files
        collection.clear();
    }

    @Override
    protected Selector getDefaultAction() {
        return Foundation.selector("menuItemClicked:");
    }
}
