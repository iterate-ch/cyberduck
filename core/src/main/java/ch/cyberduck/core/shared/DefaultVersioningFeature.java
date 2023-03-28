package ch.cyberduck.core.shared;

/*
 * Copyright (c) 2002-2023 iterate GmbH. All rights reserved.
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

import ch.cyberduck.core.AttributedList;
import ch.cyberduck.core.DisabledConnectionCallback;
import ch.cyberduck.core.ListProgressListener;
import ch.cyberduck.core.ListService;
import ch.cyberduck.core.PasswordCallback;
import ch.cyberduck.core.Path;
import ch.cyberduck.core.Session;
import ch.cyberduck.core.VersioningConfiguration;
import ch.cyberduck.core.date.DateFormatter;
import ch.cyberduck.core.date.ISO8601DateFormatter;
import ch.cyberduck.core.exception.BackgroundException;
import ch.cyberduck.core.exception.UnsupportedException;
import ch.cyberduck.core.features.Copy;
import ch.cyberduck.core.features.Versioning;
import ch.cyberduck.core.io.DisabledStreamListener;
import ch.cyberduck.core.preferences.HostPreferences;
import ch.cyberduck.core.transfer.TransferStatus;
import ch.cyberduck.ui.comparator.FilenameComparator;

import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.StringUtils;

import java.util.EnumSet;
import java.util.TimeZone;
import java.util.stream.Collectors;

public class DefaultVersioningFeature implements Versioning {

    private static final String DIRECTORY_SUFFIX = ".cyberduckversions";

    private final Session<?> session;
    private final DateFormatter formatter;

    public DefaultVersioningFeature(final Session<?> session) {
        this(session, new ISO8601DateFormatter());
    }

    public DefaultVersioningFeature(final Session<?> session, final DateFormatter formatter) {
        this.session = session;
        this.formatter = formatter;
    }

    @Override
    public VersioningConfiguration getConfiguration(final Path container) throws BackgroundException {
        return new VersioningConfiguration(new HostPreferences(session.getHost()).getBoolean("versioning.enable"));
    }

    @Override
    public void setConfiguration(final Path container, final PasswordCallback prompt, final VersioningConfiguration configuration) throws BackgroundException {
        throw new UnsupportedException();
    }

    @Override
    public void revert(final Path file) throws BackgroundException {
        session.getFeature(Copy.class).copy(file, new Path(file.getParent().getParent(),
                        StringUtils.removeEnd(file.getParent().getName(), DIRECTORY_SUFFIX), EnumSet.of(Path.Type.file)), new TransferStatus(),
                new DisabledConnectionCallback(), new DisabledStreamListener());
    }

    @Override
    public AttributedList<Path> list(final Path file, final ListProgressListener listener) throws BackgroundException {
        final AttributedList<Path> versions = new AttributedList<>();
        for(Path version : session.getFeature(ListService.class).list(file.isFile() ? getVersionedFolder(file) : file, listener).toStream()
                .filter(f -> f.getName().startsWith(FilenameUtils.getBaseName(file.getName()))).collect(Collectors.toList())) {
            version.attributes().setDuplicate(true);
            versions.add(version);
        }
        return versions.filter(new FilenameComparator(false));
    }

    /**
     * @param file File to edit
     * @return Directory to save previous versions of file
     */
    public static Path getVersionedFolder(final Path file) {
        return new Path(file.getParent(), DIRECTORY_SUFFIX, EnumSet.of(Path.Type.directory));
    }

    @Override
    public Path toVersioned(final Path file) {
        final String basename = String.format("%s-%s", FilenameUtils.getBaseName(file.getName()),
                formatter.format(System.currentTimeMillis(), TimeZone.getTimeZone("UTC")).replaceAll("[-:]", StringUtils.EMPTY));
        if(StringUtils.isNotBlank(file.getExtension())) {
            return new Path(getVersionedFolder(file), String.format("%s.%s", basename, file.getExtension()), EnumSet.of(Path.Type.file));
        }
        return new Path(getVersionedFolder(file), basename, EnumSet.of(Path.Type.file));
    }
}
