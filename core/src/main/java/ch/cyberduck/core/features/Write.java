package ch.cyberduck.core.features;

/*
 * Copyright (c) 2002-2016 iterate GmbH. All rights reserved.
 * https://cyberduck.io/
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
 */

import ch.cyberduck.core.ConnectionCallback;
import ch.cyberduck.core.LocaleFactory;
import ch.cyberduck.core.Path;
import ch.cyberduck.core.exception.AccessDeniedException;
import ch.cyberduck.core.exception.BackgroundException;
import ch.cyberduck.core.io.Checksum;
import ch.cyberduck.core.io.ChecksumCompute;
import ch.cyberduck.core.io.DisabledChecksumCompute;
import ch.cyberduck.core.io.StatusOutputStream;
import ch.cyberduck.core.transfer.TransferStatus;

import java.text.MessageFormat;
import java.util.EnumSet;
import java.util.Objects;

@Required
public interface Write<Reply> {

    /**
     * @param status   Transfer status
     * @param callback Prompt
     * @return Stream to write to for upload
     * @see AttributesAdapter#toAttributes(Reply)
     */
    StatusOutputStream<Reply> write(Path file, TransferStatus status, final ConnectionCallback callback) throws BackgroundException;

    /**
     * @return True if supporting random writes with arbitrary offset and length
     */
    default boolean random(Path file) {
        if(this.features(file).contains(Flags.random)) {
            return true;
        }
        return false;
    }

    /**
     * @return True if supporting to set timestamp on upload
     */
    default boolean timestamp(Path file) {
        if(this.features(file).contains(Flags.timestamp)) {
            return true;
        }
        return false;
    }

    default ChecksumCompute checksum(final Path file, final TransferStatus status) {
        return new DisabledChecksumCompute();
    }

    final class Append {
        /**
         * Append to existing file is supported
         */
        public final boolean append;

        /**
         * Remote file size
         */
        public Long offset = 0L;

        /**
         * Remote file checksum
         */
        public Checksum checksum = Checksum.NONE;

        public Append(final boolean append) {
            this.append = append;
        }

        public Append withChecksum(final Checksum checksum) {
            this.checksum = checksum;
            return this;
        }

        public Append withOffset(final Long offset) {
            this.offset = offset;
            return this;
        }

        public Append withStatus(final TransferStatus status) {
            return this.withOffset(TransferStatus.UNKNOWN_LENGTH == status.getRemote().getSize() ? 0L : status.getRemote().getSize()).withChecksum(status.getRemote().getChecksum());
        }

        @Override
        public boolean equals(final Object o) {
            if(this == o) {
                return true;
            }
            if(!(o instanceof Append)) {
                return false;
            }
            final Append append1 = (Append) o;
            return append == append1.append && Objects.equals(offset, append1.offset) && Objects.equals(checksum, append1.checksum);
        }

        @Override
        public int hashCode() {
            return Objects.hash(append, offset, checksum);
        }

        @Override
        public String toString() {
            final StringBuilder sb = new StringBuilder("Append{");
            sb.append("size=").append(offset);
            sb.append(", checksum=").append(checksum);
            sb.append('}');
            return sb.toString();
        }
    }

    /**
     * Existing remote file found
     */
    Append override = new Append(false);

    default void preflight(final Path file) throws BackgroundException {
        if(!file.attributes().getPermission().isWritable()) {
            throw new AccessDeniedException(MessageFormat.format(LocaleFactory.localizedString("Upload {0} failed", "Error"),
                    file.getName())).withFile(file);
        }
        final Path workdir = file.getParent();
        if(!workdir.attributes().getPermission().isWritable()) {
            throw new AccessDeniedException(MessageFormat.format(LocaleFactory.localizedString("Upload {0} failed", "Error"),
                    file.getName())).withFile(workdir);
        }
    }

    /**
     * @return Supported features
     */
    default EnumSet<Flags> features(Path file) {
        return EnumSet.noneOf(Flags.class);
    }

    /**
     * Feature flags
     */
    enum Flags {
        /**
         * Support setting modification date on upload
         */
        timestamp,
        /**
         * Random writes with arbitrary offset and length
         */
        random,
        /**
         * Set permissions on upload
         */
        permission,
        /**
         * Set ACL on upload
         */
        acl,
        /**
         * Setting checksum on upload to allow verification on server
         */
        checksum,
        /**
         * Set MIME type on upload
         */
        mime
    }
}