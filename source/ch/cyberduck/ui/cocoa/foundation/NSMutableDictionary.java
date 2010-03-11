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
import org.rococoa.cocoa.foundation.NSUInteger;

/// <i>native declaration : :62</i>
public abstract class NSMutableDictionary extends NSDictionary {
    private static final _Class CLASS = org.rococoa.Rococoa.createClass("NSMutableDictionary", _Class.class);

    public interface _Class extends ObjCClass {
        /**
         * Original signature : <code>id dictionaryWithCapacity(NSUInteger)</code><br>
         * <i>from NSMutableDictionaryCreation native declaration : :80</i>
         */
        NSMutableDictionary dictionaryWithCapacity(NSUInteger numItems);

        NSMutableDictionary dictionaryWithDictionary(NSDictionary dict);
    }

    public static NSMutableDictionary dictionary() {
        return CLASS.dictionaryWithCapacity(new NSUInteger(0));
    }

    public static NSMutableDictionary dictionaryWithCapacity(NSUInteger numItems) {
        return CLASS.dictionaryWithCapacity(numItems);
    }

    public static NSMutableDictionary dictionaryWithDictionary(NSDictionary dictionary) {
        return CLASS.dictionaryWithDictionary(dictionary);
    }

    /**
     * Original signature : <code>void removeObjectForKey(id)</code><br>
     * <i>native declaration : :64</i>
     */
    public abstract void removeObjectForKey(String aKey);

    /**
     * If aKey already exists in the receiver, the receiver’s previous value
     * object for that key is sent a release message and anObject takes its place.
     *
     * @param anObject The object receives a retain message before being added to
     *                 the receiver. This value must not be nil.
     * @param aKey     The key is copied (using copyWithZone:; keys must conform to the NSCopying protocol). The key must not be nil.
     */
    public void setObjectForKey(String anObject, String aKey) {
        this.setObjectForKey(NSString.stringWithString(anObject), aKey);
    }

    public void setObjectForKey(NSObject anObject, String aKey) {
        this.setObject_forKey(anObject, NSString.stringWithString(aKey));
    }

    /**
     * Original signature : <code>public abstract void setObject(id, id)</code><br>
     * <i>native declaration : :65</i>
     */
    public abstract void setObject_forKey(NSObject anObject, NSObject aKey);

    /**
     * Original signature : <code>public abstract void addEntriesFromDictionary(NSDictionary*)</code><br>
     * <i>from NSExtendedMutableDictionary native declaration : :71</i>
     */
    public abstract void addEntriesFromDictionary(NSDictionary otherDictionary);

    /**
     * Original signature : <code>public abstract void removeAllObjects()</code><br>
     * <i>from NSExtendedMutableDictionary native declaration : :72</i>
     */
    public abstract void removeAllObjects();

    /**
     * Original signature : <code>public abstract void removeObjectsForKeys(NSArray*)</code><br>
     * <i>from NSExtendedMutableDictionary native declaration : :73</i>
     */
    public abstract void removeObjectsForKeys(NSArray keyArray);

    /**
     * Original signature : <code>public abstract void setDictionary(NSDictionary*)</code><br>
     * <i>from NSExtendedMutableDictionary native declaration : :74</i>
     */
    public abstract void setDictionary(NSDictionary otherDictionary);

    /**
     * Original signature : <code>id initWithCapacity(NSUInteger)</code><br>
     * <i>from NSMutableDictionaryCreation native declaration : :81</i>
     */
    public abstract NSMutableDictionary initWithCapacity(NSUInteger numItems);
}
