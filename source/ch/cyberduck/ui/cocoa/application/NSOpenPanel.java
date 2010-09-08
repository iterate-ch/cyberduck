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

import org.rococoa.ID;
import org.rococoa.ObjCClass;
import org.rococoa.Selector;

/// <i>native declaration : :14</i>

public abstract class NSOpenPanel extends NSSavePanel {
    static final _Class CLASS = org.rococoa.Rococoa.createClass("NSOpenPanel", _Class.class);

    public static NSOpenPanel openPanel() {
        return CLASS.openPanel();
    }

    public interface _Class extends ObjCClass {
        /**
         * Original signature : <code>NSOpenPanel* openPanel()</code><br>
         * <i>native declaration : :19</i>
         */
        NSOpenPanel openPanel();
    }

    /**
     * Original signature : <code>NSArray* URLs()</code><br>
     * <i>native declaration : :21</i>
     */
    public abstract NSArray URLs();

    /**
     * Original signature : <code>NSArray* filenames()</code><br>
     * <i>native declaration : :22</i>
     */
    public abstract NSArray filenames();

    /**
     * Original signature : <code>BOOL resolvesAliases()</code><br>
     * <i>native declaration : :24</i>
     */
    public abstract boolean resolvesAliases();

    /**
     * Original signature : <code>void setResolvesAliases(BOOL)</code><br>
     * <i>native declaration : :25</i>
     */
    public abstract void setResolvesAliases(boolean flag);

    /**
     * Original signature : <code>BOOL canChooseDirectories()</code><br>
     * <i>native declaration : :27</i>
     */
    public abstract boolean canChooseDirectories();

    /**
     * Original signature : <code>void setCanChooseDirectories(BOOL)</code><br>
     * <i>native declaration : :28</i>
     */
    public abstract void setCanChooseDirectories(boolean flag);

    /**
     * Original signature : <code>BOOL allowsMultipleSelection()</code><br>
     * <i>native declaration : :30</i>
     */
    public abstract boolean allowsMultipleSelection();

    /**
     * Original signature : <code>void setAllowsMultipleSelection(BOOL)</code><br>
     * <i>native declaration : :31</i>
     */
    public abstract void setAllowsMultipleSelection(boolean flag);

    /**
     * Original signature : <code>BOOL canChooseFiles()</code><br>
     * <i>native declaration : :33</i>
     */
    public abstract boolean canChooseFiles();

    /**
     * Original signature : <code>void setCanChooseFiles(BOOL)</code><br>
     * <i>native declaration : :34</i>
     */
    public abstract void setCanChooseFiles(boolean flag);

    /**
     * Private
     *
     * @param show
     */
    public abstract void setShowsHiddenFiles(boolean show);

    /**
     * <i>from NSOpenPanelRuntime native declaration : :40</i><br>
     * Conversion Error : /// Original signature : <code>void beginSheetForDirectory(NSString*, NSString*, NSArray*, NSWindow*, null, null, void*)</code><br>
     * - (void)beginSheetForDirectory:(NSString*)path file:(NSString*)name types:(NSArray*)fileTypes
     * modalForWindow:(NSWindow*)docWindow modalDelegate:(null)delegate
     * didEndSelector:(null)didEndSelector contextInfo:(void*)contextInfo; (Argument delegate cannot be converted)
     */
    public abstract void beginSheetForDirectory_file_types_modalForWindow_modalDelegate_didEndSelector_contextInfo(
            String path, String name, NSArray fileTypes, NSWindow docWindow, NSObject delegate, Selector didEndSelector, ID contextInfo
    );
    /**
     * <i>from NSOpenPanelRuntime native declaration : :43</i><br>
     * Conversion Error : /// Original signature : <code>void beginForDirectory(NSString*, NSString*, NSArray*, null, null, void*)</code><br>
     * - (void)beginForDirectory:(NSString*)path file:(NSString*)name types:(NSArray*)fileTypes modelessDelegate:(null)delegate didEndSelector:(null)didEndSelector contextInfo:(void*)contextInfo; (Argument delegate cannot be converted)
     */
    /**
     * Original signature : <code>NSInteger runModalForDirectory(NSString*, NSString*, NSArray*)</code><br>
     * <i>from NSOpenPanelRuntime native declaration : :46</i>
     */
    public abstract int runModalForDirectory_file_types(String path, String name, NSArray fileTypes);

    /**
     * Original signature : <code>NSInteger runModalForTypes(NSArray*)</code><br>
     * <i>from NSOpenPanelRuntime native declaration : :47</i>
     */
    public abstract int runModalForTypes(NSArray fileTypes);
}
