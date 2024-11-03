package ch.cyberduck.core;

/*
 * Copyright (c) 2002-2017 iterate GmbH. All rights reserved.
 * https://cyberduck.io/
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
 */

import ch.cyberduck.core.exception.AccessDeniedException;
import ch.cyberduck.core.exception.BackgroundException;
import ch.cyberduck.core.local.DefaultLocalDirectoryFeature;
import ch.cyberduck.core.preferences.ApplicationResourcesFinderFactory;
import ch.cyberduck.core.preferences.Preferences;
import ch.cyberduck.core.preferences.PreferencesFactory;
import ch.cyberduck.core.preferences.SupportDirectoryFinderFactory;
import ch.cyberduck.core.profiles.LocalProfilesFinder;
import ch.cyberduck.core.profiles.ProfileDescription;
import ch.cyberduck.core.profiles.ProfilesFinder;
import ch.cyberduck.core.serializer.impl.dd.ProfilePlistReader;

import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.function.Predicate;
import java.util.stream.Collectors;

public final class ProtocolFactory {
    private static final Logger log = LogManager.getLogger(ProtocolFactory.class);

    private static final ProtocolFactory global = new ProtocolFactory();

    private final Preferences preferences = PreferencesFactory.get();

    public static final Predicate<Protocol> DEFAULT_PROTOCOL_PREDICATE
            = protocol -> !protocol.isEnabled() && !protocol.isBundled();

    public static final Predicate<Protocol> BUNDLED_PROFILE_PREDICATE
            = protocol -> protocol.isEnabled() && protocol.isBundled();

    public static ProtocolFactory get() {
        return global;
    }

    private final Set<Protocol> registered;

    private final Local bundle;
    private final Local profiles;

    public ProtocolFactory() {
        this(new LinkedHashSet<>());
    }

    /**
     * @param registered Default registered set of protocols
     */
    public ProtocolFactory(final Set<Protocol> registered) {
        this(registered,
                LocalFactory.get(ApplicationResourcesFinderFactory.get().find(),
                        PreferencesFactory.get().getProperty("profiles.folder.name")),
                LocalFactory.get(SupportDirectoryFinderFactory.get().find(),
                        PreferencesFactory.get().getProperty("profiles.folder.name")));
    }

    /**
     * @param registered Default registered set of protocols
     * @param bundle     Directory with default profiles in application installation directory
     * @param profiles   Third party profiles directory in application support
     */
    public ProtocolFactory(final Set<Protocol> registered, final Local bundle, final Local profiles) {
        this.registered = new HashSet<>(registered);
        this.bundle = bundle;
        this.profiles = profiles;
    }

    public void register(Protocol... protocols) {
        if(log.isInfoEnabled()) {
            log.info("Register protocols {}", Arrays.toString(protocols));
        }
        // Order determines list in connection dropdown
        Collections.addAll(registered, protocols);
    }

    /**
     * Load profiles embedded in bundles and installed in the application support directory.
     */
    public void load() {
        // Return default registered protocol specification as parent but not other profile
        this.load(new LocalProfilesFinder(this, bundle, DEFAULT_PROTOCOL_PREDICATE));
        // Load thirdparty protocols
        this.load(new LocalProfilesFinder(this, profiles, BUNDLED_PROFILE_PREDICATE));
        if(registered.isEmpty()) {
            log.error("No connection profiles in {} or {}", bundle, profiles);
        }
    }

    /**
     * Load all profiles found
     *
     * @param finder Finder to locate profiles
     */
    public void load(final ProfilesFinder finder) {
        try {
            for(ProfileDescription description : finder.find()) {
                final Optional<Profile> profile = description.getProfile();
                profile.ifPresent(registered::add);
            }
        }
        catch(BackgroundException e) {
            log.warn("Failure {} reading profiles from {}", finder, e);
        }
    }

    /**
     * Register profile and copy to application support directory
     *
     * @param file Connection profile to install
     * @return Installation location in application support directory
     */
    public Local register(final Local file) {
        try {
            final Profile profile = new ProfilePlistReader(this, BUNDLED_PROFILE_PREDICATE).read(file);
            if(null == profile) {
                log.error("Attempt to register unknown protocol");
                return null;
            }
            if(log.isInfoEnabled()) {
                log.info("Register profile {}", profile);
            }
            registered.add(profile);
            preferences.setProperty(StringUtils.lowerCase(String.format("profiles.%s.%s.enabled",
                    profile.getProtocol().getIdentifier(), profile.getProvider())), true);
            if(!profiles.exists()) {
                new DefaultLocalDirectoryFeature().mkdir(profiles);
            }
            if(log.isDebugEnabled()) {
                log.debug("Save profile {} to {}", profile, profiles);
            }
            if(!file.isChild(profiles)) {
                final Local target = LocalFactory.get(profiles, file.getName());
                file.copy(target);
                return target;
            }
            return file;
        }
        catch(AccessDeniedException e) {
            log.error("Failure {} reading profile {}", e, file);
            return null;
        }
    }

    /**
     * Remove connection profile
     *
     * @param profile Connection profile
     */
    public void unregister(final Profile profile) {
        if(registered.remove(profile)) {
            preferences.setProperty(StringUtils.lowerCase(String.format("profiles.%s.%s.enabled",
                    profile.getProtocol().getIdentifier(), profile.getProvider())), false);
        }
        else {
            log.warn("Failure removing protocol {}", profile);
        }
    }

    /**
     * @return List of enabled protocols
     */
    public List<Protocol> find() {
        return this.find(Protocol::isEnabled);
    }

    /**
     * @param search Search filter for all registered protocols
     * @return List of registered protocols matching search criteria.
     */
    public List<Protocol> find(final Predicate<Protocol> search) {
        return registered.stream().filter(search).sorted().collect(Collectors.toList());
    }

