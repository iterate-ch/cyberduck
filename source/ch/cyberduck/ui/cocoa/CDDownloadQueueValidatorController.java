package ch.cyberduck.ui.cocoa;

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

import com.apple.cocoa.application.NSApplication;

import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;

import ch.cyberduck.core.*;

/**
* @version $Id$
 */
public class CDDownloadQueueValidatorController extends CDValidatorController {
	private static Logger log = Logger.getLogger(CDDownloadQueueValidatorController.class);
	
	static {
		ValidatorFactory.addFactory(DownloadQueue.class, new Factory());
	}
	
	private static class Factory extends ValidatorFactory {
		protected Validator create() {
			return new CDDownloadQueueValidatorController(CDQueueController.instance());
		}
	}
	
	private CDDownloadQueueValidatorController(CDWindowController windowController) {
		super(windowController);
	}
	
	protected void load() {
		if(false == NSApplication.loadNibNamed("Validator", this)) {
			log.fatal("Couldn't load Validator.nib");
		}
		this.setEnabled(false);
	}
	
	public List getResult() {
		List result = new ArrayList();
		result.addAll(this.validatedList);
		result.addAll(this.workList);
		return result;
	}
	
	protected boolean isExisting(Path p) {
		return p.getLocal().exists();
	}
	
	protected boolean validateDirectory(Path path) {
		if(!path.getLocal().exists()) {
			//Include the directory as it has to be created before we can download any childs
			return true;
		}
		return false;
	}
	
	protected void adjustFilename(Path path) {
		String parent = path.getLocal().getParent();
		String filename = path.getLocal().getName();
		String proposal = filename;
		int no = 0;
		int index = filename.lastIndexOf(".");
		do {
			path.setLocal(new Local(parent, proposal));
			no++;
			if(index != -1) {
				proposal = filename.substring(0, index)+"-"+no+filename.substring(index);
			}
			else {
				proposal = filename+"-"+no;
			}
		}
		while(path.getLocal().exists());
	}
}