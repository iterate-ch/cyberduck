package ch.cyberduck.ui.cocoa.application;

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

import org.rococoa.Selector;

/// <i>native declaration : /System/Library/Frameworks/AppKit.framework/Headers/NSUserInterfaceValidation.h:69</i>
public interface NSValidatedUserInterfaceItem {
    /**
     * Original signature : <code>action()</code><br>
     * <i>native declaration : /System/Library/Frameworks/AppKit.framework/Headers/NSUserInterfaceValidation.h:70</i>
     */
    Selector action();

    /**
     * Original signature : <code>NSInteger tag()</code><br>
     * <i>native declaration : /System/Library/Frameworks/AppKit.framework/Headers/NSUserInterfaceValidation.h:71</i>
     */
    int tag();
}
