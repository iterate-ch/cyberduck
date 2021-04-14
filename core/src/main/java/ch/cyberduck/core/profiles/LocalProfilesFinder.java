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
import ch.cyberduck.core.Profile;
import ch.cyberduck.core.ProtocolFactory;
import ch.cyberduck.core.exception.AccessDeniedException;
import ch.cyberduck.core.exception.ChecksumException;
import ch.cyberduck.core.io.Checksum;
import ch.cyberduck.core.io.ChecksumComputeFactory;
import ch.cyberduck.core.io.HashAlgorithm;
import ch.cyberduck.core.preferences.PreferencesFactory;
import ch.cyberduck.core.preferences.SupportDirectoryFinderFactory;
import ch.cyberduck.core.serializer.Reader;
import ch.cyberduck.core.serializer.impl.dd.ProfilePlistReader;
import ch.cyberduck.core.transfer.TransferStatus;

import org.apache.log4j.Logger;

import java.util.function.Supplier;
import java.util.regex.Pattern;
import java.util.stream.Stream;

public class LocalProfilesFinder implements ProfilesFinder {
    private static final Logger log = Logger.getLogger(LocalProfilesFinder.class);

    private final Reader<Profile> reader;
    private final Local directory;

    public LocalProfilesFinder() {
        this(new ProfilePlistReader(ProtocolFactory.get()), LocalFactory.get(SupportDirectoryFinderFactory.get().find(),
            PreferencesFactory.get().getProperty("profiles.folder.name")));
    }

    public LocalProfilesFinder(final Local directory) {
        this(new ProfilePlistReader(ProtocolFactory.get()), directory);
    }

    public LocalProfilesFinder(final Reader<Profile> reader, final Local directory) {
        this.reader = reader;
        this.directory = directory;
    }

    @Override
    public Stream<ProfileDescription> find() throws AccessDeniedException {
        if(directory.exists()) {
            if(log.isDebugEnabled()) {
                log.debug(String.format("Load profiles from %s", directory));
            }
            return directory.list().filter(new ProfileFilter()).toList().stream().map(file -> new LocalProfileDescription(reader, file));
        }
        return Stream.empty();
    }

    private static final class LocalProfileDescription extends ProfileDescription {
        private final Local file;

        public LocalProfileDescription(final Reader<Profile> reader, final Local file) {
            super(file.getName(), Checksum.NONE, new Supplier<Profile>() {
                    @Override
                    public Profile get() {
                        try {
                            return reader.read(file);
                        }
                        catch(AccessDeniedException e) {
                            return null;
                        }
                    }
                }
            );
            this.file = file;
        }

        @Override
        public Checksum getChecksum() {
            try {
                // Calculate checksum lazily
                return ChecksumComputeFactory.get(HashAlgorithm.md5).compute(file.getInputStream(), new TransferStatus());
            }
            catch(ChecksumException | AccessDeniedException e) {
                return Checksum.NONE;
            }
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
