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

/// <i>native declaration : :10</i>
public abstract class NSDictionary extends NSObject {
    private static final _Class CLASS = org.rococoa.Rococoa.createClass("NSDictionary", _Class.class);

    public static NSDictionary dictionaryWithObjectsForKeys(NSArray objects, NSArray keys) {
        return CLASS.dictionaryWithObjects_forKeys(objects, keys);
    }

    public static NSDictionary dictionaryWithContentsOfURL(NSURL url) {
        return CLASS.dictionaryWithContentsOfURL(url);
    }

    public static NSDictionary dictionaryWithContentsOfFile(String path) {
        return CLASS.dictionaryWithContentsOfFile(path);
    }

    public interface _Class extends ObjCClass {
        /**
         * Original signature : <code>id dictionary()</code><br>
         * <i>from NSDictionaryCreation native declaration : :40</i>
         */
        NSDictionary dictionary();

        /**
         * Original signature : <code>id dictionaryWithObject(id, id)</code><br>
         * <i>from NSDictionaryCreation native declaration : :41</i>
         */
        NSDictionary dictionaryWithObject_forKey(NSObject object, NSObject key);

        /**
         * Original signature : <code>id dictionaryWithObjects(id*, id*, NSUInteger)</code><br>
         * <i>from NSDictionaryCreation native declaration : :42</i>
         */
        NSDictionary dictionaryWithObjects_forKeys_count(NSObject objects, NSObject keys, NSUInteger cnt);

        /**
         * Original signature : <code>id dictionaryWithObjectsAndKeys(id, null)</code><br>
         * <i>from NSDictionaryCreation native declaration : :43</i>
         */
        NSDictionary dictionaryWithObjectsAndKeys(NSObject firstObject, NSObject... varargs);

        /**
         * Original signature : <code>id dictionaryWithDictionary(NSDictionary*)</code><br>
         * <i>from NSDictionaryCreation native declaration : :44</i>
         */
        NSDictionary dictionaryWithDictionary(NSDictionary dict);

        /**
         * Original signature : <code>id dictionaryWithObjects(NSArray*, NSArray*)</code><br>
         * <i>from NSDictionaryCreation native declaration : :45</i>
         */
        NSDictionary dictionaryWithObjects_forKeys(NSArray objects, NSArray keys);

        /**
         * Original signature : <code>id dictionaryWithContentsOfFile(NSString*)</code><br>
         * <i>from NSDictionaryCreation native declaration : :53</i>
         */
        NSDictionary dictionaryWithContentsOfFile(String path);

        /**
         * Original signature : <code>id dictionaryWithContentsOfURL(NSURL*)</code><br>
         * <i>from NSDictionaryCreation native declaration : :54</i>
         */
        NSDictionary dictionaryWithContentsOfURL(NSURL url);
    }

    /**
     * Original signature : <code>NSUInteger count()</code><br>
     * <i>native declaration : :12</i>
     */
    public abstract NSUInteger count();

    /**
     * <i>native declaration : :13</i><br>
     * Conversion Error : /// Original signature : <code>objectForKey(null)</code><br>
     * - (null)objectForKey:(null)aKey; (Argument aKey cannot be converted)
     */
    public abstract NSObject objectForKey(String key);


    /**
     * Original signature : <code>NSEnumerator* keyEnumerator()</code><br>
     * <i>native declaration : :14</i>
     */
    public abstract NSEnumerator keyEnumerator();

    /**
     * Original signature : <code>NSArray* allKeys()</code><br>
     * <i>from NSExtendedDictionary native declaration : :20</i>
     */
    public abstract NSArray allKeys();
    /**
     * <i>from NSExtendedDictionary native declaration : :21</i><br>
     * Conversion Error : /// Original signature : <code>NSArray* allKeysForObject(null)</code><br>
     * - (NSArray*)allKeysForObject:(null)anObject; (Argument anObject cannot be converted)
     */
    /**
     * Original signature : <code>NSArray* allValues()</code><br>
     * <i>from NSExtendedDictionary native declaration : :22</i>
     */
    public abstract NSArray allValues();

