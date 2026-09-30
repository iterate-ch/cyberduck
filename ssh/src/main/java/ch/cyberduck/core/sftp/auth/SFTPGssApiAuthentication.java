package ch.cyberduck.core.sftp.auth;

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

import ch.cyberduck.core.AuthenticationProvider;
import ch.cyberduck.core.Credentials;
import ch.cyberduck.core.Host;
import ch.cyberduck.core.LocalFactory;
import ch.cyberduck.core.LoginCallback;
import ch.cyberduck.core.exception.BackgroundException;
import ch.cyberduck.core.preferences.HostPreferences;
import ch.cyberduck.core.preferences.HostPreferencesFactory;
import ch.cyberduck.core.preferences.PreferencesFactory;
import ch.cyberduck.core.sftp.SFTPExceptionMappingService;
import ch.cyberduck.core.threading.CancelCallback;

import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.ietf.jgss.Oid;

import javax.security.auth.Subject;
import javax.security.auth.login.AppConfigurationEntry;
import javax.security.auth.login.Configuration;
import javax.security.auth.login.LoginContext;
import javax.security.auth.login.LoginException;
import java.io.IOException;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import net.schmizz.sshj.SSHClient;
import net.schmizz.sshj.userauth.method.AuthGssApiWithMic;

public class SFTPGssApiAuthentication implements AuthenticationProvider<Boolean> {
    private static final Logger log = LogManager.getLogger(SFTPGssApiAuthentication.class);

    /**
     * Kerberos v5 mechanism OID (1.2.840.113554.1.2.2) as advertised by OpenSSH servers
     * with <code>GSSAPIAuthentication yes</code>.
     */
    private static final Oid KRB5_MECH;

    static {
        try {
            KRB5_MECH = new Oid("1.2.840.113554.1.2.2");
        }
        catch(org.ietf.jgss.GSSException e) {
            // Cannot happen for a well-formed literal OID
            throw new IllegalStateException("Failed to encode Kerberos v5 OID", e);
        }
    }

    private final SSHClient client;

    public SFTPGssApiAuthentication(final SSHClient client) {
        this.client = client;
    }

    @Override
    public Boolean authenticate(final Host bookmark, final LoginCallback prompt, final CancelCallback cancel)
            throws BackgroundException {
        final Credentials credentials = bookmark.getCredentials();
        log.debug("Login using GSS-API/Kerberos authentication with credentials {}", credentials);

        final HostPreferences preferences = HostPreferencesFactory.get(bookmark);
        // JVM wide settings read by Krb5LoginModule with refreshKrb5Config
        final String conf = preferences.getProperty("java.security.krb5.conf");
        if(StringUtils.isNotBlank(conf)) {
            final String path = LocalFactory.get(conf).getAbsolute();
            log.debug("Use Kerberos configuration {}", path);
            System.setProperty("java.security.krb5.conf", path);
        }
        final String realm = preferences.getProperty("java.security.krb5.realm");
        final String kdc = preferences.getProperty("java.security.krb5.kdc");
        if(StringUtils.isNotBlank(realm) && StringUtils.isNotBlank(kdc)) {
            log.debug("Use Kerberos realm {} with KDC {}", realm, kdc);
            System.setProperty("java.security.krb5.realm", realm);
            System.setProperty("java.security.krb5.kdc", kdc);
        }
        else if(StringUtils.isNotBlank(realm) || StringUtils.isNotBlank(kdc)) {
            log.warn("Ignore Kerberos realm {} and KDC {} not both set", realm, kdc);
        }
        LoginContext loginContext;
        try {
            final Configuration jaasConfig = new Configuration() {
                @Override
                public AppConfigurationEntry[] getAppConfigurationEntry(final String name) {
                    final Map<String, String> options = new HashMap<>();
                    options.put("useTicketCache", "true");
                    options.put("renewTGT", "true");
                    options.put("doNotPrompt", "true");
                    // Pick up changes to krb5.conf or its location without restarting
                    options.put("refreshKrb5Config", "true");
                    final String ticketCache = preferences.getProperty("ssh.authentication.gssapi.ticketcache");
                    if(StringUtils.isNotBlank(ticketCache)) {
                        options.put("ticketCache", ticketCache);
                    }
                    return new AppConfigurationEntry[]{
                            new AppConfigurationEntry(
                                    "com.sun.security.auth.module.Krb5LoginModule",
                                    AppConfigurationEntry.LoginModuleControlFlag.REQUIRED,
                                    options)
                    };
                }
            };
            loginContext = new LoginContext(PreferencesFactory.get().getProperty("application.name"),
                    new Subject(), null, jaasConfig);
            loginContext.login();
            log.debug("Kerberos TGT acquired for principals {}", loginContext.getSubject().getPrincipals());
        }
        catch(LoginException e) {
            // No TGT in cache or Kerberos not configured. Fall through to the next auth method.
            log.warn("GSS-API login failed for {}: {}", bookmark.getHostname(), e.getMessage());
            return false;
        }
        try {
            final List<Oid> mechanisms = Collections.singletonList(KRB5_MECH);
            // Security context is established with the credentials of the subject of the login context
            client.auth(credentials.getUsername(), new AuthGssApiWithMic(loginContext, mechanisms));
            final boolean authenticated = client.isAuthenticated();
            log.debug("GSS-API authentication result: authenticated={}, partialSuccess={}", authenticated,
                    client.getUserAuth().hadPartialSuccess());
            return authenticated;
        }
        catch(IOException e) {
            log.warn("GSS-API authentication failed for {}", bookmark.getHostname(), e);
            throw new SFTPExceptionMappingService().map(e);
        }
        finally {
            try {
                loginContext.logout();
            }
            catch(LoginException e) {
                log.warn("Failed to logout GSS context: {}", e.getMessage());
            }
        }
    }

    @Override
    public String getMethod() {
        return "gssapi-with-mic";
    }
}
