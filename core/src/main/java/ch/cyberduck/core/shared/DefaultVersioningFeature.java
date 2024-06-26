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
import ch.cyberduck.core.VersioningConfiguration;
import ch.cyberduck.core.date.DateFormatter;
import ch.cyberduck.core.date.InvalidDateException;
import ch.cyberduck.core.date.MDTMMillisecondsDateFormatter;
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

import java.util.Date;
import java.util.EnumSet;
import java.util.List;
import java.util.TimeZone;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class DefaultVersioningFeature implements Versioning {
    private static final Logger log = LogManager.getLogger(DefaultVersioningFeature.class);

    private final Session<?> session;
    private final FilenameVersionIdentifier formatter;
    private final VersioningDirectoryProvider provider;
    private final Pattern include;

    public DefaultVersioningFeature(final Session<?> session) {
        this(session, new DefaultVersioningDirectoryProvider(), new DefaultFilenameVersionIdentifier());
    }

    public DefaultVersioningFeature(final Session<?> session, final VersioningDirectoryProvider provider, final FilenameVersionIdentifier formatter) {
        this.session = session;
        this.provider = provider;
        this.formatter = formatter;
        this.include = Pattern.compile(new HostPreferences(session.getHost()).getProperty("versioning.include.regex"));
    }

    @Override
    public VersioningConfiguration getConfiguration(final Path file) throws BackgroundException {
        if(this.isRevertable(file)) {
            // No versioning for previous versions
            return VersioningConfiguration.empty();
        }
        if(file.isDirectory()) {
            return new VersioningConfiguration(true);
        }
        if(include.matcher(file.getName()).matches()) {
            return new VersioningConfiguration(true);
        }
        if(log.isDebugEnabled()) {
            log.debug(String.format("No match for %s in %s", file.getName(), include));
        }
        return VersioningConfiguration.empty();
    }

    @Override
    public void setConfiguration(final Path container, final PasswordCallback prompt, final VersioningConfiguration configuration) throws BackgroundException {
        throw new UnsupportedException();
    }

    @Override
    public boolean save(final Path file) throws BackgroundException {
        final Path version = new Path(provider.provide(file), formatter.toVersion(file.getName()), file.getType());
        final Move feature = session.getFeature(Move.class);
        if(!feature.isSupported(file, version.getParent(), version.getName())) {
            log.warn(String.format("Skip saving version for %s", file));
            return false;
        }
        if(file.isDirectory()) {
            if(!feature.isRecursive(file, version)) {
                log.warn(String.format("Skip saving version for directory %s", file));
                return false;
            }
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
        feature.move(file, version,
                new TransferStatus().exists(false), new Delete.DisabledCallback(), new DisabledConnectionCallback());
        return true;
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
        final Path directory = provider.provide(file);
        if(session.getFeature(Find.class).find(directory)) {
            for(Path version : session.getFeature(ListService.class).list(directory, listener).toStream()
                    .filter(f -> f.getName().startsWith(FilenameUtils.getBaseName(file.getName()))).collect(Collectors.toList())) {
                version.attributes().setDuplicate(true);
                versions.add(version);
            }
        }
        return versions.filter(new FilenameComparator(false));
    }

    @Override
    public void cleanup(final Path file, final ConnectionCallback callback) throws BackgroundException {
        final Delete delete = session.getFeature(Delete.class);
        if(file.isDirectory()) {
            if(!delete.isRecursive()) {
                return;
            }
        }
        final List<Path> versions = this.list(file, new DisabledListProgressListener()).toStream()
                .sorted(new FilenameComparator(false)).skip(
                        new HostPreferences(session.getHost()).getInteger("versioning.limit")).collect(Collectors.toList());
        if(log.isWarnEnabled()) {
            log.warn(String.format("Delete %d previous versions of %s", versions.size(), file));
        }
        delete.delete(versions, callback, new Delete.DisabledCallback());
    }

    @Override
    public EnumSet<Flags> features(final Path file) {
        if(StringUtils.equals(DefaultVersioningDirectoryProvider.NAME, file.getParent().getName())) {
            return EnumSet.of(Flags.revert);
        }
        return EnumSet.of(Flags.save, Flags.list);
    }

    public interface VersioningDirectoryProvider {
        /**
         * @param file File to edit
         * @return Directory to save previous versions of file
         */
        Path provide(Path file);
    }

    public static final class DefaultVersioningDirectoryProvider implements VersioningDirectoryProvider {
        private static final String NAME = ".duckversions";

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

    public static final class DefaultFilenameVersionIdentifier implements FilenameVersionIdentifier {
        private final Pattern format;
        private final DateFormatter formatter;

        public DefaultFilenameVersionIdentifier() {
            this(Pattern.compile("(.*)-[0-9]{8}[0-9]{6}\\.[0-9]{3}(\\..*)?"), new MDTMMillisecondsDateFormatter());
        }

        public DefaultFilenameVersionIdentifier(final Pattern format, final DateFormatter formatter) {
            this.format = format;
            this.formatter = formatter;
        }

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
            final String basename = String.format("%s-%s", FilenameUtils.getBaseName(filename), this.toTimestamp());
            if(StringUtils.isNotBlank(FilenameUtils.getExtension(filename))) {
                return String.format("%s.%s", basename, FilenameUtils.getExtension(filename));
            }
            return basename;
        }

        private String toTimestamp() {
            return formatter.format(System.currentTimeMillis(), TimeZone.getTimeZone("UTC"));
        }
    }
}
