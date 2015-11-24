package ch.cyberduck.core.features;

import ch.cyberduck.core.Path;
import ch.cyberduck.core.exception.BackgroundException;
import ch.cyberduck.core.transfer.TransferStatus;

import java.util.Map;

/**
 * @version $Id:$
 */
public interface Bulk {
    void pre(Map<Path, TransferStatus> files) throws BackgroundException;
}
