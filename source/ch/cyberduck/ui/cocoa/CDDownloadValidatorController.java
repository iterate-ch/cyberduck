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

import com.apple.cocoa.application.NSApplication;

import java.util.List;
import java.util.ArrayList;

import ch.cyberduck.core.Validator;
import ch.cyberduck.core.Preferences;
import ch.cyberduck.core.Local;
import ch.cyberduck.core.Path;

/**
* @version $Id$
 */
public class CDDownloadValidatorController extends CDValidatorController {

    public CDDownloadValidatorController(boolean resumeRequested) {
        super(resumeRequested);
    }
	
	protected void load() {
		if (false == NSApplication.loadNibNamed("Validator", this)) {
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
	
	protected boolean validateDirectory(Path path) {
        // directory won't need validation, will get created if missing otherwise ignored
		if(!path.getLocal().exists())
			path.getLocal().mkdirs();
		if (Preferences.instance().getProperty("queue.download.preserveDate").equals("true"))
			path.getLocal().setLastModified(path.attributes.getTimestamp().getTime());
        return true; //@todo
    }
		
	protected boolean exists(Path p) {
		return p.getLocal().exists();
	}
	
	protected void proposeFilename(Path path) {
        String parent = path.getLocal().getParent();
        String filename = path.getLocal().getName();
        String proposal = filename;
        int no = 0;
        int index = filename.lastIndexOf(".");
        do {
            path.setLocal(new Local(parent, proposal));
            no++;
            if (index != -1) {
                proposal = filename.substring(0, index) + "-" + no + filename.substring(index);
            }
            else {
                proposal = filename + "-" + no;
            }
        }
        while (path.getLocal().exists());
    }	
}