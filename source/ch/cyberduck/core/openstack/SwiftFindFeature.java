package ch.cyberduck.core.openstack;

import ch.cyberduck.core.DefaultIOExceptionMappingService;
import ch.cyberduck.core.Path;
import ch.cyberduck.core.PathContainerService;
import ch.cyberduck.core.exception.BackgroundException;
import ch.cyberduck.core.exception.NotfoundException;
import ch.cyberduck.core.features.Find;
import ch.cyberduck.core.shared.DefaultFindFeature;

import java.io.IOException;

import ch.iterate.openstack.swift.exception.GenericException;

/**
 * @version $Id:$
 */
public class SwiftFindFeature implements Find {

    private SwiftSession session;

    private PathContainerService containerService
            = new PathContainerService();

    public SwiftFindFeature(final SwiftSession session) {
        this.session = session;
    }

    @Override
    public boolean find(final Path file) throws BackgroundException {
        try {
            if(containerService.isContainer(file)) {
                try {
                    return session.getClient().containerExists(session.getRegion(containerService.getContainer(file)),
                            file.getName());
                }
                catch(GenericException e) {
                    throw new SwiftExceptionMappingService().map("Cannot read file attributes", e, file);
                }
                catch(IOException e) {
                    throw new DefaultIOExceptionMappingService().map("Cannot read file attributes", e, file);
                }
            }
            return new DefaultFindFeature(session).find(file);
        }
        catch(NotfoundException e) {
            return false;
        }
    }
}
