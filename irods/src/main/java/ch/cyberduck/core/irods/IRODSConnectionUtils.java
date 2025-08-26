package ch.cyberduck.core.irods;

/*
 * Copyright (c) 2002-2025 iterate GmbH. All rights reserved.
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

import org.apache.commons.lang3.StringUtils;
import org.irods.irods4j.authentication.AuthPlugin;
import org.irods.irods4j.authentication.NativeAuthPlugin;
import org.irods.irods4j.authentication.PamInteractiveAuthPlugin;
import org.irods.irods4j.authentication.PamPasswordAuthPlugin;
import org.irods.irods4j.high_level.connection.IRODSConnectionPool;
import org.irods.irods4j.high_level.connection.QualifiedUsername;
import org.irods.irods4j.low_level.api.IRODSApi;
import org.irods.irods4j.low_level.api.IRODSException;

import java.io.IOException;

final class IRODSConnectionUtils {

    public static void startIRODSConnectionPool(IRODSSession session, IRODSConnectionPool connPool) throws IRODSException, IOException {
        String host = session.getHost().getHostname();
        int port = session.getHost().getPort();
        String zone = session.getRegion();
        String username = session.getHost().getCredentials().getUsername();
        String password = session.getHost().getCredentials().getPassword();

        connPool.start(
                host,
                port,
                new QualifiedUsername(username, zone),
                conn -> {
                    try {
                        final String authScheme = StringUtils.defaultIfBlank(session.getHost().getProtocol().getAuthorization(), "native");
                        AuthPlugin plugin = null;
                        if("native".equals(authScheme)) {
                            plugin = new NativeAuthPlugin();
                        }
                        else if("pam_password".equals(authScheme)) {
                            plugin = new PamPasswordAuthPlugin(true);
                        }
                        else if("pam_interactive".equals(authScheme)) {
                            plugin = new PamInteractiveAuthPlugin(true);
                        }
                        else {
                            throw new IllegalArgumentException(String.format("Authentication scheme not recognized: %s", authScheme));
                        }
                        IRODSApi.rcAuthenticateClient(conn, plugin, password);
                        return true;
                    }
                    catch(Exception e) {
                        return false;
                    }
                });
    }
}
