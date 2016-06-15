package ch.cyberduck.core.transfer.upload;

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

import ch.cyberduck.core.DisabledProgressListener;
import ch.cyberduck.core.Host;
import ch.cyberduck.core.Local;
import ch.cyberduck.core.NullLocal;
import ch.cyberduck.core.NullSession;
import ch.cyberduck.core.Path;
import ch.cyberduck.core.TestProtocol;
import ch.cyberduck.core.exception.BackgroundException;
import ch.cyberduck.core.synchronization.Comparison;
import ch.cyberduck.core.synchronization.ComparisonServiceFilter;
import ch.cyberduck.core.transfer.TransferStatus;
import ch.cyberduck.core.transfer.symlink.DisabledUploadSymlinkResolver;

import org.junit.Test;

import java.util.EnumSet;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class CompareFilterTest {

    @Test
    public void testAcceptEqual() throws Exception {
        final CompareFilter filter = new CompareFilter(new DisabledUploadSymlinkResolver(),
                new NullSession(new Host(new TestProtocol())), new UploadFilterOptions(),
                new ComparisonServiceFilter(new NullSession(new Host(new TestProtocol())), null, new DisabledProgressListener()) {
                    @Override
                    public Comparison compare(final Path file, final Local local) throws BackgroundException {
                        return Comparison.equal;
                    }
                }
        );
        final Path file = new Path("/", EnumSet.of(Path.Type.file));
        assertFalse(filter.accept(file, new NullLocal("t") {
            @Override
            public boolean exists() {
                return true;
            }
        }, new TransferStatus().exists(true)));
    }

    @Test
    public void testAcceptDirectory() throws Exception {
        final CompareFilter filter = new CompareFilter(new DisabledUploadSymlinkResolver(),
                new NullSession(new Host(new TestProtocol())), new UploadFilterOptions(),
                new ComparisonServiceFilter(new NullSession(new Host(new TestProtocol())), null, new DisabledProgressListener()) {
                    @Override
                    public Comparison compare(final Path file, final Local local) throws BackgroundException {
                        return Comparison.equal;
                    }
                });
        assertTrue(
                filter.accept(new Path("/n", EnumSet.of(Path.Type.directory)), new NullLocal("/n") {
                            @Override
                            public boolean exists() {
                                return true;
                            }
                        },
                        new TransferStatus().exists(true)));
    }
}
