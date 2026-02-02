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

import ch.cyberduck.core.preferences.HostPreferencesFactory;
import ch.cyberduck.core.preferences.PreferencesReader;

import ch.cyberduck.core.ssl.X509TrustManager;

import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.irods.irods4j.authentication.AuthPlugin;
import org.irods.irods4j.authentication.NativeAuthPlugin;
import org.irods.irods4j.authentication.PamPasswordAuthPlugin;
import org.irods.irods4j.high_level.connection.IRODSConnection;
import org.irods.irods4j.high_level.connection.IRODSConnectionPool;
import org.irods.irods4j.high_level.connection.QualifiedUsername;
import org.irods.irods4j.low_level.api.IRODSApi;
import org.irods.irods4j.low_level.api.IRODSException;

import javax.net.ssl.TrustManager;
import java.io.IOException;

final class IRODSConnectionUtils {

    private static final Logger log = LogManager.getLogger(IRODSConnectionUtils.class);

    private enum AuthScheme {
        NATIVE,
        PAM_PASSWORD;

        public static AuthScheme fromString(String s) {
            if(null == s) {
                throw new IllegalArgumentException("Cannot convert null to AuthScheme");
            }

            if("native".equalsIgnoreCase(s) || "standard".equalsIgnoreCase(s)) {
                return NATIVE;
            }

            if("pam_password".equalsIgnoreCase(s) || "pam".equalsIgnoreCase(s)) {
                return PAM_PASSWORD;
            }

            throw new IllegalArgumentException(String.format("Cannot convert [%s] to AuthScheme", s));
        }
    }

    public static IRODSApi.ConnectionOptions initConnectionOptions(IRODSSession session) {
        log.debug("configuring iRODS connection.");
        final PreferencesReader preferences = HostPreferencesFactory.get(session.getHost());
        final IRODSApi.ConnectionOptions options = new IRODSApi.ConnectionOptions();

        options.clientServerNegotiation = preferences.getProperty(IRODSProtocol.CLIENT_SERVER_NEGOTIATION);
        options.trustManagers = new TrustManager[]{session.getFeature(X509TrustManager.class)};
        log.debug("client server negotiation = [{}], ssl protocol = [{}]", options.clientServerNegotiation, options.sslProtocol);

        options.encryptionAlgorithm = preferences.getProperty(IRODSProtocol.ENCRYPTION_ALGORITHM);
        options.encryptionKeySize = preferences.getInteger(IRODSProtocol.ENCRYPTION_KEY_SIZE);
        options.encryptionSaltSize = preferences.getInteger(IRODSProtocol.ENCRYPTION_SALT_SIZE);
        options.encryptionNumHashRounds = preferences.getInteger(IRODSProtocol.ENCRYPTION_HASH_ROUNDS);
        log.debug("encryption algorithm = [{}], encryption key size = [{}], encryption salt size = [{}], encryption hash rounds = [{}]",
                options.encryptionAlgorithm, options.encryptionKeySize, options.encryptionSaltSize, options.encryptionNumHashRounds);

        return options;
    }

    public static AuthPlugin newAuthPlugin(IRODSSession session) {
        AuthPlugin plugin = null;

        final String authSchemeStr = StringUtils.defaultIfBlank(session.getHost().getProtocol().getAuthorization(), "native");
        final AuthScheme authScheme = AuthScheme.fromString(authSchemeStr);
        switch(authScheme) {
            case NATIVE:
                plugin = new NativeAuthPlugin();
                break;

            case PAM_PASSWORD:
                plugin = new PamPasswordAuthPlugin(true);
                break;

            default:
                // Should never get here.
                throw new IllegalArgumentException("Cannot resolve authentication scheme to plugin implementation");
        }

        return plugin;
    }

    public static IRODSConnection newAuthenticatedConnection(IRODSSession session) throws Exception {
        String host = session.getHost().getHostname();
        int port = session.getHost().getPort();
        String zone = session.getRegion();
        String username = session.getHost().getCredentials().getUsername();
        String password = session.getHost().getCredentials().getPassword();
        IRODSConnection conn = new IRODSConnection(initConnectionOptions(session));
        conn.connect(host, port, new QualifiedUsername(username, zone));
        conn.authenticate(newAuthPlugin(session), password);
        return conn;
    }

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
                        IRODSApi.rcAuthenticateClient(conn, newAuthPlugin(session), password);
                        return true;
                    }
                    catch(Exception e) {
                        log.error(e.getMessage());
                        return false;
                    }
                });
    }
}
