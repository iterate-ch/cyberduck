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
import ch.cyberduck.ui.browser.PathTooltipService;
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

    private PathTooltipService tooltip = new PathTooltipService();

    protected AbstractPathTableDelegate(final NSTableColumn selectedColumn) {
        super(selectedColumn);
    }

    /**
     * @return A tooltip string containing the size and modification date of the path
     */
    @Override
    public String tooltip(Path file) {
        return tooltip.getTooltip(file);
    }

    @Override
    public Comparator<Path> getSortingComparator() {
        final boolean ascending = this.isSortedAscending();
        final String identifier = this.selectedColumnIdentifier();
        switch(BrowserTableDataSource.Columns.valueOf(identifier)) {
            case ICON:
            case KIND:
                return new FileTypeComparator(ascending);
            case EXTENSION:
                return new ExtensionComparator(ascending);
            case FILENAME:
                return new FilenameComparator(ascending);
            case SIZE:
                return new SizeComparator(ascending);
            case MODIFIED:
                return new TimestampComparator(ascending);
            case OWNER:
                return new OwnerComparator(ascending);
            case GROUP:
                return new GroupComparator(ascending);
            case PERMISSIONS:
                return new PermissionsComparator(ascending);
            case REGION:
                return new RegionComparator(ascending);
            default:
                log.error(String.format("Unknown column identifier %s", identifier));
                return new NullComparator<Path>();
        }
    }
}