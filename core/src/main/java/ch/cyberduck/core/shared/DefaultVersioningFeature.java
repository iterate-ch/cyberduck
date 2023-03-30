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
import ch.cyberduck.core.ConnectionCallback;
import ch.cyberduck.core.DisabledConnectionCallback;
import ch.cyberduck.core.DisabledListProgressListener;
import ch.cyberduck.core.ListProgressListener;
import ch.cyberduck.core.ListService;
import ch.cyberduck.core.PasswordCallback;
import ch.cyberduck.core.Path;
import ch.cyberduck.core.Session;
import ch.cyberduck.core.SimplePathPredicate;
import ch.cyberduck.core.VersioningConfiguration;
import ch.cyberduck.core.date.DateFormatter;
import ch.cyberduck.core.date.ISO8601DateFormatter;
import ch.cyberduck.core.date.InvalidDateException;
import ch.cyberduck.core.exception.BackgroundException;
import ch.cyberduck.core.exception.UnsupportedException;
import ch.cyberduck.core.features.Bulk;
import ch.cyberduck.core.features.Delete;
import ch.cyberduck.core.features.Directory;
import ch.cyberduck.core.features.Find;
import ch.cyberduck.core.features.Move;
import ch.cyberduck.core.features.Versioning;
import ch.cyberduck.core.preferences.HostPreferences;
import ch.cyberduck.core.transfer.Transfer;
import ch.cyberduck.core.transfer.TransferItem;
import ch.cyberduck.core.transfer.TransferStatus;
import ch.cyberduck.ui.comparator.FilenameComparator;

import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.Date;
import java.util.EnumSet;
import java.util.List;
import java.util.Map;
import java.util.TimeZone;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class DefaultVersioningFeature extends DisabledBulkFeature implements Versioning {
    private static final Logger log = LogManager.getLogger(DefaultVersioningFeature.class);

    private final Session<?> session;
    private final FilenameVersionIdentifier formatter;
    private final VersioningDirectoryProvider provider;
    private final Pattern include;

    private Delete delete;

    public DefaultVersioningFeature(final Session<?> session) {
        this(session, new DefaultVersioningDirectoryProvider(), new ISO8601FilenameVersionIdentifier());
    }

    public DefaultVersioningFeature(final Session<?> session, final VersioningDirectoryProvider provider, final FilenameVersionIdentifier formatter) {
        this.session = session;
        this.provider = provider;
        this.formatter = formatter;
        this.include = Pattern.compile(new HostPreferences(session.getHost()).getProperty("queue.upload.file.versioning.include.regex"));
        this.delete = session.getFeature(Delete.class);
    }

    @Override
    public VersioningConfiguration getConfiguration(final Path file) throws BackgroundException {
        switch(session.getHost().getProtocol().getVersioningMode()) {
            case custom:
                return new VersioningConfiguration(file.isDirectory() || include.matcher(file.getName()).matches());
        }
        return VersioningConfiguration.empty();
    }

    @Override
    public void setConfiguration(final Path container, final PasswordCallback prompt, final VersioningConfiguration configuration) throws BackgroundException {
        throw new UnsupportedException();
    }

    @Override
    public boolean save(final Path file) throws BackgroundException {
        if(this.getConfiguration(file).isEnabled()) {
            final Path version = new Path(provider.provide(file), formatter.toVersion(file.getName()), file.getType());
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
        final Path target = new Path(file.getParent().getParent(), formatter.fromVersion(file.getName()), file.getType());
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
        if(this.getConfiguration(file).isEnabled()) {
            final Path directory = provider.provide(file);
            if(session.getFeature(Find.class).find(directory)) {
                for(Path version : session.getFeature(ListService.class).list(directory, listener).toStream()
                        .filter(f -> f.getName().startsWith(FilenameUtils.getBaseName(file.getName()))).collect(Collectors.toList())) {
                    version.attributes().setDuplicate(true);
                    versions.add(version);
                }
            }
        }
        return versions.filter(new FilenameComparator(false));
    }

    @Override
    public void post(final Transfer.Type type, final Map<TransferItem, TransferStatus> files, final ConnectionCallback callback) throws BackgroundException {
        switch(type) {
            case upload:
                for(TransferItem item : files.keySet()) {
                    if(this.getConfiguration(item.remote).isEnabled()) {
                        final List<Path> versions = new DefaultVersioningFeature(session).list(item.remote, new DisabledListProgressListener()).toStream()
                                .sorted(new FilenameComparator(false)).skip(new HostPreferences(session.getHost()).getInteger("queue.upload.file.versioning.limit")).collect(Collectors.toList());
                        if(log.isWarnEnabled()) {
                            log.warn(String.format("Delete %d previous versions of %s", versions.size(), item.remote));
                        }
                        delete.delete(versions, callback, new Delete.DisabledCallback());
                    }
                }
        }
    }

    @Override
    public Bulk withDelete(final Delete delete) {
        this.delete = delete;
        return this;
    }

    @Override
    public boolean isRevertable(final Path version) {
        return StringUtils.equals(DefaultVersioningDirectoryProvider.NAME, version.getParent().getName());
    }

    private interface VersioningDirectoryProvider {
        /**
         * @param file File to edit
         * @return Directory to save previous versions of file
         */
        Path provide(Path file);
    }

    public static final class DefaultVersioningDirectoryProvider implements VersioningDirectoryProvider {
        private static final String NAME = ".cyberduckversions";

        @Override
        public Path provide(final Path file) {
            return new Path(file.getParent(), NAME, EnumSet.of(Path.Type.directory));
        }
    }

    public interface FilenameVersionIdentifier extends DateFormatter {
        /**
         * Translate from basename-timestamp.extension to /basename.extension
         */
        String fromVersion(String filename);

        /**
         * Translate from basename.extension to basename-timestamp.extension
         */
        String toVersion(String filename);
    }

    public static final class ISO8601FilenameVersionIdentifier implements FilenameVersionIdentifier {
        private static final char FILENAME_VERSION_SEPARATOR = '-';

        private static final ISO8601DateFormatter formatter = new ISO8601DateFormatter();
        private static final Pattern format = Pattern.compile("(.*)" + FILENAME_VERSION_SEPARATOR + "[0-9]{8}T[0-9]{6}\\.[0-9]{3}Z(\\..*)?");

        @Override
        public String format(final Date input, final TimeZone zone) {
            return formatter.format(input, zone);
        }

        @Override
        public String format(final long milliseconds, final TimeZone zone) {
            return formatter.format(milliseconds, zone);
        }

        @Override
        public Date parse(final String input) throws InvalidDateException {
            return formatter.parse(input);
        }

        @Override
        public String fromVersion(final String filename) {
            final Matcher matcher = format.matcher(filename);
            if(matcher.matches()) {
                if(StringUtils.isBlank(matcher.group(2))) {
                    return matcher.group(1);
                }
                return String.format("%s%s", matcher.group(1), matcher.group(2));
            }
            return null;
        }

        @Override
        public String toVersion(final String filename) {
            final String basename = String.format("%s%s%s", FilenameUtils.getBaseName(filename),
                    FILENAME_VERSION_SEPARATOR, toTimestamp());
            if(StringUtils.isNotBlank(FilenameUtils.getExtension(filename))) {
                return String.format("%s.%s", basename, FilenameUtils.getExtension(filename));
            }
            return basename;
        }

        private static String toTimestamp() {
            return formatter.format(System.currentTimeMillis(), TimeZone.getTimeZone("UTC"))
                    .replaceAll("[-:]", StringUtils.EMPTY);
        }
    }
}
