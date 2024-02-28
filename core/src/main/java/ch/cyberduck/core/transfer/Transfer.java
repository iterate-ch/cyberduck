package ch.cyberduck.core.transfer;

/*
 *  Copyright (c) 2005 David Kocher. All rights reserved.
 *  http://cyberduck.ch/
 *
 *  This program is free software; you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation; either version 2 of the License, or
 *  (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  Bug fixes, suggestions and comments should be sent to:
 *  dkocher@cyberduck.ch
 */

import ch.cyberduck.core.Cache;
import ch.cyberduck.core.ConnectionCallback;
import ch.cyberduck.core.DescriptiveUrl;
import ch.cyberduck.core.Host;
import ch.cyberduck.core.ListProgressListener;
import ch.cyberduck.core.Local;
import ch.cyberduck.core.LocaleFactory;
import ch.cyberduck.core.Path;
import ch.cyberduck.core.ProgressListener;
import ch.cyberduck.core.Serializable;
import ch.cyberduck.core.Session;
import ch.cyberduck.core.UUIDRandomStringService;
import ch.cyberduck.core.exception.BackgroundException;
import ch.cyberduck.core.exception.LocalAccessDeniedException;
import ch.cyberduck.core.io.BandwidthThrottle;
import ch.cyberduck.core.io.StreamListener;
import ch.cyberduck.core.serializer.Serializer;
import ch.cyberduck.core.shared.DefaultUrlProvider;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicLong;

public abstract class Transfer implements Serializable {
    private static final Logger log = LogManager.getLogger(Transfer.class);

    /**
     * Files and folders initially selected to be part of this transfer
     */
    protected final List<TransferItem> roots = new ArrayList<>();

    /**
     * The sum of the file length of all files in the <code>queue</code> or null if unknown
     */
    private AtomicLong size;

    /**
     * The number bytes already transferred of the files in the <code>queue</code> or null if unknown
     */
    private AtomicLong transferred;

    private final Map<Local, Object> locks = new HashMap<>();

    public abstract Type getType();

    public enum Type {
        download {
            @Override
            public boolean isReloadable() {
                return true;
            }
        },
        upload {
            @Override
            public boolean isReloadable() {
                return true;
            }
        },
        sync {
            @Override
            public boolean isReloadable() {
                return true;
            }
        },
        copy {
            @Override
            public boolean isReloadable() {
                return true;
            }
        },
        move {
            @Override
            public boolean isReloadable() {
                return false;
            }
        };

        public abstract boolean isReloadable();

    }

    protected final Host host;

    /**
     * In Bytes per second
     */
    protected BandwidthThrottle bandwidth;


    /**
     * The transfer has been reset
     */
    private boolean reset;

    /**
     * Last transferred timestamp
     */
    protected Date timestamp;

    /**
     * Unique identifier
     */
    protected String uuid
            = new UUIDRandomStringService().random();

    /**
     * Transfer state
     */
    private State state = State.stopped;

    private enum State {
        running,
        stopped
    }

    /**
     * @return True if in <code>running</code> state
     */
    public boolean isRunning() {
        return state == State.running;
    }

    /**
     * Create a transfer with a single root which can be a plain file or a directory
     *
     * @param host Connection details
     * @param root File or directory
     */
    public Transfer(final Host host, final Path root, final Local folder, final BandwidthThrottle bandwidth) {
        this(host, Collections.singletonList(new TransferItem(root, folder)), bandwidth);
    }

    /**
     * @param host  Connection details
     * @param roots List of files to add to transfer
     */
    public Transfer(final Host host, final List<TransferItem> roots, final BandwidthThrottle bandwidth) {
        this.host = host;
        this.roots.addAll(roots);
        this.bandwidth = bandwidth;
    }

    public abstract Transfer withCache(final Cache<Path> cache);

    public <T> T serialize(final Serializer<T> dict) {
        dict.setStringForKey(this.getType().name(), "Type");
        dict.setObjectForKey(host, "Host");
        dict.setListForKey(roots, "Items");
        dict.setStringForKey(uuid, "UUID");
        if(size != null) {
            dict.setStringForKey(String.valueOf(size), "Size");
        }
        if(transferred != null) {
            dict.setStringForKey(String.valueOf(transferred), "Current");
        }
        if(timestamp != null) {
            dict.setStringForKey(String.valueOf(timestamp.getTime()), "Timestamp");
        }
        if(bandwidth != null) {
            dict.setStringForKey(String.valueOf(bandwidth.getRate()), "Bandwidth");
        }
        return dict.getSerialized();
    }

