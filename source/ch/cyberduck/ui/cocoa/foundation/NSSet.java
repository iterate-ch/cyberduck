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

/// <i>native declaration : /System/Library/Frameworks/Foundation.framework/Headers/NSSet.h:13</i>
public abstract class NSSet extends NSObject {

    public interface _Class extends ObjCClass {
        /**
         * Original signature : <code>set()</code><br>
         * <i>from NSSetCreation native declaration : /System/Library/Frameworks/Foundation.framework/Headers/NSSet.h:43</i>
         */
        NSSet set();
        /**
         * <i>from NSSetCreation native declaration : /System/Library/Frameworks/Foundation.framework/Headers/NSSet.h:44</i><br>
         * Conversion Error : /// Original signature : <code>setWithObject(null)</code><br>
         * + (null)setWithObject:(null)object; (Argument object cannot be converted)
         */
        /**
         * Original signature : <code>setWithObjects(id*, NSUInteger)</code><br>
         * <i>from NSSetCreation native declaration : /System/Library/Frameworks/Foundation.framework/Headers/NSSet.h:45</i>
         */
        NSSet setWithObjects_count(NSObject objects, NSUInteger cnt);

        /**
         * Original signature : <code>id setWithObjects(id, null)</code><br>
         * <i>from NSSetCreation native declaration : /System/Library/Frameworks/Foundation.framework/Headers/NSSet.h:46</i>
         */
        NSSet setWithObjects(NSObject firstObj, NSObject... varargs);

        /**
         * Original signature : <code>id setWithSet(NSSet*)</code><br>
         * <i>from NSSetCreation native declaration : /System/Library/Frameworks/Foundation.framework/Headers/NSSet.h:47</i>
         */
        NSSet setWithSet(NSSet set);

        /**
         * Original signature : <code>id setWithArray(NSArray*)</code><br>
         * <i>from NSSetCreation native declaration : /System/Library/Frameworks/Foundation.framework/Headers/NSSet.h:48</i>
         */
        NSSet setWithArray(NSArray array);
    }

    /**
     * Original signature : <code>NSUInteger count()</code><br>
     * <i>native declaration : /System/Library/Frameworks/Foundation.framework/Headers/NSSet.h:15</i>
     */
    public abstract NSUInteger count();
    /**
     * <i>native declaration : /System/Library/Frameworks/Foundation.framework/Headers/NSSet.h:16</i><br>
     * Conversion Error : /// Original signature : <code>member(null)</code><br>
     * - (null)member:(null)object; (Argument object cannot be converted)
     */
    /**
     * Original signature : <code>NSEnumerator* objectEnumerator()</code><br>
     * <i>native declaration : /System/Library/Frameworks/Foundation.framework/Headers/NSSet.h:17</i>
     */
    public abstract NSEnumerator objectEnumerator();

    /**
     * Original signature : <code>NSArray* allObjects()</code><br>
     * <i>from NSExtendedSet native declaration : /System/Library/Frameworks/Foundation.framework/Headers/NSSet.h:23</i>
     */
    public abstract NSArray allObjects();

    /**
     * Original signature : <code>anyObject()</code><br>
     * <i>from NSExtendedSet native declaration : /System/Library/Frameworks/Foundation.framework/Headers/NSSet.h:24</i>
     */
    public abstract NSObject anyObject();
    /**
     * <i>from NSExtendedSet native declaration : /System/Library/Frameworks/Foundation.framework/Headers/NSSet.h:25</i><br>
     * Conversion Error : /// Original signature : <code>BOOL containsObject(null)</code><br>
     * - (BOOL)containsObject:(null)anObject; (Argument anObject cannot be converted)
     */
    /**
     * Original signature : <code>NSString* description()</code><br>
     * <i>from NSExtendedSet native declaration : /System/Library/Frameworks/Foundation.framework/Headers/NSSet.h:26</i>
     */
    public abstract String description();
    /**
     * <i>from NSExtendedSet native declaration : /System/Library/Frameworks/Foundation.framework/Headers/NSSet.h:27</i><br>
     * Conversion Error : /// Original signature : <code>NSString* descriptionWithLocale(null)</code><br>
     * - (NSString*)descriptionWithLocale:(null)locale; (Argument locale cannot be converted)
     */
    /**
     * Original signature : <code>BOOL intersectsSet(NSSet*)</code><br>
     * <i>from NSExtendedSet native declaration : /System/Library/Frameworks/Foundation.framework/Headers/NSSet.h:28</i>
     */
    public abstract boolean intersectsSet(NSSet otherSet);

