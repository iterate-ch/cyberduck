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

import java.io.Serializable;
import java.util.Observable;

import com.apple.cocoa.foundation.NSDictionary;
import com.apple.cocoa.foundation.NSMutableDictionary;

import org.apache.log4j.Logger;

/**
 * The Status class is the model of a download's status.
 * The wrapper for any status informations of a transfer as the size and transferred
 * bytes.
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
	private long size = 0;
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
		super(); //
	}

	public Status(NSDictionary dict) {
		this.size = Integer.parseInt((String)dict.objectForKey("Size"));
		this.current = Integer.parseInt((String)dict.objectForKey("Current"));
	}
	
	public NSDictionary getAsDictionary() {
		NSMutableDictionary dict = new NSMutableDictionary();
		dict.setObjectForKey(this.size+"", "Size");
		dict.setObjectForKey(this.current+"", "Current");
		return dict;
	}
	
	/**
	 * Notify all observers
	 * @param arg The message to send to the observers
	 * @see ch.cyberduck.core.Message
	 */
	public void callObservers(Message arg) {
		//	log.debug(this.countObservers()+" observers known.");
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
//		log.debug(this.toString()+">getSize:"+this.size);
		return size;
	}

	private static final long KILO = 1024; //2^10
	private static final long MEGA = 1048576; // 2^20
	private static final long GIGA = 1073741824; // 2^30

	/**
	 * @return The size of the file
	 */
	public static String getSizeAsString(long size) {
		//@todo fix GB limitation
		//@todo return correct size
		if (size < KILO)
			return size + "B";
		else if (size < MEGA)
			return new Double(size / KILO).doubleValue() + "kB";
		else if (size < GIGA)
			return new Double(size / MEGA).doubleValue() + "MB";
		else
			return new Double(size / GIGA).doubleValue() + "GB";
	}

	public void setComplete(boolean complete) {
		this.complete = complete;
//		if (complete)
//			this.setCurrent(this.getSize());
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
//		log.debug(this.toString()+">getCurrent:"+this.current);
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
		this.resume = resume;
//		if (!resume)
//			this.setCurrent(0);
	}

	public boolean isResume() {
		return this.resume;
	}

	public void reset() {
		log.debug("reset");
		this.complete = false;
		this.canceled = false;
		if(!this.isResume()) {
			log.debug("***reset>Setting current to 0, no resume***");
			this.setCurrent(0);
		}
	}
}