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

import org.apache.commons.lang3.reflect.ConstructorUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;

public class LoginCallbackFactory extends Factory<LoginCallback> {
    private static final Logger log = LogManager.getLogger(LoginCallbackFactory.class);

    LoginCallbackFactory(final Class<? extends LoginCallback> clazz) {
        super(clazz);
    }

    private LoginCallbackFactory() {
        super("factory.logincallback.class");
    }

    public LoginCallback create(final Controller controller) {
        try {
            final Constructor<? extends LoginCallback> constructor
                    = ConstructorUtils.getMatchingAccessibleConstructor(clazz, controller.getClass());
            if(null == constructor) {
                log.warn("No matching constructor for parameter {}", controller.getClass());
                // Call default constructor for disabled implementations
                return clazz.getDeclaredConstructor().newInstance();
            }
            return constructor.newInstance(controller);
        }
        catch(InstantiationException | InvocationTargetException | IllegalAccessException | NoSuchMethodException e) {
            log.error("Failure loading callback class {}. {}", clazz, e.getMessage());
            return new DisabledLoginCallback();
        }
    }

    private static LoginCallbackFactory singleton;

    /**
     * @param c Window controller
     * @return Login controller instance for the current platform.
     */
    public static synchronized LoginCallback get(final Controller c) {
        if(null == singleton) {
            singleton = new LoginCallbackFactory();
        }
        return singleton.create(c);
    }
}
