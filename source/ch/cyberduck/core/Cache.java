package ch.cyberduck.core;

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

import java.util.HashMap;
import java.util.Iterator;
import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;

/**
 * @version $Id$
 */
public class Cache extends HashMap {
	private static Logger log = Logger.getLogger(Cache.class);

	public List get(String path, Filter filter) {
		List files = new ArrayList((List)super.get(path));
		if(files != null) {
			for(Iterator i = files.iterator(); i.hasNext(); ) {
				if(!filter.accept((Path)i.next())) {
					i.remove();
				}
			}
		}
		return files;
	}

	public void put(String path, List childs) {
		super.put(path, childs);
	}
}
