package ch.cyberduck.core.shared;

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

import ch.cyberduck.core.ConnectionCallback;
import ch.cyberduck.core.DefaultIOExceptionMappingService;
import ch.cyberduck.core.Path;
import ch.cyberduck.core.ProgressListener;
import ch.cyberduck.core.Session;
import ch.cyberduck.core.exception.AccessDeniedException;
import ch.cyberduck.core.exception.BackgroundException;
import ch.cyberduck.core.exception.InteroperabilityException;
import ch.cyberduck.core.exception.RetriableAccessDeniedException;
import ch.cyberduck.core.features.MultipartWrite;
import ch.cyberduck.core.features.Touch;
import ch.cyberduck.core.features.Write;
import ch.cyberduck.core.io.BufferInputStream;
import ch.cyberduck.core.io.BufferOutputStream;
import ch.cyberduck.core.io.FileBuffer;
import ch.cyberduck.core.io.StatusOutputStream;
import ch.cyberduck.core.io.VoidStatusOutputStream;
import ch.cyberduck.core.threading.BackgroundActionState;
import ch.cyberduck.core.threading.BackgroundExceptionCallable;
import ch.cyberduck.core.threading.DefaultRetryCallable;
import ch.cyberduck.core.transfer.TransferStatus;

import org.apache.commons.io.IOUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.util.concurrent.atomic.AtomicBoolean;

public class BufferWriteFeature implements MultipartWrite<Void> {
    private static final Logger log = LogManager.getLogger(BufferWriteFeature.class);

    private final Session<?> session;

    public BufferWriteFeature(final Session<?> session) {
        this.session = session;
    }

    @Override
    public StatusOutputStream<Void> write(final Path file, final TransferStatus status, final ConnectionCallback callback) throws AccessDeniedException {
        final FileBuffer buffer = new FileBuffer();
        return new VoidStatusOutputStream(new BufferOutputStream(buffer) {
            private final AtomicBoolean close = new AtomicBoolean();

            @Override
            public void flush() {
                //
            }

            @Override
            public void close() throws IOException {
                try {
                    if(close.get()) {
                        log.warn(String.format("Skip double close of stream %s", this));
                        return;
                    }
                    // Reset offset in transfer status because data was already streamed
                    // through StreamCopier when writing to buffer
                    final TransferStatus range = new TransferStatus(status).withLength(buffer.length()).append(false);
                    if(0L == buffer.length()) {
                        session._getFeature(Touch.class).touch(file, new TransferStatus());
                    }
                    else {
                        final StatusOutputStream out = session._getFeature(Write.class).write(file, range, callback);
                        new DefaultRetryCallable<Void>(session.getHost(), new BackgroundExceptionCallable<Void>() {
                            @Override
                            public Void call() throws BackgroundException {
                                try {
                                    IOUtils.copy(new BufferInputStream(buffer), out);
                                    out.close();
                                    log.info(String.format("Completed upload for %s with status %s", file, range));
                                    return null;
                                }
                                catch(IOException e) {
                                    throw new DefaultIOExceptionMappingService().map(e);
                                }
                            }
                        }, status) {
                            @Override
                            public boolean retry(final BackgroundException failure, final ProgressListener progress, final BackgroundActionState cancel) {
                                if(failure instanceof InteroperabilityException) {
                                    return super.retry(new RetriableAccessDeniedException(failure.getDetail(), failure), progress, cancel);
                                }
                                return super.retry(failure, progress, cancel);
                            }
                        }.call();
                    }
                    super.close();
                }
                catch(BackgroundException e) {
                    throw new IOException(e);
                }
                finally {
                    close.set(true);
                }
            }
        });
    }

    @Override
    public Append append(final Path file, final TransferStatus status) throws BackgroundException {
        return new Append(false).withStatus(status);
    }
}
