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
import com.apple.cocoa.foundation.NSBundle;
import com.apple.cocoa.foundation.NSPathUtilities;

import ch.cyberduck.core.Local;
import ch.cyberduck.core.Path;

import java.util.Map;
import java.util.HashMap;

import org.apache.log4j.Logger;

public class Editor {
	private static Logger log = Logger.getLogger(Editor.class);
	
//	private static NSMutableArray instances = new NSMutableArray();
	
	private static Editor instance;

	public static Map SUPPORTED_EDITORS = new HashMap();
	
	static {
		SUPPORTED_EDITORS.put("SubEthaEdit", "de.codingmonkeys.SubEthaEdit");
		SUPPORTED_EDITORS.put("BBEdit", "com.barebones.bbedit");
		SUPPORTED_EDITORS.put("TextWrangler", "com.barebones.textwrangler");
//		SUPPORTED_EDITORS.put("PageSpinner", "com.optima.PageSpinner");
		SUPPORTED_EDITORS.put("Tex-Edit Plus", "com.transtex.texeditplus");
	}
	
//	private Path file;
	private Map filesBeingEdited;

	static {
		// Ensure native keychain library is loaded
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
	
	public static Editor instance() {
		if(null == instance)
			instance = new Editor();
		return instance;
	}
	
	private Editor() {
		this.filesBeingEdited = new HashMap();
	}
	
//	public Editor(Path file) {
//		this.file = file;
//      instances.addObject(this);
//	}
		
	public void open(Path file) {
		file.setLocal(new Local(NSPathUtilities.temporaryDirectory(), file.getName()));
		this.filesBeingEdited.put(file.getLocal().getAbsolutePath(), file);
		file.download();
		this.edit(file.getLocal().getAbsolutePath());
	}
	
	private native void edit(String path);
	
	public void didCloseFile(String file) {
		log.debug("didCloseFile");
		Path p = (Path)this.filesBeingEdited.get(file);
		p.getLocal().delete();
		this.filesBeingEdited.remove(file);
//        instances.removeObject(this);
	}
	
	public void didModifyFile(String file) {
		log.debug("didModifyFile:");
		Path p = (Path)this.filesBeingEdited.get(file);
		p.upload();
	}
}
