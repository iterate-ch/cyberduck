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

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.filefilter.FalseFileFilter;
import org.apache.commons.io.filefilter.SuffixFileFilter;
import org.apache.log4j.Logger;

import java.io.File;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

/**
 * @version $Id:$
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
     * @throws UnsupportedOperationException
     * @see #create(ch.cyberduck.core.Local)
     */
    @Override
    protected License create() {
        throw new UnsupportedOperationException();
    }

    /**
     * @param file
     * @return
     */
    protected abstract License open(Local file);

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
        final Collection<File> licenses = FileUtils.listFiles(
                new File(LocalFactory.createLocal(Preferences.instance().getProperty("application.support.path")).getAbsolute()),
                new SuffixFileFilter(".cyberducklicense"), FalseFileFilter.FALSE);
        for(File license : licenses) {
            return LicenseFactory.create(LocalFactory.createLocal(license));
        }
        log.info("No license found");
        return EMPTY_LICENSE;
    }

    private static final License EMPTY_LICENSE = new License() {
        public boolean verify() {
            return false;
        }

        public String getValue(String property) {
            return null;
        }
    };
}
