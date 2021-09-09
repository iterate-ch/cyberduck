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
import ch.cyberduck.core.Local;
import ch.cyberduck.core.Path;
import ch.cyberduck.core.PathAttributes;
import ch.cyberduck.core.Session;
import ch.cyberduck.core.exception.AccessDeniedException;
import ch.cyberduck.core.exception.BackgroundException;
import ch.cyberduck.core.features.Read;
import ch.cyberduck.core.local.DefaultLocalTouchFeature;
import ch.cyberduck.core.local.TemporaryFileServiceFactory;
import ch.cyberduck.core.shared.DefaultPathHomeFeature;
import ch.cyberduck.core.shared.DelegatingHomeFeature;
import ch.cyberduck.core.transfer.TransferStatus;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.concurrent.ConcurrentException;
import org.apache.commons.lang3.concurrent.LazyInitializer;
import org.apache.log4j.Logger;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Set;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class RemoteProfilesFinder implements ProfilesFinder {
    private static final Logger log = Logger.getLogger(RemoteProfilesFinder.class);

    private final Session<?> session;

    public RemoteProfilesFinder(final Session<?> session) {
        this.session = session;
    }

    @Override
    public Set<ProfileDescription> find(final Visitor visitor) throws AccessDeniedException {
        try {
            if(log.isInfoEnabled()) {
                log.info(String.format("Fetch profiles from %s", session.getHost()));
            }
            final ProfileFilter filter = new ProfileFilter();
            final AttributedList<Path> list = session.getFeature(ListService.class).list(new DelegatingHomeFeature(
                new DefaultPathHomeFeature(session.getHost())).find(), new DisabledListProgressListener());
            return list.filter(filter).toStream().map(file -> visitor.visit(new RemoteProfileDescription(file,
                new LazyInitializer<Local>() {
                    @Override
                    protected Local initialize() throws ConcurrentException {
                        try {
                            final Read read = session.getFeature(Read.class);
                            if(log.isInfoEnabled()) {
                                log.info(String.format("Download profile %s", file));
                            }
                            final InputStream in = read.read(file.withAttributes(new PathAttributes(file.attributes())
                                // Read latest version
                                .withVersionId(null)), new TransferStatus(), new DisabledConnectionCallback());
                            final Local temp = TemporaryFileServiceFactory.get().create(file.getName());
                            new DefaultLocalTouchFeature().touch(temp);
                            final OutputStream out = temp.getOutputStream(false);
                            try {
                                IOUtils.copy(in, out);
                            }
                            finally {
                                in.close();
                                out.close();
                            }
                            return temp;
                        }
                        catch(BackgroundException | IOException e) {
                            throw new ConcurrentException(e);
                        }
                    }
                }
            ))).collect(Collectors.toSet());
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
