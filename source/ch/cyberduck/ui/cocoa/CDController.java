package ch.cyberduck.ui.cocoa;

/*
 *  Copyright (c) 2005 David Kocher. All rights reserved.
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

import com.apple.cocoa.foundation.NSNotificationCenter;
import com.apple.cocoa.foundation.NSObject;

import org.apache.log4j.Logger;

/**
 * @version $Id$
 */
public abstract class CDController extends NSObject {
    private static Logger log = Logger.getLogger(CDController.class);

    /**
     * Free all locked resources by this controller; also remove me from all observables;
     * marks this controller to be garbage collected as soon as needed
     */
    protected void invalidate() {
        if(log.isDebugEnabled()) {
            log.debug("invalidate:" + this.toString());
        }
        NSNotificationCenter.defaultCenter().removeObserver(this);
    }

    protected void finalize() throws java.lang.Throwable {
        log.debug("finalize:" + this.toString());
        super.finalize();
    }
}