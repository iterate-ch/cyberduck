package ch.cyberduck.ui.cocoa.delegate;

/*
 * Copyright (c) 2002-2010 David Kocher. All rights reserved.
 *
 * http://cyberduck.ch/
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * Bug fixes, suggestions and comments should be sent to:
 * dkocher@cyberduck.ch
 */

import ch.cyberduck.core.AbstractPath;
import ch.cyberduck.core.Path;
import ch.cyberduck.core.i18n.Locale;
import ch.cyberduck.ui.cocoa.Action;
import ch.cyberduck.ui.cocoa.IconCache;
import ch.cyberduck.ui.cocoa.TableCellAttributes;
import ch.cyberduck.ui.cocoa.application.*;
import ch.cyberduck.ui.cocoa.foundation.NSArray;
import ch.cyberduck.ui.cocoa.foundation.NSAttributedString;
import ch.cyberduck.ui.cocoa.foundation.NSDictionary;
import ch.cyberduck.ui.cocoa.foundation.NSString;

import org.rococoa.Foundation;
import org.rococoa.Selector;
import org.rococoa.cocoa.foundation.NSInteger;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;

/**
 * @version $Id:$
 */
public abstract class URLMenuDelegate extends AbstractMenuDelegate {
    private static Logger log = Logger.getLogger(URLMenuDelegate.class);

    protected static final NSDictionary URL_FONT_ATTRIBUTES = NSDictionary.dictionaryWithObjectsForKeys(
            NSArray.arrayWithObjects(NSFont.userFontOfSize(NSFont.smallSystemFontSize()), NSColor.darkGrayColor(),
                    TableCellAttributes.PARAGRAPH_STYLE_LEFT_ALIGNMENT_TRUNCATE_MIDDLE),
            NSArray.arrayWithObjects(NSAttributedString.FontAttributeName, NSAttributedString.ForegroundColorAttributeName,
                    NSAttributedString.ParagraphStyleAttributeName)
    );

    protected abstract Path getSelectedPath();

    public NSInteger numberOfItemsInMenu(NSMenu menu) {
        Path path = this.getSelectedPath();
        if(null == path) {
            return new NSInteger(1);
        }
        return new NSInteger(path.getUrls().size() * 2);
    }

    @Override
    public boolean menuUpdateItemAtIndex(NSMenu menu, NSMenuItem item, NSInteger index, boolean cancel) {
        item.setTarget(this.id());
        Path path = this.getSelectedPath();
        item.setTitle(Locale.localizedString("Unknown"));
        if(index.intValue() == 0) {
            item.setKeyEquivalent("c");
            item.setKeyEquivalentModifierMask(NSEvent.NSCommandKeyMask | NSEvent.NSShiftKeyMask);
        }
        else {
            item.setKeyEquivalent("");
            item.setKeyEquivalentModifierMask(0);
        }
        item.setAction(null);
        item.setImage(null);
        if(path != null) {
            AbstractPath.DescriptiveUrl url = path.getUrls().get(index.intValue() / 2);
            item.setRepresentedObject(url.getUrl());
            boolean label = index.intValue() % 2 == 0;
            if(label) {
                item.setEnabled(true);
                item.setAction(Foundation.selector("copyURLClicked:"));
                item.setTitle(url.getHelp());
            }
            else {
                item.setImage(IconCache.iconNamed("site", 16));
                // Dummy menu item to preview URL only
                if(StringUtils.isNotBlank(url.getUrl())) {
                    item.setAttributedTitle(NSAttributedString.attributedStringWithAttributes(url.getUrl(), URL_FONT_ATTRIBUTES));
                }
                else {
                    item.setAttributedTitle(NSAttributedString.attributedStringWithAttributes(Locale.localizedString("Unknown"), URL_FONT_ATTRIBUTES));
                }
            }
        }
        return super.menuUpdateItemAtIndex(menu, item, index, cancel);
    }

    @Action
    public void copyURLClicked(final NSMenuItem sender) {
        this.copy(sender.representedObject());
    }

    /**
     * @param url
     */
    private void copy(String url) {
        if(StringUtils.isNotBlank(url)) {
            NSPasteboard pboard = NSPasteboard.generalPasteboard();
            pboard.declareTypes(NSArray.arrayWithObject(NSString.stringWithString(NSPasteboard.StringPboardType)), null);
            if(!pboard.setStringForType(url, NSPasteboard.StringPboardType)) {
                log.error("Error writing URL to NSPasteboard.StringPboardType.");
            }
        }
        else {
            AppKitFunctions.instance.NSBeep();
        }
    }

    public boolean validateMenuItem(NSMenuItem item) {
        if(null == this.getSelectedPath()) {
            return false;
        }
        final Selector action = item.action();
        if(action.equals(Foundation.selector("copyURLClicked:"))) {
            return StringUtils.isNotBlank(item.representedObject());
        }
        return true;
    }
}