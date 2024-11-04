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

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.EnumSet;

public class DefaultComparisonService implements ComparisonService {
    private static final Logger log = LogManager.getLogger(DefaultComparisonService.class);

    private static final ChainedComparisonService DEFAULT_FILE_COMPARISON_CHAIN = new ChainedComparisonService(EnumSet.of(Comparison.unknown, Comparison.notequal),
            new ETagComparisonService(),
            new ChainedComparisonService(
                    new ChecksumComparisonService(),
                    new VersionIdComparisonService(),
                    new ChainedComparisonService(
                            EnumSet.of(Comparison.unknown, Comparison.equal), new TimestampComparisonService(), new SizeComparisonService()))
    );

    private static final ChainedComparisonService DEFAULT_SYMLINK_COMPARISON_CHAIN = new ChainedComparisonService(
            EnumSet.of(Comparison.unknown), new TimestampComparisonService());

    private final ComparisonService files;
    private final ComparisonService symlinks;
    private final ComparisonService directories;

    public DefaultComparisonService(final Protocol protocol) {
        this(forFiles(protocol), forSymlinks(protocol), forDirectories(protocol));
    }

    public DefaultComparisonService(final ComparisonService files, final ComparisonService directories) {
        this(files, DEFAULT_SYMLINK_COMPARISON_CHAIN, directories);
    }

    public DefaultComparisonService(final ComparisonService files, final ComparisonService symlinks, final ComparisonService directories) {
        this.files = files;
        this.symlinks = symlinks;
        this.directories = directories;
    }

    @Override
    public Comparison compare(final Path.Type type, final PathAttributes local, final PathAttributes remote) {
        switch(type) {
            case directory:
                log.debug("Compare local attributes {} with remote {} using {}", local, remote, directories);
                return directories.compare(type, local, remote);
            case symboliclink:
                return symlinks.compare(type, local, remote);
            default:
                log.debug("Compare local attributes {} with remote {} using {}", local, remote, files);
                return files.compare(type, local, remote);
        }
    }

    @Override
    public int hashCode(final Path.Type type, final PathAttributes attr) {
        switch(type) {
            case directory:
                return directories.hashCode(type, attr);
        }
        return files.hashCode(type, attr);
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("DefaultComparisonService{");
        sb.append("files=").append(files);
        sb.append(", directories=").append(directories);
        sb.append('}');
        return sb.toString();
    }

    public static ComparisonService forFiles(final Protocol protocol) {
        return DEFAULT_FILE_COMPARISON_CHAIN;
    }

    public static ComparisonService forSymlinks(final Protocol protocol) {
        return DEFAULT_SYMLINK_COMPARISON_CHAIN;
    }

    public static ComparisonService forDirectories(final Protocol protocol) {
        return new ChainedComparisonService(
                new RevisionComparisonService(),
                new ETagComparisonService(),
                protocol.getDirectoryTimestamp() == Protocol.DirectoryTimestamp.implicit ? new TimestampComparisonService() : ComparisonService.disabled);
    }
}
