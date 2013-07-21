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
import ch.cyberduck.core.LocalFactory;
import ch.cyberduck.ui.cocoa.foundation.NSBundle;
import ch.cyberduck.ui.cocoa.foundation.NSDictionary;
import ch.cyberduck.ui.cocoa.foundation.NSMutableDictionary;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;

/**
 * @version $Id$
 */
public class UserDefaultsPortablePreferences extends UserDefaultsPreferences {
    private static final Logger log = Logger.getLogger(UserDefaultsPortablePreferences.class);

    private NSMutableDictionary dict;

    @Override
    public void setProperty(String property, String value) {
        if(log.isInfoEnabled()) {
            log.info(String.format("Set property %s for key %s", value, property));
        }
        if(StringUtils.isNotEmpty(value)) {
            this.dict.setObjectForKey(value, property);
        }
        else {
            this.deleteProperty(property);
        }
    }

    @Override
    public void deleteProperty(String property) {
        this.dict.removeObjectForKey(property);
    }

    @Override
    protected void load() {
        super.load();
        final Local f = LocalFactory.createLocal(
                NSBundle.mainBundle().objectForInfoDictionaryKey("application.preferences.path").toString());
        if(f.exists()) {
            log.info(String.format("Found preferences file %s", f));
            this.dict = NSMutableDictionary.dictionary();
            this.dict.setDictionary(NSDictionary.dictionaryWithContentsOfFile(f.getAbsolute()));
        }
        else {
            this.dict = NSMutableDictionary.dictionary();
        }
    }

    @Override
    public void save() {
        Local f = LocalFactory.createLocal(NSBundle.mainBundle().objectForInfoDictionaryKey(
                "application.preferences.path").toString());
        f.getParent().mkdir();
        this.dict.writeToFile(f.getAbsolute());
    }
}