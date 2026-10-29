package ch.cyberduck.core.s3;

import ch.cyberduck.core.ConnectionCallback;
import ch.cyberduck.core.Path;
import ch.cyberduck.core.PathContainerService;
import ch.cyberduck.core.ProgressListener;
import ch.cyberduck.core.exception.BackgroundException;
import ch.cyberduck.core.features.MultipartWrite;
import ch.cyberduck.core.http.HttpResponseOutputStream;
import ch.cyberduck.core.io.ChecksumCompute;
import ch.cyberduck.core.io.Checksum;
import ch.cyberduck.core.io.ChecksumComputeFactory;
import ch.cyberduck.core.io.HashAlgorithm;
import ch.cyberduck.core.io.MemorySegementingOutputStream;
import ch.cyberduck.core.preferences.HostPreferencesFactory;
import ch.cyberduck.core.threading.BackgroundActionState;
import ch.cyberduck.core.threading.BackgroundExceptionCallable;
import ch.cyberduck.core.threading.DefaultRetryCallable;
import ch.cyberduck.core.transfer.TransferStatus;

import org.apache.commons.lang3.StringUtils;
import org.apache.http.HttpHeaders;
import org.apache.http.entity.ByteArrayEntity;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jets3t.service.ServiceException;
import org.jets3t.service.model.MultipartCompleted;
import org.jets3t.service.model.MultipartPart;
import org.jets3t.service.model.MultipartUpload;
import org.jets3t.service.model.S3Object;
import org.jets3t.service.model.StorageObject;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Date;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

public class S3MultipartWriteFeature implements MultipartWrite<StorageObject> {
    private static final Logger log = LogManager.getLogger(S3MultipartWriteFeature.class);

    private final PathContainerService containerService;
    private final S3Session session;
    private final S3AccessControlListFeature acl;

    public S3MultipartWriteFeature(final S3Session session, final S3AccessControlListFeature acl) {
        this.session = session;
        this.containerService = new S3PathContainerService(session.getHost());
        this.acl = acl;
    }

    @Override
    public HttpResponseOutputStream<StorageObject> write(final Path file, final TransferStatus status, final ConnectionCallback callback) throws BackgroundException {
        final S3Object object = new S3WriteFeature(session, acl).getDetails(file, status);
        // ID for the initiated multipart upload.
        final MultipartUpload multipart;
        try {
            final Path bucket = containerService.getContainer(file);
            multipart = session.getClient().multipartStartUpload(
                    bucket.isRoot() ? StringUtils.EMPTY : bucket.getName(), object);
            log.debug("Multipart upload started for {} with ID {}", multipart.getObjectKey(), multipart.getUploadId());
            multipart.setBucketName(bucket.isRoot() ? StringUtils.EMPTY : bucket.getName());
        }
        catch(ServiceException e) {
            throw new S3ExceptionMappingService().map("Upload {0} failed", e, file);
        }
        final MultipartOutputStream proxy = new MultipartOutputStream(multipart, object, file, status);
        return new HttpResponseOutputStream<StorageObject>(new MemorySegementingOutputStream(proxy,
                HostPreferencesFactory.get(session.getHost()).getInteger("s3.upload.multipart.size")),
                new S3AttributesAdapter(session.getHost()), status) {
            @Override
            public StorageObject getStatus() {
                if(proxy.getResponse() != null) {
                    log.debug("Received response {}", proxy.getResponse());
                    object.setContentLength(proxy.getOffset());
                    object.setETag(proxy.getResponse().getEtag());
                    if(proxy.getResponse().getVersionId() != null) {
                        object.addMetadata(S3Object.S3_VERSION_ID, proxy.getResponse().getVersionId());
                    }
                }
                return object;
            }
        };
    }

    private final class MultipartOutputStream extends OutputStream {
        /**
         * Completed parts
         */
        private final List<MultipartPart> completed
                = new ArrayList<>();

        /**
         * MD5 of completed parts
         */
        private final Map<Integer, Checksum> checksums
                = new ConcurrentHashMap<>();

        private final MultipartUpload multipart;
        private final S3Object object;
        private final Path file;
        private final TransferStatus overall;
        private final AtomicBoolean close = new AtomicBoolean();
        private final AtomicReference<ServiceException> canceled = new AtomicReference<>();
        private final AtomicReference<MultipartCompleted> response = new AtomicReference<>();

        private Long offset = 0L;
        private int partNumber;