    /**
     * Original signature : <code>NSString* descriptionInStringsFileFormat()</code><br>
     * <i>from NSExtendedDictionary native declaration : :24</i>
     */
    public abstract com.sun.jna.Pointer descriptionInStringsFileFormat();
    /**
     * <i>from NSExtendedDictionary native declaration : :25</i><br>
     * Conversion Error : /// Original signature : <code>NSString* descriptionWithLocale(null)</code><br>
     * - (NSString*)descriptionWithLocale:(null)locale; (Argument locale cannot be converted)
     */
    /**
     * <i>from NSExtendedDictionary native declaration : :26</i><br>
     * Conversion Error : /// Original signature : <code>NSString* descriptionWithLocale(null, NSUInteger)</code><br>
     * - (NSString*)descriptionWithLocale:(null)locale indent:(NSUInteger)level; (Argument locale cannot be converted)
     */
    /**
     * Original signature : <code>BOOL isEqualToDictionary(NSDictionary*)</code><br>
     * <i>from NSExtendedDictionary native declaration : :27</i>
     */
    public abstract boolean isEqualToDictionary(NSDictionary otherDictionary);

    /**
     * Original signature : <code>NSEnumerator* objectEnumerator()</code><br>
     * <i>from NSExtendedDictionary native declaration : :28</i>
     */
    public abstract NSEnumerator objectEnumerator();
    /**
     * <i>from NSExtendedDictionary native declaration : :29</i><br>
     * Conversion Error : /// Original signature : <code>NSArray* objectsForKeys(NSArray*, null)</code><br>
     * - (NSArray*)objectsForKeys:(NSArray*)keys notFoundMarker:(null)marker; (Argument marker cannot be converted)
     */
    /**
     * Original signature : <code>BOOL writeToFile(NSString*, BOOL)</code><br>
     * <i>from NSExtendedDictionary native declaration : :30</i>
     */
    public abstract boolean writeToFile_atomically(String path, boolean useAuxiliaryFile);

    public boolean writeToFile(String path) {
        return this.writeToFile_atomically(path, true);
    }

    /**
     * Original signature : <code>BOOL writeToURL(NSURL*, BOOL)</code><br>
     * the atomically flag is ignored if url of a type that cannot be written atomically.<br>
     * <i>from NSExtendedDictionary native declaration : :31</i>
     */
    public abstract boolean writeToURL_atomically(NSURL url, boolean atomically);

    public boolean writeToURL(NSURL url) {
        return this.writeToURL_atomically(url, true);
    }

    /**
     * <i>from NSExtendedDictionary native declaration : :33</i><br>
     * Conversion Error : /// Original signature : <code>NSArray* keysSortedByValueUsingSelector(null)</code><br>
     * - (NSArray*)keysSortedByValueUsingSelector:(null)comparator; (Argument comparator cannot be converted)
     */
    /**
     * Original signature : <code>void getObjects(id*, id*)</code><br>
     * <i>from NSExtendedDictionary native declaration : :34</i>
     */
    public abstract void getObjects_andKeys(NSObject objects, NSObject keys);

    /**
     * Original signature : <code>id initWithObjects(id*, id*, NSUInteger)</code><br>
     * <i>from NSDictionaryCreation native declaration : :47</i>
     */
    public abstract NSDictionary initWithObjects_forKeys_count(NSObject objects, NSObject keys, NSUInteger cnt);

    /**
     * Original signature : <code>id initWithObjectsAndKeys(id, null)</code><br>
     * <i>from NSDictionaryCreation native declaration : :48</i>
     */
    public abstract NSDictionary initWithObjectsAndKeys(NSObject firstObject, NSObject... varargs);

    /**
     * Original signature : <code>id initWithDictionary(NSDictionary*)</code><br>
     * <i>from NSDictionaryCreation native declaration : :49</i>
     */
    public abstract NSDictionary initWithDictionary(NSDictionary otherDictionary);

    /**
     * Original signature : <code>id initWithDictionary(NSDictionary*, BOOL)</code><br>
     * <i>from NSDictionaryCreation native declaration : :50</i>
     */
    public abstract NSDictionary initWithDictionary_copyItems(NSDictionary otherDictionary, boolean flag);

    /**
     * Original signature : <code>id initWithObjects(NSArray*, NSArray*)</code><br>
     * <i>from NSDictionaryCreation native declaration : :51</i>
     */
    public abstract NSDictionary initWithObjects_forKeys(NSArray objects, NSArray keys);

    /**
     * Original signature : <code>id initWithContentsOfFile(NSString*)</code><br>
     * <i>from NSDictionaryCreation native declaration : :55</i>
     */
    public abstract NSDictionary initWithContentsOfFile(String path);

    /**
     * Original signature : <code>id initWithContentsOfURL(NSURL*)</code><br>
     * <i>from NSDictionaryCreation native declaration : :56</i>
     */
    public abstract NSDictionary initWithContentsOfURL(NSURL url);
}
