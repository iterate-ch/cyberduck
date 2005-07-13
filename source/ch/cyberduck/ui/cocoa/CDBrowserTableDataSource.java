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

import com.apple.cocoa.application.*;
import com.apple.cocoa.foundation.NSIndexSet;

import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;

import ch.cyberduck.core.Path;
import ch.cyberduck.core.Preferences;

/**
 * @version $Id$
 */
public abstract class CDBrowserTableDataSource {//implements NSTableView.DataSource {

    protected static final NSImage SYMLINK_ICON = NSImage.imageNamed("symlink.tiff");
    protected static final NSImage FOLDER_ICON = NSImage.imageNamed("folder16.tiff");
    protected static final NSImage NOT_FOUND_ICON = NSImage.imageNamed("notfound.tiff");

    protected List childs(Path path) {
        //get cached directory listing
        List childs = path.list(controller.getEncoding(), //character encoding
                false, // do not refresh
                this.controller.getFileFilter(),
                false); // do not notify observers (important!)
        this.sort(childs, this.isSortedAscending());
        return childs;
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
        this.sortAscending = new Boolean(sortAscending);
        Preferences.instance().setProperty("browser.sort.ascending", sortAscending);
    }

    protected boolean isSortedAscending() {
        if (null == this.sortAscending) {
            return Preferences.instance().getBoolean("browser.sort.ascending");
        }
        return this.sortAscending.booleanValue();
    }

    private NSTableColumn selectedColumn;

    private void setSelectedColumn(NSTableColumn selectedColumn) {
        this.selectedColumn = selectedColumn;
        Preferences.instance().setProperty("browser.sort.column", this.selectedColumnIdentifier());
    }

    protected String selectedColumnIdentifier() {
        if (null == this.selectedColumn) {
            return Preferences.instance().getProperty("browser.sort.column");
        }
        return (String) this.selectedColumn.identifier();
    }

    public void sort(List files, final boolean ascending) {
        final int higher = ascending ? 1 : -1;
        final int lower = ascending ? -1 : 1;
        if (files != null) {
            if (this.selectedColumnIdentifier().equals("TYPE")) {
                Collections.sort(files,
                        new Comparator() {
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
                                    return higher;
                                }
                                return lower;
                            }
                        });
            }
            else if (this.selectedColumnIdentifier().equals("FILENAME")) {
                Collections.sort(files,
                        new Comparator() {
                            public int compare(Object o1, Object o2) {
                                Path p1 = (Path) o1;
                                Path p2 = (Path) o2;
                                if (ascending) {
                                    return p1.getName().compareToIgnoreCase(p2.getName());
                                }
                                return -p1.getName().compareToIgnoreCase(p2.getName());
                            }
                        });
            }
            else if (this.selectedColumnIdentifier().equals("SIZE")) {
                Collections.sort(files,
                        new Comparator() {
                            public int compare(Object o1, Object o2) {
                                double p1 = ((Path) o1).attributes.getSize();
                                double p2 = ((Path) o2).attributes.getSize();
                                if (p1 > p2) {
                                    return higher;
                                }
                                else if (p1 < p2) {
                                    return lower;
                                }
                                return 0;
                            }
                        });
            }
            else if (this.selectedColumnIdentifier().equals("MODIFIED")) {
                Collections.sort(files,
                        new Comparator() {
                            public int compare(Object o1, Object o2) {
                                Path p1 = (Path) o1;
                                Path p2 = (Path) o2;
                                if (ascending) {
                                    return p1.attributes.getTimestamp().compareTo(p2.attributes.getTimestamp());
                                }
                                return -p1.attributes.getTimestamp().compareTo(p2.attributes.getTimestamp());
                            }
                        });
            }
            else if (this.selectedColumnIdentifier().equals("OWNER")) {
                Collections.sort(files,
                        new Comparator() {
                            public int compare(Object o1, Object o2) {
                                Path p1 = (Path) o1;
                                Path p2 = (Path) o2;
                                if (ascending) {
                                    return p1.attributes.getOwner().compareToIgnoreCase(p2.attributes.getOwner());
                                }
                                return -p1.attributes.getOwner().compareToIgnoreCase(p2.attributes.getOwner());
                            }
                        });
            }
            else if (this.selectedColumnIdentifier().equals("PERMISSIONS")) {
                Collections.sort(files,
                        new Comparator() {
                            public int compare(Object o1, Object o2) {
                                int p1 = Integer.parseInt(((Path) o1).attributes.getPermission().getOctalCode());
                                int p2 = Integer.parseInt(((Path) o2).attributes.getPermission().getOctalCode());
                                if (p1 > p2) {
                                    return higher;
                                }
                                else if (p1 < p2) {
                                    return lower;
                                }
                                return 0;
                            }
                        });
            }
        }
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


    public void outlineViewDidClickTableColumn(NSTableView tableView, NSTableColumn tableColumn) {
        this.tableViewDidClickTableColumn(tableView, tableColumn);
    }

    public void tableViewDidClickTableColumn(NSTableView tableView, NSTableColumn tableColumn) {
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
            tableView.selectRowIndexes(new NSIndexSet(this.indexOf(tableView, (Path) i.next())), true);
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
}
