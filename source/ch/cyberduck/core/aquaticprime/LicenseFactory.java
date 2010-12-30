package ch.cyberduck.core.aquaticprime;

/*
 * Copyright (c) 2002-2010 David Kocher. All rights reserved.
 *
 * http://cyberduck.ch/
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * Bug fixes, suggestions and comments should be sent to:
 * dkocher@cyberduck.ch
 */

import ch.cyberduck.core.Factory;
import ch.cyberduck.core.Local;
import ch.cyberduck.core.LocalFactory;
import ch.cyberduck.core.Preferences;
import ch.cyberduck.core.i18n.Locale;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.filefilter.FalseFileFilter;
import org.apache.commons.io.filefilter.SuffixFileFilter;
import org.apache.log4j.Logger;

import java.io.File;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

/**
 * @version $Id$
 */
public abstract class LicenseFactory extends Factory<License> {
    private static Logger log = Logger.getLogger(LicenseFactory.class);

    /**
     * Registered factories
     */
    protected static final Map<Platform, LicenseFactory> factories
            = new HashMap<Platform, LicenseFactory>();

    /**
     * @param platform
     * @param f
     */
    public static void addFactory(Factory.Platform platform, LicenseFactory f) {
        factories.put(platform, f);
    }

    /**
     * @param file
     * @return
     */
    protected abstract License open(Local file);

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

    /**
     * @return
     */
    public static License create(Local file) {
        if(!factories.containsKey(NATIVE_PLATFORM)) {
            throw new RuntimeException("No implementation for " + NATIVE_PLATFORM);
        }
        return factories.get(NATIVE_PLATFORM).open(file);
    }

    /**
     * @return If no license is installed a dummy license is returned.
     * @see #EMPTY_LICENSE
     */
    public static License find() {
        if(!factories.containsKey(NATIVE_PLATFORM)) {
            throw new RuntimeException("No implementation for " + NATIVE_PLATFORM);
        }
        return factories.get(NATIVE_PLATFORM).create();
    }

    public static final License EMPTY_LICENSE = new License() {
        public boolean verify() {
            return false;
        }

        public String getValue(String property) {
            return null;
        }

        public String getName() {
            return Locale.localizedString("Not a valid donation key", "License");
        }

        public boolean isReceipt() {
            return false;
        }

        @Override
        public boolean equals(Object obj) {
            return EMPTY_LICENSE == obj;
        }

        @Override
        public String toString() {
            return Locale.localizedString("Not a valid donation key", "License");
        }
    };
}
