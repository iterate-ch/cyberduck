package ch.cyberduck.core;

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
 * Bug fixes, suggestions and comments should be sent to feedback@cyberduck.ch
 */

import ch.cyberduck.core.date.AbstractUserDateFormatter;

import org.apache.commons.lang3.reflect.ConstructorUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.TimeZone;

public class UserDateFormatterFactory extends Factory<AbstractUserDateFormatter> {
    private static final Logger log = LogManager.getLogger(UserDateFormatterFactory.class);

    private Constructor<? extends AbstractUserDateFormatter> constructor;

    private UserDateFormatterFactory() {
        super("factory.dateformatter.class");
    }

    public AbstractUserDateFormatter create(final String timezone) {
        try {
            if(null == constructor) {
                constructor = ConstructorUtils.getMatchingAccessibleConstructor(clazz, timezone.getClass());
            }
            if(null == constructor) {
                log.warn(String.format("No matching constructor for parameter %s", timezone.getClass()));
                // Call default constructor for disabled implementations
                return clazz.getDeclaredConstructor().newInstance();
            }
            return constructor.newInstance(timezone);
        }
        catch(InstantiationException | InvocationTargetException | IllegalAccessException | NoSuchMethodException e) {
            throw new FactoryException(e.getMessage(), e);
        }
    }

    private static final UserDateFormatterFactory singleton = new UserDateFormatterFactory();

    public static AbstractUserDateFormatter get() {
        return get(TimeZone.getDefault().getID());
    }

    public static AbstractUserDateFormatter get(final String tz) {
        return singleton.create(tz);
    }
}
