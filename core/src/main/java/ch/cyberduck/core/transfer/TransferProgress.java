package ch.cyberduck.core.transfer;

/*
 * Copyright (c) 2002-2013 David Kocher. All rights reserved.
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

public final class TransferProgress {

    private final Long size;
    private final Long transferred;

    private final String progress;
    private final Double speed;

    public TransferProgress(final Long size, final Long transferred, final String progress, final Double speed) {
        this.size = size;
        this.transferred = transferred;
        this.progress = progress;
        this.speed = speed;
    }

    public Long getSize() {
        return size;
    }

    public Long getTransferred() {
        return transferred;
    }

    public String getProgress() {
        return progress;
    }

    public Double getSpeed() {
        return speed;
    }

    public boolean isComplete() {
        return transferred.longValue() == size.longValue();
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("TransferProgress{");
        sb.append("size=").append(size);
        sb.append(", transferred=").append(transferred);
        sb.append(", progress='").append(progress).append('\'');
        sb.append(", speed=").append(speed);
        sb.append('}');
        return sb.toString();
    }
}
