package ch.cyberduck.core.openstack;

import ch.cyberduck.core.DefaultIOExceptionMappingService;
import ch.cyberduck.core.Path;
import ch.cyberduck.core.PathContainerService;
import ch.cyberduck.core.exception.BackgroundException;
import ch.cyberduck.core.features.Touch;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.HashMap;

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
            if(file.attributes().isDirectory()) {
                session.getClient().storeObject(session.getRegion(containerService.getContainer(file)),
                        containerService.getContainer(file).getName(),
                        new ByteArrayInputStream(new byte[]{}), "application/directory", containerService.getKey(file),
                        new HashMap<String, String>());
            }
            else {
                session.getClient().storeObject(session.getRegion(containerService.getContainer(file)),
                        containerService.getContainer(file).getName(),
                        new ByteArrayInputStream(new byte[]{}), "application/octet-stream", containerService.getKey(file),
                        new HashMap<String, String>());
            }
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
