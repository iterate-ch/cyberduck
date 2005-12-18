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

import ch.cyberduck.core.BrowserComparator;
import ch.cyberduck.core.Path;
import ch.cyberduck.core.Preferences;

import com.apple.cocoa.application.NSOutlineView;
import com.apple.cocoa.application.NSTableColumn;
import com.apple.cocoa.application.NSTableView;

import org.apache.log4j.Logger;

import java.util.Comparator;

/**
 * @version $Id$
 */
public abstract class CDAbstractTableDelegate implements CDTableDelegate {
    private static Logger log = Logger.getLogger(CDAbstractTableDelegate.class);

    private NSTableColumn selectedColumn;

    protected void setSelectedColumn(NSTableColumn selectedColumn) {
        this.selectedColumn = selectedColumn;
        //set default value
        Preferences.instance().setProperty("browser.sort.column", this.selectedColumnIdentifier());
    }

    protected String selectedColumnIdentifier() {
        if (null == this.selectedColumn) {
            //return default value
            return Preferences.instance().getProperty("browser.sort.column");
        }
        //return previously set custom sorting preference
        return (String) this.selectedColumn.identifier();
    }

    public boolean isColumnEditable(NSTableColumn tableColumn) {
        return false;
    }

    // NSTableView.Delegate
    public boolean tableViewShouldSelectRow(NSTableView aTableView, int rowIndex) {
        return true;
    }

    // NSOutlineView.Delegate
    public boolean outlineViewShouldSelectItem(NSOutlineView outlineView, Object item) {
        return true;
    }

    // NSTableView.Delegate
    public boolean tableViewShouldEditLocation(NSTableView view, NSTableColumn tableColumn, int row) {
        return false;
    }

    // NSOutlineView.Delegate
    public boolean outlineViewShouldEditTableColumn(NSOutlineView view, NSTableColumn tableColumn, Object item) {
        return false;
    }

    public boolean selectionShouldChange() {
        return true;
    }

    // NSTableView.Delegate
    public boolean selectionShouldChangeInTableView(NSTableView tableView) {
        return this.selectionShouldChange();
    }

    // NSOutlineView.Delegate
    public boolean selectionShouldChangeInOutlineView(NSTableView tableView) {
        return this.selectionShouldChange();
    }

    public abstract void tableColumnClicked(NSTableView view, NSTableColumn tableColumn);

    public void outlineViewDidClickTableColumn(NSOutlineView view, NSTableColumn tableColumn) {
        this.tableColumnClicked(view, tableColumn);
    }

    public void tableViewDidClickTableColumn(NSOutlineView view, NSTableColumn tableColumn) {
        this.tableColumnClicked(view, tableColumn);
    }

    public abstract void tableRowDoubleClicked(Object sender);


    // ----------------------------------------------------------
    // Sorting
    // ----------------------------------------------------------

    private Boolean sortAscending;

    public void setSortedAscending(boolean sortAscending) {
        //cache custom sorting preference
        this.sortAscending = Boolean.valueOf(sortAscending);
        //set default value
        Preferences.instance().setProperty("browser.sort.ascending", sortAscending);
    }

    public boolean isSortedAscending() {
        if (null == this.sortAscending) {
            //return default value
            return Preferences.instance().getBoolean("browser.sort.ascending");
        }
        return this.sortAscending.booleanValue();
    }

    public Comparator getSortingComparator() {
        final boolean ascending = this.isSortedAscending();
        String identifier = this.selectedColumnIdentifier();
        if (identifier.equals(CDBrowserTableDataSource.TYPE_COLUMN)) {
            return new FileTypeComparator(ascending);
        }
        else if (identifier.equals(CDBrowserTableDataSource.FILENAME_COLUMN)) {
            return new FilenameComparator(ascending);
        }
        else if (identifier.equals(CDBrowserTableDataSource.SIZE_COLUMN)) {
            return new SizeComparator(ascending);
        }
        else if (identifier.equals(CDBrowserTableDataSource.MODIFIED_COLUMN)) {
            return new TimestampComparator(ascending);
        }
        else if (identifier.equals(CDBrowserTableDataSource.OWNER_COLUMN)) {
            return new OwnerComparator(ascending);
        }
        else if (identifier.equals(CDBrowserTableDataSource.PERMISSIONS_COLUMN)) {
            return new PermissionsComparator(ascending);
        }
        log.error("Unknown column identifier:" + identifier);
        return null;
    }

