package ch.cyberduck.ui.cocoa.foundation;

/*
 * Copyright (c) 2002-2009 David Kocher. All rights reserved.
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

import org.rococoa.cocoa.CGFloat;

import com.sun.jna.Structure;

/**
 * @author <a href="mailto:harald.kuhr@gmail.com">Harald Kuhr</a>
 * @version $Id$
 */
public class NSPoint extends Structure implements Structure.ByValue {

    public final CGFloat x;
    public final CGFloat y;

    public NSPoint() {
        this(0, 0);
    }

    public NSPoint(double x, double y) {
        this.x = new CGFloat(x);
        this.y = new CGFloat(y);
    }

    public NSPoint(CGFloat x, CGFloat y) {
        this.x = x;
        this.y = y;
    }
}
