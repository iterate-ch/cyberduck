package ch.cyberduck.core.dropbox;

import ch.cyberduck.core.DescriptiveUrlBag;
import ch.cyberduck.core.Path;
import ch.cyberduck.core.UrlProvider;

/**
 * Created by sebi on 01.04.16.
 */
public class DropboxUrlProvider implements UrlProvider {

    private final DropboxSession session;

    public DropboxUrlProvider(final DropboxSession dropboxSession) {
        session = dropboxSession;
    }

    @Override
    public DescriptiveUrlBag toUrl(final Path file) {
        return null;
    }
}
