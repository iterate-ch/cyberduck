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
import ch.cyberduck.core.Profile;
import ch.cyberduck.core.ProfileReaderFactory;
import ch.cyberduck.core.exception.AccessDeniedException;

import org.apache.log4j.Logger;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.regex.Pattern;

public class LocalProfilesFinder implements ProfilesFinder {
    private static final Logger log = Logger.getLogger(LocalProfilesFinder.class);

    private final Local directory;

    public LocalProfilesFinder(final Local directory) {
        this.directory = directory;
    }

    @Override
    public Set<Profile> find() throws AccessDeniedException {
        if(directory.exists()) {
            final Set<Profile> registered = new HashSet<>();
            if(log.isDebugEnabled()) {
                log.debug(String.format("Load profiles from %s", directory));
            }
            for(Local f : directory.list().filter(new ProfileFilter())) {
                try {
                    final Profile profile = ProfileReaderFactory.get().read(f);
                    if(log.isInfoEnabled()) {
                        log.info(String.format("Adding bundled protocol %s", profile));
                    }
                    // Replace previous possibly disable protocol in Preferences
                    registered.add(profile);
                }
                catch(AccessDeniedException e) {
                    log.error(String.format("Failure %s reading profile from %s", f, e));
                }
            }
            return registered;
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
