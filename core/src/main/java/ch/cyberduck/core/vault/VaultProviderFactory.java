package ch.cyberduck.core.vault;

/*
 * Copyright (c) 2002-2025 iterate GmbH. All rights reserved.
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
import ch.cyberduck.core.Session;

import org.apache.commons.lang3.reflect.ConstructorUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;

public class VaultProviderFactory extends Factory<VaultProvider> {
    private static final Logger log = LogManager.getLogger(VaultProviderFactory.class);

    private VaultProviderFactory() {
        super("factory.vaultprovider.class");
    }

    public static VaultProvider get(final Session<?> session) {
        return new VaultProviderFactory().create(session);
    }

    private VaultProvider create(final Session<?> session) {
        try {
            final Constructor<? extends VaultProvider> constructor = ConstructorUtils.getMatchingAccessibleConstructor(clazz,
                    session.getClass());
            if(null == constructor) {
                log.warn("No matching constructor for parameter {}", session.getClass());
                // Call default constructor for disabled implementations
                return clazz.getDeclaredConstructor().newInstance();
            }
            return constructor.newInstance(session);
        }
        catch(InstantiationException | InvocationTargetException | IllegalAccessException | NoSuchMethodException e) {
            log.error("Failure loading callback class {}. {}", clazz, e.getMessage());
            return VaultProvider.DISABLED;
        }
    }
}
