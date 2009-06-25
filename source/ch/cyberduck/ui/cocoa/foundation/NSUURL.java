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

import org.rococoa.NSClass;
import org.rococoa.Rococoa;

/**
 * @version $Id$
 */
public abstract class NSUURL implements NSObject {
    public static final _Class CLASS = Rococoa.createClass("NSUURL", _Class.class); //$NON-NLS-1$

    public interface _Class extends NSClass {
        NSURL URLWithString(String url);
    }

    public static NSURL URLWithString(String url) {
        return CLASS.URLWithString(url);
    }

    public abstract String host();

}
