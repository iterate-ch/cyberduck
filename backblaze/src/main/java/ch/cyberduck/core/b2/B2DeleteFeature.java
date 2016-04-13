package ch.cyberduck.core.b2;

/*
 * Copyright (c) 2002-2016 iterate GmbH. All rights reserved.
 * https://cyberduck.io/
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
 */

import ch.cyberduck.core.DefaultIOExceptionMappingService;
import ch.cyberduck.core.LoginCallback;
import ch.cyberduck.core.Path;
import ch.cyberduck.core.PathContainerService;
import ch.cyberduck.core.exception.BackgroundException;
import ch.cyberduck.core.exception.ConnectionCanceledException;
import ch.cyberduck.core.features.Delete;
import ch.cyberduck.core.preferences.PreferencesFactory;
import ch.cyberduck.core.threading.ThreadPool;

import org.apache.log4j.Logger;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

import synapticloop.b2.exception.B2ApiException;

public class B2DeleteFeature implements Delete {
    private static final Logger log = Logger.getLogger(B2DeleteFeature.class);

    private final PathContainerService containerService
            = new B2PathContainerService();

    private final B2Session session;

    private final ThreadPool pool;

    public B2DeleteFeature(final B2Session session) {
        this.session = session;
        this.pool = new ThreadPool(PreferencesFactory.get().getInteger("b2.delete.concurrency"), "delete");
    }

    @Override
    public void delete(final List<Path> files, final LoginCallback prompt, final Callback callback) throws BackgroundException {
        final List<Future<Void>> pending = new ArrayList<>();
        for(Path file : files) {
            if(containerService.isContainer(file)) {
                continue;
            }
            pending.add(this.submit(file, callback));
        }
        try {
            for(Future<Void> f : pending) {
                f.get();
            }
        }
        catch(InterruptedException e) {
            throw new ConnectionCanceledException(e);
        }
        catch(ExecutionException e) {
            log.warn(String.format("Delete failed with execution failure %s", e.getMessage()));
            if(e.getCause() instanceof BackgroundException) {
                throw (BackgroundException) e.getCause();
            }
            throw new BackgroundException(e);
        }
        finally {
            pool.shutdown();
        }
        for(Path file : files) {
            try {
                if(containerService.isContainer(file)) {
                    callback.delete(file);
                    // Finally delete bucket itself
                    session.getClient().deleteBucket(new B2FileidProvider(session).getFileid(file));
                }
            }
            catch(B2ApiException e) {
                throw new B2ExceptionMappingService(session).map("Cannot delete {0}", e, file);
            }
            catch(IOException e) {
                throw new DefaultIOExceptionMappingService().map(e);
            }
        }
    }

    private Future<Void> submit(final Path file, final Callback callback) {
        if(log.isInfoEnabled()) {
            log.info(String.format("Submit %s for delete", file));
        }
        return pool.execute(new Callable<Void>() {
            @Override
            public Void call() throws BackgroundException {
                try {
                    callback.delete(file);
                    if(file.isPlaceholder()) {
                        session.getClient().deleteFileVersion(String.format("%s%s", containerService.getKey(file), B2DirectoryFeature.PLACEHOLDER),
                                new B2FileidProvider(session).getFileid(file));
                    }
                    else if(file.isFile()) {
                        session.getClient().deleteFileVersion(containerService.getKey(file),
                                new B2FileidProvider(session).getFileid(file));
                    }
                }
                catch(B2ApiException e) {
                    throw new B2ExceptionMappingService(session).map("Cannot delete {0}", e, file);
                }
                catch(IOException e) {
                    throw new DefaultIOExceptionMappingService().map(e);
                }
                return null;
            }
        });
    }
}
