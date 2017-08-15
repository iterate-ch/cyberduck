package ch.cyberduck.core.manta;

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

import ch.cyberduck.core.Credentials;
import ch.cyberduck.core.Host;

/**
 * Created by tomascelaya on 5/25/17.
 */
public class SessionFactory {

    public static MantaSession create(final Credentials credentials) {
        return new MantaSession(
                new Host(
                        new MantaProtocol(),
                        null,
                        443,
                        credentials));
    }

    public static MantaSession create(final Credentials credentials, final String homePath) {
        return new MantaSession(
                new Host(
                        new MantaProtocol(),
                        null,
                        443,
                        homePath,
                        credentials));
    }
}
