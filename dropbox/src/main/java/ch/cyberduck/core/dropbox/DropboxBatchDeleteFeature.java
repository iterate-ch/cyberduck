package ch.cyberduck.core.dropbox;

/*
 * Copyright (c) 2002-2022 iterate GmbH. All rights reserved.
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

import ch.cyberduck.core.LocaleFactory;
import ch.cyberduck.core.PasswordCallback;
import ch.cyberduck.core.Path;
import ch.cyberduck.core.exception.BackgroundException;
import ch.cyberduck.core.exception.InteroperabilityException;
import ch.cyberduck.core.exception.NotfoundException;
import ch.cyberduck.core.exception.UnsupportedException;
import ch.cyberduck.core.features.Delete;
import ch.cyberduck.core.preferences.HostPreferences;
import ch.cyberduck.core.threading.LoggingUncaughtExceptionHandler;
import ch.cyberduck.core.threading.ScheduledThreadPool;
import ch.cyberduck.core.transfer.TransferStatus;
import ch.cyberduck.core.worker.DefaultExceptionMappingService;

import java.text.MessageFormat;
import java.time.Duration;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;

import com.dropbox.core.DbxException;
import com.dropbox.core.v2.files.DbxUserFilesRequests;
import com.dropbox.core.v2.files.DeleteArg;
import com.dropbox.core.v2.files.DeleteBatchJobStatus;
import com.dropbox.core.v2.files.DeleteBatchLaunch;
import com.dropbox.core.v2.files.DeleteBatchResultEntry;
import com.google.common.base.Throwables;
import com.google.common.util.concurrent.Uninterruptibles;

public class DropboxBatchDeleteFeature implements Delete {

    private final DropboxSession session;
    private final DropboxPathContainerService containerService;

    public DropboxBatchDeleteFeature(final DropboxSession session) {
        this.session = session;
        this.containerService = new DropboxPathContainerService(session);
    }

    @Override
    public void delete(final Map<Path, TransferStatus> files, final PasswordCallback prompt, final Callback callback) throws BackgroundException {
        final CountDownLatch signal = new CountDownLatch(1);
        final AtomicReference<BackgroundException> failure = new AtomicReference<>();
        final ScheduledThreadPool scheduler = new ScheduledThreadPool(new LoggingUncaughtExceptionHandler() {
            @Override
            public void uncaughtException(final Thread t, final Throwable e) {
                super.uncaughtException(t, e);
                failure.set(new BackgroundException(e));
                signal.countDown();
            }
        });
        try {
            final Map<Path, List<String>> containers = new HashMap<>();
            for(Path f : files.keySet()) {
                final Path container = containerService.getContainer(f);
                if(containers.containsKey(container)) {
                    containers.get(container).add(containerService.getKey(f));
                }
                else {
                    final List<String> keys = new ArrayList<>();
                    keys.add(containerService.getKey(f));
                    containers.put(container, keys);
                }
                callback.delete(f);
            }
            for(Path container : containers.keySet()) {
                final DbxUserFilesRequests requests = new DbxUserFilesRequests(session.getClient(container));
                final DeleteBatchLaunch job = requests.deleteBatch(containers.get(container).stream().map(DeleteArg::new).collect(Collectors.toList()));
                final ScheduledFuture<?> f = scheduler.repeat(() -> {
                    try {
                        // Poll status
                        final DeleteBatchJobStatus status = requests.deleteBatchCheck(job.getAsyncJobIdValue());
                        if(status.isComplete()) {
                            final List<DeleteBatchResultEntry> entries = status.getCompleteValue().getEntries();
                            for(DeleteBatchResultEntry entry : entries) {
                                if(entry.isFailure()) {
                                    switch(entry.getFailureValue().tag()) {
                                        case PATH_LOOKUP:
                                            failure.set(new NotfoundException(entry.getFailureValue().toString()));
                                            break;
                                        default:
                                            failure.set(new InteroperabilityException());
                                    }
                                }
                            }
                            signal.countDown();
                        }
                        if(status.isFailed()) {
                            signal.countDown();
                        }
                    }
                    catch(DbxException e) {
                        failure.set(new DropboxExceptionMappingService().map(e));
                        signal.countDown();
                    }
                }, new HostPreferences(session.getHost()).getLong("dropbox.delete.poll.interval.ms"), TimeUnit.MILLISECONDS);
                while(!Uninterruptibles.awaitUninterruptibly(signal, Duration.ofSeconds(1))) {
                    try {
                        if(f.isDone()) {
                            Uninterruptibles.getUninterruptibly(f);
                        }
                    }
                    catch(ExecutionException e) {
                        Throwables.throwIfInstanceOf(Throwables.getRootCause(e), BackgroundException.class);
                        throw new DefaultExceptionMappingService().map(Throwables.getRootCause(e));
                    }
                }
                if(null != failure.get()) {
                    throw failure.get();
                }
            }
        }
        catch(DbxException e) {
            throw new DropboxExceptionMappingService().map(e);
        }
        finally {
            scheduler.shutdown();
        }
    }

    @Override
    public EnumSet<Flags> features() {
        return EnumSet.of(Flags.recursive);
    }

    @Override
    public void preflight(final Path file) throws BackgroundException {
        if(file.attributes().isDuplicate()) {
            throw new UnsupportedException(MessageFormat.format(LocaleFactory.localizedString("Cannot delete {0}", "Error"), file)).withFile(file);
        }
    }
}
