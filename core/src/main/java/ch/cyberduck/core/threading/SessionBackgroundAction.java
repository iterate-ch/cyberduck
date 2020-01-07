package ch.cyberduck.core.threading;

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
 * Bug fixes, suggestions and comments should be sent to:
 * feedback@cyberduck.ch
 */

import ch.cyberduck.core.BookmarkNameProvider;
import ch.cyberduck.core.Host;
import ch.cyberduck.core.KeychainLoginService;
import ch.cyberduck.core.LocaleFactory;
import ch.cyberduck.core.LoginCallback;
import ch.cyberduck.core.LoginOptions;
import ch.cyberduck.core.PasswordStoreFactory;
import ch.cyberduck.core.ProgressListener;
import ch.cyberduck.core.Session;
import ch.cyberduck.core.StringAppender;
import ch.cyberduck.core.TranscriptListener;
import ch.cyberduck.core.exception.BackgroundException;
import ch.cyberduck.core.exception.ConnectionCanceledException;
import ch.cyberduck.core.exception.LoginFailureException;
import ch.cyberduck.core.pool.SessionPool;
import ch.cyberduck.core.preferences.PreferencesFactory;
import ch.cyberduck.core.proxy.ProxyFactory;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;

public abstract class SessionBackgroundAction<T> extends AbstractBackgroundAction<T> implements ProgressListener, TranscriptListener {
    private static final Logger log = Logger.getLogger(SessionBackgroundAction.class);

    /**
     * This action encountered one or more exceptions
     */
    private boolean failed;

    /**
     * Contains the transcript of the session while this action was running
     */
    private StringBuffer transcript
        = new StringBuffer();

    private static final String LINE_SEPARATOR
        = System.getProperty("line.separator");

    private final AlertCallback alert;
    private final LoginCallback login;
    private final ProgressListener progress;

    protected final SessionPool pool;

    public SessionBackgroundAction(final SessionPool pool,
                                   final AlertCallback alert,
                                   final LoginCallback login,
                                   final ProgressListener progress) {
        this.pool = pool;
        this.alert = alert;
        this.login = login;
        this.progress = progress;
    }

    @Override
    public void message(final String message) {
        progress.message(message);
    }

    /**
     * Append to the transcript and notify listeners.
     */
    @Override
    public void log(final Type request, final String message) {
        transcript.append(message).append(LINE_SEPARATOR);
    }

    @Override
    public void prepare() {
        super.prepare();
        this.message(this.getActivity());
    }

    protected void reset() throws BackgroundException {
        // Reset the failure status but remember the previous exception for automatic retry.
        failed = false;
    }

    /**
     * @return True if the the action had a permanent failures. Returns false if
     * there were only temporary exceptions and the action succeeded upon retry
     */
    public boolean hasFailed() {
        return failed;
    }

    @Override
    public T call() throws BackgroundException {
        try {
            return new DefaultRetryCallable<T>(pool.getHost(), new BackgroundExceptionCallable<T>() {
                @Override
                public T call() throws BackgroundException {
                    // Reset status
                    SessionBackgroundAction.this.reset();
                    // Run action
                    return SessionBackgroundAction.this.run();
                }
            }, this, this).call();
        }
        catch(ConnectionCanceledException e) {
            throw e;
        }
        catch(BackgroundException e) {
            failed = true;
            throw e;
        }
    }

    @Override
    public T run() throws BackgroundException {
        final Session<?> session = pool.borrow(this).withListener(this);
        BackgroundException failure = null;
        try {
            return this.run(session);
        }
        catch(LoginFailureException e) {
            if(PreferencesFactory.get().getBoolean("connection.retry.login.enable")) {
                if(log.isInfoEnabled()) {
                    log.info(String.format("Prompt to re-authenticate for failure %s", e));
                }
                final Host bookmark = pool.getHost();
                try {
                    // Prompt for new credentials
                    final KeychainLoginService service = new KeychainLoginService(PasswordStoreFactory.get());
                    final StringAppender details = new StringAppender();
                    details.append(LocaleFactory.localizedString("Login failed", "Credentials"));
                    details.append(e.getDetail());
                    if(service.prompt(bookmark, details.toString(), login, new LoginOptions(bookmark.getProtocol()))) {
                        if(log.isDebugEnabled()) {
                            log.debug(String.format("Re-authenticate with credentials %s", bookmark.getCredentials()));
                        }
                        // Try to authenticate again
                        service.authenticate(ProxyFactory.get().find(bookmark), session, progress, login, new CancelCallback() {
                            @Override
                            public void verify() throws ConnectionCanceledException {
                                if(SessionBackgroundAction.this.isCanceled()) {
                                    throw new ConnectionCanceledException();
                                }
                            }
                        });
                        // Run action again after login
                        return this.run();
                    }
                }
                catch(BackgroundException f) {
                    log.warn(String.format("Ignore error %s after login failure %s ", f, e));
                }
            }
            else {
                log.warn(String.format("Disabled retry for login failure %s", e));
            }
            failure = e;
            throw e;
        }
        catch(BackgroundException e) {
            failure = e;
            throw e;
        }
        finally {
            pool.release(session.removeListener(this), failure);
        }
    }

    public abstract T run(final Session<?> session) throws BackgroundException;

    @Override
    public boolean alert(final BackgroundException failure) {
        // Display alert if the action was not canceled intentionally
        if(this.isCanceled()) {
            return false;
        }
        if(log.isInfoEnabled()) {
            log.info(String.format("Run alert callback %s for failure %s", alert, failure));
        }
        // Display alert if the action was not canceled intentionally
        return alert.alert(pool.getHost(), failure, new StringBuilder(transcript.toString()));
    }

    @Override
    public void cleanup() {
        transcript.setLength(0);
        this.message(StringUtils.EMPTY);
    }

    @Override
    public String getName() {
        return BookmarkNameProvider.toString(pool.getHost());
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("SessionBackgroundAction{");
        sb.append("failed=").append(failed);
        sb.append(", pool=").append(pool);
        sb.append('}');
        return sb.toString();
    }
}
