package ch.cyberduck.core.spectra;

import ch.cyberduck.core.Path;
import ch.cyberduck.core.exception.BackgroundException;
import ch.cyberduck.core.features.Touch;
import ch.cyberduck.core.transfer.Transfer;
import ch.cyberduck.core.transfer.TransferStatus;

import java.util.Collections;

public class SpectraTouchFeature implements Touch {

    private final SpectraSession session;

    public SpectraTouchFeature(final SpectraSession session) {
        this.session = session;
    }

    @Override
    public void touch(final Path file) throws BackgroundException {
        final SpectraBulkService bulk = new SpectraBulkService(session);
        bulk.pre(Transfer.Type.upload, Collections.singletonMap(file, new TransferStatus().length(0L)));
    }


    @Override
    public boolean isSupported(final Path workdir) {
        // Creating files is only possible inside a bucket.
        return !workdir.isRoot();
    }
}
