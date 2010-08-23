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

/// <i>native declaration : :80</i>
public abstract class NSMutableArray extends NSArray {
    private static final _Class CLASS = org.rococoa.Rococoa.createClass("NSMutableArray", _Class.class);

    public static NSMutableArray array() {
        return CLASS.array();
    }

    public static NSMutableArray arrayWithCapacity(NSUInteger numItems) {
        return CLASS.arrayWithCapacity(numItems);
    }

    public interface _Class extends ObjCClass {
        /**
         * Original signature : <code>id arrayWithCapacity(NSUInteger)</code><br>
         * <i>from NSMutableArrayCreation native declaration : :118</i>
         */
        NSMutableArray arrayWithCapacity(NSUInteger numItems);

        NSMutableArray array();
    }

    /**
     * Original signature : <code>void addObject(id)</code><br>
     * <i>native declaration : :82</i>
     */
    public abstract void addObject(NSObject anObject);

    public abstract void addObject(String anObject);

    /**
     * Original signature : <code>void insertObject(id, NSUInteger)</code><br>
     * <i>native declaration : :83</i>
     */
    public abstract void insertObject_atIndex(NSObject anObject, NSUInteger index);

    public abstract void insertObject_atIndex(String anObject, NSUInteger index);

    /**
     * Original signature : <code>void removeLastObject()</code><br>
     * <i>native declaration : :84</i>
     */
    public abstract void removeLastObject();

    /**
     * Original signature : <code>void removeObjectAtIndex(NSUInteger)</code><br>
     * <i>native declaration : :85</i>
     */
    public abstract void removeObjectAtIndex(NSUInteger index);

    /**
     * Original signature : <code>void replaceObjectAtIndex(NSUInteger, id)</code><br>
     * <i>native declaration : :86</i>
     */
    public abstract void replaceObjectAtIndex_withObject(NSUInteger index, NSObject anObject);

    /**
     * Original signature : <code>void addObjectsFromArray(NSArray*)</code><br>
     * <i>from NSExtendedMutableArray native declaration : :92</i>
     */
    public abstract void addObjectsFromArray(NSArray otherArray);

    /**
     * Original signature : <code>void exchangeObjectAtIndex(NSUInteger, NSUInteger)</code><br>
     * <i>from NSExtendedMutableArray native declaration : :93</i>
     */
    public abstract void exchangeObjectAtIndex_withObjectAtIndex(NSUInteger idx1, NSUInteger idx2);

    /**
     * Original signature : <code>void removeAllObjects()</code><br>
     * <i>from NSExtendedMutableArray native declaration : :94</i>
     */
    public abstract void removeAllObjects();
    /**
     * <i>from NSExtendedMutableArray native declaration : :95</i><br>
     * Conversion Error : /// Original signature : <code>void removeObject(id, null)</code><br>
     * - (void)removeObject:(id)anObject inRange:(null)range; (Argument range cannot be converted)
     */
    /**
     * Original signature : <code>void removeObject(id)</code><br>
     * <i>from NSExtendedMutableArray native declaration : :96</i>
     */
    public abstract void removeObject(NSObject anObject);
    /**
     * <i>from NSExtendedMutableArray native declaration : :97</i><br>
     * Conversion Error : /// Original signature : <code>void removeObjectIdenticalTo(id, null)</code><br>
     * - (void)removeObjectIdenticalTo:(id)anObject inRange:(null)range; (Argument range cannot be converted)
     */
    /**
     * Original signature : <code>void removeObjectIdenticalTo(id)</code><br>
     * <i>from NSExtendedMutableArray native declaration : :98</i>
     */
    public abstract void removeObjectIdenticalTo(NSObject anObject);

    /**
     * Original signature : <code>void removeObjectsFromIndices(NSUInteger*, NSUInteger)</code><br>
     * <i>from NSExtendedMutableArray native declaration : :99</i>
     */
    public abstract void removeObjectsFromIndices_numIndices(java.nio.IntBuffer indices, NSUInteger cnt);

    /**
     * Original signature : <code>void removeObjectsInArray(NSArray*)</code><br>
     * <i>from NSExtendedMutableArray native declaration : :100</i>
     */
    public abstract void removeObjectsInArray(NSArray otherArray);
    /**
     * <i>from NSExtendedMutableArray native declaration : :101</i><br>
     * Conversion Error : /// Original signature : <code>void removeObjectsInRange(null)</code><br>
     * - (void)removeObjectsInRange:(null)range; (Argument range cannot be converted)
     */
    /**
     * <i>from NSExtendedMutableArray native declaration : :102</i><br>
     * Conversion Error : /// Original signature : <code>void replaceObjectsInRange(null, NSArray*, null)</code><br>
     * - (void)replaceObjectsInRange:(null)range withObjectsFromArray:(NSArray*)otherArray range:(null)otherRange; (Argument range cannot be converted)
     */
    /**
     * <i>from NSExtendedMutableArray native declaration : :103</i><br>
     * Conversion Error : /// Original signature : <code>void replaceObjectsInRange(null, NSArray*)</code><br>
     * - (void)replaceObjectsInRange:(null)range withObjectsFromArray:(NSArray*)otherArray; (Argument range cannot be converted)
     */
    /**
     * Original signature : <code>void setArray(NSArray*)</code><br>
     * <i>from NSExtendedMutableArray native declaration : :104</i>
     */
    public abstract void setArray(NSArray otherArray);
    /**
     * <i>from NSExtendedMutableArray native declaration : :106</i><br>
     * Conversion Error : /// Original signature : <code>void sortUsingSelector(null)</code><br>
     * - (void)sortUsingSelector:(null)comparator; (Argument comparator cannot be converted)
     */
    /**
     * Original signature : <code>void insertObjects(NSArray*, NSIndexSet*)</code><br>
     * <i>from NSExtendedMutableArray native declaration : :109</i>
     */
    public abstract void insertObjects_atIndexes(NSArray objects, com.sun.jna.Pointer indexes);

    /**
     * Original signature : <code>void removeObjectsAtIndexes(NSIndexSet*)</code><br>
     * <i>from NSExtendedMutableArray native declaration : :110</i>
     */
    public abstract void removeObjectsAtIndexes(com.sun.jna.Pointer indexes);

    /**
     * Original signature : <code>void replaceObjectsAtIndexes(NSIndexSet*, NSArray*)</code><br>
     * <i>from NSExtendedMutableArray native declaration : :111</i>
     */
    public abstract void replaceObjectsAtIndexes_withObjects(com.sun.jna.Pointer indexes, NSArray objects);

    /**
     * Original signature : <code>id initWithCapacity(NSUInteger)</code><br>
     * <i>from NSMutableArrayCreation native declaration : :119</i>
     */
    public abstract NSMutableArray initWithCapacity(NSUInteger numItems);
}
