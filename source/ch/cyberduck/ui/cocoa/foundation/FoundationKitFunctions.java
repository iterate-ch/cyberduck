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

import org.rococoa.cocoa.foundation.NSPoint;
import org.rococoa.cocoa.foundation.NSRect;
import org.rococoa.cocoa.foundation.NSSize;

import com.sun.jna.Library;
import com.sun.jna.Native;

/**
 * @version $Id:$
 */
public interface FoundationKitFunctions extends Library {
    public static final FoundationKitFunctions instance = (FoundationKitFunctions) Native.loadLibrary("Foundation", FoundationKitFunctions.class);

    /**
     * <i>native declaration : /System/Library/Frameworks/ApplicationServices.framework/Headers/../Frameworks/CoreGraphics.framework/Headers/CGGeometry.h:36</i><br>
     * enum values
     */
    public static interface CGRectEdge {
        public static final int CGRectMinXEdge = 0;
        public static final int CGRectMinYEdge = 1;
        public static final int CGRectMaxXEdge = 2;
        public static final int CGRectMaxYEdge = 3;
    }

    /**
     * <i>native declaration : /System/Library/Frameworks/ApplicationServices.framework/Headers/../Frameworks/CoreGraphics.framework/Headers/CGGeometry.h</i><br>
     * enum values
     */
    public static interface NSRectEdge {
        public static final int NSMinXEdge = 0;
        public static final int NSMinYEdge = 1;
        public static final int NSMaxXEdge = 2;
        public static final int NSMaxYEdge = 3;
    }

    /**
     * Original signature : <code>BOOL NSEqualPoints(NSPoint, NSPoint)</code><br>
     * <i>native declaration : /System/Library/Frameworks/ApplicationServices.framework/Headers/../Frameworks/CoreGraphics.framework/Headers/CGGeometry.h:447</i>
     */
    boolean NSEqualPoints(NSPoint aPoint, NSPoint bPoint);

    /**
     * Original signature : <code>BOOL NSEqualSizes(NSSize, NSSize)</code><br>
     * <i>native declaration : /System/Library/Frameworks/ApplicationServices.framework/Headers/../Frameworks/framework/Headers/CGGeometry.h:448</i>
     */
    boolean NSEqualSizes(NSSize aSize, NSSize bSize);

    /**
     * Original signature : <code>BOOL NSEqualRects(NSRect, NSRect)</code><br>
     * <i>native declaration : /System/Library/Frameworks/ApplicationServices.framework/Headers/../Frameworks/framework/Headers/CGGeometry.h:449</i>
     */
    boolean NSEqualRects(NSRect aRect, NSRect bRect);

    /**
     * Original signature : <code>BOOL NSIsEmptyRect(NSRect)</code><br>
     * <i>native declaration : /System/Library/Frameworks/ApplicationServices.framework/Headers/../Frameworks/framework/Headers/CGGeometry.h:450</i>
     */
    boolean NSIsEmptyRect(NSRect aRect);

    /**
     * Original signature : <code>NSRect NSInsetRect(NSRect, CGFloat, CGFloat)</code><br>
     * <i>native declaration : /System/Library/Frameworks/ApplicationServices.framework/Headers/../Frameworks/framework/Headers/CGGeometry.h:452</i>
     */
    NSRect NSInsetRect(NSRect aRect, org.rococoa.cocoa.CGFloat dX, org.rococoa.cocoa.CGFloat dY);

    /**
     * Original signature : <code>NSRect NSIntegralRect(NSRect)</code><br>
     * <i>native declaration : /System/Library/Frameworks/ApplicationServices.framework/Headers/../Frameworks/framework/Headers/CGGeometry.h:453</i>
     */
    NSRect NSIntegralRect(NSRect aRect);

    /**
     * Original signature : <code>NSRect NSUnionRect(NSRect, NSRect)</code><br>
     * <i>native declaration : /System/Library/Frameworks/ApplicationServices.framework/Headers/../Frameworks/framework/Headers/CGGeometry.h:454</i>
     */
    NSRect NSUnionRect(NSRect aRect, NSRect bRect);

