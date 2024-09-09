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
import ch.cyberduck.core.StringAppender;
import ch.cyberduck.core.exception.AccessDeniedException;
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
    public static final class DefaultLicenseFactory extends Factory<License> {
        private final LicenseFactory delegate;

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

    protected final Local folder;

    private final Filter<Local> filter;

    protected LicenseFactory() {
        this(SupportDirectoryFinderFactory.get().find());
    }

    protected LicenseFactory(final Local folder) {
        this(folder, new LicenseFilter());
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
        final List<License> keys = new ArrayList<>();
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
                final List<License> list = new ArrayList<>(name.getDeclaredConstructor().newInstance().open());
                list.removeIf(key -> !key.verify(callback));
                if(list.isEmpty()) {
                    return LicenseFactory.EMPTY_LICENSE;
                }
                return list.iterator().next();
            }
            catch(InstantiationException | ClassNotFoundException | IllegalAccessException | NoSuchMethodException |
                  InvocationTargetException e) {
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
        public boolean verify(final LicenseVerifierCallback callback) {
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
            final StringAppender message = new StringAppender();
            message.append(LocaleFactory.localizedString("This is free software, but it still costs money to write, support, and distribute it. If you enjoy using it, please consider a donation to the authors of this software. It will help to make Cyberduck even better!", "Donate"));
            message.append(LocaleFactory.localizedString("As a contributor to Cyberduck, you receive a registration key that disables this prompt.", "Donate"));
            return message.toString();
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
