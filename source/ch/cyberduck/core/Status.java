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

import java.io.Serializable;
import java.util.Observable;

/**
  * The Status class is the model of a download's status.
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
    /**
	* The last action has been stopped, but must not be completed.
     */
    
    /**
	* The wrapper for any status informations of a transfer like it's length and transferred
     * bytes.
     */
    public Status () {
	super();
    }

    /**
        * Notify all observers
     * @param arg The message to send to the observers
     * @see ch.cyberduck.core.Message
     */
    public void callObservers(Message arg) {
//	log.debug("callObserver:"+arg);
//	log.debug(this.countObservers()+" observers known.");
	this.setChanged();
	this.notifyObservers(arg);
    }

    /**
	* @param size the size of file in bytes.
     */
    public void setSize(long size) {
	//	log.debug("setSize:"+size);
	this.size = size;
    }

    /**
	* @return length the size of file in bytes.
     */
    public long getSize() {
//	log.debug("getSize:"+size);
	return size;
    }

    private static final int KILO = 1024; //2^10
    private static final int MEGA = 1048576; // 2^20
    private static final int GIGA = 1073741824; // 2^30

    /**
	* @return The size of the file
     */
    public String getSizeAsString() {
	if(size < KILO) {
	    return size + " B";
	}
	else if(size < MEGA) {
	    return new Double(size/KILO).intValue() + " KB";
	}
	else if(size < GIGA) {
	    return new Double(size/MEGA).intValue() + " MB";
	}
	else {
	    return new Double(size/GIGA).intValue() + " GB";
	}
    }
    
    public void setComplete(boolean b) {
        this.complete = b;
	this.callObservers(new Message(Message.COMPLETE));
	if(complete)
	    this.setCurrent(this.getSize());
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


//    public BoundedRangeModel getProgressModel() {
//	DefaultBoundedRangeModel m = null;
//	try {
//	    if(this.getSize() < 0) {
//		m = new DefaultBoundedRangeModel(0, 0, 0, 100);
//	    }
//	    m = new DefaultBoundedRangeModel(this.getCurrent(), 0, 0, this.getSize());
//	}
//	catch(IllegalArgumentException e) {
//	    m = new DefaultBoundedRangeModel(0, 0, 0, 100);
//	}
//	return m;
  //  }

    public long getCurrent() {
	return current;
    }

    /**
	* @param current The currently transfered bytes
     */
    public void setCurrent(long current) {
	//        log.debug("setCurrent(" + c + ")");
	this.current = current;
	this.callObservers(new Message(Message.DATA, this));
//	this.callObservers(new Message(Message.DATA, Status.parseDouble(this.getCurrent()/1024) + " of " + Status.parseDouble(this.getSize()/1024) + " kBytes."));
    }


    public static double parseDouble(double d) {
        //log.debug("Status.parseDouble(" + d + ")");
        String s = Double.toString(d);
        if(s.indexOf(".") != -1) {
            int l = s.substring(s.indexOf(".")).length();
            if(l > 3) {
                return Double.parseDouble(s.substring(0, s.indexOf('.') + 3));
            }
            else {
                return Double.parseDouble(s.substring(0, s.indexOf('.') + l));
            }
        }
        else {
            return d;
        }
    }
    

    public void setResume(boolean resume) {
	this.resume = resume;
	if(!resume)
	    this.setCurrent(0);
    }
    
    public boolean isResume() {
	return this.resume;
    }


    public void reset() {
	this.complete = false;
	this.canceled = false;
    }
}