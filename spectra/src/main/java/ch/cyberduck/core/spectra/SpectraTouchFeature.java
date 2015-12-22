package ch.cyberduck.core.spectra;

import ch.cyberduck.core.Path;
import ch.cyberduck.core.exception.BackgroundException;
import ch.cyberduck.core.s3.S3TouchFeature;
import ch.cyberduck.core.transfer.Transfer;
import ch.cyberduck.core.transfer.TransferStatus;

import java.util.Collections;

/**
 * @version $Id:$
 */
public class SpectraTouchFeature extends S3TouchFeature {

    private final SpectraSession session;

    public SpectraTouchFeature(final SpectraSession session) {
        super(session);
        this.session = session;
    }

    @Override
    public void touch(final Path file) throws BackgroundException {
        final SpectraBulkService bulk = new SpectraBulkService(session);
        bulk.pre(Transfer.Type.upload, Collections.singletonMap(file, new TransferStatus().length(0L)));
        super.touch(file);
    }
}
