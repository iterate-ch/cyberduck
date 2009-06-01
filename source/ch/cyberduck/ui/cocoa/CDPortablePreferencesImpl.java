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
import ch.cyberduck.ui.cocoa.foundation.*;

import org.apache.log4j.Logger;

import java.io.File;
import java.io.IOException;

/**
 * @version $Id$
 */
public class CDPortablePreferencesImpl extends Preferences {
    private static Logger log = Logger.getLogger(CDPortablePreferencesImpl.class);

    private NSMutableDictionary dict;

    public Object getObject(String property) {
        Object value = dict.objectForKey(property);
        if(null == value) {
            return super.getObject(property);
        }
        return value;
    }

    public void setProperty(String property, Object value) {
        log.info("setProperty:" + property + "," + value);
        this.dict.setObjectForKey(value.toString(), property);
    }

    public void deleteProperty(String property) {
        this.dict.removeObjectForKey(property);
        this.save();
    }

    protected void load() {
        File f = new File(NSString.stringByExpandingTildeInPath(
                NSBundle.mainBundle().objectForInfoDictionaryKey("application.preferences.path").toString()));
        if(f.exists()) {
            log.info("Found preferences file: " + f.toString());
            NSData plistData = NSData.dataWithContentsOfURL(NSURL.fileURLWithPath(f.getAbsolutePath()));
            Object propertyListFromXMLData =
                    null;
            try {
                propertyListFromXMLData = NSPropertyListSerialization.propertyListFromData(plistData);
            }
            catch(IOException e) {
                log.error("Problem reading preferences file: " + e.getMessage());
            }
            if(propertyListFromXMLData instanceof NSDictionary) {
                this.dict = (NSMutableDictionary) propertyListFromXMLData;
            }
        }
        else {
            this.dict = NSMutableDictionary.dictionary();
        }
    }

    public void save() {
        NSMutableData collection = NSMutableData.dataWithLength(0);
        try {
            collection.appendData(NSPropertyListSerialization.dataFromPropertyList(this.dict));
        }
        catch(IOException e) {
            log.error("Problem writing preferences file: " + e.getMessage());
        }
        File f = new File(NSString.stringByExpandingTildeInPath(
                NSBundle.mainBundle().objectForInfoDictionaryKey("application.preferences.path").toString()));
        f.getParentFile().mkdirs();
        if(collection.writeToURL(NSURL.fileURLWithPath(f.getAbsolutePath()))) {
            log.info("Preferences sucessfully saved to :" + f.toString());
        }
        else {
            log.error("Error saving preferences to :" + f.toString());
        }
    }
}