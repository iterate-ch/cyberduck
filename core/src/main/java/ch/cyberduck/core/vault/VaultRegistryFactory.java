package ch.cyberduck.core.vault;

/*
 * Copyright (c) 2002-2017 iterate GmbH. All rights reserved.
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
import ch.cyberduck.core.FactoryException;
import ch.cyberduck.core.HostPasswordStore;
import ch.cyberduck.core.PasswordCallback;
import ch.cyberduck.core.PasswordStoreFactory;
import ch.cyberduck.core.preferences.PreferencesFactory;

import org.apache.commons.lang3.reflect.ConstructorUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;

public class VaultRegistryFactory extends Factory<VaultRegistry> {
    private static final Logger log = LogManager.getLogger(VaultRegistryFactory.class);

    private VaultRegistryFactory() {
        super("factory.vaultregistry.class");
    }

    public static VaultRegistry get(final PasswordCallback callback) {
        return get(PasswordStoreFactory.get(), callback);
    }

    public static VaultRegistry get(final HostPasswordStore keychain, final PasswordCallback callback) {
        return PreferencesFactory.get().getBoolean("cryptomator.enable") ?
                new VaultRegistryFactory().create(keychain, callback) : VaultRegistry.DISABLED;
    }

    public VaultRegistry create(final HostPasswordStore keychain, final PasswordCallback callback) {
        if(null == clazz) {
            throw new FactoryException(String.format("No implementation given for factory %s", this.getClass().getSimpleName()));
        }
        try {
            final Constructor<? extends VaultRegistry> constructor = ConstructorUtils
                    .getMatchingAccessibleConstructor(clazz, keychain.getClass(), callback.getClass());
            if(null == constructor) {
                log.warn("No matching constructor for parameters {},{}", keychain.getClass(), callback.getClass());
                // Call default constructor for disabled implementations
                return clazz.getDeclaredConstructor().newInstance();
            }
            return constructor.newInstance(keychain, callback);
        }
        catch(InstantiationException | InvocationTargetException | IllegalAccessException |
              NoSuchMethodException e) {
            log.error("Failure loading callback class {}. {}", clazz, e.getMessage());
            return VaultRegistry.DISABLED;
        }
    }
}