package ch.cyberduck.core.s3;

import ch.cyberduck.core.ConnectionCallback;
import ch.cyberduck.core.LocaleFactory;
import ch.cyberduck.core.Path;
import ch.cyberduck.core.PathContainerService;
import ch.cyberduck.core.ProgressListener;
import ch.cyberduck.core.exception.BackgroundException;
import ch.cyberduck.core.exception.ChecksumException;
import ch.cyberduck.core.features.MultipartWrite;
import ch.cyberduck.core.http.HttpResponseOutputStream;
import ch.cyberduck.core.io.ChecksumCompute;
import ch.cyberduck.core.io.ChecksumComputeFactory;
import ch.cyberduck.core.io.HashAlgorithm;
import ch.cyberduck.core.io.MemorySegementingOutputStream;
import ch.cyberduck.core.preferences.HostPreferences;
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
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

public class S3MultipartWriteFeature implements MultipartWrite<StorageObject> {
    private static final Logger log = LogManager.getLogger(S3MultipartWriteFeature.class);

    private final PathContainerService containerService;
    private final S3Session session;
    private final S3AccessControlListFeature acl;

    public S3MultipartWriteFeature(final S3Session session, final S3AccessControlListFeature acl) {
        this.session = session;
        this.containerService = session.getFeature(PathContainerService.class);
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
            if(log.isDebugEnabled()) {
                log.debug(String.format("Multipart upload started for %s with ID %s", multipart.getObjectKey(), multipart.getUploadId()));
            }
        }
        catch(ServiceException e) {
            throw new S3ExceptionMappingService().map("Upload {0} failed", e, file);
        }
        final MultipartOutputStream proxy = new MultipartOutputStream(multipart, file, status);
        return new HttpResponseOutputStream<StorageObject>(new MemorySegementingOutputStream(proxy,
                new HostPreferences(session.getHost()).getInteger("s3.upload.multipart.size")),
                new S3AttributesAdapter(), status) {
            @Override
            public StorageObject getStatus() {
                if(proxy.getResponse() != null) {
                    if(log.isDebugEnabled()) {
                        log.debug(String.format("Received response %s", proxy.getResponse()));
                    }
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

    @Override
    public Append append(final Path file, final TransferStatus status) throws BackgroundException {
        return new Append(false).withStatus(status);
    }

    @Override
    public ChecksumCompute checksum(final Path file, final TransferStatus status) {
        return ChecksumComputeFactory.get(HashAlgorithm.sha256);
    }

    private final class MultipartOutputStream extends OutputStream {
        /**
         * Completed parts
         */
        private final List<MultipartPart> completed
                = new ArrayList<>();

        private final MultipartUpload multipart;
        private final Path file;
        private final TransferStatus overall;
        private final AtomicBoolean close = new AtomicBoolean();
        private final AtomicReference<ServiceException> canceled = new AtomicReference<>();
        private final AtomicReference<MultipartCompleted> response = new AtomicReference<>();

        private final ChecksumCompute md5 = ChecksumComputeFactory.get(HashAlgorithm.md5);
        private final ChecksumCompute sha256 = ChecksumComputeFactory.get(HashAlgorithm.sha256);

        private Long offset = 0L;
        private int partNumber;

        public MultipartOutputStream(final MultipartUpload multipart, final Path file, final TransferStatus status) {
            this.multipart = multipart;
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
                        final TransferStatus status = new TransferStatus().withParameters(parameters).withLength(len);
                        switch(session.getSignatureVersion()) {
                            case AWS4HMACSHA256:
                                status.setChecksum(sha256.compute(new ByteArrayInputStream(content, off, len), status));
                                break;
                        }
                        status.setSegment(true);
                        final S3Object part = new S3WriteFeature(session, acl).getDetails(file, status);
                        part.addMetadata(HttpHeaders.CONTENT_MD5, md5.compute(new ByteArrayInputStream(content, off, len), status).base64);
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
                        if(log.isDebugEnabled()) {
                            log.debug(String.format("Saved object %s with checksum %s", file, part.getETag()));
                        }
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
                    log.warn(String.format("Skip double close of stream %s", this));
                    return;
                }
                if(null != canceled.get()) {
                    log.warn(String.format("Skip closing with previous failure %s", canceled.get()));
                    return;
                }
                if(completed.isEmpty()) {
                    this.write(new byte[0]);
                }
                final MultipartCompleted complete = session.getClient().multipartCompleteUpload(multipart, completed);
                if(log.isDebugEnabled()) {
                    log.debug(String.format("Completed multipart upload for %s with checksum %s",
                            complete.getObjectKey(), complete.getEtag()));
                }
                if(file.getType().contains(Path.Type.encrypted)) {
                    log.warn(String.format("Skip checksum verification for %s with client side encryption enabled", file));
                }
                else {
                    if(S3Session.isAwsHostname(session.getHost().getHostname())) {
                        final StringBuilder concat = new StringBuilder();
                        for(MultipartPart part : completed) {
                            concat.append(part.getEtag());
                        }
                        final String expected = String.format("%s-%d",
                                ChecksumComputeFactory.get(HashAlgorithm.md5).compute(concat.toString()), completed.size());
                        final String reference = StringUtils.remove(complete.getEtag(), "\"");
                        if(!StringUtils.equalsIgnoreCase(expected, reference)) {
                            throw new ChecksumException(MessageFormat.format(LocaleFactory.localizedString("Upload {0} failed", "Error"), file.getName()),
                                    MessageFormat.format("Mismatch between MD5 hash {0} of uploaded data and ETag {1} returned by the server",
                                            expected, reference));
                        }
                    }
                }
                response.set(complete);
            }
            catch(BackgroundException e) {
                throw new IOException(e);
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