    /**
     * Original signature : <code>BOOL isEqualToSet(NSSet*)</code><br>
     * <i>from NSExtendedSet native declaration : /System/Library/Frameworks/Foundation.framework/Headers/NSSet.h:29</i>
     */
    public abstract boolean isEqualToSet(NSSet otherSet);

    /**
     * Original signature : <code>BOOL isSubsetOfSet(NSSet*)</code><br>
     * <i>from NSExtendedSet native declaration : /System/Library/Frameworks/Foundation.framework/Headers/NSSet.h:30</i>
     */
    public abstract boolean isSubsetOfSet(NSSet otherSet);
    /**
     * <i>from NSExtendedSet native declaration : /System/Library/Frameworks/Foundation.framework/Headers/NSSet.h:32</i><br>
     * Conversion Error : /// Original signature : <code>void makeObjectsPerformSelector(null)</code><br>
     * - (void)makeObjectsPerformSelector:(null)aSelector; (Argument aSelector cannot be converted)
     */
    /**
     * <i>from NSExtendedSet native declaration : /System/Library/Frameworks/Foundation.framework/Headers/NSSet.h:33</i><br>
     * Conversion Error : /// Original signature : <code>void makeObjectsPerformSelector(null, null)</code><br>
     * - (void)makeObjectsPerformSelector:(null)aSelector withObject:(null)argument; (Argument aSelector cannot be converted)
     */
    /**
     * <i>from NSExtendedSet native declaration : /System/Library/Frameworks/Foundation.framework/Headers/NSSet.h:35</i><br>
     * Conversion Error : /// Original signature : <code>NSSet* setByAddingObject(null)</code><br>
     * - (NSSet*)setByAddingObject:(null)anObject; (Argument anObject cannot be converted)
     */
    /**
     * Original signature : <code>NSSet* setByAddingObjectsFromSet(NSSet*)</code><br>
     * <i>from NSExtendedSet native declaration : /System/Library/Frameworks/Foundation.framework/Headers/NSSet.h:36</i>
     */
    public abstract NSSet setByAddingObjectsFromSet(NSSet other);

    /**
     * Original signature : <code>NSSet* setByAddingObjectsFromArray(NSArray*)</code><br>
     * <i>from NSExtendedSet native declaration : /System/Library/Frameworks/Foundation.framework/Headers/NSSet.h:37</i>
     */
    public abstract NSSet setByAddingObjectsFromArray(NSArray other);

    /**
     * Original signature : <code>id initWithObjects(id*, NSUInteger)</code><br>
     * <i>from NSSetCreation native declaration : /System/Library/Frameworks/Foundation.framework/Headers/NSSet.h:50</i>
     */
    public abstract NSSet initWithObjects_count(NSObject objects, NSUInteger cnt);

    /**
     * Original signature : <code>id initWithObjects(id, null)</code><br>
     * <i>from NSSetCreation native declaration : /System/Library/Frameworks/Foundation.framework/Headers/NSSet.h:51</i>
     */
    public abstract NSSet initWithObjects(NSObject firstObj, NSObject... varargs);

    /**
     * Original signature : <code>id initWithSet(NSSet*)</code><br>
     * <i>from NSSetCreation native declaration : /System/Library/Frameworks/Foundation.framework/Headers/NSSet.h:52</i>
     */
    public abstract NSSet initWithSet(NSSet set);

    /**
     * Original signature : <code>id initWithSet(NSSet*, BOOL)</code><br>
     * <i>from NSSetCreation native declaration : /System/Library/Frameworks/Foundation.framework/Headers/NSSet.h:53</i>
     */
    public abstract NSSet initWithSet_copyItems(NSSet set, boolean flag);

    /**
     * Original signature : <code>id initWithArray(NSArray*)</code><br>
     * <i>from NSSetCreation native declaration : /System/Library/Frameworks/Foundation.framework/Headers/NSSet.h:54</i>
     */
    public abstract NSSet initWithArray(NSArray array);

    /**
     * Return a set containing the results of invoking -valueForKey: on each of the receiver's members. The returned set might not have the same number of members as the receiver. The returned set will not contain any elements corresponding to instances of -valueForKey: returning nil (in contrast with -[NSArray(NSKeyValueCoding) valueForKey:], which may put NSNulls in the arrays it returns).<br>
     * Original signature : <code>id valueForKey(NSString*)</code><br>
     * <i>from NSKeyValueCoding native declaration : :191</i>
     */
    public abstract NSObject valueForKey(String key);

    /**
     * Invoke -setValue:forKey: on each of the receiver's members.<br>
     * Original signature : <code>void setValue(id, NSString*)</code><br>
     * <i>from NSKeyValueCoding native declaration : :195</i>
     */
    public abstract void setValue_forKey(NSObject value, String key);
}
