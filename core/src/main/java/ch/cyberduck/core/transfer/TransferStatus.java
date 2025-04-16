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
import ch.cyberduck.core.concurrency.Interruptibles;
import ch.cyberduck.core.exception.BackgroundException;
import ch.cyberduck.core.exception.ConnectionCanceledException;
import ch.cyberduck.core.exception.TransferStatusCanceledException;
import ch.cyberduck.core.features.Encryption;
import ch.cyberduck.core.io.Checksum;
import ch.cyberduck.core.io.StreamCancelation;
import ch.cyberduck.core.io.StreamProgress;
import ch.cyberduck.core.random.NonceGenerator;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.nio.ByteBuffer;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;

public class TransferStatus implements TransferResponse, StreamCancelation, StreamProgress {
    private static final Logger log = LogManager.getLogger(TransferStatus.class);

    public static final long KILO = 1024; //2^10
    public static final long MEGA = 1048576; // 2^20
    public static final long GIGA = 1073741824; // 2^30

    public static final long UNKNOWN_LENGTH = -1L;

    /**
     * Change target filename
     */
    private Rename rename
            = new Rename();

    /**
     * Temporary filename only used for transfer. Rename when file transfer is complete
     */
    private final Displayname displayname
            = new Displayname();

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
     * Set hidden flag on file if applicable
     */
    private boolean hidden = false;

    /**
     * Offset to read from input stream. Must be less or equals size.
     */
    private final AtomicLong offset
            = new AtomicLong(0);
    /**
     * Transfer size. May be less than the file size in attributes or 0 if creating symbolic links.
     */
    private long length = TransferStatus.UNKNOWN_LENGTH;

    /**
     * Destination size may differ when encrypted or by some other transformation
     */
    private long destinationlength = TransferStatus.UNKNOWN_LENGTH;

    /**
     * The transfer has been canceled by the user.
     */
    private final AtomicBoolean canceled
            = new AtomicBoolean();

    private final AtomicBoolean complete
            = new AtomicBoolean();

    private final CountDownLatch done
            = new CountDownLatch(1);

    private Checksum checksum = Checksum.NONE;

    /**
     * MIME type
     */
    private String mime;

    /**
     * Current remote attributes of existing file including UNIX permissions, timestamp and ACL
     */
    private PathAttributes remote = PathAttributes.EMPTY;

    /**
     * Remote attributes after upload completed to be set in write feature
     */
    private PathAttributes response = PathAttributes.EMPTY;

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
    private Long modified;
    private Long created;

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
     * Part URL
     */
    private String url;

    /**
     * Encrypted file header
     */
    private ByteBuffer header;

    /**
     * File key
     */
    private ByteBuffer filekey;

    /**
     * Chunk nonces
     */
    private NonceGenerator nonces;

    private Object lockId;

    /**
     * Region set in transfer status for folder in transfer
     */
    private String region;

    public TransferStatus() {
        // Default
    }

    public TransferStatus(final TransferStatus copy) {
        this.rename.remote = copy.rename.remote;
        this.rename.local = copy.rename.local;
        this.displayname.local = copy.displayname.local;
        this.displayname.remote = copy.displayname.remote;
        this.exists = copy.exists;
        this.append = copy.append;
        this.segment = copy.segment;
        this.segments = copy.segments;
        this.rejected = copy.rejected;
        this.hidden = copy.hidden;
        this.offset.set(copy.offset.get());
        this.length = copy.length;
        this.destinationlength = copy.destinationlength;
        this.canceled.set(copy.canceled.get());
        this.complete.set(copy.complete.get());
        this.checksum = copy.checksum;
        this.mime = copy.mime;
        this.remote = copy.remote;
        this.response = copy.response;
        this.permission = copy.permission;
        this.acl = copy.acl;
        this.encryption = copy.encryption;
        this.storageClass = copy.storageClass;
        this.modified = copy.modified;
        this.created = copy.created;
        this.parameters = copy.parameters;
        this.metadata = copy.metadata;
        this.segment = copy.segment;
        this.part = copy.part;
        this.url = copy.url;
        this.header = copy.header;
        this.filekey = copy.filekey;
        this.nonces = copy.nonces;
        this.lockId = copy.lockId;
        this.region = copy.region;
    }

    /**
     * Await completion
     *
     * @return True if complete
     */
    public boolean await() throws ConnectionCanceledException {
        // Lock until complete
        Interruptibles.await(done, ConnectionCanceledException.class);
        return complete.get();
    }

