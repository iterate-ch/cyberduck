package ch.cyberduck.ui;

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

import java.util.ArrayList;
import java.util.Iterator;
import java.util.Observable;
import java.util.Observer;

/**
* All observers must register here to be notified.
 * @version $Id$
 */
public class ObserverList extends ArrayList { //@todo extends HashMap {

    private static ObserverList instance = null;

    /**
    * Use #instance instead
     */
    private ObserverList() {
        super();
    }

    public static ObserverList instance() {
        if(instance == null) {
            instance = new ObserverList();
        }
        return instance;
    }

    /**
        * Register an Observer here that he gets notfied by all
     * registered Observables (i.e. StatusPanel)
     * @see ch.cyberduck.ui.swing.StatusPanel
     */
    public void registerObserver(Observer o) {
        this.add(o);
    }

    /** Register an observer to get notified of changes in instances of the supplied class
	* @param o The Observer to register
	* @param clazz The class the observer is interested in, must be 
	*/
    public void registerObserver(Observer o, Class clazz) {
	if(clazz.getSuperclass() != Observable.class)
	    throw new IllegalArgumentException(clazz.getName()+" must be a child of Observable.");
	//HashMap.put(key, value);
	//this.put(o, clazz);
	
    }

    /**
	* All known Observers get attached to the Observable with <code>o.addObserver()</code>
     * @param o The Observable (i.e. Bookmark)
     * @see ch.cyberduck.ui.connection.Bookmark
     */
    public void registerObservable(Observable o) {

	//Set values = this.entrySet();
	//Set keys = this.keySet();


	Iterator i = this.iterator();
        Observer next;
        while(i.hasNext()) {
            next = (Observer)i.next();
            o.addObserver(next);
        }
    }

    public void removeObservable(Observable o) {
	//@todo
    }
}
