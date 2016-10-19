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

import ch.cyberduck.core.AttributedList;
import ch.cyberduck.core.ConnectionCallback;
import ch.cyberduck.core.Filter;
import ch.cyberduck.core.Host;
import ch.cyberduck.core.ListProgressListener;
import ch.cyberduck.core.Local;
import ch.cyberduck.core.LocaleFactory;
import ch.cyberduck.core.NullComparator;
import ch.cyberduck.core.NullFilter;
import ch.cyberduck.core.Path;
import ch.cyberduck.core.PathCache;
import ch.cyberduck.core.ProgressListener;
import ch.cyberduck.core.Serializable;
import ch.cyberduck.core.Session;
import ch.cyberduck.core.exception.BackgroundException;
import ch.cyberduck.core.features.Bulk;
import ch.cyberduck.core.features.Copy;
import ch.cyberduck.core.features.Directory;
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

    private Filter<Path> filter = new NullFilter<Path>();

    private Comparator<Path> comparator = new NullComparator<Path>();

    /**
     * Mapping source to destination files
     */
    protected final Map<Path, Path> mapping;

    private Session<?> destination;

    public CopyTransfer(final Host source, final Session destination,
                        final Map<Path, Path> selected) {
        this(source, destination, selected, new BandwidthThrottle(PreferencesFactory.get().getFloat("queue.download.bandwidth.bytes")));
    }

    public CopyTransfer(final Host source, final Session destination,
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

    public Session getDestination() {
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
            dict.setObjectForKey(destination.getHost(), "Destination");
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
    public TransferAction action(final Session<?> session, boolean resumeRequested, boolean reloadRequested,
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
                        if(this.list(destination, copy, null, listener).isEmpty()) {
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
    public TransferPathFilter filter(final Session<?> session, final TransferAction action, final ProgressListener listener) {
        if(log.isDebugEnabled()) {
            log.debug(String.format("Filter transfer with action %s", action));
        }
        if(action.equals(TransferAction.comparison)) {
            return new ChecksumFilter(session, destination, mapping);
        }
        return new OverwriteFilter(session, destination, mapping);
    }

    @Override
    public List<TransferItem> list(final Session<?> session, final Path directory, final Local local,
                                   final ListProgressListener listener) throws BackgroundException {
        if(log.isDebugEnabled()) {
            log.debug(String.format("List children for %s", directory));
        }
        final AttributedList<Path> list = session.list(directory, listener).filter(comparator, filter);
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
    public void pre(final Session<?> session, final Map<Path, TransferStatus> files) throws BackgroundException {
        final Bulk download = session.getFeature(Bulk.class);
        if(null != download) {
            final Object id = download.pre(Type.download, files);
            if(log.isDebugEnabled()) {
                log.debug(String.format("Obtained bulk id %s for transfer %s", id, this));
            }
        }
        final Bulk upload = destination.getFeature(Bulk.class);
        if(null != upload) {
            final Map<Path, TransferStatus> targets = new HashMap<>();
            for(Map.Entry<Path, TransferStatus> entry : files.entrySet()) {
                targets.put(this.mapping.get(entry.getKey()), entry.getValue());
            }
            final Object id = upload.pre(Type.upload, targets);
            if(log.isDebugEnabled()) {
                log.debug(String.format("Obtained bulk id %s for transfer %s", id, this));
            }
        }
    }

    @Override
    public void transfer(final Session<?> session, final Path source, final Local n,
                         final TransferOptions options, final TransferStatus status,
                         final ConnectionCallback callback,
                         final ProgressListener progressListener, final StreamListener streamListener) throws BackgroundException {
        if(log.isDebugEnabled()) {
            log.debug(String.format("Transfer file %s with options %s", source, options));
        }
        final Path copy = mapping.get(source);
        progressListener.message(MessageFormat.format(LocaleFactory.localizedString("Copying {0} to {1}", "Status"),
                source.getName(), copy.getName()));
        if(source.isFile()) {
            if(session.getHost().equals(destination.getHost())) {
                final Copy feature = session.getFeature(Copy.class);
                if(feature != null) {
                    feature.copy(source, copy);
                    addTransferred(status.getLength());
                }
                else {
                    this.copy(session, source, destination, copy, bandwidth, streamListener, status);
                }
            }
            else {
                this.copy(session, source, destination, copy, bandwidth, streamListener, status);
            }
        }
        else {
            if(!status.isExists()) {
                progressListener.message(MessageFormat.format(LocaleFactory.localizedString("Making directory {0}", "Status"),
                        copy.getName()));
                destination.getFeature(Directory.class).mkdir(copy, null, status);
            }
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
    private void copy(final Session<?> session, final Path file, final Session<?> target, final Path copy,
                      final BandwidthThrottle throttle, final StreamListener streamListener,
                      final TransferStatus status) throws BackgroundException {
        InputStream in = null;
        OutputStream out = null;
        try {
            if(file.isFile()) {
                in = new ThrottledInputStream(session.getFeature(Read.class).read(file, status), throttle);
                // Make sure to use S3MultipartWriteFeature, see #9362
                out = new ThrottledOutputStream(target.getFeature(Write.class).write(copy, status), throttle);
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
        }
        finally {
            new DefaultStreamCloser().close(in);
            new DefaultStreamCloser().close(out);
        }
    }
}