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
	
	protected boolean resume = false;

	public Validator(boolean resume) {
		this.resume = resume;
	}
	
    /**
	* @return true if validation suceeded, false if !proceed
     */
    public boolean validate(Path path, int kind) {
		//        boolean resume = path.status.isResume();
		//        this.proceed = false;
        log.debug("validate:"+path);
        if (Queue.KIND_DOWNLOAD == kind) {
            log.debug("validating download");
            if (resume) {
				log.debug("resume:true");
                if (path.status.isComplete()) {
                    log.debug("complete:true");
                    log.debug("return:true");
                    return true;
                }
                else if (!path.status.isComplete()) {
                    log.debug("complete:false");
                    path.status.setResume(path.getLocal().exists());
                    log.debug("return:true");
                    return true;
                }
            }
            if (!resume) {
                log.debug("resume:false");
                if (path.getLocal().exists()) {
                    log.debug("local path exists:true");
                    if (Preferences.instance().getProperty("queue.download.duplicate").equals("ask")) {
                        log.debug("queue.download.duplicate:ask");
                        // Waiting for other alert sheets open to be closed first
						return this.prompt(path);
                    }
                    else if (Preferences.instance().getProperty("queue.download.duplicate").equals("similar")) {
                        log.debug("queue.download.duplicate:similar");
                        path.status.setResume(false);
                        String proposal = null;
                        String parent = path.getLocal().getParent();
                        String filename = path.getLocal().getName();
                        int no = 1;
                        int index = filename.lastIndexOf(".");
                        while (path.getLocal().exists()) {
                            if (index != -1) {
                                proposal = filename.substring(0, index) + "-" + no + filename.substring(index);
                            }
                            else {
                                proposal = filename + "-" + no;
                            }
                            path.setLocal(new Local(parent, proposal));
                            no++;
                        }
                        log.debug("return:true");
                        return true;
                    }
                    else if (Preferences.instance().getProperty("queue.download.duplicate").equals("resume")) {
                        log.debug("queue.download.duplicate:resume");
                        path.status.setResume(true);
                        log.debug("return:true");
                        return true;
                    }
                    else if (Preferences.instance().getProperty("queue.download.duplicate").equals("overwrite")) {
                        log.debug("queue.download.duplicate:overwrite");
                        path.status.setResume(false);
                        log.debug("return:true");
                        return true;
                    }
                }
                log.debug("local path exists:false");
                log.debug("return:true");
                return true;
            }
        }
        else if (Queue.KIND_UPLOAD == kind) {
            log.debug("Validating upload");
            if (resume) {
				log.debug("resume:true");
                if (path.status.isComplete()) {
                    log.debug("complete:true");
                    log.debug("return:true");
                    return true;
                }
                else if (!path.status.isComplete()) {
                    log.debug("complete:false");
                    path.status.setResume(path.exists());
                    log.debug("return:true");
                    return true;
                }
            }
            if (!resume) {
                log.debug("resume:false");
                if (path.exists()) {
                    log.debug("local path exists:true");
                    if (Preferences.instance().getProperty("queue.download.duplicate").equals("ask")) {
                        log.debug("queue.download.duplicate:ask");
                        // Waiting for other alert sheets open to be closed first
						return this.prompt(path);
                    }
                    else if (Preferences.instance().getProperty("queue.download.duplicate").equals("similar")) {
                        log.debug("queue.download.duplicate:similar");
                        path.status.setResume(false);
                        String proposal = null;
                        String parent = path.getParent().getAbsolute();
                        String filename = path.getName();
                        int no = 1;
                        int index = filename.lastIndexOf(".");
                        while (path.exists()) {
                            if (index != -1) {
                                proposal = filename.substring(0, index) + "-" + no + filename.substring(index);
                            }
                            else {
                                proposal = filename + "-" + no;
                            }
                            path.setPath(parent, proposal);
                            no++;
                        }
                        log.debug("return:true");
                        return true;
                    }
                    else if (Preferences.instance().getProperty("queue.download.duplicate").equals("resume")) {
                        log.debug("queue.download.duplicate:resume");
                        path.status.setResume(true);
                        log.debug("return:true");
                        return true;
                    }
                    else if (Preferences.instance().getProperty("queue.download.duplicate").equals("overwrite")) {
                        log.debug("queue.download.duplicate:overwrite");
                        path.status.setResume(false);
                        log.debug("return:true");
                        return true;
                    }
                }
                log.debug("local path exists:false");
                log.debug("return:true");
                return true;
            }
        }
        return false;
    }
	
	public abstract boolean prompt(Path path);
}
