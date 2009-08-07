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

import ch.cyberduck.core.Local;
import ch.cyberduck.core.Preferences;
import ch.cyberduck.ui.cocoa.foundation.NSBundle;
import ch.cyberduck.ui.cocoa.foundation.NSDictionary;
import ch.cyberduck.ui.cocoa.foundation.NSMutableDictionary;

import org.apache.log4j.Logger;

/**
 * @version $Id$
 */
public class CDPortablePreferencesImpl extends Preferences {
    private static Logger log = Logger.getLogger(CDPortablePreferencesImpl.class);

    private NSMutableDictionary dict;

    @Override
    public Object getObject(String property) {
        Object value = dict.objectForKey(property);
        if(null == value) {
            return super.getObject(property);
        }
        return value;
    }

    public void setProperty(String property, String value) {
        log.info("setProperty:" + property + "," + value);
        this.dict.setObjectForKey(value, property);
    }

    public void deleteProperty(String property) {
        this.dict.removeObjectForKey(property);
        this.save();
    }

    protected void load() {
        Local f = new Local(NSBundle.mainBundle().objectForInfoDictionaryKey("application.preferences.path").toString());
        if(f.exists()) {
            log.info("Found preferences file: " + f.toString());
            this.dict = NSMutableDictionary.dictionary();
            this.dict.setDictionary(NSDictionary.dictionaryWithContentsOfFile(f.getAbsolute()));
        }
        else {
            this.dict = NSMutableDictionary.dictionary();
        }
    }

    public void save() {
        Local f = new Local(NSBundle.mainBundle().objectForInfoDictionaryKey("application.preferences.path").toString());
        f.getParent().mkdir();
        this.dict.writeToFile(f.getAbsolute());
    }
}