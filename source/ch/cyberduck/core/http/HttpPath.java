package ch.cyberduck.core.http;

/*
 * Copyright (c) 2002-2011 David Kocher. All rights reserved.
 *
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
 * Bug fixes, suggestions and comments should be sent to:
 * dkocher@cyberduck.ch
 */

import ch.cyberduck.core.MappingMimeTypeService;
import ch.cyberduck.core.Path;
import ch.cyberduck.core.Session;
import ch.cyberduck.core.local.Local;
import ch.cyberduck.core.threading.ActionOperationBatcher;
import ch.cyberduck.core.threading.ActionOperationBatcherFactory;
import ch.cyberduck.core.threading.BackgroundException;
import ch.cyberduck.core.threading.NamedThreadFactory;

import org.apache.http.message.BasicHeader;
import org.apache.http.protocol.HTTP;
import org.apache.log4j.Logger;

import java.io.OutputStream;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ThreadFactory;

/**
 * @version $Id$
 */
public abstract class HttpPath extends Path {
    private static Logger log = Logger.getLogger(HttpPath.class);

    protected <T> HttpPath(Session s, T dict) {
        super(s, dict);
    }

    protected HttpPath(Path parent, String name, int type) {
        super(parent, name, type);
    }

    protected HttpPath(Session session, String path, int type) {
        super(session, path, type);
    }

    protected HttpPath(Path parent, final Local local) {
        super(parent, local);
    }

    private abstract class FutureHttpResponse<T> implements Runnable {

        BackgroundException exception;
        T response;

        public BackgroundException getException() {
            return exception;
        }

        public T getResponse() {
            return response;
        }
    }

    private final ThreadFactory factory = new NamedThreadFactory("http");

    /**
     * @param command Callable writing entity to stream and returning checksum
     * @param <T>     Type of returned checksum
     * @return Outputstream to write entity into.
     */
    protected <T> ResponseOutputStream<T> write(final DelayedHttpEntityCallable<T> command)
            throws BackgroundException {
        /**
         * Signal on enter streaming
         */
        final CountDownLatch entry = new CountDownLatch(1);
        final CountDownLatch exit = new CountDownLatch(1);

        try {
            final DelayedHttpEntity entity = new DelayedHttpEntity(entry) {
                @Override
                public long getContentLength() {
                    return command.getContentLength();
                }
            };
            final String type = new MappingMimeTypeService().getMime(this.getName());
            entity.setContentType(new BasicHeader(HTTP.CONTENT_TYPE, type));
            final FutureHttpResponse<T> target = new FutureHttpResponse<T>() {
                @Override
                public void run() {
                    // Need batcher for logging messages up to the interface
                    final ActionOperationBatcher autorelease = ActionOperationBatcherFactory.get();
                    try {
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
                        autorelease.operate();
                    }
                }
            };
            final Thread t = factory.newThread(target);
            t.start();
            // Wait for output stream to become available
            entry.await();
            if(null != target.getException()) {
                throw target.getException();
            }
            final OutputStream stream = entity.getStream();
            return new ResponseOutputStream<T>(stream) {
                /**
                 * Only available after this stream is closed.
                 * @return Response from server for upload
                 */
                @Override
                public T getResponse() throws BackgroundException {
                    try {
                        // Block the calling thread until after the full response from the server
                        // has been consumed.
                        exit.await();
                    }
                    catch(InterruptedException e) {
                        throw new BackgroundException(e);
                    }
                    if(null != target.getException()) {
                        throw target.getException();
                    }
                    return target.getResponse();
                }
            };
        }
        catch(InterruptedException e) {
            log.error("Error waiting for output stream:" + e.getMessage());
            throw new BackgroundException(e);
        }
    }

    @Override
    public String toURL() {
        return this.toURL(false);
    }
}
