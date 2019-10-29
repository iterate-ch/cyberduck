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

import ch.cyberduck.core.local.Application;

import java.util.List;

public interface SchemeHandler {

    /**
     * Register this bundle identifier as the default application for all schemes
     *
     * @param application The bundle identifier of the application
     * @param schemes     The protocol identifier
     */
    void setDefaultHandler(Application application, List<String> schemes);

    /**
     * @param scheme URI scheme
     * @return Null if no handler is set
     */
    Application getDefaultHandler(String scheme);

    /**
     * @param scheme URI scheme
     * @return True if current application is configured as protocol handler
     */
    boolean isDefaultHandler(List<String> scheme, Application application);

    /**
     * @param scheme URI schemes
     * @return True if current application is configured as protocol handler for all schemes
     */
    List<Application> getAllHandlers(String scheme);
}
