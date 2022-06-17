package ch.cyberduck.core.onedrive;

import ch.cyberduck.core.AttributedList;
import ch.cyberduck.core.ListProgressListener;
import ch.cyberduck.core.Path;
import ch.cyberduck.core.exception.BackgroundException;
import ch.cyberduck.core.onedrive.features.GraphFileIdProvider;

public class SharepointSiteListService extends AbstractSharepointListService {

    public SharepointSiteListService(final SharepointSiteSession session, final GraphFileIdProvider fileid) {
        super(session, fileid);
    }

    @Override
    protected AttributedList<Path> getRoot(final Path directory, final ListProgressListener listener) throws BackgroundException {
        return addSiteItems(directory, listener);
    }
}
