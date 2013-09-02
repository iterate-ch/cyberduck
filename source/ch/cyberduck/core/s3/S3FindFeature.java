package ch.cyberduck.core.s3;

import ch.cyberduck.core.Path;
import ch.cyberduck.core.PathContainerService;
import ch.cyberduck.core.exception.BackgroundException;
import ch.cyberduck.core.features.Find;

import org.jets3t.service.ServiceException;

/**
 * @version $Id:$
 */
public class S3FindFeature implements Find {

    private S3Session session;

    private PathContainerService containerService = new PathContainerService();

    public S3FindFeature(final S3Session session) {
        this.session = session;
    }

    @Override
    public boolean find(final Path file) throws BackgroundException {
        try {
            return session.getClient().isObjectInBucket(containerService.getContainer(file).getName(),
                    containerService.getKey(file));
        }
        catch(ServiceException e) {
            throw new ServiceExceptionMappingService().map("Cannot read file attributes", e, file);
        }
    }
}
