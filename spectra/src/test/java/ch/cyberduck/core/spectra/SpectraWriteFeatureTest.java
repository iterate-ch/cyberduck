/*
 * Copyright (c) 2015-2016 Spectra Logic Corporation. All rights reserved.
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

package ch.cyberduck.core.spectra;

import ch.cyberduck.core.AbstractTestCase;
import ch.cyberduck.core.Host;
import ch.cyberduck.core.Path;
import ch.cyberduck.core.PathAttributes;
import ch.cyberduck.core.PathCache;
import ch.cyberduck.core.exception.BackgroundException;
import ch.cyberduck.core.features.Attributes;
import ch.cyberduck.core.features.Find;
import ch.cyberduck.core.features.Write;
import ch.cyberduck.core.s3.S3Session;
import ch.cyberduck.core.s3.S3WriteFeature;
import ch.cyberduck.core.ssl.DefaultX509KeyManager;
import ch.cyberduck.core.ssl.DisabledX509TrustManager;

import org.junit.Test;

import java.util.EnumSet;

import static org.junit.Assert.*;

/**
 * @version $Id$
 */
public class SpectraWriteFeatureTest extends AbstractTestCase {

    @Test
    public void testAppendBelowLimit() throws Exception {
        final SpectraSession session = new SpectraSession(new Host(new SpectraProtocol()), new DisabledX509TrustManager(),
                new DefaultX509KeyManager());
        final S3WriteFeature feature = new S3WriteFeature(session, null, new Find() {
            @Override
            public boolean find(final Path file) throws BackgroundException {
                return true;
            }

            @Override
            public Find withCache(final PathCache cache) {
                return this;
            }
        }, new Attributes() {
            @Override
            public PathAttributes find(final Path file) throws BackgroundException {
                return new PathAttributes();
            }

            @Override
            public Attributes withCache(final PathCache cache) {
                return this;
            }
        });
        final Write.Append append = feature.append(new Path("/p", EnumSet.of(Path.Type.file)), 0L, PathCache.empty());
        assertFalse(append.append);
    }

    @Test
    public void testSize() throws Exception {
        final S3Session session = new SpectraSession(new Host(new SpectraProtocol()), new DisabledX509TrustManager(),
                new DefaultX509KeyManager());
        final S3WriteFeature feature = new S3WriteFeature(session, null, new Find() {
            @Override
            public boolean find(final Path file) throws BackgroundException {
                return true;
            }

            @Override
            public Find withCache(final PathCache cache) {
                return this;
            }
        }, new Attributes() {
            @Override
            public PathAttributes find(final Path file) throws BackgroundException {
                final PathAttributes attributes = new PathAttributes();
                attributes.setSize(3L);
                return attributes;
            }

            @Override
            public Attributes withCache(final PathCache cache) {
                return this;
            }
        });
        final Write.Append append = feature.append(new Path("/p", EnumSet.of(Path.Type.file)), 0L, PathCache.empty());
        assertFalse(append.append);
        assertTrue(append.override);
        assertEquals(3L, append.size, 0L);
    }
}
