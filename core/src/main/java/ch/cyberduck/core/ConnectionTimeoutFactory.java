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
import ch.cyberduck.core.preferences.PreferencesReader;

import org.apache.commons.lang3.reflect.ConstructorUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;

public class ConnectionTimeoutFactory extends Factory<ConnectionTimeout> {
    private static final Logger log = LogManager.getLogger(ConnectionTimeoutFactory.class);

    private static final ConnectionTimeoutFactory factory = new ConnectionTimeoutFactory();
    private final ConnectionTimeout instance;

    private final Constructor<? extends ConnectionTimeout> constructor;

    private ConnectionTimeoutFactory() {
        super("factory.connectiontimeout.class");
        instance = create();

        constructor = ConstructorUtils.getMatchingAccessibleConstructor(clazz, PreferencesReader.class);
    }

    public ConnectionTimeout create(final PreferencesReader preferences) {
        ConnectionTimeout temp = instance;
        if(constructor != null) {
            try {
                temp = constructor.newInstance(preferences);
            }
            catch(InstantiationException | InvocationTargetException | IllegalAccessException e) {
                log.error(String.format("Failure loading host connection timeout class %s. %s", clazz, e.getMessage()));
            }
        }
        return temp;
    }

    public static ConnectionTimeout get() {
        return factory.instance;
    }

    public static ConnectionTimeout get(final Host host) {
        return get(new HostPreferences(host));
    }

    public static ConnectionTimeout get(final PreferencesReader preferences) {
        return factory.create(preferences);
    }
}
