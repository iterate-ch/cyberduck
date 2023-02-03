package ch.cyberduck.core.proxy;

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

public interface ProxyFinder {
    /**
     * Find proxy for a given target host
     *
     * @param target Target host containing scheme, e.g. https://mytarget.com
     * @return Proxy to use
     */
    Proxy find(String target);

    interface Configuration {
        /**
         * Allow user to configure system proxy settings
         */
        void configure();
    }
}
