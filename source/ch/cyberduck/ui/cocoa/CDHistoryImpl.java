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
import java.util.List;
import java.util.Iterator;
import org.apache.log4j.Logger;
import ch.cyberduck.core.History;
import ch.cyberduck.core.Host;

/**
* @version $Id$
 */
public class CDHistoryImpl extends History {
    private static Logger log = Logger.getLogger(CDHistoryImpl.class);

    private static final String KEY = "history";

    public void save() {
	log.debug("save");
//	NSUserDefaults.standardUserDefaults().setObjectForKey(this.getData(), KEY);
	//2003-05-23 21:59:33.202 Cyberduck[11903] *** -[NSUserDefaults setObject:forKey:]: Attempt to insert non-property value '[]' of class 'java/util/ArrayList'.

//@todo	NSUserDefaults.standardUserDefaults().synchronize();
    }

    public void load() {
	log.debug("load");
	List list = (List)NSUserDefaults.standardUserDefaults().objectForKey(KEY);
	if(list != null) {
	    Iterator i = list.iterator();
	    while(i.hasNext())
		this.add((Host)i.next());
	}
    }
}
