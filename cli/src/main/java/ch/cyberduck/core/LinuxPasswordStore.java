package ch.cyberduck.core;

/*
 * Copyright (c) 2002-2019 iterate GmbH. All rights reserved.
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

import ch.cyberduck.core.exception.LocalAccessDeniedException;
import ch.cyberduck.core.preferences.PreferencesFactory;

import org.freedesktop.secret.simple.SimpleCollection;

import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.Map;

public class LinuxPasswordStore extends DefaultHostPasswordStore {

    private final String application = PreferencesFactory.get().getProperty("application.name");
    private final SimpleCollection keyring;

    public LinuxPasswordStore() throws FactoryException {
        try {
            keyring = new SimpleCollection();
        }
        catch(IOException e) {
            throw new FactoryException("Accessing secret-service DBus API failed");
        }
    }

    @Override
    public String getPassword(final String serviceName, final String accountName) {
        final List<String> list = keyring.getItems(this.createAttributes(String.format("%s@%s", accountName, serviceName)));
        if(list != null) {
            return String.valueOf(keyring.getSecret(list.get(0)));
        }
        else {
            return null;
        }
    }

    @Override
    public void addPassword(final String serviceName, final String accountName, final String password) throws LocalAccessDeniedException {
        final List<String> list = keyring.getItems(this.createAttributes(password));
        if(list == null) {
            keyring.createItem(application, password, this.createAttributes(
                String.format("%s@%s", accountName, serviceName)));
        }
    }

    @Override
    public String getPassword(final Scheme scheme, final int port, final String hostname, final String user) {
        final List<String> list = keyring.getItems(this.createAttributes(
            String.format("%s://%s@%s:%d", scheme, user, hostname, port)
        ));
        if(list != null) {
            return String.valueOf(keyring.getSecret(list.get(0)));
        }
        else {
            return null;
        }
    }

    @Override
    public void addPassword(final Scheme scheme, final int port, final String hostname, final String user, final String password) throws LocalAccessDeniedException {
        final List<String> list = keyring.getItems(this.createAttributes(password));
        if(list == null) {
            keyring.createItem(application, password, this.createAttributes(
                String.format("%s://%s@%s:%d", scheme, user, hostname, port)
            ));
        }

    }

    private Map<String, String> createAttributes(final String key) {
        return Collections.singletonMap("URI", key);
    }
}
