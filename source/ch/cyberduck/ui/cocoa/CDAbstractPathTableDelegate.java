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

import ch.cyberduck.core.*;

import org.apache.log4j.Logger;

import java.text.Collator;
import java.util.Comparator;
import java.util.Locale;

/**
 * @version $Id:$
 */
public abstract class CDAbstractPathTableDelegate extends CDAbstractTableDelegate<Path> {
    private static Logger log = Logger.getLogger(CDAbstractTableDelegate.class);

    /**
     * @return A tooltip string containing the size and modification date of the path
     */
    public String tooltip(Path p) {
        return p.getAbsolute() + "\n"
                + Status.getSizeAsString(p.attributes.getSize()) + "\n"
                + CDDateFormatter.getLongFormat(p.attributes.getModificationDate());
    }

    public Comparator<Path> getSortingComparator() {
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

        public int compare(Path p1, Path p2) {
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
        private Comparator<String> impl = new NaturalOrderComparator();

        public FilenameComparator(boolean ascending) {
            super(ascending);
        }

        public int compare(Path p1, Path p2) {
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

        public int compare(Path p1, Path p2) {
            if(p1.attributes.getSize() > p2.attributes.getSize()) {
                return ascending ? 1 : -1;
            }
            else if(p1.attributes.getSize() < p2.attributes.getSize()) {
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

        public int compare(Path p1, Path p2) {
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

        public int compare(Path p1, Path p2) {
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

        public int compare(Path p1, Path p2) {
            Permission perm1 = p1.attributes.getPermission();
            if(null == perm1) {
                perm1 = Permission.EMPTY;
            }
            Permission perm2 = p2.attributes.getPermission();
            if(null == perm2) {
                perm2 = Permission.EMPTY;
            }
            if(perm1.getOctalNumber() > perm2.getOctalNumber()) {
                return ascending ? 1 : -1;
            }
            else if(perm1.getOctalNumber() < perm2.getOctalNumber()) {
                return ascending ? -1 : 1;
            }
            return 0;
        }

        public String toString() {
            return CDBrowserTableDataSource.PERMISSIONS_COLUMN;
        }
    }
}