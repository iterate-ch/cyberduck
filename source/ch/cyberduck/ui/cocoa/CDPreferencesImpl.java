package ch.cyberduck.ui.cocoa;

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

import ch.cyberduck.core.Preferences;
import com.apple.cocoa.application.*;
import com.apple.cocoa.foundation.*;
import org.apache.log4j.Logger;

/**
* Concrete subclass using the Cocoao Preferences classes.
* @see com.apple.cocoa.foundation.NSUserprops
* @version $Id$
*/
public class CDPreferencesImpl extends Preferences { //CDPreferencesImplCocoa
    private static Logger log = Logger.getLogger(CDPreferencesImpl.class);

    private NSUserDefaults props = NSUserDefaults.standardUserDefaults();

    public String getProperty(String property) {
        log.info("getProperty(" + property + ")");
        String value = (String)props.objectForKey(property);
        if(value == null)
            return super.getProperty(property);
        return value;
    }

    public void setProperty(String property, String value) {
        log.info("setProperty(" + property + ", " + value + ")");
        props.setObjectForKey(value, property);
    }

    public void setProperty(String property, boolean v) {
        log.info("setProperty(" + property + ", " + v + ")");
        String value = "false";
        if (v) {
            value = "true";
        }
	//NSUserprops.setObjectForKey( Object value, String defaultName)
	//Sets the value of the default identified by defaultName in the standard application domain. Setting a default has no effect on the value returned by the objectForKey method if the same key exists in a domain that precedes the application domain in the search list.
        props.setObjectForKey(value, property);
    }

    public void setProperty(String property, int v) {
        log.info("setProperty(" + property + ", " + v + ")");
        String value = String.valueOf(v);
        props.setObjectForKey(value, property);
    }

    public void setDefaults() {
	super.setDefaults();
//	log.debug(NSBundle.mainBundle().resourcePath());
	System.setProperty("sshtools.home", NSBundle.mainBundle().resourcePath());
//	System.setProperty("sshtools.config", NSBundle.mainBundle().resourcePath());
    }

    /**
	* Overwrite the default values with user props if any.
     */
    public void load() {
        log.debug("load()");
	props = NSUserDefaults.standardUserDefaults();
    }

    public void save() {
	// Saves any modifications to the persistent domains and updates all persistent domains that were not modified to
	// what is on disk. Returns false if it could not save data to disk. Because synchronize is automatically invoked at
	// periodic intervals, use this method only if you cannot wait for the automatic synchronization (for example, if your
	// application is about to exit) or if you want to update user props to what is on disk even though you have not made
	// any changes.
	props.synchronize();
    }
}
