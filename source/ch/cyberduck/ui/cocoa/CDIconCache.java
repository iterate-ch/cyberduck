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

import com.apple.cocoa.application.NSImage;
import com.apple.cocoa.application.NSWorkspace;

import java.util.HashMap;

public class CDIconCache extends HashMap {
	private static CDIconCache instance;

	public static CDIconCache instance() {
        if (null == instance) {
            instance = new CDIconCache();
        }
        return instance;
	}
	
	public void put(String extension, NSImage image) {
		super.put(extension, image);
	}

	public NSImage get(String key) {
		NSImage img = (NSImage)super.get(key);
		if(null == img) {
			this.put(key, img = NSWorkspace.sharedWorkspace().iconForFileType(key));
		}
		return img;
	}
}