    public boolean isComplete() {
        return complete.get();
    }

    @Override
    public TransferStatus setComplete() {
        complete.set(true);
        done.countDown();
        return this;
    }

    @Override
    public TransferStatus setFailure(final BackgroundException failure) {
        complete.set(false);
        done.countDown();
        return this;
    }

    /**
     * If this path is currently transferred, interrupt it as soon as possible
     */
    public TransferStatus setCanceled() {
        for(TransferStatus segment : segments) {
            segment.setCanceled();
        }
        canceled.set(true);
        done.countDown();
        return this;
    }

    /**
     *
     */
    @Override
    public void validate() throws ConnectionCanceledException {
        for(TransferStatus segment : segments) {
            segment.validate();
        }
        if(canceled.get()) {
            throw new TransferStatusCanceledException();
        }
    }

    /**
     * @return Offset to read from
     */
    public long getOffset() {
        return offset.get();
    }

    /**
     * @param bytes The already transferred bytes
     */
    public TransferStatus setOffset(final long bytes) {
        offset.set(bytes);
        log.trace("Offset set to {} bytes", bytes);
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
    public TransferStatus setLength(final long bytes) {
        this.length = bytes;
        return this;
    }

    public long getDestinationlength() {
        if(UNKNOWN_LENGTH == destinationlength) {
            return length;
        }
        return destinationlength;
    }

    public TransferStatus setDestinationLength(final long destinationlength) {
        this.destinationlength = destinationlength;
        return this;
    }

    public boolean isExists() {
        return exists;
    }

    public TransferStatus setExists(final boolean exists) {
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
    public TransferStatus setAppend(final boolean append) {
        if(!append) {
            offset.set(0);
        }
        this.append = append;
        return this;
    }

    public boolean isSegment() {
        return segment;
    }

    public TransferStatus setSegment(final boolean segment) {
        this.segment = segment;
        return this;
    }

    public TransferStatus setRejected(boolean rejected) {
        this.rejected = rejected;
        return this;
    }

    public boolean isRejected() {
        return rejected;
    }

    public boolean isHidden() {
        return hidden;
    }

    public TransferStatus setHidden(final boolean hidden) {
        this.hidden = hidden;
        return this;
    }

    public Rename getRename() {
        return rename;
    }

    public Displayname getDisplayname() {
        return displayname;
    }

    public TransferStatus setRename(final Path renamed) {
        this.rename.withRemote(renamed);
        return this;
    }

    public TransferStatus setRename(final Local renamed) {
        this.rename.withLocal(renamed);
        return this;
    }

    /**
     * @param finalname Target filename to rename temporary file to
     */
    public TransferStatus setDisplayname(final Local finalname) {
        this.displayname.withLocal(finalname);
        return this;
    }

    public TransferStatus setDisplayname(final Path finalname) {
        this.displayname.withRemote(finalname);
        return this;
    }

    public TransferStatus setRename(final Rename rename) {
        this.rename = rename;
        return this;
    }

    public String getMime() {
        return mime;
    }

    public TransferStatus setMime(final String type) {
        this.mime = type;
        return this;
    }

    public Checksum getChecksum() {
        return checksum;
    }

    public TransferStatus setChecksum(final Checksum checksum) {
        this.checksum = checksum;
        return this;
    }

    public PathAttributes getRemote() {
        return remote;
    }

    public TransferStatus setRemote(final PathAttributes attributes) {
        this.remote = attributes;
        return this;
    }

    @Override
    public PathAttributes getResponse() {
        return response;
    }

    @Override
    public TransferStatus setResponse(PathAttributes attributes) {
        this.response = attributes;
        return this;
    }

    public Permission getPermission() {
        return permission;
    }

    public TransferStatus setPermission(Permission permission) {
        this.permission = permission;
        return this;
    }

    public Acl getAcl() {
        return acl;
    }

    public TransferStatus setAcl(Acl acl) {
        this.acl = acl;
        return this;
    }

    public Encryption.Algorithm getEncryption() {
        return encryption;
    }

    public TransferStatus setEncryption(final Encryption.Algorithm encryption) {
        this.encryption = encryption;
        return this;
    }

    public String getStorageClass() {
        return storageClass;
    }

    public TransferStatus setStorageClass(final String storageClass) {
        this.storageClass = storageClass;
        return this;
    }

    public Long getModified() {
        return modified;
    }

    public TransferStatus setModified(Long modified) {
        this.modified = modified;
        return this;
    }

    public Long getCreated() {
        return created;
    }

    public TransferStatus setCreated(final Long created) {
        this.created = created;
        return this;
    }

    public Map<String, String> getParameters() {
        return parameters;
    }

    public TransferStatus setParameters(final Map<String, String> parameters) {
        this.parameters = parameters;
        return this;
    }

    public Map<String, String> getMetadata() {
        return metadata;
    }

    public TransferStatus setMetadata(final Map<String, String> metadata) {
        this.metadata = metadata;
        return this;
    }

    public Integer getPart() {
        return part;
    }

    public TransferStatus setPart(final Integer part) {
        this.part = part;
        return this;
    }

    public String getUrl() {
        return url;
    }

    public TransferStatus setUrl(final String url) {
        this.url = url;
        return this;
    }

    public List<TransferStatus> getSegments() {
        if(segments.isEmpty()) {
            return Collections.singletonList(this);
        }
        return segments;
    }

    public TransferStatus setSegments(final List<TransferStatus> segments) {
        this.segments = segments;
        return this;
    }

    public boolean isSegmented() {
        return !segments.isEmpty();
    }

    public ByteBuffer getHeader() {
        return header;
    }

    public TransferStatus setHeader(final ByteBuffer header) {
        this.header = header;
        return this;
    }

    public ByteBuffer getFilekey() {
        return filekey;
    }

    public TransferStatus setFilekey(final ByteBuffer filekey) {
        this.filekey = filekey;
        return this;
    }

    public NonceGenerator getNonces() {
        return nonces;
    }

    public TransferStatus setNonces(final NonceGenerator nonces) {
        this.nonces = nonces;
        return this;
    }

    public Object getLockId() {
        return lockId;
    }

    public TransferStatus setLockId(final Object lockId) {
        this.lockId = lockId;
        return this;
    }

    public String getRegion() {
        return region;
    }

    public TransferStatus setRegion(final String region) {
        this.region = region;
        return this;
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
        return length == that.length && Objects.equals(offset.longValue(), that.offset.longValue()) && Objects.equals(part, that.part);
    }

    @Override
    public int hashCode() {
        return Objects.hash(offset.longValue(), length, part);
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("TransferStatus{");
        sb.append("rename=").append(rename);
        sb.append(", exists=").append(exists);
        sb.append(", append=").append(append);
        sb.append(", segment=").append(segment);
        sb.append(", offset=").append(offset);
        sb.append(", length=").append(length);
        sb.append(", checksum=").append(checksum);
        sb.append(", mime='").append(mime).append('\'');
        sb.append(", permission=").append(permission);
        sb.append(", acl=").append(acl);
        sb.append(", encryption=").append(encryption);
        sb.append(", storageClass='").append(storageClass).append('\'');
        sb.append(", modified=").append(modified);
        sb.append(", created=").append(created);
        sb.append(", parameters=").append(parameters);
        sb.append(", metadata=").append(metadata);
        sb.append(", lockId=").append(lockId);
        sb.append(", region=").append(region);
        sb.append(", part=").append(part);
        sb.append(", filekey=").append(filekey);
        sb.append(", canceled=").append(canceled);
        sb.append(", complete=").append(complete);
        sb.append(", done=").append(done);
        sb.append('}');
        return sb.toString();
    }

    /**
     * Rename of target filename *after* file transfer
     */
    public static final class Displayname {
        /**
         * Renamed upload target
         */
        public Path remote;
        /**
         * Target filename is temporary and file should be renamed to display name when transfer is complete
         */
        public Local local;
        /**
         * Target exists
         */
        public boolean exists;

        public Displayname withRemote(final Path remote) {
            this.remote = remote;
            return this;
        }

        public Displayname withLocal(final Local local) {
            this.local = local;
            return this;
        }

        public Displayname exists(final boolean exists) {
            this.exists = exists;
            return this;
        }

        @Override
        public String toString() {
            final StringBuilder sb = new StringBuilder("Displayname{");
            sb.append("remote=").append(remote);
            sb.append(", local=").append(local);
            sb.append('}');
            return sb.toString();
        }
    }

    /**
     * Rename of target filename *prior* file transfer
     */
    public static final class Rename {
        /**
         * Renamed upload target
         */
        public Path remote;
        /**
         * Renamed local target
         */
        public Local local;

        public Rename withRemote(final Path remote) {
            this.remote = remote;
            return this;
        }

        public Rename withLocal(final Local local) {
            this.local = local;
            return this;
        }

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
