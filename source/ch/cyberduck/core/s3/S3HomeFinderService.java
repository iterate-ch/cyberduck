package ch.cyberduck.core.s3;

import ch.cyberduck.core.HomeFinderService;
import ch.cyberduck.core.Path;
import ch.cyberduck.core.PathContainerService;
import ch.cyberduck.core.exception.BackgroundException;
import ch.cyberduck.core.shared.DefaultHomeFinderService;

/**
 * @version $Id:$
 */
public class S3HomeFinderService implements HomeFinderService {

    private S3Session session;

    private PathContainerService containerService = new PathContainerService();

    public S3HomeFinderService(final S3Session session) {
        this.session = session;
    }

    @Override
    public Path find() throws BackgroundException {
        final Path home = new DefaultHomeFinderService(session).find();
        if(containerService.isContainer(home)) {
            home.attributes().setType(Path.VOLUME_TYPE | Path.DIRECTORY_TYPE);
        }
        return home;
    }
}
