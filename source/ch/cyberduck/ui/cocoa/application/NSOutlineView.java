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

import ch.cyberduck.ui.cocoa.foundation.NSObject;

import org.rococoa.cocoa.CGFloat;

/// <i>native declaration : :73</i>
public interface NSOutlineView extends NSTableView {
    static final _Class CLASS = org.rococoa.Rococoa.createClass("NSOutlineView", _Class.class);

    public static final int NSOutlineViewDropOnItemIndex = -1;

    public static interface DataSource {
        public int outlineView_numberOfChildrenOfItem(final NSOutlineView view, NSObject item);

        public NSObject outlineView_child_ofItem(final NSOutlineView outlineView, int index, NSObject item);

        public void outlineView_setObjectValue_forTableColumn_byItem(final NSOutlineView outlineView, NSObject value,
                                                                     final NSTableColumn tableColumn, NSObject item);

        public NSObject outlineView_objectValueForTableColumn_byItem(final NSOutlineView outlineView, final NSTableColumn tableColumn, NSObject item);

    }

    public interface _Class extends org.rococoa.NSClass {
        NSOutlineView alloc();
    }

    /**
     * The 'outlineTableColumn' is the column that displays data in a hierarchical fashion, indented one identationlevel per level, decorated with indentation marker (disclosure triangle) on rows that are expandable. On MacOS 10.5, this value is saved in encodeWithCoder: and restored in initWithCoder:;<br>
     * Original signature : <code>void setOutlineTableColumn(NSTableColumn*)</code><br>
     * <i>native declaration : :103</i>
     */
    void setOutlineTableColumn(NSTableColumn outlineTableColumn);

    /**
     * Original signature : <code>NSTableColumn* outlineTableColumn()</code><br>
     * <i>native declaration : :104</i>
     */
    NSTableColumn outlineTableColumn();

    /**
     * Returns YES if 'item' is expandable and can contain other items. May call out to the delegate, if required.<br>
     * Original signature : <code>BOOL isExpandable(id)</code><br>
     * <i>native declaration : :108</i>
     */
    boolean isExpandable(NSObject item);

    /**
     * Expands 'item', if not already expanded, and all children if 'expandChildren' is YES. On 10.5 and higher, passing 'nil' for 'item' will expand  each item under the root.<br>
     * Original signature : <code>void expandItem(id, BOOL)</code><br>
     * <i>native declaration : :112</i>
     */
    void expandItem_expandChildren(NSObject item, boolean expandChildren);

    /**
     * Calls expandItem:expandChildren with 'expandChildren == NO'<br>
     * Original signature : <code>void expandItem(id)</code><br>
     * <i>native declaration : :116</i>
     */
    void expandItem(NSObject item);

    /**
     * Collapses 'item' and all children if 'collapseChildren' is YES. On 10.5 and higher, passing 'nil' for 'item' will collapse each item under the root.<br>
     * Original signature : <code>void collapseItem(id, BOOL)</code><br>
     * <i>native declaration : :120</i>
     */
    void collapseItem_collapseChildren(NSObject item, boolean collapseChildren);

    /**
     * Calls collapseItem:collapseChildren with 'collapseChildren == NO'<br>
     * Original signature : <code>void collapseItem(id)</code><br>
     * <i>native declaration : :124</i>
     */
    void collapseItem(NSObject item);

    /**
     * Reloads 'item' and all children if 'reloadChildren' is YES. On 10.5 and higher, passing 'nil' for 'item' will reload everything under the root item.<br>
     * Original signature : <code>void reloadItem(id, BOOL)</code><br>
     * <i>native declaration : :128</i>
     */
    void reloadItem_reloadChildren(NSObject item, boolean reloadChildren);

    /**
     * Calls reloadItem:reloadChildren with 'reloadChildren == NO'<br>
     * Original signature : <code>void reloadItem(id)</code><br>
     * <i>native declaration : :132</i>
     */
    void reloadItem(NSObject item);

    /**
     * Returns the parent for 'item', or nil, if the parent is the root.<br>
     * Original signature : <code>id parentForItem(id)</code><br>
     * <i>native declaration : :138</i>
     */
    NSObject parentForItem(NSObject item);

