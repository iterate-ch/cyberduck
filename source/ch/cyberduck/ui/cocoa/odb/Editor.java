package ch.cyberduck.ui.cocoa.odb;

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

import com.apple.cocoa.foundation.NSAutoreleasePool;
import com.apple.cocoa.foundation.NSMutableArray;
import com.apple.cocoa.foundation.NSObject;
import com.apple.cocoa.foundation.NSBundle;
import com.apple.cocoa.foundation.NSPathUtilities;

import ch.cyberduck.core.Local;
import ch.cyberduck.core.Path;

import java.util.Map;
import java.util.HashMap;

import org.apache.log4j.Logger;

public class Editor extends NSObject {
	private static Logger log = Logger.getLogger(Editor.class);
		
	public static Map SUPPORTED_EDITORS = new HashMap();
	
	static {
		SUPPORTED_EDITORS.put("SubEthaEdit", "de.codingmonkeys.SubEthaEdit");
		SUPPORTED_EDITORS.put("BBEdit", "com.barebones.bbedit");
		SUPPORTED_EDITORS.put("TextWrangler", "com.barebones.textwrangler");
//		SUPPORTED_EDITORS.put("PageSpinner", "com.optima.PageSpinner");
		SUPPORTED_EDITORS.put("Tex-Edit Plus", "com.transtex.texeditplus");
	}
	
	static {
		// Ensure native odb library is loaded
		try {
			NSBundle bundle = NSBundle.mainBundle();
			String lib = bundle.resourcePath() + "/Java/" + "libODBEdit.jnilib";
			log.debug("Locating libODBEdit.jnilib at '"+lib+"'");
			System.load(lib);
		}
		catch (UnsatisfiedLinkError e) {
			log.error("Could not load the ODBEdit library:" + e.getMessage());
		}
	}
	
	private static NSMutableArray instances = new NSMutableArray();
	
	public Editor() {
        instances.addObject(this);
	}
	
	private Path file;
		
	public void open(Path f) {
		this.file = f;
		String parent = NSPathUtilities.temporaryDirectory();
		String filename = file.getName();
		String proposal = filename;
		int no = 0;
		int index = filename.lastIndexOf(".");
		do {
			this.file.setLocal(new Local(parent, proposal));
			no++;
			if (index != -1) {
				proposal = filename.substring(0, index) + "-" + no + filename.substring(index);
			}
			else {
				proposal = filename + "-" + no;
			}
		}
		while (this.file.getLocal().exists());
		this.file.download();
		this.edit(file.getLocal().getAbsolutePath());
	}
	
	private native void edit(String path);
	
	public void didCloseFile() {
		log.debug("didCloseFile");
		this.file.getLocal().delete();
        instances.removeObject(this);
	}
	
	public void didModifyFile() {
		log.debug("didModifyFile:");
		this.file.upload();
		this.file.getParent().list(true);
	}
}
