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

import com.apple.cocoa.application.NSApplication;

import org.apache.log4j.Logger;

/**
 * @version $Id:$
 */
public abstract class CDBundleController extends CDController {
    private static Logger log = Logger.getLogger(CDBundleController.class);

    protected void loadBundle() {
        final String bundleName = this.getBundleName();
        if(null == bundleName) {
            log.debug("No bundle to load for "+this.toString());
        }
        else {
            this.loadBundle(bundleName);
        }
    }

    protected void loadBundle(final String bundleName) {
        synchronized(NSApplication.sharedApplication()) {
            if(!NSApplication.loadNibNamed(bundleName, this)) {
                log.fatal("Couldn't load " + bundleName + ".nib");
            }
        }
    }

    protected abstract String getBundleName();
}
