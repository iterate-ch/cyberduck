/*
 *  Copyright (c) 2002 David Kocher. All rights reserved.
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

package ch.cyberduck.core;

//import java.util.List;
//import java.util.ArrayList;
import org.apache.log4j.Logger;

/**
 * @version $Id$
 */
public class Transcript {
    private static Logger log = Logger.getLogger(Transcript.class);

    private static Transcript instance;
//    private List listeners = new ArrayList();

    private Transcripter listener;
    
    public static Transcript instance() {
	log.debug("instance");
        if(null == instance) {
	    instance = new Transcript();
	}
        return instance;
     }

    public void addListener(Transcripter l) {
	log.debug("addListener:"+l);
//	listeners.add(l);
	this.listener = l;
    }

    /*
     /**
     * Write to the view
      */
     public void transcript(String text) {
	 log.debug("transcript:"+text);
	 /*
	 Iterator i = listeners.iterator();
	 while(i.hasNext()) {
	     ((Listener)i).transcript(text);
	 }
	  */
	 listener.transcript(text);
     }

}
