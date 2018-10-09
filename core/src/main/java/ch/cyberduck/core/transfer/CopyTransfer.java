package ch.cyberduck.core.transfer;

/*
 * Copyright (c) 2002-2014 David Kocher. All rights reserved.
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

import ch.cyberduck.core.*;
import ch.cyberduck.core.exception.BackgroundException;
import ch.cyberduck.core.features.Bulk;
import ch.cyberduck.core.features.Copy;
import ch.cyberduck.core.features.Directory;
import ch.cyberduck.core.features.Upload;
import ch.cyberduck.core.features.Write;
import ch.cyberduck.core.io.BandwidthThrottle;
import ch.cyberduck.core.io.StreamListener;
import ch.cyberduck.core.preferences.PreferencesFactory;
import ch.cyberduck.core.serializer.Serializer;
import ch.cyberduck.core.shared.DefaultCopyFeature;
import ch.cyberduck.core.transfer.copy.ChecksumFilter;
import ch.cyberduck.core.transfer.copy.OverwriteFilter;

import org.apache.log4j.Logger;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CopyTransfer extends Transfer {
    private static final Logger log = Logger.getLogger(CopyTransfer.class);

    private final Filter<Path> filter = new NullFilter<Path>();

    private final Comparator<Path> comparator = new NullComparator<Path>();

    /**
     * Temporary mapping for source to destination files
     */
    private final Map<Path, Path> mapping;
    /**
     * Root files mapping source to destination
     */
    private final Map<Path, Path> selected;

    /**
     * Target host
     */
    private final Host destination;

    public CopyTransfer(final Host source, final Host destination,
                        final Map<Path, Path> selected) {
        this(source, destination, selected, new BandwidthThrottle(PreferencesFactory.get().getFloat("queue.download.bandwidth.bytes")));
    }

    public CopyTransfer(final Host source, final Host destination,
                        final Map<Path, Path> selected, final BandwidthThrottle bandwidth) {
        super(source, new ArrayList<TransferItem>(), bandwidth);
        this.destination = destination;
        this.selected = selected;
        this.mapping = new HashMap<Path, Path>(selected);
        for(Path f : selected.keySet()) {
            roots.add(new TransferItem(f));

        }
    }

    @Override
    public Transfer withCache(final Cache<Path> cache) {
        return this;
    }

    @Override
    public Type getType() {
        return Type.copy;
    }

    @Override
    public Host getDestination() {
        return destination;
    }

    @Override
    public String getLocal() {
        return null;
    }

    @Override
    public <T> T serialize(final Serializer dict) {
        dict.setStringForKey(String.valueOf(this.getType().name()), "Type");
        dict.setObjectForKey(host, "Host");
        if(destination != null) {
            dict.setObjectForKey(destination, "Destination");
        }
        dict.setListForKey(new ArrayList<Serializable>(selected.values()), "Destinations");
        dict.setListForKey(new ArrayList<Serializable>(selected.keySet()), "Roots");
        dict.setStringForKey(uuid, "UUID");
        dict.setStringForKey(String.valueOf(this.getSize()), "Size");
        dict.setStringForKey(String.valueOf(this.getTransferred()), "Current");
        if(timestamp != null) {
            dict.setStringForKey(String.valueOf(timestamp.getTime()), "Timestamp");
        }
        if(bandwidth != null) {
            dict.setStringForKey(String.valueOf(bandwidth.getRate()), "Bandwidth");
        }
        return dict.getSerialized();
    }

    @Override
    public TransferAction action(final Session<?> source, final Session<?> destination, boolean resumeRequested, boolean reloadRequested,
                                 final TransferPrompt prompt, final ListProgressListener listener) throws BackgroundException {
        if(log.isDebugEnabled()) {
            log.debug(String.format("Find transfer action for Resume=%s,Reload=%s", resumeRequested, reloadRequested));
        }
        if(resumeRequested) {
            return TransferAction.comparison;
        }
        final TransferAction action;
        if(reloadRequested) {
            action = TransferAction.forName(
                PreferencesFactory.get().getProperty("queue.copy.reload.action"));
        }
        else {
            // Use default
            action = TransferAction.forName(
                PreferencesFactory.get().getProperty("queue.copy.action"));
        }
        if(action.equals(TransferAction.callback)) {
            for(TransferItem upload : roots) {
                final Upload write = destination.getFeature(Upload.class);
                final Path copy = mapping.get(upload.remote);
                final Write.Append append = write.append(copy, upload.remote.attributes().getSize(), PathCache.empty(), new DisabledListProgressListener());
                if(append.override || append.append) {
                    // Found remote file
                    if(upload.remote.isDirectory()) {
                        // List files in target directory
                        if(this.list(destination, destination, copy, null, listener).isEmpty()) {
                            // Do not prompt for existing empty directories
                            continue;
                        }
                    }
                    // Prompt user to choose a filter
                    return prompt.prompt(upload);
                }
            }
            // No files exist yet therefore it is most straightforward to use the overwrite action
            return TransferAction.overwrite;
        }
        return action;
    }

    @Override
    public TransferPathFilter filter(final Session<?> source, final Session<?> destination, final TransferAction action, final ProgressListener listener) {
        if(log.isDebugEnabled()) {
            log.debug(String.format("Filter transfer with action %s", action));
        }
        if(action.equals(TransferAction.comparison)) {
            return new ChecksumFilter(source, destination, mapping);
        }
        return new OverwriteFilter(source, destination, mapping);
    }

    @Override
    public List<TransferItem> list(final Session<?> source, final Session<?> destination, final Path directory, final Local local,
                                   final ListProgressListener listener) throws BackgroundException {
        if(log.isDebugEnabled()) {
            log.debug(String.format("List children for %s", directory));
        }
        final AttributedList<Path> list = source.getFeature(ListService.class).list(directory, listener).filter(comparator, filter);
        final Path copy = mapping.get(directory);
        for(Path p : list) {
            mapping.put(p, new Path(copy, p.getName(), p.getType(), p.attributes()));
        }
        final List<TransferItem> nullified = new ArrayList<TransferItem>();
        for(Path p : list) {
            nullified.add(new TransferItem(p));
        }
        return nullified;
    }

    @Override
    public void pre(final Session<?> source, final Session<?> destination, final Map<TransferItem, TransferStatus> files, final ConnectionCallback callback) throws BackgroundException {
        final Bulk download = source.getFeature(Bulk.class);
        {
            final Object id = download.pre(Type.download, files, callback);
            if(log.isDebugEnabled()) {
                log.debug(String.format("Obtained bulk id %s for transfer %s", id, this));
            }
        }
        final Bulk upload = destination.getFeature(Bulk.class);
        {
            final Map<TransferItem, TransferStatus> targets = new HashMap<>();
            for(Map.Entry<TransferItem, TransferStatus> entry : files.entrySet()) {
                targets.put(new TransferItem(mapping.get(entry.getKey().remote)), entry.getValue());
            }
            final Object id = upload.pre(Type.upload, targets, callback);
            if(log.isDebugEnabled()) {
                log.debug(String.format("Obtained bulk id %s for transfer %s", id, this));
            }
        }
    }

    @Override
    public void post(final Session<?> source, final Session<?> destination, final Map<TransferItem, TransferStatus> files, final ConnectionCallback callback) throws BackgroundException {
        final Bulk download = source.getFeature(Bulk.class);
        {
            download.post(Type.download, files, callback);
        }
        final Bulk upload = destination.getFeature(Bulk.class);
        {
            final Map<TransferItem, TransferStatus> targets = new HashMap<>();
            for(Map.Entry<TransferItem, TransferStatus> entry : files.entrySet()) {
                targets.put(new TransferItem(mapping.get(entry.getKey().remote)), entry.getValue());
            }
            upload.post(Type.upload, targets, callback);
        }
    }

    @Override
    public Path transfer(final Session<?> session, final Session<?> destination, final Path source, final Local n,
                         final TransferOptions options, final TransferStatus status,
                         final ConnectionCallback connectionCallback,
                         final PasswordCallback passwordCallback, final ProgressListener listener, final StreamListener streamListener) throws BackgroundException {
        if(log.isDebugEnabled()) {
            log.debug(String.format("Transfer file %s with options %s", source, options));
        }
        listener.message(MessageFormat.format(LocaleFactory.localizedString("Copying {0} to {1}", "Status"),
            source.getName(), mapping.get(source).getName()));
        if(source.isDirectory()) {
            if(!status.isExists()) {
                final Directory feature = destination.getFeature(Directory.class);
                feature.mkdir(mapping.get(source), null, status);
                status.setComplete();
            }
        }
        else {
            // Transfer
            final Copy feature = new DefaultCopyFeature(session).withTarget(destination);
            feature.copy(source, mapping.get(source), status, connectionCallback);
            this.addTransferred(status.getLength());
        }
        return source;
    }

    @Override
    public void normalize() {
        //
    }
}
