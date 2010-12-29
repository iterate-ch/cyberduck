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
public class Donation extends AbstractLicense {
    private static Logger log = Logger.getLogger(Donation.class);

    public static void register() {
        LicenseFactory.addFactory(Factory.NATIVE_PLATFORM, new Factory());
    }

    private static class Factory extends LicenseFactory {
        @Override
        protected License open(final Local file) {
            if(file.getName().endsWith(".cyberducklicense")) {
                return new Donation(file);
            }
            return LicenseFactory.EMPTY_LICENSE;
        }

        @Override
        protected License create() {
            Local support = LocalFactory.createLocal(Preferences.instance().getProperty("application.support.path"));
            if(support.exists()) {
                final Collection<File> keys = FileUtils.listFiles(
                        new File(support.getAbsolute()),
                        new SuffixFileFilter(".cyberducklicense"), FalseFileFilter.FALSE);
                for(File key : keys) {
                    return open(LocalFactory.createLocal(key));
                }
            }
            log.info("No donation key found");
            return LicenseFactory.EMPTY_LICENSE;
        }
    }

    private static boolean JNI_LOADED = false;

    private static boolean loadNative() {
        if(!JNI_LOADED) {
            JNI_LOADED = Native.load("Prime");
        }
        return JNI_LOADED;
    }

    /**
     * @param file The license key file.
     */
    public Donation(Local file) {
        super(file);
    }

    /**
     * @return True if valid license key
     */
    public boolean verify() {
        if(!Donation.loadNative()) {
            return false;
        }
        final boolean valid = this.verify(this.getFile().getAbsolute());
        if(valid) {
            if(log.isInfoEnabled()) {
                log.info("Valid donation key:" + this.getFile().getAbsolute());
            }
        }
        else {
            log.warn("Not a valid donation key:" + this.getFile().getAbsolute());
        }
        return valid;
    }

    private native boolean verify(String license);

    public String getValue(String property) {
        if(!Donation.loadNative()) {
            return null;
        }
        return this.getValue(this.getFile().getAbsolute(), property);
    }

    /**
     * @param license
     * @param property
     * @return
     */
    private native String getValue(String license, String property);
}
