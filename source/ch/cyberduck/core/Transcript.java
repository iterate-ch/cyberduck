package ch.cyberduck.core;

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

import org.apache.log4j.Logger;
import java.util.Iterator;
import java.util.List;
import java.util.ArrayList;

/**
* Singleton
 * @version $Id$
 */
public class Transcript {
    private static Logger log = Logger.getLogger(Transcript.class);

    private static Transcript instance;

    private List listeners = new ArrayList();
    
    private Transcript() {
    //
    }
    
    public static Transcript instance() {
        if(null == instance) {
	    //@todo return a transcript dependant of hostinfo
	    instance = new Transcript();
	}
        return instance;
     }

    /**
	* Must be overwritten by the subclass to return the view widget
     */
//    public abstract Object getView();

    /**
    * @param l Add this Transripter to the list of listeners
    */
    public void addListener(Transcripter l) {
	listeners.add(l);
    }
    
    /**
    * @param l Remove this Transripter form the listeneres list
    */
    public void removeListener(Transcripter l) {
	listeners.remove(listeners.indexOf(l));
    }

    /*
     /**
     * Inform all the concrete listeners about
     * @param text The information to pass to the listeners
      */
     public synchronized void transcript(String text) {
//	 log.debug("transcript:"+text);
	 Iterator i = listeners.iterator();
	 while(i.hasNext()) {
	     ((Transcripter)i.next()).transcript(text);
	 }
     }
}
