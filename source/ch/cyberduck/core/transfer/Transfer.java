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

import ch.cyberduck.core.AttributedList;
import ch.cyberduck.core.Collection;
import ch.cyberduck.core.DescriptiveUrl;
import ch.cyberduck.core.DeserializerFactory;
import ch.cyberduck.core.Host;
import ch.cyberduck.core.LocaleFactory;
import ch.cyberduck.core.Path;
import ch.cyberduck.core.Serializable;
import ch.cyberduck.core.Session;
import ch.cyberduck.core.exception.BackgroundException;
import ch.cyberduck.core.io.BandwidthThrottle;
import ch.cyberduck.core.serializer.Deserializer;
import ch.cyberduck.core.serializer.Serializer;
import ch.cyberduck.core.shared.DefaultUrlProvider;

import org.apache.log4j.Logger;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;

/**
 * @version $Id$
 */
public abstract class Transfer implements Serializable {
    private static final Logger log = Logger.getLogger(Transfer.class);

    /**
     * Files and folders initially selected to be part of this transfer
     */
    private List<Path> roots;

    /**
     * The sum of the file length of all files in the <code>queue</code>
     */
    private long size = 0;

    /**
     * The number bytes already transferred of the files in the <code>queue</code>
     */
    private long transferred = 0;

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

    protected Host host;

    /**
     * In Bytes per second
     */
    protected BandwidthThrottle bandwidth
            = new BandwidthThrottle(BandwidthThrottle.UNLIMITED);


    /**
     * The transfer has been reset
     */
    private boolean reset;

    /**
     * Last transferred timestamp
     */
    private Date timestamp;

    /**
     * Transfer state
     */
    private State state = State.stopped;

    private enum State {
        running,
        /**
         * The transfer has been canceled and should not continue any further processing
         */
        canceled,
        stopped
    }

    /**
     * @return True if in <code>running</code> state
     */
    public boolean isRunning() {
        return state == State.running;
    }

    /**
     * @return True if in <code>canceled</code> state
     */
    public boolean isCanceled() {
        return state == State.canceled;
    }

    /**
     * Create a transfer with a single root which can
     * be a plain file or a directory
     *
     * @param host Connection details
     * @param root File or directory
     */
    public Transfer(final Host host, final Path root, final BandwidthThrottle bandwidth) {
        this(host, new Collection<Path>(Collections.<Path>singletonList(root)), bandwidth);
    }

    /**
     * @param host  Connection details
     * @param roots List of files to add to transfer
     */
    public Transfer(final Host host, final List<Path> roots, final BandwidthThrottle bandwidth) {
        this.host = host;
        this.roots = roots;
        this.bandwidth = bandwidth;
    }

    public <T> Transfer(final T serialized, final BandwidthThrottle bandwidth) {
        this.bandwidth = bandwidth;
        final Deserializer dict = DeserializerFactory.createDeserializer(serialized);
        final Object hostObj = dict.objectForKey("Host");
        if(hostObj != null) {
            this.host = new Host(hostObj);
        }
        final List rootsObj = dict.listForKey("Roots");
        if(rootsObj != null) {
            roots = new ArrayList<Path>();
            for(Object rootDict : rootsObj) {
                final Path root = new Path(rootDict);
                roots.add(root);
            }
        }
        Object sizeObj = dict.stringForKey("Size");
        if(sizeObj != null) {
            size = (long) Double.parseDouble(sizeObj.toString());
        }
        Object timestampObj = dict.stringForKey("Timestamp");
        if(timestampObj != null) {
            timestamp = new Date(Long.parseLong(timestampObj.toString()));
        }
        Object currentObj = dict.stringForKey("Current");
        if(currentObj != null) {
            transferred = (long) Double.parseDouble(currentObj.toString());
        }
        Object bandwidthObj = dict.stringForKey("Bandwidth");
        if(bandwidthObj != null) {
            bandwidth.setRate(Float.parseFloat(bandwidthObj.toString()));
        }
    }

    public <T> T serialize(final Serializer dict) {
        dict.setStringForKey(String.valueOf(this.getType().ordinal()), "Kind");
        dict.setObjectForKey(host, "Host");
        dict.setListForKey(roots, "Roots");
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
    public Path getRoot() {
        return roots.get(0);
    }

    public String getRemote() {
        return new DefaultUrlProvider(host).toUrl(this.getRoot()).find(DescriptiveUrl.Type.provider).getUrl();
    }

    public String getLocal() {
        return this.getRoot().getLocal().toURL();
    }

    public List<Path> getRoots() {
        return roots;
    }

    public Host getHost() {
        return host;
    }

    public String getName() {
        if(roots.isEmpty()) {
            return LocaleFactory.localizedString("None");
        }
        final StringBuilder name = new StringBuilder();
        name.append(roots.get(0).getName());
        if(roots.size() > 1) {
            name.append("… (").append(roots.size()).append(")");
        }
        return name.toString();
    }

    /**
     * @param session Session
     * @param action  Transfer action for duplicate files
     * @return Null if the filter could not be determined and the transfer should be canceled instead
     */
    public abstract TransferPathFilter filter(Session<?> session, TransferAction action);

    /**
     * @param session         Session
     * @param resumeRequested Requested resume
     * @param reloadRequested Requested overwrite
     * @param prompt          Callback
     * @return Duplicate file strategy from preferences or user selection
     */
    public abstract TransferAction action(Session<?> session, boolean resumeRequested, boolean reloadRequested,
                                          TransferPrompt prompt) throws BackgroundException;

    /**
     * Returns the children of this path filtering it with the default regex filter
     *
     * @param session   Session
     * @param directory The directory to list the children
     * @return A list of child items
     */
    public abstract AttributedList<Path> list(Session<?> session, Path directory, TransferStatus parent) throws BackgroundException;

    /**
     * The actual transfer implementation
     *
     * @param session Session
     * @param file    File
     * @param options Quarantine option
     * @param status  Transfer status
     */
    public abstract void transfer(Session<?> session, Path file, TransferOptions options, TransferStatus status) throws BackgroundException;

    public void start() {
        state = State.running;
    }

    public void stop() {
        state = State.stopped;
        timestamp = new Date();
    }

    /**
     * Recalculate the size of the <code>queue</code>
     */
    public void reset() {
        if(log.isDebugEnabled()) {
            log.debug(String.format("Reset status for %s", this));
        }
        transferred = 0;
        size = 0;
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
    public boolean isComplete() {
        if(this.isRunning()) {
            return false;
        }
        return size == transferred;
    }

    /**
     * @return The sum of all file lengths in this transfer.
     */
    public long getSize() {
        return size;
    }

    public void addSize(final long bytes) {
        size += bytes;
    }

    /**
     * @return The number of bytes transferred of all files.
     */
    public long getTransferred() {
        return transferred;
    }

    public void addTransferred(final long bytes) {
        transferred += bytes;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("Transfer{");
        sb.append("roots=").append(roots);
        sb.append(", session=").append(host);
        sb.append(", state=").append(state);
        sb.append('}');
        return sb.toString();
    }
}