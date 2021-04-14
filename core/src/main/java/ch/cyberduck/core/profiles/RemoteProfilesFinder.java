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

import ch.cyberduck.core.*;
import ch.cyberduck.core.exception.AccessDeniedException;
import ch.cyberduck.core.exception.BackgroundException;
import ch.cyberduck.core.exception.HostParserException;
import ch.cyberduck.core.features.Read;
import ch.cyberduck.core.preferences.PreferencesFactory;
import ch.cyberduck.core.serializer.Reader;
import ch.cyberduck.core.serializer.impl.dd.ProfilePlistReader;
import ch.cyberduck.core.ssl.DefaultTrustManagerHostnameCallback;
import ch.cyberduck.core.ssl.KeychainX509KeyManager;
import ch.cyberduck.core.ssl.KeychainX509TrustManager;
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

    private final Reader<Profile> reader;
    private final Host server;

    public RemoteProfilesFinder() throws HostParserException {
        this(new ProfilePlistReader(ProtocolFactory.get()), HostParser.parse(PreferencesFactory.get().getProperty(
            "profiles.discovery.updater.url"
        )));
    }

    public RemoteProfilesFinder(final Reader<Profile> reader, final Host server) {
        this.reader = reader;
        this.server = server;
    }

    @Override
    public Stream<ProfileDescription> find() throws AccessDeniedException {
        try {
            if(log.isInfoEnabled()) {
                log.info(String.format("Fetch profiles from %s", server));
            }
            final AnonymousConnectionService connection = new AnonymousConnectionService();
            final Session<?> session = SessionFactory.create(server,
                new KeychainX509TrustManager(new DisabledCertificateTrustCallback(), new DefaultTrustManagerHostnameCallback(server), CertificateStoreFactory.get()),
                new KeychainX509KeyManager(new DisabledCertificateIdentityCallback(), server, CertificateStoreFactory.get()));
            try {
                connection.connect(session, new DisabledCancelCallback());
                final ProfileFilter filter = new ProfileFilter();
                final AttributedList<Path> list = session.getFeature(ListService.class).list(new Path(server.getDefaultPath(), EnumSet.of(Path.Type.directory)), new DisabledListProgressListener());
                return list.filter(filter).toStream().map(file -> new PathProfileDescription(file,
                    new Supplier<Profile>() {
                        @Override
                        public Profile get() {
                            final Read read = session.getFeature(Read.class);
                            try {
                                final InputStream in = read.read(file, new TransferStatus(), new DisabledConnectionCallback());
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
            finally {
                try {
                    connection.close(session);
                }
                catch(BackgroundException e) {
                    log.warn(String.format("Ignore failure %s closing connection", e));
                }
            }
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
