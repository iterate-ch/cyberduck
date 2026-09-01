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
import ch.cyberduck.core.LoginCallback;
import ch.cyberduck.core.exception.BackgroundException;
import ch.cyberduck.core.sftp.SFTPExceptionMappingService;
import ch.cyberduck.core.threading.CancelCallback;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.ietf.jgss.Oid;

import javax.security.auth.Subject;
import javax.security.auth.login.AppConfigurationEntry;
import javax.security.auth.login.Configuration;
import javax.security.auth.login.LoginContext;
import javax.security.auth.login.LoginException;
import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
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

        // Derive realm from the target hostname before login so the temp krb5.conf is ready
        // when Krb5LoginModule calls Config.refresh() via refreshKrb5Config=true below.
        // Config.getDefaultRealm() reads only from the parsed krb5.conf file — the
        // java.security.krb5.realm system property feeds a different code path and never
        // reaches getDefaultRealm().
        final String hostname = bookmark.getHostname();
        final String[] parts = hostname.split("\\.");
        final String savedKrb5Conf = System.getProperty("java.security.krb5.conf");
        final String savedSubjectCredsOnly = System.getProperty("javax.security.auth.useSubjectCredsOnly");
        File krb5conf = null;
        if(parts.length >= 2) {
            final String realm = (parts[parts.length - 2] + "." + parts[parts.length - 1]).toUpperCase(java.util.Locale.ROOT);
            log.debug("Derived Kerberos realm {} from hostname {}", realm, hostname);
            try {
                krb5conf = File.createTempFile("cyberduck-krb5-", ".conf");
                krb5conf.deleteOnExit();
                try(PrintWriter w = new PrintWriter(krb5conf)) {
                    w.println("[libdefaults]");
                    w.println("    default_realm = " + realm);
                    w.println("    dns_lookup_kdc = true");
                }
                System.setProperty("java.security.krb5.conf", krb5conf.getAbsolutePath());
            }
            catch(IOException e) {
                log.warn("Failed to write temporary krb5.conf for realm {}: {}", realm, e.getMessage());
            }
        }

        LoginContext loginContext = null;
        boolean loggedIn = false;
        try {
            // refreshKrb5Config=true tells Krb5LoginModule to call Config.refresh() at the
            // start of login(). Since the module runs inside java.security.jgss it can access
            // sun.security.krb5.Config directly, bypassing the module encapsulation that blocks
            // our own reflection calls.
            final Configuration jaasConfig = new Configuration() {
                @Override
                public AppConfigurationEntry[] getAppConfigurationEntry(final String name) {
                    final Map<String, String> options = new HashMap<>();
                    options.put("useTicketCache", "true");
                    options.put("renewTGT", "true");
                    options.put("doNotPrompt", "true");
                    options.put("refreshKrb5Config", "true");
                    return new AppConfigurationEntry[]{
                        new AppConfigurationEntry(
                            "com.sun.security.auth.module.Krb5LoginModule",
                            AppConfigurationEntry.LoginModuleControlFlag.REQUIRED,
                            options)
                    };
                }
            };
            loginContext = new LoginContext("cyberduck-sftp", new Subject(), null, jaasConfig);
            loginContext.login();
            loggedIn = true;
            log.debug("Kerberos TGT acquired for principals {}", loginContext.getSubject().getPrincipals());
            final List<Oid> mechanisms = Collections.singletonList(KRB5_MECH);
            System.setProperty("javax.security.auth.useSubjectCredsOnly", "false");
            try {
                client.auth(credentials.getUsername(), new AuthGssApiWithMic(loginContext, mechanisms));
            }
            finally {
                if(savedKrb5Conf != null) {
                    System.setProperty("java.security.krb5.conf", savedKrb5Conf);
                }
                else {
                    System.clearProperty("java.security.krb5.conf");
                }
                if(savedSubjectCredsOnly != null) {
                    System.setProperty("javax.security.auth.useSubjectCredsOnly", savedSubjectCredsOnly);
                }
                else {
                    System.clearProperty("javax.security.auth.useSubjectCredsOnly");
                }
                if(krb5conf != null) {
                    krb5conf.delete();
                }
            }
            final boolean authenticated = client.isAuthenticated();
            log.debug("GSS-API authentication result: authenticated={}, partialSuccess={}", authenticated,
                    client.getUserAuth().hadPartialSuccess());
            return authenticated;
        }
        catch(IOException e) {
            log.warn("GSS-API authentication failed for {}", bookmark.getHostname(), e);
            throw new SFTPExceptionMappingService().map(e);
        }
        catch(LoginException e) {
            // No TGT in cache or Kerberos not configured. Fall through to next auth method.
            log.warn("GSS-API login failed for {}: {}", bookmark.getHostname(), e.getMessage());
            return false;
        }
        finally {
            if(loggedIn) {
                try {
                    loginContext.logout();
                }
                catch(LoginException e) {
                    log.warn("Failed to logout GSS context: {}", e.getMessage());
                }
            }
        }
    }

    @Override
    public String getMethod() {
        return "gssapi-with-mic";
    }
}
