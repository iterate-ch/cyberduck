package ch.cyberduck.core.irods;

import ch.cyberduck.core.PasswordCallback;
import ch.cyberduck.core.Path;
import ch.cyberduck.core.exception.BackgroundException;
import ch.cyberduck.core.exception.NotfoundException;
import ch.cyberduck.core.features.Delete;
import ch.cyberduck.core.transfer.TransferStatus;

import org.irods.irods4j.high_level.vfs.IRODSFilesystem;
import org.irods.irods4j.high_level.vfs.IRODSFilesystem.RemoveOptions;
import org.irods.irods4j.high_level.vfs.ObjectStatus;
import org.irods.irods4j.high_level.vfs.ObjectStatus.ObjectType;
import org.irods.irods4j.low_level.api.IRODSException;

import java.io.IOException;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;
import java.util.Map;

public class IRODSDeleteFeature implements Delete {

    private final IRODSSession session;

    public IRODSDeleteFeature(IRODSSession session) {
        this.session = session;
    }

    @Override
    public void delete(final Map<Path, TransferStatus> files, final PasswordCallback prompt, final Callback callback) throws BackgroundException {
        final List<Path> deleted = new ArrayList<Path>();
        for(Path file : files.keySet()) {
            boolean skip = false;
            for(Path d : deleted) {
                if(file.isChild(d)) {
                    skip = true;
                    break;
                }
            }
            if(skip) {
                continue;
            }
            deleted.add(file);
            callback.delete(file);
            final String absolute = file.getAbsolute();
            try {
                if(!IRODSFilesystem.exists(this.session.getClient().getRcComm(), absolute)) {
                    throw new NotfoundException(String.format("%s doesn't exist", absolute));
                }
                ObjectStatus status = IRODSFilesystem.status(this.session.getClient().getRcComm(), absolute);
                if(status.equals(ObjectType.DATA_OBJECT)) {
                    IRODSFilesystem.remove(this.session.getClient().getRcComm(), absolute, RemoveOptions.NO_TRASH);
                }
                else if(status.equals(ObjectType.COLLECTION)) {
                    IRODSFilesystem.removeAll(this.session.getClient().getRcComm(), absolute, RemoveOptions.NO_TRASH);
                }
            }
            catch(IOException | IRODSException e) {
                throw new IRODSExceptionMappingService().map("Cannot delete {0}", e, file);
            }
        }
    }

    @Override
    public EnumSet<Flags> features() {
        return EnumSet.of(Flags.recursive);
    }
}
