package ch.cyberduck.ui.cocoa;

import com.apple.cocoa.foundation.NSObject;
import com.apple.cocoa.foundation.NSRunLoop;
import com.apple.cocoa.foundation.NSSelector;
import com.apple.cocoa.foundation.NSTimer;
import com.apple.cocoa.foundation.NSMutableArray;
import com.apple.cocoa.foundation.NSNotificationCenter;

import org.apache.log4j.Logger;

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

/**
 * @version $Id$
 */
public abstract class CDController extends NSObject {
    private static Logger log = Logger.getLogger(CDController.class);

    private NSRunLoop mainRunLoop;

    protected static NSMutableArray instances;

    public void awakeFromNib() {
        mainRunLoop = NSRunLoop.currentRunLoop();
        if(null == instances) {
            instances = new NSMutableArray();
        }
        instances.addObject(this);
    }

    protected synchronized void invoke(Runnable thread) {
        mainRunLoop.addTimerForMode(new NSTimer(0f, this,
                new NSSelector("post", new Class[]{NSTimer.class}),
                thread,
                false //automatically invalidate
        ),
                NSRunLoop.DefaultRunLoopMode);
    }

    protected void post(NSTimer timer) {
        Object info = timer.userInfo();
        if (info instanceof Runnable) {
            ((Runnable) info).run();
        }
    }

    /**
     * Free all locked resources by this controller; also remove me from all observables
     */
    protected void invalidate() {
        NSNotificationCenter.defaultCenter().removeObserver(this);
        instances.removeObject(this);
        System.gc();
    }

    protected void finalize() throws java.lang.Throwable {
        log.debug("finalize:"+this.toString());
        super.finalize();
    }
}
