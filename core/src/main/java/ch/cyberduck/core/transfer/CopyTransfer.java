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
import ch.cyberduck.core.features.MultipartWrite;
import ch.cyberduck.core.features.Read;
import ch.cyberduck.core.features.Upload;
import ch.cyberduck.core.features.Write;
import ch.cyberduck.core.io.BandwidthThrottle;
import ch.cyberduck.core.io.DefaultStreamCloser;
import ch.cyberduck.core.io.DelegateStreamListener;
import ch.cyberduck.core.io.StreamCopier;
import ch.cyberduck.core.io.StreamListener;
import ch.cyberduck.core.io.ThrottledInputStream;
import ch.cyberduck.core.io.ThrottledOutputStream;
import ch.cyberduck.core.preferences.PreferencesFactory;
import ch.cyberduck.core.serializer.Serializer;
import ch.cyberduck.core.transfer.copy.ChecksumFilter;
import ch.cyberduck.core.transfer.copy.OverwriteFilter;

import org.apache.log4j.Logger;

import java.io.InputStream;
import java.io.OutputStream;
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
     * Mapping source to destination files
     */
    protected final Map<Path, Path> mapping;

    private final Host destination;

    public CopyTransfer(final Host source, final Host destination,
                        final Map<Path, Path> selected) {
        this(source, destination, selected, new BandwidthThrottle(PreferencesFactory.get().getFloat("queue.download.bandwidth.bytes")));
    }

    public CopyTransfer(final Host source, final Host destination,
                        final Map<Path, Path> selected, final BandwidthThrottle bandwidth) {
        super(source, new ArrayList<TransferItem>(), bandwidth);
        this.destination = destination;
        this.mapping = new HashMap<Path, Path>(selected);
        for(Path s : selected.keySet()) {
            roots.add(new TransferItem(s));
        }
    }

    @Override
    public Transfer withCache(final PathCache cache) {
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
        dict.setListForKey(new ArrayList<Serializable>(mapping.values()), "Destinations");
        dict.setListForKey(new ArrayList<Serializable>(mapping.keySet()), "Roots");
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
                final Write.Append append = write.append(copy, upload.remote.attributes().getSize(), PathCache.empty());
                if(append.override || append.append) {
                    // Found remote file
                    if(upload.remote.isDirectory()) {
                        // List files in target directory
                        if(this.list(source, destination, copy, null, listener).isEmpty()) {
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
    public void pre(final Session<?> source, final Session<?> destination, final Map<Path, TransferStatus> files, final ConnectionCallback callback) throws BackgroundException {
        final Bulk download = source.getFeature(Bulk.class);
        {
            final Object id = download.pre(Type.download, files, callback);
            if(log.isDebugEnabled()) {
                log.debug(String.format("Obtained bulk id %s for transfer %s", id, this));
            }
        }
        final Bulk upload = destination.getFeature(Bulk.class);
        {
            final Map<Path, TransferStatus> targets = new HashMap<>();
            for(Map.Entry<Path, TransferStatus> entry : files.entrySet()) {
                targets.put(this.mapping.get(entry.getKey()), entry.getValue());
            }
            final Object id = upload.pre(Type.upload, targets, callback);
            if(log.isDebugEnabled()) {
                log.debug(String.format("Obtained bulk id %s for transfer %s", id, this));
            }
        }
    }

    @Override
    public void post(final Session<?> source, final Session<?> destination, final Map<Path, TransferStatus> files, final ConnectionCallback callback) throws BackgroundException {
        final Bulk download = source.getFeature(Bulk.class);
        {
            download.post(Type.download, files, callback);
        }
        final Bulk upload = destination.getFeature(Bulk.class);
        {
            final Map<Path, TransferStatus> targets = new HashMap<>();
            for(Map.Entry<Path, TransferStatus> entry : files.entrySet()) {
                targets.put(this.mapping.get(entry.getKey()), entry.getValue());
            }
            upload.post(Type.upload, targets, callback);
        }
    }

    @Override
    public void transfer(final Session<?> session, final Session<?> destination, final Path source, final Local n,
                         final TransferOptions options, final TransferStatus status,
                         final ConnectionCallback callback,
                         final ProgressListener progressListener, final StreamListener streamListener) throws BackgroundException {
        if(log.isDebugEnabled()) {
            log.debug(String.format("Transfer file %s with options %s", source, options));
        }
        final Path copy = mapping.get(source);
        progressListener.message(MessageFormat.format(LocaleFactory.localizedString("Copying {0} to {1}", "Status"),
                source.getName(), copy.getName()));
        if(session.getHost().equals(destination.getHost())) {
            final Copy feature = session.getFeature(Copy.class);
            feature.copy(source, copy, status);
            addTransferred(status.getLength());
        }
        else {
            this.copy(session, source, destination, copy, bandwidth, streamListener, status);
        }
    }

    /**
     * Default implementation using a temporary file on localhost as an intermediary
     * with a download and upload transfer.
     *
     * @param copy     Destination
     * @param throttle The bandwidth limit
     * @param status   Transfer status
     */
    private void copy(final Session<?> source, final Path file, final Session<?> target, final Path copy,
                      final BandwidthThrottle throttle, final StreamListener streamListener,
                      final TransferStatus status) throws BackgroundException {
        InputStream in = null;
        OutputStream out = null;
        try {
            if(file.isFile()) {
                in = new ThrottledInputStream(source.getFeature(Read.class).read(file, status, new DisabledConnectionCallback()), throttle);
                Write write = target.getFeature(MultipartWrite.class);
                if(null == write) {
                    // Fallback if multipart write is not available
                    write = target.getFeature(Write.class);
                }
                out = new ThrottledOutputStream(write.write(copy, status, new DisabledConnectionCallback()), throttle);
                new StreamCopier(status, status)
                        .withLimit(status.getLength())
                        .withListener(new DelegateStreamListener(streamListener) {
                            @Override
                            public void sent(final long bytes) {
                                addTransferred(bytes);
                                super.sent(bytes);
                            }
                        }).transfer(in, out);
            }
            else if(file.isDirectory()) {
                target.getFeature(Directory.class).mkdir(copy, null, status);
            }
        }
        finally {
            new DefaultStreamCloser().close(in);
            new DefaultStreamCloser().close(out);
        }
    }
}