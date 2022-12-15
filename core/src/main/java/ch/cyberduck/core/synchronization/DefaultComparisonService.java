package ch.cyberduck.core.synchronization;

/*
 * Copyright (c) 2002-2022 iterate GmbH. All rights reserved.
 * https://cyberduck.io/
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 */

import ch.cyberduck.core.Path;
import ch.cyberduck.core.PathAttributes;
import ch.cyberduck.core.Protocol;

import java.util.EnumSet;

public class DefaultComparisonService implements ComparisonService {

    public static final ChainedComparisonService DEFAULT_FILE_COMPARISON_CHAIN = new ChainedComparisonService(
            new ChainedComparisonService(EnumSet.of(Comparison.unknown, Comparison.notequal), new ETagComparisonService(),
                    new ChainedComparisonService(
                            new ChecksumComparisonService(),
                            new VersionIdComparisonService(),
                            new ChainedComparisonService(
                                    EnumSet.of(Comparison.unknown, Comparison.equal), new TimestampComparisonService(), new SizeComparisonService()))
            )
    );

    private final ComparisonService files;
    private final ComparisonService directories;

    public DefaultComparisonService(final Protocol protocol) {
        this(DEFAULT_FILE_COMPARISON_CHAIN,
                new ChainedComparisonService(
                        new RevisionComparisonService(),
                        new ETagComparisonService(),
                        protocol.getDirectoryTimestamp() == Protocol.DirectoryTimestamp.implicit ? new TimestampComparisonService() : ComparisonService.disabled));
    }

    public DefaultComparisonService(final ComparisonService files, final ComparisonService directories) {
        this.files = files;
        this.directories = directories;
    }

    @Override
    public Comparison compare(final Path.Type type, final PathAttributes local, final PathAttributes remote) {
        switch(type) {
            case directory:
                return directories.compare(type, local, remote);
            default:
                return files.compare(type, local, remote);
        }
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("DefaultComparisonService{");
        sb.append("files=").append(files);
        sb.append(", directories=").append(directories);
        sb.append('}');
        return sb.toString();
    }
}
