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

import ch.cyberduck.core.Acl;
import ch.cyberduck.core.Local;
import ch.cyberduck.core.Path;
import ch.cyberduck.core.PathAttributes;
import ch.cyberduck.core.Permission;
import ch.cyberduck.core.features.Encryption;
import ch.cyberduck.core.io.Checksum;
import ch.cyberduck.core.io.StreamCancelation;
import ch.cyberduck.core.io.StreamProgress;

import org.apache.log4j.Logger;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;

public class TransferStatus implements StreamCancelation, StreamProgress {
    private static final Logger log = Logger.getLogger(TransferStatus.class);

    public static final long KILO = 1024; //2^10
    public static final long MEGA = 1048576; // 2^20
    public static final long GIGA = 1073741824; // 2^30

    private Rename rename
            = new Rename();
    /**
     * Target file or directory already exists
     */
    private boolean exists = false;

    /**
     * Append to file with offset
     *
     * @see #offset
     */
    private boolean append = false;

    /**
     * This is a part of a segmented transfer
     */
    private boolean segment = false;

    /**
     * Not accepted
     */
    private boolean rejected = false;

    /**
     * The number of transferred bytes. Must be less or equals size.
     */
    private AtomicLong offset
            = new AtomicLong(0);
    /**
     * Transfer size. May be less than the file size in attributes or 0 if creating symbolic links.
     */
    private long length = 0L;

    /**
     * The transfer has been canceled by the user.
     */
    private AtomicBoolean canceled
            = new AtomicBoolean();

    private AtomicBoolean complete
            = new AtomicBoolean();

    private CountDownLatch done
            = new CountDownLatch(1);

    private Checksum checksum;

    /**
     * MIME type
     */
    private String mime;

    /**
     * Current remote attributes of existing file including UNIX permissions, timestamp and ACL
     */
    private PathAttributes remote = new PathAttributes();

    /**
     * Target UNIX permissions to set when transfer is complete
     */
    private Permission permission = Permission.EMPTY;

    /**
     * Target ACL to set when transfer is complete
     */
    private Acl acl = Acl.EMPTY;

    private Encryption.Algorithm encryption = Encryption.Algorithm.NONE;

    /**
     * Storage class parameter
     */
    private String storageClass;

    /**
     * Target timestamp to set when transfer is complete
     */
    private Long timestamp;

    private Map<String, String> parameters
            = Collections.emptyMap();

    private Map<String, String> metadata
            = Collections.emptyMap();

    private List<TransferStatus> segments
            = Collections.emptyList();

    /**
     * Part number
     */
    private Integer part;

    /**
     * Await completion
     *
     * @return True if complete
     */
    public boolean await() {
        // Lock until complete
        try {
            done.await();
        }
        catch(InterruptedException e) {
            log.error("Failure waiting for status to complete");
        }
        return complete.get();
    }

    public boolean isComplete() {
        return complete.get();
    }

    @Override
    public void setComplete() {
        complete.set(true);
        done.countDown();
    }

    public TransferStatus complete() {
        this.setComplete();
        return this;
    }

    public void setFailure() {
        complete.set(false);
        done.countDown();
    }

    /**
     * If this path is currently transferred, interrupt it as soon as possible
     */
    public void setCanceled() {
        canceled.set(true);
        done.countDown();
    }

    /**
     * @return True if marked for interrupt
     */
    public boolean isCanceled() {
        return canceled.get();
    }

    /**
     * @return Number of bytes transferred
     */
    public long getOffset() {
        return offset.get();
    }

    /**
     * @param bytes The already transferred bytes
     */
    public void setOffset(final long bytes) {
        offset.set(bytes);
        if(log.isDebugEnabled()) {
            log.debug(String.format("Transferred bytes set to %d bytes", bytes));
        }
    }

    @Override
    public void progress(final long bytes) {
        this.setOffset(offset.get() + bytes);
    }

    public TransferStatus skip(final long bytes) {
        this.setOffset(bytes);
        return this;
    }

    /**
     * @return Transfer content length
     */
    public long getLength() {
        return length;
    }

    /**
     * @param bytes Transfer content length
     */
    public void setLength(final long bytes) {
        this.length = bytes;
    }

    /**
     * @param bytes Transfer content length
     */
    public TransferStatus length(final long bytes) {
        this.setLength(bytes);
        return this;
    }

    public boolean isExists() {
        return exists;
    }

    public void setExists(final boolean exists) {
        this.exists = exists;
    }

    public TransferStatus exists(boolean exists) {
        this.exists = exists;
        return this;
    }

    public boolean isAppend() {
        return append;
    }

