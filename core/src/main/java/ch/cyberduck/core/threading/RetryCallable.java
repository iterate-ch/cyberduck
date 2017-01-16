package ch.cyberduck.core.threading;

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

import ch.cyberduck.core.LocaleFactory;
import ch.cyberduck.core.ProgressListener;
import ch.cyberduck.core.exception.BackgroundException;
import ch.cyberduck.core.exception.RetriableAccessDeniedException;
import ch.cyberduck.core.io.StreamCancelation;
import ch.cyberduck.core.preferences.Preferences;
import ch.cyberduck.core.preferences.PreferencesFactory;

import org.apache.log4j.Logger;

import java.text.MessageFormat;
import java.util.concurrent.Callable;

public abstract class RetryCallable<T> implements Callable<T> {
    private static final Logger log = Logger.getLogger(RetryCallable.class);

    private final Preferences preferences = PreferencesFactory.get();

    private final FailureDiagnostics<BackgroundException> diagnostics
            = new DefaultFailureDiagnostics();

    /**
     * The number of times this action has been run
     */
    private int count = 0;

    private int backoff = preferences.getInteger("connection.retry.delay");

    @Override
    public abstract T call() throws BackgroundException;

    /**
     * @param failure  Failure
     * @param progress Listener
     * @param cancel   Progress callback
     * @return Increment counter and return true if retry attempt should be made for a failed transfer
     */
    public boolean retry(final BackgroundException failure, final ProgressListener progress, final StreamCancelation cancel) {
        if(++count > preferences.getInteger("connection.retry")) {
            log.warn(String.format("Cancel retry for failure %s", failure));
            return false;
        }
        switch(diagnostics.determine(failure)) {
            case network:
            case application:
                final int delay;
                if(failure instanceof RetriableAccessDeniedException) {
                    delay = (int) ((RetriableAccessDeniedException) failure).getRetry().getSeconds();
                }
                else {
                    delay = backoff;
                    // Exponential backoff
                    backoff *= 2;
                }
                log.warn(String.format("Retry for failure %s with delay of %ds", failure, delay));
                final BackgroundActionPauser pause = new BackgroundActionPauser(new BackgroundActionPauser.Callback() {
                    @Override
                    public boolean isCanceled() {
                        return cancel.isCanceled();
                    }

                    @Override
                    public void progress(final Integer delay) {
                        progress.message(MessageFormat.format(LocaleFactory.localizedString("Retry again in {0} seconds", "Status"), delay));
                    }
                }, delay);
                pause.await();
                return true;
        }
        log.warn(String.format("No retry for failure %s", failure));
        return false;
    }
}
