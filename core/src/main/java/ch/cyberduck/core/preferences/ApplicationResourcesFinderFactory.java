package ch.cyberduck.core.preferences;

/*
 * Copyright (c) 2002-2015 David Kocher. All rights reserved.
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

import ch.cyberduck.core.Factory;

public class ApplicationResourcesFinderFactory extends Factory<ApplicationResourcesFinder> {

    private ApplicationResourcesFinderFactory() {
        super("factory.applicationresourcesfinder.class");
    }

    public static ApplicationResourcesFinder get() {
        return new ApplicationResourcesFinderFactory().create();
    }
}
