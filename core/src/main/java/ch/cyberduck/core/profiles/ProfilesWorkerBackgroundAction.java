package ch.cyberduck.core.profiles;

/*
 * Copyright (c) 2002-2021 iterate GmbH. All rights reserved.
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

import ch.cyberduck.core.Controller;
import ch.cyberduck.core.Credentials;
import ch.cyberduck.core.HostParser;
import ch.cyberduck.core.SessionPoolFactory;
import ch.cyberduck.core.exception.HostParserException;
import ch.cyberduck.core.preferences.PreferencesFactory;
import ch.cyberduck.core.threading.WorkerBackgroundAction;

import java.util.Set;

public class ProfilesWorkerBackgroundAction extends WorkerBackgroundAction<Set<ProfileDescription>> {

    public ProfilesWorkerBackgroundAction(final Controller controller, final ProfilesSynchronizeWorker worker) throws HostParserException {
        super(controller, SessionPoolFactory.create(controller,
            HostParser.parse(PreferencesFactory.get().getProperty("profiles.discovery.updater.url")).withCredentials(
                new Credentials(PreferencesFactory.get().getProperty("connection.login.anon.name")))), worker);
    }
}
