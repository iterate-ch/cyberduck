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
public abstract class AbstractValidator implements Validator {
    private static Logger log = Logger.getLogger(Validator.class);

	public AbstractValidator(boolean resumeRequested) {
		this.resumeRequested = resumeRequested;
	}
	
	public boolean validate(Path path) {
        log.debug("validate:" + path);
		if (path.attributes.isDirectory()) {
			return this.validateDirectory(path);
		}
		if (path.attributes.isFile()) {
			return this.validateFile(path);
		}
		return false;
    }
	
	protected abstract boolean validateDirectory(Path path);

	protected abstract boolean validateFile(Path path);

    /**
     * The user requested to resume this transfer
     */
    private boolean resumeRequested = false;
	
	public boolean isResumeRequested() {
		return this.resumeRequested;
	}
}
