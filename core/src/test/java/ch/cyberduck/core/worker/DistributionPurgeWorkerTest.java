package ch.cyberduck.core.worker;

/*
 * Copyright (c) 2002-2026 iterate GmbH. All rights reserved.
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

import ch.cyberduck.core.Host;
import ch.cyberduck.core.LoginCallback;
import ch.cyberduck.core.NullSession;
import ch.cyberduck.core.Path;
import ch.cyberduck.core.Session;
import ch.cyberduck.core.TestProtocol;
import ch.cyberduck.core.cdn.Distribution;
import ch.cyberduck.core.cdn.DistributionConfiguration;
import ch.cyberduck.core.cdn.features.Purge;

import org.junit.Test;

import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumSet;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class DistributionPurgeWorkerTest {

    @Test
    public void testPurgeOnlySupportedMethods() throws Exception {
        final List<Distribution.Method> invalidated = new ArrayList<>();
        final Purge purge = (container, method, files, prompt) -> invalidated.add(method);
        final DistributionConfiguration cdn = new DistributionConfiguration() {
            @Override
            public void write(final Path container, final Distribution configuration, final LoginCallback prompt) {
                throw new UnsupportedOperationException();
            }

            @Override
            public Distribution read(final Path container, final Distribution.Method method, final LoginCallback prompt) {
                throw new UnsupportedOperationException();
            }

            @Override
            public List<Distribution.Method> getMethods(final Path container) {
                return Collections.singletonList(Distribution.DOWNLOAD);
            }

            @Override
            public String getName() {
                return "Test";
            }

            @Override
            @SuppressWarnings("unchecked")
            public <T> T getFeature(final Class<T> type, final Distribution.Method method) {
                if(type == Purge.class) {
                    return (T) purge;
                }
                return null;
            }
        };
        final Session<?> session = new NullSession(new Host(new TestProtocol())) {
            @Override
            @SuppressWarnings("unchecked")
            public <T> T _getFeature(final Class<T> type) {
                if(type == DistributionConfiguration.class) {
                    return (T) cdn;
                }
                return super._getFeature(type);
            }
        };
        final DistributionPurgeWorker worker = new DistributionPurgeWorker(
                Collections.singletonList(new Path("/c/f", EnumSet.of(Path.Type.file))), LoginCallback.noop,
                Distribution.DOWNLOAD, Distribution.WEBSITE_CDN, Distribution.CUSTOM);
        assertTrue(worker.run(session));
        assertEquals(Collections.singletonList(Distribution.DOWNLOAD), invalidated);
    }
}
