/*
 *  Copyright (c) 2008 David Kocher. All rights reserved.
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

package ch.cyberduck.ui.cocoa;

import ch.cyberduck.core.NullComparator;
import ch.cyberduck.core.Path;
import ch.cyberduck.core.Status;
import ch.cyberduck.ui.*;

import org.apache.log4j.Logger;

import java.util.Comparator;

/**
 * @version $Id$
 */
public abstract class AbstractPathTableDelegate extends AbstractTableDelegate<Path> {
    private static Logger log = Logger.getLogger(AbstractTableDelegate.class);

    /**
     * @return A tooltip string containing the size and modification date of the path
     */
    public String tooltip(Path p) {
        return p.getAbsolute() + "\n"
                + Status.getSizeAsString(p.attributes().getSize()) + "\n"
                + DateFormatterFactory.instance().getLongFormat(p.attributes().getModificationDate());
    }

    @Override
    public Comparator<Path> getSortingComparator() {
        final boolean ascending = this.isSortedAscending();
        final String identifier = this.selectedColumnIdentifier();
        if(identifier.equals(BrowserTableDataSource.ICON_COLUMN)
                || identifier.equals(BrowserTableDataSource.KIND_COLUMN)) {
            return new FileTypeComparator(ascending);
        }
        else if(identifier.equals(BrowserTableDataSource.EXTENSION_COLUMN)) {
            return new ExtensionComparator(ascending);
        }
        else if(identifier.equals(BrowserTableDataSource.FILENAME_COLUMN)) {
            return new FilenameComparator(ascending);
        }
        else if(identifier.equals(BrowserTableDataSource.SIZE_COLUMN)) {
            return new SizeComparator(ascending);
        }
        else if(identifier.equals(BrowserTableDataSource.MODIFIED_COLUMN)) {
            return new TimestampComparator(ascending);
        }
        else if(identifier.equals(BrowserTableDataSource.OWNER_COLUMN)) {
            return new OwnerComparator(ascending);
        }
        else if(identifier.equals(BrowserTableDataSource.GROUP_COLUMN)) {
            return new GroupComparator(ascending);
        }
        else if(identifier.equals(BrowserTableDataSource.PERMISSIONS_COLUMN)) {
            return new PermissionsComparator(ascending);
        }
        log.error("Unknown column identifier:" + identifier);
        return new NullComparator<Path>();
    }

}