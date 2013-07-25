package ch.cyberduck.core.openstack;

import ch.cyberduck.core.DefaultIOExceptionMappingService;
import ch.cyberduck.core.DisabledListProgressListener;
import ch.cyberduck.core.Path;
import ch.cyberduck.core.PathContainerService;
import ch.cyberduck.core.exception.BackgroundException;
import ch.cyberduck.core.features.Move;

import org.apache.log4j.Logger;

import java.io.IOException;

import ch.iterate.openstack.swift.exception.GenericException;
import ch.iterate.openstack.swift.exception.NotFoundException;

/**
 * @version $Id:$
 */
public class SwiftMoveFeature implements Move {
    private static final Logger log = Logger.getLogger(SwiftMoveFeature.class);

    private PathContainerService containerService = new PathContainerService();

    private SwiftSession session;

    public SwiftMoveFeature(final SwiftSession session) {
        this.session = session;
    }

    @Override
    public void move(final Path file, final Path renamed) throws BackgroundException {
        try {
            if(file.attributes().isFile()) {
                session.getClient().copyObject(session.getRegion(containerService.getContainer(file)),
                        containerService.getContainer(file).getName(), containerService.getKey(file),
                        containerService.getContainer(renamed).getName(), containerService.getKey(renamed));
                session.getClient().deleteObject(session.getRegion(containerService.getContainer(file)),
                        containerService.getContainer(file).getName(), containerService.getKey(file));
            }
            else if(file.attributes().isDirectory()) {
                for(Path i : session.list(file, new DisabledListProgressListener())) {
                    this.move(i, new Path(renamed, i.getName(), i.attributes().getType()));
                }
                try {
                    session.getClient().deleteObject(session.getRegion(containerService.getContainer(file)),
                            containerService.getContainer(file).getName(), containerService.getKey(file));
                }
                catch(NotFoundException e) {
                    // No real placeholder but just a delimiter returned in the object listing.
                    log.warn(e.getMessage());
                }
            }
        }
        catch(GenericException e) {
            throw new SwiftExceptionMappingService().map("Cannot rename {0}", e, file);
        }
        catch(IOException e) {
            throw new DefaultIOExceptionMappingService().map("Cannot rename {0}", e, file);
        }
    }

    @Override
    public boolean isSupported(final Path file) {
        return !file.attributes().isVolume();
    }
}
