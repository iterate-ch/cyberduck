package ch.cyberduck.core.http;

/*
 * Copyright (c) 2002-2013 David Kocher. All rights reserved.
 * http://cyberduck.ch/
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
 *
 * Bug fixes, suggestions and comments should be sent to feedback@cyberduck.ch
 */

import ch.cyberduck.core.ConnectionCallback;
import ch.cyberduck.core.Path;
import ch.cyberduck.core.Session;
import ch.cyberduck.core.exception.BackgroundException;
import ch.cyberduck.core.exception.ConnectionCanceledException;
import ch.cyberduck.core.features.AttributesFinder;
import ch.cyberduck.core.features.Find;
import ch.cyberduck.core.shared.AppendWriteFeature;
import ch.cyberduck.core.threading.NamedThreadFactory;
import ch.cyberduck.core.transfer.TransferStatus;
import ch.cyberduck.core.worker.DefaultExceptionMappingService;

import org.apache.commons.lang3.StringUtils;
import org.apache.http.message.BasicHeader;
import org.apache.http.protocol.HTTP;
import org.apache.log4j.Logger;

import java.io.IOException;
import java.io.OutputStream;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ThreadFactory;

public abstract class AbstractHttpWriteFeature<T> extends AppendWriteFeature<T> implements HttpWriteFeature<T> {
    private static final Logger log = Logger.getLogger(AbstractHttpWriteFeature.class);

    private abstract class FutureHttpResponse implements Runnable {
        Exception exception;
        T response;

        public Exception getException() {
            return exception;
        }

        public T getResponse() {
            return response;
        }
    }

    protected AbstractHttpWriteFeature(final Session<?> session) {
        super(session);
    }

    public AbstractHttpWriteFeature(final Find finder, final AttributesFinder attributes) {
        super(finder, attributes);
    }

    /**
     * @param command Callable writing entity to stream and returning checksum
     * @return Outputstream to write entity into.
     */
    @Override
    public HttpResponseOutputStream<T> write(final Path file, final TransferStatus status,
                                             final DelayedHttpEntityCallable<T> command) throws BackgroundException {
        // Signal on enter streaming
        final CountDownLatch entry = new CountDownLatch(1);
        final CountDownLatch exit = new CountDownLatch(1);

        try {
            final DelayedHttpEntity entity = new DelayedHttpEntity(entry) {
                @Override
                public long getContentLength() {
                    return command.getContentLength();
                }
            };
            if(StringUtils.isNotBlank(status.getMime())) {
                entity.setContentType(new BasicHeader(HTTP.CONTENT_TYPE, status.getMime()));
            }
            final FutureHttpResponse target = new FutureHttpResponse() {
                @Override
                public void run() {
                    try {
                        if(status.isCanceled()) {
                            throw new ConnectionCanceledException();
                        }
                        response = command.call(entity);
                    }
                    catch(BackgroundException e) {
                        exception = e;
                    }
                    finally {
                        // For zero byte files #writeTo is never called and the entry latch not triggered
                        entry.countDown();
                        // Continue reading the response
                        exit.countDown();
                    }
                }
            };
            final ThreadFactory factory
                    = new NamedThreadFactory(String.format("http-%s", file.getName()));
            final Thread t = factory.newThread(target);
            t.start();
            // Wait for output stream to become available
            entry.await();
            if(null != target.getException()) {
                if(target.getException() instanceof BackgroundException) {
                    throw (BackgroundException) target.getException();
                }
                throw new DefaultExceptionMappingService().map(target.getException());
            }
            final OutputStream stream = entity.getStream();
            return new HttpResponseOutputStream<T>(stream) {
                @Override
                public void flush() throws IOException {
                    stream.flush();
                }

                /**
                 * Only available after this stream is closed.
                 * @return Response from server for upload
                 */
                @Override
                public T getStatus() throws BackgroundException {
                    try {
                        if(status.isCanceled()) {
                            throw new ConnectionCanceledException();
                        }
                        // Block the calling thread until after the full response from the server
                        // has been consumed.
                        exit.await();
                    }
                    catch(InterruptedException e) {
                        throw new DefaultExceptionMappingService().map(e);
                    }
                    if(null != target.getException()) {
                        if(target.getException() instanceof BackgroundException) {
                            throw (BackgroundException) target.getException();
                        }
                        throw new DefaultExceptionMappingService().map(target.getException());
                    }
                    return target.getResponse();
                }
            };
        }
        catch(InterruptedException e) {
            log.warn(String.format("Error waiting for output stream for %s", file));
            throw new DefaultExceptionMappingService().map(e);
        }
    }

    @Override
    public abstract HttpResponseOutputStream<T> write(Path file, TransferStatus status, final ConnectionCallback callback) throws BackgroundException;
}
