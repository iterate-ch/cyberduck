package ch.cyberduck.ui.cocoa;

/*
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

import ch.cyberduck.core.History;
import ch.cyberduck.core.Host;
import com.apple.cocoa.application.*;
import com.apple.cocoa.foundation.*;
import org.apache.log4j.Logger;

import java.util.List;

/**
* @version $Id$
 */
public class CDHistoryImpl extends History {
    private static Logger log = Logger.getLogger(CDHistoryImpl.class);

    private static String FILE = NSPathUtilities.stringByExpandingTildeInPath("~/Library/Application Support/Cyberduck/History.data");

    public void save() {
	log.debug("Saving history to "+FILE);
	boolean success = NSArchiver.archiveRootObjectToFile(data, FILE);
	log.debug("Success archiving history:"+success);
//	NSUserDefaults.standardUserDefaults().setObjectForKey(this.getData(), KEY);
	//2003-05-23 21:59:33.202 Cyberduck[11903] *** -[NSUserDefaults setObject:forKey:]: Attempt to insert non-property value '[]' of class 'java/util/ArrayList'.

//@todo	NSUserDefaults.standardUserDefaults().synchronize();
    }

    public void load() {
	log.debug("load");
	List h = (List)NSUnarchiver.unarchiveObjectWithFile(FILE);
//	List h = (List)NSUnarchiver.unarchiveObjectWithFile("History.data");
//	List h = (List)NSUnarchiver.unarchiveObjectWithFile("~/Library/Application Support/Cyberduck/History.data");
	if(h != null)
	    this.data  = h;
    }
}
