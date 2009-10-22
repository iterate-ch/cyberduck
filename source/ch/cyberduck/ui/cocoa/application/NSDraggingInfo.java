package ch.cyberduck.ui.cocoa.application;

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

import ch.cyberduck.ui.cocoa.foundation.NSArray;
import ch.cyberduck.ui.cocoa.foundation.NSObject;
import ch.cyberduck.ui.cocoa.foundation.NSURL;

import org.rococoa.cocoa.foundation.NSPoint;
import org.rococoa.cocoa.foundation.NSUInteger;

/// <i>native declaration : :32</i>
public abstract class NSDraggingInfo extends NSObject {

    /// <i>native declaration : line 15</i>
    public static final NSUInteger NSDragOperationNone = new NSUInteger(0);
    /// <i>native declaration : line 16</i>
    public static final NSUInteger NSDragOperationCopy = new NSUInteger(1);
    /// <i>native declaration : line 17</i>
    public static final NSUInteger NSDragOperationLink = new NSUInteger(2);
    /// <i>native declaration : line 18</i>
    public static final NSUInteger NSDragOperationGeneric = new NSUInteger(4);
    /// <i>native declaration : line 19</i>
    public static final NSUInteger NSDragOperationPrivate = new NSUInteger(8);
    /// <i>native declaration : line 21</i>
    public static final NSUInteger NSDragOperationMove = new NSUInteger(16);
    /// <i>native declaration : line 22</i>
    public static final NSUInteger NSDragOperationDelete = new NSUInteger(32);

    /**
     * Original signature : <code>NSWindow* draggingDestinationWindow()</code><br>
     * <i>native declaration : :33</i>
     */
    public abstract NSWindow draggingDestinationWindow();

    /**
     * Original signature : <code>NSDragOperation draggingSourceOperationMask()</code><br>
     * <i>native declaration : :34</i>
     */
    public abstract NSUInteger draggingSourceOperationMask();

    /**
     * Original signature : <code>draggingLocation()</code><br>
     * <i>native declaration : :35</i>
     */
    public abstract NSPoint draggingLocation();

    /**
     * Original signature : <code>draggedImageLocation()</code><br>
     * <i>native declaration : :36</i>
     */
    public abstract NSPoint draggedImageLocation();

    /**
     * Original signature : <code>NSImage* draggedImage()</code><br>
     * <i>native declaration : :37</i>
     */
    public abstract NSImage draggedImage();

    /**
     * Original signature : <code>NSPasteboard* draggingPasteboard()</code><br>
     * <i>native declaration : :38</i>
     */
    public abstract NSPasteboard draggingPasteboard();

    /**
     * Original signature : <code>draggingSource()</code><br>
     * <i>native declaration : :39</i>
     */
    public abstract NSObject draggingSource();

    /**
     * Original signature : <code>NSInteger draggingSequenceNumber()</code><br>
     * <i>native declaration : :40</i>
     */
    public abstract int draggingSequenceNumber();
    /**
     * <i>native declaration : :41</i><br>
     * Conversion Error : /// Original signature : <code>void slideDraggedImageTo(null)</code><br>
     * - (void)slideDraggedImageTo:(null)screenPoint; (Argument screenPoint cannot be converted)
     */
    /**
     * Original signature : <code>NSArray* namesOfPromisedFilesDroppedAtDestination(NSURL*)</code><br>
     * <i>native declaration : :43</i>
     */
    public abstract NSArray namesOfPromisedFilesDroppedAtDestination(NSURL dropDestination);
}
