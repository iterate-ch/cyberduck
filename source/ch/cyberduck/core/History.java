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
import org.apache.log4j.Logger;

/**
 * @version $Id$
 */
public abstract class History {
	private static Logger log = Logger.getLogger(History.class);

	private Map data = new HashMap();

	public History() {
		this.load();
	}

	public abstract void save();

	public abstract void load();

	public void addItem(Host h) {
		if (this.size() == Integer.parseInt(Preferences.instance().getProperty("history.size"))) {
			if (this.size() > 0)
				this.removeItem(0);
		}
		this.data.put(h.getHostname(), h);
		this.save();
	}

	public void removeItem(int index) {
		log.debug("removeItem:" + index);
		this.data.remove(this.getItem(index).getHostname());
		this.save();
	}

	public void removeItem(Host item) {
		log.debug("removeItem:" + item);
		this.data.remove(item.getHostname());
		this.save();
	}

	public Host getItem(int index) {
		log.debug("getItem:" + index);
		Host h = (Host) this.values().toArray()[index];
		return this.getItem(h.getHostname());
	}

	public Host getItem(String key) {
		Host result = (Host) this.data.get(key);
		if (null == result)
			throw new IllegalArgumentException("No host with key " + key + " in History.");
		return result;
	}

	public void clear() {
		this.data.clear();
	}

	public int size() {
		return this.data.size();
	}

	public Collection values() {
		return data.values();
	}

	public Iterator iterator() {
		return this.values().iterator();
	}
}