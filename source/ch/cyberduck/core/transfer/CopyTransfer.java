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
import ch.cyberduck.core.DefaultIOExceptionMappingService;
import ch.cyberduck.core.DisabledListProgressListener;
import ch.cyberduck.core.Host;
import ch.cyberduck.core.ListProgressListener;
import ch.cyberduck.core.Local;
import ch.cyberduck.core.LocaleFactory;
import ch.cyberduck.core.Path;
import ch.cyberduck.core.PathCache;
import ch.cyberduck.core.ProgressListener;
import ch.cyberduck.core.Serializable;
import ch.cyberduck.core.Session;
import ch.cyberduck.core.SessionFactory;
import ch.cyberduck.core.exception.BackgroundException;
import ch.cyberduck.core.features.Copy;
import ch.cyberduck.core.features.Directory;
import ch.cyberduck.core.features.Read;
import ch.cyberduck.core.features.Write;
import ch.cyberduck.core.io.BandwidthThrottle;
import ch.cyberduck.core.io.DisabledStreamListener;
import ch.cyberduck.core.io.StreamCopier;
import ch.cyberduck.core.io.StreamListener;
import ch.cyberduck.core.io.ThrottledInputStream;
import ch.cyberduck.core.io.ThrottledOutputStream;
import ch.cyberduck.core.preferences.PreferencesFactory;
import ch.cyberduck.core.serializer.Serializer;
import ch.cyberduck.core.ssl.DefaultTrustManagerHostnameCallback;
import ch.cyberduck.core.ssl.KeychainX509KeyManager;
import ch.cyberduck.core.ssl.KeychainX509TrustManager;
import ch.cyberduck.core.ssl.X509KeyManager;
import ch.cyberduck.core.ssl.X509TrustManager;
import ch.cyberduck.core.transfer.copy.CopyTransferFilter;
import ch.cyberduck.core.transfer.normalizer.CopyRootPathsNormalizer;
import ch.cyberduck.core.transfer.symlink.DownloadSymlinkResolver;

import org.apache.commons.io.IOUtils;
import org.apache.log4j.Logger;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * @version $Id$
 */
public class CopyTransfer extends Transfer {
    private static final Logger log = Logger.getLogger(CopyTransfer.class);

    /**
     * Mapping source to destination files
     */
    protected final Map<Path, Path> files;

    private Session<?> destination;

    private PathCache cache
            = new PathCache(PreferencesFactory.get().getInteger("transfer.cache.size"));

    /**
     * @param files Source to destination mapping
     */
    public CopyTransfer(final Host host, final Host target, final Map<Path, Path> files) {
        this(host, target, files,
                new KeychainX509TrustManager(new DefaultTrustManagerHostnameCallback(target)),
                new KeychainX509KeyManager());
    }

    public CopyTransfer(final Host host, final Host target, final Map<Path, Path> files,
                        final X509TrustManager trust, final X509KeyManager key) {
        this(host, target, new CopyRootPathsNormalizer().normalize(files),
                new BandwidthThrottle(PreferencesFactory.get().getFloat("queue.download.bandwidth.bytes")),
                trust, key);
    }

    private CopyTransfer(final Host host, final Host target,
                         final Map<Path, Path> selected, final BandwidthThrottle bandwidth,
                         final X509TrustManager trust, final X509KeyManager key) {
        super(host, new ArrayList<TransferItem>(), bandwidth);
        this.destination = SessionFactory.create(target, trust, key);
        this.files = selected;
        for(Path source : selected.keySet()) {
            roots.add(new TransferItem(source));
        }
    }

    @Override
    public Transfer withCache(final PathCache cache) {
        this.cache = cache;
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
        dict.setListForKey(new ArrayList<Serializable>(files.values()), "Destinations");
        dict.setListForKey(new ArrayList<Serializable>(files.keySet()), "Roots");
        dict.setStringForKey(this.getUuid(), "UUID");
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
        return TransferAction.overwrite;
    }

    @Override
    public TransferPathFilter filter(final Session<?> session, final TransferAction action, final ProgressListener listener) {
        if(log.isDebugEnabled()) {
            log.debug(String.format("Filter transfer with action %s", action));
        }
        return new CopyTransferFilter(session, destination, files).withCache(cache);
    }

    @Override
    public List<TransferItem> list(final Session<?> session, final Path directory, final Local local,
                                   final ListProgressListener listener) throws BackgroundException {
        if(log.isDebugEnabled()) {
            log.debug(String.format("List children for %s", directory));
        }
        if(directory.isSymbolicLink()
                && new DownloadSymlinkResolver(roots).resolve(directory)) {
            if(log.isDebugEnabled()) {
                log.debug(String.format("Do not list children for symbolic link %s", directory));
            }
            return Collections.emptyList();
        }
        else {
            final AttributedList<Path> list = session.list(directory, new DisabledListProgressListener());
            final Path copy = files.get(directory);
            for(Path p : list) {
                files.put(p, new Path(copy, p.getName(), p.getType(), p.attributes()));
            }
            final List<TransferItem> nullified = new ArrayList<TransferItem>();
            for(Path p : list) {
                nullified.add(new TransferItem(p));
            }
            return nullified;
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
        final Path copy = files.get(source);
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
                destination.getFeature(Directory.class).mkdir(copy);
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
                out = new ThrottledOutputStream(target.getFeature(Write.class).write(copy, status), throttle);
                new StreamCopier(status, status)
                        .withLimit(status.getLength())
                        .withListener(new DisabledStreamListener() {
                            @Override
                            public void sent(long bytes) {
                                addTransferred(bytes);
                                streamListener.sent(bytes);
                            }
                        }).transfer(in, out);
            }
        }
        catch(IOException e) {
            throw new DefaultIOExceptionMappingService().map("Cannot copy {0}", e, file);
        }
        finally {
            IOUtils.closeQuietly(in);
            IOUtils.closeQuietly(out);
        }
    }
}