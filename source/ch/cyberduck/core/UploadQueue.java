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

import java.io.File;
import java.util.List;
import java.util.Observer;

import com.apple.cocoa.foundation.NSMutableDictionary;

/**
 * @version $Id$
 */
public class UploadQueue extends Queue {

	/**
	* The observer to notify when an upload is complete
	 */
	private Observer callback;
	
	public UploadQueue() {
		//
	}

	public UploadQueue(Path root, Observer callback) {
		this.callback = callback;
		this.addRoot(root);
	}
		
	public UploadQueue(java.util.Observer callback) {
		this.callback = callback;
	}

	public NSMutableDictionary getAsDictionary() {
		NSMutableDictionary dict = super.getAsDictionary();
		dict.setObjectForKey(Queue.KIND_UPLOAD+"", "Kind");
		return dict;
	}

	public void callObservers(Object arg) {
		super.callObservers(arg);
		if(arg instanceof Message) {
			Message msg = (Message)arg;
			if(msg.getTitle().equals(Message.QUEUE_STOP)) {
				if(this.isComplete()) {
					if(callback != null) {
						callback.update(null, new Message(Message.REFRESH));
					}
				}
			}
		}
	}
	
	protected List getChilds(List childs, Path p) {
		if(p.attributes.isDirectory()) {
			childs.add(p);
			File[] files = p.getLocal().listFiles();
			for(int i = 0; i < files.length; i++) {
				Path child = PathFactory.createPath(p.getSession(), p.getAbsolute(), new Local(files[i].getAbsolutePath()));
				// users complaining about .DS_Store files getting uploaded. It should be apple fixing their crappy file system, but whatever.
				if(!child.getName().equals(".DS_Store")) {
					this.getChilds(childs, child);
				}
			}
		}
		if(p.attributes.isFile()) {
			childs.add(p);
		}
		return childs;
	}
	
	public long getSize() {
		if(/*this.worker.isRunning() && */this.worker.isInitialized()) {
			long size = 0;
			for(java.util.Iterator iter = this.getJobs().iterator(); iter.hasNext();) {
				size += ((Path)iter.next()).getLocal().getSize();
			}
			this.size = size;
		}
		return this.size; //cached value
	}	

	protected void process(Path p) {
		p.upload();
	}
}