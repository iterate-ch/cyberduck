package ch.cyberduck.ui.cocoa;

import com.apple.cocoa.foundation.NSRunLoop;
import com.apple.cocoa.foundation.NSTimer;
import com.apple.cocoa.foundation.NSSelector;

/*
 *  Copyright (c) 2004 David Kocher. All rights reserved.
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
public abstract class CDController {

    private NSRunLoop mainRunLoop;

    public void awakeFromNib() {
        mainRunLoop = NSRunLoop.currentRunLoop();
    }

    protected synchronized void invoke(Runnable thread) {
        mainRunLoop.addTimerForMode(new NSTimer(0f, this,
                new NSSelector("post", new Class[]{NSTimer.class}),
                thread,
                false),
                NSRunLoop.DefaultRunLoopMode);
    }

    public void post (NSTimer timer) {
        Object info = timer.userInfo();
        if (info instanceof Runnable)
            ((Runnable)info).run();
    }
}
