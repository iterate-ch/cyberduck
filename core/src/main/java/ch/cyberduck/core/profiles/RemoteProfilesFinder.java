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

import ch.cyberduck.core.AttributedList;
import ch.cyberduck.core.DisabledConnectionCallback;
import ch.cyberduck.core.DisabledListProgressListener;
import ch.cyberduck.core.Filter;
import ch.cyberduck.core.ListService;
import ch.cyberduck.core.Path;
import ch.cyberduck.core.PathAttributes;
import ch.cyberduck.core.Profile;
import ch.cyberduck.core.ProtocolFactory;
import ch.cyberduck.core.Session;
import ch.cyberduck.core.exception.AccessDeniedException;
import ch.cyberduck.core.exception.BackgroundException;
import ch.cyberduck.core.features.Read;
import ch.cyberduck.core.serializer.Reader;
import ch.cyberduck.core.serializer.impl.dd.ProfilePlistReader;
import ch.cyberduck.core.transfer.TransferStatus;

import org.apache.log4j.Logger;

import java.io.IOException;
import java.io.InputStream;
import java.util.EnumSet;
import java.util.concurrent.CompletionException;
import java.util.function.Supplier;
import java.util.regex.Pattern;
import java.util.stream.Stream;

public class RemoteProfilesFinder implements ProfilesFinder {
    private static final Logger log = Logger.getLogger(RemoteProfilesFinder.class);

    private final Session<?> session;
    private final Reader<Profile> reader;

    public RemoteProfilesFinder(final Session<?> session) {
        this(new ProfilePlistReader(ProtocolFactory.get()), session);
    }

    public RemoteProfilesFinder(final Reader<Profile> reader, final Session<?> session) {
        this.reader = reader;
        this.session = session;
    }

    @Override
    public Stream<ProfileDescription> find() throws AccessDeniedException {
        try {
            if(log.isInfoEnabled()) {
                log.info(String.format("Fetch profiles from %s", session.getHost()));
            }
            final ProfileFilter filter = new ProfileFilter();
            final AttributedList<Path> list = session.getFeature(ListService.class).list(new Path(
                session.getHost().getDefaultPath(), EnumSet.of(Path.Type.directory)), new DisabledListProgressListener());
            return list.filter(filter).toStream().map(file -> new PathProfileDescription(file,
                new Supplier<Profile>() {
                    @Override
                    public Profile get() {
                        try {
                            final Read read = session.getFeature(Read.class);
                            if(log.isInfoEnabled()) {
                                log.info(String.format("Download profile %s", file));
                            }
                            final InputStream in = read.read(file.withAttributes(new PathAttributes(file.attributes())
                                // Read latest version
                                .withVersionId(null)), new TransferStatus(), new DisabledConnectionCallback());
                            final Profile profile = reader.read(in);
                            in.close();
                            return profile;
                        }
                        catch(BackgroundException | IOException e) {
                            throw new CompletionException(e);
                        }
                    }
                }
            ));
        }
        catch(BackgroundException e) {
            throw new AccessDeniedException(e.getDetail(), e);
        }
    }

    private static final class PathProfileDescription extends ProfileDescription {
        private final Path file;

        public PathProfileDescription(final Path file, final Supplier<Profile> profile) {
            super(file.getName(), file.attributes().getChecksum(), profile);
            this.file = file;
        }

        @Override
        public boolean isLatest() {
            return !file.attributes().isDuplicate();
        }

        @Override
        public String toString() {
            final StringBuilder sb = new StringBuilder("PathProfileDescription{");
            sb.append("file=").append(file);
            sb.append('}');
            return sb.toString();
        }
    }

    private static final class ProfileFilter implements Filter<Path> {
        @Override
        public boolean accept(final Path file) {
            if(file.isFile()) {
                return "cyberduckprofile".equals(Path.getExtension(file.getName()));
            }
            return false;
        }

        @Override
        public Pattern toPattern() {
            return Pattern.compile(".*\\.cyberduckprofile");
        }
    }
}
