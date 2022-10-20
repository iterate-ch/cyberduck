package ch.cyberduck.core.concurrency;

/*
 * Copyright (c) 2002-2022 iterate GmbH. All rights reserved.
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

import org.apache.commons.lang3.exception.ExceptionUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.concurrent.CountDownLatch;

import static java.lang.Thread.currentThread;

public class Interruptibles {
    private static final Logger log = LogManager.getLogger(Interruptibles.class);

    /**
     * Await count down and reset interrupt flag on thread prior throwing checked exception
     *
     * @param latch     Count down
     * @param throwable Checked exception type
     * @param <E>       Type of exception
     * @throws E Exception type when interrupted
     */
    public static <E extends Throwable> void await(final CountDownLatch latch, Class<E> throwable) throws E {
        try {
            latch.await();
        }
        catch(InterruptedException e) {
            final Thread thread = currentThread();
            if(log.isWarnEnabled()) {
                log.warn(String.format("Interrupted %s while waiting for %s", thread, latch));
            }
            thread.interrupt();
            throw ExceptionUtils.throwableOfType(e, throwable);
        }
    }
}
