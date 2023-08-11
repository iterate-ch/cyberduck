package ch.cyberduck.core.sparkle.bindings;

/*
 * Copyright (c) 2002-2023 iterate GmbH. All rights reserved.
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
import ch.cyberduck.binding.foundation.NSObject;

import org.rococoa.ID;
import org.rococoa.ObjCClass;

/**
 * Sparkle’s standard built-in user driver for updater interactions
 */
public abstract class SPUStandardUserDriver extends NSObject implements SPUUserDriver {
    private static final SPUStandardUserDriver._Class CLASS = org.rococoa.Rococoa.createClass("SPUStandardUserDriver", SPUStandardUserDriver._Class.class);

    public interface _Class extends ObjCClass {
        SPUStandardUserDriver alloc();
    }

    /**
     * @param hostBundle Main bundle
     * @param delegate   SPUStandardUserDriverDelegate
     */
    public static SPUStandardUserDriver create(final NSBundle hostBundle, final ID delegate) {
        return CLASS.alloc().initWithHostBundle_delegate(hostBundle, delegate);
    }

    /**
     * Initializes a Sparkle’s standard user driver for user update interactions
     *
     * @param hostBundle The target bundle of the host that is being updated.
     * @param delegate   SPUStandardUserDriverDelegate. The optional delegate to this user driver.
     */
    public abstract SPUStandardUserDriver initWithHostBundle_delegate(NSBundle hostBundle, ID delegate);
}
