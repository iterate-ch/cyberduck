package ch.cyberduck.core.transfer.download;

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

import ch.cyberduck.core.DescriptiveUrl;
import ch.cyberduck.core.DescriptiveUrlBag;
import ch.cyberduck.core.HostUrlProvider;
import ch.cyberduck.core.Local;
import ch.cyberduck.core.LocalFactory;
import ch.cyberduck.core.LocaleFactory;
import ch.cyberduck.core.Path;
import ch.cyberduck.core.PathAttributes;
import ch.cyberduck.core.Permission;
import ch.cyberduck.core.ProgressListener;
import ch.cyberduck.core.Session;
import ch.cyberduck.core.UrlProvider;
import ch.cyberduck.core.exception.AccessDeniedException;
import ch.cyberduck.core.exception.BackgroundException;
import ch.cyberduck.core.exception.ChecksumException;
import ch.cyberduck.core.exception.LocalAccessDeniedException;
import ch.cyberduck.core.exception.NotfoundException;
import ch.cyberduck.core.features.AttributesFinder;
import ch.cyberduck.core.features.Read;
import ch.cyberduck.core.io.Checksum;
import ch.cyberduck.core.io.ChecksumCompute;
import ch.cyberduck.core.io.ChecksumComputeFactory;
import ch.cyberduck.core.local.ApplicationLauncher;
import ch.cyberduck.core.local.ApplicationLauncherFactory;
import ch.cyberduck.core.local.IconService;
import ch.cyberduck.core.local.IconServiceFactory;
import ch.cyberduck.core.local.QuarantineService;
import ch.cyberduck.core.local.QuarantineServiceFactory;
import ch.cyberduck.core.preferences.HostPreferences;
import ch.cyberduck.core.preferences.PreferencesReader;
import ch.cyberduck.core.transfer.AutoTransferConnectionLimiter;
import ch.cyberduck.core.transfer.TransferPathFilter;
import ch.cyberduck.core.transfer.TransferStatus;
import ch.cyberduck.core.transfer.symlink.SymlinkResolver;