    /**
     * @param identifier Serialized protocol reference or scheme
     * @return Matching protocol or null if no match
     */
    public Protocol forName(final String identifier) {
        return this.forName(identifier, null);
    }

    /**
     * @param identifier Serialized protocol reference or scheme
     * @param provider   Custom inherited protocol definition
     * @return Matching protocol or null if no match
     */
    public Protocol forName(final String identifier, final String provider) {
        return this.forName(this.find(), identifier, provider);
    }

    /**
     * @param enabled    List of protocols
     * @param identifier Serialized protocol reference or scheme
     * @param provider   Custom inherited protocol definition
     * @return Matching protocol or null if no match
     */
    public Protocol forName(final List<Protocol> enabled, final String identifier, final String provider) {
        int state = 0; // Acts as findFirst-guard
        Protocol match = null;
        final Iterable<Protocol> protocols = enabled.stream().sorted(new DeprecatedProtocolComparator())::iterator;
        for(final Protocol protocol : protocols) {
            if(String.valueOf(protocol.hashCode()).equals(identifier)) {
                // Exact match with hash code
                return protocol;
            }
            /*
            All following cases are required for fallback/broader searches.
            There may be other, better matches in the stream
            Each state represents findFirst stream semantic, and overwrites the result of lower matches
             */
            else if(state < 4 && StringUtils.equals(protocol.getIdentifier(), identifier)
                    && StringUtils.equals(protocol.getProvider(), provider)) {
                // Matching vendor string for third party profiles
                match = protocol;
                state = 4;
            }
            else if(state < 3 && StringUtils.equals(protocol.getProvider(), identifier)) {
                // Matching vendor string usage in CLI
                match = protocol;
                state = 3;
            }
            else if(state < 2 && StringUtils.equals(
                    String.format("%s-%s", protocol.getIdentifier(), protocol.getProvider()), identifier)) {
                // Fallback for bug in 6.1
                match = protocol;
                state = 2;
            }
            else if(state < 1 && protocol.isBundled() && StringUtils.equals(protocol.getIdentifier(), identifier)) {
                // Matching bundled first with identifier match
                match = protocol;
                state = 1;
            }
            else if(null == match && StringUtils.equals(protocol.getType().name(), identifier)) {
                // Fallback used in forScheme
                match = protocol;
            }
        }
        if(state == 0) {
            // Matching scheme with fallback to generic protocol type
            match = this.forScheme(enabled, identifier, match);
        }
        if(null == match) {
            if(enabled.isEmpty()) {
                log.error("List of registered protocols in {} is empty", this);
            }
            log.warn("Missing registered protocol for identifier {}", identifier);
        }
        return match;
    }

    /**
     * @param identifier Serialized protocol reference or scheme
     * @return Matching protocol or default if no match
     */
    public Protocol forNameOrDefault(final String identifier) {
        return forNameOrDefault(identifier, null);
    }

    /**
     * @param identifier Serialized protocol reference or scheme
     * @param provider   Custom inherited protocol definition
     * @return Matching protocol or default if no match
     */
    public Protocol forNameOrDefault(final String identifier, final String provider) {
        return forNameOrDefault(this.find(), identifier, provider);
    }

    /**
     * @param enabled    List of protocols
     * @param identifier Serialized protocol reference or scheme
     * @param provider   Custom inherited protocol definition
     * @return Matching protocol or default if no match
     */
    public Protocol forNameOrDefault(final List<Protocol> enabled, final String identifier, final String provider) {
        final String defaultIdentifier = preferences.getDefault("connection.protocol.default");
        final Protocol match = forName(enabled, identifier, provider);
        if(null != match || Objects.equals(defaultIdentifier, identifier)) {
            return match;
        }
        return forName(enabled, defaultIdentifier, null);
    }

    public Protocol forType(final Protocol.Type type) {
        final List<Protocol> enabled = this.find();
        return this.forType(enabled, type);
    }

    public Protocol forType(final List<Protocol> enabled, final Protocol.Type type) {
        return enabled.stream().sorted(new DeprecatedProtocolComparator()).filter(protocol -> protocol.getType().equals(type)).findFirst().orElse(null);
    }

    public Protocol forScheme(final Scheme scheme) {
        return this.forScheme(scheme.name(), null);
    }

    public Protocol forScheme(final String scheme, final Protocol fallback) {
        return this.forScheme(this.find(), scheme, fallback);
    }

    private Protocol forScheme(final List<Protocol> enabled, final String scheme, final Protocol fallback) {
        final String filter;
        switch(scheme) {
            case "http":
                filter = Scheme.dav.name();
                break;
            case "https":
                filter = Scheme.davs.name();
                break;
            default:
                filter = scheme;
                break;
        }
        return enabled.stream().sorted(new DeprecatedProtocolComparator()).filter(protocol -> Arrays.asList(protocol.getSchemes()).contains(filter)).findFirst().orElse(
                enabled.stream().sorted(new DeprecatedProtocolComparator()).filter(protocol -> Arrays.asList(protocol.getSchemes()).contains(scheme)).findFirst().orElse(fallback)
        );
    }

    private static final class DeprecatedProtocolComparator implements Comparator<Protocol> {
        @Override
        public int compare(final Protocol o1, final Protocol o2) {
            return Boolean.compare(o1.isDeprecated(), o2.isDeprecated());
        }
    }


    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("ProtocolFactory{");
        sb.append("registered=").append(registered);
        sb.append(", bundle=").append(bundle);
        sb.append(", profiles=").append(profiles);
        sb.append('}');
        return sb.toString();
    }
}
