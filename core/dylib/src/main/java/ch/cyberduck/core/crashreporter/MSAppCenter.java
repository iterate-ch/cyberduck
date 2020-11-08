package ch.cyberduck.core.crashreporter;

/*
 * Copyright (c) 2002-2020 iterate GmbH. All rights reserved.
 * https://cyberduck.io/
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 */

import ch.cyberduck.binding.foundation.NSArray;
import ch.cyberduck.binding.foundation.NSObject;

import org.rococoa.ObjCClass;
import org.rococoa.Rococoa;

public abstract class MSAppCenter extends NSObject {
    private static final _Class CLASS = Rococoa.createClass("MSAppCenter", _Class.class);

    public interface _Class extends ObjCClass {
        void start_withServices(String identifier, NSArray services);
    }

    public static void start_withServices(final String identifier, final NSArray services) {
        CLASS.start_withServices(identifier, services);
    }
}