    /**
     * Mark this path with an append flag when transferred
     *
     * @param append If false, the current status is cleared
     * @see #setOffset(long)
     */
    public void setAppend(final boolean append) {
        if(!append) {
            offset.set(0);
        }
        this.append = append;
    }

    public TransferStatus append(final boolean append) {
        this.setAppend(append);
        return this;
    }

    public boolean isSegment() {
        return segment;
    }

    public void setSegment(final boolean segment) {
        this.segment = segment;
    }

    public TransferStatus segment(final boolean segment) {
        this.segment = segment;
        return this;
    }

    public void setRejected(boolean rejected) {
        this.rejected = rejected;
    }

    public TransferStatus reject(final boolean rejected) {
        this.setRejected(rejected);
        return this;
    }

    public boolean isRejected() {
        return rejected;
    }

    public Rename getRename() {
        return rename;
    }

    public TransferStatus rename(final Path renamed) {
        this.rename.remote = renamed;
        return this;
    }

    public TransferStatus rename(final Local renamed) {
        this.rename.local = renamed;
        return this;
    }

    public boolean isRename() {
        if(this.isAppend()) {
            return false;
        }
        return rename.remote != null || rename.local != null;
    }

    public void setRename(final Rename rename) {
        this.rename = rename;
    }

    public String getMime() {
        return mime;
    }

    public void setMime(final String mime) {
        this.mime = mime;
    }

    public Checksum getChecksum() {
        return checksum;
    }

    public void setChecksum(final Checksum checksum) {
        this.checksum = checksum;
    }

    public PathAttributes getRemote() {
        return remote;
    }

    public void setRemote(PathAttributes remote) {
        this.remote = remote;
    }

    public Permission getPermission() {
        return permission;
    }

    public void setPermission(Permission permission) {
        this.permission = permission;
    }

    public Acl getAcl() {
        return acl;
    }

    public void setAcl(Acl acl) {
        this.acl = acl;
    }

    public Encryption.Algorithm getEncryption() {
        return encryption;
    }

    public void setEncryption(final Encryption.Algorithm encryption) {
        this.encryption = encryption;
    }

    public String getStorageClass() {
        return storageClass;
    }

    public void setStorageClass(final String storageClass) {
        this.storageClass = storageClass;
    }

    public Long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Long timestamp) {
        this.timestamp = timestamp;
    }

    public Map<String, String> getParameters() {
        return parameters;
    }

    public void setParameters(final Map<String, String> parameters) {
        this.parameters = parameters;
    }

    public TransferStatus parameters(final Map<String, String> parameters) {
        this.parameters = parameters;
        return this;
    }

    public Map<String, String> getMetadata() {
        return metadata;
    }

    public void setMetadata(final Map<String, String> metadata) {
        this.metadata = metadata;
    }

    public TransferStatus metadata(final Map<String, String> metadata) {
        this.metadata = metadata;
        return this;
    }

    public Integer getPart() {
        return part;
    }

    public void setPart(final Integer part) {
        this.part = part;
    }

    public List<TransferStatus> getSegments() {
        if(segments.isEmpty()) {
            return Collections.singletonList(this);
        }
        return segments;
    }

    public TransferStatus withSegments(final List<TransferStatus> segments) {
        this.segments = segments;
        return this;
    }

    public boolean isSegmented() {
        return !segments.isEmpty();
    }

    @Override
    public boolean equals(final Object o) {
        if(this == o) {
            return true;
        }
        if(o == null || getClass() != o.getClass()) {
            return false;
        }
        final TransferStatus that = (TransferStatus) o;
        if(append != that.append) {
            return false;
        }
        if(exists != that.exists) {
            return false;
        }
        if(length != that.length) {
            return false;
        }
        return true;
    }

    @Override
    public int hashCode() {
        int result = (exists ? 1 : 0);
        result = 31 * result + (append ? 1 : 0);
        result = 31 * result + (int) (length ^ (length >>> 32));
        return result;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("TransferStatus{");
        sb.append("exists=").append(exists);
        sb.append(", append=").append(append);
        sb.append(", segments=").append(segments);
        sb.append(", offset=").append(offset);
        sb.append(", length=").append(length);
        sb.append(", canceled=").append(canceled);
        sb.append(", renamed=").append(rename);
        sb.append('}');
        return sb.toString();
    }

    public static final class Rename {
        /**
         * Upload target
         */
        public Path remote;
        public Local local;

        @Override
        public String toString() {
            final StringBuilder sb = new StringBuilder("Rename{");
            sb.append("local=").append(local);
            sb.append(", remote=").append(remote);
            sb.append('}');
            return sb.toString();
        }
    }
}
