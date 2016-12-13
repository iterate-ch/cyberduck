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
import ch.cyberduck.core.FactoryException;
import ch.cyberduck.core.PasswordCallback;
import ch.cyberduck.core.PasswordStore;
import ch.cyberduck.core.Path;
import ch.cyberduck.core.features.Vault;
import ch.cyberduck.core.preferences.PreferencesFactory;

import org.apache.commons.lang3.reflect.ConstructorUtils;
import org.apache.log4j.Logger;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;

public class VaultFactory extends Factory<Vault> {
    private static final Logger log = Logger.getLogger(VaultFactory.class);

    protected VaultFactory() {
        super("factory.vault.class");
    }

    public static Vault get(final Path directory, final PasswordStore keychain, final PasswordCallback prompt, final VaultLookupListener listener) {
        return new VaultFactory().create(directory, keychain, prompt, listener);
    }

    private Vault create(final Path directory, final PasswordStore keychain, final PasswordCallback prompt, final VaultLookupListener listener) {
        final String clazz = PreferencesFactory.get().getProperty("factory.vault.class");
        if(null == clazz) {
            throw new FactoryException(String.format("No implementation given for factory %s", this.getClass().getSimpleName()));
        }
        try {
            final Class<Vault> name = (Class<Vault>) Class.forName(clazz);
            final Constructor<Vault> constructor = ConstructorUtils.getMatchingAccessibleConstructor(name,
                    directory.getClass(), keychain.getClass(), prompt.getClass(), listener.getClass());
            if(null == constructor) {
                log.warn(String.format("No matching constructor for parameter %s", directory.getClass()));
                // Call default constructor for disabled implementations
                return name.newInstance();
            }
            return constructor.newInstance(directory, keychain, prompt, listener);
        }
        catch(InstantiationException | InvocationTargetException | ClassNotFoundException | IllegalAccessException e) {
            log.error(String.format("Failure loading callback class %s. %s", clazz, e.getMessage()));
            return Vault.DISABLED;
        }
    }
}
