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

import com.apple.cocoa.foundation.*;
import com.apple.cocoa.application.*;

/**
* @version $Id$
 */
public class CDHistoryImpl {
    private static Logger log = Logger.getLogger(CDHistoryImpl.class);

    public void save() {
	NSUserDefaults.standardUserDefaults().setObjectForKey(this, "history");
	NSUserDefaults.synchronize();
    }

    public void restore() {
	NSDictionary dict = NSUserDefaults.standardUserDefaults().dictionaryRepresentation();
	java.util.Enumeration enum = dict.keyEnumerator();
	while (enum.hasMoreElements()) {{
	    this.add(enum.nextElement());
	}
    }
}
