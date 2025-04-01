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

    public abstract NSPathControlItem clickedPathItem();

    public abstract void setDelegate(ID delegate);

    /**
     * A set of methods that can be implemented by the delegate of a path control object to support dragging to and from the control.
     */
    public interface Delegate {
        /**
         * Implement this method to customize the Open panel shown by a pop-up–style path.
         * <p>
         * This method is called before the Open panel is shown but after its allowed file types are
         * set to the cell’s allowed types. At this time, you can further customize the Open panel
         * as required. This method is called only when the style is set to NSPathStylePopUp.
         * Implementation of this method is optional.
         *
         * @param pathControl The path control displaying the Open panel.
         * @param panel       The Open panel to be displayed.
         */
        void pathControl_willDisplayOpenPanel(NSPathControl pathControl, NSOpenPanel panel);

        /**
         * Implement this method to customize the menu of a pop-up–style path.
         * <p>
         * This method is called before the pop-up menu is shown. At this time, you can further customize
         * the menu as required, adding and removing items. This method is called only when the style is set
         * to NSPathStylePopUp. Implementation of this method is optional.
         *
         * @param pathControl The path control displaying the pop-up menu.
         * @param menu        The pop-up menu to be displayed.
         */
        void pathControl_willPopUpMenu(NSPathControl pathControl, NSMenu menu);
    }
}
