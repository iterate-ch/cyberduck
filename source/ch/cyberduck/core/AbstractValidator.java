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

import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;

/**
 * @version $Id$
 */
public abstract class AbstractValidator implements Validator {
	private static Logger log = Logger.getLogger(AbstractValidator.class);

	protected List validatedList = new ArrayList();
	protected List workList = new ArrayList();
	protected List promptList = new ArrayList();
		
	/**
	 * The user canceled this request, no further validation should be taken
	 */
	private boolean canceled = false;

	public boolean isCanceled() {
		return this.canceled;
	}

	protected void setCanceled(boolean c) {
		this.canceled = c;
	}

	protected abstract boolean isExisting(Path p);

	protected abstract void prompt(Path p);

	protected boolean validate(Path p, boolean resume) {
		if(p.attributes.isFile()) {
			p.reset();
			if(Preferences.instance().getProperty("queue.useTransformer").equals("true")) {
				p.setPath(p.getParent().getAbsolute(), NameTransformer.instance().transform(p.getName()));
			}
			return this.validateFile(p, resume);
		}
		if(p.attributes.isDirectory()) {
			return this.validateDirectory(p);
		}
		throw new IllegalArgumentException(p.getName()+" is neither file nor directory");
	}
	
	protected boolean validateFile(Path path, boolean resumeRequested) {
		if(resumeRequested) { // resume existing files independant of settings in preferences
			path.status.setResume(this.isExisting(path));
			return true;
		}
		// When overwriting file anyway we don't have to check if the file already exists
		if(Preferences.instance().getProperty("queue.fileExists").equals("overwrite")) {
			log.info("Apply validation rule to overwrite file "+path.getName());
			path.status.setResume(false);
			return true;
		}
		if(this.isExisting(path)) {
			if(Preferences.instance().getProperty("queue.fileExists").equals("resume")) {
				log.debug("Apply validation rule to resume:"+path.getName());
				path.status.setResume(true);
				return true;
			}
			if(Preferences.instance().getProperty("queue.fileExists").equals("similar")) {
				log.debug("Apply validation rule to apply similar name:"+path.getName());
				path.status.setResume(false);
				this.adjustFilename(path);
				log.info("Changed local name to "+path.getName());
				return true;
			}
			if (Preferences.instance().getProperty("queue.fileExists").equals("ask")) {
				log.debug("Apply validation rule to ask:"+path.getName());
				this.prompt(path);
				return false;
			}
			throw new IllegalArgumentException("No rules set to validate transfers");
		}
		else {
			path.status.setResume(false);
			return true;
		}
	}
	
	protected abstract boolean validateDirectory(Path path);

	public List getValidated() {
		return this.validatedList;
	}
		
	protected abstract void adjustFilename(Path path);
}
