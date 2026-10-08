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
import ch.cyberduck.core.Factory;
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
import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
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
        final String defaultRealm = StringUtils.trim(preferences.getProperty("ssh.authentication.gssapi.realm"));
        if(StringUtils.isBlank(defaultRealm)) {
            this.configure(preferences);
            return this.login(bookmark, preferences);
        }
        // Default realm configured for this bookmark only. Only a configuration file allows to set the default
        // realm without also specifying the KDC. Restore JVM wide settings afterwards.
        final List<String> keys = Arrays.asList("java.security.krb5.conf", "java.security.krb5.realm", "java.security.krb5.kdc");
        final Map<String, String> saved = new HashMap<>();
        for(String key : keys) {
            saved.put(key, System.getProperty(key));
        }
        File temporary = null;
        try {
            temporary = File.createTempFile("cyberduck-krb5-", ".conf");
            try(PrintWriter writer = new PrintWriter(temporary, StandardCharsets.UTF_8.name())) {
                writer.println("[libdefaults]");
                writer.println("    default_realm = " + defaultRealm);
                final String kdc = preferences.getProperty("java.security.krb5.kdc");
                if(StringUtils.isNotBlank(kdc)) {
                    writer.println("[realms]");
                    writer.println("    " + defaultRealm + " = {");
                    writer.println("        kdc = " + kdc);
                    writer.println("    }");
                }
                else {
                    writer.println("    dns_lookup_kdc = true");
                }
            }
            log.debug("Use Kerberos default realm {} from temporary configuration {}", defaultRealm, temporary);
            System.setProperty("java.security.krb5.conf", temporary.getAbsolutePath());
            // Would override configuration file
            System.clearProperty("java.security.krb5.realm");
            System.clearProperty("java.security.krb5.kdc");
            return this.login(bookmark, preferences);
        }
        catch(IOException e) {
            log.warn("Failed to write temporary Kerberos configuration for realm {}: {}", defaultRealm, e.getMessage());
            this.configure(preferences);
            return this.login(bookmark, preferences);
        }
        finally {
            for(Map.Entry<String, String> entry : saved.entrySet()) {
                if(null == entry.getValue()) {
                    System.clearProperty(entry.getKey());
                }
                else {
                    System.setProperty(entry.getKey(), entry.getValue());
                }
            }
            if(temporary != null) {
                if(!temporary.delete()) {
                    log.warn("Failure deleting temporary Kerberos configuration {}", temporary);
                }
            }
        }
    }

    /**
     * Apply JVM wide settings read by Krb5LoginModule with refreshKrb5Config
     */
    private void configure(final HostPreferences preferences) {
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
    }

    private Boolean login(final Host bookmark, final HostPreferences preferences) throws BackgroundException {
        File exported = null;
        final KerberosDebug debug = log.isDebugEnabled() ? new KerberosDebug() : null;
        final boolean mit = preferences.getBoolean("ssh.authentication.gssapi.mit");
        final Factory.Platform.Name platform = Factory.Platform.getDefault();
        log.debug("Use MIT Kerberos option {} on platform {}", mit, platform);
        if(mit && platform.equals(Factory.Platform.Name.windows)) {
            // Java cannot read the in-memory default cache of MIT Kerberos for Windows. Export to a file cache.
            final File directory = MitKerberosTicketCache.locate(preferences.getProperty("ssh.authentication.gssapi.mit.path"));
            if(null == directory) {
                log.warn("MIT Kerberos for Windows not found");
            }
            else {
                log.debug("Export ticket from MIT Kerberos installed in {}", directory);
                try {
                    exported = new MitKerberosTicketCache(directory).export();
                }
                catch(IOException e) {
                    log.warn("Failure exporting ticket from MIT Kerberos: {}", e.getMessage());
                }
            }
        }
        try {
            if(exported != null) {
                // The exported ticket is a fresh copy. Renewing would require contacting the KDC and a failure
                // to renew discards the ticket.
                return this.login(bookmark, preferences, exported.getAbsolutePath(), false);
            }
            return this.login(bookmark, preferences, preferences.getProperty("ssh.authentication.gssapi.ticketcache"), true);
        }
        finally {
            if(exported != null) {
                if(!exported.delete()) {
                    log.warn("Failure deleting temporary Kerberos credentials cache {}", exported);
                }
            }
            if(debug != null) {
                debug.close();
            }
        }
    }

    private Boolean login(final Host bookmark, final HostPreferences preferences, final String ticketCache, final boolean renew) throws BackgroundException {
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
                    options.put("debug", String.valueOf(log.isDebugEnabled()));
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
