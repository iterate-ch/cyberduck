package ch.cyberduck.core;

/*
 *  Copyright (c) 2004 David Kocher. All rights reserved.
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
import java.util.Map;

public abstract class SessionFactory {

	private static Map factories = new HashMap();

	protected abstract Session create(Host h);

	public static void addFactory(String protocol, SessionFactory f) {
		factories.put(protocol, f);
	}

	public static final Session createSession(Host h) {
		String id = h.getProtocol();
		if(!factories.containsKey(id)) {
			try {
				// Load dynamically
				Class.forName("ch.cyberduck.core."+id+"."+id.toUpperCase()+"Session");
				//				Class.forName("ch.cyberduck.core."+id+"."
//							  +Preferences.instance().getProperty(id+".implementation")
//							  +"."+id.toUpperCase()+"Session");
			}
			catch(ClassNotFoundException e) {
				throw new RuntimeException("No class for type: "+id);
			}
			// See if it was put in:
			if(!factories.containsKey(id)) {
				throw new RuntimeException("No class for type: "+id);
			}
		}
		return ((SessionFactory)factories.get(id)).create(h);
	}
}