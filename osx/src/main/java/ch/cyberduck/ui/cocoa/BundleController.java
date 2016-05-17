package ch.cyberduck.ui.cocoa;

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

import ch.cyberduck.binding.application.NSAlert;
import ch.cyberduck.binding.application.NSColor;
import ch.cyberduck.binding.application.NSFont;
import ch.cyberduck.binding.application.NSView;
import ch.cyberduck.binding.foundation.NSArray;
import ch.cyberduck.binding.foundation.NSAttributedString;
import ch.cyberduck.binding.foundation.NSBundle;
import ch.cyberduck.binding.foundation.NSDictionary;
import ch.cyberduck.core.FactoryException;

import org.apache.log4j.Logger;

public abstract class BundleController extends ProxyController {
    private static Logger log = Logger.getLogger(BundleController.class);

    public static final NSDictionary TRUNCATE_MIDDLE_ATTRIBUTES = NSDictionary.dictionaryWithObjectsForKeys(
            NSArray.arrayWithObject(TableCellAttributes.PARAGRAPH_STYLE_LEFT_ALIGNMENT_TRUNCATE_MIDDLE),
            NSArray.arrayWithObject(NSAttributedString.ParagraphStyleAttributeName)
    );

    public static final NSDictionary FIXED_WITH_FONT_ATTRIBUTES = NSDictionary.dictionaryWithObjectsForKeys(
            NSArray.arrayWithObject(NSFont.userFixedPitchFontOfSize(9.0f)),
            NSArray.arrayWithObject(NSAttributedString.FontAttributeName)
    );

    protected static final NSDictionary MENU_HELP_FONT_ATTRIBUTES = NSDictionary.dictionaryWithObjectsForKeys(
            NSArray.arrayWithObjects(NSFont.userFontOfSize(NSFont.smallSystemFontSize()), NSColor.darkGrayColor(),
                    TableCellAttributes.PARAGRAPH_STYLE_LEFT_ALIGNMENT_TRUNCATE_MIDDLE),
            NSArray.arrayWithObjects(NSAttributedString.FontAttributeName, NSAttributedString.ForegroundColorAttributeName,
                    NSAttributedString.ParagraphStyleAttributeName)
    );

    public void loadBundle() {
        final String bundleName = this.getBundleName();
        if(null == bundleName) {
            log.debug(String.format("No bundle to load for controller %s", this.toString()));
            return;
        }
        this.loadBundle(bundleName);
    }

    public void loadBundle(final String bundleName) {
        if(awaked) {
            log.warn(String.format("Bundle %s already loaded", bundleName));
            return;
        }
        if(log.isInfoEnabled()) {
            log.info(String.format("Loading bundle %s", bundleName));
        }
        // Unarchives the contents of the nib file and links them to a specific owner object
        if(!NSBundle.loadNibNamed(bundleName, this.id())) {
            throw new FactoryException(String.format("Couldn't load %s.nib", bundleName));
        }
        if(!awaked) {
            this.awakeFromNib();
        }
    }

    /**
     * After loading the NIB, awakeFromNib from NSNibLoading protocol was called.
     * Not the case on 10.6 because the method is implemented by NSObject.
     */
    private boolean awaked;

    /**
     * Called by the runtime after the NIB file has been loaded sucessfully
     */
    public void awakeFromNib() {
        log.debug("awakeFromNib");
        awaked = true;
    }

    /**
     * @return The top level view object or null if unknown
     */
    protected NSView view() {
        return null;
    }

    protected abstract String getBundleName();

    public int alert(final NSAlert alert) {
        return alert.runModal();
    }
}
