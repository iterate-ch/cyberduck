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

package ch.cyberduck.ui.swing;

import org.apache.log4j.Logger;
import java.util.Properties;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.awt.Dimension;

import ch.cyberduck.core.Preferences;

public class PreferencesImpl extends Preferences {

    private static Logger log = Logger.getLogger(Preferences.class);

    private static File PREFS_DIRECTORY = new File(System.getProperty("user.home"), ".cyberduck");
    private static final String PREFERENCES_FILE = "cyberduck.preferences";
    private static Dimension screenSize = null;

    private Properties defaults;
    
    public PreferencesImpl() {
	PREFS_DIRECTORY.mkdir();
	this.defaults = new Properties();
    }

    public String getProperty(String property) {
        log.debug("getProperty(" + property + ")");
        String value = defaults.getProperty(property);
        if(value == null)
            throw new IllegalArgumentException("No property with key '" + property.toString() + "'");
        return value;
    }

    public void setProperty(String property, String value) {
        log.debug("setProperty(" + property + ", " + value + ")");
        defaults.put(property, value);
    }

    public void setProperty(String property, boolean v) {
        log.debug("setProperty(" + property + ", " + v + ")");
        String value = "false";
        if (v) {
            value = "true";
        }
        defaults.put(property, value);
    }
    
    public void setProperty(String property, int v) {
        log.debug("setProperty(" + property + ", " + v + ")");
        String value = String.valueOf(v);
        defaults.put(property, value);
    }

    public void load() {
        log.debug("load()");
        try {
            File prefs = new File(PREFS_DIRECTORY, PREFERENCES_FILE);
            if (prefs.exists()) {
                defaults.load(new FileInputStream(prefs));
            }
            else {
                log.error("Could not load current preferences.");
            }
        }
        catch(IOException e) {
            log.error("Could not load current preferences.\n" + e.getMessage());
        }
    }

    public void store() {
        log.debug("store()");
        try {
            FileOutputStream output = new FileOutputStream(new File(PREFS_DIRECTORY, PREFERENCES_FILE));
            defaults.store(output, "Cyberduck properties - YOU SHOULD NOT EDIT THIS FILE");
            output.close();
        }
        catch(IOException e) {
            log.error("Could not save current preferences.\n" + e.getMessage());
        }
    }    

    public void list() {
        defaults.list(System.out);
    }

    private String getXLocation(int componentWidth) {
        return new Integer((screenSize.width/2) - (componentWidth/2)).toString();
    }

    private String getYLocation(int componentHeight) {
        return new Integer((screenSize.height/2) - (componentHeight/2)).toString();
    }
    
}
