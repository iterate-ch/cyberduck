package ch.cyberduck.core;

/*
 *  Copyright (c) 2003 David Kocher. All rights reserved.
 *  http://icu.unizh.ch/~dkocher/
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

import java.util.Observable;
import java.util.Observer;
import java.util.Vector;
import java.util.Iterator;
import org.apache.log4j.Logger;

/**
* Used to queue multiple connections. <code>queue.start()</code> will
 * start the the connections in the order the have been added to me.
 * Useful for actions where the reihenfolge of the taken actions
 * is important, i.e. deleting directories or uploading directories.
 * @version $Id$
 */
public class Queue extends Thread implements Observer {
    private static Logger log = Logger.getLogger(Queue.class);

    public static final int KIND_DOWNLOAD = 0;
    public static final int KIND_UPLOAD = 1;

    private Vector files = new java.util.Vector();
    private Observer observer;
    private int kind;

    /**
	* The size of all files accumulated
     */
    private int size = -1;

    public Queue(int kind) {
	this.kind = kind;
    }

    /**
	*
     */
    public void add(Path file) {
	log.debug("Adding file to queue:"+file);
	file.status.addObserver(this);
	switch(kind) {
	    case KIND_DOWNLOAD:
		file.getDownloadSession().addObserver(this);
	    case KIND_UPLOAD:
		file.getUploadSession().addObserver(this);
	}
        files.add(file);
    }

    public void addObserver(Observer observer) {
	this.observer = observer;
    }

    public void update(Observable o, Object arg) {
	observer.update(o, arg);
    }

    public void run() {
	Iterator i = files.iterator();
	Path file = null;
	while(i.hasNext()) {
            file = (Path)i.next();
	    switch(kind) {
		case KIND_DOWNLOAD:
		    file.download();
		    break;
		case KIND_UPLOAD:
		    file.upload();
		    break;
	    }
	}
    }

    public int numberOfElements() {
	return files.size();
    }

    public int size() {
	if(-1 == this.size) {
	    this.size = 0;
	    Iterator i = files.iterator();
	    Path file = null;
	    while(i.hasNext()) {
		file = (Path)i.next();
		this.size = this.size + file.status.getSize();
	    }
	}
	return this.size;
    }
}

/**
* Execute pending connections in the order they have been added to the queue - first added gets first
 * executed.
 */
//    public void run() {
//        java.util.Iterator i = threads.iterator();
//        Thread thread = null;
//        while (i.hasNext()) {
//            if(thread != null) {
//                if(thread.isAlive()) {
//                    try {
//                        thread.join();
//                    }
//                    catch(InterruptedException e) {
//                        e.printStackTrace();
//                    }
//                }
//            }
//            thread = (Thread)i.next();
//            log.debug("Starting next thread");
//            thread.start();
//        }
//    }
