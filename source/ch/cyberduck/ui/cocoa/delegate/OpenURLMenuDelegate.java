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

import ch.cyberduck.ui.cocoa.Action;
import ch.cyberduck.ui.cocoa.application.AppKitFunctions;
import ch.cyberduck.ui.cocoa.application.NSEvent;
import ch.cyberduck.ui.cocoa.application.NSMenuItem;
import ch.cyberduck.ui.cocoa.application.NSWorkspace;
import ch.cyberduck.ui.cocoa.foundation.NSURL;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;


/**
 * @version $Id:$
 */
public abstract class OpenURLMenuDelegate extends URLMenuDelegate {
    private static Logger log = Logger.getLogger(OpenURLMenuDelegate.class);

    @Override
    protected String getKeyEquivalent() {
        return "b";
    }

    @Override
    protected int getModifierMask() {
        return NSEvent.NSCommandKeyMask | NSEvent.NSAlternateKeyMask;
    }

    @Action
    @Override
    public void urlClicked(final NSMenuItem sender) {
        this.open(sender.representedObject());
    }

    /**
     * @param url
     */
    private void open(String url) {
        if(StringUtils.isNotBlank(url)) {
            NSWorkspace.sharedWorkspace().openURL(NSURL.URLWithString(url));
        }
        else {
            AppKitFunctions.instance.NSBeep();
        }
    }
}
