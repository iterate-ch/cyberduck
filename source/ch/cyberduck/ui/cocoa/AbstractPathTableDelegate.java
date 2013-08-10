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
import ch.cyberduck.core.UserDateFormatterFactory;
import ch.cyberduck.core.formatter.SizeFormatterFactory;
import ch.cyberduck.ui.cocoa.application.NSTableColumn;
import ch.cyberduck.ui.comparator.ExtensionComparator;
import ch.cyberduck.ui.comparator.FileTypeComparator;
import ch.cyberduck.ui.comparator.FilenameComparator;
import ch.cyberduck.ui.comparator.GroupComparator;
import ch.cyberduck.ui.comparator.OwnerComparator;
import ch.cyberduck.ui.comparator.PermissionsComparator;
import ch.cyberduck.ui.comparator.RegionComparator;
import ch.cyberduck.ui.comparator.SizeComparator;
import ch.cyberduck.ui.comparator.TimestampComparator;

import org.apache.log4j.Logger;

import java.util.Comparator;

/**
 * @version $Id$
 */
public abstract class AbstractPathTableDelegate extends AbstractTableDelegate<Path> {
    private static Logger log = Logger.getLogger(AbstractTableDelegate.class);

    protected AbstractPathTableDelegate(final NSTableColumn selectedColumn) {
        super(selectedColumn);
    }

    /**
     * @return A tooltip string containing the size and modification date of the path
     */
    @Override
    public String tooltip(Path p) {
        return p.getAbsolute() + "\n"
                + SizeFormatterFactory.get().format(p.attributes().getSize()) + "\n"
                + UserDateFormatterFactory.get().getLongFormat(p.attributes().getModificationDate());
    }

    @Override
    public Comparator<Path> getSortingComparator() {
        final boolean ascending = this.isSortedAscending();
        final String identifier = this.selectedColumnIdentifier();
        if(identifier.equals(BrowserTableDataSource.Columns.ICON.name())
                || identifier.equals(BrowserTableDataSource.Columns.KIND.name())) {
            return new FileTypeComparator(ascending);
        }
        else if(identifier.equals(BrowserTableDataSource.Columns.EXTENSION.name())) {
            return new ExtensionComparator(ascending);
        }
        else if(identifier.equals(BrowserTableDataSource.Columns.FILENAME.name())) {
            return new FilenameComparator(ascending);
        }
        else if(identifier.equals(BrowserTableDataSource.Columns.SIZE.name())) {
            return new SizeComparator(ascending);
        }
        else if(identifier.equals(BrowserTableDataSource.Columns.MODIFIED.name())) {
            return new TimestampComparator(ascending);
        }
        else if(identifier.equals(BrowserTableDataSource.Columns.OWNER.name())) {
            return new OwnerComparator(ascending);
        }
        else if(identifier.equals(BrowserTableDataSource.Columns.GROUP.name())) {
            return new GroupComparator(ascending);
        }
        else if(identifier.equals(BrowserTableDataSource.Columns.PERMISSIONS.name())) {
            return new PermissionsComparator(ascending);
        }
        else if(identifier.equals(BrowserTableDataSource.Columns.REGION.name())) {
            return new RegionComparator(ascending);
        }
        log.error(String.format("Unknown column identifier %s", identifier));
        return new NullComparator<Path>();
    }
}