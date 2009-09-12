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

import org.rococoa.NSClass;
import org.rococoa.Selector;
import org.rococoa.cocoa.foundation.NSUInteger;

/// <i>native declaration : :14</i>
public interface NSObject extends org.rococoa.NSObject {

    public abstract NSObject copy();

    public abstract NSObject autorelease();

    /**
     * <i>native declaration : :16</i><br>
     * Conversion Error : /// Original signature : <code>BOOL isEqual(null)</code><br>
     * - (BOOL)isEqual:(null)object; (Argument object cannot be converted)
     */
    public abstract boolean isEqual(org.rococoa.ID object);

    /**
     * Original signature : <code>NSUInteger hash()</code><br>
     * <i>native declaration : :17</i>
     */
    public abstract NSUInteger hash();

    /**
     * Original signature : <code>superclass()</code><br>
     * <i>native declaration : :19</i>
     */
    public abstract NSObject superclass();

    /**
     * Original signature : <code>self()</code><br>
     * <i>native declaration : :21</i>
     */
    public abstract NSObject self();

    /**
     * Original signature : <code>NSZone* zone()</code><br>
     * <i>native declaration : :22</i>
     */
    public abstract NSZone zone();

    /**
     * <i>native declaration : :24</i><br>
     * Conversion Error : /// Original signature : <code>performSelector(null)</code><br>
     * - (null)performSelector:(null)aSelector; (Argument aSelector cannot be converted)
     */
    public abstract void performSelector(Selector aSelector);

    /**
     * <i>native declaration : :25</i><br>
     * Conversion Error : /// Original signature : <code>performSelector(null, null)</code><br>
     * - (null)performSelector:(null)aSelector withObject:(null)object; (Argument aSelector cannot be converted)
     */
    public abstract void performSelector_widthObject(Selector sel, NSObject object);

    /**
     * <i>native declaration : :26</i><br>
     * Conversion Error : /// Original signature : <code>performSelector(null, null, null)</code><br>
     * - (null)performSelector:(null)aSelector withObject:(null)object1 withObject:(null)object2; (Argument aSelector cannot be converted)
     */
    public abstract void performSelector_widthObject_withObject(Selector sel, NSObject object1, NSObject object2);

    /**
     * Original signature : <code>BOOL isProxy()</code><br>
     * <i>native declaration : :28</i>
     */
    public abstract boolean isProxy();

    /**
     * <i>native declaration : :31</i><br>
     * Conversion Error : /// Original signature : <code>BOOL isMemberOfClass(null)</code><br>
     * - (BOOL)isMemberOfClass:(null)aClass; (Argument aClass cannot be converted)
     */
    public abstract boolean isMemberOfClass(NSClass aClass);

    /**
     * Original signature : <code>BOOL conformsToProtocol(Protocol*)</code><br>
     * <i>native declaration : :32</i>
     */
    public abstract boolean conformsToProtocol(org.rococoa.NSClass aProtocol);

    /**
     * <i>native declaration : :34</i><br>
     * Conversion Error : /// Original signature : <code>BOOL respondsToSelector(null)</code><br>
     * - (BOOL)respondsToSelector:(null)aSelector; (Argument aSelector cannot be converted)
     */
    public abstract boolean respondsToSelector(Selector aSelector);

}