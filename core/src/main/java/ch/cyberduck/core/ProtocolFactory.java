package ch.cyberduck.core;

/*
 * Copyright (c) 2002-2011 David Kocher. All rights reserved.
 *
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
 * Bug fixes, suggestions and comments should be sent to:
 * dkocher@cyberduck.ch
 */

import ch.cyberduck.core.exception.AccessDeniedException;
import ch.cyberduck.core.preferences.PreferencesFactory;

import org.apache.commons.io.FilenameUtils;
import org.apache.log4j.Logger;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

public final class ProtocolFactory {
    private static final Logger log = Logger.getLogger(ProtocolFactory.class);

    /**
     * Ordered list of supported protocols.
     */
    private static final Set<Protocol> registered
            = new LinkedHashSet<Protocol>();

    public static final ProtocolFactory global = new ProtocolFactory(registered);

    private final Set<Protocol> protocols;

    public ProtocolFactory(final Set<Protocol> protocols) {
        this.protocols = protocols;
    }

    public Protocol find(final String identifier) {
        return ProtocolFactory.forName(protocols, identifier);
    }

    public static void register(Protocol... protocols) {
        // Order determines list in connection dropdown
        for(Protocol protocol : protocols) {
            register(protocol);
        }
        // Order determines list in connection dropdown
        final Local bundled = LocalFactory.get(PreferencesFactory.get().getProperty("application.profiles.path"));
        if(bundled.exists()) {
            try {
                for(Local f : bundled.list().filter(new Filter<Local>() {
                    @Override
                    public boolean accept(final Local file) {
                        return "cyberduckprofile".equals(FilenameUtils.getExtension(file.getName()));
                    }
                })) {
                    final Profile profile = ProfileReaderFactory.get().read(f);
                    if(null == profile.getProtocol()) {
                        continue;
                    }
                    if(log.isInfoEnabled()) {
                        log.info(String.format("Adding bundled protocol %s", profile));
                    }
                    // Replace previous possibly disable protocol in Preferences
                    register(profile);
                }
            }
            catch(AccessDeniedException e) {
                log.warn(String.format("Failure reading collection %s %s", bundled, e.getMessage()));
            }
        }
        // Load thirdparty protocols
        final Local library = LocalFactory.get(PreferencesFactory.get().getProperty("application.support.path"),
                PreferencesFactory.get().getProperty("profiles.folder.name"));
        if(library.exists()) {
            try {
                for(Local profile : library.list().filter(new Filter<Local>() {
                    @Override
                    public boolean accept(final Local file) {
                        return "cyberduckprofile".equals(FilenameUtils.getExtension(file.getName()));
                    }
                })) {
                    final Profile protocol = ProfileReaderFactory.get().read(profile);
                    if(null == protocol) {
                        continue;
                    }
                    if(log.isInfoEnabled()) {
                        log.info(String.format("Adding thirdparty protocol %s", protocol));
                    }
                    // Replace previous possibly disable protocol in Preferences
                    register(protocol);
                }
            }
            catch(AccessDeniedException e) {
                log.warn(String.format("Failure reading collection %s %s", library, e.getMessage()));
            }
        }
    }

    public static void register(final Protocol p) {
        registered.remove(p);
        registered.add(p);
    }

    /**
     * @return List of protocols
     */
    public static List<Protocol> getEnabledProtocols() {
        final List<Protocol> enabled = new ArrayList<Protocol>();
        for(Protocol protocol : registered) {
            if(protocol.isEnabled()) {
                enabled.add(protocol);
            }
        }
        return enabled;
    }

    /**
     * @param identifier Provider name or hash code of protocol
     * @return Matching protocol or null if no match
     */
    public static Protocol forName(final String identifier) {
        return ProtocolFactory.forName(registered, identifier);
    }

    public static Protocol forName(final Set<Protocol> protocols, final String identifier) {
        for(Protocol protocol : protocols) {
            if(protocol.getProvider().equals(identifier)) {
                return protocol;
            }
        }
        for(Protocol protocol : protocols) {
            if(String.valueOf(protocol.hashCode()).equals(identifier)) {
                return protocol;
            }
        }
        for(Protocol protocol : protocols) {
            for(String scheme : protocol.getSchemes()) {
                if(scheme.equals(identifier)) {
                    return protocol;
                }
            }
        }
        log.warn(String.format("Unknown protocol with identifier %s", identifier));
        return null;
    }

    /**
     * @param scheme Protocol scheme
     * @return Standard protocol for this scheme. This is ambigous
     */
    public static Protocol forScheme(final String scheme) {
        return ProtocolFactory.forScheme(registered, scheme);
    }

    public static Protocol forScheme(final Set<Protocol> protocols, final String scheme) {
        for(Protocol protocol : protocols) {
            for(int k = 0; k < protocol.getSchemes().length; k++) {
                if(protocol.getSchemes()[k].equals(scheme)) {
                    return protocol;
                }
            }
        }
        log.warn(String.format("Unknown scheme %s", scheme));
        return null;
    }
}