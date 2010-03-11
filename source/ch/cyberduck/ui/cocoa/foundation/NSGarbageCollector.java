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
import org.rococoa.Rococoa;

public abstract class NSGarbageCollector extends NSObject {
    private static final _Class CLASS = Rococoa.createClass("NSGarbageCollector", _Class.class);

    public interface _Class extends ObjCClass {
        /**
         * Returns nil if this process is not running with garbage collection<br>
         * Original signature : <code>+(id)defaultCollector</code><br>
         * <i>native declaration : line 21</i>
         */
        public abstract NSGarbageCollector defaultCollector();
    }

    /**
     * Returns nil if this process is not running with garbage collection<br>
     * Original signature : <code>+(id)defaultCollector</code><br>
     * <i>native declaration : line 21</i>
     */
    public static NSGarbageCollector defaultCollector() {
        return CLASS.defaultCollector();
    }

    /**
     * Original signature : <code>-(BOOL)isCollecting</code><br>
     * <i>native declaration : line 24</i>
     */
    public abstract boolean isCollecting();

    /**
     * Original signature : <code>-(void)disable</code><br>
     * temporarily disable collections<br>
     * <i>native declaration : line 26</i>
     */
    public abstract void disable();

    /**
     * Original signature : <code>-(void)enable</code><br>
     * reenable disabled collections (must be called once per call to disableCollector)<br>
     * <i>native declaration : line 27</i>
     */
    public abstract void enable();

    /**
     * Original signature : <code>-(BOOL)isEnabled</code><br>
     * <i>native declaration : line 28</i>
     */
    public abstract boolean isEnabled();

    /**
     * Original signature : <code>-(void)collectIfNeeded</code><br>
     * collects if thresholds crossed, but subject to interruption on user input<br>
     * <i>native declaration : line 30</i>
     */
    public abstract void collectIfNeeded();

    /**
     * Original signature : <code>-(void)collectExhaustively</code><br>
     * collects iteratively, but subject to interruption on user input<br>
     * <i>native declaration : line 31</i>
     */
    public abstract void collectExhaustively();

    /**
     * references outside the heap, globals, and the stack, e.g. unscanned memory, malloc memory, must be tracked by the collector<br>
     * Original signature : <code>-(void)disableCollectorForPointer:(void*)</code><br>
     * this pointer will not be collected...<br>
     * <i>native declaration : line 34</i>
     */
    public abstract void disableCollectorForPointer(com.sun.jna.Pointer ptr);

    /**
     * Original signature : <code>-(void)enableCollectorForPointer:(void*)</code><br>
     * ...until this (stacking) call is made<br>
     * <i>native declaration : line 35</i>
     */
    public abstract void enableCollectorForPointer(com.sun.jna.Pointer ptr);
}