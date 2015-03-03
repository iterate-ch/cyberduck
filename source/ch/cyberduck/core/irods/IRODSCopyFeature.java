package ch.cyberduck.core.irods;

import ch.cyberduck.core.Path;
import ch.cyberduck.core.exception.BackgroundException;
import ch.cyberduck.core.features.Copy;

import org.irods.jargon.core.exception.JargonException;
import org.irods.jargon.core.pub.DataTransferOperations;
import org.irods.jargon.core.pub.IRODSFileSystemAO;
import org.irods.jargon.core.transfer.DefaultTransferControlBlock;
import org.irods.jargon.core.transfer.TransferStatus;
import org.irods.jargon.core.transfer.TransferStatusCallbackListener;

/**
 * @version $Id$
 */
public class IRODSCopyFeature implements Copy {

    private IRODSSession session;

    public IRODSCopyFeature(final IRODSSession session) {
        this.session = session;
    }

    @Override
    public void copy(final Path source, final Path copy) throws BackgroundException {
        try {
            final IRODSFileSystemAO fs = session.filesystem();
            final DataTransferOperations transfer = fs.getIRODSAccessObjectFactory()
                    .getDataTransferOperations(fs.getIRODSAccount());
            transfer.copy(fs.getIRODSFileFactory().instanceIRODSFile(source.getAbsolute()),
                    fs.getIRODSFileFactory().instanceIRODSFile(copy.getAbsolute()), new TransferStatusCallbackListener() {
                        @Override
                        public FileStatusCallbackResponse statusCallback(final TransferStatus transferStatus) throws JargonException {
                            return FileStatusCallbackResponse.CONTINUE;
                        }

                        @Override
                        public void overallStatusCallback(final TransferStatus transferStatus) throws JargonException {
                            //
                        }

                        @Override
                        public CallbackResponse transferAsksWhetherToForceOperation(final String irodsAbsolutePath, final boolean isCollection) {
                            return CallbackResponse.YES_THIS_FILE;
                        }
                    }, DefaultTransferControlBlock.instance());
        }
        catch(JargonException e) {
            throw new IRODSExceptionMappingService().map("Cannot copy {0}", e, source);
        }
    }
}