    /**
     * Item/Row translation<br>
     * Original signature : <code>id itemAtRow(NSInteger)</code><br>
     * <i>native declaration : :144</i>
     */
    NSObject itemAtRow(int row);

    /**
     * Original signature : <code>NSInteger rowForItem(id)</code><br>
     * <i>native declaration : :145</i>
     */
    int rowForItem(NSObject item);

    /**
     * Indentation<br>
     * Original signature : <code>NSInteger levelForItem(id)</code><br>
     * <i>native declaration : :149</i>
     */
    int levelForItem(NSObject item);

    /**
     * Original signature : <code>NSInteger levelForRow(NSInteger)</code><br>
     * <i>native declaration : :150</i>
     */
    int levelForRow(int row);

    /**
     * Original signature : <code>BOOL isItemExpanded(id)</code><br>
     * <i>native declaration : :151</i>
     */
    boolean isItemExpanded(NSObject item);

    /**
     * The indentation amount per level defaults to 16.0.<br>
     * Original signature : <code>void setIndentationPerLevel(CGFloat)</code><br>
     * <i>native declaration : :155</i>
     */
    void setIndentationPerLevel(CGFloat indentationPerLevel);

    /**
     * Original signature : <code>CGFloat indentationPerLevel()</code><br>
     * <i>native declaration : :156</i>
     */
    CGFloat indentationPerLevel();

    /**
     * The indentation marker is the visual indicator that shows an item is expandable (i.e. disclosure triangle). The default value is YES.<br>
     * Original signature : <code>void setIndentationMarkerFollowsCell(BOOL)</code><br>
     * <i>native declaration : :160</i>
     */
    void setIndentationMarkerFollowsCell(boolean drawInCell);

    /**
     * Original signature : <code>BOOL indentationMarkerFollowsCell()</code><br>
     * <i>native declaration : :161</i>
     */
    boolean indentationMarkerFollowsCell();

    /**
     * Original signature : <code>void setAutoresizesOutlineColumn(BOOL)</code><br>
     * <i>native declaration : :163</i>
     */
    void setAutoresizesOutlineColumn(boolean resize);

    /**
     * Original signature : <code>BOOL autoresizesOutlineColumn()</code><br>
     * <i>native declaration : :164</i>
     */
    boolean autoresizesOutlineColumn();
    /**
     * <i>native declaration : :170</i><br>
     * Conversion Error : NSRect
     */
    /**
     * To be used from validateDrop: in order to "re-target" the proposed drop.  To specify a drop on an item I, one would specify item=I, and index=NSOutlineViewDropOnItemIndex.  To specify a drop between child 2 and 3 of an item I, on would specify item=I, and index=3 (children are zero-base indexed).  To specify a drop on an un-expandable item I, one would specify item=I, and index=NSOutlineViewDropOnItemIndex.<br>
     * Original signature : <code>void setDropItem(id, NSInteger)</code><br>
     * <i>native declaration : :179</i>
     */
    void setDropItem_dropChildIndex(NSObject item, int index);

    /**
     * This method returns YES to indicate that auto expanded items should return to their original collapsed state.  Override this method to provide custom behavior.  'deposited' tells wether or not the drop terminated due to a successful drop (as indicated by the return value from acceptDrop:).  Note that exiting the view will be treated the same as a failed drop.<br>
     * Original signature : <code>BOOL shouldCollapseAutoExpandedItemsForDeposited(BOOL)</code><br>
     * <i>native declaration : :183</i>
     */
    boolean shouldCollapseAutoExpandedItemsForDeposited(boolean deposited);

    /**
     * Persistence. The value for autosaveExpandedItems is saved out in the nib file on Mac OS 10.5 or higher. The default value is NO. Calling setAutosaveExpandedItems:YES requires you to implement outlineView:itemForPersistentObject: and outlineView:persistentObjectForItem:.<br>
     * Original signature : <code>BOOL autosaveExpandedItems()</code><br>
     * <i>native declaration : :187</i>
     */
    boolean autosaveExpandedItems();

    /**
     * Original signature : <code>void setAutosaveExpandedItems(BOOL)</code><br>
     * <i>native declaration : :188</i>
     */
    void setAutosaveExpandedItems(boolean save);
}
