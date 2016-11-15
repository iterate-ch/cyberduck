package ch.cyberduck.binding.application;

/*
 * Copyright (c) 2002-2016 iterate GmbH. All rights reserved.
 * https://cyberduck.io/
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
 */

import org.rococoa.ObjCClass;
import org.rococoa.cocoa.foundation.NSInteger;

public abstract class NSTitlebarAccessoryViewController extends NSResponder {
    private static final NSTitlebarAccessoryViewController._Class CLASS = org.rococoa.Rococoa.createClass("NSTitlebarAccessoryViewController", NSTitlebarAccessoryViewController._Class.class);

    public interface _Class extends ObjCClass {
        NSTitlebarAccessoryViewController alloc();
    }

    public static final NSInteger NSLayoutAttributeLeft = new NSInteger(1);
    public static final NSInteger NSLayoutAttributeRight = new NSInteger(2);
    public static final NSInteger NSLayoutAttributeBottom = new NSInteger(4);

    public static NSTitlebarAccessoryViewController create() {
        return CLASS.alloc().init();
    }

    public abstract NSTitlebarAccessoryViewController init();

    public abstract void removeFromParentViewController();

    public abstract void setLayoutAttribute(NSInteger layout);

    public abstract void setView(NSView view);
}
