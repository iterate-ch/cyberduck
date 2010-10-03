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

import ch.cyberduck.core.AbstractPath;
import ch.cyberduck.ui.cocoa.application.AppKitFunctions;
import ch.cyberduck.ui.cocoa.application.NSFont;
import ch.cyberduck.ui.cocoa.application.NSView;
import ch.cyberduck.ui.cocoa.application.NSWorkspace;
import ch.cyberduck.ui.cocoa.foundation.*;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;

/**
 * @version $Id$
 */
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

    protected void loadBundle() {
        final String bundleName = this.getBundleName();
        if(null == bundleName) {
            log.debug("No bundle to load for " + this.toString());
            return;
        }
        this.loadBundle(bundleName);
    }

    protected void loadBundle(final String bundleName) {
        if(awaked) {
            log.warn("Bundle " + bundleName + " already loaded");
            return;
        }
        log.info("Loading bundle " + bundleName);
        if(!NSBundle.loadNibNamed(bundleName, this.id())) {
            log.fatal("Couldn't load " + bundleName + ".nib");
            return;
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

    public static void openUrl(AbstractPath.DescriptiveUrl url) {
        openUrl(url.getUrl());
    }

    /**
     * Open URL with default web browser.
     *
     * @param url
     */
    public static void openUrl(String url) {
        if(StringUtils.isNotBlank(url)) {
            if(!NSWorkspace.sharedWorkspace().openURL(NSURL.URLWithString(url))) {
                AppKitFunctions.instance.NSBeep();
            }
        }
        else {
            AppKitFunctions.instance.NSBeep();
        }
    }
}
