package ch.cyberduck.core.transfer;

import ch.cyberduck.core.Path;
import ch.cyberduck.core.Session;
import ch.cyberduck.core.threading.BackgroundException;

/**
 * @version $Id$
 */
public interface TransferPathFilter {

    /**
     * @param session Connection
     * @param file    File
     * @return True if file should be transferred
     */
    boolean accept(Session session, Path file) throws BackgroundException;

    /**
     * Called before the file will actually get transferred. Should prepare for the transfer
     * such as calculating its size.
     * Must only be called exactly once for each file.
     * Must only be called if #accept for the file returns true
     *
     * @param session Connection
     * @param file    File
     * @return Transfer status
     */
    TransferStatus prepare(Session session, Path file) throws BackgroundException;

    /**
     * Post processing of completed transfer.
     *
     * @param session Connection
     * @param file    File
     * @param options Options
     * @param status  Transfer status
     */
    void complete(Session session, Path file, TransferOptions options, TransferStatus status) throws BackgroundException;
}