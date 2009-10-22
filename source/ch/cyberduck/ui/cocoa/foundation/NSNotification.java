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

/// <i>native declaration : :12</i>
public abstract class NSNotification extends NSObject implements NSCopying {
    private static final _Class CLASS = org.rococoa.Rococoa.createClass("NSNotification", _Class.class);

    public interface _Class extends ObjCClass {
        /**
         * <i>from NSNotificationCreation native declaration : :22</i><br>
         * Conversion Error : /// Original signature : <code>notificationWithName(NSString*, null)</code><br>
         * + (null)notificationWithName:(NSString*)aName object:(null)anObject; (Argument anObject cannot be converted)
         */
        NSNotification notificationWithName_object(String notificationName, org.rococoa.ID object);

        /**
         * <i>from NSNotificationCreation native declaration : :23</i><br>
         * Conversion Error : /// Original signature : <code>notificationWithName(NSString*, null, NSDictionary*)</code><br>
         * + (null)notificationWithName:(NSString*)aName object:(null)anObject userInfo:(NSDictionary*)aUserInfo; (Argument anObject cannot be converted)
         */
        NSNotification alloc();
    }

    public static NSNotification notificationWithName(String notificationName, org.rococoa.ID object) {
        return CLASS.notificationWithName_object(notificationName, object);
    }

    public static NSNotification notificationWithName(String notificationName, String object) {
        return CLASS.notificationWithName_object(notificationName, NSString.stringWithString(object).id());
    }

    /**
     * Original signature : <code>NSString* name()</code><br>
     * <i>native declaration : :14</i>
     */
    public abstract String name();

    /**
     * Original signature : <code>object()</code><br>
     * <i>native declaration : :15</i>
     */
    public abstract NSObject object();

    /**
     * Original signature : <code>NSDictionary* userInfo()</code><br>
     * <i>native declaration : :16</i>
     */
    public abstract NSDictionary userInfo();
}
