package ch.cyberduck.core.diagnostics;

/*
 * Copyright (c) 2002-2023 iterate GmbH. All rights reserved.
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

import ch.cyberduck.core.DefaultIOExceptionMappingService;
import ch.cyberduck.core.Host;
import ch.cyberduck.core.HostnameConfigurator;
import ch.cyberduck.core.exception.BackgroundException;
import ch.cyberduck.core.proxy.ProxySocketFactory;
import ch.cyberduck.core.socket.DefaultSocketConfigurator;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.net.Socket;

public class TcpReachability implements Reachability {
    private static final Logger log = LogManager.getLogger(TcpReachability.class);

    @Override
    public void test(final Host bookmark) throws BackgroundException {
        final HostnameConfigurator configurator = bookmark.getProtocol().getFeature(HostnameConfigurator.class);
        try (Socket socket = new ProxySocketFactory(bookmark, new DefaultSocketConfigurator(Reachability.timeout))
                .createSocket(configurator.getHostname(bookmark.getHostname()), bookmark.getPort())) {
            if(log.isInfoEnabled()) {
                log.info(String.format("Opened socket %s for %s", socket, bookmark));
            }
        }
        catch(IOException e) {
            if(log.isWarnEnabled()) {
                log.warn(String.format("Failure opening socket for %s", bookmark));
            }
            throw new DefaultIOExceptionMappingService().map(e);
        }
    }

    @Override
    public Monitor monitor(final Host bookmark, final Callback callback) {
        return Monitor.disabled;
    }
}
