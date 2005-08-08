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
import com.apple.cocoa.application.*;
import com.apple.cocoa.foundation.NSIndexSet;
import org.apache.log4j.Logger;

import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;

/**
 * @version $Id$
 */
public abstract class CDBrowserTableDataSource {//implements NSTableView.DataSource {
    private static Logger log = Logger.getLogger(CDBrowserTableDataSource.class);

    protected static final NSImage SYMLINK_ICON = NSImage.imageNamed("symlink.tiff");
    protected static final NSImage FOLDER_ICON = NSImage.imageNamed("folder16.tiff");
    protected static final NSImage NOT_FOUND_ICON = NSImage.imageNamed("notfound.tiff");

    public static final String TYPE_COLUMN = "TYPE";
    public static final String FILENAME_COLUMN = "FILENAME";
    public static final String SIZE_COLUMN = "SIZE";
    public static final String MODIFIED_COLUMN = "MODIFIED";
    public static final String OWNER_COLUMN = "OWNER";
    public static final String PERMISSIONS_COLUMN = "PERMISSIONS";

    protected List childs(Path path) {
        return path.list(false, controller.getEncoding(), false,
                this.getComparator(), controller.getFileFilter());
    }

    protected CDBrowserController controller;

    public CDBrowserTableDataSource(CDBrowserController controller) {
        this.controller = controller;
    }

    // ----------------------------------------------------------
    // Sorting
    // ----------------------------------------------------------

    private Boolean sortAscending;

    protected void setSortedAscending(boolean sortAscending) {
        //cache custom sorting preference
        this.sortAscending = new Boolean(sortAscending);
        //set default value
        Preferences.instance().setProperty("browser.sort.ascending", sortAscending);
    }

    protected boolean isSortedAscending() {
        if (null == this.sortAscending) {
            //return default value
            return Preferences.instance().getBoolean("browser.sort.ascending");
        }
        return this.sortAscending.booleanValue();
    }

    private NSTableColumn selectedColumn;

    private void setSelectedColumn(NSTableColumn selectedColumn) {
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

    public void sort(List files) {
        if (files != null) {
            Collections.sort(files, this.getComparator());
        }
    }

    public Comparator getComparator() {
        final boolean ascending = this.isSortedAscending();
        String identifier = this.selectedColumnIdentifier();
        if (identifier.equals(TYPE_COLUMN)) {
            return new FileTypeComparator(ascending);
        }
        else if (identifier.equals(FILENAME_COLUMN)) {
            return new FilenameComparator(ascending);
        }
        else if (identifier.equals(SIZE_COLUMN)) {
            return new SizeComparator(ascending);
        }
        else if (identifier.equals(MODIFIED_COLUMN)) {
            return new TimestampComparator(ascending);
        }
        else if (identifier.equals(OWNER_COLUMN)) {
            return new OwnerComparator(ascending);
        }
        else if (identifier.equals(PERMISSIONS_COLUMN)) {
            return new PermissionsComparator(ascending);
        }
        log.error("Unknown column identifier:" + identifier);
        return null;
    }

    // ----------------------------------------------------------
    // TableView/OutlineView Delegate methods
    // ----------------------------------------------------------


    public boolean selectionShouldChangeInTableView(NSTableView tableView) {
        return true;
    }

    public boolean selectionShouldChangeInOutlineView(NSTableView tableView) {
        return true;
    }


    public void outlineViewDidClickTableColumn(NSOutlineView tableView, NSTableColumn tableColumn) {
        this.tableViewDidClickTableColumn(tableView, tableColumn);
    }

    public void tableViewDidClickTableColumn(NSOutlineView tableView, NSTableColumn tableColumn) {
        List selected = controller.getSelectedPaths();
        if (this.selectedColumnIdentifier().equals(tableColumn.identifier())) {
            this.setSortedAscending(!this.isSortedAscending());
        }
        else {
            tableView.setIndicatorImage(null, tableView.tableColumnWithIdentifier(this.selectedColumnIdentifier()));
            this.setSelectedColumn(tableColumn);
        }
        tableView.setIndicatorImage(this.isSortedAscending() ?
                NSImage.imageNamed("NSAscendingSortIndicator") :
                NSImage.imageNamed("NSDescendingSortIndicator"),
                tableColumn);
        tableView.deselectAll(null);
        tableView.reloadData();
        for (Iterator i = selected.iterator(); i.hasNext();) {
            tableView.selectRowIndexes(new NSIndexSet(this.indexOf(tableView, (Path)i.next())), true);
        }
    }

    public int indexOf(NSTableView tableView, Path p) {
        return this.childs(controller.workdir()).indexOf(p);
    }


    /**
     * Returns true to permit aTableView to select the row at rowIndex, false to deny permission.
     * The delegate can implement this method to disallow selection of particular rows.
     */
    public boolean tableViewShouldSelectRow(NSTableView aTableView, int rowIndex) {
        return true;
    }

    public boolean outlineViewShouldSelectItem(NSOutlineView outlineView, Object item) {
        return true;
    }


    /**
     * Returns true to permit aTableView to edit the cell at rowIndex in aTableColumn, false to deny permission.
     * The delegate can implemen this method to disallow editing of specific cells.
     */
    public boolean tableViewShouldEditLocation(NSTableView view, NSTableColumn tableColumn, int row) {
        return false;
    }

    // ----------------------------------------------------------
    //	NSDraggingSource
    // ----------------------------------------------------------

    public boolean ignoreModifierKeysWhileDragging() {
        return false;
    }

    public int draggingSourceOperationMaskForLocal(boolean local) {
        return NSDraggingInfo.DragOperationMove | NSDraggingInfo.DragOperationCopy;
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
            return TYPE_COLUMN;
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
            return FILENAME_COLUMN;
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
            return SIZE_COLUMN;
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
            return MODIFIED_COLUMN;
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
            return TYPE_COLUMN;
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
            return PERMISSIONS_COLUMN;
        }
    }
}
