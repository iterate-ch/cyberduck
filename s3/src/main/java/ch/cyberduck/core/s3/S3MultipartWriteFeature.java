package ch.cyberduck.core.s3;

import ch.cyberduck.core.Cache;
import ch.cyberduck.core.ConnectionCallback;
import ch.cyberduck.core.LocaleFactory;
import ch.cyberduck.core.Path;
import ch.cyberduck.core.PathAttributes;
import ch.cyberduck.core.PathContainerService;
import ch.cyberduck.core.exception.BackgroundException;
import ch.cyberduck.core.exception.ChecksumException;
import ch.cyberduck.core.features.AttributesFinder;
import ch.cyberduck.core.features.Find;
import ch.cyberduck.core.features.MultipartWrite;
import ch.cyberduck.core.features.Write;
import ch.cyberduck.core.http.HttpResponseOutputStream;
import ch.cyberduck.core.io.ChecksumCompute;
import ch.cyberduck.core.io.ChecksumComputeFactory;
import ch.cyberduck.core.io.HashAlgorithm;
import ch.cyberduck.core.io.MD5ChecksumCompute;
import ch.cyberduck.core.io.SegmentingOutputStream;
import ch.cyberduck.core.preferences.Preferences;
import ch.cyberduck.core.preferences.PreferencesFactory;
import ch.cyberduck.core.shared.DefaultAttributesFinderFeature;
import ch.cyberduck.core.shared.DefaultFindFeature;
import ch.cyberduck.core.threading.BackgroundExceptionCallable;
import ch.cyberduck.core.threading.DefaultRetryCallable;
import ch.cyberduck.core.transfer.TransferStatus;

import org.apache.commons.lang3.StringUtils;
import org.apache.http.entity.ByteArrayEntity;
import org.apache.log4j.Logger;
import org.jets3t.service.ServiceException;
import org.jets3t.service.model.MultipartCompleted;
import org.jets3t.service.model.MultipartPart;
import org.jets3t.service.model.MultipartUpload;
import org.jets3t.service.model.S3Object;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;

public class S3MultipartWriteFeature implements MultipartWrite<List<MultipartPart>> {
    private static final Logger log = Logger.getLogger(S3MultipartWriteFeature.class);

    private final Preferences preferences
            = PreferencesFactory.get();

    private final PathContainerService containerService
            = new S3PathContainerService();

    private final S3Session session;
    private final Find finder;
    private final AttributesFinder attributes;

    public S3MultipartWriteFeature(final S3Session session) {
        this(session, new DefaultFindFeature(session), new DefaultAttributesFinderFeature(session));
    }

    public S3MultipartWriteFeature(final S3Session session, final Find finder, final AttributesFinder attributes) {
        this.session = session;
        this.finder = finder;
        this.attributes = attributes;
    }

    @Override
    public HttpResponseOutputStream<List<MultipartPart>> write(final Path file, final TransferStatus status, final ConnectionCallback callback) throws BackgroundException {
        final S3Object object = new S3WriteFeature(session, new S3DisabledMultipartService())
                .getDetails(containerService.getKey(file), status);
        // ID for the initiated multipart upload.
        final MultipartUpload multipart;
        try {
            multipart = session.getClient().multipartStartUpload(
                    containerService.getContainer(file).getName(), object);
            if(log.isDebugEnabled()) {
                log.debug(String.format("Multipart upload started for %s with ID %s",
                        multipart.getObjectKey(), multipart.getUploadId()));
            }
        }
        catch(ServiceException e) {
            throw new S3ExceptionMappingService().map("Upload {0} failed", e, file);
        }
        final MultipartOutputStream proxy = new MultipartOutputStream(multipart, file, status);
        return new HttpResponseOutputStream<List<MultipartPart>>(new SegmentingOutputStream(proxy,
                preferences.getInteger("s3.upload.multipart.partsize.minimum"))) {
            @Override
            public List<MultipartPart> getStatus() throws BackgroundException {
                return proxy.getCompleted();
            }
        };
    }

    @Override
    public Append append(final Path file, final Long length, final Cache<Path> cache) throws BackgroundException {
        if(finder.withCache(cache).find(file)) {
            final PathAttributes attributes = this.attributes.withCache(cache).find(file);
            return new Append(false, true).withSize(attributes.getSize()).withChecksum(attributes.getChecksum());
        }
        return Write.notfound;
    }

