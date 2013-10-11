package ch.cyberduck.core.transfer;

/*
 * Copyright (c) 2002-2011 David Kocher. All rights reserved.
 *
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
 * Bug fixes, suggestions and comments should be sent to:
 * dkocher@cyberduck.ch
 */

import ch.cyberduck.core.*;
import ch.cyberduck.core.exception.BackgroundException;
import ch.cyberduck.core.features.Copy;
import ch.cyberduck.core.features.Directory;
import ch.cyberduck.core.features.Read;
import ch.cyberduck.core.features.Write;
import ch.cyberduck.core.io.AbstractStreamListener;
import ch.cyberduck.core.io.BandwidthThrottle;
import ch.cyberduck.core.io.StreamCopier;
import ch.cyberduck.core.io.StreamListener;
import ch.cyberduck.core.io.ThrottledInputStream;
import ch.cyberduck.core.io.ThrottledOutputStream;
import ch.cyberduck.core.serializer.Deserializer;
import ch.cyberduck.core.serializer.Serializer;
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
import java.util.HashMap;
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
    protected Map<Path, Path> files = Collections.emptyMap();

    private Session<?> destination;

    /**
     * @param files Source to destination mapping
     */
    public CopyTransfer(final Host host, final Host target, final Map<Path, Path> files) {
        this(host, target, new CopyRootPathsNormalizer().normalize(files),
                new BandwidthThrottle(Preferences.instance().getFloat("queue.download.bandwidth.bytes")));
    }

    private CopyTransfer(final Host host, final Host target,
                         final Map<Path, Path> selected, final BandwidthThrottle bandwidth) {
        super(host, new ArrayList<Path>(selected.keySet()), bandwidth);
        this.destination = SessionFactory.createSession(target);
        this.files = selected;
    }

    public <T> CopyTransfer(final T serialized) {
        super(serialized, new BandwidthThrottle(Preferences.instance().getFloat("queue.download.bandwidth.bytes")));
        final Deserializer dict = DeserializerFactory.createDeserializer(serialized);
        Object hostObj = dict.objectForKey("Destination");
        if(hostObj != null) {
            destination = SessionFactory.createSession(new Host(hostObj));
        }
        final List destinationsObj = dict.listForKey("Destinations");
        if(destinationsObj != null) {
            this.files = new HashMap<Path, Path>();
            final List<Path> roots = this.getRoots();
            if(destinationsObj.size() == roots.size()) {
                for(int i = 0; i < roots.size(); i++) {
                    this.files.put(roots.get(i), new Path(destinationsObj.get(i)));
                }
            }
        }
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
        dict.setStringForKey(String.valueOf(this.getType().ordinal()), "Kind");
        dict.setObjectForKey(host, "Host");
        if(destination != null) {
            dict.setObjectForKey(destination.getHost(), "Destination");
        }
        List<Path> targets = new ArrayList<Path>();
        for(Path root : this.getRoots()) {
            if(files.containsKey(root)) {
                targets.add(files.get(root));
            }
        }
        dict.setListForKey(new ArrayList<Serializable>(targets), "Destinations");
        dict.setListForKey(this.getRoots(), "Roots");
        dict.setStringForKey(String.valueOf(this.getSize()), "Size");
        dict.setStringForKey(String.valueOf(this.getTransferred()), "Current");
        if(this.getTimestamp() != null) {
            dict.setStringForKey(String.valueOf(this.getTimestamp().getTime()), "Timestamp");
        }
        if(bandwidth != null) {
            dict.setStringForKey(String.valueOf(bandwidth.getRate()), "Bandwidth");
        }
        return dict.getSerialized();
    }

    @Override
    public TransferAction action(final Session<?> session, boolean resumeRequested, boolean reloadRequested,
                                 final TransferPrompt prompt) throws BackgroundException {
        if(log.isDebugEnabled()) {
            log.debug(String.format("Find transfer action for Resume=%s,Reload=%s", resumeRequested, reloadRequested));
        }
        return TransferAction.overwrite;
    }

    @Override
    public TransferPathFilter filter(final Session<?> session, final TransferAction action) {
        if(log.isDebugEnabled()) {
            log.debug(String.format("Filter transfer with action %s", action.toString()));
        }
        return new CopyTransferFilter(destination, files);
    }

    @Override
    public AttributedList<Path> list(final Session<?> session, final Path directory) throws BackgroundException {
        if(log.isDebugEnabled()) {
            log.debug(String.format("List children for %s", directory));
        }
        if(directory.attributes().isSymbolicLink()
                && new DownloadSymlinkResolver(this.getRoots()).resolve(directory)) {
            if(log.isDebugEnabled()) {
                log.debug(String.format("Do not list children for symbolic link %s", directory));
            }
            return AttributedList.emptyList();
        }
        else {
            final AttributedList<Path> list = session.list(directory, new DisabledListProgressListener());
            final Path copy = files.get(directory);
            for(Path p : list) {
                files.put(p, new Path(copy, p.getName(), p.attributes()));
            }
            return list;
        }
    }

    @Override
    public void transfer(final Session<?> session, final Path source, final TransferOptions options, final TransferStatus status) throws BackgroundException {
        if(log.isDebugEnabled()) {
            log.debug(String.format("Transfer file %s with options %s", source, options));
        }
        final Path copy = files.get(source);
        session.message(MessageFormat.format(LocaleFactory.localizedString("Copying {0} to {1}", "Status"),
                source.getName(), copy.getName()));
        if(source.attributes().isFile()) {
            if(session.getHost().equals(destination.getHost())) {
                final Copy feature = session.getFeature(Copy.class);
                if(feature != null) {
                    feature.copy(source, copy);
                    addTransferred(source.attributes().getSize());
                }
                else {
                    this.copy(session, source, destination, copy, bandwidth, new AbstractStreamListener() {
                        @Override
                        public void bytesSent(long bytes) {
                            addTransferred(bytes);
                        }
                    }, status);
                }
            }
            else {
                this.copy(session, source, destination, copy, bandwidth, new AbstractStreamListener() {
                    @Override
                    public void bytesSent(long bytes) {
                        addTransferred(bytes);
                    }
                }, status);
            }
        }
        else {
            if(!status.isExists()) {
                session.message(MessageFormat.format(LocaleFactory.localizedString("Making directory {0}", "Status"),
                        copy.getName()));
                destination.getFeature(Directory.class).mkdir(copy, null);
            }
        }
    }

    /**
     * Default implementation using a temporary file on localhost as an intermediary
     * with a download and upload transfer.
     *
     * @param copy     Destination
     * @param throttle The bandwidth limit
     * @param listener Callback
     * @param status   Transfer status
     */
    public void copy(final Session<?> session, final Path file, final Session<?> target, final Path copy, final BandwidthThrottle throttle,
                     final StreamListener listener, final TransferStatus status) throws BackgroundException {
        InputStream in = null;
        OutputStream out = null;
        try {
            if(file.attributes().isFile()) {
                new StreamCopier(status).transfer(in = new ThrottledInputStream(session.getFeature(Read.class).read(file, status), throttle),
                        0, out = new ThrottledOutputStream(target.getFeature(Write.class).write(copy, status), throttle),
                        listener);
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