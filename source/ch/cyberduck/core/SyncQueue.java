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
import java.util.Observer;
import java.util.Iterator;
import java.util.List;

import com.apple.cocoa.foundation.NSMutableDictionary;

/**
 * @version $Id$
 */
public class SyncQueue extends Queue {

	/**
	* The observer to notify when an upload is complete
	 */
	private Observer callback;
	
	public SyncQueue() {
		//
	}
	
	public SyncQueue(Path root, Observer callback) {
		this.callback = callback;
		this.addRoot(root);
	}
	
	public SyncQueue(java.util.Observer callback) {
		this.callback = callback;
	}
	
	public NSMutableDictionary getAsDictionary() {
		NSMutableDictionary dict = super.getAsDictionary();
		dict.setObjectForKey(Queue.KIND_SYNC+"", "Kind");
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
	
	private void addLocalChilds(List childs, Path root) {
		if(root.getLocal().exists()) {
			if(root.attributes.isDirectory()) {
				childs.add(root);
				File[] files = root.getLocal().listFiles();
				for(int i = 0; i < files.length; i++) {
					Path child = PathFactory.createPath(root.getSession(), root.getAbsolute(), new Local(files[i].getAbsolutePath()));
					if(!child.getName().equals(".DS_Store")) {
						this.addLocalChilds(childs, child);
					}
				}
			}
			if(root.attributes.isFile()) {
				if(!childs.contains(root)) {
					childs.add(root);
				}
			}
		}
	}

	private void addRemoteChilds(List childs, Path root) {
		if(root.getRemote().exists()) {
			childs.add(root);
			if(root.attributes.isDirectory() && !root.attributes.isSymbolicLink()) {
				for(Iterator i = root.list(false, true).iterator(); i.hasNext();) {
					Path child = (Path)i.next();
					child.setLocal(new Local(root.getLocal(), child.getName()));
					this.addRemoteChilds(childs, child);
				}
			}
			if(root.attributes.isFile()) {
				if(!childs.contains(root)) {
					childs.add(root);
				}
			}
		}
	}

	protected List getChilds(List childs, Path root) {
		this.addRemoteChilds(childs, root);
		this.addLocalChilds(childs, root);
		return childs;
	}

	//@todo localgetsize vs. remotegetsize
	//cache value upon validation
	public long getSize() {
		if(/*this.worker.isRunning() && */this.worker.isInitialized()) {
			long size = 0;
			for(Iterator iter = this.getJobs().iterator(); iter.hasNext();) {
				size += ((Path)iter.next()).getSize();
			}
			this.size = size;
		}
		return this.size; //cached value
	}
	
	protected void process(Path p) {
		p.sync();
	}
}