package ch.cyberduck.binding.foundation;

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

import org.rococoa.ID;
import org.rococoa.ObjCClass;
import org.rococoa.Selector;

/// <i>native declaration : :29</i>
public abstract class NSNotificationCenter extends NSObject {
    private static final _Class CLASS = org.rococoa.Rococoa.createClass("NSNotificationCenter", _Class.class);

    public interface _Class extends ObjCClass {
        /**
         * Original signature : <code>defaultCenter()</code><br>
         * <i>native declaration : :36</i>
         */
        NSNotificationCenter defaultCenter();
    }

    public static NSNotificationCenter defaultCenter() {
        return CLASS.defaultCenter();
    }

    public void addObserver(org.rococoa.ID notificationObserver, Selector notificationSelector, String notificationName, NSObject notificationSender) {
        this.addObserver_selector_name_object(notificationObserver, notificationSelector, notificationName, notificationSender);
    }

    /**
     * <i>native declaration : :38</i><br>
     * Conversion Error : /// Original signature : <code>void addObserver(null, null, NSString*, null)</code><br>
     * - (void)addObserver:(null)observer selector:(null)aSelector name:(NSString*)aName object:(null)anObject; (Argument observer cannot be converted)
     */
    public abstract void addObserver_selector_name_object(ID notificationObserver, Selector notificationSelector, String notificationName, NSObject notificationSender);

    /**
     * Original signature : <code>void postNotification(NSNotification*)</code><br>
     * <i>native declaration : :40</i>
     */
    public abstract void postNotification(NSNotification notification);
    /**
     * <i>native declaration : :41</i><br>
     * Conversion Error : /// Original signature : <code>void postNotificationName(NSString*, null)</code><br>
     * - (void)postNotificationName:(NSString*)aName object:(null)anObject; (Argument anObject cannot be converted)
     */
    /**
     * <i>native declaration : :42</i><br>
     * Conversion Error : /// Original signature : <code>void postNotificationName(NSString*, null, NSDictionary*)</code><br>
     * - (void)postNotificationName:(NSString*)aName object:(null)anObject userInfo:(NSDictionary*)aUserInfo; (Argument anObject cannot be converted)
     */
    /**
     * <i>native declaration : :44</i><br>
     * Conversion Error : /// Original signature : <code>void removeObserver(null)</code><br>
     * - (void)removeObserver:(null)observer; (Argument observer cannot be converted)
     */
    public abstract void removeObserver(org.rococoa.ID notificationObserver);
    /**
     * <i>native declaration : :45</i><br>
     * Conversion Error : /// Original signature : <code>void removeObserver(null, NSString*, null)</code><br>
     * - (void)removeObserver:(null)observer name:(NSString*)aName object:(null)anObject; (Argument observer cannot be converted)
     */
}
