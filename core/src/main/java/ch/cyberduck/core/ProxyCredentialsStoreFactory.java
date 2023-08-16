package ch.cyberduck.core;

/*
 * Copyright (c) 2002-2017 iterate GmbH. All rights reserved.
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

public class ProxyCredentialsStoreFactory extends Factory<ProxyCredentialsStore> {

    private ProxyCredentialsStoreFactory() {
        super("factory.proxycredentialsstore.class");
    }

    private static ProxyCredentialsStore store;

    public static synchronized ProxyCredentialsStore get() {
        if(null == store) {
            store = new ProxyCredentialsStoreFactory().create();
        }
        return store;
    }
}
