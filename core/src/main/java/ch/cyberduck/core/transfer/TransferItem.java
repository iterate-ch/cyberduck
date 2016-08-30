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

import ch.cyberduck.core.Local;
import ch.cyberduck.core.Path;
import ch.cyberduck.core.Referenceable;
import ch.cyberduck.core.Serializable;
import ch.cyberduck.core.serializer.Serializer;

public class TransferItem implements Referenceable, Serializable {

    public Path remote;
    public Local local;

    public TransferItem(final Path remote) {
        this(remote, null);
    }

    public TransferItem(final Path remote, final Local local) {
        this.remote = remote;
        this.local = local;
    }

    public TransferItem getParent() {
        return new TransferItem(remote.getParent(), null == local ? null : local.getParent());
    }

    @Override
    public <T> T serialize(final Serializer dict) {
        dict.setObjectForKey(remote, "Remote");
        if(local != null) {
            dict.setObjectForKey(local, "Local Dictionary");
        }
        return dict.getSerialized();
    }

    public void setLocal(Local local) {
        this.local = local;
    }

    public void setRemote(Path remote) {
        this.remote = remote;
    }

    @Override
    public boolean equals(Object o) {
        if(this == o) {
            return true;
        }
        if(o == null || getClass() != o.getClass()) {
            return false;
        }
        TransferItem that = (TransferItem) o;
        if(local != null ? !local.equals(that.local) : that.local != null) {
            return false;
        }
        if(remote != null ? !remote.equals(that.remote) : that.remote != null) {
            return false;
        }
        return true;
    }

    @Override
    public int hashCode() {
        int result = remote != null ? remote.hashCode() : 0;
        result = 31 * result + (local != null ? local.hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("TransferItem{");
        sb.append("local=").append(local);
        sb.append(", remote=").append(remote);
        sb.append('}');
        return sb.toString();
    }
}
