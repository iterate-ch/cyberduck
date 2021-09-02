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
import ch.cyberduck.core.LocalFactory;
import ch.cyberduck.core.Path;
import ch.cyberduck.core.exception.AccessDeniedException;
import ch.cyberduck.core.exception.ChecksumException;
import ch.cyberduck.core.io.Checksum;
import ch.cyberduck.core.io.ChecksumComputeFactory;
import ch.cyberduck.core.io.HashAlgorithm;
import ch.cyberduck.core.preferences.PreferencesFactory;
import ch.cyberduck.core.preferences.SupportDirectoryFinderFactory;
import ch.cyberduck.core.transfer.TransferStatus;

import org.apache.commons.lang3.concurrent.ConcurrentException;
import org.apache.commons.lang3.concurrent.LazyInitializer;
import org.apache.log4j.Logger;

import java.util.Collections;
import java.util.List;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class LocalProfilesFinder implements ProfilesFinder {
    private static final Logger log = Logger.getLogger(LocalProfilesFinder.class);

    private final Local directory;

    public LocalProfilesFinder() {
        this(LocalFactory.get(SupportDirectoryFinderFactory.get().find(),
            PreferencesFactory.get().getProperty("profiles.folder.name")));
    }

    public LocalProfilesFinder(final Local directory) {
        this.directory = directory;
    }

    @Override
    public List<ProfileDescription> find(final Visitor visitor) throws AccessDeniedException {
        if(directory.exists()) {
            if(log.isDebugEnabled()) {
                log.debug(String.format("Load profiles from %s", directory));
            }
            return directory.list().filter(new ProfileFilter()).toList().stream()
                .map(file -> visitor.visit(new LocalProfileDescription(file))).collect(Collectors.toList());
        }
        return Collections.emptyList();
    }

    private static final class LocalProfileDescription extends ProfileDescription {
        private final Local file;

        public LocalProfileDescription(final Local file) {
            super(
                new LazyInitializer<Checksum>() {
                    @Override
                    protected Checksum initialize() throws ConcurrentException {
                        try {
                            // Calculate checksum lazily
                            return ChecksumComputeFactory.get(HashAlgorithm.md5).compute(file.getInputStream(), new TransferStatus());
                        }
                        catch(ChecksumException | AccessDeniedException e) {
                            throw new ConcurrentException(e);
                        }
                    }
                }, new LazyInitializer<Local>() {
                    @Override
                    protected Local initialize() throws ConcurrentException {
                        return file;
                    }
                }
            );
            this.file = file;
        }

        @Override
        public String toString() {
            final StringBuilder sb = new StringBuilder("LocalProfileDescription{");
            sb.append("file=").append(file);
            sb.append('}');
            return sb.toString();
        }
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
