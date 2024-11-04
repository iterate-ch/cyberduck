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
import ch.cyberduck.core.LocaleFactory;
import ch.cyberduck.core.Path;
import ch.cyberduck.core.exception.AccessDeniedException;
import ch.cyberduck.core.preferences.ApplicationResourcesFinderFactory;
import ch.cyberduck.core.preferences.Preferences;
import ch.cyberduck.core.preferences.PreferencesFactory;
import ch.cyberduck.core.preferences.SupportDirectoryFinderFactory;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

public abstract class LicenseFactory extends Factory<License> {
    private static final Logger log = LogManager.getLogger(LicenseFactory.class);

    private static final Preferences preferences
            = PreferencesFactory.get();

    /**
     * Delegate returning the first key found.
     */
    @Override
    protected License create() {
        try {
            final List<License> list = this.open();
            if(list.isEmpty()) {
                return EMPTY_LICENSE;
            }
            return list.iterator().next();
        }
        catch(AccessDeniedException e) {
            log.warn("Failure finding receipt {}", e.getMessage());
        }
        return EMPTY_LICENSE;
    }

    protected final Local[] folders;

    private final Filter<Local> filter;

    protected LicenseFactory() {
        this(new Local[]{SupportDirectoryFinderFactory.get().find(), ApplicationResourcesFinderFactory.get().find()});
    }

    protected LicenseFactory(final Local[] folders) {
        this(folders, new LicenseFilter());
    }

    protected LicenseFactory(final Local[] folders, final Filter<Local> filter) {
        this.folders = folders;
        this.filter = filter;
    }

    /**
     * @param file File to parse
     * @return License possibly not yet verified depending on the implementation
     */
    protected abstract License open(Local file);

    public List<License> open() throws AccessDeniedException {
        final List<License> keys = new ArrayList<>();
        for(Local folder : folders) {
            if(folder.exists()) {
                for(Local key : folder.list().filter(filter)) {
                    keys.add(this.open(key));
                }
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
            return name.getDeclaredConstructor().newInstance().open(file);
        }
        catch(InstantiationException | ClassNotFoundException | IllegalAccessException | NoSuchMethodException |
              InvocationTargetException e) {
            throw new FactoryException(e.getMessage(), e);
        }
    }

    /**
     * @return If no license is installed a dummy license is returned.
     * @see #EMPTY_LICENSE
     */
    public static License find() {
        return find(new DisabledLicenseVerifierCallback());
    }

    public static License find(final LicenseVerifierCallback callback) {
        try {
            final String clazz = preferences.getProperty("factory.licensefactory.class");
            try {
                final Class<LicenseFactory> name = (Class<LicenseFactory>) Class.forName(clazz);
                final LicenseFactory factory = name.getDeclaredConstructor().newInstance();
                final List<License> list = new ArrayList<>(factory.open());
                list.removeIf(key -> !key.verify(callback));
                if(list.isEmpty()) {
                    return factory.unregistered();
                }
                return list.iterator().next();
            }
            catch(InstantiationException | ClassNotFoundException | IllegalAccessException | NoSuchMethodException |
                  InvocationTargetException e) {
                throw new FactoryException(e.getMessage(), e);
            }
        }
        catch(AccessDeniedException e) {
            log.warn("Failure finding receipt {}", e.getMessage());
        }
        return EMPTY_LICENSE;
    }

    protected License unregistered() {
        return EMPTY_LICENSE;
    }

    private static final License EMPTY_LICENSE = new License() {
        @Override
        public boolean verify(final LicenseVerifierCallback callback) {
            return false;
        }

        @Override
        public String getValue(String property) {
            return null;
        }

        @Override
        public String getEntitlement() {
            return LocaleFactory.localizedString("Not a valid registration key", "License");
        }

        @Override
        public boolean isReceipt() {
            return false;
        }
    };

    protected static final class LicenseFilter implements Filter<Local> {
        private final Pattern pattern = Pattern.compile(".*\\.cyberducklicense");

        @Override
        public boolean accept(final Local file) {
            return "cyberducklicense".equalsIgnoreCase(Path.getExtension(file.getName()));
        }

        @Override
        public Pattern toPattern() {
            return pattern;
        }
    }
}
