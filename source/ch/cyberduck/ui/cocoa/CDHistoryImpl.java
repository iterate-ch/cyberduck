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

import java.io.File;

import ch.cyberduck.core.History;
import com.apple.cocoa.foundation.NSPathUtilities;

/**
* @version $Id$
 */
public class CDHistoryImpl extends History {
		
	private static History instance;

    private static final File HISTORY_FILE = new File(NSPathUtilities.stringByExpandingTildeInPath("~/Library/Application Support/Cyberduck/History.plist"));
	
    static {
		HISTORY_FILE.getParentFile().mkdir();
    }
	
	private CDHistoryImpl() {
		super();
		this.load();
    }
	
    public static History instance() {
		if(null == instance) {
			instance = new CDHistoryImpl();
		}
		return instance;
    }
	
	public void load() {
		this.load(HISTORY_FILE);
	}
	
	public void save() {
		this.save(HISTORY_FILE);
	}
}