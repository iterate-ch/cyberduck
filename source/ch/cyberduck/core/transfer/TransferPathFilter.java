package ch.cyberduck.core.transfer;

import ch.cyberduck.core.Path;
import ch.cyberduck.core.ProgressListener;
import ch.cyberduck.core.Session;
import ch.cyberduck.core.exception.BackgroundException;

/**
 * @version $Id$
 */
public interface TransferPathFilter {

    /**
     * @param session Connection
     * @param file    File
     * @return True if file should be transferred
     */
    boolean accept(Session<?> session, Path file) throws BackgroundException;

    /**
     * Called before the file will actually get transferred. Should prepare for the transfer such as calculating its size.
     *
     * @param session Connection
     * @param file    File
     * @param parent  Parent transfer status
     * @return Transfer status
     */
    TransferStatus prepare(Session<?> session, Path file, final TransferStatus parent) throws BackgroundException;

    /**
     * Post processing of completed transfer.
     *
     * @param session  Connection
     * @param file     File
     * @param options  Options
     * @param status   Transfer status
     * @param listener Progress callback
     */
    void complete(Session<?> session, Path file, TransferOptions options,
                  TransferStatus status, final ProgressListener listener) throws BackgroundException;
}