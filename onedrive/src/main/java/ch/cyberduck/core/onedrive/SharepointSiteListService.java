package ch.cyberduck.core.onedrive;

import ch.cyberduck.core.AttributedList;
import ch.cyberduck.core.ListProgressListener;
import ch.cyberduck.core.Path;
import ch.cyberduck.core.exception.BackgroundException;
import ch.cyberduck.core.features.IdProvider;

public class SharepointSiteListService extends AbstractSharepointListService {

    public SharepointSiteListService(final SharepointSiteSession session, final IdProvider idProvider) {
        super(session, idProvider);
    }

    @Override
    AttributedList<Path> getRoot(final Path directory, final ListProgressListener listener) throws BackgroundException {
        return addSiteItems(directory, listener);
    }
}
