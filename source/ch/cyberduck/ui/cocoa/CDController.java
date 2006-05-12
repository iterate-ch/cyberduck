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

import com.apple.cocoa.foundation.*;
import com.apple.cocoa.application.NSFont;

import org.apache.log4j.Logger;

import java.util.Map;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.ArrayList;

/**
 * @version $Id$
 */
public abstract class CDController extends NSObject {
    private static Logger log = Logger.getLogger(CDController.class);

    protected static final NSDictionary FIXED_WITH_FONT_ATTRIBUTES = new NSDictionary(new Object[]{NSFont.userFixedPitchFontOfSize(9.0f)},
            new Object[]{NSAttributedString.FontAttributeName});

    protected static String[] availableCharsets() {
        List charsets = new ArrayList();
        for (Iterator iter = java.nio.charset.Charset.availableCharsets().values().iterator(); iter.hasNext(); ) {
            String name = ((java.nio.charset.Charset) iter.next()).displayName();
            if(!(name.startsWith("IBM") || name.startsWith("x-"))) {
                charsets.add(name);
            }
        }
        return (String[])charsets.toArray(new String[0]);
    }


    public CDController() {
        //Assuming this is always called from the main thread #currentRunLoop will return the main run loop
        mainRunLoop = NSRunLoop.currentRunLoop();
        if(null == instances) {
            instances = new NSMutableArray();
        }
        //Add this object to the array to safe weak references from being garbage collected (#hack)
        instances.addObject(this);
    }

    /**
     * Reference to the main graphical user interface thread
     */
    private NSRunLoop mainRunLoop;

    protected static NSMutableArray instances;

    protected synchronized void invoke(Runnable thread) {
        mainRunLoop.addTimerForMode(new NSTimer(0f, this,
                new NSSelector("post", new Class[]{NSTimer.class}),
                thread,
                false //automatically invalidate
        ),
                NSRunLoop.DefaultRunLoopMode);
    }

    /**
     * Called by the timer to invoke the passed method in the main thread
     * @param timer holds the <code>Runnable</code> object in #userInfo
     */
    protected void post(NSTimer timer) {
        Object info = timer.userInfo();
        if (info instanceof Runnable) {
            ((Runnable) info).run();
        }
    }

    /**
     * Free all locked resources by this controller; also remove me from all observables;
     * marks this controller to be garbage collected as soon as needed
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
