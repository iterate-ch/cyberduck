package ch.cyberduck.ui.cocoa;

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

import ch.cyberduck.binding.application.NSToolbarItem;

import org.rococoa.ObjCClass;

public abstract class AbstractToolbarFactory implements ToolbarFactory {

    public static abstract class CDToolbarItem extends NSToolbarItem {
        private static final _Class CLASS = org.rococoa.Rococoa.createClass("CDToolbarItem", _Class.class);

        public static NSToolbarItem itemWithIdentifier(String itemIdentifier) {
            return CLASS.alloc().initWithItemIdentifier(itemIdentifier);
        }

        public interface _Class extends ObjCClass {
            CDToolbarItem alloc();
        }
    }
}
