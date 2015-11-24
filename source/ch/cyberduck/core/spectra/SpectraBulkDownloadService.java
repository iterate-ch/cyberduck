package ch.cyberduck.core.spectra;

import ch.cyberduck.core.DefaultIOExceptionMappingService;
import ch.cyberduck.core.Path;
import ch.cyberduck.core.PathContainerService;
import ch.cyberduck.core.exception.BackgroundException;
import ch.cyberduck.core.features.Bulk;
import ch.cyberduck.core.s3.S3PathContainerService;
import ch.cyberduck.core.transfer.TransferStatus;

import java.io.IOException;
import java.security.SignatureException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.spectralogic.ds3client.helpers.Ds3ClientHelpers;
import com.spectralogic.ds3client.models.bulk.Ds3Object;
import com.spectralogic.ds3client.networking.FailedRequestException;
import com.spectralogic.ds3client.serializer.XmlProcessingException;

/**
 * @version $Id:$
 */
public class SpectraBulkDownloadService implements Bulk {

    private final SpectraSession session;

    private final PathContainerService containerService
            = new S3PathContainerService();

    public SpectraBulkDownloadService(final SpectraSession session) {
        this.session = session;
    }

    @Override
    public void pre(final Map<Path, TransferStatus> files) throws BackgroundException {
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
                final Ds3ClientHelpers.Job job = helper.startReadJob(
                        container.getKey().getName(), container.getValue());
            }
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
