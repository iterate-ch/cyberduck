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

import com.apple.cocoa.foundation.*;

import org.apache.log4j.Logger;

import java.io.File;

/**
 * Concrete subclass using the Cocoa Preferences classes.
 *
 * @version $Id$
 * @see com.apple.cocoa.foundation.NSUserDefaults
 */
public class CDPreferencesImpl extends Preferences {
    private static Logger log = Logger.getLogger(CDPreferencesImpl.class);

    private NSUserDefaults props;

    protected static File APP_SUPPORT_DIR;

    static {
        if(null == NSBundle.mainBundle().objectForInfoDictionaryKey("application.support.path")) {
           APP_SUPPORT_DIR = new File(
                   NSPathUtilities.stringByExpandingTildeInPath("~/Library/Application Support/Cyberduck"));
        }
        else {
            APP_SUPPORT_DIR = new File(NSPathUtilities.stringByExpandingTildeInPath(
                (String)NSBundle.mainBundle().objectForInfoDictionaryKey("application.support.path")));
        }
        APP_SUPPORT_DIR.mkdir();
    }

    public String getProperty(String property) {
        String value = (String) props.objectForKey(property);
        if (null == value) {
            return super.getProperty(property);
        }
        return value;
    }

    public void setProperty(String property, String value) {
        if (log.isDebugEnabled()) {
            log.debug("setProperty(" + property + ", " + value + ")");
        }
        this.props.setObjectForKey(value, property);
    }

    public void setProperty(String property, boolean v) {
        if (log.isDebugEnabled()) {
            log.debug("setProperty(" + property + ", " + v + ")");
        }
        String value = "false";
        if (v) {
            value = "true";
        }
        //Sets the value of the default identified by defaultName in the standard application domain.
        // Setting a default has no effect on the value returned by the objectForKey method if
        // the same key exists in a domain that precedes the application domain in the search list.
        this.props.setObjectForKey(value, property);
    }

    public void setProperty(String property, int v) {
        if (log.isDebugEnabled()) {
            log.debug("setProperty(" + property + ", " + v + ")");
        }
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
        this.props = NSUserDefaults.standardUserDefaults();
        if(NSBundle.mainBundle().objectForInfoDictionaryKey("application.preferences.path") != null) {
            File f = new File(NSPathUtilities.stringByExpandingTildeInPath(
                    (String)NSBundle.mainBundle().objectForInfoDictionaryKey("application.preferences.path")));
            if (f.exists()) {
                log.info("Found preferences file: " + f.toString());
                NSData plistData = new NSData(f);
                String[] errorString = new String[]{null};
                Object propertyListFromXMLData =
                        NSPropertyListSerialization.propertyListFromData(plistData,
                                NSPropertyListSerialization.PropertyListMutableContainersAndLeaves,
                                new int[]{NSPropertyListSerialization.PropertyListXMLFormat},
                                errorString);
                if (errorString[0] != null) {
                    log.error("Problem reading preferences file: " + errorString[0]);
                }
                if (propertyListFromXMLData instanceof NSDictionary) {
                    NSUserDefaults.standardUserDefaults().setPersistentDomainForName(
                            (NSDictionary)propertyListFromXMLData,
                            NSBundle.mainBundle().bundleIdentifier());
                }
            }
        }
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

        if(NSBundle.mainBundle().objectForInfoDictionaryKey("application.preferences.path") != null) {
            try {
                NSMutableData collection = new NSMutableData();
                String[] errorString = new String[]{null};
                collection.appendData(NSPropertyListSerialization.dataFromPropertyList(
                        NSUserDefaults.standardUserDefaults().persistentDomainForName(
                                NSBundle.mainBundle().bundleIdentifier()),
                        NSPropertyListSerialization.PropertyListXMLFormat,
                        errorString));
                if (errorString[0] != null) {
                    log.error("Problem writing queue file: " + errorString[0]);
                }
                File f = new File(NSPathUtilities.stringByExpandingTildeInPath(
                        (String)NSBundle.mainBundle().objectForInfoDictionaryKey("application.preferences.path")));
                if (collection.writeToURL(f.toURL(), true)) {
                    log.info("Preferences sucessfully saved to :" + f.toString());
                }
                else {
                    log.error("Error saving preferences to :" + f.toString());
                }
            }
            catch (java.net.MalformedURLException e) {
                log.error(e.getMessage());
            }
        }
    }
}
