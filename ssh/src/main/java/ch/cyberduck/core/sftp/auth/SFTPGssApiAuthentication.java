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

import ch.cyberduck.core.AlphanumericRandomStringService;
import ch.cyberduck.core.AuthenticationProvider;
import ch.cyberduck.core.Credentials;
import ch.cyberduck.core.Factory;
import ch.cyberduck.core.Host;
import ch.cyberduck.core.Local;
import ch.cyberduck.core.LocalFactory;
import ch.cyberduck.core.LoginCallback;
import ch.cyberduck.core.exception.AccessDeniedException;
import ch.cyberduck.core.exception.BackgroundException;
import ch.cyberduck.core.exception.LocalAccessDeniedException;
import ch.cyberduck.core.local.TemporaryFileServiceFactory;
import ch.cyberduck.core.preferences.HostPreferences;
import ch.cyberduck.core.preferences.HostPreferencesFactory;
import ch.cyberduck.core.preferences.PreferencesFactory;
import ch.cyberduck.core.sftp.SFTPExceptionMappingService;
import ch.cyberduck.core.threading.CancelCallback;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
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
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import net.schmizz.sshj.SSHClient;
import net.schmizz.sshj.userauth.method.AuthGssApiWithMic;

public class SFTPGssApiAuthentication implements AuthenticationProvider<Boolean> {
    private static final Logger log = LogManager.getLogger(SFTPGssApiAuthentication.class);

    /**
     * Bookmark specific default realm for GSS-API authentication
     */
    public static final String KERBEROS_REALM_PROPERTY = "ssh.authentication.gssapi.realm";

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
    public Boolean authenticate(final Host bookmark, final LoginCallback prompt, final CancelCallback cancel) throws BackgroundException {
        final Credentials credentials = bookmark.getCredentials();
        log.debug("Login using GSS-API/Kerberos authentication with credentials {}", credentials);
        final HostPreferences preferences = HostPreferencesFactory.get(bookmark);
        final String defaultRealm = StringUtils.trim(preferences.getProperty(KERBEROS_REALM_PROPERTY));
        if(StringUtils.isBlank(defaultRealm)) {
            return this.login(this.configure(bookmark));
        }
        // Default realm configured for this bookmark only. Only a configuration file allows setting the default
        // realm without also specifying the KDC. Restore JVM-wide settings afterward.
        final List<String> keys = Arrays.asList("java.security.krb5.conf", "java.security.krb5.realm", "java.security.krb5.kdc");
        final Map<String, String> saved = new HashMap<>();
        keys.forEach(key -> saved.put(key, System.getProperty(key)));
        try {
            final Local temporary = TemporaryFileServiceFactory.get().create(String.format("%s.conf",
                    new AlphanumericRandomStringService().random()));
            final String kdc = preferences.getProperty("java.security.krb5.kdc");
            final String content = String.format("[libdefaults]\n    default_realm = %s\n%s",
                    defaultRealm,
                    StringUtils.isNotBlank(kdc) ?
                            String.format("[realms]\n    %s = {\n        kdc = %s\n    }\n", defaultRealm, kdc) :
                            "    dns_lookup_kdc = true\n");
            try {
                // Close before use to make sure the content is flushed and any failure writing is detected
                try(final OutputStream out = temporary.getOutputStream(false)) {
                    IOUtils.write(content, out, StandardCharsets.UTF_8);
                }
                catch(IOException | LocalAccessDeniedException e) {
                    log.warn("Failed to write temporary Kerberos configuration for realm {}: {}", defaultRealm, e.getMessage());
                    return this.login(this.configure(bookmark));
                }
                log.debug("Use Kerberos default realm {} from temporary configuration {}", defaultRealm, temporary);
                System.setProperty("java.security.krb5.conf", temporary.getAbsolute());
                // Would override configuration file
                System.clearProperty("java.security.krb5.realm");
                System.clearProperty("java.security.krb5.kdc");
                return this.login(bookmark);
            }
            finally {
                if(temporary.exists()) {
                    try {
                        temporary.delete();
                    }
                    catch(AccessDeniedException e) {
                        log.warn("Failure deleting temporary Kerberos configuration {}: {}", temporary, e.getMessage());
                    }
                }
            }
        }
        finally {
            saved.forEach((key, value) -> {
                if(null == value) {
                    System.clearProperty(key);
                }
                else {
                    System.setProperty(key, value);
                }
            });
        }
    }

