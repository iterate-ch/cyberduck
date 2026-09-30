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
import ch.cyberduck.core.Host;
import ch.cyberduck.core.HostParser;
import ch.cyberduck.core.Protocol;
import ch.cyberduck.core.ProtocolFactory;
import ch.cyberduck.core.SessionPoolFactory;
import ch.cyberduck.core.exception.HostParserException;
import ch.cyberduck.core.pool.SessionPool;
import ch.cyberduck.core.preferences.PreferencesFactory;
import ch.cyberduck.core.serviceloader.AutoServiceLoaderFactory;
import ch.cyberduck.core.threading.WorkerBackgroundAction;
import ch.cyberduck.core.worker.Worker;

import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.Collections;
import java.util.Set;

public class ProfilesWorkerBackgroundAction extends WorkerBackgroundAction<Set<ProfileDescription>> {
    private static final Logger log = LogManager.getLogger(ProfilesWorkerBackgroundAction.class);

    public ProfilesWorkerBackgroundAction(final Controller controller, final ProfilesSynchronizeWorker worker) throws HostParserException {
        this(controller, SessionPoolFactory.create(controller, toHost(PreferencesFactory.get().getProperty("profiles.discovery.updater.url"))), worker);
    }

    public ProfilesWorkerBackgroundAction(final Controller controller, final SessionPool session, final Worker<Set<ProfileDescription>> worker) {
        super(controller, session, worker);
    }

    /**
     * Parse URL with protocol implementation found by service loader matching identifier or scheme regardless of
     * registration in protocol factory
     *
     * @param url Profiles repository URL such as s3://profiles.cyberduck.io
     * @return Bookmark with anonymous credentials
     */
    static Host toHost(final String url) throws HostParserException {
        final String scheme = StringUtils.substringBefore(url, ":");
        final Set<Protocol> protocols = AutoServiceLoaderFactory.<Protocol>get().load(Protocol.class);
        final Protocol match = protocols.stream()
                .filter(protocol -> StringUtils.equals(protocol.getIdentifier(), scheme))
                .findFirst()
                .orElseGet(() -> protocols.stream()
                        .filter(protocol -> StringUtils.equals(protocol.getScheme().name(), scheme))
                        .findFirst()
                        .orElse(null));
        if(null == match) {
            throw new HostParserException(String.format("Unsupported scheme %s in URI %s", scheme, url));
        }
        log.debug("Use protocol {} for {}", match, url);
        return new HostParser(new ProtocolFactory(Collections.singleton(match)), match).get(url).setCredentials(
                new Credentials(PreferencesFactory.get().getProperty("connection.login.anon.name")));
    }
}
