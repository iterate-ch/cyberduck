package ch.cyberduck.core.b2;

/*
 * Copyright (c) 2002-2016 iterate GmbH. All rights reserved.
 * https://cyberduck.io/
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 */

import ch.cyberduck.core.DefaultIOExceptionMappingService;
import ch.cyberduck.core.Path;
import ch.cyberduck.core.PathAttributes;
import ch.cyberduck.core.PathCache;
import ch.cyberduck.core.PathContainerService;
import ch.cyberduck.core.exception.BackgroundException;
import ch.cyberduck.core.exception.InteroperabilityException;
import ch.cyberduck.core.features.AttributesFinder;
import ch.cyberduck.core.features.Find;
import ch.cyberduck.core.features.Write;
import ch.cyberduck.core.http.AbstractHttpWriteFeature;
import ch.cyberduck.core.http.DelayedHttpEntityCallable;
import ch.cyberduck.core.http.HttpResponseOutputStream;
import ch.cyberduck.core.io.Checksum;
import ch.cyberduck.core.io.ChecksumCompute;
import ch.cyberduck.core.io.ChecksumComputeFactory;
import ch.cyberduck.core.io.HashAlgorithm;
import ch.cyberduck.core.preferences.PreferencesFactory;
import ch.cyberduck.core.shared.DefaultAttributesFinderFeature;
import ch.cyberduck.core.shared.DefaultFindFeature;
import ch.cyberduck.core.transfer.TransferStatus;

import org.apache.http.entity.AbstractHttpEntity;
import org.apache.log4j.Logger;

import java.io.IOException;

import synapticloop.b2.exception.B2ApiException;
import synapticloop.b2.response.B2GetUploadPartUrlResponse;
import synapticloop.b2.response.B2GetUploadUrlResponse;
import synapticloop.b2.response.BaseB2Response;

public class B2WriteFeature extends AbstractHttpWriteFeature<BaseB2Response> implements Write<BaseB2Response> {
    private static final Logger log = Logger.getLogger(B2WriteFeature.class);

    private final PathContainerService containerService
            = new B2PathContainerService();

    private final B2Session session;

    private final Find finder;

    private final AttributesFinder attributes;

    private final Long threshold;

    private final ThreadLocal<B2GetUploadUrlResponse> urls
            = new ThreadLocal<B2GetUploadUrlResponse>();

    public B2WriteFeature(final B2Session session) {
        this(session, PreferencesFactory.get().getLong("b2.upload.largeobject.threshold"));
    }

    public B2WriteFeature(final B2Session session, final Long threshold) {
        this(session, session.getFeature(Find.class, new DefaultFindFeature(session)), session.getFeature(AttributesFinder.class, new DefaultAttributesFinderFeature(session)), threshold);
    }

    public B2WriteFeature(final B2Session session, final Find finder, final AttributesFinder attributes, final Long threshold) {
        super(finder, attributes);
        this.session = session;
        this.finder = finder;
        this.attributes = attributes;
        this.threshold = threshold;
    }

    @Override
    public HttpResponseOutputStream<BaseB2Response> write(final Path file, final TransferStatus status) throws BackgroundException {
        // Submit store call to background thread
        final DelayedHttpEntityCallable<BaseB2Response> command = new DelayedHttpEntityCallable<BaseB2Response>() {
            /**
             * @return The SHA-1 returned by the server for the uploaded object
             */
            @Override
            public BaseB2Response call(final AbstractHttpEntity entity) throws BackgroundException {
                try {
                    final Checksum checksum = status.getChecksum();
                    if(null == checksum) {
                        throw new InteroperabilityException(String.format("Missing SHA1 checksum for file %s", file.getName()));
                    }
                    if(status.isSegment()) {
                        final B2GetUploadPartUrlResponse uploadUrl
                                = session.getClient().getUploadPartUrl(new B2FileidProvider(session).getFileid(file));
                        return session.getClient().uploadLargeFilePart(uploadUrl, status.getPart(), entity, checksum.toString());
                    }
                    else {
                        final B2GetUploadUrlResponse uploadUrl;
                        if(null == urls.get()) {
                            uploadUrl = session.getClient().getUploadUrl(new B2FileidProvider(session).getFileid(containerService.getContainer(file)));
                            urls.set(uploadUrl);
                        }
                        else {
                            uploadUrl = urls.get();
                        }
                        try {
                            return session.getClient().uploadFile(uploadUrl,
                                    file.isDirectory() ? String.format("%s%s", containerService.getKey(file), B2DirectoryFeature.PLACEHOLDER) : containerService.getKey(file),
                                    entity, checksum.toString(),
                                    status.getMime(),
                                    status.getMetadata());
                        }
                        catch(B2ApiException e) {
                            urls.remove();
                            throw e;
                        }
                    }
                }
                catch(B2ApiException e) {
                    throw new B2ExceptionMappingService(session).map("Upload {0} failed", e, file);
                }
                catch(IOException e) {
                    throw new DefaultIOExceptionMappingService().map("Upload {0} failed", e, file);
                }
            }

            @Override
            public long getContentLength() {
                return status.getLength();
            }
        };
        return this.write(file, status, command);
    }

    protected boolean threshold(final Long length) {
        if(length > threshold) {
            if(!PreferencesFactory.get().getBoolean("b2.upload.largeobject")) {
                // Disabled by user
                if(length < PreferencesFactory.get().getLong("b2.upload.largeobject.required.threshold")) {
                    log.warn("Large upload is disabled with property b2.upload.largeobject.required.threshold");
                    return false;
                }
            }
            return true;
        }
        else {
            // Below threshold
            return false;
        }
    }

    @Override
    public boolean temporary() {
        return false;
    }

    @Override
    public boolean random() {
        return false;
    }

    @Override
    public ChecksumCompute checksum() {
        return ChecksumComputeFactory.get(HashAlgorithm.sha1);
    }

    @Override
    public Append append(final Path file, final Long length, final PathCache cache) throws BackgroundException {
        if(finder.withCache(cache).find(file)) {
            final PathAttributes attributes = this.attributes.withCache(cache).find(file);
            return new Append(false, true).withSize(attributes.getSize()).withChecksum(attributes.getChecksum());
        }
        return Write.notfound;
    }
}
