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
import ch.cyberduck.core.features.Delete;
import ch.cyberduck.core.features.Directory;
import ch.cyberduck.core.features.Find;
import ch.cyberduck.core.features.Move;
import ch.cyberduck.core.features.Versioning;
import ch.cyberduck.core.preferences.HostPreferences;
import ch.cyberduck.core.transfer.TransferStatus;
import ch.cyberduck.ui.comparator.FilenameComparator;

import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.EnumSet;
import java.util.TimeZone;
import java.util.regex.Matcher;
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
    public VersioningConfiguration getConfiguration(final Path file) throws BackgroundException {
        return new VersioningConfiguration(file.isDirectory() || include.matcher(file.getName()).matches());
    }

    @Override
    public void setConfiguration(final Path container, final PasswordCallback prompt, final VersioningConfiguration configuration) throws BackgroundException {
        throw new UnsupportedException();
    }

    @Override
    public boolean save(final Path file) throws BackgroundException {
        if(this.getConfiguration(file).isEnabled()) {
            final Path version = toVersioned(file, formatter);
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
            if(file.isDirectory()) {
                if(!session.getFeature(Move.class).isRecursive(file, version)) {
                    throw new UnsupportedException();
                }
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
        final Path target = fromVersioned(file);
        final TransferStatus status = new TransferStatus().exists(session.getFeature(Find.class).find(target));
        if(status.isExists()) {
            if(this.save(target)) {
                status.setExists(false);
            }
        }
        if(file.isDirectory()) {
            if(!session.getFeature(Move.class).isRecursive(file, target)) {
                throw new UnsupportedException();
            }
        }
        session.getFeature(Move.class).move(file, target, status, new Delete.DisabledCallback(), new DisabledConnectionCallback());
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

    private static final char FILENAME_VERSION_SEPARATOR = '-';

    /**
     * Generate new versioned path for file
     *
     * @param file File
     * @return Same
     */
    static Path toVersioned(final Path file, final DateFormatter formatter) {
        // Translate from /basename.extension to /.cyberduckversions/basename-timestamp.extension
        final String basename = String.format("%s%s%s", FilenameUtils.getBaseName(file.getName()),
                FILENAME_VERSION_SEPARATOR, toTimestamp(formatter));
        if(StringUtils.isNotBlank(file.getExtension())) {
            return new Path(getVersionedFolder(file), String.format("%s.%s", basename, file.getExtension()), file.getType());
        }
        return new Path(getVersionedFolder(file), basename, file.getType());
    }

    static String toTimestamp(final DateFormatter formatter) {
        return formatter.format(System.currentTimeMillis(), TimeZone.getTimeZone("UTC")).replaceAll("[-:]", StringUtils.EMPTY);
    }

    static Path fromVersioned(final Path file) {
        // Translate from /.cyberduckersions/basename-timestamp.extension to /basename.extension
        final Pattern format = Pattern.compile("(.*)-[0-9]{8}T[0-9]{6}\\.[0-9]{3}[Z](\\..*)?");
        final Matcher matcher = format.matcher(file.getName());
        if(matcher.matches()) {
            if(StringUtils.isBlank(matcher.group(2))) {
                return new Path(file.getParent().getParent(), matcher.group(1), file.getType());
            }
            return new Path(file.getParent().getParent(), String.format("%s%s", matcher.group(1), matcher.group(2)), file.getType());
        }
        return file;
    }

    @Override
    public boolean isRevertable(final Path file) {
        return StringUtils.equals(DIRECTORY_SUFFIX, file.getParent().getName());
    }
}
