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
import ch.cyberduck.binding.foundation.NSAttributedString;
import ch.cyberduck.binding.foundation.NSMutableAttributedString;
import ch.cyberduck.binding.foundation.NSNumber;
import ch.cyberduck.binding.foundation.NSRange;
import ch.cyberduck.core.DescriptiveUrl;
import ch.cyberduck.core.Local;

import org.apache.commons.lang3.StringUtils;
import org.rococoa.cocoa.foundation.NSUInteger;

/**
 * From http://developer.apple.com/qa/qa2006/qa1487.html
 */
public final class HyperlinkAttributedStringFactory {

    private HyperlinkAttributedStringFactory() {
        super();
    }

    public static NSAttributedString create(final DescriptiveUrl url) {
        if(url.equals(DescriptiveUrl.EMPTY)) {
            return NSAttributedString.attributedString(StringUtils.EMPTY);
        }
        return create(url.getUrl());
    }

    /**
     * @param url URL
     * @return Clickable and underlined string to put into textfield.
     */
    public static NSAttributedString create(final String url) {
        if(null == url) {
            return NSAttributedString.attributedString(StringUtils.EMPTY);
        }
        return create(url, url);
    }

    public static NSAttributedString create(final String title, final Local file) {
        if(null == file) {
            return NSAttributedString.attributedString(title);
        }
        return create(NSMutableAttributedString.create(title,
                BundleController.TRUNCATE_MIDDLE_ATTRIBUTES), file.getAbsolute());
    }

    public static NSAttributedString create(final String title, final String url) {
        if(null == url) {
            return NSAttributedString.attributedString(title);
        }
        return create(NSMutableAttributedString.create(title,
                BundleController.TRUNCATE_MIDDLE_ATTRIBUTES), url);
    }

    /**
     * @param value     Existing attributes
     * @param hyperlink URL
     * @return Clickable and underlined string to put into text field.
     */
    private static NSAttributedString create(final NSMutableAttributedString value, final String hyperlink) {
        final NSRange range = NSRange.NSMakeRange(new NSUInteger(0), value.length());
        value.beginEditing();
        value.addAttributeInRange(NSMutableAttributedString.LinkAttributeName,
                hyperlink, range);
        // make the text appear in blue
        value.addAttributeInRange(NSMutableAttributedString.ForegroundColorAttributeName,
                NSColor.blueColor(), range);
        // system font
        value.addAttributeInRange(NSMutableAttributedString.FontAttributeName,
                NSFont.systemFontOfSize(NSFont.smallSystemFontSize()), range);
        // next make the text appear with an underline
        value.addAttributeInRange(NSMutableAttributedString.UnderlineStyleAttributeName,
                NSNumber.numberWithInt(NSMutableAttributedString.SingleUnderlineStyle), range);
        value.endEditing();
        return value;
    }
}