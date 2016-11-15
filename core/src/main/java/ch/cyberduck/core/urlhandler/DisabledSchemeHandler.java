package ch.cyberduck.core.urlhandler;

/*
 * Copyright (c) 2002-2014 David Kocher. All rights reserved.
 * http://cyberduck.io/
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
 * feedback@cyberduck.io
 */

import ch.cyberduck.core.Scheme;
import ch.cyberduck.core.local.Application;

import java.util.Collections;
import java.util.List;

public final class DisabledSchemeHandler implements SchemeHandler {
    @Override
    public void setDefaultHandler(final List<Scheme> scheme, final Application application) {
        //
    }

    @Override
    public Application getDefaultHandler(final Scheme scheme) {
        return Application.notfound;
    }

    @Override
    public boolean isDefaultHandler(final List<Scheme> scheme, final Application application) {
        return false;
    }

    @Override
    public List<Application> getAllHandlers(final Scheme scheme) {
        return Collections.emptyList();
    }
}
