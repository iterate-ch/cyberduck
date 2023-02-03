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

import ch.cyberduck.core.Host;
import ch.cyberduck.core.LocaleFactory;
import ch.cyberduck.core.ProgressListener;
import ch.cyberduck.core.date.RemainingPeriodFormatter;
import ch.cyberduck.core.diagnostics.ReachabilityFactory;
import ch.cyberduck.core.exception.BackgroundException;
import ch.cyberduck.core.exception.ConnectionCanceledException;
import ch.cyberduck.core.exception.RetriableAccessDeniedException;
import ch.cyberduck.core.preferences.Preferences;
import ch.cyberduck.core.preferences.PreferencesFactory;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.text.MessageFormat;
import java.util.concurrent.Callable;

public abstract class AbstractRetryCallable<T> implements Callable<T> {
    private static final Logger log = LogManager.getLogger(AbstractRetryCallable.class);

    private final Preferences preferences = PreferencesFactory.get();

    private final Host host;
    /**
     * The number of times to retry a failed action
     */
    private final int retry;

    /**
     * The number of times this action has been run
     */
    private int count = 0;
    /**
     * Backoff in seconds
     */
    private int backoff;

    public AbstractRetryCallable(final Host host, final int retry, final int delay) {
        this.host = host;
        this.retry = retry;
        this.backoff = delay;
    }

    @Override
    public abstract T call() throws BackgroundException;

    /**
     * @param failure  Failure
     * @param progress Listener
     * @param cancel   Progress callback
     * @return Increment counter and return true if retry attempt should be made for a failed transfer
     */
    public boolean retry(final BackgroundException failure, final ProgressListener progress, final BackgroundActionState cancel) {
        if(++count > retry) {
            if(log.isWarnEnabled()) {
                log.warn(String.format("Cancel retry for failure %s after %d counts", failure, retry));
            }
            return false;
        }
        int delay;
        final FailureDiagnostics<BackgroundException> diagnostics = new DefaultFailureDiagnostics();
        switch(diagnostics.determine(failure)) {
            case network:
                if(!ReachabilityFactory.get().isReachable(host)) {
                    log.warn(String.format("Cancel retry for failure %s with host %s not reachable", failure, host));
                    return false;
                }
                delay = backoff;
                break;
            case application:
                if(failure instanceof RetriableAccessDeniedException) {
                    // Explicitly retry
                    delay = (int) ((RetriableAccessDeniedException) failure).getDelay().getSeconds();
                    break;
                }
            default:
                if(log.isWarnEnabled()) {
                    log.warn(String.format("No retry for failure %s", failure));
                }
                return false;
        }
        if(log.isWarnEnabled()) {
            log.warn(String.format("Retry for failure %s with delay of %ds", failure, delay));
        }
        if(delay > 0) {
            final BackgroundActionPauser pause = new BackgroundActionPauser(new BackgroundActionPauser.Callback() {
                @Override
                public void validate() throws ConnectionCanceledException {
                    if(cancel.isCanceled()) {
                        if(log.isWarnEnabled()) {
                            log.warn(String.format("Abort pausing retry after failure %s", failure));
                        }
                        throw new ConnectionCanceledException();
                    }
                }

                @Override
                public void progress(final Integer seconds) {
                    progress.message(MessageFormat.format(LocaleFactory.localizedString("Retry again in {0} ({1} more attempts)", "Status"),
                            new RemainingPeriodFormatter().format(seconds), retry - count));
                }
            }, delay);
            pause.await();
        }
        // Exponential backoff
        if(preferences.getBoolean("connection.retry.backoff.enable")) {
            backoff *= 2;
        }
        return !cancel.isCanceled();
    }

    /**
     * @return Execution count
     */
    public int getCount() {
        return count;
    }
}
