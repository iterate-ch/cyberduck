package ch.cyberduck.core.onedrive;

import ch.cyberduck.core.AttributedList;
import ch.cyberduck.core.ListProgressListener;
import ch.cyberduck.core.Path;
import ch.cyberduck.core.exception.BackgroundException;
import ch.cyberduck.core.features.VersionIdProvider;

public class SharepointSiteListService extends AbstractSharepointListService {

    public SharepointSiteListService(final SharepointSiteSession session, final VersionIdProvider idProvider) {
        super(session);
    }

    @Override
    AttributedList<Path> getRoot(final Path directory, final ListProgressListener listener) throws BackgroundException {
        return addSiteItems(directory, listener);
    }
}