import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public abstract class AbstractDownloadFilter implements TransferPathFilter {
    private static final Logger log = LogManager.getLogger(AbstractDownloadFilter.class);

    private final PreferencesReader preferences;
    private final Session<?> session;
    private final SymlinkResolver<Path> symlinkResolver;
    private final QuarantineService quarantine = QuarantineServiceFactory.get();
    private final ApplicationLauncher launcher = ApplicationLauncherFactory.get();
    private final IconService icon = IconServiceFactory.get();

    private final AttributesFinder attribute;
    private final DownloadFilterOptions options;

    protected AbstractDownloadFilter(final SymlinkResolver<Path> symlinkResolver, final Session<?> session, final DownloadFilterOptions options) {
        this(symlinkResolver, session, session.getFeature(AttributesFinder.class), options);
    }

    public AbstractDownloadFilter(final SymlinkResolver<Path> symlinkResolver, final Session<?> session, final AttributesFinder attribute, final DownloadFilterOptions options) {
        this.session = session;
        this.symlinkResolver = symlinkResolver;
        this.attribute = attribute;
        this.options = options;
        this.preferences = new HostPreferences(session.getHost());
    }

    @Override
    public boolean accept(final Path file, final Local local, final TransferStatus parent, final ProgressListener progress) throws BackgroundException {
        final Local volume = local.getVolume();
        if(!volume.exists()) {
            throw new NotfoundException(String.format("Volume %s not mounted", volume.getAbsolute()));
        }
        return true;
    }

    @Override
    public TransferStatus prepare(final Path file, final Local local, final TransferStatus parent, final ProgressListener progress) throws BackgroundException {
        log.debug("Prepare {}", file);
        final TransferStatus status = new TransferStatus();
        if(parent.isExists()) {
            if(local.exists()) {
                if(file.getType().contains(Path.Type.file)) {
                    if(local.isDirectory()) {
                        throw new LocalAccessDeniedException(String.format("Cannot replace folder %s with file %s", local.getAbbreviatedPath(), file.getName()));
                    }
                }
                if(file.getType().contains(Path.Type.directory)) {
                    if(local.isFile()) {
                        throw new LocalAccessDeniedException(String.format("Cannot replace file %s with folder %s", local.getAbbreviatedPath(), file.getName()));
                    }
                }
                status.setExists(true);
            }
        }
        final PathAttributes attributes;
        if(file.isSymbolicLink()) {
            // A server will resolve the symbolic link when the file is requested.
            final Path target = file.getSymlinkTarget();
            // Read remote attributes of symlink target
            attributes = attribute.find(target);
            if(!symlinkResolver.resolve(file)) {
                if(file.isFile()) {
                    // Content length
                    status.setLength(attributes.getSize());
                }
            }
            // No file size increase for symbolic link to be created locally
        }
        else {
            // Read remote attributes
            attributes = attribute.find(file);
            if(file.isFile()) {
                // Content length
                status.setLength(attributes.getSize());
                if(StringUtils.startsWith(attributes.getDisplayname(), "file:")) {
                    final String filename = StringUtils.removeStart(attributes.getDisplayname(), "file:");
                    if(!StringUtils.equals(file.getName(), filename)) {
                        status.withDisplayname(LocalFactory.get(local.getParent(), filename));
                        int no = 0;
                        while(status.getDisplayname().local.exists()) {
                            String proposal = String.format("%s-%d", FilenameUtils.getBaseName(filename), ++no);
                            if(StringUtils.isNotBlank(Path.getExtension(filename))) {
                                proposal += String.format(".%s", Path.getExtension(filename));
                            }
                            status.withDisplayname(LocalFactory.get(local.getParent(), proposal));
                        }
                    }
                }
            }
        }
        status.setRemote(attributes);
        if(options.timestamp) {
            status.setModified(attributes.getModificationDate());
        }
        if(options.permissions) {
            Permission permission = Permission.EMPTY;
            if(preferences.getBoolean("queue.download.permissions.default")) {
                if(file.isFile()) {
                    permission = new Permission(
                            preferences.getInteger("queue.download.permissions.file.default"));
                }
                if(file.isDirectory()) {
                    permission = new Permission(
                            preferences.getInteger("queue.download.permissions.folder.default"));
                }
            }
            else {
                permission = attributes.getPermission();
            }
            status.setPermission(permission);
        }
        status.setAcl(attributes.getAcl());
        if(options.segments) {
            if(!session.getFeature(Read.class).offset(file)) {
                log.warn("Reading with offsets not supported for {}", file);
            }
            else {
                if(file.isFile()) {
                    // Free space on disk
                    long space = 0L;
                    try {
                        space = Files.getFileStore(Paths.get(local.getParent().getAbsolute())).getUsableSpace();
                    }
                    catch(IOException e) {
                        log.warn("Failure to determine disk space for {}", file.getParent());
                    }
                    long threshold = preferences.getLong("queue.download.segments.threshold");
                    if(status.getLength() * 2 > space) {
                        log.warn("Insufficient free disk space {} for segmented download of {}", space, file);
                    }
                    else if(status.getLength() > threshold) {
                        // if file is smaller than threshold do not attempt to segment
                        final long segmentSize = findSegmentSize(status.getLength(),
                                new AutoTransferConnectionLimiter().getLimit(session.getHost()), threshold,
                                preferences.getLong("queue.download.segments.size"),
                                preferences.getLong("queue.download.segments.count"));

                        // with default settings this can handle files up to 16 GiB, with 128 segments at 128 MiB.
                        // this scales down to files of size 20MiB with 2 segments at 10 MiB
                        long remaining = status.getLength(), offset = 0;
                        // Sorted list
                        final List<TransferStatus> segments = new ArrayList<>();
                        final Local segmentsFolder = LocalFactory.get(local.getParent(), String.format("%s.cyberducksegment", local.getName()));
                        for(int segmentNumber = 1; remaining > 0; segmentNumber++) {
                            final Local segmentFile = LocalFactory.get(
                                    segmentsFolder, String.format("%d.cyberducksegment", segmentNumber));
                            // Last part can be less than 5 MB. Adjust part size.
                            long length = Math.min(segmentSize, remaining);
                            final TransferStatus segmentStatus = new TransferStatus()
                                    .segment(true) // Skip completion filter for single segment
                                    .append(true) // Read with offset
                                    .withOffset(offset)
                                    .withLength(length)
                                    .withRename(segmentFile);
                            log.debug("Adding status {} for segment {}", segmentStatus, segmentFile);
                            segments.add(segmentStatus);
                            remaining -= length;
                            offset += length;
                        }
                        status.withSegments(segments);
                    }
                }
            }
        }
        if(options.checksum) {
            status.setChecksum(attributes.getChecksum());
        }
        return status;
    }

    @Override
    public void apply(final Path file, final Local local, final TransferStatus status,
                      final ProgressListener listener) throws BackgroundException {
        //
    }

    /**
     * Update timestamp and permission
     */
    @Override
    public void complete(final Path file, final Local local,
                         final TransferStatus status, final ProgressListener listener) throws BackgroundException {
        log.debug("Complete {} with status {}", file.getAbsolute(), status);
        if(status.isSegment()) {
            log.debug("Skip completion for single segment {}", status);
            return;
        }
        if(status.isComplete()) {
            if(status.isSegmented()) {
                // Obtain ordered list of segments to reassemble
                final List<TransferStatus> segments = status.getSegments();
                log.info("Compile {} segments to file {}", segments.size(), local);
                if(local.exists()) {
                    local.delete();
                }
                for(Iterator<TransferStatus> iterator = segments.iterator(); iterator.hasNext(); ) {
                    final TransferStatus segmentStatus = iterator.next();
                    // Segment
                    final Local segmentFile = segmentStatus.getRename().local;
                    log.info("Append segment {} to {}", segmentFile, local);
                    segmentFile.copy(local, new Local.CopyOptions().append(true));
                    log.info("Delete segment {}", segmentFile);
                    segmentFile.delete();
                    if(!iterator.hasNext()) {
                        final Local folder = segmentFile.getParent();
                        log.info("Remove segment folder {}", folder);
                        folder.delete();
                    }
                }
            }
            log.debug("Run completion for file {} with status {}", local, status);
            if(file.isFile()) {
                // Bounce Downloads folder dock icon by sending download finished notification
                launcher.bounce(local);
                // Remove custom icon if complete. The Finder will display the default icon for this file type
                if(options.icon) {
                    icon.set(local, status);
                    icon.remove(local);
                }
                if(options.quarantine || options.wherefrom) {
                    final DescriptiveUrlBag provider = session.getFeature(UrlProvider.class).toUrl(file).filter(DescriptiveUrl.Type.provider, DescriptiveUrl.Type.http);
                    for(DescriptiveUrl url : provider) {
                        try {
                            if(options.quarantine) {
                                // Set quarantine attributes
                                quarantine.setQuarantine(local, new HostUrlProvider().withUsername(false).get(session.getHost()), url.getUrl());
                            }
                            if(options.wherefrom) {
                                // Set quarantine attributes
                                quarantine.setWhereFrom(local, url.getUrl());
                            }
                        }
                        catch(LocalAccessDeniedException e) {
                            log.warn("Failure to quarantine file {}. {}", file, e.getMessage());
                        }
                        break;
                    }
                }
            }
            if(!Permission.EMPTY.equals(status.getPermission())) {
                if(file.isDirectory()) {
                    // Make sure we can read & write files to directory created.
                    status.getPermission().setUser(status.getPermission().getUser().or(Permission.Action.read).or(Permission.Action.write).or(Permission.Action.execute));
                }
                if(file.isFile()) {
                    // Make sure the owner can always read and write.
                    status.getPermission().setUser(status.getPermission().getUser().or(Permission.Action.read).or(Permission.Action.write));
                }
                log.info("Updating permissions of {} to {}", local, status.getPermission());
                try {
                    local.attributes().setPermission(status.getPermission());
                }
                catch(AccessDeniedException e) {
                    // Ignore
                    log.warn(e.getMessage());
                }
            }
            if(status.getModified() != null) {
                log.info("Updating timestamp of {} to {}", local, status.getModified());
                try {
                    local.attributes().setModificationDate(status.getModified());
                }
                catch(AccessDeniedException e) {
                    // Ignore
                    log.warn(e.getMessage());
                }
            }
            if(file.isFile()) {
                if(options.checksum) {
                    if(file.getType().contains(Path.Type.decrypted)) {
                        log.warn("Skip checksum verification for {} with client side encryption enabled", file);
                    }
                    else {
                        final Checksum checksum = status.getChecksum();
                        if(Checksum.NONE != checksum) {
                            final ChecksumCompute compute = ChecksumComputeFactory.get(checksum.algorithm);
                            listener.message(MessageFormat.format(LocaleFactory.localizedString("Calculate checksum for {0}", "Status"),
                                    file.getName()));
                            final Checksum download = compute.compute(local.getInputStream(), new TransferStatus());
                            if(!checksum.equals(download)) {
                                throw new ChecksumException(
                                        MessageFormat.format(LocaleFactory.localizedString("Download {0} failed", "Error"), file.getName()),
                                        MessageFormat.format(LocaleFactory.localizedString("Mismatch between {0} hash {1} of downloaded data and checksum {2} returned by the server", "Error"),
                                                download.algorithm.toString(), download.hash, checksum.hash));
                            }
                        }
                    }
                }
            }
            if(file.isFile()) {
                if(status.getDisplayname().local != null) {
                    log.info("Rename file {} to {}", file, status.getDisplayname().local);
                    local.rename(status.getDisplayname().local);
                }
                if(options.open) {
                    launcher.open(local);
                }
            }
        }
    }

    static long findSegmentSize(final long length, final int initialSplit, final long segmentThreshold, final long segmentSizeMaximum, final long segmentCountLimit) {
        // Make segments
        long parts, segmentSize, nextParts = initialSplit;
        // find segment size
        // starting with part count of queue.connections.limit
        // but not more than queue.download.segments.count
        // or until smaller than queue.download.segments.threshold
        do {
            parts = nextParts;
            nextParts = Math.min(nextParts * 2, segmentCountLimit);
            // round up to next byte
            segmentSize = (length + 1) / parts;
        }
        while(segmentSize > segmentThreshold && parts < segmentCountLimit);
        // round to next divisible by 2
        segmentSize = (segmentSize * 2 + 1) / 2;
        // if larger than maximum segment size
        if(segmentSize > segmentSizeMaximum) {
            // double segment size until parts smaller than queue.download.segments.count
            long nextSize = segmentSizeMaximum;
            do {
                segmentSize = nextSize;
                nextSize *= 2;
                parts = length / segmentSize;
            }
            while(parts > segmentCountLimit);
        }
        return segmentSize;
    }
}
