package ch.cyberduck.ui.cocoa;

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

import java.util.ArrayList;
import java.util.List;

import com.apple.cocoa.application.NSApplication;

import org.apache.log4j.Logger;

import ch.cyberduck.core.Path;
import ch.cyberduck.core.UploadQueue;
import ch.cyberduck.core.Validator;
import ch.cyberduck.core.ValidatorFactory;

/**
 * @version $Id$
 */
public class CDUploadQueueValidatorController extends CDValidatorController {
	protected static Logger log = Logger.getLogger(CDUploadQueueValidatorController.class);

	static {
		ValidatorFactory.addFactory(UploadQueue.class, new Factory());
	}

	private static class Factory extends ValidatorFactory {
		protected Validator create(boolean resumeRequested) {
			return new CDUploadQueueValidatorController(resumeRequested);
		}
	}

	private CDUploadQueueValidatorController(boolean resumeRequested) {
		super(resumeRequested);
	}

	protected void load() {
		if(false == NSApplication.loadNibNamed("Validator", this)) {
			log.fatal("Couldn't load Validator.nib");
		}
		this.setEnabled(false);
	}

	public List getResult() {
		List result = new ArrayList();
		result.addAll(this.validated);
		result.addAll(this.workset);
		return result;
	}

	protected boolean exists(Path p) {
		return p.exists(); //@todo , p.exists(false)?
	}

	protected void adjustFilename(Path path) {
		String parent = path.getParent().getAbsolute();
		String filename = path.getName();
		String proposal = filename;
		int no = 0;
		int index = filename.lastIndexOf(".");
		do {
			path.setPath(parent, proposal);
			no++;
			if(index != -1) {
				proposal = filename.substring(0, index)+"-"+no+filename.substring(index);
			}
			else {
				proposal = filename+"-"+no;
			}
		}
		while(path.exists());
	}
}