    private class FileTypeComparator extends BrowserComparator {

        public FileTypeComparator(boolean ascending) {
            super(ascending);
        }

        public int compare(Object o1, Object o2) {
            Path p1 = (Path) o1;
            Path p2 = (Path) o2;
            if (p1.attributes.isDirectory() && p2.attributes.isDirectory()) {
                return 0;
            }
            if (p1.attributes.isFile() && p2.attributes.isFile()) {
                return 0;
            }
            if (p1.attributes.isFile()) {
                return ascending ? 1 : -1;
            }
            return ascending ? -1 : 1;
        }

        public String toString() {
            return CDBrowserTableDataSource.TYPE_COLUMN;
        }
    }

    private class FilenameComparator extends BrowserComparator {

        public FilenameComparator(boolean ascending) {
            super(ascending);
        }

        public int compare(Object o1, Object o2) {
            Path p1 = (Path) o1;
            Path p2 = (Path) o2;
            if (ascending) {
                return p1.getName().compareToIgnoreCase(p2.getName());
            }
            return -p1.getName().compareToIgnoreCase(p2.getName());
        }

        public String toString() {
            return CDBrowserTableDataSource.FILENAME_COLUMN;
        }
    }

    private class SizeComparator extends BrowserComparator {

        public SizeComparator(boolean ascending) {
            super(ascending);
        }

        public int compare(Object o1, Object o2) {
            double p1 = ((Path) o1).attributes.getSize();
            double p2 = ((Path) o2).attributes.getSize();
            if (p1 > p2) {
                return ascending ? 1 : -1;
            }
            else if (p1 < p2) {
                return ascending ? -1 : 1;
            }
            return 0;
        }

        public String toString() {
            return CDBrowserTableDataSource.SIZE_COLUMN;
        }
    }

    private class TimestampComparator extends BrowserComparator {

        public TimestampComparator(boolean ascending) {
            super(ascending);
        }

        public int compare(Object o1, Object o2) {
            Path p1 = (Path) o1;
            Path p2 = (Path) o2;
            if (ascending) {
                return p1.attributes.getTimestamp().compareTo(p2.attributes.getTimestamp());
            }
            return -p1.attributes.getTimestamp().compareTo(p2.attributes.getTimestamp());
        }

        public String toString() {
            return CDBrowserTableDataSource.MODIFIED_COLUMN;
        }
    }

    private class OwnerComparator extends BrowserComparator {

        public OwnerComparator(boolean ascending) {
            super(ascending);
        }

        public int compare(Object o1, Object o2) {
            Path p1 = (Path) o1;
            Path p2 = (Path) o2;
            if (ascending) {
                return p1.attributes.getOwner().compareToIgnoreCase(p2.attributes.getOwner());
            }
            return -p1.attributes.getOwner().compareToIgnoreCase(p2.attributes.getOwner());
        }

        public String toString() {
            return CDBrowserTableDataSource.TYPE_COLUMN;
        }
    }

    private class PermissionsComparator extends BrowserComparator {

        public PermissionsComparator(boolean ascending) {
            super(ascending);
        }

        public int compare(Object o1, Object o2) {
            int p1 = Integer.parseInt(((Path) o1).attributes.getPermission().getOctalCode());
            int p2 = Integer.parseInt(((Path) o2).attributes.getPermission().getOctalCode());
            if (p1 > p2) {
                return ascending ? 1 : -1;
            }
            else if (p1 < p2) {
                return ascending ? -1 : 1;
            }
            return 0;
        }

        public String toString() {
            return CDBrowserTableDataSource.PERMISSIONS_COLUMN;
        }
    }
}
