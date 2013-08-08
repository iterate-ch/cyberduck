package ch.cyberduck.core.openstack;

import ch.cyberduck.core.DefaultIOExceptionMappingService;
import ch.cyberduck.core.LoginController;
import ch.cyberduck.core.Path;
import ch.cyberduck.core.PathContainerService;
import ch.cyberduck.core.cdn.Distribution;
import ch.cyberduck.core.cdn.features.Purge;
import ch.cyberduck.core.exception.BackgroundException;

import java.io.IOException;
import java.util.List;

import ch.iterate.openstack.swift.exception.GenericException;

/**
 * @version $Id:$
 */
public class SwiftDistributionPurgeFeature implements Purge {

    private SwiftSession session;

    public SwiftDistributionPurgeFeature(final SwiftSession session) {
        this.session = session;
    }

    @Override
    public void invalidate(final Path container, final Distribution.Method method, final List<Path> files, final LoginController prompt) throws BackgroundException {
        try {
            final PathContainerService containerService = new PathContainerService();
            for(Path file : files) {
                if(containerService.isContainer(file)) {
                    session.getClient().purgeCDNContainer(session.getRegion(containerService.getContainer(file)),
                            container.getName(), null);
                }
                else {
                    session.getClient().purgeCDNObject(session.getRegion(containerService.getContainer(file)),
                            container.getName(), containerService.getKey(file), null);
                }
            }
        }
        catch(GenericException e) {
            throw new SwiftExceptionMappingService().map("Cannot write CDN configuration", e);
        }
        catch(IOException e) {
            throw new DefaultIOExceptionMappingService().map("Cannot write CDN configuration", e);
        }
    }
}
