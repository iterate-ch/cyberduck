package ch.cyberduck.core.s3;

/*
 * Copyright (c) 2002-2013 David Kocher. All rights reserved.
 * http://cyberduck.ch/
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
 *
 * Bug fixes, suggestions and comments should be sent to feedback@cyberduck.ch
 */

import ch.cyberduck.core.AsciiRandomStringService;
import ch.cyberduck.core.DisabledLoginCallback;
import ch.cyberduck.core.Path;
import ch.cyberduck.core.TranscriptListener;
import ch.cyberduck.core.features.Delete;
import ch.cyberduck.test.IntegrationTest;

import org.apache.commons.lang3.StringUtils;
import org.junit.Test;
import org.junit.experimental.categories.Category;

import java.util.Collections;
import java.util.EnumSet;
import java.util.concurrent.atomic.AtomicBoolean;

import static org.junit.Assert.assertTrue;

@Category(IntegrationTest.class)
public class S3BucketCreateServiceTest extends AbstractS3Test {

    @Test
    public void testCreateBucket() throws Exception {
        final AtomicBoolean header = new AtomicBoolean();
        final AtomicBoolean signature = new AtomicBoolean();
        session.withListener(new TranscriptListener() {
            @Override
            public void log(final Type request, final String message) {
                if(StringUtils.contains(message, "Expect: 100-continue")) {
                    header.set(true);
                }
                if(StringUtils.contains(message, "SignedHeaders=content-type;date;expect;host;x-amz-acl;x-amz-content-sha256;x-amz-date")) {
                    signature.set(true);
                }
            }
        });
        final S3BucketCreateService create = new S3BucketCreateService(session);
        final Path bucket = new Path(new AsciiRandomStringService().random(), EnumSet.of(Path.Type.directory, Path.Type.volume));
        create.create(bucket, "eu-central-1");
        assertTrue(header.get());
        bucket.attributes().setRegion("eu-central-1");
        assertTrue(new S3FindFeature(session, new S3AccessControlListFeature(session)).find(bucket));
        new S3DefaultDeleteFeature(session).delete(Collections.<Path>singletonList(bucket), new DisabledLoginCallback(), new Delete.DisabledCallback());
    }
}
