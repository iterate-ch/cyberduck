package ch.cyberduck.core.threading;

/*
 * Copyright (c) 2002-2013 David Kocher. All rights reserved.
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
 * feedback@cyberduck.ch
 */

import ch.cyberduck.core.Factory;

import org.apache.commons.lang3.reflect.ConstructorUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;

public class ActionOperationBatcherFactory extends Factory<ActionOperationBatcher> {
    private static final Logger log = LogManager.getLogger(ActionOperationBatcherFactory.class);

    public ActionOperationBatcherFactory() {
        super("factory.autorelease.class");
    }

    public ActionOperationBatcher create(final Integer batchsize) {
        try {
            final Constructor<? extends ActionOperationBatcher> constructor = ConstructorUtils.getMatchingAccessibleConstructor(clazz, batchsize.getClass());
            if(null == constructor) {
                log.warn(String.format("No matching constructor for parameter %s", batchsize.getClass()));
                // Call default constructor for disabled implementations
                return clazz.getDeclaredConstructor().newInstance();
            }
            return constructor.newInstance(batchsize);
        }
        catch(InstantiationException | InvocationTargetException | IllegalAccessException | NoSuchMethodException e) {
            log.error(String.format("Failure loading callback class %s. %s", clazz, e.getMessage()));
            return new DisabledActionOperationBatcher();
        }
    }

    public static ActionOperationBatcher get() {
        return get(1);
    }

    public static ActionOperationBatcher get(final Integer batchsize) {
        return new ActionOperationBatcherFactory().create(batchsize);
    }
}