    /**
     * Original signature : <code>NSRect NSIntersectionRect(NSRect, NSRect)</code><br>
     * <i>native declaration : /System/Library/Frameworks/ApplicationServices.framework/Headers/../Frameworks/framework/Headers/CGGeometry.h:455</i>
     */
    NSRect NSIntersectionRect(NSRect aRect, NSRect bRect);

    /**
     * Original signature : <code>NSRect NSOffsetRect(NSRect, CGFloat, CGFloat)</code><br>
     * <i>native declaration : /System/Library/Frameworks/ApplicationServices.framework/Headers/../Frameworks/framework/Headers/CGGeometry.h:456</i>
     */
    NSRect NSOffsetRect(NSRect aRect, org.rococoa.cocoa.CGFloat dX, org.rococoa.cocoa.CGFloat dY);

    /**
     * Original signature : <code>void NSDivideRect(NSRect, NSRect*, NSRect*, CGFloat, NSRectEdge)</code><br>
     * <i>native declaration : /System/Library/Frameworks/ApplicationServices.framework/Headers/../Frameworks/framework/Headers/CGGeometry.h:457</i><br>
     *
     * @param edge @see NSRectEdge
     */
    void NSDivideRect(NSRect inRect, NSRect slice, NSRect rem, org.rococoa.cocoa.CGFloat amount, int edge);

    /**
     * Original signature : <code>BOOL NSPointInRect(NSPoint, NSRect)</code><br>
     * <i>native declaration : /System/Library/Frameworks/ApplicationServices.framework/Headers/../Frameworks/framework/Headers/CGGeometry.h:458</i>
     */
    boolean NSPointInRect(NSPoint aPoint, NSRect aRect);

    /**
     * Original signature : <code>BOOL NSMouseInRect(NSPoint, NSRect, BOOL)</code><br>
     * <i>native declaration : /System/Library/Frameworks/ApplicationServices.framework/Headers/../Frameworks/framework/Headers/CGGeometry.h:459</i>
     */
    boolean NSMouseInRect(NSPoint aPoint, NSRect aRect, boolean flipped);

    /**
     * Original signature : <code>BOOL NSContainsRect(NSRect, NSRect)</code><br>
     * <i>native declaration : /System/Library/Frameworks/ApplicationServices.framework/Headers/../Frameworks/framework/Headers/CGGeometry.h:460</i>
     */
    boolean NSContainsRect(NSRect aRect, NSRect bRect);

    /**
     * Original signature : <code>BOOL NSIntersectsRect(NSRect, NSRect)</code><br>
     * <i>native declaration : /System/Library/Frameworks/ApplicationServices.framework/Headers/../Frameworks/framework/Headers/CGGeometry.h:461</i>
     */
    boolean NSIntersectsRect(NSRect aRect, NSRect bRect);

    /**
     * Original signature : <code>NSString* NSStringFromPoint(NSPoint)</code><br>
     * <i>native declaration : /System/Library/Frameworks/ApplicationServices.framework/Headers/../Frameworks/framework/Headers/CGGeometry.h:463</i>
     */
    String NSStringFromPoint(NSPoint aPoint);

    /**
     * Original signature : <code>NSString* NSStringFromSize(NSSize)</code><br>
     * <i>native declaration : /System/Library/Frameworks/ApplicationServices.framework/Headers/../Frameworks/framework/Headers/CGGeometry.h:464</i>
     */
    String NSStringFromSize(NSSize aSize);

    /**
     * Original signature : <code>NSString* NSStringFromRect(NSRect)</code><br>
     * <i>native declaration : /System/Library/Frameworks/ApplicationServices.framework/Headers/../Frameworks/framework/Headers/CGGeometry.h:465</i>
     */
    String NSStringFromRect(NSRect aRect);

    /**
     * Original signature : <code>NSPoint NSPointFromString(NSString*)</code><br>
     * <i>native declaration : /System/Library/Frameworks/ApplicationServices.framework/Headers/../Frameworks/framework/Headers/CGGeometry.h:466</i>
     */
    NSPoint NSPointFromString(String aString);

    /**
     * Original signature : <code>NSSize NSSizeFromString(NSString*)</code><br>
     * <i>native declaration : /System/Library/Frameworks/ApplicationServices.framework/Headers/../Frameworks/framework/Headers/CGGeometry.h:467</i>
     */
    NSSize NSSizeFromString(String aString);
}

