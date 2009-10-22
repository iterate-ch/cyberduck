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

import org.rococoa.ObjCClass;
import org.rococoa.cocoa.CGFloat;
import org.rococoa.cocoa.foundation.NSInteger;

/// <i>native declaration : :17</i>
public abstract class NSTableColumn extends NSObject {
    private static final _Class CLASS = org.rococoa.Rococoa.createClass("NSTableColumn", _Class.class);

    public static final int NSTableColumnNoResizing = 0; // Disallow any kind of resizing.
    public static final int NSTableColumnAutoresizingMask = (1 << 0);     // This column can be resized as the table is resized.
    public static final int NSTableColumnUserResizingMask = (1 << 1);     // The user can resize this column manually.

    public static NSTableColumn tableColumnWithIdentifier(String identifier) {
        return CLASS.alloc().initWithIdentifier(identifier);
    }

    public interface _Class extends ObjCClass {
        NSTableColumn alloc();
    }

    /**
     * Original signature : <code>id initWithIdentifier(id)</code><br>
     * <i>native declaration : :40</i>
     */
    public abstract NSTableColumn initWithIdentifier(String identifier);

    /**
     * Original signature : <code>void setIdentifier(id)</code><br>
     * <i>native declaration : :42</i>
     */
    public abstract void setIdentifier(String identifier);

    /**
     * Original signature : <code>id identifier()</code><br>
     * <i>native declaration : :43</i>
     */
    public abstract String identifier();

    /**
     * Original signature : <code>void setTableView(NSTableView*)</code><br>
     * <i>native declaration : :44</i>
     */
    public abstract void setTableView(NSTableView tableView);

    /**
     * Original signature : <code>NSTableView* tableView()</code><br>
     * <i>native declaration : :45</i>
     */
    public abstract NSTableView tableView();

    /**
     * Original signature : <code>void setWidth(CGFloat)</code><br>
     * <i>native declaration : :46</i>
     */
    public abstract void setWidth(CGFloat width);

    public void setWidth(double width) {
        this.setWidth(new CGFloat(width));
    }

    /**
     * Original signature : <code>CGFloat width()</code><br>
     * <i>native declaration : :47</i>
     */
    public abstract CGFloat width();

    /**
     * Original signature : <code>void setMinWidth(CGFloat)</code><br>
     * <i>native declaration : :48</i>
     */
    public abstract void setMinWidth(CGFloat width);

    public void setMinWidth(double width) {
        this.setMinWidth(new CGFloat(width));
    }

    /**
     * Original signature : <code>CGFloat minWidth()</code><br>
     * <i>native declaration : :49</i>
     */
    public abstract CGFloat minWidth();

    /**
     * Original signature : <code>void setMaxWidth(CGFloat)</code><br>
     * <i>native declaration : :50</i>
     */
    public abstract void setMaxWidth(CGFloat maxWidth);

    public void setMaxWidth(double width) {
        this.setMaxWidth(new CGFloat(width));
    }

    /**
     * Original signature : <code>CGFloat maxWidth()</code><br>
     * <i>native declaration : :51</i>
     */
    public abstract CGFloat maxWidth();

    /**
     * Original signature : <code>void setHeaderCell(NSCell*)</code><br>
     * Manage the cell used to draw the header for this column<br>
     * <i>native declaration : :53</i>
     */
    public abstract void setHeaderCell(NSCell cell);

    /**
     * Original signature : <code>id headerCell()</code><br>
     * <i>native declaration : :54</i>
     */
    public abstract NSCell headerCell();

    /**
     * Manage the cell used to draw the actual values in the column. NSTableView will call -dataCellForRow:. By default, -dataCellForRow: just calls -dataCell.  Subclassers can override -dataCellForRow: if they need to potentially use different cells for different rows. The returned cell should properly implement copyWithZone:, since NSTableView may make copies of the cells.<br>
     * Original signature : <code>void setDataCell(NSCell*)</code><br>
     * <i>native declaration : :58</i>
     */
    public abstract void setDataCell(NSCell cell);

    /**
     * Original signature : <code>id dataCell()</code><br>
     * <i>native declaration : :59</i>
     */
    public abstract NSCell dataCell();

    /**
     * Original signature : <code>id dataCellForRow(NSInteger)</code><br>
     * <i>native declaration : :60</i>
     */
    public abstract NSCell dataCellForRow(NSInteger row);

    /**
     * Original signature : <code>void setEditable(BOOL)</code><br>
     * <i>native declaration : :62</i>
     */
    public abstract void setEditable(boolean flag);

    /**
     * Original signature : <code>BOOL isEditable()</code><br>
     * <i>native declaration : :63</i>
     */
    public abstract boolean isEditable();

    /**
     * Original signature : <code>void sizeToFit()</code><br>
     * <i>native declaration : :64</i>
     */
    public abstract void sizeToFit();

    /**
     * A column is considered sortable if it has a sortDescriptorPrototype.  This prototype defines several things about the columns sorting.  The prototype's ascending value defines the default sorting direction.  Its key defines an arbitrary attribute which helps clients identify what to sort, while the selector defines how to sort.  Note that, it is not required that the key be the same as the identifier.  However, the key must be unique from the key used by other columns.  The sortDescriptor is archived.<br>
     * Original signature : <code>void setSortDescriptorPrototype(NSSortDescriptor*)</code><br>
     * <i>native declaration : :70</i>
     */
    public abstract void setSortDescriptorPrototype(com.sun.jna.Pointer sortDescriptor);

    /**
     * Original signature : <code>NSSortDescriptor* sortDescriptorPrototype()</code><br>
     * <i>native declaration : :71</i>
     */
    public abstract com.sun.jna.Pointer sortDescriptorPrototype();

    /**
     * The resizing mask controls the resizability of a table column.  Compatability Note: This method replaces setResizable.<br>
     * Original signature : <code>void setResizingMask(NSUInteger)</code><br>
     * <i>native declaration : :78</i>
     */
    public abstract void setResizingMask(int resizingMask);

    /**
     * Original signature : <code>NSUInteger resizingMask()</code><br>
     * <i>native declaration : :79</i>
     */
    public abstract int resizingMask();
}
