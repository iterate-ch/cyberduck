package ch.cyberduck.core.openstack;

import ch.cyberduck.core.DefaultIOExceptionMappingService;
import ch.cyberduck.core.Path;
import ch.cyberduck.core.PathContainerService;
import ch.cyberduck.core.exception.BackgroundException;
import ch.cyberduck.core.features.Read;
import ch.cyberduck.core.transfer.TransferStatus;

import java.io.IOException;
import java.io.InputStream;

import ch.iterate.openstack.swift.exception.GenericException;

/**
 * @version $Id$
 */
public class SwiftReadFeature implements Read {

    private PathContainerService containerService
            = new PathContainerService();

    private SwiftSession session;

    public SwiftReadFeature(final SwiftSession session) {
        this.session = session;
    }

    @Override
    public InputStream read(final Path file, final TransferStatus status) throws BackgroundException {
        try {
            if(status.isAppend()) {
                return session.getClient().getObject(session.getRegion(containerService.getContainer(file)),
                        containerService.getContainer(file).getName(), containerService.getKey(file),
                        status.getCurrent(), status.getLength());
            }
            return session.getClient().getObject(session.getRegion(containerService.getContainer(file)),
                    containerService.getContainer(file).getName(), containerService.getKey(file));
        }
        catch(GenericException e) {
            throw new SwiftExceptionMappingService().map("Download failed", e, file);
        }
        catch(IOException e) {
            throw new DefaultIOExceptionMappingService().map(e, file);
        }
    }

    @Override
    public boolean isResumable() {
        return true;
    }
}
