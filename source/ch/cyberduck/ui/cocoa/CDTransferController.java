/*
 *  ch.cyberduck.ui.cocoa.CDTransferController.java
 *  Cyberduck
 *
 *  Copyright (c) 2002 David Kocher. All rights reserved.
 *  http://icu.unizh.ch/~dkocher/
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

import ch.cyberduck.core.Path;

import com.apple.cocoa.foundation.*;
import com.apple.cocoa.application.*;

import org.apache.log4j.Logger;

/**
* @version $Id$
*/
public class CDTransferController extends NSObject {

    private static Logger log = Logger.getLogger(CDTransferController.class);

    public NSTableView transferTable; /* IBOutlet */

    public CDTransferController() {
	super();
	log.debug("CDTransferController");
    }

    public void awakeFromNib() {
//        transferTable.tableColumnWithIdentifier("PROGRESS").setDataCell(new CDProgressCell());
    }    

    public void download(Path p) {
	log.debug("download");
        p.download();
    }

    public void upload(Path p) {
	log.debug("upload");
        p.upload();
    }
}
