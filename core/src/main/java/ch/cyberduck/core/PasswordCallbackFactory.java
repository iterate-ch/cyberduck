package ch.cyberduck.core;

/*
 * Copyright (c) 2002-2016 iterate GmbH. All rights reserved.
 * https://cyberduck.io/
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
 */

import ch.cyberduck.core.preferences.PreferencesFactory;

import org.apache.commons.lang3.reflect.ConstructorUtils;
import org.apache.log4j.Logger;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;

public class PasswordCallbackFactory extends Factory<PasswordCallback> {
    private static final Logger log = Logger.getLogger(PasswordCallbackFactory.class);

    public PasswordCallback create(final Controller controller) {
        final String clazz = PreferencesFactory.get().getProperty("factory.passwordcallback.class");
        if(null == clazz) {
            throw new FactoryException(String.format("No implementation given for factory %s", this.getClass().getSimpleName()));
        }
        try {
            final Class<PasswordCallback> name = (Class<PasswordCallback>) Class.forName(clazz);
            final Constructor<PasswordCallback> constructor = ConstructorUtils.getMatchingAccessibleConstructor(name, controller.getClass());
            if(null == constructor) {
                log.warn(String.format("No matching constructor for parameter %s", controller.getClass()));
                // Call default constructor for disabled implementations
                return name.newInstance();
            }
            return constructor.newInstance(controller);
        }
        catch(InstantiationException | InvocationTargetException | ClassNotFoundException | IllegalAccessException e) {
            log.error(String.format("Failure loading callback class %s. %s", clazz, e.getMessage()));
            return new DisabledPasswordCallback();
        }
    }

    /**
     * @param c Window controller
     * @return Login controller instance for the current platform.
     */
    public static PasswordCallback get(final Controller c) {
        return new PasswordCallbackFactory().create(c);
    }
}
