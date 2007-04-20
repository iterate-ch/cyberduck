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
import ch.cyberduck.core.Permission;
import ch.cyberduck.core.Preferences;
import ch.cyberduck.core.Status;

import com.apple.cocoa.application.NSOutlineView;
import com.apple.cocoa.application.NSTableColumn;
import com.apple.cocoa.application.NSTableView;
import com.apple.cocoa.foundation.NSNotification;
import com.apple.cocoa.foundation.NSObject;

import org.apache.log4j.Logger;

import java.text.Collator;
import java.util.Comparator;
import java.util.Locale;

/**
 * @version $Id$
 */
public abstract class CDAbstractTableDelegate extends NSObject implements CDTableDelegate {
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
     * @return A tooltip string containing the size and modification date of the path
     */
    protected String tooltipForPath(Path p) {
        return p.getAbsolute() + "\n"
                + Status.getSizeAsString(p.attributes.getSize()) + "\n"
                + CDDateFormatter.getLongFormat(p.attributes.getModificationDate(), p.getHost().getTimezone());
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

    /**
     * @see NSOutlineView.Notifications
     */
    public void outlineViewSelectionDidChange(NSNotification notification) {
        this.selectionDidChange(notification);
    }

    public abstract void selectionDidChange(NSNotification notification);

    // ----------------------------------------------------------
    // Sorting
    // ----------------------------------------------------------

    private Boolean sortAscending;

    public void setSortedAscending(boolean sortAscending) {
        //cache custom sorting preference
        this.sortAscending = Boolean.valueOf(sortAscending);
        //set default value
        Preferences.instance().setProperty("browser.sort.ascending", this.sortAscending.booleanValue());
    }

    public boolean isSortedAscending() {
        if(null == this.sortAscending) {
            //return default value
            return Preferences.instance().getBoolean("browser.sort.ascending");
        }
        return this.sortAscending.booleanValue();
    }

    public Comparator getSortingComparator() {
        final boolean ascending = this.isSortedAscending();
        String identifier = this.selectedColumnIdentifier();
        if(identifier.equals(CDBrowserTableDataSource.ICON_COLUMN)
                || identifier.equals(CDBrowserTableDataSource.KIND_COLUMN)) {
            return new FileTypeComparator(ascending);
        }
        else if(identifier.equals(CDBrowserTableDataSource.FILENAME_COLUMN)) {
            return new FilenameComparator(ascending);
        }
        else if(identifier.equals(CDBrowserTableDataSource.SIZE_COLUMN)) {
            return new SizeComparator(ascending);
        }
        else if(identifier.equals(CDBrowserTableDataSource.MODIFIED_COLUMN)) {
            return new TimestampComparator(ascending);
        }
        else if(identifier.equals(CDBrowserTableDataSource.OWNER_COLUMN)) {
            return new OwnerComparator(ascending);
        }
        else if(identifier.equals(CDBrowserTableDataSource.PERMISSIONS_COLUMN)) {
            return new PermissionsComparator(ascending);
        }
        log.error("Unknown column identifier:" + identifier);
        return null;
    }

    private class FileTypeComparator extends BrowserComparator {
        private Collator impl = Collator.getInstance(Locale.getDefault());

        public FileTypeComparator(boolean ascending) {
            super(ascending);
        }

        public int compare(Object o1, Object o2) {
            Path p1 = (Path) o1;
            Path p2 = (Path) o2;
            if((p1.attributes.isDirectory() && p2.attributes.isDirectory())
                    || p1.attributes.isFile() && p2.attributes.isFile()) {
                if(ascending) {
                    return impl.compare(p1.kind(), p2.kind());
                }
                return -impl.compare(p1.kind(), p2.kind());
            }
            if(p1.attributes.isFile()) {
                return ascending ? 1 : -1;
            }
            return ascending ? -1 : 1;
        }

        public String toString() {
            return CDBrowserTableDataSource.ICON_COLUMN;
        }
    }

    private class FilenameComparator extends BrowserComparator {
        private Collator impl = Collator.getInstance(Locale.getDefault());

        public FilenameComparator(boolean ascending) {
            super(ascending);
        }

        public int compare(Object o1, Object o2) {
            Path p1 = (Path) o1;
            Path p2 = (Path) o2;
            if(ascending) {
                return impl.compare(p1.getName(), p2.getName());
            }
            return -impl.compare(p1.getName(), p2.getName());
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
            if(p1 > p2) {
                return ascending ? 1 : -1;
            }
            else if(p1 < p2) {
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
            long d1 = p1.attributes.getModificationDate();
            if(-1 == d1) {
                return 0;
            }
            long d2 = p2.attributes.getModificationDate();
            if(-1 == d2) {
                return 0;
            }
            if(ascending) {
                return d1 > d2 ? 1 : -1;
            }
            return d1 > d2 ? -1 : 1;
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
            if(ascending) {
                return p1.attributes.getOwner().compareToIgnoreCase(p2.attributes.getOwner());
            }
            return -p1.attributes.getOwner().compareToIgnoreCase(p2.attributes.getOwner());
        }

        public String toString() {
            return CDBrowserTableDataSource.OWNER_COLUMN;
        }
    }

    private class PermissionsComparator extends BrowserComparator {

        public PermissionsComparator(boolean ascending) {
            super(ascending);
        }

        public int compare(Object o1, Object o2) {
            Permission perm1 = ((Path) o1).attributes.getPermission();
            if(null == perm1) {
                perm1 = Permission.EMPTY;
            }
            Permission perm2 = ((Path) o2).attributes.getPermission();
            if(null == perm2) {
                perm2 = Permission.EMPTY;
            }
            int p1 = perm1.getOctalNumber();
            int p2 = perm2.getOctalNumber();
            if(p1 > p2) {
                return ascending ? 1 : -1;
            }
            else if(p1 < p2) {
                return ascending ? -1 : 1;
            }
            return 0;
        }

        public String toString() {
            return CDBrowserTableDataSource.PERMISSIONS_COLUMN;
        }
    }
}
