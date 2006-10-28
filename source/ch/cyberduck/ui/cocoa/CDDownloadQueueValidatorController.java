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
import ch.cyberduck.core.Status;

import com.apple.cocoa.application.NSApplication;
import com.apple.cocoa.application.NSTableColumn;
import com.apple.cocoa.application.NSTableView;
import com.apple.cocoa.foundation.NSAttributedString;

import org.apache.log4j.Logger;

/**
 * @version $Id$
 */
public class CDDownloadQueueValidatorController extends CDValidatorController {
    private static Logger log = Logger.getLogger(CDDownloadQueueValidatorController.class);

    public CDDownloadQueueValidatorController() {
        synchronized(NSApplication.sharedApplication()) {
            if(!NSApplication.loadNibNamed("Validator", this)) {
                log.fatal("Couldn't load Validator.nib");
            }
            this.setEnabled(false);
        }
    }

    // ----------------------------------------------------------
    // NSTableView.DataSource
    // ----------------------------------------------------------

    public Object tableViewObjectValueForLocation(NSTableView view, NSTableColumn tableColumn, int row) {
        if(row < this.numberOfRowsInTableView(view)) {
            String identifier = (String) tableColumn.identifier();
            Path p = (Path) this.workList.get(row);
            if(p != null) {
                if(identifier.equals(WARNING_COLUMN)) {
                    if(p.getRemote().attributes.getSize() == 0) {
                        return ALERT_ICON;
                    }
                    if(p.getLocal().attributes.getSize() > p.getRemote().attributes.getSize()) {
                        return ALERT_ICON;
                    }
                }
                if(identifier.equals(SIZE_COLUMN)) {
                    return new NSAttributedString(Status.getSizeAsString(p.attributes.getSize()),
                            CDTableCell.PARAGRAPH_DICTIONARY_RIGHHT_ALIGNEMENT);
                }
            }
            return super.tableViewObjectValueForLocation(view, tableColumn, row);
        }
        return null;
    }
}