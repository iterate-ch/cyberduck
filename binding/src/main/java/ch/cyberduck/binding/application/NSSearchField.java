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

public abstract class NSSearchField extends NSTextField {
    private static final _Class CLASS = org.rococoa.Rococoa.createClass("NSSearchField", _Class.class);

    public static NSSearchField searchField() {
        return CLASS.alloc().init();
    }

    public interface _Class extends ObjCClass {
        NSSearchField alloc();
    }

    public abstract NSSearchField init();

    /**
     * When the value of this property is YES, the cell calls its action method immediately upon notification of any
     * changes to the search field. When the value is NO, the cell pauses briefly after receiving a notification and
     * then calls its action method. Pausing gives the user an opportunity to type more text into the search field
     * and minimize the number of searches that are performed.
     *
     * @param flag A Boolean value indicating whether the cell calls its action method immediately when an
     *             appropriate action occurs.
     */
    public abstract void setSendsSearchStringImmediately(boolean flag);

    public abstract boolean sendsSearchStringImmediately();

    /**
     * When the value of this property is YES, the cell calls its action method when the user clicks the search button
     * or presses Return. When the value is NO, the cell calls the action method after each keystroke.
     * The default value of this property is NO.
     *
     * @param flag A Boolean value indicating whether the cell calls its search action method
     *             when the user clicks the search button (or presses Return) or after each keystroke.
     */
    public abstract void setSendsWholeSearchString(final boolean flag);

    public abstract boolean sendsWholeSearchString();
}
