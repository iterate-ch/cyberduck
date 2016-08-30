package ch.cyberduck.core.urlhandler;

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

import ch.cyberduck.core.Scheme;
import ch.cyberduck.core.local.Application;

import java.util.List;

public interface SchemeHandler {

    /**
     * @param scheme      URL scheme
     * @param application Application to set as default handler
     */
    void setDefaultHandler(List<Scheme> scheme, Application application);

    /**
     * @param scheme URL scheme
     * @return Null if no handler is set
     */
    Application getDefaultHandler(Scheme scheme);

    /**
     * @param scheme URL scheme
     * @return True if current application is configured as protocol handler
     */
    boolean isDefaultHandler(List<Scheme> scheme, Application application);

    /**
     * @param scheme URL schemes
     * @return True if current application is configured as protocol handler for all schemes
     */
    List<Application> getAllHandlers(Scheme scheme);
}