    /**
     * @param bytesPerSecond Maximum number of bytes to transfer by second
     */
    public void setBandwidth(final float bytesPerSecond) {
        if(log.isDebugEnabled()) {
            log.debug(String.format("Throttle bandwidth to %s bytes per second", bytesPerSecond));
        }
        bandwidth.setRate(bytesPerSecond);
    }

    public void setBandwidth(final BandwidthThrottle bandwidth) {
        this.bandwidth = bandwidth;
    }

    /**
     * @return Rate in bytes per second allowed for this transfer
     */
    public BandwidthThrottle getBandwidth() {
        return bandwidth;
    }

    /**
     * @return Time when transfer did end
     */
    public Date getTimestamp() {
        return timestamp;
    }

    /**
     * @return The first <code>root</code> added to this transfer
     */
    public TransferItem getRoot() {
        return roots.iterator().next();
    }

    public DescriptiveUrl getRemote() {
        if(this.roots.size() == 1) {
            return new DefaultUrlProvider(host).toUrl(this.getRoot().remote).find(DescriptiveUrl.Type.provider);
        }
        else {
            return new DefaultUrlProvider(host).toUrl(this.getRoot().remote.getParent()).find(DescriptiveUrl.Type.provider);
        }
    }

    public String getLocal() {
        final Local local = roots.iterator().next().local;
        if(roots.size() == 1) {
            return local.getAbbreviatedPath();
        }
        else {
            return local.getParent().getAbbreviatedPath();
        }
    }

    public List<TransferItem> getRoots() {
        return roots;
    }

    public Host getSource() {
        return host;
    }

    public Host getDestination() {
        return null;
    }

    public String getName() {
        if(roots.isEmpty()) {
            return LocaleFactory.localizedString("None");
        }
        final StringBuilder name = new StringBuilder();
        name.append(roots.iterator().next().remote.getName());
        if(roots.size() > 1) {
            name.append("… (").append(roots.size()).append(")");
        }
        return name.toString();
    }

    /**
     * @param source      Connection to source server of transfer. May be null.
     * @param destination Connection to target server of transfer
     * @param action      Transfer action for duplicate files
     * @param listener    Progress listener
     * @return Null if the filter could not be determined and the transfer should be canceled instead
     */
    public abstract TransferPathFilter filter(Session<?> source, final Session<?> destination, TransferAction action, ProgressListener listener);

    /**
     * @param source          Connection to source server of transfer. May be null.
     * @param destination     Connection to target server of transfer
     * @param resumeRequested Requested resume
     * @param reloadRequested Requested overwrite
     * @param prompt          Callback
     * @param listener        Listener
     * @return Duplicate file strategy from preferences or user selection
     */
    public abstract TransferAction action(Session<?> source, final Session<?> destination, boolean resumeRequested, boolean reloadRequested,
                                          TransferPrompt prompt, ListProgressListener listener) throws BackgroundException;

    /**
     * Returns the children of this path filtering it with the default regex filter
     *
     * @param session   Connection to source server of transfer. May be null.
     * @param directory The directory to list the children
     * @param local     Local directory
     * @param listener  Listener
     * @return A list of child items
     */
    public abstract List<TransferItem> list(Session<?> session, Path directory, Local local, ListProgressListener listener) throws BackgroundException;

    /**
     * @param source      Connection to source server of transfer. May be null.
     * @param destination Connection to target server of transfer
     * @param files       Files pending transfer
     * @param filter      Transfer filter
     * @param error       Error callback
     * @param listener    Listener
     * @param callback    Prompt
     */
    public void pre(final Session<?> source, final Session<?> destination, final Map<TransferItem, TransferStatus> files,
                    final TransferPathFilter filter, final TransferErrorCallback error, final ProgressListener listener, final ConnectionCallback callback) throws BackgroundException {
        for(TransferItem item : roots) {
            try {
                switch(this.getType()) {
                    case download:
                        final Local directory = item.local.getParent();
                        locks.put(directory, directory.lock(true));
                        break;
                    case upload:
                        locks.put(item.local, item.local.lock(true));
                        break;
                }
            }
            catch(LocalAccessDeniedException e) {
                log.warn(String.format("Failure obtaining lock for %s. %s", item.local, e));
            }
        }
    }

