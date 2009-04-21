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

import com.apple.cocoa.foundation.NSBundle;

import ch.cyberduck.core.Local;
import ch.cyberduck.core.Preferences;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.filefilter.FalseFileFilter;
import org.apache.commons.io.filefilter.SuffixFileFilter;
import org.apache.log4j.Logger;

import java.io.File;
import java.util.Collection;

/**
 * @version $Id:$
 */
public class License {
    private static Logger log = Logger.getLogger(License.class);

    private static boolean JNI_LOADED = false;

    private static final Object lock = new Object();

    private static boolean jni_load() {
        synchronized(lock) {
            if(!JNI_LOADED) {
                try {
                    NSBundle bundle = NSBundle.mainBundle();
                    String lib = bundle.resourcePath() + "/Java/" + "libPrime.dylib";
                    log.info("Locating libPrime.dylib at '" + lib + "'");
                    System.load(lib);
                    JNI_LOADED = true;
                    log.info("libPrime.dylib loaded");
                }
                catch(UnsatisfiedLinkError e) {
                    log.error("Could not load the libPrime.dylib library:" + e.getMessage());
                }
            }
            return JNI_LOADED;
        }
    }

    /**
     * @return Null if no license found
     */
    public static License find() {
        if(!License.jni_load()) {
            return null;
        }
        final Collection<File> licenses = FileUtils.listFiles(
                new File(new Local(Preferences.instance().getProperty("application.support.path")).getAbsolute()),
                new SuffixFileFilter(".cyberducklicense"), FalseFileFilter.FALSE);
        for(File license : licenses) {
            return new License(new Local(license));
        }
        log.warn("No license found");
        return null;
    }

    private Local file;

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
        if(!License.jni_load()) {
            return false;
        }
        if(!file.exists()) {
            log.info("License file not found:" + file.getAbsolute());
            return false;
        }
        final boolean valid = this.verify(file.getAbsolute());
        if(valid) {
            log.info("Valid license file:" + file.getAbsolute());
        }
        else {
            log.warn("Not a valid license file:" + file.getAbsolute());
        }
        return valid;
    }

    public native boolean verify(String license);
}
