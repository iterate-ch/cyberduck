package ch.cyberduck.core.s3;

import ch.cyberduck.core.DefaultTouchFeature;
import ch.cyberduck.core.Path;
import ch.cyberduck.core.Session;

/**
 * @version $Id:$
 */
public class S3TouchFeature extends DefaultTouchFeature {

    public S3TouchFeature(final Session session) {
        super(session);
    }

    @Override
    public boolean isSupported(final Path workdir) {
        // Creating files is only possible inside a bucket.
        return !workdir.isRoot();
    }
}