    /**
     * Apply JVM wide settings read by Krb5LoginModule with refreshKrb5Config
     */
    private Host configure(final Host bookmark) {
        final HostPreferences preferences = HostPreferencesFactory.get(bookmark);
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
        return bookmark;
    }

    /**
     * Attempts to authenticate to a remote host using GSS-API with Kerberos. This method
     * interacts with the operating system and Kerberos libraries to establish a security
     * context using the Kerberos protocol.
     *
     * @param bookmark The host configuration object including connection details such as
     *                 the target server, user credentials, and preferences.
     * @return True if the authentication succeeds, false otherwise.
     * @throws BackgroundException If an error occurs during the authentication process
     *                             or Kerberos ticket handling.
     */
    private Boolean login(final Host bookmark) throws BackgroundException {
        final HostPreferences preferences = HostPreferencesFactory.get(bookmark);
        switch(Factory.Platform.getDefault()) {
            case windows:
                if(preferences.getBoolean("ssh.authentication.gssapi.mit")) {
                    // Java cannot read the in-memory default cache of MIT Kerberos for Windows. Export to a file cache.
                    final Local directory = MitKerberosTicketCache.locate(preferences.getProperty("ssh.authentication.gssapi.mit.path"));
                    log.debug("Use MIT Kerberos directory {}", directory);
                    if(null == directory) {
                        log.warn("MIT Kerberos for Windows not found");
                    }
                    else {
                        log.debug("Export ticket from MIT Kerberos installed in {}", directory);
                        try {
                            final File exported = new MitKerberosTicketCache(directory).export();
                            // The exported ticket is a fresh copy. Renewing would require contacting the KDC and a failure
                            // to renew discards the ticket.
                            try {
                                return this.login(bookmark, exported.getAbsolutePath(), false);
                            }
                            finally {
                                if(!exported.delete()) {
                                    log.warn("Failure deleting temporary Kerberos credentials cache {}", exported);
                                }
                            }
                        }
                        catch(IOException e) {
                            log.warn("Failure exporting ticket from MIT Kerberos: {}", e.getMessage());
                        }
                    }
                }
        }
        return this.login(bookmark, preferences.getProperty("ssh.authentication.gssapi.ticketcache"), true);
    }

    /**
     * Attempts to authenticate to a remote host using GSS-API with Kerberos. This method establishes
     * a security context utilizing Kerberos credentials from a provided ticket cache or credentials
     * configuration.
     *
     * @param bookmark    The host configuration containing connection details and user credentials.
     * @param ticketCache The file path to the Kerberos ticket cache. If empty, the default cache will be used.
     * @param renew       A flag indicating whether to attempt renewal of Kerberos tickets from the cache.
     * @return True if the authentication succeeds, false otherwise.
     * @throws BackgroundException If authentication fails or an error occurs during the process.
     */
    private Boolean login(final Host bookmark, final String ticketCache, final boolean renew) throws BackgroundException {
        final Credentials credentials = bookmark.getCredentials();
        LoginContext loginContext;
        try {
            final Configuration jaasConfig = new Configuration() {
                @Override
                public AppConfigurationEntry[] getAppConfigurationEntry(final String name) {
                    final Map<String, String> options = new HashMap<>();
                    options.put("useTicketCache", "true");
                    options.put("renewTGT", String.valueOf(renew));
                    options.put("doNotPrompt", "true");
                    // Pick up changes to krb5.conf or its location without restarting
                    options.put("refreshKrb5Config", "true");
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