    @Override
    public boolean temporary() {
        return false;
    }

    @Override
    public boolean random() {
        return false;
    }

    private final class MultipartOutputStream extends OutputStream {
        /**
         * Completed parts
         */
        private final List<MultipartPart> completed
                = new ArrayList<MultipartPart>();

        private final MultipartUpload multipart;
        private final Path file;
        private final TransferStatus overall;
        private final AtomicBoolean close = new AtomicBoolean();
        private int partNumber;

        public MultipartOutputStream(final MultipartUpload multipart, final Path file, final TransferStatus status) {
            this.multipart = multipart;
            this.file = file;
            this.overall = status;
        }

        public List<MultipartPart> getCompleted() {
            return completed;
        }

        @Override
        public void write(final int value) throws IOException {
            throw new IOException(new UnsupportedOperationException());
        }

        @Override
        public void write(final byte[] content, final int off, final int len) throws IOException {
            try {
                completed.add(new DefaultRetryCallable<MultipartPart>(new BackgroundExceptionCallable<MultipartPart>() {
                    @Override
                    public MultipartPart call() throws BackgroundException {
                        final Map<String, String> parameters = new HashMap<String, String>();
                        parameters.put("uploadId", multipart.getUploadId());
                        parameters.put("partNumber", String.valueOf(++partNumber));
                        final TransferStatus status = new TransferStatus().withParameters(parameters).length(len);
                        switch(session.getSignatureVersion()) {
                            case AWS4HMACSHA256:
                                status.setChecksum(S3MultipartWriteFeature.this.checksum()
                                        .compute(new ByteArrayInputStream(content, off, len), status)
                                );
                                break;
                        }
                        status.setSegment(true);
                        final S3Object part = new S3WriteFeature(session, new S3DisabledMultipartService())
                                .getDetails(containerService.getKey(file), status);
                        try {
                            session.getClient().putObjectWithRequestEntityImpl(
                                    containerService.getContainer(file).getName(), part,
                                    new ByteArrayEntity(content, off, len), parameters);
                        }
                        catch(ServiceException e) {
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
                }, overall).call());
            }
            catch(Exception e) {
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
                if(completed.isEmpty()) {
                    session.getClient().multipartAbortUpload(multipart);
                }
                else {
                    final MultipartCompleted complete = session.getClient().multipartCompleteUpload(multipart, completed);
                    if(log.isDebugEnabled()) {
                        log.debug(String.format("Completed multipart upload for %s with checksum %s",
                                complete.getObjectKey(), complete.getEtag()));
                    }
                    if(file.getType().contains(Path.Type.encrypted)) {
                        log.warn(String.format("Skip checksum verification for %s with client side encryption enabled", file));
                    }
                    else {
                        final StringBuilder concat = new StringBuilder();
                        for(MultipartPart part : completed) {
                            concat.append(part.getEtag());
                        }
                        final String expected = String.format("%s-%d",
                                new MD5ChecksumCompute().compute(concat.toString(), overall), completed.size());
                        final String reference;
                        if(complete.getEtag().startsWith("\"") && complete.getEtag().endsWith("\"")) {
                            reference = complete.getEtag().substring(1, complete.getEtag().length() - 1);
                        }
                        else {
                            reference = complete.getEtag();
                        }
                        if(!expected.equals(reference)) {
                            if(session.getHost().getHostname().endsWith(preferences.getProperty("s3.hostname.default"))) {
                                throw new ChecksumException(MessageFormat.format(LocaleFactory.localizedString("Upload {0} failed", "Error"), file.getName()),
                                        MessageFormat.format("Mismatch between MD5 hash {0} of uploaded data and ETag {1} returned by the server",
                                                expected, reference));
                            }
                            else {
                                log.warn(String.format("Mismatch between MD5 hash %s of uploaded data and ETag %s returned by the server", expected, reference));
                            }
                        }
                    }
                }
            }
            catch(ChecksumException e) {
                throw new IOException(e.getMessage(), e);
            }
            catch(ServiceException e) {
                throw new IOException(e.getErrorMessage(), new S3ExceptionMappingService().map(e));
            }
            finally {
                close.set(true);
            }
        }
    }

    @Override
    public ChecksumCompute checksum() {
        return ChecksumComputeFactory.get(HashAlgorithm.sha256);
    }
}
