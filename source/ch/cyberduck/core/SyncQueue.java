package ch.cyberduck.core;

/*
 *  Copyright (c) 2005 David Kocher. All rights reserved.
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
import java.util.Iterator;
import java.util.List;
import java.util.Observer;

import ch.cyberduck.ui.cocoa.growl.Growl;

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

	public SyncQueue(Path root) {
		super(root);
	}
		
	public SyncQueue(Path root, Observer callback) {
		super(root);
		this.callback = callback;
	}

	public SyncQueue(java.util.Observer callback) {
		this.callback = callback;
	}

	public NSMutableDictionary getAsDictionary() {
		NSMutableDictionary dict = super.getAsDictionary();
		dict.setObjectForKey(String.valueOf(Queue.KIND_SYNC), "Kind");
		return dict;
	}

	protected void finish(boolean shouldCloseAfterTransfer) {
		super.finish(shouldCloseAfterTransfer);
		if(this.isComplete() && !this.isCanceled()) {
			this.callObservers(new Message(Message.PROGRESS, NSBundle.localizedString("Synchronization complete",
																					  "Growl Notification")));
			this.callObservers(new Message(Message.QUEUE_STOP));
			Growl.instance().notify(NSBundle.localizedString("Synchronization complete",
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

	private void addLocalChilds(List childs, Path p) {
		if(!this.isCanceled()) {
			if(p.getLocal().exists()) {// && p.getLocal().canRead()) {
				if(!childs.contains(p)) {
					childs.add(p);
				}
				if(p.attributes.isDirectory()) {
					File[] files = p.getLocal().listFiles();
					for(int i = 0; i < files.length; i++) {
						Path child = PathFactory.createPath(p.getSession(), p.getAbsolute(), new Local(files[i].getAbsolutePath()));
						if(!child.getName().equals(".DS_Store")) {
							this.addLocalChilds(childs, child);
						}
					}
				}
			}
		}
	}

	private void addRemoteChilds(List childs, Path p) {
		if(!this.isCanceled()) {
			if(p.getRemote().exists()) {
				if(!childs.contains(p)) {
					childs.add(p);
				}
				if(p.attributes.isDirectory() && !p.attributes.isSymbolicLink()) {
					p.attributes.setSize(0);
					for(Iterator i = p.list(false, new NullFilter()).iterator(); i.hasNext();) {
						Path child = (Path)i.next();
						child.setLocal(new Local(p.getLocal(), child.getName()));
						this.addRemoteChilds(childs, child);
					}
				}
			}
		}
	}

	protected List getChilds(List childs, Path root) {
		this.addRemoteChilds(childs, root);
		this.addLocalChilds(childs, root);
		return childs;
	}

	protected void reset() {
		this.size = 0;
		for(Iterator iter = this.getJobs().iterator(); iter.hasNext();) {
			Path path = ((Path)iter.next());
			if(path.getRemote().exists() && path.getLocal().exists()) {
				if(path.getLocal().getTimestampAsCalendar().before(path.attributes.getTimestampAsCalendar())) {
					this.size += path.getRemote().attributes.getSize();
				}
				if(path.getLocal().getTimestampAsCalendar().after(path.attributes.getTimestampAsCalendar())) {
					this.size += path.getLocal().getSize();
				}
			}
			else if(path.getRemote().exists()) {
				this.size += path.getRemote().attributes.getSize();
			}
			else if(path.getLocal().exists()) {
				this.size += path.getLocal().getSize();
			}
		}
	}

	protected void process(Path p) {
		p.sync();
	}
}