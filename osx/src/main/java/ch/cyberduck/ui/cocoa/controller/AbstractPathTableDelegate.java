package ch.cyberduck.ui.cocoa.controller;

/*
 * Copyright (c) 2002-2016 iterate GmbH. All rights reserved.
 * https://cyberduck.io/
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 */

import ch.cyberduck.binding.AbstractTableDelegate;
import ch.cyberduck.binding.application.NSTableColumn;
import ch.cyberduck.core.NullComparator;
import ch.cyberduck.core.Path;
import ch.cyberduck.ui.browser.BrowserColumn;
import ch.cyberduck.ui.browser.PathTooltipService;
import ch.cyberduck.ui.browser.SizeTooltipService;
import ch.cyberduck.ui.comparator.ChecksumComparator;
import ch.cyberduck.ui.comparator.ExtensionComparator;
import ch.cyberduck.ui.comparator.FileTypeComparator;
import ch.cyberduck.ui.comparator.FilenameComparator;
import ch.cyberduck.ui.comparator.GroupComparator;
import ch.cyberduck.ui.comparator.OwnerComparator;
import ch.cyberduck.ui.comparator.PermissionsComparator;
import ch.cyberduck.ui.comparator.RegionComparator;
import ch.cyberduck.ui.comparator.SizeComparator;
import ch.cyberduck.ui.comparator.StorageClassComparator;
import ch.cyberduck.ui.comparator.TimestampComparator;
import ch.cyberduck.ui.comparator.VersionComparator;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.Comparator;

public abstract class AbstractPathTableDelegate extends AbstractTableDelegate<Path, BrowserColumn> {
    private static final Logger log = LogManager.getLogger(AbstractTableDelegate.class);

    protected AbstractPathTableDelegate(final NSTableColumn selectedColumn) {
        super(selectedColumn);
    }

    /**
     * @return A tooltip string containing the size and modification date of the path
     */
    @Override
    public String tooltip(final Path file, final BrowserColumn column) {
        switch(column) {
            case size:
                return new SizeTooltipService().getTooltip(file);
            default:
                return new PathTooltipService().getTooltip(file);
        }
    }

    @Override
    public Comparator<Path> getSortingComparator() {
        final boolean ascending = this.isSortedAscending();
        final String identifier = this.selectedColumnIdentifier();
        switch(BrowserColumn.valueOf(identifier)) {
            case icon:
            case kind:
                return new FileTypeComparator(ascending);
            case extension:
                return new ExtensionComparator(ascending);
            case filename:
                return new FilenameComparator(ascending);
            case size:
                return new SizeComparator(ascending);
            case modified:
                return new TimestampComparator(ascending);
            case owner:
                return new OwnerComparator(ascending);
            case group:
                return new GroupComparator(ascending);
            case permission:
                return new PermissionsComparator(ascending);
            case region:
                return new RegionComparator(ascending);
            case version:
                return new VersionComparator(ascending);
            case storageclass:
                return new StorageClassComparator(ascending);
            case checksum:
                return new ChecksumComparator(ascending);
            default:
                log.error(String.format("Unknown column identifier %s", identifier));
                return new NullComparator<Path>();
        }
    }
}
