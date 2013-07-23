package ch.cyberduck.core.features;

import ch.cyberduck.core.Path;
import ch.cyberduck.core.exception.BackgroundException;
import ch.cyberduck.core.transfer.TransferStatus;

import java.io.OutputStream;

/**
 * @version $Id:$
 */
public interface Write {

    /**
     * @param status Transfer status
     * @return Stream to write to for upload
     */
    OutputStream write(Path file, TransferStatus status) throws BackgroundException;

    boolean isResumable();
}
