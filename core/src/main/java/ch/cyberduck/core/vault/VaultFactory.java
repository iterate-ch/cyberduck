package ch.cyberduck.core.vault;

/*
 * Copyright (c) 2002-2016 iterate GmbH. All rights reserved.
 * https://cyberduck.io/
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
 */

import ch.cyberduck.core.Factory;
import ch.cyberduck.core.Path;
import ch.cyberduck.core.features.Vault;

import org.apache.commons.lang3.reflect.ConstructorUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;

public class VaultFactory extends Factory<Vault> {
    private static final Logger log = LogManager.getLogger(VaultFactory.class);

    protected VaultFactory() {
        super("factory.vault.class");
    }

    public static Vault get(final Path directory, final String masterkey, final String config, final byte[] pepper) {
        return new VaultFactory().create(directory, masterkey, config, pepper);
    }

    private Vault create(final Path directory, final String masterkey, final String config, final byte[] pepper) {
        try {
            final Constructor<? extends Vault> constructor = ConstructorUtils.getMatchingAccessibleConstructor(clazz,
                    directory.getClass(), masterkey.getClass(), config.getClass(), pepper.getClass());
            if(null == constructor) {
                log.warn(String.format("No matching constructor for parameter %s", directory.getClass()));
                // Call default constructor for disabled implementations
                return clazz.getDeclaredConstructor().newInstance();
            }
            return constructor.newInstance(directory, masterkey, config, pepper);
        }
        catch(InstantiationException | InvocationTargetException | IllegalAccessException | NoSuchMethodException e) {
            log.error(String.format("Failure loading callback class %s. %s", clazz, e.getMessage()));
            return Vault.DISABLED;
        }
    }
}
