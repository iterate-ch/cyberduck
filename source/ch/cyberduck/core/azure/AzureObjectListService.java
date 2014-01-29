package ch.cyberduck.core.azure;

import ch.cyberduck.core.AttributedList;
import ch.cyberduck.core.ListProgressListener;
import ch.cyberduck.core.ListService;
import ch.cyberduck.core.Path;
import ch.cyberduck.core.PathAttributes;
import ch.cyberduck.core.PathContainerService;
import ch.cyberduck.core.PathNormalizer;
import ch.cyberduck.core.Preferences;
import ch.cyberduck.core.exception.BackgroundException;
import ch.cyberduck.core.exception.NotfoundException;

import org.apache.commons.lang3.StringUtils;

import java.net.URISyntaxException;
import java.util.EnumSet;

import com.microsoft.windowsazure.services.blob.client.BlobListingDetails;
import com.microsoft.windowsazure.services.blob.client.BlobRequestOptions;
import com.microsoft.windowsazure.services.blob.client.CloudBlob;
import com.microsoft.windowsazure.services.blob.client.CloudBlobContainer;
import com.microsoft.windowsazure.services.blob.client.CloudBlobDirectory;
import com.microsoft.windowsazure.services.blob.client.ListBlobItem;
import com.microsoft.windowsazure.services.core.storage.ResultContinuation;
import com.microsoft.windowsazure.services.core.storage.ResultSegment;
import com.microsoft.windowsazure.services.core.storage.RetryNoRetry;
import com.microsoft.windowsazure.services.core.storage.StorageException;

/**
 * @version $Id:$
 */
public class AzureObjectListService implements ListService {

    private AzureSession session;

    private PathContainerService containerService
            = new PathContainerService();

    public AzureObjectListService(AzureSession session) {
        this.session = session;
    }

    @Override
    public AttributedList<Path> list(final Path directory, final ListProgressListener listener) throws BackgroundException {
        try {
            final CloudBlobContainer container = session.getClient().getContainerReference(containerService.getContainer(directory).getName());
            final AttributedList<Path> children = new AttributedList<Path>();
            ResultContinuation token = null;
            ResultSegment<ListBlobItem> result;
            final String prefix;
            if(containerService.isContainer(directory)) {
                prefix = StringUtils.EMPTY;
            }
            else {
                prefix = containerService.getKey(directory).concat(String.valueOf(Path.DELIMITER));
            }
            do {
                final BlobRequestOptions options = new BlobRequestOptions();
                options.setRetryPolicyFactory(new RetryNoRetry());
                result = container.listBlobsSegmented(
                        prefix, false, EnumSet.noneOf(BlobListingDetails.class),
                        Preferences.instance().getInteger("azure.listing.chunksize"), token, options, null);
                for(ListBlobItem object : result.getResults()) {
                    if(new Path(object.getUri().getPath(), Path.DIRECTORY_TYPE).equals(directory)) {
                        continue;
                    }
                    final PathAttributes attributes = new PathAttributes(
                            object instanceof CloudBlobDirectory ? Path.DIRECTORY_TYPE : Path.FILE_TYPE);
                    if(object instanceof CloudBlob) {
                        final CloudBlob blob = (CloudBlob) object;
                        attributes.setSize(blob.getProperties().getLength());
                        attributes.setModificationDate(blob.getProperties().getLastModified().getTime());
                        attributes.setETag(blob.getProperties().getEtag());
                        attributes.setChecksum(blob.getProperties().getContentMD5());
                    }
                    if(object instanceof CloudBlobDirectory) {
                        attributes.setPlaceholder(true);
                    }
                    final Path child = new Path(directory, PathNormalizer.name(object.getUri().getPath()), attributes);
                    children.add(child);
                }
                listener.chunk(children);
                token = result.getContinuationToken();
            }
            while(result.getHasMoreResults());
            return children;
        }
        catch(StorageException e) {
            throw new AzureExceptionMappingService().map("Listing directory failed", e);
        }
        catch(URISyntaxException e) {
            throw new NotfoundException(e.getMessage(), e);
        }
    }
}
