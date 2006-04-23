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

import ch.cyberduck.core.NullComparator;
import ch.cyberduck.core.Path;
import ch.cyberduck.core.PathFactory;

import com.apple.cocoa.application.NSBrowser;
import com.apple.cocoa.application.NSBrowserCell;
import com.apple.cocoa.application.NSTableColumn;
import com.apple.cocoa.foundation.NSNotification;

import java.util.Comparator;

/**
 * @version $Id$
 */
public class CDBrowserColumnViewModel extends CDBrowserTableDataSource implements CDTableDelegate {

    public CDBrowserColumnViewModel(CDBrowserController controller) {
        super(controller);
    }

    public boolean isColumnEditable(NSTableColumn tableColumn) {
        return false;
    }

    public void enterKeyPressed(final Object sender) {

    }

    public void deleteKeyPressed(final Object sender) {

    }

    public boolean isSortedAscending() {
        return true;
    }

    public Comparator getSortingComparator() {
        return new NullComparator();
    }

    public int browserNumberOfRowsInColumn(NSBrowser sender, int col) {
        if (controller.isMounted()) {
            return this.childs(PathFactory.createPath(controller.workdir().getSession(),
                    this.pathOfColumn(sender, col))).size();
        }
        return 0;
    }

    public void browserWillDisplayCell(NSBrowser sender, NSBrowserCell cell, int row, int col) {
        if (cell instanceof CDBrowserCell) {
            Path path = (Path) this.childs(PathFactory.createPath(controller.workdir().getSession(),
                    this.pathOfColumn(sender, col))).get(row);
            ((CDBrowserCell) cell).setPath(path);
        }
//        if (cell instanceof NSTextFieldCell) {
//            if (CDBrowserController.this.isConnected()) {
//                ((NSTextFieldCell) cell).setTextColor(NSColor.controlTextColor());
//            }
//            else {
//                ((NSTextFieldCell) cell).setTextColor(NSColor.disabledControlTextColor());
//            }
//        }
    }

    public void controlTextDidEndEditing(NSNotification aNotification) {

    }

    public String pathOfColumn(NSBrowser sender, int column) {
        if (0 == column) {
            return Path.DELIMITER;
        }
        //Returns a string representing the path from the first column up to, but not including, the column at index column.
        return sender.pathToColumn(column);
    }
}