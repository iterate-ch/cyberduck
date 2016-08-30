package ch.cyberduck.binding.foundation;

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

import org.rococoa.ObjCClass;

public abstract class NSThread extends NSObject {
    private static final _Class CLASS = org.rococoa.Rococoa.createClass("NSThread", _Class.class);

    public static boolean isMainThread() {
        return CLASS.isMainThread();
    }

    public interface _Class extends ObjCClass {

        /**
         * Returns a Boolean value that indicates whether the current thread is the main thread.
         *
         * @return
         */
        boolean isMainThread();
    }
}
