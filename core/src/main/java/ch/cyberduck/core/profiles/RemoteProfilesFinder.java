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
import ch.cyberduck.core.ssl.DefaultTrustManagerHostnameCallback;
import ch.cyberduck.core.ssl.KeychainX509KeyManager;
import ch.cyberduck.core.ssl.KeychainX509TrustManager;
import ch.cyberduck.core.threading.CancelCallback;
import ch.cyberduck.core.transfer.TransferStatus;

import org.apache.log4j.Logger;

import java.io.IOException;
import java.io.InputStream;
import java.util.Collections;
import java.util.EnumSet;
import java.util.HashSet;
import java.util.Set;
import java.util.regex.Pattern;

public class RemoteProfilesFinder implements ProfilesFinder {
    private static final Logger log = Logger.getLogger(RemoteProfilesFinder.class);
    private final Host server;

    public RemoteProfilesFinder() throws HostParserException {
        this(new HostParser(new ProtocolFactory(Collections.singleton(ProtocolFactory.get()
            .forName(Scheme.davs.name())))).get(PreferencesFactory.get().getProperty("profiles.url")));
    }

    public RemoteProfilesFinder(final Host server) {
        this.server = server;
    }

    @Override
    public Set<Profile> find() throws AccessDeniedException {
        final CancelCallback cancel = new DisabledCancelCallback();
        final ProgressListener listener = new DisabledProgressListener();
        try {
            if(log.isInfoEnabled()) {
                log.info(String.format("Fetch trial license from %s", server));
            }
            final AnonymousConnectionService connection = new AnonymousConnectionService();
            final Session<?> session = SessionFactory.create(server,
                new KeychainX509TrustManager(new DisabledCertificateTrustCallback(), new DefaultTrustManagerHostnameCallback(server), CertificateStoreFactory.get()),
                new KeychainX509KeyManager(new DisabledCertificateIdentityCallback(), server, CertificateStoreFactory.get()));
            try {
                connection.connect(session, cancel);
                final Reader<Profile> reader = ProfileReaderFactory.get();
                final Set<Profile> registered = new HashSet<>();
                session.getFeature(ListService.class).list(new Path(server.getDefaultPath(), EnumSet.of(Path.Type.directory)), new IndexedListProgressListener() {

                    private final ProfileFilter filter = new ProfileFilter();

                    @Override
                    public void visit(final AttributedList<Path> list, final int index, final Path file) {
                        if(filter.accept(file)) {
                            final Read read = session.getFeature(Read.class);
                            try {
                                final InputStream in = read.read(file, new TransferStatus(), new DisabledConnectionCallback());
                                registered.add(reader.read(in));
                                in.close();
                            }
                            catch(BackgroundException | IOException e) {
                                log.warn(String.format("Ignore failure %s reading profile %s", e, file));
                            }
                        }
                    }

                    @Override
                    public void message(final String message) {
                        listener.message(message);
                    }
                });
                return registered;
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
