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

import com.apple.cocoa.foundation.*;

import java.util.ArrayList;
import java.util.List;
import java.io.File;

/**
* @version $Id$
 */
public class UploadQueue extends Queue {
	
	public UploadQueue() {
		super();
    }

	public UploadQueue(java.util.Observer callback) {
		super(callback);
    }
	
	public NSMutableDictionary getAsDictionary() {
        NSMutableDictionary dict = super.getAsDictionary();
        dict.setObjectForKey(Queue.KIND_UPLOAD+"", "Kind");
        return dict;
    }
	
	public List getChilds(Path p) {
		return this.getChilds(new ArrayList(), p);
	}
	
	private List getChilds(List list, Path p) {
        list.add(p);
        if (p.getLocal().isDirectory()) {
            p.attributes.setType(Path.DIRECTORY_TYPE);
            p.status.setSize(0); //@todo
            File[] files = p.getLocal().listFiles();
            for (int i = 0; i < files.length; i++) {
                Path child = PathFactory.createPath(p.getSession(), p.getAbsolute(), new Local(files[i].getAbsolutePath()));
                // users complaining about .DS_Store files getting uploaded. It should be apple fixing their crappy file system, but whatever.
                if (!child.getName().equals(".DS_Store")) {
                    this.getChilds(list, child);
                }
            }
        }
        else if (p.getLocal().isFile()) {
            p.attributes.setType(Path.FILE_TYPE);
            p.status.setSize(p.getLocal().size()); //setting the file size to the known size of the local file
        }
		return list;
	}
	
	protected void process(Path p) {
		p.upload();
	}	
}