        public MultipartOutputStream(final MultipartUpload multipart, final S3Object object, final Path file, final TransferStatus status) {
            this.multipart = multipart;
            this.object = object;
            this.file = file;
            this.overall = status;
        }

        @Override
        public void write(final int value) throws IOException {
            throw new IOException(new UnsupportedOperationException());
        }

        @Override
        public void write(final byte[] content, final int off, final int len) throws IOException {
            try {
                completed.add(new DefaultRetryCallable<MultipartPart>(session.getHost(), new BackgroundExceptionCallable<MultipartPart>() {
                    @Override
                    public MultipartPart call() throws BackgroundException {
                        final Map<String, String> parameters = new HashMap<>();
                        parameters.put("uploadId", multipart.getUploadId());
                        parameters.put("partNumber", String.valueOf(++partNumber));
                        final TransferStatus status = new TransferStatus().setParameters(parameters).setLength(len);
                        status.setParent(overall);
                        status.setSegment(true);
                        final S3Object part = new S3WriteFeature(session, acl).getDetails(file, status);
                        final Checksum md5 = ChecksumComputeFactory.get(HashAlgorithm.md5).compute(new ByteArrayInputStream(content, off, len), status);
                        checksums.put(partNumber, md5);
                        part.addMetadata(HttpHeaders.CONTENT_MD5, md5.base64);
                        try {
                            final Path bucket = containerService.getContainer(file);
                            session.getClient().putObjectWithRequestEntityImpl(
                                    bucket.isRoot() ? StringUtils.EMPTY : bucket.getName(), part,
                                    new ByteArrayEntity(content, off, len), parameters);
                        }
                        catch(ServiceException e) {
                            canceled.set(e);
                            throw new S3ExceptionMappingService().map("Upload {0} failed", e, file);
                        }
                        log.debug("Saved object {} with checksum {}", file, part.getETag());
                        return new MultipartPart(partNumber,
                                null == part.getLastModifiedDate() ? new Date(System.currentTimeMillis()) : part.getLastModifiedDate(),
                                null == part.getETag() ? StringUtils.EMPTY : part.getETag(),
                                part.getContentLength());
                    }
                }, overall) {
                    @Override
                    public boolean retry(final BackgroundException failure, final ProgressListener progress, final BackgroundActionState cancel) {
                        if(super.retry(failure, progress, cancel)) {
                            canceled.set(null);
                            return true;
                        }
                        return false;
                    }
                }.call());
                offset += len;
            }
            catch(BackgroundException e) {
                throw new IOException(e.getMessage(), e);
            }
        }

        @Override
        public void close() throws IOException {
            try {
                if(close.get()) {
                    log.warn("Skip double close of stream {}", this);
                    return;
                }
                if(null != canceled.get()) {
                    log.warn("Skip closing with previous failure {}", canceled.get().getMessage());
                    return;
                }
                if(completed.isEmpty()) {
                    this.write(new byte[0]);
                }
                final MultipartCompleted complete = session.getClient().multipartCompleteUpload(multipart, completed);
                log.debug("Completed multipart upload for {} with checksum {}", complete.getObjectKey(), complete.getEtag());
                response.set(complete);
                // Only objects stored as plaintext or encrypted with SSE-S3 have an ETag derived from the MD5 of the data
                object.setETag(complete.getEtag());
                if(HashAlgorithm.md5_multipart == new S3AttributesAdapter(session.getHost()).toAttributes(object).getChecksum().algorithm) {
                    final Checksum expected = S3MultipartUploadService.composite(completed, checksums);
                    if(!Checksum.NONE.equals(expected)) {
                        // Compared with the checksum returned by the server by the upload filter when the upload is complete
                        overall.setVerify(expected.algorithm).setDigest(expected);
                    }
                }
            }
            catch(ServiceException e) {
                throw new IOException(e.getErrorMessage(), new S3ExceptionMappingService().map(e));
            }
            finally {
                close.set(true);
            }
        }

        public MultipartCompleted getResponse() {
            return response.get();
        }

        public Long getOffset() {
            return offset;
        }

        @Override
        public String toString() {
            final StringBuilder sb = new StringBuilder("MultipartOutputStream{");
            sb.append("multipart=").append(multipart);
            sb.append(", file=").append(file);
            sb.append('}');
            return sb.toString();
        }
    }

    @Override
    public EnumSet<Flags> features(final Path file) {
        return EnumSet.of(Flags.timestamp);
    }
}
