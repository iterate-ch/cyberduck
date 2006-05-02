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

import ch.cyberduck.core.Local;
import ch.cyberduck.core.Path;
import ch.cyberduck.core.Queue;
import ch.cyberduck.core.Status;

import com.apple.cocoa.application.NSApplication;
import com.apple.cocoa.application.NSTableView;
import com.apple.cocoa.application.NSTableColumn;
import com.apple.cocoa.application.NSCell;
import com.apple.cocoa.application.NSImage;
import com.apple.cocoa.foundation.NSSize;

import org.apache.log4j.Logger;

import java.util.ArrayList;
import java.util.List;

/**
 * @version $Id$
 */
public class CDDownloadQueueValidatorController extends CDValidatorController {
    private static Logger log = Logger.getLogger(CDDownloadQueueValidatorController.class);

    public CDDownloadQueueValidatorController(Queue queue) {
        super(queue);
        synchronized(CDQueueController.instance()) {
            if(!NSApplication.loadNibNamed("Validator", this)) {
                log.fatal("Couldn't load Validator.nib");
            }
            this.setEnabled(false);
        }
    }

    public List getResult() {
        List result = new ArrayList();
        result.addAll(this.validatedList);
        result.addAll(this.workList);
        return result;
    }

    protected boolean isExisting(Path p) {
        return p.getLocal().exists() && p.getLocal().getSize() > 0;
    }

    protected boolean validateDirectory(Path path) {
        return true;
    }

    protected void prompt(Path p) {
        this.workList.add(p);
        super.prompt(p);
    }

    protected void adjustFilename(Path path) {
        String parent = path.getLocal().getParent();
        String filename = path.getLocal().getName();
        String proposal = filename;
        int no = 0;
        int index = filename.lastIndexOf(".");
        do {
            path.setLocal(new Local(parent, proposal));
            no++;
            if(index != -1) {
                proposal = filename.substring(0, index) + "-" + no + filename.substring(index);
            }
            else {
                proposal = filename + "-" + no;
            }
        }
        while(path.getLocal().exists());
    }

    public Object tableViewObjectValueForLocation(NSTableView view, NSTableColumn tableColumn, int row) {
        if (row < this.numberOfRowsInTableView(view)) {
            String identifier = (String) tableColumn.identifier();
            Path p = (Path) this.workList.get(row);
            if (p != null) {
                if (identifier.equals("WARNING")) {
                    if(p.getRemote().attributes.getSize() == 0) {
                        return NSImage.imageNamed("alert.tiff");
                    }
                    if(p.getLocal().attributes.getSize() >= p.getRemote().attributes.getSize()) {
                        return NSImage.imageNamed("alert.tiff");
                    }
                }
                if (identifier.equals("SIZE")) {
                    return Status.getSizeAsString(p.getRemote().attributes.getSize());
                }
            }
            return super.tableViewObjectValueForLocation(view, tableColumn, row);
        }
        return null;
    }
}