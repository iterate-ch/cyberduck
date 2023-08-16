package ch.cyberduck.core.updater;

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

import ch.cyberduck.core.Controller;
import ch.cyberduck.core.Factory;
import ch.cyberduck.core.SingleThreadController;

import org.apache.commons.lang3.reflect.ConstructorUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;

public class PeriodicUpdateCheckerFactory extends Factory<PeriodicUpdateChecker> {
    private static final Logger log = LogManager.getLogger(PeriodicUpdateCheckerFactory.class);

    private Constructor<? extends PeriodicUpdateChecker> constructor;

    private PeriodicUpdateCheckerFactory() {
        super("factory.updater.class");
    }

    public PeriodicUpdateChecker create(final Controller controller) {
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
            return new DisabledPeriodicUpdater();
        }
    }

    private static PeriodicUpdateCheckerFactory singleton;

    public static synchronized PeriodicUpdateChecker get() {
        return get(new SingleThreadController());
    }

    public static synchronized PeriodicUpdateChecker get(final Controller controller) {
        if(null == singleton) {
            singleton = new PeriodicUpdateCheckerFactory();
        }
        return singleton.create(controller);
    }
}
