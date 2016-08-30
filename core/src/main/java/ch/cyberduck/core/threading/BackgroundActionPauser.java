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

import ch.cyberduck.core.ProgressListener;
import ch.cyberduck.core.io.StreamCancelation;
import ch.cyberduck.core.preferences.PreferencesFactory;

import org.apache.log4j.Logger;

import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.BrokenBarrierException;
import java.util.concurrent.CyclicBarrier;

public class BackgroundActionPauser {
    private static final Logger log = Logger.getLogger(BackgroundActionPauser.class);

    /**
     * The delay to wait before execution of the action in seconds
     */
    private Integer delay;

    private final Callback callback;

    public BackgroundActionPauser(final Callback callback) {
        this(callback, PreferencesFactory.get().getInteger("connection.retry.delay"));
    }

    /**
     * @param delay In seconds
     */
    public BackgroundActionPauser(final Callback callback, final Integer delay) {
        this.callback = callback;
        this.delay = delay;
    }

    public void await(final ProgressListener listener) {
        if(0 == delay) {
            log.info("No pause between retry");
            return;
        }
        final Timer wakeup = new Timer();
        final CyclicBarrier wait = new CyclicBarrier(2);
        // Schedule for immediate execution with an interval of 1s
        wakeup.scheduleAtFixedRate(new PauserTimerTask(listener, wait), 0, 1000);
        try {
            // Wait for notify from wakeup timer
            wait.await();
        }
        catch(InterruptedException | BrokenBarrierException e) {
            log.error(e.getMessage(), e);
        }
    }

    private final class PauserTimerTask extends TimerTask {
        private final ProgressListener listener;
        private final CyclicBarrier wait;

        public PauserTimerTask(final ProgressListener listener, final CyclicBarrier wait) {
            this.listener = listener;
            this.wait = wait;
        }

        @Override
        public void run() {
            if(0 == delay || callback.isCanceled()) {
                // Cancel the timer repetition
                this.cancel();
                return;
            }
            callback.progress(delay--);
        }

        @Override
        public boolean cancel() {
            try {
                // Notify to return to caller from #pause()
                wait.await();
            }
            catch(InterruptedException | BrokenBarrierException e) {
                log.error(e.getMessage(), e);
            }
            return super.cancel();
        }
    }

    public interface Callback extends StreamCancelation {
        /**
         * @return True if task should be cancled and wait interrupted.
         */
        @Override
        boolean isCanceled();

        /**
         * @param delay Remaining delay
         */
        void progress(final Integer delay);
    }
}
