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


import ch.cyberduck.ui.cocoa.foundation.NSAutoreleasePool;
import ch.cyberduck.ui.cocoa.foundation.NSNotificationCenter;
import ch.cyberduck.ui.cocoa.foundation.NSObject;

import org.apache.log4j.Logger;
import org.rococoa.ID;
import org.rococoa.Rococoa;

/**
 * @version $Id$
 */
public abstract class CDController {
    private static Logger log = Logger.getLogger(CDController.class);

    private NSAutoreleasePool pool;

    public CDController() {
        pool = NSAutoreleasePool.push();
    }


    /**
     * You need to keep a reference to the returned value for as long as it is
     * active. When it is GCd, it will release the Objective-C proxy.
     */
    private NSObject proxy;

    private ID id;

    /**
     * @return
     */
    public org.rococoa.ID id() {
        return this.id(NSObject.class);
    }

    public org.rococoa.ID id(Class<? extends NSObject> type) {
        if(null == proxy) {
            proxy = Rococoa.proxy(this, type);
        }
        if(null == id) {
            id = proxy.id();
        }
        return id;
    }

    /**
     * Free all locked resources by this controller; also remove me from all observables;
     * marks this controller to be garbage collected as soon as needed
     */
    protected void invalidate() {
        if(log.isDebugEnabled()) {
            log.debug("invalidate:" + this.toString());
        }
        NSNotificationCenter.defaultCenter().removeObserver(this.id());
        proxy = null;
        id = null;
//        pool.drain();
        pool = null;
        if(log.isDebugEnabled()) {
            System.gc();
        }
    }

    @Override
    protected void finalize() throws java.lang.Throwable {
        if(log.isTraceEnabled()) {
            log.trace("finalize:" + this.toString());
        }
        super.finalize();
    }
}