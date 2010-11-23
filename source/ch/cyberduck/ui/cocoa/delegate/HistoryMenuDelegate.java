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

import ch.cyberduck.core.AbstractHostCollection;
import ch.cyberduck.core.HistoryCollection;
import ch.cyberduck.core.Host;
import ch.cyberduck.core.i18n.Locale;
import ch.cyberduck.ui.DateFormatterFactory;
import ch.cyberduck.ui.cocoa.BrowserController;
import ch.cyberduck.ui.cocoa.IconCache;
import ch.cyberduck.ui.cocoa.MainController;
import ch.cyberduck.ui.cocoa.TableCellAttributes;
import ch.cyberduck.ui.cocoa.application.NSColor;
import ch.cyberduck.ui.cocoa.application.NSFont;
import ch.cyberduck.ui.cocoa.application.NSMenu;
import ch.cyberduck.ui.cocoa.application.NSMenuItem;
import ch.cyberduck.ui.cocoa.foundation.NSArray;
import ch.cyberduck.ui.cocoa.foundation.NSAttributedString;
import ch.cyberduck.ui.cocoa.foundation.NSDictionary;

import org.rococoa.Foundation;
import org.rococoa.Selector;
import org.rococoa.cocoa.foundation.NSInteger;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;

import java.util.Date;

/**
 * @version $Id$
 */
public abstract class HistoryMenuDelegate extends CollectionMenuDelegate<Host> {
    private static Logger log = Logger.getLogger(HistoryMenuDelegate.class);

    protected static final NSDictionary TIMESTAMP_FONT_ATTRIBUTES = NSDictionary.dictionaryWithObjectsForKeys(
            NSArray.arrayWithObjects(NSFont.userFontOfSize(NSFont.smallSystemFontSize()), NSColor.darkGrayColor(),
                    TableCellAttributes.PARAGRAPH_STYLE_LEFT_ALIGNMENT_TRUNCATE_MIDDLE),
            NSArray.arrayWithObjects(NSAttributedString.FontAttributeName, NSAttributedString.ForegroundColorAttributeName,
                    NSAttributedString.ParagraphStyleAttributeName)
    );

    public HistoryMenuDelegate() {
        super(HistoryCollection.defaultCollection());
    }

    @Override
    public NSInteger numberOfItemsInMenu(NSMenu menu) {
        if(this.isPopulated()) {
            // If you return a negative value, the number of items is left unchanged
            // and menu:updateItem:atIndex:shouldCancel: is not called.
            return new NSInteger(-1);
        }
        if(this.collection().size() > 0) {
            // The number of history plus a delimiter and the 'Clear' menu
            return new NSInteger(this.collection().size() * 2 + 2);
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
        final int size = this.collection().size();
        if(size == 0) {
            item.setTitle(Locale.localizedString("No recently connected servers available"));
            item.setTarget(null);
            item.setAction(null);
            item.setImage(null);
            item.setEnabled(false);
            // No more menu updates.
            return false;
        }
        else if(index.intValue() < size * 2) {
            boolean label = index.intValue() % 2 == 0;
            Host h = this.collection().get(index.intValue() / 2);
            if(label) {
                item.setTitle(h.getNickname());
                item.setAction(this.getDefaultAction());
                item.setRepresentedObject(h.getUuid());
                item.setTarget(this.id());
                item.setEnabled(true);
                item.setImage(IconCache.iconNamed(h.getProtocol().icon(), 16));
            }
            else {
                // Dummy menu item with timestamp
                Date timestamp = h.getTimestamp();
                if(null != timestamp) {
                    item.setAttributedTitle(NSAttributedString.attributedStringWithAttributes(
                            DateFormatterFactory.instance().getLongFormat(timestamp.getTime()), TIMESTAMP_FONT_ATTRIBUTES));
                }
                else {
                    item.setAttributedTitle(NSAttributedString.attributedStringWithAttributes(
                            Locale.localizedString("Unknown"), TIMESTAMP_FONT_ATTRIBUTES));
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
            item.setTitle(Locale.localizedString("Clear Menu"));
            item.setAction(Foundation.selector("clearMenuItemClicked:"));
            item.setTarget(this.id());
            item.setEnabled(true);
        }
        return super.menuUpdateItemAtIndex(menu, item, index, cancel);
    }

    public void historyMenuItemClicked(NSMenuItem sender) {
        log.debug("historyMenuItemClicked:" + sender);
        BrowserController controller = MainController.newDocument();
        controller.mount(((AbstractHostCollection) this.collection()).lookup(sender.representedObject()));
    }

    public void clearMenuItemClicked(NSMenuItem sender) {
        // Delete all bookmark files
        this.collection().clear();
    }

    @Override
    protected Selector getDefaultAction() {
        return Foundation.selector("historyMenuItemClicked:");
    }
}
