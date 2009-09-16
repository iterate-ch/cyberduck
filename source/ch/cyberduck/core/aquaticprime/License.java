package ch.cyberduck.core.aquaticprime;

/*
 *  Copyright (c) 2009 David Kocher. All rights reserved.
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
import ch.cyberduck.core.Native;
import ch.cyberduck.core.Preferences;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.filefilter.FalseFileFilter;
import org.apache.commons.io.filefilter.SuffixFileFilter;
import org.apache.log4j.Logger;

import java.io.File;
import java.util.Collection;

/**
 * @version $Id$
 */
public class License {
    private static Logger log = Logger.getLogger(License.class);

    private static boolean JNI_LOADED = false;

    private static boolean loadNative() {
        if(!JNI_LOADED) {
            JNI_LOADED = Native.load("Prime");
        }
        return JNI_LOADED;
    }

    /**
     * @return Null if no license found
     */
    public static License find() {
        final Collection<File> licenses = FileUtils.listFiles(
                new File(LocalFactory.createLocal(Preferences.instance().getProperty("application.support.path")).getAbsolute()),
                new SuffixFileFilter(".cyberducklicense"), FalseFileFilter.FALSE);
        for(File license : licenses) {
            return new License(LocalFactory.createLocal(license));
        }
        log.info("No license found");
        return License.EMPTY;
    }

    private static final License EMPTY = new License() {
        @Override
        public boolean verify() {
            return false;
        }

        @Override
        public String getValue(String property) {
            return null;
        }
    };

    private Local file;

    private License() {
        ;
    }

    /**
     * @param file
     */
    public License(Local file) {
        this.file = file;
    }

    /**
     * @return True if valid license key
     */
    public boolean verify() {
        if(!License.loadNative()) {
            return false;
        }
        final boolean valid = this.verify(file.getAbsolute());
        if(valid) {
            log.info("Valid donation key:" + file.getAbsolute());
        }
        else {
            log.warn("Not a valid donation key:" + file.getAbsolute());
        }
        return valid;
    }

    private native boolean verify(String license);

    /**
     * @return
     */
    public String getValue(String property) {
        if(!License.loadNative()) {
            return null;
        }
        return this.getValue(file.getAbsolute(), property);
    }

    private native String getValue(String license, String property);
}
