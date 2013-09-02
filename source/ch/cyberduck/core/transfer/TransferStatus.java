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

import org.apache.log4j.Logger;

/**
 * The Status class is the model of a download's status.
 * The wrapper for any status informations of a transfer as the size and transferred
 * bytes.
 *
 * @version $Id$
 */
public final class TransferStatus {
    private static final Logger log = Logger.getLogger(TransferStatus.class);

    public static final long KILO = 1024; //2^10
    public static final long MEGA = 1048576; // 2^20
    public static final long GIGA = 1073741824; // 2^30

    /**
     * Target file or directory already exists
     */
    private boolean exists = false;

    /**
     * Append to file
     */
    private boolean append = false;

    /**
     * The number of transfered bytes. Must be less or equals size.
     */
    private long current = 0L;

    /**
     * Transfer size. May be less than the file size in attributes or 0 if creating symbolic links.
     */
    private long length = 0L;

    /**
     * The transfer has been canceled by the user.
     */
    private boolean canceled = false;

    /**
     * A state variable to mark this path if the path is explicitly selected
     * for inclusion in the transfer prompt
     */
    private boolean selected = true;

    public void setComplete() {
    }

    public boolean isComplete() {
        return current == length;
    }

    /**
     * If this path is currently transferred, interrupt it as soon as possible
     */
    public void setCanceled() {
        canceled = true;
    }

    /**
     * @return True if marked for interrupt
     */
    public boolean isCanceled() {
        return canceled;
    }

    /**
     * @return Number of bytes transferred
     */
    public long getCurrent() {
        return current;
    }

    /**
     * @param current The already transferred bytes
     */
    public void setCurrent(final long current) {
        this.current = current;
        if(log.isInfoEnabled()) {
            log.info(String.format("Transferred bytes set to %d bytes", current));
        }
    }

    public void addCurrent(final long transferred) {
        this.setCurrent(current + transferred);
    }

    public TransferStatus current(final long transferred) {
        this.current = transferred;
        return this;
    }

    public long getLength() {
        return length;
    }

    public void setLength(final long length) {
        this.length = length;
    }

    public TransferStatus length(final long length) {
        this.length = length;
        return this;
    }

    public TransferStatus selected(final boolean selected) {
        this.selected = selected;
        return this;
    }

    public boolean isSelected() {
        return selected;
    }

    /**
     * Mark for inclusion from transfer prompt
     *
     * @param selected True if selected
     */
    public void setSelected(final boolean selected) {
        this.selected = selected;
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

    /**
     * Mark this path with an append flag when transferred
     *
     * @param append If false, the current status is cleared
     * @see #setCurrent(long)
     */
    public void setAppend(final boolean append) {
        if(!append) {
            current = 0;
        }
        this.append = append;
    }

    public boolean isAppend() {
        return append;
    }

    public TransferStatus append(final boolean append) {
        this.append = append;
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
        if(canceled != that.canceled) {
            return false;
        }
        if(current != that.current) {
            return false;
        }
        if(length != that.length) {
            return false;
        }
        if(append != that.append) {
            return false;
        }
        if(selected != that.selected) {
            return false;
        }
        if(exists != that.exists) {
            return false;
        }
        return true;
    }

    @Override
    public int hashCode() {
        int result = (append ? 1 : 0);
        result = 31 * result + (int) (current ^ (current >>> 32));
        result = 31 * result + (int) (length ^ (length >>> 32));
        result = 31 * result + (canceled ? 1 : 0);
        result = 31 * result + (selected ? 1 : 0);
        result = 31 * result + (exists ? 1 : 0);
        return result;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder();
        sb.append("TransferStatus");
        sb.append("{append=").append(append);
        sb.append(", current=").append(current);
        sb.append(", length=").append(length);
        sb.append(", canceled=").append(canceled);
        sb.append(", selected=").append(selected);
        sb.append(", exists=").append(exists);
        sb.append('}');
        return sb.toString();
    }
}