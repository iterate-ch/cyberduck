package ch.cyberduck.ui.cocoa;

/*
 *  Copyright (c) 2003 David Kocher. All rights reserved.
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

import ch.cyberduck.core.Preferences;
import com.apple.cocoa.foundation.NSPathUtilities;
import com.apple.cocoa.foundation.NSUserDefaults;
import org.apache.log4j.Logger;

/**
* Concrete subclass using the Cocoao Preferences classes.
 * @see com.apple.cocoa.foundation.NSUserDefaults
 * @version $Id$
 */
public class CDPreferencesImpl extends Preferences {
    private static Logger log = Logger.getLogger(CDPreferencesImpl.class);
    
    private NSUserDefaults props = NSUserDefaults.standardUserDefaults();
    private static final File APP_SUPPORT_DIR = new File(NSPathUtilities.stringByExpandingTildeInPath("~/Library/Application Support/Cyberduck"));
	
    static {
		APP_SUPPORT_DIR.mkdir();
    }
	
    public String getProperty(String property) {
		//        log.debug("getProperty(" + property + ")");
        String value = (String)props.objectForKey(property);
        if(value == null)
            return super.getProperty(property);
        return value;
    }
	
    public void setProperty(String property, String value) {
        log.debug("setProperty(" + property + ", " + value + ")");
        this.props.setObjectForKey(value, property);
    }
	
    public void setProperty(String property, boolean v) {
        log.debug("setProperty(" + property + ", " + v + ")");
        String value = "false";
        if (v) {
            value = "true";
        }
		//Sets the value of the default identified by defaultName in the standard application domain. Setting a default has no effect on the value returned by the objectForKey method if the same key exists in a domain that precedes the application domain in the search list.
        this.props.setObjectForKey(value, property);
    }
	
    public void setProperty(String property, int v) {
        log.debug("setProperty(" + property + ", " + v + ")");
        String value = String.valueOf(v);
        this.props.setObjectForKey(value, property);
    }
	
    public void setDefaults() {
		super.setDefaults();
    }
	
    /**
		* Overwrite the default values with user props if any.
     */
    public void load() {
		//        log.debug("load()");
		this.props = NSUserDefaults.standardUserDefaults();
    }
	
    public void save() {
		// Saves any modifications to the persistent domains and updates all persistent domains that were not modified to
  // what is on disk. Returns false if it could not save data to disk. Because synchronize is automatically invoked at
  // periodic intervals, use this method only if you cannot wait for the automatic synchronization (for example, if your
  // application is about to exit) or if you want to update user props to what is on disk even though you have not made
  // any changes.
		this.props.synchronize();
    }
}
