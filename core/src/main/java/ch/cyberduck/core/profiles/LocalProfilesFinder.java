package ch.cyberduck.core.profiles;

/*
 * Copyright (c) 2002-2021 iterate GmbH. All rights reserved.
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

import ch.cyberduck.core.Filter;
import ch.cyberduck.core.Local;
import ch.cyberduck.core.Path;
import ch.cyberduck.core.Protocol;
import ch.cyberduck.core.ProtocolFactory;
import ch.cyberduck.core.exception.AccessDeniedException;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.Collections;
import java.util.Set;
import java.util.function.Predicate;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class LocalProfilesFinder implements ProfilesFinder {
    private static final Logger log = LogManager.getLogger(LocalProfilesFinder.class);

    private final ProtocolFactory protocols;
    private final Local directory;
    private final Predicate<Protocol> parent;

    /**
     * @param protocols Registered protocols
     * @param directory Folder to search for connection profiles
     */
    public LocalProfilesFinder(final ProtocolFactory protocols, final Local directory) {
        this(protocols, directory, protocol -> true);
    }

    /**
     * @param protocols Registered protocols
     * @param directory Folder to search for connection profiles
     * @param parent    Filter to apply for parent protocol reference in registered protocols
     */
    public LocalProfilesFinder(final ProtocolFactory protocols, final Local directory, final Predicate<Protocol> parent) {
        this.protocols = protocols;
        this.directory = directory;
        this.parent = parent;
    }

    @Override
    public Set<ProfileDescription> find(final Visitor visitor) throws AccessDeniedException {
        if(directory.exists()) {
            log.debug("Load profiles from {}", directory);
            return directory.list().filter(new ProfileFilter()).toList().stream()
                    .map(file -> visitor.visit(new LocalProfileDescription(protocols, parent, file))).collect(Collectors.toSet());
        }
        return Collections.emptySet();
    }

    private static final class ProfileFilter implements Filter<Local> {
        @Override
        public boolean accept(final Local file) {
            return "cyberduckprofile".equals(Path.getExtension(file.getName()));
        }

        @Override
        public Pattern toPattern() {
            return Pattern.compile(".*\\.cyberduckprofile");
        }
    }
}
