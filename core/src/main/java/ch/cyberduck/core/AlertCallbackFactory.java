package ch.cyberduck.core;

/*
 * Copyright (c) 2002-2017 iterate GmbH. All rights reserved.
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

import ch.cyberduck.core.threading.AlertCallback;
import ch.cyberduck.core.threading.DisabledAlertCallback;

import org.apache.commons.lang3.reflect.ConstructorUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;

public class AlertCallbackFactory extends Factory<AlertCallback> {
    private static final Logger log = LogManager.getLogger(AlertCallbackFactory.class);

    private Constructor<? extends AlertCallback> constructor;

    protected AlertCallbackFactory() {
        super("factory.alertcallback.class");
    }

    public AlertCallback create(final Controller controller) {
        try {
            if(null == constructor) {
                constructor = ConstructorUtils.getMatchingAccessibleConstructor(clazz, controller.getClass());
            }
            if(null == constructor) {
                log.warn(String.format("No matching constructor for parameter %s", controller.getClass()));
                // Call default constructor for disabled implementations
                return clazz.getDeclaredConstructor().newInstance();
            }
            return constructor.newInstance(controller);
        }
        catch(InstantiationException | InvocationTargetException | IllegalAccessException | NoSuchMethodException e) {
            log.error(String.format("Failure loading callback class %s. %s", clazz, e.getMessage()));
            return new DisabledAlertCallback();
        }
    }

    /**
     * @param c Window controller
     * @return Login controller instance for the current platform.
     */
    public static AlertCallback get(final Controller c) {
        return new AlertCallbackFactory().create(c);
    }
}
