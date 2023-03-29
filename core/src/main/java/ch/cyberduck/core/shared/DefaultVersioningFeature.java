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
import ch.cyberduck.core.features.Delete;
import ch.cyberduck.core.features.Directory;
import ch.cyberduck.core.features.Find;
import ch.cyberduck.core.features.Move;
import ch.cyberduck.core.features.Versioning;
import ch.cyberduck.core.io.DisabledStreamListener;
import ch.cyberduck.core.preferences.HostPreferences;
import ch.cyberduck.core.transfer.TransferStatus;
import ch.cyberduck.ui.comparator.FilenameComparator;

import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.EnumSet;
import java.util.TimeZone;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class DefaultVersioningFeature implements Versioning {
    private static final Logger log = LogManager.getLogger(DefaultVersioningFeature.class);

    private static final String DIRECTORY_SUFFIX = ".cyberduckversions";

    private final Session<?> session;
    private final DateFormatter formatter;
    private final Pattern include;

    public DefaultVersioningFeature(final Session<?> session) {
        this(session, new ISO8601DateFormatter());
    }

    public DefaultVersioningFeature(final Session<?> session, final DateFormatter formatter) {
        this.session = session;
        this.formatter = formatter;
        this.include = Pattern.compile(new HostPreferences(session.getHost()).getProperty("queue.upload.file.versioning.include.regex"));
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
    public boolean save(final Path file) throws BackgroundException {
        if(include.matcher(file.getName()).matches()) {
            final Path version = this.toVersioned(file);
            if(!session.getFeature(Move.class).isSupported(file, version)) {
                return false;
            }
            final Path directory = version.getParent();
            if(!session.getFeature(Find.class).find(directory)) {
                if(log.isDebugEnabled()) {
                    log.debug(String.format("Create directory %s for versions", directory));
                }
                session.getFeature(Directory.class).mkdir(directory, new TransferStatus());
            }
            if(log.isDebugEnabled()) {
                log.debug(String.format("Rename existing file %s to %s", file, version));
            }
            session.getFeature(Move.class).move(file, version,
                    new TransferStatus().exists(false), new Delete.DisabledCallback(), new DisabledConnectionCallback());
            return true;
        }
        else {
            if(log.isDebugEnabled()) {
                log.debug(String.format("No match for %s in %s", file.getName(), include));
            }
            return false;
        }
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
        final Path directory = getVersionedFolder(file);
        if(session.getFeature(Find.class).find(directory)) {
            for(Path version : session.getFeature(ListService.class).list(directory, listener).toStream()
                    .filter(f -> f.getName().startsWith(FilenameUtils.getBaseName(file.getName()))).collect(Collectors.toList())) {
                version.attributes().setDuplicate(true);
                versions.add(version);
            }
        }
        return versions.filter(new FilenameComparator(false));
    }

    /**
     * @param file File to edit
     * @return Directory to save previous versions of file
     */
    private static Path getVersionedFolder(final Path file) {
        return new Path(file.getParent(), DIRECTORY_SUFFIX, EnumSet.of(Path.Type.directory));
    }


    /**
     * Generate new versioned path for file
     *
     * @param file File
     * @return Same
     */
    private Path toVersioned(final Path file) {
        final String basename = String.format("%s-%s", FilenameUtils.getBaseName(file.getName()),
                formatter.format(System.currentTimeMillis(), TimeZone.getTimeZone("UTC")).replaceAll("[-:]", StringUtils.EMPTY));
        if(StringUtils.isNotBlank(file.getExtension())) {
            return new Path(getVersionedFolder(file), String.format("%s.%s", basename, file.getExtension()), EnumSet.of(Path.Type.file));
        }
        return new Path(getVersionedFolder(file), basename, EnumSet.of(Path.Type.file));
    }
}
