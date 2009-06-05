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

import org.rococoa.cocoa.NSPoint;

/// <i>native declaration : :32</i>
public interface NSDraggingInfo {
    static final _Class CLASS = org.rococoa.Rococoa.createClass("NSDraggingInfo", _Class.class);

    public interface _Class extends org.rococoa.NSClass {
        NSDraggingInfo alloc();
    }

    /**
     * Original signature : <code>NSWindow* draggingDestinationWindow()</code><br>
     * <i>native declaration : :33</i>
     */
    NSWindow draggingDestinationWindow();

    /**
     * Original signature : <code>NSDragOperation draggingSourceOperationMask()</code><br>
     * <i>native declaration : :34</i>
     */
    int draggingSourceOperationMask();

    /**
     * Original signature : <code>draggingLocation()</code><br>
     * <i>native declaration : :35</i>
     */
    NSPoint draggingLocation();

    /**
     * Original signature : <code>draggedImageLocation()</code><br>
     * <i>native declaration : :36</i>
     */
    NSPoint draggedImageLocation();

    /**
     * Original signature : <code>NSImage* draggedImage()</code><br>
     * <i>native declaration : :37</i>
     */
    NSImage draggedImage();

    /**
     * Original signature : <code>NSPasteboard* draggingPasteboard()</code><br>
     * <i>native declaration : :38</i>
     */
    NSPasteboard draggingPasteboard();

    /**
     * Original signature : <code>draggingSource()</code><br>
     * <i>native declaration : :39</i>
     */
    NSObject draggingSource();

    /**
     * Original signature : <code>NSInteger draggingSequenceNumber()</code><br>
     * <i>native declaration : :40</i>
     */
    int draggingSequenceNumber();
    /**
     * <i>native declaration : :41</i><br>
     * Conversion Error : /// Original signature : <code>void slideDraggedImageTo(null)</code><br>
     * - (void)slideDraggedImageTo:(null)screenPoint; (Argument screenPoint cannot be converted)
     */
    /**
     * Original signature : <code>NSArray* namesOfPromisedFilesDroppedAtDestination(NSURL*)</code><br>
     * <i>native declaration : :43</i>
     */
    NSArray namesOfPromisedFilesDroppedAtDestination(NSURL dropDestination);

    public static final int DragOperationNone = 0;
    public static final int DragOperationCopy = 1;
    public static final int DragOperationLink = 2;
    public static final int DragOperationGeneric = 4;
    public static final int DragOperationPrivate = 8;
    public static final int DragOperationAll = 15;
    public static final int DragOperationMove = 16;
    public static final int DragOperationDelete = 32;
    public static final int DragOperationEvery = -1;
}
