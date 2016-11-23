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

import ch.cyberduck.core.pool.SessionPool;
import ch.cyberduck.core.preferences.PreferencesFactory;
import ch.cyberduck.core.transfer.DisabledTransferPrompt;
import ch.cyberduck.core.transfer.Transfer;
import ch.cyberduck.core.transfer.TransferPrompt;

import org.apache.commons.lang3.reflect.ConstructorUtils;
import org.apache.log4j.Logger;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;

public class TransferPromptControllerFactory extends Factory<TransferPrompt> {
    private static final Logger log = Logger.getLogger(TransferPromptControllerFactory.class);

    public TransferPrompt create(final Controller c, final Transfer transfer, final SessionPool session) {
        final String clazz = PreferencesFactory.get().getProperty(
                String.format("factory.transferpromptcallback.%s.class", transfer.getType().name()));
        if(null == clazz) {
            throw new FactoryException(String.format("No implementation given for factory %s", this.getClass().getSimpleName()));
        }
        try {
            final Class<TransferPrompt> name = (Class<TransferPrompt>) Class.forName(clazz);
            final Constructor<TransferPrompt> constructor = ConstructorUtils
                    .getMatchingAccessibleConstructor(name, c.getClass(), transfer.getClass(), session.getClass());
            if(null == constructor) {
                log.warn(String.format("No matching constructor for parameter %s", c.getClass()));
                // Call default constructor for disabled implementations
                return name.newInstance();
            }
            return constructor.newInstance(c, transfer, session);
        }
        catch(InstantiationException | InvocationTargetException | ClassNotFoundException | IllegalAccessException e) {
            log.error(String.format("Failure loading callback class %s. %s", clazz, e.getMessage()));
            return new DisabledTransferPrompt();
        }
    }

    /**
     * @param c Window controller
     * @return Login controller instance for the current platform.
     */
    public static TransferPrompt get(final Controller c, final Transfer transfer, final SessionPool session) {
        return new TransferPromptControllerFactory().create(c, transfer, session);
    }
}
