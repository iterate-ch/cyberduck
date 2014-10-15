package ch.cyberduck.ui;

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

import ch.cyberduck.core.Factory;
import ch.cyberduck.core.transfer.DisabledTransferErrorCallback;
import ch.cyberduck.core.transfer.TransferErrorCallback;

import java.util.HashMap;
import java.util.Map;

/**
 * @version $Id$
 */
public abstract class TransferErrorCallbackControllerFactory extends Factory<TransferErrorCallback> {

    public abstract TransferErrorCallback create(Controller c);

    /**
     * Registered factories
     */
    private static final Map<Platform, TransferErrorCallbackControllerFactory> factories
            = new HashMap<Platform, TransferErrorCallbackControllerFactory>();

    /**
     * @param c Window controller
     * @return Login controller instance for the current platform.
     */
    public static TransferErrorCallback get(final Controller c) {
        if(!factories.containsKey(NATIVE_PLATFORM)) {
            return new DisabledTransferErrorCallback();
        }
        return factories.get(NATIVE_PLATFORM).create(c);
    }

    public static void addFactory(Platform p, TransferErrorCallbackControllerFactory f) {
        factories.put(p, f);
    }
}
