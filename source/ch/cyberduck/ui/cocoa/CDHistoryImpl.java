package ch.cyberduck.ui.cocoa;

/*
 *  Copyright (c) 2003 David Kocher. All rights reserved.
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

import ch.cyberduck.core.History;
import java.io.File;
import com.apple.cocoa.application.*;
import com.apple.cocoa.foundation.*;

/**
* @version $Id$
 */
public class CDHistoryImpl extends History {

    private static final File HISTORY_FILE = new File(NSPathUtilities.stringByExpandingTildeInPath("~/Library/Application Support/Cyberduck/Favorites.plist"));

    public CDHistoryImpl() {
	super();
    }
    
    public File getPath() {
	return HISTORY_FILE;
    }
    
//    public void save() {
//	log.info("Saving history to "+FILE);
//	boolean success = NSArchiver.archiveRootObjectToFile(data, FILE);
//	log.info("Success archiving history:"+success);
//  }

    
//    public void load() {
//	log.debug("load");
//	NSMutableArray hosts = (NSMutableArray)NSUnarchiver.unarchiveObjectWithFile(FILE);
//	if(hosts != null) {
//	    log.info("Success loading history");
//	    this.data  = hosts;
//	}
//	else
//	    log.info("Failed loading history");
//    }
}
