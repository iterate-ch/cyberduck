package ch.cyberduck.core;

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

import ch.cyberduck.core.preferences.PreferencesFactory;

import org.apache.commons.lang3.reflect.ConstructorUtils;
import org.apache.log4j.Logger;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;

public class LoginCallbackFactory extends Factory<LoginCallback> {
    private static final Logger log = Logger.getLogger(LoginCallbackFactory.class);

    protected LoginCallbackFactory() {
        super("factory.logincallback.class");
    }

    public LoginCallback create(final Controller controller) {
        final String clazz = PreferencesFactory.get().getProperty("factory.logincallback.class");
        if(null == clazz) {
            throw new FactoryException(String.format("No implementation given for factory %s", this.getClass().getSimpleName()));
        }
        try {
            final Class<LoginCallback> name = (Class<LoginCallback>) Class.forName(clazz);
            final Constructor<LoginCallback> constructor = ConstructorUtils.getMatchingAccessibleConstructor(name, controller.getClass());
            if(null == constructor) {
                log.warn(String.format("No matching constructor for parameter %s", controller.getClass()));
                // Call default constructor for disabled implementations
                return name.newInstance();
            }
            return constructor.newInstance(controller);
        }
        catch(InstantiationException | InvocationTargetException | ClassNotFoundException | IllegalAccessException e) {
            log.error(String.format("Failure loading callback class %s. %s", clazz, e.getMessage()));
            return new DisabledLoginCallback();
        }
    }

    /**
     * @param c Window controller
     * @return Login controller instance for the current platform.
     */
    public static LoginCallback get(final Controller c) {
        return new LoginCallbackFactory().create(c);
    }
}
