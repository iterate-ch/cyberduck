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

import org.rococoa.ObjCClass;

/// <i>native declaration : :27</i>
public abstract class NSDistributedNotificationCenter extends NSNotificationCenter {
    private static final _Class CLASS = org.rococoa.Rococoa.createClass("NSDistributedNotificationCenter", _Class.class);

    public interface _Class extends ObjCClass {
        /**
         * Original signature : <code>NSDistributedNotificationCenter* notificationCenterForType(NSString*)</code><br>
         * <i>native declaration : :29</i>
         */
        NSDistributedNotificationCenter notificationCenterForType(String notificationCenterType);

        /**
         * Original signature : <code>defaultCenter()</code><br>
         * <i>native declaration : :32</i>
         */
        NSDistributedNotificationCenter defaultCenter();
    }
    /**
     * <i>native declaration : :35</i><br>
     * Conversion Error : /// Original signature : <code>void addObserver(null, null, NSString*, NSString*, NSNotificationSuspensionBehavior)</code><br>
     * - (void)addObserver:(null)observer selector:(null)selector name:(NSString*)name object:(NSString*)object suspensionBehavior:(NSNotificationSuspensionBehavior)suspensionBehavior; (Argument observer cannot be converted)
     */
    /**
     * Original signature : <code>void postNotificationName(NSString*, NSString*, NSDictionary*, BOOL)</code><br>
     * <i>native declaration : :38</i>
     */
    public abstract void postNotificationName_object_userInfo_deliverImmediately(String name, String object, NSDictionary userInfo, boolean deliverImmediately);
}
