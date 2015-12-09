package ch.cyberduck.core.features;

import ch.cyberduck.core.Path;
import ch.cyberduck.core.exception.BackgroundException;
import ch.cyberduck.core.transfer.Transfer;
import ch.cyberduck.core.transfer.TransferStatus;

import java.util.Map;

/**
 * @version $Id:$
 */
public interface Bulk<R> {
    R pre(Transfer.Type type, Map<Path, TransferStatus> files) throws BackgroundException;
}
