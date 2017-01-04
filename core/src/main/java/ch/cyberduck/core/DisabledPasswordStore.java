package ch.cyberduck.core;

/*
 * Copyright (c) 2002-2013 David Kocher. All rights reserved.
 * http://cyberduck.ch/
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
 * Bug fixes, suggestions and comments should be sent to feedback@cyberduck.ch
 */

public class DisabledPasswordStore extends HostPasswordStore {

    @Override
    public String getPassword(final String serviceName, final String accountName) {
        return null;
    }

    @Override
    public void addPassword(final String serviceName, final String accountName, final String password) {
        //
    }

    @Override
    public String getPassword(final Scheme scheme, final int port, final String hostname, final String user) {
        return null;
    }

    @Override
    public void addPassword(final Scheme scheme, final int port, final String hostname, final String user, final String password) {
        //
    }
}
