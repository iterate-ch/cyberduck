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

import com.apple.cocoa.foundation.NSBundle;
import com.apple.cocoa.foundation.NSMutableDictionary;

import java.io.File;
import java.util.List;
import java.util.Observer;

import ch.cyberduck.ui.cocoa.growl.Growl;

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

	public UploadQueue(Path root) {
		super(root);
	}
	
	public UploadQueue(Path root, Observer callback) {
		super(root);
		this.callback = callback;
	}

	public UploadQueue(java.util.Observer callback) {
		this.callback = callback;
	}

	public NSMutableDictionary getAsDictionary() {
		NSMutableDictionary dict = super.getAsDictionary();
		dict.setObjectForKey(String.valueOf(Queue.KIND_UPLOAD), "Kind");
		return dict;
	}

	protected void finish() {
		super.finish();
		if(this.isComplete()) {
			this.callObservers(new Message(Message.PROGRESS, NSBundle.localizedString("Upload complete",
																					  "Growl Notification")));
			this.callObservers(new Message(Message.QUEUE_STOP));
			Growl.instance().notify(NSBundle.localizedString("Upload complete",
															 "Growl Notification"),
									this.getName());
			if(callback != null) {
				callback.update(null, new Message(Message.REFRESH));
			}
		}
		else {
			this.callObservers(new Message(Message.QUEUE_STOP));
		}
	}
	
	protected List getChilds(List childs, Path p) {
		if(p.getLocal().exists()) {// && p.getLocal().canRead()) {
			childs.add(p);
			if(p.attributes.isDirectory()) {
				p.attributes.setSize(0);
				File[] files = p.getLocal().listFiles();
				for(int i = 0; i < files.length; i++) {
					if(files[i].canRead()) {
						Path child = PathFactory.createPath(p.getSession(), p.getAbsolute(), new Local(files[i].getAbsolutePath()));
						// users complaining about .DS_Store files getting uploaded. It should be apple fixing their crappy file system, but whatever.
						if(!child.getName().equals(".DS_Store")) {
							this.getChilds(childs, child);
						}
					}
				}
			}
		}
		return childs;
	}

	protected void reset() {
		this.size = 0;
		for(java.util.Iterator iter = this.getJobs().iterator(); iter.hasNext();) {
			this.size += ((Path)iter.next()).getLocal().getSize();
		}
	}

	protected void process(Path p) {
		p.upload();
	}
}