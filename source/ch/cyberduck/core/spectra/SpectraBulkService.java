package ch.cyberduck.core.spectra;

import ch.cyberduck.core.DefaultIOExceptionMappingService;
import ch.cyberduck.core.Path;
import ch.cyberduck.core.PathContainerService;
import ch.cyberduck.core.exception.BackgroundException;
import ch.cyberduck.core.features.Bulk;
import ch.cyberduck.core.s3.S3PathContainerService;
import ch.cyberduck.core.transfer.Transfer;
import ch.cyberduck.core.transfer.TransferStatus;

import java.io.IOException;
import java.security.SignatureException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import com.spectralogic.ds3client.helpers.Ds3ClientHelpers;
import com.spectralogic.ds3client.models.bulk.Ds3Object;
import com.spectralogic.ds3client.networking.FailedRequestException;
import com.spectralogic.ds3client.serializer.XmlProcessingException;

/**
 * @version $Id:$
 */
public class SpectraBulkService implements Bulk<UUID> {

    private final SpectraSession session;

    private final PathContainerService containerService
            = new S3PathContainerService();

    public SpectraBulkService(final SpectraSession session) {
        this.session = session;
    }

    @Override
    public UUID pre(final Transfer.Type type, final Map<Path, TransferStatus> files) throws BackgroundException {
        final Ds3ClientHelpers helper = Ds3ClientHelpers.wrap(session.getClient());
        final Map<Path, List<Ds3Object>> jobs = new HashMap<Path, List<Ds3Object>>();
        for(Map.Entry<Path, TransferStatus> item : files.entrySet()) {
            final Path container = containerService.getContainer(item.getKey());
            if(!jobs.containsKey(container)) {
                jobs.put(container, new ArrayList<Ds3Object>());
            }
            jobs.get(container).add(
                    new Ds3Object(containerService.getKey(item.getKey()), item.getValue().getLength()));
        }
        try {
            for(Map.Entry<Path, List<Ds3Object>> container : jobs.entrySet()) {
                switch(type) {
                    case download:
                        final Ds3ClientHelpers.Job read = helper.startReadJob(
                                container.getKey().getName(), container.getValue());
                        return read.getJobId();
                    case upload:
                        final Ds3ClientHelpers.Job write = helper.startWriteJob(
                                container.getKey().getName(), container.getValue());
                        return write.getJobId();
                    default:
                        throw new BackgroundException();
                }
            }
            return null;
        }
        catch(XmlProcessingException | SignatureException e) {
            throw new BackgroundException(e);
        }
        catch(FailedRequestException e) {
            throw new SpectraExceptionMappingService().map(e);
        }
        catch(IOException e) {
            throw new DefaultIOExceptionMappingService().map(e);
        }
    }
}
