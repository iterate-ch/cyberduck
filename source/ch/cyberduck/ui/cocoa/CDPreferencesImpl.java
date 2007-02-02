package ch.cyberduck.ui.cocoa;

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

import ch.cyberduck.core.Preferences;

import com.apple.cocoa.foundation.NSUserDefaults;

import org.apache.log4j.Logger;

/**
 * Concrete subclass using the Cocoa Preferences classes.
 *
 * @version $Id$
 * @see com.apple.cocoa.foundation.NSUserDefaults
 */
public class CDPreferencesImpl extends Preferences {
    private static Logger log = Logger.getLogger(Preferences.class);

    private NSUserDefaults props;

    public Object getObject(String property) {
        Object value = props.objectForKey(property);
        if (null == value) {
            return super.getObject(property);
        }
        return value;
    }

    public void setProperty(String property, Object value) {
        log.info("setProperty:"+property+","+value);
        // Sets the value of the default identified by defaultName in the standard application domain.
        // Setting a default has no effect on the value returned by the objectForKey method if
        // the same key exists in a domain that precedes the application domain in the search list.
        this.props.setObjectForKey(value, property);
        this.save();
    }

    public void deleteProperty(String property) {
        this.props.removeObjectForKey(property);
        this.save();
    }

    /**
     * Overwrite the default values with user props if any.
     */
    public void load() {
        this.props = NSUserDefaults.standardUserDefaults();

        // Setting default values that must be accessible using [NSUserDefaults standardUserDefaults]
        if(null == props.objectForKey("browser.view.autoexpand.useDelay"))
            this.setProperty("browser.view.autoexpand.useDelay", super.getProperty("browser.view.autoexpand.useDelay"));
        if(null == props.objectForKey("browser.view.autoexpand.delay"))
            this.setProperty("browser.view.autoexpand.delay", super.getProperty("browser.view.autoexpand.delay"));
    }

    public void save() {
        // Saves any modifications to the persistent domains and updates all
        // persistent domains that were not modified to  what is on disk.
        // Returns false if it could not save data to disk. Because synchronize
        // is automatically invoked at periodic intervals, use this method only
        // if you cannot wait for the automatic synchronization (for example, if
        // your application is about to exit) or if you want to update user props
        // to what is on disk even though you have not made any changes.
        this.props.synchronize();
    }
}
