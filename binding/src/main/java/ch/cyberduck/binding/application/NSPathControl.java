package ch.cyberduck.binding.application;

/*
 * Copyright (c) 2002-2019 iterate GmbH. All rights reserved.
 * https://cyberduck.io/
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 */

import ch.cyberduck.binding.foundation.NSURL;

import org.rococoa.ID;
import org.rococoa.ObjCClass;
import org.rococoa.cocoa.foundation.NSRect;

public abstract class NSPathControl extends NSControl {
    private static final _Class CLASS = org.rococoa.Rococoa.createClass("NSPathControl", _Class.class);

    public static NSPathControl pathControlWithFrame(NSRect frameRect) {
        return CLASS.alloc().initWithFrame(frameRect);
    }

    public interface _Class extends ObjCClass {
        NSPathControl alloc();
    }

    @Override
    public abstract NSPathControl initWithFrame(NSRect frameRect);

    public abstract NSURL URL();

    public abstract void setURL(NSURL aString);

    public abstract void setDelegate(ID delegate);

    public interface Delegate {
        void pathControl_willDisplayOpenPanel(NSPathControl control, NSOpenPanel panel);
    }
}
