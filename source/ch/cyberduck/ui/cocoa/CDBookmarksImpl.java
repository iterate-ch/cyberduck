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

import ch.cyberduck.core.Bookmarks;
import com.apple.cocoa.foundation.NSPathUtilities;
import java.io.File;
import org.apache.log4j.Logger;

/**
* @version $Id$
 */
public class CDBookmarksImpl extends Bookmarks { //implements NSTableView.DataSource {
    private static Logger log = Logger.getLogger(CDBookmarksImpl.class);
	
    private static Bookmarks instance;
	
    private static final File FAVORTIES_FILE = new File(NSPathUtilities.stringByExpandingTildeInPath("~/Library/Application Support/Cyberduck/Favorites.plist"));
	
    static {
		FAVORTIES_FILE.getParentFile().mkdir();
    }
	
    private CDBookmarksImpl() {
		super();
		this.load();
    }
	
    public static Bookmarks instance() {
		if(null == instance) {
			instance = new CDBookmarksImpl();
		}
		return instance;
    }
	
	protected void finalize() throws Throwable {
		super.finalize();
		this.save();
	}
	
	public void save() {
		this.save(FAVORTIES_FILE);
	}
	
	public void load() {
		this.load(FAVORTIES_FILE);
	}
}
