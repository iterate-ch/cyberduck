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
public abstract class Validator {
    private static Logger log = Logger.getLogger(Validator.class);

    protected int kind;

    /**
     * The user canceled this request, no further validation should be taken
     */
    protected boolean isCanceled = false;
    /**
     * The user requested to resume this transfer
     */
    protected boolean resumeRequested = false;

    public Validator(int kind, boolean resumeRequested) {
        this.kind = kind;
        this.resumeRequested = resumeRequested;
    }

    public abstract boolean prompt(Path path);

    /**
     * @return true if validation suceeded, false if the file should not
     *         be inclueded in the transfer queue
     */
    public boolean validate(Path path) {
        log.debug("validate:" + path);
        if (this.isCanceled) {
            log.info("Canceled " + path.getName() + " - no further validation needed");
            return false;
        }
		if(path.isDirectory()) {
			if(Queue.KIND_DOWNLOAD == kind)
				path.getLocal().mkdirs();
			if(Queue.KIND_UPLOAD == kind)
				if(!path.exists())
					path.mkdir(false);
			return true; // directory won't need validation, will get created if missing otherwise ignored
		}
        if (resumeRequested) {
            boolean fileExists = Queue.KIND_DOWNLOAD == kind ? path.getLocal().getTemp().exists() : path.exists();
            log.info("File " + path.getName() + " exists:" + fileExists);
            path.status.setResume(fileExists);
            return true;
        }
        else {//if (!resumeRequested) {
            // When overwriting file anyway we don't have to check if the file already exists
            if (Preferences.instance().getProperty("queue.fileExists").equals("overwrite")) {
                log.debug("Defaulting to overwrite on " + path.getName());
                path.status.setResume(false);
                return true;
            }
            boolean fileExists = (Queue.KIND_DOWNLOAD == kind) ? path.getLocal().getTemp().exists() : path.exists();
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
                        while (path.getLocal().getTemp().exists());
                        log.debug("Changed name to " + path.getName());
                        return true;
                    }
                    else {//if (Queue.KIND_UPLOAD == kind) {
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
                        log.debug("Changed name to " + path.getName());
                        return true;
                    }
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
    }
}
