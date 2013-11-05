package ch.cyberduck.core.s3;

import ch.cyberduck.core.Path;
import ch.cyberduck.core.PathContainerService;
import ch.cyberduck.core.exception.BackgroundException;

import org.apache.log4j.Logger;
import org.jets3t.service.MultipartUploadChunk;
import org.jets3t.service.S3ServiceException;
import org.jets3t.service.model.MultipartPart;
import org.jets3t.service.model.MultipartUpload;

import java.util.List;

/**
 * @version $Id:$
 */
public class S3MultipartService {
    private static final Logger log = Logger.getLogger(S3MultipartService.class);

    private S3Session session;

    private PathContainerService containerService
            = new PathContainerService();

    public S3MultipartService(S3Session session) {
        this.session = session;
    }

    public MultipartUpload find(final Path file) throws BackgroundException {
        // This operation lists in-progress multipart uploads. An in-progress multipart upload is a
        // multipart upload that has been initiated, using the Initiate Multipart Upload request, but has
        // not yet been completed or aborted.
        String nextUploadIdMarker = null;
        String nextKeyMarker = null;
        do {
            final MultipartUploadChunk chunk;
            try {
                chunk = session.getClient().multipartListUploadsChunked(
                        containerService.getContainer(file).getName(), containerService.getKey(file),
                        null, nextKeyMarker, nextUploadIdMarker, null, true);
            }
            catch(S3ServiceException e) {
                throw new ServiceExceptionMappingService().map("Upload failed", e, file);
            }
            for(MultipartUpload upload : chunk.getUploads()) {
                if(log.isInfoEnabled()) {
                    log.info(String.format("Resume multipart upload %s", upload.getUploadId()));
                }
                return upload;
            }
            nextKeyMarker = chunk.getPriorLastKey();
            nextUploadIdMarker = chunk.getPriorLastIdMarker();
        }
        while(nextUploadIdMarker != null);
        return null;
    }

    public List<MultipartPart> list(final MultipartUpload multipart) throws BackgroundException {
        if(log.isInfoEnabled()) {
            log.info(String.format("List completed parts of %s", multipart.getUploadId()));
        }
        // This operation lists the parts that have been uploaded for a specific multipart upload.
        try {
            return session.getClient().multipartListParts(multipart);
        }
        catch(S3ServiceException e) {
            throw new ServiceExceptionMappingService().map("Upload failed", e);
        }
    }
}
