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

import ch.cyberduck.core.Path;
import ch.cyberduck.core.Queue;
import ch.cyberduck.core.Status;

import com.apple.cocoa.application.NSApplication;
import com.apple.cocoa.application.NSTableView;
import com.apple.cocoa.application.NSTableColumn;
import com.apple.cocoa.foundation.NSAttributedString;

import org.apache.log4j.Logger;

/**
 * @version $Id$
 */
public class CDUploadQueueValidatorController extends CDValidatorController {

    private static Logger log = Logger.getLogger(CDUploadQueueValidatorController.class);

    public CDUploadQueueValidatorController(final Queue queue) {
        super(queue);
        synchronized(CDQueueController.instance()) {
            if (!NSApplication.loadNibNamed("Validator", this)) {
                log.fatal("Couldn't load Validator.nib");
            }
            this.setEnabled(false);
        }
    }

    protected boolean exists(Path p) {
        return p.exists();
    }

    protected boolean validateDirectory(Path p) {
        if (!p.getRemote().exists()) {
            //Directory does not exist yet; include so it will be created on the server
            return true;
        }
        //Directory already exists; do not include as this would throw "file already exists"
        return false;
    }

    protected void prompt(Path p) {
        this.workList.add(p);
        super.prompt(p);
    }

    protected void adjustFilename(Path path) {
        String parent = path.getParent().getAbsolute();
        String filename = path.getName();
        String proposal = filename;
        int no = 0;
        int index = filename.lastIndexOf(".");
        do {
            path.setPath(parent, proposal);
            no++;
            if (index != -1 && index != 0) {
                proposal = filename.substring(0, index) + "-" + no + filename.substring(index);
            }
            else {
                proposal = filename + "-" + no;
            }
        }
        while (path.exists());
    }

    public Object tableViewObjectValueForLocation(NSTableView view, NSTableColumn tableColumn, int row) {
        if (row < this.numberOfRowsInTableView(view)) {
            String identifier = (String) tableColumn.identifier();
            Path p = (Path) this.workList.get(row);
            if (p != null) {
                if (identifier.equals(WARNING_COLUMN)) {
                    if(p.getLocal().attributes.getSize() == 0) {
                        return ALERT_ICON;
                    }
                    if(p.getRemote().attributes.getSize() > p.getLocal().attributes.getSize()) {
                        return ALERT_ICON;
                    }
                }
                if (identifier.equals(SIZE_COLUMN)) {
                    return new NSAttributedString(Status.getSizeAsString(p.getLocal().attributes.getSize()),
                            CDTableCell.PARAGRAPH_DICTIONARY_RIGHHT_ALIGNEMENT);
                }
            }
            return super.tableViewObjectValueForLocation(view, tableColumn, row);
        }
        return null;
    }
}