package ch.cyberduck.core.sftp.auth;

/*
 * Copyright (c) 2002-2026 iterate GmbH. All rights reserved.
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
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.ietf.jgss.GSSContext;
import org.ietf.jgss.GSSCredential;
import org.ietf.jgss.GSSException;
import org.ietf.jgss.GSSManager;
import org.ietf.jgss.GSSName;
import org.ietf.jgss.Oid;

import javax.security.auth.Subject;
import javax.security.auth.kerberos.KerberosPrincipal;
import java.security.Provider;

/**
 * Maps host-based service names such as <code>host@server.example.com</code> to an explicit Kerberos principal
 * <code>host/server.example.com@REALM</code>. Allows to choose the realm of the server per connection without
 * changing the JVM-wide Kerberos configuration.
 */
public class KerberosRealmGSSManager extends GSSManager {
    private static final Logger log = LogManager.getLogger(KerberosRealmGSSManager.class);

    /**
     * Kerberos principal name type (1.2.840.113554.1.2.2.1)
     */
    static final Oid NT_KRB5_PRINCIPAL_NAME;

    static {
        try {
            NT_KRB5_PRINCIPAL_NAME = new Oid("1.2.840.113554.1.2.2.1");
        }
        catch(GSSException e) {
            // Cannot happen for a well-formed literal OID
            throw new IllegalStateException("Failed to encode Kerberos principal name OID", e);
        }
    }

    private final GSSManager proxy;
    /**
     * Realm of the server or null to map the host to a realm using the Kerberos configuration
     */
    private final String realm;
    /**
     * Subject with Kerberos ticket granting ticket. Its realm is used when the realm of the host cannot be determined
     */
    private final Subject subject;

    /**
     * @param proxy   Delegate
     * @param realm   Realm of the server or null to map the host to a realm using the Kerberos configuration
     * @param subject Subject with Kerberos principal of the client used as fallback for the realm
     */
    public KerberosRealmGSSManager(final GSSManager proxy, final String realm, final Subject subject) {
        this.proxy = proxy;
        this.realm = StringUtils.trimToNull(realm);
        this.subject = subject;
    }

    /**
     * @param service Host-based service name in the form <code>service@hostname</code>
     * @return Kerberos principal name with explicit realm or null to resolve the realm using the Kerberos configuration
     */
    String toPrincipal(final String service) {
        final String name = StringUtils.substringBefore(service, "@");
        final String host = StringUtils.substringAfter(service, "@");
        if(StringUtils.isBlank(name) || StringUtils.isBlank(host)) {
            return null;
        }
        String realm = this.realm;
        if(null == realm) {
            try {
                // Mapping by domain_realm or default_realm in configuration
                final KerberosPrincipal mapped = new KerberosPrincipal(String.format("%s/%s", name, host), KerberosPrincipal.KRB_NT_SRV_HST);
                log.debug("Use realm {} from Kerberos configuration for {}", mapped.getRealm(), host);
                return null;
            }
            catch(IllegalArgumentException e) {
                log.debug("Failure determining realm for {} from Kerberos configuration: {}", host, e.getMessage());
            }
            realm = this.getClientRealm();
            if(null == realm) {
                log.warn("Unable to determine realm for {}", host);
                return null;
            }
            log.debug("Use realm {} of client principal for {}", realm, host);
        }
        else {
            log.debug("Use configured realm {} for {}", realm, host);
        }
        try {
            // Hostname is canonicalized as for host-based service names unless disabled with dns_canonicalize_hostname
            return new KerberosPrincipal(String.format("%s/%s@%s", name, host, realm), KerberosPrincipal.KRB_NT_SRV_HST).getName();
        }
        catch(IllegalArgumentException e) {
            log.warn("Failure parsing principal for {}: {}", host, e.getMessage());
            return String.format("%s/%s@%s", name, host, realm);
        }
    }

    private String getClientRealm() {
        if(null == subject) {
            return null;
        }
        for(KerberosPrincipal principal : subject.getPrincipals(KerberosPrincipal.class)) {
            if(StringUtils.isNotBlank(principal.getRealm())) {
                return principal.getRealm();
            }
        }
        return null;
    }

    @Override
    public GSSName createName(final String nameStr, final Oid nameType) throws GSSException {
        if(GSSName.NT_HOSTBASED_SERVICE.equals(nameType)) {
            final String principal = this.toPrincipal(nameStr);
            if(principal != null) {
                return proxy.createName(principal, NT_KRB5_PRINCIPAL_NAME);
            }
        }
        return proxy.createName(nameStr, nameType);
    }

    @Override
    public GSSName createName(final String nameStr, final Oid nameType, final Oid mech) throws GSSException {
        if(GSSName.NT_HOSTBASED_SERVICE.equals(nameType)) {
            final String principal = this.toPrincipal(nameStr);
            if(principal != null) {
                return proxy.createName(principal, NT_KRB5_PRINCIPAL_NAME, mech);
            }
        }
        return proxy.createName(nameStr, nameType, mech);
    }

    @Override
    public GSSName createName(final byte[] name, final Oid nameType) throws GSSException {
        return proxy.createName(name, nameType);
    }

    @Override
    public GSSName createName(final byte[] name, final Oid nameType, final Oid mech) throws GSSException {
        return proxy.createName(name, nameType, mech);
    }

    @Override
    public Oid[] getMechs() {
        return proxy.getMechs();
    }

    @Override
    public Oid[] getNamesForMech(final Oid mech) throws GSSException {
        return proxy.getNamesForMech(mech);
    }

    @Override
    public Oid[] getMechsForName(final Oid nameType) {
        return proxy.getMechsForName(nameType);
    }

    @Override
    public GSSCredential createCredential(final int usage) throws GSSException {
        return proxy.createCredential(usage);
    }

    @Override
    public GSSCredential createCredential(final GSSName name, final int lifetime, final Oid mech, final int usage) throws GSSException {
        return proxy.createCredential(name, lifetime, mech, usage);
    }

    @Override
    public GSSCredential createCredential(final GSSName name, final int lifetime, final Oid[] mechs, final int usage) throws GSSException {
        return proxy.createCredential(name, lifetime, mechs, usage);
    }

    @Override
    public GSSContext createContext(final GSSName peer, final Oid mech, final GSSCredential myCred, final int lifetime) throws GSSException {
        return proxy.createContext(peer, mech, myCred, lifetime);
    }

    @Override
    public GSSContext createContext(final GSSCredential myCred) throws GSSException {
        return proxy.createContext(myCred);
    }

    @Override
    public GSSContext createContext(final byte[] interProcessToken) throws GSSException {
        return proxy.createContext(interProcessToken);
    }

    @Override
    public void addProviderAtFront(final Provider p, final Oid mech) throws GSSException {
        proxy.addProviderAtFront(p, mech);
    }

    @Override
    public void addProviderAtEnd(final Provider p, final Oid mech) throws GSSException {
        proxy.addProviderAtEnd(p, mech);
    }
}
