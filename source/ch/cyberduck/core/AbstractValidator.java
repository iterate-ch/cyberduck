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

import java.util.List;
import java.util.Iterator;
import java.util.ArrayList;

/**
 * @version $Id$
 */
public abstract class AbstractValidator implements Validator {
    private static Logger log = Logger.getLogger(Validator.class);

	public AbstractValidator(boolean resumeRequested) {
		this.resumeRequested = resumeRequested;
	}
	
	/**
		* The user canceled this request, no further validation should be taken
     */
    private boolean isCanceled = false;
	
	public boolean isCanceled() {
		log.debug("isCanceled"+isCanceled);
		return this.isCanceled;
	}
	
	public void setCanceled(boolean c) {
		this.isCanceled = c;
	}
		
	protected abstract boolean prompt(Path p);
	
	protected abstract boolean exists(Path p);
	
	public List validate(Queue q) {
		List l = new ArrayList();
		for (Iterator i = q.getRoots().iterator(); i.hasNext(); ) {
			Path p = (Path)i.next();
			if(p.exists()) {
				log.debug("Iterating over childs of " + p);
				List tree = q.getChilds(new ArrayList(), p);
				for(Iterator childs = tree.iterator(); childs.hasNext();) {
					Path child = (Path)childs.next();
					log.debug("Validating " + child.toString());
					if (this.validate(child)) {
						child.status.reset();
						l.add(child);
					}
				}
			}
		}
		return l;
	}
	
	protected boolean validate(Path p) {
        if (!this.isCanceled) {
			if (p.attributes.isDirectory()) {
				return this.validateDirectory(p);
			}
			if (p.attributes.isFile()) {
				return this.validateFile(p);
			}
		}
		log.info("Canceled " + p.getName() + " - no further validation needed");
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
				return this.prompt(path);
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
	
    /**
     * The user requested to resume this transfer
     */
    private boolean resumeRequested = false;
	
	public boolean isResumeRequested() {
		return this.resumeRequested;
	}
}
