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
 * @version $Id$
 */
public class CDPortablePreferencesImpl extends Preferences {
    private static Logger log = Logger.getLogger(CDPortablePreferencesImpl.class);

    private NSMutableDictionary props;

    public Object getObject(String property) {
        Object value = this.props.objectForKey(property);
        if (null == value) {
            return super.getObject(property);
        }
        return value;
    }

    public void setProperty(String property, Object value) {
        if (log.isDebugEnabled()) {
            log.debug("setProperty(" + property + ", " + value + ")");
        }
        this.props.setObjectForKey(value, property);
    }

    public void deleteProperty(String property) {
        this.props.removeObjectForKey(property);
    }

    public void load() {
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
                this.props = (NSMutableDictionary)propertyListFromXMLData;
            }
        }
        else {
            this.props = new NSMutableDictionary();
        }
    }

    public void save() {
        try {
            NSMutableData collection = new NSMutableData();
            String[] errorString = new String[]{null};
            collection.appendData(NSPropertyListSerialization.dataFromPropertyList(
                    this.props,
                    NSPropertyListSerialization.PropertyListXMLFormat,
                    errorString));
            if (errorString[0] != null) {
                log.error("Problem writing preferences file: " + errorString[0]);
            }
            File f = new File(NSPathUtilities.stringByExpandingTildeInPath(
                    (String)NSBundle.mainBundle().objectForInfoDictionaryKey("application.preferences.path")));
            f.getParentFile().mkdirs();
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
