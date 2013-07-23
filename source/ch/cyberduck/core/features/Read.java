package ch.cyberduck.core.features;

import ch.cyberduck.core.Path;
import ch.cyberduck.core.exception.BackgroundException;
import ch.cyberduck.core.transfer.TransferStatus;

import java.io.InputStream;

/**
 * @version $Id:$
 */
public interface Read {

    /**
     * @param status Transfer status
     * @return Stream to read from to download file
     */
    InputStream read(Path file, TransferStatus status) throws BackgroundException;

    boolean isResumable();
}
