package ch.cyberduck.ui.swing;

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

import org.apache.log4j.Logger;
import java.util.Properties;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.awt.Dimension;

import ch.cyberduck.core.Preferences;

/**
* @version $Id$
 */
public class PreferencesImpl extends Preferences { //PreferencesImplSwing
    private static Logger log = Logger.getLogger(PreferencesImpl.class);

    private static final File PREFS_DIRECTORY = new File(System.getProperty("user.home"), ".cyberduck");
    private static final String PREFERENCES_FILE = "cyberduck.preferences";

    private Properties props;
    
    public PreferencesImpl() {
	PREFS_DIRECTORY.mkdir();
	this.props = new Properties();
    }

    public String getProperty(String property) {
        log.debug("getProperty(" + property + ")");
        String value = props.getProperty(property);
        if(value == null)
            return super.getProperty(property);
        return value;
    }

    public void setProperty(String property, String value) {
        log.debug("setProperty(" + property + ", " + value + ")");
        props.put(property, value);
    }

    public void setProperty(String property, boolean v) {
        log.debug("setProperty(" + property + ", " + v + ")");
        String value = "false";
        if (v) {
            value = "true";
        }
        props.put(property, value);
    }
    
    public void setProperty(String property, int v) {
        log.debug("setProperty(" + property + ", " + v + ")");
        String value = String.valueOf(v);
        props.put(property, value);
    }

    public void load() {
        log.debug("load()");
        try {
            File prefs = new File(PREFS_DIRECTORY, PREFERENCES_FILE);
            if (prefs.exists()) {
                props.load(new FileInputStream(prefs));
            }
            else {
                log.error("Could not load current preferences");
            }
        }
        catch(IOException e) {
            log.error("Could not load current preferences: " + e.getMessage());
        }
    }

    public void save() {
        log.debug("store()");
        try {
            FileOutputStream output = new FileOutputStream(new File(PREFS_DIRECTORY, PREFERENCES_FILE));
            props.store(output, "Cyberduck properties - YOU SHOULD NOT EDIT THIS FILE");
            output.close();
        }
        catch(IOException e) {
            log.error("Could not save current preferences: " + e.getMessage());
        }
    }    
}
