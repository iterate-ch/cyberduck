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

import java.util.Iterator;
import java.util.List;

import com.apple.cocoa.foundation.NSMutableDictionary;

/**
 * @version $Id$
 */
public class DownloadQueue extends Queue {

	public NSMutableDictionary getAsDictionary() {
		NSMutableDictionary dict = super.getAsDictionary();
		dict.setObjectForKey(Queue.KIND_DOWNLOAD+"", "Kind");
		return dict;
	}

	protected List getChilds(List list, Path p) {
		list.add(p);
		if(p.attributes.isDirectory() && !p.attributes.isSymbolicLink()) {
			p.setSize(0);
			for(Iterator i = p.list(false, true).iterator(); i.hasNext();) {
				Path child = (Path)i.next();
				child.setLocal(new Local(p.getLocal(), child.getName()));
				this.getChilds(list, child);
			}
		}
		return list;
	}

	protected void reset() {
		this.size = 0;
		for(Iterator iter = this.getJobs().iterator(); iter.hasNext();) {
			this.size += ((Path)iter.next()).getSize();
		}
	}
		
	public long getSize() {
		if(/*this.worker.isRunning() && */this.worker.isInitialized()) {
			/*
			long size = 0;
			for(Iterator iter = this.getJobs().iterator(); iter.hasNext();) {
				size += ((Path)iter.next()).getSize();
			}
			this.size = size;
			 */
		}
		return this.size; //cached value
	}
	
	protected void process(Path p) {
		p.download();
	}
}