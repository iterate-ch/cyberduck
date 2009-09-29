package ch.cyberduck.ui.cocoa.quicklook;

/*
 * Copyright (c) 2002-2009 David Kocher. All rights reserved.
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

import ch.cyberduck.core.Factory;

import java.util.HashMap;
import java.util.Map;

/**
 * @version $Id$
 */
public abstract class QuickLookFactory extends Factory<QuickLookInterface> {

    /**
     * Registered factories
     */
    protected static final Map<Platform, QuickLookFactory> factories = new HashMap<Platform, QuickLookFactory>();

    /**
     * @param platform
     * @param f
     */
    public static void addFactory(Platform platform, QuickLookFactory f) {
        factories.put(platform, f);
    }

    private static QuickLookInterface instance;

    public static QuickLookInterface instance() {
        if(null == instance) {
            if(factories.containsKey(VERSION_PLATFORM)) {
                instance = factories.get(VERSION_PLATFORM).create();
            }
            else {
                instance = new AbstractQuickLook() {
                    public boolean isAvailable() {
                        return false;
                    }

                    public boolean isOpen() {
                        return false;
                    }

                    public void open() {
                        throw new UnsupportedOperationException();
                    }

                    public void close() {
                        throw new UnsupportedOperationException();
                    }
                };
            }
        }
        return instance;
    }

    @Override
    protected abstract QuickLookInterface create();
}
