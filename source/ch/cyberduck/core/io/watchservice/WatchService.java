package ch.cyberduck.core.io.watchservice;

import java.io.Closeable;
import java.io.IOException;
import java.util.concurrent.TimeUnit;

public interface WatchService extends Closeable {

    /**
     * Register the given object with this watch service
     */
    WatchKey register(WatchableFile watchableFile,
                      WatchEvent.Kind<?>[] events,
                      WatchEvent.Modifier... modifers)
            throws IOException;

    /**
     * Closes this watch service.
     * <p/>
     * <p> If a thread is currently blocked in the {@link #take take} or {@link
     * #poll(long, TimeUnit) poll} methods waiting for a key to be queued then
     * it immediately receives a {@link ClosedWatchServiceException}. Any
     * valid keys associated with this watch service are {@link WatchKey#isValid
     * invalidated}.
     * <p/>
     * <p> After a watch service is closed, any further attempt to invoke
     * operations upon it will throw {@link ClosedWatchServiceException}.
     * If this watch service is already closed then invoking this method
     * has no effect.
     *
     * @throws IOException if an I/O error occurs
     */
    void close() throws IOException;

    /**
     * Retrieves and removes the next watch key, or {@code null} if none are
     * present.
     *
     * @return the next watch key, or {@code null}
     * @throws ClosedWatchServiceException if this watch service is closed
     */
    WatchKey poll();

    /**
     * Retrieves and removes the next watch key, waiting if necessary up to the
     * specified wait time if none are yet present.
     *
     * @param timeout how to wait before giving up, in units of unit
     * @param unit    a {@code TimeUnit} determining how to interpret the timeout
     *                parameter
     * @return the next watch key, or {@code null}
     * @throws ClosedWatchServiceException if this watch service is closed, or it is closed while waiting
     *                                     for the next key
     * @throws InterruptedException        if interrupted while waiting
     */
    WatchKey poll(long timeout, TimeUnit unit)
            throws InterruptedException;

    /**
     * Retrieves and removes next watch key, waiting if none are yet present.
     *
     * @return the next watch key
     * @throws ClosedWatchServiceException if this watch service is closed, or it is closed while waiting
     *                                     for the next key
     * @throws InterruptedException        if interrupted while waiting
     */
    WatchKey take() throws InterruptedException;
}
