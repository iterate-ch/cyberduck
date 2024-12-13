package ch.cyberduck.binding.application;

/*
 * Copyright (c) 2002-2024 iterate GmbH. All rights reserved.
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

import ch.cyberduck.binding.foundation.NSBundle;

import org.rococoa.ObjCClass;

public abstract class NSViewController extends NSResponder {
    private static final NSViewController._Class CLASS = org.rococoa.Rococoa.createClass("NSViewController", NSViewController._Class.class);

    public interface _Class extends ObjCClass {
        NSViewController alloc();
    }

    public static NSViewController create() {
        return CLASS.alloc();
    }

    public static NSViewController create(String nib, NSBundle bundle) {
        return CLASS.alloc().initWithNibName_bundle(nib, bundle);
    }

    @Override
    public abstract NSViewController init();

    /**
     * The NSViewController object looks for the nib file in the bundle's language-specific project directories first, followed by the Resources directory.
     * <p>
     * The specified nib file should typically have the class of the file's owner set to NSViewController, or a custom subclass, with the view outlet connected to a view.
     * <p>
     * If you pass in nil for nibNameOrNil, nibName returns nil and loadView throws an exception; in this case you must set view before view is invoked, or override loadView.
     *
     * @param nib    The name of the nib file, without any leading path information.
     * @param bundle The bundle in which to search for the nib file. If you specify nil, this method looks for the nib file in the main bundle.
     * @return Returns a view controller object initialized to the nib file in the specified bundle.
     */
    public abstract NSViewController initWithNibName_bundle(String nib, NSBundle bundle);

    /**
     * If this property’s value is not already set when you access it, the view controller invokes the loadView method. That method, in turn, sets the view from the nib file identified by the view controller’s nibName and nibBundle properties.
     * <p>
     * If you want to set a view controller’s view directly, set this property’s value immediately after creating the view controller.
     *
     * @param view The view controller’s primary view.
     */
    public abstract void setView(NSView view);
}
