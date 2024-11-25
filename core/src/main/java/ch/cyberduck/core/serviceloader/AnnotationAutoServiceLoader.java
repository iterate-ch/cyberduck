package ch.cyberduck.core.serviceloader;

/*
 * Copyright (c) 2002-2024 iterate GmbH. All rights reserved.
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

import java.util.HashSet;
import java.util.ServiceLoader;
import java.util.Set;

public class AnnotationAutoServiceLoader<T> implements AutoServiceLoader<T> {

    @Override
    public Set<T> load(final Class<T> type) {
        final ServiceLoader<T> loader = ServiceLoader.load(type);
        final Set<T> services = new HashSet<>();
        for(T t : loader) {
            services.add(t);
        }
        return services;
    }
}
