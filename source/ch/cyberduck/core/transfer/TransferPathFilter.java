package ch.cyberduck.core.transfer;

import ch.cyberduck.core.Path;
import ch.cyberduck.core.Session;
import ch.cyberduck.core.threading.BackgroundException;

/**
 * @version $Id$
 */
public interface TransferPathFilter {

    boolean accept(final Session session, Path file) throws BackgroundException;

    /**
     * Called before the file will actually get transferred. Should prepare for the transfer
     * such as calculating its size.
     * Must only be called exactly once for each file.
     * Must only be called if #accept for the file returns true
     *
     * @param p File
     * @return Transfer status
     * @see ch.cyberduck.core.Filter#accept(Object)
     */
    TransferStatus prepare(final Session session, Path p) throws BackgroundException;

    /**
     * Post processing of completed transfer.
     *
     * @param p       File
     * @param options Options
     * @param status  Transfer status
     */
    void complete(final Session session, Path p, TransferOptions options, TransferStatus status) throws BackgroundException;
}