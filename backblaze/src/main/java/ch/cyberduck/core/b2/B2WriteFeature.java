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
import ch.cyberduck.core.http.ResponseOutputStream;
import ch.cyberduck.core.io.Checksum;
import ch.cyberduck.core.shared.DefaultAttributesFinderFeature;
import ch.cyberduck.core.shared.DefaultFindFeature;
import ch.cyberduck.core.transfer.TransferStatus;

import org.apache.http.entity.AbstractHttpEntity;

import java.io.IOException;

import synapticloop.b2.exception.B2ApiException;
import synapticloop.b2.response.B2FileResponse;
import synapticloop.b2.response.B2GetUploadUrlResponse;

public class B2WriteFeature extends AbstractHttpWriteFeature<B2FileResponse> implements Write {

    private final PathContainerService containerService
            = new B2PathContainerService();

    private final B2Session session;

    private final Find finder;

    private final AttributesFinder attributes;

    private final ThreadLocal<B2GetUploadUrlResponse> urls
            = new ThreadLocal<B2GetUploadUrlResponse>();

    public B2WriteFeature(final B2Session session) {
        this(session, session.getFeature(Find.class, new DefaultFindFeature(session)), session.getFeature(AttributesFinder.class, new DefaultAttributesFinderFeature(session)));
    }

    public B2WriteFeature(final B2Session session, final Find finder, final AttributesFinder attributes) {
        super(finder, attributes);
        this.session = session;
        this.finder = finder;
        this.attributes = attributes;
    }

    @Override
    public ResponseOutputStream<B2FileResponse> write(final Path file, final TransferStatus status) throws BackgroundException {
        try {
            final B2GetUploadUrlResponse uploadUrl;
            if(null == urls.get()) {
                uploadUrl = session.getClient().getUploadUrl(new B2FileidProvider(session).getFileid(containerService.getContainer(file)));
                urls.set(uploadUrl);
            }
            else {
                uploadUrl = urls.get();
            }
            // Submit store call to background thread
            final DelayedHttpEntityCallable<B2FileResponse> command = new DelayedHttpEntityCallable<B2FileResponse>() {
                /**
                 * @return The SHA-1 returned by the server for the uploaded object
                 */
                @Override
                public B2FileResponse call(final AbstractHttpEntity entity) throws BackgroundException {
                    try {
                        final Checksum checksum = status.getChecksum();
                        if(null == checksum) {
                            throw new InteroperabilityException(String.format("Missing SHA1 checksum for file %s", file.getName()));
                        }
                        return session.getClient().uploadFile(uploadUrl,
                                containerService.getKey(file),
                                entity, checksum.toString(),
                                status.getMime(),
                                status.getMetadata());
                    }
                    catch(B2ApiException e) {
                        urls.remove();
                        throw new B2ExceptionMappingService(session).map("Upload {0} failed", e, file);
                    }
                    catch(IOException e) {
                        urls.remove();
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
        catch(B2ApiException e) {
            throw new B2ExceptionMappingService(session).map("Upload {0} failed", e, file);
        }
        catch(IOException e) {
            throw new DefaultIOExceptionMappingService().map("Upload {0} failed", e, file);
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
    public Append append(final Path file, final Long length, final PathCache cache) throws BackgroundException {
        if(finder.withCache(cache).find(file)) {
            final PathAttributes attributes = this.attributes.withCache(cache).find(file);
            return new Append(false, true).withSize(attributes.getSize()).withChecksum(attributes.getChecksum());
        }
        return Write.notfound;
    }
}
