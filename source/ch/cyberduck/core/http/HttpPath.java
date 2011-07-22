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

import ch.cyberduck.core.Local;
import ch.cyberduck.core.Path;
import ch.cyberduck.core.threading.ActionOperationBatcher;
import ch.cyberduck.core.threading.ActionOperationBatcherFactory;

import org.apache.log4j.Logger;

import java.io.IOException;
import java.io.OutputStream;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ThreadFactory;

/**
 * @version $Id:$
 */
public abstract class HttpPath extends Path {
    private static Logger log = Logger.getLogger(HttpPath.class);

    protected <T> HttpPath(T dict) {
        super(dict);
    }

    protected HttpPath(String parent, String name, int type) {
        super(parent, name, type);
    }

    protected HttpPath(String path, int type) {
        super(path, type);
    }

    protected HttpPath(String parent, final Local local) {
        super(parent, local);
    }

    private abstract class FutureHttpResponse<T> implements Runnable {

        IOException exception;

        T response;

        public IOException getException() {

            return exception;
        }

        public T getResponse() {
            return response;
        }
    }

    private final ThreadFactory factory = new ThreadFactory() {
        private int threadCount = 1;

        public Thread newThread(Runnable r) {
            Thread thread = new Thread(r);
            thread.setName("http-" + threadCount++);
            return thread;
        }
    };

    /**
     * @param command
     * @param <T>
     * @return
     * @throws java.io.IOException
     */
    protected <T> ResponseOutputStream<T> write(final DelayedHttpEntityCallable<T> command) throws IOException {
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
            final FutureHttpResponse<T> target = new FutureHttpResponse<T>() {
                public void run() {
                    // Need batcher for logging messages up to the interface
                    final ActionOperationBatcher autorelease = ActionOperationBatcherFactory.get();
                    try {
                        response = command.call(entity);
                    }
                    catch(IOException e) {
                        entry.countDown();
                        exception = e;
                    }
                    finally {
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
                 * @return
                 */
                @Override
                public T getResponse() throws IOException {
                    try {
                        // Block the calling thread until after the full response from the server
                        // has been consumed.
                        exit.await();
                    }
                    catch(InterruptedException e) {
                        throw new IOException(e.getMessage());
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
            throw new IOException(e.getMessage());
        }
    }

    @Override
    public String toURL() {
        return this.toURL(false);
    }
}
