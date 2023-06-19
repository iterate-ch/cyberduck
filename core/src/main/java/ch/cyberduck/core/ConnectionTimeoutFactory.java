package ch.cyberduck.core;

/*
 * Copyright (c) 2002-2022 iterate GmbH. All rights reserved.
 * https://cyberduck.io/
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 */


import ch.cyberduck.core.preferences.HostPreferences;
import ch.cyberduck.core.preferences.PreferencesFactory;
import ch.cyberduck.core.preferences.PreferencesReader;

import org.apache.commons.lang3.reflect.ConstructorUtils;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;

public class ConnectionTimeoutFactory extends Factory<ConnectionTimeout> {

    private Constructor<? extends ConnectionTimeout> constructor;

    private ConnectionTimeoutFactory() {
        super("factory.connectiontimeout.class");
    }

    public ConnectionTimeout create(final PreferencesReader preferences) throws FactoryException {
        try {
            if(null == constructor) {
                constructor = ConstructorUtils.getMatchingAccessibleConstructor(clazz, PreferencesReader.class);
            }
            return constructor.newInstance(preferences);
        }
        catch(InstantiationException | InvocationTargetException | IllegalAccessException e) {
            throw new FactoryException(e.getMessage(), e);
        }
    }

    private static final ConnectionTimeoutFactory singleton = new ConnectionTimeoutFactory();

    public static ConnectionTimeout get() {
        return get(PreferencesFactory.get());
    }

    public static ConnectionTimeout get(final Host host) {
        return get(new HostPreferences(host));
    }

    public static ConnectionTimeout get(final PreferencesReader preferences) {
        return singleton.create(preferences);
    }
}
