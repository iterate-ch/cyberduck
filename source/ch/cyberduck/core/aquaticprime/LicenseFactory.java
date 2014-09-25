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
import ch.cyberduck.core.FactoryException;
import ch.cyberduck.core.Filter;
import ch.cyberduck.core.Local;
import ch.cyberduck.core.LocalFactory;
import ch.cyberduck.core.LocaleFactory;
import ch.cyberduck.core.Preferences;
import ch.cyberduck.core.exception.AccessDeniedException;

import org.apache.commons.io.FilenameUtils;
import org.apache.log4j.Logger;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @version $Id$
 */
public abstract class LicenseFactory extends Factory<License> {
    private static final Logger log = Logger.getLogger(LicenseFactory.class);

    /**
     * Delegate returning the first key found.
     */
    public static final class DefaultLicenseFactory extends Factory<License> {
        private LicenseFactory delegate;

        public DefaultLicenseFactory(final LicenseFactory delegate) {
            this.delegate = delegate;
        }

        @Override
        public License create() {
            try {
                final List<License> list = delegate.open();
                if(list.isEmpty()) {
                    return LicenseFactory.EMPTY_LICENSE;
                }
                return list.iterator().next();
            }
            catch(AccessDeniedException e) {
                log.error(String.format("Failure finding receipt %s", e.getMessage()));
            }
            return LicenseFactory.EMPTY_LICENSE;
        }
    }

    /**
     * Registered factories
     */
    private static final Map<Platform, LicenseFactory> factories
            = new HashMap<Platform, LicenseFactory>();

    public static void addFactory(Factory.Platform platform, LicenseFactory f) {
        factories.put(platform, f);
    }

    protected Local folder;

    private String extension;

    protected LicenseFactory() {
        this(LocalFactory.createLocal(Preferences.instance().getProperty("application.support.path")));
    }

    protected LicenseFactory(final Local folder) {
        this(folder, "cyberducklicense");
    }

    protected LicenseFactory(final Local folder, final String extension) {
        this.folder = folder;
        this.extension = extension;
    }

    /**
     * @param file File to parse
     * @return License possibly not yet verified depending on the implementation
     */
    protected abstract License open(Local file);

    public List<License> open() throws AccessDeniedException {
        final List<License> keys = new ArrayList<License>();
        if(folder.exists()) {
            for(Local key : folder.list().filter(new Filter<Local>() {
                @Override
                public boolean accept(final Local file) {
                    return extension.equals(FilenameUtils.getExtension(file.getName()));
                }
            })) {
                keys.add(this.open(key));
            }
        }
        return keys;
    }

    /**
     * @param file File to parse
     * @return Read license from file
     */
    public static License create(final Local file) {
        if(!factories.containsKey(NATIVE_PLATFORM)) {
            throw new FactoryException(String.format("No implementation for %s", NATIVE_PLATFORM));
        }
        return factories.get(NATIVE_PLATFORM).open(file);
    }

    /**
     * @return If no license is installed a dummy license is returned.
     * @see #EMPTY_LICENSE
     */
    public static License find() {
        if(!factories.containsKey(NATIVE_PLATFORM)) {
            throw new FactoryException(String.format("No implementation for %s", NATIVE_PLATFORM));
        }
        try {
            final List<License> list = factories.get(NATIVE_PLATFORM).open();
            if(list.isEmpty()) {
                return LicenseFactory.EMPTY_LICENSE;
            }
            return list.iterator().next();
        }
        catch(AccessDeniedException e) {
            log.error(String.format("Failure finding receipt %s", e.getMessage()));
        }
        return LicenseFactory.EMPTY_LICENSE;
    }

    public static final License EMPTY_LICENSE = new License() {
        @Override
        public boolean verify() {
            return false;
        }

        @Override
        public String getValue(String property) {
            return null;
        }

        @Override
        public String getName() {
            return LocaleFactory.localizedString("Not a valid donation key", "License");
        }

        @Override
        public boolean isReceipt() {
            return false;
        }

        @Override
        public boolean equals(Object obj) {
            if(obj instanceof License) {
                return EMPTY_LICENSE == obj;
            }
            return false;
        }

        @Override
        public int hashCode() {
            return this.toString().hashCode();
        }

        @Override
        public String toString() {
            return LocaleFactory.localizedString("Not a valid donation key", "License");
        }
    };
}
