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
import ch.cyberduck.core.AttributedList;
import com.apple.cocoa.application.*;
import com.apple.cocoa.foundation.NSIndexSet;
import com.apple.cocoa.foundation.NSArray;
import org.apache.log4j.Logger;

import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.net.URL;

/**
 * @version $Id$
 */
public abstract class CDBrowserTableDataSource {
    private static Logger log = Logger.getLogger(CDBrowserTableDataSource.class);

    protected static final NSImage SYMLINK_ICON = NSImage.imageNamed("symlink.tiff");
    protected static final NSImage FOLDER_ICON = NSImage.imageNamed("folder16.tiff");
    protected static final NSImage NOT_FOUND_ICON = NSImage.imageNamed("notfound.tiff");

    protected static final String TYPE_COLUMN = "TYPE";
    protected static final String FILENAME_COLUMN = "FILENAME";
    protected static final String SIZE_COLUMN = "SIZE";
    protected static final String MODIFIED_COLUMN = "MODIFIED";
    protected static final String OWNER_COLUMN = "OWNER";
    protected static final String PERMISSIONS_COLUMN = "PERMISSIONS";

    protected AttributedList childs(Path path) {
        return path.list(false, controller.getEncoding(), false,
                controller.getComparator(), controller.getFileFilter());
    }

    protected CDBrowserController controller;

    public CDBrowserTableDataSource(CDBrowserController controller) {
        this.controller = controller;
    }

    public int indexOf(NSView tableView, Path p) {
        return this.childs(controller.workdir()).indexOf(p);
    }

    public boolean contains(Path p) {
        return this.childs(controller.workdir()).contains(p);
    }

    // ----------------------------------------------------------
    //	NSDraggingSource
    // ----------------------------------------------------------

    public boolean ignoreModifierKeysWhileDragging() {
        return false;
    }

    public int draggingSourceOperationMaskForLocal(boolean local) {
        log.debug("draggingSourceOperationMaskForLocal:" + local);
        if (local)
            return NSDraggingInfo.DragOperationMove | NSDraggingInfo.DragOperationCopy;
        return NSDraggingInfo.DragOperationCopy;
    }
}