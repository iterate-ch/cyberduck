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

/**
 * @version $Id$
 */
public abstract class AbstractValidator implements Validator {

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

	protected List validated = new ArrayList();
	protected List workset = new ArrayList();

	private void transformName(Path path) {
		// if using name transformation, transform this name!
		if(Preferences.instance().getProperty("queue.useTransformer").equals("true")) {
//			log.debug("Preparing to transform name: " + path.getName());
			String newName = NameTransformer.instance().transform(path.getName());
			if(!newName.equals(path.getName())) {
//				log.debug("New name is: " + newName);
				path.setPath(path.getParent().getAbsolute(), newName);
			} 
			else {
//				log.debug("Name not changed: "+path.getName());
			}
		}
	}		
	
	protected abstract void prompt(Path p);

	protected boolean validate(Path p, boolean resume) {
		if(p.attributes.isFile()) {
			return this.validateFile(p, resume);
		}
		if(p.attributes.isDirectory()) {
			return this.validateDirectory(p);
		}
		throw new IllegalArgumentException(p.getName()+" is neither file nor directory");
	}
	
	protected abstract boolean validateFile(Path path, boolean resumeRequested);

	protected abstract boolean validateDirectory(Path path);

	protected abstract void adjustFilename(Path path);
}
