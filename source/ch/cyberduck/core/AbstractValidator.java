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
	
	protected boolean validateFile(Path path) {
        if (this.isResumeRequested()) {
            boolean fileExists = this.exists(path);
            log.info("File " + path.getName() + " exists:" + fileExists);
            path.status.setResume(fileExists);
            return true;
        }
        // When overwriting file anyway we don't have to check if the file already exists
        if (Preferences.instance().getProperty("queue.fileExists").equals("overwrite")) {
            log.debug("Defaulting to overwrite on " + path.getName());
            path.status.setResume(false);
            return true;
        }
		boolean fileExists = this.exists(path);
        log.info("File " + path.getName() + " exists:" + fileExists);
        if (fileExists) {
            if (Preferences.instance().getProperty("queue.fileExists").equals("resume")) {
                log.debug("Defaulting to resume on " + path.getName() + " succeeded:" + fileExists);
                path.status.setResume(fileExists);
                return true;
            }
            else if (Preferences.instance().getProperty("queue.fileExists").equals("similar")) {
                log.debug("Defaulting to similar name on " + path.getName());
                path.status.setResume(false);
				this.proposeFilename(path);
				log.debug("Changed name to " + path.getName());
				return true;
			}
			else {//if (Preferences.instance().getProperty("queue.fileExists").equals("ask")) {
                log.debug("Prompting user on " + path.getName());
				return false;
            }
			}
        else {//if (!fileExists) {
            path.status.setResume(false);
            return true;
        }
	}
		
	protected boolean validateDirectory(Path path) {
		return true;
	}
	
	protected abstract void proposeFilename(Path path);

	protected abstract boolean exists(Path path);
	
    /**
     * The user requested to resume this transfer
     */
    private boolean resumeRequested = false;
	
	public boolean isResumeRequested() {
		return this.resumeRequested;
	}
}
