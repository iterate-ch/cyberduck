package ch.cyberduck.binding;

/*
 * Copyright (c) 2002-2016 iterate GmbH. All rights reserved.
 * https://cyberduck.io/
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
 */

import ch.cyberduck.binding.application.NSColor;
import ch.cyberduck.binding.application.NSFont;
import ch.cyberduck.binding.application.NSMutableParagraphStyle;
import ch.cyberduck.binding.application.NSParagraphStyle;
import ch.cyberduck.binding.application.NSText;
import ch.cyberduck.binding.application.NSTextField;
import ch.cyberduck.binding.application.NSTextView;
import ch.cyberduck.binding.application.NSView;
import ch.cyberduck.binding.foundation.NSArray;
import ch.cyberduck.binding.foundation.NSAttributedString;
import ch.cyberduck.binding.foundation.NSBundle;
import ch.cyberduck.binding.foundation.NSDictionary;
import ch.cyberduck.core.FactoryException;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;

public abstract class BundleController extends ProxyController {
    private static final Logger log = Logger.getLogger(BundleController.class);

    public static final NSMutableParagraphStyle PARAGRAPH_STYLE_LEFT_ALIGNMENT_TRUNCATE_MIDDLE;
    public static final NSMutableParagraphStyle PARAGRAPH_STYLE_LEFT_ALIGNMENT_TRUNCATE_TAIL;
    public static final NSMutableParagraphStyle PARAGRAPH_STYLE_RIGHT_ALIGNMENT_TRUNCATE_TAIL;

    static {
        PARAGRAPH_STYLE_LEFT_ALIGNMENT_TRUNCATE_MIDDLE = NSMutableParagraphStyle.paragraphStyle();
        PARAGRAPH_STYLE_LEFT_ALIGNMENT_TRUNCATE_MIDDLE.setParagraphStyle(NSParagraphStyle.defaultParagraphStyle());
        PARAGRAPH_STYLE_LEFT_ALIGNMENT_TRUNCATE_MIDDLE.setAlignment(NSText.NSLeftTextAlignment);
        PARAGRAPH_STYLE_LEFT_ALIGNMENT_TRUNCATE_MIDDLE.setLineBreakMode(NSParagraphStyle.NSLineBreakByTruncatingMiddle);
    }

    static {
        PARAGRAPH_STYLE_LEFT_ALIGNMENT_TRUNCATE_TAIL = NSMutableParagraphStyle.paragraphStyle();
        PARAGRAPH_STYLE_LEFT_ALIGNMENT_TRUNCATE_TAIL.setParagraphStyle(NSParagraphStyle.defaultParagraphStyle());
        PARAGRAPH_STYLE_LEFT_ALIGNMENT_TRUNCATE_TAIL.setAlignment(NSText.NSLeftTextAlignment);
        PARAGRAPH_STYLE_LEFT_ALIGNMENT_TRUNCATE_TAIL.setLineBreakMode(NSParagraphStyle.NSLineBreakByTruncatingTail);
    }

    static {
        PARAGRAPH_STYLE_RIGHT_ALIGNMENT_TRUNCATE_TAIL = NSMutableParagraphStyle.paragraphStyle();
        PARAGRAPH_STYLE_RIGHT_ALIGNMENT_TRUNCATE_TAIL.setParagraphStyle(NSParagraphStyle.defaultParagraphStyle());
        PARAGRAPH_STYLE_RIGHT_ALIGNMENT_TRUNCATE_TAIL.setAlignment(NSText.NSRightTextAlignment);
        PARAGRAPH_STYLE_RIGHT_ALIGNMENT_TRUNCATE_TAIL.setLineBreakMode(NSParagraphStyle.NSLineBreakByTruncatingTail);
    }

    public static final NSDictionary TRUNCATE_MIDDLE_ATTRIBUTES = NSDictionary.dictionaryWithObjectsForKeys(
            NSArray.arrayWithObject(PARAGRAPH_STYLE_LEFT_ALIGNMENT_TRUNCATE_MIDDLE),
            NSArray.arrayWithObject(NSAttributedString.ParagraphStyleAttributeName)
    );

    public static final NSDictionary LABEL_ATTRIBUTES = NSDictionary.dictionaryWithObjectsForKeys(
            NSArray.arrayWithObject(PARAGRAPH_STYLE_RIGHT_ALIGNMENT_TRUNCATE_TAIL),
            NSArray.arrayWithObject(NSAttributedString.ParagraphStyleAttributeName)
    );

    public static final NSDictionary FIXED_WITH_FONT_ATTRIBUTES = NSDictionary.dictionaryWithObjectsForKeys(
            NSArray.arrayWithObject(NSFont.userFixedPitchFontOfSize(9.0f)),
            NSArray.arrayWithObject(NSAttributedString.FontAttributeName)
    );

    protected static final NSDictionary MENU_HELP_FONT_ATTRIBUTES = NSDictionary.dictionaryWithObjectsForKeys(
            NSArray.arrayWithObjects(NSFont.userFontOfSize(NSFont.smallSystemFontSize()), NSColor.darkGrayColor(),
                    PARAGRAPH_STYLE_LEFT_ALIGNMENT_TRUNCATE_MIDDLE),
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

    protected void updateField(final NSTextView f, final String value) {
        if(null == f) {
            return;
        }
        f.setString(StringUtils.isNotBlank(value) ? value : StringUtils.EMPTY);
    }

    protected void updateField(final NSTextField f, final String value) {
        if(null == f) {
            return;
        }
        f.setStringValue(StringUtils.isNotBlank(value) ? value : StringUtils.EMPTY);
    }

    protected void updateField(final NSTextField f, final String value, final NSDictionary attributes) {
        if(null == f) {
            return;
        }
        f.setAttributedStringValue(NSAttributedString.attributedStringWithAttributes(StringUtils.isNotBlank(value) ? value : StringUtils.EMPTY, attributes));
    }
}
