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
import ch.cyberduck.core.exception.AccessDeniedException;
import ch.cyberduck.core.preferences.Preferences;
import ch.cyberduck.core.preferences.PreferencesFactory;

import org.apache.commons.io.FilenameUtils;
import org.apache.log4j.Logger;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public abstract class LicenseFactory extends Factory<License> {
    private static final Logger log = Logger.getLogger(LicenseFactory.class);

    private static final Preferences preferences
            = PreferencesFactory.get();

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
                log.warn(String.format("Failure finding receipt %s", e.getMessage()));
            }
            return LicenseFactory.EMPTY_LICENSE;
        }
    }

    protected Local folder;

    private Filter<Local> filter;

    protected LicenseFactory() {
        this(LocalFactory.get(PreferencesFactory.get().getProperty("application.support.path")));
    }

    protected LicenseFactory(final Local folder) {
        this(folder, new Filter<Local>() {
            @Override
            public boolean accept(final Local file) {
                return "cyberducklicense".equals(FilenameUtils.getExtension(file.getName()));
            }
        });
    }

    protected LicenseFactory(final Local folder, final Filter<Local> filter) {
        this.folder = folder;
        this.filter = filter;
    }

    /**
     * @param file File to parse
     * @return License possibly not yet verified depending on the implementation
     */
    protected abstract License open(Local file);

    public List<License> open() throws AccessDeniedException {
        final List<License> keys = new ArrayList<License>();
        if(folder.exists()) {
            for(Local key : folder.list().filter(filter)) {
                keys.add(this.open(key));
            }
        }
        return keys;
    }

    /**
     * @param file File to parse
     * @return Read license from file
     */
    public static License get(final Local file) {
        final String clazz = preferences.getProperty("factory.licensefactory.class");
        if(null == clazz) {
            throw new FactoryException();
        }
        try {
            final Class<LicenseFactory> name = (Class<LicenseFactory>) Class.forName(clazz);
            return name.newInstance().open(file);
        }
        catch(InstantiationException | ClassNotFoundException | IllegalAccessException e) {
            throw new FactoryException(e.getMessage(), e);
        }
    }

    /**
     * @return If no license is installed a dummy license is returned.
     * @see #EMPTY_LICENSE
     */
    public static License find() {
        try {
            final String clazz = preferences.getProperty("factory.licensefactory.class");
            if(null == clazz) {
                return LicenseFactory.EMPTY_LICENSE;
            }
            try {
                final Class<LicenseFactory> name = (Class<LicenseFactory>) Class.forName(clazz);
                final List<License> list = new ArrayList<License>(name.newInstance().open());
                for(Iterator<License> iter = list.iterator(); iter.hasNext(); ) {
                    final License key = iter.next();
                    if(!key.verify()) {
                        iter.remove();
                    }
                }
                if(list.isEmpty()) {
                    return LicenseFactory.EMPTY_LICENSE;
                }
                return list.iterator().next();
            }
            catch(InstantiationException | ClassNotFoundException | IllegalAccessException e) {
                throw new FactoryException(e.getMessage(), e);
            }
        }
        catch(AccessDeniedException e) {
            log.warn(String.format("Failure finding receipt %s", e.getMessage()));
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
            return LocaleFactory.localizedString("Not a valid registration key", "License");
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
            return LocaleFactory.localizedString("Not a valid registration key", "License");
        }
    };
}
