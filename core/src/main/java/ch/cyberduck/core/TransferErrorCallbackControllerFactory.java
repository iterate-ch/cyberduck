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

import ch.cyberduck.core.preferences.Preferences;
import ch.cyberduck.core.preferences.PreferencesFactory;
import ch.cyberduck.core.transfer.DisabledTransferErrorCallback;
import ch.cyberduck.core.transfer.TransferErrorCallback;

import org.apache.commons.lang3.reflect.ConstructorUtils;
import org.apache.log4j.Logger;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;

public class TransferErrorCallbackControllerFactory extends Factory<TransferErrorCallback> {
    private static final Logger log = Logger.getLogger(TransferErrorCallbackControllerFactory.class);

    private static final Preferences preferences
            = PreferencesFactory.get();

    public TransferErrorCallback create(final Controller c) {
        final String clazz = preferences.getProperty("factory.transfererrorcallback.class");
        if(null == clazz) {
            throw new FactoryException(String.format("No implementation given for factory %s", this.getClass().getSimpleName()));
        }
        try {
            final Class<TransferErrorCallback> name = (Class<TransferErrorCallback>) Class.forName(clazz);
            final Constructor<TransferErrorCallback> constructor = ConstructorUtils.getMatchingAccessibleConstructor(name, c.getClass());
            if(null == constructor) {
                log.warn(String.format("No matching constructor for parameter %s", c.getClass()));
                // Call default constructor for disabled implementations
                return name.newInstance();
            }
            return constructor.newInstance(c);
        }
        catch(InstantiationException | InvocationTargetException | ClassNotFoundException | IllegalAccessException e) {
            log.error(String.format("Failure loading callback class %s. %s", clazz, e.getMessage()));
            return new DisabledTransferErrorCallback();
        }
    }

    /**
     * @param c Window controller
     * @return Login controller instance for the current platform.
     */
    public static TransferErrorCallback get(final Controller c) {
        return new TransferErrorCallbackControllerFactory().create(c);
    }
}
