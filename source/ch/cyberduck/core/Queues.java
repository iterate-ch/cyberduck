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

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import org.apache.log4j.Logger;

/**
 * @version $Id$
 */
public abstract class Queues {
    private static Logger log = Logger.getLogger(Queues.class);
	
    private List data = new ArrayList();
		
    public abstract void save();
	
    public abstract void load();
	
	// ----------------------------------------------------------
	//	Data Manipulation
	// ----------------------------------------------------------
	
    public void addItem(Queue item) {
		log.debug("addItem:"+item);
		this.data.add(item);
    }

	public void addItem(Queue item, int row) {
		log.debug("addItem:"+item);
		this.data.add(row, item);
    }
	
	public void removeItem(int index) {
		log.debug("removeItem:"+index);
		this.data.remove(index);
	}
	
    public void removeItem(Queue item) {
		log.debug("removeItem:"+item);
		this.removeItem(this.data.lastIndexOf(item));
    }
	
    public Queue getItem(int index) {
//		log.debug("getItem:"+index);
		Queue result = (Queue)this.data.get(index);
		if(null == result)
			throw new IllegalArgumentException("No host with index "+index+" in Bookmarks.");
		return result;
    }
	
	public int indexOf(Object o) {
		return this.data.indexOf(o);
	}
	
    public Collection values() {
		return data;
    }
	
	public int size() {
		return this.data.size();
	}
	
	public void clear() {
		this.data.clear();
	}
	
    public Iterator iterator() {
		return data.iterator();
    }
}
