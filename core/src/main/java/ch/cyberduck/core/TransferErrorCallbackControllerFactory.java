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

import ch.cyberduck.core.transfer.CancelTransferErrorCallback;
import ch.cyberduck.core.transfer.DisabledTransferErrorCallback;
import ch.cyberduck.core.transfer.FailFastTransferErrorCallback;
import ch.cyberduck.core.transfer.SynchronizedTransferErrorCallback;
import ch.cyberduck.core.transfer.TransferErrorCallback;

import org.apache.commons.lang3.reflect.ConstructorUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;

public class TransferErrorCallbackControllerFactory extends Factory<TransferErrorCallback> {
    private static final Logger log = LogManager.getLogger(TransferErrorCallbackControllerFactory.class);

    private Constructor<? extends TransferErrorCallback> constructor;

    private TransferErrorCallbackControllerFactory() {
        super("factory.transfererrorcallback.class");
    }

    public TransferErrorCallback create(final Controller c) {
        try {
            if(null == constructor) {
                constructor = ConstructorUtils.getMatchingAccessibleConstructor(clazz, c.getClass());
            }
            if(null == constructor) {
                log.warn("No matching constructor for parameter {}", c.getClass());
                // Call default constructor for disabled implementations
                return clazz.getDeclaredConstructor().newInstance();
            }
            return constructor.newInstance(c);
        }
        catch(InstantiationException | InvocationTargetException | IllegalAccessException | NoSuchMethodException e) {
            log.error("Failure loading callback class {}. {}", clazz, e.getMessage());
            return new DisabledTransferErrorCallback();
        }
    }

    private static TransferErrorCallbackControllerFactory singleton;

    /**
     * @param c Window controller
     * @return Login controller instance for the current platform.
     */
    public static synchronized TransferErrorCallback get(final Controller c) {
        if(null == singleton) {
            singleton = new TransferErrorCallbackControllerFactory();
        }
        return new SynchronizedTransferErrorCallback(new CancelTransferErrorCallback(new FailFastTransferErrorCallback(singleton.create(c))));
    }
}
