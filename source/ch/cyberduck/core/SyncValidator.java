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
public class SyncValidator extends AbstractValidator {
    private static Logger log = Logger.getLogger(Validator.class);
	
	public SyncValidator() {
        super(false);
    }
	
	public boolean validate(Path path) {
		return this.exists(path);
	}		
	
	protected boolean exists(Path path) {
		return path.exists() && path.getLocal().exists();
	}
	
	protected void proposeFilename(Path path) {
		// syncing, never change name
	}
}	