    /**
     * @param source      Connection to source server of transfer. May be null.
     * @param destination Connection to target server of transfer
     * @param files       Files transferred
     * @param error       Error callback
     * @param listener    Listener
     * @param callback    Prompt
     */
    public void post(final Session<?> source, final Session<?> destination, final Map<TransferItem, TransferStatus> files,
                     final TransferErrorCallback error, final ProgressListener listener, final ConnectionCallback callback) throws BackgroundException {
        for(Iterator<Map.Entry<Local, Object>> iter = locks.entrySet().iterator(); iter.hasNext(); ) {
            final Map.Entry<Local, Object> entry = iter.next();
            switch(this.getType()) {
                case download:
                    final Local directory = entry.getKey().getParent();
                    directory.release(entry.getValue());
                    break;
                case upload:
                    entry.getKey().release(entry.getValue());
                    break;
            }
            iter.remove();
        }
    }

    /**
     * The actual transfer implementation
     *
     * @param source             Connection to source server of transfer
     * @param destination        Connection to target server of transfer
     * @param file               Remote file path
     * @param local              Local file reference
     * @param options            Quarantine option
     * @param overall            Overall transfer status
     * @param segment            Segment transfer status
     * @param connectionCallback Prompt for alerts to user
     * @param progressListener   Progress messages listener
     * @param streamListener     Byte count listener
     */
    public abstract void transfer(Session<?> source, Session<?> destination, Path file, Local local,
                                  TransferOptions options, final TransferStatus overall, TransferStatus segment,
                                  ConnectionCallback connectionCallback,
                                  ProgressListener progressListener,
                                  StreamListener streamListener) throws BackgroundException;

    public void start() {
        state = State.running;
        // Will be set to true in #reset when transfer action is determined
        reset = false;
    }

    /**
     * Normalize paths of root files
     *
     * @see ch.cyberduck.core.transfer.normalizer.RootPathsNormalizer
     */
    public abstract void normalize();

    public void stop() {
        state = State.stopped;
        timestamp = new Date();
    }

    /**
     * Recalculate the size of the <code>queue</code>
     */
    public synchronized void reset() {
        if(log.isDebugEnabled()) {
            log.debug(String.format("Reset status for %s", this));
        }
        transferred = null;
        size = null;
        reset = true;
    }

    /**
     * @return True if transfer has been reset before starting.
     */
    public boolean isReset() {
        return reset;
    }

    /**
     * @return True if the bytes transferred equal the size of the queue
     */
    public synchronized boolean isComplete() {
        if(null == size || null == transferred) {
            return false;
        }
        return Objects.equals(this.getSize(), this.getTransferred());
    }

    /**
     * @return The sum of all file lengths in this transfer.
     */
    public Long getSize() {
        if(null == size) {
            return 0L;
        }
        return size.get();
    }

    public void addSize(final long bytes) {
        if(null == size) {
            // Initialize
            size = new AtomicLong(0L);
        }
        if(bytes > 0) {
            size.addAndGet(bytes);
        }
        if(log.isDebugEnabled()) {
            log.debug(String.format("Size set to %d bytes", size.get()));
        }
    }

    /**
     * @return The number of bytes transferred of all files.
     */
    public Long getTransferred() {
        if(null == transferred) {
            return 0L;
        }
        return transferred.get();
    }

    public void addTransferred(final long bytes) {
        if(null == transferred) {
            // Initialize
            transferred = new AtomicLong(0L);
        }
        // Allow decrement for failed segments
        transferred.addAndGet(bytes);
        if(log.isTraceEnabled()) {
            log.trace(String.format("Transferred set to %d bytes", transferred.get()));
        }
    }

    public String getUuid() {
        return uuid;
    }

    public void setUuid(final String uuid) {
        this.uuid = uuid;
    }

    public void setTimestamp(final Date timestamp) {
        this.timestamp = timestamp;
    }

    public void setSize(final Long bytes) {
        if(null == size) {
            // Initialize
            size = new AtomicLong(0L);
        }
        size.set(bytes);
    }

    public void setTransferred(final Long bytes) {
        if(null == transferred) {
            // Initialize
            transferred = new AtomicLong(0L);
        }
        transferred.set(bytes);
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("Transfer{");
        sb.append("transferred=").append(transferred);
        sb.append(", size=").append(size);
        sb.append(", roots=").append(roots);
        sb.append(", state=").append(state);
        sb.append(", host=").append(host);
        sb.append('}');
        return sb.toString();
    }
}
