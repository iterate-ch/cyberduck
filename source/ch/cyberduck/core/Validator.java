package ch.cyberduck.core;

/*
 *  Copyright (c) 2003 David Kocher. All rights reserved.
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
public abstract class Validator {
	private static Logger log = Logger.getLogger(Validator.class);
	
	/**
	* The user canceled this request, no further validation should be taken
	 */
	private boolean canceled = false;
	/**
		* The user requested to resume this transfer
	 */
	private boolean resumeRequested = false;
	protected int kind;

	public Validator(int kind, boolean resumeRequested) {
		this.kind = kind;
		this.resumeRequested = resumeRequested;
	}
	
	protected void setCanceled() {
		this.canceled = true;
	}
	
	protected boolean isCanceled() {
		return this.canceled;
	}
	
    /**
	* @return true if validation suceeded, false if !proceed
     */
    public boolean validate(Path path) {
        log.debug("validate:"+path);
		if(this.isCanceled()) {
			log.info("*** Canceled "+path.getName()+" - no further validation needed");
			return false;
		}
		boolean exists = false;
		if (Queue.KIND_DOWNLOAD == kind)
			exists = path.getLocal().exists();
		if (Queue.KIND_UPLOAD == kind)
			exists = path.exists();
		log.info("*** File "+path.getName()+" exists:"+exists);
		if (resumeRequested) {
			path.status.setResume(exists);
			log.info("*** Returning "+path.getName()+" from validation after setting resume to "+exists);
			return true;
		}
		if (exists) {
			if (Preferences.instance().getProperty("queue.fileExists").equals("ask")) {
				log.debug("*** Prompting user on "+path.getName());
				// @todo Waiting for other alert sheets open to be closed first
				return this.prompt(path);
			}
			else if (Preferences.instance().getProperty("queue.fileExists").equals("similar")) {
				log.debug("Using similar name");
				path.status.setResume(false);
				if (Queue.KIND_DOWNLOAD == kind) {
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
				if (Queue.KIND_UPLOAD == kind) {
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
					while (path.exists());
				}
				return true;
			}
			else if (Preferences.instance().getProperty("queue.fileExists").equals("resume")) {
				log.debug("Resume");
				path.status.setResume(true);
				return true;
			}
			else if (Preferences.instance().getProperty("queue.fileExists").equals("overwrite")) {
				log.debug("Overwrite");
				path.status.setResume(false);
				return true;
			}
		}
		path.status.setResume(false);
		return true;
    }
	
	public abstract boolean prompt(Path path);
}
