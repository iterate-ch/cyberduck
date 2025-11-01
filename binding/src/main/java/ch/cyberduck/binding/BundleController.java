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
import ch.cyberduck.core.Factory;
import ch.cyberduck.core.FactoryException;

import ch.cyberduck.core.LocaleFactory;

import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public abstract class BundleController extends ProxyController {
    private static final Logger log = LogManager.getLogger(BundleController.class);

    protected static final String DEFAULT = LocaleFactory.localizedString("Default");

    public static final NSMutableParagraphStyle PARAGRAPH_STYLE_LEFT_ALIGNMENT_TRUNCATE_MIDDLE;
    public static final NSMutableParagraphStyle PARAGRAPH_STYLE_LEFT_ALIGNMENT_TRUNCATE_TAIL;
    public static final NSMutableParagraphStyle PARAGRAPH_STYLE_RIGHT_ALIGNMENT_TRUNCATE_TAIL;

    static {
        PARAGRAPH_STYLE_LEFT_ALIGNMENT_TRUNCATE_MIDDLE = NSMutableParagraphStyle.paragraphStyle();
        PARAGRAPH_STYLE_LEFT_ALIGNMENT_TRUNCATE_MIDDLE.setAlignment(NSText.NSTextAlignmentLeft);
        PARAGRAPH_STYLE_LEFT_ALIGNMENT_TRUNCATE_MIDDLE.setLineBreakMode(NSParagraphStyle.NSLineBreakByTruncatingMiddle);
    }

    static {
        PARAGRAPH_STYLE_LEFT_ALIGNMENT_TRUNCATE_TAIL = NSMutableParagraphStyle.paragraphStyle();
        PARAGRAPH_STYLE_LEFT_ALIGNMENT_TRUNCATE_TAIL.setAlignment(NSText.NSTextAlignmentLeft);
        PARAGRAPH_STYLE_LEFT_ALIGNMENT_TRUNCATE_TAIL.setLineBreakMode(NSParagraphStyle.NSLineBreakByTruncatingTail);
    }

    public static final int TEXT_ALIGNMENT_RIGHT;

    static {
        if(Factory.Platform.osversion.matches("(10|11)\\..*")) {
            TEXT_ALIGNMENT_RIGHT = NSText.NSRightTextAlignment;
        }
        else {
            // Fix #12703 for macOS 12+
            TEXT_ALIGNMENT_RIGHT = NSText.NSTextAlignmentRight;
        }
    }

    public static final int TEXT_ALIGNMENT_CENTER;

    static {
        if(Factory.Platform.osversion.matches("(10|11)\\..*")) {
            TEXT_ALIGNMENT_CENTER = NSText.NSCenterTextAlignment;
        }
        else {
            // Fix #12703 for macOS 12+
            TEXT_ALIGNMENT_CENTER = NSText.NSTextAlignmentCenter;
        }
    }

    static {
        PARAGRAPH_STYLE_RIGHT_ALIGNMENT_TRUNCATE_TAIL = NSMutableParagraphStyle.paragraphStyle();
        PARAGRAPH_STYLE_RIGHT_ALIGNMENT_TRUNCATE_TAIL.setAlignment(TEXT_ALIGNMENT_RIGHT);
        PARAGRAPH_STYLE_RIGHT_ALIGNMENT_TRUNCATE_TAIL.setLineBreakMode(NSParagraphStyle.NSLineBreakByTruncatingTail);
    }

    public static final NSDictionary TRUNCATE_MIDDLE_ATTRIBUTES = NSDictionary.dictionaryWithObjectsForKeys(
        NSArray.arrayWithObject(PARAGRAPH_STYLE_LEFT_ALIGNMENT_TRUNCATE_MIDDLE),
        NSArray.arrayWithObject(NSAttributedString.ParagraphStyleAttributeName)
    );

    public static final NSDictionary TRUNCATE_TAIL_ATTRIBUTES = NSDictionary.dictionaryWithObjectsForKeys(
        NSArray.arrayWithObject(PARAGRAPH_STYLE_RIGHT_ALIGNMENT_TRUNCATE_TAIL),
        NSArray.arrayWithObject(NSAttributedString.ParagraphStyleAttributeName)
    );

    public static final NSDictionary MENU_HELP_FONT_ATTRIBUTES = NSDictionary.dictionaryWithObjectsForKeys(
        NSArray.arrayWithObjects(NSFont.menuFontOfSize(NSFont.smallSystemFontSize()),
            PARAGRAPH_STYLE_LEFT_ALIGNMENT_TRUNCATE_MIDDLE,
            NSColor.secondaryLabelColor()),
        NSArray.arrayWithObjects(NSAttributedString.FontAttributeName,
            NSAttributedString.ParagraphStyleAttributeName,
            NSAttributedString.ForegroundColorAttributeName)
    );

    public void loadBundle() {
        final String bundleName = this.getBundleName();
        if(null == bundleName) {
            log.debug("No bundle to load for controller {}", this.toString());
            return;
        }
        this.loadBundle(bundleName);
    }

    public void loadBundle(final String bundleName) {
        if(awaked) {
            log.warn("Bundle {} already loaded", bundleName);
            return;
        }
        log.info("Loading bundle {}", bundleName);
        // Unarchives the contents of the nib file and links them to a specific owner object
        if(!NSBundle.loadNibNamed(bundleName, this.id())) {
            throw new FactoryException(String.format("Failure loading %s.xib", bundleName));
        }
        if(!awaked) {
            this.awakeFromNib();
        }
    }

    /**
     * After loading the NIB, awakeFromNib from NSNibLoading protocol was called.
     * Not the case on 10.6 because the method is implemented by NSObject.
     */
    protected boolean awaked;

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
    public NSView view() {
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
