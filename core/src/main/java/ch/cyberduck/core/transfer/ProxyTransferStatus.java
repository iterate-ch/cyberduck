package ch.cyberduck.core.transfer;

/*
 * Copyright (c) 2002-2023 iterate GmbH. All rights reserved.
 * https://cyberduck.io/
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 */

import ch.cyberduck.core.PathAttributes;
import ch.cyberduck.core.exception.BackgroundException;
import ch.cyberduck.core.exception.ConnectionCanceledException;
import ch.cyberduck.core.io.StreamCancelation;
import ch.cyberduck.core.io.StreamProgress;

public class ProxyTransferStatus extends TransferStatus implements StreamCancelation, StreamProgress {

    private final StreamCancelation cancel;
    private final StreamProgress progress;
    private final TransferResponse response;

    public ProxyTransferStatus(final TransferStatus proxy) {
        super(proxy);
        this.cancel = proxy;
        this.progress = proxy;
        this.response = proxy;
    }

    @Override
    public void validate() throws ConnectionCanceledException {
        cancel.validate();
    }

    @Override
    public void setComplete() {
        progress.setComplete();
    }

    @Override
    public void setFailure(final BackgroundException failure) {
        progress.setFailure(failure);
    }

    @Override
    public PathAttributes getResponse() {
        return response.getResponse();
    }

    @Override
    public void setResponse(final PathAttributes attributes) {
        response.setResponse(attributes);
    }
}
