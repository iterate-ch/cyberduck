package ch.cyberduck.core.transfer;

import ch.cyberduck.core.Path;
import ch.cyberduck.core.Filter;

/**
 * @version $Id$
 */
public abstract class TransferPathFilter implements Filter<Path> {
    /**
     * Called before the file will actually get transferred. Should prepare for the transfer
     * such as calculating its size.
     * Must only be called exactly once for each file.
     * Must only be called if #accept for the file returns true
     *
     * @param p File
     * @return Transfer status
     */
    public abstract TransferStatus prepare(Path p);

    /**
     * Post processing of completed transfer.
     *
     * @param p       File
     * @param options Options
     * @param status  Transfer status
     */
    public abstract void complete(Path p, TransferOptions options, TransferStatus status);
}