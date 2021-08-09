package ch.cyberduck.core.brick;/*
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

import ch.cyberduck.core.brick.io.swagger.client.ApiException;
import ch.cyberduck.core.brick.io.swagger.client.api.FileMigrationsApi;
import ch.cyberduck.core.brick.io.swagger.client.model.FileActionEntity;
import ch.cyberduck.core.brick.io.swagger.client.model.FileMigrationEntity;
import ch.cyberduck.core.exception.BackgroundException;
import ch.cyberduck.core.exception.ConnectionCanceledException;
import ch.cyberduck.core.preferences.Preferences;
import ch.cyberduck.core.preferences.PreferencesFactory;
import ch.cyberduck.core.threading.ScheduledThreadPool;

import org.apache.log4j.Logger;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

import com.google.common.util.concurrent.Uninterruptibles;

public class BrickFileMigrationFeature {
    private static final Logger log = Logger.getLogger(BrickFileMigrationFeature.class);

    private final Preferences preferences = PreferencesFactory.get();

    protected void poll(final BrickApiClient client, final FileActionEntity entity) throws BackgroundException {
        final CountDownLatch signal = new CountDownLatch(1);
        final ScheduledThreadPool scheduler = new ScheduledThreadPool();
        final long timeout = preferences.getLong("brick.migration.interrupt.ms");
        final long start = System.currentTimeMillis();
        final AtomicReference<BackgroundException> failure = new AtomicReference<>();
        try {
            scheduler.repeat(() -> {
                try {
                    if(System.currentTimeMillis() - start > timeout) {
                        failure.set(new ConnectionCanceledException(String.format("Interrupt polling for migration key after %d", timeout)));
                        signal.countDown();
                        return;
                    }
                    // Poll status
                    final FileMigrationEntity.StatusEnum migration = new FileMigrationsApi(client)
                        .getFileMigrationsId(entity.getFileMigrationId()).getStatus();
                    switch(migration) {
                        case COMPLETE:
                            signal.countDown();
                            return;
                        default:
                            log.warn(String.format("Wait for copy to complete with current status %s", migration));
                            break;
                    }
                }
                catch(ApiException e) {
                    log.warn(String.format("Failure processing scheduled task. %s", e.getMessage()), e);
                    failure.set(new BrickExceptionMappingService().map(e));
                    signal.countDown();
                }
            }, preferences.getLong("brick.migration.interval.ms"), TimeUnit.MILLISECONDS);
            Uninterruptibles.awaitUninterruptibly(signal);
            if(null != failure.get()) {
                throw failure.get();
            }
        }
        finally {
            scheduler.shutdown();
        }
    }

}
