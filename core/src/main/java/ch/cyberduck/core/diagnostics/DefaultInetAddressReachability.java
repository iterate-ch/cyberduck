package ch.cyberduck.core.diagnostics;

/*
 * Copyright (c) 2002-2015 David Kocher. All rights reserved.
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

import ch.cyberduck.core.ConnectionTimeoutFactory;
import ch.cyberduck.core.DefaultIOExceptionMappingService;
import ch.cyberduck.core.Host;
import ch.cyberduck.core.HostnameConfiguratorFactory;
import ch.cyberduck.core.exception.BackgroundException;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.net.ConnectException;
import java.net.InetAddress;

/**
 * A typical implementation will use ICMP ECHO REQUESTs if the privilege can be obtained, otherwise it will try to
 * establish a TCP connection on port 7 (Echo) of the destination host.
 */
public class DefaultInetAddressReachability extends DisabledReachability {
    private static final Logger log = LogManager.getLogger(DefaultInetAddressReachability.class);

    @Override
    public void test(final Host bookmark) throws BackgroundException {
        try {
            if(!InetAddress.getByName(HostnameConfiguratorFactory.get(bookmark.getProtocol()).getHostname(bookmark.getHostname())).isReachable(
                    ConnectionTimeoutFactory.get(bookmark).getTimeout() * 1000
            )) {
                throw new ConnectException();
            }
        }
        catch(IOException e) {
            if(log.isWarnEnabled()) {
                log.warn(String.format("Failure opening ICMP socket for %s", bookmark));
            }
            throw new DefaultIOExceptionMappingService().map(e);
        }
    }
}
