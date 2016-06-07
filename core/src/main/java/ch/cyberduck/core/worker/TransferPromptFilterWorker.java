package ch.cyberduck.core.worker;

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
 * Bug fixes, suggestions and comments should be sent to:
 * feedback@cyberduck.ch
 */

import ch.cyberduck.core.AttributedList;
import ch.cyberduck.core.Cache;
import ch.cyberduck.core.LocaleFactory;
import ch.cyberduck.core.ProgressListener;
import ch.cyberduck.core.Session;
import ch.cyberduck.core.exception.BackgroundException;
import ch.cyberduck.core.exception.ConnectionCanceledException;
import ch.cyberduck.core.transfer.Transfer;
import ch.cyberduck.core.transfer.TransferAction;
import ch.cyberduck.core.transfer.TransferItem;
import ch.cyberduck.core.transfer.TransferPathFilter;
import ch.cyberduck.core.transfer.TransferStatus;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;

import java.text.MessageFormat;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public class TransferPromptFilterWorker extends Worker<Map<TransferItem, TransferStatus>> {
    private static final Logger log = Logger.getLogger(TransferPromptFilterWorker.class);

    private Transfer transfer;

    private TransferAction action;

    private Cache<TransferItem> cache;

    private ProgressListener listener;

    public TransferPromptFilterWorker(final Transfer transfer, final TransferAction action,
                                      final Cache<TransferItem> cache, final ProgressListener listener) {
        this.cache = cache;
        this.action = action;
        this.transfer = transfer;
        this.listener = listener;
    }

    @Override
    public Map<TransferItem, TransferStatus> run(final Session<?> session) throws BackgroundException {
        final Map<TransferItem, TransferStatus> status = new HashMap<TransferItem, TransferStatus>();
        for(TransferItem file : transfer.getRoots()) {
            if(this.isCanceled()) {
                throw new ConnectionCanceledException();
            }
            status.put(file.getParent(), new TransferStatus().exists(true));
        }
        final TransferPathFilter filter = transfer.filter(session, action, listener);
        if(log.isDebugEnabled()) {
            log.debug(String.format("Filter cache %s with filter %s", cache, filter));
        }
        for(TransferItem key : cache.keySet()) {
            if(this.isCanceled()) {
                throw new ConnectionCanceledException();
            }
            final AttributedList<TransferItem> list = cache.get(key);
            for(TransferItem file : list) {
                if(this.isCanceled()) {
                    throw new ConnectionCanceledException();
                }
                final boolean accept = filter.accept(file.remote, file.local, status.get(file.getParent()));
                status.put(file, filter.prepare(file.remote, file.local, status.get(file.getParent()))
                        .reject(!accept));
            }
        }
        return status;
    }

    @Override
    public String getActivity() {
        return MessageFormat.format(LocaleFactory.localizedString("Apply {0} filter", "Status"),
                StringUtils.uncapitalize(action.getTitle()));
    }

    @Override
    public Map<TransferItem, TransferStatus> initialize() {
        return Collections.emptyMap();
    }

    @Override
    public boolean equals(final Object o) {
        if(this == o) {
            return true;
        }
        if(o == null || getClass() != o.getClass()) {
            return false;
        }
        final TransferPromptFilterWorker that = (TransferPromptFilterWorker) o;
        if(cache != null ? !cache.equals(that.cache) : that.cache != null) {
            return false;
        }
        if(transfer != null ? !transfer.equals(that.transfer) : that.transfer != null) {
            return false;
        }
        return true;
    }

    @Override
    public int hashCode() {
        int result = transfer != null ? transfer.hashCode() : 0;
        result = 31 * result + (cache != null ? cache.hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("TransferPromptFilterWorker{");
        sb.append("transfer=").append(transfer);
        sb.append('}');
        return sb.toString();
    }
}
