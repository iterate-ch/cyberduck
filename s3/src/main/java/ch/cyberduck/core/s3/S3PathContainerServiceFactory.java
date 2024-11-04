package ch.cyberduck.core.s3;

/*
 * Copyright (c) 2002-2023 iterate GmbH. All rights reserved.
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

import ch.cyberduck.core.Factory;
import ch.cyberduck.core.FactoryException;
import ch.cyberduck.core.Host;
import ch.cyberduck.core.PathContainerService;

import org.apache.commons.lang3.reflect.ConstructorUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;

public class S3PathContainerServiceFactory extends Factory<PathContainerService> {
    private static final Logger log = LogManager.getLogger(S3PathContainerServiceFactory.class);

    private S3PathContainerServiceFactory() {
        super("factory.s3.pathcontainerservice.class");
    }

    public static PathContainerService get(final Host host) {
        return new S3PathContainerServiceFactory().create(host);
    }

    private PathContainerService create(final Host host) {
        try {
            final Constructor<? extends PathContainerService> constructor = ConstructorUtils.getMatchingAccessibleConstructor(clazz, host.getClass());
            if(null == constructor) {
                log.warn("No matching constructor for parameter {}", host.getClass());
                // Call default constructor for disabled implementations
                return clazz.getDeclaredConstructor().newInstance();
            }
            return constructor.newInstance(host);
        }
        catch(InstantiationException | InvocationTargetException | IllegalAccessException | NoSuchMethodException e) {
            throw new FactoryException(e.getMessage(), e);
        }
    }
}
