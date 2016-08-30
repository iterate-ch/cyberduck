package ch.cyberduck.binding.application;

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

import org.rococoa.cocoa.foundation.NSUInteger;

public interface NSDraggingDestination {

    /**
     * Original signature : <code>NSDragOperation draggingEntered(id<NSDraggingInfo>)</code><br>
     * <i>native declaration : line 47</i>
     */
    NSUInteger draggingEntered(org.rococoa.ID sender);

    /**
     * Original signature : <code>NSDragOperation draggingUpdated(org.rococoa.ID)</code><br>
     * if the destination responded to draggingEntered: but not to draggingUpdated: the return value from draggingEntered: is used<br>
     * <i>native declaration : line 48</i>
     */
    NSUInteger draggingUpdated(org.rococoa.ID sender);

    /**
     * Original signature : <code>void draggingExited(org.rococoa.ID)</code><br>
     * <i>native declaration : line 49</i>
     */
    void draggingExited(org.rococoa.ID sender);

    /**
     * Original signature : <code>BOOL prepareForDragOperation(org.rococoa.ID)</code><br>
     * <i>native declaration : line 50</i>
     */
    boolean prepareForDragOperation(org.rococoa.ID sender);

    /**
     * Original signature : <code>BOOL performDragOperation(org.rococoa.ID)</code><br>
     * <i>native declaration : line 51</i>
     */
    boolean performDragOperation(org.rococoa.ID sender);

    /**
     * Original signature : <code>void concludeDragOperation(org.rococoa.ID)</code><br>
     * <i>native declaration : line 52</i>
     */
    void concludeDragOperation(org.rococoa.ID sender);

    /**
     * draggingEnded: is implemented as of Mac OS 10.5<br>
     * Original signature : <code>void draggingEnded(org.rococoa.ID)</code><br>
     * <i>native declaration : line 54</i>
     */
    void draggingEnded(org.rococoa.ID sender);

    /**
     * the receiver of -wantsPeriodicDraggingUpdates should return NO if it does not require periodic -draggingUpdated messages (eg. not autoscrolling or otherwise dependent on draggingUpdated: sent while mouse is stationary)<br>
     * Original signature : <code>BOOL wantsPeriodicDraggingUpdates()</code><br>
     * <i>native declaration : line 57</i>
     */
    boolean wantsPeriodicDraggingUpdates();
}
