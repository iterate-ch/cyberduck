package ch.cyberduck.ui.cocoa;

/*
 *  Copyright (c) 2003 David Kocher. All rights reserved.
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

import org.apache.log4j.Logger;

import java.util.Collections;
import java.util.Comparator;

import ch.cyberduck.core.Path;

/**
* @version $Id$
 */
public class CDTableDelegate {
	private static Logger log = Logger.getLogger(CDTableDelegate.class);
	
	private boolean sortAscending = true;
	private NSTableColumn lastClickedColumn;


	/**	Returns true to permit aTableView to select the row at rowIndex, false to deny permission.
	* The delegate can implement this method to disallow selection of particular rows.
	*/
	public  boolean tableViewShouldSelectRow(NSTableView aTableView, int rowIndex) {
		return true;
	}
	
	
	/**	Returns true to permit aTableView to edit the cell at rowIndex in aTableColumn, false to deny permission.
	*The delegate can implemen this method to disallow editing of specific cells.
	*/
	public boolean tableViewShouldEditLocation(NSTableView view, NSTableColumn tableColumn, int row) {
		return false;
	}
	
	public void tableViewDidClickTableColumn(NSTableView tableView, NSTableColumn tableColumn) {
		log.debug("tableViewDidClickTableColumn");
		if(lastClickedColumn == tableColumn) {
			sortAscending = !sortAscending;
		}
		else {
			if(lastClickedColumn != null)
				tableView.setIndicatorImage(null, lastClickedColumn);
			lastClickedColumn = tableColumn;
			tableView.setHighlightedTableColumn(tableColumn);
		}
		
		tableView.setIndicatorImage(sortAscending ? NSImage.imageNamed("NSAscendingSortIndicator") : NSImage.imageNamed("NSDescendingSortIndicator"), tableColumn);
		
		final int higher = sortAscending ? 1 : -1 ;
		final int lower = sortAscending ? -1 : 1;
		final boolean ascending = sortAscending;
		if(tableColumn.identifier().equals("TYPE")) {
			Collections.sort(browserModel.values(),
					new Comparator() {
						public int compare(Object o1, Object o2) {
							Path p1 = (Path) o1;
							Path p2 = (Path) o2;
							if(p1.isDirectory() && p2.isDirectory())
								return 0;
							if(p1.isFile() && p2.isFile())
								return 0;
							if(p1.isFile())
								return higher;
							return lower;
						}
					}
					);
		}
		else if(tableColumn.identifier().equals("FILENAME")) {
			Collections.sort(browserModel.list(),
					new Comparator() {
						public int compare(Object o1, Object o2) {
							Path p1 = (Path)o1;
							Path p2 = (Path)o2;
							if(ascending)
								return p1.getName().compareToIgnoreCase(p2.getName());
							else
								return -p1.getName().compareToIgnoreCase(p2.getName());
						}
					}
					);
		}
		else if(tableColumn.identifier().equals("SIZE")) {
			Collections.sort(browserModel.list(),
					new Comparator() {
						public int compare(Object o1, Object o2) {
							long p1 = ((Path)o1).status.getSize();
							long p2 = ((Path)o2).status.getSize();
							if (p1 > p2)
								return lower;
							else if (p1 < p2)
								return higher;
							else if (p1 == p2)
								return 0;
							return 0;
						}
					}
					);
		}
		else if(tableColumn.identifier().equals("MODIFIED")) {
			Collections.sort(browserModel.list(),
					new Comparator() {
						public int compare(Object o1, Object o2) {
							Path p1 = (Path) o1;
							Path p2 = (Path) o2;
							if(ascending)
								return p1.attributes.getModifiedDate().compareTo(p2.attributes.getModifiedDate());
							else
								return -p1.attributes.getModifiedDate().compareTo(p2.attributes.getModifiedDate());
						}
					}
					);
		}
		else if(tableColumn.identifier().equals("OWNER")) {
			Collections.sort(browserModel.list(),
					new Comparator() {
						public int compare(Object o1, Object o2) {
							Path p1 = (Path) o1;
							Path p2 = (Path) o2;
							if(ascending)
								return p1.attributes.getOwner().compareToIgnoreCase(p2.attributes.getOwner());
							else
								return -p1.attributes.getOwner().compareToIgnoreCase(p2.attributes.getOwner());
						}
					}
					);
		}
		tableView.reloadData();
	}	
}
