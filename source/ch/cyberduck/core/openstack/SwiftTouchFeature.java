package ch.cyberduck.core.openstack;

import ch.cyberduck.core.DefaultIOExceptionMappingService;
import ch.cyberduck.core.Path;
import ch.cyberduck.core.PathContainerService;
import ch.cyberduck.core.exception.BackgroundException;
import ch.cyberduck.core.features.Touch;

import java.io.IOException;

/**
 * @version $Id$
 */
public class SwiftTouchFeature implements Touch {

    private SwiftSession session;

    final PathContainerService containerService = new PathContainerService();

    public SwiftTouchFeature(final SwiftSession session) {
        this.session = session;
    }

    @Override
    public void touch(final Path file) throws BackgroundException {
        try {
            session.getClient().createPath(session.getRegion(containerService.getContainer(file)),
                    containerService.getContainer(file).getName(),
                    containerService.getKey(file)
            );
        }
        catch(IOException e) {
            throw new DefaultIOExceptionMappingService().map("Cannot create file {0}", e, file);
        }
    }

    @Override
    public boolean isSupported(final Path workdir) {
        // Creating files is only possible inside a container.
        return !workdir.isRoot();
    }
}
