package ch.cyberduck.ui.cocoa;

/*
 *  Copyright (c) 2005 David Kocher. All rights reserved.
 *  http://cyberduck.ch/
 *
 *  This program is free software; you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation; either version 2 of the License, or
 *  (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  Bug fixes, suggestions and comments should be sent to:
 *  dkocher@cyberduck.ch
 */

import com.apple.cocoa.application.NSCell;
import com.apple.cocoa.application.NSOutlineView;
import com.apple.cocoa.application.NSTableColumn;
import com.apple.cocoa.application.NSTableView;
import com.apple.cocoa.foundation.NSMutableRect;
import com.apple.cocoa.foundation.NSNotification;
import com.apple.cocoa.foundation.NSPoint;

import ch.cyberduck.core.NullComparator;
import ch.cyberduck.core.Preferences;

import org.apache.log4j.Logger;

import java.util.Comparator;

/**
 * @version $Id$
 */
public abstract class CDAbstractTableDelegate<E> extends CDController implements CDTableDelegate<E> {
    private static Logger log = Logger.getLogger(CDAbstractTableDelegate.class);

    private NSTableColumn selectedColumn;

    protected void setSelectedColumn(NSTableColumn selectedColumn) {
        this.selectedColumn = selectedColumn;
        // Update the default value
        Preferences.instance().setProperty("browser.sort.column", this.selectedColumnIdentifier());
    }

    /**
     * @return The identifier of the column selected or the default sorting column if no selection
     */
    protected String selectedColumnIdentifier() {
        if(null == this.selectedColumn) {
            //return default value
            return Preferences.instance().getProperty("browser.sort.column");
        }
        //return previously set custom sorting preference
        return (String) this.selectedColumn.identifier();
    }

    /**
     * @return By default no column is editable. To be overriden in subclasses
     */
    public boolean isColumnEditable(NSTableColumn column) {
        return false;
    }

    /**
     * @see NSTableView.DataSource
     */
    public boolean tableViewShouldSelectRow(NSTableView view, int rowIndex) {
        return true;
    }

    /**
     * @see NSTableView.DataSource
     */
    public boolean outlineViewShouldSelectItem(NSOutlineView view, Object item) {
        return true;
    }

    /**
     * @see NSTableView.DataSource
     */
    public boolean tableViewShouldEditLocation(NSTableView view, NSTableColumn tableColumn, int row) {
        return false;
    }

    /**
     * @see NSTableView.DataSource
     */
    public boolean outlineViewShouldEditTableColumn(NSOutlineView view, NSTableColumn tableColumn, Object item) {
        return false;
    }

    public boolean selectionShouldChange() {
        return true;
    }

    /**
     * @see NSTableView.DataSource
     */
    public boolean selectionShouldChangeInTableView(NSTableView view) {
        return this.selectionShouldChange();
    }

    /**
     * @see NSOutlineView.DataSource
     */
    public boolean selectionShouldChangeInOutlineView(NSTableView view) {
        return this.selectionShouldChange();
    }

    public abstract void tableColumnClicked(NSTableView view, NSTableColumn tableColumn);

    /**
     * @see NSOutlineView.Delegate
     */
    public void outlineViewDidClickTableColumn(NSOutlineView view, NSTableColumn tableColumn) {
        this.tableColumnClicked(view, tableColumn);
    }

    /**
     * @see NSTableView.Delegate
     */
    public void tableViewDidClickTableColumn(NSOutlineView view, NSTableColumn tableColumn) {
        this.tableColumnClicked(view, tableColumn);
    }

    public abstract void tableRowDoubleClicked(final Object sender);

    /**
     * @see NSTableView.Notifications
     */
    public void tableViewSelectionDidChange(NSNotification notification) {
        this.selectionDidChange(notification);
    }


    public void tableViewSelectionIsChanging(NSNotification notification) {
        this.selectionIsChanging(notification);
    }

    /**
     * @see NSOutlineView.Notifications
     */
    public void outlineViewSelectionDidChange(NSNotification notification) {
        this.selectionDidChange(notification);
    }

    /**
     * @see NSOutlineView.Notifications
     */
    public void outlineViewSelectionIsChanging(NSNotification notification) {
        this.selectionIsChanging(notification);
    }

    public abstract void selectionDidChange(NSNotification notification);

    public void selectionIsChanging(NSNotification notification) {
        ;
    }

    /**
     * @see NSOutlineView.Delegate
     */
    public String outlineViewToolTipForCell(NSOutlineView view, NSCell cell, NSMutableRect rect, NSTableColumn tableColumn,
                                            final E item, NSPoint mouseLocation) {
        return this.tooltip(item);
    }

    // ----------------------------------------------------------
    // Sorting
    // ----------------------------------------------------------

    public Comparator<E> getSortingComparator() {
        return new NullComparator<E>();
    }

    private Boolean sortAscending;

    public void setSortedAscending(boolean sortAscending) {
        //cache custom sorting preference
        this.sortAscending = sortAscending;
        //set default value
        Preferences.instance().setProperty("browser.sort.ascending", this.sortAscending.booleanValue());
    }

    public boolean isSortedAscending() {
        if(null == this.sortAscending) {
            //return default value
            return Preferences.instance().getBoolean("browser.sort.ascending");
        }
        return this.sortAscending;
    }
}
