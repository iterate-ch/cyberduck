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

import org.apache.log4j.Logger;

/**
* @version $Id$
 */
public class UploadValidator extends AbstractValidator {
    private static Logger log = Logger.getLogger(Validator.class);
	
	public UploadValidator(boolean resumeRequested) {
        super(resumeRequested);
    }
		
    protected boolean validateDirectory(Path path) {
        // directory won't need validation, will get created if missing otherwise ignored
		if (!this.exists(path)) {
			path.mkdir(false);
		}
        return true;
    }
	
	protected boolean exists(Path path) {
		return path.exists();
	}
	
	protected void proposeFilename(Path path) {
        String parent = path.getParent().getAbsolute();
        String filename = path.getName();
        String proposal = filename;
        int no = 0;
        int index = filename.lastIndexOf(".");
        do {
            path.setPath(parent, proposal);
            no++;
            if (index != -1) {
                proposal = filename.substring(0, index) + "-" + no + filename.substring(index);
            }
            else {
                proposal = filename + "-" + no;
            }
        }
        while (this.exists(path));
    }	
}	