package ch.cyberduck.core;

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

import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

/**
 * @version $Id$
 */
public abstract class History extends Bookmarks {
	
	private Map data = new HashMap();

	public History() {
		super();
	}
	
	public void addItem(Host h) {
		this.data.put(h.getHostname(), h);
    }
	
	public void removeItem(int index) {
		log.debug("removeItem:"+index);
		this.data.remove(this.getItem(index));
	}
	
    public void removeItem(Host item) {
		log.debug("removeItem:"+item);
		this.data.remove(item.getHostname());
    }
	
    public Host getItem(int index) {
		return this.getItem(this.values().toArray()[index].toString());
	}
	
	public Host getItem(String key) {
		return (Host)this.data.get(key);
//		if(null == result)
			//throw new IllegalArgumentException("Host "+key+" not found in Bookmarks.");
//		return result;
	}

	public Collection values() {
		return data.values();
    }
	
	public Iterator iterator() {
		return this.values().iterator();
    }
}