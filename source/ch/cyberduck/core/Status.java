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

import com.apple.cocoa.foundation.NSDictionary;
import com.apple.cocoa.foundation.NSMutableDictionary;

import java.io.Serializable;
import java.util.Observable;

import org.apache.log4j.Logger;

/**
 * The Status class is the model of a download's status.
 * The wrapper for any status informations of a transfer as the size and transferred
 * bytes.
 *
 * @version $Id$
 */
public class Status extends Observable implements Serializable {
    private static Logger log = Logger.getLogger(Status.class);

    /**
     * Download is resumable
     */
    private transient boolean resume = false;
    /**
     * The file length
     */
    private long size = -1;
    /**
     * The number of transfered bytes. Must be less or equals size.
     */
    private long current = 0;
    /**
     * Indiciating wheter the transfer has been cancled by the user.
     */
    private boolean canceled;
    /**
     * Indicates that the last action has been completed.
     */
    private boolean complete = false;

    public Status() {
        super();
    }

    public Status(NSDictionary dict) {
        Object sizeObj = dict.objectForKey("Size");
        if (sizeObj != null) {
            this.size = Integer.parseInt((String)sizeObj);
        }
        Object currentObj = dict.objectForKey("Current");
        if (currentObj != null) {
            this.current = Integer.parseInt((String)currentObj);
        }
    }

    public NSDictionary getAsDictionary() {
        NSMutableDictionary dict = new NSMutableDictionary();
        dict.setObjectForKey(this.size + "", "Size");
        dict.setObjectForKey(this.current + "", "Current");
        return dict;
    }

    /**
     * Notify all observers
     *
     * @param arg The message to send to the observers
     * @see ch.cyberduck.core.Message
     */
    public void callObservers(Message arg) {
        this.setChanged();
        this.notifyObservers(arg);
    }

    /**
     * @param size the size of file in bytes.
     */
    public void setSize(long size) {
        this.size = size;
    }

    /**
     * @return length the size of file in bytes.
     */
    public long getSize() {
        return this.size;
    }

    private static final long KILO = 1024; //2^10
    private static final long MEGA = 1048576; // 2^20
    private static final long GIGA = 1073741824; // 2^30

    /**
     * @return The size of the file
     */
    public static String getSizeAsString(long size) {
		if (-1 == size) {
			return "Unknown size";
		}
        if (size < KILO) {
            return size + "B";
        }
        if (size < MEGA) {
            String v = String.valueOf((double)size / KILO);
            return v.substring(0, v.indexOf('.') + 2) + "kB";
        }
        if (size < GIGA) {
            String v = String.valueOf((double)size / MEGA);
            return v.substring(0, v.indexOf('.') + 2) + "MB";
        }
        else {
            String v = String.valueOf((double)size / GIGA);
            return v.substring(0, v.indexOf('.') + 2) + "GB";
        }
    }

    public void setComplete(boolean complete) {
        this.complete = complete;
        if (complete) {
            if (this.getCurrent() < this.getSize()) {
                log.warn("Item marked as complete, but current is " + this.getCurrent() + " and total is " + this.getSize());
            }
            this.setCurrent(this.getSize());
        }
    }

    public boolean isComplete() {
        return this.complete;
    }

    public void setCanceled(boolean b) {
        canceled = b;
    }

    public boolean isCanceled() {
        return canceled;
    }

    public long getCurrent() {
        return this.current;
    }

    /**
     * @param current The currently transfered bytes
     */
    public void setCurrent(long current) {
        this.current = current;
        this.callObservers(new Message(Message.DATA));
    }

    public void setResume(boolean resume) {
        log.info("setResume:" + resume);
        this.resume = resume;
    }

    public boolean isResume() {
        return this.resume;
    }

    public void reset() {
        if (log.isDebugEnabled()) {
            log.debug("reset (resume=" + resume + ")");
        }
        this.complete = false;
        this.canceled = false;
        if (!this.isResume()) {
            this.setCurrent(0);
        }
    }
}
